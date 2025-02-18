package impl.stategy.strategy;

import com.alibaba.fastjson.JSON;
import com.tuya.edgegateway.atop.device.impl.strategy.annotation.EdgeStrategy;
import com.tuya.edgegateway.manager.base.configuration.ApplicationHelper;
import com.tuya.edgegateway.manager.base.exception.EdgeException;
import com.tuya.edgegateway.manager.base.exception.EdgeExceptionCode;
import com.tuya.edgegateway.manager.base.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author patrickkk
 * @date 2020/5/15
 */
@Slf4j
@Service
public class StrategyFactoryContext{

    /**
     * 所有策略
     */
    private static Map<String, Class<?>> allStrategies;

    @PostConstruct
    private void init() {
        Reflections reflections = new Reflections("com.tuya.edgegateway.atop.device.impl.strategy.impl");
        Set<Class<?>> annotatedClasses =
                reflections.getTypesAnnotatedWith(EdgeStrategy.class);
        allStrategies = new ConcurrentHashMap<>(16);
        for (Class<?> classObject : annotatedClasses) {
            EdgeStrategy strategy = classObject.getAnnotation(EdgeStrategy.class);
            allStrategies.put(strategy.tag(), classObject);
        }
        allStrategies = Collections.unmodifiableMap(allStrategies);
        log.info("StrategyFactoryContext.allStrategies: {}", JSON.toJSONString(allStrategies));
    }

    public IDeviceReport getInstance(String name) {
        IDeviceReport deviceReport = null;
        try {
            Class<?> orDefault = allStrategies.getOrDefault(name, null);
            log.info("StrategyFactoryContext.allStrategies: {}", JSON.toJSONString(allStrategies));
            if (orDefault != null) {
                deviceReport = (IDeviceReport) ApplicationHelper.getBean(orDefault);
            } else {
                log.error("StrategyFactoryContext.getInstance.error: {} ,params: {}", JSON.toJSONString(allStrategies), name);
                throw new EdgeException(EdgeExceptionCode.PROCESS_TAG_NOTEXIST);
            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getExceptionStack(e));
        }
        return deviceReport;
    }
}
