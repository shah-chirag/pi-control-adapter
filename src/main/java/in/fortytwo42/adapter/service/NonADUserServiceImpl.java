
package in.fortytwo42.adapter.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.glassfish.jersey.client.ClientConfig;
//import org.glassfish.jersey.logging.LoggingFeature;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.cam.facade.CamUserFacadeImpl;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeIntf;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.transferobj.BlockUserApplicationTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.ConsumerTO;
import in.fortytwo42.adapter.transferobj.UserBindingResponseTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AuditLogConstant;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.handler.BindingAsyncResponseHandler;
import in.fortytwo42.daos.dao.ApplicationDaoIntf;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.AuthenticationAttemptDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.ServiceDaoIntf;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.exception.TransactionNotFoundException;
import in.fortytwo42.daos.exception.UserApplicationRelNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.enterprise.extension.core.ApprovalAttemptV2;
import in.fortytwo42.enterprise.extension.core.BindingInfoV2;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.ApprovalStatus;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.exceptions.IAMWebException;
import in.fortytwo42.enterprise.extension.utils.GsonProvider;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.CallbackUrl;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceCompositeKey;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.enums.ApplicationType;
import in.fortytwo42.entities.enums.IAMStatus;
import in.fortytwo42.entities.enums.ResetPinUserUnblockStatus;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

public class NonADUserServiceImpl implements NonADUserServiceIntf {

    private static final String I_AM_EXCEPTION_IS_THROWN = "~I-AM exception is thrown";
    private static final String USER_IS_CREATED = "~user is created";
    private static final String USER_BINDING = "User binding~";
    private static final String CUST_ID = "CUST_ID";
    private static final String MOBILE_NO = "MOBILE_NO";
    private static final String BIND = "BIND";
    private static final String UNBIND = "UNBIND";
    private static Logger logger= LogManager.getLogger(NonADUserServiceImpl.class);

    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    private UserApplicationRelDaoIntf userApplicationRelDao = DaoFactory.getUserApplicationRel();
    private ServiceDaoIntf serviceDao = DaoFactory.getServiceDao();
    private UserDaoIntf userDao = DaoFactory.getUserDao();
    private ApplicationDaoIntf applicationDao = DaoFactory.getApplicationDao();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private AuthenticationAttemptDaoIntf authenticationAttemptDao = DaoFactory.getAuthenticationAttemptDao();

    //TODO: Service to service
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();


    private BindingAsyncResponseHandler bindingAsyncResponseHandler = BindingAsyncResponseHandler.getInstance();
    private Config config = Config.getInstance();
    private IAMUtil iamUtil = IAMUtil.getInstance();
    private PermissionUtil permissionUtil = PermissionUtil.getInstance();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private CamUserFacadeIntf camUserFacade = CamUserFacadeImpl.getInstance();

