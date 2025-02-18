
package lan.domains.recover.params;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author : patrickkk
 * @date 2021-11-12
 */
@Data
@Builder
public class DeviceErrorParamsDTO implements Serializable {

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * dpId
     */
    private String dpId;

    /**
     * dp名称
     */
    private String dpName;

    /**
     * dp值
     */
    private String dpValue;
}
