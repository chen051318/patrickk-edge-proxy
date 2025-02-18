package consumer.binlog.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：patrickkk
 * @version: 1.0$
 * @since ：2021/3/29 11:32 上午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface KafkaBinLogTable {

    /**
     * 执行顺序，数值越小执行优先级越高(业务节点从1开始计数)
     * @return
     */
    int sort() default -1;

    /**
     * 处理的表名
     */
    String tableName();

    /**
     * 监听事件event(包括INSERT/UPDATE/DELETE/SELECT)
     */
    String[] event();

    /**
     * 监听字段(在更新条件下，若监听字段的值更新了，则触发相应方法；反之，若监听字段为更新，则直接跳过)
     */
    String[] fields() default "";


}
