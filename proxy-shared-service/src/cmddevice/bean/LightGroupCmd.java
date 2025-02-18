package cmddevice.bean;

import cmddevice.bean.CmdSupport;
import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author patrickkk  2020/8/26 17:01
 * 路灯分组 dp=5
 * cmd2.0
 */
@Getter
@Setter
public class LightGroupCmd extends CmdSupport {

    /**
     * 分组号
     */
    private Integer groupNo;

    @Override
    public Integer getDpid() {
        return 5;
    }

    @Override
    public Boolean getGateway() {
        return false;
    }

    @Override
    public String getCmdVersion() {
        return "2.0";
    }

    @Override
    public Object getData() {
        return groupNo;
    }

    @Override
    public String toJsonDP() {
        Map<Integer, Integer> encodeStrMap = new HashMap<>();
        encodeStrMap.put(this.getDpid(), groupNo);
        return JSON.toJSONString(encodeStrMap);
    }

}
