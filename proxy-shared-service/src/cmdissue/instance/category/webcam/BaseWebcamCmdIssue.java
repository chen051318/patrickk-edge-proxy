package cmdissue.instance.category.webcam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cmdissue.instance.DefaultCmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author : patrickkk
 * @date 2021-06-30
 */
@Slf4j
public class BaseWebcamCmdIssue extends DefaultCmdIssueInstance {

    private static final String VERSION_KEY = "version";

    private static final String VERSION_2_0_0 = "2.0.0";

    @Resource
    private IMessagePushServiceClient messagePushServiceClient;

    @Resource
    private IDeviceManager deviceManager;

    @Resource
    private IDeviceExtManager deviceExtManager;

    @Resource
    private IDevicePropExtSharedService devicePropertyExtSharedService;

    @Resource
    private IDeviceMServiceClient deviceMServiceClient;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        if (!StringUtils.equals("5", cmdIssueDTO.getDpid())) {
            super.issue(cmdIssueDTO);
            return;
        }

        DeviceDTO deviceDTO = deviceManager.queryDeviceByDeviceId(cmdIssueDTO.getDeviceId());
        if (null == deviceDTO) {
            log.warn("Device {} not exist", cmdIssueDTO.getDeviceId());
            return;
        }

        String dpData = cmdIssueDTO.getData();
        JSONObject jsonObj = JSON.parseObject(dpData);
        JSONObject dpObj = jsonObj.getJSONObject("5");
        JSONObject dataObj = dpObj.getJSONObject("data");
        String mode = dpObj.getString("mode");
        String type = dpObj.getString("type");
        String reqType = dpObj.getString("reqType");

        if (getModeList().contains(mode)) {
            //构造设备属性对象
            DeviceExtDTO deviceExtDTO = new DeviceExtDTO();
            deviceExtDTO.setDeviceId(cmdIssueDTO.getDeviceId());
            deviceExtDTO.setProperty(mode);

            if (StringUtils.equals(OP.add.name(), type)) {
                deviceExtDTO.setValue(dataObj.toJSONString());
                devicePropertyExtSharedService.addOrUpdate(deviceExtDTO);
            } else if (StringUtils.equals(OP.del.name(), type)) {
                deviceExtDTO.setStatus(0);
                deviceExtManager.updateByDeviceId(deviceExtDTO);
            } else {
                log.warn("BaseWebcamCmdIssue, operate error, cmdIssueDTO: {}", cmdIssueDTO);
            }
        }

        // 如果支持协议，走协议，如果不支持，走dp
        if (isSupportProtocol(deviceDTO)) {
            log.info("Use protocol, device id {}", deviceDTO.getDeviceId());

            // 生成协议数据对象
            ProtocolDTO<Map<String, Object>> protocolDTO = new ProtocolDTO<>();
            protocolDTO.setCid(deviceDTO.getCid());
            protocolDTO.setReqId(cmdIssueDTO.getSn().toString());
            protocolDTO.setReqType("ipc_ai");
            protocolDTO.setAiSkill(reqType);
            protocolDTO.setFunction(mode + "_" + type);

            protocolDTO.setV("1.0.0");
            protocolDTO.setData(dataObj.getInnerMap());

            GatewayCache iotDeviceDTO = deviceMServiceClient.getGatewayCache(cmdIssueDTO.getDeviceId());
            if (null == iotDeviceDTO) {
                log.warn("Iot device {} not exist", cmdIssueDTO.getDeviceId());
                return;
            }

            // 发送协议消息
            messagePushServiceClient.issueDeviceProtocolCommand(iotDeviceDTO.getLocalKey(), 64,
                    iotDeviceDTO.getProtocolVersion(), cmdIssueDTO.getDeviceId(), protocolDTO);
            return;
        }
        super.issue(cmdIssueDTO);
    }

    private List<String> getModeList() {
        return Arrays.stream(AiConst.AiModeEnum.values()).map(AiConst.AiModeEnum::getMode).collect(Collectors.toList());
    }

    /**
     * 判断设备是否支持协议
     *
     * @param deviceDTO 三方设备信息
     * @return 是否支持协议
     */
    private boolean isSupportProtocol(DeviceDTO deviceDTO) {
        return Objects.equals(VERSION_2_0_0, getVersionFromExtendData(deviceDTO.getExtendData()));
    }

    private String getVersionFromExtendData(String extendData) {
        Map<String, Object> extendMap = ExtendDataUtil.extendData2Map(extendData);
        if (!CollectionUtils.isEmpty(extendMap) && extendMap.containsKey(VERSION_KEY)) {
            return extendMap.get(VERSION_KEY).toString();
        }
        return null;
    }
}
