package cmdissue.instance.category.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cmdissue.instance.CmdIssueStrategy;
import cmdissue.instance.DefaultCmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 边缘网关指令发送
 *
 * @author patrickkk  2020/06/04 17:07
 */
@CmdIssueStrategy(productType = "wf_xnwg")
@Component("wf_xnwg_")
@Slf4j
public class GatewayCmdIssueInstance extends DefaultCmdIssueInstance {

    private static final List<String> modeList = Arrays.asList(
            "ai_alg_webcam_rel", //算法节点和子设备的关联关系 新增和删除
            "ai_trans_webcam_rel", //流传输节点和子设备的关系
            "ai_thirdcamserial_rel"//超脑三方相机序列号和x摄像头设备cid关系
    );

    @Resource
    private IDeviceExtManager deviceExtManager;

    @Resource
    private IDevicePropExtSharedService devicePropertyExtSharedService;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        if (!StringUtils.equals("1", cmdIssueDTO.getDpid())) {
            super.issue(cmdIssueDTO);
            return;
        }

        String dpData = cmdIssueDTO.getData();
        JSONObject jsonObj = JSON.parseObject(dpData);
        JSONObject dpObj = jsonObj.getJSONObject("1");
        JSONObject dataObj = dpObj.getJSONObject("data");
        String mode = dpObj.getString("mode");
        String type = dpObj.getString("type");

        if (modeList.contains(mode)) {
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
                log.warn("GatewayCmdIssueInstance, operate error, cmdIssueDTO: {}", cmdIssueDTO);
            }
        }
        super.issue(cmdIssueDTO);
    }
}
