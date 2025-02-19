package impl.stategy.strategy.impl.intrusion;

import com.alibaba.fastjson.JSONArray;
import com.x.atop.client.domain.api.ApiRequestDO;
import com.x.edgegateway.atop.device.impl.strategy.IDeviceReport;
import com.x.edgegateway.atop.device.impl.strategy.annotation.EdgeStrategy;
import com.x.edgegateway.common.model.DeviceStrategy;
import com.x.edgegateway.manager.kafka.domain.KafkaDeviceUploadStringMessage;
import com.x.edgegateway.manager.kafka.producer.IKafkaIntrusionMsgProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @author patrickkk
 * @date 2020/9/12 4:14 下午
 */
@EdgeStrategy(tag = DeviceStrategy.Intrusion.RELATION_TAG)
@Service
@Slf4j
public class IntrusionRelationReport implements IDeviceReport {

    @Resource
    private IKafkaIntrusionMsgProducer kafkaIntrusionMsgProducer;

    @Override
    public void report(ApiRequestDO apiRequestDO, String data) {
        log.info("IntrusionRelationReport input: {}", data);
        Assert.notNull(data, "博世周届设备拓扑关系不能为空");
        JSONArray array = JSONArray.parseArray(data);
        array.stream().iterator().forEachRemaining(d -> {
            KafkaDeviceUploadStringMessage stringMessage = new KafkaDeviceUploadStringMessage();
            stringMessage.setData(d.toString());
            kafkaIntrusionMsgProducer.pushIntrusionMessage(stringMessage);
        });
    }
}
