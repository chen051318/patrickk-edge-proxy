package cmddevice.bean.ota;

import com.x.edgegateway.client.domain.ndp.common.CmdSupport;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zhaoyu
 * @date 2020/6/5
 */
@Getter
@Setter
public class DeviceOtaCmd extends CmdSupport {

    private List<DeviceOtaData> otaUpgradeFileRequestList;

    private String bizType;

    private String operateType;

    public DeviceOtaCmd() {
        setDpid(2);
        setGateway(true);
        this.bizType = "ota";
        this.operateType = "upgrade";
    }

    @Override
    public Object getData() {
        return null;
    }

}
