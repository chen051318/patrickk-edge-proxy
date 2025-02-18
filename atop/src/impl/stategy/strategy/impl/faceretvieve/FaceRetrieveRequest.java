package impl.stategy.strategy.impl.faceretvieve;

import com.tuya.edgegateway.atop.device.impl.strategy.impl.BaseReportDTO;
import lombok.Data;

import java.util.List;

/**
 * @author : patrickkk
 * @date 2021-09-27
 */
@Data
public class FaceRetrieveRequest extends BaseReportDTO {
    private List<FaceRetrieveItem> faceRetrieveItems;

    @Data
    public static class FaceRetrieveItem {
        /**
         * 人脸图片id
         */
        private String faceImageId;
        /**
         * 背景图片id
         */
        private String bgImageId;
        /**
         * 设备id
         */
        private String deviceId;
        /**
         * 相似度分值
         */
        private Float score;
        /**
         * 图片抓拍时间
         */
        private Long captureTime;
    }
}
