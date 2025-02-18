
package lan.domains.recover.params;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author : patrickkk
 * @date 2021-11-12
 */
@Data
@Builder
public class SceneErrorParamsDTO implements Serializable {

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * dpId
     */
    private String dpId;

    /**
     * dp名称
     */
    private String dpName;
}
