
package lan.executor;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.tuya.jupiter.client.domain.linkage.LinkageRuleBindDO;
import com.tuya.jupiter.client.domain.linkage.constants.RuleBindType;
import com.tuya.jupiter.client.domain.linkage.vo.LinkageActionVO;
import com.tuya.jupiter.client.domain.linkage.vo.LinkageConditionVO;
import com.tuya.luban.biz.enums.GatewayErrorEnum;
import com.tuya.luban.biz.service.lan.domains.LanRecoverContext;
import com.tuya.luban.biz.service.lan.domains.LinkageRule;
import com.tuya.luban.biz.service.lan.domains.MetaInfo;
import com.tuya.luban.biz.service.lan.domains.recover.params.DeviceErrorParamsDTO;
import com.tuya.luban.client.enums.RuleConstant;
import com.tuya.luban.integration.jupiter.ILinkageRuleBindIntegration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 场景面板自动化
 * 
 * @author : patrickkk
 * @date 2022-08-19
 */
@Slf4j
@Component
public class ScenePanelRuleExecutor extends DefaultLanRuleExecutor {

    @Resource
    private ILinkageRuleBindIntegration linkageRuleBindIntegration;

    @Override
    void preCreateRule(LanRecoverContext context, LinkageRule rule) {

    }

    @Override
    void catchCreateRule(LanRecoverContext context, LinkageRule rule, Exception e) {
        LinkageConditionVO conditionVO = rule.getConditions().get(0);
        LinkageActionVO actionVO = rule.getActions().get(0);
        Map<String, MetaInfo> meta = context.getConfig().getScenes().getMeta();
        String deviceName = Optional.ofNullable(meta).map(m -> m.get(conditionVO.getEntityId())).map(MetaInfo::getName)
                .orElse("");
        lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_SCENE_PANEL_BIND_ERROR,
                DeviceErrorParamsDTO.builder().deviceName(deviceName).dpName(actionVO.getEntityName()).build(),
                e.getMessage());
    }

    @Override
    List<GatewayErrorEnum> getMockError() {
        return Lists.newArrayList(GatewayErrorEnum.WG_RE_SCENE_PANEL_BIND_ERROR);
    }

    @Override
    public String doCreateRule(LanRecoverContext context, LinkageRule rule) {
        Map<String, String> devIdMap = context.getDevIdMap();
        Map<String, String> ruleMap = context.getRuleVOMap();

        LinkageConditionVO conditionVO = rule.getConditions().get(0);
        LinkageActionVO actionVO = rule.getActions().get(0);
        LinkageRuleBindDO ruleBindDO = new LinkageRuleBindDO();
        String devId = devIdMap.get(conditionVO.getEntityId());
        ruleBindDO.setDevId(devId);
        ruleBindDO.setBtnId(Integer.valueOf(conditionVO.getEntitySubIds()));
        ruleBindDO.setRuleId(ruleMap.get(actionVO.getEntityId()));
        JSONArray expr = conditionVO.getExpr().getJSONArray(0);
        String value = expr.getString(2);
        ruleBindDO.setDpId(Integer.valueOf(conditionVO.getEntitySubIds()));
        ruleBindDO.setDpValue(value);
        ruleBindDO.setGwId("");
        ruleBindDO.setLocalSid("");
        ruleBindDO.setBindType(RuleBindType.BIND_TYPE_WIFI);
        linkageRuleBindIntegration.saveRuleBind4WiFi(ruleBindDO, context.getUid(), context.getOwnerId());
        return devId;
    }

    @Override
    public String getUniqueKey() {
        return RuleConstant.RULE_PANEL_AUTOMATION + "";
    }
}
