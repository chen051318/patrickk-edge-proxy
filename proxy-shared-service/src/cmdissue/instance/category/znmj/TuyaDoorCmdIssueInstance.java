package cmdissue.instance.category.znmj;

import cmdissue.instance.CmdIssueStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 门禁指令发送
 *
 * @author patrickkk  2020/1/15 14:07
 */
@CmdIssueStrategy(productType = "wf_znmj", vendorCode = "x")
@Component("wf_znmj_")
@Slf4j
public class xDoorCmdIssueInstance extends DoorCmdIssueInstance {

    @Resource
    private IKafkaDoorMsgProducer kafkaDoorMsgProducer;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        boolean flag = doFilter(cmdIssueDTO);
        if (flag) {
            return;
        }

        // AiPad特殊处理
        if (StringUtils.equals("128", cmdIssueDTO.getDpid())) {
            KafkaAipadMessage message = new KafkaAipadMessage();
            message.setUid(cmdIssueDTO.getUid());
            message.setDeviceId(cmdIssueDTO.getDeviceId());
            message.setData(cmdIssueDTO.getData());
            kafkaDoorMsgProducer.pushAipadMessage(message);
            return;
        }
        super.issueNotFilter(cmdIssueDTO);
    }
}
