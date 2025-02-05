
package in.fortytwo42.adapter.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.daos.dao.InfinispanConnectionManager;

public class InfinispanUtil {
    private static final int DEFAULT_INFINISPAN_REC_LIFETIME=1800;
    
    private static final String INFINISPAN_UTIL_LOG = "<<<<< InfinispanUtil";

    private static Logger logger= LogManager.getLogger(InfinispanUtil.class);

    private InfinispanUtil() {
        Config config = Config.getInstance();
        InfinispanConnectionManager.ConnectionManagerBuilder builder = InfinispanConnectionManager.ConnectionManagerBuilder.getInstance();
        boolean isInfinispnEnabled = Boolean.parseBoolean(config.getProperty(Constant.INFINISPAN_ENABLED));
        if (isInfinispnEnabled) {
            builder.setHost(config.getProperty(Constant.INFINISPAN_HOST));
            builder.setPort(Integer.parseInt(config.getProperty(Constant.INFINISPAN_PORT)));
            builder.setUsername(config.getProperty(Constant.INFINISPAN_USERNAME));
            builder.setPassword(config.getProperty(Constant.INFINISPAN_PASSWORD));
            builder.setInfinispanCronExpression(config.getProperty(Constant.INFINISPAN_CRON_EXPRESSION));
            builder.setKeepAlive(Boolean.parseBoolean(config.getProperty(Constant.INFINISPAN_KEEP_ALIVE)));
            builder.setSocketTimeout(Integer.parseInt(config.getProperty(Constant.INFINISPAN_SOCKET_TIMEOUT)));
            builder.setConnectionTimeout(Integer.parseInt(config.getProperty(Constant.INFINISPAN_CONNECTION_TIMEOUT)));
            builder.setRetryAttempts(Integer.parseInt(config.getProperty(Constant.INFINISPAN_MAX_RETRIES)));
            try{
                builder.setInfinispanRecLifetime(Integer.parseInt(config.getProperty(Constant.INFINISPAN_REC_LIFETIME)));
            }catch (Exception e){
                logger.log(Level.DEBUG, "Failed to parse "+Constant.INFINISPAN_REC_LIFETIME+" from config using default value: " + DEFAULT_INFINISPAN_REC_LIFETIME);
                builder.setInfinispanRecLifetime(DEFAULT_INFINISPAN_REC_LIFETIME);
            }
        }
        builder.setInfinispanEnabled(isInfinispnEnabled);
        logger.log(Level.DEBUG, INFINISPAN_UTIL_LOG + ": loaded ConnectionManagerBuilder");
    }

    private static final class InstanceHolder {
        private static final InfinispanUtil INSTANCE = new InfinispanUtil();

        private InstanceHolder() {
            super();
        }
    }

    public static InfinispanUtil getInstance() {
        return InfinispanUtil.InstanceHolder.INSTANCE;
    }

    public static void clearCache(){
        Config config = Config.getInstance();
        boolean isInfinispnEnabled = Boolean.parseBoolean(config.getProperty(Constant.INFINISPAN_ENABLED));
        if (isInfinispnEnabled) {
            InfinispanConnectionManager obj= InfinispanConnectionManager.getInstance();
            obj.clearCaches();
        }
    }
}
