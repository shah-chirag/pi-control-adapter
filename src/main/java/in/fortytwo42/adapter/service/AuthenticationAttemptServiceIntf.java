package in.fortytwo42.adapter.service;

import java.util.List;

import in.fortytwo42.tos.transferobj.AuthenticationAttemptHistoryTO;
import in.fortytwo42.tos.transferobj.TransactionReportRequestTO;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.ApprovalAttemptPollerTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.exception.TransactionNotFoundException;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.AuthenticationAttemptHistory;
import in.fortytwo42.entities.bean.User;

public interface AuthenticationAttemptServiceIntf {

	String generateTransactionId();

	AuthenticationAttempt updateApprovalAtttempt(Session session, ApprovalAttemptPollerTO attemptPollerTO) throws AuthException;

	AuthenticationAttemptTO getAuthAttemptByApplicationAndTransactionId(String applicationId, String transactionId) throws TransactionNotFoundException, AuthException;

	void createAuthenticationAttempt(Session session, User user, Application application, String serviceName, String transactionId, long timeout)  throws ServiceNotFoundException;

    AuthenticationAttempt createAuthenticationAttempt(Session session, ApprovalAttemptPollerTO approvalAttemptPollerData);

    AuthenticationAttempt getAuthAttemptByTransactionId(String transactionId) throws TransactionNotFoundException;

    void updateAuthAttempt(Session session, AuthenticationAttempt authenticationAttempt, String approvalStatus) throws AuthException;

    AuthenticationAttempt getAuthAttemptById(Long authattemptId) throws AuthException;

	AuthenticationAttempt createAuthenticationAttempt(Session session, ApprovalAttemptTO approvalAttempt, Application application);

	AuthenticationAttempt createAuthenticationAttempt(Session session, ApprovalAttemptTO approvalAttempt);

	List<AuthenticationAttempt> getAuthenticationRequests(String attemptType, String attemptStatus, String searchText, int limit, int offset);

	Long getTotalRequestCount(String attemptType, String attemptStatus, String searchText);

	List<AuthenticationAttempt> getReceivedAuthenticationRequests(String attemptType, String attemptStatus, int limit, int offset, String searchText);

	Long getTotalCountOfReceivedRequests(String attemptType, String attemptStatus, String searchText);

	List<AuthenticationAttempt> getSentAuthenticationRequests(String attemptType, String attemptStatus, int limit, int offset, String searchText);

	Long getTotalCountOfSentRequests(String attemptType, String attemptStatus, String searchText);
	
    AuthenticationAttemptHistory getAuthAttemptHistoryById(Long authattemptId) throws AuthException;

    PaginatedTO<AuthenticationAttemptHistory> getAuthAttemptHistoryForAuditTrail(int page, String applicationId, String searchText, List<String> userAccountIds, Long fromDate, Long toDate);

    AuthenticationAttemptHistory getAuthAttemptHistory(Long authattemptId) throws NotFoundException;

    AuthenticationAttempt getAuthAttempt(Long authattemptId) throws NotFoundException;

    AuthenticationAttemptHistory getAuthAttemptHistoryBySourceId(Long sourceId) throws AuthException;

    AuthenticationAttemptTO updateAuthAttemptByApplicationAndTransactionId(Session session, String applicationId, String transactionId, String attemptStatus, ApprovalAttemptTO approvalAttemptResponse)
            throws AuthException, TransactionNotFoundException;
	in.fortytwo42.tos.transferobj.PaginatedTO<AuthenticationAttemptHistoryTO> getTransactionDetails(TransactionReportRequestTO requestTO) throws AuthException;

}
