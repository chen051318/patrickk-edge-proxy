package impl.stategy.strategy.impl;

import lombok.Data;

/**
 * @author : patrickkk
 * @date 2021-07-02
 */
@Data
public class BaseReportDTO {
    /**
     * sn
     */
    private Long sn;
    /**
     * 0:失败 1:成功
     */
    private Integer success;
    /**
     * 消息
     */
    private String message;
}
