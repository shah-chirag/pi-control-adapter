
package in.fortytwo42.adapter.service;

import java.util.List;

import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import in.fortytwo42.tos.transferobj.TemplateDetailsTO;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AccountCustomStateMachineWETO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.ContactWeTO;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.PolicyWeTO;
import in.fortytwo42.adapter.transferobj.StateMachineWorkFlowWETO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.daos.exception.RequestNotFoundException;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.ConfigTO;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;
import in.fortytwo42.tos.transferobj.MapperTO;
import in.fortytwo42.tos.transferobj.RequestTO;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;
import in.fortytwo42.tos.transferobj.UserGroupTO;
import in.fortytwo42.tos.transferobj.UserTO;

public interface RequestServiceIntf {

    AttributeTO createAttributeVerificationRequest(Session session, AttributeTO verifyAttributeTO, String actor, Long id) throws AuthException;

    void createRequest(Session session, Request request);

    List<Request> getRequests(RequestType requestType, ApprovalStatus approvalStatus);

    PaginatedTO<RequestTO> getPendingAttributeVerificationRequests(int page, int limit);

    void updateRequest(Session session, Request request) throws RequestNotFoundException;

    PaginatedTO<RequestTO> getPendingAttributeAdditionRequests(int page, int limit);

    AttributeTO createAttributeAdditionRequest(Session session, AttributeTO addAttributeTO, String actor,Long id) throws AuthException;

    Request getPendingRequestById(Long id) throws AuthException;

    Request getRequestById(Long requestId, RequestType requestType) throws RequestNotFoundException;

    List<Request> getPendingPaginatedRequests(int page, int limit, Long toDate, Long fromDate);

    Long getPendingTotalCount(int page, int limit, Long toDate, Long fromDate);

    List<Request> getNonPendingPaginatedRequests(int page, int limit, Long toDate, Long fromDate);

    Long getNonPendingTotalCount(int page, int limit, Long toDate, Long fromDate);

    PaginatedTO<RequestTO> getPendingAttributeRequests(int page, int limit, String requestType, String approvalStatus);

    Long getNonPendingTotalCount(int page, int limit, Long toDate, Long fromDate, RequestType requestType);

    List<Request> getNonPendingPaginatedRequests(int page, int limit, Long toDate, Long fromDate, RequestType requestType);

    Long getPendingTotalCount(int page, int limit, Long toDate, Long fromDate, RequestType requestType);

    List<Request> getPendingPaginatedRequests(int page, int limit, Long toDate, Long fromDate, RequestType requestType);

    PaginatedTO<RequestTO> getPaginatedApproveAndRejectedRequests(int page, int limit, RequestType requestType);

    AttributeTO createEditAttributeAdditionRequest(Session session, AttributeTO addAttributeTO, String actor, Long id) throws AuthException;

    UserTO createRequestAttributeRequest(Session session, UserTO userTO, String actor, Long id, RequestType requestType) throws AuthException;

    ApplicationTO createApplicationOnboardRequest(Session session, ApplicationTO applicationTO, String actor,Long id,
                                                  boolean isSaveRequest) throws AuthException;

    ApplicationTO createApplicationEditRequest(Session session, ApplicationTO applicationTO, String actor,Long id,
                                               boolean isSaveRequest) throws AuthException;

    // --------------------------- New Methods ---------------------------
    List<Request> getPaginatedRequests(RequestType requestType, int page, int limit, String searchText, Long fromDate, Long toDate);

    List<Request> getPendingPaginatedRequests(RequestType requestType, int page, int limit, String searchText, Long fromDate, Long toDate);
    
    UserTO createUserOnboardRequest(Session session, UserTO userTo, boolean isEncrypted, String actor,Long id,
                                    boolean isSaveRequest) throws AuthException;

    ApplicationTO createDeleteSRAApplicationSettingRequest(Session session, ApplicationTO applicationTO, String actor,Long id, boolean isSaveRequest) throws AuthException;
    
    UserBindingTO createUserServiceUnbindRequest(Session session, UserBindingTO userBindingTO, String actor,Long id,boolean isSaveRequest) throws AuthException;
    
    UserBindingTO createUserServiceBindRequest(Session session, UserBindingTO userBindingTO, String actor,Long id,boolean isSaveRequest) throws AuthException;
    
    UserTO createUserEditRequest(Session session, UserTO userTO, String actor,Long id,boolean isSaveRequest) throws AuthException;
    
    PolicyWeTO createPolicyOnboardRequest(Session session, PolicyWeTO policyWE, String actor,Long id, boolean isSaveRequest) throws AuthException;
    
    PolicyWeTO createPolicyEditRequest(Session session, PolicyWeTO policyWE, String actor,Long id,boolean isSaveRequest) throws AuthException;

    DeviceTO createEditDeviceRequest(Session session, DeviceTO deviceTO, String actor,Long id, RequestType requestType,
                                     boolean isSaveRequest) throws AuthException;

    TokenTO createEditTokenRequest(Session session, TokenTO tokenTO, String actor,Long id, RequestType editToken,
                                   boolean isSaveRequest) throws AuthException;
    
