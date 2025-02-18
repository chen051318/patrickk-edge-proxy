package lan.domains;

import com.tuya.luban.biz.domain.base.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class SceneInfo extends ToString {

    /**
     * 场景基础数据
     */
    private Map<String, MetaInfo> meta;

    /**
     * 模板联动数据
     */
    private List<LinkageRule> template;

    /**
     * 模板场景面板数据
     */
    private List<RulePanel> rulePanel;
}
