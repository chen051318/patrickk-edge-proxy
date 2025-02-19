
package lan.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.x.asgard.client.domain.relation.vo.ImportDeviceVO;
import com.x.asgard.client.domain.relation.vo.ImportRoomVO;
import com.x.athena.client.domain.relation.DeviceTopoRelationVO;
import com.x.jupiter.client.domain.group.vo.MultiControlDetailVO;
import com.x.jupiter.client.domain.group.vo.MultiControlGroupVO;
import com.x.jupiter.client.domain.linkage.vo.LinkageConditionVO;
import com.x.jupiter.client.domain.linkage.vo.LinkageDeviceRuleShellVO;
import com.x.luban.biz.service.lan.domains.*;
import com.x.luban.biz.service.linkagerule.domain.LinkageRuleReqDTO;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author patrickkk
 * @Date 2020-07-06
 */
public class ConvertUtil {

    /**
     * 转换多控组对象
     *
     * @param importLinkageRuleVO
     * @return
     */
    public static MultiControlGroupVO getMultiControlGroupVO(LinkageRuleReqDTO importLinkageRuleVO) {
        MultiControlGroupVO multiControlGroupVO = new MultiControlGroupVO();
        multiControlGroupVO.setGroupType(1);
        multiControlGroupVO.setGroupName(importLinkageRuleVO.getName());

        List<MultiControlDetailVO> controlDetailVOS = importLinkageRuleVO.getActions().stream().map(linkageActionVO -> {
            MultiControlDetailVO multiControlDetailVO = new MultiControlDetailVO();
            multiControlDetailVO.setDevId(linkageActionVO.getEntityId());
            multiControlDetailVO.setDpId(linkageActionVO.getDpId());
            multiControlDetailVO.setEnabled(Boolean.TRUE);
            return multiControlDetailVO;
        }).collect(Collectors.toList());

        multiControlGroupVO.setGroupDetail(controlDetailVOS);

        return multiControlGroupVO;
    }

    /**
     * 转换多控组对象
     *
     * @param importLinkageRuleVO
     * @return
     */
    public static MultiControlGroupVO getMultiControlGroupVO(LinkageRule importLinkageRuleVO) {
        MultiControlGroupVO multiControlGroupVO = new MultiControlGroupVO();
        multiControlGroupVO.setGroupType(1);
        multiControlGroupVO.setGroupName(importLinkageRuleVO.getName());

        List<MultiControlDetailVO> controlDetailVOS = importLinkageRuleVO.getActions().stream().map(linkageActionVO -> {
            MultiControlDetailVO multiControlDetailVO = new MultiControlDetailVO();
            multiControlDetailVO.setDevId(linkageActionVO.getEntityId());
            multiControlDetailVO.setDpId(linkageActionVO.getDpId());
            multiControlDetailVO.setEnabled(Boolean.TRUE);
            return multiControlDetailVO;
        }).collect(Collectors.toList());

        multiControlGroupVO.setGroupDetail(controlDetailVOS);

        return multiControlGroupVO;
    }

    public static LinkageDeviceRuleShellVO getLinkageDeviceRuleShellVO(LinkageRule templateVO,
                                                                       List<LinkageConditionVO> conditions,
                                                                       LinkageConditionVO conditionVO) {
        LinkageDeviceRuleShellVO shellVO = new LinkageDeviceRuleShellVO();
        //无线开关信息
        shellVO.setDevId(conditionVO.getEntityId());
        shellVO.setDpId(Integer.valueOf(conditionVO.getEntitySubIds()));
        //自动化信息
        shellVO.setBackground("https://rule/cover/bedroom5.png");
        shellVO.setName(conditionVO.getEntityName() + "自动化");
        shellVO.setEnabled(Boolean.TRUE);
        shellVO.setConditions(conditions);
        shellVO.setActions(templateVO.getActions());
        return shellVO;
    }

    public static Map<String, String> getDeviceMap(ConfigInfo config, List<DeviceTopoRelationVO> relationList) {
        Map<String, MetaInfo> sceneMeta = config.getScenes().getMeta();
        Map<String, String> devIdMap = Maps.newHashMap();
        Map<String, DeviceTopoRelationVO> macMap = relationList.stream()
                .collect(Collectors.toMap(DeviceTopoRelationVO::getNodeId, v -> v, (a, b) -> a));
        for (Map.Entry<String, MetaInfo> entry : sceneMeta.entrySet()) {
            DeviceTopoRelationVO device = macMap.get(entry.getValue().getMac());
            devIdMap.put(entry.getKey(), device != null ? device.getDevId() : null);
        }
        return devIdMap;
    }

    public static List<ImportRoomVO> getImportRoomList(List<RoomInfo> roomList) {
        List<ImportRoomVO> result = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(roomList)) {
            for (RoomInfo room : roomList) {
                ImportRoomVO importRoom = new ImportRoomVO();
                importRoom.setName(room.getName());
                List<ImportDeviceVO> devices = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(room.getDevices())) {
                    for (DeviceInfo device : room.getDevices()) {
                        ImportDeviceVO importDevice = new ImportDeviceVO();
                        importDevice.setMac(device.getMac());
                        importDevice.setName(device.getName());
                        devices.add(importDevice);
                    }
                }
                importRoom.setDevices(devices);
                result.add(importRoom);
            }
        }
        return result;
    }
}
