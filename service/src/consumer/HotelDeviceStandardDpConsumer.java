package consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tuya.basic.mq.domain.KafkaMqData;
import com.tuya.hotel.biz.datapoint.base.AbstractDeviceDpConsumerService;
import com.tuya.hotel.biz.datapoint.base.DeviceDpConsumerCategoryCode2BeanNamePrefixMapper;
import com.tuya.hotel.biz.datapoint.base.DeviceDpConsumerServiceHelper;
import com.tuya.hotel.biz.datapoint.bo.DeviceDpBO;
import com.tuya.hotel.biz.device.service.IDeviceBizService;
import com.tuya.hotel.biz.es.HotelDeviceEsService;
import com.tuya.hotel.biz.es.HotelEsDeviceDO;
import com.tuya.hotel.biz.merchant.IHotelMerchantService;
import com.tuya.hotel.common.config.HotelConfigService;
import com.tuya.hotel.common.constant.RedisKey;
import com.tuya.hotel.common.enums.HotelCategoryCodeEnum;
import com.tuya.hotel.common.util.HotelRandomUtils;
import com.tuya.hotel.common.util.HotelRedisClient;
import com.tuya.hotel.common.util.IRedisClient;
import com.tuya.hotel.mq.dto.DeviceStandardDpDetailKafkaDTO;
import com.tuya.hotel.mq.dto.DeviceStandardDpInfo;
import com.tuya.hotel.mq.dto.DeviceStandardDpKafkaDTO;
import com.tuya.smart.client.domain.device.DataPointDO;
import com.tuya.vienna.monitor.concurrent.RunnableWrapper;
import com.tuya.vienna.monitor.concurrent.ThreadPoolExecutorHelper;
import com.tuya.vienna.monitor.kafka.AbstractKafkaConsumer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tuya.hotel.common.constant.HotelConstant.VIRTUAL_KG_DEVICE;
import static com.tuya.hotel.common.constant.RedisKey.DEVICE_DP_HIGH_FREQUENCY_REPORTING;
import static com.tuya.hotel.common.constant.RedisKey.buildRedisKey;

/**
 *
 * @Author: chenzp@tuya.com patrickkk
 * @date: 2020-05-23
 * @description: 设备dp上报的dp_code标准化后的消息消费逻辑
 */
public class HotelDeviceStandardDpConsumer extends AbstractKafkaConsumer<String> {

    @Autowired
    private HotelConfigService hotelConfigService;
    @Resource
    private HotelDeviceEsService hotelDeviceEsService;
    @Resource
    private DeviceDpConsumerServiceHelper deviceDpConsumerServiceHelper;
    @Resource
    private IHotelMerchantService hotelMerchantService;
    @Resource
    private HotelRedisClient hotelRedisClient;

    @Resource
    private ThreadPoolExecutorHelper dpExecutor;

    @Resource
    private IRedisClient redisClient;

    protected static Logger logger = LoggerFactory.getLogger(HotelDeviceStandardDpConsumer.class);

