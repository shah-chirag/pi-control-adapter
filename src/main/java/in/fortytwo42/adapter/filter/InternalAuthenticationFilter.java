package in.fortytwo42.adapter.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.handler.PasswordHashHandler;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.enterprise.extension.utils.RandomString;
import in.fortytwo42.entities.bean.Application;

// TODO: Auto-generated Javadoc
/**
 * The Class InternalAuthenticationFilter.
 */
@InternalSecure
@Provider
@Priority(Priorities.AUTHORIZATION)
public class InternalAuthenticationFilter implements ContainerRequestFilter {

    /** The internal authentication filter. */
    private String INTERNAL_AUTHENTICATION_FILTER = "<<<<< InternalAuthenticationFilter";
	private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

	private static Logger logger= LogManager.getLogger(InternalAuthenticationFilter.class);
    
	/** The resource info. */
	@Context
	private ResourceInfo resourceInfo;

	/**
	 * Filter.
	 *
	 * @param requestContext the request context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.log(Level.DEBUG, INTERNAL_AUTHENTICATION_FILTER + " filter : start");
		String applicationId = requestContext.getHeaderString(Constant.HEADER_APPLICATION_ID);
		String applicationSecret = requestContext.getHeaderString(Constant.HEADER_APPLICATION_SECRET);
		if (applicationId == null || applicationId.trim().isEmpty() || applicationSecret == null || applicationSecret.trim().isEmpty()) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_CLIENT_ID(), Constant.USER_IS_NOT_AUTHORIZED_TO_ACCESS_RESOURCE, errorConstant.getERROR_MESSAGE_USER_PROVIDED_INVALID_CLIENT_ID_OR_SECRET())).build());
		} else {
			try {
				Application application = DaoFactory.getApplicationDao().getApplicationByApplicationId(applicationId);
				boolean isPlainTextPasswordAllowed = application.getIsPlaintextPasswordAllowed() != null ? application.getIsPlaintextPasswordAllowed().booleanValue() : false;
				if (application.getAuthenticationRequired() && !isApplicationSecretValid(application.getApplicationSecret(), applicationSecret, isPlainTextPasswordAllowed, applicationId)) {
					requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
							.entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_CLIENT_ID(), Constant.USER_IS_NOT_AUTHORIZED_TO_ACCESS_RESOURCE, errorConstant.getERROR_MESSAGE_USER_PROVIDED_INVALID_CLIENT_ID_OR_SECRET()))
							.build());
				}
				requestContext.getHeaders().add(Constant.HEADER_APPLICATION_NAME, application.getApplicationName());
			} catch (Exception e) {
				logger.log(Level.ERROR, e);
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
						.entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_CLIENT_ID(), Constant.USER_IS_NOT_AUTHORIZED_TO_ACCESS_RESOURCE, errorConstant.getERROR_MESSAGE_USER_PROVIDED_INVALID_CLIENT_ID_OR_SECRET())).build());
			}
		}
        logger.log(Level.DEBUG, INTERNAL_AUTHENTICATION_FILTER + " filter : end");
	}
	
	/**
	 * Checks if is application secret valid.
	 *
	 * @param applicationSecret the application secret
	 * @param receivedApplicationSecret the received application secret
	 * @return true, if is application secret valid
	 */
	private boolean isApplicationSecretValid(String applicationSecret, String receivedApplicationSecret, Boolean isPlaintextPasswordAllowed, String applicationId) {
        logger.log(Level.DEBUG, INTERNAL_AUTHENTICATION_FILTER + " isApplicationSecretValid : start");
        if (isPlaintextPasswordAllowed != null && isPlaintextPasswordAllowed) {
            String decryptedSecret = AES128Impl.decryptData(applicationSecret, KeyManagementUtil.getAESKey());
            logger.log(Level.DEBUG, "receivedApplicationSecret : " + receivedApplicationSecret + " decryptedSecret : " + decryptedSecret);
            if(receivedApplicationSecret.equals(decryptedSecret)) {
                return true;
            }
        }
		int saltSize;
		try {
			saltSize = Integer.parseInt(Config.getInstance().getProperty(Constant.SALT_SIZE));
		} catch (NumberFormatException e) {
			saltSize = 20;
		}
		if(receivedApplicationSecret.length() > saltSize) {
			String salt = receivedApplicationSecret.substring(0, saltSize);
			String hashedSecret = receivedApplicationSecret.substring(saltSize);
			String decryptedSecret = AES128Impl.decryptData(applicationSecret, KeyManagementUtil.getAESKey());
			logger.log(Level.DEBUG, "salt : " + salt + " decryptedSecret :" + decryptedSecret);
			String generatedHash = SHAImpl.hashData256(decryptedSecret + salt);
			logger.log(Level.DEBUG, "hashedSecret : " + hashedSecret + " generatedHash :" + generatedHash);
			boolean isSecreteValid = hashedSecret.equals(generatedHash);
			logger.log(Level.DEBUG, "isSecreteValid >>" + isSecreteValid);
			if (isSecreteValid) {
			    isSecreteValid = this.isApplicationSecreteValid(applicationId, receivedApplicationSecret);
			    System.out.println(" isSecreteValid isSecreteValid : "+isSecreteValid);
			    logger.log(Level.DEBUG, "isSecreteValid  ::" + isSecreteValid);
			}
			logger.log(Level.DEBUG, INTERNAL_AUTHENTICATION_FILTER + " isApplicationSecretValid : end");
			return isSecreteValid;
		}
        logger.log(Level.DEBUG, INTERNAL_AUTHENTICATION_FILTER + " isApplicationSecretValid : end");
		return false;
	}
	
	private boolean isApplicationSecreteValid(String applicationId, String applicationSecret) {
        return PasswordHashHandler.getInstance().isApplicationSecreteValid(applicationId, applicationSecret);
    }

	public static void main(String[] args) {
	    String appliationSecrete = AES128Impl.encryptData("nonad@iam", KeyManagementUtil.getAESKey());
	    String secrete = InternalAuthenticationFilter.generateHash("nonad@ia");
	    System.out.println("secrete : "+secrete);
	    boolean isValid = new InternalAuthenticationFilter().isApplicationSecretValid(appliationSecrete, secrete, null, "abcd");
        System.out.println("isValid : "+isValid);
	}
	
	public static String generateHash(String applicationSecretePlainText) {
        String salt = RandomString.nextString(20);
        String generatedHash = SHAImpl.hashData256(String.valueOf(applicationSecretePlainText) + salt);
        String applicationSecrete = String.valueOf(salt) + generatedHash;
	    return applicationSecrete;
	}
}
