package consumer.binlog.node;

import com.x.assembly.data.watch.client.domain.DataBaseFieldDO;
import com.x.kunlun.biz.chain.kafka.binlog.annotation.KafkaBinLogTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author ：patrickkk
 * @version: 1.0$
 * @since ：2021/3/29 11:34 上午
 */
@KafkaBinLogTable(tableName = "test", event = {"INSERT", "UPDATE"}, sort = 2)
@Component
public class KafkaBinLogTest2Node extends KafkaBinLogSupperNode {
    public static final Logger logger = LoggerFactory.getLogger(KafkaBinLogTest2Node.class);
    @Override
    public boolean doHandler(String topic, Map<String, DataBaseFieldDO> data, String tableName, String eventType, int partition,
                             long offset, String key, Long eventTime) {
        logger.info("call KafkaBinLogTes2tNode.doHandler");
        return true;
    }
}
