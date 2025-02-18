
package lan.executor;

import com.google.common.collect.Lists;
import com.tuya.jupiter.client.domain.linkage.vo.LinkageConditionVO;
import com.tuya.jupiter.client.domain.linkage.vo.LinkageDeviceRuleShellVO;
import com.tuya.luban.biz.enums.GatewayErrorEnum;
import com.tuya.luban.biz.service.lan.domains.LanRecoverContext;
import com.tuya.luban.biz.service.lan.domains.LinkageRule;
import com.tuya.luban.biz.service.lan.domains.recover.params.SceneErrorParamsDTO;
import com.tuya.luban.biz.service.lan.utils.ConvertUtil;
import com.tuya.luban.client.enums.RuleConstant;
import com.tuya.luban.integration.jupiter.ILinkageDeviceRuleShellIntegration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 无线开关自动化
 * 
 * @author : patrickkk
 * @date 2022-08-19
 */
@Slf4j
@Component
public class WirelessSwitchRuleExecutor extends DefaultLanRuleExecutor {

    @Resource
    private ILinkageDeviceRuleShellIntegration linkageDeviceRuleShellIntegration;

    @Override
    void preCreateRule(LanRecoverContext context, LinkageRule rule) {
        //处理entityId
        dealRuleEntityId(context, rule);
    }

    @Override
    void catchCreateRule(LanRecoverContext context, LinkageRule rule, Exception e) {
        lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_AUTOMATION_SAVE_ERROR,
                SceneErrorParamsDTO.builder().sceneName(rule.getName()).build(), e.getMessage());
    }

    @Override
    List<GatewayErrorEnum> getMockError() {
        return Lists.newArrayList(GatewayErrorEnum.WG_RE_AUTOMATION_SAVE_ERROR);
    }

    @Override
    public String doCreateRule(LanRecoverContext context, LinkageRule rule) {
        List<LinkageConditionVO> conditions = rule.getConditions();
        LinkageConditionVO conditionVO = conditions.get(0);

        LinkageDeviceRuleShellVO shellVO = ConvertUtil.getLinkageDeviceRuleShellVO(rule, conditions, conditionVO);
        LinkageDeviceRuleShellVO result = linkageDeviceRuleShellIntegration.saveDeviceRule(context.getUid(),
                context.getOwnerId(), shellVO);
        return result.getId() + "";
    }

    @Override
    public String getUniqueKey() {
        return RuleConstant.WIRELESS + "";
    }
}
