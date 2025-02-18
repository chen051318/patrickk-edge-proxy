
package lan.domains.recover;

import lombok.Data;

import java.io.Serializable;

/**
 * 网关恢复信息
 *
 * @author patrickkk
 * @since 2021-11-11 20:38:24
 */
@Data
public class GatewayRecoverDTO implements Serializable {

    /**
     * 网关id
     */
    private String wgId;

    /**
     * 网关mac地址
     */
    private String mac;

    /**
     * 恢复id
     */
    private String recoverId;

    /**
     * 恢复配置id
     */
    private Long recoverExtId;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 任务id
     */
    private String recordId;

    /**
     * 房源id
     */
    private String hid;

    /**
     * 恢复状态 失败:0 成功:1
     */
    private Integer recoverStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

}
