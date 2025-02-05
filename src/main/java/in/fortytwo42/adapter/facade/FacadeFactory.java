package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.service.LicenseServiceImpl;
import in.fortytwo42.adapter.service.LicenseServiceIntf;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Facade objects.
 */
public class FacadeFactory {

	/**
	 * Instantiates a new facade factory.
	 */
	private FacadeFactory() {
		super();
	}
	
	/**
	 * Gets the non AD user facade.
	 *
	 * @return the non AD user facade
	 */
	public static NonADUserFacadeIntf getNonADUserFacade() {
		return NonADUserFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the encryption facade.
	 *
	 * @return the encryption facade
	 */
	public static EncryptionFacadeIntf getEncryptionFacade() {
		return EncryptionFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the auth attempt facade.
	 *
	 * @return the auth attempt facade
	 */
	public static AuthAttemptFacadeIntf getAuthAttemptFacade() {
		return AuthAttemptFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the user facade.
	 *
	 * @return the user facade
	 */
	public static UserFacadeIntf getUserFacade() {
		return UserFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the attribute facade.
	 *
	 * @return the attribute facade
	 */
	public static AttributeStoreFacadeIntf getAttributeFacade() {
		return AttributeStoreFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the service facade.
	 *
	 * @return the service facade
	 */
	public static ServiceFacadeIntf getServiceFacade() {
		return ServiceFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the evidence facade.
	 *
	 * @return the evidence facade
	 */
	public static EvidenceFacadeIntf getEvidenceFacade() {
		return EvidenceFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the application facade.
	 *
	 * @return the application facade
	 */
	public static ApplicationFacadeIntf getApplicationFacade() {
	    return ApplicationFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the request facade.
	 *
	 * @return the request facade
	 */
	public static RequestFacadeIntf getRequestFacade() {
	    return RequestFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the builds the details facade.
	 *
	 * @return the builds the details facade
	 */
	public static BuildDetailsFacadeIntf getBuildDetailsFacade() {
	    return BuildDetailsFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the Server Registry facade.
	 *
	 * @return the Server Registry facade
	 */
	public static ServerRegistryFacadeIntf getServerRegistryFacade() {
	    return ServerRegistryFacadeImpl.getInstance();
	}
	
	/**
	 * Gets the attribute master facade.
	 *
	 * @return the attribute master facade
	 */
	public static AttributeMasterFacadeIntf getAttributeMasterFacade() {
		return AttributeMasterFacadeImpl.getInstance();
	}
	
	/**
     * Gets the tunnel log facade.
     *
     * @return the tunnel log facade
     */
    public static TunnelLogFacadeIntf getTunnelLogFacade() {
        return TunnelLogFacadeImpl.getInstance();
    }
    
    public static SRAGatewaySettingFacadeIntf getSRAGatewaySettingFacade(){
        return SRAGatewaySettingFacadeImpl.getInstance();
    }
    
    public static PolicyFacadeIntf getPolicyFacade(){
        return PolicyFacadeImpl.getInstance();
    }
    
    /**
     * Gets the Device facade
     * @return {@link DeviceFacadeIntf}
     */
    public static DeviceFacadeIntf getDeviceFacade() {
        return DeviceFacadeImpl.getInstance();
    }
    
    /**
     * Gets the Token facade.
     * @return {@link TokenFacadeIntf}
     */
    public static TokenFacadeIntf getTokenFacade() {
        return TokenFacadeImpl.getInstance();
    }
 
    
    /**
     * Gets the AuditLog facade.
     * @return {@link AuditLogsFacadeIntf}
     */
    public static AuditLogsFacadeIntf getAuditLogsFacade() {
        return AuditLogsFacadeImpl.getInstance();
    }
    
    public static UserGroupFacadeIntf getUserGroupFacade() {
        return UserGroupFacadeImpl.getInstance();
    }
    
    public static ContactFacadeIntf getContactFacade(){
        return ContactFacadeImpl.getInstance();
    }
 
    public static IdentityProviderFacadeIntf getIdentityProviderFacade() {
        return IdentityProviderFacadeImpl.getInstance();
    }
    
    public static MapperFacadeIntf getMapperFacade() {
        return MapperFacadeImpl.getInstance();
    }
    
    public static ConfigFacadeIntf getConfigFacade() {
        return ConfigFacadeImpl.getInstance();
    }
    
    public static QRCodeFacadeIntf getQRCodeFacade() {
        return QRCodeFacadeImpl.getInstance();
    }
    
    public static LicenseServiceIntf getLicenseFacade() {
        return LicenseServiceImpl.getInstance();
    }
    
    public static EnterpriseFacadeIntf getEnterpriseFacade() {
        return EnterpriseFacadeImpl.getInstance();
    }

	public static HOTPFacadeIntf getHOTPFacade() {
		return HOTPFacadeImpl.getInstance();
	}

	public static AdHotpFacadeIntf getAdHOTPFacade() {
		return AdHotpFacadeImpl.getInstance();
	}

	public static CamTokenFacadeIntf getCamTokenFacade() {
		return CamTokenFacadeImpl.getInstance();
	}
	public static HealthCheckFacadIntf getHealthCheckFacade() {
		return HealthCheckFacadImpl.getInstance();
	}

	public static StateMachineWorkflowFacadeIntf getStateMachineWorkflowFacade(){
		return StateMachineWorkflowFacadeImpl.getInstance();
	}

	public static CryptoPinFacadeIntf getCryptoPinFacade(){
		return CryptoPinFacadeImpl.getInstance();
	}
	public static AccountCustomStateMachineFacadeIntf getAccountCustomStateMachineFacade(){
		return AccountCustomStateMachineFacadeImpl.getInstance();
	}

	public static FalloutProcessFacadeIntf getFalloutFacade(){
		return FalloutProcessFacdeImpl.getInstance();
	}
	
	public static CacheFacadeIntf getCacheFacade(){
        return  CacheFacadeImpl.getInstance();
    }

	public static TOTPFacadeIntf getTOTPFacade(){
		return TOTPFacadeImpl.getInstance();
	}

	public static AdminLoginLogFacadeIntf getAdminLoginLogFacade(){
        return  AdminLoginLogFacadeImpl.getInstance();
    }

	public static LDAPDetailsFacadeIntf getLDAPDetailsFacade(){
		return  LDAPDetailsFacadeImpl.getInstance();
	}
	public static TemplateDetailsFacadeIntf getTemplateDetailsFacade(){
		return  TemplateDetailsFacadeImpl.getInstance();
	}
}
