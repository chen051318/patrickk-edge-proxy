package cmdissue;

import com.tuya.edgegateway.client.common.PageResult;
import com.tuya.edgegateway.client.common.ServiceResult;
import com.tuya.edgegateway.client.domain.gateway.*;

import java.util.List;

/**
 * 网关设备服务类接口
 *
 * @author patrickkk  2020/9/23 09:57
 */
public interface IGatewayDeviceService {

    /**
     * 网关配网
     *
     * @param gatewayActiveRequest 配网请求参数对象
     * @return
     */
    ServiceResult<GatewayResponse> gatewayActive(GatewayActiveRequest gatewayActiveRequest);

    /**
     * 查询网关设备列表
     *
     * @param gatewayQueryRequest
     * @return
     */
    ServiceResult<PageResult<GatewayResponse>> queryGatewayListPage(GatewayQueryRequest gatewayQueryRequest);

    /**
     * 根据网关id列表查询网关信息
     *
     * @param gatewayIdList
     * @return
     */
    ServiceResult<List<GatewayResponse>> queryGatewayByGwIdList(List<String> gatewayIdList);

    /**
     * 小区对应的边缘网关
     *
     * @param projectId 小区id
     * @param type      网关类别
     * @return
     */
    @Deprecated
    ServiceResult<GatewayResponse> gatewayOfType(String projectId, String type);

    /**
     * 查询网关信息
     *
     * @param gatewayRequest
     * @return
     */
    ServiceResult<GatewayResponse> queryGatewayByPrjAndGwId(GatewayQueryRequest gatewayRequest);


    /**
     * 子设备全部激活后, 通知网关
     *
     * @param    request
     * @return
     */
    ServiceResult<Boolean> notifyGatewayForActiveEvent(GatewayNotifyRequest request);

    /**
     * 根据网关id更新网关信息
     *
     * @param request
     * @return
     */
    ServiceResult<Boolean> modifyByGatewayId(GatewayModifyRequest request);

    /**
     * 根据网关id删除网关记录（逻辑删除）
     *
     * @param request
     * @return
     */
    ServiceResult<Boolean> delByGatewayId(GatewayDelRequest request);
}
