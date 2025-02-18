
package lan.domains;

import lombok.Data;

import java.util.Map;

/**
 * 无网恢复上下文信息
 * 
 * @author : patrickkk
 * @date 2022-08-19
 */
@Data
public class LanRecoverContext {

    /**
     * 网关id
     */
    String gwId;

    /**
     * 家庭所有者
     */
    String uid;

    /**
     * 家庭id
     */
    String ownerId;

    /**
     * 虚拟设备id-真实设备id
     */
    Map<String, String> devIdMap;

    /**
     * mac-真实设备id
     */
    Map<String, String> macMap;

    /**
     * 虚拟场景id - 真实场景id
     */
    Map<String, String> ruleVOMap;

    private ConfigInfo config;

    public LanRecoverContext(String gwId, String uid, String ownerId, Map<String, String> devIdMap, Map<String, String> macMap,
                             Map<String, String> ruleVOMap, ConfigInfo config) {
        this.gwId = gwId;
        this.uid = uid;
        this.ownerId = ownerId;
        this.devIdMap = devIdMap;
        this.macMap = macMap;
        this.ruleVOMap = ruleVOMap;
        this.config = config;
    }
}
