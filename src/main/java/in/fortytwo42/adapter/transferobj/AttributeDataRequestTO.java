
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.adapter.enums.VerificationStatus;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

@JsonInclude(value = Include.NON_NULL)
public class AttributeDataRequestTO {

    private Long id;
    private String status;
    private AttributeDataTO attributeData;
    private List<AttributeDataTO> attributes;
    private List<AttributeDataTO> searchAttributes;
    private String callStatus;
    private String applicationId;
    private String applicationSecrete;
    private VerificationStatus verificationStatus;
    private Boolean isConsentRequired;
    private String approvalStatus;
    private boolean xDimfaUnbind;
    
    public AttributeDataRequestTO() {
        super();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AttributeDataTO getAttributeData() {
        return attributeData;
    }

    public void setAttributeData(AttributeDataTO attributeData) {
        this.attributeData = attributeData;
    }

    public List<AttributeDataTO> getSearchAttributes() {
        return searchAttributes;
    }

    public void setSearchAttributes(List<AttributeDataTO> searchAttributes) {
        this.searchAttributes = searchAttributes;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
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

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Boolean getIsConsentRequired() {
        return isConsentRequired;
    }

    public void setIsConsentRequired(Boolean isConsentRequired) {
        this.isConsentRequired = isConsentRequired;
    }
    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public List<AttributeDataTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDataTO> attributes) {
        this.attributes = attributes;
    }

    public boolean isxDimfaUnbind() {
        return xDimfaUnbind;
    }

    public void setxDimfaUnbind(boolean xDimfaUnbind) {
        this.xDimfaUnbind = xDimfaUnbind;
    }

    @Override
    public String toString() {
        return "AttributeDataRequestTO [" +
                (id != null ? ("id=" + id) : "") +
                (status != null ? (", status='" + status + "'") : "") +
                (attributeData != null ? (", attributeData=" + attributeData) : "") +
                (attributes != null ? (", attributes=" + attributes) : "") +
                (searchAttributes != null ? (", searchAttributes=" + searchAttributes) : "") +
                (callStatus != null ? (", callStatus='" + callStatus + "'") : "") +
                (applicationId != null ? (", applicationId='" + applicationId + "'") : "") +
                (applicationSecrete != null ? (", applicationSecrete='" + applicationSecrete + "'") : "") +
                (verificationStatus != null ? (", verificationStatus=" + verificationStatus) : "") +
                (isConsentRequired != null ? (", isConsentRequired=" + isConsentRequired) : "") +
                (approvalStatus != null ? (", approvalStatus='" + approvalStatus + "'") : "") +
                "]";
    }
}
