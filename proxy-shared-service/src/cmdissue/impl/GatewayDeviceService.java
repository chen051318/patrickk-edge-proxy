package cmdissue.impl;

import cmdissue.IGatewayDeviceService;
import com.tuya.caesar.client.domain.protocol.ProtocolNumberEnum;
import com.tuya.caesar.client.domain.protocol.ProtocolTypeEnum;
import com.tuya.edgegateway.client.IGatewayDeviceService;
import com.tuya.edgegateway.client.common.PageResult;
import com.tuya.edgegateway.client.common.Paging;
import com.tuya.edgegateway.client.common.ServiceResult;
import com.tuya.edgegateway.client.common.ServiceResultCode;
import com.tuya.edgegateway.client.domain.gateway.*;
import com.tuya.edgegateway.core.device.domain.DeviceQuery;
import com.tuya.edgegateway.core.device.domain.GatewayQuery;
import com.tuya.edgegateway.integration.service.athena.IDeviceMServiceClient;
import com.tuya.edgegateway.integration.service.caesar.IMessagePushServiceClient;
import com.tuya.edgegateway.integration.service.usercenter.IUserServiceClient;
import com.tuya.edgegateway.manager.base.exception.EdgeException;
import com.tuya.edgegateway.manager.base.utils.BeanPropertyCopyUtils;
import com.tuya.edgegateway.manager.base.utils.PagingUtils;
import com.tuya.edgegateway.manager.device.IDeviceManager;
import com.tuya.edgegateway.manager.device.IGatewayManager;
import com.tuya.edgegateway.manager.device.domain.GatewayActiveDTO;
import com.tuya.edgegateway.manager.device.domain.GatewayDTO;
import com.tuya.edgegateway.manager.kafka.producer.IKafkaMsgProducer;
import com.tuya.edgegateway.manager.log.IOperationLogManager;
import com.tuya.edgegateway.manager.log.domain.OperationLogDTO;
import com.tuya.edgegateway.manager.log.domain.convert.OperationLogConvert;
import com.tuya.edgegateway.manager.netscheme.ProjectManager;
import com.tuya.edgegateway.manager.netscheme.domain.ProjectDTO;
import com.tuya.edgegateway.shared.service.device.IGatewaySharedService;
import com.tuya.smart.client.domain.device.vo.GatewayCache;
import com.tuya.usercenter.user.client.dto.usermgr.mix.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: patrickkk
 * date： 2019/10/29
 */
@Slf4j
@Service("gatewayDeviceService")
public class GatewayDeviceService implements IGatewayDeviceService {

    @Resource
    private IDeviceManager deviceManager;

    @Resource
    private IGatewayManager gatewayManager;

    @Resource
    private IKafkaMsgProducer kafkaMsgProducer;

    @Resource
    private IGatewaySharedService gatewaySharedService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IOperationLogManager operationLogManager;

    @Resource
    private IDeviceMServiceClient deviceMServiceClient;

    @Resource
    private IMessagePushServiceClient messagePushServiceClient;

    @Resource
    private IUserServiceClient userServiceClient;

    @Resource
    private ProjectManager projectManager;

    @Override
    public ServiceResult<GatewayResponse> gatewayActive(GatewayActiveRequest gatewayActiveRequest) {
        Assert.notNull(gatewayActiveRequest, "请求不能为空");
        Assert.hasText(gatewayActiveRequest.getProjectId(), "项目id不能为空");
        Assert.hasText(gatewayActiveRequest.getThirdGatewayId(), "三方网关id不能为空");
        Assert.hasText(gatewayActiveRequest.getGatewayType(), "网关类型不能为空");

            /*if (!Strings.isNullOrEmpty(gatewayActiveRequest.getThirdGatewayId())) {
                // 校验网关设备id白名单
                List<String> thirdGatewayIdList = ApolloConfigUtil.getThirdGatewayIdList();
            boolean exist = thirdGatewayIdList.stream().anyMatch(x -> x.equalsIgnoreCase(gatewayActiveRequest.getThirdGatewayId()));
            if (!exist) {
                return ServiceResult.asFailed(ServiceResultCode.LOGIC_VALIDATE_ERROR, "设备ID不存在");
            }
        }*/

        //【1】激活网关
        GatewayDTO gatewayDTO = transactionTemplate.execute(status -> {
            try {
                GatewayDTO innerGatewayDTO = gatewaySharedService.activateGateway(BeanPropertyCopyUtils.copy(gatewayActiveRequest, GatewayActiveDTO.class));
                //【2】记录日志
                OperationLogDTO operationLogDTO = OperationLogConvert.convert(gatewayActiveRequest);
                operationLogManager.add(operationLogDTO);
                return innerGatewayDTO;
            } catch (Exception e) {
                log.error("gatewayActive error, 事务回滚, gatewayActiveRequest=" + gatewayActiveRequest, e);
                status.setRollbackOnly();
                throw e;
            }
        });

        return ServiceResult.asSuccessfully(BeanPropertyCopyUtils.copy(gatewayDTO, GatewayResponse.class));
    }

