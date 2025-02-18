package consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tuya.ai.speech.client.mq.SpeechVoiceEventDataWrapper;
import com.tuya.basic.mq.domain.KafkaMqData;
import com.tuya.hotel.biz.es.HotelDeviceEsService;
import com.tuya.hotel.biz.es.HotelEsDeviceDO;
import com.tuya.hotel.biz.roomdetection.IRoomDetectionRoomStatusBizService;
import com.tuya.hotel.mq.dto.SpeechVoiceEventDTO;
import com.tuya.vienna.monitor.kafka.AbstractKafkaConsumer;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * SpeechVoiceEventConsumer.java
 *
 * 处理  speech_voice_event 消息
 *
 * "topic":"speech_voice_event"
 * {"type":"hotel_nobody","devId":"vdevo167049388709641","asrTest":"","createTime":""}
 *
 * 调用该接口的音响，5分钟内有人声识别，就会触发。
 *
 * @author chenzp@tuya.com patrickkk
 * @since 2023/3/9 15:04
 **/
@Slf4j
public class SpeechVoiceEventConsumer extends AbstractKafkaConsumer<Object> {


    @Resource
    private IRoomDetectionRoomStatusBizService roomDetectionRoomStatusBizService;
    @Resource
    private HotelDeviceEsService hotelDeviceEsService;

    public static final String EVENT_TYPE_HOTEL_NOBODY = "speech_voice_event";



    @Override
    protected boolean consume(String topic, KafkaMqData<Object> data, int partition, long offset, String key) throws Exception {

        Object bizData = data.getData();
        if (data == null || bizData == null) {
            return true;
        }

        SpeechVoiceEventDTO eventDTO = null;

        if (bizData instanceof String) {
            log.info("bizData is String");
            eventDTO = JSON.parseObject((String) bizData, SpeechVoiceEventDTO.class);
        } else if (bizData instanceof SpeechVoiceEventDataWrapper) {
            log.info("bizData is SpeechVoiceEventDataWrapper");
            SpeechVoiceEventDataWrapper wrapper = (SpeechVoiceEventDataWrapper) bizData;
            eventDTO = new SpeechVoiceEventDTO();
            eventDTO.setType(wrapper.getType());
            eventDTO.setTts(wrapper.getTts());
            eventDTO.setDeviceId(wrapper.getDeviceId());
            eventDTO.setCreateTime(wrapper.getCreateTime());

        } else if (bizData instanceof JSONObject) {
            log.info("bizData is JSONObject");
            JSONObject jsonObject = (JSONObject) bizData;
            eventDTO = new SpeechVoiceEventDTO();
            eventDTO.setType(jsonObject.getString("type"));
            eventDTO.setTts(jsonObject.getString("tts"));
            eventDTO.setDeviceId(jsonObject.getString("deviceId"));
            eventDTO.setCreateTime(jsonObject.getLong("createTime"));
        }


        if (null == eventDTO || !eventDTO.valuesValid()) {
            log.info("处理  speech_voice_event 消息, 消息值不正确, eventDTO", bizData);
        }

        if (!EVENT_TYPE_HOTEL_NOBODY.equals(eventDTO.getType())) {
            log.info("处理  speech_voice_event 消息, 无需处理的消息类型, eventDTO", bizData);
        }

        HotelEsDeviceDO hotelEsDeviceDO = hotelDeviceEsService.getByDeviceId(eventDTO.getDeviceId());
        if (Objects.isNull(hotelEsDeviceDO)) {
            log.info("处理  speech_voice_event 消息, 设备不存在, devId: {}", eventDTO.getDeviceId());
        }

        // 语音音箱的无人检测上报,直接更新房间的数据

        roomDetectionRoomStatusBizService.updateLatestTimeOfPersonPresence(hotelEsDeviceDO.getHotelRoomId(), eventDTO.getCreateTime(), eventDTO.getDeviceId());

        return true;
    }


}
