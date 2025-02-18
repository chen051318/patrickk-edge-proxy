package consumer.binlog.node;

import com.tuya.assembly.data.watch.client.domain.DataBaseFieldDO;

import java.util.Map;

/**
 * kafka binlog处理节点抽象类
 *
 * @author ：patrickkk
 * @since ：2021/3/29 11:31 上午
 */
public abstract class KafkaBinLogSupperNode {

    /**
     * 节点处理逻辑，需要幂等的接口自己处理幂等
     *
     * @param topic
     * @param data
     * @param tableName
     * @param eventType
     * @param partition
     * @param offset
     * @param key
     * @param eventTime
     * @return
     */
    public abstract boolean doHandler(String topic, Map<String, DataBaseFieldDO> data, String tableName, String eventType,
                                      int partition, long offset, String key, Long eventTime);

}
