package cmdissue.model;

import com.tuya.edgegateway.client.common.BaseBean;
import lombok.Getter;
import lombok.Setter;

/**
 * @author patrickkk
 * @date 2020/6/17
 */
@Getter
@Setter
public class TalkExtVO extends BaseBean {

    /**
     * 开始通话时间
     */
    private long beginTime;

    /**
     * 通话结束时间
     */
    private long endTime;

}
