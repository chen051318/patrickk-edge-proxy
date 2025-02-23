package cmdissue.instance.category.card;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cmdissue.instance.CmdIssueStrategy;
import cmdissue.instance.DefaultCmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 发卡器写卡片信息指令发送
 *
 * @author patrickkk
 * @date 2021/6/8 11:26 上午
 */
@CmdIssueStrategy(productType = "fkq_1w_1", vendorCode = "xinhuo")
@Component("fkq_1w_1_xinhuo")
@Slf4j
public class CardCmdIssueInstance extends DefaultCmdIssueInstance  implements InitializingBean {

    /**
     * redis缓存保存的时间间隔,10分钟
     */
    private static final Long SN_INTERVAL = 5L;


    @Resource
    private LokiClient<String, Long> lokiClient;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        try {
            if (StringUtils.equals("4", cmdIssueDTO.getDpid())) {//重置发卡器设备
                super.issue(cmdIssueDTO);
                return;
            }

            //计算获取xNum，sn-xNum存入redis
            int xNum = getxNum(cmdIssueDTO.getSn());

            //data内容转byte，并base64
            String data = cmdIssueDTO.getData();
            JSONObject dpDataObject = JSONObject.parseObject(data);
            byte[] bytes = dpDataObject.getObject(cmdIssueDTO.getDpid(), byte[].class);

            //dp指令下发
            Map<Integer, Object> map = new HashMap<>();
            if (StringUtils.equals("3", cmdIssueDTO.getDpid())) { //读取发卡器设备信息
                DeviceInfoVO deviceInfoVO = new DeviceInfoVO(xNum);
                byte[] deviceInfoBytes = DataPointConverter.getInstance().convert(deviceInfoVO);
                map.put(Integer.parseInt(cmdIssueDTO.getDpid()), Base64.encodeBase64String(deviceInfoBytes));
                cmdIssueDTO.setData(JSON.toJSONString(map));
                super.issue(cmdIssueDTO);
                return;
            }

            if (StringUtils.equals("1", cmdIssueDTO.getDpid())) { //读卡信息
                CardReadVO cardReadVO = DataPointConverter.getInstance().convert(bytes, CardReadVO.class);
                cardReadVO.setxNum(xNum);
                byte[] cardReadBytes = DataPointConverter.getInstance().convert(cardReadVO);
                log.info("read card cmd: {}", Base64.encodeBase64String(cardReadBytes));
                map.put(Integer.parseInt(cmdIssueDTO.getDpid()), Base64.encodeBase64String(cardReadBytes));
                cmdIssueDTO.setData(JSON.toJSONString(map));
                super.issue(cmdIssueDTO);
                return;
            }

            //写卡信息 dpid: 2
            //1.解析数据头
            byte[] cardHeadInfoByte = new byte[6];
            System.arraycopy(bytes, 0, cardHeadInfoByte, 0, 6);

            CardWriteVO cardHeadInfoVO = DataPointConverter.getInstance().convert(cardHeadInfoByte, CardWriteVO.class);
            cardHeadInfoVO.setxNum(xNum);
            //卡号的数据长度
            Integer datalength = cardHeadInfoVO.getDataLength();

            //2.截取数据内容, 从字节数组的第6字节开始
            byte[] cardContentByte = new byte[datalength];
            System.arraycopy(bytes, 6, cardContentByte, 0, datalength);

            //3.拼装byte[]，并重新base64
            cardHeadInfoByte = DataPointConverter.getInstance().convert(cardHeadInfoVO);
            byte[] byteData = ByteUtils.concat(cardHeadInfoByte, cardContentByte);

            //4.重新组装dpmap
            map.put(Integer.parseInt(cmdIssueDTO.getDpid()), Base64.encodeBase64String(byteData));
            cmdIssueDTO.setData(JSON.toJSONString(map));
            super.issue(cmdIssueDTO);
        } catch (Exception e) {
            log.error("CardCmdIssueInstance error", e);
        }
    }

    /**
     * 获取从新计算num ，sn缓存
     *
     * @param sn
     * @return
     */
    private int getxNum(Long sn) {
        //计算并获取xNum
        long xNum0 = lokiClient.opsForValue().increment(RedisKeys.Card.CARD_x_NUM_CACHE, 1);
        //取模, 指令中指令只有两个字节, 避免数字过大
        xNum0 = xNum0 & 0xffff;
        int xNum = (int) xNum0;

        //redis中保存xNum和指令sn的映射关系
        String key = String.format(RedisKeys.Card.CARD_SN_CACHE, xNum);
        lokiClient.opsForValue().set(key, sn, SN_INTERVAL, TimeUnit.MINUTES);
        return xNum;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        lokiClient.expire(RedisKeys.Card.CARD_x_NUM_CACHE, 10, TimeUnit.DAYS);
        log.info("CardCmdIssueInstance init xNum redis expire time finish");
    }
}
