
package lan.executor;

import com.alibaba.fastjson.JSON;
import com.tuya.jupiter.client.domain.linkage.constants.ActionExecutorType;
import com.tuya.jupiter.client.domain.linkage.constants.EntityType;
import com.tuya.jupiter.client.domain.linkage.vo.LinkageActionVO;
import com.tuya.luban.biz.common.executor.ExecutorTypeEnum;
import com.tuya.luban.biz.config.ApolloDynamicConfig;
import com.tuya.luban.biz.enums.GatewayErrorEnum;
import com.tuya.luban.biz.service.lan.ILanGatewayRecoverService;
import com.tuya.luban.biz.service.lan.domains.LanRecoverContext;
import com.tuya.luban.biz.service.lan.domains.LinkageRule;
import com.tuya.luban.common.common.LubanContextUtil;
import com.tuya.luban.integration.athena.IAthenaDeviceIntegration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : patrickkk
 * @date 2022-08-19
 */
@Slf4j
public abstract class DefaultLanRuleExecutor implements ILanRuleExecutor {

    @Autowired
    @Lazy
    protected ILanGatewayRecoverService lanGatewayRecoverService;

    @Autowired
    protected ApolloDynamicConfig apolloDynamicConfig;

    @Autowired
    protected IAthenaDeviceIntegration athenaDeviceIntegration;

    void dealRuleEntityId(LanRecoverContext context, LinkageRule rule) {
        rule.clearRuleId();
        Map<String, String> devIdMap = context.getDevIdMap();
        Map<String, String> ruleMap = context.getRuleVOMap();
        //处理condition
        dealConditions(rule, devIdMap);
        //处理action
        dealActions(context, rule, devIdMap, ruleMap);
        rule.setOwnerId(context.getOwnerId());
    }

    @Override
    public void createRule(LanRecoverContext context, LinkageRule rule) {
        //预先处理
        preCreateRule(context, rule);
        //执行
        try {
            //模拟异常
            mockError();
            //幂等校验
            if (checkRepeat() && hasCreated(context, rule)) {
                log.info("ignore repeat create. rule = {}", JSON.toJSONString(rule));
                return;
            }
            //执行创建
            String ruleId = doCreateRule(context, rule);
            //记录创建结果
            addRecord(context, rule, ruleId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //异常捕获
            catchCreateRule(context, rule, e);
        }
    }

    /**
     * 是否检测重复创建
     *
     * @return
     */
    boolean checkRepeat() {
        return true;
    }

    private boolean hasCreated(LanRecoverContext context, LinkageRule rule) {
        String wgId = context.getGwId();
        String itemId = rule.getVrRuleId();
        Object extension = LubanContextUtil.getExtension(wgId);
        Map<String, String> recoverItemMap;
        if (extension == null) {
            recoverItemMap = lanGatewayRecoverService.getRecoverItemMap(wgId);
            log.info("checkRepeatRecover itemId = {}, recoverItemMap = {}", itemId, JSON.toJSONString(recoverItemMap));
            LubanContextUtil.setExtension(wgId, recoverItemMap);
        } else {
            recoverItemMap = (HashMap<String, String>) extension;
        }
        if (MapUtils.isEmpty(recoverItemMap)) {
            return false;
        }
        return recoverItemMap.containsKey(itemId);
    }

    /**
     * 记录恢复结果
     *
     * @param context
     * @param rule
     * @param ruleId
     */
    private void addRecord(LanRecoverContext context, LinkageRule rule, String ruleId) {
        lanGatewayRecoverService.addRecoverItem(context.getGwId(), rule.getVrRuleId(), ruleId);
    }

    private void mockError() {
        List<GatewayErrorEnum> mockErrors = getMockError();
        if (CollectionUtils.isEmpty(mockErrors)) {
            return;
        }
        mockErrors.forEach(mock -> lanGatewayRecoverService.mockError(mock));
    }

    abstract void preCreateRule(LanRecoverContext context, LinkageRule rule);

    abstract List<GatewayErrorEnum> getMockError();

    abstract String doCreateRule(LanRecoverContext context, LinkageRule rule);

    abstract void catchCreateRule(LanRecoverContext context, LinkageRule rule, Exception e);

    private void dealActions(LanRecoverContext context, LinkageRule rule, Map<String, String> devIdMap,
                             Map<String, String> ruleMap) {
        if (CollectionUtils.isEmpty(rule.getActions())) {
            return;
        }
        //遍历 actions
        rule.getActions().forEach(action -> {
            action.setId("");
            // 绑定entityId
            String entityId = getEntityId(devIdMap, ruleMap, action);
            String parentDevId = action.getParentDevId();
            if (parentDevId != null && devIdMap.containsKey(parentDevId)) {
                action.setParentDevId(devIdMap.get(parentDevId));
            }
            if (StringUtils.isNotEmpty(entityId)) {
                action.setEntityId(entityId);
            } else {
                log.warn("not find entityId,action = {}, devIdMap = {}", JSON.toJSONString(action), JSON.toJSONString(devIdMap));
            }
            if (action.getExtraProperty() != null) {
                action.getExtraProperty().put("gwId", context.getGwId());
            }
        });
    }

    private String getEntityId(Map<String, String> devIdMap, Map<String, String> ruleMap, LinkageActionVO action) {
        String entityId = devIdMap.get(action.getEntityId());
        if (StringUtils.equals(ActionExecutorType.DELAY, action.getActionExecutor())) {
            entityId = ActionExecutorType.DELAY;
        } else if (StringUtils.equals(ActionExecutorType.RULE_TRIGGER, action.getActionExecutor())) {
            entityId = ruleMap.get(action.getEntityId());
        }
        return entityId;
    }

    private void dealConditions(LinkageRule rule, Map<String, String> devIdMap) {
        if (CollectionUtils.isNotEmpty(rule.getConditions())) {
            //遍历 conditions
            rule.getConditions().forEach(condition -> {
                condition.setId("");
                if (!EntityType.TIMER_TRIG.getValue().equals(condition.getEntityType())) {
                    // 绑定entityId
                    condition.setEntityId(devIdMap.get(condition.getEntityId()));
                }
                String parentDevId = condition.getParentDevId();
                if (parentDevId != null && devIdMap.containsKey(parentDevId)) {
                    condition.setParentDevId(devIdMap.get(parentDevId));
                }
            });
        }
    }

    @Override
    public String getExecutorType() {
        return ExecutorTypeEnum.LAN_RULE_RECOVER.getType();
    }
}
