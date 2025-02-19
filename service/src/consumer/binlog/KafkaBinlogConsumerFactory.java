package consumer.binlog;

import com.x.assembly.data.watch.client.domain.DataBaseWatchDO;
import com.x.basic.mq.domain.KafkaMqData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * binlog处理的工厂类
 *
 * @author ：patrickkk
 * @since ：2021/3/29 11:39 上午
 */
@Component
public class KafkaBinlogConsumerFactory {

    private static final Logger logger = LoggerFactory.getLogger(KafkaBinlogConsumerFactory.class);

    @Resource
    private KafkaBinLogConsumerChain chain;

    public boolean doHandler(String topic, KafkaMqData<DataBaseWatchDO> data, int partition, long offset, String key) {
        return chain.doHandler(topic, data, partition, offset, key);
    }

}
