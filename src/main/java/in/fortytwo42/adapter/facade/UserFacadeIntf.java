package  in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.transferobj.ADUserBindingTO;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.CryptoTokenTO;
import in.fortytwo42.adapter.transferobj.EvidenceRequestTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.PasswordTO;
import in.fortytwo42.adapter.transferobj.UserAuthenticationTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.transferobj.UserDataTO;
import in.fortytwo42.adapter.transferobj.UserIciciStatusTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.transferobj.UserResponseTO;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.RequestNotFoundException;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.enterprise.extension.webentities.PasswordPolicyWE;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.UserTO;
import org.hibernate.Session;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface UserFacadeIntf {

	/**
	 * Return names of all the Attributes and its verifier added in the user account.
	 * @param attributeName Attribute name to find the user
	 * @param attributeValue Attribute value to find the user
	 * @return Attribute data containing Attribute name and its verifier
	 * @throws AuthException If user is not present for the provided attribute name and value.
	 */
	UserTO getUserAttributeNames(String attributeName, String attributeValue) throws AuthException;

	/**
	 * Creates ATTRIBUTE_REQUEST Approval attempt for requesting Attribute data from the user. 
	 * @param userTO Object containing User identifier and Attribute name for creating the request.
	 * @return ATTRIBUTE_REQUEST data with request id
	 * @throws AuthException Incase of failure creating the approval attempt for the user.
	 */
	UserTO requestAttributeFromUser(UserTO userTO) throws AuthException;

	/**
	 * Fetch the approval status of the ATTRIBUTE_REQUEST attempt created by the Enterprise for the user.
	 * @param authAttemptId Request id
	 * @return ATTRIBUTE_REQUEST data with user approval status. If the request is APPROVED by the user, it also returns Attribute value and evidence metadata. 
	 * @throws AuthException
	 * @throws AttributeNotFoundException
	 */
	UserTO getAttributeRequest(Long authAttemptId) throws AuthException, AttributeNotFoundException;

	/**
	 * Creates EVIDENCE_REQUEST Approval attempt for requesting Attribute and it's Evidences from the Attribute verifier. 
	 * @param userTO Object containing Attribute name and verifier account id
	 * @return EVIDENCE_REQUEST data with request id
	 * @throws AuthException Incase of failure creating the approval attempt for the verifier.
	 */
	UserTO requestEvidenceFromUserVerifier(UserTO userTO) throws AuthException;

	/**
	 * Approves or Rejects EVIDENCE_REQUEST request received by the Enterprise. 
	 * @param evidenceRequestTO Evidence data containing request id and approval status
	 * @return EVIDENCE_REQUEST data with SUCCESS status
	 * @throws AuthException Incase of failure processing the request.
	 */
	EvidenceRequestTO approveOrRejectEvidenceRequest(EvidenceRequestTO evidenceRequestTO) throws AuthException;

	/**
	 * Returns list of pending EVIDENCE_REQUEST sent or received by the Enterprise based on requestType.
	 * @param requestType RECEIVED if received requests are to be fetched. SENT if sent requests are to be fetched.
	 * @param limit limit value
	 * @param offset offset value
	 * @return list of EVIDENCE_REQUEST
	 * @throws AuthException Incase of failure in processing any request.
	 */
	PaginatedTO<EvidenceRequestTO> getPendingEvidenceRequest(String requestType, int limit, int offset) throws AuthException;

	/**
	 * Fetch EVIDENCE_REQUEST details from authentication attempts for the provided id.
	 * @param authAttemptId Athentication attempt id
	 * @return Evidence details with approval status
	 * @throws AuthException If the request is not present for the provided id.
	 */
	EvidenceRequestTO getEvidenceRequest(Long authAttemptId) throws AuthException;

	/**
	 * Fetch all the attributes present in the database for the requested user.
	 * @param attributeName Attribute name of the user
	 * @param attributeValue Attribute value of the user
	 * @return User identifier and Attribute list
	 * @throws AuthException If user is not present in the database.
	 */
	UserTO getUserAttributesFromDb(String attributeName, String attributeValue) throws AuthException;

    UserDataTO getUserAttributesFromDb(String accountId, String role, String actor) throws AuthException;

    UserTO approveRequestAttribute(UserTO userTO, String actor, String role, Long id) throws AuthException, RequestNotFoundException;
    
    boolean validatePassword(PasswordTO passwordTO) throws AuthException;
    
    Map<String,Object> getQuestions() throws AuthException;
    
    CSVUploadTO uploadOnboardUsers(String fileType, InputStream inputStream, String role,String accountId, Long id,String fileName) throws AuthException;

    UserIciciTO onboardUserV4(UserIciciTO userTO) throws AuthException;

    UserIciciTO onboardUser(UserIciciTO userTO, String role, String username) throws AuthException;
    
    UserTO onboardUser(UserTO userTO,String role,String accountId,Long id, boolean isEncrypted, boolean saveRequest) throws AuthException;
    
    UserTO approveOnboardUser(Session session, UserTO userTO,String role, String accountId, boolean isEncrypted) throws AuthException;

    UserResponseTO authenticate(UserAuthenticationTO userTO, String ipAddress, String userAgent) throws AuthException;
	UserResponseTO authenticateADorNonADUser(UserAuthenticationTO userTO, String ipAddress, String userAgent) throws AuthException;

    /**
     * Reads sample user on-board CSV file.
     * @return {@link String} content of CSV file.
     */
    String readSampleCsvFile(String fileType);
    
    void logout(String username, String token, Long expiry);
    
    public PasswordPolicyWE getPasswordPolicies(String userType) throws AuthException;

	PaginatedTO<UserTO> getUsers(UserRole userRole, String userUpdateStatus, int page, String searchText, String attributeName, String iamStatusFilter, String userStatusFilter,
			String _2faStatusFilter,
			String approvalStatus, String userState, String role, String userTypeFilter, Long userGroupId, Boolean export) throws AuthException;

	UserTO getUserDetails(String userAccountId, String role) throws AuthException;
    
    List<ApplicationTO> getSubscribedApplicationsByUser(Long userId, String role) throws AuthException;
    

    boolean autoBindADUser(ADUserBindingTO adUserBindingTO) throws AuthException;

    boolean unbindADUser(ADUserBindingTO adUserBindingTO) throws AuthException, IAMException, UserBlockedException;

    void bindServicesToUser(UserBindingTO userBindingTO, String role, String actor,Long id, boolean saveRequest) throws AuthException;
    
    void approveBindServicesToUser(Session session, UserBindingTO userBindingTO, String role, String actor,Long id,boolean saveRequest,boolean checkerApproved) throws AuthException;
    
    UserTO editUser(UserTO userTO, String role, String actor, Long id, boolean saveRequest) throws AuthException;
	UserTO editUserRole(UserTO userTO, String role, String actor,Long id, boolean saveRequest) throws AuthException;

    UserTO approveEditUser(Session session, UserTO userTO, String role, String actor) throws AuthException;
    
    List<ApplicationTO> getTunnelingSubscribedApplicationsByUser(Long userId, String role) throws AuthException;

    CryptoTokenTO verifyCryptoToken(String applicationId, CryptoTokenTO cryptoTokenTO, boolean isEnterpriseToken) throws AuthException;

	CryptoTokenTO generateToken(String applicationId, CryptoTokenTO cryptoTokenTO) throws AuthException;

    String downloadUpdateUserStatus(String fileName, String role) throws AuthException;

    List<UserIciciTO> onboardUsers(List<UserIciciTO> userTOs) throws AuthException;
    
    AccountWE getTokensByAccountId(String accountId) throws AuthException;
    
    AccountWE getDevicesByAccountId(String accountId) throws AuthException;

	UserTO editUser(UserTO userTO) throws AuthException;

	UserTO editUserAttributes(UserTO userTO, String role, String actor,Long id, boolean saveRequest) throws AuthException;

	UserTO approveEditUserAttributes(Session session, UserTO userTO, String role, String actor) throws AuthException;

	CSVUploadTO uploadEditUsersStatus(String fileType, InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException;

	String readSampleEditUserStatusCsvFile(String fileType);

	CSVUploadTO uploadUserApplicationMapping(String fileType, InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException;

	String readSampleUserApplicationMappingCsvFile(String fileType);

    in.fortytwo42.enterprise.extension.tos.ApplicationTO validateApplication(String applicationId) throws AuthException;

    UserIciciTO changeUserPassword(UserIciciTO userTO) throws AuthException;

	UserIciciTO addAttributes(UserIciciTO userTO) throws AuthException;

	UserIciciStatusTO userStatus(UserIciciStatusTO userTO, String applicationId, String serviceName) throws AuthException;

    CryptoTokenTO verifyCryptoTokenTOTP(String applicationId, CryptoTokenTO cryptoTokenTO) throws AuthException;
	String getToken(Long id,String username, String type, String permissions, String ipAddress, String userAgent,
	                 boolean isGrantType);
	/*resets the users lastLogin time to current system time
	* throw Auth Exception */
	 UserTO editUserLastLogInTime(UserTO userTO, String role, String actor,Long id, boolean saveRequest) throws AuthException;
	 UserTO approveEditUserTimestamp(Session session, UserTO userTO, String role, String actor) throws AuthException;

    UserTO disableUser(UserTO userTO, String role, String username,Long id, boolean isEncrypted, boolean saveRequest) throws AuthException;
	/**
	 * edit users  role
	 * throw auth exception
	 * */
	UserTO updateUserRole(Session session, UserTO userTO, String role, String actor) throws AuthException, NotFoundException;

	UserTO approveDisableUser(Session session, UserTO userTO, String role, String actor) throws AuthException, NotFoundException;

    UserTO handleADFSUserRoleChange(Session session, UserTO userTO, String role, boolean isEncrypted) throws AuthException;
}
