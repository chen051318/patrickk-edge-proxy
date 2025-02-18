package cmdissue.instance;

import com.tuya.edgegateway.integration.service.caesar.IDpPublishServiceClient;
import com.tuya.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 指令发送实例默认实现类
 *
 * @author patrickkk  2020/12/18 15:51
 */
@Slf4j
public class DefaultCmdIssueInstance implements ICmdIssueInstance {

    @Resource
    private IDpPublishServiceClient dpPublishServiceClient;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        String deviceId = cmdIssueDTO.getDeviceId();
        String dpData = cmdIssueDTO.getData();
        dpPublishServiceClient.issueDpCommand(cmdIssueDTO.getUid(), deviceId,
                cmdIssueDTO.getCmdVersion(), cmdIssueDTO.getSn(), dpData);
    }
}
