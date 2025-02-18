package impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tuya.arthas.client.domain.vo.datapoint.SchemaAnalyzeVO;
import com.tuya.arthas.client.domain.vo.product.ProductVO;
import com.tuya.atop.client.domain.api.ApiRequestDO;
import com.tuya.atop.client.domain.common.AtopResult;
import com.tuya.edgegateway.atop.device.IEGAtopService;
import com.tuya.edgegateway.atop.device.impl.strategy.IDeviceReport;
import com.tuya.edgegateway.atop.device.impl.strategy.StrategyFactoryContext;
import com.tuya.edgegateway.atop.model.request.GatewayActiveRequest;
import com.tuya.edgegateway.atop.model.vo.DeviceVO;
import com.tuya.edgegateway.atop.model.vo.FaceScoreVO;
import com.tuya.edgegateway.atop.model.vo.GatewayVO;
import com.tuya.edgegateway.atop.model.vo.UploadTokenVO;
import com.tuya.edgegateway.client.IBridgeService;
import com.tuya.edgegateway.client.common.PageResult;
import com.tuya.edgegateway.client.common.Paging;
import com.tuya.edgegateway.client.common.ServiceResult;
import com.tuya.edgegateway.client.common.ServiceResultCode;
import com.tuya.edgegateway.client.domain.bridge.request.FaceDetectRequest;
import com.tuya.edgegateway.client.domain.bridge.vo.FaceDetectVO;
import com.tuya.edgegateway.client.domain.cmd.IssueDataCmdResponseVO;
import com.tuya.edgegateway.client.domain.device.DeviceRequest;
import com.tuya.edgegateway.client.netscheme.domain.constants.NetSchemeTypeEnum;
import com.tuya.edgegateway.common.utils.HttpUtils;
import com.tuya.edgegateway.core.device.domain.DeviceQuery;
import com.tuya.edgegateway.integration.service.arthas.IProductServiceClient;
import com.tuya.edgegateway.integration.service.arthas.ISchemaDataPointServiceClient;
import com.tuya.edgegateway.integration.service.athena.IDeviceMServiceClient;
import com.tuya.edgegateway.integration.service.basic.IShortUrlServiceClient;
import com.tuya.edgegateway.manager.base.exception.EdgeException;
import com.tuya.edgegateway.manager.base.utils.BeanPropertyCopyUtils;
import com.tuya.edgegateway.manager.base.utils.PagingUtils;
import com.tuya.edgegateway.manager.cmdissue.domain.IssueDataCmdResultDTO;
import com.tuya.edgegateway.manager.cos.model.FileDownloadDataDTO;
import com.tuya.edgegateway.manager.device.IDeviceManager;
import com.tuya.edgegateway.manager.device.IGatewayManager;
import com.tuya.edgegateway.manager.device.domain.DeviceDTO;
import com.tuya.edgegateway.manager.device.domain.GatewayActiveDTO;
import com.tuya.edgegateway.manager.device.domain.GatewayDTO;
import com.tuya.edgegateway.shared.service.cmdissue.ICmdIssueSharedService;
import com.tuya.edgegateway.shared.service.cos.ICosSharedService;
import com.tuya.edgegateway.shared.service.cos.model.UploadToken;
import com.tuya.edgegateway.shared.service.device.IDeviceActiveSharedService;
import com.tuya.edgegateway.shared.service.device.IGatewaySharedService;
import com.tuya.edgegateway.shared.service.device.model.DeviceReportRequest;
import com.tuya.smart.client.domain.device.vo.GatewayCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: patrickkk
 * date： 2019/12/4
 */
@Service("EGAtopService")
@Slf4j
public class EGAtopService implements IEGAtopService {

    @Resource
    private IDeviceManager deviceManager;

    @Resource
    private IGatewayManager gatewayManager;

    @Resource
    private ICmdIssueSharedService cmdIssueSharedService;

    @Resource
    private IDeviceActiveSharedService deviceActiveSharedService;

    @Resource
    private IGatewaySharedService gatewaySharedService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private StrategyFactoryContext strategyFactoryContext;

    @Resource
    private ICosSharedService cosSharedService;

    @Resource
    private IBridgeService bridgeService;

    @Resource
    private IProductServiceClient productServiceClient;

    @Resource
    private ISchemaDataPointServiceClient schemaDataPointServiceClient;

    @Resource
    private IShortUrlServiceClient shortUrlServiceClient;

    @Resource
    private IDeviceMServiceClient deviceMServiceClient;

    @Value("${gateway.mqtt.url}")
    private String mqttUrl;

