
package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MediaType;

import in.fortytwo42.adapter.transferobj.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.glassfish.jersey.client.ClientConfig;
//import org.glassfish.jersey.logging.LoggingFeature;
import org.hibernate.Session;
import org.keycloak.representations.idm.UserRepresentation;

import com.google.gson.Gson;

import in.fortytwo42.adapter.cam.facade.CamUserFacadeImpl;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeIntf;
import in.fortytwo42.adapter.cam.util.CamUtil;
import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.AuthenticationAttemptServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.NonADUserServiceIntf;
import in.fortytwo42.adapter.service.PermissionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.ServiceProcessorIntf;
import in.fortytwo42.adapter.service.UserApplicationRelServiceIntf;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AttributeValidationUtil;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.handler.AuthAttemptHistoryHandler;
import in.fortytwo42.adapter.util.handler.BindingAsyncResponseHandler;
import in.fortytwo42.daos.dao.AuthenticationAttemptDaoIntf;
import in.fortytwo42.daos.dao.AuthenticationAttemptHistoryDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.exception.TransactionNotFoundException;
import in.fortytwo42.daos.exception.UserApplicationRelNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.tos.EncryptionDataTO;
import in.fortytwo42.enterprise.extension.utils.GsonProvider;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.enterprise.extension.webentities.AttributeMetaDataWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.AuthenticationAttemptHistory;
import in.fortytwo42.entities.bean.CallbackUrl;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceCompositeKey;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.enums.ApplicationType;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.IAMStatus;
import in.fortytwo42.entities.enums.RequestSubType;
import in.fortytwo42.entities.enums.ResetPinUserUnblockStatus;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

// TODO: Auto-generated Javadoc
/**
 * The Class NonADUserFacadeImpl.
 */
public class NonADUserFacadeImpl implements NonADUserFacadeIntf {

    /** The nonad user facade impl log. */
    private String NONAD_USER_FACADE_IMPL_LOG = "<<<<< NonADUserFacadeImpl";

    /** The Constant I_AM_EXCEPTION_IS_THROWN. */
    private static final String I_AM_EXCEPTION_IS_THROWN = "~I-AM exception is thrown";

    /** The Constant USER_IS_CREATED. */
    private static final String USER_IS_CREATED = "~user is created";

    /** The Constant USER_BINDING. */
    private static final String USER_BINDING = "User binding~";

    private static Logger logger= LogManager.getLogger(NonADUserFacadeImpl.class);

    private NonADUserServiceIntf nonADUserService = ServiceFactory.getNonADUserService();
    /** The user processor. */
    private UserServiceIntf userService = ServiceFactory.getUserService();
    /** The service processor. */
    private ServiceProcessorIntf serviceProcessor = ServiceFactory.getServiceProcessor();
    /** The user application rel processor. */
    private UserApplicationRelServiceIntf userApplicationRelService = ServiceFactory.getUserApplicationRelService();
    /** The permission processor. */
    private PermissionServiceIntf permissionService = ServiceFactory.getPermissionService();
    /** The authentication attempt processor. */
    private AuthenticationAttemptServiceIntf authenticationAttemptService = ServiceFactory.getAuthenticationService();
    /** The iam extension processor. */
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();

    private AuthenticationAttemptDaoIntf authenticationAttemptDao = DaoFactory.getAuthenticationAttemptDao();
    private AuthenticationAttemptHistoryDaoIntf authenticationAttemptHistoryDao = DaoFactory.getAuthenticationHistoryDao();
    private UserDaoIntf userDao = DaoFactory.getUserDao();
    private RequestDaoIntf requestDao = DaoFactory.getRequestDao();

    private final AuthenticationAttemptHistoryDaoIntf authenticationAttemptDaoIntf = DaoFactory.getAuthenticationHistoryDao();
    private UserApplicationRelDaoIntf userApplicationRelDao = DaoFactory.getUserApplicationRel();

    private BindingAsyncResponseHandler bindingAsyncResponseHandler = BindingAsyncResponseHandler.getInstance();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();
    private AuthAttemptHistoryHandler authAttemptHistoryHandler = AuthAttemptHistoryHandler.getInstance();
    private IAMUtil iamUtil = IAMUtil.getInstance();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private CamUserFacadeIntf camUserFacade = CamUserFacadeImpl.getInstance();
    private static final String SRA = "SRA";

