package cmdissue.instance.category.znmj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cmdissue.instance.CmdIssueStrategy;
import cmdissue.instance.DefaultCmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;


/**
 * 通用门禁指令发送
 *
 * @author patrickkk  2020/1/15 14:07
 */
@CmdIssueStrategy(productType = "wf_znmj")
@Component("wf_znmj_")
@Slf4j
public class DoorCmdIssueInstance extends DefaultCmdIssueInstance {


    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        boolean flag = doFilter(cmdIssueDTO);
        if (flag) {
            return;
        }
        super.issue(cmdIssueDTO);
    }

    public void issueNotFilter(CmdIssueRecordDTO cmdIssueDTO) {
        super.issue(cmdIssueDTO);
    }


    /**
     * @param cmdIssueDTO
     * @return true    已处理完成,直接返回
     */
    protected boolean doFilter(CmdIssueRecordDTO cmdIssueDTO) {
        //50 下发秘钥指令
        if (StringUtils.equals("50", cmdIssueDTO.getDpid())) {
            String dpData = cmdIssueDTO.getData();
            JSONObject jsonObj = JSON.parseObject(dpData);
            dpData = jsonObj.getString("50");

            //转码后重新设置
            try {
                dpData = new Base64().encodeToString(dpData.getBytes(StandardCharsets.UTF_8));
            } catch (Exception ex) {
                log.warn("doFilter, base64编码失败, dpData: " + dpData, ex);
                return true;
            }
            jsonObj.put("50", dpData);

            cmdIssueDTO.setData(jsonObj.toJSONString());
            super.issue(cmdIssueDTO);
            return true;
        }
        return false;
    }
}
