
package in.fortytwo42.adapter.facade;

import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import in.fortytwo42.adapter.enums.AttributeStatus;
import in.fortytwo42.adapter.service.*;
import in.fortytwo42.daos.dao.UserApplicationRelDaoImpl;
import in.fortytwo42.entities.bean.*;
import in.fortytwo42.entities.enums.AdminSessionState;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.bson.types.ObjectId;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dev.samstevens.totp.code.HashingAlgorithm;
import in.fortytwo42.adapter.cam.dto.CamAttribute;
import in.fortytwo42.adapter.cam.dto.Credential;
import in.fortytwo42.adapter.cam.dto.EditUserRequest;
import in.fortytwo42.adapter.cam.dto.ResetPasswordUserRequest;
import in.fortytwo42.adapter.cam.dto.UserCreationRequest;
import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeImpl;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeIntf;
import in.fortytwo42.adapter.controller.AddAttributeFactory;
import in.fortytwo42.adapter.controller.OnboardUserFactory;
import in.fortytwo42.adapter.controller.Onboarder;
import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.enums.AddAttributeType;
import in.fortytwo42.adapter.enums.OnboardUserType;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.jar.MongoConnectionManager;
import in.fortytwo42.adapter.jar.MongoConnectionManagerIam;
import in.fortytwo42.adapter.jar.entities.Consumer;
import in.fortytwo42.adapter.transferobj.ADUserBindingTO;
import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.CryptoTokenTO;
import in.fortytwo42.adapter.transferobj.EvidenceRequestTO;
import in.fortytwo42.adapter.transferobj.KeyValueTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.PasswordTO;
import in.fortytwo42.adapter.transferobj.SenderDetailTO;
import in.fortytwo42.adapter.transferobj.UserAttributeTO;
import in.fortytwo42.adapter.transferobj.UserAuthenticationTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.transferobj.UserDataTO;
import in.fortytwo42.adapter.transferobj.UserIciciStatusTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.transferobj.UserResponseTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AttributeValidationUtil;
import in.fortytwo42.adapter.util.AuditLogConstant;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.FileDownloader;
import in.fortytwo42.adapter.util.FileUtil;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.TOTPUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.factory.CsvFactory;
import in.fortytwo42.adapter.util.handler.AuthAttemptHistoryHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.dao.ApplicationDaoImpl;
import in.fortytwo42.daos.dao.AttributeStoreDaoImpl;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.AuthenticationAttemptDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RoleDaoIntf;
import in.fortytwo42.daos.dao.ServiceDaoIntf;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.dao.UserAuthPrincipalDaoIntf;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.RequestNotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.exception.UserApplicationRelNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.ApprovalAttemptV2;
import in.fortytwo42.enterprise.extension.core.BindingInfoV2;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.enums.ApprovalAttemptMode;
import in.fortytwo42.enterprise.extension.enums.AttributeOperationStatus;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.enums.AttributeValueModel;
import in.fortytwo42.enterprise.extension.enums.CryptoEntityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.exceptions.UserFoundOnIDSException;
import in.fortytwo42.enterprise.extension.exceptions.ValidationException;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.tos.GenerateAttributeClaimSelfSignedTO;
import in.fortytwo42.enterprise.extension.tos.QuestionAnswerTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.utils.RandomString;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.enterprise.extension.webentities.DeviceTO;
import in.fortytwo42.enterprise.extension.webentities.PasswordPolicyWE;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;
import in.fortytwo42.entities.enums.ApplicationType;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.entities.enums.AuthenticationStatus;
import in.fortytwo42.entities.enums.IAMStatus;
import in.fortytwo42.entities.enums.LoginStatus;
import in.fortytwo42.entities.enums.OnboardStatus;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.enums.SessionState;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.entities.enums.UserState;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.ids.entities.beans.Account;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.integration.producer.JMSProducerExtension;
import in.fortytwo42.tos.enums.AttributeAction;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.NotificationStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.AdHotpTO;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.NotificationTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserTO;
import in.fortytwo42.tos.transferobj.VerifierTO;

/**
 * The Class UserFacadeImpl.
 */
public class UserFacadeImpl implements UserFacadeIntf {

    private static final String USER_FACADE_IMPL_LOG = "<<<<< UserFacadeImpl";

    private static Logger logger= LogManager.getLogger(UserFacadeImpl.class);
    private AuthenticationAttemptServiceIntf authenticationAttemptService = ServiceFactory.getAuthenticationService();
    private AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private UserServiceIntf userService = ServiceFactory.getUserService();
    private UserAuthPrincipalServiceIntf userAuthPrincipalService = ServiceFactory.getUserAuthPrincipalService();
    private AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
    private UserApplicationRelServiceIntf userApplicationRelService = ServiceFactory.getUserApplicationRelService();

    private UserLockServiceIntf userLockService = ServiceFactory.getUserLockService();
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    private AttributeStoreFacadeIntf attributeStoreFacadeIntf = FacadeFactory.getAttributeFacade();
    private ServiceProcessorIntf serviceProcessor = ServiceFactory.getServiceProcessor();
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private AuthenticationAttemptDaoIntf authenticationAttemptDao = DaoFactory.getAuthenticationAttemptDao();
    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    private UserAuthPrincipalDaoIntf userAuthPrincipalDao = DaoFactory.getUserAuthPrincipalDao();
    private UserApplicationRelDaoIntf userApplicationRelDao = DaoFactory.getUserApplicationRel();
    private UserDaoIntf userDao = DaoFactory.getUserDao();
    private ServiceDaoIntf serviceDao = DaoFactory.getServiceDao();
    private AuthAttemptHistoryHandler authAttemptHistoryHandler = AuthAttemptHistoryHandler.getInstance();
    private Config config = Config.getInstance();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();
    private PermissionUtil permissionUtil = PermissionUtil.getInstance();
    private IAMUtil iamUtil = IAMUtil.getInstance();
    private CamUserFacadeIntf camUserFacade = CamUserFacadeImpl.getInstance();
    private RoleDaoIntf roleDao = DaoFactory.getRoleDao();
    private AdminLoginLogServiceIntf adminLoginLogService = ServiceFactory.getAdminLoginLogService();

    private final ExecutorService pool;

