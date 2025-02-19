package consumer.binlog;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.x.assembly.data.watch.client.domain.DataBaseFieldDO;
import com.x.assembly.data.watch.client.domain.DataBaseWatchDO;
import com.x.basic.mq.domain.KafkaMqData;
import com.x.kunlun.biz.chain.kafka.binlog.annotation.KafkaBinLogTable;
import com.x.kunlun.biz.chain.kafka.binlog.node.KafkaBinLogEnvCheckNode;
import com.x.kunlun.biz.chain.kafka.binlog.node.KafkaBinLogSupperNode;
import com.x.kunlun.biz.chain.kafka.binlog.node.KafkaBinLogTableAndIdBlackFilterNode;
import com.x.kunlun.biz.listener.YugongUtils;
import com.x.kunlun.biz.listener.domain.DataBaseEventEnums;
import com.x.quexie.trace.TraceIdUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * kafka binlog 消费责任链
 *
 * @author ：patrickkk
 * @since ：2021/3/29 11:29 上午
 */
@Component
public class KafkaBinLogConsumerChain {

    private static final Logger logger = LoggerFactory.getLogger(KafkaBinLogConsumerChain.class);

    private static Map<String, List<KafkaBinLogSupperNode>> binLogNodeMap = Maps.newHashMap();

    @Resource
    private ApplicationContext applicationContext;

    public void addNode(KafkaBinLogSupperNode node) {
        //获取表名
        KafkaBinLogTable kafkaBinLogTable = node.getClass().getAnnotation(KafkaBinLogTable.class);
        //使用浅拷贝，进行值添加
        List<KafkaBinLogSupperNode> tableBinLogNodeList =
                binLogNodeMap.computeIfAbsent(kafkaBinLogTable.tableName(), k -> Lists.newArrayList());
        tableBinLogNodeList.add(node);
    }

    public boolean doHandler(String topic, KafkaMqData<DataBaseWatchDO> data, int partition, long offset, String key) {
        if (StringUtils.isEmpty(TraceIdUtil.getTraceId())) {
            TraceIdUtil.refreshTraceId("KafkaBinLogConsumerChain_" + topic);
        }
        String tableName = data.getData().getTableName();
        List<KafkaBinLogSupperNode> nodeList = binLogNodeMap.get(tableName);
        if (CollectionUtils.isEmpty(nodeList)) {
            logger.info("跳过表【{}】的binlog消费", tableName);
            return true;
        }
        Map<String, DataBaseFieldDO> mapData = YugongUtils.covert2Map(data.getData());
        //通用node处理--------
        //1. env校验，如果不是当前环境的消息，不处理
        boolean isContinueHandle = applicationContext.getBean(KafkaBinLogEnvCheckNode.class)
                .doHandler(topic, mapData, tableName, data.getData().getEventType(), partition, offset, key, data.getCt());
        if (!isContinueHandle) {
            //预发打印日志太多， 环境不一致的不打印
//            logger.info("环境不一致，当前表不处理");
            return true;
        }
        //2. 黑名单过滤，如果表中数据id在黑名单中，不处理
        isContinueHandle = applicationContext.getBean(KafkaBinLogTableAndIdBlackFilterNode.class)
                .doHandler(topic, mapData, tableName, data.getData().getEventType(), partition, offset, key, data.getCt());
        if (!isContinueHandle) {
            logger.info("当前数据在黑名单，不处理");
            return true;
        }

        //业务node处理--------
        //返回kafka是否消费，true：已消费，不重发；false：未消费/有消费失败，要重发；
        boolean isConsumed = true;
        for (KafkaBinLogSupperNode kafkaBinLogSupperNode : nodeList) {
            //校验该节点是否满足触发条件
            KafkaBinLogTable kafkaBinLogTable = kafkaBinLogSupperNode.getClass().getAnnotation(KafkaBinLogTable.class);
            boolean checkFlag = check(kafkaBinLogTable, topic, data, tableName, partition, offset, key);
            if (!checkFlag) {
                //可能存在同一个binlog， update，delete， insert使用不同的策略进行处理
                //binlog 日志太多
//                logger.info("[{}]触发校验未通过,data[{}]", JsonUtilsV2.toJson(kafkaBinLogTable), JsonUtilsV2.toJson(data));
            } else {
                //责任链消费，即使中间有失败的，也会跑完整个责任链，要求每个责任链里面有幂等校验
                try {
                    isConsumed = isConsumed && kafkaBinLogSupperNode
                            .doHandler(topic, mapData, tableName, data.getData().getEventType(), partition, offset, key, data.getCt());
                } catch (Exception e) {
                    //责任链有一个消费异常了，就要求kafka重试
                    logger.error(e.getMessage(), e);
                    isConsumed = false;
                }
            }
        }
        return isConsumed;
    }

