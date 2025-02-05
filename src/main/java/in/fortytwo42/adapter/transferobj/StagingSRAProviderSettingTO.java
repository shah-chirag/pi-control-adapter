package in.fortytwo42.adapter.transferobj;
import in.fortytwo42.entities.bean.BaseEntity;
import in.fortytwo42.tos.transferobj.RemoteAccessSettingTO;

public class StagingSRAProviderSettingTO extends BaseEntity {

	private Long id;

	private String stagingApplicationId;

	private String address;

	private Integer port;

	private Long referenceId;
	
	private Integer defaultLocalPort;
	
    private String applicationAccountId;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getStagingApplicationId() {
		return stagingApplicationId;
	}

	public void setStagingApplicationId(String stagingApplicationId) {
		this.stagingApplicationId = stagingApplicationId;
	}


	public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
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

    @Override
	public RemoteAccessSettingTO convertToTO() {
		RemoteAccessSettingTO remoteAccessSettingTO = new RemoteAccessSettingTO();
		remoteAccessSettingTO.setSraProviderAddress(address);
		remoteAccessSettingTO.setSraProviderPort(port);
		remoteAccessSettingTO.setDefaultLocalPort(defaultLocalPort);
		return remoteAccessSettingTO;
	}

	@Override
	public Object convertToTOLazy() {
		// TODO Auto-generated method stub
		return null;
	}

}
