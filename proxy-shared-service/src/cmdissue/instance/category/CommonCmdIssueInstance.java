package cmdissue.instance.category;

import cmdissue.instance.CmdIssueStrategy;
import cmdissue.instance.DefaultCmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通用的指令发送实例
 * <ore>
 * <li> 没有特殊逻辑,指令通过该实例发送</li>
 * </pre>
 *
 * @author patrickkk  2020/12/18 15:58
 */
@CmdIssueStrategy(productType = "common_cmd_issue")
@Component("common_cmd_issue_")
@Slf4j
public class CommonCmdIssueInstance extends DefaultCmdIssueInstance {

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        super.issue(cmdIssueDTO);
    }
}
