
package lan.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tuya.luban.biz.cache.RedisKey;
import com.tuya.luban.biz.config.ApolloDynamicConfig;
import com.tuya.luban.biz.domain.config.LanRecoverConfig;
import com.tuya.luban.biz.enums.BizOptTypeEnum;
import com.tuya.luban.biz.enums.GatewayErrorEnum;
import com.tuya.luban.biz.enums.GatewayRecoverStatusEnum;
import com.tuya.luban.biz.exception.BizErrorEnum;
import com.tuya.luban.biz.exception.BizException;
import com.tuya.luban.biz.service.lan.ILanGatewayRecoverService;
import com.tuya.luban.biz.service.lan.domains.ConfigInfo;
import com.tuya.luban.biz.service.lan.domains.GatewayDeviceInfo;
import com.tuya.luban.biz.service.lan.domains.recover.GatewayRecoverDTO;
import com.tuya.luban.biz.service.lan.domains.recover.RecoverErrorDTO;
import com.tuya.luban.biz.service.lan.domains.recover.RecoverResultDTO;
import com.tuya.luban.biz.service.lan.domains.recover.RetryRecoverDTO;
import com.tuya.luban.biz.service.lan.mapper.IGatewayRecoverMapper;
import com.tuya.luban.biz.service.projectdoor.domains.ProjectDoorResponse;
import com.tuya.luban.biz.service.projectdoor.impl.SpaceTreeManagerWrapper;
import com.tuya.luban.biz.service.taskext.ITaskExtInfoManager;
import com.tuya.luban.biz.service.taskext.domain.TaskExtDTO;
import com.tuya.luban.biz.service.taskext.enums.ContentTypeEnum;
import com.tuya.luban.biz.service.taskext.enums.ExtTypeEnum;
import com.tuya.luban.biz.service.taskext.enums.ScopeTypeEnum;
import com.tuya.luban.biz.service.template.ITemplateConfigManage;
import com.tuya.luban.biz.service.template.domains.DeviceDTO;
import com.tuya.luban.biz.service.template.domains.TemplateDTO;
import com.tuya.luban.client.enums.ConstructionTypeEnum;
import com.tuya.luban.common.cache.redis.RedisHelper;
import com.tuya.luban.core.dao.IGatewayRecoverDAO;
import com.tuya.luban.core.dao.IGatewayRecoverErrorDAO;
import com.tuya.luban.core.dao.ILanRecoverExtDAO;
import com.tuya.luban.core.dao.ITaskRecordDAO;
import com.tuya.luban.core.dao.domains.meta.LanRecoverExt;
import com.tuya.luban.core.dao.domains.meta.LubanTaskRecord;
import com.tuya.luban.core.dao.domains.operate.OperateLogDO;
import com.tuya.luban.core.dao.domains.recover.GatewayRecoverDO;
import com.tuya.luban.core.dao.domains.recover.GatewayRecoverErrorDO;
import com.tuya.luban.core.dao.domains.recover.GatewayRecoverQuery;
import com.tuya.luban.core.dao.impl.OperateLogDAO;
import com.tuya.luban.core.utils.PrimaryIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : patrickkk
 * @date 2021-11-12
 */
@Slf4j
@Component
public class LanGatewayRecoverService implements ILanGatewayRecoverService {

    @Autowired
    private IGatewayRecoverDAO gatewayRecoverDAO;

    @Autowired
    private IGatewayRecoverErrorDAO gatewayRecoverErrorDAO;

    @Autowired
    private SpaceTreeManagerWrapper spaceTreeManagerWrapper;

    @Autowired
    private ITaskRecordDAO taskRecordDAO;

    @Autowired
    private ApolloDynamicConfig apolloDynamicConfig;

    @Autowired
    private ILanRecoverExtDAO lanRecoverExtDAO;

    @Autowired
    private OperateLogDAO operateLogDAO;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private ITemplateConfigManage templateConfigManage;

    @Autowired
    private ITaskExtInfoManager taskExtInfoManager;

    private static final int DEBUG_ERROR_MAX_LENGTH = 255;

