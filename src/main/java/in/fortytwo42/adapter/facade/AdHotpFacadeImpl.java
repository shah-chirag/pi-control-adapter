package in.fortytwo42.adapter.facade;

import java.io.IOException;
import javax.ws.rs.ProcessingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.fortytwo42.adapter.service.LDAPDetailsServiceIntf;
import in.fortytwo42.daos.dao.UserApplicationRelDaoImpl;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.itzmeds.adfs.client.SignOnException;

import in.fortytwo42.adapter.controller.AccountOnboarder;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ADSyncServiceIntf;
import in.fortytwo42.adapter.service.AdHotpServiceIntf;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceImpl;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AdfsUtil;
import in.fortytwo42.adapter.util.AttributeValidationUtil;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.RSAUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.exceptions.ValidationException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.ApplicationType;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.integration.exception.ActiveMQConectionException;
import in.fortytwo42.tos.transferobj.AdHotpTO;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;

public class AdHotpFacadeImpl implements AdHotpFacadeIntf {

    private final UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

	private final UserServiceIntf userService = ServiceFactory.getUserService();

	private final IamExtensionServiceIntf iamExtension = ServiceFactory.getIamExtensionService();

	private final ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();

	private final AdHotpServiceIntf adHotpService = ServiceFactory.getAdHotpService();

	private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

	private final AdfsUtil adfsUtil = AdfsUtil.getInstance();

	private final IAMUtil iamUtil = IAMUtil.getInstance();

	private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

	private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

	private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

	private final ADSyncServiceIntf adSyncService = ServiceFactory.getADSyncService();

	private final UserApplicationRelDaoIntf userApplicationDao = DaoFactory.getUserApplicationRel();

	private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

	private static Logger logger = LogManager.getLogger(AdHotpFacadeImpl.class);

	private static final String ADHOTP_FACADE_IMPL_LOG = "<<<<< ADHOTPFacadeImpl";

	private LDAPDetailsServiceIntf ldapDetailsServiceIntf = ServiceFactory.getLdapDetailsService();

	private final ExecutorService pool;
	private int retry = 1;

