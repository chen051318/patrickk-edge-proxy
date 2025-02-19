package cmddevice.builders;

import com.x.edgegateway.client.domain.ndp.common.IssueDataCmdBuilder;
import com.x.edgegateway.client.domain.ndp.light.LightControl.*;

/**
 * @author patrickkk  2020/8/26 17:01
 */
public final class LightControlCmdBuilders {

    private static LightControlCmdBuilders lightControlCmdBuilders = new LightControlCmdBuilders();

    private LightControlCmdBuilders() {
    }

    public static LightControlCmdBuilders getInstance() {
        return lightControlCmdBuilders;
    }

    /**
     * 获取灯控分组指令builder
     *
     * @return
     */
    public LightGroupCmdBuilder lightGroupCmdBuilder() {
        return LightGroupCmdBuilder.anLightGroupCmd();
    }

    /**
     * 获取开关灯指令builder
     *
     * @return
     */
    public LightControlCmdBuilder lightControlCmdBuilder() {
        return LightControlCmdBuilder.anLightControlCmd();
    }

    /**
     * 获取分组开关灯指令builder
     *
     * @return
     */
    public GroupLightControlCmdBuilder groupLightControlCmdBuilder() {
        return GroupLightControlCmdBuilder.anLightControlCmd();
    }

    /**
     * 获取梯控指令builder
     *
     * @return
     */
    public LightBrightCmdBuilder lightBrightCmdBuilder() {
        return LightBrightCmdBuilder.anLightControlCmd();
    }

    /**
     * 获取分组调光指令builder
     *
     * @return
     */
    public GroupLightBrightCmdBuilder groupLightBrightCmdBuilder() {
        return GroupLightBrightCmdBuilder.anLightControlCmd();
    }
}