    private static final String ALG_URL = "https://ai.tuya-inc.top:7799/cv/throwning-object/tuya-ai/test/upload";

    private static final String AUTH_KEY = "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6ImVjYTk5MjM3OTI0ZjdhZjIyZjQxNjhmOTU5ZTk0YjcyZmNiNmFmNjcifQ.eyJpc3MiOiJodHRwOi8vZGV4LmF1dGguc3ZjOjU1NTYvZGV4Iiwic3ViIjoiQ2cxa1pYWnBZMlV0WTJWdWRHVnlFZ1ZzYjJOaGJBIiwiYXVkIjoiYWktb2lkYy1hdXRoc2VydmljZSIsImV4cCI6NDc2NDI5MDczMSwiaWF0IjoxNjEwNjkwNzMxLCJhdF9oYXNoIjoiRVQ3QlFaR2E0RGNMZG1ET29VWTdaUSIsImVtYWlsIjoiZGV2aWNlLWNlbnRlckB0dXlhLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiZGV2aWNlLWNlbnRlciJ9.zPrWPSu4G699oJBWb7j48Lq5GjgaupeoGMmNAif_N93TjQOA2EbdLVfK05Riu7bFBKqPN_M2QX0MhIKqWywXuqTygLHW3-RGIoY_FzFkS_EZ4U5cxL_7pG0OQiazoTRxoHDepZdESrel4kLLs0kONb_1ewBr1bCtBz6MuiiViQUeyMlatE5P9gWIsSD3Sm6cfFU_FvrWKm0hAyZJUW_eP7amuj8oo6SBSEMbw223_7hIWQa4sWiCHBd0P7nN0OvT4Rx-hz7Flr7sqiStuEZeSUxVR67wSOaj3wWuGCnnw9rcjIJxMgPpL6xJeAeeXYKkoqFMXjFBGhLRhhwVhG8CaA";

    @Override
    public AtopResult<GatewayVO> uploadGateway(String input, ApiRequestDO apiRequestDO) {
        Assert.hasText(input, "input参数不能为空");

        GatewayActiveRequest activeRequest = JSON.parseObject(input, GatewayActiveRequest.class);
        Assert.notNull(activeRequest, "gatewayActiveRequest不能为空！");
        Assert.hasText(activeRequest.getThirdGatewayId(), "三方网关id不能为空");

        GatewayDTO gatewayDTO = transactionTemplate.execute(status -> {
            try {
                GatewayDTO innerGatewayDTO = gatewaySharedService.activateGateway(BeanPropertyCopyUtils.copy(activeRequest, GatewayActiveDTO.class));
                return innerGatewayDTO;
            } catch (Exception e) {
                log.error("uploadGateway error, 事务回滚, input=" + input, e);
                status.setRollbackOnly();
                throw e;
            }
        });

        return AtopResult.newInstance(BeanPropertyCopyUtils.copy(gatewayDTO, GatewayVO.class));
    }

    @Override
    public AtopResult<Boolean> uploadDevices(String jsonData, ApiRequestDO apiRequestDO) {
        // 【1】参数校验
        Assert.hasText(apiRequestDO.getGwId(), "gatewayId不能为空！");
        Assert.hasText(jsonData, "上报设备数据不能为空！");

        // 【2】请求参数转换
        List<DeviceRequest> deviceRequestList = parseDevicedata(jsonData);
        if (CollectionUtils.isEmpty(deviceRequestList)) {
            log.info("uploadDevices, 上传的设备列表为空");
            return AtopResult.newInstance(true);
        }

        if (deviceRequestList.size() > 20) {
            throw new EdgeException("上传的设备列表长度超过20");
        }

        // 【3】查询边缘网关记录
        GatewayDTO gatewayDTO = new GatewayDTO();
        gatewayDTO.setGatewayId(apiRequestDO.getGwId());

        // 【4】对上报的设备列表激活
        List<DeviceDTO> deviceDTOList = BeanPropertyCopyUtils.copy(deviceRequestList, DeviceDTO.class);
        deviceDTOList.forEach(deviceDTO -> {
                    if (StringUtils.isBlank(deviceDTO.getConnectType())) {
                        deviceDTO.setConnectType(NetSchemeTypeEnum.GATEWAY_AUTO_REPORT.getCode());
                    }
                    deviceActiveSharedService.uploadDevice(gatewayDTO, deviceDTO, null);
                }
        );
        return AtopResult.newInstance(true);
    }

