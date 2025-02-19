package impl.stategy.strategy.impl.ota;

import com.x.edgegateway.client.common.BaseBean;
import lombok.Getter;
import lombok.Setter;

/**
 * @author patrickkk
 * @date 2020/6/8
 */
@Getter
@Setter
public class DeviceOtaRequest extends BaseBean {

    private long sn;

    /**
     * 3:ota升级成功,4:ota升级失败,恢复到升级前成功
     */
    private Integer status;

}
