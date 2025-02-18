package cmdissue;

import com.tuya.edgegateway.client.common.PageResult;
import com.tuya.edgegateway.client.common.Paging;
import com.tuya.edgegateway.client.domain.cmd.IssueCmdResultVO;
import com.tuya.edgegateway.client.domain.cmd.constants.CmdPriorityEnum;
import com.tuya.edgegateway.client.domain.ndp.common.ICmd;
import com.tuya.edgegateway.core.cmdissue.domain.CmdIssueRecordQuery;
import com.tuya.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;
import com.tuya.edgegateway.manager.cmdissue.domain.IssueDataCmdResultDTO;

/**
 * @author: patrickkk
 * date： 2019/12/9
 */
public interface ICmdIssueSharedService {

    /**
     * 指令发送
     *
     * @param  uidForIOT
     * @param  deviceId
     * @param  cmd
     * @param  priorityEnum
     */
    IssueCmdResultVO issueDeviceDpCommand(String uidForIOT, String deviceId, ICmd cmd, CmdPriorityEnum priorityEnum);

    /**
     * 协议发送
     *
     * @param uidForIOT
     * @param deviceId
     * @param cmd
     */
    IssueCmdResultVO issueDeviceProtocolCommand(String uidForIOT, String deviceId, ICmd cmd);


    /**
     * 失败指令补偿
     *
     * @param cmdIssueDTO
     */
    void compensate(final CmdIssueRecordDTO cmdIssueDTO);


    /**
     * 处理下发数据指令的结果
     *
     * @param issueDataCmdResultDTO
     */
    void dealCmdIssueResult(IssueDataCmdResultDTO issueDataCmdResultDTO);

    /**
     * backendNg 查询列表
     *
     * @param condition
     * @param paging
     * @return
     */
    PageResult<CmdIssueRecordDTO> queryListByCondition(CmdIssueRecordQuery condition, Paging paging);
}