    @Override
    public AtopResult<Boolean> uploadIssueDataCmdResult(Long sn, Integer success, String message, String data, ApiRequestDO apiRequestDO) {
        Assert.hasText(apiRequestDO.getGwId(), "gatewayId不能为空！");
        Assert.isTrue(sn > 0, "sn不能为空！");

        IssueDataCmdResultDTO issueDataCmdResultDTO = new IssueDataCmdResultDTO();
        issueDataCmdResultDTO.setSn(sn);
        issueDataCmdResultDTO.setSuccess(success);
        issueDataCmdResultDTO.setMessage(message);

        cmdIssueSharedService.dealCmdIssueResult(issueDataCmdResultDTO);

        return AtopResult.newInstance(true);
    }

    @Override
    public AtopResult<Boolean> uploadIssueDataCmdResultWithType(String input, ApiRequestDO apiRequestDO) {
        Assert.hasText(input, "input 参数不能为空");
        Assert.hasText(apiRequestDO.getGwId(), "网关id不能为空");

        DeviceReportRequest deviceReportRequest = JSON.parseObject(input, DeviceReportRequest.class);
        String processTag = deviceReportRequest.getBizType() + deviceReportRequest.getOperateType();

        IDeviceReport reporter = strategyFactoryContext.getInstance(processTag);
        if (null == reporter) {
            log.warn("process tag not found {}", processTag);
            return AtopResult.newInstance(ServiceResultCode.SERVICE_INVOKE_TYPE_INVALID.name(), ServiceResultCode.SERVICE_INVOKE_TYPE_INVALID.getMeaning());
        }

        reporter.report(apiRequestDO, deviceReportRequest.getData());

        return AtopResult.newInstance(true);
    }

    @Override
    public AtopResult<PageResult<DeviceVO>> queryDevice(Integer pageIndex, Integer limit, ApiRequestDO apiRequestDO) {
        Assert.hasText(apiRequestDO.getGwId(), "gatewayId不能为空！");
        Assert.notNull(pageIndex, "pageIndex不能为空！");
        Assert.notNull(limit, "limit不能为空！");
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }

        Paging paging = new Paging();
        paging.setPageIndex(pageIndex);
        paging.setLimit(limit);

        DeviceQuery query = new DeviceQuery();
        query.setGatewayId(apiRequestDO.getGwId());
        query.setLimit(paging.getLimit());
        query.setOffset(paging.getLimit() * (paging.getPageIndex() - 1));

        long count = deviceManager.count(query);
        if (count <= 0) {
            return AtopResult.newInstance(PagingUtils.toEmptyPageResult(paging));
        }

