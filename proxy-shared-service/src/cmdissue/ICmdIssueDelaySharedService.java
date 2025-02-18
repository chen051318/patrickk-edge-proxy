package cmdissue;

import com.tuya.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;

/**
 * @author patrickkk  2021/7/21 14:53
 */
public interface ICmdIssueDelaySharedService {

    /**
     * @param sn
     */
    void improveIssueCmd(Long sn);

    /**
     * 指令发送
     *
     * @param cmdIssueDTO
     */
    void issueGeneralCmd(CmdIssueRecordDTO cmdIssueDTO);


    /**
     * 计算下一次指令发送时间
     *
     * @param deviceId
     * @param cmdIntervalFactor
     * @return
     */
    Long calcNextProcessTime(String deviceId, Long cmdIntervalFactor);

    /**
     * 发送指令执行结果
     *
     * @param cmdIssueDTO
     * @param processStatus
     * @param message
     */
    void sendCmdResultMessage(CmdIssueRecordDTO cmdIssueDTO, int processStatus, String message);
}
