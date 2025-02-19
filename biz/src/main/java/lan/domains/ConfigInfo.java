
package lan.domains;

import com.x.luban.biz.domain.base.ToString;
import com.x.luban.biz.enums.LanTemplateVersionEnum;
import com.x.luban.biz.exception.BizErrorEnum;
import com.x.luban.biz.exception.BizException;
import com.x.luban.biz.service.template.domains.template.DpDTO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置文件模型
 *
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class ConfigInfo extends ToString {

    /**
     * 施工ID
     */
    private String constructionId;

    /**
     * 授权id
     */
    private String recordId;

    /**
     * 模板id
     */
    private String configId;

    /**
     * 配置版本
     */
    private String version;

    /**
     * 房间id
     */
    private String hid;

    /**
     * 房间信息列表
     */
    private List<RoomInfo> rooms;

    /**
     * 网关信息集
     */
    private List<GatewayDeviceInfo> gateways;

    /**
     * 待修改的设备dp信息
     * {
     * "${deviceId}":{
     * "${dpId1}":"按钮名称1",
     * "${dpId2}":"按钮名称2"
     * }
     * }
     */
    private Map<String, Map<String, String>> deviceDP;

    /**
     * dp预设信息
     * {
     * "$deviceId":dpList
     * }
     */
    private Map<String, List<DpDTO>> deviceDPValue = new HashMap<>(2);

    /**
     * 联动列表
     */
    private SceneInfo scenes;

    /**
     * 是否是多网关配置
     * 
     * @return
     */
    public boolean isMultiGatewayConfig() {
        if (version == null) {
            version = LanTemplateVersionEnum.V1_0.getVersion();
        }
        //1.0版本都是单网关
        if (LanTemplateVersionEnum.V1_0.equalCode(version)) {
            return false;
        }
        if (gateways == null || gateways.size() == 0) {
            throw new BizException(BizErrorEnum.DATA_ERROR, "网关上传配置信息错误");
        }
        List<String> gatewayMacList = toGatewayMacs();
        if (gatewayMacList.size() == 0) {
            throw new BizException(BizErrorEnum.DATA_ERROR, "网关上传配置信息错误,mac信息缺失");
        }
        //多网关
        return gatewayMacList.size() > 1;
    }

    /**
     * 不是多网关
     * 
     * @return
     */
    public boolean noMultiGatewayConfig() {
        return !isMultiGatewayConfig();
    }

    public List<String> toGatewayMacs() {
        return gateways.stream().map(GatewayDeviceInfo::getMac).filter(StringUtils::isNotEmpty).distinct()
                .collect(Collectors.toList());
    }

    public Map<String, String> toDeviceNameMap() {
        if (CollectionUtils.isNotEmpty(rooms)) {
            return rooms.stream().map(RoomInfo::getDevices).filter(CollectionUtils::isNotEmpty).flatMap(List::stream)
                    .collect(Collectors.toMap(DeviceInfo::getMac, DeviceInfo::getName, (a, b) -> a));
        } else if (CollectionUtils.isNotEmpty(gateways)) {
            return gateways.stream().map(GatewayDeviceInfo::toAllDeviceInfos).filter(CollectionUtils::isNotEmpty)
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(DeviceInfo::getMac, DeviceInfo::getName, (a, b) -> a));
        }
        return new HashMap<>();
    }

    /**
     * 获取子设备
     * 
     * @return
     */
    public Map<String, String> toSubDeviceNameMap() {
        if (CollectionUtils.isNotEmpty(rooms)) {
            return rooms.stream().map(RoomInfo::getDevices).filter(CollectionUtils::isNotEmpty).flatMap(List::stream)
                    .filter(deviceInfo -> StringUtils.isNotEmpty(deviceInfo.getMac()))
                    .collect(Collectors.toMap(DeviceInfo::getMac, DeviceInfo::getName, (a, b) -> a));
        } else if (CollectionUtils.isNotEmpty(gateways)) {
            return gateways.stream().map(GatewayDeviceInfo::getSubDevices).filter(CollectionUtils::isNotEmpty)
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(DeviceInfo::getMac, DeviceInfo::getName, (a, b) -> a));
        }
        return new HashMap<>();
    }
}
