package cmddevice.bean;

import cmddevice.builders.DoorControlCmdBuilders;
import cmddevice.builders.ElevatorControlCmdBuilders;
import cmddevice.builders.LightControlCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.card.CardIssuerBuilders;
import com.tuya.edgegateway.client.domain.ndp.dc.DoorControlCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.ec.ElevatorControlCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.energy.EnergyCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.firecontrol.FireControlCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.gw.GateWayCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.gw.devicerelation.DeviceRelationCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.intrusion.IntrusionControlCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.intrusion.IntrusionPointCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.light.LightControlCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.log.upload.DeviceLogCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.nvr.NvrCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.ota.DeviceOtaCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.pa.ParkingAreaCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.virtual.VirtualCmdBuilders;
import com.tuya.edgegateway.client.domain.ndp.webcam.WebcamCmdBuilders;

/**
 * 构建面向所有设备指令的build工厂类
 *
 * @author patrickkk  2020/9/23 11:01
 */
public abstract class CmdBuilderFactory {

    /**
     * 构建面向网关设备的下发指令builders
     *
     * @return
     */
    public static GateWayCmdBuilders gateWayCmdBuilders() {
        return new GateWayCmdBuilders();
    }

    /**
     * 构建面向门禁设备的下发指令builders
     *
     * @return
     */
    public static DoorControlCmdBuilders doorControlCmdBuilders() {
        return DoorControlCmdBuilders.getInstance();
    }

    /**
     * 构建面向梯控设备的下发指令builders
     *
     * @return
     */
    public static ElevatorControlCmdBuilders elevatorControlCmdBuilders() {
        return ElevatorControlCmdBuilders.getInstance();
    }

    /**
     * 构建灯控设备的下发指令builders
     *
     * @return
     */
    public static LightControlCmdBuilders lightControlCmdBuilders() {
        return LightControlCmdBuilders.getInstance();
    }

    /**
     * 构建面向监控设备的下发指令builders
     *
     * @return
     */
    public static WebcamCmdBuilders webcamCmdBuilders() {
        return WebcamCmdBuilders.getInstance();
    }

    /**
     * 构建面向消防设备的下发指令builders
     *
     * @return
     */
    public static FireControlCmdBuilders fireControlCmdBuilders() {
        return FireControlCmdBuilders.getInstance();
    }

    /**
     * 构建周届主机设备下发指令的builders
     *
     * @return
     */
    public static IntrusionControlCmdBuilders intrusionControlCmdBuilders() {
        return IntrusionControlCmdBuilders.getInstance();
    }

    /**
     * 构建周届防区设备下发指令的builders
     *
     * @return
     */
    public static IntrusionPointCmdBuilders intrusionPointCmdBuilders() {
        return IntrusionPointCmdBuilders.getInstance();
    }

    /**
     * 能源管理设备下发指令的builders
     *
     * @return
     */
    public static EnergyCmdBuilders energyCmdBuilders() {
        return EnergyCmdBuilders.getInstance();
    }

    /**
     * 构建面向车场设备的下发指令builders
     *
     * @return
     */
    public static ParkingAreaCmdBuilders parkingAreaCmdBuilders() {
        return ParkingAreaCmdBuilders.getInstance();
    }


    /**
     * 构建日志上传的下发指令builders
     *
     * @return
     */
    public static DeviceLogCmdBuilders deviceLogCmdBuilders() {
        return DeviceLogCmdBuilders.getInstance();
    }

    public static DeviceOtaCmdBuilders deviceOtaCmdBuilders() {
        return DeviceOtaCmdBuilders.getInstance();
    }

    /**
     * 构建ai设备关系的下发指令builders
     *
     * @return
     */
    public static DeviceRelationCmdBuilders deviceRelationCmdBuilders() {
        return DeviceRelationCmdBuilders.getInstance();
    }

    /**
     * 构建虚拟设备的下发指令builders
     *
     * @return
     */
    public static VirtualCmdBuilders virtualCmdBuilders() {
        return VirtualCmdBuilders.getInstance();
    }

    public static CardIssuerBuilders cardIssuerBuilders() {
        return CardIssuerBuilders.getInstance();
    }

    /**
     * nvr相关指令
     *
     * @return
     */
    public static NvrCmdBuilder nvrCmdBuilder() {
        return NvrCmdBuilder.getInstance();
    }
}
