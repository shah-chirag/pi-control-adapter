package in.fortytwo42.adapter.util.handler;

import java.io.Closeable;
import java.time.Duration;

import javax.ws.rs.container.AsyncResponse;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.EventType;

import in.fortytwo42.adapter.transferobj.AuthAttemptTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

// TODO: Auto-generated Javadoc
/**
 * The Class ADAsyncResponseHandler.
 *
 * @author ChiragShah
 */
public class AsyncResponseHandler implements Closeable {
    
    /** The ad sync responce handler. */
    private String AD_SYNC_RESPONCE_HANDLER = "<<<<< ADAsyncResponseHandler";

    private static Logger logger= LogManager.getLogger(AsyncResponseHandler.class);
    
    /** Store TransactionId and AsyncResponse store to provide async repsonse. */
    private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

    /** The cache event listener configuration. */
    private final CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
            .newEventListenerConfiguration(new ADCacheListener(), EventType.EXPIRED, EventType.EVICTED)
            .unordered().asynchronous();

    /** The async response cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, AsyncResponse> asyncResponseCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(AuthAttemptTO.class, AsyncResponse.class, ResourcePoolsBuilder.heap(300000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .withService(cacheEventListenerConfiguration)
            .build();
    
    /** The username cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, String> usernameCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(AuthAttemptTO.class, String.class, ResourcePoolsBuilder.heap(100000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .build();

    /** The async response store. */
    private final Cache<AuthAttemptTO, AsyncResponse> asyncResponseStore = cacheManager.createCache("AsyncResponseCache", asyncResponseCacheConfiguration);

    /** The username store. */
    private final Cache<AuthAttemptTO, String> usernameStore = cacheManager.createCache("usernameCache", usernameCacheConfiguration);

    /**
     * Instantiates a new AD async response handler.
     */
    private AsyncResponseHandler() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final AsyncResponseHandler INSTANCE = new AsyncResponseHandler();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of ADAsyncResponseHandler.
     *
     * @return single instance of ADAsyncResponseHandler
     */
    public static AsyncResponseHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Store async response reference.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @param asyncResponse the async response
     * @param username the username
     */
    public void storeAsyncResponseReference(String transactionId, String applicationId, AsyncResponse asyncResponse, String username) {
        logger.log(Level.DEBUG, AD_SYNC_RESPONCE_HANDLER + " storeAsyncResponseReference : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        asyncResponseStore.put(authAttemptTO, asyncResponse);
        usernameStore.put(authAttemptTO, username);
        logger.log(Level.DEBUG, AD_SYNC_RESPONCE_HANDLER + " storeAsyncResponseReference : end");
    }

    /**
     * Gets the async response.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @return the async response
     */
    public AsyncResponse getAsyncResponse(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, AD_SYNC_RESPONCE_HANDLER + " getAsyncResponse : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, AD_SYNC_RESPONCE_HANDLER + " getAsyncResponse : end");
        return asyncResponseStore.get(authAttemptTO);
    }

    /**
     * Gets the username.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @return the username
     */
    public String getUsername(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, AD_SYNC_RESPONCE_HANDLER + " getUsername : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, AD_SYNC_RESPONCE_HANDLER + " getUsername : end");
        return usernameStore.get(authAttemptTO);
    }

    /**
     * Removes the request.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     */
    public void removeRequest(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, AD_SYNC_RESPONCE_HANDLER + " removeRequest : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        asyncResponseStore.remove(authAttemptTO);
        usernameStore.remove(authAttemptTO);
        logger.log(Level.DEBUG, AD_SYNC_RESPONCE_HANDLER + " removeRequest : end");
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        cacheManager.close();
    }
}
