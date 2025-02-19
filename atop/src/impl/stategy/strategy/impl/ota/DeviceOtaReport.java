package impl.stategy.strategy.impl.ota;

import com.alibaba.fastjson.JSON;
import com.x.atop.client.domain.api.ApiRequestDO;
import com.x.edgegateway.atop.device.impl.strategy.IDeviceReport;
import com.x.edgegateway.atop.device.impl.strategy.annotation.EdgeStrategy;
import com.x.edgegateway.common.model.DeviceStrategy;
import com.x.edgegateway.manager.cmdissue.domain.IssueDataCmdResultDTO;
import com.x.edgegateway.shared.service.cmdissue.ICmdIssueSharedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author patrickkk
 * @date 2020/6/8
 */
@EdgeStrategy(tag = DeviceStrategy.Ota.UPGRADE_TAG)
@Service
@Slf4j
public class DeviceOtaReport implements IDeviceReport {

    @Resource
    private ICmdIssueSharedService cmdIssueSharedService;

    @Override
    public void report(ApiRequestDO apiRequestDO, String data) {
        log.info("DeviceOtaReport, report: {}", data);
        DeviceOtaRequest deviceOtaRequest = JSON.parseObject(data, DeviceOtaRequest.class);

        // 更新cmd指令执行结果
        IssueDataCmdResultDTO issueDataCmdResultDTO = new IssueDataCmdResultDTO();
        issueDataCmdResultDTO.setSn(deviceOtaRequest.getSn());
        int success = 0;
        String message = "升级成功";
        if (deviceOtaRequest.getStatus() == 3) {
            success = 1;
        } else if (deviceOtaRequest.getStatus() == 4) {
            message = "升级失败";
        } else {
            log.info("DeviceOtaReport, report.notprocess:{}", success);
            return;
        }
        issueDataCmdResultDTO.setSuccess(success);
        issueDataCmdResultDTO.setMessage(message);
        cmdIssueSharedService.dealCmdIssueResult(issueDataCmdResultDTO);
    }
}
