
package lan.domains.recover;

import com.alibaba.fastjson.JSON;
import com.x.luban.biz.util.I18nUtils;
import com.x.luban.core.dao.domains.recover.GatewayRecoverErrorDO;
import lombok.Data;
import org.apache.commons.lang.text.StrSubstitutor;

import java.io.Serializable;
import java.util.Map;

/**
 * 恢复失败元英查询
 * 
 * @author : patrickkk
 * @date 2021-11-12
 */
@Data
public class RecoverErrorDTO implements Serializable {

    /**
     * 错误类型
     */
    private String errorType;

    /**
     * 错误信息
     */
    private String errorMessage;

    public void toErrorMessage(GatewayRecoverErrorDO errorDO, String lang) {
        String failParams = errorDO.getFailParams();
        Map params = JSON.parseObject(failParams, Map.class);
        String failLangCode = errorDO.getFailLangCode();
        String content = I18nUtils.getI18nString(failLangCode, lang);
        StrSubstitutor strSubstitutor = new StrSubstitutor(params);
        errorMessage = strSubstitutor.replace(content);
    }
}