    private MongoConnectionManager idsMongoConnectionManager =  MongoConnectionManager.getInstance();
    private MongoConnectionManagerIam iamMongoConnectionManager = MongoConnectionManagerIam.getInstance();
    private UserFacadeImpl() {
        super();
        int poolSize = 10;
        try {
            poolSize = Integer.parseInt(config.getProperty(Constant.CSV_PROCESSING_THREAD_POOL_SIZE));
        }
        catch (NumberFormatException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        private static final UserFacadeImpl INSTANCE = new UserFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of UserFacadeImpl.
     *
     * @return single instance of UserFacadeImpl
     */
    public static UserFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Gets the user attribute names.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @return the user attribute names
     * @throws AuthException the auth exception
     */
    @Override
    public UserTO getUserAttributeNames(String attributeName, String attributeValue) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUserAttributeNames : start");
        long startTimeProcessAN = System.currentTimeMillis();
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->UserFacadeImpl -> getAllUserAttributesNames -iamExtensionService.getAllUserAttributesNames |Epoch:"+startTimeProcessAN);
        AccountWE accountWE = iamExtensionService.getAllUserAttributesNames(attributeName.toUpperCase(), attributeValue);
        long endTimeProcessAN = System.currentTimeMillis();
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserFacadeImpl -> getAllUserAttributesNames -iamExtensionService.getAllUserAttributesNames |Epoch:"+endTimeProcessAN);
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF "+(endTimeProcessAN-startTimeProcessAN));

        if (accountWE == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUserAttributeNames : end");
        return iamExtensionService.convertToUserTO(accountWE);
    }

    /**
     * Gets the user attributes from db.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @return the user attributes from db
     * @throws AuthException the auth exception
     */
    @Override
    public UserTO getUserAttributesFromDb(String attributeName, String attributeValue) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUserAttributesFromDb : start");
        IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
        String accountId = iamExtensionService.getAccountId(attributeName.toUpperCase(), attributeValue, iamExtensionV2);
        if (accountId == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        AccountWE accountWE = iamExtensionService.getAllUserAttributesNames(attributeName.toUpperCase(), attributeValue);
        UserTO userTO = getAttributes(accountId, accountWE);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUserAttributesFromDb : end");
        return userTO;
    }

    /**
     * Gets the user attributes from db.
     *
     * @param accountId the account id
     * @param role      the role
     * @param actor     the actor
     * @return the user attributes from db
     * @throws AuthException the auth exception
     */
    @Override
    public UserDataTO getUserAttributesFromDb(String accountId, String role, String actor) throws AuthException {
        //TODO: Add role-permission validation.
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUserAttributesFromDb : start");
        if (accountId == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        AccountWE accountWE = iamExtensionService.getAllAttributesForAccount(accountId);
        List<AttributeDataTO> attributeDataTOs = attributeStoreService.getUserAttributes(accountId);
        for (AttributeTO attributeTO : accountWE.getAttributes()) {
            System.out.println("***** in for loop " + attributeTO.getAttributeName() + "  " + attributeTO.getAttributeValue());
            AttributeDataTO attributeData = new AttributeDataTO();
            attributeData.setSignTransactionId(attributeTO.getSignTransactionId());
            System.out.println("*** signTransactionId " + attributeTO.getSignTransactionId());
            int index = attributeDataTOs.indexOf(attributeData);
            System.out.println("*** index " + index);
            if (index >= 0) {
                AttributeDataTO attributeDataTO = attributeDataTOs.get(index);
                if (attributeTO.getStatus() != null) {
                    attributeDataTO.setStatus(attributeTO.getStatus());
                }
            }
            /*else {
                attributeData.setAttributeName(attributeTO.getAttributeName());
                attributeData.setAttributeType(attributeTO.getAttributeType());
                if (attributeTO.getStatus() != null) {
                    attributeData.setStatus(attributeTO.getStatus());
                }
                attributeDataTOs.add(attributeData);
            }*/
        }
        List<UserAttributeTO> userAttributes = groupAttributes(attributeDataTOs);
        UserDataTO userDataTO = new UserDataTO();
        userDataTO.setUserIdentifier(accountId);
        userDataTO.setAttributes(userAttributes);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUserAttributesFromDb : end");
        return userDataTO;
    }

    /**
     * Group attributes.
     *
     * @param attributeDataTOs the attribute data T os
     * @return the list
     */
    private List<UserAttributeTO> groupAttributes(List<AttributeDataTO> attributeDataTOs) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " groupAttributes : start");
        List<UserAttributeTO> userAttributes = new ArrayList<>();
        try {
            for (AttributeDataTO attributeDataTO : attributeDataTOs) {
                String attributeName = attributeDataTO.getAttributeName();
                UserAttributeTO userAttributeTO = new UserAttributeTO();
                userAttributeTO.setAttributeName(attributeName);
                int index = userAttributes.indexOf(userAttributeTO);
                if (index >= 0) {
                    UserAttributeTO userAttribute = userAttributes.get(index);
                    attributeDataTO.setAttributeName(null);
                    userAttribute.getAttributeData().add(attributeDataTO);
                }
                else {
                    List<AttributeDataTO> attributes = new ArrayList<>();
                    attributeDataTO.setAttributeName(null);
                    attributes.add(attributeDataTO);
                    userAttributeTO.setAttributeData(attributes);
                    userAttributes.add(userAttributeTO);
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " groupAttributes : end");
        }
        return userAttributes;
    }

    /**
     * Request attribute from user.
     *
     * @param userTO the user TO
     * @return the user TO
     * @throws AuthException the auth exception
     */
    @Override
    public UserTO requestAttributeFromUser(UserTO userTO) throws AuthException {
        AccountWE userAccount = iamExtensionService.getAllAttributesForAccount(userTO.getUserIdentifier());
        if (userAccount == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        boolean isAttributePresent = false;
        for (AttributeTO attribute : userAccount.getAttributes()) {
            if (attribute.getAttributeName().equals(userTO.getAttributes().get(0).getAttributeName().toUpperCase())) {
                isAttributePresent = true;
            }
        }
        if (!isAttributePresent) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT());
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            AuthenticationAttempt authenticationAttempt = requestAttribute(session, userTO, RequestType.ATTRIBUTE_DEMAND.name());
            userTO.setId(authenticationAttempt.getId());
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
        return userTO;
    }

    /**
     * Gets the attribute request.
     *
     * @param authAttemptId the auth attempt id
     * @return the attribute request
     * @throws AuthException              the auth exception
     * @throws AttributeNotFoundException the attribute not found exception
     */
    @Override
    public UserTO getAttributeRequest(Long authAttemptId) throws AuthException, AttributeNotFoundException {
        AuthenticationAttemptHistory authenticationAttemptHistory;
        authenticationAttemptHistory = authenticationAttemptService.getAuthAttemptHistoryBySourceId(authAttemptId);
        UserTO userTO = new UserTO();
        if (authenticationAttemptHistory.getAttemptType().equals(Constant.ATTRIBUTE_DEMAND)) {
            userTO.setId(authAttemptId);
            userTO.setUserIdentifier(authenticationAttemptHistory.getReceiverAccountId());
            if (authenticationAttemptHistory.getAttemptStatus().equals(Constant.APPROVED)) {
                List<AttributeDataTO> attributeDataTOs = getAttributeList(authAttemptId, authenticationAttemptHistory.getReceiverAccountId());
                userTO.setAttributes(attributeDataTOs);
            }
            else {
                String[] details = authenticationAttemptHistory.getTransactionDetails().split("\\|");
                userTO.setAttributes(new Gson().fromJson(details[0], new TypeToken<List<AttributeDataTO>>() {
                }.getType()));
            }
            userTO.setStatus(authenticationAttemptHistory.getAttemptStatus());
            return userTO;
        }
        else {
            throw new AuthException(null,errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(), errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
        }
    }

    /**
     * Gets the evidence request.
     *
     * @param authAttemptId the auth attempt id
     * @return the evidence request
     * @throws AuthException the auth exception
     */
    @Override
    public EvidenceRequestTO getEvidenceRequest(Long authAttemptId) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getEvidenceRequest : start");
        AuthenticationAttemptHistory authenticationAttemptHistory = null;
        authenticationAttemptHistory = authenticationAttemptService.getAuthAttemptHistoryBySourceId(authAttemptId);
        if (authenticationAttemptHistory.getAttemptType().equals(Constant.EVIDENCE_REQUEST)) {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getEvidenceRequest : end");
            return getCreatedEvidenceRequestById(authenticationAttemptHistory);
        }
        else {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getEvidenceRequest : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(), errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
        }
    }

    /**
     * Gets the attribute list.
     *
     * @param authAttemptId  the auth attempt id
     * @param userIdentifier the user identifier
     * @return the attribute list
     * @throws AuthException the auth exception
     */
    private List<AttributeDataTO> getAttributeList(Long authAttemptId, String userIdentifier) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAttributeList : start");
        List<AttributeDataTO> attributeDataTOs = attributeStoreService.getAttributeList(authAttemptId);
        AccountWE accountWE = iamExtensionService.getAllAttributesForAccount(userIdentifier);
        List<AttributeTO> attributTOList = accountWE.getAttributes();
        for (AttributeDataTO attributeDataTO : attributeDataTOs) {
            AttributeTO attributeTO = new AttributeTO();
            attributeTO.setSignTransactionId(attributeDataTO.getSignTransactionId());
            int index = attributTOList.indexOf(attributeTO);
            if (index >= 0) {
                attributeTO = attributTOList.get(index);
                attributeDataTO.setVerifiers(iamExtensionService.convertToVerifierTO(attributeTO.getVerifiers()));
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAttributeList : end");
        return attributeDataTOs;
    }

    /**
     * Request evidence from user verifier.
     *
     * @param userTO the user TO
     * @return the user TO
     * @throws AuthException the auth exception
     */
    @Override
    public UserTO requestEvidenceFromUserVerifier(UserTO userTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " requestEvidenceFromUserVerifier : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            // Considering single attribute and single verifier
            AttributeDataTO attributeDataTO = userTO.getAttributes().get(0);
            attributeNameToUpperCase(attributeDataTO);
            VerifierTO verifierTO = attributeDataTO.getVerifiers().get(0);

            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            String transactionId = authenticationAttemptService.generateTransactionId();
            String transactionSummary = "Evidence Request";
            List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
            for (AttributeDataTO attribute : userTO.getAttributes()) {
                attribute.setAttributeName(attribute.getAttributeName().toUpperCase());
                AttributeDataTO attributeData = new AttributeDataTO();
                attributeData.setAttributeName(attribute.getAttributeName());
                attributeDataTOs.add(attributeData);
            }
            validateEvidenceRequest(attributeDataTO, verifierTO, userTO);
            String transactionDetails = new Gson().toJson(attributeDataTOs) + "|" + userTO.getUserIdentifier();
            ApprovalAttemptV2 approvalAttempt = new ApprovalAttemptV2.Builder().approvalAttemptMode(ApprovalAttemptMode.ENTERPRISE_TO_ENTERPRISE).approvalAttemptType(Constant.EVIDENCE_REQUEST)
                    .transactionDetails(transactionDetails).transactionSummary(transactionSummary).transactionId(transactionId)
                    .approvalStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING).service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL)
                    .enterpriseAccountId(verifierTO.getVerifierAccountId())
                    .build();
            ApprovalAttemptTO approvalAttemptTO = iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
            AuthenticationAttempt authenticationAttempt = authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO);
            authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
            userTO.setId(authenticationAttempt.getId());
            sessionFactoryUtil.closeSession(session);
            return userTO;
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
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " requestEvidenceFromUserVerifier : end");
        }
    }

    /**
     * Approve or reject evidence request.
     *
     * @param evidenceRequestTO the evidence request TO
     * @return the evidence request TO
     * @throws AuthException the auth exception
     */
    @Override
    public EvidenceRequestTO approveOrRejectEvidenceRequest(EvidenceRequestTO evidenceRequestTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveOrRejectEvidenceRequest : start");
        AuthenticationAttempt authenticationAttempt = null;
        try {
            authenticationAttempt = authenticationAttemptService.getAuthAttemptById(evidenceRequestTO.getId());
        }
        catch (AuthException e) {
            throw new AuthException(null,errorConstant.getERROR_CODE_INVALID_REQUEST(),errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
        }
        if (authenticationAttempt.getAttemptStatus().equals(Constant.USER_APPROVAL_PENDING)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER(), errorConstant.getERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER());
        }
        if (authenticationAttempt.getAttemptStatus().equals(Constant.APPROVED)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_EVIDENCE_REQUEST_ALREADY_APPROVED(), errorConstant.getERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_APPROVED());
        }
        if (authenticationAttempt.getAttemptStatus().equals(Constant.REJECTED)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_EVIDENCE_REQUEST_ALREADY_REJECTED(), errorConstant.getERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_REJECTED());
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtensionV2);
            String encryptedData = null;
            if (Constant.APPROVED.equals(evidenceRequestTO.getApprovalStatus())) {
                evidenceRequestTO.setStatus(Constant.USER_APPROVAL_PENDING);
                String transactionId = authenticationAttemptService.generateTransactionId();
                String transactionSummary = "Evidence Request";
                String[] transactionDetailsData = authenticationAttempt.getTransactionDetails().split("\\|");
                String userAccountId = transactionDetailsData[1];
                List<AttributeDataTO> attributeDataTOs = new Gson().fromJson(transactionDetailsData[0], new TypeToken<ArrayList<AttributeDataTO>>() {
                }.getType());
                AttributeDataTO attributeDataTO = attributeDataTOs.get(0);
                SenderDetailTO senderDetails = new Gson().fromJson(authenticationAttempt.getSenderIdDetails(), SenderDetailTO.class);
                String transactionDetail = senderDetails.getNAME() + " has requested data for attribute : " + attributeDataTO.getAttributeName() + " - ";
                StringBuilder transactionDetailBuilder = new StringBuilder();
                transactionDetailBuilder.append(senderDetails.getNAME() + " has requested data for attribute : " + attributeDataTO.getAttributeName() + " - ");
                List<AttributeDataTO> attributeTOs = attributeStoreService.getAttributesWithEvidence(userAccountId, attributeDataTO.getAttributeName());
                for (AttributeDataTO attribute : attributeTOs) {
                    transactionDetailBuilder.append(attribute.getAttributeValue() + Constant._COMMA);
                }
                transactionDetailBuilder.append(" with reference transaction Id : " + authenticationAttempt.getTransactionId());
                transactionDetail = transactionDetailBuilder.toString();
                int timeout = Integer.parseInt(config.getProperty(Constant.ENTERPRISE_TO_PEER_TIMEOUT));
                ApprovalAttemptV2 approvalAttempt = new ApprovalAttemptV2.Builder().approvalAttemptType(Constant.EVIDENCE_REQUEST_USER_CONSENT)
                        .transactionDetails(transactionDetail).transactionSummary(transactionSummary).transactionId(transactionId).signTransactionId(authenticationAttempt.getSignTransactionId())
                        .approvalStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING).service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL)
                        .consumerAccountId(userAccountId)
                        .encryptedData(encryptedData).approvalAttemptMode(ApprovalAttemptMode.ENTERPRISE_TO_PEER).timeOut(timeout).build();
                ApprovalAttemptTO approvalAttemptTO = iamExtensionV2.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
                AuthenticationAttempt authAttempt = authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO);
                authAttemptHistoryHandler.logAuthAttemptHistoryData(authAttempt);
                authenticationAttemptService.updateAuthAttempt(session, authenticationAttempt, Constant.USER_APPROVAL_PENDING);
                /*String userIdentifier = transactionDetails[1];
                List<AttributeDataTO> attributeDataTOs = new Gson().fromJson(transactionDetails[0],new TypeToken<ArrayList<AttributeDataTO>>(){}.getType());
                AttributeDataTO attributeDataTO = attributeDataTOs.get(0);
                List<AttributeDataTO> attributeTOs = attributeStoreProcessorIntf.getAttributes(userIdentifier, attributeDataTO.getAttributeName());
                String encryptionKey = iamExtensionV2.getEncryptionKeyForSignTransactionId(authenticationAttempt.getSignTransactionId(), evidenceRequestTO.getApprovalStatus(), token);
                encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(attributeTOs), encryptionKey);*/
            }
            else {
                iamExtensionV2.editApprovalAttempt(authenticationAttempt.getTransactionId(), evidenceRequestTO.getApprovalStatus(), encryptedData, token);
                //authenticationAttemptProcessorIntf.updateAuthAttempt(authenticationAttempt, evidenceRequestTO.getApprovalStatus());
                if (authenticationAttempt != null) {
                    authenticationAttempt.setAttemptStatus(evidenceRequestTO.getApprovalStatus());
                    authenticationAttempt.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
                    authAttemptHistoryHandler.updateAuthAttemptHistoryDataByTrasactionId(session, authenticationAttempt);
                    authenticationAttemptDao.remove(session, authenticationAttempt);
                }
                evidenceRequestTO.setStatus(Constant.SUCCESS_STATUS);
                //TODO: Added for dev testing on single adapter. To be removed on uat/production.s
                if (config.getProperty(Constant.DEBUG_MODE) != null && Boolean.parseBoolean(config.getProperty(Constant.DEBUG_MODE))) {
                    authenticationAttemptDao.remove(session, authenticationAttempt);
                }
            }
            sessionFactoryUtil.closeSession(session);
            return evidenceRequestTO;
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
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveOrRejectEvidenceRequest : end");
        }
    }

    /**
     * Gets the pending evidence request.
     *
     * @param requestType the request type
     * @param limit       the limit
     * @param offset      the offset
     * @return the pending evidence request
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<EvidenceRequestTO> getPendingEvidenceRequest(String requestType, int limit, int offset) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getPendingEvidenceRequest : start");
        if (requestType == null || Constant.RECEIVED.equalsIgnoreCase(requestType)) {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getPendingEvidenceRequest : end");
            return getEvidenceRequestReceivedByMe(limit, offset);
        }
        else {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getPendingEvidenceRequest : end");
            return getEvidenceRequestCreatedByMe(limit, offset);
        }
    }

    /**
     * Returns list of pending EVIDENCE_REQUEST received by the Enterprise from another Enterprise.
     *
     * @param limit  limit value
     * @param offset offset value
     * @return list of EVIDENCE_REQUEST
     */
    private PaginatedTO<EvidenceRequestTO> getEvidenceRequestReceivedByMe(int limit, int offset) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getEvidenceRequestReceivedByMe : start");
        PaginatedTO<EvidenceRequestTO> pendingAuthAttempt = new PaginatedTO<>();
        try {
            List<AuthenticationAttempt> authenticationAttempts = authenticationAttemptService.getReceivedAuthenticationRequests(Constant.EVIDENCE_REQUEST, Constant.PENDING, limit, offset, null);
            List<EvidenceRequestTO> evidenceRequests = new ArrayList<>();
            for (AuthenticationAttempt authenticationAttempt : authenticationAttempts) {
                EvidenceRequestTO evidenceRequest = getReceivedEvidenceRequestById(authenticationAttempt);
                evidenceRequests.add(evidenceRequest);
            }
            pendingAuthAttempt.setTotalCount(authenticationAttemptService.getTotalCountOfReceivedRequests(Constant.EVIDENCE_REQUEST, Constant.PENDING, null));
            pendingAuthAttempt.setList(evidenceRequests);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getEvidenceRequestReceivedByMe : end");
        return pendingAuthAttempt;
    }

    /**
     * Fetch approval status for EVIDENCE_REQUEST received by the Enterprise from another Enterprise.
     *
     * @param authenticationAttempt EVIDENCE_REQUEST authentication attempt
     * @return Request details with approval status
     */
    private EvidenceRequestTO getReceivedEvidenceRequestById(AuthenticationAttempt authenticationAttempt) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getReceivedEvidenceRequestById : start");
        EvidenceRequestTO evidenceRequest = new EvidenceRequestTO();
        String[] transactionDetails = authenticationAttempt.getTransactionDetails().split("\\|");
        evidenceRequest.setId(authenticationAttempt.getId());
        evidenceRequest.setAttributes(new Gson().fromJson(transactionDetails[0], new TypeToken<ArrayList<AttributeDataTO>>() {
        }.getType()));
        evidenceRequest.setUserIdentifier(transactionDetails[1]);
        evidenceRequest.setRequestorIdentifier(authenticationAttempt.getSenderAccountId());
        Map<String, String> senderDetails = new Gson().fromJson(authenticationAttempt.getSenderIdDetails(), new TypeToken<Map<String, String>>() {
        }.getType());
        evidenceRequest.setRequestorId(senderDetails.get(Constant.ID));
        evidenceRequest.setRequestorName(senderDetails.get(Constant.NAME));
        evidenceRequest.setApprovalStatus(authenticationAttempt.getAttemptStatus());
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getReceivedEvidenceRequestById : end");
        return evidenceRequest;
    }

    /**
     * Returns list of pending EVIDENCE_REQUEST sent the Enterprise to the verifier.
     *
     * @param limit  limit value
     * @param offset offset value
     * @return list of EVIDENCE_REQUEST
     * @throws AuthException Incase of failure in processing any request.
     */
    private PaginatedTO<EvidenceRequestTO> getEvidenceRequestCreatedByMe(int limit, int offset) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getEvidenceRequestCreatedByMe : start");
        PaginatedTO<EvidenceRequestTO> pendingAuthAttempt = new PaginatedTO<>();
        try {
            List<AuthenticationAttempt> authenticationAttempts = authenticationAttemptService.getSentAuthenticationRequests(Constant.EVIDENCE_REQUEST, Constant.PENDING, limit, offset, null);
            List<EvidenceRequestTO> evidenceRequestTOs = new ArrayList<>();
            for (AuthenticationAttempt authenticationAttempt : authenticationAttempts) {
                EvidenceRequestTO evidenceRequestTO = getCreatedEvidenceRequestById(authenticationAttempt);
                evidenceRequestTOs.add(evidenceRequestTO);
            }
            pendingAuthAttempt.setTotalCount(authenticationAttemptService.getTotalCountOfSentRequests(Constant.EVIDENCE_REQUEST, Constant.PENDING, null));
            pendingAuthAttempt.setList(evidenceRequestTOs);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getEvidenceRequestCreatedByMe : end");
        return pendingAuthAttempt;
    }

    /**
     * Fetch approval status for EVIDENCE_REQUEST created by the Enterprise for the verifier.
     *
     * @param authenticationAttempt EVIDENCE_REQUEST authentication attempt
     * @return Request details with approval status
     * @throws AuthException If Attribute data is not present for the request with approval status APPROVED.
     */
    private EvidenceRequestTO getCreatedEvidenceRequestById(AuthenticationAttempt authenticationAttempt) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getCreatedEvidenceRequestById : start");
        EvidenceRequestTO evidenceRequestTO = new EvidenceRequestTO();
        evidenceRequestTO.setId(authenticationAttempt.getId());
        String[] transactionDetails = authenticationAttempt.getTransactionDetails().split("\\|");
        List<AttributeDataTO> attributeDataTOs = null;
        evidenceRequestTO.setUserIdentifier(transactionDetails[1]);
        if (Constant.APPROVED.equals(authenticationAttempt.getAttemptStatus())) {
            attributeDataTOs = new ArrayList<>();
            attributeDataTOs.addAll(attributeStoreService.getAttributeList(authenticationAttempt.getId()));
            //attributeDataTOs.add(attributeStoreProcessorIntf.getAttributeWithEvidenceHash(authenticationAttempt.getId()));
        }
        else {
            attributeDataTOs = new Gson().fromJson(transactionDetails[0], new TypeToken<ArrayList<AttributeDataTO>>() {
            }.getType());
        }
        evidenceRequestTO.setApprovalStatus(authenticationAttempt.getAttemptStatus());
        Map<String, String> receiverDetails = new Gson().fromJson(authenticationAttempt.getReceiverIdDetails(), new TypeToken<Map<String, String>>() {
        }.getType());

        VerifierTO verifierTO =
                              new VerifierTO();
        verifierTO.setVerifierAccountId(authenticationAttempt.getReceiverAccountId());
        verifierTO.setEnterpriseId(receiverDetails.get(Constant.ID));
        verifierTO.setEnterpriseName(receiverDetails.get(Constant.NAME));
        List<VerifierTO> verifierTOs =
                                     new ArrayList<>();
        verifierTOs.add(verifierTO);
        attributeDataTOs.get(0).setVerifiers(verifierTOs);
        evidenceRequestTO.setAttributes(attributeDataTOs);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getCreatedEvidenceRequestById : end");
        return evidenceRequestTO;
    }

    private EvidenceRequestTO getCreatedEvidenceRequestById(AuthenticationAttemptHistory authenticationAttemptHistory) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getCreatedEvidenceRequestById : start");
        EvidenceRequestTO evidenceRequestTO = new EvidenceRequestTO();
        evidenceRequestTO.setId(authenticationAttemptHistory.getId());
        String[] transactionDetails = authenticationAttemptHistory.getTransactionDetails().split("\\|");
        List<AttributeDataTO> attributeDataTOs = null;
        evidenceRequestTO.setUserIdentifier(transactionDetails[1]);
        if (Constant.APPROVED.equals(authenticationAttemptHistory.getAttemptStatus())) {
            attributeDataTOs = new ArrayList<>();
            attributeDataTOs.addAll(attributeStoreService.getAttributeList(authenticationAttemptHistory.getSourceId()));
            //attributeDataTOs.add(attributeStoreProcessorIntf.getAttributeWithEvidenceHash(authenticationAttemptHistory.getId()));
        }
        else {
            attributeDataTOs = new Gson().fromJson(transactionDetails[0], new TypeToken<ArrayList<AttributeDataTO>>() {
            }.getType());
        }
        evidenceRequestTO.setApprovalStatus(authenticationAttemptHistory.getAttemptStatus());
        Map<String, String> receiverDetails = new Gson().fromJson(authenticationAttemptHistory.getReceiverIdDetails(), new TypeToken<Map<String, String>>() {
        }.getType());

        VerifierTO verifierTO =
                              new VerifierTO();
        verifierTO.setVerifierAccountId(authenticationAttemptHistory.getReceiverAccountId());
        verifierTO.setEnterpriseId(receiverDetails.get(Constant.ID));
        verifierTO.setEnterpriseName(receiverDetails.get(Constant.NAME));
        List<VerifierTO> verifierTOs =
                                     new ArrayList<>();
        verifierTOs.add(verifierTO);
        System.out.println("attributeDataTOs..>>" + attributeDataTOs.size());
        attributeDataTOs.get(0).setVerifiers(verifierTOs);
        evidenceRequestTO.setAttributes(attributeDataTOs);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getCreatedEvidenceRequestById : end");
        return evidenceRequestTO;
    }

    /**
     * Gets the attributes.
     *
     * @param accountId the account id
     * @param accountWE the account WE
     * @return the attributes
     */
    private UserTO getAttributes(String accountId, AccountWE accountWE) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAttributes : start");
        List<AttributeDataTO> attributeDataTOs = attributeStoreService.getAttributes(accountId);
        //AccountWE accountWE = iamExtensionProcessorIntf.getAllUserAttributesNames(attributeName, attributeValue);
        List<AttributeDataTO> attDataTOs = new ArrayList<>();
        for (AttributeDataTO attributeDataTO : attributeDataTOs) {
            int i = 0;
            for (AttributeTO attributeTO : accountWE.getAttributes()) {
                attributeTO.setAttributeName(attributeTO.getAttributeName().toUpperCase());
                if (attributeDataTO.getAttributeName().equals(attributeTO.getAttributeName()) && attributeDataTO.getAttributeValue().equals(attributeTO.getAttributeValue())) {
                    if (attributeTO.getStatus() != null) {
                        i = 1;
                        attributeDataTO.setStatus(attributeTO.getStatus());
                        attDataTOs.add(attributeDataTO);
                    }
                }
                if (i == 1) {
                    break;
                }
            }
            if (i == 0) {
                attDataTOs.add(attributeDataTO);
            }
        }
        UserTO userTO = new UserTO();
        userTO.setAttributes(attDataTOs);
        userTO.setUserIdentifier(accountId);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAttributes : end");
        return userTO;
    }

    /**
     * Approve request attribute.
     *
     * @param userTO the user TO
     * @param actor  the actor
     * @param role   the role
     * @return the user TO
     * @throws AuthException            the auth exception
     * @throws RequestNotFoundException the request not found exception
     */
    @Override
    public UserTO approveRequestAttribute(UserTO userTO, String actor, String role, Long id) throws AuthException, RequestNotFoundException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveRequestAttribute : start");
        RequestType requestType = RequestType.valueOf(userTO.getActionType());
        Request request;
        Session session = sessionFactoryUtil.getSession();
        try {
            try {
                request = requestService.getRequestById(userTO.getId(), requestType);
            }
            catch (RequestNotFoundException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(), errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
            }
            if (userTO.getApprovalStatus().equals(Constant.APPROVED)) {
                UserTO requestTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                AuthenticationAttempt authenticationAttempt = requestAttributes(session, requestTO, requestType);
                requestTO.setAuthId(authenticationAttempt.getId());
                request.setRequestJSON(new Gson().toJson(requestTO));
                request.setApprovalStatus(ApprovalStatus.USER_APPROVAL_PENDING);
            }
            else {
                request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
            }
            try {
                request.setApprover(userDao.getActiveUserById(session,id));
            }
            catch (NotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            request.setChecker(actor);
            request.setApproverComments(userTO.getComments());
            try {
                requestService.updateRequest(session, request);
                sessionFactoryUtil.closeSession(session);
            }
            catch (RequestNotFoundException e) {
                session.getTransaction().rollback();
                throw e;
            }
            finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveRequestAttribute : end");
            return userTO;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Request attribute.
     *
     * @param userTO the user TO
     * @param type   the type
     * @return the authentication attempt
     * @throws AuthException the auth exception
     */
    private AuthenticationAttempt requestAttribute(Session session, UserTO userTO, String type) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " requestAttribute : start");
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            String accountId = userTO.getUserIdentifier();
            String transactionId = authenticationAttemptService.generateTransactionId();
            String transactionSummary = Constant.ATTRIBUTE_DEMAND.equals(type) ? "Attribute Demand" : "Attribute Request";
            String transactionDetails = new Gson().toJson(userTO.getAttributes()) + "|" + iamExtension.getDID(config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID), reqRefNum);
            ApprovalAttemptV2 approvalAttempt = new ApprovalAttemptV2.Builder().approvalAttemptMode(ApprovalAttemptMode.ENTERPRISE_TO_PEER).approvalAttemptType(type)
                    .transactionDetails(transactionDetails).transactionSummary(transactionSummary).transactionId(transactionId)
                    .approvalStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING).service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL).consumerAccountId(accountId)
                    .build();
            ApprovalAttemptTO approvalAttemptTO = iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
            AuthenticationAttempt authenticationAttempt = authenticationAttemptService.createAuthenticationAttempt(session, approvalAttemptTO);
            authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
            return authenticationAttempt;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        finally {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " requestAttribute : end");
        }
    }

    /**
     * Request attributes.
     *
     * @param userTO      the user TO
     * @param requestType the request type
     * @return the authentication attempt
     * @throws AuthException the auth exception
     */
    public AuthenticationAttempt requestAttributes(Session session, UserTO userTO, RequestType requestType) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " requestAttribute : start");
        AuthenticationAttempt authenticationAttempt = requestAttribute(session, userTO, requestType.name());
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " requestAttribute : end");
        return authenticationAttempt;
    }

    private void validateEvidenceRequest(AttributeDataTO attributeDataTO, VerifierTO verifierTO, UserTO userTO) throws AuthException {
        AccountWE userAccount = iamExtensionService.getAllAttributesForAccount(userTO.getUserIdentifier());
        if (userAccount == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        boolean isAttributePresent = false;
        attributeNameToUpperCase(attributeDataTO);
        for (AttributeTO attribute : userAccount.getAttributes()) {
            if (attribute.getAttributeName().equals(attributeDataTO.getAttributeName())) {
                isAttributePresent = true;
            }
        }
        if (!isAttributePresent) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT());
        }
        if (iamExtensionService.getAllAttributesForAccount(verifierTO.getVerifierAccountId()) == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_VERIFIER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_VERIFIER_NOT_FOUND());
        }
    }

    @Override
    public boolean validatePassword(PasswordTO passwordTO) throws AuthException {
        in.fortytwo42.enterprise.extension.tos.PasswordTO passwordTO2 = new in.fortytwo42.enterprise.extension.tos.PasswordTO();
        //        passwordTO2.setAccountObjectId(passwordTO.getAccountObjectId());
        String decryptedPassword;
        try {
            decryptedPassword = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), passwordTO.getPassword());
        }
        catch (Exception e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_PASSWORD());
        }
        passwordTO2.setPassword(decryptedPassword);
        passwordTO2.setAccountType(passwordTO.getAccountType().getAccountType());
        if (!Constant.isMock) {
            iamExtensionService.validatePassword(passwordTO2);
        }
        return true;

    }

    @Override
    public PasswordPolicyWE getPasswordPolicies(String userType) throws AuthException {
        AccountType accountType = Constant.MAKER.equals(userType) || Constant.CHECKER.equals(userType) || Constant.APPLICATION_MAKER.equals(userType) || Constant.APPLICATION_CHECKER.equals(userType) || Constant.APPLICATION_VIEWONLY.equals(userType) ? AccountType.ADMIN
                                                                                                       : AccountType.valueOf(userType);
        return iamExtensionService.getPasswordPolicies(accountType.name());

    }

    @Override
    public Map<String, Object> getQuestions() throws AuthException {
        if (!Constant.isMock) {
            return iamExtensionService.getQuestions();
        }
        else {
            Map<String, Object> map = new HashMap<>();
            List<String> questions1 = new ArrayList<>();
            questions1.add("What is your mother's name?");
            questions1.add("What is your birth date?");
            questions1.add("What city you were born in?");
            questions1.add("What is your favourite book's name?");
            map.put("question2", questions1);
            List<String> questions2 = new ArrayList<>();
            questions2.add("What is your sibling's name?");
            questions2.add("What is the name of your best friend?");
            questions2.add("What is your college's name?");
            questions2.add("What is your pet's name?");
            map.put("question1", questions2);
            System.out.println("getQuestion in afterMock");
            return map;
        }
    }



    private List<String> checkDuplicate(List<AttributeDataTO> searchAttributes,List<AttributeMetadataTO> attributeMetadataTOList) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " checkDuplicate : start");
        List<Account> accounts = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<String> accountIds = new ArrayList<>();

        List<AttributeTO> attributeTOs = new ArrayList<>();
        for (AttributeDataTO attributeDataTO : searchAttributes) {
            attributeDataTO.setIsDefault(true);
            attributeTOs.add(getAttributeFromAttributeData(attributeDataTO, attributeMetadataTOList,false));
        }
        //To check for multiple accounts
        try {
            accounts = idsMongoConnectionManager.getAccountByMultipleAttributes(attributeTOs);//TODO: Check for specific attributes
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(accounts.size()>1){
            for(Account account: accounts){
                accountIds.add(account.getId().toString());
            }
        }
        if(accounts.size()==1){
            try {
                //To check user with multiple account ids
                users = userService.getUserByAccountIdList(accounts.get(0).getId().toString());
                if(users.size() > 1){
                    for(User user:users){
                        accountIds.add(user.getAccountId());
                    }
                    return accountIds;
                }
            }
            catch (UserNotFoundException e) {
                e.printStackTrace();
            }

            try{
                List<Consumer> consumers = iamMongoConnectionManager.getConsumerByAccountId(accounts.get(0).getId().toString());
                if(consumers.size()>1){
                    for(Consumer consumer: consumers){
                        accountIds.add(consumer.getAccountId());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        boolean ddupModeWith38 = Boolean.parseBoolean(config.getProperty(Constant.DDUP_MODE_WITH_38));
        Map<String,String> onlyAccountIds=new HashMap<>();
        for(AttributeDataTO attributeDataTO: searchAttributes) {
            List<AttributeStore> attributeStores =  attributeStoreDao.getAttributesByattributeNameandValueWithoutType(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());

            if( attributeStores !=null){
                if( attributeStores.size() >1) {
                    for (AttributeStore attributeStore : attributeStores) {
                        accountIds.add(attributeStore.getUser().getAccountId());
                    }
                }
                if(ddupModeWith38) {
                    boolean ddupModeWith813 = Boolean.parseBoolean(config.getProperty(Constant.DDUP_MODE_WITH_813));
                    if(ddupModeWith813) {
                        accountIds.addAll(getAccountIdIfNotPresent(onlyAccountIds, accounts, attributeStores));
                    }
                    else{
                        accountIds.addAll(getAccountIdIfNotPresent( accounts, attributeStores));
                    }
                }
            }

        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " checkDuplicate : start");
        return accountIds;
    }

    private List<String> getAccountIdIfNotPresent(List<Account> accounts, List<AttributeStore> attributeStores) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAccountIdIfNotPresent : start");
        List<String> accountIds = new ArrayList<>();
        String accountId;
        for (AttributeStore attributeStore : attributeStores) {
            accountId = attributeStore.getUser().getAccountId();
            if (accountId != null) {
                if (accounts.isEmpty()) {
                    accountIds.add(accountId); //add account id from adapter if it is not present in ids
                }
                for (Account account : accounts) {
                    if (!accountId.equals(account.getId().toString())) {
                        accountIds.add(accountId); //add account id from adapter if it doesn't match
                        accountIds.add(account.getId().toString()); //add account id from adapter if it doesn't match
                        break;
                    }
                }
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAccountIdIfNotPresent : end");
        return accountIds;
    }


    private List<String> getAccountIdIfNotPresent(Map<String,String> onlyAccountIds,List<Account> accounts,
                                                  List<AttributeStore> attributeStores){
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAccountIdIfNotPresent : start");

        List<String> accountIds = new ArrayList<>();
        String accountId;
        for (AttributeStore attributeStore : attributeStores) {
            accountId = attributeStore.getUser().getAccountId();
            if(onlyAccountIds.isEmpty()){
                onlyAccountIds.put(accountId,accountId);
            }
            else if(accounts.isEmpty()&&onlyAccountIds.get(accountId)!=null){
                accountIds.add(onlyAccountIds.values().iterator().next());
                accountIds.add(accountId); //add account id from adapter if it is not present in ids
                break;
            }
            for(Account account : accounts){
                if(!accountId.equals(account.getId().toString())){
                    accountIds.add(accountId); //add account id from adapter if it doesn't match
                    accountIds.add(account.getId().toString()); //add account id from adapter if it doesn't match
                    break;
                }
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAccountIdIfNotPresent : end");
        return accountIds;
    }

    private void deleteUserUsingAccountId(String accountId, Session session){
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " deleteUserUsingAccountId : start"+accountId);
        try {
            Set<ObjectId> ids = new HashSet<>();
            List<String> consumerAccountIds = new ArrayList<>();
            List<User> users = null;
            try {
                users = userService.getUserByAccountIdList(accountId);
            }catch (Exception e){
                e.printStackTrace();
            }
            boolean exceptionOccured = false;
            ids.add(new ObjectId(accountId));
            consumerAccountIds.add(accountId);
            if(users!=null && users.size()>0) {
                for(User user:users) {
                    List<User> tempUsers = new ArrayList<>();
                    Transaction transaction = session.beginTransaction();
                    try {
                        tempUsers.add(user);
                        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "User details for account Id ObjectId(" + accountId + ") User ID " + user.getId() + "KC id " + user.getKcId());

                        List<UserApplicationServiceRel> userBindingRels = userApplicationRelDao.getRelsByUserId(user.getId());
                        if (userBindingRels != null && userBindingRels.size() > 0) {
                            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "Rel delete started for account Id ObjectId(" + accountId + ")");
                            userApplicationRelDao.bulkDelete(session, userBindingRels);
                            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "Rel deleted for account Id ObjectId(" + accountId + ")");
                        }

                        List<AttributeStore> attributes = attributeStoreDao.getUserAttributes(user.getId());
                        if (attributes != null && attributes.size() > 0) {
                            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "Attributed delete started for account Id ObjectId(" + accountId + ")");
                            attributeStoreDao.bulkDelete(session, attributes);
                            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "Attributed deleted for account Id ObjectId(" + accountId + ")");
                        }
                        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "User delete started for account Id ObjectId(" + accountId + ")");

                        userDao.bulkDelete(session, tempUsers);
                        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "User deleted for account Id ObjectId(" + accountId + ")");
                        transaction.commit();
                    }
                    catch (Exception e) {
                        exceptionOccured = true;
                        transaction.rollback();
                        System.out.println("Error occurred for account Id ObjectId(" + accountId + ")");
                        e.printStackTrace();
                    }
                    if(exceptionOccured){
                        return;
                    }
                    ids.add(new ObjectId(accountId));
                    consumerAccountIds.add(accountId);
                    String userKcId = null;
                    if (user != null) {
                        userKcId = user.getKcId();
                    }
                    try {
                        if (userKcId == null) {
                            userKcId = camUserFacade.getUserDetailsWithUsername(Config.getInstance().getProperty(Constant.CAM_REALM), accountId).getId();
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Didnt get user on cam for account Id " + accountId);
                    }
                    if (userKcId != null) {
                        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "CAM deletion started for account Id ObjectId(" + accountId + ")");
                        try {
                            camUserFacade.deleteUsers(Config.getInstance().getProperty(Constant.CAM_REALM), userKcId);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "CAM deletion ended for account Id ObjectId(" + accountId + ")");

                    }
                }
            }
            try {
                logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG +"IDS deletion started for account Id ObjectId(" + accountId+")");
                idsMongoConnectionManager.remove(ids);
                logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG +"IDS deletion ended for account Id ObjectId(" + accountId+")");
            } catch (Exception e){
                logger.log(Level.ERROR, USER_FACADE_IMPL_LOG + "Error occured for deleting account for account Id ObjectId(" + accountId+")");
                e.printStackTrace();
            }
            try {
                logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG +"IAM deletion started for account Id ObjectId(" + accountId+")");

                iamMongoConnectionManager.remove(consumerAccountIds);
                logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG +"IAM deletion ended for account Id ObjectId(" + accountId+")");

            }catch (Exception e){
                logger.log(Level.ERROR, USER_FACADE_IMPL_LOG + "Error occured for deleting consumer for account Id ObjectId(" + accountId+")");

                e.printStackTrace();
            }
        } catch (Exception e){
            logger.log(Level.ERROR, USER_FACADE_IMPL_LOG + "Error occured for account Id ObjectId(" + accountId+")");
        }
        finally {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " deleteUserUsingAccountId : end"+accountId);
        }
    }

    private UserLock createLock(String attribute,Session session) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " createLock : start");
        Transaction transaction = session.beginTransaction();
        try {
            UserLock userLock = new UserLock();
            userLock.setAttribute(attribute);
            userLockService.addUserLock(session, userLock);
            transaction.commit();
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " createLock : end");
            return userLock;
        }
        catch (Exception e){
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " createLock : end");
            transaction.rollback();
            throw e;
        }


    }
    @Override
    public UserIciciTO onboardUserV4(UserIciciTO userTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : start");
        Session session = IamThreadContext.getSessionWithoutTransaction();
        IAMExtensionV2 iamExtension;
        Token token;
        UserLock userLock = null;
        try {
            iamExtension = iamExtensionService.getIAMExtension();
            token = iamExtensionService.getToken(iamExtension);
        } catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        try {
            boolean userLockMode = Boolean.parseBoolean(config.getProperty(Constant.USER_LOCK_MODE));

            isAttributeDataInSearchAttributes(userTO);
            if(userLockMode) {
                try {
                    String attributeValue = null;
                    for(AttributeDataTO attributeDataTO: userTO.getSearchAttributes()){
                        if(Constant.MOBILE_NO.equals(attributeDataTO.getAttributeName())){
                            attributeValue = attributeDataTO.getAttributeValue();
                        }
                    }
                    if(attributeValue == null){
                        attributeValue = userTO.getSearchAttributes().get(0).getAttributeValue();
                    }
                    userLock = createLock(attributeValue, session);
                }
                catch (Exception e) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_ONBOARD_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_ONBOARD_ALREADY_PRESENT());
                }
            }
            boolean isProcessCalled = false;
            AccountWE accountWE = new AccountWE();
            User user = new User();
            List<OnboardUserType> onboardProcess = createOnboardProcess();
            List<AttributeMetadataTO> attributeMetadataTOList = validateSearchAttributes(userTO.getSearchAttributes());
            boolean ddupMode = Boolean.parseBoolean(config.getProperty(Constant.DDUP_MODE));
            if(ddupMode) {
                List<String> accountIds = checkDuplicate(userTO.getSearchAttributes(), attributeMetadataTOList);
                if (accountIds.size() > 1) {
                    Set<String> accountIdsSet = new HashSet<>(accountIds);

                    for (String accountId : accountIdsSet) {
                        deleteUserUsingAccountId(accountId, session);
                    }
                }
            }
            for (OnboardUserType type : onboardProcess) {
                Boolean ddupTestMode = Boolean.parseBoolean(config.getProperty(Constant.DDUP_TEST_MODE));
                ddupTestMode = ddupTestMode == null ? false : ddupTestMode;
                if(ddupTestMode && userTO.getOnboardUserType() != null) {
                    if (type.equals(userTO.getOnboardUserType())) {
                        break;
                    }
                }
                Onboarder onboarder = OnboardUserFactory.buildOnboarder(type);
                if (!onboarder.validate(token, iamExtension, userTO, accountWE, user)) {
                    isProcessCalled = true;
                    long startTimeProcess = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->UserFacadeImpl -> " + type + " -> process |Epoch:" + startTimeProcess);
                    onboarder.process(token, iamExtension, userTO, accountWE, user, session);
                    long endTimeProcess = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserFacadeImpl -> " + type + " -> process |Epoch:" + endTimeProcess);
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "-> UserFacadeImpl -> " + type + " -> process -> DIFF " + (endTimeProcess - startTimeProcess));
                }
            }
            if (!isProcessCalled) {
                userTO.setStatus("FAILED");
                userTO.setErrorCode(errorConstant.getALREADY_PRESENT_IN_SYSTEM_CODE());
                userTO.setErrorMessage("User already onboarded in the system.");
            } else {
                userTO.setStatus("SUCCESS");
                AuditLogUtil.sendAuditLog("User " + userTO.getSearchAttributes().get(0).getAttributeValue() + " successfully onboarded externally", "USER", ActionType.ONBOARD, null, IdType.ACCOUNT,
                        "", null, accountWE.getId(), null);
            }
        } catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : end");
            throw e;
        } catch (ValidationException e) {
            session.getTransaction().rollback();
            logger.log(Level.DEBUG, e.getMessage(), e);
            userTO.setStatus(Constant.FAILED);
            userTO.setErrorCode(errorConstant.getVALIDATION_ERROR_CODE());
            userTO.setErrorMessage(e.getMessage());
        } catch (UserFoundOnIDSException e) {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : identity merging process start");
            processIdentityMerging(userTO, iamExtension, token, session, e);
        } finally {
            if(userLock!=null) {
                Transaction transaction = session.beginTransaction();
                try {
                    userLockService.deleteUserLock(session, userLock);
                    transaction.commit();
                } catch (Exception e){
                    transaction.rollback();
                }
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : end");
        return userTO;
    }

    private void isAttributeDataInSearchAttributes(UserIciciTO userTO) throws ValidationException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " isAttributeDataInSearchAttributes : start");
        if (userTO.getSearchAttributes() != null && !userTO.getSearchAttributes().isEmpty()) {
            if (userTO.getAttributeData() != null && !userTO.getAttributeData().isEmpty()) {
                boolean runOnce = true;
                int isRegisteredCountSearch = 0;
                int isRegisteredCountData = 0;
                List<AttributeDataTO> searchAttributes = userTO.getSearchAttributes();
                List<AttributeDataTO> attributeData = userTO.getAttributeData();
                for (AttributeDataTO attribute : attributeData) {
                    attributeNameToUpperCase(attribute);
                    if (attribute.getIsRegistered() != null && attribute.getIsRegistered()) {
                        isRegisteredCountData++;
                    }
                    for (AttributeDataTO searchAttribute : searchAttributes) {
                        attributeNameToUpperCase(searchAttribute);
                        if (runOnce) {
                            if (searchAttribute.getIsRegistered() != null && searchAttribute.getIsRegistered()) {
                                isRegisteredCountSearch++;
                            }
                        }
                        if (attribute.getAttributeName().equals(searchAttribute.getAttributeName()) && attribute.getAttributeValue().equals(searchAttribute.getAttributeValue())) {
                            throw new ValidationException("Duplicate attribute in search attributes and attribute data.");
                        }
                    }
                    runOnce = false;
                }
                manageIsRegistered(userTO, isRegisteredCountSearch, isRegisteredCountData);
            } else {
                int isRegisteredCountSearch = 0;
                for (AttributeDataTO searchAttribute : userTO.getSearchAttributes()) {
                    attributeNameToUpperCase(searchAttribute);
                    if (searchAttribute.getIsRegistered() != null && searchAttribute.getIsRegistered()) {
                        isRegisteredCountSearch++;
                    }
                }
                manageIsRegistered(userTO, isRegisteredCountSearch, 0);
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " isAttributeDataInSearchAttributes : end");
    }

    private void manageIsRegistered(UserIciciTO userTO, int isRegisteredCountSearch, int isRegisteredCountData) {
        if (isRegisteredCountSearch == 0 && isRegisteredCountData == 0) {
            userTO.getSearchAttributes().get(0).setIsRegistered(true);
            return;
        }
        else if (isRegisteredCountSearch == 1 && isRegisteredCountData == 0) {
            return;
        }
        else if (isRegisteredCountSearch == 0 && isRegisteredCountData == 1) {
            return;
        }
        else if (isRegisteredCountSearch > 1 && isRegisteredCountData == 0) {
            for (AttributeDataTO attribute : userTO.getSearchAttributes()) {
                attribute.setIsRegistered(false);
            }
            userTO.getSearchAttributes().get(0).setIsRegistered(true);
            return;
        }
        else if (isRegisteredCountSearch == 0 && isRegisteredCountData > 1) {
            for (AttributeDataTO attribute : userTO.getAttributeData()) {
                attribute.setIsRegistered(false);
            }
            userTO.getAttributeData().get(0).setIsRegistered(true);
            return;
        }
        else {
            for (AttributeDataTO attribute : userTO.getSearchAttributes()) {
                attribute.setIsRegistered(false);
            }
            for (AttributeDataTO attribute : userTO.getAttributeData()) {
                attribute.setIsRegistered(false);
            }
            userTO.getSearchAttributes().get(0).setIsRegistered(true);
            return;
        }

    }

    private List<AttributeMetadataTO> validateSearchAttributes(List<AttributeDataTO> searchAttributes) throws AuthException {
        List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
        HashMap<String, String> singleValuedAttr = new HashMap<>();
        for (AttributeDataTO attributeDataTO : searchAttributes) {
            attributeNameToUpperCase(attributeDataTO);
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
            int index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
            if (index < 0) {
                attributeMetadataTO.setAttributeName("OTHERS");
                index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
            }
            attributeMetadataTO = attributeMetaDataWEs.get(index);
            if (!attributeMetadataTO.getIsUnique()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SEARCH_ATTRIBUTE(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE() + ": " + attributeMetadataTO.getAttributeName());
            }
            if (singleValuedAttr.containsKey(attributeDataTO.getAttributeName())) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SEARCH_ATTRIBUTE(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE() + ": " + attributeMetadataTO.getAttributeName());
            }
            if (attributeMetadataTO.getAttributeValueModel().equals(AttributeValueModel.SINGLE_VALUED.name())) {
                singleValuedAttr.put(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());
            }
        }
        return attributeMetaDataWEs;
    }



    private List<OnboardUserType> createOnboardProcess() {
        List<OnboardUserType> onboardProcess = new ArrayList<>();
        onboardProcess.add(OnboardUserType.createAccount);
        onboardProcess.add(OnboardUserType.createConsumer);
        onboardProcess.add(OnboardUserType.onboardOnCrypto);
        onboardProcess.add(OnboardUserType.createUser);
        onboardProcess.add(OnboardUserType.addAttributes);
        onboardProcess.add(OnboardUserType.onboardUser);
        onboardProcess.add(OnboardUserType.applicationAutoBinding);
        onboardProcess.add(OnboardUserType.cryptoAttributesAndAttributeEditingOnboarder);
//        onboardProcess.add(OnboardUserType.addCredentialsToAccount);
        return onboardProcess;
    }

    private List<AddAttributeType> createAttributeAddProcess() {
        List<AddAttributeType> addAttributeProcess = new ArrayList<>();
        addAttributeProcess.add(AddAttributeType.addAttributesOnIds);
        addAttributeProcess.add(AddAttributeType.addAttributesOnAdapter);
        addAttributeProcess.add(AddAttributeType.addAttributesOnCrypto);
        addAttributeProcess.add(AddAttributeType.addAttributesOnCAM);

        return addAttributeProcess;
    }

    private List<AddAttributeType> createAttributeAddProcessForOnboard() {
        List<AddAttributeType> addAttributeProcess = new ArrayList<>();
        addAttributeProcess.add(AddAttributeType.addAttributesOnIds);
        addAttributeProcess.add(AddAttributeType.addAttributesOnAdapter);
        addAttributeProcess.add(AddAttributeType.addAttributesOnCrypto);
        addAttributeProcess.add(AddAttributeType.onboardUserOnCAM);
        addAttributeProcess.add(AddAttributeType.addAttributesOnCAM);
        addAttributeProcess.add(AddAttributeType.setCredentials);
        addAttributeProcess.add(AddAttributeType.applicationAutoBinding);
        return addAttributeProcess;
    }

    @Override
    public UserIciciTO onboardUser(UserIciciTO userTO, String role, String username) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : start");

        Session session = sessionFactoryUtil.getSession();
        try {
            isAttributeDataInSearchAttributes(userTO);
            validateSearchAttributes(userTO.getSearchAttributes());
            userTO = onboardUserOnIdsIamCryptoAdapter(session, userTO, role, username);
            sessionFactoryUtil.closeSession(session);
        }
        catch (ValidationException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            session.getTransaction().rollback();
            throw new AuthException(new Exception(),errorConstant.getERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE(), e.getMessage());
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
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : start");
        return userTO;
    }

    @Override
    public UserTO onboardUser(UserTO userTO, String role, String username,Long id, boolean isEncrypted, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : start");
        Session session = sessionFactoryUtil.getSession();
        try {

            //            for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
            //                String decryptedAttributeValue = null;
            //                try {
            //                    decryptedAttributeValue = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), attributeDataTO.getAttributeValue());
            //                }
            //                catch (Exception e) {
            //                    session.getTransaction().rollback();
            //                }
            //                attributeDataTO.setAttributeValue(decryptedAttributeValue);
            //                attributeStoreService.checkAttributesPresent(attributeDataTO);
            //            }
            //            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : start*****"+userTO.getAttributes().get(0).getAttributeValue());
            validateRequest(userTO, isEncrypted);
            requestService.createUserOnboardRequest(session, userTO, isEncrypted, username,id, saveRequest);
            if (!saveRequest) {
                //User user = attributeStoreDao.getUserByAttributeValue(username);
                User user = userService.getActiveUser(id);
                userTO = approveOnboardUser(session, userTO, role, user.getAccountId(), isEncrypted);
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
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser : start");
        return userTO;
    }

    private void validateRequest(UserTO userTO, boolean isEncrypted) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateRequest : start");
        UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
        if (userTO.getSubscribedApplications() != null) {
            for (ApplicationTO applicationTO : userTO.getSubscribedApplications()) {
                in.fortytwo42.enterprise.extension.tos.ApplicationTO applicationTO1 = userFacade.validateApplication(applicationTO.getApplicationId());
                logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateRequest : applicationActive" + applicationTO1.getApplicationActive());
                if (applicationTO1.getApplicationActive() == Boolean.FALSE) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_APPLICATION_NOT_ACTIVE(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_ACTIVE());
                }
            }
        }
        List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        for (AttributeDataTO attribute : userTO.getAttributes()) {
            attributeNameToUpperCase(attribute);
            try {
                logger.log(Level.DEBUG, " attributeName : " + attribute.getAttributeName());
                logger.log(Level.DEBUG, " attributeValue : " + attribute.getAttributeValue());
                AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
                attributeMetadataTO.setAttributeName(attribute.getAttributeName());
                int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
                if (index < 0) {
                    attributeMetadataTO.setAttributeName("OTHERS");
                    index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
                }
                attributeMetadataTO = attributeMetaDataTOs.get(index);
                if (attributeMetadataTO.getIsUnique() != null && attributeMetadataTO.getIsUnique()) {
                    String attributeValue = attribute.getAttributeValue();
                    if (isEncrypted) {
                        attributeValue = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), attribute.getAttributeValue());
                        String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
                        Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
                        if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                            attributeValue = attributeValue.toUpperCase();
                        }
                    }
                    AttributeStore activeAttribute =  attributeStoreService.getActiveAttribute(attribute.getAttributeName(), attributeValue);
                    if(Constant.ADFS.equals(userTO.getAuthType()) && activeAttribute!=null) {
                        List<Role> roleOfActiveUser = activeAttribute.getUser().getRoles().stream().filter(e -> e.getName().equals(UserRole.USER)).collect(Collectors.toList());
                        if (roleOfActiveUser.isEmpty()) {
                            throw new AuthException(null, errorConstant.getERROR_CODE_USER_ONBOARD_ALREADY_PRESENT(),
                                    attribute.getAttributeName() + ":" + errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT_TO_OTHER());
                        }
                    }else if(userTO.getSubscribedApplications()!=null){
                        for (ApplicationTO applicationTO : userTO.getSubscribedApplications()) {
                            Application application = DaoFactory.getApplicationDao().getApplicationByApplicationId(applicationTO.getApplicationId());
                            try (Session session = sessionFactoryUtil.getSession()) {
                                if (UserApplicationRelDaoImpl.getInstance().isApplicationUserBindingPresent(application.getId(), activeAttribute.getUser().getId(), session)) {
                                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_ONBOARD_ALREADY_PRESENT(),
                                           activeAttribute.getAttributeName()+":"+errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                                }
                            }
                        }
                    }else if(activeAttribute != null && !Constant.ADFS.equals(userTO.getAuthType())){
                        throw new AuthException(null, errorConstant.getERROR_CODE_USER_ONBOARD_ALREADY_PRESENT(),
                                activeAttribute.getAttributeName()+":"+errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                    }
                    logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUser :  user already present : end");
                }
            }
            catch (AttributeNotFoundException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            catch (ApplicationNotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(),
                        errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());

            }

        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateRequest : end");
    }

    public UserIciciTO userTOToUserIciciTO(UserTO userTO,boolean isEncrypted) throws AuthException {
        UserIciciTO userIciciTO = new UserIciciTO();
        List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
        List<AttributeDataTO> searchAttributesList = new ArrayList<>();
        List<AttributeDataTO> attributeDataList = new ArrayList<>();
        for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
            attributeNameToUpperCase(attributeDataTO);
            AttributeDataTO attributeData = new AttributeDataTO();
            attributeData.setAttributeName(attributeDataTO.getAttributeName());
            if(isEncrypted) {
                String decryptedAttributeValue=CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), attributeDataTO.getAttributeValue());
                String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
                Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
                if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                     decryptedAttributeValue = decryptedAttributeValue.toUpperCase();
                }
                attributeData.setAttributeValue(decryptedAttributeValue);
            }
            else attributeData.setAttributeValue(attributeDataTO.getAttributeValue());
            if (attributeDataTO.getIsDefault() != null) {
                attributeData.setIsDefault(attributeDataTO.getIsDefault());
            }
            Optional<AttributeMetadataTO> attributeMetadataTO = attributeMetaDataWEs.stream().filter(metadata -> metadata.getAttributeName().equalsIgnoreCase(attributeDataTO.getAttributeName())).findFirst();
            if (!attributeMetadataTO.isPresent()) {
                attributeMetadataTO = attributeMetaDataWEs.stream().filter(metadata -> metadata.getAttributeName().equalsIgnoreCase("OTHERS")).findFirst();
            }
            if (attributeMetadataTO.get().getIsUnique()) {
                if (attributeDataTO.getIsRegistered() != null) {
                    attributeData.setIsRegistered(attributeDataTO.getIsRegistered());
                }
                searchAttributesList.add(attributeData);
            }
            else {
                attributeDataList.add(attributeData);
            }
        }
        userIciciTO.setSearchAttributes(searchAttributesList);
        userIciciTO.setAttributeData(attributeDataList);

        List<in.fortytwo42.tos.transferobj.QuestionAnswerTO> questionAnswerList = new ArrayList<>();
        if (userTO.getQuestionAnswers() != null) {
            for (in.fortytwo42.tos.transferobj.QuestionAnswerTO questionAnswerTO : userTO.getQuestionAnswers()) {
                in.fortytwo42.tos.transferobj.QuestionAnswerTO questionAnswer = new in.fortytwo42.tos.transferobj.QuestionAnswerTO();
                questionAnswer.setQuestion(questionAnswerTO.getQuestion());
                if(isEncrypted) {
                    String decryptedAnswer = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), questionAnswerTO.getAnswer());
                    questionAnswer.setAnswer(decryptedAnswer);
                }else questionAnswer.setAnswer(questionAnswerTO.getAnswer());
                questionAnswerList.add(questionAnswer);
            }
            userIciciTO.setQuestionAnswers(questionAnswerList);
        }

        List<ApplicationTO> subscribedApplicationList = new ArrayList<>();
        if (userTO.getSubscribedApplications() != null) {
            for (ApplicationTO applicationTO : userTO.getSubscribedApplications()) {
                ApplicationTO application = new ApplicationTO();
                application.setApplicationName(applicationTO.getApplicationName());
                application.setApplicationId(applicationTO.getApplicationId());
                List<ServiceTO> serviceTOList = new ArrayList<>();
                for (ServiceTO serviceTO : applicationTO.getServices()) {
                    ServiceTO service = new ServiceTO();
                    service.setServiceName(serviceTO.getServiceName());
                    serviceTOList.add(service);
                }
                application.setServices(serviceTOList);
                subscribedApplicationList.add(application);
            }
            userIciciTO.setSubscribedApplications(subscribedApplicationList);
        }
        if (!Constant.ADFS.equals(userTO.getAuthType())) {
            if (isEncrypted) {
                String decryptedCredential = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), userTO.getUserCredential());
                userIciciTO.setUserCredential(decryptedCredential);
            }
            else
                userIciciTO.setUserCredential(userTO.getUserCredential());
        }
        if (userTO.getCamEnabled() != null) {
            userIciciTO.setCamEnabled(userTO.getCamEnabled());
        }
        userIciciTO.setAccountType(userTO.getAccountType());
        userIciciTO.setCredentialsThroughEmail(userTO.getCredentialsThroughEmail());
        userIciciTO.setAuthType(userTO.getAuthType());
        return userIciciTO;
    }

    //TODO: Sequential retry
    @Override
    public UserTO approveOnboardUser(Session session, UserTO userTO, String role, String parentAccountId, boolean isEncrypted) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveOnboardUser : start");

        UserTO adfsRoleChangeTO = handleADFSUserRoleChange(session, userTO, role, isEncrypted);
        if (adfsRoleChangeTO != null) {
            return adfsRoleChangeTO;
        }

        UserIciciTO userIciciTO = userTOToUserIciciTO(userTO,isEncrypted);

        if(userTO.getAccountType().equalsIgnoreCase(AccountType.USER.name()) || userTO.getAccountType().equalsIgnoreCase(AccountType.SUPER_USER.name())) {
            String errorMessage = ValidationUtilV3.isRequestValidForOnboardUserV4(userIciciTO);
            if (errorMessage == null) {
                Session sessionWoTransaction = sessionFactoryUtil.openSessionWithoutTransaction();
                try {
                    IamThreadContext.setSessionWithoutTransaction(sessionWoTransaction);
                    userIciciTO = onboardUserV4(userIciciTO);
                    if (userIciciTO.getStatus().equalsIgnoreCase("FAILED")) {
                        throw new AuthException(new Exception(), userIciciTO.getErrorCode(), userIciciTO.getErrorMessage());
                    }
                    List<AttributeDataTO> attributes = new ArrayList<>();
                    attributes.addAll(userIciciTO.getSearchAttributes());
                    attributes.addAll(userIciciTO.getAttributeData());
                    userTO.setAttributes(attributes);
                    userTO.setStatus(userIciciTO.getStatus());
                    return userTO;
                }
                finally {
                    if(sessionWoTransaction.isOpen()) {
                        sessionWoTransaction.close();
                    }
                }
            } else {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorMessage);
            }
        }

        try {
            isAttributeDataInSearchAttributes(userIciciTO);
        }
        catch (ValidationException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            session.getTransaction().rollback();
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE(), e.getMessage());
        }
        userIciciTO = onboardUserOnIdsIamCryptoAdapter(session, userIciciTO, role, parentAccountId);

        userTO.setId(userIciciTO.getId());
        userTO.setStatus(userIciciTO.getStatus());
        if (userTO.getCredentialsThroughEmail() != null && userTO.getCredentialsThroughEmail() == true) {
            try {
                JMSProducerExtension jmsProducerExtension;
                String brokerUrl = Config.getInstance().getProperty(Constant.BROKER_URL);
                String username = Config.getInstance().getProperty(Constant.QUEUE_USERNAME);
                String password = Config.getInstance().getProperty(Constant.QUEUE_PASSWORD);
                jmsProducerExtension = JMSProducerExtension.getInstance(brokerUrl, username, password);
                List<AttributeDataTO> attributes = userTO.getAttributes();
                String email = null;
                for (AttributeDataTO attribute : attributes) {
                    if (attribute.getAttributeName().equals(Constant.EMAIL_ID) && attribute.getIsDefault()) {
                        email = attribute.getAttributeValue();
                    }
                }
                String message = userTO.getUserCredential();
                String smsDecrypted = "Hi +" + userTO.getUsername()
                                      + ",\n"
                                      + "\n"
                                      +
                                      " Welcome you to I-AM.\n"
                                      +
                                      "To gain access to your account, please use the following Activation Code:"
                                      + CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), message)
                                      + ".\n"
                                      +
                                      "Kindly use this code when logging in for the first time to activate your account successfully.\n"
                                      +
                                      "Please note that this email is system-generated, and we kindly request that you refrain from replying directly to this message.\n"
                                      +
                                      ""
                                      + "\n";

                String emailDecrypted = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), email);
                jmsProducerExtension.sendEmail(Config.getInstance().getProperty(Constant.EMAIL_QUEUE_NAME),
                        Constant.APPLICATION_IDENTITY_STORE, emailDecrypted, smsDecrypted, Config.getInstance().getProperty(Constant.EMAIL_SUBJECT),
                        ThreadContext.get(Constant.REQUEST_REFERENCE));
            }
            catch (Exception e) {
                throw new AuthException(e, errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
            }

        }

        //        AccountWE accountWE = onboardUserOnIdsAndIam(userTO, parentAccountId, isEncrypted);
        //        AccountWE accountWE = onboardUserOnIdsAndIam(userIciciTO, isEncrypted);
        //        String accountId = accountWE.getId();
        //        userTO.setAccountId(accountId);
        //
        //        User user;
        //        //changes in userto and appto(both flow), boolean check -> isCamEnabled
        ////        boolean isCamEnabled = config.getProperty(Constant.IS_KEYCLOAK_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_KEYCLOAK_ENABLED));
        ////        if(isKeycloakEnabled) {
        //
        //        boolean isCamEnabled = userTO.getCamEnabled() != null && userTO.getCamEnabled();
        //        if(isCamEnabled){
        //            UserCreationRequest userCreationRequest = new UserCreationRequest();
        //
        //            List<AttributeTO> accountIdentifiers = accountWE.getAttributes();
        //            String userName = accountIdentifiers.get(0).getAttributeValue();
        //            userCreationRequest.setUsername(userName);
        //
        //            //attributes
        //            List<CamAttribute> camAttributes = new ArrayList<>();
        //            for(AttributeTO attributeTO : accountIdentifiers){
        //                CamAttribute camAttribute = new CamAttribute();
        //                camAttribute.setCustomAttributeName(attributeTO.getAttributeName());
        //                camAttribute.setCustomAttributeValue(attributeTO.getAttributeValue());
        //                camAttributes.add(camAttribute);
        //            }
        //            userCreationRequest.setAttributes(camAttributes);
        //
        //            //credentials ->  to support multiple credentials
        //            List<Credential> credentials = new ArrayList<>();
        //            Credential credential = new Credential();
        //            credential.setTemporary(false);
        //            credential.setType("password");
        //            String userCredential = accountWE.getUserCredential() != null ? accountWE.getUserCredential() : userTO.getUserCredential();
        //            credential.setValue(userCredential);
        //            credentials.add(credential);
        //            userCreationRequest.setCredentials(credentials);
        //
        //            UserResponseDto createdUser = CamUserFacade.onboardCamUser(Config.getInstance().getProperty(Constant.CAM_REALM), userCreationRequest);
        //
        //            user = userService.createUserV2(session, userTO.getAccountId(), UserRole.valueOf(userTO.getAccountType()), TwoFactorStatus.ENABLED.name(), createdUser);
        //        }
        //        else{
        //            user = userService.createUser(session, userTO.getAccountId(), UserRole.valueOf(userTO.getAccountType()), TwoFactorStatus.ENABLED.name());
        //        }
        //
        //        List<AttributeStore> attributeStores = new ArrayList<>();
        //        for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
        //            String decryptedAttributeValue = attributeDataTO.getAttributeValue();
        //            if (isEncrypted) {
        //                try {
        //                    decryptedAttributeValue = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), attributeDataTO.getAttributeValue());
        //                }
        //                catch (Exception e) {
        //                    session.getTransaction().rollback();
        //                }
        //            }
        //            attributeDataTO.setAttributeValue(decryptedAttributeValue);
        //            attributeStores.add(attributeStoreService.saveAttributeData(session, attributeDataTO, user));
        //        }
        //        user.setAttributeStores(attributeStores);
        //
        //        if(userTO.getSubscribedApplications()!=null) {
        //            userService.autoBindUserToApplication(session, userTO.getSubscribedApplications(), user, accountWE.getUserCredential());
        //        }
        //        user = userService.updateUser(session, user);
        //        userTO.setStatus(Constant.SUCCESS_STATUS);
        //        userTO.setId(user.getId());
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveOnboardUser : end");
        return userTO;
    }

    private AccountWE onboardUserOnIdsAndIam(UserTO userTO, String parentAccountId, boolean isEncrypted) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUserOnIdsAndIam : start");
        if (!Constant.isMock) {
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token;
            try {
                token = iamExtensionService.getToken(iamExtension);
                AccountWE accountWE = onboardAccount(iamExtension, token, userTO, parentAccountId, isEncrypted);
                if (accountWE != null) {
                    onboardConsumer(iamExtension, token, accountWE.getId());
                }
                return accountWE;
            }
            catch (IAMException e) {
                iamExceptionConvertorUtil.convertToAuthException(e);
                logger.log(Level.ERROR, e.getMessage(), e);

            }
        }
        else {
            AccountWE accountWE = new AccountWE();
            accountWE.setId(UUIDGenerator.generate().substring(0, 14));
            return accountWE;
        }
        return null;
    }

    private AccountWE onboardUserOnIdsAndIam(UserIciciTO userTO,String parentAccountId) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " onboardUserOnIdsAndIam : start");
        if (!Constant.isMock) {
            boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));;
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token;
            try {
                String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                token = iamExtensionService.getToken(iamExtension);
                Map<String, String> attributeValueWithPlainValue = new HashMap<>();
                AccountWE account = null;
                try {
                    String accountType=userTO.getAccountType();
                    userTO.setAccountType(AccountType.USER.toString());
                    account = onboardAccount(iamExtension, token, userTO, attributeValueWithPlainValue, parentAccountId);
                    userTO.setAccountType(accountType);
                }
                catch (ValidationException e) {
                    logger.log(Level.DEBUG, e.getMessage(), e);
                    userTO.setStatus(Constant.FAILURE_STATUS);
                    userTO.setErrorMessage(e.getMessage());
                    return account;
                }

                if (account != null) {
                    onboardConsumer(iamExtension, token, account.getId());
                }
                if (enableCrypto) {
                    iamExtension.onboardEntityOnCrypto(account, CryptoEntityType.ENTITY_USER, token, reqRefNum);
                }
                AccountWE accountWE = iamExtension.onboardAccount(account, account.getId(), true, token);
                String status = accountWE.getStatus() != null ? accountWE.getStatus() : Constant.SUCCESS_STATUS;
                userTO.setStatus(status);
                if (accountWE.getErrorMessage() != null) {
                    userTO.setErrorMessage(accountWE.getErrorMessage());
                }
                accountWE.setUserCredential(account.getUserCredential());
                Map<String, Object> attributeValueWithKey = null;
                if (enableCrypto) {
                    attributeValueWithKey = iamExtension.registerAttributesOnCryptov3(accountWE, attributeValueWithPlainValue, token, reqRefNum);
                }
                else {
                    attributeValueWithKey = new HashMap<>();
                }
                System.out.println(" ** after registerAndActivateAccount " + accountWE.getCryptoDID());
                for (AttributeTO attributeTO : accountWE.getAttributes()) {
                    String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
                    System.out.println("*****" + attributeTO.getOperationStatus() + " ******");
                    if (AttributeOperationStatus.SUCCESSFUL == attributeTO.getOperationStatus()) {
                        if (enableCrypto) {
                            if (attributeValueWithKey.containsKey(plainValue)) {
                                System.out.println("*** before setEncryptedAttributeValue" + attributeTO
                                        .getAttributeName() + " value " + plainValue + " key " + attributeValueWithKey.get(plainValue));
                                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                                                                                                      (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(plainValue);
                                attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                                attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                                attributeTO.setOperationStatus(null);
                            }
                        }
                        else {
                            attributeTO.setOperationStatus(null);
                        }
                    }
                    else {
                        if (!enableCrypto) {
                            attributeValueWithKey.put(plainValue, attributeTO.getErrorMessage());
                        }
                    }
                }
                if (userTO.getAttributeData() != null) {
                    for (AttributeDataTO attributeDataTOTemp : userTO.getAttributeData()) {
                        attributeNameToUpperCase(attributeDataTOTemp);
                        System.out.println("*****" + attributeDataTOTemp.getAttributeValue() + " ******");
                        if (enableCrypto) {
                            if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey
                                        .get(attributeDataTOTemp.getAttributeValue());
                                attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                                attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                            }
                            else {
                                System.out.println("*****" + attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) + " ******");
                                attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                                attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                            }
                        }
                        else {
                            if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                                attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                                attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                            }
                            else {
                                attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                            }
                        }
                    }
                }
                for (AttributeDataTO attributeDataTOTemp : userTO.getSearchAttributes()) {
                    attributeNameToUpperCase(attributeDataTOTemp);
                    if (enableCrypto) {
                        if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                            if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey
                                        .get(attributeDataTOTemp.getAttributeValue());
                                attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                                attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                            }
                            else {
                                attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                                attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                            }
                        }
                    }
                    else {
                        if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                            attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                            attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                        }
                        else {
                            attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                        }
                    }
                }
                System.out.println(" ** after beforeOnBoardAccount" + accountWE.getCryptoDID());
                iamExtension.editAttributes(accountWE.getAttributes(), accountWE.getId(), token);
                return accountWE;
            }
            catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                throw iamExceptionConvertorUtil.convertToAuthException(e);
            }
        }
        else {
            AccountWE accountWE = new AccountWE();
            accountWE.setId(UUIDGenerator.generate().substring(0, 14));
            return accountWE;
        }
    }

    private AccountWE onboardAccount(IAMExtensionV2 iamExtension, Token token, UserIciciTO userTO, Map<String, String> attributeValueWithPlainValue, String parentAccountId)
            throws AuthException, ValidationException {
        try {
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = null;
            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOs = new ArrayList<>();
            String decryptedAttributeValue = null;
            for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                attributeNameToUpperCase(attributeDataTO);
                attributeDataTO.setIsDefault(true);
                attributeDataTO.setIsRegistered(true);
                //this.attributeDataTOPass = attributeDataTO;
                decryptedAttributeValue = attributeDataTO.getAttributeValue();
                attributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                attributeTOs.add(attributeTO);
                attributeValueWithPlainValue.put(attributeTO.getAttributeValue(), decryptedAttributeValue);
            }
            /*System.out.println("Before set attribute Value" + attributeTO.getAttributeValue());
            this.attributeDataTOPass.setAttributeValue(decryptedAttributeValue);
            System.out.println("After set attribute value " + this.attributeDataTOPass.getAttributeValue());*/
            AccountType accountType = AccountType.USER;
            //AccountWE accountWE = iamExtension.createAccountIfNotExistWithToken(attributeTO.getAttributeName(), attributeTO.getAttributeValue(), accountType,token);
            AccountWE accountWE = iamExtension.createAccountIfNotExistWithToken(attributeTOs, accountType, token);
            if (accountWE.getId() != null) {
                List<in.fortytwo42.enterprise.extension.tos.AttributeTO> identifiers = new ArrayList<>();
                for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                    in.fortytwo42.enterprise.extension.tos.AttributeTO accountAttributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                    for (in.fortytwo42.enterprise.extension.tos.AttributeTO tempAccountAttributeTO : accountWE.getAttributes()) {
                        if (tempAccountAttributeTO.getAttributeName().equals(attributeDataTO.getAttributeName())
                            && tempAccountAttributeTO.getAttributeValue().equals(accountAttributeTO.getAttributeValue())) {
                            accountAttributeTO.setSignTransactionId(tempAccountAttributeTO.getSignTransactionId());
                            attributeDataTO.setSignTransactionId(tempAccountAttributeTO.getSignTransactionId());
                        }
                    }
                    identifiers.add(accountAttributeTO);
                }
                if (userTO.getAttributeData() != null) {
                    for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
                        attributeNameToUpperCase(attributeDataTO);
                        in.fortytwo42.enterprise.extension.tos.AttributeTO tempAttributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                        attributeValueWithPlainValue.put(tempAttributeTO.getAttributeValue(), attributeDataTO.getAttributeValue());
                        tempAttributeTO.setIsDefault(attributeDataTO.getIsDefault());
                        identifiers.add(tempAttributeTO);
                    }
                }
                accountWE.setAttributes(identifiers);

                // user credentials
                if (userTO.getUserCredential() != null) {
                    String decryptedAccountPassword = userTO.getUserCredential();
                    String hashedPassword = StringUtil.getHex(SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), decryptedAccountPassword).getBytes()));
                    accountWE.setUserCredential(hashedPassword);
                }

                // if Q&A present in request
                if (userTO.getQuestionAnswers() != null) {
                    List<in.fortytwo42.tos.transferobj.QuestionAnswerTO> weQuestionAnswers = userTO.getQuestionAnswers();
                    List<QuestionAnswerTO> weQuestionAnswerTOS = new ArrayList<>();
                    if (weQuestionAnswers != null && !weQuestionAnswers.isEmpty()) {
                        for (in.fortytwo42.tos.transferobj.QuestionAnswerTO weQuestionAnswer : weQuestionAnswers) {
                            QuestionAnswerTO questionAnswerTO = new QuestionAnswerTO();
                            questionAnswerTO.setQuestion(weQuestionAnswer.getQuestion());
                            String decryptedAnswer = weQuestionAnswer.getAnswer();
                            questionAnswerTO.setAnswer(StringUtil.getHex(SHAImpl.hashData256(StringUtil
                                    .build(IAMConstants.SALT, accountWE.getId(), decryptedAnswer.toLowerCase())
                                    .getBytes())));
                            weQuestionAnswerTOS.add(questionAnswerTO);
                        }
                        accountWE.setQuestionAnswers(weQuestionAnswerTOS);
                    }
                }
                else {
                    // question and answer
                    List<QuestionAnswerTO> weQuestionAnswerTOS = new ArrayList<>();
                    QuestionAnswerTO cityQuestion = new QuestionAnswerTO();
                    cityQuestion.setQuestion("What city were you born in?");
                    cityQuestion.setAnswer("Pune");
                    weQuestionAnswerTOS.add(cityQuestion);
                    QuestionAnswerTO middleNameQuestion = new QuestionAnswerTO();
                    middleNameQuestion.setQuestion("What is your mothers maiden name?");
                    middleNameQuestion.setAnswer("Pune");
                    weQuestionAnswerTOS.add(middleNameQuestion);
                    accountWE.setQuestionAnswers(weQuestionAnswerTOS);
                }
                accountWE.setParentAccountId(parentAccountId);
                AccountType type = userTO.getAccountType() != null ? AccountType.valueOf(userTO.getAccountType()) : AccountType.USER;
                accountWE.setAccountType(type);
                if (accountWE.getState() != null && (accountWE.getState().equals(Constant.PROVISIONED))) {
                    accountWE.setState("PARTIALLY_ACTIVE");
                }
            }
            return accountWE;
        }
        catch (IAMException e) {
            AuthException exception = iamExceptionConvertorUtil.convertToAuthException(e);
            logger.log(Level.ERROR, e.getMessage(), e);
            throw exception;
        }

    }

    private boolean isValidSearchAttributes(UserIciciTO userTO, boolean isEncrypted) throws AuthException {
        List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
        in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = null;
        List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOs = new ArrayList<>();
        AccountType accountType = userTO.getAccountType() != null ? AccountType.valueOf(userTO.getAccountType()) : AccountType.USER;
        for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
            attributeNameToUpperCase(attributeDataTO);
            attributeDataTO.setIsDefault(true);
            attributeDataTO.setIsRegistered(true);
            attributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, isEncrypted);
            attributeTOs.add(attributeTO);
        }
        return isEncrypted;
    }

    private AccountWE onboardAccount(IAMExtensionV2 iamExtension, Token token, UserTO userTO, String parentAccountId, boolean isEncrypted) throws AuthException {
        try {
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = null;
            for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
                attributeNameToUpperCase(attributeDataTO);
                if (attributeDataTO.getIsRegistered() != null && attributeDataTO.getIsRegistered()) {
                    attributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, isEncrypted);
                }
            }
            AccountType accountType = AccountType.USER;
            AccountWE accountWE = iamExtension.createAccountIfNotExist(attributeTO.getAttributeName(), attributeTO.getAttributeValue(), accountType);
            String accountId = accountWE.getId();
            if (accountId != null) {
                List<in.fortytwo42.enterprise.extension.tos.AttributeTO> identifiers = new ArrayList<>();
                for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
                    identifiers.add(getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, isEncrypted));
                }
                accountWE.setAttributes(identifiers);
                String decrypteAccountPassword = userTO.getUserCredential();
                if (isEncrypted) {
                    try {
                        decrypteAccountPassword = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), userTO.getUserCredential());
                    }
                    catch (Exception e) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(),errorConstant.getERROR_MESSAGE_USER_PASSWORD());
                    }
                }
                String hashedPassword = StringUtil.getHex(
                        SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountId, decrypteAccountPassword).getBytes()));
                accountWE.setUserCredential(hashedPassword);
                List<in.fortytwo42.tos.transferobj.QuestionAnswerTO> weQuestionAnswers = userTO.getQuestionAnswers();
                List<QuestionAnswerTO> weQuestionAnswerTOS = new ArrayList<>();
                if (weQuestionAnswers != null && !weQuestionAnswers.isEmpty()) {
                    for (in.fortytwo42.tos.transferobj.QuestionAnswerTO weQuestionAnswer : weQuestionAnswers) {
                        QuestionAnswerTO questionAnswerTO = new QuestionAnswerTO();
                        questionAnswerTO.setQuestion(weQuestionAnswer.getQuestion());
                        String decryptedAnswer = weQuestionAnswer.getAnswer();
                        if (isEncrypted) {
                            try {
                                decryptedAnswer = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), weQuestionAnswer.getAnswer());
                            }
                            catch (Exception e) {
                                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(),errorConstant.getERROR_MESSAGE_USER_PASSWORD());
                            }
                        }
                        questionAnswerTO.setAnswer(StringUtil.getHex(SHAImpl.hashData256(StringUtil
                                .build(IAMConstants.SALT, accountId, decryptedAnswer.toLowerCase())
                                .getBytes())));
                        weQuestionAnswerTOS.add(questionAnswerTO);
                    }
                }
                else if (accountType.equals(AccountType.ADMIN) || accountType.equals(AccountType.SUPER_ADMIN)) {
                    QuestionAnswerTO cityQuestion = new QuestionAnswerTO();
                    cityQuestion.setQuestion("What city were you born in?");
                    cityQuestion.setAnswer("Pune");
                    weQuestionAnswerTOS.add(cityQuestion);
                    QuestionAnswerTO middleNameQuestion = new QuestionAnswerTO();
                    middleNameQuestion.setQuestion("What is your mothers maiden name?");
                    middleNameQuestion.setAnswer("Pune");
                    weQuestionAnswerTOS.add(middleNameQuestion);
                }
                accountWE.setQuestionAnswers(weQuestionAnswerTOS);
                accountWE.setParentAccountId(parentAccountId);
                accountWE.setAccountType(AccountType.USER);
                if (accountWE.getState() != null && (accountWE.getState().equals(Constant.PROVISIONED)))
                    try {
                        accountWE.setState(userTO.getState());
                        AccountWE account = iamExtension.onboardAccount(accountWE, accountId, false, token);
                        account.setUserCredential(accountWE.getUserCredential());
                        return account;
                    }
                    catch (IAMException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                        throw new AuthException(e, errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), e.getMessage());
                    }
            }
            return accountWE;
        }
        catch (IAMException e) {
            AuthException exception = iamExceptionConvertorUtil.convertToAuthException(e);
            logger.log(Level.ERROR, e.getMessage(), e);
            throw exception;
        }
    }

    private void onboardConsumer(IAMExtensionV2 iamExtension, Token token, String accountId) throws IAMException {
        iamExtension.createConsumerIfNotExistEnterpriseToken(accountId, token);
    }

    private in.fortytwo42.enterprise.extension.tos.AttributeTO getAttributeFromAttributeData(AttributeDataTO attributeDataTO, List<AttributeMetadataTO> attributeMetaDataTOs, boolean isEncrypted)
            throws AuthException {
        attributeNameToUpperCase(attributeDataTO);
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName("OTHERS");
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String securityType = attributeMetadataTO.getAttributeStoreSecurityPolicy();
        String maskPattern = (String) attributeMetadataTO.getAttributeSettings().get(Constant.MASK_PATTERN);
        in.fortytwo42.enterprise.extension.tos.AttributeTO attribute = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        attribute.setAttributeName(attributeDataTO.getAttributeName());
        attribute.setIsDefault(attributeDataTO.getIsDefault());
        String decryptedAttributeValue = attributeDataTO.getAttributeValue();
        if (isEncrypted) {
            try {
                decryptedAttributeValue = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), attributeDataTO.getAttributeValue());
            }
            catch (Exception e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_PASSWORD());
            }
        }
        if (maskPattern != null) {
            //attribute.setMaskAttributeValue(decryptedAttributeValue.replaceAll(config.getProperty(Constant.MASK_PATTERN), config.getProperty(Constant.MASK_CHARACTER)));
        }
        if (attributeMetadataTO.getIsUnique() != null) {
            attribute.setIsUnique(attributeMetadataTO.getIsUnique());
            attributeDataTO.setIsUnique(attributeMetadataTO.getIsUnique());
        }
        attribute.setAttributeValue(applySecurityPolicy(decryptedAttributeValue, AttributeSecurityType.valueOf(securityType)));
        return attribute;
    }

    private String applySecurityPolicy(String attributeValue, AttributeSecurityType attributeSecurityType) {
        String hashedAttributeValue;
        if (attributeSecurityType == AttributeSecurityType.SHA512) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData512(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        }
        else if (attributeSecurityType == AttributeSecurityType.SHA256) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData256(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        }
        else {
            hashedAttributeValue = attributeValue;
        }
        return hashedAttributeValue.toUpperCase();
    }

    @Override
    public CSVUploadTO uploadOnboardUsers(String fileType, InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("YYYYMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("IST"));
        String dateFormatted = formatter.format(date);
        String requestId = UUID.randomUUID().toString();
        String filename = fileName.split(".csv")[0] + "_" + dateFormatted + "_" + requestId + ".csv";
        CSVUploadTO csvUploadTO = new CSVUploadTO();
        csvUploadTO.setRequestId(requestId);
        csvUploadTO.setFileName(filename);
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            CsvFactory.processCsv(fileType, inputStream, role, username,id, filename);
        });
        return csvUploadTO;
    }

    @Override
    public UserResponseTO authenticate(UserAuthenticationTO userTO, String ipAddress, String userAgent) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " authenticate : start");

        UserAuthPrincipal userAuthPrincipal = null;
        Long count = null;
        Session session = sessionFactoryUtil.getSession();
        try {
            Integer enterprisecount = ServiceFactory.getEnterpriseService().getEnterpriseCount();
            try {
                count = userAuthPrincipalDao.getUserAuthPrincipalCount(userTO.getUsername(), Constant.ORIGIN);
            }
            catch (UserNotFoundException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                count = 0l;
            }
            User user = userService.authenticate(userTO.getUsername(), userTO.getPassword());
            if (count >= 1) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SESSION_IS_ALREADY_ACTIVE(), errorConstant.getERROR_MESSAGE_USER_SESSION_IS_ALREADY_ACTIVE());
            }
            Set<Role> roleList = user.getRoles();
            if (enterprisecount <= 0) {
                Role role = new Role();
                role.setName(UserRole.SUPER_ADMIN);
                if (!roleList.contains(role)) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_ONBOARDED(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_ONBOARDED());
                }
            }
            StringBuilder roles = new StringBuilder();
            StringBuilder permissions = new StringBuilder();
            for (Role role : roleList) {
                roles.append(role.getName().name()).append(Constant._COMMA);
            }
            roles.deleteCharAt(roles.lastIndexOf(Constant._COMMA));
            UserResponseTO userResponseTO = new UserResponseTO();
            String username = userTO.getUsername();
            String token = getToken(user.getId(),username, roles.toString(), permissions.toString(), ipAddress, userAgent, false);
            userResponseTO.setToken(token);
            userResponseTO.setStatus(Constant.SUCCESS_STATUS);
            userAuthPrincipalService.userAuditLog(session, userTO.getUsername(), AuthenticationStatus.SUCCESS, SessionState.ACTIVE, token);
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " authenticate : end");
            sessionFactoryUtil.closeSession(session);
            AuditLogUtil.sendAuditLog(AuditLogConstant.USER_LOGIN + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.AUTHENTICATION, user.getAccountId(), IdType.ACCOUNT, "",
                    "", user.getAccountId(), null);
            return userResponseTO;

        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            AuditLogUtil.sendAuditLog("User login failed", "ENTERPRISE", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", "", "", null);
            throw e;
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            AuditLogUtil.sendAuditLog("User login failed", "ENTERPRISE", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", "", "", null);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID());
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }

    }

    @Override
    public UserResponseTO authenticateADorNonADUser(UserAuthenticationTO userTO, String ipAddress, String userAgent) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " authenticateADorNonADUser : start");
        Long count;
        Session session = sessionFactoryUtil.getSession();
      //  Timestamp adminLogoutTime = null;
        Timestamp adminLoginTime = new Timestamp(System.currentTimeMillis());
        LoginStatus status = LoginStatus.S;
        String roleUser=null;
        try {
            Integer enterprisecount = ServiceFactory.getEnterpriseService().getEnterpriseCount();
            try {
                count = userAuthPrincipalDao.getUserAuthPrincipalCount(userTO.getUsername(), Constant.ORIGIN);
            }
            catch (UserNotFoundException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                count = 0l;
            }
            User user;
            AdfsDetailsTO adfsDetails = null;
            try {
//                user = attributeStoreDao.getUserByAttributeValueWithUpperCase(userTO.getUsername());
                String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
                Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
                if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                    user = attributeStoreDao.getActiveAttributeWithoutCase(Constant.USER_ID, userTO.getUsername().toUpperCase()).getUser();
                }else {
                    user = attributeStoreDao.getActiveAttributeWithoutCase(Constant.USER_ID, userTO.getUsername()).getUser();
                }
                roleUser= user.getRoles().stream().findFirst().get().getName().name();
            }
            catch (AttributeNotFoundException e) {
                status = LoginStatus.F;
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID());
            }
            if(!user.getUserState().equals(UserState.A)){
                status = LoginStatus.F;
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_USER_STATE());
            }
            Role userRole = new Role();
            userRole.setName(UserRole.USER);
            if (user.getRoles().contains(userRole)) {
                status = LoginStatus.F;
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID());
            }
            Role superAdmin = new Role();
            superAdmin.setName(UserRole.SUPER_ADMIN);
            if (!user.getRoles().contains(superAdmin)) {
                validateLastLoginTime(user, session);
            }
            if (Constant.ADFS.equals(user.getAuthType())) {
                try {
                adfsDetails = userService.authenticateADUser(userTO.getUsername(), userTO.getPassword());
                }catch (AuthException e){
                    status = LoginStatus.F1FA;
                    throw  e;
                }
            }
            else {
                try {
                authenticateNonADUser(userTO, user, session);
                }catch (AuthException e){
                    status=LoginStatus.F;
                    throw e;
                }
            }
            if (count >= 1) {
                status = LoginStatus.F;
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SESSION_IS_ALREADY_ACTIVE(), errorConstant.getERROR_MESSAGE_USER_SESSION_IS_ALREADY_ACTIVE());
            }
            String isMFAEnabled = config.getProperty(Constant.MFA_EENABLED);
            UserResponseTO userResponseTO = new UserResponseTO();
            if (Constant.ADFS.equals(user.getAuthType()) && StringUtil.isNotNullOrEmpty(isMFAEnabled) && Boolean.parseBoolean(isMFAEnabled)) {
                if(!StringUtil.isNotNullOrEmpty(adfsDetails.getEmail()) && !StringUtil.isNotNullOrEmpty(adfsDetails.getMobile())){
                    status = LoginStatus.F1FA;
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
                }
                Set<Role> roleList = user.getRoles();
                if (enterprisecount <= 0) {
                    Role role = new Role();
                    role.setName(UserRole.SUPER_ADMIN);
                    if (!roleList.contains(role)) {
                        status = LoginStatus.F1FA;
                        throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_ONBOARDED(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_ONBOARDED());
                    }
                }
                AdHotpTO adHotpTO = new AdHotpTO();
                List<NotificationTO> notifications = new ArrayList<>();

                if (StringUtil.isNotNullOrEmpty(adfsDetails.getMobile())) {
                    NotificationTO notification = new NotificationTO();
                    notification.setMessageBody("Dear Customer, <OTP> is the OTP to LOGIN to DIMFA. OTPs are SECRET. DO NOT disclose it to anyone. Bank NEVER asks for OTP.");
                    notification.setNotificationType("SMS");
                    notification.setTemplateId("TMPT1001");
                    notifications.add(notification);
                }
                if (StringUtil.isNotNullOrEmpty(adfsDetails.getEmail())) {
                    NotificationTO notification = new NotificationTO();
                    notification.setMessageBody("Dear Customer, <OTP> is the OTP to LOGIN to DIMFA. OTPs are SECRET. DO NOT disclose it to anyone. Bank NEVER asks for OTP.");
                    notification.setNotificationType("EMAIL");
                    notification.setTemplateId("TMPT1001");
                    notifications.add(notification);
                }
                if(adHotpTO.getSearchAttributes()==null){
                    List<AttributeDataTO> attributeDataTOList = new ArrayList<>();
                    AttributeDataTO attributeDataTO = new AttributeDataTO();
                    attributeDataTO.setAttributeName(Constant.USER_ID);
                    attributeDataTO.setAttributeValue(userTO.getUsername());
                    attributeDataTOList.add(attributeDataTO);
                    adHotpTO.setSearchAttributes(attributeDataTOList);
                }
                adHotpTO.setNotification(notifications);
                Enterprise enterprise;
                try{
                    enterprise = DaoFactory.getEnterpriseDao().getEnterpriseByAccountId(config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID));
                } catch (NotFoundException e) {
                    status = LoginStatus.F2FA;
                    throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_FOUND());
                }
                AdHotpTO optResponse = ServiceFactory.getAdHotpService().generateHotp(adHotpTO, session, enterprise.getEnterpriseId(),enterprise.getEnterpriseName(), userTO.getUsername(), adfsDetails, null);
                boolean isOtpSuccess = false;
                for (NotificationTO notification : optResponse.getNotification()) {
                    if (NotificationStatus.SUCCESS.equals(notification.getStatus())) {
                        isOtpSuccess = true;
                        break;
                    }
                }
                if (isOtpSuccess) {
                    String token = getToken(user.getId(),userTO.getUsername(), Constant.MFA, "", ipAddress,
                            userAgent, true);
                    userResponseTO.setToken(token);
                    userResponseTO.setStatus(Constant.SUCCESS_STATUS);
                    userResponseTO.setMfaEnabled(true);
                    userResponseTO.setTokenTtl(Integer.parseInt(Config.getInstance().getProperty(Constant.TTL)));
                    status = LoginStatus.S1FA;
                }
                else {
                    status = LoginStatus.F1FA;
                    throw new AuthException(null, errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_HOTP_GENERATION_FAILED());
                }
            }
            else {
                Set<Role> roleList = user.getRoles();
                if (enterprisecount <= 0) {
                    Role role = new Role();
                    role.setName(UserRole.SUPER_ADMIN);
                    if (!roleList.contains(role)) {
                        status = LoginStatus.F;
                        throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_ONBOARDED(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_ONBOARDED());
                    }
                }
                StringBuilder roles = new StringBuilder();
                StringBuilder permissions = new StringBuilder();
                for (Role role : roleList) {
                    roles.append(role.getName().name()).append(Constant._COMMA);
                }
                roles.deleteCharAt(roles.lastIndexOf(Constant._COMMA));
                String token = getToken(user.getId(),userTO.getUsername().toUpperCase(), roles.toString(),
                        permissions.toString(), ipAddress, userAgent, false);
                userResponseTO.setToken(token);
                userResponseTO.setStatus(Constant.SUCCESS_STATUS);
                userResponseTO.setMfaEnabled(false);
                userAuthPrincipalService.userAuditLog(session, userTO.getUsername(), AuthenticationStatus.SUCCESS, SessionState.ACTIVE, token);
            }
            adminLoginLogService.adminLoginLog( userTO.getUsername(), status,adminLoginTime ,AdminSessionState.A,roleUser);
            sessionFactoryUtil.closeSession(session);
            AuditLogUtil.sendAuditLog(AuditLogConstant.USER_LOGIN + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.AUTHENTICATION, user.getAccountId(),
                    IdType.ACCOUNT, ThreadContext.get(Constant.REQUEST_REFERENCE), "", user.getAccountId(), null);
            return userResponseTO;
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            adminLoginLogService.adminLoginLog(userTO.getUsername(), status, adminLoginTime, AdminSessionState.C,roleUser);
            AuditLogUtil.sendAuditLog("User login failed", "ENTERPRISE", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", "", "", null);
            throw e;
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            adminLoginLogService.adminLoginLog(userTO.getUsername(), LoginStatus.F, adminLoginTime, AdminSessionState.C,roleUser);
            AuditLogUtil.sendAuditLog("User login failed", "ENTERPRISE", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", "", "", null);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID());
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " authenticateADorNonADUser : end");
        }
    }

    private void authenticateNonADUser(UserAuthenticationTO userTO, User user, Session session) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " authenticateNonADUser : start");
        int maxRetries = config.getProperty(Constant.ADMIN_LOGIN_RETRIES)!=null ? Integer.parseInt(config.getProperty(Constant.ADMIN_LOGIN_RETRIES)) : 3;

        int retriesLeft = user.getRetriesLeft()!=null ? user.getRetriesLeft() : maxRetries;
        try{
            if(user.getUserStatus().equals(UserStatus.BLOCK)){
                handleBlockedUser(user, maxRetries, session);
            }
            userService.authenticateNonADUser(user, userTO.getUsername().toUpperCase(), userTO.getPassword());
            if(retriesLeft != maxRetries){
                resetUserRetries(user, session, maxRetries);
            }
        }
        catch(AuthException e){
            if(e.getErrorCode().equals(errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID())){
                handleInvalidPassword(user, session, maxRetries);
            }
            throw e;
        }
        finally{
            if(!session.isJoinedToTransaction()){
                session.beginTransaction();
            }
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " authenticateNonADUser : end");
        }
    }

    private void resetUserRetries(User user, Session session, int maxRetries) {
        user.setRetriesLeft(maxRetries);
        userService.updateUser(session, user);
        session.getTransaction().commit();
    }

    private void handleBlockedUser(User user, int maxRetries, Session session) throws AuthException {
        Long accLockDuration = config.getProperty(Constant.ACCOUNT_LOCK_DURATION_IN_MILLIS)!=null ? Long.parseLong(config.getProperty(Constant.ACCOUNT_LOCK_DURATION_IN_MILLIS)) : 86400000L;

        Long unblockTime = System.currentTimeMillis() - accLockDuration;
        if(user.getLastLockoutTime() != null && user.getLastLockoutTime() > unblockTime){
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_BLOCK(), errorConstant.getERROR_MESSAGE_USER_BLOCK());
        }
        else{
            user.setUserStatus(UserStatus.ACTIVE);
            user.setLastLockoutTime(0L);
            resetUserRetries(user,session,maxRetries);
        }
    }

    private void handleInvalidPassword(User user, Session session, int maxRetries) {
        int retriesLeft;
        retriesLeft = user.getRetriesLeft()!=null ? user.getRetriesLeft() : maxRetries;
        if(retriesLeft>0){
            --retriesLeft;
            user.setRetriesLeft(retriesLeft);
        }
        if(retriesLeft==0){
            user.setUserStatus(UserStatus.BLOCK);
            user.setLastLockoutTime(System.currentTimeMillis());
        }
        userService.updateUser(session, user);
        session.getTransaction().commit();
    }

    public String getToken(Long id,String username, String type, String permissions, String ipAddress, String userAgent, boolean isGrantType) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getToken : start");
        Map<String, Object> payload = new HashMap<>();
        payload.put(Constant.ID,id);
        payload.put(Constant.USER_NAME, username);
        payload.put(Constant.STATE, Constant.SUCCESS_STATUS);
        if(isGrantType){
            payload.put(Constant.GRANT_TYPE, type);
        }
        else {
            payload.put(Constant.ROLE, type);
        }
        payload.put(Constant.PERMISSIONS, permissions);
        payload.put(Constant.IP_ADDRESS, ipAddress);
        payload.put(Constant.USER_AGENT, userAgent);

        Config config = Config.getInstance();
        String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
        payload.put(Constant.HEADER_ENTERPRISE_ACCOUNT_ID, enterpriseAccountId);

        String issuer = config.getProperty(Constant.ISSUER);
        String user = config.getProperty(Constant.TOKEN_USER);
        String tokenValidity = config.getProperty(Constant.TOKEN_VALIDITY);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getToken : end");
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getToken : start" + payload.toString());
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getToken : start" + ipAddress + userAgent);
        return JWTokenImpl.generateToken(KeyManagementUtil.getAESKey(), RandomString.nextInt(1), issuer, user, payload, Long.valueOf(tokenValidity));
    }

    @Override
    public String readSampleCsvFile(String fileName) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " readSampleCsvFile : start");
        fileName = fileName != null ? fileName : "user-onboard.csv";
        String content = FileUtil.getSampleUserOnboardCsv(fileName);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " readSampleCsvFile : end");
        return content;
    }

    @Override
    public String readSampleEditUserStatusCsvFile(String fileName) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " readSampleEditUserStatusCsvFile : start");
        fileName = fileName != null ? fileName : "bulk_edit_user_status.csv";
        String content = FileUtil.getSampleUserOnboardCsv(fileName);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " readSampleEditUserStatusCsvFile : end");
        return content;
    }

    @Override
    public String readSampleUserApplicationMappingCsvFile(String fileName) {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " readSampleUserApplicationMappingCsvFile : start");
        fileName = fileName != null ? fileName : "bulk_user_application_mapping.csv";
        String content = FileUtil.getSampleUserOnboardCsv(fileName);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " readSampleUserApplicationMappingCsvFile : end");
        return content;
    }

    @Override
    public void logout(String username, String token, Long expiry) {
        Session session = sessionFactoryUtil.getSession();
        try {
            adminLoginLogService.updateAdminLogoutTime( username, AdminSessionState.C, new Timestamp(System.currentTimeMillis()));
            userService.logout(session, username, token, expiry);
            AuditLogUtil.sendAuditLog(AuditLogConstant.USER_LOGOUT + AuditLogConstant.FOR_USER + username, "ENTERPRISE", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", "", "", null);
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

    /**
     * Gets the users.
     *
     * @param userUpdateStatus the user update status
     * @param page             the page
     * @param searchText       the search text
     * @param iamStatusFilter  the iam status filter
     * @param userStatusFilter the user status filter
     * @param _2faStatusFilter the 2 fa status filter
     * @param approvalStatus   the approval status
     * @param role             the role
     * @return the users
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<UserTO> getUsers(UserRole userRole, String userUpdateStatus, int page, String searchText,String attributeName, String iamStatusFilter, String userStatusFilter, String _2faStatusFilter,
            String approvalStatus, String userState, String role, String userTypeFilter, Long userGroupId, Boolean export) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUsers : start");
        permissionUtil.validateGetUsersPermission(role);
        PaginatedTO<UserTO> paginatedTO = userService.getUsers(userRole, userUpdateStatus, page, searchText, attributeName,iamStatusFilter, userStatusFilter, _2faStatusFilter, approvalStatus, userState, role, userTypeFilter,
                userGroupId, export);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUsers : end");
        return paginatedTO;
    }

    @Override
    public UserTO getUserDetails(String userAccountId, String role) throws AuthException {
        return userService.getUserDetails(userAccountId, role);
    }

    public UserTO getUserDetails(String userAccountId) throws AuthException {
        return userService.getUserDetails(userAccountId);
    }

    /**
     * Gets the subscribed applications by user.
     *
     * @param userId the user id
     * @param role   the role
     * @return the subscribed applications by user
     * @throws AuthException the auth exception
     */
    @Override
    public List<ApplicationTO> getSubscribedApplicationsByUser(Long userId, String role) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getSubscribedApplicationsByUser : start");
        permissionUtil.validateGetUsersPermission(role);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getSubscribedApplicationsByUser : end");
        return userApplicationRelService.getApplicationRelsForUser(userId);
    }

    @Override
    public List<ApplicationTO> getTunnelingSubscribedApplicationsByUser(Long userId, String role) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getSubscribedApplicationsByUser : start");
        permissionUtil.validateGetUsersPermission(role);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getSubscribedApplicationsByUser : end");
        return userApplicationRelService.getTunnelingApplicationRelsForUser(userId);

    }

    public boolean autoBindADUser(ADUserBindingTO adUserBindingTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " autoBindADUser : start");
        Application application = applicationService.getApplicationByApplicationId(adUserBindingTO.getApplicationId());
        User adUser = userService.getActiveUser(adUserBindingTO.getUserId());
        userService.validateUser(adUser);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " autoBindADUser : end");
        return bindUserToService(adUser, application, adUserBindingTO.getServiceName());
    }

    /**
     * Bind user to service.
     *
     * @param adUser      the ad user
     * @param application the application
     * @param serviceName the service name
     * @return true, if successful
     * @throws AuthException the auth exception
     */
    private boolean bindUserToService(User adUser, Application application, String serviceName) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " bindUserToService : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            Service service = serviceProcessor.getService(serviceName);
            IAMExtensionV2 iamExtension = IAMUtil.getInstance().getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = IAMUtil.getInstance().authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            UserApplicationServiceCompositeKey userApplicationServiceCompositeKey = new UserApplicationServiceCompositeKey();
            userApplicationServiceCompositeKey.setUser(adUser);
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
            if (!userApplicationRelDao.isApplicationUserBindingPresent(application.getId(), adUser.getId())) {
                boolean isADUserBindingDone = iamExtension.bindUserToApplication(adUser.getAccountId(), token);
                if (!isADUserBindingDone) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_FAILED(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_FAILED());
                }
            }
            if (userApplicationServiceRel == null) {
                userApplicationServiceRel = new UserApplicationServiceRel();
                userApplicationServiceRel.setId(userApplicationServiceCompositeKey);
                userApplicationServiceRel.setBindingStatus(BindingStatus.ACTIVE);
                userApplicationServiceRel.setTwoFactorStatus(TwoFactorStatus.ENABLED);
                userApplicationRelDao.create(session, userApplicationServiceRel);
            }
            else {
                userApplicationServiceRel.setBindingStatus(BindingStatus.ACTIVE);
                userApplicationRelDao.update(session, userApplicationServiceRel);
            }
            adUser.setIamStatus(IAMStatus.ENABLED);

            userDao.update(session, adUser);
            sessionFactoryUtil.closeSession(session);
            return true;
        }
        catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " bindUserToService : end");
        }
    }

    /**
     * Validate user application rel.
     *
     * @param userApplicationRel the user application rel
     * @throws AuthException the auth exception
     */
    private void validateUserApplicationRel(UserApplicationServiceRel userApplicationRel) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateUserApplicationRel : start");
        if (userApplicationRel != null) {
            if (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT());
            }
            if (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_BLOCKED(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_BLOCKED());
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateUserApplicationRel : end");
    }

    /**
     * Unbind AD user.
     *
     * @param adUserBindingTO the ad user binding TO
     * @return true, if successful
     * @throws AuthException the auth exception
     */
    @Override
    public boolean unbindADUser(ADUserBindingTO adUserBindingTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " unbindADUser : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            Application application = applicationService.getApplicationByApplicationId(adUserBindingTO.getApplicationId());
            User user = userService.getActiveUser(adUserBindingTO.getUserId());
            userService.validateUser(user);
            Service service = serviceProcessor.getService(adUserBindingTO.getServiceName());
            UserApplicationServiceRel adUserApplicationRel = userApplicationRelService.getUserApplicationRel(user, application, service);
            if (adUserApplicationRel == null || (adUserApplicationRel.getBindingStatus() != BindingStatus.ACTIVE && adUserApplicationRel.getBindingStatus() != BindingStatus.BLOCKED)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND());
            }
            boolean isSingleBindingPresent = userApplicationRelService.getUserAndApplicationRelCount(application.getId(), user.getId()) == 1;
            boolean isConsumerUnbind = false;
            if (isSingleBindingPresent) {
                IAMExtensionV2 iamExtension = IAMUtil.getInstance().getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
                Token token = IAMUtil.getInstance().authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
                isConsumerUnbind = iamExtension.unbindConsumerApplication(token, user.getAccountId());
            }
            else {
                isConsumerUnbind = true;
            }
            if (isConsumerUnbind) {
                adUserApplicationRel.setBindingStatus(BindingStatus.INACTIVE);
                userApplicationRelDao.update(session, adUserApplicationRel);
            }
            userService.updateIAMStatusToDisabled(session, user);
            sessionFactoryUtil.closeSession(session);
            return isConsumerUnbind;
        }
        catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " unbindADUser : end");
        }
    }

    /**
     * Bind services to user.
     *
     * @param userBindingTO the user binding TO
     * @param role          the role
     * @param actor         the actor
     * @throws AuthException the auth exception
     */
    @Override
    public void bindServicesToUser(UserBindingTO userBindingTO, String role, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " bindServicesToUser : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            approveBindServicesToUser(session, userBindingTO, role, actor,id, saveRequest, false);
            sessionFactoryUtil.closeSession(session);
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " bindServicesToUser : end");
        }
        catch (AuthException e) {
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

    @Override
    public void approveBindServicesToUser(Session session, UserBindingTO userBindingTO, String role, String actor,Long id, boolean saveRequest, boolean checkerApproved) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " bindServicesToUser : start");
        try {
            User user = userService.getActiveUser(session, userBindingTO.getId());
            permissionUtil.validateEditUserPermission(role);
            userService.validateUser(user);
            ApplicationTO applicationTO = userBindingTO.getApplication();
            Application application = applicationService.getApplicationByApplicationId(applicationTO.getApplicationId());
            List<UserApplicationServiceRel> userApplicationStagingRels = new ArrayList<>();
            List<UserApplicationServiceRel> userApplicationRels = userApplicationRelDao.getApplicationRelsForUser(user.getId());

            boolean isMobileApplicationBindingPresent = stageUserApplicationServiceBindingInDB(session, user, applicationTO, userApplicationStagingRels, userApplicationRels, application, saveRequest);
            if (!userApplicationStagingRels.isEmpty()) {
                if (!checkerApproved) {
                    requestService.createUserServiceBindRequest(session, userBindingTO, actor,id, saveRequest);
                }

                if (!saveRequest) {
                    if (isMobileApplicationBindingPresent) {
                        updateIamStatusToEnabled(session, user);
                        enableToken(application, user, userApplicationStagingRels);
                    }
                    else {
                        Service service = userApplicationStagingRels.get(0).getId().getService();
                        // default should be that - user consent should be sent
                        boolean isUserConsentRequired = userBindingTO.getUserConsentRequired() == null || userBindingTO.getUserConsentRequired();
                        if (isUserConsentRequired) {
                            createBindingTransactionForUser(session, user, application, service.getServiceName(), Constant._ASTERICKS);
                        }
                        else {
                            List<ApplicationTO> applicationTOS = new ArrayList<>();
                            applicationTOS.add(applicationTO);
                            userService.autoBindUserToApplication(session, applicationTOS, user, null);
                        }
                    }
                }
            }
            else {
                logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " bindServicesToUser : end");
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_SERVICE_ALREADY_PRESENT());
            }
            AuditLogUtil.sendAuditLog(AuditLogConstant.APPLICATION_SERVICE_BIND_SUCCESSFUL + AuditLogConstant.BY
                                      + actor
                                      + AuditLogConstant.FOR_APPLICATION
                                      + userBindingTO.getApplication().getApplicationName()
                                      + AuditLogConstant.FOR_USER
                                      + userBindingTO.getUsername(),
                    "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", user.getAccountId(), null);
        }
        catch (IAMException e) {
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        catch (ServiceNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
        }
    }

    private void updateIamStatusToEnabled(Session session, User user) {
        if (user.getIamStatus() != IAMStatus.ENABLED) {
            user.setIamStatus(IAMStatus.ENABLED);
            userDao.update(session, user);
        }
    }

    private void createBindingTransactionForUser(Session session, User user, Application application, String serviceName, String serverId)
            throws IAMException, ServiceNotFoundException {
        String transactionId = RandomString.nextString(20);
        BindingInfoV2.Builder builder = new BindingInfoV2.Builder();
        builder.consumerAccountId(user.getAccountId());
        int timeout = Constant.DEFAULT_BINDING_TIMEOUT;
        try {
            timeout = Integer.parseInt(Config.getInstance().getProperty(Constant.BINDING_APPROVAL_TIMEOUT));
        }
        catch (NumberFormatException e) {
            timeout = 5000;
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        builder.timeOut(timeout);
        builder.transactionDetails(Constant.BINDING_REQUEST_DETAILS + application.getApplicationName());
        builder.transactionSummary(Constant.BINDING_REQUEST);
        builder.transactionId(transactionId);
        builder.serviceName(serviceName);
        builder.serverId(serverId);
        BindingInfoV2 consumerRegistrationInfo = builder.build();

        IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
        Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
        iamExtension.initConsumerApplicationBinding(token, consumerRegistrationInfo);
        saveAuthAttemptToDb(session, user, application, serviceName, transactionId, timeout);
    }

    private void saveAuthAttemptToDb(Session session, User user, Application application, String serviceName, String transactionId, long timeout) throws ServiceNotFoundException {
        AuthenticationAttempt authenticationAttempt = new AuthenticationAttempt();
        authenticationAttempt.setTransactionId(transactionId);
        authenticationAttempt.setTransactionDetails(Constant.BINDING_REQUEST_DETAILS + application.getApplicationName());
        authenticationAttempt.setTransactionSummary(Constant.BINDING_REQUEST);
        authenticationAttempt.setAttemptType(Constant.REGULATORY);
        authenticationAttempt.setIsPinCheckRequired(true);
        authenticationAttempt.setTimeout(timeout);
        authenticationAttempt.setAttemptStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING.name());
        authenticationAttempt.setDateTimeCreated(new Timestamp(System.currentTimeMillis()));
        authenticationAttempt.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
        authenticationAttempt.setService(serviceDao.getServiceByServiceName(serviceName));
        authenticationAttempt.setUser(user);
        authenticationAttempt.setSenderAccountId(application.getApplicationAccountId());
        //        authenticationAttempt.setSenderIdDetails(application.getApplicationId());
        setSenderDetails(application, authenticationAttempt);
        authenticationAttemptDao.create(session, authenticationAttempt);
        authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
    }

    private void setSenderDetails(Application application, AuthenticationAttempt authenticationAttempt) {
        Map<String, String> senderDetails = new HashMap<>();
        authenticationAttempt.setSenderAccountId(application.getApplicationAccountId());
        senderDetails.put(Constant.ID, application.getApplicationId());
        senderDetails.put(Constant.NAME, application.getApplicationName());
        senderDetails.put(Constant.PARENT_ID, config.getProperty(Constant.ENTERPRISE_ID));
        senderDetails.put(Constant.PARENT_NAME, config.getProperty(Constant.ENTERPRISE_NAME));
        authenticationAttempt.setSenderIdDetails(new Gson().toJson(senderDetails));
    }

    /**
     * Stage user application service binding in DB.
     *
     * @param user                       the user
     * @param applicationTO              the application TO
     * @param userApplicationStagingRels the user application staging rels
     * @param userApplicationRels        the user application rels
     * @param application                the application
     * @throws AuthException the auth exception
     */
    private boolean stageUserApplicationServiceBindingInDB(Session session, User user, ApplicationTO applicationTO, List<UserApplicationServiceRel> userApplicationStagingRels,
            List<UserApplicationServiceRel> userApplicationRels, Application application, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " stageUserApplicationServiceBindingInDB : start");
        List<ServiceTO> userServiceTOs = applicationTO.getServices();
        List<Service> applicationServices = application.getServices();
        boolean isMobileApplicationBindingPresent = false;
        for (ServiceTO serviceTO : userServiceTOs) {
            Service service = serviceProcessor.getService(serviceTO.getServiceName());
            if (!applicationServices.contains(service)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SERVICE_FOR_APPLICATION(), errorConstant.getERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION());
            }
            UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
            userApplicationCompositeKey.setUser(user);
            userApplicationCompositeKey.setApplication(application);
            userApplicationCompositeKey.setService(service);
            UserApplicationServiceRel userApplicationRel = new UserApplicationServiceRel();
            userApplicationRel.setId(userApplicationCompositeKey);
            if (!userApplicationRels.contains(userApplicationRel)) {

                UserApplicationServiceCompositeKey tempUserApplicationServiceCompositeKey = new UserApplicationServiceCompositeKey();
                tempUserApplicationServiceCompositeKey.setUser(user);
                tempUserApplicationServiceCompositeKey.setApplication(application);
                tempUserApplicationServiceCompositeKey.setService(service);
                UserApplicationServiceRel userApplicationServiceRel = null;
                if (!isMobileApplicationBindingPresent) {
                    isMobileApplicationBindingPresent = userApplicationRelService.getUserAndApplicationRelCount(application.getId(), user.getId()) > 0;
                }
                if (!saveRequest && isMobileApplicationBindingPresent) {
                    try {
                        userApplicationServiceRel = userApplicationRelDao.getUserApplicationForId(tempUserApplicationServiceCompositeKey);
                        userApplicationServiceRel.setBindingStatus(BindingStatus.ACTIVE);
                        userApplicationRelDao.update(session, userApplicationServiceRel);
                    }
                    catch (UserApplicationRelNotFoundException e) {
                    }
                    if (userApplicationServiceRel == null) {
                        userApplicationServiceRel = new UserApplicationServiceRel();
                        userApplicationServiceRel.setId(userApplicationCompositeKey);
                        userApplicationServiceRel.setBindingStatus(BindingStatus.ACTIVE);
                        userApplicationServiceRel.setTwoFactorStatus(TwoFactorStatus.ENABLED);
                        userApplicationRelDao.create(session, userApplicationServiceRel);
                    }
                    userApplicationStagingRels.add(userApplicationServiceRel);
                }
                else {
                    userApplicationStagingRels.add(userApplicationRel);
                }
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " stageUserApplicationServiceBindingInDB : end");
        return isMobileApplicationBindingPresent;

    }

    /**
     * Edits the user.
     *
     * @param userTO the user TO
     * @param role   the role
     * @param actor  the actor
     * @return the user TO
     * @throws AuthException the auth exception
     */
    @Override
    public UserTO editUser(UserTO userTO, String role, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUser : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            User user = userService.getActiveUser(session, userTO.getUserId());
            isValidUserEdit(user.convertToTO(), userTO, role);
            userTO = requestService.createUserEditRequest(session, userTO, actor,id, saveRequest);
            if (!saveRequest) {
                userTO = approveEditUser(session, userTO, role, actor);
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
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUser : end");
        return userTO;
    }

    @Override
    public UserTO editUserRole(UserTO userTO, String role, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUserRole : start");
        AccountWE accountWE = null;
        Session session = sessionFactoryUtil.getSession();
        try {
            User user = userService.getActiveUser(session, userTO.getUserId());
            if (userTO.getIamUserStatus() == null || userTO.getIamUserStatus().isEmpty()) {
                List<AttributeStore> attributeStore = attributeStoreDao.getAttributeByUserIdAndState(session, AttributeState.ACTIVE, user.getId());
                String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
                IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(enterpriseAccountId);
                List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
                for (AttributeStore attributeStore1 : attributeStore) {
                    AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
                    attributeMetadataTO.setAttributeName(attributeStore1.getAttributeName());
                    int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
                    if (index < 0) {
                        attributeMetadataTO.setAttributeName("OTHERS");
                        index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
                    }
                    attributeMetadataTO = attributeMetaDataTOs.get(index);
                    if (attributeMetadataTO.getIsUnique() != null && attributeMetadataTO.getIsUnique()) {
                        accountWE = iamExtension.getAccount(attributeStore1.getAttributeName().toUpperCase(),
                                attributeStore1.getAttributeValue().toUpperCase());
                    }
                    if (accountWE != null) {
                        break;
                    }
                }
                if (accountWE != null ) {
                    userTO.setIamUserStatus(accountWE.getState());
                }
            }
            isValidUserEditRole(user.convertToTO(), userTO, role);
            userTO = requestService.createUserRoleEditRequest(session, userTO, actor,id, saveRequest);
            if (!saveRequest) {
                userTO = updateUserRole(session, userTO, role, actor);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        catch (IAMException | AttributeNotFoundException | NotFoundException e) {
            throw new AuthException(new Exception(),errorConstant.getERROR_CODE_INVALID_DATA(),e.getMessage());
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUserRole : end");
        return userTO;

    }

    @Override
    public UserTO editUserLastLogInTime(UserTO userTO, String role, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUserLastLogInTime : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            userTO = requestService.createUserEditLastLogInTimeRequest(session, userTO, actor,id, saveRequest);
            if (!saveRequest) {
                userTO = approveEditUserTimestamp(session, userTO, role, actor);
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
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUserLastLogInTime : end");
        return userTO;
    }

    public UserTO editUserAttributes(UserTO userTO, String role, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUserAttributes : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            userTO = requestService.createUserEditAttributesRequest(session, userTO, actor,id, saveRequest);
            if (!saveRequest) {
                userTO = approveEditUserAttributes(session, userTO, role, actor);
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
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUserAttributes : end");
        return userTO;
    }

    public UserTO editUser(UserTO userTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUser : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            String accountId = userService.getAccountId(userTO);
            User user = userService.getActiveUser(session, accountId);
            isValidUserEdit(user.convertToTO(), userTO);
            userTO = approveEditUser(session, userTO, accountId);
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
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUser : end");
        return userTO;
    }

    private void isValidUserEdit(UserTO userTO, UserTO editedUserTO) throws AuthException {
        try {
            TwoFactorStatus.valueOf(editedUserTO.getTwoFactorStatus());
        }
        catch (IllegalArgumentException i) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), Constant.COMPULSORY_FIELDS + Constant.TWO_FACTOR_STATUS);
        }
        try {
            UserStatus.valueOf(editedUserTO.getUserStatus());
        }
        catch (IllegalArgumentException i) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), Constant.COMPULSORY_FIELDS + Constant.USER_STATUS);
        }

        boolean twoFactorStatus = userTO.getTwoFactorStatus().equals(editedUserTO.getTwoFactorStatus());
        boolean userStatus = userTO.getUserStatus().equals(editedUserTO.getUserStatus());
        boolean isCredentialsUpdate = editedUserTO.getUserCredential() != null;
        if (editedUserTO.getIamUserStatus() != null) {
            boolean iamUserStatus = getUserDetails(userTO.getAccountId()).getIamUserStatus().equals(editedUserTO.getIamUserStatus());
            if (twoFactorStatus && userStatus && iamUserStatus && !isCredentialsUpdate) {
                throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
            }
        }
        else {
            if (twoFactorStatus && userStatus && !isCredentialsUpdate) {
                throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
            }
        }
    }

    private void isValidUserEdit(UserTO userTO, UserTO editedUserTO, String role) throws AuthException {
        boolean twoFactorStatus = userTO.getTwoFactorStatus().equals(editedUserTO.getTwoFactorStatus());
        boolean userStatus = userTO.getUserStatus().equals(editedUserTO.getUserStatus());
        boolean isCredentialsUpdate = editedUserTO.getUserCredential() != null;
        boolean isCamEnabled = false;
        if (userTO.getCamEnabled() != null) {
            boolean userCamEnabled = userTO.getCamEnabled();
            if (editedUserTO.getCamEnabled() != null) {
                boolean editUserCamEnabled = editedUserTO.getCamEnabled();
                if (editUserCamEnabled != userCamEnabled) {
                    isCamEnabled = true;
                }
            }
        }
        else if (editedUserTO.getCamEnabled() != null) {
            isCamEnabled = true;
        }
        if (editedUserTO.getIamUserStatus() != null) {
            boolean iamUserStatus = getUserDetails(userTO.getAccountId(), role).getIamUserStatus().equals(editedUserTO.getIamUserStatus());
            if (twoFactorStatus && userStatus && iamUserStatus && !isCredentialsUpdate) {
                throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
            }
        }
        else {
            if (twoFactorStatus && userStatus && !isCredentialsUpdate && !isCamEnabled) {
                throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
            }
        }
    }
    private void isValidUserEditRole(UserTO userTO, UserTO editedUserTO, String role) throws AuthException {
        Session session = sessionFactoryUtil.getSession();
        try {
            if (userTO.getAccountType() != null) {
                Role roleDb;
                try {
                    roleDb = roleDao.getRoleByName(UserRole.valueOf(userTO.getAccountType().toUpperCase()), session);
                    if(roleDb!=null){
                       UserRole userRole= roleDb.getName();
                       if(editedUserTO.getAccountType().equalsIgnoreCase(String.valueOf(userRole))){
                           throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
                       }
                    }
                }
                catch (NotFoundException e) {
                    logger.log(Level.DEBUG,e.getMessage());
                }
            }
        }finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public UserTO approveEditUser(Session session, UserTO userTO, String accountId) throws AuthException {
        attributeNameToUpperCase(userTO);
        User user = userService.getActiveUser(session, accountId);
        userTO.setAccountId(accountId);
        user.setTwoFactorStatus(TwoFactorStatus.valueOf(userTO.getTwoFactorStatus()));
        user.setUserStatus(UserStatus.valueOf(userTO.getUserStatus()));

        user = userService.updateUser(session, user);
        updateUserStatusOnIAM(userTO);
        updateUserCredentials(userTO, user, false);
        userTO = user.convertToTO();
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUser : end");
        return userTO;
    }

    @Override
    public UserTO approveEditUser(Session session, UserTO userTO, String role, String actor) throws AuthException{
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUser : start");
        permissionUtil.validateEditUserPermission(role);
        User user = userService.getActiveUser(session, userTO.getUserId());
        user.setTwoFactorStatus(TwoFactorStatus.valueOf(userTO.getTwoFactorStatus()));
        user.setUserStatus(UserStatus.valueOf(userTO.getUserStatus()));
        List<AttributeStore> attributeStores = user.getAttributeStores();
        updateUserStatusOnIAM(userTO);
        AccountWE accountWE = updateUserCredentials(userTO, user, true);
        updateAttributeOnIdsAndCam(userTO, attributeStores);

        if (userTO.getCamEnabled() != null) {
            //changes
            //        boolean isKeycloakEnabled = config.getProperty(Constant.IS_KEYCLOAK_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_KEYCLOAK_ENABLED));
            boolean isCamEnabled = userTO.getCamEnabled();
            if (isCamEnabled && user.getKcId() == null) {
                UserCreationRequest userCreationRequest = new UserCreationRequest();
                String userName = attributeStores.get(0).getAttributeValue();
                userCreationRequest.setUsername(userName);

                List<CamAttribute> camAttributes = new ArrayList<>();
                for (AttributeStore attributeStore : attributeStores) {
                    CamAttribute camAttribute = new CamAttribute();
                    camAttribute.setCustomAttributeName(attributeStore.getAttributeName());
                    camAttribute.setCustomAttributeValue(attributeStore.getAttributeValue());
                    camAttributes.add(camAttribute);
                }
                userCreationRequest.setAttributes(camAttributes);

                //credentials ->  to support multiple credentials
                if (userTO.getUserCredential() != null) {
                    List<Credential> credentials = new ArrayList<>();
                    Credential credential = new Credential();
                    credential.setTemporary(false);
                    credential.setType("password");
                    String userCredential = accountWE.getUserCredential() != null ? accountWE.getUserCredential() : userTO.getUserCredential();
                    credential.setValue(userCredential);
                    credentials.add(credential);
                    userCreationRequest.setCredentials(credentials);
                }

                UserResponseDto camUser = camUserFacade.onboardCamUser(Config.getInstance().getProperty(Constant.CAM_REALM), userCreationRequest);
                if (camUser.getUserKcId() != null) {
                    user.setKcId(camUser.getUserKcId());
                    user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_COMPLETE.name());
                }
                else {
                    user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_FAILED.name());
                }
            }
            else if (isCamEnabled && user.getKcId() != null && userTO.getUserCredential() != null) {
                String userCredential = accountWE.getUserCredential() != null ? accountWE.getUserCredential() : userTO.getUserCredential();
                ResetPasswordUserRequest request = new ResetPasswordUserRequest("password", userCredential, false);
                camUserFacade.resetPassword(Config.getInstance().getProperty(Constant.CAM_REALM), user.getKcId(), request);
            }
            else {
                if (user.getKcId() != null) {
                    boolean camUserDeleted = camUserFacade.deleteUser(Config.getInstance().getProperty(Constant.CAM_REALM), user.getKcId());
                    if (camUserDeleted) {
                        user.setKcId(null);
                    }
                }
            }
        }

        user = userService.updateUser(session, user);
        userTO = user.convertToTO();
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " editUser : end");
        return userTO;
    }

    @Override
    public UserTO updateUserRole(Session session, UserTO userTO, String role, String actor) throws AuthException, NotFoundException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " updateUserRole : start");
        User user = userService.getActiveUser(session, userTO.getUserId());
        if(userTO.getAuthType()!=null){
            user.setAuthType(userTO.getAuthType());
        }
        if(!userTO.getAccountType().equals(String.valueOf(AccountType.USER))){
            user.setUserState(UserState.A);
        }else user.setUserState(UserState.D);
        if(userTO.getAccountType()!=null) {
            Role roleDb;
            roleDb = roleDao.getRoleByName(UserRole.valueOf(userTO.getAccountType().toUpperCase()), session);
            Set<Role> roles = new HashSet<>();
            roles.add(roleDb);
            user.setRoles(roles);
        }
        updateUserRoleOnIds(userTO);
        user = userService.updateUser(session, user);
        userTO = user.convertToTO();
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " updateUserRole : end");
        return userTO;
    }

    @Override
    public UserTO approveEditUserTimestamp(Session session, UserTO userTO, String role, String actor) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveEditUserTimestamp : start");
        User user = userService.getActiveUser(session, userTO.getUserId());
        user.setUserStatus(UserStatus.ACTIVE);
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(currentTimeMillis);
        user.setLastLoginTime(timestamp);
        user = userService.updateUser(session, user);
        userTO = user.convertToTO();
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveEditUserTimestamp : end");
        return userTO;
    }

    @Override
    public UserTO approveEditUserAttributes(Session session, UserTO userTO, String role, String actor) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveEditUserAttributes : start");
        permissionUtil.validateEditUserPermission(role);
        User user = userService.getActiveUser(session, userTO.getUserId());

        List<AttributeStore> attributeStores = user.getAttributeStores();
        updateAttributeOnIdsAndCam(userTO, attributeStores);

        user = userService.updateUser(session, user);
        userTO = user.convertToTO();
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveEditUserAttributes : end");
        return userTO;
    }

    private void updateAttributeOnIdsAndCam(UserTO userTO, List<AttributeStore> attributeStores) {
        if (userTO.getAttributes() != null && !userTO.getAttributes().isEmpty()) {

            userTO.getAttributes().forEach(attr -> {
                attributeNameToUpperCase(attr);
                AttributeStore attributeStore = attributeStores.stream()
                        .filter(p -> p.getAttributeName().equals(attr.getAttributeName()) && p.getAttributeValue().equalsIgnoreCase(attr.getAttributeValue()) && p.getAttributeState().equals(AttributeState.ACTIVE)
                                     && p.getIsDefault() != null
                                     && p.getIsDefault().equals(attr.getIsDefault()))
                        .findFirst().orElse(null);

                if (attributeStore == null && attr.getIsDefault().equals(Boolean.TRUE)) {
                    AttributeDataRequestTO attributeDataRequestTO;
                    try {
                        attributeDataRequestTO = fetchAndStoreUniqueAttributes(userTO);
                    }
                    catch (AuthException e) {
                        throw new RuntimeException(e);
                    }
                    //                    //set search attributes
                    //                    List<AttributeDataTO> searchAttrs = new ArrayList<>();
                    //                    AttributeDataTO searchAttribute = new AttributeDataTO();
                    //                    searchAttribute.setAttributeName(attr.getAttributeName());
                    //                    searchAttribute.setAttributeValue(attr.getAttributeValue());
                    //
                    //                    //set attribute
                    //                    AttributeDataTO attributeData = new AttributeDataTO();
                    //                    attributeData.setAttributeAction(AttributeAction.UPDATE);
                    //                    attributeData.setAttributeValue(attr.getAttributeValue());
                    //                    attributeData.setAttributeName(attr.getAttributeName());
                    //                    attributeData.setIsDefault(attr.getIsDefault());
                    //
                    //                    searchAttrs.add(searchAttribute);
                    //                    attributeDataRequestTO.setSearchAttributes(searchAttrs);
                    //                    attributeDataRequestTO.setAttributeData(attributeData);

                    try {
                        attributeStoreFacadeIntf.sendAttributeUpdateOrDeleteRequest(attributeDataRequestTO);
                    }
                    catch (AuthException | IAMException e) {
                        throw new RuntimeException(e);
                    }

                }

            });
        }
    }

    private AttributeDataRequestTO fetchAndStoreUniqueAttributes(UserTO userTO) throws AuthException {
        AttributeDataRequestTO attributeDataRequestTO = new AttributeDataRequestTO();
        List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        try {
            for (AttributeDataTO attr : userTO.getAttributes()) {
                attributeMetadataTO.setAttributeName(attr.getAttributeName());
            }
            int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            if (index < 0) {
                attributeMetadataTO.setAttributeName("OTHERS");
                index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            }
            attributeMetadataTO = attributeMetaDataTOs.get(index);
        } catch (ArrayIndexOutOfBoundsException e){
            logger.log(Level.ERROR, "<<<<< userTO : " + new Gson().toJson(userTO));
            logger.log(Level.ERROR,attributeMetaDataTOs.toString());
            logger.log(Level.ERROR,attributeMetadataTO.toString());
            throw e;
        }
        List<AttributeDataTO> searchAttrs = new ArrayList<>();
        if (attributeMetadataTO.getIsUnique()) {
            for (AttributeDataTO attr : userTO.getAttributes()) {
                AttributeDataTO searchAttribute = new AttributeDataTO();
                searchAttribute.setAttributeName(attr.getAttributeName());
                searchAttribute.setAttributeValue(attr.getAttributeValue());
                searchAttrs.add(searchAttribute);
                attributeDataRequestTO.setSearchAttributes(searchAttrs);
                // Setting attribute data
                AttributeDataTO attributeData = new AttributeDataTO();
                attributeData.setAttributeAction(AttributeAction.UPDATE);
                attributeData.setAttributeValue(attr.getAttributeValue());
                attributeData.setAttributeName(attr.getAttributeName());
                attributeData.setIsDefault(attr.getIsDefault());
                attributeDataRequestTO.setAttributeData(attributeData);
            }
        }
        else {
            UserTO userTO1 = new UserTO();
            userTO1.setAttributes(new EntityToTOConverter<AttributeStore, AttributeDataTO>().convertEntityListToTOList(AttributeStoreDaoImpl.getInstance().getUserAttributes(userTO.getUserId())));
            List<AttributeMetadataTO> attributeMetaDataTOs1 = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
            AttributeMetadataTO attributeMetadataTO1 = new AttributeMetadataTO();
            for (AttributeDataTO attr : userTO1.getAttributes()) {
                attributeMetadataTO1.setAttributeName(attr.getAttributeName());
                int index1 = attributeMetaDataTOs1.indexOf(attributeMetadataTO1);
                if (index1 < 0) {
                    attributeMetadataTO1.setAttributeName("OTHERS");
                    index1 = attributeMetaDataTOs1.indexOf(attributeMetadataTO1);
                }
                attributeMetadataTO1 = attributeMetaDataTOs1.get(index1);
                if (attributeMetadataTO1.getIsUnique() && !attr.getAttributeState().equals(AttributeState.DELETE.name())) {
                    //                    for (AttributeDataTO attr : userTO.getAttributes()) {
                    AttributeDataTO searchAttribute = new AttributeDataTO();
                    searchAttribute.setAttributeName(attr.getAttributeName());
                    searchAttribute.setAttributeValue(attr.getAttributeValue());
                    searchAttrs.add(searchAttribute);
                    attributeDataRequestTO.setSearchAttributes(searchAttrs);
                    //                    }
                    break;
                }
            }
            for (AttributeDataTO attr : userTO.getAttributes()) {
                AttributeDataTO attributeData = new AttributeDataTO();
                attributeData.setAttributeAction(AttributeAction.UPDATE);
                attributeData.setAttributeValue(attr.getAttributeValue());
                attributeData.setAttributeName(attr.getAttributeName());
                attributeData.setIsDefault(attr.getIsDefault());
                attributeDataRequestTO.setAttributeData(attributeData);
            }
        }
        return attributeDataRequestTO;
    }

    private void updateUserStatusOnIAM(UserTO userTO) throws AuthException {
        if (userTO.getIamUserStatus() != null || userTO.getIamAccountAccessStatus() != null) {
                iamExtensionService.updateUserStatus(userTO.getAccountId(), userTO.getIamUserStatus(), userTO.getIamAccountAccessStatus() );
        }
    }
    private void updateUserRoleOnIds(UserTO userTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " updateUserRoleOnIds : start");
        if (userTO.getIamUserStatus() != null || userTO.getIamAccountAccessStatus() != null) {
            String accountType =null;
            if(userTO.getAccountType()!=null) {
                accountType=AccountType.USER.toString();
            }
            iamExtensionService.updateUserRoleOnIds(userTO.getAccountId(), userTO.getIamUserStatus(),accountType );
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " updateUserRoleOnIds : end");
    }

    private AccountWE updateUserCredentials(UserTO userTO, User user, boolean isEncrypted) throws AuthException {
        if (userTO.getUserCredential() != null) {
            String accountId = user.getAccountId();
            AccountWE accountWE = iamExtensionService.getAllAttributesForAccount(accountId);
            if (Constant.PARTIALLY_ACTIVE.equals(accountWE.getState())) {
                AccountWE weEditAccount = new AccountWE();
                String decrypteAccountPassword = userTO.getUserCredential();
                if (isEncrypted) {
                    try {
                        decrypteAccountPassword = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), userTO.getUserCredential());
                    }
                    catch (Exception e) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_USER_PASSWORD());
                    }
                }
                String hashedPassword = StringUtil.getHex(
                        SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountId, decrypteAccountPassword).getBytes()));
                weEditAccount.setUserCredential(hashedPassword);
                AccountWE account = iamExtensionService.editUserCredentials(weEditAccount, accountId);
                account.setUserCredential(hashedPassword);
                return account;
            }
            else {
                throw new AuthException(null, errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), errorConstant.getERROR_MESSAGE_EDIT_CREDENTIAL_FALIED());
            }

        }
        return null;
    }

    private void enableToken(Application application, User user, List<UserApplicationServiceRel> userApplicationStagingRels) {
        boolean isAuth42ServicePresent = false;
        for (UserApplicationServiceRel userApplicationServiceRel : userApplicationStagingRels) {
            if (Constant.AUTH42.equals(userApplicationServiceRel.getId().getService().getServiceName())) {
                isAuth42ServicePresent = true;
            }
        }
        if (isAuth42ServicePresent) {
            userService.enableToken(user.getAccountId(), application, true);
        }
    }

    @Override
    public CryptoTokenTO verifyCryptoToken(String applicationId, CryptoTokenTO cryptoTokenTO, boolean isEnterpriseToken) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " verifyCryptoToken : start");
        checkUserPresent(cryptoTokenTO);
        for (in.fortytwo42.adapter.transferobj.AttributeTO attributeTO : cryptoTokenTO.getSearchAttributes()) {
            attributeTO.setAttributeName(attributeTO.getAttributeName().toUpperCase());
            AttributeValidationUtil.validateSearchAttributeValueAndUniqueness(attributeTO.getAttributeName(), attributeTO.getAttributeValue());
        }
        boolean isTokenVerified = false;
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            List<in.fortytwo42.adapter.transferobj.AttributeTO> searchAttributes = null;
            if (isEnterpriseToken) {
                searchAttributes = new ArrayList<>();
                in.fortytwo42.adapter.transferobj.AttributeTO attributeTO = new in.fortytwo42.adapter.transferobj.AttributeTO();
                attributeTO.setAttributeName(Constant.USER_ID);
                attributeTO.setAttributeValue(applicationId);
                searchAttributes.add(attributeTO);
            }
            else {
                searchAttributes = cryptoTokenTO.getSearchAttributes();
            }
            AccountWE accountWE = getAccount(searchAttributes, iamExtension, token);
            for (in.fortytwo42.adapter.transferobj.AttributeTO attributeTO : cryptoTokenTO.getSearchAttributes()) {
                String cryptoDID = accountWE.getCryptoDID();
                if (cryptoDID == null) {
                    cryptoDID = iamExtension.getDID(accountWE.getId(), reqRefNum);
                }
                isTokenVerified = iamExtension.verifyNumTokenV2(cryptoTokenTO.getCryptoToken(), cryptoDID, attributeTO.getAttributeValue(), token, reqRefNum);
            }
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            isTokenVerified = false;
        }
        cryptoTokenTO.setStatus(isTokenVerified ? Constant.SUCCESS_STATUS : Constant.FAILURE_STATUS);
        AuditLogUtil.sendAuditLog(cryptoTokenTO.getCryptoToken() + "crypto token verified successfully ", "USER", ActionType.ONBOARD, "", IdType.ACCOUNT, "", null, "", null);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " verifyCryptoToken : end");
        return cryptoTokenTO;
    }

    private void checkUserPresent(CryptoTokenTO cryptoTokenTO) throws AuthException {
        User user = null;
        for (in.fortytwo42.adapter.transferobj.AttributeTO attributeTO : cryptoTokenTO.getSearchAttributes()) {
            attributeTO.setAttributeName(attributeTO.getAttributeName().toUpperCase());
            AttributeStore attribute = ServiceFactory.getAttributeStoreService().getAttributeByNameValue(attributeTO.getAttributeName(), attributeTO.getAttributeValue());
            user = attribute.getUser();
            attributeTO.setAttributeValue(applySecurityPolicy(attributeTO));
        }
        if (user == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        else if (user.getUserStatus() == UserStatus.BLOCK) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_BLOCK(), errorConstant.getERROR_MESSAGE_USER_BLOCK());
        }
    }

    private AccountWE getAccount(List<in.fortytwo42.adapter.transferobj.AttributeTO> searchAttrinutes, IAMExtensionV2 iamExtension, Token token) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAccount : start");
        List<AttributeDataTO> attributes = new ArrayList<>();
        for (in.fortytwo42.adapter.transferobj.AttributeTO attributeTO : searchAttrinutes) {
            AttributeDataTO attributeDataTO = new AttributeDataTO();
            attributeDataTO.setAttributeName(attributeTO.getAttributeName());
            attributeDataTO.setAttributeValue(applySecurityPolicy(attributeTO));
            attributeNameToUpperCase(attributeDataTO);
            attributes.add(attributeDataTO);
        }
        AccountWE accountWE = iamExtensionService.searchAccount(attributes, iamExtension, token);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAccount : end");
        return accountWE;
    }

    private String applySecurityPolicy(in.fortytwo42.adapter.transferobj.AttributeTO attributeTO) throws AuthException {
        List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeTO.getAttributeName().toUpperCase());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName("OTHERS");
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String securityType = attributeMetadataTO.getAttributeStoreSecurityPolicy();
        AttributeSecurityType attributeSecurityType = AttributeSecurityType.valueOf(securityType);
        String attributeValue = attributeTO.getAttributeValue();
        String hashedAttributeValue;
        if (attributeSecurityType == AttributeSecurityType.SHA512) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData512(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        }
        else if (attributeSecurityType == AttributeSecurityType.SHA256) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData256(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        }
        else {
            hashedAttributeValue = attributeValue;
        }
        return hashedAttributeValue.toUpperCase();
    }

    @Override
    public CryptoTokenTO generateToken(String applicationId, CryptoTokenTO cryptoTokenTO) throws AuthException {
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            checkUserPresent(cryptoTokenTO);
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            //            AccountWE accountWE = getAccount(cryptoTokenTO.getSearchAttributes(), iamExtension, token);
            String token1 = null;
            for (in.fortytwo42.adapter.transferobj.AttributeTO attributeTO : cryptoTokenTO.getSearchAttributes()) {
                token1 = iamExtension.generateNumTokenV2(attributeTO.getAttributeValue(), token, reqRefNum);
                cryptoTokenTO.setCryptoToken(token1);
            }
            return cryptoTokenTO;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public String downloadUpdateUserStatus(String fileName, String role) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " downloadUpdateUserStatus : start");
        String content = new FileDownloader().downloadCSVStatusFile(fileName, role);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " downloadUpdateUserStatus : end");
        return content;
    }

    @Override
    public List<UserIciciTO> onboardUsers(List<UserIciciTO> userTOs) throws AuthException {
        for (UserIciciTO userTO : userTOs) {
            userTO = onboardUserV4(userTO);
        }
        return userTOs;
    }

    @Override
    public AccountWE getTokensByAccountId(String accountId) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "getTokensbyDeviceID: start");
        AccountWE accountWE = iamExtensionService.getTokensByAccountId(accountId);
        if (accountWE.getToken() == null || accountWE.getToken().isEmpty()) {
            List<TokenWE> tokens = new ArrayList<TokenWE>();
            accountWE.setId(accountId);
            accountWE.setToken(tokens);
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "getTokensbyaccountID : end");
            return accountWE;
        }
        else {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getTokensbyDeviceID : end");
            return accountWE;
        }
    }

    @Override
    public AccountWE getDevicesByAccountId(String accountId) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "getdevicesbyaccountId: start");
        AccountWE accountWE = iamExtensionService.getDevicesByAccountId(accountId);
        if (accountWE.getDevises() == null || accountWE.getDevises().isEmpty()) {
            List<DeviceTO> devices = new ArrayList<DeviceTO>();
            accountWE.setId(accountId);
            accountWE.setDevises(devices);
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "getdevicesbyaccountId : end");
            return accountWE;
        }
        else {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "getdevicesbyaccountId : end");
            return accountWE;
        }
    }

    public UserIciciTO onboardUserOnIdsIamCryptoAdapter(Session session, UserIciciTO userTO, String role, String parentAccountId) throws AuthException {
        // if camEnabled is true -> user credential is mandatory
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "onboardUserOnIdsIamCryptoAdapter: start");
        boolean isCamEnabledTrue = userTO.getCamEnabled() != null && userTO.getCamEnabled();
        if (isCamEnabledTrue && userTO.getUserCredential() == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_CREDENTIALS_NOT_PRESENT(), errorConstant.getERROR_MESSAGE_USER_CREDENTIALS_NOT_PRESENT());
        }
        User user = new User();
        user.setAuthType(userTO.getAuthType());
        if (Constant.ADFS.equals(userTO.getAuthType())) {
            IamExtensionServiceIntf iamExtension = ServiceFactory.getIamExtensionService();
            IAMExtensionV2 iamExtensionV2 = iamExtension.getIAMExtension();
            AccountWE account = iamExtension.createAccountIfNotExist(userTO.getSearchAttributes().get(0).getAttributeName(), userTO.getSearchAttributes().get(0).getAttributeValue(), iamExtensionV2);
	        boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
	        if (enableCrypto) {
		        onboardOnCrypto(iamExtensionV2, account, userTO, enableCrypto);
	        }
	        if(account!=null && account.getId()!=null){
		        try {
			        iamExtensionV2.createConsumerIfNotExistEnterpriseToken(account.getId(), iamExtension.getToken(iamExtensionV2));
		        }catch (IAMException e){
			        throw iamExceptionConvertorUtil.convertToAuthException(e);
		        }
	        }
	        String accountType = userTO.getAccountType() != null ? userTO.getAccountType() : AccountType.USER.toString();
	        user = userService.createUser(session, account.getId(), UserRole.valueOf(accountType), TwoFactorStatus.ENABLED.name(), user);

            List<AttributeStore> attributeStores = new ArrayList<>();
            List<AttributeStore> tempAttributeStores = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                logger.log(Level.DEBUG, "searchAttribute : " + new Gson().toJson(attributeDataTO));
                AttributeStore attributeStore = attributeStoreService.saveAttributeData(session, attributeDataTO, user, false);
                attributeStores.add(attributeStore);
                tempAttributeStores.add(attributeStore);
            }
            if (userTO.getAttributeData() != null) {
                for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
                    if (AttributeOperationStatus.SUCCESSFUL.toString().equals(attributeDataTO.getStatus())) {
                        AttributeStore attributeStore = attributeStoreService.saveAttributeData(session, attributeDataTO, user, false);
                        attributeStores.add(attributeStore);
                        tempAttributeStores.add(attributeStore);

                    }
                }
            }

            user.setAttributeStores(attributeStores);
            user.setCredentialsThroughEmail(userTO.getCredentialsThroughEmail());

            user = userService.updateUser(session, user);

            if (parentAccountId != null) {
                userTO.setStatus(Constant.SUCCESS_STATUS);
                userTO.setId(user.getId());
            }
            AuditLogUtil.sendAuditLog(AuditLogConstant.USER_CREATION_SUCCESSFUL + AuditLogConstant.FOR_USER + userTO.getSearchAttributes().get(0).getAttributeValue(), "ENTERPRISE", ActionType.ONBOARD,
                    parentAccountId, IdType.ACCOUNT, "", "","", null);
        }
        else {
            boolean isCreateUser = false;
            AccountWE account = onboardUserOnIdsAndIam(userTO, parentAccountId);
            if (account == null) {
                return userTO;
            }
            try {
                user = userService.getActiveUser(session, account.getId());
            }
            catch (AuthException e) {
                isCreateUser = true;
                //  user = userService.createUser(session, account.getId(), UserRole.valueOf(AccountType.USER.toString()), TwoFactorStatus.ENABLED.name());
                String accountType = userTO.getAccountType() != null ? userTO.getAccountType() : AccountType.USER.toString();
                user = userService.createUser(session, account.getId(), UserRole.valueOf(accountType), TwoFactorStatus.ENABLED.name(), user);
            }
            logger.log(Level.DEBUG, "isCreateUser : " + isCreateUser);

            List<AttributeStore> attributeStores = new ArrayList<>();
            List<AttributeStore> tempAttributeStores = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                if (isCreateUser) {
                    logger.log(Level.DEBUG, "searchAttribute : " + new Gson().toJson(attributeDataTO));
                    AttributeStore attributeStore = attributeStoreService.saveAttributeData(session, attributeDataTO, user, false);
                    attributeStores.add(attributeStore);
                    tempAttributeStores.add(attributeStore);
                }
                else {
                    attributeStores.addAll(user.getAttributeStores());
                }
            }
            if (userTO.getAttributeData() != null) {
                for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
                    if (AttributeOperationStatus.SUCCESSFUL.toString().equals(attributeDataTO.getStatus())) {
                        AttributeStore attributeStore = attributeStoreService.saveAttributeData(session, attributeDataTO, user, false);
                        attributeStores.add(attributeStore);
                        tempAttributeStores.add(attributeStore);

                    }
                }
            }

            user.setAttributeStores(attributeStores);
            user.setCredentialsThroughEmail(userTO.getCredentialsThroughEmail());
            boolean isCamEnabled = userTO.getCamEnabled() != null && userTO.getCamEnabled();
            if (isCamEnabled && user.getKcId() == null) {
                UserCreationRequest userCreationRequest = new UserCreationRequest();
                String userName = attributeStores.get(0).getAttributeValue();
                //userCreationRequest.setUsername(userName);
                userCreationRequest.setUsername(account.getId());
                List<CamAttribute> camAttributes = new ArrayList<>();
                for (AttributeStore attributeStore : attributeStores) {
                    CamAttribute camAttribute = new CamAttribute();
                    camAttribute.setCustomAttributeName(attributeStore.getAttributeName());
                    camAttribute.setCustomAttributeValue(attributeStore.getAttributeValue());
                    camAttributes.add(camAttribute);
                }
                userCreationRequest.setAttributes(camAttributes);

                //credentials ->  to support multiple credentials
                List<Credential> credentials = new ArrayList<>();
                Credential credential = new Credential();
                credential.setTemporary(false);
                credential.setType("password");
                String userCredential = account.getUserCredential() != null ? account.getUserCredential() : userTO.getUserCredential();
                credential.setValue(userCredential);
                credentials.add(credential);
                userCreationRequest.setCredentials(credentials);

                UserResponseDto camUser = camUserFacade.onboardCamUser(Config.getInstance().getProperty(Constant.CAM_REALM), userCreationRequest);

                if (camUser.getUserKcId() != null) {
                    user.setKcId(camUser.getUserKcId());
                    user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_COMPLETE.name());
                }
                else {
                    user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_FAILED.name());
                }
            }

            user = userService.updateUser(session, user);

            if (parentAccountId != null) {
                userTO.setStatus(Constant.SUCCESS_STATUS);
                userTO.setId(user.getId());
            }

            if (user.getKcId() != null && !tempAttributeStores.isEmpty() && !isCreateUser) {
                EditUserRequest editUserRequest = new EditUserRequest();
                editUserRequest.setUserKcId(user.getKcId());
                List<CamAttribute> camAttributes = new ArrayList<>();
                for (AttributeStore attributeStore : tempAttributeStores) {
                    CamAttribute camAttribute = new CamAttribute();
                    camAttribute.setCustomAttributeName(attributeStore.getAttributeName());
                    camAttribute.setCustomAttributeValue(attributeStore.getAttributeValue());
                    camAttributes.add(camAttribute);
                }
                editUserRequest.setAttributes(camAttributes);
                camUserFacade.editCamUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
            }

            if (user.getKcId() != null && userTO.getUserCredential() != null) {
                String userCredential = account.getUserCredential() != null ? account.getUserCredential() : userTO.getUserCredential();
                ResetPasswordUserRequest request = new ResetPasswordUserRequest("password", userCredential, false);
                camUserFacade.resetPassword(Config.getInstance().getProperty(Constant.CAM_REALM), user.getKcId(), request);
            }

            if (userTO.getSubscribedApplications() != null) {
                userService.autoBindUserToApplication(session, userTO.getSubscribedApplications(), user, account.getUserCredential());
            }
            AuditLogUtil.sendAuditLog(AuditLogConstant.USER_CREATION_SUCCESSFUL + AuditLogConstant.FOR_USER + userTO.getSearchAttributes().get(0).getAttributeValue(), "ENTERPRISE", ActionType.ONBOARD,
                    parentAccountId, IdType.ACCOUNT, "", "", account.getId(), null);
        }

        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + "onboardUserOnIdsIamCryptoAdapter: end");
        return userTO;
    }

    private void onboardOnCrypto(IAMExtensionV2 iamExtensionV2, AccountWE account, UserIciciTO userTO, Boolean enableCrypto) {
        try {
            Map<String, String> attributeValueWithPlainValue = new HashMap<>();
            Token token = iamExtensionService.getToken(iamExtensionV2);
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            iamExtensionV2.onboardEntityOnCrypto(account, CryptoEntityType.ENTITY_USER, token, reqRefNum);

            Map<String, Object> attributeValueWithKey = null;
            if (enableCrypto) {
                for(AttributeDataTO attributeDataTO:userTO.getSearchAttributes()){
                    attributeValueWithPlainValue.put(attributeDataTO.getAttributeName().toUpperCase(),attributeDataTO.getAttributeValue());
                }
                for(AttributeTO attributeTO:account.getAttributes()){
                    if(!AttributeOperationStatus.SUCCESSFUL.equals(attributeTO.getOperationStatus())){
                        attributeTO.setOperationStatus(AttributeOperationStatus.SUCCESSFUL);
                    }
                }
                attributeValueWithKey = iamExtensionV2.registerAttributesOnCryptov3(account, attributeValueWithPlainValue, token, reqRefNum);
            }
            else {
                attributeValueWithKey = new HashMap<>();
            }
            System.out.println(" ** after registerAndActivateAccount " + account.getCryptoDID());
            for (AttributeTO attributeTO : account.getAttributes()) {
                String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
                System.out.println("*****" + attributeTO.getOperationStatus() + " ******");
                if (AttributeOperationStatus.SUCCESSFUL == attributeTO.getOperationStatus()) {
                    if (enableCrypto) {
                        if (attributeValueWithKey.containsKey(plainValue)) {
                            System.out.println("*** before setEncryptedAttributeValue" + attributeTO
                                    .getAttributeName() + " value " + plainValue + " key " + attributeValueWithKey.get(plainValue));
                            GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                                    (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(plainValue);
                            attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                            attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                            attributeTO.setOperationStatus(null);
                        }
                    }
                    else {
                        attributeTO.setOperationStatus(null);
                    }
                }
                else {
                    if (!enableCrypto) {
                        attributeValueWithKey.put(plainValue, attributeTO.getErrorMessage());
                    }
                }
            }
            if (userTO.getAttributeData() != null) {
                for (AttributeDataTO attributeDataTOTemp : userTO.getAttributeData()) {
                    attributeNameToUpperCase(attributeDataTOTemp);
                    System.out.println("*****" + attributeDataTOTemp.getAttributeValue() + " ******");
                    if (enableCrypto) {
                        if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                            GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey
                                    .get(attributeDataTOTemp.getAttributeValue());
                            attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                            attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                        }
                        else {
                            System.out.println("*****" + attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) + " ******");
                            attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                            attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                        }
                    }
                    else {
                        if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                            attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                            attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                        }
                        else {
                            attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                        }
                    }
                }
            }
            for (AttributeDataTO attributeDataTOTemp : userTO.getSearchAttributes()) {
                attributeNameToUpperCase(attributeDataTOTemp);
                if (enableCrypto) {
                    if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                        if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                            GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey
                                    .get(attributeDataTOTemp.getAttributeValue());
                            attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                            attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                        }
                        else {
                            attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                            attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                        }
                    }
                }
                else {
                    if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                        attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                    }
                    else {
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                    }
                }
            }

            iamExtensionV2.editAccount(account, account.getId(),token);
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public CSVUploadTO uploadEditUsersStatus(String fileType, InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("YYYYMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("IST"));
        String dateFormatted = formatter.format(date);
        String requestId = UUID.randomUUID().toString();
        String filename = fileName.split(".csv")[0] + "_" + dateFormatted + "_" + requestId + ".csv";
        CSVUploadTO csvUploadTO = new CSVUploadTO();
        csvUploadTO.setRequestId(requestId);
        csvUploadTO.setFileName(filename);
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            CsvFactory.processCsv(fileType, inputStream, role, id, filename);
        });
        return csvUploadTO;
    }

    @Override
    public CSVUploadTO uploadUserApplicationMapping(String fileType, InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("YYYYMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("IST"));
        String dateFormatted = formatter.format(date);
        String requestId = UUID.randomUUID().toString();
        String filename = fileName.split(".csv")[0] + "_" + dateFormatted + "_" + requestId + ".csv";
        CSVUploadTO csvUploadTO = new CSVUploadTO();
        csvUploadTO.setRequestId(requestId);
        csvUploadTO.setFileName(filename);
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            CsvFactory.processCsv(fileType, inputStream, role, id, filename);
        });
        return csvUploadTO;
    }

    @Override
    public in.fortytwo42.enterprise.extension.tos.ApplicationTO validateApplication(String applicationId) throws AuthException {
        in.fortytwo42.enterprise.extension.tos.ApplicationTO applicationTO = iamExtensionService.validateApplication(applicationId);
        return applicationTO;
    }

    @Override
    public UserIciciTO changeUserPassword(UserIciciTO userTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " changeUserPassword: start");
        Session session = IamThreadContext.getSessionWithoutTransaction();
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        Token token;
        try {
            validateSearchAttributes(userTO.getSearchAttributes());
            token = iamExtensionService.getToken(iamExtension);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserFacadeImpl -> changeUserPassword iamExtensionService.getToken |Epoch:"+System.currentTimeMillis());
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            List<AttributeTO> attributeTOs = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                attributeTOs.add(getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false));
            }
            long startTimeProcessGA = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->UserFacadeImpl -> changeUserPassword iamExtension.getAccountByAttributes |Epoch:"+startTimeProcessGA);
            AccountWE accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
            long endTimeProcessGA = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserFacadeImpl -> changeUserPassword iamExtension.getAccountByAttributes |Epoch:"+endTimeProcessGA);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF "+(endTimeProcessGA-startTimeProcessGA));

            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty()) {
                userTO.setStatus(Constant.FAILED);
                userTO.setErrorMessage("User not found in the system");
                userTO.setErrorCode(errorConstant.getUSER_NOT_FOUND());
                return userTO;
            }
            String hashedPassword = StringUtil.getHex(
                    SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), userTO.getUserCredential()).getBytes()));
            accountWE.setApplicationId(userTO.getApplicationId());
            accountWE.setUserCredential(hashedPassword);
            User user = userService.getActiveUser(session, accountWE.getId());
            if (user == null) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            accountWE.setAttributes(null);
            long startTimeProcessEA = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->UserFacadeImpl -> changeUserPassword iamExtensionService.editUserCredentials |Epoch:"+startTimeProcessEA);
            AccountWE account = iamExtensionService.editUserCredentials(accountWE, user.getAccountId());
            long endTimeProcessEA = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserFacadeImpl -> changeUserPassword iamExtensionService.editUserCredentials |Epoch:"+endTimeProcessEA);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF "+(endTimeProcessEA-startTimeProcessEA));

            if (user.getKcId() != null && !user.getKcId().isEmpty() && (accountWE.getKcId() == null || accountWE.getKcId().isEmpty())) {
                String password = StringUtil.getHex(SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), (accountWE.getId() + user.getKcId())).getBytes()));
                ResetPasswordUserRequest request = new ResetPasswordUserRequest("password", password, false);
                camUserFacade.resetPassword(Config.getInstance().getProperty(Constant.CAM_REALM), user.getKcId(), request);
                AccountWE camAccountForKcId = new AccountWE();
                camAccountForKcId.setId(accountWE.getId());
                camAccountForKcId.setKcId(user.getKcId());
                iamExtension.editAccount(camAccountForKcId, accountWE.getId(), token);
            }
            userTO.setStatus("SUCCESS");
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " changeUserPassword: end");
            AuditLogUtil.sendAuditLog("User " + userTO.getSearchAttributes().get(0).getAttributeValue() + " password successfully changed", "USER", ActionType.CHANGED_PASSWORD, "", IdType.ACCOUNT, "",
                    null, accountWE.getId(), null);
            return userTO;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            userTO.setStatus(Constant.FAILED);
            userTO.setErrorMessage("User not found in the system");
            userTO.setErrorCode(errorConstant.getUSER_NOT_FOUND());
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " changeUserPassword: end");
            return userTO;
        }
    }

    @Override
    public UserIciciTO addAttributes(UserIciciTO userTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " addAttributes: start");
        Session session = IamThreadContext.getSessionWithoutTransaction();
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        try {
            validateSearchAttributes(userTO.getSearchAttributes());

            boolean isProcessCalled = false;
            AccountWE accountWE = new AccountWE();
            User user = new User();
            List<AddAttributeType> addAttributeProcess = createAttributeAddProcess();

            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);

            for (AddAttributeType type : addAttributeProcess) {
                Onboarder onboarder = AddAttributeFactory.buildOnboarder(type);
                if (!onboarder.validate(token, iamExtension, userTO, accountWE, user)) {
                    isProcessCalled = true;
                    long startTimeProcess = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->UserFacadeImpl -> "+type+" -> process |Epoch:"+startTimeProcess);
                    onboarder.process(token, iamExtension, userTO, accountWE, user, session);
                    long endTimeProcess = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserFacadeImpl -> "+type+" -> process |Epoch:"+endTimeProcess);
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "-> UserFacadeImpl -> "+type+" -> process -> DIFF "+(endTimeProcess-startTimeProcess));
                }
            }
            if (!isProcessCalled) {
                userTO.setStatus("FAILED");
                userTO.setErrorCode(errorConstant.getALREADY_PRESENT_IN_SYSTEM_CODE());
                userTO.setErrorMessage("Attribute already present in the system");

            }else {
                userTO.setStatus("SUCCESS");
                AuditLogUtil.sendAuditLog("Attributes successfully added to user " + userTO.getSearchAttributes().get(0).getAttributeValue() + " externally", "USER", ActionType.AUTHENTICATION, "",
                        IdType.ACCOUNT, "", null, accountWE.getId(), null);
            }
            return userTO;
        }
        catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        catch (AuthException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            if (e.getMessage().contains(errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE())) {
                throw e;
            }
            userTO.setStatus(Constant.FAILED);
            userTO.setErrorCode(errorConstant.getVALIDATION_ERROR_CODE());
            userTO.setErrorMessage(e.getMessage());
            return userTO;
        }
        catch (ValidationException | UserFoundOnIDSException e) {
            logger.log(Level.DEBUG, e.getMessage(), e);
            session.getTransaction().rollback();
            userTO.setStatus(Constant.FAILED);
            userTO.setErrorCode(errorConstant.getVALIDATION_ERROR_CODE());
            userTO.setErrorMessage(e.getMessage());
        }
        finally {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " addAttributes: end");
        }
        return userTO;
    }

    public UserIciciStatusTO userStatus(UserIciciStatusTO userStatusTO, String applicationId, String serviceName) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " userStatus : start");
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        if (application.getApplicationType() == ApplicationType.AD) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_TYPE());
        }
        if (application.getTwoFactorStatus() == TwoFactorStatus.DISABLED) {
            userStatusTO.setStatusCode(Constant.APPLICATION_INACTIVE);
            return userStatusTO;
        }
        try {
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(),
                    AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            List<AttributeTO> searchAttributes = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : userStatusTO.getSearchAttributes()) {
                AttributeTO attributeTO = new AttributeTO();
                attributeTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
                attributeTO.setAttributeValue(attributeDataTO.getAttributeValue());
                searchAttributes.add(attributeTO);
            }
            AccountWE accountWE = iamExtension.searchAccount(searchAttributes, token);
            if (accountWE.getId() == null) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            boolean isConsumerActive = iamExtension.isConsumerActive(token, accountWE.getId());
            if (!isConsumerActive) {
                userStatusTO.setStatusCode(Constant.CONSUMER_NOT_REGISTERED);
                return userStatusTO;
            }
            User user = null;
            try {
                user = userService.getActiveUser(accountWE.getId());
            }
            catch (AuthException e) {
                logger.log(Level.INFO, e);
            }
            if (user == null) {
                userStatusTO.setStatusCode(Constant.CONSUMER_APPLICATION_BINDING_INACTIVE);
                return userStatusTO;
            }
            if (user.getUserStatus() == UserStatus.BLOCK) {
                userStatusTO.setStatusCode(Constant.USER_BLOCKED);
                return userStatusTO;
            }
            if (user.getTwoFactorStatus() == TwoFactorStatus.DISABLED) {
                userStatusTO.setStatusCode(Constant.CONSUMER_INACTVE);
                return userStatusTO;
            }

            if (serviceName == null) {
                serviceName = "APPROVAL";
            }
            Service service = serviceProcessor.getService(serviceName);
            if (service == null) {
                userStatusTO.setStatusCode(Constant.SERVICE_INACTIVE);
                return userStatusTO;
            }
            else if (!application.getServices().contains(service)) {
                userStatusTO.setStatusCode(Constant.SERVICE_APPLICATION_BINDING_INACTIVE);
                return userStatusTO;
            }

            UserApplicationServiceRel userApplicationRel = userApplicationRelService.getUserApplicationRel(user, application, service);
            if (userApplicationRel != null) {
                if (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE) {
                    userStatusTO.setStatusCode(Constant.CONSUMER_APPLICATION_BINDING_ACTIVE);
                    return userStatusTO;
                }
                else if (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED) {
                    userStatusTO.setStatusCode(Constant.CONSUMER_APPLICATION_BINDING_BLOCKED);
                    return userStatusTO;
                }
                else if (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED_FOR_RESET_PIN) {
                    userStatusTO.setStatusCode(Constant.BLOCKED_FOR_RESET_PIN_CODE);
                    return userStatusTO;
                }
                else if (userApplicationRel.getBindingStatus() == BindingStatus.RESET_PIN_COMPLETED) {
                    userStatusTO.setStatusCode(Constant.RESET_PIN_COMPLETED_CODE);
                    return userStatusTO;
                }
            }
            userStatusTO.setStatusCode(Constant.CONSUMER_APPLICATION_BINDING_INACTIVE);
            return userStatusTO;
        }
        catch (IAMException e) {
            logger.log(Level.INFO, e);
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT,
                    "User binding~", System.currentTimeMillis() + Constant.TILT, application.getApplicationId(), "~I-AM exception is thrown"));
            userStatusTO.setStatusCode(Constant.CONSUMER_NOT_REGISTERED);
            return userStatusTO;
        }
        finally {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " userStatus : end");
        }
    }

    private void attributeNameToUpperCase(AttributeDataTO attributeDataTO) {
        attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
    }

    private void attributeNameToUpperCase(UserTO userTO) {
        if (userTO.getAttributes() != null) {
            for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
                attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
            }
        }
        if (userTO.getSearchAttributes() != null) {
            for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
            }
        }
    }

    @Override
    public CryptoTokenTO verifyCryptoTokenTOTP(String applicationId, CryptoTokenTO cryptoTokenTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " verifyCryptoToken : start");
        String comment=null;
        checkUserPresent(cryptoTokenTO);
        for (in.fortytwo42.adapter.transferobj.AttributeTO attributeTO : cryptoTokenTO.getSearchAttributes()) {
            attributeTO.setAttributeName(attributeTO.getAttributeName().toUpperCase());
            AttributeValidationUtil.validateSearchAttributeValueAndUniqueness(attributeTO.getAttributeName(), attributeTO.getAttributeValue());
        }
        boolean isTokenVerified = false;
        Application application = null;
        AccountWE accountWE = null;
        String userSeed = null;
        try {
            application = null;
            try {
                application = ApplicationDaoImpl.getInstance().getApplicationByApplicationId(applicationId);
            } catch (ApplicationNotFoundException e) {
                logger.log(Level.ERROR, USER_FACADE_IMPL_LOG + errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
                comment = e.getMessage();
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(),errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
            }
            String applicationPassword = AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey());
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
            Token token = IAMUtil.getInstance().authenticateV2(iamExtensionV2, applicationId, applicationPassword);
            List<in.fortytwo42.adapter.transferobj.AttributeTO> searchAttributes = cryptoTokenTO.getSearchAttributes();
            accountWE = getAccount(searchAttributes, iamExtensionV2, token);
            String cryptoDID = accountWE.getCryptoDID();
            String attributeName = cryptoTokenTO.getAttributeData().getAttributeName().toUpperCase();
            String attributeValue = cryptoTokenTO.getAttributeData().getAttributeValue().toUpperCase();
            logger.log(Level.DEBUG,USER_FACADE_IMPL_LOG + cryptoDID +"::"+ attributeName +"::"+ attributeValue);
            userSeed = iamExtensionV2.getTokenSeedOnAttributeData(token, cryptoDID, attributeName,  attributeValue, reqRefNum);
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " user seed generated: " + userSeed);
            if (userSeed.length() < 20) {
                StringBuilder seed = new StringBuilder(userSeed);
                while (seed.length() < 20) {
                    seed.append("0");
                }
                userSeed = seed.toString();
            }
            userSeed = userSeed.toUpperCase();
            byte[] userSeedByteArray = Arrays.copyOfRange(userSeed.getBytes(), 0, 20);
            Base32 base32 = new Base32();
            userSeed = base32.encodeToString(userSeedByteArray);
            String code = cryptoTokenTO.getCryptoToken();
            int codeDigits = application.getNumberOfDigits();
            int timePeriod = application.getTotpExpiry().intValue();
            HashingAlgorithm hashAlgorithm = getHashingAlgorithm(application);
            int allowedNoOfPrevOTP = Constant.ALLOWED_NUMBER_OF_PREVIOUS_OTP_FOR_TOTP;
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " user seed base 32 encoded value: " + userSeed);
            logger.log(Level.DEBUG,USER_FACADE_IMPL_LOG + code + "::" + codeDigits + "::" + timePeriod + "::" + hashAlgorithm + "::" + allowedNoOfPrevOTP);
            isTokenVerified = TOTPUtil.verifyCode(userSeed, code, codeDigits, timePeriod, allowedNoOfPrevOTP, hashAlgorithm);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            comment=e.getMessage();
            isTokenVerified = false;
        }
        cryptoTokenTO.setStatus(isTokenVerified ? Constant.SUCCESS_STATUS : Constant.FAILURE_STATUS);
        AuditLogUtil.sendAuditLog(cryptoTokenTO.getCryptoToken() + "crypto token verified successfully ", "USER", ActionType.ONBOARD, "", IdType.ACCOUNT, "", null, "", null);
        FacadeFactory.getTOTPFacade().createTotpAuditTrail(application.getApplicationName(),cryptoTokenTO.getStatus(),cryptoTokenTO.getSearchAttributes().get(0).getAttributeName(),
                cryptoTokenTO.getSearchAttributes().get(0).getAttributeValue(),accountWE.getId(),userSeed,comment);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " verifyCryptoToken : end");
        return cryptoTokenTO;
    }

    private HashingAlgorithm getHashingAlgorithm(Application application) {
        HashingAlgorithm hashAlgorithm = null;
        switch (application.getAlgorithm()) {
            case SHA256:
                hashAlgorithm = HashingAlgorithm.SHA256;
                break;
            case SHA512:
                hashAlgorithm = HashingAlgorithm.SHA512;
                break;
            default:
                hashAlgorithm = HashingAlgorithm.SHA1;
                break;
        }
        return hashAlgorithm;
    }

    private UserIciciTO processIdentityMerging(UserIciciTO userTO, IAMExtensionV2 iamExtension, Token token, Session session, UserFoundOnIDSException e) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " processIdentityMerging : start");
        //TODO: Check for each attribute accounts
        //TODO: if multiple found throw error
        List<AttributeDataTO> allAttributes = getAllAttributes(userTO);
        List<AttributeDataTO> searchAttributes = getUniqueAttributes(allAttributes);
        AccountWE accountWE = validateAndGetIdentityForMerging(searchAttributes, iamExtension, token);
        List<AttributeTO> searchAttributesfromIdentity = getUniqueAttributeTO(accountWE.getAttributes());
        User user = ServiceFactory.getUserService().getActiveUser(session, accountWE.getId());
        //TODO: if one found check applicationID mapping
        if (!validateApplicationBinding(userTO.getSubscribedApplications(), user)) {
            if(isSearchAttributesPresentOnIds(searchAttributes,searchAttributesfromIdentity)){
                logger.error("User already onboarded in the system.");
                session.getTransaction().rollback();
                userTO.setStatus("FAILED");
                userTO.setErrorCode(errorConstant.getALREADY_PRESENT_IN_SYSTEM_CODE());
                userTO.setErrorMessage("User already onboarded in the system.");
                logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " processIdentityMerging : end");
                return userTO;
            }
            session.getTransaction().rollback();
            logger.log(Level.DEBUG, e.getMessage(), e);
            userTO.setStatus(Constant.FAILED);
            userTO.setErrorCode(errorConstant.getVALIDATION_ERROR_CODE());
            userTO.setErrorMessage(e.getMessage());
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " processIdentityMerging : end");
            return userTO;
        }
        //TODO: if different do addAttribute flow
        //TODO: send the account as the parameter rather than null
        UserIciciTO userTOCopy = getRequestForAttributesAddition(userTO, accountWE, allAttributes);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " processIdentityMerging userTOCopy "+new Gson().toJson(userTOCopy));
        List<AddAttributeType> addAttributeProcess = createAttributeAddProcessForOnboard();

        boolean isProcessCalled = false;
        try {
            for (AddAttributeType type : addAttributeProcess) {
                Onboarder onboarder = AddAttributeFactory.buildOnboarder(type);
                if (!onboarder.validate(token, iamExtension, userTOCopy, accountWE, user)) {
                    logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " processIdentityMerging AddAttributeType : " + type + " Process called");
                    isProcessCalled = true;
                    onboarder.process(token, iamExtension, userTOCopy, accountWE, user, session);
                }
            }
        } catch (AuthException e1) {
            session.getTransaction().rollback();
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " processIdentityMerging : end");
            throw e1;
        } catch (ValidationException e1) {
            session.getTransaction().rollback();
            logger.log(Level.DEBUG, e.getMessage(), e);
            userTO.setStatus(Constant.FAILED);
            userTO.setErrorCode(errorConstant.getVALIDATION_ERROR_CODE());
            userTO.setErrorMessage(e.getMessage());
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " processIdentityMerging : end");
            return userTO;
        } catch (UserFoundOnIDSException e1) {
            logger.log(Level.ERROR, e1.getMessage(), e);
        }
        userTO.setStatus(Constant.SUCCESS_STATUS);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " processIdentityMerging : end");
        return userTO;
    }

    private boolean isSearchAttributesPresentOnIds(List<AttributeDataTO> requestAttributes,
            List<AttributeTO> searchAttributesFromIdentity){
        int toCheck=requestAttributes.size();
        int checked=0;
        for(AttributeDataTO attributeDataTO: requestAttributes){
            if(isPresentOnIds(attributeDataTO,searchAttributesFromIdentity)){
                checked+=1;
            }
        }
        if(toCheck==checked){
            return true;
        }else {
            return false;
        }
    }
    private boolean isPresentOnIds(AttributeDataTO attributeDataTO,List<AttributeTO> searchAttributesFromIdentity){
        for(AttributeTO attributeTO : searchAttributesFromIdentity){
            if(attributeTO.getStatus().equals(AttributeState.ACTIVE.name())
                    && attributeDataTO.getAttributeName().equals(attributeTO.getAttributeName())
                    && attributeDataTO.getAttributeValue().equals(attributeTO.getAttributeValue())){
                return true;
            }
        }
        return false;
    }

    private AccountWE validateAndGetIdentityForMerging(List<AttributeDataTO> searchAttributes, IAMExtensionV2 iamExtension, Token token) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateAndGetIdentityForMerging : start");
        Set<String> accountIds = new HashSet<>();
        AccountWE accountWE = null;
        AccountWE accountWEReturn = null;
        for (AttributeDataTO attributeDataTO : searchAttributes) {
            List<AttributeTO> attributeTOs = new ArrayList<>();
            AttributeTO attributeTO = new AttributeTO();
            attributeTO.setAttributeName(attributeDataTO.getAttributeName());
            attributeTO.setAttributeValue(attributeDataTO.getAttributeValue());
            attributeTOs.add(attributeTO);
            try {
                accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
                if (accountWE != null && accountWE.getId() != null && !accountWE.getId().isEmpty()) {
                    accountIds.add(accountWE.getId());
                    accountWEReturn = accountWE;
                }
            } catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                throw iamExceptionConvertorUtil.convertToAuthException(e);
            }
        }
        if (accountIds.size() > 1) {
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateAndGetIdentityForMerging found multiple identities : "+accountIds.toString());
            logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateAndGetIdentityForMerging : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_MULTIPLE_IDENTITIES_FOUND(), errorConstant.getERROR_MESSAGE_MULTIPLE_IDENTITIES_FOUND());
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateAndGetIdentityForMerging : end");
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateAndGetIdentityForMerging found single identity : "+accountWE.getId());
        return accountWEReturn;
    }

    public boolean validateApplicationBinding(List<ApplicationTO> subscribedApplications, User user) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateApplicationBinding : start");
        boolean status = false;
        List<String> bindedApplicationIds = new ArrayList<>();
        //TODO : check binding without binding status.
        List<UserApplicationServiceRel> userApplicationServiceRels = ServiceFactory.getUserApplicationRelService().getUserApplicationRel(user.getId());
        for (UserApplicationServiceRel userApplicationServiceRel : userApplicationServiceRels) {
            UserApplicationServiceCompositeKey userApplicationServiceCompositeKey = userApplicationServiceRel.getId();
            bindedApplicationIds.add(userApplicationServiceCompositeKey.getApplication().getApplicationId());
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateApplicationBinding existing binded applications : "+bindedApplicationIds.toString());
        for (ApplicationTO applicationTO : subscribedApplications) {
            if (!bindedApplicationIds.contains(applicationTO.getApplicationId())) {
                status = true;
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateApplicationBinding status : "+status);
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " validateApplicationBinding : end");
        return status;
    }

    private UserIciciTO getRequestForAttributesAddition(UserIciciTO userTO, AccountWE accountWE, List<AttributeDataTO> attributes) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getRequestForAttributesAddition : start");
        UserIciciTO userTOCopy = new UserIciciTO();

        List<AttributeDataTO> attributesInAccount = new ArrayList<>();
        List<AttributeDataTO> attributesTobeAdded = new ArrayList<>();
        List<KeyValueTO> attributeKeyValuesTobeAdded = new ArrayList<>();
        List<KeyValueTO> attributesKeyValueInAccount = new ArrayList<>();
        for (AttributeTO attributeInAccount : accountWE.getAttributes()) {
            if(attributeInAccount.getStatus()!=null &&  attributeInAccount.getStatus().equalsIgnoreCase(AttributeStatus.ACTIVE.toString()) ) {
                logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " existing attribute :  AttributeName : " + attributeInAccount.getAttributeName() + " AttributeValue : " + attributeInAccount.getAttributeValue());
                KeyValueTO keyValueTO = new KeyValueTO();
                keyValueTO.setKey(attributeInAccount.getAttributeName());
                keyValueTO.setValue(attributeInAccount.getAttributeValue());
                if(attributeInAccount.getIsDefault() != null) {
                    keyValueTO.setDefault(attributeInAccount.getIsDefault());
                }
                attributesKeyValueInAccount.add(keyValueTO);
                AttributeDataTO attributeDataTO = new AttributeDataTO();
                attributeDataTO.setAttributeName(attributeInAccount.getAttributeName());
                attributeDataTO.setAttributeValue(attributeInAccount.getAttributeValue());
                attributesInAccount.add(attributeDataTO);
            }
        }
        for (AttributeDataTO attributeDataTO : attributes) {
            KeyValueTO keyValueTO = new KeyValueTO();
            keyValueTO.setKey(attributeDataTO.getAttributeName());
            keyValueTO.setValue(attributeDataTO.getAttributeValue());
            if(attributeDataTO.getIsDefault() != null){
                keyValueTO.setDefault(attributeDataTO.getIsDefault());
            }
            attributeKeyValuesTobeAdded.add(keyValueTO);
        }
        attributesTobeAdded = getAttributesTobeAdded(attributesKeyValueInAccount, attributeKeyValuesTobeAdded, attributesTobeAdded);
        List<AttributeDataTO> uniqueAttributesFromAccount = getUniqueAttributes(attributesInAccount);
        userTOCopy.setSearchAttributes(uniqueAttributesFromAccount);
        userTOCopy.setAttributeData(attributesTobeAdded);
        userTOCopy.setSubscribedApplications(userTO.getSubscribedApplications());
        userTOCopy.setCamEnabled(userTO.getCamEnabled());
        userTOCopy.setUserCredential(userTO.getUserCredential());
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getRequestForAttributesAddition : end");
        return userTOCopy;
    }

    public List<AttributeDataTO> getAttributesTobeAdded(List<KeyValueTO> attributesInAccount, List<KeyValueTO> attributesInRequest, List<AttributeDataTO> attributesTobeAdded){
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAttributesTobeAdded : start");
        attributesInRequest.removeAll(attributesInAccount);
        for (KeyValueTO KeyValueTO : attributesInRequest) {
            AttributeDataTO attributeDataTO = new AttributeDataTO();
            attributeDataTO.setAttributeName(KeyValueTO.getKey());
            attributeDataTO.setAttributeValue(KeyValueTO.getValue());
            if(KeyValueTO.getDefault() != null){
                attributeDataTO.setIsDefault(KeyValueTO.getDefault());
            }
            attributesTobeAdded.add(attributeDataTO);
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAttributesTobeAdded : end");
        return attributesTobeAdded;
    }

    public List<AttributeDataTO> getAllAttributes(UserIciciTO userTO) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAllAttributes : start");
        List<AttributeDataTO> allAttributes = new ArrayList<>();
        for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
            attributeDataTO.setAttributeValue(attributeDataTO.getAttributeValue().toUpperCase());
            allAttributes.add(attributeDataTO);
        }
        if (userTO.getAttributeData() != null && !userTO.getAttributeData().isEmpty()) {
            for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
                attributeDataTO.setAttributeValue(attributeDataTO.getAttributeValue().toUpperCase());
                allAttributes.add(attributeDataTO);
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getAllAttributes : end");
        return allAttributes;
    }

    public List<AttributeDataTO> getUniqueAttributes(List<AttributeDataTO> allAttributes) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUniqueAttributes : start");
        List<AttributeDataTO> uniqueAttributes = new ArrayList<>();
        List<AttributeMetadataTO> attributeMetaDataTOs = attributeMasterService.getAllAttributeMetaData();
        for (AttributeDataTO attributeDataTO : allAttributes) {
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
            int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            if (index < 0) {
                attributeMetadataTO.setAttributeName("OTHERS");
                index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            }
            attributeMetadataTO = attributeMetaDataTOs.get(index);
            if (attributeMetadataTO.getIsUnique() != null && attributeMetadataTO.getIsUnique()) {
                uniqueAttributes.add(attributeDataTO);
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUniqueAttributes : end");
        return uniqueAttributes;
    }
    public List<AttributeTO> getUniqueAttributeTO(List<AttributeTO> allAttributes) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUniqueAttributes : start");
        List<AttributeTO> uniqueAttributes = new ArrayList<>();
        List<AttributeMetadataTO> attributeMetaDataTOs = attributeMasterService.getAllAttributeMetaData();
        for (AttributeTO attributeTO : allAttributes) {
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            attributeMetadataTO.setAttributeName(attributeTO.getAttributeName());
            int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            if (index < 0) {
                attributeMetadataTO.setAttributeName("OTHERS");
                index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            }
            attributeMetadataTO = attributeMetaDataTOs.get(index);
            if (attributeMetadataTO.getIsUnique() != null && attributeMetadataTO.getIsUnique()) {
                uniqueAttributes.add(attributeTO);
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " getUniqueAttributes : end");
        return uniqueAttributes;
    }

    @Override
    public UserTO disableUser(UserTO userTO, String role, String username,Long id, boolean isEncrypted,boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " disableUser : start");
        Session session = sessionFactoryUtil.getSession();
        try {
           // User user = userService.getActiveUser(session, userTO.getUserId());
            /*if (userTO.getIamUserStatus() == null || userTO.getIamUserStatus().isEmpty()) {
                List<AttributeStore> attributeStore = attributeStoreDao.getAttributeByUserIdAndState(session, AttributeState.ACTIVE, user.getId());
                String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
                IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(enterpriseAccountId);
                for (AttributeStore attributeStore1 : attributeStore) {
                    accountWE = iamExtension.getAccount(attributeStore1.getAttributeName().toUpperCase(),
                            attributeStore1.getAttributeValue().toUpperCase());
                    if (accountWE != null) {
                        break;
                    }
                }
                if (accountWE != null ) {
                    userTO.setIamUserStatus(accountWE.getState());
                }
            }*/
            requestService.createDisableUserRequest(session, userTO, isEncrypted, username, id,saveRequest);
            if (!saveRequest) {
                userTO = approveDisableUser(session, userTO, role, userTO.getAccountId());
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        catch (NotFoundException e) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " disableUser : start");
        return userTO;
    }

    @Override
    public UserTO approveDisableUser(Session session, UserTO userTO, String role, String actor) throws AuthException, NotFoundException {
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveDisableUser : start");
       // AccountWE accountWE = null;
            User user = userService.getActiveUser(session, userTO.getUserId());
              if(userTO.getUserState()!=null) {
                  user.setUserState(UserState.valueOf(userTO.getUserState()));
              }
                user = userService.updateUser(session, user);
            userTO = user.convertToTO();

           /* if (userTO.getIamUserStatus() == null || userTO.getIamUserStatus().isEmpty()) {
                List<AttributeStore> attributeStores = attributeStoreDao.getAttributeByUserIdAndState(session, AttributeState.ACTIVE, user.getId());
                String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
                IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(enterpriseAccountId);
                for (AttributeStore attributeStore : attributeStores) {
                    accountWE = iamExtension.getAccount(attributeStore.getAttributeName().toUpperCase(),
                            attributeStore.getAttributeValue().toUpperCase());
                    if (accountWE != null) {
                        break;
                    }
                }
                if (accountWE != null ) {
                    userTO.setIamUserStatus(accountWE.getState());
                } else {
                    throw new AuthException(new Exception(),errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
                }
            }
            updateUserRole(session, userTO, role, actor);*/
        logger.log(Level.DEBUG, USER_FACADE_IMPL_LOG + " approveDisableUser : end");
        return userTO;
    }

    public void validateLastLoginTime(User user, Session session) throws AuthException {
        if (user.getLastLoginTime() == null) {
            user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
            userDao.update(session, user);
        } else {
            Long loginExpiry = (long) 30 * 24 * 12 * 60 * 60 * 1000;
            try {
                loginExpiry = Long.parseLong(config.getProperty(Constant.LOGIN_EXPIRY_TIME_IN_MILLIS));
            } catch (Exception e) {
                logger.log(Level.ERROR, USER_FACADE_IMPL_LOG + " Login Expiry Time Not Found ");
            }
            long loginExpiryTime = user.getLastLoginTime().getTime() + loginExpiry;
            if (loginExpiryTime < System.currentTimeMillis()) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_LOGIN_TIME_EXPIRED(), errorConstant.getERROR_MESSAGE_USER_LOGIN_TIME_EXPIRED());
            } else {
                user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
                userDao.update(session, user);
            }
        }
    }

    @Override
    public UserTO handleADFSUserRoleChange(Session session, UserTO userTO, String role, boolean isEncrypted) throws AuthException {
        String attributeValue = null;
        AttributeStore activeAttribute = null;
        AccountWE accountWe = new AccountWE();
        if(Constant.ADFS.equals(userTO.getAuthType())) {
            try {
                for (AttributeDataTO attribute : userTO.getAttributes()) {
                    if (isEncrypted) {
                        attributeValue = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), attribute.getAttributeValue());
                    }
                    else {
                        attributeValue = attribute.getAttributeValue();
                    }
                    try {
                        String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
                        IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(enterpriseAccountId);
                        accountWe = iamExtension.getAccount(attribute.getAttributeName().toUpperCase(), attributeValue.toUpperCase());
                        if(accountWe !=null) {
                           User user = userService.getUserByAccountId( accountWe.getId(),session);
                           if(user!=null){
                               List<AttributeStore> attributeStores = user.getAttributeStores();
                               if(attributeStores!=null && !attributeStores.isEmpty()){
                                for(AttributeStore attributeStore: attributeStores){
                                    if(attributeStore.getAttributeValue().equalsIgnoreCase(attributeValue)){
                                        activeAttribute = attributeStore;
                                        break;
                                    }
                                }
                               }
                           }
                        }
                        break;
                    }
                    catch (UserNotFoundException e) {
                        logger.log(Level.DEBUG, e.getMessage());
                    }
                }
                if (Constant.ADFS.equals(userTO.getAuthType()) && activeAttribute != null) {
                    userTO.setAttributes(null);
                    userTO.setTwoFactorStatus(String.valueOf(activeAttribute.getUser().getTwoFactorStatus()));
                    userTO.setUserId(activeAttribute.getUser().getId());
                    userTO.setUserStatus(String.valueOf(activeAttribute.getUser().getUserStatus()));
                    userTO.setTwoFactorStatus(String.valueOf(activeAttribute.getUser().getTwoFactorStatus()));
                    if (accountWe != null) {
                        userTO.setIamUserStatus(accountWe.getState());
                    }
                    userTO.setAccountId(activeAttribute.getUser().getAccountId());
                    return updateUserRole(session, userTO, role, null);
                }
            } catch (IAMException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), e.getMessage());
            } catch (NotFoundException e) {
                throw new AuthException(new Exception(),errorConstant.getERROR_CODE_INVALID_DATA(),e.getMessage());
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String userSeed = "68e451968bdbe3debdcba2a3819cbde0596ce215e53b3a3141f0f2d6d4aa30e0";
        userSeed = userSeed.toUpperCase();
        byte[] userSeedByteArray = Arrays.copyOfRange(userSeed.getBytes(), 0, 20);
        userSeed = Hex.encodeHexString(userSeedByteArray);
        System.out.println("User Seed Hex Value: " + userSeed);
    }
}
