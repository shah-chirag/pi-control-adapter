package in.fortytwo42.adapter.facade;

import java.time.Duration;
import java.util.ArrayList;

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

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.HealthCheckServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

public class HealthCheckFacadImpl implements HealthCheckFacadIntf {

    private static Logger logger= LogManager.getLogger(HealthCheckFacadImpl.class);
    private HealthCheckServiceIntf HealthCheckStoreService = ServiceFactory.getHealthCheckService();
    private static final String     HEALTH_CHECK_FACADE_IMPL_LOG = "<<<<< HealthCheckFacadeImpl";
     Cache<String, String> healthStatusStore1=null;
    final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    private final CacheConfiguration<String, ArrayList> responseCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, ArrayList.class, ResourcePoolsBuilder.heap(1000000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .build();
    Cache<String, ArrayList> healthStatusStore  = cacheManager.createCache("responseCache", responseCacheConfiguration);
    ArrayList list=new ArrayList<>();

    public HealthCheckFacadImpl() {
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        /**
         * Instantiates a new instance holder.
         */
        private static final HealthCheckFacadImpl INSTANCE = new HealthCheckFacadImpl();
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of EvidenceFacadeImpl.
     *
     * @return single instance of EvidenceFacadeImpl
     */
    public static HealthCheckFacadImpl getInstance() {
        return HealthCheckFacadImpl.InstanceHolder.INSTANCE;
    }
    @Override
    public String getHealthCheckofConnections() throws AuthException {
        logger.log(Level.DEBUG, HEALTH_CHECK_FACADE_IMPL_LOG + " getHealthCheckofConnections : start");
       String health= HealthCheckStoreService.getHealthCheckofConnections();
        logger.log(Level.DEBUG, HEALTH_CHECK_FACADE_IMPL_LOG + " getHealthCheckofConnections : end");
        return health;
    }
}

