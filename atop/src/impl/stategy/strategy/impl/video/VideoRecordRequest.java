package impl.stategy.strategy.impl.video;

import com.x.edgegateway.atop.device.impl.strategy.impl.BaseReportDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author patrickkk  2020/8/13 19:16
 */
@Getter
@Setter
public class VideoRecordRequest extends BaseReportDTO {
    /**
     * 总记录数
     */
    private int sumCount;

    /**
     * 视频列表
     */
    private List<VideoRecordVO> videoRecordList;
}
