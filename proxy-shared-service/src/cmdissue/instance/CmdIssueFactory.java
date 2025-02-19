package cmdissue.instance;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author patrickkk  2020/12/18 16:11
 */
@Slf4j
@Service
public class CmdIssueFactory {

    /**
     * 指令发送实例map
     */
    private Map<String, Class<?>> instanceMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        Reflections reflections = new Reflections("com.x.edgegateway.shared.service.cmdissue.instance.category");
        Set<Class<?>> annotatedClasses =
                reflections.getTypesAnnotatedWith(CmdIssueStrategy.class);

        for (Class<?> classObject : annotatedClasses) {
            CmdIssueStrategy strategy = classObject.getAnnotation(CmdIssueStrategy.class);
            String key = strategy.productType() + "_" + strategy.vendorCode();
            instanceMap.put(key, classObject);
        }

        instanceMap = Collections.unmodifiableMap(instanceMap);
        log.info("CmdIssueFactory.instanceMap: {}", JSON.toJSONString(instanceMap));
    }

    public ICmdIssueInstance getInstance(String productType, String vendorCode) {
        ICmdIssueInstance cmdIssueInstance = null;
        try {
            //品类+厂商指令实例
            String key = productType + "_" + vendorCode;
            Class<?> orDefault = instanceMap.getOrDefault(key, null);
            if (orDefault == null) { //品类的指令实例
                key = productType + "_";
                orDefault = instanceMap.getOrDefault(key, null);
            }
            if (orDefault == null) { //通用的指令实例
                key = "common_cmd_issue_";
                orDefault = instanceMap.getOrDefault(key, null);
            }
            cmdIssueInstance = (ICmdIssueInstance) ApplicationHelper.getBean(key);
            if (cmdIssueInstance == null) {//兜底
                cmdIssueInstance = (ICmdIssueInstance) ApplicationHelper.getBean(orDefault);
            }
            return cmdIssueInstance;
        } catch (Exception e) {
            throw new EdgeException("获取指令发送实例异常", e);
        }
    }
}
