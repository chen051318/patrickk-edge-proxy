package cmddevice.builders;

import cmddevice.bean.AbstractCmdBuilder;
import cmddevice.bean.LightGroupCmd;
import com.tuya.edgegateway.client.domain.ndp.common.AbstractCmdBuilder;
import org.springframework.util.Assert;

/**
 * @author patrickkk  2020/8/26 17:01
 */
public class LightGroupCmdBuilder extends AbstractCmdBuilder<LightGroupCmd, LightGroupCmdBuilder> {

    /**
     * 分组编号
     */
    private Integer groupNo;

    private LightGroupCmdBuilder() {
    }

    public static LightGroupCmdBuilder anLightGroupCmd() {
        return new LightGroupCmdBuilder();
    }

    public LightGroupCmdBuilder withGroupNo(Integer groupNo) {
        this.groupNo = groupNo;
        return this;
    }

    @Override
    protected Class<LightGroupCmd> cmdClass() {
        return LightGroupCmd.class;
    }

    @Override
    protected void addFields(LightGroupCmd cmd) {
        Assert.notNull(groupNo, "组号字段不能为空！");
        cmd.setGroupNo(groupNo);
    }
}
