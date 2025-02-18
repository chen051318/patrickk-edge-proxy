package impl.stategy.strategy.impl.video;

import com.alibaba.fastjson.JSON;
import com.tuya.atop.client.domain.api.ApiRequestDO;
import com.tuya.edgegateway.atop.device.impl.strategy.annotation.EdgeStrategy;
import com.tuya.edgegateway.atop.device.impl.strategy.impl.AbstractReportProcessor;
import com.tuya.edgegateway.client.domain.cmd.constants.CmdBizTypeEnum;
import com.tuya.edgegateway.common.model.DeviceStrategy;
import com.tuya.edgegateway.manager.cmdissue.ICmdIssueManager;
import com.tuya.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;
import com.tuya.edgegateway.manager.kafka.domain.VideoRecordMessage;
import com.tuya.edgegateway.manager.kafka.producer.IKafkaCategoryMsgProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 视频录像列表上报
 *
 * @author patrickkk  2020/8/13 19:16
 */
@EdgeStrategy(tag = DeviceStrategy.Video.PLAYBACK_TAG)
@Service
@Slf4j
public class VideoRecordReport extends AbstractReportProcessor {
    @Resource
    private IKafkaCategoryMsgProducer kafkaCategoryMsgProducer;

    @Resource
    private ICmdIssueManager cmdIssueManager;

    @Override
    public void report(ApiRequestDO apiRequestDO, String data) {
        log.info("VideoRecordReport input: {}", data);
        if (StringUtils.isBlank(data)) {
            return;
        }

        VideoRecordRequest videoRecordRequest = JSON.parseObject(data, VideoRecordRequest.class);
        CmdIssueRecordDTO cmdIssueDTO = cmdIssueManager.queryCmdIssueBySn(videoRecordRequest.getSn(), false);
        if (cmdIssueDTO == null) {
            log.error("VideoRecordReport, 指令不存在,sn: {}", videoRecordRequest.getSn());
            return;
        }

        //【2】发送kafka消息
        Map<String, Object> map = new HashMap<>();
        map.put("sn", videoRecordRequest.getSn());
        map.put("bsn", cmdIssueDTO.getBsn());
        map.put("deviceId", cmdIssueDTO.getDeviceId());
        map.put("sumCount", videoRecordRequest.getSumCount());
        map.put("videoRecordList", videoRecordRequest.getVideoRecordList());


        onReceivedCmdResult(videoRecordRequest.getSn(), videoRecordRequest.getSuccess(),
                videoRecordRequest.getMessage(), JSON.toJSONString(map), CmdBizTypeEnum.VIDEO_RESULT);


        // 兼容老的方式发送kafka消息
        VideoRecordMessage videoRecordMessage = new VideoRecordMessage();
        videoRecordMessage.setData(map);
        kafkaCategoryMsgProducer.pushVideoRecordMessage(videoRecordMessage);
    }
}
