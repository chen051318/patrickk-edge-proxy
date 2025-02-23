package cmdissue.instance.category.webcam;

import lombok.Getter;
import lombok.Setter;

/**
 * @author : patrickkk
 * @date 2021-06-25
 */
@Getter
@Setter
public class ProtocolDTO<T> extends BaseBean {
    /**
     * 设备cid
     */
    private String cid;
    /**
     * 请求类型
     */
    private String reqType;
    /**
     * ai能力
     */
    private String aiSkill;
    /**
     * 操作 功能名称
     */
    private String function;
    /**
     * 请求ID
     */
    private String reqId;
    /**
     * 版本
     */
    private String v;
    /**
     * 数据内容
     */
    private T data;
}