    /**
     * The Class InstanceHolder.
     */
    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final NonADUserFacadeImpl INSTANCE = new NonADUserFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of NonADUserFacadeImpl.
     *
     * @return single instance of NonADUserFacadeImpl
     */
    public static NonADUserFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Bind consumer.
     *
     * @param consumerBindingTO the consumer binding TO
     * @param applicationId the application id
     * @param serverId the server id
     * @param asyncResponse the async response
     * @throws AuthException the auth exception
     */
    @Override
    public void bindConsumer(ConsumerBindingTO consumerBindingTO, String applicationId, AsyncResponse asyncResponse) throws AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " bindConsumer : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            attributeNameToUpperCase(consumerBindingTO.getCustomAttribute());
            for (AttributeDataTO attributeDataTO : consumerBindingTO.getSearchAttributes()) {
                attributeNameToUpperCase(attributeDataTO);
                AttributeValidationUtil.validateSearchAttributeValueAndUniqueness(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());
            }
            if(consumerBindingTO.getCustomAttribute() != null) {
                AttributeValidationUtil.validateSearchAttributeValueAndUniqueness(consumerBindingTO.getCustomAttribute().getAttributeName(), consumerBindingTO.getCustomAttribute().getAttributeValue());
            }
            Application application = applicationService.getNonADApplicationByApplicationId(applicationId);
            // if transaction not found then proceed or else error out
            try{
                AuthenticationAttemptHistory authenticationAttemptHistory = authenticationAttemptDaoIntf.getAuthAttemptByApplicationIdAndTransactionId(application.getApplicationAccountId(),consumerBindingTO.getTransactionId());
                if( authenticationAttemptHistory != null) {
                    throw new AuthException(null,errorConstant.getERROR_CODE_INVALID_DATA(),errorConstant.getERROR_MESSAGE_TRANSACTION_ALREADY_EXISTS());
                }
            } catch (TransactionNotFoundException ignored) {
             // ignore the transaction not found
            }
            Service service = serviceProcessor.getService(consumerBindingTO.getServiceName());
            validateService(application, service);
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            AccountWE accountWE = iamExtensionService.searchAccount(consumerBindingTO.getSearchAttributes(), iamExtension, token);
            User user = null;
            try {
                user = userService.getActiveUser(session, accountWE.getId());
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            if (user != null) {
                userService.validateUser(user);

                UserApplicationServiceCompositeKey userApplicationServiceCompositeKey = new UserApplicationServiceCompositeKey();
                userApplicationServiceCompositeKey.setUser(user);
                userApplicationServiceCompositeKey.setApplication(application);
                userApplicationServiceCompositeKey.setService(service);
                UserApplicationServiceRel userApplicationServiceRel = null;
                try {
                    userApplicationServiceRel = userApplicationRelDao.getUserApplicationForId(userApplicationServiceCompositeKey);
                }
                catch (UserApplicationRelNotFoundException e) {
                    logger.log(Level.ERROR, e);
                }
                validateUserApplicationRel(userApplicationServiceRel);
            }
            else {
                List<AttributeStore> attributeStores = new ArrayList<>();
                user = new User();
                user = userService.createUser(session, accountWE.getId(), UserRole.USER, consumerBindingTO.getTwoFactorStatus(), user);
                for (AttributeDataTO attributeDataTO : consumerBindingTO.getSearchAttributes()) {
                    boolean userConsent = consumerBindingTO.getUserConsentRequired() != null ? consumerBindingTO.getUserConsentRequired() : true;
                    attributeStores.add(attributeStoreService.saveAttributeData(session, attributeDataTO, user, userConsent));
                }
                user.setAttributeStores(attributeStores);
                user = userService.updateUser(session, user);
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, USER_BINDING, System.currentTimeMillis() + Constant.TILT,
                        application.getApplicationId(), Constant.TILT, consumerBindingTO.getTransactionId(), USER_IS_CREATED));
            }
            Integer timeout = consumerBindingTO.getTimeOut() == null ? application.getTransactionTimeout() : consumerBindingTO.getTimeOut();
            consumerBindingTO.setTimeOut(timeout);
            boolean isPublicAttributePresent = consumerBindingTO.getCustomAttribute() != null ? iamExtensionService.isPublicAttributePresent(consumerBindingTO.getCustomAttribute(), accountWE) : true;
            initiateUserServiceBinding(session, consumerBindingTO, service, application, user, isPublicAttributePresent);
            Boolean activateEncryption = consumerBindingTO.getActivateEncryption() != null ? consumerBindingTO.getActivateEncryption() : false;
            logger.log(Level.INFO, "Adding to binding cache " + consumerBindingTO.getTransactionId() + " " + application.getApplicationAccountId());
            bindingAsyncResponseHandler.storeAsyncResponseReference(consumerBindingTO.getTransactionId(), application.getApplicationAccountId(), asyncResponse, consumerBindingTO,
                    activateEncryption);
            sessionFactoryUtil.closeSession(session);
            AuditLogUtil.sendAuditLog("User " + consumerBindingTO.getSearchAttributes().get(0).getAttributeValue() + " application " + applicationId + " service " + service.getServiceName() + " binding successful", "USER", ActionType.BIND_USER_APPLICATION, "", IdType.ACCOUNT, "", null, accountWE.getId(), null);
        }
        catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, "User binding~",
                            System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT,
                            consumerBindingTO.getTransactionId(), I_AM_EXCEPTION_IS_THROWN));
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " bindConsumer : start");
        }
    }

    /**
     * Validate user application rel.
     *
     * @param userApplicationRel the user application rel
     * @throws AuthException the auth exception
     */
    private void validateUserApplicationRel(UserApplicationServiceRel userApplicationRel) throws AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " validateUserApplicationRel : start");
        if (userApplicationRel != null) {
            if (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT());
            }
            if (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_BLOCKED(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_BLOCKED());
            }
        }
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " validateUserApplicationRel : end");
    }

    /**
     * Unbind consumer.
     *
     * @param consumerBindingTO the consumer binding TO
     * @param applicationId the application id
     * @param version the version
     * @return true, if successful
     * @throws AuthException the auth exception
     */
    @Override
    public boolean unbindConsumer(ConsumerBindingTO consumerBindingTO, String applicationId, String version) throws AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " unbindConsumer : start");
        Session session=null;
        try {
            session = IamThreadContext.getSessionWithoutTransaction();
            attributeNameToUpperCase(consumerBindingTO);
            Application application = applicationService.getNonADApplicationByApplicationId(applicationId, session);
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            for (AttributeDataTO attributeDataTO : consumerBindingTO.getSearchAttributes()) {
                AttributeValidationUtil.validateSearchAttributeValueAndUniqueness(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());
            }
            AccountWE accountWE = iamExtensionService.searchAccount(consumerBindingTO.getSearchAttributes(), iamExtension, token);
            User user = userService.getActiveUser(session, accountWE.getId());
            userService.validateUser(user);
            Service service = serviceProcessor.getService(consumerBindingTO.getServiceName(), session);
            UserApplicationServiceRel userApplicationRel = userApplicationRelService.getUserApplicationRel(user, application, service, session);
            if (userApplicationRel == null || (userApplicationRel.getBindingStatus() == BindingStatus.PENDING || userApplicationRel.getBindingStatus() == BindingStatus.INACTIVE)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND());
            }
            boolean isSingleBindingPresent = userApplicationRelService.getUserAndApplicationRelCount(application.getId(), user.getId(), session) == 1;
            boolean isConsumerUnbind =false;
            if (isSingleBindingPresent) {
                session.beginTransaction();
                try {
                    userApplicationRelService.updateUserApplicationRel(session, userApplicationRel, BindingStatus.INACTIVE);
                    iamExtension.forceTimeoutApprovalAttemptsByLookupId(token, user.getAccountId(), application.getApplicationAccountId() + "|" + service.getServiceName());
                    if(application.getKcId() != null && user.getKcId() != null) {
                        camUserFacade.bindUserToApplication(Config.getInstance().getProperty(Constant.CAM_REALM), application.getKcId(), user.getKcId(), application.getApplicationId(), Constant.UNBIND_OPERATION);
                    }
                            isConsumerUnbind=iamExtension.unbindConsumerApplication(token, user.getAccountId());
                            AccountWE camAccountForKcId = new AccountWE();
                            camAccountForKcId.setId(accountWE.getId());
                            camAccountForKcId.setKcId(Constant.UNBIND_OPERATION);
                            iamExtension.editAccount(camAccountForKcId, accountWE.getId(), token);
                            user.setKcId(null);
                            userService.updateUser(session, user);
                    List<UserApplicationServiceRel> userApplicationRels = userApplicationRelService.getUserApplicationRel(user.getId(), session);
                    //TODO: check with chirag if IAMStatus is required. If not, this status can be removed.
                    if (userApplicationRels == null || userApplicationRels.isEmpty()) {
                        user.setIamStatus(IAMStatus.DISABLED);
                        userService.updateUser(session, user);
                    }
                    session.getTransaction().commit();
                    if (consumerBindingTO.getDeleteProfile() != null) {
                        if (consumerBindingTO.getDeleteProfile()) {
                            AccountWE accountWE1 = new AccountWE();
                            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeDataTOList = new ArrayList<>();
                            for (AttributeDataTO attr : consumerBindingTO.getSearchAttributes()) {
                                in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
                                attributeTO.setAttributeName(attr.getAttributeName());
                                attributeTO.setAttributeValue(attr.getAttributeValue());
                                attributeDataTOList.add(attributeTO);
                            }
                            if (consumerBindingTO.getAttributeData() != null) {
                                for (AttributeDataTO attr : consumerBindingTO.getAttributeData()) {
                                    in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
                                    attributeTO.setAttributeName(attr.getAttributeName());
                                    attributeTO.setAttributeValue(attr.getAttributeValue());
                                    attributeDataTOList.add(attributeTO);
                                }
                            }
                            accountWE1.setAttributes(attributeDataTOList);
                            accountWE1.setApplicationId(applicationId);
                            iamExtension.deleteProfile(accountWE1, accountWE.getId(), token);
                        }
                    }
                }
                catch (Exception e) {
                    session.getTransaction().rollback();
                    throw e;
                }
            }

            long auth42ServiceBindingCount = userApplicationRelService.getUserServiceRelCount(service.getId(), user.getId(), IamThreadContext.getSessionWithoutTransaction());
            if(auth42ServiceBindingCount <= 0) {
                userService.enableToken(user.getAccountId(), application, false);
            }
            AuditLogUtil.sendAuditLog("User " + consumerBindingTO.getSearchAttributes().get(0).getAttributeValue() + " application " + applicationId + " service " + service.getServiceName() + " unbinding successful", "USER", ActionType.BIND_USER_APPLICATION, "", IdType.ACCOUNT, "", null, accountWE.getId(), null);
            return isConsumerUnbind;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            if(session!=null){
                session.getTransaction().rollback();
            }
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            if(session!=null) {
                session.close();
            }
            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " unbindConsumer : end");
        }
    }

    /**
     * Gets the consumer status.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     * @param applicationId the application id
     * @param serviceName the service name
     * @return the consumer status
     * @throws AuthException the auth exception
     */
    @Override
    public ConsumerTO getConsumerStatus(String attributeName, String attributeValue, String applicationId, String serviceName) throws AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " getConsumerStatus : start");
        ConsumerTO consumerBindingTO = new ConsumerTO();
        consumerBindingTO.setAttributeName(attributeName.toUpperCase());
        consumerBindingTO.setAttributeValue(attributeValue);
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        if (application.getApplicationType() == ApplicationType.AD) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_TYPE());
        }
        if (application.getTwoFactorStatus() == TwoFactorStatus.DISABLED) {
            consumerBindingTO.setStatusCode(Constant.APPLICATION_INACTIVE);
            return consumerBindingTO;
        }
        try {
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(),
                    AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            String accountId = getAccountId(attributeName.toUpperCase(), attributeValue, iamExtension);

            boolean isConsumerActive = iamExtension.isConsumerActive(token, accountId);
            if (!isConsumerActive) {
                consumerBindingTO.setStatusCode(Constant.CONSUMER_NOT_REGISTERED);
                return consumerBindingTO;
            }
            User user = null;
            try {
                user = userService.getActiveUser(accountId);
            }
            catch (AuthException e) {
                logger.log(Level.INFO, e);
            }
            if (user == null) {
                consumerBindingTO.setStatusCode(Constant.CONSUMER_APPLICATION_BINDING_INACTIVE);
                return consumerBindingTO;
            }
            if (user.getUserStatus() == UserStatus.BLOCK) {
                consumerBindingTO.setStatusCode(Constant.USER_BLOCKED);
                return consumerBindingTO;
            }
            if (user.getTwoFactorStatus() == TwoFactorStatus.DISABLED) {
                consumerBindingTO.setStatusCode(Constant.CONSUMER_INACTVE);
                return consumerBindingTO;
            }

            Service service = serviceProcessor.getService(serviceName);
            if (service == null) {
                consumerBindingTO.setStatusCode(Constant.SERVICE_INACTIVE);
                return consumerBindingTO;
            }
            else if (!application.getServices().contains(service)) {
                consumerBindingTO.setStatusCode(Constant.SERVICE_APPLICATION_BINDING_INACTIVE);
                return consumerBindingTO;
            }

            UserApplicationServiceRel userApplicationRel = userApplicationRelService.getUserApplicationRel(user, application, service);
            if (userApplicationRel != null) {
                if (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE) {
                    consumerBindingTO.setStatusCode(Constant.CONSUMER_APPLICATION_BINDING_ACTIVE);
                    return consumerBindingTO;
                }
                else if (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED) {
                    consumerBindingTO.setStatusCode(Constant.CONSUMER_APPLICATION_BINDING_BLOCKED);
                    return consumerBindingTO;
                }
                else if (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED_FOR_RESET_PIN) {
                    consumerBindingTO.setStatusCode(Constant.BLOCKED_FOR_RESET_PIN_CODE);
                    return consumerBindingTO;
                }
                else if (userApplicationRel.getBindingStatus() == BindingStatus.RESET_PIN_COMPLETED) {
                    consumerBindingTO.setStatusCode(Constant.RESET_PIN_COMPLETED_CODE);
                    return consumerBindingTO;
                }
            }
            consumerBindingTO.setStatusCode(Constant.CONSUMER_APPLICATION_BINDING_INACTIVE);
            return consumerBindingTO;
        }
        catch (IAMException e) {
            logger.log(Level.INFO, e);
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), I_AM_EXCEPTION_IS_THROWN));
            consumerBindingTO.setStatusCode(Constant.CONSUMER_NOT_REGISTERED);
            return consumerBindingTO;
        }
        finally {
            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " getConsumerStatus : end");
        }

    }

    /**
     * Update user application service rel.
     *
     * @param userTO the user TO
     * @param applicationId the application id
     * @return the consumer TO
     * @throws AuthException the auth exception
     */
    @Override
    public ConsumerTO updateUserApplicationServiceRel(ConsumerTO userTO, String applicationId) throws AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " updateUserApplicationServiceRel : start");
        attributeNameToUpperCase(userTO);
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        String accountId = null;
        try {
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            accountId = this.getAccountId(userTO.getAttributeName(), userTO.getAttributeValue(), iamExtension);

        }
        catch (IAMException e) {
            logger.log(Level.INFO, e);
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), I_AM_EXCEPTION_IS_THROWN));
        }
        User user = userService.getActiveUser(accountId);
        userService.validateUser(user);
        List<UserApplicationServiceRel> userApplicationRels = userApplicationRelService.getBlockedApplicationRelsForUser(user.getId(), application.getId());
        if (userApplicationRels != null && !userApplicationRels.isEmpty()) {
            List<UserApplicationServiceRel> userApplicationRelsToUpdate = new ArrayList<>();
            for (UserApplicationServiceRel userApplicationRel : userApplicationRels) {
                if (userApplicationRel.getBindingStatus().equals(BindingStatus.BLOCKED_FOR_RESET_PIN)) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_BLOCKED_FOR_RESET_PIN(), errorConstant.getERROR_MESSAGE_BLOCKED_FOR_RESET_PIN());
                }
                userApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
                userApplicationRelsToUpdate.add(userApplicationRel);
            }
            if (!userApplicationRelsToUpdate.isEmpty()) {
                Session session = sessionFactoryUtil.getSession();
                try {

                    userService.bulkUpdateUserApplicationRel(session, userApplicationRelsToUpdate);
                    userTO.setStatus(Constant.SUCCESS_STATUS);
                    sessionFactoryUtil.closeSession(session);
                }
                catch (Exception e) {
                    session.getTransaction().rollback();
                    throw e;
                }
                finally {
                    if (session.isOpen()) {
                        session.close();
                    }
                }
                logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " updateUserApplicationServiceRel : end");
                return userTO;
            }
        }
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " updateUserApplicationServiceRel : end");
        throw new AuthException(null, errorConstant.getERROR_CODE_SERVICES_ALREADY_ENABLED_FOR_USER(), errorConstant.getERROR_MESSAGE_SERVICES_ALREADY_ENABLED_FOR_USER());
    }

    /**
     * Update multiple user application service rel copy.
     *
     * @param blockUserApplicationTO the block user application TO
     * @throws AuthException the auth exception
     * @throws UserBlockedException the user blocked exception
     */
    @Override
    public void updateMultipleUserApplicationServiceRelCopy(BlockUserApplicationTO blockUserApplicationTO) throws AuthException, UserBlockedException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " updateMultipleUserApplicationServiceRelCopy : start");
        List<Application> applicationList = applicationService.getApplications();
        for (Application application : applicationList) {
            String accountId = null;
            try {
                IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
                //accountId = this.getAccountId(blockUserApplicationTO.getAttributeName(), blockUserApplicationTO.getAttributeValue(), iamExtension);
                accountId = blockUserApplicationTO.getAccountId();
            }
            catch (IAMException e) {
                logger.log(Level.INFO, e);
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT,
                        USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), I_AM_EXCEPTION_IS_THROWN));
            }
            List<User> userList = userService.getNonADUsersByAccountId(accountId);
            if (userList.isEmpty()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            processApplicationWiseUserUpdate(blockUserApplicationTO, userList, application);
            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " updateMultipleUserApplicationServiceRelCopy : end");
        }
    }

    /**
     * Approve user application rel binding.
     *
     * @param userApplicationRelTO the user application rel TO
     * @param role the role
     * @param actor the actor
     * @return the user application rel TO
     * @throws AuthException the auth exception
     */
    //TODO: Ask Chirag whether to remove this
    //	@Override
    //	public UserApplicationRelTO approveUserApplicationRelBinding(UserApplicationRelTO userApplicationRelTO, String role, String actor) throws AuthException{
    ////        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " approveUserApplicationRelBinding : start");
    ////	    if (!permissionProcessor.isPermissionValidForRole(PermissionUtil.APPROVE_USER_APPLICATION_REL_BINDING, role)) {
    ////			throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
    ////		}
    ////		StagingUserApplicationRelBindings stagingUserApplicationRel;
    ////		try {
    ////			stagingUserApplicationRel = userApplicationRelProcessor.getPendingUserApplicationSettings(userApplicationRelTO.getUserId());
    ////		} catch (UserApplicationBindingNotFoundException e) {
    ////		    logger.log(Level.ERROR, e.getMessage(), e);
    ////			throw new AuthException(null, errorConstant.getERROR_CODE_USER_APPLICATION_BINDING_NOT_FOUND, errorConstant.getERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND);
    ////		}
    ////		Application application = applicationProcessor.getApplicationByApplicationId(stagingUserApplicationRel.getApplicationId());
    ////		if (TransactionApprovalStatus.valueOf(userApplicationRelTO.getApprovalStatus()) == TransactionApprovalStatus.APPROVED) {
    ////			List<UserApplicationServiceRel> userApplicationRels = userApplicationRelProcessor.getUserApplicationRel(application.getId(), stagingUserApplicationRel.getUserId());
    ////			for (UserApplicationServiceRel userApplicationRel : userApplicationRels) {
    ////				userApplicationRel.setTwoFactorStatus(stagingUserApplicationRel.getTwoFactorStatus());
    ////				userApplicationRel.setBindingStatus(stagingUserApplicationRel.getBindingStatus());
    ////			}
    ////			userApplicationRelProcessor.bulkUpdateUserApplicationRel(userApplicationRels);
    ////			stagingUserApplicationRel.setApprovalStatus( in.fortytwo42.adapter.idc.enums.ApprovalStatus.APPROVED_BY_CHECKER);
    ////		} else if (TransactionApprovalStatus.valueOf(userApplicationRelTO.getApprovalStatus()) == TransactionApprovalStatus.REJECTED) {
    ////			stagingUserApplicationRel.setApprovalStatus( in.fortytwo42.adapter.idc.enums.ApprovalStatus.REJECTED_BY_CHECKER);
    ////		}
    ////		stagingUserApplicationRel.setChecker(actor);
    ////		stagingUserApplicationRel.setCheckerComments(userApplicationRelTO.getCheckerComments());
    ////		stagingUserApplicationRel.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
    ////		StagingUserApplicationRelBindings stagingUserApplicationRelResult = userApplicationRelProcessor.updateStagingUserApplicationRelBinding(stagingUserApplicationRel);
    ////        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " approveUserApplicationRelBinding : end");
    //		return stagingUserApplicationRelResult.convertToTO();
    //	}

    /**
     * Update user application rel.
     *
     * @param stagingUserApplicationRelSettingsTO the staging user application rel settings TO
     * @param role the role
     * @param actor the actor
     * @return the user application rel TO
     * @throws AuthException the auth exception
     */
    @Override
    public UserApplicationRelTO updateUserApplicationRel(UserApplicationRelTO stagingUserApplicationRelSettingsTO, String role, String actor) throws AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " updateUserApplicationRel : start");
        //        if (!permissionProcessor.isPermissionValidForRole(PermissionUtil.UPDATE_USER_APPLICATION_REL, role)) {
        //            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        //        }
        //        try {
        //            userApplicationRelProcessor.getPendingUserApplicationSettings(stagingUserApplicationRelSettingsTO.getApplicationId(), stagingUserApplicationRelSettingsTO.getUserId());
        //            throw new AuthException(null, errorConstant.getERROR_CODE_USER_UPDATE_PENDING, errorConstant.getERROR_MESSAGE_USER_UPDATE_PENDING);
        //        }
        //        catch (UserApplicationBindingNotFoundException e) {
        //            logger.log(Level.ERROR, e.getMessage(), e);
        //        }
        //        Application application = applicationProcessor.getApplicationByApplicationId(stagingUserApplicationRelSettingsTO.getApplicationId());
        //        User user;
        //        try {
        //            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
        //            String accountId = this.getAccountId(stagingUserApplicationRelSettingsTO.getAttributeName(), stagingUserApplicationRelSettingsTO.getAttributeValue(), iamExtension);
        //            user = userProcessor.getActiveUser(accountId);
        //        }
        //        catch (IAMException e) {
        //            logger.log(Level.ERROR, e.getMessage(), e);
        //            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND, I_AM_EXCEPTION_IS_THROWN);
        //        }
        //        boolean isUpdated = false;
        //        Object[] userApplicationRel;
        //        try {
        //            userApplicationRel = userApplicationRelDaogetUserApplicationRel(application.getId(), user.getId());
        //        }
        //        catch (UserApplicationRelNotFoundException e) {
        //            logger.log(Level.ERROR, e.getMessage(), e);
        //            throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND, errorConstant.getERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND);
        //        }
        //        BindingStatus updatedBindingStatus = BindingStatus.valueOf(stagingUserApplicationRelSettingsTO.getBindingStatus());
        //        TwoFactorStatus updated2FAStatus = TwoFactorStatus.valueOf(stagingUserApplicationRelSettingsTO.getTwoFactorStatus());
        //        BindingStatus currentBindingStatus = BindingStatus.valueOf(String.valueOf(userApplicationRel[0]));
        //        TwoFactorStatus current2FAStatus = TwoFactorStatus.valueOf(String.valueOf(userApplicationRel[1]));
        //
        //        if (updatedBindingStatus != currentBindingStatus) {
        //            isUpdated = true;
        //        }
        //        if (updated2FAStatus != current2FAStatus) {
        //            isUpdated = true;
        //        }
        //        if (isUpdated) {
        //            StagingUserApplicationRelBindings stagingUserApplicationRelBindings = new StagingUserApplicationRelBindings();
        //            stagingUserApplicationRelBindings.setUsername(user.getUsername());
        //            stagingUserApplicationRelBindings.setApplicationId(application.getApplicationId());
        //            stagingUserApplicationRelBindings.setDateTimeCreated(new Timestamp(System.currentTimeMillis()));
        //            stagingUserApplicationRelBindings.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
        //            stagingUserApplicationRelBindings.setMakerComments(stagingUserApplicationRelSettingsTO.getComments());
        //            stagingUserApplicationRelBindings.setBindingStatus(updatedBindingStatus);
        //            stagingUserApplicationRelBindings.setTwoFactorStatus(updated2FAStatus);
        //            stagingUserApplicationRelBindings.setUserId(user.getId());
        //            stagingUserApplicationRelBindings.setActor(actor);
        //            stagingUserApplicationRelBindings.setApprovalStatus(in.fortytwo42.adapter.enums.ApprovalStatus.CHECKER_APPROVAL_PENDING);
        //            userApplicationRelProcessor.createStagingUserApplicationRelSetting(stagingUserApplicationRelBindings);
        //            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " updateUserApplicationRel : end");
        //            return stagingUserApplicationRelBindings.convertToTO();
        //        }
        //        else {
        //            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " updateUserApplicationRel : end");
        //            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_TWO_FACTOR_STATUS, errorConstant.getERROR_MESSAGE_INVALID_TWO_FACTOR_STATUS);
        //        }
        return stagingUserApplicationRelSettingsTO;
    }

    /**
     * Gets the account id.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     * @param iamExtension the iam extension
     * @return the account id
     */
    private String getAccountId(String attributeName, String attributeValue, IAMExtensionV2 iamExtension) {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " getAccountId : start");
        try {
            AccountWE accountWE = iamExtension.getAccount(attributeName.toUpperCase(), attributeValue);
            return accountWE.getId();
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
        finally {
            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " getAccountId : end");
        }
    }

    /**
     * Initiate user service binding.
     *
     * @param consumerBindingTO the consumer binding TO
     * @param service the service
     * @param application the application
     * @param user the user
     * @param serverId the server id
     * @param isPublicAttributePresent the is public attribute present
     * @throws IAMException the IAM exception
     * @throws AuthException the auth exception
     */
    private void initiateUserServiceBinding(Session session, ConsumerBindingTO consumerBindingTO, Service service, Application application, User user,
            boolean isPublicAttributePresent)
            throws IAMException, AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " initiateUserServiceBinding : start");
        attributeNameToUpperCase(consumerBindingTO.getCustomAttribute());
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
        Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
        String approvalAttemptType = null, encryptedData = null, signTransactionId = null;
        AttributeDataTO publicAttribute = consumerBindingTO.getCustomAttribute();
        if (consumerBindingTO.getUserConsentRequired()){
            if (!isPublicAttributePresent) {
                AttributeMetaDataWE attributeMetaDataWE = iamExtensionService.getAttributeMetadata(publicAttribute.getAttributeName());
                EncryptionDataTO encryptionDataTO = iamExtension.initiateAttributeClaimSelfSigned(application.getApplicationAccountId(), user.getAccountId(),
                        consumerBindingTO.getCustomAttribute().getAttributeName(), consumerBindingTO.getCustomAttribute().getAttributeValue(), null, consumerBindingTO.getTransactionId(),
                        consumerBindingTO.getTransactionSummary(), consumerBindingTO.getTransactionDetails(), token, reqRefNum);
                publicAttribute.setSignTransactionId(encryptionDataTO.getAttributeSignTransactionId());
                signTransactionId = encryptionDataTO.getSignTransactionId();
                encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(publicAttribute), encryptionDataTO.getEncryptionKey());
                publicAttribute.setAttributeType(attributeMetaDataWE.getAttributeType().name());
                if (attributeMetaDataWE.getAttributeType() == in.fortytwo42.enterprise.extension.enums.AttributeType.DERIVED) {
                    publicAttribute.setAttributeRelationId(application.getApplicationAccountId());
                    publicAttribute.setAttributeRelatonEntity(Constant.APPLICATION_ENTITY);
                }
            }
    }
        AuthenticationAttempt authenticationAttempt = null;
        // default should be that - user consent should be sent
        boolean isUserConsentRequired = consumerBindingTO.getUserConsentRequired() == null || consumerBindingTO.getUserConsentRequired();
        if(isUserConsentRequired) {
            ApprovalAttemptTO approvalAttemptTO;
            approvalAttemptTO = createBindingAttempt(consumerBindingTO, service, application, user, isPublicAttributePresent, approvalAttemptType, encryptedData, signTransactionId);
            approvalAttemptTO.setServiceName(service.getServiceName());
            authenticationAttempt = authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO, application);
            authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
            consumerBindingTO.setApprovalStatus(authenticationAttempt.getAttemptStatus());
        }
        else {
            List<ApplicationTO> applicationTOS = new ArrayList<>();
            ApplicationTO applicationTO = new ApplicationTO();
            applicationTO.setApplicationId(application.getApplicationId());
            List<ServiceTO> serviceTOS = new ArrayList<>();
            ServiceTO serviceTO = new ServiceTO();
            serviceTO.setServiceName(consumerBindingTO.getServiceName());
            serviceTOS.add(serviceTO);
            applicationTO.setServices(serviceTOS);
            applicationTOS.add(applicationTO);
            userService.autoBindUserToApplication(session, applicationTOS, user, null);
        }
        if (!isPublicAttributePresent) {
            attributeStoreService.saveAttributeData(session, publicAttribute, authenticationAttempt.getId(), user, true);
        }
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " initiateUserServiceBinding : end");
    }

    /**
     * Creates the binding attempt.
     *
     * @param consumerBindingTO the consumer binding TO
     * @param service the service
     * @param application the application
     * @param user the user
     * @param serverId the server id
     * @param isPublicAttributePresent the is public attribute present
     * @param approvalAttemptType the approval attempt type
     * @param encryptedData the encrypted data
     * @param signTransactionId the sign transaction id
     * @return the approval attempt TO
     * @throws AuthException the auth exception
     * @throws IAMException the IAM exception
     */
    private ApprovalAttemptTO createBindingAttempt(ConsumerBindingTO consumerBindingTO, Service service, Application application, User user, boolean isPublicAttributePresent,
            String approvalAttemptType, String encryptedData, String signTransactionId) throws AuthException, IAMException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " createBindingAttempt : start");
        attributeNameToUpperCase(consumerBindingTO.getCustomAttribute());
        ApprovalAttemptTO approvalAttemptTO;
        if (userApplicationRelService.isApplicationUserBindingPresent(application.getId(), user.getId())) {
            UserApplicationServiceRel userApplicationRel = userApplicationRelService.getUserApplicationRel(user, application, service);
            boolean isServceBindingPresent = userApplicationRelService.isServiceBindingPresent(userApplicationRel);
            if (!isServceBindingPresent && !isPublicAttributePresent) {
                approvalAttemptType = Constant.ATTRIBUTE_ADDITION_WITH_SERVICE_BINDING;
            }
            else if (!isServceBindingPresent) {
                approvalAttemptType = Constant.NORMAL;
            }
            else if (!isPublicAttributePresent) {
                approvalAttemptType = Constant.ATTRIBUTE_ADDITION_WITH_SERVICE_BINDING;
            }
            else {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT());
            }
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, "User binding~", System.currentTimeMillis() + Constant.TILT,
                    consumerBindingTO.getTransactionId(), "~Binding request generation called"));
            approvalAttemptTO = iamExtensionService.createApprovalAttemptOnIAM(user.getAccountId(), consumerBindingTO, application,  approvalAttemptType, encryptedData, signTransactionId);
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, "User binding~", System.currentTimeMillis() + Constant.TILT,
                    consumerBindingTO.getTransactionId(), "~Binding request generated successfully"));
        }
        else {
            if (isPublicAttributePresent) {
                approvalAttemptType = Constant.REGULATORY;
            }
            else {
                approvalAttemptType = Constant.ATTRIBUTE_ADDITION_WITH_SERVICE_BINDING;
            }
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, "User binding~", System.currentTimeMillis() + Constant.TILT,
                    consumerBindingTO.getTransactionId(), "~Binding request generation called"));
            approvalAttemptTO =
                              iamExtensionService.createBindingApprovalAttempt(user.getAccountId(), consumerBindingTO, application, approvalAttemptType, signTransactionId, encryptedData);
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, "User binding~", System.currentTimeMillis() + Constant.TILT,
                    consumerBindingTO.getTransactionId(), "~Binding request generated successfully"));
        }
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " createBindingAttempt : end");
        return approvalAttemptTO;
    }

    /**
     * Validate service.
     *
     * @param application the application
     * @param service the service
     * @throws AuthException the auth exception
     */
    private void validateService(Application application, Service service) throws AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " validateService : start");
        logger.log(Level.DEBUG,application.getServices().get(0).getServiceName());
        logger.log(Level.DEBUG,service.getServiceName());
        if (!application.getServices().contains(service)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SERVICE_FOR_APPLICATION(),
                    errorConstant.getERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION());
        }
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " validateService : end");
    }

    /**
     * Process application wise user update.
     *
     * @param blockUserApplicationTO the block user application TO
     * @param userList the user list
     * @param application the application
     */
    private void processApplicationWiseUserUpdate(BlockUserApplicationTO blockUserApplicationTO, List<User> userList, Application application) {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " processApplicationWiseUserUpdate : start");
        List<String> usernames = new ArrayList<>();
        for (User user : userList) {
            processUserApplicationServiceUpdate(blockUserApplicationTO, application, usernames, user);
        }
        if (!usernames.isEmpty() && !application.getCallbackUrls().isEmpty()) {
            if (ResetPinUserUnblockStatus.APPLICATION_SELF_UNBLOCK == application.getUnblockSettings()) {
                for (CallbackUrl callbackUrl : application.getCallbackUrls()) {
                    if (callbackUrl.getCallbackType().equals(Constant.UPDATE_USER_URL) && !callbackUrl.getUrl().isEmpty()) {
                        sendDataToCallbackUrl(usernames, blockUserApplicationTO.getConsumerId(), callbackUrl.getUrl(),
                                blockUserApplicationTO.getStatus().equals(Constant.BLOCKED) ? BindingStatus.BLOCKED_FOR_RESET_PIN.name() : BindingStatus.RESET_PIN_COMPLETED.name());
                    }
                }
            }
        }
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " processApplicationWiseUserUpdate : end");
    }

    /**
     * Process user application service update.
     *
     * @param blockUserApplicationTO the block user application TO
     * @param application the application
     * @param usernames the usernames
     * @param user the user
     */
    private void processUserApplicationServiceUpdate(BlockUserApplicationTO blockUserApplicationTO, Application application,
            List<String> usernames, User user) {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " processUserApplicationServiceUpdate : start");
        boolean sendUsernameForUpdate = false;
        List<UserApplicationServiceRel> userApplicationRels = userApplicationRelService.getUserApplicationRels(application.getId(), user.getId());
        if (userApplicationRels != null && !userApplicationRels.isEmpty()) {
            List<UserApplicationServiceRel> userApplicationRelsToUpdate = new ArrayList<>();

            for (UserApplicationServiceRel userApplicationRel : userApplicationRels) {
                switch (application.getUnblockSettings()) {
                    case APPLICATION_SELF_UNBLOCK:
                        if (blockUserApplicationTO.getStatus().equals(Constant.BLOCKED)) {
                            if (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE || userApplicationRel.getBindingStatus() == BindingStatus.RESET_PIN_COMPLETED) {
                                userApplicationRel.setBindingStatus(BindingStatus.BLOCKED_FOR_RESET_PIN);
                                userApplicationRelsToUpdate.add(userApplicationRel);
                                sendUsernameForUpdate = true;
                            }
                        }
                        else if (blockUserApplicationTO.getStatus().equals(Constant.ACTIVE) && (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED
                                                                                                || userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED_FOR_RESET_PIN
                                                                                                || userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE)) {
                            userApplicationRel.setBindingStatus(BindingStatus.RESET_PIN_COMPLETED);
                            userApplicationRelsToUpdate.add(userApplicationRel);
                            sendUsernameForUpdate = true;
                        }
                        break;
                    case AUTO_UNBLOCK:
                        // TODO
                        break;
                    case APPLICATION_CUSTOMER_SERVICE_UNBLOCK:
                        if (blockUserApplicationTO.getStatus().equals(Constant.BLOCKED)) {
                            if (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE) {
                                userApplicationRel.setBindingStatus(BindingStatus.BLOCKED);
                                userApplicationRelsToUpdate.add(userApplicationRel);
                            }
                        }
                        break;
                    default:
                        break;
                }

            }

            if (!userApplicationRelsToUpdate.isEmpty()) {
                Session session = sessionFactoryUtil.getSession();
                try {
                    userService.bulkUpdateUserApplicationRel(session, userApplicationRelsToUpdate);
                    sessionFactoryUtil.closeSession(session);
                }
                catch (Exception e) {
                    session.getTransaction().rollback();
                    throw e;
                }
                finally {
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            }
            if (sendUsernameForUpdate) {
                usernames.add(userService.getUsername(user.getId()));
            }
        }
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " processUserApplicationServiceUpdate : end");
    }

    /**
     * Send data to callback url.
     *
     * @param usernames the usernames
     * @param consumerId the consumer id
     * @param callbackUrl the callback url
     * @param bindingStatus the binding status
     */
    private void sendDataToCallbackUrl(List<String> usernames, String consumerId, String callbackUrl, String bindingStatus) {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " sendDataToCallbackUrl : start");
        UserBindingResponseTO userBindingRequestObdx = new UserBindingResponseTO();
        userBindingRequestObdx.setUsernames(usernames);
        userBindingRequestObdx.setConsumerId(consumerId);
        userBindingRequestObdx.setUserStatus(bindingStatus);
        WebTarget webTargetCloud;
        Client client;
        ClientConfig config = new ClientConfig();
        config.register(GsonProvider.class);
        //config.register(new LoggingFeature());
        client = ClientBuilder.newClient(config);
        webTargetCloud = client.target(callbackUrl);// OBDX callbackURl
        Invocation.Builder invocationBuilder = webTargetCloud.request(MediaType.APPLICATION_JSON);
        try {
            logger.log(Level.INFO, "Sending data " + callbackUrl + " " + new Gson().toJson(userBindingRequestObdx));
            UserBindingResponseTO userBindingResponseTO = invocationBuilder.post(Entity.entity(userBindingRequestObdx, MediaType.APPLICATION_JSON), UserBindingResponseTO.class);
            logger.log(Level.INFO, "Response received " + new Gson().toJson(userBindingResponseTO));
        }
        catch (WebApplicationException e) {
            logger.log(Level.ERROR, e);
        }
        finally {
            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " sendDataToCallbackUrl : end");
        }
    }

    /**
     * Complete consumer binding.
     *
     * @param authenticationAttempt the authentication attempt
     * @return the consumer binding TO
     * @throws AuthException the auth exception
     */
    @Override
    public ConsumerBindingTO completeConsumerBinding(Session session, AuthenticationAttempt authenticationAttempt) throws AuthException {
        logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " completeConsumerBinding : start");
        try {
            Application application = applicationService.getApplicationByApplicationAccountId(authenticationAttempt.getSenderAccountId());
            // AuthenticationAttempt authenticationAttempt = authenticationAttemptDaoIntf.getAuthAttemptBySenderAccountIdAndTransactionId(application.getApplicationAccountId(), transactionId);

            AuthenticationAttemptHistory authenticationAttemptHistory =
                                                                      authenticationAttemptHistoryDao.getAuthAttemptByApplicationIdAndTransactionId(authenticationAttempt.getSenderAccountId(),
                                                                              authenticationAttempt.getTransactionId());
            User user = userService.getActiveUser(session, authenticationAttemptHistory.getReceiverAccountId());
            Service service;
            try {
                service = serviceProcessor.getServiceByServiceName(authenticationAttemptHistory.getServiceName());
            }
            catch (ServiceNotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
            }
            UserApplicationServiceRel userApplicationRel = userApplicationRelService.getUserApplicationRel(user, application, service);
            ConsumerBindingTO consumerBindingTO = bindingAsyncResponseHandler.getConsumerBindingTO(authenticationAttempt.getTransactionId(), authenticationAttempt.getSenderAccountId());
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                       + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, authenticationAttempt.getSenderAccountId(), Constant.TILT, consumerBindingTO.getTransactionId(),
                    "~approval status is " + authenticationAttempt.getAttemptStatus()));
            Boolean activateEncryption = bindingAsyncResponseHandler.isActivateEncryption(authenticationAttempt.getTransactionId(), authenticationAttempt.getSenderAccountId());

            if (!authenticationAttempt.getAttemptStatus().equals(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.APPROVED.name())) {
                consumerBindingTO.setStatus(Constant.FAILURE_STATUS);
                consumerBindingTO.setApprovalStatus(authenticationAttempt.getAttemptStatus());
            }
            else {
                //TODO: update attribute store status if present for auth attempt id.
                //TODO: check on Approval attempt type.
                if (!userApplicationRelService.isApplicationUserBindingPresent(application.getId(), user.getId())) {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                               + Constant.TILT,
                            USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), Constant.TILT, authenticationAttempt.getTransactionId(),
                            "~complete user binding called on I-AM server"));
                    CamUtil.onboardUserAndBind(application,user,null, null);
                    iamExtensionService.completeUserBinding(application, user.getAccountId(), authenticationAttempt.getTransactionId(), authenticationAttemptHistory.getServiceName());
                }
                if (userApplicationRel != null) {
                    userApplicationRelService.updateUserApplicationRel(session, userApplicationRel, BindingStatus.ACTIVE);
                }
                else {
                    userApplicationRelService.createUserApplicationRel(session, user, application, service, BindingStatus.ACTIVE);
                }
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                           + Constant.TILT,
                        USER_BINDING, System.currentTimeMillis() + Constant.TILT, authenticationAttempt.getSenderAccountId(), Constant.TILT, consumerBindingTO.getTransactionId(),
                        "~User service rel updated with status ACTIVE"));
                if (user.getIamStatus() == IAMStatus.DISABLED) {
                    user.setIamStatus(IAMStatus.ENABLED);
                    userDao.update(session, user);
                }

                if (activateEncryption != null && activateEncryption) {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                               + Constant.TILT,
                            USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), Constant.TILT, authenticationAttempt.getTransactionId(),
                            "~activate encryption called"));
                    String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                    IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
                    Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));

                    iamExtension.activateEncryptionForConsumer(token, user.getAccountId(), reqRefNum);
                }
                if(Constant.AUTH42.equals(service.getServiceName())) {
                    userService.enableToken(user.getAccountId(), application, true);
                }
                consumerBindingTO.setStatus(Constant.SUCCESS_STATUS);
                consumerBindingTO.setApprovalStatus(authenticationAttempt.getAttemptStatus());
            }
            return consumerBindingTO;
        }
        catch (TransactionNotFoundException e) {
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                       + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, authenticationAttempt.getSenderAccountId(), Constant.TILT, authenticationAttempt.getTransactionId(),
                    "~transaction not found"));
            throw new AuthException(null, errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                       + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, authenticationAttempt.getSenderAccountId(), Constant.TILT, authenticationAttempt.getTransactionId(),
                    "~I-AM exception thrown"));
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            logger.log(Level.DEBUG, NONAD_USER_FACADE_IMPL_LOG + " completeConsumerBinding : end");
        }
    }

    @Override
    public UserStatusTO updateUserStatus(UserStatusTO userStatusTO, String role, String actor) throws AuthException {
        /*PermissionProcessorIntf permissionProcessorIntf = ProcessorFactory.getPermissionProcessor();
        if (permissionProcessorIntf.isPermissionValidForRole(RequestSubType.USER_STATUS_UPDATE.name(), role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }*/
        AccountWE account = iamExtensionService.getAllAttributesForAccount(userStatusTO.getAccountId());
        logger.log(Level.INFO, "account : " + new Gson().toJson(account));
        if (account.getState().equals(userStatusTO.getState())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
        }
        List<Request> requests = requestDao.getRequestsBySubType(RequestSubType.USER_STATUS_UPDATE, ApprovalStatus.CHECKER_APPROVAL_PENDING,
                ApprovalStatus.USER_APPROVAL_PENDING);
        if (requests != null && !requests.isEmpty()) {
            for (Request request : requests) {
                UserStatusTO userStatusRequestTO = new Gson().fromJson(request.getRequestJSON(), UserStatusTO.class);
                if (userStatusRequestTO.getAccountId().equals(userStatusTO.getAccountId())) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT());
                }
            }
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            userStatusTO = userService.createUserStatusUpdateRequest(session, userStatusTO, actor);
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return userStatusTO;
    }

    @Override
    public UserStatusTO approveUserStatusUpdateRequest(UserStatusTO userStatusTO, String role, String actor) throws AuthException {
        /*PermissionProcessorIntf permissionProcessorIntf = ProcessorFactory.getPermissionProcessor();
        if (permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.APPROVE_USER_STATUS_UPDATE, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }*/
        Request request = null;
        try {
            request = requestDao.getById(userStatusTO.getId());
        }
        catch (NotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(), errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            userStatusTO = userService.approveUserStatusUpdateRequest(session, request, userStatusTO);
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return userStatusTO;
    }

    @Override
    public TunnelingApplicationTO checkSubscription(String applicationId, TunnelingApplicationTO tunnelingApplicationTO) throws AuthException {
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        Service service = getService(SRA);
        validateService(application, service);
        User user = getUser(tunnelingApplicationTO.getConsumerId());
        UserApplicationServiceRel userApplicationRel = getUserApplicationRel(user, application, service);
        String status = checkUserApplicationRel(userApplicationRel);
        tunnelingApplicationTO.setStatus(status);
        return tunnelingApplicationTO;
    }

    private String checkUserApplicationRel(UserApplicationServiceRel userApplicationRel) throws AuthException {
        if (userApplicationRel != null) {
            if (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE || userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED
                || userApplicationRel.getBindingStatus() == BindingStatus.RESET_PIN_COMPLETED
                || userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED_FOR_RESET_PIN) {
                return Constant.SUCCESS_STATUS;
            }
            else {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND());
            }
        }
        return Constant.FAILURE_STATUS;
    }

    private UserApplicationServiceRel getUserApplicationRel(User user, Application application, Service service) {
        UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
        userApplicationCompositeKey.setApplication(application);
        userApplicationCompositeKey.setUser(user);
        userApplicationCompositeKey.setService(service);
        UserApplicationServiceRel userApplicationRel = null;
        try {
            userApplicationRel = userApplicationRelDao.getUserApplicationForId(userApplicationCompositeKey);
            return userApplicationRel;
        }
        catch (UserApplicationRelNotFoundException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
        return null;
    }

    private Service getService(String serviceName) throws AuthException {
        try {
            return serviceProcessor.getServiceByServiceName(serviceName);
        }
        catch (ServiceNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
        }
    }

    private User getUser(String mobile) throws AuthException {
        try {
            return userDao.getNonADUser(mobile);
        }
        catch (UserNotFoundException e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
    }

    public void validAttributeValues(String attributeName, String attributeValues) throws AuthException {
        AttributeMetadataTO attributeMetadataTO = iamExtensionService.getAttributeMetadataForAttributeName(attributeName.toUpperCase());
        if (attributeMetadataTO != null) {
            String attributeValueRegex = (String) attributeMetadataTO.getAttributeSettings().get(Constant.VALIDATION_REGEX);
            if (attributeValueRegex != null) {
                boolean isAttributeValueRegex = Pattern.matches(attributeValueRegex, attributeValues);
                if (!isAttributeValueRegex) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE());
                }
            }
            if(!attributeMetadataTO.getIsUnique()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SEARCH_ATTRIBUTE(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE()+": "+attributeMetadataTO.getAttributeName());
            }
        }
    }

    @Override
    public void unbindServicesFromUser(UserBindingTO userBindingTO, String role, String actor,Long id, boolean saveRequest) throws AuthException, UserBlockedException {

        Session session = sessionFactoryUtil.getSession();
        try {
            logger.log(Level.DEBUG, userBindingTO.getId()+" **UserbindingTO id "+ nonADUserService);
            nonADUserService.unbindServicesFromUser(session, userBindingTO, role, actor,id, saveRequest, false);
            sessionFactoryUtil.closeSession(session);
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    private void attributeNameToUpperCase(AttributeDataTO attributeDataTO) {
        if (attributeDataTO.getAttributeName() != null) {
            attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
        }
    }

    private void attributeNameToUpperCase(ConsumerTO consumerTO) {
        if (consumerTO.getAttributeName() != null) {
            consumerTO.setAttributeName(consumerTO.getAttributeName().toUpperCase());
        }
    }

    private void attributeNameToUpperCase(ConsumerBindingTO consumerBindingTO) {
        if (consumerBindingTO.getCustomAttribute() != null) {
            consumerBindingTO.getCustomAttribute().setAttributeName(consumerBindingTO.getCustomAttribute().getAttributeName().toUpperCase());
        }
        if (consumerBindingTO.getSearchAttributes() != null) {
            for (AttributeDataTO attributeDataTO : consumerBindingTO.getSearchAttributes()) {
                if (attributeDataTO.getAttributeName() != null) {
                    attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
                }
            }
        }
    }
}