    SRAGatewaySettingTO createSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO,String actor,Long id, boolean isSaveRequest) throws AuthException;
    
    SRAGatewaySettingTO editSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id, boolean isSaveRequest) throws AuthException;
    
    SRAGatewaySettingTO deleteSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO,String actor,Long id, boolean isSaveRequest) throws AuthException;

    UserGroupTO createUserGroupCreateRequest(Session session, UserGroupTO userGroupTO, String actor,Long id,boolean saveRequest) throws AuthException;

    UserGroupTO createUserGroupUpdateRequest(Session session, UserGroupTO userGroupTO, String actor,Long id, boolean saveRequest) throws AuthException;

    UserGroupTO createUserGroupMappingRequest(Session session, UserGroupTO userGroupTO, String actor,Long id, boolean saveRequest) throws AuthException;

    UserGroupTO createApplicationUserGroupMappingRequest(Session session, UserGroupTO userGroupTO, String actor,Long id, boolean saveRequest) throws AuthException;

    UserGroupTO removeUserGroupRequest(Session session, UserGroupTO userGroupTO, String actor,Long id,boolean saveRequest) throws AuthException;
    
    ContactWeTO createContactOnboardRequest(Session session, ContactWeTO contactWE, String actor,Long id,boolean isSaveRequest) throws AuthException;
    
    ContactWeTO createContactEditRequest(Session session, ContactWeTO contactWE, String actor,Long id,boolean isSaveRequest) throws AuthException;
    IdentityProviderTO createIdentityProviderCreateRequest(Session session, IdentityProviderTO identityProviderTO,String actor,Long id, boolean saveRequest) throws AuthException;

    MapperTO createMapperCreateRequest(Session session, MapperTO mapperTO, String actor,Long id, boolean saveRequest) throws AuthException;
    
    AttributeMetadataTO createAttributeMetadataAdditionRequest(Session session, AttributeMetadataTO addAttributeTO,
                                                               String actor,Long id, boolean isSaveRequest) throws AuthException;
    AttributeMetadataTO createAttributeMetadataUpdateRequest(Session session, AttributeMetadataTO addAttributeTO,
                                                             String actor,Long id, boolean isSaveRequest) throws AuthException;
    AttributeMetadataTO createAttributeMetadataDeleteRequest(Session session, AttributeMetadataTO addAttributeTO,
                                                             String actor,Long id, boolean isSaveRequest) throws AuthException;

    UserTO createUserEditAttributesRequest(Session session, UserTO userTO, String actor,Long id, boolean saveRequest) throws AuthException;

    StateMachineWorkFlowWETO createStateMachineWorkFlowOnboardRequest(Session session,StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor,Long id, boolean isSaveRequest) throws AuthException;

    StateMachineWorkFlowWETO createStateMachineWorkFlowUpdateRequest(Session session,StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor,Long id,boolean isSaveRequest) throws AuthException;


    AccountCustomStateMachineWETO createAccountCustomStateMachineOnboardRequest(Session session,AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor,Long id, boolean isSaveRequest) throws AuthException;

    AccountCustomStateMachineWETO createAccountCustomStateMachineUpdateRequest(Session session,AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor,Long id,boolean isSaveRequest) throws AuthException;
    AttributeTO createAttributeUpdationRequest(Session session, AttributeTO addAttributeTO, String actor,Long id) throws AuthException;

    UserTO createDisableUserRequest(Session session, UserTO userTO, boolean isEncrypted, String actor,Long id, boolean isSaveRequest) throws AuthException;
    /*create user role edit request */
    public UserTO createUserRoleEditRequest(Session session, UserTO userTO, String actor,Long id, boolean isSaveRequest) throws AuthException;
    UserTO createUserEditLastLogInTimeRequest(Session session, UserTO userTO, String actor,Long id, boolean isSaveRequest) throws AuthException;

    FalloutConfigTO createFalloutConfigEditRequest(Session session, FalloutConfigTO falloutConfigTO, String actor,Long id, boolean saveRequest) throws AuthException;
    FalloutSyncDataTo createUpdateFalloutSyncData(Session session,FalloutSyncDataTo falloutSyncDataTo, String actor,Long id, boolean isSaveRequest) throws AuthException;
    ConfigTO createAddConfigRequest(Session session, ConfigTO configTO, String actor, Long id, boolean saveRequest) throws AuthException;

    ConfigTO createUpdateConfigRequest(Session session, ConfigTO configTO, String actor, Long id, boolean saveRequest) throws AuthException;
    public ConfigTO createDeleteConfigRequest(Session session, ConfigTO configTO, String actor, Long id, boolean isSaveRequest) throws AuthException;
    LdapDetailsTO createAddLdapDetailsRequest(Long id,Session session, LdapDetailsTO ldapDetailsTO, String actor, boolean saveRequest) throws  AuthException;

    LdapDetailsTO createEditLdapDetailsRequest(Long id,Session session, LdapDetailsTO ldapDetailsTO, String actor, boolean isSaveRequest) throws AuthException;

    TemplateDetailsTO createOnboardTemplateDetailsRequest(Long id, Session session, TemplateDetailsTO templateDetailsTO, String actor, boolean isSaveRequest) throws AuthException;

    TemplateDetailsTO createEditTemplateDetailsRequest(Long id, Session session, TemplateDetailsTO templateDetailsTO, String actor, boolean isSaveRequest) throws AuthException;

    TemplateDetailsTO createDeleteTemplateDetailsRequest(Long id, Session session, TemplateDetailsTO templateDetailsTO, String actor, boolean isSaveRequest) throws AuthException;
}
