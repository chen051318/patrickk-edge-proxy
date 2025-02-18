
package lan.domains.recover;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 无网恢复结果
 * 
 * @author : patrickkk
 * @date 2021-11-24
 */
@Data
public class RecoverResultDTO implements Serializable {

    /**
     * 恢复状态
     */
    private Integer status;

    /**
     * 异常信息
     */
    List<RecoverErrorDTO> errors;
}
