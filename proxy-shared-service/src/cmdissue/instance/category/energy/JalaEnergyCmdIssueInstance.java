package cmdissue.instance.category.energy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cmdissue.ICmdIssueSharedService;
import cmdissue.instance.CmdIssueStrategy;
import cmdissue.instance.DefaultCmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 佳岚断路器指令发送
 *
 * @author patrickkk  2020/12/18 15:58
 */
@CmdIssueStrategy(productType = "dlq_8c_4", vendorCode = "jala")
@Component("dlq_8c_4_jala")
@Slf4j
public class JalaEnergyCmdIssueInstance extends DefaultCmdIssueInstance {

    private static final String[] DP_ARRAY = new String[]{"1", "6", "11", "12", "13", "17", "18", "33"};

    @Resource
    private IDeviceManager deviceManager;

    @Resource
    private ICmdIssueSharedService cmdIssueSharedService;

    @Resource
    private IJalaBridgeServiceClient jalaBridgeServiceClient;

    @Resource
    private JalaValueRangeConfig rangeConfig;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        Map<String, String> dpMap = new HashMap<>();
        String dpData = cmdIssueDTO.getData();
        JSONObject jsonObj = JSON.parseObject(dpData);
        for (String jalaDpId : DP_ARRAY) {
            String dpJson = jsonObj.getString(jalaDpId + "");
            if (StringUtils.isNotBlank(dpJson)) {
                dpMap.put(jalaDpId, dpJson);
            }
        }

        ServiceResult<Boolean> result;

        // 佳岚的指令不能发送太快，发快了佳岚有问题，这里加锁并睡眠，…………####
        synchronized (this) {
            result = doExecute(dpMap, cmdIssueDTO);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //处理指令执行结果消息
        Boolean flag = false;
        if (null != result && result.getSuccess() && result.getData()) {
            flag = true;
        }

        IssueDataCmdResultDTO issueDataCmdResultDTO = new IssueDataCmdResultDTO();
        issueDataCmdResultDTO.setSn(cmdIssueDTO.getSn());
        issueDataCmdResultDTO.setSuccess(flag == true ? 1 : 0);
        issueDataCmdResultDTO.setMessage(result.getMessage());
        cmdIssueSharedService.dealCmdIssueResult(issueDataCmdResultDTO);
    }

