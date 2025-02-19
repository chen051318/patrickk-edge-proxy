
package lan.domains;

import com.x.luban.biz.domain.base.ToString;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class DeviceInfo extends ToString {

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备mac地址
     */
    private String mac;

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 设备id(网关存的json无此字段)
     */
    private String deviceId;

    public String toRoomName() {
        if (StringUtils.isEmpty(roomName)) {
            return "";
        }
        return roomName;
    }
}
