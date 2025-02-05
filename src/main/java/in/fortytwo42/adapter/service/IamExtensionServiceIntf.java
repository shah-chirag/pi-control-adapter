package  in.fortytwo42.adapter.service;

import java.util.List;
import java.util.Map;

import in.fortytwo42.adapter.enums.Component;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.CryptoEntityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.ApplicationTO;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.enterprise.extension.tos.ContactTO;
import in.fortytwo42.enterprise.extension.tos.EnterpriseTO;
import in.fortytwo42.enterprise.extension.tos.EnterpriseWE;
import in.fortytwo42.enterprise.extension.tos.GatewaySettingsTO;
import in.fortytwo42.enterprise.extension.tos.MessageTO;
import in.fortytwo42.enterprise.extension.tos.PasswordTO;
import in.fortytwo42.enterprise.extension.tos.RemoteAccessSettingsTO;
import in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO;
import in.fortytwo42.enterprise.extension.tos.VerifierTO;
import in.fortytwo42.enterprise.extension.webentities.AccountCustomStateMachineWE;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.enterprise.extension.webentities.AttemptTypeWE;
import in.fortytwo42.enterprise.extension.webentities.AttributeMetaDataWE;
import in.fortytwo42.enterprise.extension.webentities.ChallengeWE;
import in.fortytwo42.enterprise.extension.webentities.DeviceWE;
import in.fortytwo42.enterprise.extension.webentities.PasswordPolicyWE;
import in.fortytwo42.enterprise.extension.webentities.PolicyWE;
import in.fortytwo42.enterprise.extension.webentities.StateMachineWorkFlowWE;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;
import in.fortytwo42.tos.transferobj.UserTO;
public interface IamExtensionServiceIntf {

	/**
	 * Return IAMExtensionV2 object for the enterprise.
	 * @return IAMExtensionV2 object
	 * @throws AuthException Incase of any failure in creating the object.
	 */
	IAMExtensionV2 getIAMExtension() throws AuthException;

	IAMExtensionV2 getIAMExtensionWithoutCrypto() throws AuthException;

	/**
	 * Autheticates the Enterprise with IDS server and returns I-AM Token.
	 * @param iamExtension IAMExtension object
	 * @return Enterprise I-AM Token
	 * @throws IAMException Incase of any failure obtaining I-AM token.
	 */
	Token getToken(IAMExtensionV2 iamExtension) throws IAMException;

	Token getTokenWithoutCrypto(IAMExtensionV2 iamExtension) throws IAMException;

	/**
	 * Autheticates the Application with IDS server and returns I-AM Token.
	 * @param iamExtension IAMExtension object
	 * @return Application I-AM Token
	 * @throws IAMException Incase of any failure obtaining I-AM token.
	 */
	Token getToken(IAMExtensionV2 iamExtension, Application application) throws IAMException;

	/**
	 * Search user account using list of provided attributes.
	 * @param attributes List of user attributes
	 * @param iamExtension IAMExtension object
	 * @param token I-AM Token
	 * @return User account
	 * @throws AuthException If user account is not present.
	 */
	AccountWE searchAccount(List<AttributeDataTO> attributes, IAMExtensionV2 iamExtension, Token token) throws AuthException;

	/**
	 * Verifies if the provided attribute is present in user account.
	 * @param publicAttribute Attribute data
	 * @param accountWE User account
	 * @return true is attribute is present else returns false.
	 */
	boolean isPublicAttributePresent(AttributeDataTO publicAttribute, AccountWE accountWE);

	/**
	 * Creates approval attempt for the user. 
	 * @param accountId user account id
	 * @param authenticationAttemptTO Approval attempt details
	 * @param application Creator Application obj 
	 * @param serverId Server id
	 * @return Approval attempt response data
	 * @throws IAMException Incase of failure creating approval attempt.
	 */
	ApprovalAttemptTO createApprovalAttemptOnIAM(String accountId, AuthenticationAttemptTO authenticationAttemptTO, Application application) throws IAMException;

	/**
	 * Creates approval attempt for the user. 
	 * @param accountId user account id
	 * @param consumerBindingTO Object containing Approval attempt details
	 * @param application Creator Application obj 
	 * @param serverId Server id
	 * @param approvalAttemptType Approval attempt type
	 * @param encryptedData encrypted data to be sent to the user
	 * @param signTransactionId Crypto sign transaction id
	 * @return Approval attempt response data
	 * @throws IAMException Incase of failure creating approval attempt.
	 */
	ApprovalAttemptTO createApprovalAttemptOnIAM(String accountId, ConsumerBindingTO consumerBindingTO, Application application, String approvalAttemptType, String encryptedData,
			String signTransactionId) throws IAMException;

