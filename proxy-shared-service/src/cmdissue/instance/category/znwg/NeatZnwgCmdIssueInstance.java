package cmdissue.instance.category.znwg;

import cmdissue.ICmdIssueSharedService;
import cmdissue.instance.CmdIssueStrategy;
import cmdissue.instance.DefaultCmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 尼特智能温感指令发送实例
 *
 * @author patrickkk  2020/12/18 15:58
 */
@CmdIssueStrategy(productType = "znwg_7n_1",vendorCode = "neat")
@Component("znwg_7n_1_neat")
@Slf4j
public class NeatZnwgCmdIssueInstance extends DefaultCmdIssueInstance {

    @Resource
    private IDeviceManager deviceManager;

    @Resource
    private ICmdIssueSharedService cmdIssueSharedService;

    @Resource
    private INeatCmdIssueServiceClient neatCmdIssueServiceClient;

    @Resource
    private IDeviceFunctionService deviceFunctionService;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        String deviceId = cmdIssueDTO.getDeviceId();
        String productType = cmdIssueDTO.getProductType();
        String dpData = cmdIssueDTO.getData();
        //查询设备功能点
        ServiceResult<List<FunctionSimpleCfgVO>> result = deviceFunctionService.queryFunctionListByDeviceId(deviceId);
        if (result == null || CollectionUtils.isEmpty(result.getData())) {
            log.info("znwg_7n_1_neat issue fail device not found function deviceId {}", deviceId);
            return;
        }
        //判断是否有消音功能 消音code:tyabiucuax
        List<String> deviceFunctionCode = result.getData().stream().map(FunctionSimpleCfgVO::getFunctionCode).collect(Collectors.toList());
        if (!deviceFunctionCode.contains("tyabiucuax")) {
            log.info("znwg_7n_1_neat issue fail device not have silence function deviceId {}", deviceId);
            return;
        }
        DeviceDTO deviceDTO = deviceManager.queryDeviceByDeviceId(deviceId);
        //执行指令
        ServiceResult<Boolean> serviceResult = neatCmdIssueServiceClient.issueNeatCommand(deviceDTO.getCid(), productType, dpData);

        //处理指令执行结果消息
        IssueDataCmdResultDTO issueDataCmdResultDTO = new IssueDataCmdResultDTO();
        issueDataCmdResultDTO.setSn(cmdIssueDTO.getSn());
        issueDataCmdResultDTO.setSuccess(serviceResult.getSuccess() == true ? 1 : 0);
        issueDataCmdResultDTO.setMessage(serviceResult.getMessage());

        cmdIssueSharedService.dealCmdIssueResult(issueDataCmdResultDTO);
    }
}
