package consumer.binlog.node;

import com.tuya.assembly.data.watch.client.domain.DataBaseFieldDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author ：patrickkk
 * @version: 1.0$
 * @since ：2021/3/29 12:30 下午
 */
public class KafkaBinLogTableNameCheckNode extends KafkaBinLogSupperNode {

    private static final Logger logger = LoggerFactory.getLogger(KafkaBinLogTableNameCheckNode.class);

    private String tableName;

    public KafkaBinLogTableNameCheckNode(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public boolean doHandler(String topic, Map<String, DataBaseFieldDO> data, String tableName, String eventType, int partition,
                             long offset, String key, Long eventTime) {
        if (this.tableName.equals(tableName)) {
            return true;
        }
        logger.info("表名不一致, current tableName[{}], dataTableName[{}]", this.tableName, tableName);
        return false;
    }
}