	/**
	 * Initializes the registration of  the consumer for the services provided by the application. The consumer will have to approve 
     * in order to subscribe to the application.
	 * @param consumerAccountId user account id
	 * @param consumerBindingTO Object containing Approval attempt details
	 * @param application Creator Application obj 
	 * @param serverId Server id
	 * @param approvalAttemptType Approval attempt type
	 * @param signTransactionId Crypto sign transaction id
	 * @param encryptedData encrypted data to be sent to the user
	 * @return Approval attempt response data
	 * @throws IAMException Incase of failure creating approval attempt.
	 */
	ApprovalAttemptTO createBindingApprovalAttempt(String consumerAccountId, ConsumerBindingTO consumerBindingTO, Application application, String approvalAttemptType,
			String signTransactionId, String encryptedData) throws IAMException;

	/**
	 * After getting a positive acknowledgment from the consumer, the registration needs to be marked complete. 
     * Only after calling this function, the consumer will be subscribed to the application.
	 * @param application Creator Application obj 
	 * @param consumerAccountId user account id
	 * @param transactionId transaction id
	 * @param serviceName Service name 
	 * @throws IAMException Incase of failure in user application binding.
	 */
	void completeUserBinding(Application application, String consumerAccountId, String transactionId, String serviceName) throws IAMException;

	/**
	 * Returns user account id.
	 * @param attributeName Attribute name to find the user
	 * @param attributeValue Attribute value to find the user
	 * @param iamExtension IAMExtension object
	 * @return user account id.
	 */
	String getAccountId(String attributeName, String attributeValue, IAMExtensionV2 iamExtension);

	/**
	 * Fetch all the attribute names and its verifiers added for the user.
	 * @param attributeName Attribute name to find the user
	 * @param attributeValue Attribute value to find the user
	 * @return Return user account with attribute details
	 * @throws AuthException Incase of failure fetching attribute metadata.
	 */
	AccountWE getAllUserAttributesNames(String attributeName, String attributeValue) throws AuthException;

	/**
	 * Convert {@link AccountWE} to {@link UserTO}
	 * @param accountWE {@link AccountWE}
	 * @return {@link UserTO} with user identifier and attribute list
	 */
	UserTO convertToUserTO(AccountWE accountWE);

	/**
	 * Get list of attributes metadata for which attribute is not added by the user.
	 * @param accountId account id
	 * @param accountType Type of account: DEFAULT, APPLICATION, ENTERPRISE
	 * @param isSettingsRequired if true returns attribute settings data
	 * @return list of attribute metadata
	 * @throws AuthException Incase of failure fetching attribute metadata.
	 */
	List<AttributeMetadataTO> getDifferentialAttributeMetaData(String accountId, String accountType, boolean isSettingsRequired) throws AuthException;

	/**
	 * Fetch list of attribute metadata for attributeType and accountType.
	 * @param attributeType Type of attribute: PUBLIC, PRIVATE, DERIVED
	 * @param accountType Type of account: DEFAULT, APPLICATION, ENTERPRISE
	 * @param isSettingsRequired if true returns attribute settings data
	 * @return list of attribute metadata
	 * @throws AuthException Incase of failure fetching attribute metadata.
	 */
	List<AttributeMetadataTO> getAttributeMetaDataForAttributeType(String attributeType, String accountType, boolean isSettingsRequired) throws AuthException;

	/**
	 * Fetch list of attribute metadata for attributeType and accountType.
	 * @param attributeType Type of attribute: PUBLIC, PRIVATE, DERIVED
	 * @param accountType Type of account: DEFAULT, APPLICATION, ENTERPRISE
	 * @param isSettingsRequired if true returns attribute settings data
	 * @return list of attribute metadata
	 * @throws AuthException Incase of failure fetching attribute metadata.
	 */
	List<AttributeMetaDataWE> getAttributeMetaDataWEForAttributeType(String attributeType, String accountType, boolean isSettingsRequired) throws AuthException;

	/**
	 * Convert list of {@link in.fortytwo42.enterprise.extension.tos.v6.VerifierTO} to list of {@link VerifierTO}
	 * @param verifierTOs list of {@link in.fortytwo42.enterprise.extension.tos.v6.VerifierTO}
	 * @return list of {@link VerifierTO}
	 */
	List<in.fortytwo42.tos.transferobj.VerifierTO> convertToVerifierTO(List<in.fortytwo42.enterprise.extension.tos.VerifierTO> verifierTOs);

