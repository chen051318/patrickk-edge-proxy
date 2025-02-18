package impl.stategy.strategy.impl.parking;

import com.tuya.edgegateway.atop.device.impl.strategy.impl.BaseReportDTO;
import lombok.Data;

/**
 * @author : patrickkk
 * @date 2021-04-15
 */
@Data
public class ParkingPaymentRequest extends BaseReportDTO {
    /**
     * 订单id true
     */
    private String orderNo;
    /**
     * 缴费类型，1: 固定车，2: 临停，3: 代缴，4: 无牌，5: 未知 true
     */
    private Integer feeType;
    /**
     * 车牌号 true
     *
     * @eg 浙A12345
     */
    private String plateNo;
    /**
     * 入场时间 true
     */
    private Long inboundTime;
    /**
     * 开始计费时间 true
     */
    private Long startTime;
    /**
     * 停车时长（分钟）true
     */
    private Integer elapsedTime;
    /**
     * 应收金额(分) true
     */
    private Integer payableAmount;
    /**
     * 实收金额(分) true
     */
    private Integer paidAmount;
    /**
     * 优惠金额(分) false
     */
    private Integer discountAmount;
}