    private boolean check(KafkaBinLogTable kafkaBinLogTable, String topic, KafkaMqData<DataBaseWatchDO> data, String tableName, int partition, long offset, String key) {

        // 公共处理节点（例如tableName，env校验的节点是没有KafkaBinLogTable注解的，因此不用拦截）
        if (kafkaBinLogTable == null) {
            return true;
        }

        Set<String> eventSet = Arrays.stream(kafkaBinLogTable.event()).collect(Collectors.toSet());
        // binlog监听没有select
        if (eventSet.contains(DataBaseEventEnums.MYSQL_TABLE_INSERT) &&
                DataBaseEventEnums.MYSQL_TABLE_INSERT.equals(data.getData().getEventType())) {
            //插入操作，不做校验通过
            return true;
        }

        if (eventSet.contains(DataBaseEventEnums.MYSQL_TABLE_DELETE) &&
                DataBaseEventEnums.MYSQL_TABLE_DELETE.equals(data.getData().getEventType())) {
            //插入操作，不做校验通过
            return true;
        }

        if (eventSet.contains(DataBaseEventEnums.MYSQL_TABLE_UPDATE) &&
                DataBaseEventEnums.MYSQL_TABLE_UPDATE.equals(data.getData().getEventType())) {
            Set<String> fieldSet = Arrays.stream(kafkaBinLogTable.fields()).collect(Collectors.toSet());
            //参数不填写，表示所有数据更新，都触发
            if (kafkaBinLogTable.fields().length == 1 && "".equals(kafkaBinLogTable.fields()[0])) {
                return true;
            }
            for (DataBaseFieldDO dataBaseFieldDO : data.getData().getFields()) {
                //字段更新了，且在我们监听范围
                if (dataBaseFieldDO.getUpdate() != null && dataBaseFieldDO.getUpdate() && fieldSet.contains(dataBaseFieldDO.getField())) {
                    return true;
                }
            }
            return false;
        }
        //兜底
        return false;
    }

    /**
     * 初始化kafka消费的处理类
     */
    @PostConstruct
    public void loadKafkaBinLogNode() {
        Map<String, Object> binLogNodeMap = applicationContext.getBeansWithAnnotation(KafkaBinLogTable.class);
        List<KafkaBinLogSupperNode> nodeList = binLogNodeMap.values().stream()
                .filter(node -> node instanceof KafkaBinLogSupperNode)
                .map(node -> (KafkaBinLogSupperNode) node)
                .sorted((node1, node2) -> {
                    KafkaBinLogTable kafkaBinLogTable1 = node1.getClass().getAnnotation(KafkaBinLogTable.class);
                    KafkaBinLogTable kafkaBinLogTable2 = node2.getClass().getAnnotation(KafkaBinLogTable.class);
                    if (kafkaBinLogTable1.sort() > kafkaBinLogTable1.sort()) {
                        return 1;
                    } else if (kafkaBinLogTable1.sort() == kafkaBinLogTable2.sort()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }).collect(Collectors.toList());
        nodeList.forEach(node -> {
            addNode(node);
        });
    }

}
