package in.fortytwo42.adapter.facade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import in.fortytwo42.adapter.service.*;
import in.fortytwo42.adapter.transferobj.*;
import in.fortytwo42.adapter.util.*;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.entities.bean.*;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.transferobj.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.cam.dto.CamAttribute;
import in.fortytwo42.adapter.cam.dto.EditUserRequest;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeImpl;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeIntf;
import in.fortytwo42.adapter.cam.service.CamAdminServiceIntf;
import in.fortytwo42.adapter.controller.AttributeUpdater;
import in.fortytwo42.adapter.controller.AttributeValidater;
import in.fortytwo42.adapter.controller.DeleteAttributeFactory;
import in.fortytwo42.adapter.controller.UpdateAttributeFactory;
import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.enums.AttributeDeleteType;
import in.fortytwo42.adapter.enums.AttributeUpdateType;
import in.fortytwo42.adapter.enums.TransactionApprovalStatus;
import in.fortytwo42.adapter.enums.VerificationStatus;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.AttributeVerifierTO;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.factory.CsvFactory;
import in.fortytwo42.adapter.util.handler.AuthAttemptHistoryHandler;
import in.fortytwo42.daos.dao.AttributeStoreDaoImpl;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.AuthenticationAttemptDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.RequestNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.EnterpriseTO;
import in.fortytwo42.enterprise.extension.tos.GenerateAttributeClaimSelfSignedTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountPolicyWE;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.enterprise.extension.webentities.AttributeMetaDataWE;
import in.fortytwo42.enterprise.extension.webentities.AttributeVerifierWE;
import in.fortytwo42.enterprise.extension.webentities.PolicyWE;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.entities.enums.RequestSubType;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.enums.AttributeAction;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import org.hibernate.Transaction;
import org.jetbrains.annotations.Nullable;
import in.fortytwo42.adapter.controller.AttributeDeleterFromIds;

public class AttributeStoreFacadeImpl implements AttributeStoreFacadeIntf {


    // TODO: Facade to Facade
    private AuthAttemptFacadeIntf authAttemptFacade = FacadeFactory.getAuthAttemptFacade();

    /**
     * The attribute store facade impl log.
     */
    private static final String ATTRIBUTE_STORE_FACADE_IMPL_LOG = "<<<<< AttributeStoreFacadeImpl";

    private static final Logger logger= LogManager.getLogger(AttributeStoreFacadeImpl.class);

    private AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();
    private AuthenticationAttemptServiceIntf authAttemptService = ServiceFactory.getAuthenticationService();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private UserServiceIntf userService = ServiceFactory.getUserService();
    private EvidenceStoreServiceIntf evidenceStoreService = ServiceFactory.getEvidenceStoreService();
    private AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
    private PermissionServiceIntf permissionService = ServiceFactory.getPermissionService();
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private RequestDaoIntf requestDao = DaoFactory.getRequestDao();
    private UserDaoIntf userDao=DaoFactory.getUserDao();
    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    private AuthenticationAttemptDaoIntf authenticationAttemptDao = DaoFactory.getAuthenticationAttemptDao();

    private Config config = Config.getInstance();
    private PermissionUtil permissionUtil = PermissionUtil.getInstance();
    private AuthAttemptHistoryHandler authAttemptHistoryHandler = AuthAttemptHistoryHandler.getInstance();
    private IAMUtil iamUtil = IAMUtil.getInstance();
    /**
     * The Session Factory Util
     */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private final ExecutorService pool;

    private CamAdminServiceIntf camAdminService = ServiceFactory.getCamAdminService();

