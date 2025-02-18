package cmdissue.instance;

import java.lang.annotation.*;

/**
 * 指令发送实例注解
 *
 * @author patrickkk  2020/12/18 15:58
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CmdIssueStrategy {
    /**
     * 产品品类
     *
     * @return
     */
    String productType() default "";

    /**
     * 供应商code
     *
     * @return
     */
    String vendorCode() default "";
}