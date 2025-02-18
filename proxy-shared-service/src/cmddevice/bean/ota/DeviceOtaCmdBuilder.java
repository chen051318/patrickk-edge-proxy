package cmddevice.bean.ota;

import com.tuya.edgegateway.client.domain.ndp.common.AbstractCmdBuilder;

import java.util.List;

/**
 * @author zhaoyu
 * @date 2020/6/5
 */
public class DeviceOtaCmdBuilder extends AbstractCmdBuilder<DeviceOtaCmd, DeviceOtaCmdBuilder> {
    private List<DeviceOtaData> otaUpgradeFileRequestList;

    public static DeviceOtaCmdBuilder anDeviceOtaCmd() {
        return new DeviceOtaCmdBuilder();
    }

    public DeviceOtaCmdBuilder withOtaUpgradeFileRequestList(List<DeviceOtaData> otaUpgradeFileRequestList) {
        this.otaUpgradeFileRequestList = otaUpgradeFileRequestList;
        return this;
    }

    @Override
    protected Class<DeviceOtaCmd> cmdClass() {
        return DeviceOtaCmd.class;
    }

    @Override
    protected void addFields(DeviceOtaCmd cmd) {
        cmd.setOtaUpgradeFileRequestList(otaUpgradeFileRequestList);
    }
}
