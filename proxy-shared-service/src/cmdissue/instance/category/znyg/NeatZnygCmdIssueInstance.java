package cmdissue.instance.category.znyg;

import cmdissue.instance.CmdIssueStrategy;
import cmdissue.instance.DefaultCmdIssueInstance;
import cmdissue.instance.ICmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 尼特智能烟感指令发送实例
 *
 * @author patrickkk  2020/12/18 15:58
 */
@CmdIssueStrategy(productType = "znyg_7n_1",vendorCode = "neat")
@Component("znyg_7n_1_neat")
@Slf4j
public class NeatZnygCmdIssueInstance extends DefaultCmdIssueInstance {

    @Resource
    private ICmdIssueInstance znyg_7n_1_neat;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        znyg_7n_1_neat.issue(cmdIssueDTO);
    }
}