	/**
	 * Fetch all the attribute names and its verifiers added for the user.
	 * @param accountId User account id
	 * @return Return user account with attribute details
	 * @throws AuthException Incase of failure fetching attribute metadata.
	 */
	AccountWE getAllAttributesForAccount(String accountId) throws AuthException;

	/**
	 * Fetch Attribute metadata for the attribute name.
	 * @param attributeName Attribute name to fetch the attribute metadata
	 * @return Attribute metadata
	 * @throws AuthException Incase of failure fetching attribute metadata.
	 */
	AttributeMetaDataWE getAttributeMetadata(String attributeName) throws AuthException;
	
	AttributeMetadataTO createAttributeMetadata(AttributeMetadataTO attributeMetaDataTO) throws AuthException;
    
	AttributeMetadataTO editAttributeMetadata(String attributeName, AttributeMetadataTO attributeMetaDataTO) throws AuthException;
    
	AttributeMetadataTO getAttributeMetadataForAttributeName(String attributeName) throws AuthException;
	
	AttributeMetadataTO deleteAttributeMetadata(AttributeMetadataTO attributeMetadataTO) throws AuthException;

	AttributeMetadataTO getAttributeMetadataForAttributeNameWithoutCrypto(String attributeName) throws AuthException;

	List<AttributeMetadataTO> getAllAttributeMetaData(String accountId, String accountType) throws AuthException;

    PaginatedTO<AttributeMetadataTO> getAllAttributeMetaData(int page, int limit, String searchText) throws AuthException;

    void updateUserStatus(String accountId, String state) throws AuthException;

    void updateUserStatus(String accountId, String state, String accountAccessStatus) throws AuthException;

	void updateUserRoleOnIds(String accountId, String state,String accountType) throws AuthException;
	
    void updateSRAConsumerSettings(Application application,RemoteAccessSettingsTO remoteAccessSettingsTO) throws AuthException;
    
    void updateSRAProviderSettings(Application application, GatewaySettingsTO gatewaySettingsTO) throws AuthException;
    
    void removeSRASettings(Application application) throws AuthException;
    
    String onboardApplication(String applicationId,String applicationAccountId, String password,CryptoEntityType cryptoEntityType) throws AuthException;
    
    in.fortytwo42.adapter.transferobj.PaginatedTO<ThirdPartyVerifierTO> getVerifiers(String verifierType, String attributeName, int page, int limit) throws AuthException;
    
    in.fortytwo42.adapter.transferobj.PaginatedTO<EnterpriseTO> getEnterprises(int page, int limit) throws AuthException;

    EnterpriseWE getEnterprise() throws AuthException;

    void validatePassword(PasswordTO passwordTO) throws AuthException;

    void verifyPassword(PasswordTO passwordTO) throws AuthException;

    Map<String,Object> getQuestions() throws AuthException;
    
    GatewaySettingsTO createGatewaySetttings(SRAGatewaySettingTO gatewaySettingsTO) throws AuthException;
    
    GatewaySettingsTO editGatewaySetttings(SRAGatewaySettingTO gatewaySettingsTO) throws AuthException;
    
    void deleteGatewaySetttings(String gatewaySettingname) throws AuthException;
    
    PasswordPolicyWE getPasswordPolicies(String userType) throws AuthException;
    
    PolicyWE onboardPolicy(PolicyWE policyWE) throws AuthException;
    
    PolicyWE validatePolicy(PolicyWE policyWE) throws AuthException;
    
    PolicyWE editPolicy(PolicyWE policyWE) throws AuthException;

    List<PolicyWE> getAllPolicies() throws AuthException;
    
    PaginatedTO<TokenWE> getTokens(Integer page, Integer pageSize) throws AuthException;

    PaginatedTO<DeviceWE> getDevices(Integer page, Integer pageSize) throws AuthException;

    DeviceTO editDevice(in.fortytwo42.adapter.transferobj.DeviceTO deviceTO) throws AuthException;

    DeviceTO editDeviceBindToken(DeviceTO deviceTO) throws AuthException;

    PaginatedTO<TokenWE> getDeviceTokens(String deviceId, Integer page, Integer limit) throws AuthException;

    TokenTO editToken(TokenTO tokenTO) throws AuthException;

    TokenTO editTokenRemoteWipe(TokenTO tokenTO) throws AuthException;