    private NonADUserServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final NonADUserServiceImpl INSTANCE = new NonADUserServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static NonADUserServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void initConsumerBinding(Session session, ConsumerBindingTO consumerBindingTO, String applicationId, String serviceName, String serverId, AsyncResponse asyncResponse)
            throws AuthException, UserBlockedException {
        try {
            consumerBindingTO.setUsername(consumerBindingTO.getUsername().trim());
            consumerBindingTO.setConsumerId(consumerBindingTO.getConsumerId().trim());
            User user = null;
            Application application = null;
            try {
                application = applicationDao.getApplicationByApplicationId(applicationId);
                validateApplicationForUser(application);
                user = getUser(consumerBindingTO.getUsername(), consumerBindingTO.getConsumerId());
            }
            catch (AuthException e) {
            }
            catch (ApplicationNotFoundException e) {
                logger.log(Level.ERROR, e);
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
            }
            if (user != null) {
                validateUser(user);
            }
            Service service = getService(serviceName);
            validateService(application, service);
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            Integer timeout = consumerBindingTO.getTimeOut();
            if (timeout == null) {
                timeout = application.getTransactionTimeout();
            }
            UserApplicationServiceRel userApplicationRel = null;
            if (user != null) {
                userApplicationRel = getUserApplicationRel(user, application, service);
                validateUserApplicationRel(userApplicationRel);
            }
            else {
                user = createUser(consumerBindingTO);
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT,
                        USER_BINDING, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), USER_IS_CREATED));
            }
            initiateUserServiceBinding(session, consumerBindingTO, serviceName, application, user, iamExtension, token, timeout, serverId);
            if (userApplicationRel == null) {
                createUserApplicationRel(session, user, application, service, BindingStatus.PENDING);
            }
            Boolean activateEncryption = consumerBindingTO.getActivateEncryption();
            if (activateEncryption == null) {
                activateEncryption = false;
            }
            bindingAsyncResponseHandler.storeAsyncResponseReference(consumerBindingTO.getTransactionId(), applicationId, asyncResponse, getUsername(user.getId()), activateEncryption);
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                      + Constant.TILT,
                    "User binding~", System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), I_AM_EXCEPTION_IS_THROWN));
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        catch (ServiceNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());

        }
    }

    private void initiateUserServiceBinding(Session session, ConsumerBindingTO consumerBindingTO, String serviceName, Application application, User user, IAMExtensionV2 iamExtension, Token token,
            Integer timeout,
            String serverId)
            throws IAMException, ServiceNotFoundException {
        if (isMobileApplicationBindingPresent(consumerBindingTO.getConsumerId(), application)) {
            createApprovalAttempt(iamExtension, token, consumerBindingTO, timeout, serviceName, serverId);
            saveAuthAttemptToDb(session, consumerBindingTO, user, application, serviceName, timeout, Constant.NORMAL);
        }
        else {
            createBindingTransactionForUser(iamExtension, token, consumerBindingTO, timeout, serviceName, serverId);
            saveAuthAttemptToDb(session, consumerBindingTO, user, application, serviceName, timeout, Constant.REGULATORY);
        }
    }

    private boolean isMobileApplicationBindingPresent(String mobile, Application application) {
        return userApplicationRelDao.getMobileAndApplicationRelCount(application.getId(), mobile) > 0;
    }

    private void createApprovalAttempt(IAMExtensionV2 iamExtension, Token token, ConsumerBindingTO consumerBindingTO, Integer timeout, String serviceName, String serverId) throws IAMException {
        ApprovalAttemptV2.Builder attemptBuilder = new ApprovalAttemptV2.Builder();
        attemptBuilder.consumerAccountId(consumerBindingTO.getConsumerId());
        attemptBuilder.timeOut(timeout);
        attemptBuilder.transactionDetails(consumerBindingTO.getTransactionDetails());
        attemptBuilder.transactionId(consumerBindingTO.getTransactionId());
        attemptBuilder.transactionSummary(consumerBindingTO.getTransactionSummary());
        attemptBuilder.approvalAttemptType(Constant.NORMAL);
        attemptBuilder.serviceName(serviceName);
        attemptBuilder.isAuthenticationRequired(true);
        attemptBuilder.service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL);
        attemptBuilder.serverId(serverId);
        ApprovalAttemptV2 approvalAttempt = attemptBuilder.build();
        logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                  + Constant.TILT,
                "User binding~", System.currentTimeMillis() + Constant.TILT, consumerBindingTO.getTransactionId(), "~Binding request generation called"));
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
        logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                  + Constant.TILT,
                "User binding~", System.currentTimeMillis() + Constant.TILT, consumerBindingTO.getTransactionId(), "~Binding request generated successfully"));
    }

    private void validateUserApplicationRel(UserApplicationServiceRel userApplicationRel) throws AuthException {
        if (userApplicationRel != null) {
            if (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE || userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED
                || userApplicationRel.getBindingStatus() == BindingStatus.RESET_PIN_COMPLETED
                || userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED_FOR_RESET_PIN) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT());
            }
        }
    }

    private void createBindingTransactionForUser(IAMExtensionV2 iamExtension, Token token, ConsumerBindingTO consumerBindingTO, Integer timeout, String serviceName, String serverId)
            throws IAMException {
        BindingInfoV2.Builder builder = new BindingInfoV2.Builder();
        builder.consumerAccountId(consumerBindingTO.getConsumerId());
        builder.timeOut(timeout);
        builder.transactionDetails(consumerBindingTO.getTransactionDetails());
        builder.transactionSummary(consumerBindingTO.getTransactionSummary());
        builder.transactionId(consumerBindingTO.getTransactionId());
        builder.serviceName(serviceName);
        builder.serverId(serverId);
        BindingInfoV2 consumerRegistrationInfo = builder.build();
        iamExtension.initConsumerApplicationBinding(token, consumerRegistrationInfo);
    }

    private void saveAuthAttemptToDb(Session session, ConsumerBindingTO consumerBindingTO, User user, Application application, String serviceName, int timeout, String approvalAttemptType)
            throws ServiceNotFoundException {
        AuthenticationAttempt authenticationAttempt = new AuthenticationAttempt();
        authenticationAttempt.setTransactionId(consumerBindingTO.getTransactionId());
        authenticationAttempt.setTransactionDetails(consumerBindingTO.getTransactionDetails());
        authenticationAttempt.setTransactionSummary(consumerBindingTO.getTransactionSummary());
        authenticationAttempt.setAttemptType(approvalAttemptType);
        authenticationAttempt.setIsPinCheckRequired(true);
        authenticationAttempt.setSenderAccountId(application.getApplicationAccountId());
        authenticationAttempt.setTimeout(Long.valueOf(timeout));
        authenticationAttempt.setAttemptStatus(ApprovalStatus.PENDING.name());
        authenticationAttempt.setService(serviceDao.getServiceByServiceName(serviceName));
        authenticationAttempt.setUser(user);
        authenticationAttemptDao.create(session, authenticationAttempt);
        logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                  + Constant.TILT,
                USER_BINDING, System.currentTimeMillis() + Constant.TILT, consumerBindingTO.getTransactionId(), "~Binding request saved db"));
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
            logger.log(Level.INFO, e);
        }
        return null;
    }

    private void createUserApplicationRel(Session session, User user, Application application, Service service, BindingStatus bindingStatus) {
        UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
        userApplicationCompositeKey.setApplication(application);
        userApplicationCompositeKey.setUser(user);
        userApplicationCompositeKey.setService(service);
        UserApplicationServiceRel userApplicationRel = new UserApplicationServiceRel();
        userApplicationRel.setId(userApplicationCompositeKey);
        userApplicationRel.setBindingStatus(bindingStatus);
        userApplicationRel.setTwoFactorStatus(TwoFactorStatus.ENABLED);
        userApplicationRelDao.create(session, userApplicationRel);
        logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                  + Constant.TILT,
                USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), "~User service binding created with status pending"));
    }

    private void updateUserApplicationRel(Session session, UserApplicationServiceRel userApplicationRel, BindingStatus bindingStatus) {
        userApplicationRel.setBindingStatus(bindingStatus);
        userApplicationRelDao.update(session, userApplicationRel);
    }

    private User createUser(ConsumerBindingTO consumerBindingTO) {
        UserDaoIntf userDaoIntf = userDao;
        User user = new User();
        //        user.setMobile(consumerBindingTO.getConsumerId());
        //        user.setUsername(consumerBindingTO.getUsername());
        //        boolean isFirstNamePresent = consumerBindingTO.getFirstName() != null && !consumerBindingTO.getFirstName().trim().isEmpty();
        //        boolean isLastNamePresent = consumerBindingTO.getLastName() != null && !consumerBindingTO.getLastName().trim().isEmpty();
        //        if (isFirstNamePresent || isLastNamePresent) {
        //            String fullname = "";
        //            if (isFirstNamePresent) {
        //                String firstName = consumerBindingTO.getFirstName().trim();
        //                fullname = firstName + Constant._SPACE;
        //                user.setFirstName(firstName);
        //            }
        //            if (isLastNamePresent) {
        //                String lastName = consumerBindingTO.getLastName();
        //                fullname += lastName.trim();
        //                user.setLastName(lastName);
        //            }
        //            user.setFullName(fullname);
        //        }
        //        user.setEmail(consumerBindingTO.getEmail());
        //        user.setLocation(consumerBindingTO.getLocation());
        //        TwoFactorStatus twoFactorStatus = TwoFactorStatus.ENABLED;
        //        try {
        //            TwoFactorStatus updatedTwoFactorStatus = TwoFactorStatus.valueOf(consumerBindingTO.getTwoFactorStatus());
        //            if (consumerBindingTO.getTwoFactorStatus() != null && updatedTwoFactorStatus != null) {
        //                twoFactorStatus = updatedTwoFactorStatus;
        //            }
        //        }
        //        catch (Exception e) {
        //            logger.log(Level.INFO, e);
        //        }
        //        user.setTwoFactorStatus(twoFactorStatus);
        //        user.setUserStatus(UserStatus.ACTIVE);
        //        user.setIamStatus(IAMStatus.DISABLED);
        //        user.setUserType(UserType.USER);
        //        user = userDaoIntf.create(user);
        return user;
    }

    @Override
    public ConsumerBindingTO completeConsumerBinding(Session session, String applicationId, String transactionId, String approvalStatus) throws AuthException, UserBlockedException {
        try {
            AuthenticationAttempt authenticationAttempt = authenticationAttemptDao.getAuthAttemptByApplicationIdAndTransactionId(applicationId, transactionId);
            Application application = applicationDao.getApplicationByApplicationId(applicationId);
            User user = authenticationAttempt.getUser();
            Service service = authenticationAttempt.getService();
            UserApplicationServiceRel userApplicationRel = getUserApplicationRel(user, application, service);
            ConsumerBindingTO consumerBindingTO = new ConsumerBindingTO();
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                      + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), "~approval status is " + approvalStatus));
            Boolean activateEncryption = bindingAsyncResponseHandler.isActivateEncryption(transactionId, applicationId);
            if (!approvalStatus.equals(ApprovalStatus.APPROVED.name())) {
                consumerBindingTO.setStatus(Constant.FAILURE_STATUS);
            }
            else {
                String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
                Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
                if (authenticationAttempt.getAttemptType().equals(Constant.REGULATORY)) {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                              + Constant.TILT,
                            USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), Constant.TILT, transactionId, "~complete user binding called on I-AM server"));
                    completeUserBinding(iamExtension, token, authenticationAttempt.getSenderAccountId(), transactionId, authenticationAttempt.getService().getServiceName());
                }
                if (userApplicationRel != null) {
                    updateUserApplicationRel(session, userApplicationRel, BindingStatus.ACTIVE);
                }
                else {
                    createUserApplicationRel(session, user, application, service, BindingStatus.ACTIVE);
                }
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                          + Constant.TILT,
                        USER_BINDING, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), "~User service rel updated with status ACTIVE"));

                user.setIamStatus(IAMStatus.ENABLED);
                userDao.update(session, user);

                if (activateEncryption != null && activateEncryption) {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                              + Constant.TILT,
                            USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), Constant.TILT, transactionId, "~activate encryption called"));
                    iamExtension.activateEncryptionForConsumer(token, getMobile(authenticationAttempt.getUser().getId()), reqRefNum);
                }
                consumerBindingTO.setStatus(Constant.SUCCESS_STATUS);
            }
            consumerBindingTO.setActivateEncryption(activateEncryption);
            consumerBindingTO.setTwoFactorStatus(user.getTwoFactorStatus().name());
            consumerBindingTO.setEmail(getEmail(user.getId()));
            consumerBindingTO.setTransactionId(transactionId);
            consumerBindingTO.setTransactionDetails(authenticationAttempt.getTransactionDetails());
            consumerBindingTO.setTransactionSummary(authenticationAttempt.getTransactionSummary());
            consumerBindingTO.setConsumerId(getMobile(user.getId()));
            consumerBindingTO.setUsername(getUsername(user.getId()));
            consumerBindingTO.setTimeOut(authenticationAttempt.getTimeout().intValue());
            consumerBindingTO.setApprovalStatus(approvalStatus);
            return consumerBindingTO;
        }
        catch (TransactionNotFoundException e) {
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                      + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, transactionId, "~transaction not found"));
            throw new AuthException(null, errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                      + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, transactionId, "~I-AM exception thrown"));
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        catch (ApplicationNotFoundException e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
    }

    private void completeUserBinding(IAMExtensionV2 iamExtension, Token token, String mobile, String transactionId, String serviceName) throws IAMException {
        BindingInfoV2.Builder builder = new BindingInfoV2.Builder();
        builder.consumerAccountId(mobile);
        builder.transactionId(transactionId);
        builder.serviceName(serviceName);
        BindingInfoV2 consumerRegistrationInfo = builder.build();
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        iamExtension.completeConsumerApplicationBinding(token, consumerRegistrationInfo, reqRefNum);
    }

    @Override
    public boolean unbindConsumer(Session session, ConsumerBindingTO consumerBindingTO, String clientId, String serviceName, String version) throws AuthException, UserBlockedException {
        try {
            consumerBindingTO.setUsername(consumerBindingTO.getUsername().trim());
            Application application = applicationDao.getApplicationByApplicationId(clientId);
            validateApplicationForUser(application);
            User user;
            if (Constant.VERSION_V2.equals(version)) {
                return unbindCNBUsers(session, consumerBindingTO, application, serviceName);
            }
            else {
                consumerBindingTO.setConsumerId(consumerBindingTO.getConsumerId().trim());
                user = getUser(consumerBindingTO.getUsername(), consumerBindingTO.getConsumerId());
            }
            validateUser(user);
            Service service = getService(serviceName);
            validateService(application, service);
            UserApplicationServiceRel userApplicationRel = getUserApplicationRel(user, application, service);
            if (userApplicationRel == null || (userApplicationRel.getBindingStatus() == BindingStatus.PENDING || userApplicationRel.getBindingStatus() == BindingStatus.INACTIVE)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND());
            }
            boolean isConsumerUnbind = false;
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            boolean isSingleMobileBindingPresent = userApplicationRelDao.getMobileAndApplicationRelCount(application.getId(), getMobile(user.getId())) == 1;
            if (isSingleMobileBindingPresent) {
                isConsumerUnbind = iamExtension.unbindConsumerApplication(token, getMobile(user.getId()));
            }
            else {
                isConsumerUnbind = true;
            }
            if (isConsumerUnbind) {
                userApplicationRel.setBindingStatus(BindingStatus.INACTIVE);
                userApplicationRelDao.update(session, userApplicationRel);
                iamExtension.forceTimeoutApprovalAttemptsByLookupId(token, getUsername(user.getId()) + "|" + getMobile(user.getId()), serviceName);
                List<UserApplicationServiceRel> userApplicationRels = userApplicationRelDao.getApplicationRelsForUser(user.getId());
                if (userApplicationRels == null || userApplicationRels.isEmpty()) {
                    user.setIamStatus(IAMStatus.DISABLED);
                    userDao.update(session, user);
                }
            }
            return isConsumerUnbind;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        catch (ApplicationNotFoundException e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
    }

    public boolean unbindCNBUsers(Session session, ConsumerBindingTO consumerBindingTO, Application application, String serviceName) throws AuthException {
        try {
            boolean isConsumerUnbind = false;
            List<UserApplicationServiceRel> usernameApplicationRelsList = userApplicationRelDao.getUsernameApplicationRel(application.getId(), consumerBindingTO.getUsername());
            if (usernameApplicationRelsList.isEmpty()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND());
            }

            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));

            for (UserApplicationServiceRel userApplicationRel : usernameApplicationRelsList) {
                if (userApplicationRel == null || (userApplicationRel.getBindingStatus() == BindingStatus.PENDING || userApplicationRel.getBindingStatus() == BindingStatus.INACTIVE)) {
                    continue;
                }
                User user = userApplicationRel.getId().getUser();
                boolean isSingleMobileBindingPresent = userApplicationRelDao.getMobileAndApplicationRelCount(application.getId(), getMobile(user.getId())) == 1;
                if (isSingleMobileBindingPresent) {
                    isConsumerUnbind = iamExtension.unbindConsumerApplication(token, getMobile(user.getId()));
                }
                else {
                    isConsumerUnbind = true;
                }
                if (isConsumerUnbind) {
                    userApplicationRel.setBindingStatus(BindingStatus.INACTIVE);
                    userApplicationRelDao.update(session, userApplicationRel);
                    iamExtension.forceTimeoutApprovalAttemptsByLookupId(token, getUsername(user.getId()) + "|" + getMobile(user.getId()), userApplicationRel.getId().getService().getServiceName());
                    List<UserApplicationServiceRel> userApplicationRels = userApplicationRelDao.getApplicationRelsForUser(user.getId());
                    if (userApplicationRels == null || userApplicationRels.isEmpty()) {
                        user.setIamStatus(IAMStatus.DISABLED);
                        userDao.update(session, user);
                    }
                }
            }
            return isConsumerUnbind;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public ConsumerTO getConsumerStatus(String username, String consumerId, String clientId, String serviceName) throws AuthException {
        ConsumerTO consumerBindingTO = new ConsumerTO();
        consumerBindingTO.setConsumerId(consumerId.trim());
        consumerBindingTO.setUsername(username.trim());
        Application application = null;
        try {
            application = applicationDao.getApplicationByApplicationId(clientId);
            if (application.getApplicationType() == ApplicationType.AD) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_TYPE());
            }
            if (application.getTwoFactorStatus() == TwoFactorStatus.DISABLED) {
                consumerBindingTO.setStatusCode(Constant.APPLICATION_INACTIVE);
                return consumerBindingTO;
            }
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            boolean isConsumerActive = iamExtension.isConsumerActive(token, consumerId.trim());
            if (!isConsumerActive) {
                consumerBindingTO.setStatusCode(Constant.CONSUMER_NOT_REGISTERED);
                return consumerBindingTO;
            }
        }
        catch (IAMException e) {
            logger.log(Level.INFO, e);
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT,
                    USER_BINDING, System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), I_AM_EXCEPTION_IS_THROWN));
            consumerBindingTO.setStatusCode(Constant.CONSUMER_NOT_REGISTERED);
            return consumerBindingTO;
        } 
        catch (ApplicationNotFoundException e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        
        User user = null;
        try {
            user = getUser(username.trim(), consumerId.trim());
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

        Service service = null;
        try {
            service = serviceDao.getServiceByServiceName(serviceName);
        }
        catch (ServiceNotFoundException e) {
            logger.log(Level.INFO, e);
        }
        if (service == null) {
            consumerBindingTO.setStatusCode(Constant.SERVICE_INACTIVE);
            return consumerBindingTO;
        }
        else if (!application.getServices().contains(service)) {
            consumerBindingTO.setStatusCode(Constant.SERVICE_APPLICATION_BINDING_INACTIVE);
            return consumerBindingTO;
        }

        UserApplicationServiceRel userApplicationRel = getUserApplicationRel(user, application, service);
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

    private void validateApplicationForUser(Application application) throws AuthException {
        if (!ApplicationType.NON_AD.equals(application.getApplicationType())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_FOR_USER(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_FOR_USER());
        }
    }

    private void validateUser(User user) throws AuthException {
        if (user.getUserStatus() == UserStatus.BLOCK) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_BLOCK(), errorConstant.getERROR_MESSAGE_USER_BLOCK());
        }
    }

    private void validateService(Application application, Service service) throws AuthException {
        if (!application.getServices().contains(service)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SERVICE_FOR_APPLICATION(), errorConstant.getERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION());
        }
    }

    private Service getService(String serviceName) throws AuthException {
        try {
            return serviceDao.getServiceByServiceName(serviceName);
        }
        catch (ServiceNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
        }
    }

    private User getUser(String username, String mobile) throws AuthException {
        try {
            return userDao.getActiveNonADUser(username, mobile);
        }
        catch (UserNotFoundException e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
    }

    @Override
    public void bindServicesToUser(Session session, UserBindingTO userBindingTO, String role, String actor) throws AuthException, UserBlockedException {
        //        UserDaoIntf userDaoIntf = userDao;
        //        User user;
        //        try {
        //            user = userDaoIntf.getActiveUserById(userBindingTO.getId());
        //        }
        //        catch (UserNotFoundException e) {
        //            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND, errorConstant.getERROR_MESSAGE_USER_NOT_FOUND);
        //        }
        //        PermissionUtil.getInstance().validateEditUserPermission(user.getUserType(), role);
        //        if (!getUsername(user.getId()).equals(userBindingTO.getUsername()) || !getMobile(user.getId()).equals(userBindingTO.getMobile())) {
        //            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA, errorConstant.getERROR_MESSAGE_INVALID_DATA);
        //        }
        //        validateUser(user);
        //        try {
        //            userCopyDaoIntf.getPendingUserRequestByUsername(user.getUsername());
        //            throw new AuthException(null, errorConstant.getERROR_CODE_USER_UPDATE_PENDING, errorConstant.getERROR_MESSAGE_USER_UPDATE_PENDING);
        //        }
        //        catch (UserNotFoundException e) {
        //            logger.log(Level.INFO, e);
        //        }
        //        try {
        //            ApplicationTO applicationTO = userBindingTO.getApplication();
        //            ApplicationDaoIntf applicationDaoIntf = applicationDao;
        //            ServiceDaoIntf serviceDaoIntf = serviceDao;
        //            List<UserApplicationServiceRel> userApplicationRels = userApplicationRelDao.getApplicationRelsForUser(user.getId());
        //            Application application = applicationDaoIntf.getApplicationByApplicationId(applicationTO.getApplicationId());
        //            if ((user.getUserType() == UserType.ADUSER && application.getApplicationType() == ApplicationType.AD) ||
        //                (user.getUserType() == UserType.USER && application.getApplicationType() == ApplicationType.NON_AD)) {
        //                stageUserApplicationServiceBindingInDB(user, applicationTO, userApplicationStagingRels, serviceDaoIntf, userApplicationRels, application);
        //            }
        //            else {
        //                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_FOR_USER, errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_FOR_USER);
        //            }
        //            if (!userApplicationStagingRels.isEmpty()) {
        //                UserStaging adUserStaging = getUserStaging(userBindingTO, actor, user, Constant.SERVICE_BINDING);
        //                adUserStaging = userCopyDaoIntf.create(adUserStaging);
        //                DaoFactory.getUserApplicationStagingRel().bulkInsert(userApplicationStagingRels, adUserStaging.getId());
        //                sendNotificationUserBind(actor, user);
        //            }
        //            else {
        //                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_ALREADY_PRESENT, errorConstant.getERROR_MESSAGE_USER_SERVICE_ALREADY_PRESENT);
        //            }
        //        }
        //        catch (ServiceNotFoundException e) {
        //            throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND, errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND);
        //        }
        //        catch (ApplicationNotFoundException e) {
        //            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND, errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND);
        //        }
    }

    private void sendNotificationUserBind(String actor, User user) {
        try {
            String emailBody = String.format(config.getProperty(Constant.USER_EMAIL_BODY), Constant.SERVICE_BINDING_TEXT, getUsername(user.getId()), getMobile(user.getId()), Constant.RAISED,
                    actor);
            String smsBody =
                           String.format(config.getProperty(Constant.USER_SMS_BODY), Constant.SERVICE_BINDING_TEXT, getUsername(user.getId()), getMobile(user.getId()), Constant.RAISED, actor);
            //            EventsHandler.getInstance().generateRequestEvents(ActionName.USER_REQUEST_INITIATED, emailBody, smsBody);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e);
        }
    }

    @Override
    public void unbindServicesFromUser(Session session, UserBindingTO userBindingTO, String role, String actor,Long id,boolean saveRequest, boolean checkerApproved) throws AuthException{
        User user;
        try {
            user = userDao.getActiveUserById(userBindingTO.getId());
        }
        catch (UserNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        logger.log(Level.DEBUG, "Validate permission "+userBindingTO.getId());
        permissionUtil.validateEditUserPermission(role);

       /* if (!getUsername(user.getId()).equals(userBindingTO.getUsername())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
        }*/
        validateUser(user);
        try {
            ApplicationTO applicationTO = userBindingTO.getApplication();
            List<UserApplicationServiceRel> userApplicationRels = userApplicationRelDao.getApplicationRelsForUser(user.getId());
            if (userApplicationRels == null || userApplicationRels.isEmpty()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_ACTIVE(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_ACTIVE());
            }
            Application application = applicationDao.getApplicationByApplicationId(applicationTO.getApplicationId());
            List<UserApplicationServiceRel> userApplicationStagingRels = new ArrayList<>();
            stageUserApplicationServiceUnbindDataInDB(user, applicationTO, userApplicationStagingRels, serviceDao, userApplicationRels, application);

            if (!userApplicationStagingRels.isEmpty()) {
                if(!checkerApproved) {
                    requestService.createUserServiceUnbindRequest(session, userBindingTO, actor,id,saveRequest);
                }
                if (!saveRequest) {
                    try {
                        Long applicationBindingCount = userApplicationRelDao.getUserAndApplicationRelCount(application.getId(), user.getId());
                        logger.log(Level.DEBUG, "unbindServicesFromUser " + applicationBindingCount + " userApplicationStagingRels "+ userApplicationStagingRels.size());
                        IAMExtensionV2 iamExtension = IAMUtil.getInstance().getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
                        Token token = IAMUtil.getInstance().authenticateV2(iamExtension, application.getApplicationId(),
                                AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
                        updateIsTokenEnabled(token, "USER_ID", userBindingTO.getUsername(), user.getAccountId(), userApplicationRels, userBindingTO.getApplication(), iamExtension);
                        if (applicationBindingCount == userApplicationStagingRels.size()) {
                            iamExtension.unbindConsumerApplication(token, user.getAccountId());
                            if(application.getKcId() != null && user.getKcId() != null) {
                                boolean camUserDeleted = camUserFacade.bindUserToApplication(Config.getInstance().getProperty(Constant.CAM_REALM), application.getKcId(), user.getKcId(), application.getApplicationId(), Constant.UNBIND_OPERATION);
                                if(camUserDeleted){
                                    user.setKcId(null);
                                    userDao.update(session, user);
                                }
                            }
                        }
                    }
                    catch (IAMException e) {
                        throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
                    }
                    catch (IAMWebException e) {
                        throw new AuthException(new Exception(), e.getErrorCode(), e.getMessage());
                    }
                    userApplicationRelDao.bulkUpdate(session, userApplicationStagingRels);
                    updateIAMStatusToDisabled(session, user);
                }
            }
            else {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_NOT_FOUND());
            }
            AuditLogUtil.sendAuditLog(AuditLogConstant.APPLICATION_SERVICE_UNBIND_SUCCESSFUL + AuditLogConstant.BY + actor + AuditLogConstant.FOR_APPLICATION + userBindingTO.getApplication().getApplicationName() + AuditLogConstant.FOR_USER + userBindingTO.getUsername(), "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", user.getAccountId(), null);
        }
        catch (ServiceNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }

    }

    private void updateIsTokenEnabled(Token token, String attributeName, String attributeValue, String accountId, List<UserApplicationServiceRel> userApplicationRels, ApplicationTO application, IAMExtensionV2 iamExtension) throws IAMException, IAMWebException {
        boolean appHasAuth42 = false;
        boolean servicesHaveAuth42 = false;
        for (ServiceTO service : application.getServices()) {
            if (service.getServiceName().equalsIgnoreCase("AUTH42")) {
                appHasAuth42 = true;
                break;
            }
        }
        for (UserApplicationServiceRel userApplicationServiceRel : userApplicationRels) {
            if (userApplicationServiceRel.getId().getService().getServiceName().equalsIgnoreCase("AUTH42") && !userApplicationServiceRel.getId().getApplication().getApplicationId().equalsIgnoreCase(application.getApplicationId())) {
                servicesHaveAuth42 = true;
                break;
            }
        }
        boolean update = (appHasAuth42 && !servicesHaveAuth42);
        iamExtension.updateIsTokenEnabled(token, attributeName.toUpperCase(), attributeValue, accountId, update);
    }

    private void updateIAMStatusToDisabled(Session session, User user) {
        List<UserApplicationServiceRel> adUserUpdatedApplicationRels = userApplicationRelDao.getApplicationRelsForUser(user.getId());
        if (adUserUpdatedApplicationRels == null || adUserUpdatedApplicationRels.isEmpty()) {
            user.setIamStatus(IAMStatus.DISABLED);
            userDao.update(session, user);
        }
    }

    private void stageUserApplicationServiceUnbindDataInDB(User user, ApplicationTO applicationTO, List<UserApplicationServiceRel> userApplicationStagingRels, ServiceDaoIntf serviceDaoIntf,
            List<UserApplicationServiceRel> userApplicationRels, Application application) throws ServiceNotFoundException, AuthException {
        List<ServiceTO> userServiceTOs = applicationTO.getServices();
        List<Service> applicationServices = application.getServices();
        for (ServiceTO serviceTO : userServiceTOs) {
            Service service = serviceDaoIntf.getServiceByServiceName(serviceTO.getServiceName());
            if (!applicationServices.contains(service)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SERVICE_FOR_APPLICATION(), errorConstant.getERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION());
            }
            UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
            userApplicationCompositeKey.setUser(user);
            userApplicationCompositeKey.setApplication(application);
            userApplicationCompositeKey.setService(service);
            UserApplicationServiceRel userApplicationRel = new UserApplicationServiceRel();
            userApplicationRel.setId(userApplicationCompositeKey);
            if (userApplicationRels.contains(userApplicationRel)) {
                UserApplicationServiceCompositeKey tempUserApplicationServiceCompositeKey = new UserApplicationServiceCompositeKey();
                tempUserApplicationServiceCompositeKey.setUser(user);
                tempUserApplicationServiceCompositeKey.setApplication(application);
                tempUserApplicationServiceCompositeKey.setService(service);
                try {
                    UserApplicationServiceRel userApplicationServiceRel = userApplicationRelDao.getUserApplicationForId(userApplicationCompositeKey);
                    if (userApplicationServiceRel.getBindingStatus().equals(BindingStatus.ACTIVE) || userApplicationServiceRel.getBindingStatus().equals(BindingStatus.BLOCKED)) {
                        userApplicationServiceRel.setBindingStatus(BindingStatus.INACTIVE);
                        userApplicationStagingRels.add(userApplicationServiceRel);
                    }
                }
                catch (UserApplicationRelNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
        }
    }

    private void sendNotificationUserUnbind(String actor, User user) {
        try {
            String emailBody = String.format(config.getProperty(Constant.USER_EMAIL_BODY), Constant.SERVICE_UNBINDING_TEXT, getUsername(user.getId()), getMobile(user.getId()), Constant.RAISED,
                    actor);
            String smsBody = String.format(config.getProperty(Constant.USER_SMS_BODY), Constant.SERVICE_UNBINDING_TEXT, getUsername(user.getId()), getMobile(user.getId()), Constant.RAISED,
                    actor);
            //            EventsHandler.getInstance().generateRequestEvents(ActionName.USER_REQUEST_INITIATED, emailBody, smsBody);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e);
        }
    }

    @Override
    public ConsumerTO updateUserApplicationServiceRel(Session session, ConsumerTO userTO, String applicationId) throws AuthException {
        userTO.setUsername(userTO.getUsername().trim());
        userTO.setConsumerId(userTO.getConsumerId().trim());
        User user = getUser(userTO.getUsername(), userTO.getConsumerId());
        validateNonADUser(user, userTO.getConsumerId());
        Application application = null;
        try {
            application = applicationDao.getApplicationByApplicationId(applicationId);
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        List<UserApplicationServiceRel> userApplicationRels = userApplicationRelDao.getBlockedApplicationRelsForUser(user.getId(), application.getId());
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
                userApplicationRelDao.bulkUpdate(session, userApplicationRelsToUpdate);
                userTO.setStatus(Constant.SUCCESS_STATUS);
                return userTO;
            }
        }
        throw new AuthException(null, errorConstant.getERROR_CODE_SERVICES_ALREADY_ENABLED_FOR_USER(), errorConstant.getERROR_MESSAGE_SERVICES_ALREADY_ENABLED_FOR_USER());
    }

    @Override
    public void updateMultipleUserApplicationServiceRelCopy(Session session, BlockUserApplicationTO blockUserApplicationTO) throws AuthException, UserBlockedException {
        List<User> userList = userDao.getNonADUsersForMobileNumber(blockUserApplicationTO.getConsumerId());
        if (userList.isEmpty()) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        //List<Application> applicationList = applicationDao.getResetPinEnabledApplications();
        List<Application> applicationList = applicationDao.getApplications();
        for (Application application : applicationList) {
            processApplicationWiseUserUpdate(session, blockUserApplicationTO, userList, application);
        }
    }

    private void processApplicationWiseUserUpdate(Session session, BlockUserApplicationTO blockUserApplicationTO, List<User> userList, Application application) {
        List<String> usernames = new ArrayList<>();
        for (User user : userList) {
            processUserApplicationServiceUpdate(session, blockUserApplicationTO, application, usernames, user);
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
    }

    private void processUserApplicationServiceUpdate(Session session, BlockUserApplicationTO blockUserApplicationTO, Application application,
            List<String> usernames, User user) {
        boolean sendUsernameForUpdate = false;
        List<UserApplicationServiceRel> userApplicationRels = userApplicationRelDao.getUserApplicationRels(application.getId(), user.getId());
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
                userApplicationRelDao.bulkUpdate(session, userApplicationRelsToUpdate);
            }
            if (sendUsernameForUpdate) {
                usernames.add(getUsername(user.getId()));
            }
        }
    }

    private void sendDataToCallbackUrl(List<String> usernames, String consumerId, String callbackUrl, String bindingStatus) {
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
    }

    private void validateNonADUser(User user, String mobile) throws AuthException {
        if (user.getUserStatus() == UserStatus.BLOCK) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_BLOCK(), errorConstant.getERROR_MESSAGE_USER_BLOCK());
        }
        if (!mobile.equals(getMobile(user.getId()))) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_MOBILE(), errorConstant.getERROR_MESSAGE_INVALID_MOBILE());
        }
    }

    private String getUpdatedMobile(String mobile) {
        String fedUserMobile = mobile.replaceAll("[\\s-]", "").trim();
        if (fedUserMobile.length() - 10 == 0) {
            return Constant.COUNTRY_CODE + fedUserMobile.substring(fedUserMobile.length() - 10, fedUserMobile.length());
        }
        return fedUserMobile;
    }

    private in.fortytwo42.adapter.transferobj.ConsumerBindingTO convertToTOV3(ConsumerBindingTO consumerBindingTO, String serviceName, String type) {
        in.fortytwo42.adapter.transferobj.ConsumerBindingTO userBindingTO = new in.fortytwo42.adapter.transferobj.ConsumerBindingTO();
        AttributeDataTO publicAttribute = new AttributeDataTO();
        publicAttribute.setAttributeName(CUST_ID);
        publicAttribute.setAttributeValue(consumerBindingTO.getUsername());
        userBindingTO.setCustomAttribute(publicAttribute);
        List<AttributeDataTO> searchAttributes = new ArrayList<>();
        AttributeDataTO searchAttrinute = new AttributeDataTO();
        searchAttrinute.setAttributeName(MOBILE_NO);
        searchAttrinute.setAttributeValue(consumerBindingTO.getConsumerId());
        searchAttributes.add(searchAttrinute);
        userBindingTO.setSearchAttributes(searchAttributes);
        switch (type) {
            case "BIND":
                userBindingTO.setTransactionId(consumerBindingTO.getTransactionId());
                userBindingTO.setTransactionDetails(consumerBindingTO.getTransactionDetails());
                userBindingTO.setTransactionSummary(consumerBindingTO.getTransactionSummary());
                userBindingTO.setTwoFactorStatus(consumerBindingTO.getTwoFactorStatus());
                userBindingTO.setActivateEncryption(consumerBindingTO.getActivateEncryption());
                userBindingTO.setServiceName(serviceName);
                break;
            default:
                break;
        }

        return userBindingTO;
    }

    public String getMobile(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.MOBILE_NO);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    public String getUsername(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.USER_ID);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    public String getFullName(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.FULL_NAME);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    public String getEmail(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.EMAIL);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    @Override
    public UserApplicationRelTO updateUserApplicationRel(Session session, UserApplicationRelTO stagingUserApplicationRelSettingsTO, String role, String actor) throws AuthException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserApplicationRelTO approveUserApplicationRelBinding(Session session, UserApplicationRelTO userApplicationRelTO, String role, String actor) throws AuthException {
        // TODO Auto-generated method stub
        return null;
    }
}
