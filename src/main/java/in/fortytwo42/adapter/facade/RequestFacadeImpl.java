
package in.fortytwo42.adapter.facade;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import in.fortytwo42.tos.transferobj.ConfigTO;
import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import in.fortytwo42.tos.transferobj.TemplateDetailsTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.enums.TransactionApprovalStatus;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.DeviceServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.NonADUserServiceIntf;
import in.fortytwo42.adapter.service.PermissionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.SRAGatewaySettingServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.TokenServiceIntf;
import in.fortytwo42.adapter.service.UserApplicationRelServiceIntf;
import in.fortytwo42.adapter.transferobj.AccountCustomStateMachineWETO;
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.ContactWeTO;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.PolicyWeTO;
import in.fortytwo42.adapter.transferobj.StateMachineWorkFlowWETO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.util.AuditLogConstant;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.RequestNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.bean.Role;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;
import in.fortytwo42.tos.transferobj.MapperTO;
import in.fortytwo42.tos.transferobj.RequestTO;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;
import in.fortytwo42.tos.transferobj.UserGroupTO;
import in.fortytwo42.tos.transferobj.UserTO;

// TODO: Auto-generated Javadoc
/**
 * The Class RequestFacadeImpl.
 */
public class RequestFacadeImpl implements RequestFacadeIntf {

    /** The nonad user facade impl log. */
    private String REQUEST_FACADE_IMPL_LOG = "<<<<< RequestFacadeImpl";

    private static Logger logger= LogManager.getLogger(RequestFacadeImpl.class);
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    //TODO: Facade to Facade
    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
    private ApplicationFacadeIntf applicationFacade = FacadeFactory.getApplicationFacade();
    private PolicyFacadeIntf policyFacade = FacadeFactory.getPolicyFacade();
    private ContactFacadeIntf contactFacade = FacadeFactory.getContactFacade();

    /** The permission processor intf. */
    private PermissionServiceIntf permissionService = ServiceFactory.getPermissionService();
    /** The request processor. */
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    /** The application processor. */
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    private NonADUserServiceIntf nonADUserService = ServiceFactory.getNonADUserService(); 
    /** The user application rel processor. */
    private UserApplicationRelServiceIntf userApplicationRelService = ServiceFactory.getUserApplicationRelService();

    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    
    private SRAGatewaySettingServiceIntf sraGatewaySettingService = ServiceFactory.getSRAGatewaySettingService();

    private DeviceServiceIntf deviceServiceIntf = ServiceFactory.getDeviceService();
    
    private TokenServiceIntf tokenServiceIntf = ServiceFactory.getTokenService();
    
    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private Config config = Config.getInstance();
    
    private UserGroupFacadeIntf userGroupFacadeIntf = FacadeFactory.getUserGroupFacade();
    
    private IdentityProviderFacadeIntf identityProviderFacade = FacadeFactory.getIdentityProviderFacade();

    private StateMachineWorkflowFacadeIntf stateMachineWorkflowFacade = FacadeFactory.getStateMachineWorkflowFacade();
    private FalloutProcessFacadeIntf falloutFacade= FacadeFactory.getFalloutFacade();


    private AccountCustomStateMachineFacadeIntf accountCustomStateMachineFacade = FacadeFactory.getAccountCustomStateMachineFacade();
    private AttributeStoreFacadeIntf attributeFacade = FacadeFactory.getAttributeFacade();
    private PermissionUtil permissionUtil = PermissionUtil.getInstance();

    private LDAPDetailsFacadeIntf ldapFacade = FacadeFactory.getLDAPDetailsFacade();

