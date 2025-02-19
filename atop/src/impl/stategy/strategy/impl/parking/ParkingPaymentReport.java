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
import com.x.edgegateway.manager.pa.domain.PaymentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author : patrickkk
 * @date 2021-07-02
 */
@EdgeStrategy(tag = "paymentGenerate" + "response")
@Service
@Slf4j
public class ParkingPaymentReport extends AbstractReportProcessor {
    @Autowired
    private IGatewayManager gatewayManager;

    @Override
    public void report(ApiRequestDO apiRequestDO, String data) {
        log.info(LogMarkers.SENSITIVE, "ParkingPaymentReport, data is {}", data);
        ParkingPaymentRequest request = JSON.parseObject(data, ParkingPaymentRequest.class);
        Assert.notNull(request.getSn(), "sn must not null");
        Assert.hasText(request.getOrderNo(), "order no must not null");
        Assert.hasText(request.getPlateNo(), "plate no must not null");

        PaymentDTO dto = new PaymentDTO();
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
