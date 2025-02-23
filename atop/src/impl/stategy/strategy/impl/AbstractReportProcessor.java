package impl.stategy.strategy.impl;

import com.x.edgegateway.atop.device.impl.strategy.IDeviceReport;
import com.x.edgegateway.client.domain.cmd.constants.CmdBizTypeEnum;
import com.x.edgegateway.core.util.sensitivelog.LogMarkers;
import com.x.edgegateway.manager.cmdissue.domain.IssueDataCmdResultDTO;
import com.x.edgegateway.shared.service.cmdissue.ICmdIssueSharedService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author : patrickkk
 * @date 2021-07-02
 */
@Slf4j
public abstract class AbstractReportProcessor implements IDeviceReport {
    @Resource
    private ICmdIssueSharedService cmdIssueSharedService;


    public void onReceivedCmdResult(Long sn, Integer success, String message, String data, CmdBizTypeEnum cmdBizType) {
        log.info(LogMarkers.SENSITIVE, "[onReceivedCmdResult] sn:{}, data:{}", sn, data);
        IssueDataCmdResultDTO issueDataCmdResultDTO = new IssueDataCmdResultDTO();
        issueDataCmdResultDTO.setSn(sn);
        issueDataCmdResultDTO.setSuccess(success);
        issueDataCmdResultDTO.setMessage(message);
        issueDataCmdResultDTO.setData(data);
        issueDataCmdResultDTO.setCmdBizType(Objects.isNull(cmdBizType) ? null : cmdBizType.getCode());

        cmdIssueSharedService.dealCmdIssueResult(issueDataCmdResultDTO);
    }
}
