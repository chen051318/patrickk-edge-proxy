package impl.stategy.strategy.impl.parking;

import com.x.edgegateway.atop.device.impl.strategy.impl.BaseReportDTO;
import lombok.Data;

/**
 * 当前车信息
 *
 * @author : patrickkk
 * @date 2021-05-07
 */
@Data
public class ParkingCurrentCarRequest extends BaseReportDTO {
    /**
     * 车牌 true
     *
     * @eg 浙A12345
     */
    private String plateNo;

    /**
     * 抓拍时间(13位时间戳) true
     */
    private Long captureTime;
}