    @Override
    public boolean consume(String topic, KafkaMqData<String> data, int partition, long offset, String key) throws Exception {
        long processStartTime = System.currentTimeMillis();

        String jsonString = data.getData();
        DeviceStandardDpDetailKafkaDTO dataPointDOData;
        try {
            DeviceStandardDpKafkaDTO deviceStandardDpKafkaDTO = JSONObject.parseObject(jsonString, DeviceStandardDpKafkaDTO.class);
            String deviceDpJsonString = deviceStandardDpKafkaDTO.getData();
            dataPointDOData = JSONObject.parseObject(deviceDpJsonString, DeviceStandardDpDetailKafkaDTO.class);
        } catch (Exception e) {
            logger.warn("设备Dp上报标准化消息格式错误,原始jsonString:{}", jsonString, e);
            return true;
        }
        // 检测上报的 dp 是否为 hotel-saas 需要处理的 ==========  start ===============================================================
        String devId = dataPointDOData.getDevId();

        // 首先判断下 devId 是后续确定不会处理
        if (isWillIgnoreDevId(devId)) {
//            logger.info("后续不会进行业务处理的devId, devId:{}", devId);
            return true;
        }

        if (!dataPointDOData.isDeviceReport()) {
            logger.info("非设备主动上报, 不处理, reportType:{}, deviceId:{}, cost:{}", dataPointDOData.getReportType(),
                    dataPointDOData.getDevId(), System.currentTimeMillis() - processStartTime);
            return true;
        }

        String uid = dataPointDOData.getUid();
        // filter uid
        if (!hotelMerchantService.existsUid(uid)) {
            logger.warn("不存在的uid, 设备dp上报, uid:{}, deviceId:{}, cost: {}", uid, devId, System.currentTimeMillis() - processStartTime);
            return true;
        }

        // 过滤掉长时间未处理的 dp report
        filterTimeoutDpReport(dataPointDOData);
        if (CollectionUtils.isEmpty(dataPointDOData.getDataPoints()) && CollectionUtils.isEmpty(dataPointDOData.getStandardDpData())) {
            return true;
        }

        if (isHighFrequencyReport(devId)) {
            logger.info("dp high frequency report limited, partition: {}, devId: {}, jsonString: {}, cost: {}", partition, devId, jsonString, System.currentTimeMillis() - processStartTime);
        }

        //先缓存查询设备的品类，对不需要处理的品类进行过滤，减少每个请求都要查询es造成的额外开销
        String categoryCode = getCategoryFromCache(devId);
        if (StringUtils.isNotBlank(categoryCode) && !isContainHandleCategoryCode(categoryCode)) {
            logger.warn("不需要监听的category, 不进入后续处理, 缓存中获取的categoryCode:{},devId:{}, cost: {}", categoryCode, devId, System.currentTimeMillis() - processStartTime);
            markAsWillIgnoreDevId(devId, "ignore_category");
            return true;
        }
        // 查询设备信息, 如果缓存中没有category，缓存6小时
        HotelEsDeviceDO deviceDO = getDeviceDoAndPutCache(devId, StringUtils.isBlank(categoryCode));
        if (null == deviceDO) {
            logger.warn("不存在的device, 设备dp上报, uid:{}, deviceId:{}, cost: {}", uid, devId, System.currentTimeMillis() - processStartTime);
            return true;
        }

        //设备的categoryCode不在需要监听的category集合时，则过滤丢弃
        if (!isContainHandleCategoryCode(deviceDO.getCategoryCode())) {
            logger.warn("不需要监听的category, 不进入后续处理, categoryCode:{},deviceId:{}, cost: {}", deviceDO.getCategoryCode(), devId, System.currentTimeMillis() - processStartTime);
            markAsWillIgnoreDevId(devId, "ignore_category");
            return true;
        }

        // dp消费黑名单
        if (hotelConfigService.getDpNotHandleProductIdString().contains(deviceDO.getProductId())) {
            logger.info("deviceId:{}, pid:{}, 产品拉黑,丢弃!", devId, deviceDO.getProductId());
            markAsWillIgnoreDevId(devId, "black_pid");
            return true;
        }

        // move from doLogic(), 把转 dp BO 上提,方便处理
        List<DataPointDO> dataPointList = dataPointDOData.getDataPoints();
        List<DeviceStandardDpInfo> standardDpDataList = dataPointDOData.getStandardDpData();
        List<DeviceDpBO> deviceDpBOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(standardDpDataList) && !HotelCategoryCodeEnum.MS.getCode().equals(deviceDO.getCategoryCode())) {
            deviceDpBOList = standardDpDataList.stream().map(DeviceStandardDpInfo::toDeviceDpBO).collect(Collectors.toList());
        } else {
            deviceDpBOList = dataPointList.stream().map(DeviceDpBO::from).collect(Collectors.toList());
        }


        // 加一个直接过滤要处理的 dp 的消息
        filterNotHotelBizDpCode(deviceDpBOList, deviceDO, partition);

        if (CollectionUtils.isEmpty(deviceDpBOList)) {
//            logger.warn("没有需要处理的 categoryCode_dpCode, 不进入后续处理, partition:{}, categoryCode:{},deviceId:{}, cost: {}", partition, deviceDO.getCategoryCode(), devId, System.currentTimeMillis() - processStartTime);
            return true;
        }
        // 检测上报的 dp 是否为 hotel-saas 需要处理的 ==========  end ===============================================================

