package impl.stategy.strategy.impl.devicelog;

import com.tuya.edgegateway.atop.device.impl.strategy.impl.BaseReportDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author patrickkk
 * @date 2020/5/11
 */
@Getter
@Setter
public class DeviceLogUploadRequest extends BaseReportDTO {
    private String fileId;
}
