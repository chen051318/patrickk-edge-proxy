package lan.domains;

import com.x.luban.biz.domain.base.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class BindInfo extends ToString {

    /**
     * 场景面板按键ID
     */
    private String btnId;

    /**
     * 场景名称
     */
    private String ruleName;

    /**
     * 场景ID
     */
    private String ruleId;

    private String sid;

    private String gid;
}
