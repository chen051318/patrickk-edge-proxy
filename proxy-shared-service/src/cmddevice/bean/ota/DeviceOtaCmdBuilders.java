package cmddevice.bean.ota;

/**
 * @author zhaoyu
 * @date 2020/6/5
 */
public class DeviceOtaCmdBuilders {

    private static DeviceOtaCmdBuilders deviceOtaCmdBuilders = new DeviceOtaCmdBuilders();

    private DeviceOtaCmdBuilders() {
    }

    public static DeviceOtaCmdBuilders getInstance() {
        return deviceOtaCmdBuilders;
    }

    /**
     * 设备呼叫开门的指令
     *
     * @return
     */
    public DeviceOtaCmdBuilder deviceOtaCmdBuilder() {
        return DeviceOtaCmdBuilder.anDeviceOtaCmd();
    }
}