    @Override
    public ServiceResult<PageResult<GatewayResponse>> queryGatewayListPage(GatewayQueryRequest gatewayQueryRequest) {
        Assert.notNull(gatewayQueryRequest, "请求参数不能为null");
        Assert.isTrue(gatewayQueryRequest.getLimit() <= 200, "每页记录数不能超过200条");

        GatewayQuery gatewayQuery = new GatewayQuery();
        gatewayQuery.setKeyword(gatewayQueryRequest.getKeyword());
        gatewayQuery.setProjectId(gatewayQueryRequest.getProjectId());
        gatewayQuery.setGatewayId(gatewayQueryRequest.getGatewayId());
        gatewayQuery.setProductType(gatewayQueryRequest.getProductType());
        gatewayQuery.setProductId(gatewayQueryRequest.getProductId());
        gatewayQuery.setThirdGatewayId(gatewayQueryRequest.getThirdGatewayId());
        gatewayQuery.setOnlineStatus(gatewayQueryRequest.getOnlineStatus());
        gatewayQuery.setUid(gatewayQuery.getUid());
        gatewayQuery.setTaskId(gatewayQueryRequest.getTaskId());

        Paging paging = new Paging();
        paging.setPageIndex(gatewayQueryRequest.getPageIndex());
        paging.setLimit(gatewayQueryRequest.getLimit());

        PageResult<GatewayDTO> gatewayDTOPageResult = gatewaySharedService.queryGatewayPage(gatewayQuery, paging);
        PageResult<GatewayResponse> result = PagingUtils.copyPageResult(gatewayDTOPageResult, GatewayResponse.class);
        List<GatewayResponse> content = result.getContent();

        Map<String, GatewayCache> gatewayCacheMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(content)) {
            List<String> ids = content.stream().map(GatewayResponse::getGatewayId).collect(Collectors.toList());
            gatewayCacheMap = deviceMServiceClient.getGatewayCaches(ids);
        }

        for (var x : content) {
            DeviceQuery query = new DeviceQuery();
            query.setGatewayId(x.getGatewayId());
            long count = deviceManager.count(query);
            x.setDeviceNum(count);

            UserDTO userDTO = userServiceClient.getVoByUid(x.getUid());
            if (userDTO != null) {
                x.setUserAccount(userDTO.getUsername() == null ? "" : userDTO.getUsername());
            }
            ProjectDTO projectDTO = projectManager.load(x.getProjectId(), x.getUid(), false);
            if (projectDTO != null) {
                x.setProjectName(projectDTO.getProjectName() == null ? "" : projectDTO.getProjectName());
            }
        }