    private static final ThreadLocal<GatewayRecoverDO> CONTEXT_HOLDER = new TransmittableThreadLocal();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGatewayRecover(String gatewayId, LubanTaskRecord record, String roomId, String content,
                                   ConfigInfo configInfo) {
        //修复西蒙恢复房间问题
        content = repairRoom(record.getProjectId(), content, configInfo);
        //查询是否已存在恢复记录
        GatewayRecoverQuery gatewayRecoverQuery = new GatewayRecoverQuery();
        gatewayRecoverQuery.setWgId(gatewayId);
        List<GatewayRecoverDO> recovers = gatewayRecoverDAO.findByCondition(gatewayRecoverQuery);
        if (CollectionUtils.isEmpty(recovers)) {
            //保存配置信息到mongo
            Long recoverExtId = saveConfig(gatewayId, content);
            //新增恢复记录
            GatewayRecoverDO gatewayRecoverDO = new GatewayRecoverDO();
            gatewayRecoverDO.setNamespace(record.getNamespace());
            gatewayRecoverDO.setProjectId(record.getProjectId());
            gatewayRecoverDO.setRecordId(record.getRecordId());
            gatewayRecoverDO.setHid(roomId);
            gatewayRecoverDO.setWgId(gatewayId);
            gatewayRecoverDO.setRecoverExtId(recoverExtId);
            gatewayRecoverDO.setRecoverStatus(GatewayRecoverStatusEnum.RECOVER_SUCCESS.getStatus());
            int count = gatewayRecoverDAO.addGatewayRecover(gatewayRecoverDO);
            CONTEXT_HOLDER.set(gatewayRecoverDO);
            if (count <= 0) {
                log.error("插入网关恢复记录失败");
            }
            return;
        }
        //已经存在了，重置recoverId，重试次数+1
        GatewayRecoverDO gatewayRecoverDO = recovers.get(0);
        gatewayRecoverDO.setRecoverId(PrimaryIdGenerator.generateId());
        gatewayRecoverDO.setRetryCount(gatewayRecoverDO.getRetryCount() + 1);
        gatewayRecoverDO.setRecoverStatus(GatewayRecoverStatusEnum.RECOVER_SUCCESS.getStatus());
        // 更新任务id, 修复相同设备id，重复提交不同任务导致的等待场景异常
        gatewayRecoverDO.setRecordId(configInfo.getRecordId());
        gatewayRecoverDO.setHid(configInfo.getHid());
        gatewayRecoverDAO.updateGatewayRecover(gatewayRecoverDO);
        CONTEXT_HOLDER.set(gatewayRecoverDO);
    }