        /// 实际处理业务的地方
        return doLogic(partition, offset, key, deviceDpBOList, deviceDO);

    }

    private void markAsWillIgnoreDevId(String devId, String reason) {
        reason = StringUtils.isNotBlank(reason) ? reason : "defalut";
        redisClient.set(RedisKey.buildWillIgnoreDevIdKey(devId), reason, (86400 + HotelRandomUtils.nextInt( 3600)));
    }

    private boolean isWillIgnoreDevId(String devId) {
        Object value = redisClient.get(RedisKey.buildWillIgnoreDevIdKey(devId));
        return null != value;
    }


    private void filterNotHotelBizDpCode(List<DeviceDpBO> deviceDpBOList, HotelEsDeviceDO deviceDO, int partition) {
        if (CollectionUtils.isEmpty(deviceDpBOList)) {
            return;
        }

        //  做一下开关, 上线发布有问题时不过滤
        if(!hotelConfigService.isFilterNotHotelBizDpCode()) {
            return;
        }

        Iterator<DeviceDpBO> standardDpInfoIterator = deviceDpBOList.iterator();
        while (standardDpInfoIterator.hasNext()) {
            DeviceDpBO standardDpInfo = standardDpInfoIterator.next();
            if (Objects.isNull(standardDpInfo)) {
                continue;
            }
            if (!isSupportDpCode(deviceDO, deviceDO.getCategoryCode(), standardDpInfo.getCode())) {
                standardDpInfoIterator.remove();
//                logger.warn("不需要业务处理的 categoryCode_dpCode, partition: {}, deviceId:{}, categoryCode:{}, dpCode:{}", partition, deviceDO.getDevId(), deviceDO.getCategoryCode(), standardDpInfo.getCode());
            }
        }
    }


    @Resource
    private IDeviceBizService deviceBizService;

    private boolean isSupportDpCode(HotelEsDeviceDO deviceDO, String categoryCode, String dpCode) {
        String dpConsumerName = DeviceDpConsumerCategoryCode2BeanNamePrefixMapper.getBeanNamePrefixByCategoryCode(categoryCode);
        if (VIRTUAL_KG_DEVICE.equals(dpConsumerName)) {
            String uid = deviceDO.getUid();
            String constructionRoomId = deviceDO.getConstructionRoomId();
            String devId = deviceDO.getDevId();

            // virtual_kg,  dpCode 转换成 virtualCategoryCode
            String virtualCategoryCode = deviceBizService.getVirtualCategoryCodeWithCache(uid, constructionRoomId, devId, dpCode);
            return deviceDpConsumerServiceHelper.isHotelSupportCategoryDpCodes(dpConsumerName, virtualCategoryCode);
        } else {
            // 非 virtual_kg, 直接判断处理
            return deviceDpConsumerServiceHelper.isHotelSupportCategoryDpCodes(dpConsumerName, dpCode);
        }
    }



    private void filterTimeoutDpReport(DeviceStandardDpDetailKafkaDTO dataPointDOData) {

        if (hotelConfigService.getDpTimeoutSwitch()) {
            // 消息超时不消费
            if (CollectionUtils.isNotEmpty(dataPointDOData.getStandardDpData())) {
                Iterator<DeviceStandardDpInfo> standardDpInfoIterator = dataPointDOData.getStandardDpData().iterator();
                while (standardDpInfoIterator.hasNext()) {
                    DeviceStandardDpInfo standardDpInfo = standardDpInfoIterator.next();
                    if (Objects.isNull(standardDpInfo)) {
                        continue;
                    }
                    Long time = standardDpInfo.getTime();
                    if (time != null && time != 0) {
                        int timeoutSecond = hotelConfigService.getDpTimeout();
                        if (System.currentTimeMillis() - standardDpInfo.getTime() > timeoutSecond * 1000L) {
                            logger.warn("dp上报超时, deviceId:{}", JSON.toJSONString(standardDpInfo));
                            standardDpInfoIterator.remove();
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(dataPointDOData.getDataPoints())) {
                Iterator<DataPointDO> dpIterator = dataPointDOData.getDataPoints().iterator();
                while (dpIterator.hasNext()) {
                    DataPointDO dpInfo = dpIterator.next();
                    if (Objects.isNull(dpInfo)) {
                        continue;
                    }
                    Long time = dpInfo.getTime();
                    if (time != null && time != 0) {
                        int timeoutSecond = hotelConfigService.getDpTimeout();
                        if (System.currentTimeMillis() - time > timeoutSecond * 1000L) {
                            logger.warn("dp上报超时, deviceId:{}", JSON.toJSONString(dpInfo));
                            dpIterator.remove();
                        }
                    }
                }
            }
        }
    }


    private boolean isHighFrequencyReport(String devId) {
        if (hotelConfigService.getDeviceHighFrequencyReportLimitSwitch()) {
            try {
                String key = buildRedisKey(DEVICE_DP_HIGH_FREQUENCY_REPORTING, devId);
                long count = redisClient.incr(key, 1, hotelConfigService.getDeviceHighFrequencyReportingIntervalSeconds());
                if (count > hotelConfigService.getDeviceHighFrequencyReportingCountThreshold()) {
//                    logger.warn("设备短时高频重复上报的dp, deviceId: {}", devId);
                    return true;
                }
            } catch (Exception e) {
                logger.error("", e);
            }
            return false;
        }
        return false;
    }

    private boolean doLogic(int partition, long offset, String key, List<DeviceDpBO> deviceDpBOList, HotelEsDeviceDO deviceDO) throws IOException {

        String devId = deviceDO.getDevId();

        AbstractDeviceDpConsumerService dpConsumerService =
                deviceDpConsumerServiceHelper.getBeanByName(deviceDO.getCategoryCode());
        if (Objects.isNull(dpConsumerService)) {
            logger.info("deviceId:{}, categoryCode:{}, 暂未获取到对应的处理器,丢弃!", devId, deviceDO.getCategoryCode());
            return true;
        }


        logger.info("收到设备状态上报标准化Dp消息, categoryCode:{}, deviceDpBOList:{}", deviceDO.getCategoryCode(), JSON.toJSONString(deviceDpBOList));

        // 实际处理 dp 上报的 ==========   start  ===============================================================

        // 如果未打印日志, 先排查设备的categoryCode是否在需要监听的category集合中
        boolean isAsync = hotelConfigService.isAsyncDpReportCategory(deviceDO.getCategoryCode());


        for (DeviceDpBO deviceDpBO : deviceDpBOList) {
            if (isAsync) {
                dpExecutor.execute(RunnableWrapper.of(() -> dpConsumerService.processDpMessage(deviceDpBO, deviceDO, partition, offset, key), "dpExecutor", 1000));
            } else {
                dpConsumerService.processDpMessage(deviceDpBO, deviceDO, partition, offset, key);
            }
        }

        // 实际处理 dp 上报的 ==========   end  ===============================================================

        return true;
    }

    private boolean isContainHandleCategoryCode(String categoryCode) {
        Set<String> productSet = hotelConfigService.getDpHandleCategoryCodeSet();
        return productSet.contains(categoryCode);
    }

    @Override
    public int getRetryTimes() {
        return 0;
    }

    @Override
    protected boolean printLog() {
        // 父类不打印日志
        return false;
    }

    /**
     * 从缓存中获取设备品类
     *
     * @param deviceId
     * @return
     */
    private String getCategoryFromCache(String deviceId) {
        Object obj = hotelRedisClient.hget(getKey(), deviceId);
        return obj == null ? null : (String) obj;
    }

    /**
     * 从es查询设备数据并写入缓存，时效6小时
     *
     * @param deviceId
     * @return
     */
    private HotelEsDeviceDO getDeviceDoAndPutCache(String deviceId, boolean putToCache) {
        HotelEsDeviceDO deviceDO = hotelDeviceEsService.getByDeviceId(deviceId);
        if (putToCache && null != deviceDO && StringUtils.isNotBlank(deviceDO.getCategoryCode())) {
            hotelRedisClient.hset(getKey(), deviceId, deviceDO.getCategoryCode(), 6 * 3600);
        }
        return deviceDO;
    }

    private String getKey() {
        return RedisKey.buildRedisKeyWithEnvAndHotelPreKey(RedisKey.DEVICE_CATEGORY_CACHE_MAP);
    }
}
