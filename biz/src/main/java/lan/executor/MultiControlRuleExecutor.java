
package lan.executor;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tuya.athena.client.domain.device.DeviceVO;
import com.tuya.caesar.client.domain.dp.DpPublishOptions;
import com.tuya.jupiter.client.domain.group.vo.MultiControlGroupVO;
import com.tuya.luban.biz.enums.GatewayErrorEnum;
import com.tuya.luban.biz.service.construction.domain.DeviceDpInfo;
import com.tuya.luban.biz.service.lan.domains.LanRecoverContext;
import com.tuya.luban.biz.service.lan.domains.LinkageRule;
import com.tuya.luban.biz.service.lan.domains.recover.params.SceneErrorParamsDTO;
import com.tuya.luban.biz.service.lan.utils.ConvertUtil;
import com.tuya.luban.client.enums.RuleConstant;
import com.tuya.luban.integration.caesar.IDpPublishIntegration;
import com.tuya.luban.integration.jupiter.IMultiControlGroupIntegration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 多控组
 * 
 * @author : patrickkk
 * @date 2022-08-19
 */
@Slf4j
@Component
public class MultiControlRuleExecutor extends DefaultLanRuleExecutor {

    private static final String XIMENG_CHANGE = "change";

    private static final String XIMENG_SCENE = "scene";

    @Autowired
    private IDpPublishIntegration dpPulishIntegration;

    @Resource
    private IMultiControlGroupIntegration multiControlGroupIntegration;

    @Override
    void preCreateRule(LanRecoverContext context, LinkageRule rule) {
        //处理entityId
        dealRuleEntityId(context, rule);
    }

    @Override
    void catchCreateRule(LanRecoverContext context, LinkageRule rule, Exception e) {
        lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_GROUP_SAVE_ERROR,
                SceneErrorParamsDTO.builder().sceneName(rule.getName()).build(), e.getMessage());
        log.warn("constructionBindService.saveDeviceGroup error,uid={},ownerId={},importLinkageRuleVO={}",
                context.getUid(), context.getOwnerId(), JSON.toJSONString(rule));
    }

    @Override
    List<GatewayErrorEnum> getMockError() {
        return Lists.newArrayList(GatewayErrorEnum.WG_RE_GROUP_SAVE_ERROR);
    }

    @Override
    public String doCreateRule(LanRecoverContext context, LinkageRule rule) {
        //保存多控组
        MultiControlGroupVO multiControlGroupVO = ConvertUtil.getMultiControlGroupVO(rule);
        //检测西蒙设备，并发送dp
        checkXimengDeviceAndSendDp(multiControlGroupVO);
        MultiControlGroupVO groupVO = multiControlGroupIntegration.saveMultiGroup(multiControlGroupVO, context.getUid(),
                context.getOwnerId());
        return groupVO.getId() + "";
    }


    /**
     * 检测西蒙设备，并发送dp
     *
     * @param multiControlGroupVO
     */
    private void checkXimengDeviceAndSendDp(MultiControlGroupVO multiControlGroupVO) {
        List<DeviceDpInfo> deviceDpInfos = multiControlGroupVO.getGroupDetail().stream()
                .map(multiControlDetailVO -> new DeviceDpInfo(multiControlDetailVO.getDevId(),
                        multiControlDetailVO.getDpId()))
                .collect(Collectors.toList());
        checkXimengDeviceAndSendDp(deviceDpInfos, XIMENG_CHANGE);
    }

    /**
     * 检测西蒙设备，并发送dp
     *
     * @param deviceDpInfos
     * @param dpCode
     */
    private void checkXimengDeviceAndSendDp(List<DeviceDpInfo> deviceDpInfos, String dpCode) {
        try {
            List<String> multiControlPids = apolloDynamicConfig.getLanTemplateConfig().getMultiControlPids();
            if (CollectionUtils.isEmpty(multiControlPids)) {
                log.info("no find multiControlPids");
                return;
            }
            Set<String> multiControlPidSet = new HashSet<>(multiControlPids);
            List<String> devIds = deviceDpInfos.stream().map(DeviceDpInfo::getDeviceId).collect(Collectors.toList());
            List<DeviceVO> devices = athenaDeviceIntegration.getByIds(devIds);
            if (CollectionUtils.isEmpty(devices)) {
                log.info("no find devices");
                return;
            }
            String uid = devices.get(0).getUid();
            Set<String> sendDpDevIdSet = devices.stream()
                    .filter(deviceVO -> multiControlPidSet.contains(deviceVO.getProductId())).map(DeviceVO::getId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(sendDpDevIdSet)) {
                log.info("no find sendDpDevIdSet");
                return;
            }
            String sendDpId = apolloDynamicConfig.getLanTemplateConfig().getXimengMultiDpId();
            String sendDpValue = apolloDynamicConfig.getLanTemplateConfig().getXimengMultiDpValue();
            if (XIMENG_SCENE.equals(dpCode)) {
                sendDpId = apolloDynamicConfig.getLanTemplateConfig().getXimengSceneDpId();
                sendDpValue = apolloDynamicConfig.getLanTemplateConfig().getXimengSceneDpValue();
            }
            DpPublishOptions options = DpPublishOptions.create().from("luban").uid(uid).reason("施工恢复，预先激活设备多控");
            for (DeviceDpInfo deviceDpInfo : deviceDpInfos) {
                if (sendDpDevIdSet.contains(deviceDpInfo.getDeviceId())) {
                    String devId = deviceDpInfo.getDeviceId();
                    Integer dpId = deviceDpInfo.getDpId();
                    String dpValue = sendDpValue + dpId;
                    Map<String, String> dps = Map.of(sendDpId, dpValue);
                    dpPulishIntegration.issueDataPoint(devId, JSON.toJSONString(dps), options);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String getUniqueKey() {
        return RuleConstant.MULTI_CONTROL_GROUP + "";
    }
}
