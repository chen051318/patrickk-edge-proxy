import com.tuya.atop.client.domain.api.ApiRequestDO;
import com.tuya.atop.client.domain.common.AtopResult;
import com.tuya.edgegateway.client.domain.cmd.IssueCmdResultVO;
import com.tuya.edgegateway.client.domain.cmd.IssueDataCmdInfoVO;

/**
 * 指令发送atop接口
 *
 * @author patrickkk  2020/10/21 16:38
 */
public interface ICmdIssueAtopService {

    /**
     * 指令发送(研发测试平台使用)
     * <pre>
     *    <li>支持门禁下发住户指令</li>
     *    <li>支持门禁下发删除住户指令</li>
     *    <li>支持下发上报日志指令</li>
     * </pre>
     *
     * @param uidForIOT
     * @param deviceId
     * @param cmdJson
     * @param apiRequestDO
     * @return
     * @clientApi  tuya.industry.base.edge.cmd.issue_1.0
     */
    AtopResult<IssueCmdResultVO> issueDeviceCommand(String uidForIOT, String deviceId, String cmdJson, ApiRequestDO apiRequestDO);

    /**
     * 查询单条指令记录
     *
     * @param sn
     * @param apiRequestDO
     * @return
     * @clientApi tuya.industry.base.edge.cmd.query_1.0
     */
    AtopResult<IssueDataCmdInfoVO> queryIssueDataCmd(Long sn, ApiRequestDO apiRequestDO);
}
