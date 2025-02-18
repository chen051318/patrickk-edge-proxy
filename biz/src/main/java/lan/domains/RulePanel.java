package lan.domains;

import com.tuya.luban.biz.domain.base.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class RulePanel extends ToString {

    private String devId;

    private String productId;

    private List<BindInfo> binds;
}