    /**
     * 
     * @param deviceId
     * @return
     * @throws AuthException 
     */
    DeviceWE getDevice(String deviceId) throws AuthException;

    /**
     * 
     * @param tokenId
     * @return
     * @throws AuthException
     */
    TokenWE getToken(String tokenId) throws AuthException;
    
    Token authenticateUser(String userId, String password) throws AuthException;

    in.fortytwo42.enterprise.extension.tos.PaginatedTO<AuditLogTO> getAuditLogs(String queryParams) throws AuthException;

    AuditLogTO getDefaultAttributes(AuditLogTO auditLog) throws AuthException;

    DeviceWE getDeviceByUdid(String deviceUdid) throws AuthException;

    TokenWE getTokenByUdid(String tokenUdid) throws AuthException;

    Map<String, DeviceWE> getDevices(List<String> deviceIds) throws AuthException;

    Map<String, TokenWE> getTokens(List<String> tokenIds) throws AuthException;

    AccountWE editUserCredentials(AccountWE weEditAccount, String accountId) throws AuthException;

    PaginatedTO<PolicyWE> getAllPolicies(Integer page, Integer pageSize) throws AuthException;

    ContactTO onboardContact(ContactTO contactTO) throws AuthException;
    
    ContactTO editContact(ContactTO contactTO) throws AuthException;
    
    PaginatedTO<AccountWE> getAllContacts(String accountId,String attributeValue,Integer page, Integer limit) throws AuthException;
    
    PaginatedTO<AccountWE> getAccountsWithAttributeValue(String accountId,String attributeValue,Integer page, Integer limit) throws AuthException;
    
    List<String> getAccountIds(String attributeName) throws AuthException;
    
    AuditLogTO verifyAuditLog(AuditLogTO auditLogTO) throws AuthException;
    

    AccountWE getTokensByAccountId(String accountId) throws AuthException;

    AccountWE getDevicesByAccountId(String accountId) throws AuthException;

	AuditLogTO postAuditLogs(AuditLogTO auditLogTO) throws AuthException;

	AccountWE createAccountIfNotExist(String attributeName, String attributeValue, IAMExtensionV2 iamExtension) throws AuthException;

	AccountWE editAttribute(AttributeTO attributeTO, String accountId) throws AuthException;

    AccountWE deleteAttribute(AttributeTO attributeTO, String accountId,String cryptoDID) throws AuthException;

    void addAttribute(String attributeName, String attributeValue, String accountId) throws AuthException;

	MessageTO sendNotification(MessageTO messageTO) throws AuthException;

    DeviceTO unbindUsersFromDevice(DeviceTO deviceTO) throws AuthException;

	AccountWE getUserDeviceState(String accountId, String deviceId) throws AuthException;

    AccountWE getAccount(String attributeName, String attributeValue) throws AuthException;

    ApplicationTO validateApplication(String applicationId) throws AuthException;

	void deleteQRTrxFromAuthAttempt() throws AuthException;

	PaginatedTO<StateMachineWorkFlowWE> getAllStateMachineWorkflows(Integer page, Integer pageSize, String searchQuery, String accountId) throws AuthException;

	PaginatedTO<AttemptTypeWE> getAllAttemptTypes(Integer page, Integer pageSize) throws AuthException;

	StateMachineWorkFlowWE onboardStateMachineWorkFlow(StateMachineWorkFlowWE stateMachineWorkFlowWE) throws AuthException;

	StateMachineWorkFlowWE updateStateMachineWorkFlow(StateMachineWorkFlowWE stateMachineWorkFlowWE) throws AuthException;
	StateMachineWorkFlowWE validateStateMachineWorkFlow(StateMachineWorkFlowWE stateMachineWorkFlowWE) throws AuthException;
	PaginatedTO<AccountCustomStateMachineWE> getAllAccountCustomStateMachine(Integer page, Integer pageSize, String searchQuery, String accountId) throws AuthException;

	AccountCustomStateMachineWE onboardAccountCustomStateMachine(AccountCustomStateMachineWE accountCustomStateMachineWE) throws AuthException;

	AccountCustomStateMachineWE updateAccountCustomStateMachine(AccountCustomStateMachineWE accountCustomStateMachineWE) throws AuthException;
	AccountCustomStateMachineWE validateAccountCustomStateMachine(AccountCustomStateMachineWE accountCustomStateMachineWE) throws AuthException;

	List<ChallengeWE> getAllChallengeTypes() throws AuthException;
	void clearCache(Component cacheComponent) throws AuthException;
}
