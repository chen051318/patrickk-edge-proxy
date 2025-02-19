package consumer.binlog.node;

import com.x.assembly.data.watch.client.domain.DataBaseFieldDO;
import com.x.kunlun.biz.utils.ConvertUtils;
import com.x.kunlun.common.Constant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ：blackknight
 * @version: 1.0$
 * @description: 过滤apollo配置的数据黑名单, 黑名单内的数据不处理，格式:
 * 表名.字段名.值,表名.字段名.值,表名.字段名.值
 * @since ：2021/8/12 12:30 下午
 */
@Component
public class KafkaBinLogTableAndIdBlackFilterNode extends KafkaBinLogSupperNode {

    private static final Logger logger = LoggerFactory.getLogger(KafkaBinLogTableAndIdBlackFilterNode.class);

    @Value("${kafka.binlog.blackfilter:}")
    private String kafkaBinLogBlackFilter;

    /**
     * 过滤黑名单：
     * Map<tableName, Map<column, Set<Value>>>
     */
    private static Map<String, Map<String, Set<String>>> kafkaBinLogBlackFilterMap = new HashMap<>();

    /**
     * 初始化过滤分词
     */
    @PostConstruct
    private void init() {
        if (StringUtils.isBlank(kafkaBinLogBlackFilter)) {
            //没配置黑名单，不过滤
            return;
        }
        if (kafkaBinLogBlackFilter.startsWith(Constant.LEFT_SQUARE_BRACKET)) {
            //去掉第一个数组起始符
            kafkaBinLogBlackFilter = kafkaBinLogBlackFilter.substring(1);
        }
        if (kafkaBinLogBlackFilter.endsWith(Constant.RIGHT_SQUARE_BRACKET)) {
            //去掉最后一个数组结束符
            kafkaBinLogBlackFilter = kafkaBinLogBlackFilter.substring(0, kafkaBinLogBlackFilter.length() - 1);
        }
        String[] filterList = kafkaBinLogBlackFilter.split(Constant.COMMA);
        if (filterList.length == 0) {
            //没有有效的匹配配置
            return;
        }
        for (String filter : filterList) {
            if (StringUtils.isBlank(filter)) {
                continue;
            }
            String[] filterValueList = filter.split("\\" + Constant.DOT);
            if (filterValueList.length != 3) {
                //配置错误，不作为过滤条件
                continue;
            }
            String tableName = filterValueList[0];
            String column = filterValueList[1];
            String value = filterValueList[2];
            Map<String, Set<String>> tableFilterMap = kafkaBinLogBlackFilterMap.computeIfAbsent(tableName, k -> new HashMap<>(1));
            Set<String> columnFilterSet = tableFilterMap.computeIfAbsent(column, k -> new HashSet<>(1));
            columnFilterSet.add(value);
            tableFilterMap.put(tableName, columnFilterSet);
            kafkaBinLogBlackFilterMap.put(tableName, tableFilterMap);
        }
    }

    @Override
    public boolean doHandler(String topic, Map<String, DataBaseFieldDO> data, String tableName, String eventType, int partition,
                             long offset, String key, Long eventTime) {
        if (data == null || data.isEmpty()) {
            //没数据，不处理
            return true;
        }
        if (kafkaBinLogBlackFilterMap.get(tableName) == null
                || (kafkaBinLogBlackFilterMap.get(tableName)).isEmpty()) {
            //对应表没过滤器，不处理
            return true;
        }
        Map<String, Set<String>> tableFilterMap = kafkaBinLogBlackFilterMap.get(tableName);
        for (Map.Entry<String, Set<String>> entry : tableFilterMap.entrySet()) {
            String column = entry.getKey();
            DataBaseFieldDO fieldDO = data.get(column);
            if (fieldDO == null) {
                //没有监听这个字段，不过滤
                continue;
            }
            String value = ConvertUtils.getString(fieldDO.getValue());
            if (entry.getValue().contains(value)) {
                //如果在黑名单中，说明需要拦截，不再后续处理
                return false;
            }
        }
        //不在黑名单，放行
        return true;
    }
}
