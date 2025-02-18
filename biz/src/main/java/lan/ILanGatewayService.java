
package lan;

import com.tuya.luban.client.domains.response.recover.GatewayRecoverProgressDTO;

import java.util.LinkedHashMap;

/**
 * 无网施工网关服务
 *
 * @Author patrickkk
 * @Date 2020-07-04
 */
public interface ILanGatewayService {

    /**
     * 激活网关配置
     *
     * @param gatewayId
     * @param content
     */
    void activateGatewayConfig(String gatewayId, String content);

    /**
     * 激活网关配置进度查询
     * 
     * @param gatewayId
     * @return
     */
    GatewayRecoverProgressDTO getActivateGatewayConfigProgress(String uid, String gatewayId, String lang);

    /**
     * 初始化进度
     */
    void mockInitGatewayRecoverProgress(String recordId, LinkedHashMap<String, Integer> itemNumMap);

    /**
     * 执行中修改进度
     */
    void mockUpdateGatewayRecoverProgress(String recordId, String itemCode, Integer addNum);

    /**
     * 执行完成修改进度
     */
    void mockCompleteGatewayRecoverProgress(String recordId);

    /**
     * 检测配置是否已经存在
     * 
     * @param content
     * @param gatewayId
     * @return
     */
    boolean checkConfig(String content, String gatewayId);
}
