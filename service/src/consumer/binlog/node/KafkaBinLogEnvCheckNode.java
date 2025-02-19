package consumer.binlog.node;

import com.x.assembly.data.watch.client.domain.DataBaseFieldDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author ：patrickkk
 * @version: 1.0$
 * @since ：2021/3/29 12:30 下午
 */
@Component
public class KafkaBinLogEnvCheckNode extends KafkaBinLogSupperNode {

    public static final Logger logger = LoggerFactory.getLogger(KafkaBinLogEnvCheckNode.class);
    @Resource
    private String env;

    @Override
    public boolean doHandler(String topic, Map<String, DataBaseFieldDO> data, String tableName, String eventType, int partition,
                             long offset, String key, Long eventTime) {
        if (this.env.equals(data.get("env").getValue())) {
            return true;
        }
//        logger.info("环境不一致,current env[{}], data env[{}]", this.env, data.get("env").getValue());
        return false;
    }
}
