
package lan.domains.recover;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author : patrickkk
 * @date 2021-11-15
 */
@Data
@Builder
public class RetryRecoverDTO implements Serializable {

    /**
     * 网关id
     */
    private String gatewayId;

    /**
     * 网关上传的配置
     */
    private String content;
}
