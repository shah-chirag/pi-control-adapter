
package in.fortytwo42.adapter.util.handler;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import in.fortytwo42.adapter.transferobj.ApplicationSecreteTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

public class PasswordHashHandler implements Closeable {
    private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

    private static Logger logger= LogManager.getLogger(PasswordHashHandler.class);

    private final CacheConfiguration<ApplicationSecreteTO, Long> passwordHashCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(ApplicationSecreteTO.class, Long.class, ResourcePoolsBuilder.heap(100000))
            .withEvictionAdvisor((key, value) -> (System.currentTimeMillis() - value) >= (Long.valueOf(Config.getInstance().getProperty(Constant.APPLICATION_SECRETE_VALIDITY)) * 1000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Long.valueOf(Config.getInstance().getProperty(Constant.APPLICATION_SECRETE_VALIDITY)))))
            .build();

    private final Cache<ApplicationSecreteTO, Long> passwordCashStore = cacheManager.createCache("PasswordHashCache", passwordHashCacheConfiguration);

    private PasswordHashHandler() {
        super();
    }

    private static final class InstanceHolder {
        private static final PasswordHashHandler INSTANCE = new PasswordHashHandler();

        private InstanceHolder() {
            super();
        }
    }

    public static PasswordHashHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void storePasswordHash(String applicationId, String applicationSecret) {
        ApplicationSecreteTO applicationSecreteTO = new ApplicationSecreteTO(applicationId, applicationSecret);
        passwordCashStore.put(applicationSecreteTO, System.currentTimeMillis());
    }

    public void storePasswordHash(ApplicationSecreteTO applicationSecreteTO) {
        passwordCashStore.put(applicationSecreteTO, System.currentTimeMillis());
    }

    public boolean isApplicationSecreteValid(String applicationId, String applicationSecret) {
        ApplicationSecreteTO applicationSecreteTO = new ApplicationSecreteTO(applicationId, applicationSecret);
        boolean isSecretPresent = passwordCashStore.containsKey(applicationSecreteTO);
        logger.log(Level.DEBUG, "isSecretePresent  :::" + isSecretPresent);
        if (!isSecretPresent) {
            passwordCashStore.put(applicationSecreteTO, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public boolean isApplicationSecreteValid(String applicationId, String applicationSecret, Long currentTime) {
        ApplicationSecreteTO applicationSecreteTO = new ApplicationSecreteTO(applicationId, applicationSecret);
        if (passwordCashStore.containsKey(applicationSecreteTO)) {
            Long time = passwordCashStore.get(applicationSecreteTO);
            if ((currentTime - time) >= Long.valueOf(Config.getInstance().getProperty(Constant.APPLICATION_SECRETE_VALIDITY))) {
                passwordCashStore.remove(applicationSecreteTO);
                storePasswordHash(applicationSecreteTO);
                return true;
            }
            return false;
        }
        storePasswordHash(applicationSecreteTO);
        return true;
    }

    @Override
    public void close() throws IOException {
        cacheManager.close();
    }

}
