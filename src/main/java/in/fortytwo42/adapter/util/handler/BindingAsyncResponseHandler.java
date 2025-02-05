
package  in.fortytwo42.adapter.util.handler;

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
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

// TODO: Auto-generated Javadoc
/**
 * The Class BindingAsyncResponseHandler.
 */
public class BindingAsyncResponseHandler implements Closeable {

    /** The binding async response handler. */
    private String BINDING_ASYNC_RESPONSE_HANDLER = "<<<<< BindingAsyncResponseHandler";

    private static Logger logger= LogManager.getLogger(BindingAsyncResponseHandler.class);
    
    /** Store TransactionId and AsyncResponse store to provide async repsonse. */
    private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

    /** The cache event listener configuration. */
    private final CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
            .newEventListenerConfiguration(new NonADCacheListener(), EventType.EXPIRED, EventType.EVICTED) 
            .unordered().asynchronous();
    
    /** The async response cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, AsyncResponse> asyncResponseCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(AuthAttemptTO.class, AsyncResponse.class, ResourcePoolsBuilder.heap(1000000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .withService(cacheEventListenerConfiguration)
            .build();
    
    /** The username cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, String> usernameCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(AuthAttemptTO.class, String.class, ResourcePoolsBuilder.heap(1000000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .build();

    /** The activate encryption cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, Boolean> activateEncryptionCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(AuthAttemptTO.class, Boolean.class, ResourcePoolsBuilder.heap(1000000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .build();

    /** The user data cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, ConsumerBindingTO> userDataCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(AuthAttemptTO.class, ConsumerBindingTO.class, ResourcePoolsBuilder.heap(1000000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .build();
    
    /** The async response store. */
    private final Cache<AuthAttemptTO, AsyncResponse> asyncResponseStore = cacheManager.createCache("AsyncResponseCache", asyncResponseCacheConfiguration);

    /** The username store. */
    private final Cache<AuthAttemptTO, String> usernameStore = cacheManager.createCache("usernameCache", usernameCacheConfiguration);

    /** The activate encryption store. */
    private final Cache<AuthAttemptTO, Boolean> activateEncryptionStore = cacheManager.createCache("activateEncryptionCache", activateEncryptionCacheConfiguration);

    /** The user data store. */
    private final Cache<AuthAttemptTO, ConsumerBindingTO> userDataStore = cacheManager.createCache("userDataCache", userDataCacheConfiguration);

    /**
     * Instantiates a new binding async response handler.
     */
    private BindingAsyncResponseHandler() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final BindingAsyncResponseHandler INSTANCE = new BindingAsyncResponseHandler();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of BindingAsyncResponseHandler.
     *
     * @return single instance of BindingAsyncResponseHandler
     */
    public static BindingAsyncResponseHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Store async response reference.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @param asyncResponse the async response
     * @param username the username
     * @param activateEncryption the activate encryption
     */
    public void storeAsyncResponseReference(String transactionId, String applicationId, AsyncResponse asyncResponse, String username, Boolean activateEncryption) {
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " storeAsyncResponseReference : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        asyncResponseStore.put(authAttemptTO, asyncResponse);
        usernameStore.put(authAttemptTO, username);
        activateEncryptionStore.put(authAttemptTO, activateEncryption);
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " storeAsyncResponseReference : end");
    }

    /**
     * Store async response reference.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @param asyncResponse the async response
     * @param consumerBindingTO the consumer binding TO
     * @param activateEncryption the activate encryption
     */
    public void storeAsyncResponseReference(String transactionId, String applicationId, AsyncResponse asyncResponse, ConsumerBindingTO consumerBindingTO, Boolean activateEncryption) {
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " storeAsyncResponseReference : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        asyncResponseStore.put(authAttemptTO, asyncResponse);
        userDataStore.put(authAttemptTO, consumerBindingTO);
        activateEncryptionStore.put(authAttemptTO, activateEncryption);
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " storeAsyncResponseReference : end");
    }
    
    /**
     * Gets the async response.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @return the async response
     */
    public AsyncResponse getAsyncResponse(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " getAsyncResponse : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " getAsyncResponse : end");
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
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " getUsername : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " getUsername : end");
        return usernameStore.get(authAttemptTO);
    }

    /**
     * Gets the consumer binding TO.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @return the consumer binding TO
     */
    public ConsumerBindingTO getConsumerBindingTO(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " getConsumerBindingTO : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " getConsumerBindingTO : end");
        return userDataStore.get(authAttemptTO);
    }

    /**
     * Checks if is activate encryption.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @return the boolean
     */
    public Boolean isActivateEncryption(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " isActivateEncryption : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " isActivateEncryption : end");
        return activateEncryptionStore.get(authAttemptTO);
    }

    /**
     * Removes the request.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     */
    public void removeRequest(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " removeRequest : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        asyncResponseStore.remove(authAttemptTO);
        usernameStore.remove(authAttemptTO);
        activateEncryptionStore.remove(authAttemptTO);
        userDataStore.remove(authAttemptTO);
        logger.log(Level.DEBUG, BINDING_ASYNC_RESPONSE_HANDLER + " removeRequest : end");
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        cacheManager.close();
    }
}