	private AdHotpFacadeImpl() {
		super();
		int poolSize = Constant.POOL_SIZE;
		try {
			poolSize = Integer.parseInt(Config.getInstance().getProperty(Constant.RESOURCE_LOG_THREAD_POOL_SIZE));
		} catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage(), e);
		}
		pool = Executors.newFixedThreadPool(poolSize);

		try {
			retry = Integer.parseInt(Config.getInstance().getProperty(Constant.ADFS_SEND_OTP_RETRY));
		} catch (Exception e) {
			logger.log(Level.WARN, "ADFS retry attempt is not configured");
			logger.log(Level.ERROR, e.getMessage(), e);
		}
	}

	private static final class InstanceHolder {

		/** The Constant INSTANCE. */
		private static final AdHotpFacadeImpl INSTANCE = new AdHotpFacadeImpl();

		/**
		 * Instantiates a new instance holder.
		 */
		private InstanceHolder() {
			super();
		}
	}

	public static AdHotpFacadeImpl getInstance() {
		return AdHotpFacadeImpl.InstanceHolder.INSTANCE;
	}

	public AdHotpTO generateAdOtp(AdHotpTO adHotpTO, String applicationId, String service) throws AuthException, AttributeNotFoundException {

		logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " generateAdHotp : start");
		if(service == null || service.isEmpty()){
			String serviceName = Config.getInstance().getProperty(Constant.DEFAULT_SERVICE_NAME);
			service = (serviceName == null || serviceName.isEmpty()) ? Constant.APPROVAL : serviceName;
		}
		Application application = applicationService.getApplicationByApplicationId(applicationId);
		String searchAttributeValue = null;
		String attributeName = null;
		AdfsDetailsTO adfsDetailsTO = null;
		boolean isPasswordSent = adHotpTO.getSearchAttributes() != null && adHotpTO.getPassword() != null;
		boolean isEmailAndMobileSentInRequestBody = adHotpTO.getSearchAttributes() != null && adHotpTO.getAttributeData() != null;
		if (isPasswordSent) {
			attributeName = adHotpTO.getSearchAttributes().get(0).getAttributeName();
			searchAttributeValue = adHotpTO.getSearchAttributes().get(0).getAttributeValue();
			String decryptedPassword = decryptPassword(adHotpTO);
			adfsDetailsTO = authenticateUserWithAdfsOrAD(application, searchAttributeValue, adfsDetailsTO, decryptedPassword);
			logger.log(Level.DEBUG, "ADFS/AD authentication sucess");
		} else if (isEmailAndMobileSentInRequestBody) {
			adfsDetailsTO = handleEmailAndMobileInRequestData(adHotpTO, application);
			attributeName = adHotpTO.getSearchAttributes().get(0).getAttributeName();
			searchAttributeValue = adHotpTO.getSearchAttributes().get(0).getAttributeValue();
		} else {
			throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_INVALID_USERID_PASSWORD(), errorConstant.getERROR_MESSAGE_INVALID_USERID_PASSWORD());
		}
		String accountId = getAccountId(adHotpTO);
		Session dbSession = sessionFactoryUtil.getSession();
		try {
		    adfsDetailsTO.setMobile(adfsDetailsTO.getMobile() != null ? adfsDetailsTO.getMobile().replaceAll("\\s", ""):adfsDetailsTO.getMobile());
			AdHotpTO optResponse = null;

			if (accountId == null) {
				optResponse = onboardOnIDS(adHotpTO, applicationId, service, application, searchAttributeValue, attributeName, adfsDetailsTO, dbSession, optResponse);
			} else {
				optResponse = updateMobileAndEmailOnIDS(adHotpTO, service, application, searchAttributeValue, adfsDetailsTO, accountId, dbSession, optResponse);
			}
			sessionFactoryUtil.closeSession(dbSession);
			optResponse.setPassword(null);
			AuditLogUtil.sendAuditLog("generateAdOtp successfully ", "ENTERPRISE", ActionType.AUTHENTICATE_ONLINE, "",
					IdType.ACCOUNT, "", "", "", null);

			return optResponse;
		} catch (AuthException e) {
			dbSession.getTransaction().rollback();
			throw e;
		} catch (ActiveMQConectionException e) {
			dbSession.getTransaction().rollback();
			throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_INVALID_CONNECTION_SETTINGS(),
					errorConstant.getERROR_MESSAGE_INVALID_CONNECTION_SETTINGS());
		}finally {
			if (dbSession.isOpen()) {
				dbSession.close();
			}
			logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " generateAdHotp : end");
		}
	}

    private AdHotpTO updateMobileAndEmailOnIDS(AdHotpTO adHotpTO, String service, Application application, String searchAttributeValue, AdfsDetailsTO adfsDetailsTO, String accountId,
            Session dbSession, AdHotpTO optResponse) throws AuthException, ActiveMQConectionException {
        // send sms/email
        if (adHotpTO.getNotification() != null) {
			optResponse = adHotpService.generateHotp(adHotpTO, dbSession, application.getApplicationId(),application.getApplicationName(), searchAttributeValue, adfsDetailsTO, application.getAttemptCount());
        } else {
        	throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_NOTIFICATION_TYPE_NOT_FOUND(),
					errorConstant.getERROR_MESSAGE_NOTIFICATION_TYPE_NOT_FOUND());
        }
        // update attributes
        String finalSearchAttributeValue = searchAttributeValue;
        AdfsDetailsTO finalAdfsDetailsTO1 = adfsDetailsTO;
		String reqRefNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
			ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
        	updateAttributes(accountId, finalAdfsDetailsTO1, application, service, finalSearchAttributeValue);
        });
        return optResponse;
    }

    private AdHotpTO onboardOnIDS(AdHotpTO adHotpTO, String applicationId, String service, Application application, String searchAttributeValue, String attributeName, AdfsDetailsTO adfsDetailsTO,
            Session dbSession, AdHotpTO optResponse) throws AuthException, ActiveMQConectionException {
		IAMExtensionV2 iamExtensionV2 = iamExtension.getIAMExtensionWithoutCrypto();
        //String newAccountId = iamExtension.createAccountIfNotExist(attributeName, searchAttributeValue, iamExtensionV2);
		//String newAccountId = createAccountwithAccountOnboarder(adfsDetailsTO, searchAttributeValue, iamExtensionV2);

        // send sms/email
        if (adHotpTO.getNotification() != null) {
			optResponse = adHotpService.generateHotp(adHotpTO, dbSession, application.getApplicationId(),application.getApplicationName(), searchAttributeValue, adfsDetailsTO, application.getAttemptCount());
        } else {
        	throw new AuthException(new Throwable(),errorConstant.getERROR_CODE_NOTIFICATION_TYPE_NOT_FOUND(),
					errorConstant.getERROR_MESSAGE_NOTIFICATION_TYPE_NOT_FOUND());
        }

        // onboard user
        String finalSearchAttributeValue = searchAttributeValue;
        AdfsDetailsTO finalAdfsDetailsTO = adfsDetailsTO;
		String reqRefNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
			ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
        	//onboardAdUser(applicationId, service, finalSearchAttributeValue, finalAdfsDetailsTO);
	        IAMUtil.getInstance().onboardUserApi(application, service, finalSearchAttributeValue, finalAdfsDetailsTO);
        });
        return optResponse;
    }

	private String createAccountwithAccountOnboarder(AdfsDetailsTO finalAdfsDetailsTO, String finalSearchAttributeValue, IAMExtensionV2 iamExtension) throws AuthException{
		try{
			Token token = iamExtensionService.getTokenWithoutCrypto(iamExtension);
			UserIciciTO userTO = new UserIciciTO();
			List<AttributeDataTO> searchAttributeList = new ArrayList<>();
			if(finalSearchAttributeValue!=null){
				AttributeDataTO attributeDataTO = new AttributeDataTO();
				attributeDataTO.setAttributeName(Constant.USER_ID);
				attributeDataTO.setAttributeValue(finalSearchAttributeValue);
				searchAttributeList.add(attributeDataTO);
			}
			if(finalAdfsDetailsTO.getMobile()!=null){
				AttributeDataTO attributeDataTO = new AttributeDataTO();
				attributeDataTO.setAttributeName(Constant.MOBILE_NO);
				attributeDataTO.setAttributeValue(finalAdfsDetailsTO.getMobile());
				searchAttributeList.add(attributeDataTO);
			}
			userTO.setSearchAttributes(searchAttributeList);

			List<AttributeDataTO> attributeDataList = new ArrayList<>();
			if(finalAdfsDetailsTO.getEmail()!=null){
				AttributeDataTO attributeDataTO = new AttributeDataTO();
				attributeDataTO.setAttributeName(Constant.EMAIL_ID);
				attributeDataTO.setAttributeValue(finalAdfsDetailsTO.getEmail());
				attributeDataList.add(attributeDataTO);
			}
			userTO.setAttributeData(attributeDataList);

			List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
			List<AttributeTO> attributeTOs = new ArrayList<>();
			for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
				if(attributeDataTO.getIsDefault() == null) {
					attributeDataTO.setIsDefault(true);
				}
            /*if(attributeDataTO.getIsRegistered() == null) {
                attributeDataTO.setIsRegistered(true);
            }*/
			attributeTOs.add(AccountOnboarder.getInstance().getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs));

			}
			if(userTO.getAttributeData() != null) {
				for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
					attributeTOs.add(AccountOnboarder.getInstance().getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs));
				}
			}
			AccountType accountType= AccountType.USER;
			AccountWE accountWE = iamExtension.createAccountWithAllAttributes(attributeTOs, userTO.getUserCredential(), accountType, token,null);
			return accountWE.getId();
		} catch (ValidationException e){
			logger.log(Level.DEBUG,e.getMessage(),e);
		}
		catch(Exception e){
			logger.log(Level.ERROR,e.getMessage(),e);
			throw new AuthException(e,errorConstant.getERROR_CODE_SERVER_ERROR(),errorConstant.getERROR_MESSAGE_SERVER_ERROR());
		}
		return null;
	}

	private AdfsDetailsTO authenticateUserWithAdfsOrAD(Application application, String searchAttributeValue,
			AdfsDetailsTO adfsDetailsTO, String decryptedPassword) throws AuthException {
		try {
			if (application.getApplicationType().toString().equals(ApplicationType.ADFS.toString())) {
				int retryAttempt = retry;
				while (retryAttempt >= 1){
					try {
						adfsDetailsTO = adfsUtil.getAdfs(searchAttributeValue, decryptedPassword);
						break;
					} catch (ProcessingException e){
						logger.log(Level.WARN, e);
						if(e.getCause() instanceof java.net.UnknownHostException){
							retryAttempt --;
							if(retryAttempt == 0){
								throw  new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
							}
							logger.log(Level.DEBUG, "retryAttempt : "+ (retry-retryAttempt));
						} else {
							throw e;
						}
					}
				}
			} else if (application.getApplicationType().toString().equals(ApplicationType.AD.toString())) {
				String userId = searchAttributeValue;
				String userDomainName =null;
				if (ValidationUtilV3.isValid(searchAttributeValue) && searchAttributeValue.contains("@")) {
					int index = searchAttributeValue.indexOf('@');
					userId = searchAttributeValue.substring(0, index);
					userDomainName =searchAttributeValue.substring(index+1);
				}
				LdapDetailsTO ldapDetailsTO=null;
				if(userDomainName !=null){
					try {
						ldapDetailsTO= ldapDetailsServiceIntf.getLdapDetailsByUserDomainName(userDomainName).convertToTO();
					}catch (NotFoundException e){
						ldapDetailsTO=null;
					}
				}if(ldapDetailsTO!=null) {
					adfsDetailsTO =	adSyncService.getAttributesWithCustomerAD(userId, decryptedPassword, ldapDetailsTO);
				}else {
					adfsDetailsTO = adSyncService.getAttributes(userId, decryptedPassword);
				}
			} else {
				throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_INVALID_APPLICATION_TYPE(),
						errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_TYPE());
			}
		} catch (SignOnException | JsonProcessingException e) {
			throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_ADFS_DETAIL_NOT_FOUND(),
					errorConstant.getERROR_MESSAGE_ADFS_DETAIL_NOT_FOUND());
		} catch (IOException e) {
			throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_AD_DETAIL_NOT_FOUND(),
					errorConstant.getERROR_MESSAGE_AD_DETAIL_NOT_FOUND());
		}
		return adfsDetailsTO;
	}

	private String decryptPassword(AdHotpTO adHotpTO) throws AuthException {
		String decryptedPassword;
		try {
			decryptedPassword = RSAUtil.decryptData(adHotpTO.getPassword());
		} catch (Exception e) {
			throw new AuthException(new Throwable(),errorConstant.getERROR_CODE_INVALID_USERID_PASSWORD(),
					errorConstant.getERROR_MESSAGE_INVALID_USERID_PASSWORD());
		}
		return decryptedPassword;
	}

	private AdfsDetailsTO handleEmailAndMobileInRequestData(AdHotpTO adHotpTO, Application application)
			throws AuthException {
		AdfsDetailsTO adfsDetailsTO;
		AttributeStoreServiceImpl attributeStoreService = AttributeStoreServiceImpl.getInstance();
		adfsDetailsTO = new AdfsDetailsTO();
		for (AttributeDataTO attributeDataTO : adHotpTO.getSearchAttributes()) {
			AttributeValidationUtil.validateSearchAttributeValueAndUniquenessWithoutCrypto(attributeDataTO.getAttributeName(),
					attributeDataTO.getAttributeValue());
		}
		User user = null;
		try {
			IAMExtensionV2 iamExtension = iamUtil
					.getIAMExtensionV2WithoutCrypto(application.getEnterprise().getEnterpriseAccountId());
			Token token = iamUtil.authenticateV2WithoutCrypto(iamExtension, application.getApplicationId(),
					AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
			AccountWE accountWE = iamExtensionService.searchAccount(adHotpTO.getSearchAttributes(), iamExtension,
					token);
			user = userService.getActiveUserForAuthAttempt(accountWE.getId());
		} catch (IAMException e) {
			logger.log(Level.ERROR, e);
			throw iamExceptionConvertorUtil.convertToAuthException(e);
		} catch (AuthException e) {
			logger.log(Level.FATAL, ADHOTP_FACADE_IMPL_LOG + e.getMessage(), e);
		}
		for (AttributeDataTO attributeDataTO : adHotpTO.getAttributeData()) {
			String attributeDataName = attributeDataTO.getAttributeName();
			String attributeDataValue = attributeDataTO.getAttributeValue();
			if (ValidationUtilV3.isAttributeValueValid(attributeDataName, attributeDataValue)) {
				if (attributeDataName.equalsIgnoreCase("EMAIL_ID")) {
					AttributeDataTO attributeDataTO1 = null;
					try {
						attributeDataTO1 = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(
								attributeDataName, attributeDataValue, user.getId()).convertToTO();
					} catch (Exception e) {
						logger.log(Level.FATAL, ADHOTP_FACADE_IMPL_LOG + e.getMessage(), e);
					}
					if (attributeDataTO1 == null) {
						adfsDetailsTO.setEmail(attributeDataValue);
						adfsDetailsTO.setIsEmailDefault(attributeDataTO.getIsDefault());
					} else {
						User otherUser = null;
						try {
							otherUser = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(
									attributeDataName, attributeDataValue, user.getId()).getUser();
						} catch (AuthException e) {
							logger.log(Level.FATAL, ADHOTP_FACADE_IMPL_LOG + e.getMessage(), e);
						}
						if (user != null && otherUser != null && otherUser.equals(user)) {
							adfsDetailsTO.setEmail(attributeDataValue);
							adfsDetailsTO.setIsEmailDefault(attributeDataTO.getIsDefault());
						} else {
							attributeDataTO.setStatus("Attribute already present");
						}
					}
				} else if (attributeDataName.equalsIgnoreCase("MOBILE_NO")) {
					AttributeDataTO attributeDataTO1 = null;
					try {
						attributeDataTO1 = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(
								attributeDataName, attributeDataValue, user.getId()).convertToTO();
					} catch (Exception e) {
						logger.log(Level.FATAL, e.getMessage(), e);
					}
					if (attributeDataTO1 == null) {
						adfsDetailsTO.setMobile(attributeDataValue);
						adfsDetailsTO.setIsMobileDefault(attributeDataTO.getIsDefault());
					} else {
						User otherUser = null;
						try {
							otherUser = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(
									attributeDataName, attributeDataValue, user.getId()).getUser();
						} catch (AuthException e) {
							logger.log(Level.FATAL, ADHOTP_FACADE_IMPL_LOG + e.getMessage(), e);
						}
						if (user != null && otherUser != null && otherUser.equals(user)) {
							adfsDetailsTO.setMobile(attributeDataValue);
							adfsDetailsTO.setIsMobileDefault(attributeDataTO.getIsDefault());
						} else {
							attributeDataTO.setStatus("Attribute already present");
						}
					}
				}
			} else {
				throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE(),
						errorConstant.getERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE());
			}
		}
		if (adfsDetailsTO.getEmail() == null && adfsDetailsTO.getMobile() == null) {
			throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_INVALID_ATTRIBUTE_TYPE(),
					errorConstant.getERROR_MESSAGE_INVALID_ATTRIBUTE_TYPE());
		}
		return adfsDetailsTO;
	}

	// this function will add or update attributes on adapter and ids
	private void updateAttributes(String accountId, AdfsDetailsTO adfsDetailsTO, Application application,
			String service, String finalSearchAttributeValue) {
		Session session = sessionFactoryUtil.getSession();
		try {
			logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " updateAttributes : start");
			User user = null;
			try {
				user = userService.getActiveUser(session, accountId);
				if (user != null) {
					if(!UserApplicationRelDaoImpl.getInstance().isApplicationUserBindingPresent(application.getId(), user.getId(), session)){
						IAMUtil.getInstance().onboardUserApi(application, service, finalSearchAttributeValue, adfsDetailsTO);
					}
				}
			} catch (AuthException e) {
				try {
					IAMUtil.getInstance().onboardUserApi(application, service, finalSearchAttributeValue, adfsDetailsTO);
//					onboardAdUser(application.getApplicationId(), service, finalSearchAttributeValue, adfsDetailsTO);
					user = userService.getActiveUser(session, accountId);
				} catch (AuthException exception) {
					logger.log(Level.ERROR, exception.getMessage(), exception);
				}
			}

			if (user != null) {
				boolean mobilePresent = false;
				boolean emailPresent = false;
				// update attributes if present in adapter
				if (user.getAttributeStores() != null) {
					for (AttributeStore attributeStore : user.getAttributeStores()) {
						if (Constant.MOBILE_NO.equals(attributeStore.getAttributeName())) {
							mobilePresent = true;
							if (adfsDetailsTO.getMobile() != null
									&& !Objects.equals(attributeStore.getAttributeValue(), adfsDetailsTO.getMobile())
									&& ValidationUtilV3.isMobileValid(adfsDetailsTO.getMobile())) {
//								attributeStore.setAttributeValue(adfsDetailsTO.getMobile());
//								attributeStoreService.update(session, attributeStore);
							}
						}
						if (Constant.EMAIL_ID.equals(attributeStore.getAttributeName())) {
							emailPresent = true;
							if (adfsDetailsTO.getEmail() != null
									&& !Objects.equals(attributeStore.getAttributeValue(), adfsDetailsTO.getEmail())
									&& ValidationUtilV3.isEmailValid(adfsDetailsTO.getEmail())) {
//								attributeStore.setAttributeValue(adfsDetailsTO.getEmail());
//								attributeStoreService.update(session, attributeStore);
							}
						}
					}
				}

				// create attributes if not exists - in adapter and ids
				UserIciciTO userIciciTO = new UserIciciTO();
				List<AttributeDataTO> attributeDataTOList = new ArrayList<>();

				List<AttributeDataTO> searchAttributeList = new ArrayList<>();
				AttributeDataTO searchAttribute = new AttributeDataTO();
				searchAttribute.setAttributeName(Constant.USER_ID);
				searchAttribute.setAttributeValue(finalSearchAttributeValue);
				searchAttributeList.add(searchAttribute);
				userIciciTO.setSearchAttributes(searchAttributeList);

				if (!mobilePresent && adfsDetailsTO.getMobile() != null
						&& ValidationUtilV3.isMobileValid(adfsDetailsTO.getMobile())) {
					AttributeDataTO attributeMobile = new AttributeDataTO();
					attributeMobile.setAttributeName(Constant.MOBILE_NO);
					attributeMobile.setAttributeValue(adfsDetailsTO.getMobile());
					attributeMobile.setIsDefault(true);
					attributeDataTOList.add(attributeMobile);
				}
				if (!emailPresent && adfsDetailsTO.getEmail() != null
						&& ValidationUtilV3.isEmailValid(adfsDetailsTO.getEmail())) {
					AttributeDataTO attributeEmail = new AttributeDataTO();
					attributeEmail.setAttributeName(Constant.EMAIL_ID);
					attributeEmail.setAttributeValue(adfsDetailsTO.getEmail());
					attributeEmail.setIsDefault(true);
					attributeDataTOList.add(attributeEmail);
				}
				if (!attributeDataTOList.isEmpty()) {
					userIciciTO.setAttributeData(attributeDataTOList);
					userFacade.onboardUserV4(userIciciTO);
				}

				// binding user to application - if binding not exists
				bindUserToApplicationIfNotExits(application, service, session, user);


			}

			AccountWE accountWE = iamExtension.getAllAttributesForAccount(accountId);
			// update attributes if present in ids
			if (accountWE.getAttributes() != null) {
				for (AttributeTO attributeTO : accountWE.getAttributes()) {
					if (Constant.MOBILE_NO.equals(attributeTO.getAttributeName())) {
						if (adfsDetailsTO.getMobile() != null
								&& !Objects.equals(attributeTO.getAttributeValue(), adfsDetailsTO.getMobile())
								&& ValidationUtilV3.isMobileValid(adfsDetailsTO.getMobile())) {
							attributeTO.setUpdatedAttributeValue(adfsDetailsTO.getMobile());
//							iamExtension.editAttribute(attributeTO, accountId);
						}
					}
					if (Constant.EMAIL_ID.equals(attributeTO.getAttributeName())
							&& ValidationUtilV3.isEmailValid(adfsDetailsTO.getEmail())) {
						if (adfsDetailsTO.getEmail() != null && !Objects
								.equals(attributeTO.getAttributeValue().toLowerCase(), adfsDetailsTO.getEmail().toLowerCase())) {
							attributeTO.setUpdatedAttributeValue(adfsDetailsTO.getEmail());
//							iamExtension.editAttribute(attributeTO, accountId);
						}
					}
				}
			}

			sessionFactoryUtil.closeSession(session);
		} catch (Exception e) {
			session.getTransaction().rollback();
			logger.log(Level.ERROR, e.getMessage(), e);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
			logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " updateAttributes : end");
		}
	}

	private void bindUserToApplicationIfNotExits(Application application, String service, Session session, User user)
			throws AuthException {
		boolean isBindingPresent = userApplicationDao.isApplicationUserBindingPresent(application.getId(),
				user.getId());
		if (!isBindingPresent) {
			List<ApplicationTO> applications = new ArrayList<>();
			ApplicationTO applicationTO = new ApplicationTO();
			applicationTO.setApplicationId(application.getApplicationId());
			List<ServiceTO> services = new ArrayList<>();
			ServiceTO serviceTO = new ServiceTO();
			serviceTO.setServiceName(service);
			services.add(serviceTO);
			applicationTO.setServices(services);
			applications.add(applicationTO);
			userService.autoBindUserToApplication(session, applications, user, null);
		}
	}

	// this function will onboard user on both adapter and ids
	private void onboardAdUser(String applicationId, String service, String finalSearchAttributeValue,
			AdfsDetailsTO finalAdfsDetailsTO) { //TODO: Can remove this no usages
		logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " onboardAdUser : start");
		try {
			UserIciciTO user = new UserIciciTO();
			List<AttributeDataTO> searchAttributeList = new ArrayList<>();
			AttributeDataTO searchAttribute = new AttributeDataTO();
			searchAttribute.setAttributeName(Constant.USER_ID);
			searchAttribute.setAttributeValue(finalSearchAttributeValue);
			searchAttributeList.add(searchAttribute);
			user.setSearchAttributes(searchAttributeList);
			user.setAccountType("USER");
			List<AttributeDataTO> attributeDataTOList = new ArrayList<>();
			if (finalAdfsDetailsTO.getMobile() != null) {
				logger.log(Level.DEBUG,
						"Mobile pattern match : " + ValidationUtilV3.isMobileValid(finalAdfsDetailsTO.getMobile()));
				if (ValidationUtilV3.isMobileValid(finalAdfsDetailsTO.getMobile())) {
					AttributeDataTO attributeMobile = new AttributeDataTO();
					attributeMobile.setAttributeName(Constant.MOBILE_NO);
					attributeMobile.setAttributeValue(finalAdfsDetailsTO.getMobile());
					attributeMobile.setIsDefault(finalAdfsDetailsTO.getIsMobileDefault());
					if(attributeMobile.getIsDefault()==null){
						attributeMobile.setIsDefault(true);
					}
					attributeDataTOList.add(attributeMobile);
				}
			}
			if (finalAdfsDetailsTO.getEmail() != null) {
				logger.log(Level.DEBUG,
						"Email pattern match : " + ValidationUtilV3.isEmailValid(finalAdfsDetailsTO.getEmail()));
				if (ValidationUtilV3.isEmailValid(finalAdfsDetailsTO.getEmail())) {
					AttributeDataTO attributeEmail = new AttributeDataTO();
					attributeEmail.setAttributeName(Constant.EMAIL_ID);
					attributeEmail.setAttributeValue(finalAdfsDetailsTO.getEmail());
					attributeEmail.setIsDefault(finalAdfsDetailsTO.getIsEmailDefault());
					if(attributeEmail.getIsDefault()==null){
						attributeEmail.setIsDefault(true);
					}
					attributeDataTOList.add(attributeEmail);
				}
			}
			if (!attributeDataTOList.isEmpty()) {
				user.setAttributeData(attributeDataTOList);
			}
			List<ApplicationTO> applications = new ArrayList<>();
			ApplicationTO applicationTO = new ApplicationTO();
			applicationTO.setApplicationId(applicationId);
			List<ServiceTO> services = new ArrayList<>();
			ServiceTO serviceTO = new ServiceTO();
			serviceTO.setServiceName(service);
			services.add(serviceTO);
			applicationTO.setServices(services);
			applications.add(applicationTO);
			user.setSubscribedApplications(applications);
			logger.log(Level.DEBUG, "Onboard user request : " + new Gson().toJson(user));
			userFacade.onboardUser(user, null, null);
		} catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage(), e);
		}
		logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " onboardAdUser : end");
	}

	private String getAccountId(AdHotpTO adHotpTO) throws AuthException {
		String searchAttributeValue = null;
		String attributeName = null;

		for (AttributeDataTO attributeDataTO : adHotpTO.getSearchAttributes()) {
			attributeName = attributeDataTO.getAttributeName();
			searchAttributeValue = attributeDataTO.getAttributeValue();
		}
		IAMExtensionV2 iamExtensionV2 = iamExtension.getIAMExtensionWithoutCrypto();
		return iamExtension.getAccountId(attributeName, searchAttributeValue, iamExtensionV2);
	}
}
