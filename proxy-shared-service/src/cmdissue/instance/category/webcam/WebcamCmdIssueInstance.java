package cmdissue.instance.category.webcam;

import cmdissue.instance.CmdIssueStrategy;
import org.springframework.stereotype.Component;

/**
 * 摄像头指令发送
 *
 * @author patrickkk  2020/06/04 15:07
 */
@CmdIssueStrategy(productType = "sp_1w_5")
@Component("sp_1w_5_")
public class WebcamCmdIssueInstance extends BaseWebcamCmdIssue {
}
