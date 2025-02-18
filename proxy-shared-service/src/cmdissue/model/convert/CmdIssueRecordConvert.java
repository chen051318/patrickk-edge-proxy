package cmdissue.model.convert;

import com.tuya.edgegateway.client.domain.cmd.IssueCmdResultVO;
import com.tuya.edgegateway.client.domain.ndp.common.ICmd;
import com.tuya.edgegateway.client.domain.ndp.common.IssueDataCmd;
import com.tuya.edgegateway.core.cmdissue.domain.CmdIssueRecordDO;
import com.tuya.edgegateway.manager.base.utils.RetryUtils;
import com.tuya.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;
import com.tuya.edgegateway.manager.device.domain.DeviceDTO;

/**
 * @author patrickkk  2020/8/11 14:24
 */
public class CmdIssueRecordConvert {

    public static CmdIssueRecordDTO convert2CmdIssueRecord(String uidForIOT, DeviceDTO deviceDTO, ICmd
            cmd, int intervalFactor) {
        CmdIssueRecordDTO cmdIssueRecordDTO = new CmdIssueRecordDTO();
        cmdIssueRecordDTO.setUid(uidForIOT);
        cmdIssueRecordDTO.setDeviceId(deviceDTO == null ? null : deviceDTO.getDeviceId());

        if (cmd.getGateway()) {
            cmdIssueRecordDTO.setGatewayFlag(CmdIssueRecordDO.GatewayFlag_Yes);
        } else {
            cmdIssueRecordDTO.setGatewayFlag(CmdIssueRecordDO.GatewayFlag_No);
        }

        if (cmd instanceof IssueDataCmd || cmd.getRetryEnable() == true) {
            cmdIssueRecordDTO.setDataFlag(CmdIssueRecordDO.DataFlag_DataCmd);
        } else {
            cmdIssueRecordDTO.setDataFlag(CmdIssueRecordDO.DataFlag_ControlCmd);
        }

        cmdIssueRecordDTO.setProductType(deviceDTO == null ? null : deviceDTO.getProductType());
        cmdIssueRecordDTO.setProductId(deviceDTO == null ? null : deviceDTO.getProductId());
        cmdIssueRecordDTO.setSupplierCode(deviceDTO == null ? null : deviceDTO.getDeviceProviderCode());
        cmdIssueRecordDTO.setDpid(cmd.getDpid().toString());
        cmdIssueRecordDTO.setSn(cmd.getSn());
        cmdIssueRecordDTO.setBsn(cmd.getBsn());
        cmdIssueRecordDTO.setPreSn(cmd.getPreSn());
        cmdIssueRecordDTO.setProjectId(deviceDTO == null ? null : deviceDTO.getProjectId());
        cmdIssueRecordDTO.setGatewayId(deviceDTO == null ? null : deviceDTO.getGatewayId());
        cmdIssueRecordDTO.setCmdVersion(cmd.getCmdVersion());
        cmdIssueRecordDTO.setData(cmd.toJsonDP());

        //设置重试次数和时间
        cmdIssueRecordDTO.setRetryCount(0);
        cmdIssueRecordDTO.setStatus(1);
        cmdIssueRecordDTO.setNextProcessTime(deviceDTO == null ? null : RetryUtils.calcNextProcessTime(System.currentTimeMillis(), 0, intervalFactor));
        cmdIssueRecordDTO.setGmtCreate(System.currentTimeMillis());
        cmdIssueRecordDTO.setGmtModified(System.currentTimeMillis());

        return cmdIssueRecordDTO;
    }

    public static IssueCmdResultVO convert2CmdResultVO(CmdIssueRecordDTO cmdIssueDTO, int processStatus) {
        if (cmdIssueDTO == null) {
            return null;
        }

        IssueCmdResultVO issueCmdResultVO = new IssueCmdResultVO();
        issueCmdResultVO.setDeviceId(cmdIssueDTO.getDeviceId());
        issueCmdResultVO.setSn(cmdIssueDTO.getSn());
        issueCmdResultVO.setBsn(cmdIssueDTO.getBsn());
        issueCmdResultVO.setRetryCount(cmdIssueDTO.getRetryCount());
        issueCmdResultVO.setNextProcessTime(cmdIssueDTO.getNextProcessTime());
        issueCmdResultVO.setSuccess(processStatus == 4 ? 1 : 0);
        issueCmdResultVO.setProcessStatus(processStatus);
        issueCmdResultVO.setMessage(cmdIssueDTO.getMsg());
        return issueCmdResultVO;
    }
}
