package lan.domains;

import com.x.luban.biz.domain.base.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class RoomInfo extends ToString {

    /**
     * 房间名称
     */
    private String name;
    /**
     * 房间内的设备列表
     */
    private List<DeviceInfo> devices;
}