    /**
     * 执行指令发送
     *
     * @param dpMap
     * @param cmdIssueDTO
     * @return
     */
    public ServiceResult<Boolean> doExecute(Map<String, String> dpMap, CmdIssueRecordDTO cmdIssueDTO) {
        DeviceDTO deviceDTO = deviceManager.queryDeviceByDeviceId(cmdIssueDTO.getDeviceId());
        if (deviceDTO == null) {
            log.error("JalaEnergyCmdIssueInstance, 查询设备记录失败, deviceId: {}", cmdIssueDTO.getDeviceId());
            throw new EdgeException("能源管理指令发送,查询设备记录失败");
        }

        DeviceDTO parentDeviceDTO = deviceManager.queryDeviceByDeviceId(deviceDTO.getParentDevId());
        if (parentDeviceDTO == null) {
            log.error("JalaEnergyCmdIssueInstance, 查询父设备记录失败, deviceId: {}", cmdIssueDTO.getDeviceId());
            throw new EdgeException("能源管理指令发送,查询父设备记录失败");
        }

        String controllerId = (String) parentDeviceDTO.getInnerExtendMap().get(DevExtKeyConstants.Energy.CONTROLLER_ID);
        Map<String, Object> innerExtendMap = deviceDTO.getInnerExtendMap();
        Integer lineNo = (Integer) innerExtendMap.get(DevExtKeyConstants.Energy.LINE_NO);
        String model = (String) innerExtendMap.get(DevExtKeyConstants.Energy.MODEL);
        String deviceId = deviceDTO.getDeviceId();


        ServiceResult<Boolean> result = ServiceResult.asFailed(ServiceResultCode.SERVICE_NOT_EXISTED);
        if (StringUtils.isNotBlank(dpMap.get("1"))) {
            //线路开关
            String dpJson = dpMap.get("1");
            boolean switchEnable = Boolean.parseBoolean(dpJson);

            JalaSwitchEnableRequest request = new JalaSwitchEnableRequest();
            request.setDeviceId(deviceId);
            request.setControllerId(controllerId);
            request.add(lineNo, switchEnable ? 1 : 0);

            result = jalaBridgeServiceClient.setupSwitchEnable(request);

            if (result.getSuccess() && result.getData()) {
                DeviceDTO updateDeviceDTO = new DeviceDTO();
                updateDeviceDTO.setDeviceId(deviceDTO.getDeviceId());
                innerExtendMap.put(DevExtKeyConstants.Energy.SWITCH_STATUS, switchEnable ? 1 : 0);
                updateDeviceDTO.setExtendData(JSON.toJSONString(innerExtendMap));
                deviceManager.updateByDeviceId(updateDeviceDTO);
            }

            log.info("JalaEnergyCmdIssueInstance, 设置线路开关, result: {}, sn: {}", result.getSuccess(), cmdIssueDTO.getSn());
        }

        // 佳岚的电流和持续时间需要一起设置，否则会报错
        if (StringUtils.isNotBlank(dpMap.get("6")) || StringUtils.isNotBlank(dpMap.get("18"))) {
            //设置最大电流及持续时间
            String tmpDpId = "6";
            String dpJson6 = dpMap.get(tmpDpId);
            Integer max;
            if (StringUtils.isNotBlank(dpJson6)) {
                max = Integer.parseInt(dpJson6) / 10000;
                innerExtendMap.put(DevExtKeyConstants.Energy.CURRENT_MAX, max);
            } else {
                // 没有时，取最后一次的dp值
                max = Integer.parseInt(innerExtendMap.get(DevExtKeyConstants.Energy.CURRENT_MAX).toString());
                log.info("Use last max current ({}) value {}", tmpDpId, max);
            }

            tmpDpId = "18";
            String dpJson18 = dpMap.get(tmpDpId);
            Integer duration;
            if (StringUtils.isNotBlank(dpJson18)) {
                duration = Integer.parseInt(dpJson18) / 100;
                innerExtendMap.put(DevExtKeyConstants.Energy.DURATION, duration);
            } else {
                // 没有时，取最后一次的dp值
                duration = Integer.parseInt(innerExtendMap.get(DevExtKeyConstants.Energy.DURATION).toString());
                log.info("Use last duration ({}) value {}", tmpDpId, duration);
            }

            JalaMaxCurrentRequest request = new JalaMaxCurrentRequest();
            request.setDeviceId(deviceId);
            request.setThirdDeviceId(parentDeviceDTO.getThirdDeviceId());
            request.setLineId(deviceDTO.getThirdDeviceId());
            request.setMax(max);
            request.setDuration(duration);

            result = jalaBridgeServiceClient.setupMaxCurrent(request);

            if (result.getSuccess() && result.getData()) {
                DeviceDTO updateDeviceDTO = new DeviceDTO();
                updateDeviceDTO.setDeviceId(deviceDTO.getDeviceId());
                updateDeviceDTO.setExtendData(JSON.toJSONString(innerExtendMap));
                deviceManager.updateByDeviceId(updateDeviceDTO);
            }

            log.info("JalaEnergyCmdIssueInstance, 设置最大电流及持续时间, result: {}, max: {}, duration: {}, sn: {}", result.getSuccess(), max, duration, cmdIssueDTO.getSn());
        }

        // 佳岚的欠压和过压要一起设置，要不然会有问题
        if (StringUtils.isNotBlank(dpMap.get("11")) || StringUtils.isNotBlank(dpMap.get("12"))) {
            String tmpDpId = "11";
            //设置欠压及过压值
            String dpJson11 = dpMap.get(tmpDpId);
            Integer under;
            if (StringUtils.isNotBlank(dpJson11)) {
                under = Integer.parseInt(dpJson11) / 10000;
                innerExtendMap.put(DevExtKeyConstants.Energy.VOLTAGE_UNDER, under);
            } else {
                // 没有时，取最后一次的dp值
                under = Integer.parseInt(innerExtendMap.get(DevExtKeyConstants.Energy.VOLTAGE_UNDER).toString());
                log.info("Use last under ({}) value {}", tmpDpId, under);
            }

            tmpDpId = "12";
            String dpJson12 = dpMap.get(tmpDpId);
            Integer over;
            if (StringUtils.isNotBlank(dpJson12)) {
                over = Integer.parseInt(dpJson12) / 10000;
                innerExtendMap.put(DevExtKeyConstants.Energy.VOLTAGE_OVER, over);
            } else {
                // 没有时，取最后一次的dp值
                over = Integer.parseInt(innerExtendMap.get(DevExtKeyConstants.Energy.VOLTAGE_OVER).toString());
                log.info("Use last over ({}) value {}", tmpDpId, over);
            }

            JalaVoltageRangeRequest request = new JalaVoltageRangeRequest();
            request.setDeviceId(deviceId);
            request.setThirdDeviceId(parentDeviceDTO.getThirdDeviceId());
            request.setLineId(deviceDTO.getThirdDeviceId());
            request.setOver(over);
            request.setUnder(under);

            result = jalaBridgeServiceClient.setupVoltageRange(request);

            if (result.getSuccess() && result.getData()) {
                DeviceDTO updateDeviceDTO = new DeviceDTO();
                updateDeviceDTO.setDeviceId(deviceDTO.getDeviceId());
                updateDeviceDTO.setExtendData(JSON.toJSONString(innerExtendMap));
                deviceManager.updateByDeviceId(updateDeviceDTO);
            }

            log.info("JalaEnergyCmdIssueInstance, 设置欠压及过压值, result: {}, under: {}, over: {}, sn: {}", result.getSuccess(), under, over, cmdIssueDTO.getSn());
        }


        if (StringUtils.isNotBlank(dpMap.get("13"))) {
            //设置漏电预警值
            String dpJson13 = dpMap.get("13");
            Integer leakValue = Integer.parseInt(dpJson13) / 10000;

            JalaLeakWarnValueRequest request = new JalaLeakWarnValueRequest();
            request.setDeviceId(deviceId);
            request.setThirdDeviceId(parentDeviceDTO.getThirdDeviceId());
            request.setLineId(deviceDTO.getThirdDeviceId());
            request.setLeakValue(leakValue);

            result = jalaBridgeServiceClient.setupLeakWarnValue(request);
            log.info("JalaEnergyCmdIssueInstance, 设置漏电预警值, result: {}, leakValue: {}, sn: {}", result.getSuccess(), leakValue, deviceDTO.getDeviceId());
        }

        if (StringUtils.isNotBlank(dpMap.get("33"))) {
            //设置漏电动作值
            String dpJson33 = dpMap.get("33");
            int errLeakValue = Integer.parseInt(dpJson33) / 10000;

            JalaLeakActionValueRequest request = new JalaLeakActionValueRequest();
            request.setDeviceId(deviceId);
            request.setThirdDeviceId(parentDeviceDTO.getThirdDeviceId());
            request.setLineId(deviceDTO.getThirdDeviceId());
            request.setErrLeakValue(errLeakValue);

            result = jalaBridgeServiceClient.setupLeakActionValue(request);
            log.info("JalaEnergyCmdIssueInstance, 设置漏电动作值, result: {}, errLeakValue: {}, sn: {}", result.getSuccess(), errLeakValue, cmdIssueDTO.getSn());
        }

        if (StringUtils.isNotBlank(dpMap.get("17"))) {
            //设置手动开关
            String dpJson17 = dpMap.get("17");
            Integer handEnabled = Integer.parseInt(dpJson17);

            JalaHandEnableRequest request = new JalaHandEnableRequest();
            request.setDeviceId(deviceId);
            request.setThirdDeviceId(parentDeviceDTO.getThirdDeviceId());
            request.setLineId(deviceDTO.getThirdDeviceId());
            request.setEnabled(handEnabled);

            result = jalaBridgeServiceClient.setupHandEnable(request);
            log.info("JalaEnergyCmdIssueInstance, 设置手动开关, result: {}, handEnabled: {}, sn: {}", result.getSuccess(), handEnabled, cmdIssueDTO.getSn());
        }

        return result;
    }
}
