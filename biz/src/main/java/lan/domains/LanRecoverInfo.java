package lan.domains;

import com.tuya.luban.biz.domain.base.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author jiangwei
 * @date 2020/8/7 10:16 AM
 */
@Getter
@Setter
@Accessors(chain = true)
public class LanRecoverInfo extends ToString {

    private String gatewayId;

    private ConfigInfo configInfo;
}
