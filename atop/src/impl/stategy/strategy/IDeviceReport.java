package impl.stategy.strategy;

import com.x.atop.client.domain.api.ApiRequestDO;

/**
 * @author patrickkk
 * @date 2020/5/15
 */
public interface IDeviceReport {

    /**
     * 记录上报
     *
     * @param apiRequestDO
     * @param data
     */
    void report(ApiRequestDO apiRequestDO, String data);
}