        List<DeviceDTO> deviceDTOList = deviceManager.queryDeviceList(query);
        PageResult<DeviceVO> pageResult = PagingUtils.toPageResult(BeanPropertyCopyUtils.copy(deviceDTOList, DeviceVO.class), count, paging);
        return AtopResult.newInstance(pageResult);
    }

    @Override
    public AtopResult<DeviceVO> queryDeviceForCid(String cid, ApiRequestDO apiRequestDO) {
        Assert.hasText(apiRequestDO.getGwId(), "gatewayId不能为空！");
        Assert.hasText(cid, "cid不能为空！");

        DeviceDTO deviceDTO = deviceManager.queryDeviceByCid(apiRequestDO.getGwId(), cid);
        DeviceVO deviceResponseVO = BeanPropertyCopyUtils.copy(deviceDTO, DeviceVO.class);
        return AtopResult.newInstance(deviceResponseVO);
    }

    @Override
    public AtopResult<GatewayVO> queryGatewayForGatewayId(String gatewayId, ApiRequestDO apiRequestDO) {
        Assert.hasText(gatewayId, "gatewayId不能为空");

        GatewayDTO gatewayDTO = gatewayManager.queryByGatewayId(gatewayId);
        Assert.notNull(gatewayDTO, "该网关不存在");
        GatewayVO gatewayVO = BeanPropertyCopyUtils.copy(gatewayDTO, GatewayVO.class);
        gatewayVO.setMqttUrl(mqttUrl);

        GatewayCache gatewayCache = deviceMServiceClient.getGatewayCache(gatewayId);
        if (null != gatewayCache) {
            gatewayVO.setLocalKey(gatewayCache.getLocalKey());
            gatewayVO.setSecKey(gatewayCache.getSecKey());
        }

        return AtopResult.newInstance(gatewayVO);
    }

    @Override
    @Deprecated
    public AtopResult<List<IssueDataCmdResponseVO>> queryIssueDataCmd(Long startCursor, Integer limit, ApiRequestDO apiRequestDO) {
        Assert.hasText(apiRequestDO.getGwId(), "gatewayId不能为空！");
        Assert.isTrue(limit > 0 && limit <= 100, "limit不能小于0并且不能大于100");
        return AtopResult.newInstance(Collections.EMPTY_LIST);
    }

    @Override
    public AtopResult<List<SchemaAnalyzeVO>> querySchema(String cid, ApiRequestDO apiRequestDO) {
        Assert.hasText(apiRequestDO.getGwId(), "gatewayId不能为空！");

        ProductVO productVO;
        if (StringUtils.isNotBlank(cid)) {
            DeviceDTO deviceDTO = deviceManager.queryDeviceByCid(apiRequestDO.getGwId(), cid);
            Assert.notNull(deviceDTO, "设备不存在");
            productVO = productServiceClient.getVoById(deviceDTO.getProductId());
        } else {
            GatewayDTO gatewayDTO = gatewayManager.queryByGatewayId(apiRequestDO.getGwId());
            Assert.notNull(gatewayDTO, "网关不存在");
            productVO = productServiceClient.getVoById(gatewayDTO.getProductId());
        }
        Assert.notNull(productVO, "未查询到该产品");
        return AtopResult.newInstance(schemaDataPointServiceClient.analyzeSchema(productVO.getSchemaId()));
    }

    private List<DeviceRequest> parseDevicedata(String jsonData) {
        List<DeviceRequest> deviceRequestVOList = JSON.parseArray(jsonData, DeviceRequest.class);
        //验证
        if (CollectionUtils.isNotEmpty(deviceRequestVOList)) {
            deviceRequestVOList.stream().forEach(deviceSyncRequest -> {
                Assert.hasText(deviceSyncRequest.getCid(), "cid不能为空");
                Assert.hasText(deviceSyncRequest.getProductId(), "productId不能为空！");
            });
        }
        return deviceRequestVOList;
    }

    @Override
    public AtopResult<UploadTokenVO> getUploadToken(String subjectType, ApiRequestDO apiRequestDO) {
        Assert.hasText(subjectType, "subjectType不能为空！");

        UploadToken uploadToken = cosSharedService.getUploadToken(subjectType);
        return AtopResult.newInstance(BeanPropertyCopyUtils.copy(uploadToken, UploadTokenVO.class));
    }

    @Override
    public AtopResult<FaceScoreVO> evaluateFaceQuality(String fileId, ApiRequestDO apiRequestDO) {
        //根据文件ID获取图片url
        FileDownloadDataDTO fileDownloadDataDTO = cosSharedService.downloadUrlByTmpFileId(fileId);
        if (fileDownloadDataDTO == null) {
            return AtopResult.newInstance(ServiceResultCode.INTERNAL_ERROR.name(), ServiceResultCode.INTERNAL_ERROR.getMeaning());
        }

        //人脸分数监测
        FaceDetectRequest request = new FaceDetectRequest();
        request.setFaceUrl(fileDownloadDataDTO.getDownloadUrl());
        //调用评分接口
        ServiceResult<FaceDetectVO> ret = bridgeService.faceDetectScore(request);

        //长链接转短链接
        String shotUrl = shortUrlServiceClient.getShotUrl(fileDownloadDataDTO.getDownloadUrl());

        //组装返回结果
        FaceScoreVO faceScoreVO = new FaceScoreVO();
        faceScoreVO.setUrl(shotUrl);
        Double value = ret.getData() == null ? 0 : ret.getData().getScore().doubleValue();
        faceScoreVO.setValue(value);

        return AtopResult.newInstance(faceScoreVO);
    }


    @Override
    public AtopResult<Boolean> uploadAlgInfo(String input, ApiRequestDO apiRequestDO) {
        Assert.hasText(input, "请求参数不能为空");
        JSONObject jsonObject = JSON.parseObject(input);
        String fileId = jsonObject.getString("fileId");
        Assert.hasText(fileId, "文件id不能为空");
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", AUTH_KEY);

        FileDownloadDataDTO fileDownloadDataDTO = cosSharedService.downloadUrlByTmpFileId(fileId);
        if (fileDownloadDataDTO == null) {
            return AtopResult.newInstance(ServiceResultCode.INTERNAL_ERROR.name(), ServiceResultCode.INTERNAL_ERROR.getMeaning());
        }

        String downloadUrl = fileDownloadDataDTO.getDownloadUrl();
        HttpUtils.multipartPostWithHeadName(ALG_URL, header, "file-name", downloadUrl);
        return AtopResult.newInstance(true);
    }
}
