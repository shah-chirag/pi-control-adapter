
package in.fortytwo42.adapter.facade;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import in.fortytwo42.tos.transferobj.TransactionReportRequestTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.AuthenticationAttemptServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.EvidenceStoreServiceIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.ServiceProcessorIntf;
import in.fortytwo42.adapter.service.UserApplicationRelServiceIntf;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.ApprovalAttemptPollerTO;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.EsbResponseTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.QRCodeDataTO;
import in.fortytwo42.adapter.transferobj.QRCodeTO;
import in.fortytwo42.adapter.transferobj.UserAuthenticationTO;
import in.fortytwo42.adapter.transferobj.UserResponseTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AttributeValidationUtil;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.CallbackUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.handler.AsyncResponseHandler;
import in.fortytwo42.adapter.util.handler.AuthAttemptAsyncResponseHandler;
import in.fortytwo42.adapter.util.handler.AuthAttemptHistoryHandler;
import in.fortytwo42.adapter.util.handler.BindingAsyncResponseHandler;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.AuthenticationAttemptDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.RequestNotFoundException;
import in.fortytwo42.daos.exception.TransactionNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.ApprovalAttemptV2;
import in.fortytwo42.enterprise.extension.core.DecryptionDataV2;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.ApprovalAttemptMode;
import in.fortytwo42.enterprise.extension.enums.ApprovalStatus;
import in.fortytwo42.enterprise.extension.enums.CI2Type;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.tos.EncryptionDataTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.AuthenticationAttemptHistory;
import in.fortytwo42.entities.bean.CallbackUrl;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.entities.enums.AuthenticationStatus;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.enums.AttributeAction;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.AuthenticationAttemptHistoryTO;
import in.fortytwo42.tos.transferobj.UserTO;

// TODO: Auto-generated Javadoc
/**
 * The Class AuthAttemptFacadeImpl.
 */
public class AuthAttemptFacadeImpl implements AuthAttemptFacadeIntf {

    //TODO: Facade to Facade
    private final NonADUserFacadeIntf nonADUserFacade = FacadeFactory.getNonADUserFacade();

    /** The atuth attempt facade impl log. */
    private final String ATUTH_ATTEMPT_FACADE_IMPL_LOG = "<<<<< AuthAttemptFacadeImpl";

    /** The Constant POLLER_CALLBACK. */
    private static final String POLLER_CALLBACK = "Poller callback~";

    /** The Constant POLLER_CALLBACK_RECEIVED_FOR_CREATE_APPROVAL_ATTEMPT. */
    private static final String POLLER_CALLBACK_RECEIVED_FOR_CREATE_APPROVAL_ATTEMPT = "~poller callback received for create approval attempt";

    /** The Constant ASYNC_GET_CALL. */
    private static final String ASYNC_GET_CALL = "Async Get call~";

    /** The authentication attempt processor intf. */
    private final AuthenticationAttemptServiceIntf authenticationAttemptService = ServiceFactory.getAuthenticationService();
    /** The application processor intf. */
    private final ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    /** The user application rel processor intf. */
    private final UserApplicationRelServiceIntf userApplicationRelService = ServiceFactory.getUserApplicationRelService();
    /** The service processor intf. */
    private final ServiceProcessorIntf serviceProcessorIntf = ServiceFactory.getServiceProcessor();
    /** The attribute store processor intf. */
    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();
    private final EvidenceStoreServiceIntf evidenceStoreService = ServiceFactory.getEvidenceStoreService();
    private final RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    /** The user processor intf. */
    private final UserServiceIntf userService = ServiceFactory.getUserService();
    
    private final AuthenticationAttemptDaoIntf authenticationAttemptDao = DaoFactory.getAuthenticationAttemptDao();
    private final RequestDaoIntf requestDao = DaoFactory.getRequestDao();

    private final AuthAttemptHistoryHandler authAttemptHistoryHandler = AuthAttemptHistoryHandler.getInstance();
    private final BindingAsyncResponseHandler bindingAsyncResponseHandler = BindingAsyncResponseHandler.getInstance();
    private final AsyncResponseHandler asyncResponseHandler = AsyncResponseHandler.getInstance();
    private final AuthAttemptAsyncResponseHandler authAttemptAsyncResponseHandler = AuthAttemptAsyncResponseHandler.getInstance();
    private final Config config = Config.getInstance();
    private final IAMUtil iamUtil = IAMUtil.getInstance();
    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();
    /** The Session Factory Util */
    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private final CallbackUtil callbackUtil = CallbackUtil.getInstance();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(AuthAttemptFacadeImpl.class);


    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final AuthAttemptFacadeImpl INSTANCE = new AuthAttemptFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of AuthAttemptFacadeImpl.
     *
     * @return single instance of AuthAttemptFacadeImpl
     */
    public static AuthAttemptFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Two factor authentication.
     *
     * @param user the user
     * @param application the application
     * @param service the service
     * @param userAuthenticationTO the user authentication TO
     * @param timeout the timeout
     * @param asyncResponse the async response
     * @param serverId the server id
     * @param iamExtension the iam extension
     * @param token the token
     * @throws AuthException the auth exception
     * @throws IAMException the IAM exception
     */
    //    @Override
    //    public void twoFactorAuthentication(User user, Application application,  in.fortytwo42.entities.bean.Service service, UserAuthenticationTO userAuthenticationTO, Integer timeout,
    //            AsyncResponse asyncResponse, String serverId, IAMExtensionV2 iamExtension, Token token) throws AuthException, IAMException {
    //        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " twoFactorAuthentication : start");
    //        boolean isAuthenticated;
    //        if (userAuthenticationTO.getAuthenticationRequired() == null) {
    //            isAuthenticated = false;
    //        }
    //        else {
    //            isAuthenticated = userAuthenticationTO.getAuthenticationRequired();
    //        }
    //        ApprovalAttemptTO approvalAttemptTO = null;//iamExtensionProcessor.createApprovalAttemptOnIAM(user.getAccountId(), generateTransactionId(), userAuthenticationTO.getTransactionSummary(),
    //        //userAuthenticationTO.getTransactionDetails(), IAMConstants.NORMAL, service.getServiceName(), isAuthenticated , application, serverId, timeout);
    //        ADAsyncResponseHandler.getInstance().storeAsyncResponseReference(approvalAttemptTO.getTransactionId(), application.getApplicationId(), asyncResponse, user.getUsername());
    //        authenticationAttemptProcessorIntf.createAuthenticationAttempt(approvalAttemptTO);
    //        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " twoFactorAuthentication : end");
    //    }

    /**
     * Process poller callback.
     *
     * @param approvalAttemptPollerData the approval attempt poller data
     * @return the esb response TO
     * @throws AuthException the auth exception
     */
    @Override
    public EsbResponseTO processPollerCallback(ApprovalAttemptPollerTO approvalAttemptPollerData) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processPollerCallback : start");
        processCallback(approvalAttemptPollerData);

