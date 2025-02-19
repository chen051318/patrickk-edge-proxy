package consumer;

import com.google.common.collect.Sets;
import consumer.binlog.KafkaBinlogConsumerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Set;

/**
 * kunlun_device同步到es binlog的入口
 *
 * @author ：patrickkk
 * @version: 1.0$
 * @since ：2021/6/22 5:42 下午
 */
public class DeviceBinlogConsumer extends AbstractKafkaConsumer<DataBaseWatchDO> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceBinlogConsumer.class);

    @Resource
    private KafkaBinlogConsumerFactory kafkaBinlogConsumerFactory;

    @Resource
    private DeviceEsWriteManager deviceEsWriteManager;

    /**
     * @see IDeviceOpManage#onlineDevice(String, String)
     */
    private static final Set<String> deviceOnlineUpdateFields = Sets.newHashSet("is_online", "gmt_modified");

    @Override
    protected boolean consume(String topic, KafkaMqData<DataBaseWatchDO> data, int partition, long offset, String key) throws Exception {
        //todo 日志输出文本过长，对性能可能存在影响
        //设备上下线事件 ， 不打印日志， 减少
        if (!isDeviceOnlineUpdateEvent(data)) {
            logger.info("DeviceBinlogConsumer consume, data:{}", JsonUtilsV2.toJson(data));
        }
        return kafkaBinlogConsumerFactory.doHandler(topic, data, partition, offset, key);
    }

    private static boolean isDeviceOnlineUpdateEvent(KafkaMqData<DataBaseWatchDO> data) {
        if (DataBaseEventEnums.UPDATE.getType().equals(data.getData().getEventType())) {
            //设备上下线只会更新 online
            return data.getData().getFields().stream().filter(DataBaseFieldDO::getUpdate)
                    .allMatch(t -> deviceOnlineUpdateFields.contains(t.getField()));
        }

        return false;
    }

}
