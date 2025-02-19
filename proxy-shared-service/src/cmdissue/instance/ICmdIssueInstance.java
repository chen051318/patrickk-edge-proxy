package cmdissue.instance;

import com.x.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;

/**
 * 指令发送实例接口
 *
 * @author patrickkk  2020/12/18 15:51
 */
public interface ICmdIssueInstance {
    void issue(CmdIssueRecordDTO cmdIssueDTO);
}
