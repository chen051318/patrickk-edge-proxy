
package lan.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tuya.arthas.client.enums.DataPointPropertyTypeEnum;
import com.tuya.asgard.client.domain.relation.vo.ImportResVO;
import com.tuya.athena.client.domain.device.DeviceRichVO;
import com.tuya.athena.client.domain.relation.DeviceTopoRelationVO;
import com.tuya.basic.client.util.MD5Util;
import com.tuya.caesar.client.domain.dp.DpPublishOptions;
import com.tuya.jupiter.client.domain.linkage.LinkageRuleBindDO;
import com.tuya.luban.biz.cache.RedisKey;
import com.tuya.luban.biz.common.async.AsyncEventHandler;
import com.tuya.luban.biz.common.executor.ExecutorTypeEnum;
import com.tuya.luban.biz.common.executor.IExecutorHandler;
import com.tuya.luban.biz.config.ApolloDynamicConfig;
import com.tuya.luban.biz.constant.LubanBizConstant;
import com.tuya.luban.biz.domain.template.RoomVO;
import com.tuya.luban.biz.domain.template.TemplateDetailVO;
import com.tuya.luban.biz.enums.GatewayErrorEnum;
import com.tuya.luban.biz.enums.LanGatewayRecoverItemEnum;
import com.tuya.luban.biz.eventbus.event.task.LanRecoverEvent;
import com.tuya.luban.biz.exception.BizErrorEnum;
import com.tuya.luban.biz.exception.BizException;
import com.tuya.luban.biz.helper.DeviceTagHelper;
import com.tuya.luban.biz.service.cnstr.manager.ReportManager;
import com.tuya.luban.biz.service.lan.ILanGatewayRecoverService;
import com.tuya.luban.biz.service.lan.ILanGatewayService;
import com.tuya.luban.biz.service.lan.domains.BindInfo;
import com.tuya.luban.biz.service.lan.domains.ConfigInfo;
import com.tuya.luban.biz.service.lan.domains.DeviceInfo;
import com.tuya.luban.biz.service.lan.domains.GatewayDeviceInfo;
import com.tuya.luban.biz.service.lan.domains.LanRecoverContext;
import com.tuya.luban.biz.service.lan.domains.LinkageRule;
import com.tuya.luban.biz.service.lan.domains.MetaInfo;
import com.tuya.luban.biz.service.lan.domains.RoomInfo;
import com.tuya.luban.biz.service.lan.domains.RulePanel;
import com.tuya.luban.biz.service.lan.domains.recover.GatewayRecoverDTO;
import com.tuya.luban.biz.service.lan.domains.recover.params.DeviceErrorParamsDTO;
import com.tuya.luban.biz.service.lan.executor.ILanRuleExecutor;
import com.tuya.luban.biz.service.lan.utils.ConvertUtil;
import com.tuya.luban.biz.service.taskrecord.taskturning.ITaskTurningManage;
import com.tuya.luban.biz.service.taskrecord.taskturning.domains.TaskStatusKafkaDTO;
import com.tuya.luban.biz.service.template.ITemplateConfigManage;
import com.tuya.luban.biz.service.template.ITemplateManage;
import com.tuya.luban.biz.service.template.domains.DeviceDTO;
import com.tuya.luban.biz.service.template.domains.TemplateDTO;
import com.tuya.luban.biz.service.template.domains.template.DpDTO;
import com.tuya.luban.biz.service.template.domains.template.SubDeviceDTO;
import com.tuya.luban.biz.service.template.util.TemplateUtil;
import com.tuya.luban.biz.util.ValidateUtils;
import com.tuya.luban.client.domains.response.recover.GatewayRecoverItemProgressDTO;
import com.tuya.luban.client.domains.response.recover.GatewayRecoverProgressDTO;
import com.tuya.luban.client.domains.response.template.ProductInfo;
import com.tuya.luban.client.enums.TaskStatusEnum;
import com.tuya.luban.common.cache.redis.RedisHelper;
import com.tuya.luban.common.utils.CommonPoolUtil;
import com.tuya.luban.common.utils.CommonUtils;
import com.tuya.luban.common.utils.JacksonUtils;
import com.tuya.luban.common.utils.trace.ProxyTrace;
import com.tuya.luban.common.utils.trace.TraceUtil;
import com.tuya.luban.core.dao.IGatewayRecoverDAO;
import com.tuya.luban.core.dao.ITaskRecordDAO;
import com.tuya.luban.core.dao.domains.meta.LubanTaskRecord;
import com.tuya.luban.core.dao.domains.recover.GatewayRecoverDO;
import com.tuya.luban.core.dao.domains.recover.GatewayRecoverQuery;
import com.tuya.luban.integration.apollo.IGatewayMIntegration;
import com.tuya.luban.integration.asgard.IProAppMServiceIntegration;
import com.tuya.luban.integration.athena.IAthenaDeviceIntegration;
import com.tuya.luban.integration.athena.IDeviceTopoRelationIntegration;
import com.tuya.luban.integration.caesar.IDpPublishIntegration;
import com.tuya.luban.integration.jupiter.ILinkageDeviceRuleShellIntegration;
import com.tuya.luban.integration.jupiter.ILinkageRuleBindIntegration;
import com.tuya.luban.integration.jupiter.ILinkageRuleServiceIntegration;
import com.tuya.luban.integration.jupiter.IMultiControlGroupIntegration;
import com.tuya.luban.integration.numen.IGroupUserIntegration;
import com.tuya.luban.integration.numen.ILocationIntegration;
import com.tuya.luban.integration.numen.IRoomMIntegration;
import com.tuya.luban.integration.numen.IRoomRelationMIntegration;
import com.tuya.luban.integration.numen.IRoomSearchBizIntegration;
import com.tuya.luban.integration.zeus.IDpWriteMServiceIntegration;
import com.tuya.numen.client.domain.group.vo.GroupUserVO;
import com.tuya.numen.client.domain.locaiton.LocationVO;
import com.tuya.numen.client.domain.room.enums.RelationTypeEnum;
import com.tuya.numen.client.domain.room.vo.DeviceRoomRelationVO;
import com.tuya.numen.client.domain.room.vo.RoomBeanVO;
import com.tuya.numen.client.domain.room.vo.RoomVOV2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Component("lanGatewayService")
public class LanGatewayService implements ILanGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(LanGatewayService.class);

    @Autowired
    private RedisHelper redisHelper;

    @Resource
    private IProAppMServiceIntegration proAppMServiceIntegration;

    @Resource
    private IDeviceTopoRelationIntegration deviceTopoRelationIntegration;

    @Resource
    private IDpWriteMServiceIntegration dpWriteMServiceIntegration;

    @Resource
    private ILinkageRuleBindIntegration linkageRuleBindIntegration;

    @Resource
    private ILinkageRuleServiceIntegration linkageRuleServiceIntegration;

    @Resource
    private IMultiControlGroupIntegration multiControlGroupIntegration;

    @Resource
    private ILinkageDeviceRuleShellIntegration linkageDeviceRuleShellIntegration;

    @Autowired
    private AsyncEventHandler eventHandler;

    @Resource
    private DeviceTagHelper deviceTagHelper;

    @Autowired
    private IDpPublishIntegration dpPulishIntegration;

    @Autowired
    private IAthenaDeviceIntegration athenaDeviceIntegration;

    @Autowired
    private IGroupUserIntegration groupUserIntegration;

    @Autowired
    private ApolloDynamicConfig apolloDynamicConfig;

    @Autowired
    private ILanGatewayRecoverService lanGatewayRecoverService;

    @Autowired
    private IExecutorHandler executorHandler;

    @Resource
    private IRoomSearchBizIntegration roomSearchBizIntegration;

    @Resource
    private IRoomMIntegration roomMIntegration;

    @Resource
    private IRoomRelationMIntegration roomRelationMIntegration;

    @Resource
    private ILocationIntegration locationIntegration;

    @Resource
    private IGatewayMIntegration gatewayMIntegration;

    @Resource
    private ITaskRecordDAO taskRecordDAO;

    @Resource
    private ITemplateManage templateManage;

    @Resource
    private ITemplateConfigManage templateConfigManage;

    @Resource
    private ITaskTurningManage taskTurningManage;

    @Resource
    private ReportManager reportManager;

    @Resource
    private IGatewayRecoverDAO gatewayRecoverDAO;

    private static final String XIMENG_CHANGE = "change";

    private static final String XIMENG_SCENE = "scene";

    /**
     * 激活网关配置,恢复无网施工预设置的联动、多控、无线开关自动化等
     *
     * @param gatewayId
     * @param content
     */
    @Override
    @Async
    public void activateGatewayConfig(String gatewayId, String content) {
        long start = Instant.now().toEpochMilli();
        Stopwatch stopwatch = Stopwatch.createStarted();

        String gatewayRecoverProgressBizId = "";
        try {
            //从配置文件转换为中间对象
            ConfigInfo config = buildConfig(content);
            ValidateUtils.notNull(config, "config is null", BizErrorEnum.DATA_ERROR);

            String recordId = config.getRecordId();
            String hid = config.getHid();
            ValidateUtils.notNull(recordId, "recordId is null", BizErrorEnum.DATA_ERROR);
            ValidateUtils.notNull(hid, "hid is null", BizErrorEnum.DATA_ERROR);
            //查询任务信息
            LubanTaskRecord record = taskRecordDAO.getById(recordId);
            if (record == null) {
                logger.error("任务不存在");
                throw new BizException(BizErrorEnum.RECORD_NOT_EXIST);
            }
            String namespace = record.getNamespace();
            TemplateDetailVO templateDetail = templateManage.getTemplateDetail(record.getTemplateId(),
                    record.getVersion(), "zh");
            Map<String, String> deviceRoomDeviceNameMap = getVirtualDeviceIdRoomDeviceNameMap(templateDetail);
            Map<String, String> roomDeviceNameMacMap = getGatewayDeviceNameMacMap(config);
            TemplateDTO templateDTO = templateConfigManage.getTemplateDTO(record.getTemplateId(), record.getVersion());
            // key:虚拟场景id, value:场景名称
            Map<String, String> sceneNameMap = templateDTO.getSceneNameMap();

            // 保存网关恢复记录
            lanGatewayRecoverService.saveGatewayRecover(gatewayId, record, hid, content, config);

            logger.info("[configUpload] begin, gatewayId={}, content={}", gatewayId, content);

            DeviceRichVO deviceRichVO = athenaDeviceIntegration.getAllDeviceRichById2(gatewayId);
            if ((deviceRichVO == null || !deviceRichVO.getStatus())) {
                lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_GATEWAY_QUERY_ERROR,
                        DeviceErrorParamsDTO.builder().deviceId(gatewayId).build(), null);
                throw new BizException(BizErrorEnum.DEVICE_DETAIL_NO_EXIST);
            }
            logger.info("[configUpload] begin, gateway={}", JacksonUtils.objectToJson(deviceRichVO));
            lanGatewayRecoverService.updateGatewayMac(gatewayId, deviceRichVO.getUuid());
            boolean allGatewayUpload = lanGatewayRecoverService.allGatewayUpload(config);
            logger.info("[configUpload] allGatewayUpload = {}", allGatewayUpload);
            if (!allGatewayUpload) {
                logger.info("等待其他网关激活");
                return;
            }

            //查询网关下子设备列表
            List<DeviceTopoRelationVO> relationList = getDeviceRelations(gatewayId, config);
            if (CollectionUtils.isEmpty(relationList)) {
                tagDevice(config, gatewayId, Collections.emptyMap());
                lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_SUB_DEVICE_QUERY_ERROR,
                        DeviceErrorParamsDTO.builder().deviceId(gatewayId).build(), null);
                throw new BizException(BizErrorEnum.DEVICE_NOT_FOUND);
            }
            logger.info("[configUpload] relationList={}", JSON.toJSONString(relationList));

            // 整体进度初始化缓存
            LinkedHashMap<LanGatewayRecoverItemEnum, Integer> itemNumMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(relationList)) {
                itemNumMap.put(LanGatewayRecoverItemEnum.DEVICE, relationList.size());
            }
            List<RoomInfo> roomList = getRoomList(config);
            if (CollectionUtils.isNotEmpty(roomList)) {
                itemNumMap.put(LanGatewayRecoverItemEnum.ROOM, roomList.size());

                List<DeviceInfo> bindList = roomList.stream()
                        .filter(roomInfo -> CollectionUtils.isNotEmpty(roomInfo.getDevices())).map(RoomInfo::getDevices)
                        .flatMap(List::stream).collect(Collectors.toList());
                itemNumMap.put(LanGatewayRecoverItemEnum.DEVICE_ROOM_BIND, bindList.size());
            }
            if (CollectionUtils.isNotEmpty(config.getScenes().getTemplate())) {
                itemNumMap.put(LanGatewayRecoverItemEnum.LINKAGE_RULE, config.getScenes().getTemplate().size());
            }
            if (MapUtils.isNotEmpty(itemNumMap)) {
                initGatewayRecoverProgress(recordId, itemNumMap);
                gatewayRecoverProgressBizId = recordId;
            }

            String uid = deviceRichVO.getUid();
            String ownerId = deviceRichVO.getOwnerId();
            if (StringUtils.isBlank(ownerId)) {
                logger.error("[configUpload] ownerId is null");
                lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_OWNER_QUERY_ERROR,
                        DeviceErrorParamsDTO.builder().deviceId(gatewayId).build(), null);
                throw new BizException(BizErrorEnum.GROUP_NOT_EXIST);
            }
            //如果网关uid为空，则使用家庭拥有者uid代替
            logger.info("[configUpload], gateway.uid={}, ownerId={}", uid, ownerId);
            if (StringUtils.isBlank(uid)) {
                GroupUserVO groupUserVO = groupUserIntegration.getOwnerMemberByGroupId(Long.valueOf(ownerId));
                uid = Optional.ofNullable(groupUserVO).map(GroupUserVO::getUid).orElse(null);
            }
            logger.info("[configUpload], uid={}, ownerId={}", uid, ownerId);
            if (StringUtils.isBlank(uid)) {
                lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_OWNER_QUERY_ERROR,
                        DeviceErrorParamsDTO.builder().deviceId(gatewayId).build(), null);
                throw new BizException(BizErrorEnum.USER_NOT_EXIST);
            }

            //缓存配置信息
            cacheContent(gatewayId, content);

            TraceUtil.traceTimer(stopwatch.stop().elapsed().toMillis(), ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                    ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "gateway.query"));
            stopwatch.reset().start();

            //校验设备是否恢复失败
            checkDeviceRecover(config, relationList, deviceRichVO);
            TraceUtil.traceTimer(stopwatch.stop().elapsed().toMillis(), ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                    ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "device.recovery"));
            stopwatch.reset().start();

            //查询设备信息,key=temp_devId,value=devId
            Map<String, String> devIdMap = ConvertUtil.getDeviceMap(config, relationList);
            //查询设备信息,key=mac_id,value=devId
            Map<String, String> macMap = relationList.stream().collect(
                    Collectors.toMap(DeviceTopoRelationVO::getNodeId, DeviceTopoRelationVO::getDevId, (a, b) -> a));

            logger.info("[configUpload] devIdMap={},macMap={}", JSON.toJSONString(devIdMap), JSON.toJSONString(macMap));

            //将房间-设备关系导入IOT
            importRoom(config, deviceRichVO, ownerId, uid, relationList);
            TraceUtil.traceTimer(stopwatch.stop().elapsed().toMillis(), ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                    ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "import.room"));
            stopwatch.reset().start();

            //上下文信息
            LanRecoverContext lanRecoverContext = new LanRecoverContext(gatewayId, uid, ownerId, devIdMap, macMap,
                    new HashMap<>(), config);
            //生成云端联动
            generateLinkageRule(config, lanRecoverContext);
            TraceUtil.traceTimer(stopwatch.stop().elapsed().toMillis(), ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                    ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "linkage"));
            stopwatch.reset().start();

            logger.info("[configUpload] ruleMap={}", JSON.toJSONString(lanRecoverContext.getRuleVOMap()));

            // 需要重新改dp名称的设备
            Map<String, Map<String, String>> needResetDpMap = Maps.newHashMap();
            //绑定场景面板
            bindRulePanel(config, lanRecoverContext, needResetDpMap, sceneNameMap);
            logger.info("[configUpload] needResetDpMap={}", JSON.toJSONString(needResetDpMap));

            TraceUtil.traceTimer(stopwatch.stop().elapsed().toMillis(), ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                    ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "bind.panel"));
            stopwatch.reset().start();

            //更新DP名称和下发DPValue
            saveDeviceDP(config, macMap, uid, namespace, deviceRoomDeviceNameMap, roomDeviceNameMacMap, macMap);

            TraceUtil.traceTimer(stopwatch.stop().elapsed().toMillis(), ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                    ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "save.dp"));
            stopwatch.reset().start();

            // 设备打标
            tagDevice(config, gatewayId, macMap);

            TraceUtil.traceTimer(stopwatch.stop().elapsed().toMillis(), ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                    ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "tag.device"));
            stopwatch.reset().start();

            // 预留dp数据问题修复口子
            // resetDeviceDpNameByMap(needResetDpMap);

            // 龙信历史版本场景开关修复
            fixLilinSceneNameEmpty(namespace, templateDTO, lanRecoverContext, config, sceneNameMap);
            TraceUtil.traceTimer(stopwatch.stop().elapsed().toMillis(), ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                    ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "lilin.scene"));

            //发送恢复事件
            LanRecoverEvent event = new LanRecoverEvent(gatewayId, config);
            eventHandler.post(event);

            //发送无网恢复完成消息
            TaskStatusKafkaDTO kafkaDTO = reportManager.getKafkaDTOForLan(record, TaskStatusEnum.INSTALL_APPROVED,
                    ownerId);
            taskTurningManage.sendKafkaMqMessage(LubanBizConstant.CONSTRUCTION_TASK_STATUS_TOPIC, kafkaDTO);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (e instanceof BizException) {
                //已记录失败原因
                logger.info("已记录的业务异常 = {}", e.getMessage());
            } else {
                //未知失败原因
                lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_UNKNOWN_ERROR,
                        DeviceErrorParamsDTO.builder().deviceId(gatewayId).build(), e.getMessage());
            }
        } finally {
            lanGatewayRecoverService.removeCache();
            // 整体进度完成 修改缓存
            if (StringUtils.isNotBlank(gatewayRecoverProgressBizId)) {
                completeGatewayRecoverProgress(gatewayRecoverProgressBizId);
            }
        }
        TraceUtil.traceTimer(Instant.now().toEpochMilli() - start, ProxyTrace.Metric.CONSTR_LAN_RECOVERY,
                ImmutablePair.of(ProxyTrace.Tag.CONSUMING_NODE, "totalize"));

    }

    private List<RoomInfo> getRoomList(ConfigInfo configInfo) {
        try {
            if (configInfo.noMultiGatewayConfig() && CollectionUtils.isNotEmpty(configInfo.getRooms())) {
                return configInfo.getRooms();
            }

            List<RoomInfo> result = new ArrayList<>();
            List<GatewayDeviceInfo> gateways = configInfo.getGateways();
            if (CollectionUtils.isNotEmpty(gateways)) {
                //获取多网关下的所有子设备信息
                List<String> macs = configInfo.toGatewayMacs();
                List<GatewayRecoverDTO> recovers = lanGatewayRecoverService.findByMacList(macs, configInfo.getHid());
                Map<String, String> macGatewayMap = recovers.stream()
                        .collect(Collectors.toMap(GatewayRecoverDTO::getMac, GatewayRecoverDTO::getWgId, (a, b) -> b));
                gateways.forEach(gatewayDeviceInfo -> {
                    if (StringUtils.isNotEmpty(macGatewayMap.get(gatewayDeviceInfo.getMac()))) {
                        result.addAll(gatewayDeviceInfo.toRoomInfos());
                    }
                });
            }
            return result;
        } catch (Exception e) {
            logger.error("importRoom catch an error", e);
        }
        return null;
    }

    @Override
    public GatewayRecoverProgressDTO getActivateGatewayConfigProgress(String uid, String gatewayId, String lang) {
        GatewayRecoverProgressDTO result = new GatewayRecoverProgressDTO();

        // 查询恢复记录
        GatewayRecoverQuery gatewayRecoverQuery = new GatewayRecoverQuery();
        gatewayRecoverQuery.setWgId(gatewayId);
        List<GatewayRecoverDO> recoverList = gatewayRecoverDAO.findByCondition(gatewayRecoverQuery);
        if (CollectionUtils.isEmpty(recoverList)) {
            return result;
        }
        String taskRecordId = recoverList.get(0).getRecordId();

        // 只有设备所有者才能看到进度
        String wgId = recoverList.get(0).getWgId();
        DeviceRichVO deviceRichVO = athenaDeviceIntegration.getAllDeviceRichById2(wgId);
        logger.info("LanGatewayService.getActivateGatewayConfigProgress uid:{},deviceRichVO:{}", uid, JSON.toJSONString(deviceRichVO));
        if(!deviceRichVO.getUid().equals(uid) && !deviceRichVO.getOwnerId().equals(uid)){
            return result;
        }

        // 查询任务进度
        GatewayRecoverProgressDTO redisValue = redisHelper
                .getValue(RedisKey.GATEWAY_RECOVER_PROGRESS.getRedisKey(taskRecordId), GatewayRecoverProgressDTO.class);
        if (Objects.isNull(redisValue)) {
            return result;
        }
        // 处理多语言
        if(CollectionUtils.isNotEmpty(redisValue.getItemList())){
            redisValue.getItemList().forEach(item -> {
                String langName = LanGatewayRecoverItemEnum.getI18NameByCode(item.getItemCode(), lang);
                if (StringUtils.isNotBlank(langName)) {
                    item.setItemName(langName);
                }
            });
            redisValue.setItemList(redisValue.getItemList().stream()
                    .sorted(Comparator.comparing(GatewayRecoverItemProgressDTO::getSort)).collect(Collectors.toList()));
        }

        // 如果有一次查询成功，则删除缓存
        if(Objects.nonNull(redisValue.getProgressStatus()) && redisValue.getProgressStatus() == 2){
            redisHelper.del(RedisKey.GATEWAY_RECOVER_PROGRESS.getRedisKey(taskRecordId));
        }
        return redisValue;
    }

    @Override
    public void mockInitGatewayRecoverProgress(String recordId, LinkedHashMap<String, Integer> itemNumMap) {
        LinkedHashMap<LanGatewayRecoverItemEnum, Integer> itemEnumMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : itemNumMap.entrySet()) {
            LanGatewayRecoverItemEnum itemEnum = LanGatewayRecoverItemEnum.getEnumByCode(entry.getKey());
            if (Objects.nonNull(itemEnum)) {
                itemEnumMap.put(itemEnum, entry.getValue());
            }
        }
        initGatewayRecoverProgress(recordId, itemEnumMap);
    }

    @Override
    public void mockUpdateGatewayRecoverProgress(String recordId, String itemCode, Integer addNum) {
        LanGatewayRecoverItemEnum itemEnum = LanGatewayRecoverItemEnum.getEnumByCode(itemCode);
        if (Objects.nonNull(itemEnum)) {
            updateGatewayRecoverProgress(recordId, itemEnum, addNum);
        }
    }

    @Override
    public void mockCompleteGatewayRecoverProgress(String recordId) {
        completeGatewayRecoverProgress(recordId);
    }

    /**
     * 初始化进度
     */
    private void initGatewayRecoverProgress(String recordId,
                                            LinkedHashMap<LanGatewayRecoverItemEnum, Integer> itemNumMap) {
        try {
            GatewayRecoverProgressDTO result = new GatewayRecoverProgressDTO();
            result.setIfShowWindow(Boolean.TRUE);
            result.setProgressStatus(1);

            List<GatewayRecoverItemProgressDTO> itemList = new ArrayList<>();
            for (Map.Entry<LanGatewayRecoverItemEnum, Integer> entry : itemNumMap.entrySet()) {
                GatewayRecoverItemProgressDTO item = new GatewayRecoverItemProgressDTO();
                item.setItemCode(entry.getKey().getItemCode());
                item.setItemName(entry.getKey().getCnName());
                item.setRecoverTotalNum(entry.getValue());
                item.setRecoverStatus(1);
                if (entry.getKey().equals(LanGatewayRecoverItemEnum.DEVICE)) {
                    item.setHasRecoverNum(item.getRecoverTotalNum());
                    item.setRecoverStatus(2);
                }
                item.setSort(entry.getKey().getSort());
                itemList.add(item);
            }
            result.setItemList(itemList);
            redisHelper.setValue(RedisKey.GATEWAY_RECOVER_PROGRESS.getRedisKey(recordId), result);
        }catch (Exception e){
            logger.info("LanGatewayService.initGatewayRecoverProgress 异常:", e);
        }
    }

    /**
     * 执行中修改进度
     */
    private void updateGatewayRecoverProgress(String recordId, LanGatewayRecoverItemEnum itemEnum, Integer addNum) {
        try {
            String redisKey = RedisKey.GATEWAY_RECOVER_PROGRESS.getRedisKey(recordId);
            GatewayRecoverProgressDTO result = redisHelper.getValue(redisKey, GatewayRecoverProgressDTO.class);
            if (Objects.isNull(result)) {
                return;
            }
            for (GatewayRecoverItemProgressDTO item : result.getItemList()) {
                if (itemEnum.getItemCode().equals(item.getItemCode())) {
                    item.setHasRecoverNum(Math.min(item.getHasRecoverNum() + addNum, item.getRecoverTotalNum()));
                    if (item.getHasRecoverNum() >= item.getRecoverTotalNum()) {
                        item.setRecoverStatus(2);
                    }
                }
            }
            redisHelper.setValue(redisKey, result);
        }catch (Exception e){
            logger.info("LanGatewayService.updateGatewayRecoverProgress 异常:", e);
        }
    }

    /**
     * 执行完成修改进度
     */
    private void completeGatewayRecoverProgress(String recordId) {
        try {
            String redisKey = RedisKey.GATEWAY_RECOVER_PROGRESS.getRedisKey(recordId);
            GatewayRecoverProgressDTO result = redisHelper.getValue(redisKey, GatewayRecoverProgressDTO.class);
            if (Objects.isNull(result)) {
                return;
            }
            result.setProgressStatus(2);
            for (GatewayRecoverItemProgressDTO item : result.getItemList()) {
                if (item.getHasRecoverNum().equals(item.getRecoverTotalNum())) {
                    item.setRecoverStatus(2);
                }
                if (item.getHasRecoverNum() < item.getRecoverTotalNum()) {
                    item.setRecoverStatus(3);
                }
                if (item.getHasRecoverNum() > item.getRecoverTotalNum()) {
                    item.setHasRecoverNum(item.getRecoverTotalNum());
                }
            }
            redisHelper.setValue(redisKey, result);
        }catch (Exception e){
            logger.info("LanGatewayService.completeGatewayRecoverProgress 异常:", e);
        }
    }

    private void resetDeviceDpNameByMap(Map<String, Map<String, String>> needResetDpMap) {
        if (needResetDpMap.isEmpty()) {
            return;
        }
        needResetDpMap.forEach((devId, dpMap) -> {
            dpMap.forEach((dpId, dpName) -> {
                try {
                    lanGatewayRecoverService.mockError(GatewayErrorEnum.WG_RE_DEVICE_DP_NAME_ERROR);
                    dpWriteMServiceIntegration.updateName(devId, Integer.parseInt(dpId), dpName);
                } catch (Exception e) {
                    logger.warn("constructionBindService.resetDeviceDpNameByMap error,deviceId={},dpId={},dpName={}",
                            devId, dpId, dpName);
                    lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_DEVICE_DP_NAME_ERROR,
                            DeviceErrorParamsDTO.builder().dpId(dpId).dpName(dpName).build(), e.getMessage());
                }
            });
        });
    }

    private void fixLilinSceneNameEmpty(String namespace, TemplateDTO templateDTO, LanRecoverContext lanRecoverContext,
                                        ConfigInfo config, Map<String, String> sceneNameMap) {

        if (!apolloDynamicConfig.inLilinNamespace(namespace)) {
            return;
        }

        // 虚拟设备id : 真实设备id
        Map<String, String> devIdMap = lanRecoverContext.getDevIdMap();
        // mac-真实设备id
        Map<String, String> macMap = lanRecoverContext.getMacMap();
        // 真实设备id : 虚拟设备id
        Map<String, String> realDevVirtualDevMap = devIdMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getValue(), entry -> entry.getKey(), (a, b) -> b));

        logger.info("[fixLilinSceneNameEmpty] realDevVirtualDevMap:{}", JSON.toJSONString(realDevVirtualDevMap));

        // 循环模版内定义的房间的子设备
        // key:virtualDevId - value:subDevices
        Map<String, List<SubDeviceDTO>> subDeviceMap = Maps.newHashMap();
        templateDTO.getRooms().forEach(roomDTO -> {
            List<DeviceDTO> devices = roomDTO.getDevices();
            if (CollectionUtils.isEmpty(devices)) {
                return;
            }
            devices.forEach(deviceDTO -> {
                List<SubDeviceDTO> subDevices = deviceDTO.getSubDevices();
                if (CollectionUtils.isEmpty(subDevices)) {
                    return;
                }
                subDeviceMap.put(deviceDTO.getDevId(), subDevices);
            });
        });

        logger.info("[fixLilinSceneNameEmpty] subDeviceMap:{}", JSON.toJSONString(subDeviceMap));

        config.getDeviceDP().forEach((mac, dpIdNameMap) -> {
            String realDevId = macMap.get(mac);
            String virtualDevId = realDevVirtualDevMap.get(realDevId);
            if (StringUtils.isBlank(virtualDevId)) {
                return;
            }

            logger.info("[fixLilinSceneNameEmpty] realDevId:{}, virtualDevId:{}", realDevId, virtualDevId);

            List<SubDeviceDTO> templateSubDevices = subDeviceMap.get(virtualDevId);
            if (CollectionUtils.isEmpty(templateSubDevices)) {
                return;
            }

            logger.info("[fixLilinSceneNameEmpty] virtualDevId:{}, templateSubDevices:{}", virtualDevId,
                    JSON.toJSONString(templateSubDevices));

            dpIdNameMap.forEach((dpId, dpName) -> {
                SubDeviceDTO subDevice = findTargetSubDeviceDTO(dpId, templateSubDevices);
                if (Objects.isNull(subDevice)) {
                    return;
                }
                logger.info("[fixLilinSceneNameEmpty] virtualDevId:{}, subDevice:{}", virtualDevId,
                        JSON.toJSONString(subDevice));

                if (StringUtils.isBlank(subDevice.getSceneName())) {
                    // 无对应场景名称, 从sceneMap中获取名称，保存
                    String dpNameNew = sceneNameMap.get(subDevice.getSceneId());
                    try {
                        logger.info("[fixLilinSceneNameEmpty] virtualDevId:{}, dpId:{}, dpNameNew:{}", virtualDevId,
                                dpId, dpNameNew);
                        lanGatewayRecoverService.mockError(GatewayErrorEnum.WG_RE_DEVICE_DP_NAME_ERROR);
                        dpWriteMServiceIntegration.updateName(realDevId, Integer.parseInt(dpId), dpNameNew);
                    } catch (Exception e) {
                        logger.warn(
                                "constructionBindService.fixLilinSceneNameEmpty error,deviceId={},dpId={},dpName={}",
                                realDevId, dpId, dpNameNew);
                        lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_DEVICE_DP_NAME_ERROR,
                                DeviceErrorParamsDTO.builder().dpId(dpId).dpName(dpNameNew).build(), e.getMessage());
                    }
                }
            });
        });
    }

    private SubDeviceDTO findTargetSubDeviceDTO(String dpId, List<SubDeviceDTO> templateSubDevices) {
        return templateSubDevices.stream().filter(subDeviceDTO -> subDeviceDTO.getDpId().equals(Integer.valueOf(dpId)))
                .findAny().orElse(null);
    }

    /**
     * 获取网关下子设备信息
     * 
     * @param gatewayId
     * @param config
     * @return
     */
    private List<DeviceTopoRelationVO> getDeviceRelations(String gatewayId, ConfigInfo config) {
        if (config.noMultiGatewayConfig()) {
            return deviceTopoRelationIntegration.getByMeshId(gatewayId);
        }
        //获取多网关下的所有子设备信息
        List<String> macs = config.toGatewayMacs();
        List<GatewayRecoverDTO> recovers = lanGatewayRecoverService.findByMacList(macs, config.getHid());
        if (CollectionUtils.isEmpty(recovers)) {
            throw new BizException(BizErrorEnum.GATEWAY_NOT_RECOVER);
        }
        List<String> gatewayIds = recovers.stream().map(GatewayRecoverDTO::getWgId).distinct()
                .collect(Collectors.toList());
        logger.info("gatewayIds  = {}", JSON.toJSONString(gatewayIds));
        List<Future<List<DeviceTopoRelationVO>>> futures = gatewayIds.stream()
                .map(wgId -> CommonPoolUtil.submit(() -> deviceTopoRelationIntegration.getByMeshId(wgId)))
                .collect(Collectors.toList());
        List<DeviceTopoRelationVO> relations = CommonPoolUtil.getFutureList(futures).stream()
                .filter(CollectionUtils::isNotEmpty).flatMap(List::stream).collect(Collectors.toList());
        //加上网关的mac和deviceId
        recovers.forEach(recover -> {
            DeviceTopoRelationVO deviceTopoRelationVO = new DeviceTopoRelationVO();
            deviceTopoRelationVO.setDevId(recover.getWgId());
            deviceTopoRelationVO.setNodeId(recover.getMac());
            relations.add(deviceTopoRelationVO);
        });
        logger.info("all relations = {}", JSON.toJSONString(relations));
        return relations;
    }

    private void checkDeviceRecover(ConfigInfo config, List<DeviceTopoRelationVO> relationList,
                                    DeviceRichVO deviceRichVO) {
        Set<String> macSet = relationList.stream().map(DeviceTopoRelationVO::getNodeId).collect(Collectors.toSet());
        macSet.add(deviceRichVO.getUuid());
        Map<String, String> macNameMap = config.toSubDeviceNameMap();
        macNameMap.forEach((mac, deviceName) -> {
            if (StringUtils.isNotEmpty(mac) && !macSet.contains(mac)) {
                lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_SUB_DEVICE_RECOVER_ERROR,
                        DeviceErrorParamsDTO.builder().deviceName(deviceName).build(), null);
            }
            try {
                lanGatewayRecoverService.mockError(GatewayErrorEnum.WG_RE_SUB_DEVICE_RECOVER_ERROR);
            } catch (Exception e) {
                lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_SUB_DEVICE_RECOVER_ERROR,
                        DeviceErrorParamsDTO.builder().deviceName(deviceName).build(), null);
            }
        });
    }

    private void tagDevice(ConfigInfo config, String gatewayId, Map<String, String> macMap) {
        try {
            deviceTagHelper.tagDevice(gatewayId, null, config.getRecordId());

            if (MapUtils.isEmpty(macMap)) {
                return;
            }

            for (Map.Entry<String, String> entry : macMap.entrySet()) {
                deviceTagHelper.tagDevice(entry.getValue(), entry.getKey(), config.getRecordId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * 场景默认展示颜色
     *
     * @param templateList 场景列表
     */
    private void fillDefaultRGBColor(List<LinkageRule> templateList) {

        if (CollectionUtils.isEmpty(templateList)) {
            return;
        }

        List<String> rgbColorList = apolloDynamicConfig.getSceneDefaultRgbColorList();
        for (int i = 0; i < templateList.size(); i++) {
            LinkageRule rule = templateList.get(i);
            // 顺序配置默认列表中的颜色
            rule.setDisplayColor(rgbColorList.get(i % rgbColorList.size()));
        }

    }

    private void generateLinkageRule(ConfigInfo config, LanRecoverContext lanRecoverContext) {

        //查询联动配置
        List<LinkageRule> templateList = config.getScenes().getTemplate();

        // 补齐场景默认背景色
        fillDefaultRGBColor(templateList);

        //找出包含场景的自动化，先生成场景，然后生成自动化
        Map<Integer, List<LinkageRule>> ruleGroupMap = templateList.stream()
                .collect(Collectors.groupingBy(LinkageRule::toGroupType));

        logger.info("[configUpload bindRulePanel] ruleGroupMap={}", JSON.toJSONString(ruleGroupMap));

        //先处理不包含场景的自动化
        saveRules(lanRecoverContext, ruleGroupMap.get(LinkageRule.COMMON_RULE));

        //处理包含场景的自动化
        saveRules(lanRecoverContext, ruleGroupMap.get(LinkageRule.ACTION_RULE));

    }

    private void saveRules(LanRecoverContext lanRecoverContext, List<LinkageRule> normalList) {
        if (CollectionUtils.isNotEmpty(normalList)) {
            for (LinkageRule templateVO : normalList) {
                ILanRuleExecutor lanRuleExecutor = executorHandler
                        .getExecutorThrow(ExecutorTypeEnum.LAN_RULE_RECOVER.getType(), templateVO.toRuleType() + "");
                lanRuleExecutor.createRule(lanRecoverContext, templateVO);

                // 修改任务进度
                updateGatewayRecoverProgress(lanRecoverContext.getConfig().getRecordId(),
                        LanGatewayRecoverItemEnum.LINKAGE_RULE, 1);
            }
        }
    }

    private void bindRulePanel(ConfigInfo config, LanRecoverContext lanRecoverContext,
                               Map<String, Map<String, String>> needResetDpMap, Map<String, String> sceneNameMap) {
        Map<String, String> devIdMap = lanRecoverContext.getDevIdMap();
        String gatewayId = lanRecoverContext.getGwId();
        Map<String, String> ruleMap = lanRecoverContext.getRuleVOMap();

        logger.info("[configUpload bindRulePanel] rulePanel={}", JSON.toJSONString(config.getScenes().getRulePanel()));

        if (CollectionUtils.isNotEmpty(config.getScenes().getRulePanel())) {
            Map<String, MetaInfo> meta = config.getScenes().getMeta();
            config.getScenes().getRulePanel().forEach(rulePanelVO -> {
                String productId = rulePanelVO.getProductId();
                String devId = rulePanelVO.getDevId();
                String rulePanelName = Optional.ofNullable(meta).map(m -> m.get(devId)).map(MetaInfo::getName)
                        .orElse("");
                rulePanelVO.setDevId(devIdMap.get(rulePanelVO.getDevId()));
                // key:realDevId, value: map{dpId:name}
                needResetDpMap.putIfAbsent(rulePanelVO.getDevId(), Maps.newHashMap());
                Map<String, String> dpIdNameMap = needResetDpMap.get(rulePanelVO.getDevId());

                for (BindInfo importBindVO : rulePanelVO.getBinds()) {
                    // 设置再更新的Map
                    // key:虚拟场景id, value:场景名称
                    String sceneName = sceneNameMap.get(importBindVO.getRuleId());
                    dpIdNameMap.put(importBindVO.getBtnId(), sceneName);

                    LinkageRuleBindDO ruleBindDO = new LinkageRuleBindDO();
                    ruleBindDO.setDevId(rulePanelVO.getDevId());
                    ruleBindDO.setBtnId(Integer.valueOf(importBindVO.getBtnId()));
                    ruleBindDO.setLocalSid(importBindVO.getSid());
                    ruleBindDO.setRuleId(ruleMap.get(importBindVO.getRuleId()));
                    ruleBindDO.setGwId(gatewayId);
                    // 定制逻辑，西蒙M7的八位场景开关
                    if (apolloDynamicConfig.getSimon8SceneSwitchPid().equals(productId)) {
                        // btn==1时，需要设置dpId=9
                        if (ruleBindDO.getBtnId() == 1) {
                            ruleBindDO.setDpId(9);
                        } else {
                            // 另外btn设置dpId=btnId
                            ruleBindDO.setDpId(ruleBindDO.getBtnId());
                        }
                    } else {
                        // 默认设置dpId=btnId
                        ruleBindDO.setDpId(ruleBindDO.getBtnId());
                    }

                    logger.info("ConfigUpload: ruleBindDO = {}", JSON.toJSONString(ruleBindDO));
                    try {
                        lanGatewayRecoverService.mockError(GatewayErrorEnum.WG_RE_SCENE_PANEL_BIND_ERROR);

                        // 其他场景面板走 标准zigbee场景绑定
                        linkageRuleBindIntegration.saveRuleBind(ruleBindDO);
                    } catch (Exception e) {
                        lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_SCENE_PANEL_BIND_ERROR,
                                DeviceErrorParamsDTO.builder().deviceName(rulePanelName).dpId(importBindVO.getBtnId())
                                        .dpName(importBindVO.getRuleName()).build(),
                                e.getMessage());
                        logger.error("linkageRuleBindIntegration.saveRuleBind error");
                    }
                }
            });
        }

    }

    private void saveDeviceDP(ConfigInfo config, Map<String, String> macMap, String uid, String namespace,
                              Map<String, String> deviceRoomDeviceNameMap, Map<String, String> roomDeviceNameMacMap,
                              Map<String, String> macDevIdMap) {

        Map<String, String> deviceNameMap = config.toDeviceNameMap();
        List<RulePanel> rulePanels = config.getScenes().getRulePanel();
        Map<String, String> deviceProductMap = rulePanels.stream()
                .collect(Collectors.toMap(RulePanel::getDevId, RulePanel::getProductId, (a, b) -> a));

        // 设备名称预设
        preSetDeviceNameDp(config, macMap, deviceNameMap);

        // 设备状态预设
        preSetDeviceDpValue(config, macMap, uid, namespace, deviceProductMap, deviceNameMap, deviceRoomDeviceNameMap,
                roomDeviceNameMacMap, macDevIdMap);

    }

    private void preSetDeviceNameDp(ConfigInfo config, Map<String, String> macMap, Map<String, String> deviceNameMap) {
        config.getDeviceDP().forEach((mac, dpMap) -> {
            String devId = macMap.get(mac);
            dpMap.forEach((dpId, dpName) -> {
                try {
                    lanGatewayRecoverService.mockError(GatewayErrorEnum.WG_RE_DEVICE_DP_NAME_ERROR);
                    dpWriteMServiceIntegration.updateName(devId, Integer.parseInt(dpId), dpName);
                } catch (Exception e) {
                    logger.warn("constructionBindService.saveDeviceDP error,deviceId={},mac={},dpId={},dpName={}",
                            devId, mac, dpId, dpName);
                    String deviceName = deviceNameMap.get(mac);
                    lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_DEVICE_DP_NAME_ERROR,
                            DeviceErrorParamsDTO.builder().deviceName(deviceName).dpId(dpId).dpName(dpName).build(),
                            e.getMessage());
                }
            });
        });
    }

    private void preSetDeviceDpValue(ConfigInfo config, Map<String, String> macMap, String uid, String namespace,
                                     Map<String, String> scenePanelDeviceProductMap, Map<String, String> deviceNameMap,
                                     Map<String, String> deviceRoomDeviceNameMap,
                                     Map<String, String> roomDeviceNameMacMap, Map<String, String> macDevIdMap) {
        config.getDeviceDPValue().forEach((mac, dpList) -> {
            String devId = macMap.get(mac);
            // 这里只有场景面板类设备才能拿到productId
            String productId = scenePanelDeviceProductMap.get(devId);
            dpList.forEach(dpInfo -> {
                if (Objects.isNull(dpInfo.getDpValue())) {
                    return;
                }
                if (apolloDynamicConfig.isSimonNamespace(namespace)) {
                    // 西蒙复合开关定制逻辑, 名单内的pid设备不切换开关模式，只切换场景模式
                    // 不在名单内的西蒙设备则按照Map设置值, 名单内设备下发开关切换会导致设备重启，数据丢失，名单外设备正常
                    if (Objects.nonNull(productId) && apolloDynamicConfig.inSimonMultiControlPidList(productId)) {
                        if (TemplateUtil.isSwitchButton(dpInfo.getDpValue() + "")) {
                            return;
                        }
                    }
                }
                // 过滤无效dp
                if (dpInfo.getValid().equals(LubanBizConstant.MAGIC_NUM_ZERO)) {
                    return;
                }
                try {
                    lanGatewayRecoverService.mockError(GatewayErrorEnum.WG_RE_DEVICE_DP_VALUE_ERROR);

                    // 立林的下发dp点需要加密，另外namespace正常下发，后续考虑路由
                    Map<String, Object> dpMap = getDpMapByNamespace(namespace, dpInfo, deviceRoomDeviceNameMap,
                            roomDeviceNameMacMap, macDevIdMap);
                    logger.info("[preSetDeviceDpValue] devId:{}, dpMap ={}", devId, JSON.toJSONString(dpMap));

                    DpPublishOptions options = DpPublishOptions.create().from("luban").uid(uid).reason("无网dp值预设");
                    dpPulishIntegration.issueDataPoint(devId, JSON.toJSONString(dpMap), options);

                } catch (Exception e) {
                    logger.warn("constructionBindService.saveDeviceDPValue error,deviceId={},mac={},dpId={},dpValue={}",
                            devId, mac, dpInfo.getDpId(), dpInfo.getDpValue());
                    String deviceName = deviceNameMap.get(mac);
                    lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_DEVICE_DP_VALUE_ERROR,
                            DeviceErrorParamsDTO.builder().deviceName(deviceName).dpId(dpInfo.getDpId() + "")
                                    .dpValue(dpInfo.getDpValue() + "").build(),
                            e.getMessage());
                }
            });
        });
    }

    private Map<String, Object> getDpMapByNamespace(String namespace, DpDTO dpInfo,
                                                    Map<String, String> deviceRoomDeviceNameMap,
                                                    Map<String, String> roomDeviceNameMacMap,
                                                    Map<String, String> macDevIdMap) {
        if (apolloDynamicConfig.inLilinNamespace(namespace)) {
            // 1.将dpValueString中的device_id替换为真实设备id
            String dpValueString = handleLilinDpValueString(dpInfo.getDpValue() + "", deviceRoomDeviceNameMap,
                    roomDeviceNameMacMap, macDevIdMap);
            // 2.立林的raw类型dp预设需要将dpValue值base64加密后再发送
            if (DataPointPropertyTypeEnum.RAW.getCode().equals(dpInfo.getDpType())) {
                return Map.of(dpInfo.getDpId() + "", CommonUtils.base64Encode(dpValueString));
            } else {
                return Map.of(dpInfo.getDpId() + "", dpValueString);
            }
        } else {
            // 另外的namespace不需要操作
            return Map.of(dpInfo.getDpId() + "", dpInfo.getDpValue());
        }
    }

    /**
     * 立林的特殊dpValue下发处理
     * 除了485、地暖执行器，另外的温控面板、输入输出设备、新风空调执行器，需要换成真实设备id
     * <p>
     * 这里的判断逻辑可能会有问题，此处以 roomName+deviceName唯一匹配mac，如果一个房间下设备不唯一，则无效
     * 目前立林需求下唯一
     *
     * @return 转为真实设备id后的dpString
     */
    private String handleLilinDpValueString(String originDpValueString, Map<String, String> deviceRoomDeviceNameMap,
                                            Map<String, String> roomDeviceNameMacMap, Map<String, String> macDevIdMap) {

        //        logger.info("[preSetDeviceDpValue] originDpValueString ={}，deviceRoomDeviceNameMap:{}, roomDeviceNameMacMap:{}," +
        //                        "macDevIdMap:{}", originDpValueString, JSON.toJSONString(deviceRoomDeviceNameMap),
        //                JSON.toJSONString(roomDeviceNameMacMap), JSON.toJSONString(macDevIdMap));

        if (originDpValueString.indexOf(LubanBizConstant.DEVICE_ID_STR) < 0) {
            return originDpValueString;
        }

        // 1.解析json
        JSONObject jsonObject = JSON.parseObject(originDpValueString);
        String virtualDeviceId = jsonObject.getString(LubanBizConstant.DEVICE_ID_STR);
        if (StringUtils.isBlank(virtualDeviceId)) {
            return originDpValueString;
        }

        // 2.匹配真实设备id
        String realDevId = getRealDeviceByVirtualDevice(virtualDeviceId, deviceRoomDeviceNameMap, roomDeviceNameMacMap,
                macDevIdMap);
        if (StringUtils.isBlank(realDevId)) {
            return originDpValueString;
        }

        // 3.reset
        jsonObject.put(LubanBizConstant.DEVICE_ID_STR, realDevId);
        return jsonObject.toJSONString();

    }

    private String getRealDeviceByVirtualDevice(String virtualDeviceId, Map<String, String> deviceRoomDeviceNameMap,
                                                Map<String, String> roomDeviceNameMacMap,
                                                Map<String, String> macDevIdMap) {

        // 1.根据roomName+deviceName匹配mac，从而拿到真实的设备id
        String roomNameDeviceName = deviceRoomDeviceNameMap.get(virtualDeviceId);
        if (StringUtils.isBlank(roomNameDeviceName)) {
            return null;
        }

        // 2.匹配mac地址
        String mac = roomDeviceNameMacMap.get(roomNameDeviceName);
        if (StringUtils.isBlank(mac)) {
            return null;
        }

        // 3.真实设备id
        String realDevId = macDevIdMap.get(mac);
        return realDevId;

    }

    /**
     * Map key:virtualDeviceId, value: roomName+deviceName
     *
     * @param templateDetail 模板详情
     * @return map
     */
    private Map<String, String> getVirtualDeviceIdRoomDeviceNameMap(TemplateDetailVO templateDetail) {

        List<RoomVO> rooms = templateDetail.getRooms();
        if (CollectionUtils.isEmpty(rooms)) {
            return Maps.newHashMap();
        }

        Map<String, String> result = Maps.newHashMap();
        for (RoomVO room : rooms) {
            String roomName = room.getName();
            for (ProductInfo productInfo : room.getProductInfos()) {
                result.put(productInfo.getDeviceId(), roomName + productInfo.getDeviceName());
            }
        }

        return result;

    }

    /**
     * Map key:roomName+deviceName, value: mac
     *
     * @param config 模板详情
     * @return map
     */
    private Map<String, String> getGatewayDeviceNameMacMap(ConfigInfo config) {

        List<GatewayDeviceInfo> gateways = config.getGateways();
        if (CollectionUtils.isEmpty(gateways)) {
            return Maps.newHashMap();
        }

        Map<String, String> result = Maps.newHashMap();
        for (GatewayDeviceInfo gateway : gateways) {
            for (DeviceInfo subDevice : gateway.getSubDevices()) {
                result.put(subDevice.getRoomName() + subDevice.getName(), subDevice.getMac());
            }
        }

        return result;

    }

    private ConfigInfo buildConfig(String content) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ConfigInfo config;
        try {
            config = objectMapper.readValue(content, ConfigInfo.class);
        } catch (IOException e) {
            throw new BizException(BizErrorEnum.UNKNOWN);
        }
        logger.info("ConfigInfo={}", JSON.toJSONString(config));
        return config;
    }

    @Override
    public boolean checkConfig(String content, String gatewayId) {
        Boolean configUploadLock = apolloDynamicConfig.getConfigUploadLock();
        logger.info("LanGatewayService checkConfig, configUploadLock={}", configUploadLock);
        if (configUploadLock) {
            //缓存配置md5，保证配置只处理一次
            StringBuilder sb = new StringBuilder();
            String md5 = MD5Util.getMD5(sb.append(content).append("#").append(gatewayId).toString());
            try {
                return redisHelper.opsForString().setIfAbsent(md5, md5, RedisKey.CONFIG_MD5_KEY);
            } catch (Exception e) {
                logger.error("redis setIfAbsent error,md5={}", md5);
            }
            return false;
        }
        return true;
    }

    private void cacheContent(String gatewayId, String content) {
        try {
            // 设置兜底数据 30天有效期
            redisHelper.opsForString().set(gatewayId, content, RedisKey.CONFIG_KEY);
        } catch (Throwable e) {
            logger.error("activateGatewayConfig redis set is Exception ", e);
        }
    }

    private void importRoom(List<RoomInfo> roomList, String gatewayId) {
        ImportResVO importResVO = new ImportResVO();
        importResVO.setRooms(ConvertUtil.getImportRoomList(roomList));

        logger.info("proAppMServiceIntegration.importRoomData, importResVO={}, gwId={}", JSONObject.toJSON(importResVO),
                gatewayId);

        try {
            lanGatewayRecoverService.mockError(GatewayErrorEnum.WG_RE_ROOM_DEVICE_IMPORT_ERROR);
            proAppMServiceIntegration.importRoomData(importResVO, gatewayId);
        } catch (Exception e) {
            logger.info("proAppMServiceIntegration.importRoomData error, gwId={}", gatewayId, e);
            lanGatewayRecoverService.addGatewayRecoverError(GatewayErrorEnum.WG_RE_ROOM_DEVICE_IMPORT_ERROR,
                    Maps.newHashMap(), e.getMessage());
        }
    }

    private void importRoomRPC(List<RoomInfo> roomList, List<DeviceTopoRelationVO> relationList, DeviceRichVO gatewayVO,
                               String ownerId, String uid, ConfigInfo configInfo) {
        Map<String, String> nodeId2devId = new HashMap<>();
        if (!CollectionUtils.isEmpty(relationList)) {
            Map<String, String> collect = relationList.stream().collect(
                    Collectors.toMap(DeviceTopoRelationVO::getNodeId, DeviceTopoRelationVO::getDevId, (a, b) -> b));
            nodeId2devId.putAll(collect);
            nodeId2devId.put(gatewayVO.getUuid(), gatewayVO.getId());
        }
        logger.info("[nodeId2devId={}]", JSONObject.toJSON(nodeId2devId));

        LocationVO location = locationIntegration.queryLocationByGId(ownerId);
        //查询已存在的房间
        List<RoomBeanVO> oldRoomList = roomSearchBizIntegration.getByOwnerId(ownerId);
        Map<String, RoomBeanVO> oldRoomMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(oldRoomList)) {
            oldRoomMap = oldRoomList.stream().collect(Collectors.toMap(RoomBeanVO::getName, a -> a, (a, b) -> b));
        }
        List<RoomVOV2> addRoomList = Lists.newArrayList();

        //创建房间
        for (RoomInfo roomInfo : roomList) {
            RoomBeanVO roomBeanVO = oldRoomMap.get(roomInfo.getName());
            Long roomId = Objects.isNull(roomBeanVO) ? null : roomBeanVO.getId();
            //不存在的 房间创建,存在的不处理
            if (Objects.isNull(roomBeanVO)) {
                RoomVOV2 roomVOV2 = new RoomVOV2();
                roomVOV2.setName(roomInfo.getName());
                roomVOV2.setOwnerId(ownerId);
                roomVOV2.setUid(uid);
                roomVOV2.setStatus(true);
                roomId = roomMIntegration.addRoom(roomVOV2);
                roomVOV2.setId(roomId);
                addRoomList.add(roomVOV2);
            }
            updateGatewayRecoverProgress(configInfo.getRecordId(), LanGatewayRecoverItemEnum.ROOM, 1);

            //保存设备到对应房间
            if (CollectionUtils.isNotEmpty(roomInfo.getDevices())) {
                List<DeviceRoomRelationVO> deviceList = Lists.newArrayList();
                for (DeviceInfo device : roomInfo.getDevices()) {
                    String deviceId = nodeId2devId.get(device.getMac());
                    if (StringUtils.isNotBlank(deviceId)) {

                        DeviceRoomRelationVO importDevice = new DeviceRoomRelationVO(deviceId,
                                RelationTypeEnum.DEVICE.getValue());
                        deviceList.add(importDevice);
                        //修改设备名称
                        gatewayMIntegration.updateGatewayName(deviceId, device.getName());
                    }
                }
                roomRelationMIntegration.batchSaveRoomRelation(roomId, deviceList, uid);
                updateGatewayRecoverProgress(configInfo.getRecordId(), LanGatewayRecoverItemEnum.DEVICE_ROOM_BIND,
                        roomInfo.getDevices().size());
            }
        }
        logger.info("[importRoom,addRoomList={}]", JSONObject.toJSON(addRoomList));
        List<Long> addRoomIds = addRoomList.stream().map(RoomVOV2::getId).collect(Collectors.toList());

        // 房间位置bind
        if (Objects.nonNull(location) && CollectionUtils.isNotEmpty(addRoomIds)) {
            roomRelationMIntegration.batchSaveLocationRoomRelation(uid, ownerId, String.valueOf(location.getId()),
                    addRoomIds);
        }

    }

    /**
     * @param configInfo 配置信息
     * @param gatewayVO 网关
     * @param ownerId 家庭id
     * @param uid 用户uid
     * @param relationList 子设备列表
     */
    private void importRoom(ConfigInfo configInfo, DeviceRichVO gatewayVO, String ownerId, String uid,
                            List<DeviceTopoRelationVO> relationList) {
        try {
            if (configInfo.noMultiGatewayConfig() && CollectionUtils.isNotEmpty(configInfo.getRooms())) {
                //            importRoom(configInfo.getRooms(), gatewayId);
                importRoomRPC(configInfo.getRooms(), relationList, gatewayVO, ownerId, uid, configInfo);
                return;
            }
            List<GatewayDeviceInfo> gateways = configInfo.getGateways();
            if (CollectionUtils.isNotEmpty(gateways)) {
                //获取多网关下的所有子设备信息
                List<String> macs = configInfo.toGatewayMacs();
                List<GatewayRecoverDTO> recovers = lanGatewayRecoverService.findByMacList(macs, configInfo.getHid());
                Map<String, String> macGatewayMap = recovers.stream()
                        .collect(Collectors.toMap(GatewayRecoverDTO::getMac, GatewayRecoverDTO::getWgId, (a, b) -> b));
                gateways.forEach(gatewayDeviceInfo -> {
                    String gDeviceId = macGatewayMap.get(gatewayDeviceInfo.getMac());
                    if (StringUtils.isNotEmpty(gDeviceId)) {
                        List<RoomInfo> roomInfos = gatewayDeviceInfo.toRoomInfos();
                        //                    importRoom(roomInfos, gDeviceId);
                        importRoomRPC(roomInfos, relationList, gatewayVO, ownerId, uid, configInfo);
                    } else {
                        logger.warn("not find mac,mac = {}", gatewayDeviceInfo.getMac());
                    }
                });
            }
        } catch (Exception e) {
            logger.error("importRoom catch an error", e);
        }
    }

}
