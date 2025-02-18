
package lan.domains;

import com.tuya.jupiter.client.domain.linkage.constants.ActionExecutorType;
import com.tuya.jupiter.client.domain.linkage.vo.LinkageActionVO;
import com.tuya.jupiter.client.domain.linkage.vo.LinkageConditionVO;
import com.tuya.jupiter.client.domain.linkage.vo.LinkageRuleVO;
import com.tuya.luban.client.enums.RuleConstant;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class LinkageRule extends LinkageRuleVO {

    /**
     * 普通联动
     */
    public static final Integer COMMON_RULE = 0;

    /**
     * 动作中包含场景
     */
    public static final Integer ACTION_RULE = -1;

    /**
     * 虚拟规则id（需要执行了clearRuleId方法才有）
     */
    private String vrRuleId;

    /**
     * 是否是多控组
     */
    private boolean deviceGroup;

    /**
     * 是否是无线开关自动化信息
     */
    private boolean wirelessSwitch;

    /**
     * 是否是场景面板自动场景
     */
    private boolean scenePanel;

    /**
     * 场景面板sid
     */
    private String scenePanelSid;

    /**
     * 场景面板gid
     */
    private String scenePanelGid;

    /**
     * 场景类型
     * 
     * @see com.tuya.luban.client.enums.RuleConstant
     */
    private Integer linkageRuleType;

    public void clearRuleId() {
        this.vrRuleId = super.getId();
        super.setId("");
    }

    public boolean actionRule() {
        List<LinkageActionVO> actions = getActions();
        if (CollectionUtils.isEmpty(actions)) {
            return false;
        }
        return actions.stream()
                .anyMatch(action -> StringUtils.equals(action.getActionExecutor(), ActionExecutorType.RULE_TRIGGER));
    }

    public Integer toGroupType() {
        if (scenePanel || actionRule()) {
            return ACTION_RULE;
        }
        return COMMON_RULE;
    }

    public Integer toRuleType() {
        if (deviceGroup) {
            return RuleConstant.MULTI_CONTROL_GROUP;
        }
        if (scenePanel) {
            return RuleConstant.RULE_PANEL_AUTOMATION;
        }
        if (wirelessSwitch) {
            return RuleConstant.WIRELESS;
        }
        if (linkageRuleType != null) {
            return linkageRuleType;
        }
        return COMMON_RULE;
    }

    public boolean checkMultiGateway() {
        Set<String> gatewayIdSet = this.getActions().stream().map(LinkageActionVO::getParentDevId)
                .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(this.getConditions())) {
            Set<String> conditionGatewayIds = this.getConditions().stream().map(LinkageConditionVO::getParentDevId)
                    .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
            gatewayIdSet.addAll(conditionGatewayIds);
        }
        return gatewayIdSet.size() > 1;
    }
}
