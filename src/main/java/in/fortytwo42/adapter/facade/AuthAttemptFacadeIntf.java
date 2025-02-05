package  in.fortytwo42.adapter.facade;

import javax.ws.rs.container.AsyncResponse;

import in.fortytwo42.tos.transferobj.TransactionReportRequestTO;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.transferobj.ApprovalAttemptPollerTO;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.EsbResponseTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.QRCodeDataTO;
import in.fortytwo42.adapter.transferobj.UserAuthenticationTO;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.AuthenticationAttemptHistoryTO;

public interface AuthAttemptFacadeIntf {

	/**
	 * Process poller callback according to approval attempt type.
	 * @param attemptPollerTO Approval Attempt data received in poller callback
	 * @return Response data with Poller callback success status 
	 * @throws AuthException Incase of failure fetching or processing the Authentication attempt request.
	 */
	EsbResponseTO processPollerCallback(ApprovalAttemptPollerTO attemptPollerTO) throws AuthException;

    AuthenticationAttemptTO getApprovalAttempt(String applicationId, String transactionId) throws AuthException;
    
    AuthenticationAttemptTO createApprovalAttempt(AsyncResponse asyncResponse, AuthenticationAttemptTO approvalAttemptTO, String applicationId) throws AuthException, UserBlockedException;

	AuthenticationAttemptTO createApprovalAttemptV4(AsyncResponse asyncResponse, AuthenticationAttemptTO authenticationAttemptTO, String applicationId) throws AuthException, UserBlockedException;

	AuthenticationAttemptTO getApprovalAttemptDBPoll(String transactionId, String applicationId) throws AuthException;

	void processCallbackCnB(String applicationId, AuthenticationAttemptTO approvalAttemptTOResponse) throws AuthException;

	void twoFactorAuthentication(User user, Application application, Service service, UserAuthenticationTO userAuthenticationTO, Integer timeout, AsyncResponse asyncResponse,
			 IAMExtensionV2 iamExtension, Token token) throws AuthException, IAMException;

	/**
	 * Create Attribute Addition Request for User. The attribute data is encrypted and is sent to user in Approval attempt.
	 * @param userId User Id for whom ATTRIBUTE_ADDITION request is to be sent
	 * @param attribute Attribute data to be added for the user
	 * @return Authentication attempt created for ATTRIBUTE_ADDITION request
	 * @throws AuthException Incase of any error in request creation.
	 */
	AuthenticationAttempt createAttributeAdditionRequest(Long userId, AttributeDataTO attribute) throws AuthException;
    
	/**
	 * Create Attribute Addition Request for User. The attribute data is encrypted and is sent to user in Approval attempt.
	 * @param attributeDataRequestTO Attribute list to fetch user and Attribute data to be added for the user
	 * @return Authentication attempt created for ATTRIBUTE_ADDITION request
	 * @throws AuthException Incase of any error in request creation.
	 */
	AuthenticationAttempt createAttributeAdditionRequest(Session session, AttributeDataRequestTO attributeDataRequestTO) throws AuthException;

    AuthenticationAttempt createEditAttributeAdditionRequest(Session session, Long userId, AttributeDataTO attribute) throws AuthException;

	PaginatedTO<AuthenticationAttemptHistoryTO> getAuthAttemptAuditTrail(int page, String applicationId, String searchtext,
			Long startDate, Long endDate);

    AuthenticationAttempt createAttributeEditRequest(Session session, AttributeDataRequestTO attributeDataRequestTO) throws AuthException;

    QRCodeDataTO createQRBasedApprovalAttempt(AsyncResponse asyncResponse, AuthenticationAttemptTO approvalAttemptTO, String applicationId) throws AuthException, UserBlockedException;

	QRCodeDataTO createQRBasedApprovalAttemptV4(AsyncResponse asyncResponse, AuthenticationAttemptTO approvalAttemptTO, String applicationId) throws AuthException, UserBlockedException;

	void deleteQRTrxFromAuthAttempt() throws AuthException;

	/**
	 * use to get list of transactions based on filter and search text
	 * @param requestTO
	 * @return list of transactions
	 * @throws AuthException
	 */
	in.fortytwo42.tos.transferobj.PaginatedTO<AuthenticationAttemptHistoryTO> getTransactionDetails(TransactionReportRequestTO requestTO) throws AuthException;
}