    private CamUserFacadeIntf CamUserFacade = CamUserFacadeImpl.getInstance();
    private static final String ATTRIBUTE_VERIFIERS_CACHE = "attributesVerifiersCache";
    private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    private final CacheConfiguration<String, String> attributeVerifierCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(100000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.ATTRIBUTE_VERIFIER_CACHE_TIMEOUT_IN_SECONDS)!=null?Config.getInstance().getProperty(Constant.ATTRIBUTE_VERIFIER_CACHE_TIMEOUT_IN_SECONDS):"1800"))))
            .build();
    private final Cache<String, String> attributeVerifierStore = cacheManager.createCache("attributeVerifiersCache", attributeVerifierCacheConfiguration);
    private UserApplicationRelServiceIntf userApplicationRelService = ServiceFactory.getUserApplicationRelService();
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    private CamUserFacadeIntf camUserFacade = CamUserFacadeImpl.getInstance();

    private AttributeStoreFacadeImpl() {
        super();
        int poolSize = 10;
        try {
            poolSize = Integer.parseInt(config.getProperty(Constant.CSV_PROCESSING_THREAD_POOL_SIZE));
        } catch (NumberFormatException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);

    }

    private static final class InstanceHolder {
        private static final AttributeStoreFacadeImpl INSTANCE = new AttributeStoreFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static AttributeStoreFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void verifyAttribute(AttributeTO verifyAttributeTO, String actor, Long id) throws AuthException {
        attributeNameToUpperCase(verifyAttributeTO);
        Gson gson = new Gson();
        AuthenticationAttempt authAttempt = authAttemptService.getAuthAttemptById(verifyAttributeTO.getId());
        if (authAttempt.getAttemptStatus().equals(Constant.PENDING)
                && authAttempt.getAttemptType().equals(Constant.ATTRIBUTE_VERIFICATION)) {
            List<Request> requests = requestDao.getRequests(RequestType.ATTRIBUTE_VERIFICATION,
                    ApprovalStatus.CHECKER_APPROVAL_PENDING);
            if (requests != null && !requests.isEmpty()) {
                for (Request request : requests) {
                    AttributeTO verifyAttributeTORequest = gson.fromJson(request.getRequestJSON(), AttributeTO.class);
                    if (verifyAttributeTORequest.getId().longValue() == verifyAttributeTO.getId().longValue()) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_ALREADY_SENT_TO_CHECKER(),
                                errorConstant.getERROR_MESSAGE_REQUEST_ALREADY_SENT_TO_CHECKER());
                    }
                }
            }
            Session session = sessionFactoryUtil.getSession();
            try {
                requestService.createAttributeVerificationRequest(session, verifyAttributeTO, actor, id);
                sessionFactoryUtil.closeSession(session);
            } catch (AuthException e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
        } else {
            throw new AuthException(null, errorConstant.getERROR_CODE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND(),
                    errorConstant.getERROR_MESSAGE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND());
        }
    }

    @Override
    public PaginatedTO<AttributeTO> getAttributeVerificationRequests(int page, String searchText) {
        int limit = Integer.parseInt(config.getProperty(Constant.LIMIT));
        int offset = (limit * page) - limit;
        List<AuthenticationAttempt> athenticationAttempts = authAttemptService.getReceivedAuthenticationRequests(
                Constant.ATTRIBUTE_VERIFICATION, Constant.PENDING, limit, offset, searchText);
        System.out.println("athenticationAttempts..>>" + athenticationAttempts.size());
        List<AttributeTO> attributeRequests = new ArrayList<AttributeTO>();
        for (AuthenticationAttempt athenticationAttempt : athenticationAttempts) {
            System.out.println("athenticationAttempt..>>" + new Gson().toJson(athenticationAttempt));
            AttributeTO attributeRequest = new AttributeTO();
            String transactionDetail = athenticationAttempt.getTransactionDetails();
            String[] details = transactionDetail.split("\\|");
            if (details.length > 1) {
                attributeRequest.setId(athenticationAttempt.getId());
                attributeRequest.setDataTimeCreated(Long.parseLong(details[0]));
                String userName = details[1];
                attributeRequest.setUserName(userName);
                attributeRequest.setAttributeName(details[2].toUpperCase());
                attributeRequest.setAttributeType(details[2]);
                String attributeTitle = details.length == 4 ? details[3] : details[2];
                attributeRequest.setAttributeTitle(attributeTitle);
            }
            System.out.println("attributeRequest..>>" + attributeRequest);
            attributeRequests.add(attributeRequest);
        }
        PaginatedTO<AttributeTO> pendingAuthAttempt = new PaginatedTO<>();
        pendingAuthAttempt.setTotalCount(
                authAttemptService.getTotalRequestCount(Constant.ATTRIBUTE_VERIFICATION, Constant.PENDING, searchText));
        pendingAuthAttempt.setList(attributeRequests);
        return pendingAuthAttempt;
    }

    @Override
    public PaginatedTO<UserTO> getPendingAttributeVerificationRequests(int limit, int offset) throws AuthException {
        List<AuthenticationAttempt> athenticationAttempts = authAttemptService
                .getAuthenticationRequests(Constant.ATTRIBUTE_VERIFICATION, Constant.PENDING, null, limit, offset);
        System.out.println("<<<<< athenticationAttempts" + athenticationAttempts.size());
        List<UserTO> userTOs = new ArrayList<>();
        for (AuthenticationAttempt athenticationAttempt : athenticationAttempts) {
            UserTO userTO = new UserTO();
            userTO.setId(athenticationAttempt.getId());
            userTO.setUserIdentifier(athenticationAttempt.getSenderAccountId());
            List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
            try {
                AttributeDataTO attributeDataTO = attributeStoreService.getAttribute(athenticationAttempt.getId());
                attributeDataTOs.add(attributeDataTO);
            } catch (Exception e) {
                logger.log(Level.ERROR, e);
            }
            userTO.setAttributes(attributeDataTOs);
            userTOs.add(userTO);
        }
        PaginatedTO<UserTO> pendingAuthAttempt = new PaginatedTO<>();
        pendingAuthAttempt.setTotalCount(
                authAttemptService.getTotalRequestCount(Constant.ATTRIBUTE_VERIFICATION, Constant.PENDING, null));
        pendingAuthAttempt.setList(userTOs);
        return pendingAuthAttempt;
    }

    @Override
    public PaginatedTO<RequestTO> getPendingAttributeVerificationRequests(int page, int limit, String role)
            throws AuthException {
        permissionUtil.validateAttributeApprovalPermission(role);
        return requestService.getPendingAttributeVerificationRequests(page, limit);
    }

    @Override
    public AttributeTO approvePendingRequest(AttributeTO attributeTO, String role, String actor,Long id) throws AuthException {
        attributeNameToUpperCase(attributeTO);
        Request request = requestService.getPendingRequestById(attributeTO.getId());
        // TODO: permission handling

        if (request.getRequestType() == RequestType.ATTRIBUTE_VERIFICATION) {
            approveAttributeVerification(attributeTO, request);
        } else if (request.getRequestType() == RequestType.ATTRIBUTE_ADDITION) {
            AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_ADDITION_REQUEST_APPROVED + AuditLogConstant.FOR_ATTRIBUTE + attributeTO.getAttributeName() + " = " + attributeTO.getAttributeValue() + AuditLogConstant.BY + actor, "ENTERPRISE", ActionType.ADD_ATTRIBUTE_TO_ACCOUNT, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", request.getRequestor().getAccountId(), null);
            approveAttributeAddition(attributeTO, request);
        } else if (request.getRequestType() == RequestType.ATTRIBUTE_UPDATION) {
            approveEditAttributeAddition(attributeTO, request);
        } else if (request.getRequestType() == RequestType.ATTRIBUTE_MASTER_ADDITION) {
            AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_CREATION_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + (new Gson().fromJson(request.getRequestJSON(),
                    AttributeMetadataTO.class)).getAttributeName().toUpperCase(), "ENTERPRISE", ActionType.AUTHENTICATION, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
            approveCreateAttributeMasterAddition(attributeTO, request);
            AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_CREATION_SUCCESSFUL + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + (new Gson().fromJson(request.getRequestJSON(),
                    AttributeMetadataTO.class)).getAttributeName().toUpperCase(), "ENTERPRISE", ActionType.AUTHENTICATION, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
        } else if (request.getRequestType() == RequestType.ATTRIBUTE_MASTER_UPDATION) {
            AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_UPDATE_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + (new Gson().fromJson(request.getRequestJSON(),
                    AttributeMetadataTO.class)).getAttributeName().toUpperCase(), "ENTERPRISE", ActionType.AUTHENTICATION, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
            approveEditAttributeMasterAddition(attributeTO, request);
            AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_UPDATE_SUCCESSFUL + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + (new Gson().fromJson(request.getRequestJSON(),
                    AttributeMetadataTO.class)).getAttributeName().toUpperCase(), "ENTERPRISE", ActionType.AUTHENTICATION, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
        } else if (request.getRequestType() == RequestType.ATTRIBUTE_MASTER_DELETION) {
            AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_DELETE_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + (new Gson().fromJson(request.getRequestJSON(),
                    AttributeMetadataTO.class)).getAttributeName().toUpperCase(), "ENTERPRISE", ActionType.AUTHENTICATION, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
            approveDeleteAttributeMasterAddition(attributeTO, request);
            AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_DELETE_SUCCESSFUL + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + (new Gson().fromJson(request.getRequestJSON(),
                    AttributeMetadataTO.class)).getAttributeName().toUpperCase(), "ENTERPRISE", ActionType.AUTHENTICATION, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
        } else {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_REQUEST(),
                    errorConstant.getERROR_MESSAGE_INVALID_REQUEST());
        }
        try {
            request.setApprover(userDao.getActiveUserById(id));
        } catch (UserNotFoundException e1) {
            logger.log(Level.ERROR, e1.getMessage(), e1);
        }
        request.setChecker(actor);
        request.setApproverComments(attributeTO.getComments());
        Session session = sessionFactoryUtil.getSession();
        try {
            requestService.updateRequest(session, request);
            attributeTO.setStatus(Constant.SUCCESS_STATUS);
            sessionFactoryUtil.closeSession(session);
        } catch (RequestNotFoundException e) {
            session.getTransaction().rollback();
            throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(),
                    errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return attributeTO;
    }

    /*
     * private void approveAttributeVerification(AttributeTO attributeTO, Request
     * request) throws AuthException { AttributeTO makerRequest = new
     * Gson().fromJson(request.getRequestJSON(), AttributeTO.class);
     * AuthenticationAttempt authenticationAttempt =
     * authAttemptProcessor.getAuthAttemptById(makerRequest.getId()); if
     * (authenticationAttempt.getAttemptStatus().equals(Constant.PENDING)) { if
     * (attributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.
     * name())) { request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER); }
     * else { String approvalStatus; if
     * (makerRequest.getAttributeState().equals(Constant.VERIFICATION_SUCCESS)) {
     * approvalStatus = Constant.APPROVED; } else { approvalStatus =
     * Constant.REJECTED; }
     * attributeStoreProcessor.verifyAttribute(authenticationAttempt.getId(),
     * authenticationAttempt.getTransactionId(),
     * approvalStatus,authenticationAttempt.getSignTransactionId());
     * authAttemptProcessor.updateAuthAttempt(authenticationAttempt,
     * approvalStatus);
     * request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER); } } else {
     * throw new AuthException(null, errorConstant.getERROR_CODE_ALREADY_APPROVED,
     * errorConstant.getERROR_MESSAGE_UPDATE_APPROVAL_ATTEMPT_FAILED +
     * authenticationAttempt.getAttemptStatus()); } }
     */

    private void approveAttributeVerification(AttributeTO attributeTO, Request request) throws AuthException {
        AttributeTO makerRequest = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);
        AuthenticationAttempt authenticationAttempt = authAttemptService.getAuthAttemptById(makerRequest.getId());
        if (authenticationAttempt.getAttemptStatus().equals(Constant.PENDING)) {
            if (attributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
            } else {
                String approvalStatus;
                if (makerRequest.getAttributeState().equals(Constant.VERIFICATION_SUCCESS)) {
                    approvalStatus = Constant.APPROVED;
                } else {
                    approvalStatus = Constant.REJECTED;
                }
                System.out.println("approvalStatus : " + approvalStatus);
                approveVerifyAttribute(authenticationAttempt, approvalStatus);
                request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
            }
        } else {
            throw new AuthException(null, errorConstant.getERROR_CODE_ALREADY_APPROVED(),
                    errorConstant.getERROR_MESSAGE_UPDATE_APPROVAL_ATTEMPT_FAILED()
                            + authenticationAttempt.getAttemptStatus());
        }
    }

    /*
     * @Override public AttributeTO verifyAttributeRequest(AttributeTO attributeTO)
     * throws AuthException { AuthenticationAttempt authenticationAttempt =
     * authAttemptProcessor.getAuthAttemptById(attributeTO.getId()); if
     * (in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING.name().
     * equals(authenticationAttempt.getAttemptStatus())) {
     * attributeStoreProcessor.verifyAttribute(authenticationAttempt.getId(),
     * authenticationAttempt.getTransactionId(), attributeTO.getApprovalStatus(),
     * authenticationAttempt.getSignTransactionId());
     * authAttemptProcessor.updateAuthAttempt(authenticationAttempt,
     * attributeTO.getApprovalStatus());
     * attributeTO.setStatus(Constant.SUCCESS_STATUS); return attributeTO; } else if
     * (in.fortytwo42.enterprise.extension.enums.ApprovalStatus.TIMEOUT.name().
     * equals(authenticationAttempt.getAttemptStatus())) { throw new
     * AuthException(null, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT,
     * errorConstant.getERROR_MESSAGE_REQUEST_IS_TIMEOUT); } else { throw new
     * AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_PENDING,
     * errorConstant.getERROR_MESSAGE_REQUEST_NOT_PENDING +
     * authenticationAttempt.getAttemptStatus()); } }
     */

    @Override
    public AttributeTO verifyAttributeRequest(AttributeTO attributeTO) throws AuthException {
        attributeNameToUpperCase(attributeTO);
        AttributeStore attributeStore;
        try {
            attributeStore = attributeStoreDao.getAttributeByAuthId(attributeTO.getId());
        } catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(),
                    errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        if (!attributeStore.getAttributeName().equals(attributeTO.getAttributeName())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_ATTRIBUTE_NAME(),
                    errorConstant.getERROR_MESSAGE_INVALID_ATTRIBUTE_NAME());
        }
        AuthenticationAttempt authenticationAttempt;
        try {
            authenticationAttempt = authAttemptService.getAuthAttempt(attributeTO.getId());
        } catch (NotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(),
                    errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
        }
        if (in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING.name()
                .equals(authenticationAttempt.getAttemptStatus())) {
            approveVerifyAttribute(authenticationAttempt, attributeTO.getApprovalStatus());
            attributeTO.setStatus(Constant.SUCCESS_STATUS);
            return attributeTO;
        } else if (in.fortytwo42.enterprise.extension.enums.ApprovalStatus.TIMEOUT.name()
                .equals(authenticationAttempt.getAttemptStatus())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(),
                    errorConstant.getERROR_MESSAGE_REQUEST_IS_TIMEOUT());
        } else {
            throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_PENDING(),
                    errorConstant.getERROR_MESSAGE_REQUEST_NOT_PENDING() + authenticationAttempt.getAttemptStatus());
        }
    }

    public void approveVerifyAttribute(AuthenticationAttempt authenticationAttempt, String approvalStatus)
            throws AuthException {
        Session session = sessionFactoryUtil.getSession();
        try {

            attributeStoreService.verifyAttribute(session, authenticationAttempt.getId(),
                    authenticationAttempt.getTransactionId(), approvalStatus,
                    authenticationAttempt.getSignTransactionId());
            // authAttemptProcessor.updateAuthAttempt(authenticationAttempt,
            // approvalStatus);
            if (authenticationAttempt != null) {
                authenticationAttempt.setAttemptStatus(approvalStatus);
                authAttemptHistoryHandler.updateAuthAttemptHistoryDataByTrasactionId(session, authenticationAttempt);
                authenticationAttemptDao.remove(session, authenticationAttempt);
            }
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public AttributeDataTO getAttribute(Long authAttemptId) throws AuthException {
        return attributeStoreService.getAttribute(authAttemptId);
    }

    @Override
    public PaginatedTO<RequestTO> getPendingAttributeAdditionRequests(int page, int limit, String role)
            throws AuthException {
        permissionUtil.validateAttributeApprovalPermission(role);
        return requestService.getPendingAttributeAdditionRequests(page, limit);
    }

    /*
     * private void approveAttributeAddition(AttributeTO attributeTO, Request
     * request) throws AuthException { if
     * (attributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.
     * name())) { request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER); }
     * else { request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
     * AttributeTO requestTO = new Gson().fromJson(request.getRequestJSON(),
     * AttributeTO.class); AttributeDataTO attribute = new AttributeDataTO();
     * attribute.setAttributeName(requestTO.getAttributeName());
     * attribute.setAttributeValue(requestTO.getAttributeValue());
     * attribute.setEvidence(requestTO.getEvidence());
     * attribute.setAttributeType(requestTO.getAttributeType()); String evidence =
     * getHashForEvidence(requestTO.getEvidence());
     * attribute.setEvidenceHash(evidence); AuthenticationAttempt
     * authenticationAttempt =
     * authAttemptFacade.createAttributeAdditionRequest(requestTO.getId(),
     * attribute); AttributeStore attributeStore =
     * ProcessorFactory.getAttributeStoreProcessor().saveAttributeData(attribute,
     * authenticationAttempt.getId(), authenticationAttempt.getReceiverAccountId(),
     * Constant.PENDING); if (requestTO.getEvidence() != null) {
     * ProcessorFactory.getEvidenceStoreProcessor().storeEvidence(attributeStore.
     * getId(), requestTO.getEvidence()); } } }
     */
    private void approveAttributeAddition(AttributeTO attributeTO, Request request) throws AuthException {
        attributeNameToUpperCase(attributeTO);
        List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeTO.getAttributeName());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName("OTHERS");
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        if (attributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
            request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
        } else {
            request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
            AttributeTO requestTO = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);
            attributeNameToUpperCase(requestTO);
            AttributeDataRequestTO attributeDataRequestTO = new AttributeDataRequestTO();
            AttributeDataTO attribute = new AttributeDataTO();
            attribute.setAttributeName(requestTO.getAttributeName());
            attribute.setAttributeValue(requestTO.getAttributeValue());
            attribute.setEvidence(requestTO.getEvidence());
            attribute.setAttributeType(requestTO.getAttributeType());
            attribute.setIsDefault(requestTO.getIsDefault());
            String evidence = getHashForEvidence(requestTO.getEvidence());
            attribute.setEvidenceHash(evidence);
            attribute.setIsUnique(attributeMetadataTO.getIsUnique());
            attributeDataRequestTO.setAttributeData(attribute);
            attributeDataRequestTO.setId(requestTO.getId());
            attributeDataRequestTO.setCallStatus(Constant.ADMIN);
            attributeDataRequestTO.setIsConsentRequired(requestTO.getIsConsentRequired());
            AttributeValidationUtil.isAttributePresentOnAdapter(attribute.getAttributeName(), attribute.getAttributeValue(), attributeDataRequestTO.getId());
            if (attributeDataRequestTO.getIsConsentRequired() == null || attributeDataRequestTO.getIsConsentRequired()) {
                approveRequest(attributeDataRequestTO);
            } else {
                approveRequestWithoutConsent(attributeDataRequestTO);
            }
        }
    }

    public String getHashForEvidence(List<String> evidences) {
        if (evidences != null && !evidences.isEmpty()) {
            String evidenceHash = null;
            for (String evidence : evidences) {
                byte[] decodedEvidence = Base64.getMimeDecoder().decode(evidence);
                String newEvidenceHash = SHAImpl.sha256Hex(decodedEvidence);
                if (evidenceHash == null) {
                    evidenceHash = newEvidenceHash;
                } else {
                    evidenceHash += newEvidenceHash;
                }
            }
            return SHAImpl.sha256Hex(evidenceHash.getBytes());
        }
        return null;
    }

    @Override
    public AttributeTO addAttribute(AttributeTO addAttributeTO, String actor, String role,Long id) throws AuthException {
        attributeNameToUpperCase(addAttributeTO);
        if (!permissionService.isPermissionValidForRole(PermissionUtil.ATTRIBUTE_ADDITION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(),
                    errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        User user = userService.getActiveUser(addAttributeTO.getId());
        List<Request> requests = requestDao.getRequests(RequestType.ATTRIBUTE_ADDITION,
                ApprovalStatus.CHECKER_APPROVAL_PENDING, ApprovalStatus.USER_APPROVAL_PENDING);
        if (requests != null && !requests.isEmpty()) {
            for (Request request : requests) {
                AttributeTO addAttributeTORequest = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);
                if (addAttributeTORequest.getId().equals(addAttributeTO.getId())) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ADDITION_ALREADY_PRESENT(),
                            errorConstant.getERROR_MESSAGE_ATTRIBUTE_ADDITION_ALREADY_PRESENT());
                }
            }
        }
        String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
        List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        try {
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(enterpriseAccountId);
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            attributeMetadataTO.setAttributeName(addAttributeTO.getAttributeName());
            int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            if (index < 0) {
                attributeMetadataTO.setAttributeName("OTHERS");
                index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            }
            attributeMetadataTO = attributeMetaDataTOs.get(index);
            //handling attribute addition unique case
            if (attributeMetadataTO.getIsUnique()!=null&& attributeMetadataTO.getIsUnique()) {

                String attributeValue = addAttributeTO.getAttributeValue();
                AccountWE accountWE = iamExtension.getAccount(addAttributeTO.getAttributeName(),
                        addAttributeTO.getAttributeValue());
                if (accountWE.getId() != null) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(),
                            addAttributeTO.getAttributeName()+ errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                }
            }
            //handling non-unique attribute already present in users attribute
            List<AttributeDataTO> attributeDataTO  = new EntityToTOConverter<AttributeStore, AttributeDataTO>().convertEntityListToTOList(AttributeStoreDaoImpl.getInstance().getUserAttributes(user.getId()));
            for (AttributeDataTO attribute : attributeDataTO) {
                if (attribute.getAttributeName().equalsIgnoreCase(addAttributeTO.getAttributeName()) && attribute.getAttributeValue().equalsIgnoreCase(addAttributeTO.getAttributeValue()) && !attribute.getAttributeState().equals(AttributeState.DELETE.name())) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(),
                            addAttributeTO.getAttributeName()+ errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                }

            }
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        /*
         * if(user.getUsername() !=null) {
         * addAttributeTO.setUserName(user.getUsername()); }
         */
        Session session = sessionFactoryUtil.getSession();
        try {
            addAttributeTO = requestService.createAttributeAdditionRequest(session, addAttributeTO, actor,id);
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return addAttributeTO;
    }

    @Override
    public PaginatedTO<RequestTO> getPendingAttributeRequests(int page, String role, String requestType)
            throws AuthException {
        permissionUtil.validateAttributeApprovalPermission(role);
        int limit = Integer.parseInt(config.getProperty(Constant.LIMIT));
        return requestService.getPendingAttributeRequests(page, limit, requestType,
                ApprovalStatus.CHECKER_APPROVAL_PENDING.name());
    }

    @Override
    public PaginatedTO<RequestTO> getPaginatedNonPendingRequests(int page, String role, String requestType) {
        int limit = Integer.parseInt(config.getProperty(Constant.LIMIT));
        return requestService.getPaginatedApproveAndRejectedRequests(page, limit, RequestType.valueOf(requestType));
    }

    /*
     * @Override public AttributeDataRequestTO
     * sendAttributeAdditionRequest(AttributeDataRequestTO attributeDataRequestTO)
     * throws AuthException { AuthenticationAttempt authenticationAttempt = null;
     * try {
     * attributeDataRequestTO.getAttributeData().setEvidenceHash(getHashForEvidence(
     * attributeDataRequestTO.getAttributeData().getEvidence()));
     * authenticationAttempt =
     * authAttemptFacade.createAttributeAdditionRequest(attributeDataRequestTO); }
     * catch (AuthException e) { IAMLogger.getInstance().log(Level.FATAL,
     * e.getMessage(), e); throw new AuthException(e, Constant.BAD_DATA_ERROR_CODE,
     * e.getMessage()); } AttributeStore attributeStore =
     * ProcessorFactory.getAttributeStoreProcessor().saveAttributeData(
     * attributeDataRequestTO.getAttributeData(), authenticationAttempt.getId(),
     * authenticationAttempt.getReceiverAccountId(), Constant.PENDING); if
     * (attributeDataRequestTO.getAttributeData().getEvidence() != null) {
     * ProcessorFactory.getEvidenceStoreProcessor().storeEvidence(attributeStore.
     * getId(), attributeDataRequestTO.getAttributeData().getEvidence()); }
     * attributeDataRequestTO.setStatus(Constant.PENDING);
     * attributeDataRequestTO.setId(authenticationAttempt.getId()); return
     * attributeDataRequestTO; }
     */


    public AttributeMetadataTO getAttributeMaster(String attributeName) throws AuthException {
        AttributeMetaDataWE attributeMetadataWE = iamExtensionService.getAttributeMetadata(attributeName.toUpperCase());
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeMetadataWE.getAttributeName());
        attributeMetadataTO.setAttributeSettings(attributeMetadataWE.getAttributeSettings());
        attributeMetadataTO.setApplicableAccountTypes(attributeMetadataWE.getApplicableAccountTypes());
        attributeMetadataTO.setAttributeStoreSecurityPolicy(attributeMetadataWE.getAttributeStoreSecurityPolicy().name());
        attributeMetadataTO.setAttributeType(attributeMetadataWE.getAttributeType().name());
        attributeMetadataTO.setAttributeValueModel(attributeMetadataWE.getAttributeValueModel().name());
        attributeMetadataTO.setStatus(attributeMetadataWE.getStatus());
        List<AttributeVerifierTO> attributeVerifiers = new ArrayList<>();
        for (AttributeVerifierWE verifierWE : attributeMetadataWE.getAttributeVerifiers()) {
            AttributeVerifierTO attributeVerifierTO = new AttributeVerifierTO();
            attributeVerifierTO.setIsDefault(verifierWE.getIsDefault());
            attributeVerifierTO.setIsActive(verifierWE.getIsActive());
            attributeVerifierTO.setPriority(verifierWE.getPriority());
            attributeVerifierTO.setSourceId(verifierWE.getSourceId());
            attributeVerifierTO.setSourceType(verifierWE.getSourceType());
            attributeVerifierTO.setVerifierType(verifierWE.getVerifierType());
            attributeVerifierTO.setVerifierId(verifierWE.getVerifierId());
            attributeVerifierTO.setVerifierName(verifierWE.getVerifierName());
            attributeVerifierTO.setVerificationType(verifierWE.getVerificationType());
            attributeVerifiers.add(attributeVerifierTO);
        }
        attributeMetadataTO.setAttributeVerifiers(attributeVerifiers);
        return attributeMetadataTO;
    }

    @Override
    public AttributeDataRequestTO sendAttributeAdditionRequest(AttributeDataRequestTO attributeDataRequestTO)
            throws AuthException {
        /*
         * String evidenceHash =
         * EvidenceStoreProcessorImpl.getInstance().getHashForEvidence(
         * attributeDataRequestTO.getAttributeData().getEvidenceId()); List<String>
         * evidences = new ArrayList<String>(); String evidencehash =
         * getHashForEvidence();
         * attributeDataRequestTO.getAttributeData().setEvidenceHash(evidenceHash);
         * attributeDataRequestTO.setCallStatus(Constant.APPLICATION);
         */

        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        String attributeName = attributeDataRequestTO.getAttributeData().getAttributeName();
        //using attribute name fetching all info about attribute from DB
        AttributeMasterFacadeIntf attributeFacade = FacadeFactory.getAttributeMasterFacade();
        AttributeMetadataTO attributeMataDataFromDB = attributeFacade.getAttributeMaster(attributeName);
        AttributeDataTO attribute = attributeDataRequestTO.getAttributeData();
        if (attribute.getAttributeName().equalsIgnoreCase(attributeMataDataFromDB.getAttributeName()) && attribute.getAttributeType()
                .equalsIgnoreCase(attributeMataDataFromDB.getAttributeType())) {


            AttributeMetadataTO attributeMetadataTO = getAttributeMaster(attributeDataRequestTO.getAttributeData().getAttributeName());
            if (attributeMetadataTO.getAttributeSettings().get(Constant.EVIDENCE_COUNT) != null) {
                double evidenceCount = (double) attributeMetadataTO.getAttributeSettings().get(Constant.EVIDENCE_COUNT);
                int evidenceCountValue = (int) evidenceCount;
                if (evidenceCountValue > 0 && attributeDataRequestTO.getAttributeData().getEvidenceId() == null) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_EVIDENCE_IS_REQUIRED(), errorConstant.getERROR_MESSAGE_EVIDENCE_IS_REQUIRED());
                }
                List<String> evidences = new ArrayList<>();
                if (attributeDataRequestTO.getAttributeData().getEvidenceId() != null) {
                    EvidenceStore evidenceStore = evidenceStoreService
                            .getEvidenceByEvidenceId(attributeDataRequestTO.getAttributeData().getEvidenceId());
                    File evidenceFile = new File(evidenceStore.getFilePath());
                    String evidenceData = FileUtil.encodeFileToBase64Binary(evidenceFile);
                    evidences.add(evidenceData);
                    attributeDataRequestTO.getAttributeData().setEvidence(evidences);
                }
                attributeDataRequestTO.getAttributeData().setEvidenceHash(getHashForEvidence(evidences));
            }

            attributeDataRequestTO.setCallStatus(Constant.APPLICATION);
            return approveRequest(attributeDataRequestTO);
        } else {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_ATTRIBUTE_TYPE(),
                    errorConstant.getERROR_MESSAGE_INVALID_ATTRIBUTE_TYPE());

        }
    }

    @Override
    public AttributeDataRequestTO approveRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException {
        Session session = sessionFactoryUtil.getSession();
        try {
            AuthenticationAttempt authenticationAttempt = null;
            try {
                attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
                authenticationAttempt = authAttemptFacade.createAttributeAdditionRequest(session, attributeDataRequestTO);
                authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
            } catch (AuthException e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                throw new AuthException(e, Constant.BAD_DATA_ERROR_CODE, e.getMessage());
            }
            try {
                logger.log(Level.DEBUG, "session : " + session);
                logger.log(Level.DEBUG, "attributeDataRequestTO : " + new Gson().toJson(attributeDataRequestTO));
                logger.log(Level.DEBUG, "authenticationAttempt : " + new Gson().toJson(authenticationAttempt));
                logger.log(Level.DEBUG, "authenticationAttempt : " + new Gson().toJson(authenticationAttempt));
                User user = new User();
                try {
                    user = userService.getUserByAccountId(authenticationAttempt.getReceiverAccountId());
                } catch (UserNotFoundException e) {
                    user = userService.createUser(session, authenticationAttempt.getReceiverAccountId(), UserRole.USER,
                            TwoFactorStatus.DISABLED.toString(), user);
                }
                List<AttributeDataTO> userAttributeDataTOs = new EntityToTOConverter<AttributeStore, AttributeDataTO>().convertEntityListToTOList(AttributeStoreDaoImpl.getInstance().getUserAttributes(user.getId()));
                List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
                int index = 0;
                for(AttributeMetadataTO attributeMetadataTOES:attributeMetaDataWEs){
                    if( attributeMetadataTOES.getAttributeName().equalsIgnoreCase(attributeDataRequestTO.getAttributeData().getAttributeName())){
                        index= attributeMetaDataWEs.indexOf(attributeMetadataTOES);
                        break;
                    }
                }
                AttributeMetadataTO attributeDataTO1=   attributeMetaDataWEs.get(index);
                if(attributeDataTO1.getAttributeValueModel().equals( "SINGLE_VALUED")){
                    for(AttributeDataTO attribute:userAttributeDataTOs){
                        if(attribute.getAttributeName().equals(attributeDataTO1.getAttributeName())){
                            throw new AuthException(null,errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(),errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                        }
                    }
                }
                boolean attributeExists = false;
                for (AttributeDataTO attributeDataTO : userAttributeDataTOs) {
                    if (attributeDataTO.getAttributeName().equalsIgnoreCase(attributeDataRequestTO.getAttributeData().getAttributeName())) {
                        attributeExists = true;
                        break;
                    }
                }
                if (!attributeExists) {
                    attributeDataRequestTO.getAttributeData().setIsDefault(true);
                }
                AttributeStore attributeStore = attributeStoreService.saveAttributeData(session,
                        attributeDataRequestTO.getAttributeData(), authenticationAttempt.getId(), user, true);
                if (attributeDataRequestTO.getAttributeData().getEvidence() != null) {
                    if (attributeDataRequestTO.getAttributeData().getEvidenceId() != null) {
                        if (attributeDataRequestTO.getCallStatus().equals(Constant.APPLICATION)) {
                            evidenceStoreService.storeEvidenceUsingAttributeStore(session, attributeStore,
                                    attributeDataRequestTO.getAttributeData().getEvidenceId());
                        }
                    } else {
                        if (attributeDataRequestTO.getAttributeData().getEvidence() != null) {
                            evidenceStoreService.saveEvidence(session, attributeStore,
                                    attributeDataRequestTO.getAttributeData().getEvidence());
                        }
                    }
                }
                attributeDataRequestTO.setStatus(Constant.PENDING);
                attributeDataRequestTO.setId(authenticationAttempt.getId());
                if (attributeDataRequestTO.getAttributeData().getSignTransactionId() != null) {
                    attributeDataRequestTO.getAttributeData().setSignTransactionId(null);
                }
                sessionFactoryUtil.closeSession(session);
                AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_ADDITION_SUCCESSFUL + AuditLogConstant.FOR_ATTRIBUTE + attributeDataRequestTO.getAttributeData().getAttributeName() + " = " + attributeDataRequestTO.getAttributeData().getAttributeValue() + AuditLogConstant.FOR_USER + user.getAccountId(), "ENTERPRISE", ActionType.ADD_ATTRIBUTE_TO_ACCOUNT, user.getAccountId(), IdType.ACCOUNT, "", "", user.getAccountId(), null);
            } catch (AuthException e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
            return attributeDataRequestTO;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public void approveRequestWithoutConsent(AttributeDataRequestTO attributeDataRequestTO) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " approveRequestWithoutConsent: start");
        Session session = sessionFactoryUtil.getSession();
        try {
            attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            User user = userService.getActiveUser(attributeDataRequestTO.getId());
            UserTO userTO = user.convertToTO();
            userTO.setAttributes(new EntityToTOConverter<AttributeStore, AttributeDataTO>().convertEntityListToTOList(AttributeStoreDaoImpl.getInstance().getUserActiveAttributes(user.getId())));
            UserIciciTO userIciciTO = userTOToUserIciciTO(userTO);
            AttributeDataTO attributeDataTO = attributeDataRequestTO.getAttributeData();
            boolean attributeExists = false;
            for (AttributeDataTO attributeDataTO1 : userTO.getAttributes()) {
                if (attributeDataTO1.getAttributeName().equalsIgnoreCase(attributeDataTO.getAttributeName())) {
                    attributeExists = true;
                    break;
                }
            }
            if (!attributeExists) {
                attributeDataTO.setIsDefault(true);
            }
            userIciciTO.getAttributeData().add(attributeDataTO);
            Map<String, String> attributeValueWithPlainValue = new HashMap<String, String>();
            List<AttributeStore> tempAttributeStores = new ArrayList<>();
            AccountWE accountWE = addAttributeToIdsIamCrypto(iamExtension, token, userIciciTO, attributeValueWithPlainValue);
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            int index = 0;
             for(AttributeMetadataTO attributeMetadataTOES:attributeMetaDataWEs){
                   if( attributeMetadataTOES.getAttributeName().equalsIgnoreCase(attributeDataRequestTO.getAttributeData().getAttributeName())){
                      index= attributeMetaDataWEs.indexOf(attributeMetadataTOES);
                      break;
                   }
             }
             AttributeMetadataTO attributeDataTO1=   attributeMetaDataWEs.get(index);
             List<AttributeDataTO> userAttributes= userTO.getAttributes();
             if(attributeDataTO1.getAttributeValueModel().equals( "SINGLE_VALUED")) {
                 for(AttributeDataTO attribute:userAttributes){
                     if(attribute.getAttributeName().equals(attributeDataTO1.getAttributeName())){
                         throw new AuthException(null,errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(),errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                     }
                 }
             }
             Map<String, Object> attributeValueWithKey = null;
             try {
                 String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                 attributeValueWithKey = iamExtension.registerAttributesOnCrypto(accountWE, attributeValueWithPlainValue, token, reqRefNum); //iamExtension.registerAttributesOnCrypto(accountWE, attributeValueWithPlainValue, token);
             } catch (IAMException e) {
                 logger.log(Level.ERROR, e.getMessage(), e);
                 attributeValueWithKey = new HashMap<>();
                 GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = new GenerateAttributeClaimSelfSignedTO();
                 generateAttributeClaimSelfSignedTO.setTransactionID("6c1f17951a9f89df82cf25980f41bcb32720dde11b9698039d31a151091a371f01c0163c034badef");
                 generateAttributeClaimSelfSignedTO.setKey("753c2355163b9bb7c71065d0b26427fd");
                 for (in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO : accountWE.getAttributes()) {
                     String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
                     attributeValueWithKey.put(plainValue, generateAttributeClaimSelfSignedTO);
                 }
             }

//            to set SignTransactionId after registering on crypto
            for (in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO : accountWE.getAttributes()) {
                String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
                if (attributeValueWithKey.containsKey(plainValue)) {
                    GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                            (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(plainValue);
                    attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                    attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                    if(attributeDataTO.getAttributeName().equals(attributeTO.getAttributeName())
                            && attributeDataTO.getAttributeValue().equalsIgnoreCase(attributeTO.getAttributeValue())
                    ){
                        attributeDataTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                    }
                }

            }
            AccountWE accountWE1= iamExtension.onboardAccount(accountWE, accountWE.getId(), true, token);
            if (attributeDataRequestTO.getAttributeData() != null) {
                AttributeStore attributeStore = attributeStoreService.saveAttributeData(session, attributeDataTO, user, false);
                tempAttributeStores.add(attributeStore);
                if (user.getKcId() != null && !tempAttributeStores.isEmpty()) {
                    EditUserRequest editUserRequest = new EditUserRequest();
                    editUserRequest.setUserKcId(user.getKcId());
                    List<CamAttribute> camAttributes = new ArrayList<>();
                    for (AttributeStore attributeStores : tempAttributeStores) {
                        CamAttribute camAttribute = new CamAttribute();
                        camAttribute.setCustomAttributeName(attributeStores.getAttributeName());
                        camAttribute.setCustomAttributeValue(attributeStores.getAttributeValue());
                        camAttributes.add(camAttribute);
                    }
                    editUserRequest.setAttributes(camAttributes);
                    CamUserFacade.editCamUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                }
                if (attributeDataRequestTO.getAttributeData().getEvidence() != null) {
                    if (attributeDataRequestTO.getAttributeData().getEvidenceId() != null) {
                        if (attributeDataRequestTO.getCallStatus().equals(Constant.APPLICATION)) {
                            evidenceStoreService.storeEvidenceUsingAttributeStore(session, attributeStore,
                                    attributeDataRequestTO.getAttributeData().getEvidenceId());
                        }
                    } else {
                        if (attributeDataRequestTO.getAttributeData().getEvidence() != null) {
                            evidenceStoreService.saveEvidence(session, attributeStore,
                                    attributeDataRequestTO.getAttributeData().getEvidence());
                        }
                    }
                }
                attributeDataRequestTO.setStatus(Constant.PENDING);
                if (attributeDataRequestTO.getAttributeData().getSignTransactionId() != null) {
                    attributeDataRequestTO.getAttributeData().setSignTransactionId(null);
                }
            } else {
                throw new AuthException(new Exception(), Constant.BAD_DATA_ERROR_CODE, "Attribute data is null.");
            }
            sessionFactoryUtil.closeSession(session);
            AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_ADDITION_SUCCESSFUL + AuditLogConstant.FOR_ATTRIBUTE + attributeDataRequestTO.getAttributeData().getAttributeName() + " = " + attributeDataRequestTO.getAttributeData().getAttributeValue() + AuditLogConstant.FOR_USER + accountWE.getId(), "ENTERPRISE", ActionType.ADD_ATTRIBUTE_TO_ACCOUNT, accountWE.getId(), IdType.ACCOUNT, "", "", accountWE.getId(), null);
        } catch (IAMException e) {
            session.getTransaction().rollback();
            throw new AuthException(e, Long.valueOf(e.getErrorCode()), e.getMessage());
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " approveRequestWithoutConsent: end");
        }
    }

    private UserIciciTO userTOToUserIciciTO(UserTO userTO) {
        UserIciciTO userIciciTO = new UserIciciTO();
        List<AttributeDataTO> searchAttributesList = new ArrayList<>();
        List<AttributeDataTO> attributeDataList = new ArrayList<>();
        for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
            attributeNameToUpperCase(attributeDataTO);
            AttributeDataTO attributeData = new AttributeDataTO();
            attributeData.setAttributeName(attributeDataTO.getAttributeName());
            attributeData.setAttributeValue(attributeDataTO.getAttributeValue());
            if (attributeDataTO.getIsDefault() != null) {
                attributeData.setIsDefault(attributeDataTO.getIsDefault());
            }
            if (Constant.USER_ID.equals(attributeDataTO.getAttributeName())) {
                if (attributeDataTO.getIsRegistered() != null) {
                    attributeData.setIsRegistered(attributeDataTO.getIsRegistered());
                }
                searchAttributesList.add(attributeData);
            } else {
                attributeDataList.add(attributeData);
            }
        }
        if(searchAttributesList.isEmpty() && !userTO.getAttributes().isEmpty()){
            for(AttributeDataTO attributeDataTO : userTO.getAttributes()){
                if (Constant.MOBILE_NO.equals(attributeDataTO.getAttributeName())) {
                    AttributeDataTO attributeData = new AttributeDataTO();
                    attributeData.setAttributeName(attributeDataTO.getAttributeName());
                    attributeData.setAttributeValue(attributeDataTO.getAttributeValue());
                    if (attributeDataTO.getIsDefault() != null) {
                        attributeData.setIsDefault(attributeDataTO.getIsDefault());
                    }
                    if (attributeDataTO.getIsRegistered() != null) {
                        attributeData.setIsRegistered(attributeDataTO.getIsRegistered());
                    }
                    searchAttributesList.add(attributeData);
                }
            }
        }
        userIciciTO.setSearchAttributes(searchAttributesList);
        userIciciTO.setAttributeData(attributeDataList);

        return userIciciTO;
    }

    private String applySecurityPolicy(String attributeValue, AttributeSecurityType attributeSecurityType) {
        String hashedAttributeValue;
        if (attributeSecurityType == AttributeSecurityType.SHA512) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData512(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        } else if (attributeSecurityType == AttributeSecurityType.SHA256) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData256(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        } else {
            hashedAttributeValue = attributeValue;
        }
        return hashedAttributeValue.toUpperCase();
    }

    private in.fortytwo42.enterprise.extension.tos.AttributeTO getAttributeFromAttributeData(AttributeDataTO attributeDataTO, List<AttributeMetadataTO> attributeMetaDataTOs) throws AuthException {
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
        in.fortytwo42.enterprise.extension.tos.AttributeTO attribute = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        attribute.setAttributeName(attributeDataTO.getAttributeName());
        attribute.setIsDefault(attributeDataTO.getIsDefault());
        String decryptedAttributeValue = attributeDataTO.getAttributeValue();
        attribute.setAttributeValue(applySecurityPolicy(decryptedAttributeValue, AttributeSecurityType.valueOf(securityType)));
        return attribute;
    }

    public AccountWE addAttributeToIdsIamCrypto(IAMExtensionV2 iamExtension, Token token, UserIciciTO userTO, Map<String, String> attributeValueWithPlainValue) throws AuthException {
        try {
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = null;
            for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                attributeNameToUpperCase(attributeDataTO);
                attributeDataTO.setIsDefault(true);
                attributeDataTO.setIsRegistered(true);
                attributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs);
                attributeValueWithPlainValue.put(attributeTO.getAttributeValue(), attributeDataTO.getAttributeValue());
            }
            AccountType accountType = AccountType.USER;
            AccountWE accountWE = iamExtension.createAccountIfNotExist(attributeTO.getAttributeName().toUpperCase(), attributeTO.getAttributeValue(), accountType);

            if (accountWE.getId() != null) {
                List<in.fortytwo42.enterprise.extension.tos.AttributeTO> identifiers = new ArrayList<>();
                for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                    in.fortytwo42.enterprise.extension.tos.AttributeTO accountAttributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs);
                    for (in.fortytwo42.enterprise.extension.tos.AttributeTO tempAccountAttributeTO : accountWE.getAttributes()) {
                        if (tempAccountAttributeTO.getAttributeName().equals(attributeDataTO.getAttributeName())
                                && tempAccountAttributeTO.getAttributeValue().equals(accountAttributeTO.getAttributeValue())) {
                            accountAttributeTO.setSignTransactionId(tempAccountAttributeTO.getSignTransactionId());
                            accountAttributeTO.setOperationStatus(tempAccountAttributeTO.getOperationStatus());
                            attributeDataTO.setSignTransactionId(tempAccountAttributeTO.getSignTransactionId());
                        }
                    }
                    identifiers.add(accountAttributeTO);
                }
                if (userTO.getAttributeData() != null) {
                    for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
                        attributeNameToUpperCase(attributeDataTO);
                        in.fortytwo42.enterprise.extension.tos.AttributeTO tempAttributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs);
                        attributeValueWithPlainValue.put(tempAttributeTO.getAttributeValue(), attributeDataTO.getAttributeValue());
                        tempAttributeTO.setIsDefault(attributeDataTO.getIsDefault());
                        for (in.fortytwo42.enterprise.extension.tos.AttributeTO tempAccountAttributeTO : accountWE.getAttributes()) {
                            if (tempAccountAttributeTO.getAttributeName().equals(attributeDataTO.getAttributeName())
                                    && tempAccountAttributeTO.getAttributeValue().equalsIgnoreCase(attributeDataTO.getAttributeValue())) {
                                tempAttributeTO.setOperationStatus(tempAccountAttributeTO.getOperationStatus());
                                tempAttributeTO.setSignTransactionId(tempAccountAttributeTO.getSignTransactionId());
                            }
                        }
                        identifiers.add(tempAttributeTO);
                    }
                }
                accountWE.setAttributes(identifiers);

                // account state
                accountWE.setAccountType(AccountType.valueOf(UserRole.valueOf(("USER")).getAccountType()));
                if (accountWE.getState() != null && (accountWE.getState().equals(Constant.PROVISIONED))) {
                    accountWE.setState("PARTIALLY_ACTIVE");
                }
            }
            return accountWE;
        } catch (IAMException e) {
            AuthException exception = iamExceptionConvertorUtil.convertToAuthException(e);
            logger.log(Level.ERROR, e.getMessage(), e);
            throw exception;
        }
    }

    @Override
    public AttributeDataRequestTO getAttributeAdditionStatus(Long authAttemptId) throws AuthException {
        AuthenticationAttemptHistory authenticationAttemptHistory;
        authenticationAttemptHistory = authAttemptService.getAuthAttemptHistoryBySourceId(authAttemptId);
        if (authenticationAttemptHistory == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ADDITION_ID_INVALID(),
                    errorConstant.getERROR_MESSAGE_ATTRIBUTE_ADDITION_ID_INVALID());
        }
        AttributeDataRequestTO attributeDataRequestTO = new AttributeDataRequestTO();
        attributeDataRequestTO.setId(authAttemptId);
        attributeDataRequestTO.setStatus(authenticationAttemptHistory.getAttemptStatus());
        AttributeDataTO attributeDataTO = attributeStoreService.getAttribute(authAttemptId);
        attributeNameToUpperCase(attributeDataTO);
        attributeDataTO.setSignTransactionId(null);
        attributeDataRequestTO.setAttributeData(attributeDataTO);
        return attributeDataRequestTO;
    }
    /*
     * private String getApprovalPermission(String requestType) { return
     * requestType.equals(Constant.ATTRIBUTE_ADDITION)?PermissionUtil.
     * APPROVE_ATTRIBUTE_ADDITION:PermissionUtil.APPROVE_ATTRIBUTE_VERIFICATION; }
     */

    @Override
    public CSVUploadTO uploadAttributes(String fileType, InputStream inputStream, String role,Long id, String fileName)
            throws AuthException {
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("YYYYMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("IST"));
        String dateFormatted = formatter.format(date);
        String requestId = UUID.randomUUID().toString();
        String filename = fileName + "_" + dateFormatted + "_" + requestId + ".csv";
        CSVUploadTO csvUploadTO = new CSVUploadTO();
        csvUploadTO.setRequestId(requestId);
        csvUploadTO.setFileName(filename);
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            CsvFactory.processCsv(fileType, inputStream, role,id, filename);
        });
        return csvUploadTO;
    }

    @Override
    public AttributeTO editAttribute(AttributeTO addAttributeTO, String actor,Long id, String role) throws AuthException {
        attributeNameToUpperCase(addAttributeTO);
        if (!permissionService.isPermissionValidForRole(PermissionUtil.ATTRIBUTE_UPDATION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(),
                    errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        User user = userService.getActiveUser(addAttributeTO.getId());
        List<Request> requests = requestDao.getRequests(RequestType.ATTRIBUTE_UPDATION,
                ApprovalStatus.CHECKER_APPROVAL_PENDING, ApprovalStatus.USER_APPROVAL_PENDING);
        if (requests != null && !requests.isEmpty()) {
            for (Request request : requests) {
                AttributeTO addAttributeTORequest = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);
                if (addAttributeTORequest.getId() == addAttributeTO.getId()) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ADDITION_ALREADY_PRESENT(),
                            errorConstant.getERROR_MESSAGE_ATTRIBUTE_ADDITION_ALREADY_PRESENT());
                }
            }
        }
        /*
         * String enterpriseAccountId =
         * Config.getInstance().getProperty(Constant.ENTERPRISE_ACCOUNT_ID); try {
         * IAMExtensionV2 iamExtension =
         * IAMUtil.getInstance().getIAMExtensionV2(enterpriseAccountId); AccountWE
         * accountWE = iamExtension.getAccount(addAttributeTO.getAttributeName(),
         * addAttributeTO.getAttributeValue()); if (accountWE.getId() == null) { throw
         * new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_PRESENT,
         * errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT); } } catch (IAMException
         * e) { logger.log(Level.ERROR, e.getMessage(), e); }
         */
        Session session = sessionFactoryUtil.getSession();
        try {
            addAttributeTO = requestService.createEditAttributeAdditionRequest(session, addAttributeTO, actor, id);
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return addAttributeTO;
    }

    /*
     * private String getApprovalPermission(String requestType) { return
     * requestType.equals(Constant.ATTRIBUTE_ADDITION)?PermissionUtil.
     * APPROVE_ATTRIBUTE_ADDITION:PermissionUtil.APPROVE_ATTRIBUTE_VERIFICATION; }
     */
    private void approveEditAttributeAddition(AttributeTO attributeTO, Request request) throws AuthException {
        if (attributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
            request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
        } else {
            Session session = sessionFactoryUtil.getSession();
            try {
                request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
                AttributeTO requestTO = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);
                attributeNameToUpperCase(requestTO);
                AttributeDataTO attribute = new AttributeDataTO();
                attribute.setAttributeName(requestTO.getAttributeName().toUpperCase());
                attribute.setAttributeValue(requestTO.getAttributeValue());
                attribute.setEvidence(requestTO.getEvidence());
                attribute.setAttributeType(requestTO.getAttributeType());
                String evidence = getHashForEvidence(requestTO.getEvidence());
                attribute.setEvidenceHash(evidence);
                AuthenticationAttempt authenticationAttempt = authAttemptFacade
                        .createEditAttributeAdditionRequest(session, requestTO.getId(), attribute);
                authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
                AttributeStore attributeStore = attributeStoreService.saveEditAttributeData(session, attribute,
                        authenticationAttempt.getId(), authenticationAttempt.getReceiverAccountId());
                if (requestTO.getEvidence() != null) {
                    evidenceStoreService.storeEvidence(session, attributeStore.getId(), requestTO.getEvidence());
                }
                sessionFactoryUtil.closeSession(session);
            } catch (AuthException e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
        }
    }

    @Override
    public UserTO requestAttribute(UserTO userTO, String actor, String role, Long id) throws AuthException {
        attributeNameToUpperCase(userTO);
        // TODO : Add role-permission validation
        RequestType requestType = RequestType.valueOf(userTO.getActionType());
        List<Request> requests = requestDao.getRequests(requestType, ApprovalStatus.CHECKER_APPROVAL_PENDING,
                ApprovalStatus.USER_APPROVAL_PENDING);
        if (requests != null && !requests.isEmpty()) {
            for (Request request : requests) {
                UserTO requestTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                if (requestTO.getUserIdentifier().equals(userTO.getUserIdentifier())) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_REQUEST_ALREADY_PRESENT(),
                            errorConstant.getERROR_MESSAGE_ATTRIBUTE_REQUEST_ALREADY_PRESENT());
                }
            }
        }
        /*
         * User user =
         * ProcessorFactory.getUserProcessor().getActiveUser(userTO.getId());
         * if(user.getUsername() !=null) { userTO.setUsername(user.getUsername()); }
         */
        Session session = sessionFactoryUtil.getSession();
        try {
            requestService.createRequestAttributeRequest(session, userTO, actor, id, requestType);
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return userTO;
    }

    @Override
    public EvidenceStoreTO uploadAttributeEvidence(InputStream inputStream, String role, String fileName)
            throws AuthException {
        try {
            int value = inputStream.available();
            if (value == 0) {
                throw new AuthException(null, errorConstant.getERROR_CODE_FILE_IS_EMPTY(),
                        errorConstant.getERROR_MESSAGE_FILE_IS_EMPTY());
            }
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        if (!FileUtil.getFileExtension(fileName).equalsIgnoreCase("png")) {
            if (!FileUtil.getFileExtension(fileName).equalsIgnoreCase("jpg")) {
                if (!FileUtil.getFileExtension(fileName).equalsIgnoreCase("jpeg")) {
                    if (!FileUtil.getFileExtension(fileName).equalsIgnoreCase("bmp")) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_FILE_NOT_SUPPORTED(),
                                errorConstant.getERROR_MESSAGE_NOT_SUPPORTED());
                    }
                }
            }
        }
        byte[] evidenceData = FileUtil.getByteArray(inputStream);
        Session session = sessionFactoryUtil.getSession();
        try {
            EvidenceStoreTO evidenceStoreTO = evidenceStoreService.storeEvidence(session, fileName, evidenceData);
            sessionFactoryUtil.closeSession(session);
            return evidenceStoreTO;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    private void approveCreateAttributeMasterAddition(AttributeTO attributeTO, Request request) throws AuthException {
        attributeNameToUpperCase(attributeTO);
        if (attributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
            request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
        } else {
            request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
            logger.log(Level.DEBUG, " addAttributeMetaData : start sourceId" + request.getRequestJSON());
            AttributeMetadataTO attributeMetadataTO = new Gson().fromJson(request.getRequestJSON(),
                    AttributeMetadataTO.class);
            attributeNameToUpperCase(attributeMetadataTO);
            attributeMasterService.approveCreateAddAttributeMetaDataRequest(attributeMetadataTO);
        }
    }

    private void approveEditAttributeMasterAddition(AttributeTO attributeTO, Request request) throws AuthException {
        attributeNameToUpperCase(attributeTO);
        if (attributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
            request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
        } else {
            request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
            AttributeMetadataTO attributeMetadataTO = new Gson().fromJson(request.getRequestJSON(),
                    AttributeMetadataTO.class);
            attributeNameToUpperCase(attributeMetadataTO);
            attributeMasterService.approveEditAddAttributeMetaDataRequest(attributeMetadataTO);
        }
    }

    private void approveDeleteAttributeMasterAddition(AttributeTO attributeTO, Request request) throws AuthException {
        attributeNameToUpperCase(attributeTO);
        if (attributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
            request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
        } else {
            Session session = sessionFactoryUtil.getSession();
            try {
                request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
                AttributeMetadataTO attributeMetadataTO = new Gson().fromJson(request.getRequestJSON(),
                        AttributeMetadataTO.class);
                attributeNameToUpperCase(attributeMetadataTO);
                attributeMasterService.approveDeleteAddAttributeMetaDataRequest(attributeMetadataTO);
                List<Request> requests = requestDao.getRequestsBySubType(RequestSubType.ATTRIBUTE_MASTER_UPDATION,
                        ApprovalStatus.CHECKER_APPROVAL_PENDING, ApprovalStatus.USER_APPROVAL_PENDING);
                if (requests != null && !requests.isEmpty()) {
                    for (Request req : requests) {
                        AttributeMetadataTO addAttributeTORequest = new Gson().fromJson(req.getRequestJSON(),
                                AttributeMetadataTO.class);
                        if (addAttributeTORequest.getAttributeName().equals(attributeMetadataTO.getAttributeName())) {
                            req.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
                            try {
                                requestService.updateRequest(session, req);
                            } catch (RequestNotFoundException e) {
                                throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(),
                                        errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
                            }
                        }
                    }
                }
                sessionFactoryUtil.closeSession(session);
            } catch (AuthException e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }

        }
    }

    @Override
    public PaginatedTO<in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO> getVerifiers(String role,
                                                                                                 String verifierType, String attributeName, int page) throws AuthException {
        int limit = Integer.parseInt(config.getProperty(Constant.LIMIT));
//        if (attributeName != null) {
//            return attributeStoreService.getVerifiers(verifierType, attributeName.toUpperCase(), page, limit);
//        }
//        return attributeStoreService.getVerifiers(verifierType, attributeName, page, limit);
        String stringVerifierTO;
        AttributeVerifierWTO verifierWTO;
        if((stringVerifierTO=attributeVerifierStore.get(ATTRIBUTE_VERIFIERS_CACHE))!=null){
            logger.log(Level.DEBUG,"getVerifiers : Retrieving from cache key : "+ATTRIBUTE_VERIFIERS_CACHE);
            verifierWTO=StringUtil.fromJson(stringVerifierTO,AttributeVerifierWTO.class);
            return verifierWTO.getAttributeVerifierTOs();
        }
        else{
            synchronized (attributeVerifierStore){
                if((stringVerifierTO=attributeVerifierStore.get(ATTRIBUTE_VERIFIERS_CACHE))!=null){
                    logger.log(Level.DEBUG,"getVerifiers : Retrieving from cache key : "+ATTRIBUTE_VERIFIERS_CACHE);
                    verifierWTO=StringUtil.fromJson(stringVerifierTO,AttributeVerifierWTO.class);
                    return verifierWTO.getAttributeVerifierTOs();
                }
                else{
                    logger.log(Level.DEBUG,"getVerifiers : Retrieving from api and adding to cache");
                    verifierWTO = new AttributeVerifierWTO(attributeStoreService.getVerifiers(verifierType, attributeName!=null?attributeName.toUpperCase():attributeName, page, limit));
                    attributeVerifierStore.putIfAbsent(ATTRIBUTE_VERIFIERS_CACHE,StringUtil.toJson(verifierWTO));
                    return StringUtil.fromJson(attributeVerifierStore.get(ATTRIBUTE_VERIFIERS_CACHE),AttributeVerifierWTO.class).getAttributeVerifierTOs();
                }
            }
        }
    }

    @Override
    public PaginatedTO<EnterpriseTO> getEnterprises(int page) throws AuthException {
        int limit = Integer.parseInt(config.getProperty(Constant.LIMIT));
        return attributeStoreService.getEnterprises(page, limit);
    }


    public AttributeDataRequestTO updateAttributeOfUser(AttributeTO attributeTO) throws AuthException, IAMException {
      User user= userService.getActiveUser(attributeTO.getAccountId());
        List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        List<AttributeDataTO> searchAttributes= new ArrayList<>();
        AttributeDataTO searchAttribute= new AttributeDataTO();
        int count=0;
        List<AttributeDataTO> userAttributeData = new EntityToTOConverter<AttributeStore, AttributeDataTO>().convertEntityListToTOList(AttributeStoreDaoImpl.getInstance().getUserAttributes(user.getId()));
        for(AttributeDataTO userAttribute:userAttributeData){
            for (AttributeMetadataTO attributeMetadataTO : attributeMetaDataTOs) {
                if (attributeMetadataTO.getIsUnique().equals(Boolean.TRUE) && attributeMetadataTO.getAttributeName().equals(userAttribute.getAttributeName()) && !userAttribute.getAttributeState().equals(AttributeState.DELETE.name())) {
                    searchAttribute.setAttributeName(userAttribute.getAttributeName());
                    searchAttribute.setAttributeValue(userAttribute.getAttributeValue());
                    count++;
                    break;
                }
            }
            if(count>0){
                break;
            }
        }
        searchAttributes.add(searchAttribute);
        AttributeDataTO attributeDataTO= new AttributeDataTO();
        attributeDataTO.setAttributeName(attributeTO.getAttributeName());
        attributeDataTO.setAttributeValue(attributeTO.getAttributeValue());
        if(attributeTO.getOldAttributeValue()!=null) {
            attributeDataTO.setOldattributeValue(attributeTO.getOldAttributeValue());
        }
        attributeDataTO.setAttributeAction(attributeTO.getAttributeAction());
        AttributeDataRequestTO attributeDataRequestTO=new AttributeDataRequestTO();
        attributeDataRequestTO.setSearchAttributes(searchAttributes);
        attributeDataRequestTO.setAttributeData(attributeDataTO);
        if(attributeTO.getIsConsentRequired()!=null) {
            attributeDataRequestTO.setIsConsentRequired(attributeTO.getIsConsentRequired());
        }
        return   sendAttributeEditRequest(attributeDataRequestTO);

    }

    @Override
    public AttributeDataRequestTO sendAttributeEditRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException, IAMException {
        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " sendAttributeEditRequest : start");
        //user-consent check
        //default flow should be that - user consent should be sent
        boolean isUserConsentRequired = attributeDataRequestTO.getIsConsentRequired() == null ? false : attributeDataRequestTO.getIsConsentRequired();
        if (Boolean.TRUE.equals(isUserConsentRequired)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), "isConsentRequired cannot be empty/true");
        }
        if (isUserConsentRequired) {
            attributeDataRequestTO = approveEditAttributeRequest(attributeDataRequestTO);
        } else {
            attributeDataRequestTO = approveUpdateOrDeleteAttribute(attributeDataRequestTO);
        }
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " sendAttributeEditRequest : end");
        return attributeDataRequestTO;
    }

    @Override
    public AttributeDataRequestTO sendAttributeUpdateRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException, IAMException {
        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " sendAttributeEditRequest : start");
        //user-consent check
        //default flow should be that - user consent should be sent
        boolean isUserConsentRequired = attributeDataRequestTO.getIsConsentRequired() != null && attributeDataRequestTO.getIsConsentRequired();
        if (Boolean.TRUE.equals(isUserConsentRequired)) {
            throw new AuthException(null, null, "isConsentRequired cannot be empty/true");
        }
        if (isUserConsentRequired) {
            attributeDataRequestTO = approveEditAttributeRequest(attributeDataRequestTO);
        } else {
            attributeDataRequestTO = approveUpdateOrDeleteAttribute(attributeDataRequestTO);
        }
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " sendAttributeEditRequest : end");
        return attributeDataRequestTO;
    }


    @Override
    public AttributeDataRequestTO sendAttributeUpdateOrDeleteRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException, IAMException {
        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " sendAttributeUpdateOrDeleteRequest : start");
        attributeDataRequestTO = approveUpdateOrDeleteAttribute(attributeDataRequestTO);
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " sendAttributeUpdateOrDeleteRequest : end");
        return attributeDataRequestTO;
    }


    @Override
    public AttributeDataRequestTO verifyAttribute(AttributeDataRequestTO attributeDataRequestTO) throws AuthException {
        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " sendAttributeEditRequest : start");
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        Token token;
        try {
            token = iamExtensionService.getToken(iamExtension);
            String accountId;
            AccountWE accountWE = iamExtensionService.searchAccount(attributeDataRequestTO.getSearchAttributes(), iamExtension, token);
            accountId = accountWE.getId();
            attributeDataRequestTO.setVerificationStatus(VerificationStatus.FAILED);
            for (in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO : accountWE.getAttributes()) {
                if (attributeTO.getAttributeName().equalsIgnoreCase(attributeDataRequestTO.getAttributeData().getAttributeName()) && attributeTO.getAttributeValue().equalsIgnoreCase(attributeDataRequestTO.getAttributeData().getAttributeValue())) {
                    attributeDataRequestTO.setVerificationStatus(VerificationStatus.SUCCESSFUL);
                    break;
                }
            }
            AuditLogUtil.sendAuditLog(AuditLogConstant.VERIFY_ATTRIBUTE_SUCCESS, AuditLogConstant.USER, ActionType.VERIFY, "", IdType.ACCOUNT, "", null, "", null);
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " sendAttributeEditRequest : end");
            return attributeDataRequestTO;
        } catch (IAMException e) {
            throw iamExceptionConvertorUtil.convertToAuthException(e);

        }
    }

    @Override
    public AttributeDataRequestTO verifyAttributeV4(AttributeDataRequestTO attributeDataRequestTO) throws AuthException{
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " verifyAttributeV4 : start");
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        Token token;
        try {
            ValidationUtilV3.validateSearchAttributes(attributeDataRequestTO.getSearchAttributes());
            token = iamExtensionService.getToken(iamExtension);
            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOs = attributeDataRequestTO.getSearchAttributes().stream()
                    .map(attributes -> {
                        in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
                        attributeTO.setAttributeName(attributes.getAttributeName());
                        attributeTO.setAttributeValue(attributes.getAttributeValue());
                        return attributeTO;
                    })
                    .collect(Collectors.toList());
            AccountWE accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);

            if (accountWE.getId() != null && !accountWE.getId().isEmpty()) {
                attributeDataRequestTO.getAttributes().forEach(attribute -> {
                    boolean valid = accountWE.getAttributes().stream().anyMatch(accAttribute ->
                            accAttribute.getStatus().equals(Constant.ACTIVE) &&
                                    accAttribute.getAttributeName().equalsIgnoreCase(attribute.getAttributeName()) &&
                                    accAttribute.getAttributeValue().equalsIgnoreCase(attribute.getAttributeValue())
                    );
                    attribute.setVerificationStatus(valid ? String.valueOf(VerificationStatus.SUCCESSFUL) :
                            String.valueOf(VerificationStatus.FAILED));
                });
            }
            else {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            AuditLogUtil.sendAuditLog(AuditLogConstant.VERIFY_ATTRIBUTE_SUCCESS, AuditLogConstant.USER, ActionType.VERIFY, "", IdType.ACCOUNT, "", null, "", null);
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " verifyAttributeV4 : end");
            return attributeDataRequestTO;
        }
        catch (IAMException e) {
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    public AttributeDataRequestTO approveEditAttributeRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException {
        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " approveEditAttributeRequest : start");

        Session session = IamThreadContext.getSessionWithoutTransaction();
        boolean isSessionCreatedHere = false;
        if(session == null || !session.isOpen()) {
            isSessionCreatedHere = true;
            session = sessionFactoryUtil.openSessionWithoutTransaction();
        }
        session.beginTransaction();

        try {
            AuthenticationAttempt authenticationAttempt = null;
            AttributeStore oldAttribute = null;
            try {

                String attributeValueDB = attributeDataRequestTO.getAttributeData().getAttributeValue();
                if (attributeDataRequestTO.getAttributeData().getOldattributeValue() != null) {
                    attributeValueDB = attributeDataRequestTO.getAttributeData().getOldattributeValue();
                    oldAttribute = attributeStoreService.getAttributeByAttributeNameAndValue(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValueDB, session);
                }

                attributeDataRequestTO.setCallStatus(Constant.APPLICATION);
                authenticationAttempt = authAttemptFacade.createAttributeEditRequest(session, attributeDataRequestTO);
                authAttemptHistoryHandler.logAuthAttemptHistoryData(authenticationAttempt);
            } catch (AuthException e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                throw new AuthException(e, Constant.BAD_DATA_ERROR_CODE, e.getMessage());
            }
            User user;
            try {
                user = userService.getUserByAccountId(authenticationAttempt.getReceiverAccountId(), session);
            } catch (UserNotFoundException e) {
                throw new AuthException(e, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            try {
                String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
                if (AttributeAction.UPDATE.equals(attributeDataRequestTO.getAttributeData().getAttributeAction())) {
                    if (attributeDataRequestTO.getAttributeData().getOldattributeValue() != null) {
                        attributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
                        //	AttributeStore oldAttribute = attributeStoreService.getAttributeByAttributeNameAndValue(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue);

                        if (oldAttribute.getIsDefault() != null && oldAttribute.getIsDefault().equals(Boolean.TRUE)) {
                            attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.TRUE);
                        } else {
                            attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.FALSE);
                        }

                        attributeStoreService.saveAttributeData(session, attributeDataRequestTO.getAttributeData(), authenticationAttempt.getId(), user, true);
                    }
                } else {
                    attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
                }
                AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeData(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue);
                if (AttributeAction.UPDATE.equals(attributeDataRequestTO.getAttributeData().getAttributeAction()) && attributeDataRequestTO.getAttributeData().getOldattributeValue() == null) {
                    attributeTobeUpdate.setIsDefault(attributeDataRequestTO.getAttributeData().getIsDefault());
                } else {
                    attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
                }
                attributeStoreService.update(session, attributeTobeUpdate);
                attributeDataRequestTO.setStatus(Constant.PENDING);
                attributeDataRequestTO.setId(authenticationAttempt.getId());
            } catch (AuthException e) {
                session.getTransaction().rollback();
                throw e;
            }
            session.getTransaction().commit();
            return attributeDataRequestTO;
        } finally {
            if (isSessionCreatedHere && session.isOpen()) {
            session.close();
        }
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " approveEditAttributeRequest : end");
        }
    }
    public boolean validateAccountPolicy(AccountPolicyWE accountPolicy, String attributeValue) {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " validateAccountPolicy : start");
        boolean isAccountPolicysatisfied = true;
        if (attributeValue.length() > accountPolicy.getMaxUserIdLength()) {
            isAccountPolicysatisfied = false;
        }
        if (!Pattern.matches(accountPolicy.getUserIdFRegex(), attributeValue)) {
            isAccountPolicysatisfied = false;
        }
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " validateAccountPolicy : end");
        return isAccountPolicysatisfied;
    }


    public AttributeDataRequestTO approveUpdateOrDeleteAttribute(AttributeDataRequestTO attributeDataRequestTO) throws AuthException, IAMException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " approveEditAttributeRequest : start");
        ValidationUtilV3.validateSearchAttributes(attributeDataRequestTO.getSearchAttributes());
        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        Session session = IamThreadContext.getSessionWithoutTransaction();
        boolean isSessionCreatedHere = false;
        if(session == null ||  !session.isOpen()) {
            isSessionCreatedHere = true;
            session = sessionFactoryUtil.openSessionWithoutTransaction();
        }
        session.beginTransaction();
        try {
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
//		AccountWE accountWE = iamExtensionService.searchAccount(attributeDataRequestTO.getSearchAttributes(), iamExtension, token);
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOs = new ArrayList<>();
            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeDataTOs = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : attributeDataRequestTO.getSearchAttributes()) {
                in.fortytwo42.enterprise.extension.tos.AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                attributeTOs.add(tempAttribute);
            }
            AccountWE accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            User user;
            try {
                user = userService.getUserByAccountId(accountWE.getId(), session);
            } catch (UserNotFoundException e) {
                throw new AuthException(e, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            try {
                String attributeValue = "";

                if (AttributeAction.UPDATE.equals(attributeDataRequestTO.getAttributeData().getAttributeAction())) {
                    if(attributeDataRequestTO.getAttributeData().getAttributeName().equals(Constant.USER_ID)) {
                        PaginatedTO<PolicyWE> policyWEPaginatedTO = iamExtensionService.getAllPolicies(1,10);
                        List<PolicyWE> policies = policyWEPaginatedTO.getList();
                        AccountPolicyWE userAccountPolicy = new AccountPolicyWE();
                        for (PolicyWE policy : policies) {
                            if (accountWE.getAccountType().equals(policy.getAccountType())) {
                                userAccountPolicy = policy.getAccountPolicy();
                                break;
                            }
                        }
                        boolean isAccountPolicySatisfied = validateAccountPolicy(userAccountPolicy, attributeDataRequestTO.getAttributeData().getAttributeValue());
                        if (!isAccountPolicySatisfied) {
                            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(), errorConstant.getACCOUNT_POLICY_FAILED());
                        }
                    }
                    in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();

                    if (attributeDataRequestTO.getAttributeData().getOldattributeValue() != null) {
                        List<AttributeMetadataTO> attributeMetaDatas = attributeMasterService.getAllAttributeMetaData();
                        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
                        attributeMetadataTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
                        int index = attributeMetaDatas.indexOf(attributeMetadataTO);
                        attributeMetadataTO = attributeMetaDatas.get(index);
                        attributeDataRequestTO.getAttributeData().setIsUnique(attributeMetadataTO.getIsUnique());
                        // update - attributeValue
                        attributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
                        AttributeStore oldAttribute = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId(), session);

                        AttributeStore newAttribute = null;
                        if (attributeMetadataTO.getIsUnique()) {
                            try {
                                newAttribute = attributeStoreService.getActiveAttributeWithUpperCase(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue());
                            } catch (AttributeNotFoundException e) {
                                logger.log(Level.DEBUG, " updated attribute value not found in the system");
                            }
                        } else {
                            try {
                                newAttribute = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue(), user.getId(), session);
                            } catch (AuthException e) {
                                logger.log(Level.DEBUG, " updated attribute value not found in the system");
                            }
                        }
                        if (newAttribute != null) {
                            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                        }

                        if (oldAttribute.getIsDefault() != null && oldAttribute.getIsDefault().equals(Boolean.TRUE)) {
                            attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.TRUE);
                        } else {
                            attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.FALSE);
                        }
                        attributeDataRequestTO.getAttributeData().setIsRegistered(oldAttribute.getIsRegistered());
                        attributeStoreService.saveAttributeData(session, attributeDataRequestTO.getAttributeData(), null, user, false);
                        attributeTO.setUpdatedAttributeValue(attributeDataRequestTO.getAttributeData().getAttributeValue());
                        attributeTO.setAttributeValue(attributeValue);
                        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
                        attributeTO.setIsDefault(attributeDataRequestTO.getAttributeData().getIsDefault());
                        boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));

                        if (enableCrypto) {
                            if (accountWE.getCryptoDID() == null || accountWE.getCryptoDID().isEmpty()) {
                                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), "Account not onboarded on crypto.");
                            }
                            Map<String, String> attributeValueWithPlainValue = new HashMap<String, String>();
                            attributeValueWithPlainValue.put(attributeTO.getUpdatedAttributeValue().toUpperCase(), attributeTO.getUpdatedAttributeValue().toUpperCase());
                            AccountWE updatedAttributeAccount = new AccountWE();
                            updatedAttributeAccount.setId(accountWE.getId());
                            in.fortytwo42.enterprise.extension.tos.AttributeTO updatedAttribute = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
                            updatedAttribute.setAttributeName(attributeTO.getAttributeName());
                            updatedAttribute.setAttributeValue(attributeTO.getUpdatedAttributeValue().toUpperCase());
                            updatedAttribute.setIsDefault(attributeTO.getIsDefault());
                            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> updatedAttributeList = new ArrayList<>();
                            updatedAttributeList.add(updatedAttribute);
                            updatedAttributeAccount.setAttributes(updatedAttributeList);
                            updatedAttributeAccount.setCryptoDID(accountWE.getCryptoDID());
                            Map<String, Object> attributeValueWithKey =null;
                            try {
                                String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                                attributeValueWithKey= iamExtension.registerAttributesOnCrypto(updatedAttributeAccount, attributeValueWithPlainValue, token, reqRefNum);
                            }catch (IAMException ex){
                                logger.log(Level.ERROR, ex.getMessage(), ex);
                                ex.printStackTrace();
                                if (ex.getMessage().equals("AVMC duplication detected.")) {
                                    attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
                                }
                                attributeValueWithKey = new HashMap<>();
                                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = new GenerateAttributeClaimSelfSignedTO();
                                generateAttributeClaimSelfSignedTO.setTransactionID("6c1f17951a9f89df82cf25980f41bcb32720dde11b9698039d31a151091a371f01c0163c034badef");
                                generateAttributeClaimSelfSignedTO.setKey("753c2355163b9bb7c71065d0b26427fd");
                                for (in.fortytwo42.enterprise.extension.tos.AttributeTO attributeToRec : updatedAttributeAccount.getAttributes()) {
                                    String plainValue = attributeValueWithPlainValue.get(attributeToRec.getAttributeValue());
                                    attributeValueWithKey.put(plainValue, generateAttributeClaimSelfSignedTO);
                                }
                            }
                            if (attributeValueWithKey.containsKey(updatedAttribute.getAttributeValue())) {
                                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                                        (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(updatedAttribute.getAttributeValue());
                                attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                                attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                                attributeTO.setOperationStatus(null);
                            }
                        }

                    } else {
                        // update - isDefault
                        attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
                        // to check if attribute is present or not
                        attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId());
                        attributeTO.setAttributeValue(attributeValue);
                        if (attributeDataRequestTO.getAttributeData().getIsDefault() != null) {
                            // TODO: update using CriteriaUpdate
                            attributeTO.setIsDefault(attributeDataRequestTO.getAttributeData().getIsDefault());
                            if (attributeDataRequestTO.getAttributeData().getIsDefault().equals(Boolean.TRUE)) {
                                int updatedRows = attributeStoreDao.updateDefaultFlagInAttribute(session, user.getId(), attributeDataRequestTO.getAttributeData().getAttributeName(), Boolean.FALSE);
                                if (updatedRows > 0) {
                                    logger.log(Level.DEBUG, "Rows with default attribute updated");
                                }
                            }
                        }
                        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
                    }
                    iamExtensionService.editAttribute(attributeTO, accountWE.getId());
                    if (user.getKcId() != null && !user.getKcId().isEmpty() && attributeDataRequestTO.getAttributeData().getOldattributeValue() != null) {
                        EditUserRequest editUserRequest = new EditUserRequest();
                        List<CamAttribute> camAttributeList = new ArrayList<>();
                        CamAttribute camAttribute = new CamAttribute(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue());
                        camAttributeList.add(camAttribute);
                        editUserRequest.setUserKcId(user.getKcId());
                        editUserRequest.setAttributes(camAttributeList);
                        boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                        if (!camStatus) {
                            logger.log(Level.ERROR, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " CAM Edit user failed for UPDATE Attribute.");
                        }

                        editUserRequest = new EditUserRequest();
                        camAttributeList = new ArrayList<>();
                        CamAttribute camAttributeDelete = new CamAttribute(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getOldattributeValue());
                        camAttributeList.add(camAttributeDelete);
                        editUserRequest.setUserKcId(user.getKcId());
                        editUserRequest.setAttributeAction(AttributeAction.DELETE);
                        editUserRequest.setAttributes(camAttributeList);
                        camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                        if (!camStatus) {
                            logger.log(Level.ERROR, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " CAM Edit user failed for UPDATE Attribute.");
                        }
                    }
                } else {
                    attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
                    in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
//				attributeTO.setUpdatedAttributeValue(attributeValue);
                    attributeTO.setAttributeValue(attributeValue);
                    attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
                    iamExtensionService.deleteAttribute(attributeTO, accountWE.getId(), accountWE.getCryptoDID());

                    if (user.getKcId() != null && !user.getKcId().isEmpty()) {
                        EditUserRequest editUserRequest = new EditUserRequest();
                        List<CamAttribute> camAttributeList = new ArrayList<>();
                        CamAttribute camAttribute = new CamAttribute(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue);
                        camAttributeList.add(camAttribute);
                        editUserRequest.setUserKcId(user.getKcId());
                        editUserRequest.setAttributeAction(AttributeAction.DELETE);
                        editUserRequest.setAttributes(camAttributeList);
                        boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                        if (!camStatus) {
                            logger.log(Level.ERROR, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " CAM Edit user failed for Delete Attribute.");
                        }
                    }
                }
                AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId(), session);
                if (AttributeAction.UPDATE.equals(attributeDataRequestTO.getAttributeData().getAttributeAction()) && attributeDataRequestTO.getAttributeData().getOldattributeValue() == null) {
                    attributeTobeUpdate.setIsDefault(attributeDataRequestTO.getAttributeData().getIsDefault());
                } else {
                    attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
                }
                attributeStoreService.update(session, attributeTobeUpdate);
                attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
                attributeDataRequestTO.setId(null);
                session.getTransaction().commit();
                AuditLogUtil.sendAuditLog("Attribute for user " + attributeDataRequestTO.getSearchAttributes().get(0).getAttributeValue() + " " + attributeDataRequestTO.getAttributeData().getAttributeAction().name() + " successful", "USER", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", null, accountWE.getId(), null);

            } catch (AuthException e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                if (isSessionCreatedHere && session.isOpen()) {
                    session.close();
                }
            }
            return attributeDataRequestTO;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public String readSampleAttributeUpdateCsvFile(String fileName) {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " readSampleAttributeUpdateCsvFile : start");
        fileName = fileName != null ? fileName : "bulk_attribute_update.csv";
        String content = FileUtil.getSampleUserOnboardCsv(fileName);
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " readSampleAttributeUpdateCsvFile : end");
        return content;
    }

    @Override
    public String downloadAttributeUpdateStatus(String fileName, String role) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " downloadAttributeUpdateStatus : start");
        String content = new FileDownloader().downloadCSVStatusFile(fileName, role);
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " downloadAttributeUpdateStatus : end");
        return content;
    }

    private in.fortytwo42.enterprise.extension.tos.AttributeTO getAttributeFromAttributeData(
            AttributeDataTO attributeDataTO,
            List<AttributeMetadataTO> attributeMetaDataTOs,
            boolean isEncrypted) throws AuthException {
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
            } catch (Exception e) {
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

    private void attributeNameToUpperCase(UserTO userTO) {
        for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
            attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
        }
        if (userTO.getSearchAttributes() != null) {
            for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
            }
        }
    }

    private void attributeNameToUpperCase(AttributeDataTO attributeDataTO) {
        if (attributeDataTO.getAttributeName() != null) {
            attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
        }
    }

    private void attributeNameToUpperCase(AttributeTO attributeTO) {
        if (attributeTO.getAttributeName() != null) {
            attributeTO.setAttributeName(attributeTO.getAttributeName().toUpperCase());
        }
    }

    private void attributeNameToUpperCase(AttributeMetadataTO attributeMetadataTO) {
        attributeMetadataTO.setAttributeName(attributeMetadataTO.getAttributeName().toUpperCase());
    }
    @Override
    public AttributeTO addAttributeupdateRequest(AttributeTO addAttributeTO, String actor, String role,Long id) throws AuthException, AttributeNotFoundException {
        if (!permissionService.isPermissionValidForRole(PermissionUtil.ATTRIBUTE_UPDATION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(),
                    errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }

        User user =userService.getActiveUser(addAttributeTO.getAccountId());
        List<Request> requests = requestDao.getRequests(RequestType.ATTRIBUTE_UPDATION,
                ApprovalStatus.CHECKER_APPROVAL_PENDING, ApprovalStatus.USER_APPROVAL_PENDING);
        if (requests != null && !requests.isEmpty()) {
            for (Request request : requests) {
                AttributeTO addAttributeTORequest = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);
                if (Objects.equals(addAttributeTORequest.getId(), addAttributeTO.getId())) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_UPDATION_ALREADY_PRESENT(),
                            errorConstant.getERROR_MESSAGE_ATTRIBUTE_UPDATE_REQUEST_ALREADY_PRESENT1());
                }
            }
        }
        String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
        List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        try {
            IAMExtensionV2 iamExtension1 = iamUtil.getIAMExtensionV2(enterpriseAccountId);
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            attributeMetadataTO.setAttributeName(addAttributeTO.getAttributeName());
            int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            if (index < 0) {
                attributeMetadataTO.setAttributeName("OTHERS");
                index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
            }
            attributeMetadataTO = attributeMetaDataTOs.get(index);
            //handling attribute addition unique case
            if(addAttributeTO.getAttributeAction().equals(AttributeAction.UPDATE)) {
                if (attributeMetadataTO.getIsUnique() != null && attributeMetadataTO.getIsUnique()) {
                    AccountWE accountWE = iamExtension1.getAccount(addAttributeTO.getAttributeName(),
                            addAttributeTO.getAttributeValue());
                    if (accountWE.getId() != null) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(),
                                addAttributeTO.getAttributeName() + errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                    }
                }
            }
            //handling non-unique attribute already present in users attribute

            List<AttributeStore> attributeDataTO = AttributeStoreDaoImpl.getInstance().getUserAttributes(user.getId());
            if (addAttributeTO.getAttributeAction().equals(AttributeAction.UPDATE)){
                for (AttributeStore attribute : attributeDataTO) {
                    if (!attribute.getAttributeState().equals(AttributeState.DELETE) && attribute.getAttributeName().equalsIgnoreCase(addAttributeTO.getAttributeName()) && attribute.getAttributeValue().equalsIgnoreCase(addAttributeTO.getAttributeValue())) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(),
                                addAttributeTO.getAttributeName() + errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                    }
                }
            }
            if (addAttributeTO.getAttributeAction().equals(AttributeAction.DELETE)){
                boolean attributePresent=false;
                for (AttributeStore attribute : attributeDataTO) {
                    if (attribute.getAttributeName().equalsIgnoreCase(addAttributeTO.getAttributeName()) && attribute.getAttributeValue().equalsIgnoreCase(addAttributeTO.getAttributeValue()) && !attribute.getAttributeState().equals(AttributeState.DELETE)) {
                        attributePresent=true;
                    }
                }
                if(!attributePresent){
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_PRESENT(),
                            addAttributeTO.getAttributeName() + errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
                }
            }

        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        Session session = sessionFactoryUtil.getSession();
        try {
            addAttributeTO = requestService.createAttributeUpdationRequest(session, addAttributeTO, actor,id);
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return addAttributeTO;
    }

    @Override
    public AttributeDataRequestTO attributeEditAndTakeOver(AttributeDataRequestTO attributeDataRequestTO) throws AuthException{
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " attributeEditAndTakeOver : start");
        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        boolean isUserConsentRequired = attributeDataRequestTO.getIsConsentRequired() != null && attributeDataRequestTO.getIsConsentRequired();
        if (Boolean.TRUE.equals(isUserConsentRequired)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_CONSENT_REQUIRED_NOT_ALLOWED(), errorConstant.getERROR_MESSAGE_CONSENT_REQUIRED_NOT_ALLOWED());
        }
        Session session = IamThreadContext.getSessionWithoutTransaction();
        try {
            attributeDataRequestTO = updateAndTakeOverAttribute(attributeDataRequestTO, session);
        }
        catch (AuthException e) {
            throw e;
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e);
            throw e;
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " attributeEditAndTakeOver : end");
        }
        return attributeDataRequestTO;
    }

    private List<AttributeDeleteType> createDeleteProcess() {
        List<AttributeDeleteType> deleterProcess = new ArrayList<>();
        deleterProcess.add(AttributeDeleteType.deleteFromadapter);
        deleterProcess.add(AttributeDeleteType.deleteFromCam);
        deleterProcess.add(AttributeDeleteType.deleteFromIds);

        return deleterProcess;
    }

    public boolean attributeExistOnIds(AccountWE accountWE, String attributeValue, String attributeName) {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " attributeExistOnIds : start");
        List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOList = accountWE.getAttributes();
        boolean isExist = false;
        for (in.fortytwo42.enterprise.extension.tos.AttributeTO attribute : attributeTOList) {
            if (attribute.getAttributeName().equals(attributeName) && attribute.getAttributeValue().equalsIgnoreCase(attributeValue) && attribute.getStatus().equals(Constant.ACTIVE)) {
                isExist = true;
                break;
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " attributeExistOnIds : end");
        return isExist;
    }

    private AttributeDataRequestTO updateAndTakeOverAttribute(AttributeDataRequestTO attributeDataRequestTO, Session session) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " updateAndTakeOverAttribute : start");
        ValidationUtilV3.validateSearchAttributes(attributeDataRequestTO.getSearchAttributes());
        attributeNameToUpperCase(attributeDataRequestTO.getAttributeData());
        try {
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            String attributeName = attributeDataRequestTO.getAttributeData().getAttributeName();
            Token token = iamExtensionService.getToken(iamExtension);
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            attributeMetadataTO.setAttributeName(attributeName);
            int index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
            attributeMetadataTO = attributeMetaDataWEs.get(index);
            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOs = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : attributeDataRequestTO.getSearchAttributes()) {
                in.fortytwo42.enterprise.extension.tos.AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                attributeTOs.add(tempAttribute);
            }
            AccountWE accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            User user = userService.getUserByAccountId(accountWE.getId());
            if (AttributeAction.UPDATE.equals(attributeDataRequestTO.getAttributeData().getAttributeAction())) {
                List<AttributeUpdateType> updateProcess = createUpdateProcess();
                boolean isProcessCalledForUpdate = false;
                String oldattributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
                if (!attributeExistOnIds(accountWE, oldattributeValue, attributeDataRequestTO.getAttributeData().getAttributeName())){
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
                }
                if(!oldattributeValue.equalsIgnoreCase(attributeDataRequestTO.getAttributeData().getAttributeValue())) {
                    if (attributeMetadataTO.getIsUnique() != null && Boolean.TRUE.equals(attributeMetadataTO.getIsUnique())) {
                        AccountWE newAccountWE;
                        User newUser = null;
                        List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOs1 = new ArrayList<>();
                        AttributeDataTO attributeDataTO = attributeDataRequestTO.getAttributeData();
                        in.fortytwo42.enterprise.extension.tos.AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                        attributeTOs1.add(tempAttribute);
                        newAccountWE = iamExtension.getAccountByAttributes(attributeTOs1, token);
                        if (newAccountWE != null && newAccountWE.getId() != null && !newAccountWE.getId().isEmpty()) {
                            newUser = userService.getUserByAccountId(newAccountWE.getId());
                        }
                        if (newUser != null) {
                            List<AttributeDeleteType> deleteProcess = createDeleteProcess();
                            for (AttributeDeleteType type : deleteProcess) {
                                AttributeValidater attributeDeleter = DeleteAttributeFactory.buildAttributeDeleter(type);
                                if (!attributeDeleter.validate(attributeDataRequestTO, newUser, newAccountWE)) {
                                    long startTimeProcess = System.currentTimeMillis();
                                    attributeDeleter.process(attributeDataRequestTO, newUser, session, newAccountWE);
                                    long endTimeProcess = System.currentTimeMillis();
                                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->AttributeStoreFacadeImpl -> " + type + " -> process |Epoch:" + endTimeProcess);
                                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "-> AttributeStoreFacadeImpl -> " + type + " -> process -> DIFF " + (endTimeProcess - startTimeProcess));
                                }
                            }
                        }
                    }
                }
                for (AttributeUpdateType updateType : updateProcess) {
                    AttributeUpdater attributeUpdater = UpdateAttributeFactory.buildAttributeUpdater(updateType);
                    if (!attributeUpdater.validate(attributeDataRequestTO, user, accountWE)) {
                        isProcessCalledForUpdate = true;
                        long startTimeProcess = System.currentTimeMillis();
                        attributeUpdater.process(attributeDataRequestTO, user, session, accountWE);
                        long endTimeProcess = System.currentTimeMillis();
                        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserFacadeImpl -> " + updateType + " -> process |Epoch:" + endTimeProcess);
                        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "-> UserFacadeImpl -> " + updateType + " -> process -> DIFF " + (endTimeProcess - startTimeProcess));
                    }
                }
                if (isProcessCalledForUpdate) {
                    attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
                    attributeDataRequestTO.setId(null);
                } else {
                    if(!oldattributeValue.equalsIgnoreCase(attributeDataRequestTO.getAttributeData().getAttributeValue())) {
                        throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
                    } else {
                        attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
                        attributeDataRequestTO.setId(null);
                    }
                }
                AuditLogUtil.sendAuditLog("Attribute for user " + attributeDataRequestTO.getSearchAttributes().get(0).getAttributeValue() + " " + attributeDataRequestTO.getAttributeData().getAttributeAction().name() + " successful", "USER", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", null, accountWE.getId(), null);


//                AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
//                String attributeName = attributeDataRequestTO.getAttributeData().getAttributeName();
//                String oldAttributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
//                String newAttributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
//                attributeMetadataTO.setAttributeName(attributeName);
//                int index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
//                attributeMetadataTO = attributeMetaDataWEs.get(index);
//                validateOldAttributeValue(attributeName, oldAttributeValue, user.getId());
//                validateNewAttributeValue(attributeName, newAttributeValue, user.getId());
//                if (attributeMetadataTO .getIsUnique() != null && Boolean.TRUE.equals(attributeMetadataTO.getIsUnique())) {
//                    deleteAttribute(attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token, session);
//                }
//                updateAttribute(attributeDataRequestTO, attributeMetaDataWEs, user, accountWE, iamExtension, token, session);
//                attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
//                attributeDataRequestTO.setId(null);
//                AuditLogUtil.sendAuditLog("Attribute for user " + attributeDataRequestTO.getSearchAttributes().get(0).getAttributeValue()
//                                          + " " + attributeDataRequestTO.getAttributeData().getAttributeAction().name()+ " successful", "USER", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", "", accountWE.getId(), null);
            } else if (AttributeAction.DELETE.equals(attributeDataRequestTO.getAttributeData().getAttributeAction())) {
                validateAttributeForDelete(attributeDataRequestTO, user, accountWE);
                if (attributeDataRequestTO.getAttributeData().getAttributeName().equals(Constant.MOBILE_NO)) {
                    boolean isMultiBinding = unbindApplicationToUser(attributeDataRequestTO, user, iamExtension, accountWE);
                    if (isMultiBinding) {
                        attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
                        return attributeDataRequestTO;
                    }
                } else if (attributeDataRequestTO.getAttributeData().getAttributeName().equals(Constant.USER_ID)) {
                    unbindApplicationToUser(attributeDataRequestTO, user, iamExtension, accountWE);
                }
                List<AttributeDeleteType> deleteProcess = createDeleteProcess();
                boolean isProcessCalled = false;
                for (AttributeDeleteType type : deleteProcess) {
                    AttributeValidater attributeDeleter = DeleteAttributeFactory.buildAttributeDeleter(type);
                    if (!attributeDeleter.validate(attributeDataRequestTO, user, accountWE)) {
                        isProcessCalled = true;
                        long startTimeProcess = System.currentTimeMillis();
                        attributeDeleter.process(attributeDataRequestTO, user, session, accountWE);
                        long endTimeProcess = System.currentTimeMillis();
                        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->AttributeStoreFacadeImpl -> " + type + " -> process |Epoch:" + endTimeProcess);
                        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "-> AttributeStoreFacadeImpl -> " + type + " -> process -> DIFF " + (endTimeProcess - startTimeProcess));
                    }
                }
                if (isProcessCalled) {
                    attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
                    attributeDataRequestTO.setId(null);
                } else {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
                }
                AuditLogUtil.sendAuditLog(
                        "Attribute for user " + attributeDataRequestTO.getSearchAttributes().get(0).getAttributeValue() + " " + attributeDataRequestTO.getAttributeData().getAttributeAction()
                                .name() + " successful", "USER", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", null, accountWE.getId(), null);


                /*String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
                in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
                //attributeTO.setUpdatedAttributeValue(attributeValue);
                attributeTO.setAttributeValue(attributeValue);
                attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
                iamExtensionService.deleteAttribute(attributeTO, accountWE.getId(), accountWE.getCryptoDID());

                if (user.getKcId() != null && !user.getKcId().isEmpty()) {
                    EditUserRequest editUserRequest = new EditUserRequest();
                    List<CamAttribute> camAttributeList = new ArrayList<>();
                    CamAttribute camAttribute = new CamAttribute(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue);
                    camAttributeList.add(camAttribute);
                    editUserRequest.setUserKcId(user.getKcId());
                    editUserRequest.setAttributeAction(AttributeAction.DELETE);
                    editUserRequest.setAttributes(camAttributeList);
                    boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                    if (!camStatus) {
                        logger.log(Level.ERROR, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " CAM Edit user failed for Delete Attribute.");
                    }
                }
                AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId());
                attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
                attributeStoreService.update(session, attributeTobeUpdate);
                attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
                attributeDataRequestTO.setId(null);
                AuditLogUtil.sendAuditLog("Attribute for user " + attributeDataRequestTO.getSearchAttributes().get(0).getAttributeValue() + " " + attributeDataRequestTO.getAttributeData().getAttributeAction().name() + " successful", "USER", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", null, accountWE.getId(), null);
            */
            }
            return attributeDataRequestTO;
        }
        catch (UserNotFoundException e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        catch (IAMException e){
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    private void validateAttributeForDelete(AttributeDataRequestTO attributeDataRequestTO, User user, AccountWE accountWE) throws AuthException {
        if(AttributeDeleterFromIds.getInstance().validate(attributeDataRequestTO, user, accountWE)){
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
    }

    private boolean unbindApplicationToUser(AttributeDataRequestTO attributeDataRequestTO, User user, IAMExtensionV2 iamExtension, AccountWE accountWE) throws AuthException {
        Session session = sessionFactoryUtil.openSessionWithoutTransaction();
        boolean isMultiBinding = false;
        try {
            if (attributeDataRequestTO.isxDimfaUnbind()) {
                session.beginTransaction();
                List<ApplicationTO> userApplicationRels = userApplicationRelService.getApplicationRelsForUser(user.getId());
                if (userApplicationRels.size() > 1) {
                    isMultiBinding = true;
                    Application application = applicationService.getNonADApplicationByApplicationId(attributeDataRequestTO.getApplicationId(), session);
                    Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
                    List<Service> service = application.getServices();
                    UserApplicationServiceRel userApplicationRel = userApplicationRelService.getUserApplicationRel(user, application, service.get(0), session);
                    if (userApplicationRel == null) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_USER_APPLICATION_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND());
                    }
                    userApplicationRelService.updateUserApplicationRel(session, userApplicationRel, BindingStatus.INACTIVE);
                    iamExtension.forceTimeoutApprovalAttemptsByLookupId(token, user.getAccountId(), application.getApplicationAccountId() + "|" + service.get(0).getServiceName());
                    if (application.getKcId() != null && user.getKcId() != null) {
                        camUserFacade.bindUserToApplication(Config.getInstance().getProperty(Constant.CAM_REALM), application.getKcId(), user.getKcId(), application.getApplicationId(), Constant.UNBIND_OPERATION);
                    }
                    iamExtension.unbindConsumerApplication(token, accountWE.getId());
                    AccountWE camAccountForKcId = new AccountWE();
                    camAccountForKcId.setId(accountWE.getId());
                    camAccountForKcId.setKcId(Constant.UNBIND_OPERATION);
                    iamExtension.editAccount(camAccountForKcId, accountWE.getId(), token);
                    user = session.get(User.class, user.getId());
//                    user.setKcId(null);
                    userService.updateUser(session, user);
                    session.getTransaction().commit();
                }
            }
            return isMultiBinding;
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw new AuthException(e, e.getErrorCode(), e.getMessage());
        } catch (IAMException e) {
            session.getTransaction().rollback();
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private List<AttributeUpdateType> createUpdateProcess() {
        List<AttributeUpdateType> updateProcess = new ArrayList<>();
        updateProcess.add(AttributeUpdateType.updateFromadAdapter);
        updateProcess.add(AttributeUpdateType.updateFromCam);
        updateProcess.add(AttributeUpdateType.UpdateFromIds);
        return updateProcess;
    }

    private void validateNewAttributeValue(String attributeName, String newAttributeValue, Long userId) {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " validateNewAttributeValue : start");
        try {
            attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName, newAttributeValue, userId);
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(), newAttributeValue+Constant._COLON+errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
        }
        catch (AuthException e) {
            logger.log(Level.DEBUG, e.getMessage(), e);
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " validateNewAttributeValue : end");
        }
    }

    private void validateOldAttributeValue(String attributeName, String oldAttributeValue, Long userId) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " validateOldAttributeValue : start");
        attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName, oldAttributeValue, userId);
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " validateOldAttributeValue : end");
    }

    private void updateAttribute(AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDatas, User user, AccountWE accountWE, IAMExtensionV2 iamExtension, Token token, Session session) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " updateAttribute : start");
        in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        int index = attributeMetaDatas.indexOf(attributeMetadataTO);
        attributeMetadataTO = attributeMetaDatas.get(index);
        attributeDataRequestTO.getAttributeData().setIsUnique(attributeMetadataTO.getIsUnique());
        String attributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
        AttributeStore oldAttribute = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId());
        if (oldAttribute.getIsDefault() != null && oldAttribute.getIsDefault().equals(Boolean.TRUE)) {
            attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.TRUE);
        }
        else {
            attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.FALSE);
        }
        attributeStoreService.saveAttributeData(session, attributeDataRequestTO.getAttributeData(), null, user, false);
        attributeTO.setUpdatedAttributeValue(attributeDataRequestTO.getAttributeData().getAttributeValue());
        attributeTO.setAttributeValue(attributeValue);
        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        attributeTO.setIsDefault(attributeDataRequestTO.getAttributeData().getIsDefault());
        boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
        try {
            if (enableCrypto) {
                if (accountWE.getCryptoDID() == null || accountWE.getCryptoDID().isEmpty()) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), "Account not onboarded on crypto.");
                }

                Map<String, String> attributeValueWithPlainValue = new HashMap<>();
                attributeValueWithPlainValue.put(attributeTO.getUpdatedAttributeValue(), attributeTO.getUpdatedAttributeValue());
                AccountWE updatedAttributeAccount = new AccountWE();
                updatedAttributeAccount.setId(accountWE.getId());
                in.fortytwo42.enterprise.extension.tos.AttributeTO updatedAttribute = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
                updatedAttribute.setAttributeName(attributeTO.getAttributeName());
                updatedAttribute.setAttributeValue(attributeTO.getUpdatedAttributeValue().toUpperCase());
                updatedAttribute.setIsDefault(attributeTO.getIsDefault());
                List<in.fortytwo42.enterprise.extension.tos.AttributeTO> updatedAttributeList = new ArrayList<>();
                updatedAttributeList.add(updatedAttribute);
                updatedAttributeAccount.setAttributes(updatedAttributeList);
                updatedAttributeAccount.setCryptoDID(accountWE.getCryptoDID());
                String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                Map<String, Object> attributeValueWithKey = iamExtension.registerAttributesOnCrypto(updatedAttributeAccount, attributeValueWithPlainValue, token, reqRefNum);
                if (attributeValueWithKey.containsKey(updatedAttribute.getAttributeValue())) {
                    GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(updatedAttribute.getAttributeValue());
                    attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                    attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                    attributeTO.setOperationStatus(null);
                }
            }
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
        }
        iamExtensionService.editAttribute(attributeTO, accountWE.getId());
        if (user.getKcId() != null && !user.getKcId().isEmpty() && attributeDataRequestTO.getAttributeData().getOldattributeValue() != null) {
            editCamUsersAttribute("UPDATE", attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue(), user);
            editCamUsersAttribute("DELETE", attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getOldattributeValue(), user);
        }
        AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue,
                user.getId());
        attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
        attributeStoreService.update(session, attributeTobeUpdate);
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " updateAttribute : end");
    }

    private void editCamUsersAttribute(String action, String attributeName, String attributeValue, User user) {
        EditUserRequest editUserRequest = new EditUserRequest();
        List<CamAttribute> camAttributeList = new ArrayList<>();
        CamAttribute camAttribute = new CamAttribute(attributeName, attributeValue);
        camAttributeList.add(camAttribute);
        editUserRequest.setUserKcId(user.getKcId());
        editUserRequest.setAttributes(camAttributeList);
        if (action != null && !action.isEmpty() && "DELETE".equals(action)) {
            editUserRequest.setAttributeAction(AttributeAction.DELETE);
        }
        boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
        if (!camStatus) {
            logger.log(Level.ERROR, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " CAM Edit user failed for " + action + " Attribute.");
        }
    }

    private void deleteAttribute(AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs, IAMExtensionV2 iamExtension, Token token, Session session) throws AuthException, IAMException , UserNotFoundException{
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " deleteAttribute : start");
        String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
        in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        attributeTO.setAttributeValue(attributeValue);
        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        List<in.fortytwo42.enterprise.extension.tos.AttributeTO> attributeTOs = new ArrayList<>();
        AttributeDataTO attributeDataTO = attributeDataRequestTO.getAttributeData();
        in.fortytwo42.enterprise.extension.tos.AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
        attributeTOs.add(tempAttribute);
        AccountWE accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
        if (accountWE != null && accountWE.getId() != null && !accountWE.getId().isEmpty()) {
            User user = userService.getUserByAccountId(accountWE.getId());
            AttributeStore attributeTobeUpdated = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId());
            iamExtensionService.deleteAttribute(attributeTO, accountWE.getId(), accountWE.getCryptoDID());
            if (user.getKcId() != null && !user.getKcId().isEmpty()) {
                EditUserRequest editUserRequest = new EditUserRequest();
                List<CamAttribute> camAttributeList = new ArrayList<>();
                CamAttribute camAttribute = new CamAttribute(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue);
                camAttributeList.add(camAttribute);
                editUserRequest.setUserKcId(user.getKcId());
                editUserRequest.setAttributeAction(AttributeAction.DELETE);
                editUserRequest.setAttributes(camAttributeList);
                boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                if (!camStatus) {
                    logger.log(Level.ERROR, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " CAM Edit user failed for DELETE Attribute.");
                }
            }
            attributeTobeUpdated.setAttributeState(AttributeState.DELETE);
            attributeStoreService.update(session, attributeTobeUpdated);
        }
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_FACADE_IMPL_LOG + " deleteAttribute : start");
    }

}
