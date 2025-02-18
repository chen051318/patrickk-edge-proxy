
package lan.executor;

import com.google.common.collect.Lists;
import com.tuya.jupiter.client.domain.linkage.constants.RuleType;
import com.tuya.luban.biz.enums.GatewayErrorEnum;
import com.tuya.luban.biz.service.lan.domains.LanRecoverContext;
import com.tuya.luban.biz.service.lan.domains.LinkageRule;
import com.tuya.luban.biz.service.lan.domains.recover.params.SceneErrorParamsDTO;
import com.tuya.luban.integration.jupiter.ILinkageRuleServiceIntegration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 多控组
 * 
 * @author : patrickkk
 * @date 2022-08-19
 */
@Slf4j
@Component
public class CommonRuleExecutor extends DefaultLanRuleExecutor {

    @Resource
    private ILinkageRuleServiceIntegration linkageRuleServiceIntegration;

    @Override
    void preCreateRule(LanRecoverContext context, LinkageRule rule) {
        //处理entityId
        dealRuleEntityId(context, rule);
    }

    @Override
    void catchCreateRule(LanRecoverContext context, LinkageRule rule, Exception e) {
        GatewayErrorEnum errorEnum = GatewayErrorEnum.WG_RE_SCENE_SAVE_ERROR;
        if (CollectionUtils.isNotEmpty(rule.getConditions())) {
            errorEnum = GatewayErrorEnum.WG_RE_AUTOMATION_SAVE_ERROR;
        }
        lanGatewayRecoverService.addGatewayRecoverError(errorEnum,
                SceneErrorParamsDTO.builder().sceneName(rule.getName()).build(), e.getMessage());
        log.warn("saveRule error: uid = {}, ownerId = {}, templateVO.name = {}", context.getUid(), context.getOwnerId(),
                rule.getName());
    }

    @Override
    List<GatewayErrorEnum> getMockError() {
        return Lists.newArrayList(GatewayErrorEnum.WG_RE_SCENE_SAVE_ERROR,
                GatewayErrorEnum.WG_RE_AUTOMATION_SAVE_ERROR);
    }

    @Override
    public String doCreateRule(LanRecoverContext context, LinkageRule rule) {
        boolean multiGateway = rule.checkMultiGateway();
        if (multiGateway) {
            clearParams(rule);
        }
        String ruleId = linkageRuleServiceIntegration.saveRuleV2(context.getUid(), context.getOwnerId(), rule,
                RuleType.USER_DEFINE.getValue());
        context.getRuleVOMap().put(rule.getVrRuleId(), ruleId);
        return ruleId;
    }

    private void clearParams(LinkageRule rule) {
        rule.setAttribute(null);
    }

    @Override
    public String getUniqueKey() {
        return LinkageRule.COMMON_RULE + "";
    }
}
