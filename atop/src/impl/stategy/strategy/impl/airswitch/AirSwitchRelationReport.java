package impl.stategy.strategy.impl.airswitch;

import com.x.atop.client.domain.api.ApiRequestDO;
import com.x.edgegateway.atop.device.impl.strategy.IDeviceReport;
import com.x.edgegateway.atop.device.impl.strategy.annotation.EdgeStrategy;
import com.x.edgegateway.common.model.DeviceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author patrickkk
 * @date 2020/12/16 11:39 上午
 */
@EdgeStrategy(tag = DeviceStrategy.AirSwitch.LINE_TAG)
@Service
@Slf4j
public class AirSwitchRelationReport implements IDeviceReport {
    @Override
    public void report(ApiRequestDO apiRequestDO, String data) {
        log.info("AirSwitchRelationReport. report input{}", data);
        Assert.notNull(data, "空气开关设备拓扑关系不能为空");
        //todo 解析拓扑关系
    }
}