        EsbResponseTO esbResponseTO = new EsbResponseTO();
        esbResponseTO.setIaminboundId(approvalAttemptPollerData.getIamTransaction());
        esbResponseTO.setStatusCode(Constant.RESPONSE_CODE_200);
        esbResponseTO.setSubStatusCode(Constant.RESPONSE_SUB_CODE_100);
        esbResponseTO.setStatusDesc(Constant.SUCCESS_STATUS);
        esbResponseTO.setVersion(Long.toString(approvalAttemptPollerData.getVersion()));
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processPollerCallback : end");
        return esbResponseTO;
    }

    /**
     * Process poller callback according to approval attempt type.
     * 
     * @param approvalAttemptPollerData
     *            Approval Attempt data received in poller callback
     * @throws AuthException
     *             Incase of failure fetching or processing the Authentication
     *             attempt request.
     */
    private void processCallback(ApprovalAttemptPollerTO approvalAttemptPollerData) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processCallback : start");
        logger.log(Level.DEBUG, "%%%%% approvalAttemptPollerData : " + new Gson().toJson(approvalAttemptPollerData));
        Session session = sessionFactoryUtil.getSession();
        try {
            switch (approvalAttemptPollerData.getApprovalAttemptType()) {
                case Constant.ATTRIBUTE_VERIFICATION:
                    processCallbackForAttributeVerification(session, approvalAttemptPollerData);
                    break;
                case Constant.EVIDENCE_REQUEST:
                    processEvidenceRequestCallback(session, approvalAttemptPollerData);
                    break;
                case Constant.EVIDENCE_REQUEST_USER_CONSENT:
                    processEvidenceRequestUserConsentCallback(session, approvalAttemptPollerData);
                    break;
                default:
                    AuthenticationAttempt authenticationAttempt = null;
                    try {
                        //authenticationAttempt = authenticationAttemptDao.getAuthAttemptByTransactionId(approvalAttemptPollerData.getTransactionId());
                        authenticationAttempt = authenticationAttemptDao.getAuthAttemptBySenderAccountIdAndTransactionId(approvalAttemptPollerData.getSenderAccountId(), approvalAttemptPollerData.getTransactionId());
                    }
                    catch (TransactionNotFoundException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                    updateAuthenticationAttempt(session, authenticationAttempt, approvalAttemptPollerData.getApprovalStatus());
                    handleCallback(session, authenticationAttempt, approvalAttemptPollerData);
                    break;
            }
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

        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processCallback : end");
    }
    
    private void updateAuthenticationAttempt(Session session, AuthenticationAttempt authenticationAttempt, String approvalStatus) {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " updateAuthenticationAttempt : start");
        if (authenticationAttempt != null) {
            authenticationAttempt.setAttemptStatus(approvalStatus);
            authAttemptHistoryHandler.updateAuthAttemptHistoryDataByTrasactionId(session, authenticationAttempt);
            authenticationAttemptDao.remove(session, authenticationAttempt);
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " updateAuthenticationAttempt : end");
    }
    
    private void handleCallback(Session session, AuthenticationAttempt authenticationAttempt, ApprovalAttemptPollerTO approvalAttemptPollerData) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleCallback : start");
        switch (approvalAttemptPollerData.getApprovalAttemptType()) {
            case Constant.ATTRIBUTE_ADDITION:
            case Constant.ATTRIBUTE_EDIT:
                updateRequest(session, authenticationAttempt);
                break;
            case Constant.ATTRIBUTE_REQUEST:
            case Constant.ATTRIBUTE_DEMAND:
                processCallbackForAttributeRequest(session, approvalAttemptPollerData, authenticationAttempt);
                break;
            /*case Constant.REGULATORY:
                userService.updateUserBindingOnApproval(session, authenticationAttempt);
                break;*/
            case Constant.ATTRIBUTE_UPDATION:
                updateEditRequest(session, authenticationAttempt);
                break;
            default:
                if(approvalAttemptPollerData.getApprovalAttemptType().equals(Constant.REGULATORY)) {
                    userService.updateUserBindingOnApproval(session, authenticationAttempt);
                }
                AsyncResponse bindingAsyncResponse = bindingAsyncResponseHandler.getAsyncResponse(approvalAttemptPollerData.getTransactionId(), approvalAttemptPollerData.getSenderAccountId());
                AsyncResponse asyncResponse2FACall = asyncResponseHandler.getAsyncResponse(approvalAttemptPollerData.getTransactionId(), approvalAttemptPollerData.getSenderAccountId());
                AsyncResponse authAttemptSyncResponse = authAttemptAsyncResponseHandler.getAsyncResponse(approvalAttemptPollerData.getTransactionId(), approvalAttemptPollerData.getSenderAccountId());
                if (bindingAsyncResponse != null) {
                    handleBindingAsyncResponse(session, approvalAttemptPollerData, authenticationAttempt);
                }
                if (asyncResponse2FACall != null) {
                    handleAsyncResponse2FA(approvalAttemptPollerData, asyncResponse2FACall);
                }
                else if (authAttemptSyncResponse != null) {
                    logger.log(Level.DEBUG, "%%%%% authAttemptSyncResponse");
                    handleAuthAttemptAsyncResponse(approvalAttemptPollerData, authAttemptSyncResponse);
                }
                break;
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleCallback : end");
    }
    
    private void handleBindingAsyncResponse(Session session, ApprovalAttemptPollerTO approvalAttemptPollerData, AuthenticationAttempt authenticationAttempt)
            throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleBindingAsyncResponse : start");
        logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, POLLER_CALLBACK, System.currentTimeMillis() + Constant.TILT,
                approvalAttemptPollerData.getApplicationId(), Constant.TILT, approvalAttemptPollerData.getTransactionId(), "~poller callback received for user binding"));
        ConsumerBindingTO consumerBindingTO = nonADUserFacade.completeConsumerBinding(session, authenticationAttempt);
