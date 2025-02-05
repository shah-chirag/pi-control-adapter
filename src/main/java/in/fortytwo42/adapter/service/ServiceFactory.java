
package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.cam.service.CamAdminServiceImpl;
import in.fortytwo42.adapter.cam.service.CamAdminServiceIntf;

/**
 * 
 * @author ChiragShah
 *
 */
public class ServiceFactory {

    private ServiceFactory() {
        super();
    }


    public static AuthenticationAttemptServiceIntf getAuthenticationService() {
        return AuthenticationAttemptServiceImpl.getInstance();
    }

    public static SMSServiceIntf getSMSService() {
        return SMSServiceImpl.getInstance();
    }
    public static EmailServiceIntf getEmailService() {
        return EmailServiceImpl.getInstance();
    }

    public static ApplicationServiceIntf getApplicationService() {
        return ApplicationServiceImpl.getInstance();
    }

    /*
     * public static AdminProcessorIntf getAdminProcessor() { return
     * AdminProcessorImpl.getInstance(); }
     */

    public static ServiceProcessorIntf getServiceProcessor() {
        return ServiceProcessorImpl.getInstance();
    }

    public static UserAuthPrincipalServiceIntf getUserAuthPrincipalService() {
        return UserAuthPrincipalServiceImpl.getInstance();
    }

    /* public static RoleProcessorIntf getRoleProcessor() {
        return RoleProcessorImpl.getInstance();
    }
    
    public static PermissionProcessorIntf getPermissionProcessor() {
        return PermissionProcessorImpl.getInstance();
    }
    
    public static FEDGroupProcessorIntf getFEDGroupProcessor() {
        return FEDGroupProcessorImpl.getInstance();
    }
    
    public static RequestProcessorIntf getRequestProcessor() {
        return RequestProcessorImpl.getInstance();
    }
    
    public static FEDUsersProcessorIntf getFEDUserProcessor() {
        return FEDUsersProcessorImpl.getInstance();
    }*/

    public static UserServiceIntf getUserService() {
        return UserServiceImpl.getInstance();
    }

    public static UserApplicationRelServiceIntf getUserApplicationRelService() {
        return UserApplicationRelServiceImpl.getInstance();
    }

    public static PermissionServiceIntf getPermissionService() {
        return PermissionServiceImpl.getInstance();
    }

    public static RequestServiceIntf getRequestService() {
        return RequestServiceImpl.getInstance();
    }

    public static AttributeStoreServiceIntf getAttributeStoreService() {
        return AttributeStoreServiceImpl.getInstance();
    }

    public static EvidenceStoreServiceIntf getEvidenceStoreService() {
        return EvidenceStoreServiceImpl.getInstance();
    }

    public static CallbackUrlServiceIntf getCallbackUrlService() {
        return CallbackUrlServiceImpl.getInstance();
    }

    public static IamExtensionServiceIntf getIamExtensionService() {
        return IamExtensionServiceImpl.getInstance();
    }

    public static AttributeMasterServiceIntf getAttributeMasterService() {
        return AttributeMasterServiceImpl.getInstance();
    }

    public static NonADUserServiceIntf getNonADUserService() {
        return NonADUserServiceImpl.getInstance();
    }
    
    public static TunnelLogServiceIntf getTunnelLogService() {
        return TunnelLogServiceImpl.getInstance();
    }
    
    
    public static SRAGatewaySettingServiceIntf getSRAGatewaySettingService() {
        return SRAGatewaySettingServiceImpl.getInstance();
    }
    
    /**
     * 
     * @return
     */
    public static DeviceServiceIntf getDeviceService() {
        return DeviceServiceImpl.getInstance();
    }
    
    public static TokenServiceIntf getTokenService() {
        return TokenServiceImpl.getInstance();
    }
    
    public static ADConnectServiceIntf getADConnectService() {
        return ADConnectServiceImpl.getInstance();
    }
    
    public static UserGroupServiceIntf getUserGroupService() {
        return UserGroupServiceImpl.getInstance();
    }
    
    public static UserUserGroupRelServiceIntf getUserUserGroupRelService() {
        return UserUserGroupRelServiceImpl.getInstance();
    }
    
    public static UserGroupApplicationRelServiceIntf getUserApplicationServiceRelService() {
        return UserGroupApplicationRelServiceImpl.getInstance();
    }


    public static IdentityProviderServiceIntf IdentityProviderService() {
        return IdentityProviderServiceImpl.getInstance();
    }
    
    public static MapperServiceIntf getMapperService() {
        return MapperServiceImpl.getInstance();
    }


    public static ADSyncServiceIntf getADSyncService() {
        return ADSyncServiceImpl.getInstance();
    }
    
    public static EnterpriseServiceIntf getEnterpriseService() {
        return EnterpriseServiceImpl.getInstance();
    }

    public static CamAdminServiceIntf getCamAdminService() { return CamAdminServiceImpl.getInstance(); }

    public static HOTPServiceIntf getHOTPService() {return HOTPServiceImpl.getInstance();}

    public static AdHotpServiceIntf getAdHotpService() {return AdHotpServiceImpl.getInstance();}
    public static HealthCheckServiceIntf getHealthCheckService() {
        return HealthCheckServiceImpl.getInstance();
    }
    
    public static ResourceLogServiceIntf getResourceLogService() {
        return ResourceLogServiceImpl.getInstance();
    }

    public static FalloutServiceIntf getFalloutService() {
        return FalloutServiceImpl.getInstance();
    }
    public static ErrorConstantsFromConfigIntf getErrorConstant() {
        return ErrorConstantsFromConfig.getInstance();
    }

    public static FalloutConfigServiceIntf getFalloutConfigService() {
        return FalloutConfigServiceImpl.getInstance();
    }

    public static ConfigServiceIntf getConfigService() {
        return ConfigServiceImpl.getInstance();
    }

    public static UserLockServiceIntf getUserLockService() {
        return UserLockServiceImpl.getInstance();
    }
    public static LDAPDetailsServiceIntf getLdapDetailsService() {
        return LDAPDetailsServiceImpl.getInstance();
    }

    public static TOTPServiceIntf getTOTPService(){
        return TOTPServiceImpl.getInstance();
    }

    public static AdminLoginLogServiceIntf getAdminLoginLogService() {
        return AdminLoginLogServiceImpl.getInstance();
    }

    public static TemplateDetailsServiceIntf getTemPlateDetailsService() {
        return TemplateDetailsServiceImpl.getInstance();
    }

}