        return ServiceResult.asSuccessfully(result);
    }

    @Override
    public ServiceResult<List<GatewayResponse>> queryGatewayByGwIdList(List<String> gatewayIdList) {
        Assert.isTrue(CollectionUtils.isNotEmpty(gatewayIdList), "网关id列表不能为空");
        Assert.isTrue(gatewayIdList.size() <= 20, "查询网关列表不能超过20条数据");
        List<GatewayResponse> gatewayResponseList = BeanPropertyCopyUtils.copy(gatewayManager.queryGatewayListByGwIds(gatewayIdList),
                GatewayResponse.class);
        return ServiceResult.asSuccessfully(gatewayResponseList);
    }

    @Override
    public ServiceResult<GatewayResponse> gatewayOfType(String projectId, String type) {
        Assert.hasText(projectId, "小区id不能为空！");
        Assert.hasText(type, "type不能为空！");
        GatewayDTO gatewayDTO = gatewayManager.queryByProjectIdAndType(projectId, type);
        return ServiceResult.asSuccessfully(BeanPropertyCopyUtils.copy(gatewayDTO, GatewayResponse.class));
    }

    @Override
    public ServiceResult<GatewayResponse> queryGatewayByPrjAndGwId(GatewayQueryRequest gatewayRequest) {
        Assert.notNull(gatewayRequest, "请求参数不能为null");
        Assert.hasText(gatewayRequest.getGatewayId(), "网关id不能为空");
        GatewayDTO gatewayDTO = gatewayManager.queryGatewayByPrjAndGwId(BeanPropertyCopyUtils.copy(gatewayRequest, GatewayQuery.class));
        return ServiceResult.asSuccessfully(BeanPropertyCopyUtils.copy(gatewayDTO, GatewayResponse.class));
    }

    @Override
    public ServiceResult<Boolean> notifyGatewayForActiveEvent(GatewayNotifyRequest request) {
        Assert.notNull(request, "请求参数不能为null");
        Assert.hasText(request.getGatewayId(), "网关id不能为空");

        GatewayCache gatewayCache = deviceMServiceClient.getGatewayCache(request.getGatewayId());
        if (gatewayCache == null) {
            throw new EdgeException("网关设备不存在!");
        }
        if (gatewayCache.isSub()) {
            throw new EdgeException("非网关设备不能操作!");
        }

        if (StringUtils.isNotBlank(request.getDeviceId())) {
            //子设备信息有变化
            Map<String, Object> dataMap = new HashMap();
            dataMap.put("deviceId", request.getDeviceId());
            messagePushServiceClient.issueDeviceProtocolCommand(gatewayCache, ProtocolNumberEnum.SUB_DEVICE_ADD_DEL, ProtocolTypeEnum.UPD, dataMap);
        } else {
            //网关下的子设备信息有变化
            messagePushServiceClient.issueDeviceProtocolCommand(gatewayCache, ProtocolNumberEnum.SUB_DEVICE_ADD_DEL, ProtocolTypeEnum.BAT_ADD, null);
        }
        return ServiceResult.asSuccessfully(true);
    }

    @Override
    public ServiceResult<Boolean> modifyByGatewayId(GatewayModifyRequest request) {
        Assert.notNull(request, "请求参数不能为null");
        Assert.hasText(request.getGatewayType(), "网关类型不能为空");
        Assert.hasText(request.getDeviceName(), "网关名称不能为空");

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    //【1】更新网关
                    GatewayDTO gatewayDTO = new GatewayDTO();
                    gatewayDTO.setGatewayType(request.getGatewayType());
                    gatewayDTO.setDeviceName(request.getDeviceName());
                    gatewayDTO.setGatewayId(request.getGatewayId());
                    gatewayManager.updateByGwId(gatewayDTO);

                    //【2】记录日志
                    OperationLogDTO operationLogDTO = OperationLogConvert.convert(request);
                    operationLogManager.add(operationLogDTO);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw new EdgeException("更新网关记录异常", e);
                }
            }
        });
        return ServiceResult.asSuccessfully(true);
    }

    @Override
    public ServiceResult<Boolean> delByGatewayId(GatewayDelRequest request) {
        Assert.notNull(request, "请求参数不能为null");
        Assert.hasText(request.getGatewayId(), "网关id不能为空");

        //根据网关id查询子设备，有记录不允许删除
        DeviceQuery query = new DeviceQuery();
        query.setGatewayId(request.getGatewayId());

        long count = deviceManager.count(query);
        if (count > 0) {
            return ServiceResult.asFailed(ServiceResultCode.INTERNAL_ERROR, "该网关下有子设备，不能删除");
        }

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    //【1】查询网关记录
                    GatewayDTO unBindGatewayDTO = gatewayManager.queryByGatewayId(request.getGatewayId());
                    if (unBindGatewayDTO == null) {
                        throw new EdgeException("网关不存在,不能删除");
                    }

                    //【2】删除网关
                    gatewayManager.deleteByGwId(unBindGatewayDTO.getGatewayId());
                    //发送kafka网关解绑消息
                    kafkaMsgProducer.pushGatewayUnbindMessage(unBindGatewayDTO);

                    //【3】记录日志
                    OperationLogDTO operationLogDTO = OperationLogConvert.convert(request);
                    operationLogManager.add(operationLogDTO);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw new EdgeException("删除网关记录异常", e);
                }
            }
        });

        return ServiceResult.asSuccessfully(true);
    }
}
