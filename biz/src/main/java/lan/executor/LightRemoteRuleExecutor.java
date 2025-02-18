package lan.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.tuya.luban.biz.enums.GatewayErrorEnum;
import com.tuya.luban.biz.service.devicecluster.IDeviceClusterManage;
import com.tuya.luban.biz.service.devicecluster.domain.LightRemoteDeviceDTO;
import com.tuya.luban.biz.service.devicecluster.domain.SubClusterDTO;
import com.tuya.luban.biz.service.lan.domains.ConfigInfo;
import com.tuya.luban.biz.service.lan.domains.LanRecoverContext;
import com.tuya.luban.biz.service.lan.domains.LinkageRule;
import com.tuya.luban.biz.service.lan.domains.recover.params.DeviceErrorParamsDTO;
import com.tuya.luban.client.enums.RuleConstant;
import com.tuya.luban.core.dao.ITaskHouseholdRelatDAO;
import com.tuya.luban.core.dao.domains.meta.LubanTaskHouseholdRelat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 照明遥控器
 *
 * @author : patrickkk
 * @date 2022-08-19
 */
@Slf4j
@Component
public class LightRemoteRuleExecutor extends DefaultLanRuleExecutor {

    @Resource
    private IDeviceClusterManage deviceClusterManage;

    @Autowired
    private ITaskHouseholdRelatDAO taskHouseholdRelatDAO;

    @Override
    void preCreateRule(LanRecoverContext context, LinkageRule rule) {
        rule.clearRuleId();
        Map<String, String> macMap = context.getMacMap();
        //处理entityId
        rule.getConditions().forEach(condition -> {
            String entityId = condition.getEntityId();
            condition.setEntityId(macMap.get(entityId));
        });
        rule.getActions().forEach(action -> {
            String entityId = action.getEntityId();
            action.setEntityId(macMap.get(entityId));
        });
    }

    @Override
    void catchCreateRule(LanRecoverContext context, LinkageRule rule, Exception e) {
        lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_LIGHT_REMOTE_SAVE_ERROR,
                DeviceErrorParamsDTO.builder().deviceName(rule.getName()).build(), e.getMessage());
    }

    @Override
    List<GatewayErrorEnum> getMockError() {
        return Lists.newArrayList(GatewayErrorEnum.WG_RE_LIGHT_REMOTE_SAVE_ERROR);
    }

    @Override
    public String doCreateRule(LanRecoverContext context, LinkageRule rule) {
        ConfigInfo config = context.getConfig();
        String recordId = config.getRecordId();
        String hid = config.getHid();
        LubanTaskHouseholdRelat relat = taskHouseholdRelatDAO.getByRecordIdAndHid(recordId, hid);
        //转换数据结构
        LightRemoteDeviceDTO lightRemoteDeviceDTO = toLightRemoteDeviceDTO(rule);
        log.info("lightRemoteDeviceDTO = {}", JSON.toJSONString(lightRemoteDeviceDTO));
        deviceClusterManage.saveLightRemoteDevice(relat.getId() + "", context.getUid(), lightRemoteDeviceDTO);
        return lightRemoteDeviceDTO.getRemoteDeviceId();
    }

    private LightRemoteDeviceDTO toLightRemoteDeviceDTO(LinkageRule rule) {
        String entityId = rule.getConditions().get(0).getEntityId();
        LightRemoteDeviceDTO lightRemoteDeviceDTO = new LightRemoteDeviceDTO();
        lightRemoteDeviceDTO.setVtClusterId(rule.getVrRuleId());
        lightRemoteDeviceDTO.setRemoteDeviceId(entityId);
        lightRemoteDeviceDTO.setLinkageRuleType(rule.getLinkageRuleType());
        Map<String, List<String>> subDeviceMap = new HashMap<>();
        rule.getActions().forEach(acion -> {
            JSONObject extraProperty = acion.getExtraProperty();
            String subClusterId = extraProperty.getString("subClusterId");
            subClusterId = subClusterId == null ? "" : subClusterId;
            List<String> deviceIds = subDeviceMap.computeIfAbsent(subClusterId, k -> new ArrayList<>());
            deviceIds.add(acion.getEntityId());
        });
        List<SubClusterDTO> subClusters = subDeviceMap.entrySet().stream().map(entry -> {
            SubClusterDTO subClusterDTO = new SubClusterDTO();
            subClusterDTO.setSubClusterId(rule.getVrRuleId() + "_" + entry.getKey());
            subClusterDTO.setDeviceIds(entry.getValue());
            return subClusterDTO;
        }).collect(Collectors.toList());
        lightRemoteDeviceDTO.setSubClusters(subClusters);
        return lightRemoteDeviceDTO;
    }

    @Override
    public String getUniqueKey() {
        return RuleConstant.SINGLE_LIGHT_REMOTE_DEVICE + "";
    }

    @Override
    public List<String> getUniqueKeyList() {
        return Lists.newArrayList(RuleConstant.SINGLE_LIGHT_REMOTE_DEVICE + "", RuleConstant.MULTI_LIGHT_REMOTE_DEVICE + "");
    }
}