    private String repairRoom(String projectId, String content, ConfigInfo configInfo) {
        try {
            LanRecoverConfig lanRecoverConfig = apolloDynamicConfig.getLanRecoverConfig();
            //不需要修复房间
            if (!lanRecoverConfig.isRepairRoom(projectId)) {
                log.info("not repair device room");
                return content;
            }
            log.info("need repair device room");
            //查询模版中设备的房间信息
            TemplateDTO templateDTO = templateConfigManage.getTemplateDTO(Long.valueOf(configInfo.getConfigId()));
            Map<String, String> deviceRoomMap = new HashMap<>();
            templateDTO.getRooms().forEach(roomDTO -> {
                List<DeviceDTO> devices = roomDTO.getDevices();
                if (CollectionUtils.isNotEmpty(devices)) {
                    devices.forEach(deviceDTO -> {
                        deviceRoomMap.put(deviceDTO.getName(), roomDTO.getName());
                    });
                }
            });
            //按照设备名称替换设备房间信息
            List<GatewayDeviceInfo> gateways = configInfo.getGateways();
            if (CollectionUtils.isNotEmpty(gateways)) {
                gateways.stream().map(GatewayDeviceInfo::toAllDeviceInfos).flatMap(List::stream).forEach(deviceInfo -> {
                    if (deviceRoomMap.containsKey(deviceInfo.getName())) {
                        deviceInfo.setRoomName(deviceRoomMap.get(deviceInfo.getName()));
                    }
                });
            }
            content = JSON.toJSONString(configInfo);
            log.info("repair room after. config = {}", content);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return content;
    }

    @Override
    public List<GatewayRecoverDTO> findByMacList(List<String> macList, String hid) {
        GatewayRecoverQuery gatewayRecoverQuery = new GatewayRecoverQuery();
        gatewayRecoverQuery.setHids(Lists.newArrayList(hid));
        gatewayRecoverQuery.setMacList(macList);
        List<GatewayRecoverDO> recovers = gatewayRecoverDAO.findByCondition(gatewayRecoverQuery);
        return IGatewayRecoverMapper.INSTANCE.toGatewayRecoverDTOList(recovers);
    }

    private Long saveConfig(String gatewayId, String content) {
        //保存上传配置信息
        LanRecoverExt recoverExt = new LanRecoverExt();
        recoverExt.setGatewayId(gatewayId);
        recoverExt.setContext(content);
        recoverExt.setStatus(1);
        return lanRecoverExtDAO.add(recoverExt);
    }

    @Override
    public void updateGatewayMac(String gatewayId, String mac) {
        GatewayRecoverDO recoverDO = getGatewayRecover(gatewayId);
        if (recoverDO == null) {
            return;
        }
        //更新网关mac地址
        recoverDO.setMac(mac);
        gatewayRecoverDAO.updateGatewayRecover(recoverDO);
    }

    @Override
    public boolean allGatewayUpload(ConfigInfo config) {
        //单网关方式，当前网关上报就是所有网关上报
        if (config.noMultiGatewayConfig()) {
            return true;
        }
        List<String> macs = config.toGatewayMacs();
        //获取所有mac地址上报记录
        GatewayRecoverQuery gatewayRecoverQuery = new GatewayRecoverQuery();
        gatewayRecoverQuery.setMacList(macs);
        gatewayRecoverQuery.setHids(Lists.newArrayList(config.getHid()));
        // 只查询当前任务对应的记录，兼容无网恢复后再下发无网维修的场景
        gatewayRecoverQuery.setRecordId(config.getRecordId());
        List<GatewayRecoverDO> gatewayRecovers = gatewayRecoverDAO.findByCondition(gatewayRecoverQuery);
        log.info("[allGatewayUpload] macList = {},gatewayRecovers = {}", JSON.toJSONString(macs), JSON.toJSONString(gatewayRecovers));
        boolean allUpload = gatewayRecovers.size() == macs.size();
        if (!allUpload) {
            //没有全部上来，则将状态更新为待恢复
            GatewayRecoverDO recoverDO = CONTEXT_HOLDER.get();
            recoverDO.setRecoverStatus(GatewayRecoverStatusEnum.WAIT_RECOVER.getStatus());
            gatewayRecoverDAO.updateGatewayRecover(recoverDO);
        } else {
            //将之前的网关恢复记录都设置成成功
            gatewayRecovers.forEach(gatewayRecoverDO -> {
                gatewayRecoverDO.setRecoverStatus(GatewayRecoverStatusEnum.RECOVER_SUCCESS.getStatus());
                gatewayRecoverDAO.updateGatewayRecover(gatewayRecoverDO);
            });
        }
        //补充gatewayId，后面导入房间时要用
        Map<String, String> gatewayMacMap = gatewayRecovers.stream()
                .collect(Collectors.toMap(GatewayRecoverDO::getMac, GatewayRecoverDO::getWgId, (a, b) -> a));
        config.getGateways().forEach(gatewayDeviceInfo -> {
            String mac = gatewayDeviceInfo.getMac();
            gatewayDeviceInfo.setGatewayId(gatewayMacMap.get(mac));
        });
        return allUpload;
    }

    GatewayRecoverDO getGatewayRecover(String gatewayId) {
        GatewayRecoverQuery gatewayRecoverQuery = new GatewayRecoverQuery();
        gatewayRecoverQuery.setWgId(gatewayId);
        List<GatewayRecoverDO> gatewayRecovers = gatewayRecoverDAO.findByCondition(gatewayRecoverQuery);
        if (CollectionUtils.isEmpty(gatewayRecovers)) {
            log.warn("找不到网关恢复记录");
            return null;
        }
        return gatewayRecovers.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGatewayRecoverError(GatewayErrorEnum errorEnum, Object params, String debugError) {
        log.info("addGatewayRecoverError errEnum = {} ,params ={}, debugError = {}", errorEnum,
                JSON.toJSONString(params), debugError);
        GatewayRecoverDO recover = CONTEXT_HOLDER.get();
        if (recover == null) {
            log.error("未找到恢复记录缓存");
            return;
        }
        //查询是否已存在恢复记录
        GatewayRecoverQuery gatewayRecoverQuery = new GatewayRecoverQuery();
        gatewayRecoverQuery.setWgId(recover.getWgId());
        List<GatewayRecoverDO> recovers = gatewayRecoverDAO.findByCondition(gatewayRecoverQuery);
        if (CollectionUtils.isEmpty(recovers)) {
            log.error("未找到恢复记录");
            return;
        }
        //设置恢复状态失败
        GatewayRecoverDO recoverDO = recovers.get(0);
        recoverDO.setRecoverStatus(GatewayRecoverStatusEnum.RECOVER_FAIL.getStatus());
        gatewayRecoverDAO.updateGatewayRecover(recoverDO);
        //恢复id
        String recoverId = recoverDO.getRecoverId();
        //新增问题
        GatewayRecoverErrorDO gatewayRecoverErrorDO = new GatewayRecoverErrorDO();
        gatewayRecoverErrorDO.setRecoverId(recoverId);
        gatewayRecoverErrorDO.setWgId(recover.getWgId());
        gatewayRecoverErrorDO.setFailType(errorEnum.getType().name());
        gatewayRecoverErrorDO.setFailLangCode(errorEnum.name());
        String failParams = JSON.toJSONString(params);
        // 日志做截断处理，防止数据库超长，如果在日志期限内，可以查看日志获取错误的详情日志
        gatewayRecoverErrorDO.setFailParams(failParams.length() > 255 ? failParams.substring(0, 255) : failParams);
        if (StringUtils.isNotEmpty(debugError) && debugError.length() > DEBUG_ERROR_MAX_LENGTH) {
            debugError = debugError.substring(0, DEBUG_ERROR_MAX_LENGTH);
        }
        gatewayRecoverErrorDO.setFailDebugMsg(debugError);
        gatewayRecoverErrorDAO.batchAddGatewayRecoverErrors(Lists.newArrayList(gatewayRecoverErrorDO));
    }

    @NotNull
    private List<RecoverErrorDTO> getRecoverErrorDTOS(List<GatewayRecoverErrorDO> gatewayRecoverErrors, String lang) {
        if (CollectionUtils.isEmpty(gatewayRecoverErrors)) {
            return Lists.newArrayList();
        }
        return gatewayRecoverErrors.stream().map(recoverError -> {
            RecoverErrorDTO recoverErrorDTO = new RecoverErrorDTO();
            recoverErrorDTO.setErrorType(recoverError.getFailType());
            //翻译多语言
            recoverErrorDTO.toErrorMessage(recoverError, lang);
            return recoverErrorDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, RecoverResultDTO> getGatewayRecoverStatus(String namespace, String projectId, List<String> hids,
                                                                 String lang) {
        Assert.notNull(namespace, "namespace is null");
        Assert.notNull(projectId, "projectId is null");
        if (CollectionUtils.isEmpty(hids)) {
            return Maps.newHashMap();
        }
        Map<String, RecoverResultDTO> recoverStatusMap = new HashMap<>();
        GatewayRecoverQuery query = new GatewayRecoverQuery();
        query.setNamespace(namespace);
        query.setProjectId(projectId);
        query.setHids(hids);
        List<GatewayRecoverDO> recovers = gatewayRecoverDAO.findByCondition(query);
        if (CollectionUtils.isEmpty(recovers)) {
            putOldTaskRecoverStatus(hids, recoverStatusMap);
            return recoverStatusMap;
        }
        //查询异常结果
        List<String> recoverIds = recovers.stream().map(GatewayRecoverDO::getRecoverId).collect(Collectors.toList());
        List<GatewayRecoverErrorDO> errors = gatewayRecoverErrorDAO.queryByRecoverIds(recoverIds);
        Map<String, List<GatewayRecoverErrorDO>> errorMap = errors.stream()
                .collect(Collectors.groupingBy(GatewayRecoverErrorDO::getRecoverId));
        Map<String, List<GatewayRecoverDO>> recoverMap = recovers.stream()
                .collect(Collectors.groupingBy(GatewayRecoverDO::getHid));
        recoverMap.forEach((hid, recoverList) -> {
            RecoverResultDTO recoverResultDTO = new RecoverResultDTO();
            recoverResultDTO.setStatus(getRecoverStatus(recoverList));
            List<GatewayRecoverErrorDO> errorList = getRecoverErrors(recoverList, errorMap);
            recoverResultDTO.setErrors(getRecoverErrorDTOS(errorList, lang));
            recoverStatusMap.put(hid, recoverResultDTO);
        });
        //组装结果
        log.info("recoverStatusMap = {}", JSON.toJSONString(recoverStatusMap));
        putOldTaskRecoverStatus(hids, recoverStatusMap);
        log.info("put old after recoverStatusMap = {}", JSON.toJSONString(recoverStatusMap));
        return recoverStatusMap;
    }

    private void putOldTaskRecoverStatus(List<String> hids, Map<String, RecoverResultDTO> recoverStatusMap) {
        //过滤出没有恢复状态房间
        List<String> filterHids = hids.stream().filter(hid -> !recoverStatusMap.containsKey(hid))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterHids)) {
            log.info("filterHids is empty");
            return;
        }
        //查询恢复日志
        List<OperateLogDO> operateLogs = operateLogDAO.queryByTargetIdsAndType(filterHids,
                BizOptTypeEnum.TASK_LAN_RECOVER.getKey());
        if (CollectionUtils.isEmpty(operateLogs)) {
            return;
        }
        operateLogs.stream().map(OperateLogDO::getTargetId).forEach(hid -> {
            RecoverResultDTO recoverResultDTO = new RecoverResultDTO();
            recoverResultDTO.setStatus(GatewayRecoverStatusEnum.RECOVER_SUCCESS.getStatus());
            recoverStatusMap.put(hid, recoverResultDTO);
        });
    }

    private Integer getRecoverStatus(List<GatewayRecoverDO> recovers) {
        boolean error = recovers.stream()
                .anyMatch(recover -> GatewayRecoverStatusEnum.RECOVER_FAIL.equalStatus(recover.getRecoverStatus()));
        if (error) {
            return GatewayRecoverStatusEnum.RECOVER_FAIL.getStatus();
        }
        return GatewayRecoverStatusEnum.RECOVER_SUCCESS.getStatus();
    }

    private List<GatewayRecoverErrorDO> getRecoverErrors(List<GatewayRecoverDO> recovers,
                                                         Map<String, List<GatewayRecoverErrorDO>> errorMap) {
        return recovers.stream().map(recover -> errorMap.get(recover.getRecoverId()))
                .filter(CollectionUtils::isNotEmpty).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * 获取失败的恢复记录（如果没有失败的，就返回成功的记录）
     * 
     * @param namespace
     * @param projectId
     * @param hid
     * @return
     */
    private GatewayRecoverDO getGatewayRecoverDO(String namespace, String projectId, String hid) {
        GatewayRecoverQuery query = new GatewayRecoverQuery();
        query.setNamespace(namespace);
        query.setProjectId(projectId);
        query.setHids(Lists.newArrayList(hid));
        List<GatewayRecoverDO> recovers = gatewayRecoverDAO.findByCondition(query);
        if (CollectionUtils.isEmpty(recovers)) {
            throw new BizException(BizErrorEnum.GATEWAY_NOT_RECOVER);
        }
        //优先找其中失败的记录（多网关情况下，可能是其中一个失败）
        return recovers.stream()
                .filter(recover -> GatewayRecoverStatusEnum.RECOVER_FAIL.equalStatus(recover.getRecoverStatus()))
                .findAny().orElse(recovers.get(0));
    }

    private void checkRoomExist(String namespace, String hid, String projectId) {
        //查询房间是否存在
        ProjectDoorResponse projectDoor = spaceTreeManagerWrapper.getProjectDoor(namespace, hid, projectId);
        if (projectDoor == null) {
            throw new BizException(BizErrorEnum.ROOM_NOT_EXIST);
        }
    }

    @Override
    public RetryRecoverDTO retryRecover(String namespace, String projectId, String hid) {
        //检测房间是否存在
        checkRoomExist(namespace, hid, projectId);
        //查询网关恢复状态
        GatewayRecoverDO gatewayRecoverDO = getGatewayRecoverDO(namespace, projectId, hid);
        Integer recoverStatus = gatewayRecoverDO.getRecoverStatus();
        if (GatewayRecoverStatusEnum.RECOVER_SUCCESS.equalStatus(recoverStatus)) {
            throw new BizException(BizErrorEnum.GATEWAY_RECOVER_SUCCESS);
        }
        String content;
        Long recoverExtId = gatewayRecoverDO.getRecoverExtId();
        LanRecoverExt recoverExt = lanRecoverExtDAO.findById(recoverExtId);
        log.info("recoverExt = {}", JSON.toJSONString(recoverExt));
        if (recoverExt == null) {
            //读取配置缓存
            content = redisHelper.opsForString().get(gatewayRecoverDO.getWgId(), RedisKey.CONFIG_KEY);
        } else {
            content = recoverExt.getContext();
        }
        log.info("content = {}", content);
        if (StringUtils.isEmpty(content)) {
            throw new BizException(BizErrorEnum.GATEWAY_CONFIG_NOT_FIND);
        }
        return RetryRecoverDTO.builder().gatewayId(gatewayRecoverDO.getWgId()).content(content).build();
    }

    @Override
    public void addRecoverItem(String wgId, String itemId, String value) {
        TaskExtDTO taskExtDTO = new TaskExtDTO();
        taskExtDTO.setExtName(itemId);
        taskExtDTO.setExtType(ExtTypeEnum.LAN_RECOVER_ITEM_STATUS.getCode());
        taskExtDTO.setContent(value);
        taskExtDTO.setContentType(ContentTypeEnum.STRING.getCode());
        taskExtDTO.setRecordType(ConstructionTypeEnum.LAN.getCode());
        taskExtDTO.setScope(wgId);
        taskExtDTO.setScopeType(ScopeTypeEnum.DEVICE_ID.getCode());
        taskExtInfoManager.addTaskExt(taskExtDTO);
    }

    @Override
    public void removeRecoverItem(String wgId, String itemId, String operator) {
        List<TaskExtDTO> taskExts = taskExtInfoManager.findTaskExt(ConstructionTypeEnum.LAN.getCode(),
                ScopeTypeEnum.DEVICE_ID.getCode(), wgId, ExtTypeEnum.LAN_RECOVER_ITEM_STATUS.getCode());
        List<Long> filterList = taskExts.stream()
                .filter(taskExtDTO -> StringUtils.equals(itemId, taskExtDTO.getExtName())).map(TaskExtDTO::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterList)) {
            return;
        }
        taskExtInfoManager.batchDeleteTaskExt(filterList, operator);
    }

    @Override
    public Map<String, String> getRecoverItemMap(String wgId) {
        List<TaskExtDTO> taskExts = taskExtInfoManager.findTaskExt(ConstructionTypeEnum.LAN.getCode(),
                ScopeTypeEnum.DEVICE_ID.getCode(), Lists.newArrayList(wgId));
        if (CollectionUtils.isEmpty(taskExts)) {
            return Maps.newHashMap();
        }
        return taskExts.stream().collect(Collectors.toMap(TaskExtDTO::getExtName, TaskExtDTO::getContent, (a, b) -> b));
    }

    @Override
    public void mockError(GatewayErrorEnum errorEnum) {
        GatewayRecoverDO recoverDO = CONTEXT_HOLDER.get();
        if (recoverDO == null) {
            return;
        }
        String projectId = recoverDO.getProjectId();
        LanRecoverConfig lanRecoverConfig = apolloDynamicConfig.getLanRecoverConfig();

        if (lanRecoverConfig.isMock(projectId, errorEnum)) {
            throw new RuntimeException("模拟异常");
        }
    }

    @Override
    public void removeCache() {
        CONTEXT_HOLDER.remove();
    }
}
