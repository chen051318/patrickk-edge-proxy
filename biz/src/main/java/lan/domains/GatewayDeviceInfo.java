
package lan.domains;

import com.google.common.collect.Lists;
import com.x.luban.biz.domain.base.ToString;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class GatewayDeviceInfo extends ToString {

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备mac地址
     */
    private String mac;

    /**
     * 网关id（服务端补充的）
     */
    private String gatewayId;

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 子设备集
     */
    private List<DeviceInfo> subDevices;

    public List<DeviceInfo> toAllDeviceInfos() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setMac(mac);
        deviceInfo.setName(name);
        deviceInfo.setRoomName(roomName);
        if (CollectionUtils.isEmpty(subDevices)) {
            return Lists.newArrayList(deviceInfo);
        }
        ArrayList<DeviceInfo> deviceInfos = new ArrayList<>(subDevices);
        deviceInfos.add(deviceInfo);
        return deviceInfos;
    }

    public List<RoomInfo> toRoomInfos() {
        Map<String, List<DeviceInfo>> deviceRoomMap = toAllDeviceInfos().stream()
                .collect(Collectors.groupingBy(DeviceInfo::toRoomName));
        return deviceRoomMap.entrySet().stream().map(entry -> {
            RoomInfo roomInfo = new RoomInfo();
            roomInfo.setName(entry.getKey());
            roomInfo.setDevices(entry.getValue());
            return roomInfo;
        }).collect(Collectors.toList());
    }
}
