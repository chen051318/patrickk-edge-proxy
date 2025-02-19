package impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.x.atop.client.domain.api.ApiRequestDO;
import com.x.atop.client.domain.common.AtopResult;
import com.x.edgegateway.atop.device.ICmdIssueAtopService;
import com.x.edgegateway.client.domain.cmd.IssueCmdResultVO;
import com.x.edgegateway.client.domain.cmd.IssueDataCmdInfoVO;
import com.x.edgegateway.client.domain.cmd.constants.CmdPriorityEnum;
import com.x.edgegateway.client.domain.ndp.CmdBuilderFactory;
import com.x.edgegateway.client.domain.ndp.common.ICmd;
import com.x.edgegateway.client.domain.ndp.ota.DeviceOtaData;
import com.x.edgegateway.core.util.sensitivelog.LogMarkers;
import com.x.edgegateway.manager.base.exception.EdgeExceptionCode;
import com.x.edgegateway.manager.base.utils.BeanPropertyCopyUtils;
import com.x.edgegateway.manager.base.utils.BizIdGenerator;
import com.x.edgegateway.manager.cmdissue.ICmdIssueManager;
import com.x.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;
import com.x.edgegateway.shared.service.cmdissue.ICmdIssueSharedService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author patrickkk  2020/10/21 16:18
 */
@Service("cmdIssueAtopService")
@Slf4j
public class CmdIssueAtopService implements ICmdIssueAtopService {

    @Resource
    private ICmdIssueSharedService cmdIssueSharedService;

    @Resource
    private ICmdIssueManager cmdIssueManager;

    @Resource
    private BizIdGenerator bizIdGenerator;

    @Override
    public AtopResult<IssueCmdResultVO> issueDeviceCommand(String uidForIOT, String deviceId, String cmdJson, ApiRequestDO apiRequestDO) {
        //【1】参数校验
        Assert.hasText(deviceId, "设备id不能为空！");
        Assert.hasText(cmdJson, "cmd对象不能为空");

        //【2】生成cmd对象并设置sn号
        ICmd cmd = convertJson2Cmd(deviceId, cmdJson);
        cmd.setSn(bizIdGenerator.generateId());

        log.info(LogMarkers.SENSITIVE, "atop.issueDeviceCommand, deviceId: {}, cmdJson: {}", deviceId, JSON.toJSONString(cmdJson));

        //【3】指令发送
        IssueCmdResultVO cmdResultVO = cmdIssueSharedService.issueDeviceDpCommand(uidForIOT, deviceId, cmd, CmdPriorityEnum.MIDDLE);

        //【4】返回结果
        return AtopResult.newInstance(cmdResultVO);
    }

    @Override
    public AtopResult<IssueDataCmdInfoVO> queryIssueDataCmd(Long sn, ApiRequestDO apiRequestDO) {
        //【1】参数校验
        Assert.isTrue(sn > 0, "sn号不能为空！");

        //【2】指令查询
        CmdIssueRecordDTO cmdIssueRecordDTO = cmdIssueManager.queryCmdIssueBySn(sn, false);
        if (cmdIssueRecordDTO == null) {
            log.warn("queryIssueDataCmd, 指令记录不存在, sn: {}", sn);
            return new AtopResult(EdgeExceptionCode.BUSINESS_EXCEPTION);
        }
        IssueDataCmdInfoVO issueDataCmdInfoVO = BeanPropertyCopyUtils.copy(cmdIssueRecordDTO, IssueDataCmdInfoVO.class);

        //【3】返回结果
        return AtopResult.newInstance(issueDataCmdInfoVO);
    }


    private ICmd convertJson2Cmd(String deviceId, String cmdJson) {
        JSONObject cmdJsonObject = JSON.parseObject(cmdJson);
        boolean gateway = cmdJsonObject.getBoolean("gateway");

        ICmd cmd = null;
        if (gateway) {
            String otaUpgradeFileRequestList = cmdJsonObject.getString("otaUpgradeFileRequestList");
            List<DeviceOtaData> otaUpgradeFileList = JSON.parseArray(otaUpgradeFileRequestList, DeviceOtaData.class);
            cmd = CmdBuilderFactory.deviceOtaCmdBuilders().deviceOtaCmdBuilder().withOtaUpgradeFileRequestList(otaUpgradeFileList).build();
        } else {
            String mode = cmdJsonObject.getString("mode");
            String type = cmdJsonObject.getString("type");

            if (StringUtils.equals(mode, "dc_userInfo") && StringUtils.equals(type, "add")) {
                JSONObject jsonObject = cmdJsonObject.getJSONObject("data");
                cmd = CmdBuilderFactory.doorControlCmdBuilders().issueUserInfoDataAddCmdBuilder()
                        .dataBuilder()
                        .withUid(jsonObject.getString("uid"))
                        .withName(jsonObject.getString("name"))
                        .withIdCard(jsonObject.getString("idcard"))
                        .withPhone(jsonObject.getString("phone"))
                        .withBeginTime(jsonObject.getLong("beginTime"))
                        .withEndTime(jsonObject.getLong("endTime"))
                        .parentBuilder()
                        .build();
            } else if (StringUtils.equals(mode, "dc_userInfo") && StringUtils.equals(type, "del")) {
                JSONObject jsonObject = cmdJsonObject.getJSONObject("data");
                cmd = CmdBuilderFactory.doorControlCmdBuilders().issueUserInfoDataDelCmdBuilder()
                        .dataBuilder()
                        .withUid(jsonObject.getString("uid"))
                        .parentBuilder()
                        .build();
            } else {
                log.error("该指令不支持,deviceId: {}, cmdJson: {}", deviceId, cmdJson);
            }
        }
        return cmd;
    }
}
