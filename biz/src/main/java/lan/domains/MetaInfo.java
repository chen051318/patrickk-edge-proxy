package lan.domains;

import com.x.luban.biz.domain.base.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * 元数据
 *
 * @Author patrickkk
 * @Date 2020-07-04
 */
@Getter
@Setter
public class MetaInfo extends ToString {
    /**
     * 名称
     */
    private String name;
    /**
     * 房间
     */
    private String room;
    /**
     * mac地址
     */
    private String mac;
}
