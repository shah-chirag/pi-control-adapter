
package in.fortytwo42.adapter.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.fortytwo42.daos.dao.TransactionReportsDaoIntfImpl;
import in.fortytwo42.tos.transferobj.AuthenticationAttemptHistoryTO;
import in.fortytwo42.tos.transferobj.TransactionReportRequestTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.ApprovalAttemptPollerTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.DateUtil;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.AuthenticationAttemptDaoIntf;
import in.fortytwo42.daos.dao.AuthenticationAttemptHistoryDaoImpl;
import in.fortytwo42.daos.dao.AuthenticationAttemptHistoryDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.ServiceDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.exception.TransactionNotFoundException;
import in.fortytwo42.enterprise.extension.enums.ApprovalAttemptMode;
import in.fortytwo42.enterprise.extension.enums.ApprovalStatus;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.utils.RandomString;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.AuthenticationAttemptHistory;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class AuthenticationAttemptServiceImpl implements AuthenticationAttemptServiceIntf {

    private  ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private static final String ASYNC_GET_CALL = "Async Get call~";
    private AuthenticationAttemptDaoIntf authenticationAttemptDao = DaoFactory.getAuthenticationAttemptDao();
    private ApplicationServiceIntf applicationProcessor = ServiceFactory.getApplicationService();
    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    private ServiceDaoIntf serviceDao = DaoFactory.getServiceDao();

    private static Logger logger= LogManager.getLogger(AuthenticationAttemptServiceImpl.class);

    private AuthenticationAttemptHistoryDaoIntf authenticationAttemptHistoryDao = DaoFactory.getAuthenticationHistoryDao();

    private Config config = Config.getInstance();

    private static final class InstanceHolder {
        private static final AuthenticationAttemptServiceImpl INSTANCE = new AuthenticationAttemptServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static AuthenticationAttemptServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String generateTransactionId() {
        return RandomString.nextString(20);
    }

    /* (non-Javadoc)
     * @see  in.fortytwo42.adapter.idc.processor.v3.AuthenticationAttemptProcessorIntf#updateApprovalAtttempt( in.fortytwo42.adapter.idc.transferobj.ApprovalAttemptPollerTO)
     */
    @Override
    public AuthenticationAttempt updateApprovalAtttempt(Session session, ApprovalAttemptPollerTO attemptPollerTO) throws AuthException {
        try {
            AuthenticationAttempt authenticationAttempt = authenticationAttemptDao.getAuthAttemptBySenderAccountIdAndTransactionId(attemptPollerTO.getSenderAccountId(),
                    attemptPollerTO.getTransactionId());
            authenticationAttempt.setAttemptStatus(attemptPollerTO.getApprovalStatus());
            return authenticationAttemptDao.update(session, authenticationAttempt);
        }
        catch (TransactionNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
        }
    }

    @Override
    public AuthenticationAttemptTO getAuthAttemptByApplicationAndTransactionId(String applicationId, String transactionId) throws TransactionNotFoundException, AuthException {
        Application application = applicationProcessor.getApplicationByApplicationId(applicationId);
        AuthenticationAttempt authenticationAttempt = authenticationAttemptDao.getAuthAttemptBySenderAccountIdAndTransactionId(application.getApplicationAccountId(), transactionId);
        return convertToAdapterApprovalAttemptTO(authenticationAttempt);
    }

    private AuthenticationAttemptTO convertToAdapterApprovalAttemptTO(AuthenticationAttempt authenticationAttempt) {
        AuthenticationAttemptTO approvalAttemptTO = new AuthenticationAttemptTO();
        approvalAttemptTO.setTransactionId(authenticationAttempt.getTransactionId());
        approvalAttemptTO.setTransactionSummary(authenticationAttempt.getTransactionSummary());
        approvalAttemptTO.setTransactionDetails(authenticationAttempt.getTransactionDetails());
        approvalAttemptTO.setApprovalAttemptType(authenticationAttempt.getAttemptType());
        approvalAttemptTO.setApprovalStatus(authenticationAttempt.getAttemptStatus());
        approvalAttemptTO.setSignTransactionId(authenticationAttempt.getSignTransactionId());
        approvalAttemptTO.setAuthenticated(authenticationAttempt.getIsPinCheckRequired());
        approvalAttemptTO.setServiceName(authenticationAttempt.getService().getServiceName());
        if (authenticationAttempt.getTimeout() != null) {
            approvalAttemptTO.setValidtill((int) (long) authenticationAttempt.getTimeout());
        }
        if(authenticationAttempt.getSenderAccountId() != null) {
            approvalAttemptTO.setSenderAccountId(authenticationAttempt.getSenderAccountId());
        }
        if(authenticationAttempt.getReceiverAccountId() != null) {
            approvalAttemptTO.setReceiverAccountId(authenticationAttempt.getReceiverAccountId());
            AttributeStoreServiceIntf attributeStoreServiceIntf = ServiceFactory.getAttributeStoreService();
            List<AttributeDataTO> attributes = attributeStoreServiceIntf.getAttributes(authenticationAttempt.getReceiverAccountId());
            approvalAttemptTO.setAttributes(attributes);
        }
        return approvalAttemptTO;
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
    public AuthenticationAttemptTO updateAuthAttemptByApplicationAndTransactionId(Session session, String applicationId, String transactionId, String attemptStatus, ApprovalAttemptTO approvalAttemptResponse)
            throws AuthException, TransactionNotFoundException {
        Application application = applicationProcessor.getApplicationByApplicationId(applicationId);
        logger.log(Level.DEBUG, "attemptStatus : " + attemptStatus + " , applicationId : " + applicationId + " , transactionId : " + transactionId);
        AuthenticationAttempt authenticationAttempt = null;
        AuthenticationAttemptHistory authenticationAttemptHistory = null;
        authenticationAttemptHistory = authenticationAttemptHistoryDao.getAuthAttemptByApplicationIdAndTransactionId(application.getApplicationAccountId(), transactionId);
        if(approvalAttemptResponse.getConsumerAccountId() != null) {
            authenticationAttemptHistory.setReceiverAccountId(approvalAttemptResponse.getConsumerAccountId());
        }
        if (!attemptStatus.equals(ApprovalStatus.PENDING.name())) {
            if (!authenticationAttemptHistory.getAttemptStatus().equals(attemptStatus)) {
                authenticationAttemptHistory.setAttemptStatus(attemptStatus);
                authenticationAttemptHistory.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
                logger.log(Level.DEBUG,
                        StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, ASYNC_GET_CALL, Constant.TILT, applicationId, Constant.TILT, transactionId,
                                "~Start DB update"));
                authenticationAttemptHistoryDao.update(session, authenticationAttemptHistory);
                logger.log(Level.DEBUG,
                        StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, ASYNC_GET_CALL, Constant.TILT, applicationId, Constant.TILT, transactionId,
                                "~ DB Update Done"));
                try {
                    authenticationAttempt = authenticationAttemptDao.getAuthAttemptBySenderAccountIdAndTransactionId(application.getApplicationAccountId(), transactionId);
                    authenticationAttemptDao.remove(session, authenticationAttempt);
                }
                catch (TransactionNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
        }else if(authenticationAttemptHistory.getAttemptType().equals(Constant.QR_LOGIN)){
            AuthenticationAttemptHistoryDaoImpl.getInstance().update(session, authenticationAttemptHistory);
        }
        authenticationAttempt = convertAuthAttemptHistory(authenticationAttemptHistory);

        return convertToAdapterApprovalAttemptTO(authenticationAttempt);
    }

    @Override
    public AuthenticationAttempt createAuthenticationAttempt(Session session, ApprovalAttemptTO approvalAttempt) {
        return createAuthenticationAttempt(session, approvalAttempt, null);

    }

    @Override
    public AuthenticationAttempt createAuthenticationAttempt(Session session, ApprovalAttemptTO approvalAttempt, Application application) {
        try {
            AuthenticationAttempt authenticationAttempt = new AuthenticationAttempt();
            authenticationAttempt.setTransactionId(approvalAttempt.getTransactionId());
            authenticationAttempt.setTransactionDetails(approvalAttempt.getTransactionDetails());
            authenticationAttempt.setTransactionSummary(approvalAttempt.getTransactionSummary());
            authenticationAttempt.setAttemptType(approvalAttempt.getApprovalAttemptType());
            authenticationAttempt.setIsPinCheckRequired(approvalAttempt.getAuthenticated());
            authenticationAttempt.setTimeout((long) approvalAttempt.getApprovalTimeout());
            authenticationAttempt.setAttemptStatus(ApprovalStatus.PENDING.name());
            authenticationAttempt = authenticationAttemptDao.create(session, authenticationAttempt);
            String serviceName = approvalAttempt.getServiceName() != null ? approvalAttempt.getServiceName() : in.fortytwo42.enterprise.extension.enums.Service.APPROVAL.name();
            Service service = serviceDao.getServiceByServiceName(serviceName);
            authenticationAttempt.setService(service);
            authenticationAttempt.setSenderAccountId(approvalAttempt.getApplicationAccountId());
            authenticationAttempt.setSignTransactionId(approvalAttempt.getSignTransactionId());
            authenticationAttempt.setApprovalAttemptMode(approvalAttempt.getApprovalAttemptMode());
            setSenderDetails(approvalAttempt, application, authenticationAttempt);
            if (!approvalAttempt.getApprovalAttemptType().equals(IAMConstants.QR_LOGIN)) {
                setReceiverDetails(approvalAttempt, application, authenticationAttempt);
            }
            logger.log(Level.INFO, "creating auth attempt ");
            return authenticationAttemptDao.update(session, authenticationAttempt);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e);
            return null;
        }
    }

    private void setSenderDetails(ApprovalAttemptTO approvalAttempt, Application application, AuthenticationAttempt authenticationAttempt) {
        Map<String, String> senderDetails = new HashMap<>();
        if (ApprovalAttemptMode.APPLICATION_TO_PEER.name().equals(approvalAttempt.getApprovalAttemptMode())
            || ApprovalAttemptMode.APPLICATION_TO_APPLICATION.name().equals(approvalAttempt.getApprovalAttemptMode())
            || ApprovalAttemptMode.APPLICATION_TO_ENTERPRISE.name().equals(approvalAttempt.getApprovalAttemptMode())) {
            authenticationAttempt.setSenderAccountId(application.getApplicationAccountId());
            senderDetails.put(Constant.ID, application.getApplicationId());
            senderDetails.put(Constant.NAME, application.getApplicationName());
            senderDetails.put(Constant.PARENT_ID, config.getProperty(Constant.ENTERPRISE_ID));
            senderDetails.put(Constant.PARENT_NAME, config.getProperty(Constant.ENTERPRISE_NAME));
        }
        else {
            authenticationAttempt.setSenderAccountId(config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID));
            senderDetails.put(Constant.ID, config.getProperty(Constant.ENTERPRISE_ID));
            senderDetails.put(Constant.NAME, config.getProperty(Constant.ENTERPRISE_NAME));
        }
        authenticationAttempt.setSenderIdDetails(new Gson().toJson(senderDetails));
    }

    private void setReceiverDetails(ApprovalAttemptTO approvalAttempt, Application application, AuthenticationAttempt authenticationAttempt) {
        Map<String, String> receiverDetails = new HashMap<>();
        if (ApprovalAttemptMode.APPLICATION_TO_APPLICATION.name().equals(approvalAttempt.getApprovalAttemptMode())
            || ApprovalAttemptMode.ENTERPRISE_TO_APPLICATION.name().equals(approvalAttempt.getApprovalAttemptMode()) || ApprovalAttemptMode.APPLICATION_TO_PEER.name().equals(approvalAttempt.getApprovalAttemptMode()) ) {
            authenticationAttempt.setReceiverAccountId(approvalAttempt.getApplicationAccountId());
            receiverDetails.put(Constant.ID, application.getApplicationId());
            receiverDetails.put(Constant.NAME, application.getApplicationName());
            receiverDetails.put(Constant.PARENT_ID, approvalAttempt.getEnterpriseName());
            receiverDetails.put(Constant.PARENT_NAME, config.getProperty(Constant.ENTERPRISE_NAME));
        }
        else if (ApprovalAttemptMode.APPLICATION_TO_ENTERPRISE.name().equals(approvalAttempt.getApprovalAttemptMode())
                 || ApprovalAttemptMode.ENTERPRISE_TO_ENTERPRISE.name().equals(approvalAttempt.getApprovalAttemptMode()) || ApprovalAttemptMode.ENTERPRISE_TO_PEER.name().equals(approvalAttempt.getApprovalAttemptMode()) ) {
            authenticationAttempt.setReceiverAccountId(approvalAttempt.getEnterpriseAccountId());
            receiverDetails.put(Constant.ID, approvalAttempt.getEnterpriseId());
            receiverDetails.put(Constant.NAME, approvalAttempt.getEnterpriseName());
        }
        else {
            authenticationAttempt.setReceiverAccountId(approvalAttempt.getConsumerAccountId());
        }
        authenticationAttempt.setReceiverIdDetails(new Gson().toJson(receiverDetails));
    }

    @Override
    public void createAuthenticationAttempt(Session session, User user, Application application, String serviceName, String transactionId, long timeout) throws ServiceNotFoundException {
        AuthenticationAttempt authenticationAttempt = new AuthenticationAttempt();
        authenticationAttempt.setTransactionId(transactionId);
        authenticationAttempt.setTransactionDetails(Constant.BINDING_REQUEST_DETAILS + application.getApplicationName());
        authenticationAttempt.setTransactionSummary(Constant.BINDING_REQUEST);
        authenticationAttempt.setAttemptType(Constant.REGULATORY);
        authenticationAttempt.setIsPinCheckRequired(true);
        authenticationAttempt.setSenderAccountId(application.getApplicationAccountId());
        authenticationAttempt.setTimeout(timeout);
        authenticationAttempt.setAttemptStatus(ApprovalStatus.PENDING.name());
        authenticationAttempt.setService(serviceDao.getServiceByServiceName(serviceName));
        //TODO: update sender receiver details
        authenticationAttemptDao.create(session, authenticationAttempt);
    }

    @Override
    public AuthenticationAttempt createAuthenticationAttempt(Session session, ApprovalAttemptPollerTO approvalAttemptPollerData) {
        AuthenticationAttempt authenticationAttempt = new AuthenticationAttempt();
        authenticationAttempt.setTransactionId(approvalAttemptPollerData.getTransactionId());
        authenticationAttempt.setTransactionDetails(approvalAttemptPollerData.getTxnDetails());
        authenticationAttempt.setTransactionSummary(approvalAttemptPollerData.getTxnSummary());
        //        authenticationAttempt.setServiceId(approvalAttemptPollerData.getS);
        //authenticationAttempt.setTimeout(approvalAttemptPollerData.getT);
        authenticationAttempt.setAttemptType(approvalAttemptPollerData.getApprovalAttemptType());
        authenticationAttempt.setAttemptStatus(approvalAttemptPollerData.getApprovalStatus());
        authenticationAttempt.setDateTimeCreated(DateUtil.getTimestamp(approvalAttemptPollerData.getDateTimeCreated()));
        authenticationAttempt.setDateTimeModified(DateUtil.getTimestamp(approvalAttemptPollerData.getDateTimeModified()));
        authenticationAttempt.setSignTransactionId(approvalAttemptPollerData.getSignTransactionId());
        authenticationAttempt.setApprovalAttemptMode(approvalAttemptPollerData.getApprovalAttemptMode());
        authenticationAttempt.setSenderAccountId(approvalAttemptPollerData.getSenderAccountId());
        authenticationAttempt.setSenderIdDetails(approvalAttemptPollerData.getSenderIdDetails());
        authenticationAttempt.setReceiverAccountId(approvalAttemptPollerData.getReceiverAccountId());
        authenticationAttempt.setReceiverIdDetails(approvalAttemptPollerData.getReceiverIdDetails());
        logger.log(Level.INFO, "creating auth attempt from poller");
        return authenticationAttemptDao.create(session, authenticationAttempt);
    }

    @Override
    public List<AuthenticationAttempt> getAuthenticationRequests(String attemptType, String attemptStatus, String searchText, int limit, int offset) {
        return authenticationAttemptDao.getAuthenticationAttempts(attemptType, attemptStatus, null, null, searchText, limit, offset);
    }

    @Override
    public Long getTotalRequestCount(String attemptType, String attemptStatus, String searchText) {
        return authenticationAttemptDao.getTotalRequestCount(attemptType, attemptStatus, null, null, searchText);
    }

    @Override
    public List<AuthenticationAttempt> getReceivedAuthenticationRequests(String attemptType, String attemptStatus, int limit, int offset, String searchText) {
        return authenticationAttemptDao.getAuthenticationAttempts(attemptType, attemptStatus, null, config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID), searchText, limit, offset);
    }

    @Override
    public Long getTotalCountOfReceivedRequests(String attemptType, String attemptStatus, String searchText) {
        return authenticationAttemptDao.getTotalRequestCount(attemptType, attemptStatus, null, config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID), searchText);
    }

    @Override
    public List<AuthenticationAttempt> getSentAuthenticationRequests(String attemptType, String attemptStatus, int limit, int offset, String searchText) {
        return authenticationAttemptDao.getAuthenticationAttempts(attemptType, attemptStatus, config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID), null, searchText, limit, offset);
    }

    @Override
    public Long getTotalCountOfSentRequests(String attemptType, String attemptStatus, String searchText) {
        return authenticationAttemptDao.getTotalRequestCount(attemptType, attemptStatus, config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID), null, searchText);
    }

    @Override
    public AuthenticationAttempt getAuthAttemptByTransactionId(String transactionId) throws TransactionNotFoundException {
        return authenticationAttemptDao.getAuthAttemptByTransactionId(transactionId);
    }

    @Override
    public void updateAuthAttempt(Session session, AuthenticationAttempt authenticationAttempt, String approvalStatus) throws AuthException {
        authenticationAttempt.setAttemptStatus(approvalStatus);
        authenticationAttemptDao.update(session, authenticationAttempt);
    }

    @Override
    public AuthenticationAttempt getAuthAttemptById(Long authattemptId) throws AuthException {
        try {
            return authenticationAttemptDao.getById(authattemptId);
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
        }
    }

    @Override
    public AuthenticationAttemptHistory getAuthAttemptHistoryById(Long authattemptId) throws AuthException {
        try {
            return authenticationAttemptHistoryDao.getById(authattemptId);
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
        }
    }

    @Override
    public PaginatedTO<AuthenticationAttemptHistory> getAuthAttemptHistoryForAuditTrail(int page, String applicationId, String searchText, List<String> userAccountIds, Long fromDate, Long toDate) {

        List<AuthenticationAttemptHistory> authAttemptHistList = authenticationAttemptHistoryDao.getTransactionHistoryAuditTrail1(page,
                Integer.parseInt(config.getProperty(Constant.LIMIT)), applicationId, searchText, userAccountIds, fromDate, toDate);
        Long count = authenticationAttemptHistoryDao.getTransactionHistoryAuditTrailCount(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), applicationId, searchText,
                userAccountIds, fromDate, toDate);
        PaginatedTO<AuthenticationAttemptHistory> paginatedList = new PaginatedTO<AuthenticationAttemptHistory>();
        paginatedList.setList(authAttemptHistList);
        paginatedList.setTotalCount(count);

        return paginatedList;

    }

    @Override
    public AuthenticationAttemptHistory getAuthAttemptHistory(Long authattemptId) throws NotFoundException {
        return authenticationAttemptHistoryDao.getById(authattemptId);
    }

    @Override
    public AuthenticationAttempt getAuthAttempt(Long authattemptId) throws NotFoundException {
        return authenticationAttemptDao.getById(authattemptId);
    }

    @Override
    public AuthenticationAttemptHistory getAuthAttemptHistoryBySourceId(Long sourceId) throws AuthException {
        try {
            return authenticationAttemptHistoryDao.getAuthAttemptHistoryBySourceId(sourceId);
        }
        catch (TransactionNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
        }
    }

    private AuthenticationAttempt convertAuthAttemptHistory(AuthenticationAttemptHistory authenticationAttemptHistory) {
        AuthenticationAttempt authenticationAttempt = new AuthenticationAttempt();

        if (authenticationAttemptHistory.getTransactionId() != null) {
            authenticationAttempt.setTransactionId(authenticationAttemptHistory.getTransactionId());
        }
        if (authenticationAttemptHistory.getTransactionSummary() != null) {
            authenticationAttempt.setTransactionSummary(authenticationAttemptHistory.getTransactionSummary());
        }
        if (authenticationAttemptHistory.getTransactionDetails() != null) {
            authenticationAttempt.setTransactionDetails(authenticationAttemptHistory.getTransactionDetails());
        }
        if (authenticationAttemptHistory.getAttemptType() != null) {
            authenticationAttempt.setAttemptType(authenticationAttemptHistory.getAttemptType());
        }
        if (authenticationAttemptHistory.getAttemptStatus() != null) {
            authenticationAttempt.setAttemptStatus(authenticationAttemptHistory.getAttemptStatus());
        }
        if (authenticationAttemptHistory.getSignTransactionId() != null) {
            authenticationAttempt.setSignTransactionId(authenticationAttemptHistory.getSignTransactionId());
        }
        if (authenticationAttemptHistory.getIsPinCheckRequired() != null) {
            authenticationAttempt.setIsPinCheckRequired(authenticationAttemptHistory.getIsPinCheckRequired());
        }
        if (authenticationAttemptHistory.getServiceName() != null) {
            Service service = new Service();
            service.setServiceName(authenticationAttemptHistory.getServiceName());
            authenticationAttempt.setService(service);
        }
        if (authenticationAttemptHistory.getTimeout() != null) {
            authenticationAttempt.setTimeout(authenticationAttemptHistory.getTimeout());
        }
        if(authenticationAttemptHistory.getSenderAccountId() != null) {
            authenticationAttempt.setSenderAccountId(authenticationAttemptHistory.getSenderAccountId());
        }
        if(authenticationAttemptHistory.getReceiverAccountId() != null) {
            authenticationAttempt.setReceiverAccountId(authenticationAttemptHistory.getReceiverAccountId());
        }
        return authenticationAttempt;
    }

    @Override
    public in.fortytwo42.tos.transferobj.PaginatedTO<AuthenticationAttemptHistoryTO> getTransactionDetails(TransactionReportRequestTO requestTO) throws AuthException {
        return TransactionReportsDaoIntfImpl.getInstance().getTransactionDetails(requestTO);
    }
}
