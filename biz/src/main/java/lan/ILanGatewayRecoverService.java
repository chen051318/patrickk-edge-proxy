
package lan;

import com.x.luban.biz.enums.GatewayErrorEnum;
import com.x.luban.biz.service.lan.domains.ConfigInfo;
import com.x.luban.biz.service.lan.domains.recover.GatewayRecoverDTO;
import com.x.luban.biz.service.lan.domains.recover.RecoverResultDTO;
import com.x.luban.biz.service.lan.domains.recover.RetryRecoverDTO;
import com.x.luban.core.dao.domains.meta.LubanTaskRecord;

import java.util.List;
import java.util.Map;

/**
 * @author : patrickkk
 * @date 2021-11-12
 */
public interface ILanGatewayRecoverService {

    /**
     * 保存网关恢复记录
     * 
     * @param gatewayId
     * @param record
     * @param roomId
     * @param content
     * @param configInfo
     * @return
     */
    void saveGatewayRecover(String gatewayId, LubanTaskRecord record, String roomId, String content, ConfigInfo configInfo);

    /**
     * 根据mac集获取网关恢复信息
     * 
     * @param macList
     * @param hid
     * @return
     */
    List<GatewayRecoverDTO> findByMacList(List<String> macList, String hid);

    /**
     * 更新网关mac地址
     * 
     * @param gatewayId
     * @param mac
     */
    void updateGatewayMac(String gatewayId, String mac);

    /**
     * 检测是否所有网关都已恢复
     * 
     * @param config
     * @return
     */
    boolean allGatewayUpload(ConfigInfo config);

    /**
     * 添加网关恢复失败信息
     * 
     * @param errorEnum
     * @param params
     * @param debugError
     */
    void addGatewayRecoverError(GatewayErrorEnum errorEnum, Object params, String debugError);

    /**
     * 获取网关恢复状态
     * 
     * @param namespace
     * @param projectId
     * @param hids
     * @param lang
     * @return
     */
    Map<String, RecoverResultDTO> getGatewayRecoverStatus(String namespace, String projectId, List<String> hids,
                                                          String lang);

    /**
     * 尝试恢复重试
     * 
     * @param namespace
     * @param projectId
     * @param hid
     * @return 网关配置信息
     */
    RetryRecoverDTO retryRecover(String namespace, String projectId, String hid);

    /**
     * 记录恢复项
     *
     * @param wgId
     * @param itemId
     * @param value
     */
    void addRecoverItem(String wgId, String itemId, String value);

    /**
     * 记录恢复项
     *
     * @param wgId
     * @param itemId
     * @param operator
     */
    void removeRecoverItem(String wgId, String itemId, String operator);

    /**
     * 查询房间恢复项状态
     * 
     * @param wgId
     * @return
     */
    Map<String, String> getRecoverItemMap(String wgId);

    /**
     * 模拟报错（测试使用）
     * 
     * @param errorEnum
     */
    void mockError(GatewayErrorEnum errorEnum);

    void removeCache();

}
