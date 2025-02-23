package impl.stategy.strategy.impl.parking;

import com.alibaba.fastjson.JSON;
import com.x.atop.client.domain.api.ApiRequestDO;
import com.x.edgegateway.atop.device.impl.strategy.annotation.EdgeStrategy;
import com.x.edgegateway.atop.device.impl.strategy.impl.AbstractReportProcessor;
import com.x.edgegateway.client.domain.cmd.constants.CmdBizTypeEnum;
import com.x.edgegateway.core.util.sensitivelog.LogMarkers;
import com.x.edgegateway.manager.base.utils.BeanPropertyCopyUtils;
import com.x.edgegateway.manager.device.IGatewayManager;
import com.x.edgegateway.manager.device.domain.GatewayDTO;
import com.x.edgegateway.manager.pa.domain.CurrentCarDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author : patrickkk
 * @date 2021-07-02
 */
@EdgeStrategy(tag = "currentCarQuery" + "response")
@Service
@Slf4j
public class CurrentCarQueryReport extends AbstractReportProcessor {
    @Autowired
    private IGatewayManager gatewayManager;

    @Override
    public void report(ApiRequestDO apiRequestDO, String data) {
        log.info(LogMarkers.SENSITIVE, "CurrentCarQueryReport, data is {}", data);

        ParkingCurrentCarRequest request = JSON.parseObject(data, ParkingCurrentCarRequest.class);
        Assert.notNull(request.getSn(), "sn must not null");
        Assert.hasText(request.getPlateNo(), "plate no must not null");
        Assert.notNull(request.getCaptureTime(), "capture time must not null");

        CurrentCarDTO dto = new CurrentCarDTO();
        BeanPropertyCopyUtils.copy(request, dto);
        dto.setProjectId(checkAndGetGateway(apiRequestDO.getGwId()).getProjectId());
        dto.setSn(request.getSn().toString());
        dto.setGatewayId(apiRequestDO.getGwId());

        onReceivedCmdResult(request.getSn(), 1, "", JSON.toJSONString(dto), CmdBizTypeEnum.PARKING_RESULT);
    }

    private GatewayDTO checkAndGetGateway(String gatewayId) {
        Assert.hasText(gatewayId, "gatewayId不能为空！");
        GatewayDTO gatewayDTO = gatewayManager.queryByGatewayId(gatewayId);
        Assert.notNull(gatewayDTO, "网关[" + gatewayId + "]不存在");
        return gatewayDTO;
    }
}