//        bindingAsyncResponse.resume(Response.status(Response.Status.OK).entity(consumerBindingTO).build());
        bindingAsyncResponseHandler.removeRequest(approvalAttemptPollerData.getTransactionId(), approvalAttemptPollerData.getSenderAccountId());
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleBindingAsyncResponse : end");
    }

    private void handleAsyncResponse2FA(ApprovalAttemptPollerTO approvalAttemptPollerData, AsyncResponse asyncResponse2FACall) {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleAsyncResponse2FA : start");
        logger.log(Level.DEBUG,
                StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, POLLER_CALLBACK, System.currentTimeMillis() + Constant.TILT,
                        approvalAttemptPollerData.getApplicationId(), Constant.TILT, approvalAttemptPollerData.getTransactionId(),
                        "~poller callback received for ad authentication with approval status " + approvalAttemptPollerData.getApprovalStatus()));
        UserResponseTO userResponseTO = new UserResponseTO();
        if (approvalAttemptPollerData.getApprovalStatus().equals(ApprovalStatus.APPROVED.name())) {
            userResponseTO.setStatus(AuthenticationStatus.TWO_FA_SUCCESS.name());
        }
        else {
            userResponseTO.setStatus(AuthenticationStatus.TWO_FA_FAILED.name());
        }
        userResponseTO.setApprovalStatus(approvalAttemptPollerData.getApprovalStatus());
        userResponseTO.setUsername(asyncResponseHandler.getUsername(approvalAttemptPollerData.getTransactionId(), approvalAttemptPollerData.getSenderAccountId()));
        asyncResponse2FACall.resume(Response.status(Response.Status.OK).entity(userResponseTO).build());
        asyncResponseHandler.removeRequest(approvalAttemptPollerData.getTransactionId(), approvalAttemptPollerData.getSenderAccountId());
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleAsyncResponse2FA : end");
    }

    private void handleAuthAttemptAsyncResponse(ApprovalAttemptPollerTO approvalAttemptPollerData, AsyncResponse authAttemptSyncResponse) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleAuthAttemptAsyncResponse : start");
        logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, POLLER_CALLBACK, System.currentTimeMillis() + Constant.TILT,
                approvalAttemptPollerData.getApplicationId(), Constant.TILT, approvalAttemptPollerData.getTransactionId(), POLLER_CALLBACK_RECEIVED_FOR_CREATE_APPROVAL_ATTEMPT));
        try {
            Application application = applicationService.getApplicationByApplicationAccountId(approvalAttemptPollerData.getSenderAccountId());
            logger.log(Level.DEBUG, "%%%%% application Id : " + application.getApplicationId());
            AuthenticationAttemptTO adapterApprovalAttemptTO = getApprovalAttempt(application.getApplicationId(), approvalAttemptPollerData.getTransactionId());
            logger.log(Level.DEBUG, "%%%%% adapterApprovalAttemptTO : " + adapterApprovalAttemptTO.getTransactionId());
            authAttemptSyncResponse.resume(Response.status(Response.Status.OK).entity(adapterApprovalAttemptTO).build());
            logger.log(Level.DEBUG, "%%%%% Async response resume complete : ");
            authAttemptAsyncResponseHandler.removeRequest(approvalAttemptPollerData.getTransactionId(), approvalAttemptPollerData.getSenderAccountId());
            logger.log(Level.DEBUG, "%%%%% request removed from authAttemptAsyncResponseHandler ");
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e);
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleAuthAttemptAsyncResponse : end");
    }

    /**
     * Gets the approval attempt.
     *
     * @param applicationId the application id
     * @param transactionId the transaction id
     * @return the approval attempt
     * @throws AuthException the auth exception
     */
    @Override
    public AuthenticationAttemptTO getApprovalAttempt(String applicationId, String transactionId) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " getApprovalAttempt : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            Application application = applicationService.getApplicationByApplicationId(applicationId);
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, ASYNC_GET_CALL, Constant.TILT, applicationId, Constant.TILT, transactionId, "~call IAM Get"));
            ApprovalAttemptTO approvalAttemptResponse = iamExtension.getAppprovalAttemptTO(token, transactionId);
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, ASYNC_GET_CALL, Constant.TILT, applicationId, Constant.TILT, transactionId,
                            "~call IAM Get Done"));
            logger.log(Level.DEBUG, "approvalAttemptResponse : "+new Gson().toJson(approvalAttemptResponse));
            AuthenticationAttemptTO authenticationAttemptTO = authenticationAttemptService.updateAuthAttemptByApplicationAndTransactionId(session, application.getApplicationId(), transactionId,
                    approvalAttemptResponse.getApprovalStatus(), approvalAttemptResponse);
            logger.log(Level.DEBUG, "%%%%% authenticationAttemptTO : "+new Gson().toJson(authenticationAttemptTO));
            sessionFactoryUtil.closeSession(session);
            return authenticationAttemptTO;
        }
        catch (TransactionNotFoundException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw new UndeclaredThrowableException(e);
        }
        catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }catch (Exception e) {
            logger.log(Level.ERROR, e);
            throw new UndeclaredThrowableException(e);
        }
        finally {
            if(session.isOpen()){
                session.close();
            }
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " getApprovalAttempt : end");
        }
    }

    /**
     * Creates the approval attempt.
     *
     * @param asyncResponse the async response
     * @param authenticationAttemptTO the authentication attempt TO
     * @param applicationId the application id
     * @return the authentication attempt TO
     * @throws AuthException the auth exception
     * @throws UserBlockedException 
     */
    @Override
    public AuthenticationAttemptTO createApprovalAttempt(AsyncResponse asyncResponse, AuthenticationAttemptTO authenticationAttemptTO, String applicationId) throws AuthException, UserBlockedException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createApprovalAttempt : start");
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        for (AttributeDataTO attributeDataTO : authenticationAttemptTO.getSearchAttributes()) {
            AttributeValidationUtil.validateSearchAttributeValueAndUniqueness(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());
        }
        try {
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            AccountWE accountWE = iamExtensionService.searchAccount(authenticationAttemptTO.getSearchAttributes(), iamExtension, token);
            User user = userService.getActiveUserForAuthAttempt(accountWE.getId());
            validateUser(user);
            Service service = serviceProcessorIntf.getService(authenticationAttemptTO.getServiceName());
            UserApplicationServiceRel userApplicationRel = userApplicationRelService.getUserApplicationRelForAccountId(user, application, service);
            userApplicationRelService.validateUserApplicationRel(userApplicationRel);
            return generateApprovalAttempt(asyncResponse, accountWE.getId(), authenticationAttemptTO, application);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createApprovalAttempt : end");
        }
    }

    @Override
    public AuthenticationAttemptTO createApprovalAttemptV4(AsyncResponse asyncResponse, AuthenticationAttemptTO authenticationAttemptTO, String applicationId) throws AuthException, UserBlockedException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createApprovalAttempt : start");
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        for (AttributeDataTO attributeDataTO : authenticationAttemptTO.getSearchAttributes()) {
            AttributeValidationUtil.validateSearchAttributeValueAndUniqueness(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());
        }
        try {
            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOs = new ArrayList<>();
            for (AttributeDataTO attributes : authenticationAttemptTO.getSearchAttributes()) {
                in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
                attributeTO.setAttributeName(attributes.getAttributeName());
                attributeTO.setAttributeValue(attributes.getAttributeValue());
                attributeTOs.add(attributeTO);
            }
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
//            AccountWE accountWE = iamExtensionService.searchAccount(authenticationAttemptTO.getSearchAttributes(), iamExtension, token);
            AccountWE accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            User user = userService.getActiveUserForAuthAttempt(accountWE.getId());
            validateUser(user);
            Service service = serviceProcessorIntf.getService(authenticationAttemptTO.getServiceName());
            UserApplicationServiceRel userApplicationRel = userApplicationRelService.getUserApplicationRelForAccountId(user, application, service);
            userApplicationRelService.validateUserApplicationRel(userApplicationRel);
            return generateApprovalAttempt(asyncResponse, accountWE.getId(), authenticationAttemptTO, application);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createApprovalAttempt : end");
        }
    }

    /**
     * Generate approval attempt.
     *
     * @param asyncResponse the async response
     * @param accountId the user
     * @param authenticationAttemptTO the authentication attempt TO
     * @param application the application
     * @return the authentication attempt TO
     * @throws AuthException the auth exception
     */
    private AuthenticationAttemptTO generateApprovalAttempt(AsyncResponse asyncResponse, String accountId, AuthenticationAttemptTO authenticationAttemptTO, Application application) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " generateApprovalAttempt : start");
        try {
            if (authenticationAttemptTO.getValidtill() == null) {
                authenticationAttemptTO.setValidtill(application.getTransactionTimeout());
            }
            if (authenticationAttemptTO.getAuthenticated() == null) {
                authenticationAttemptTO.setAuthenticated(application.getAuthenticationRequired());
            }
            ApprovalAttemptTO approvalAttemptTO = iamExtensionService.createApprovalAttemptOnIAM(accountId, authenticationAttemptTO, application);
            if (approvalAttemptTO != null) {
                logger.log(Level.DEBUG,
                        StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, Constant.CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT,
                                application.getApplicationId(), Constant.TILT, authenticationAttemptTO.getTransactionId(), Constant.TILT, authenticationAttemptTO.getApprovalAttemptType(),
                                "~approval attempt generation on I-AM server complete"));
                if (asyncResponse != null) {
                    logger.log(Level.DEBUG, "%%%%% TransactionId : "+authenticationAttemptTO.getTransactionId());
                    logger.log(Level.DEBUG, "%%%%% application account Id : "+application.getApplicationAccountId());
                    asyncResponse.setTimeout((approvalAttemptTO.getApprovalTimeout()+10), Constant.DEFAULT_TIME_UNIT);
                    authAttemptAsyncResponseHandler.storeAsyncResponseReference(authenticationAttemptTO.getTransactionId(), application.getApplicationAccountId(), asyncResponse,
                            authenticationAttemptTO);
                }
                Session session = sessionFactoryUtil.getSession();
                try {

                    AuthenticationAttempt authenticationAttempt = authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO, application);
                    authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
                    if(authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.QR)) {
                        authenticationAttemptTO.setApprovalAttemptId(approvalAttemptTO.getId());
                    }
                    authenticationAttemptTO.setApprovalStatus(ApprovalStatus.PENDING.name());
                    authenticationAttemptTO.setSignTransactionId(approvalAttemptTO.getSignTransactionId());
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
                AuditLogUtil.sendAuditLog(authenticationAttemptTO.getApprovalAttemptType()+ "approval attempt created successfully ", "USER", ActionType.CREATE_ATTEMPT, "", IdType.ACCOUNT, "", null, "", null);
                return authenticationAttemptTO;
            }
            else {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
            }
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }catch (IllegalArgumentException e){
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        }
        finally {
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " generateApprovalAttempt : end");
        }
    }

    /**
     * Gets the approval attempt DB poll.
     *
     * @param transactionId the transaction id
     * @param applicationId the application id
     * @return the approval attempt DB poll
     * @throws AuthException the auth exception
     */
    @Override
    public AuthenticationAttemptTO getApprovalAttemptDBPoll(String transactionId, String applicationId) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " getApprovalAttemptDBPoll : start");
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        AuthenticationAttemptTO authenticationAttempt = null;
        int i = 0;
        do {
            try {
                authenticationAttempt = authenticationAttemptService.getAuthAttemptByApplicationAndTransactionId(application.getApplicationId(), transactionId);
            }
            catch (TransactionNotFoundException e) {
            }
            if (authenticationAttempt == null) {
                throw new AuthException(null, errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
            }
            else if (!authenticationAttempt.getApprovalStatus().equals(ApprovalStatus.PENDING.name())) {
                break;
            }
            try {
                Thread.sleep(5000l);
            }
            catch (InterruptedException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            i++;
        } while (authenticationAttempt.getApprovalStatus().equals(ApprovalStatus.PENDING.name()) && (authenticationAttempt.getValidtill() > (i * 5)));
        if (authenticationAttempt.getApprovalStatus().equals(ApprovalStatus.PENDING.name())) {
            authenticationAttempt.setApprovalStatus(ApprovalStatus.TIMEOUT.name());
        }
        processCallbackCnB(applicationId, authenticationAttempt);
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " getApprovalAttemptDBPoll : end");
        return authenticationAttempt;
    }

    /**
     * Process callback cn B.
     *
     * @param applicationId the application id
     * @param approvalAttemptTOResponse the approval attempt TO response
     * @throws AuthException the auth exception
     */
    @Override
    public void processCallbackCnB(String applicationId, AuthenticationAttemptTO approvalAttemptTOResponse) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processCallbackCnB : start");
        Application application = null;
        try {
            application = applicationService.getApplicationWithCallbackUrl(applicationId);
        }
        catch (Exception e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        List<CallbackUrl> callbackUrls = application.getCallbackUrls();
        String callbackUrl = null;
        String authToken = null;
        if (callbackUrls != null && !callbackUrls.isEmpty()) {
            for (CallbackUrl callbackUrlObj : callbackUrls) {
                if (Constant.UPDATE_AUTH_ATTEMPT_URL.equals(callbackUrlObj.getCallbackType())) {
                    callbackUrl = callbackUrlObj.getUrl();
                    authToken = callbackUrlObj.getToken();
                    callbackUtil.sendAuthDataToCnB(callbackUrl, authToken, approvalAttemptTOResponse);
                }
            }
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processCallbackCnB : end");
    }

    /**
     * Process poller callback for approvalAtteptType ATTRIBUTE_VERIFICATION. 
     * This request is sent by User to the Enterprise for attribute verification. 
     * @param approvalAttemptPollerData Approval Attempt data received in poller callback
     */
    private void processCallbackForAttributeVerification(Session session, ApprovalAttemptPollerTO approvalAttemptPollerData) {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processCallbackForAttributeVerification : start");
        try {
            AuthenticationAttempt authenticationAttempt = null;
            try {
                authenticationAttempt = authenticationAttemptService.getAuthAttemptByTransactionId(approvalAttemptPollerData.getTransactionId());
            }
            catch (TransactionNotFoundException e) {
                logger.log(Level.DEBUG, e);
            }
            if (authenticationAttempt == null) {
                authenticationAttempt = authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptPollerData);
                authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
                String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
                Token token = iamExtensionService.getToken(iamExtension);
                String encryptedData = approvalAttemptPollerData.getEncryptedData();
                logger.log(Level.DEBUG, "<<<<< sig Id : " + approvalAttemptPollerData.getSignTransactionId());
                logger.log(Level.DEBUG, "<<<<< authenticationAttempt : " + authenticationAttempt.getApprovalAttemptMode());
                DecryptionDataV2 decryptionDataV2 = iamExtension.getDecryptionKeyForAttribute(token, approvalAttemptPollerData.getSignTransactionId(), authenticationAttempt.getApprovalAttemptMode(), reqRefNum);
                String decryptionKey = decryptionDataV2.getDecryptionKey();
                String decryptedData = AES128Impl.decryptDataWithMD5(encryptedData, decryptionKey);
                //logger.log(Level.DEBUG, "<<<<< decryptedData : " + decryptedData);
                AttributeDataTO attribute = new Gson().fromJson(decryptedData, AttributeDataTO.class);
                logger.log(Level.DEBUG, "<<<<< attributeName : " + attribute.getAttributeName());
                logger.log(Level.DEBUG, "<<<<< attributeValue : " + attribute.getAttributeValue());
                System.out.println(new Gson().toJson(attribute));
                logger.log(Level.DEBUG, "<<<<< evidence : " + (attribute.getEvidence() != null));
                //logger.log(Level.DEBUG, "<<<<< attribute : "+new Gson().toJson(attribute));
                User user = new User();
                try {
                    user = userService.getUserByAccountId(authenticationAttempt.getSenderAccountId());
                }
                catch (UserNotFoundException e) {
                    user = userService.createUser(session, approvalAttemptPollerData.getSenderAccountId(), UserRole.USER, TwoFactorStatus.DISABLED.toString(), user);
                    String[] details = authenticationAttempt.getTransactionDetails().split("\\|");
                    if (details.length > 1) {
                        attribute.setIsDefault(true);
                        attribute.setIsRegistered(true);
                    }
                }
                AttributeStore attributeStore = attributeStoreService.saveAttributeData(session, attribute, authenticationAttempt.getId(), user, true);
                if (attribute.getEvidence() != null && !attribute.getEvidence().isEmpty()) {
                    evidenceStoreService.saveEvidence(session, attributeStore, attribute.getEvidence());
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e);
        }
        finally {
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processCallbackForAttributeVerification : end");
        }
    }

    /**
     * Process poller callback for approvalAtteptType ATTRIBUTE_REQUEST. 
     * This request is created by the Enterprise for requesting the attribute from the user.
     * If the request is approved by the user, this function decypts and saves received attribute data in AttributeStore.
     * @param approvalAttemptPollerData Approval Attempt data received in poller callback
     * @param authenticationAttempt Authentication attempt created for ATTRIBUTE_REQUEST
     */
    private void processCallbackForAttributeRequest(Session session, ApprovalAttemptPollerTO approvalAttemptPollerData, AuthenticationAttempt authenticationAttempt) {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processCallbackForAttributeRequest : start");
        if (authenticationAttempt.getAttemptStatus().equals(Constant.APPROVED)) {
            try {
                //TODO: sagar to test this.
                String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
                Token token = iamExtensionService.getToken(iamExtensionV2);
                DecryptionDataV2 decryptionData = iamExtensionV2.getDecryptionKey(token, authenticationAttempt.getSignTransactionId(), CI2Type.VERIFY, reqRefNum);
                logger.log(Level.INFO, decryptionData.getDecryptionKey() + " " + approvalAttemptPollerData.getEncryptedData());
                decryptAndStoreAttributeData(session, authenticationAttempt.getId(), approvalAttemptPollerData, decryptionData.getDecryptionKey());
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e);
            }
        }

        RequestType requestType = approvalAttemptPollerData.getApprovalAttemptType().equals(Constant.ATTRIBUTE_DEMAND) ? RequestType.ATTRIBUTE_DEMAND : RequestType.ATTRIBUTE_REQUEST;
        List<Request> requests = requestService.getRequests(requestType, in.fortytwo42.entities.enums.ApprovalStatus.USER_APPROVAL_PENDING);
        if (requests != null && !requests.isEmpty()) {
            for (Request request : requests) {
                UserTO requestTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                if (requestTO.getAuthId().equals(authenticationAttempt.getId())) {
                    in.fortytwo42.entities.enums.ApprovalStatus approvalStatus = authenticationAttempt.getAttemptStatus().equals(Constant.APPROVED)
                                                                                                                                                   ? in.fortytwo42.entities.enums.ApprovalStatus.APPROVED_BY_USER
                                                                                                                                                   : in.fortytwo42.entities.enums.ApprovalStatus.REJECTED_BY_USER;
                    request.setApprovalStatus(approvalStatus);
                    try {
                        requestService.updateRequest(session, request);
                    }
                    catch (RequestNotFoundException e) {
                        logger.log(Level.ERROR, e);
                    }
                }
            }
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processCallbackForAttributeRequest : start");
    }

    /**
     * Process poller callback for approvalAttemptType EVIDENCE_REQUEST. 
     *
     * @param approvalAttemptPollerData Approval Attempt data received in poller callback
     * @throws AuthException the auth exception
     */
    private void processEvidenceRequestCallback(Session session, ApprovalAttemptPollerTO approvalAttemptPollerData) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processEvidenceRequestCallback : start");
        String enterpriseType = config.getProperty(Constant.ENTERPRISE_TYPE);
        if (enterpriseType.equals("SENDER") || // TODO: enterpriseType.equals("SENDER") added for dev testing on single adapter. should be removed once done.
        // Current Enterprise is the Sender. Hence we consider that poller callback is received on approval by the Receiver. 
            approvalAttemptPollerData.getSenderAccountId().equals(config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID))) {
            //AuthenticationAttempt authenticationAttempt = authenticationAttemptProcessorIntf.updateApprovalAtttempt(approvalAttemptPollerData);
            AuthenticationAttempt authenticationAttempt = null;
            try {
                //authenticationAttempt = authenticationAttemptDao.getAuthAttemptByApplicationIdAndTransactionId(approvalAttemptPollerData.getApplicationId(), approvalAttemptPollerData.getTransactionId());
                authenticationAttempt = authenticationAttemptDao.getAuthAttemptByTransactionId(approvalAttemptPollerData.getTransactionId());
            }
            catch (TransactionNotFoundException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            if (authenticationAttempt != null) {
                authenticationAttempt.setAttemptStatus(approvalAttemptPollerData.getApprovalStatus());
                authAttemptHistoryHandler.updateAuthAttemptHistoryData(session, authenticationAttempt);
                authenticationAttemptDao.update(session, authenticationAttempt);
            }
            if (Constant.APPROVED.equals(authenticationAttempt.getAttemptStatus())) {
                try {
                    String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                    IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
                    Token token = iamExtensionService.getToken(iamExtensionV2);
                    DecryptionDataV2 decryptionData = iamExtensionV2.getDecryptionKey(token, authenticationAttempt.getSignTransactionId(), CI2Type.VERIFY, reqRefNum);
                    decryptAndStoreAttributeData(session, authenticationAttempt.getId(), approvalAttemptPollerData, decryptionData.getDecryptionKey());
                }
                catch (IAMException e) {
                    logger.log(Level.ERROR, e);
                }
            }
        }
        else {
            //Current Enterprise is the receiver. Hence we create new authentication atttempt.
            authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptPollerData);
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processEvidenceRequestCallback : end");
    }

    /**
     * Decrypts the encrypted Attribute data received in Approval attempt poller callback and stores it to AttributeStore. 
     * This function also stores Evidence Data if present.
     * @param authAttemptId Authentication attempt id for which the attribute is to be stored
     * @param approvalAttemptPollerData Approval Attempt data received in poller callback
     * @param decyptionKey key for decrypting the encrypted data
     */
    private void decryptAndStoreAttributeData(Session session, Long authAttemptId, ApprovalAttemptPollerTO approvalAttemptPollerData, String decyptionKey) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " decryptAndStoreAttributeData : start");
        String decryptedData = AES128Impl.decryptDataWithMD5(approvalAttemptPollerData.getEncryptedData(), decyptionKey);
        List<AttributeDataTO> attributeTOs = new Gson().fromJson(decryptedData, new TypeToken<List<AttributeDataTO>>() {
        }.getType());
        logger.log(Level.DEBUG, "<<<<< attributeTOs : >>>>> " + new Gson().toJson(attributeTOs));
        User user = new User();
        try {
            user = userService.getUserByAccountId(approvalAttemptPollerData.getReceiverAccountId());
        }
        catch (UserNotFoundException e) {
            user =  userService.createUser(session, approvalAttemptPollerData.getReceiverAccountId(), UserRole.USER,TwoFactorStatus.DISABLED.toString(), user);
        }
        for (AttributeDataTO attributeDataTO : attributeTOs) {
            logger.log(Level.DEBUG, "<<<<< attributeDataTO : "+new Gson().toJson(attributeDataTO));
            AttributeStore attributeStore = attributeStoreService.saveAttributeData(session, attributeDataTO, authAttemptId, user, true);
            //logger.log(Level.DEBUG, "<<<<< attributeStore : "+new Gson().toJson(attributeStore));
            if (attributeDataTO.getEvidence() != null && !attributeDataTO.getEvidence().isEmpty()) {
                evidenceStoreService.saveEvidence(session, attributeStore, attributeDataTO.getEvidence());
            }
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " decryptAndStoreAttributeData : end");
    }

    /**
     * Update request.
     *
     * @param authenticationAttempt the authentication attempt
     * @throws AuthException the auth exception
     */
    private void updateRequest(Session session, AuthenticationAttempt authenticationAttempt) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " updateRequest : start");
        AttributeDataTO attributeStore = attributeStoreService.getAttribute(authenticationAttempt.getId());
        List<Request> requests = requestDao.getRequests(RequestType.ATTRIBUTE_ADDITION, in.fortytwo42.entities.enums.ApprovalStatus.USER_APPROVAL_PENDING);
        if (requests != null && !requests.isEmpty()) {
            User user = userService.getActiveUser(authenticationAttempt.getReceiverAccountId());
            for (Request request : requests) {
                AttributeTO attributeTO = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);

                if (attributeTO.getId().equals(user.getId()) && attributeTO.getAttributeName().equals(attributeStore.getAttributeName())
                    && attributeTO.getAttributeValue().equals(attributeStore.getAttributeValue())) {
                    in.fortytwo42.entities.enums.ApprovalStatus approvalStatus = authenticationAttempt.getAttemptStatus().equals(Constant.APPROVED)
                                                                                                                                                   ? in.fortytwo42.entities.enums.ApprovalStatus.APPROVED_BY_USER
                                                                                                                                                   : in.fortytwo42.entities.enums.ApprovalStatus.REJECTED_BY_USER;
                    request.setApprovalStatus(approvalStatus);
                    requestDao.update(session, request);
                    break;
                }
            }
        }
        try {
            AttributeStoreDaoIntf attributeStoreDaoIntf = DaoFactory.getAttributeStoreDao();
            AttributeStore attribute = attributeStoreDaoIntf.getAttributeByAuthId(authenticationAttempt.getId());
            attribute.setAttributeState(AttributeState.ACTIVE);
            attributeStoreDaoIntf.update(session, attribute);
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " updateRequest : end");
    }

    /**
     * Creates the attribute addition request.
     *
     * @param userId the user id
     * @param attribute the attribute
     * @return the authentication attempt
     * @throws AuthException the auth exception
     */
    @Override
    public AuthenticationAttempt createAttributeAdditionRequest(Long userId, AttributeDataTO attribute) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createAttributeAdditionRequest : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            User user = userService.getActiveUser(userId);
            String accountId = user.getAccountId();
            String transactionId = authenticationAttemptService.generateTransactionId();
            String transactionSummary = "Attribute Addition Request";
            String transactionDetails = new Date().getTime() + Constant._PIPE + attribute.getAttributeName() + Constant._PIPE + attribute.getAttributeType();
            EncryptionDataTO encryptionDataTO = iamExtension.initiateAttributeClaimSelfSigned(token.getAccountId(), accountId, attribute.getAttributeName(), attribute.getAttributeValue(),
                    attribute.getEvidenceHash(), transactionId, transactionSummary, transactionDetails, token, reqRefNum);
            attribute.setSignTransactionId(encryptionDataTO.getAttributeSignTransactionId());
            String encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(attribute), encryptionDataTO.getEncryptionKey());
            ApprovalAttemptV2 approvalAttempt = new ApprovalAttemptV2.Builder().approvalAttemptType(Constant.ATTRIBUTE_ADDITION)
                    .transactionDetails(transactionDetails).transactionSummary(transactionSummary).transactionId(transactionId).signTransactionId(encryptionDataTO.getSignTransactionId())
                    .approvalStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING).service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL).consumerAccountId(accountId)
                    .encryptedData(encryptedData).approvalAttemptMode(ApprovalAttemptMode.ENTERPRISE_TO_PEER).build();
            ApprovalAttemptTO approvalAttemptTO = iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
            AuthenticationAttempt authenticationAttempt = authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO);
            sessionFactoryUtil.closeSession(session);
            return authenticationAttempt;
        }
        catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createAttributeAdditionRequest : end");
        }
    }

    /**
     * Creates the attribute addition request.
     *
     * @param attributeDataRequestTO the attribute data request TO
     * @return the authentication attempt
     * @throws AuthException the auth exception
     */
    @Override
    public AuthenticationAttempt createAttributeAdditionRequest(Session session, AttributeDataRequestTO attributeDataRequestTO) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createAttributeAdditionRequest : start");
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            // User account is fetched using Search Attribute List.
            String accountId;
            if ((Constant.ADMIN).equals(attributeDataRequestTO.getCallStatus())) {
                User user = userService.getActiveUser(attributeDataRequestTO.getId());
                accountId = user.getAccountId();
            }
            else {
                AccountWE accountWE = iamExtensionService.searchAccount(attributeDataRequestTO.getSearchAttributes(), iamExtension, token);
                accountId = accountWE.getId();
            }
            String transactionId = authenticationAttemptService.generateTransactionId();
            String transactionSummary = "Attribute Addition Request";
            String transactionDetails = new Date().getTime() + Constant._PIPE
                                        + attributeDataRequestTO.getAttributeData().getAttributeName()
                                        + Constant._PIPE
                                        + attributeDataRequestTO.getAttributeData().getAttributeType();
            EncryptionDataTO encryptionDataTO = iamExtension.initiateAttributeClaimSelfSigned(token.getAccountId(), accountId, attributeDataRequestTO.getAttributeData().getAttributeName(),
                    attributeDataRequestTO.getAttributeData().getAttributeValue(),
                    attributeDataRequestTO.getAttributeData().getEvidenceHash(), transactionId, transactionSummary, transactionDetails, token, reqRefNum);
            attributeDataRequestTO.getAttributeData().setSignTransactionId(encryptionDataTO.getAttributeSignTransactionId());
            // Attribute Data is encrypted using Encrypted key received from Crypto. This encrypted data will be sent to user in Approval attempt.
            String encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(attributeDataRequestTO.getAttributeData()), encryptionDataTO.getEncryptionKey());
            //Attribute addition Attempt with encrypted data is generated for user. 
            ApprovalAttemptV2 approvalAttempt = new ApprovalAttemptV2.Builder().approvalAttemptType(Constant.ATTRIBUTE_ADDITION)
                    .transactionDetails(transactionDetails).transactionSummary(transactionSummary).transactionId(transactionId).signTransactionId(encryptionDataTO.getSignTransactionId())
                    .approvalStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING).service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL).consumerAccountId(accountId)
                    .encryptedData(encryptedData).approvalAttemptMode(ApprovalAttemptMode.ENTERPRISE_TO_PEER).build();
            ApprovalAttemptTO approvalAttemptTO = iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
            logger.log(Level.DEBUG, "<<<< approvalAttemptTO : "+new Gson().toJson(approvalAttemptTO));
            return authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createAttributeAdditionRequest : end");
        }
    }

    //TODO: check which data is received from Admin panel.
    /**
     * Creates the edit attribute addition request.
     *
     * @param userId the user id
     * @param attribute the attribute
     * @return the authentication attempt
     * @throws AuthException the auth exception
     */
    /* private User getUser(AttributeTO requestTO, IAMExtensionV2 iamExtension) throws AuthException, IAMException {
        if (requestTO.getId() != null) {
        	return  ProcessorFactory.getUserProcessor().getActiveUser(requestTO.getId());
        }
        else {
            AttributeDataTO attribute = requestTO.getIdentifier();
            String accountId = iamExtension.getAccount(attribute.getAttributeName(), attribute.getAttributeValue()).getId();
            return ProcessorFactory.getUserProcessor().getActiveUser(accountId);
        }
    }*/
    @Override
    public AuthenticationAttempt createEditAttributeAdditionRequest(Session session, Long userId, AttributeDataTO attribute) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createEditAttributeAdditionRequest : end");
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            User user = userService.getActiveUser(userId);
            String accountId = user.getAccountId();
            String transactionId = authenticationAttemptService.generateTransactionId();
            String transactionSummary = "Attribute Addition Request";
            String transactionDetails = new Date().getTime() + Constant._PIPE + attribute.getAttributeName() + Constant._PIPE + attribute.getAttributeType();
            EncryptionDataTO encryptionDataTO = iamExtension.initiateAttributeClaimSelfSigned(token.getAccountId(), accountId, attribute.getAttributeName(), attribute.getAttributeValue(),
                    attribute.getEvidenceHash(), transactionId, transactionSummary, transactionDetails, token, reqRefNum);
            attribute.setSignTransactionId(encryptionDataTO.getAttributeSignTransactionId());
            String encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(attribute), encryptionDataTO.getEncryptionKey());
            ApprovalAttemptV2 approvalAttempt = new ApprovalAttemptV2.Builder().approvalAttemptType(Constant.ATTRIBUTE_UPDATION)
                    .transactionDetails(transactionDetails).transactionSummary(transactionSummary).transactionId(transactionId).signTransactionId(encryptionDataTO.getSignTransactionId())
                    .approvalStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING).service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL).consumerAccountId(accountId)
                    .encryptedData(encryptedData).approvalAttemptMode(ApprovalAttemptMode.ENTERPRISE_TO_PEER).build();
            ApprovalAttemptTO approvalAttemptTO = iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
            return authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createEditAttributeAdditionRequest : end");
        }
    }

    /**
     * Update edit request.
     *
     * @param authenticationAttempt the authentication attempt
     * @throws AuthException the auth exception
     */
    private void updateEditRequest(Session session, AuthenticationAttempt authenticationAttempt) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " updateEditRequest : start");
        AttributeDataTO attributeStore = attributeStoreService.getAttribute(authenticationAttempt.getId());
        List<Request> requests = requestDao.getRequests(RequestType.ATTRIBUTE_UPDATION, in.fortytwo42.entities.enums.ApprovalStatus.USER_APPROVAL_PENDING);
        if (requests != null && !requests.isEmpty()) {
            User user = userService.getActiveUser(authenticationAttempt.getReceiverAccountId());
            for (Request request : requests) {
                AttributeTO attributeTO = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);

                if (attributeTO.getId().equals(user.getId()) && attributeTO.getAttributeName().equals(attributeStore.getAttributeName())
                    && attributeTO.getAttributeValue().equals(attributeStore.getAttributeValue())) {
                    in.fortytwo42.entities.enums.ApprovalStatus approvalStatus = authenticationAttempt.getAttemptStatus().equals(Constant.APPROVED)
                                                                                                                                                   ? in.fortytwo42.entities.enums.ApprovalStatus.APPROVED_BY_USER
                                                                                                                                                   : in.fortytwo42.entities.enums.ApprovalStatus.REJECTED_BY_USER;
                    request.setApprovalStatus(approvalStatus);
                    requestDao.update(session, request);
                    break;
                }
            }
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " updateEditRequest : end");
    }

    @Override
    public PaginatedTO<AuthenticationAttemptHistoryTO> getAuthAttemptAuditTrail(int page, String applicationId, String searchText, Long fromDate, Long toDate) {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " getAuthAttemptAuditTrail : start");
        List<String> userAccountIds = new ArrayList<>();
        if (searchText != null && !searchText.trim().isEmpty()) {
            userAccountIds = attributeStoreService.getUserAccountIdBySearchtext(searchText);
        }
        PaginatedTO<AuthenticationAttemptHistory> list = authenticationAttemptService.getAuthAttemptHistoryForAuditTrail(page, applicationId, searchText, userAccountIds, fromDate, toDate);
        List<AuthenticationAttemptHistoryTO> authAttemptHistToList = new ArrayList<>();
        //    	authAttemptHistToList = new EntityToTOConverter<AuthenticationAttemptHistory, AuthenticationAttemptHistoryTO>().convertEntityListToTOList(list.getList());
        for (AuthenticationAttemptHistory authAttemptHist : list.getList()) {
            AuthenticationAttemptHistoryTO authAttempt = authAttemptHist.convertToTO(true, searchText);
            authAttemptHistToList.add(authAttempt);
        }

        PaginatedTO<AuthenticationAttemptHistoryTO> paginatedList = new PaginatedTO<AuthenticationAttemptHistoryTO>();
        paginatedList.setList(authAttemptHistToList);
        paginatedList.setTotalCount(list.getTotalCount());
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " getAuthAttemptAuditTrail : end");
        return paginatedList;
    }

    private void processEvidenceRequestUserConsentCallback(Session session, ApprovalAttemptPollerTO approvalAttemptPollerData) {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processEvidenceRequestUserConsentCallback : start");
        logger.log(Level.DEBUG, "<<<<< approvalAttemptPollerData : " + new Gson().toJson(approvalAttemptPollerData));
        AuthenticationAttempt authenticationAttempt = null;
        try {
            authenticationAttempt = authenticationAttemptDao.getAuthAttemptByTransactionId(approvalAttemptPollerData.getTransactionId());
        }
        catch (TransactionNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, "<<<<< authenticationAttempt : " + new Gson().toJson(authenticationAttempt));
        if (authenticationAttempt != null) {
            authenticationAttempt.setAttemptStatus(approvalAttemptPollerData.getApprovalStatus());
            authenticationAttempt.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
            logger.log(Level.DEBUG, "<<<<< authenticationAttempt Updated : " + new Gson().toJson(authenticationAttempt));
            authAttemptHistoryHandler.updateAuthAttemptHistoryDataByTrasactionId(session, authenticationAttempt);
            authenticationAttemptDao.remove(session, authenticationAttempt);
        }
        AuthenticationAttempt evidenceRequest = null;
        String transactionId = approvalAttemptPollerData.getTxnDetails().split("Id : ")[1];
        try {
            evidenceRequest = authenticationAttemptDao.getAuthAttemptByTransactionId(transactionId);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        if (evidenceRequest != null) {
            handleEvidenceRequest(session, evidenceRequest, approvalAttemptPollerData.getApprovalStatus());
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " processEvidenceRequestUserConsentCallback : end");
    }

    private void handleEvidenceRequest(Session session, AuthenticationAttempt evidenceRequest, String status) {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleEvidenceRequest : start");
        logger.log(Level.DEBUG, "<<<<< evidenceRequest : " + new Gson().toJson(evidenceRequest));
        logger.log(Level.DEBUG, "<<<<< status : " + status);
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtensionV2);
            String encryptedData = null;
            if (Constant.APPROVED.equals(status)) {
                String[] transactionDetails = evidenceRequest.getTransactionDetails().split("\\|");
                String userIdentifier = transactionDetails[1];
                List<AttributeDataTO> attributeDataTOs = new Gson().fromJson(transactionDetails[0], new TypeToken<ArrayList<AttributeDataTO>>() {
                }.getType());
                AttributeDataTO attributeDataTO = attributeDataTOs.get(0);
                List<AttributeDataTO> attributeTOs = attributeStoreService.getAttributesWithEvidence(userIdentifier, attributeDataTO.getAttributeName());
                String encryptionKey = iamExtensionV2.getEncryptionKeyForSignTransactionId(evidenceRequest.getSignTransactionId(), status, token, reqRefNum);
                encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(attributeTOs), encryptionKey);
            }
            String approvalStatus = Constant.TIMEOUT.equals(status) ? Constant.REJECTED : status;
            logger.log(Level.DEBUG, "<<<<< TransactionId : " + evidenceRequest.getTransactionId());
            logger.log(Level.DEBUG, "<<<<< status : " + status);
            logger.log(Level.DEBUG, "<<<<< encryptedData : " + encryptedData);
            iamExtensionV2.editApprovalAttempt(evidenceRequest.getTransactionId(), status, encryptedData, token);
            //authenticationAttemptProcessorIntf.updateAuthAttempt(authenticationAttempt, evidenceRequestTO.getApprovalStatus());
            evidenceRequest.setAttemptStatus(approvalStatus);
            authenticationAttemptDao.update(session, evidenceRequest);
            authAttemptHistoryHandler.updateAuthAttemptHistoryDataByTrasactionId(session, evidenceRequest);
            //TODO: Added for dev testing on single adapter. To be removed on uat/production.s
            if (config.getProperty(Constant.DEBUG_MODE) != null && Boolean.parseBoolean(config.getProperty(Constant.DEBUG_MODE))) {
                authenticationAttemptDao.remove(session, evidenceRequest);
            }
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " handleEvidenceRequest : end");
        }
    }

    @Override
    public void twoFactorAuthentication(User user, Application application, Service service, UserAuthenticationTO userAuthenticationTO, Integer timeout, AsyncResponse asyncResponse,
            IAMExtensionV2 iamExtension, Token token) throws AuthException, IAMException {
        // TODO Auto-generated method stub

    }

    @Override
    public AuthenticationAttempt createAttributeEditRequest(Session session, AttributeDataRequestTO attributeDataRequestTO) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createAttributeEditRequest : start");
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
                Token token = iamExtensionService.getToken(iamExtension);
                String accountId;
                if (attributeDataRequestTO.getCallStatus().equals(Constant.ADMIN)) {
                    User user = userService.getActiveUser(IamThreadContext.getSessionWithoutTransaction(), attributeDataRequestTO.getId());
                    accountId = user.getAccountId();
                }
                else {
                long startTime = System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->AuthAttemptFacadeImpl -> createAttributeEditRequest -iamExtensionService.searchAccount |Epoch:"+startTime);
                AccountWE accountWE = iamExtensionService.searchAccount(attributeDataRequestTO.getSearchAttributes(), iamExtension, token);
                long endTime = System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->AuthAttemptFacadeImpl -> createAttributeEditRequest -iamExtensionService.searchAccount |Epoch:"+endTime);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF "+(endTime-startTime));
                accountId = accountWE.getId();
            }
            String transactionId = authenticationAttemptService.generateTransactionId();
            String transactionSummary = AttributeAction.UPDATE.equals(attributeDataRequestTO.getAttributeData().getAttributeAction()) ? "Attribute Edit Request" : "Attribute Delete Request";
            String attributeType = attributeDataRequestTO.getAttributeData().getAttributeType() == null ? (iamExtensionService
                    .getAttributeMetadata(attributeDataRequestTO.getAttributeData().getAttributeName()).getAttributeType().name()) : attributeDataRequestTO.getAttributeData().getAttributeType();
            String transactionDetails = new Date().getTime() + Constant._PIPE
                                        + attributeDataRequestTO.getAttributeData().getAttributeName()
                                        + Constant._PIPE
                                        + attributeType;
            EncryptionDataTO encryptionDataTO = iamExtension.initiateAttributeClaimSelfSigned(token.getAccountId(), accountId, attributeDataRequestTO.getAttributeData().getAttributeName(),
                    attributeDataRequestTO.getAttributeData().getAttributeValue(),
                    attributeDataRequestTO.getAttributeData().getEvidenceHash(), transactionId, transactionSummary, transactionDetails, token, reqRefNum);
            attributeDataRequestTO.getAttributeData().setSignTransactionId(encryptionDataTO.getAttributeSignTransactionId());

            String encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(attributeDataRequestTO.getAttributeData()), encryptionDataTO.getEncryptionKey());
            String approvalAttemptType = AttributeAction.UPDATE.equals(attributeDataRequestTO.getAttributeData().getAttributeAction()) ? Constant.ATTRIBUTE_EDIT : Constant.ATTRIBUTE_DELETE;
            ApprovalAttemptV2 approvalAttempt = new ApprovalAttemptV2.Builder().approvalAttemptType(approvalAttemptType)
                    .transactionDetails(transactionDetails).transactionSummary(transactionSummary).transactionId(transactionId).signTransactionId(encryptionDataTO.getSignTransactionId())
                    .approvalStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING).service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL).consumerAccountId(accountId)
                    .encryptedData(encryptedData).approvalAttemptMode(ApprovalAttemptMode.ENTERPRISE_TO_PEER).build();
            ApprovalAttemptTO approvalAttemptTO = iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
            logger.log(Level.DEBUG, "<<<< approvalAttemptTO : "+new Gson().toJson(approvalAttemptTO));
            return authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createAttributeEditRequest : end");
        }
    }

    @Override
    public QRCodeDataTO createQRBasedApprovalAttempt(AsyncResponse asyncResponse, AuthenticationAttemptTO approvalAttemptTO, String applicationId) throws AuthException, UserBlockedException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createQRBasedApprovalAttempt : start");
        if(!approvalAttemptTO.getApprovalAttemptType().equals(IAMConstants.QR)){
            throw  new AuthException(null,errorConstant.getERROR_CODE_INVALID_DATA(),"Approval attempt type should be QR only");
        }
        AuthenticationAttemptTO authenticationAttemptTO = createApprovalAttempt(asyncResponse, approvalAttemptTO, applicationId);
        logger.log(Level.DEBUG, "<<<<< txn - authenticationAttemptTO : "+new Gson().toJson(authenticationAttemptTO));
        QRCodeTO qrCodeTO = new QRCodeTO();
        qrCodeTO.setApprovalAttemptId(authenticationAttemptTO.getApprovalAttemptId());
        String imageData = FacadeFactory.getQRCodeFacade().generateQRCode(new Gson().toJson(qrCodeTO));
        QRCodeDataTO qrCodeDataTO = new QRCodeDataTO();
        qrCodeDataTO.setQrCode(imageData);
        qrCodeDataTO.setTransactionId(authenticationAttemptTO.getTransactionId());
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createQRBasedApprovalAttempt : end");
        AuditLogUtil.sendAuditLog(approvalAttemptTO.getApprovalAttemptType()+ " qr based approval attempt generated successfully ", "USER", ActionType.ONBOARD, "", IdType.ACCOUNT, "", null, "", null);
        return qrCodeDataTO;
    }

    @Override
    public QRCodeDataTO createQRBasedApprovalAttemptV4(AsyncResponse asyncResponse, AuthenticationAttemptTO approvalAttemptTO, String applicationId) throws AuthException, UserBlockedException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createQRBasedApprovalAttemptV4 : start");
        if(!approvalAttemptTO.getApprovalAttemptType().equals(IAMConstants.QR)){
            throw  new AuthException(null,errorConstant.getERROR_CODE_INVALID_DATA(),"Approval attempt type should be QR only");
        }
        AuthenticationAttemptTO authenticationAttemptTO = createApprovalAttemptV4(asyncResponse, approvalAttemptTO, applicationId);
        logger.log(Level.DEBUG, "<<<<< txn - authenticationAttemptTO : "+new Gson().toJson(authenticationAttemptTO));
        QRCodeTO qrCodeTO = new QRCodeTO();
        qrCodeTO.setApprovalAttemptId(authenticationAttemptTO.getApprovalAttemptId());
        String imageData = FacadeFactory.getQRCodeFacade().generateQRCode(new Gson().toJson(qrCodeTO));
        QRCodeDataTO qrCodeDataTO = new QRCodeDataTO();
        qrCodeDataTO.setQrCode(imageData);
        qrCodeDataTO.setTransactionId(authenticationAttemptTO.getTransactionId());
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " createQRBasedApprovalAttemptV4 : end");
        return qrCodeDataTO;
    }

    /**
     * Validate user.
     *
     * @param user the user
     * @throws UserBlockedException the user blocked exception
     * @throws AuthException the auth exception
     */
    private void validateUser(User user) throws UserBlockedException, AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " validateUser : start");
        if (user.getUserStatus() == UserStatus.BLOCK) {
            throw new UserBlockedException();
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " validateUser : end");
    }

    public void deleteQRTrxFromAuthAttempt() throws AuthException{
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " deleteQRTrxFromAuthAttempt : start");
        try{
            iamExtensionService.deleteQRTrxFromAuthAttempt();
        } catch (AuthException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " deleteQRTrxFromAuthAttempt : start");
    }


    @Override
    public in.fortytwo42.tos.transferobj.PaginatedTO<AuthenticationAttemptHistoryTO> getTransactionDetails(TransactionReportRequestTO requestTO) throws AuthException {
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " getTransactionDetails : start");
        in.fortytwo42.tos.transferobj.PaginatedTO<AuthenticationAttemptHistoryTO> authenticationAttemptHistoryTOs = authenticationAttemptService.getTransactionDetails(requestTO);
        logger.log(Level.DEBUG, ATUTH_ATTEMPT_FACADE_IMPL_LOG + " getTransactionDetails : end");
        return authenticationAttemptHistoryTOs;
    }
}
