
package lan.executor;

import com.x.luban.biz.common.executor.IExecutor;
import com.x.luban.biz.service.lan.domains.LanRecoverContext;
import com.x.luban.biz.service.lan.domains.LinkageRule;

/**
 * 无网场景恢复
 * 
 * @author : patrickkk
 * @date 2022-08-19
 */
public interface ILanRuleExecutor extends IExecutor {

    /**
     * 创建联动
     * 
     * @param context
     * @param rule
     */
    void createRule(LanRecoverContext context, LinkageRule rule);

}