    private TemplateDetailsFacadeIntf templateDetailsFacadeIntf = FacadeFactory.getTemplateDetailsFacade();

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final RequestFacadeImpl INSTANCE = new RequestFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of RequestFacadeImpl.
     *
     * @return single instance of RequestFacadeImpl
     */
    public static RequestFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public RequestTO approveRequest(RequestTO requestTO, String token) throws AuthException, IAMException {
        logger.log(Level.DEBUG, REQUEST_FACADE_IMPL_LOG + " approveRequest : start");
        Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(token);
        String role = payload.get(Constant.ROLE);
        String actor = payload.get(Constant.USER_NAME);
        String useId = payload.get(Constant.ID);
        Session session = sessionFactoryUtil.getSession();
        SRAGatewaySettingTO sraGatewaySettingTO = null;
        UserBindingTO userBindingTO = null;
        UserTO userTO = null;
        ApplicationTO applicationTO = null;
        PolicyWeTO policyWeTO = null;
        UserGroupTO userGroupTO = null;
        ContactWeTO contactWeTO = null;
        MapperTO mapperTO = null;
        StateMachineWorkFlowWETO stateMachineWorkFlowWETO = null;
        AccountCustomStateMachineWETO accountCustomStateMachineWETO = null;
        AttributeTO attributeDataRequestTo = null;
        ConfigTO configTO = null;
        LdapDetailsTO ldapDetailsTO=null;
        TemplateDetailsTO templateDetailsTO = null;
        try {
            Request request = requestService.getPendingRequestById(requestTO.getId());
            //            if (!permissionService.isPermissionValidForRole(getPermissionForRequest(request.getRequestType()), role)) {
            //                throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
            //            }
            request.setChecker(actor);
            User user = request.getRequestor();
            //User user = attributeStoreDao.getUserByAttributeValueWithUpperCase(request.getMaker());
            List<Role> roleOfActingUser = user.getRoles().stream().filter(e -> e.getName().equals(UserRole.SUPER_ADMIN)).collect(Collectors.toList());

            if (actor.equals(request.getMaker())) {
                throw new AuthException(null, errorConstant.getERROR_CODE_SAME_MAKER_REQUEST_APPROVED(), errorConstant.getERROR_MESSAGE_SAME_MAKER_REQUEST_APPROVED());
            }
            if (TransactionApprovalStatus
                    .valueOf(requestTO.getApprovalStatus()) == TransactionApprovalStatus.APPROVED) {
                request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
                switch (request.getRequestType()) {
                    case USER_ONBOARD:
                        userTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_CREATION_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getAttributes().get(0).getAttributeValue(), "ENTERPRISE", ActionType.ONBOARD, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
                        if (roleOfActingUser != null && !roleOfActingUser.isEmpty()) {
                            permissionUtil.validateUsersPermissions(role);
                        }
                        userFacade.approveOnboardUser(session, userTO, role, request.getRequestor().getAccountId(), true);
                        break;
                    case USER_EDIT:
                        userTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_EDIT_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.EDIT_ACCOUNT, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", userTO.getAccountId(), null);
                        userFacade.approveEditUser(session, userTO, role, actor);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_EDIT_SUCCESSFUL + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.EDIT_ACCOUNT, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", userTO.getAccountId(), null);
                        break;
                    case SRA_GATEWAY_SETTING_ONBOARD:
                        sraGatewaySettingTO = new Gson().fromJson(request.getRequestJSON(), SRAGatewaySettingTO.class);
                        sraGatewaySettingService.approveAddSRAGatewaySetting(session, sraGatewaySettingTO);
                        requestTO.setId(sraGatewaySettingTO.getId());
                        break;
                    case SRA_GATEWAY_SETTING_UPDATE:
                        sraGatewaySettingTO = new Gson().fromJson(request.getRequestJSON(), SRAGatewaySettingTO.class);
                        sraGatewaySettingService.approveUpdateSRAGatewaySetting(session, sraGatewaySettingTO);
                        requestTO.setId(sraGatewaySettingTO.getId());
                        break;
                    case SRA_GATEWAY_SETTING_DELETION:
                        sraGatewaySettingTO = new Gson().fromJson(request.getRequestJSON(), SRAGatewaySettingTO.class);
                        sraGatewaySettingService.approveDeleteSRAGatewaySetting(session, sraGatewaySettingTO);
                        break;
                    case SRA_APPLICATION_SETTING_DELETE:
                        applicationTO = new Gson().fromJson(request.getRequestJSON(), ApplicationTO.class);
                        applicationService.approveDeleteSRAApplicationSetting(session, applicationTO);
                        break;
                    case UNBIND_SERVICE:
                        userBindingTO = new Gson().fromJson(request.getRequestJSON(), UserBindingTO.class);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.APPLICATION_SERVICE_UNBIND_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_APPLICATION + userBindingTO.getApplication().getApplicationName() + AuditLogConstant.FOR_USER + userBindingTO.getUsername(), "ENTERPRISE", ActionType.UNBIND, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
                        nonADUserService.unbindServicesFromUser(session, userBindingTO, role, actor,Long.parseLong(useId), false, true);
                        break;
                    case BIND_SERVICE:
                        userBindingTO = new Gson().fromJson(request.getRequestJSON(), UserBindingTO.class);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.APPLICATION_SERVICE_BIND_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_APPLICATION + userBindingTO.getApplication().getApplicationName() + AuditLogConstant.FOR_USER + userBindingTO.getUsername(), "ENTERPRISE", ActionType.UNBIND, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
                        userFacade.approveBindServicesToUser(session, userBindingTO, role, actor,Long.parseLong(useId), false, true);
                        // default should be that - user consent should be sent
                        boolean isUserConsentRequired = userBindingTO.getUserConsentRequired() == null || userBindingTO.getUserConsentRequired();
                        if (isUserConsentRequired) {
                            request.setApprovalStatus(ApprovalStatus.USER_APPROVAL_PENDING);
                        }
                        break;
                    case APPLICATION_ONBOARD:
                        Gson gson = new Gson();
                        applicationTO = gson.fromJson(request.getRequestJSON(), ApplicationTO.class);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.APPLICATION_ONBOARD_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_APPLICATION + applicationTO.getApplicationName(), "ENTERPRISE", ActionType.ONBOARD, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);
                        applicationFacade.approveApplicationOnboard(session, role, actor, applicationTO);
                        request.setRequestJSON(gson.toJson(applicationTO));
                        break;
                    case APPLICATION_EDIT:
                        applicationTO = new Gson().fromJson(request.getRequestJSON(), ApplicationTO.class);
                        applicationFacade.approveApplicationEdit(session, role, actor, applicationTO);
                        break;
                    case EDIT_DEVICE:
                        DeviceTO deviceRequestTO = new Gson().fromJson(request.getRequestJSON(), DeviceTO.class);
                        deviceServiceIntf.editDevice(deviceRequestTO);
                        break;
                    case EDIT_DEVICE_BIND_TOKEN:
                        DeviceTO tokenBindRequestTO = new Gson().fromJson(request.getRequestJSON(), DeviceTO.class);
                        deviceServiceIntf.editDeviceBindToken(tokenBindRequestTO);
                        break;
                    case EDIT_TOKEN:
                        TokenTO tokenRequest = new Gson().fromJson(request.getRequestJSON(), TokenTO.class);
                        tokenServiceIntf.editToken(tokenRequest);
                        break;
                    case EDIT_TOKEN_REMOTE_WIPE:
                        TokenTO tokenWipeRequest = new Gson().fromJson(request.getRequestJSON(), TokenTO.class);
                        tokenServiceIntf.editTokenRemoteWipe(tokenWipeRequest);
                        break;
                    case POLICY_EDIT:
                        policyWeTO = new Gson().fromJson(request.getRequestJSON(), PolicyWeTO.class);
                        policyFacade.approveEditPolicy(session, policyWeTO, actor);
                        break;
                    case POLICY_ONBOARD:
                        policyWeTO = new Gson().fromJson(request.getRequestJSON(), PolicyWeTO.class);
                        policyFacade.approveOnboardPolicy(session, policyWeTO, actor);
                        break;
                    case CONTACT_EDIT:
                        contactWeTO = new Gson().fromJson(request.getRequestJSON(), ContactWeTO.class);
                        contactFacade.approveEditContact(session, contactWeTO, actor);
                        break;
                    case CONTACT_ONBOARD:
                        contactWeTO = new Gson().fromJson(request.getRequestJSON(), ContactWeTO.class);
                        contactFacade.approveOnboardContact(session, contactWeTO, actor);
                        break;
                    case USER_GROUP_CREATE:
                        userGroupTO = new Gson().fromJson(request.getRequestJSON(), UserGroupTO.class);
                        userGroupFacadeIntf.approveCreateUserGroupRequest(session, userGroupTO, actor);
                        break;
                    case USER_GROUP_UPDATE:
                        userGroupTO = new Gson().fromJson(request.getRequestJSON(), UserGroupTO.class);
                        userGroupFacadeIntf.approveUpdateUserGroupRequest(session, userGroupTO, actor);
                        break;
                    case USER_GROUP_MAPPING:
                        userGroupTO = new Gson().fromJson(request.getRequestJSON(), UserGroupTO.class);
                        userGroupFacadeIntf.approveUserGroupMappingRequest(session, userGroupTO, actor);
                        break;
                    case USER_GROUP_APPLICATION_MAPPING:
                        userGroupTO = new Gson().fromJson(request.getRequestJSON(), UserGroupTO.class);
                        userGroupFacadeIntf.approveApplicationUserGroupMappingRequest(session, userGroupTO, actor);
                        break;
                    case USER_GROUP_DELETE:
                        userGroupTO = new Gson().fromJson(request.getRequestJSON(), UserGroupTO.class);
                        userGroupFacadeIntf.approveUserGroupDeleteRequest(session, userGroupTO, actor);
                        break;
                    case IDENTITY_PROVIDER_CREATE:
                        IdentityProviderTO identityProviderTO = new Gson().fromJson(request.getRequestJSON(), IdentityProviderTO.class);
                        identityProviderFacade.approveIdentityProviderRequest(session, actor, identityProviderTO);
                        break;
                    case MAPPER_CREATE:
                        mapperTO = new Gson().fromJson(request.getRequestJSON(), MapperTO.class);
                        identityProviderFacade.approveMapperCreateRequest(session, actor, mapperTO);
                        break;

                    case ATTRIBUTE_MASTER_ADDITION:
                        break;
                    case ATTRIBUTE_MASTER_DELETION:
                        break;
                    case ATTRIBUTE_MASTER_UPDATION:
                        break;
                    case USER_EDIT_ATTRIBUTE:
                        userTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_DEFAULT_ATTRIBUTE_REQUEST_APPROVED + AuditLogConstant.FOR_ATTRIBUTE + userTO.getAttributes().get(0).getAttributeName() + " = " + userTO.getAttributes().get(0).getAttributeValue() + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.AUTHENTICATION, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
                        userFacade.approveEditUserAttributes(session, userTO, role, actor);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_DEFAULT_ATTRIBUTE_UPDATE_SUCCESSFUL + AuditLogConstant.FOR_ATTRIBUTE + userTO.getAttributes().get(0).getAttributeName() + " = " + userTO.getAttributes().get(0).getAttributeValue() + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getAccountId(), "ENTERPRISE", ActionType.AUTHENTICATION, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", userTO.getAccountId(), null);
                        break;
                    case EDIT_DEVICE_UNBIND_USER:
                        DeviceTO userUnbindRequestTO = new Gson().fromJson(request.getRequestJSON(), DeviceTO.class);
                        deviceServiceIntf.unbindUsersFromDevice(userUnbindRequestTO);
                        break;
                    case STATE_MACHINE_WORKFLOW_ONBOARD:
                        stateMachineWorkFlowWETO = new Gson().fromJson(request.getRequestJSON(), StateMachineWorkFlowWETO.class);
                        stateMachineWorkflowFacade.approveOnboardStateMachineWorkFlow(session, stateMachineWorkFlowWETO, actor);
                        break;
                    case STATE_MACHINE_WORKFLOW_UPDATE:
                        stateMachineWorkFlowWETO = new Gson().fromJson(request.getRequestJSON(), StateMachineWorkFlowWETO.class);
                        stateMachineWorkflowFacade.approveUpdateStateMachineWorkFlow(session, stateMachineWorkFlowWETO, actor);
                        break;
                    case ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD:
                        accountCustomStateMachineWETO = new Gson().fromJson(request.getRequestJSON(), AccountCustomStateMachineWETO.class);
                        accountCustomStateMachineFacade.approveOnboardAccountCustomStateMachine(session, accountCustomStateMachineWETO, actor);
                        break;
                    case ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE:
                        accountCustomStateMachineWETO = new Gson().fromJson(request.getRequestJSON(), AccountCustomStateMachineWETO.class);
                        accountCustomStateMachineFacade.approveUpdateAccountCustomStateMachine(session, accountCustomStateMachineWETO, actor);
                        break;
                    case ATTRIBUTE_UPDATION:
                        attributeDataRequestTo = new Gson().fromJson(request.getRequestJSON(), AttributeTO.class);
                        attributeFacade.updateAttributeOfUser(attributeDataRequestTo);
                        break;
                    case UPDATE_USER_LAST_LOGIN_TIME:
                        userTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                        userFacade.approveEditUserTimestamp(session, userTO, role, actor);
                        break;
                    case DISABLE_USER:
                        userTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_DISABLE_REQUEST_APPROVED + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.EDIT_ACCOUNT, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", userTO.getAccountId(), null);
                        if (roleOfActingUser != null && !roleOfActingUser.isEmpty()) {
                            permissionUtil.validateUsersPermissions(role);
                            if(userTO.getUsername()!=null && actor.equalsIgnoreCase(userTO.getUsername())){
                                throw new AuthException(null, errorConstant.getERROR_CODE_SAME_MAKER_REQUEST_APPROVED(), errorConstant.getERROR_MESSAGE_SELF_BLOCKING_NON_PERMITTED());
                            }
                        }
                        userFacade.approveDisableUser(session, userTO, role, actor);
                        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_EDIT_SUCCESSFUL + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.EDIT_ACCOUNT, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", userTO.getAccountId(), null);
                        break;
                    case UPDATE_USER_ROLE:
                        userTO = new Gson().fromJson(request.getRequestJSON(), UserTO.class);
                        if (roleOfActingUser != null && !roleOfActingUser.isEmpty()) {
                            permissionUtil.validateUsersPermissions(role);
                        }
                        userFacade.updateUserRole(session, userTO, role, actor);
                        break;
                    case UPDATE_FALLOUT_CONFIG:
                        FalloutConfigTO falloutConfigTO = new Gson().fromJson(request.getRequestJSON(), FalloutConfigTO.class);
                        FacadeFactory.getFalloutFacade().approveEditRequest(session, falloutConfigTO,  role, actor);
                        break;
                    case UPDATE_FALLOUT_SYNC_DATA:
                        FalloutSyncDataTo falloutSyncDataTo= new Gson().fromJson(request.getRequestJSON(), FalloutSyncDataTo.class);
                        falloutFacade.updateFalloutSyncData( falloutSyncDataTo);
                        break;
                    case ADD_CONFIG:
                        configTO = new Gson().fromJson(request.getRequestJSON(), ConfigTO.class);
                        FacadeFactory.getConfigFacade().approveRequest(session, configTO, role, actor);
                        break;
                    case UPDATE_CONFIG:
                        configTO = new Gson().fromJson(request.getRequestJSON(),ConfigTO.class);
                        FacadeFactory.getConfigFacade().approveRequest(session, configTO, role, actor);
                        break;
                    case DELETE_CONFIG:
                        configTO = new Gson().fromJson(request.getRequestJSON(),ConfigTO.class);
                        FacadeFactory.getConfigFacade().approveDeleteRequest(session, configTO, role, actor);
                        break;
                    case ADD_LDAP_DETAILS:
                        ldapDetailsTO = new Gson().fromJson(request.getRequestJSON(),LdapDetailsTO.class);
                        ldapFacade.addLdapDetails(ldapDetailsTO);
                        break;
                    case EDIT_LDAP_DETAILS:
                        ldapDetailsTO = new Gson().fromJson(request.getRequestJSON(),LdapDetailsTO.class);
                        ldapFacade.editLdapDetail(ldapDetailsTO);
                        break;
                    case ONBOARD_TEMP_DETAILS:
                        templateDetailsTO = new Gson().fromJson(request.getRequestJSON(), TemplateDetailsTO.class);
                        templateDetailsFacadeIntf.approveOnboardTemplateDetails(session,templateDetailsTO);
                        break;
                    case EDIT_TEMP_DETAILS:
                        templateDetailsTO = new Gson().fromJson(request.getRequestJSON(),TemplateDetailsTO.class);
                        templateDetailsFacadeIntf.approveEditTemplateDetails(session,templateDetailsTO);
                        break;
                    case DELETE_TEMP_DETAILS:
                        templateDetailsTO = new Gson().fromJson(request.getRequestJSON(),TemplateDetailsTO.class);
                        templateDetailsFacadeIntf.approveDeleteTemplateDetails(session,templateDetailsTO);
                        break;
                    default:
                        break;
                }
            } else {
                request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
            }
            UserDaoIntf userDao = DaoFactory.getUserDao();
            request.setApprover(userDao.getActiveUserById(Long.valueOf(useId)));
            //request.setApprover(attributeStoreDao.getUserByAttributeValueWithUpperCase(actor));
            request.setApproverComments(requestTO.getComments());
            requestService.updateRequest(session, request);
            sessionFactoryUtil.closeSession(session);
        } catch (RequestNotFoundException  e) {
            session.getTransaction().rollback();
            throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(), errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
        } catch (UserNotFoundException e) {
            session.getTransaction().rollback();
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        } catch (NotFoundException  e) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        requestTO.setStatus(Constant.SUCCESS_STATUS);
        logger.log(Level.DEBUG, REQUEST_FACADE_IMPL_LOG + " approveRequest : end");
        return requestTO;
    }

    /**
     * Gets the all requests.
     *
     * @param actionType the action type
     * @param page the page
     * @param token the token
     * @param toDate the to date
     * @param fromDate the from date
     * @return the all requests
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<RequestTO> getAllRequests(RequestType requestType, String actionType, int page, String token, Long toDate, Long fromDate, String searchText) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_FACADE_IMPL_LOG + " getAllRequests : start");
        Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(token);
        String role = payload.get(Constant.ROLE);
        String actor = payload.get(Constant.USER_NAME);
//        if (!permissionService.isPermissionValidForRole(PermissionUtil.FED_GET_REQUESTS, role)) {
//            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//        }
        List<Request> requests;
        Long count;
        if (Constant.PENDING.equals(actionType)) {
            requests = requestService.getPendingPaginatedRequests(requestType, page, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText, toDate, fromDate);
            count = requestService.getPendingTotalCount(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate);
        }
        else {
            requests = requestService.getPaginatedRequests(requestType, page, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText, toDate, fromDate);
            count = requestService.getNonPendingTotalCount(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate);
        }
        PaginatedTO<RequestTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(new EntityToTOConverter<Request, RequestTO>().convertEntityListToTOList(requests));
        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, REQUEST_FACADE_IMPL_LOG + " getAllRequests : end");
        return paginatedTO;
    }

    /**
     * Gets the requests.
     *
     * @param actionType the action type
     * @param page the page
     * @param token the token
     * @param toDate the to date
     * @param fromDate the from date
     * @param requestType the request type
     * @return the requests
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<RequestTO> getRequests(String actionType, int page, String token, Long toDate, Long fromDate, String requestType) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_FACADE_IMPL_LOG + " getRequests : start");
        Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(token);
        String role = payload.get(Constant.ROLE);
        String actor = payload.get(Constant.USER_NAME);
//        if (!permissionService.isPermissionValidForRole(PermissionUtil.FED_GET_REQUESTS, role)) {
//            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//        }
        List<Request> requests;
        Long count;
        if (Constant.PENDING.equals(actionType)) {
            requests = requestService.getPendingPaginatedRequests(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate, RequestType.valueOf(requestType));
            count = requestService.getPendingTotalCount(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate, RequestType.valueOf(requestType));
        }
        else {
            requests = requestService.getNonPendingPaginatedRequests(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate, RequestType.valueOf(requestType));
            count = requestService.getNonPendingTotalCount(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate, RequestType.valueOf(requestType));
        }
        PaginatedTO<RequestTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(new EntityToTOConverter<Request, RequestTO>().convertEntityListToTOList(requests));
        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, REQUEST_FACADE_IMPL_LOG + " getRequests : end");
        return paginatedTO;
    }
    
    @Override
    public PaginatedTO<RequestTO> getPendingRequests(int page, String role, String requestType,String actionType) throws AuthException {
//        permissionUtil.validateAttributeApprovalPermission(role);
        int limit = Integer.parseInt(config.getProperty(Constant.LIMIT));
        return requestService.getPendingAttributeRequests(page, limit, requestType, actionType);
    }

    /**
     * Gets the paginated approve and rejected requests.
     *
     * @param page the page
     * @param limit the limit
     * @param role the role
     * @param requestType the request type
     * @return the paginated approve and rejected requests
     */
    @Override
    public PaginatedTO<RequestTO> getPaginatedApproveAndRejectedRequests(int page, int limit, String role, String requestType) {
        logger.log(Level.DEBUG, REQUEST_FACADE_IMPL_LOG + " getPaginatedApproveAndRejectedRequests : start");
        /*Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(token);
        String role = payload.get(Constant.ROLE);
        String actor = payload.get(Constant.USER_NAME);
        PermissionProcessorIntf permissionProcessorIntf = ProcessorFactory.getPermissionProcessor();
        if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.FED_GET_REQUESTS, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }
        List<Request> requests;
        Long count;
        if (Constant.PENDING.equals(actionType)) {
            requests = requestProcessor.getPaginatedApproveAndRejectedRequests(page, limit, requestType);
            count = requestProcessor.getPendingTotalCount(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate,RequestType.valueOf(requestType));
        } else {
            requests = requestProcessor.getNonPendingPaginatedRequests(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate,RequestType.valueOf(requestType));
            count = requestProcessor.getNonPendingTotalCount(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), toDate, fromDate,RequestType.valueOf(requestType));
        }*/
        PaginatedTO<RequestTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(new EntityToTOConverter<Request, RequestTO>().convertEntityListToTOList(null));
        paginatedTO.setTotalCount(1l);
        logger.log(Level.DEBUG, REQUEST_FACADE_IMPL_LOG + " getPaginatedApproveAndRejectedRequests : start");
        return paginatedTO;
    }

}
