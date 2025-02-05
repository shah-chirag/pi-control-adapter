package in.fortytwo42.adapter.transferobj;
import java.util.List;

import in.fortytwo42.entities.bean.BaseEntity;
import in.fortytwo42.tos.transferobj.RemoteAccessSettingTO;

public class SRAApplicationSettingTO extends BaseEntity {

    private Long id;
   
	private String url;

	private String internalAddress;

	private String externalAddress;

	private Integer internalPort;

	private Integer externalPort;

	private String protocol;

	private String gatewayName;
	
	private Long providerAplicationObjectId;
	
	private Long referenceId;
	
	private Boolean portForwardingFacade;
	
    private Integer defaultLocalPort;

    private String applicationAccountId;
	private List<String> whiteListedURLs;

	public List<String> getWhiteListedURLs() {
		return whiteListedURLs;
	}

	public void setWhiteListedURLs(List<String> whiteListedURLs) {
		this.whiteListedURLs = whiteListedURLs;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getInternalAddress() {
		return internalAddress;
	}

	public void setInternalAddress(String internalAddress) {
		this.internalAddress = internalAddress;
	}

	public String getExternalAddress() {
		return externalAddress;
	}

	public void setExternalAddress(String externalAddress) {
		this.externalAddress = externalAddress;
	}

	public Integer getInternalPort() {
		return internalPort;
	}

	public void setInternalPort(Integer internalPort) {
		this.internalPort = internalPort;
	}

	public Integer getExternalPort() {
		return externalPort;
	}

	public void setExternalPort(Integer externalPort) {
		this.externalPort = externalPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Boolean getPortForwardingFacade() {
		return portForwardingFacade;
	}

	public void setPortForwardingFacade(Boolean portForwardingFacade) {
		this.portForwardingFacade = portForwardingFacade;
	}
	
	public Long getProviderAplicationObjectId() {
		return providerAplicationObjectId;
	}

	public void setProviderAplicationObjectId(Long providerAplicationObjectId) {
		this.providerAplicationObjectId = providerAplicationObjectId;
	}

	public Integer getDefaultLocalPort() {
        return defaultLocalPort;
    }

    public void setDefaultLocalPort(Integer defaultLocalPort) {
        this.defaultLocalPort = defaultLocalPort;
    }

    public String getApplicationAccountId() {
        return applicationAccountId;
    }

    public void setApplicationAccountId(String applicationAccountId) {
        this.applicationAccountId = applicationAccountId;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    @Override
	public RemoteAccessSettingTO convertToTO() {
		RemoteAccessSettingTO remoteAccessSettingTO = new RemoteAccessSettingTO();
		remoteAccessSettingTO.setUrl(url);
		remoteAccessSettingTO.setProtocol(protocol);
		remoteAccessSettingTO.setInternalAddress(internalAddress);
		remoteAccessSettingTO.setInternalPort(internalPort);
		remoteAccessSettingTO.setExternalAddress(externalAddress);
		remoteAccessSettingTO.setExternalPort(externalPort);
		//remoteAccessSettingTO.setSraProviderApplicationId(providerApplicationId);
		remoteAccessSettingTO.setPortForwardingFacade(portForwardingFacade);
		remoteAccessSettingTO.setDefaultLocalPort(defaultLocalPort);
		return remoteAccessSettingTO;
	}

	@Override
	public Object convertToTOLazy() {
		// TODO Auto-generated method stub
		return null;
	}

}

