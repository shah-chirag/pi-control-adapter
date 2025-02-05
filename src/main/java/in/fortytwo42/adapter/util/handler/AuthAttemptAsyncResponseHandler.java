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
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import in.fortytwo42.adapter.transferobj.AuthAttemptTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

// TODO: Auto-generated Javadoc
/**
 * The Class AuthAttemptAsyncResponseHandler.
 */
public class AuthAttemptAsyncResponseHandler implements Closeable {
    
    /** The auth attempt async responce handler. */
    private String AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER = "<<<<< AuthAttemptAsyncResponseHandler";

    private static Logger logger= LogManager.getLogger(AuthAttemptAsyncResponseHandler.class);
    
	/** Store TransactionId and AsyncResponse store to provide async repsonse. */
    private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

    /** The async response cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, AsyncResponse> asyncResponseCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(AuthAttemptTO.class, AsyncResponse.class, ResourcePoolsBuilder.heap(100000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .build();
    
    /** The username cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, String> usernameCacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(AuthAttemptTO.class, String.class, ResourcePoolsBuilder.heap(1000000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .build();

    /** The auth cache configuration. */
    private final CacheConfiguration<AuthAttemptTO, AuthenticationAttemptTO> authCacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(AuthAttemptTO.class, AuthenticationAttemptTO.class, ResourcePoolsBuilder.heap(1000000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT)) + 2)))
            .build();

    /** The async response store. */
    private final Cache<AuthAttemptTO, AsyncResponse> asyncResponseStore = cacheManager.createCache("AsyncResponseCache", asyncResponseCacheConfiguration);

    /** The username store. */
    private final Cache<AuthAttemptTO, String> usernameStore = cacheManager.createCache("usernameCache", usernameCacheConfiguration);

    /** The auth store. */
    private final Cache<AuthAttemptTO, AuthenticationAttemptTO> authStore = cacheManager.createCache("authCache", authCacheConfiguration);

    /**
     * Instantiates a new auth attempt async response handler.
     */
    private AuthAttemptAsyncResponseHandler() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final AuthAttemptAsyncResponseHandler INSTANCE = new AuthAttemptAsyncResponseHandler();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of AuthAttemptAsyncResponseHandler.
     *
     * @return single instance of AuthAttemptAsyncResponseHandler
     */
    public static AuthAttemptAsyncResponseHandler getInstance() {
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
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " storeAsyncResponseReference : start");
    	AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        asyncResponseStore.put(authAttemptTO, asyncResponse);
        usernameStore.put(authAttemptTO, username);
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " storeAsyncResponseReference : end");
    }

    /**
     * Store async response reference.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @param asyncResponse the async response
     * @param authenticationAttemptTO the authentication attempt TO
     */
    public void storeAsyncResponseReference(String transactionId, String applicationId, AsyncResponse asyncResponse, AuthenticationAttemptTO authenticationAttemptTO) {
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " storeAsyncResponseReference : start");
        AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        asyncResponseStore.put(authAttemptTO, asyncResponse);
        authStore.put(authAttemptTO, authenticationAttemptTO);
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " storeAsyncResponseReference : end");
    }
    
    /**
     * Gets the async response.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @return the async response
     */
    public AsyncResponse getAsyncResponse(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " getAsyncResponse : start");
    	AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " getAsyncResponse : end");
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
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " getUsername : start");
    	AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " getUsername : end");
        return usernameStore.get(authAttemptTO);
    }

    /**
     * Gets the auth attempt TO.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @return the auth attempt TO
     */
    public AuthenticationAttemptTO getAuthAttemptTO(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " getAuthAttemptTO : start");
    	AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " getAuthAttemptTO : end");
    	return authStore.get(authAttemptTO);
	}
    
    /**
     * Removes the request.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     */
    public void removeRequest(String transactionId, String applicationId) {
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " removeRequest : start");
    	AuthAttemptTO authAttemptTO = new AuthAttemptTO(applicationId, transactionId);
        asyncResponseStore.remove(authAttemptTO);
        usernameStore.remove(authAttemptTO);
        authStore.remove(authAttemptTO);
        logger.log(Level.DEBUG, AUTH_ATTEMPT_ASYNC_RESPONCE_HANDLER + " removeRequest : end");
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        cacheManager.close();
    }
}
