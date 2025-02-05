package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.AttributeDataTO;
import lombok.ToString;

@JsonInclude(value = Include.NON_NULL)
@ToString
public class ConsumerBindingTO {

	private String status;
	private Integer timeOut;
	private String transactionId;
	private String transactionSummary;
	private String transactionDetails;
	private Boolean activateEncryption;
	private String twoFactorStatus;
	private AttributeDataTO customAttribute;
	private List<AttributeDataTO> searchAttributes;
	private String serviceName;
	private String approvalStatus;
	
    private String consumerId;
    private String email;
    private String firstName;
    private String fullName;
    private String lastName;
    private String location;
    private String username;
    private String applicationId;
    private String applicationSecrete;
	private Boolean userConsentRequired;
	private Boolean deleteProfile;
	private List<AttributeDataTO> attributeData;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(Integer timeOut) {
		this.timeOut = timeOut;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getTransactionSummary() {
		return transactionSummary;
	}

	public void setTransactionSummary(String transactionSummary) {
		this.transactionSummary = transactionSummary;
	}

	public String getTransactionDetails() {
		return transactionDetails;
	}

	public void setTransactionDetails(String transactionDetails) {
		this.transactionDetails = transactionDetails;
	}

	public Boolean getActivateEncryption() {
		return activateEncryption;
	}

	public void setActivateEncryption(Boolean activateEncryption) {
		this.activateEncryption = activateEncryption;
	}

	public String getTwoFactorStatus() {
		return twoFactorStatus;
	}

	public void setTwoFactorStatus(String twoFactorStatus) {
		this.twoFactorStatus = twoFactorStatus;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public AttributeDataTO getCustomAttribute() {
        return customAttribute;
    }

    public void setCustomAttribute(AttributeDataTO customAttribute) {
        this.customAttribute = customAttribute;
    }

    public List<AttributeDataTO> getSearchAttributes() {
		return searchAttributes;
	}

	public void setSearchAttributes(List<AttributeDataTO> searchAttributes) {
		this.searchAttributes = searchAttributes;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationSecrete() {
        return applicationSecrete;
    }

    public void setApplicationSecrete(String applicationSecrete) {
        this.applicationSecrete = applicationSecrete;
    }

	public Boolean getUserConsentRequired() {
		return userConsentRequired;
	}

	public void setUserConsentRequired(Boolean userConsentRequired) {
		this.userConsentRequired = userConsentRequired;
	}

	public Boolean getDeleteProfile() {
		return deleteProfile;
	}

	public void setDeleteProfile(Boolean deleteProfile) {
		this.deleteProfile = deleteProfile;
	}

	public List<AttributeDataTO> getAttributeData() {
		return attributeData;
	}

	public void setAttributeData(List<AttributeDataTO> attributeData) {
		this.attributeData = attributeData;
	}
}
