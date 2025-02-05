package in.fortytwo42.adapter.transferobj;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.enums.AttributeAction;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

/**
 * @author Admin
 *
 */
@JsonInclude(value = Include.NON_NULL)
public class AttributeTO {

	private String attributeName;
    private String attributeValue;
    private String attributeType;
    private AttributeDataTO identifier;
    private String signTransactionId;
    private Long authAttemptId;
    private List<String> evidence;
	private String attributeState;
	private String status;
	private String comments;
	private String approvalStatus;
	private Long id;
	private String userName;
	private String evidenceHash;
    private Long dataTimeCreated;
    private String attributeTitle;
	private Boolean isDefault;
	private Boolean isConsentRequired;
	private String accountId;
	private String oldAttributeValue;
	private AttributeAction attributeAction;


	public AttributeDataTO getIdentifier() {
    	return identifier;
    }
    
    public void setIdentifier(AttributeDataTO identifier) {
    	this.identifier = identifier;
    }

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public String getAttributeState() {
		return attributeState;
	}

	public void setAttributeState(String attributeState) {
		this.attributeState = attributeState;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

    public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
	
	public String getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

	public String getSignTransactionId() {
		return signTransactionId;
	}

	public void setSignTransactionId(String signTransactionId) {
		this.signTransactionId = signTransactionId;
	}

	public Long getAuthAttemptId() {
		return authAttemptId;
	}

	public void setAuthAttemptId(Long authAttemptId) {
		this.authAttemptId = authAttemptId;
	}

	public List<String> getEvidence() {
		return evidence;
	}

	public void setEvidence(List<String> evidence) {
		this.evidence = evidence;
	}

    public String getEvidenceHash() {
        return evidenceHash;
    }

    public void setEvidenceHash(String evidenceHash) {
        this.evidenceHash = evidenceHash;
    }

	public Long getDataTimeCreated() {
		return dataTimeCreated;
	}

	public void setDataTimeCreated(Long dataTimeCreated) {
		this.dataTimeCreated = dataTimeCreated;
	}

	public String getAttributeTitle() {
		return attributeTitle;
	}

	public void setAttributeTitle(String attributeTitle) {
		this.attributeTitle = attributeTitle;
	}

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

	public Boolean getIsConsentRequired() { return isConsentRequired; }

	public void setIsConsentRequired(Boolean consentRequired) { isConsentRequired = consentRequired; }

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getOldAttributeValue() {
		return oldAttributeValue;
	}

	public void setOldAttributeValue(String oldAttributeValue) {
		this.oldAttributeValue = oldAttributeValue;
	}

	public AttributeAction getAttributeAction() {
		return attributeAction;
	}

	public void setAttributeAction(AttributeAction attributeAction) {
		this.attributeAction = attributeAction;
	}

	@Override
	public String toString() {
		return "AttributeTO [" +
				(attributeName != null ? ("attributeName='" + attributeName + "'") : "") +
				(attributeValue != null ? (", attributeValue='" + attributeValue + "'") : "") +
				(attributeType != null ? (", attributeType='" + attributeType + "'") : "") +
				(identifier != null ? (", identifier=" + identifier) : "") +
				(signTransactionId != null ? (", signTransactionId='" + signTransactionId + "'") : "") +
				(authAttemptId != null ? (", authAttemptId=" + authAttemptId) : "") +
				(evidence != null ? (", evidence=" + evidence) : "") +
				(attributeState != null ? (", attributeState='" + attributeState + "'") : "") +
				(status != null ? (", status='" + status + "'") : "") +
				(comments != null ? (", comments='" + comments + "'") : "") +
				(approvalStatus != null ? (", approvalStatus='" + approvalStatus + "'") : "") +
				(id != null ? (", id=" + id) : "") +
				(userName != null ? (", userName='" + userName + "'") : "") +
				(evidenceHash != null ? (", evidenceHash='" + evidenceHash + "'") : "") +
				(dataTimeCreated != null ? (", dataTimeCreated=" + dataTimeCreated) : "") +
				(attributeTitle != null ? (", attributeTitle='" + attributeTitle + "'") : "") +
				(isDefault != null ? (", isDefault=" + isDefault) : "") +
				(isConsentRequired != null ? (", isConsentRequired=" + isConsentRequired) : "") +
				(accountId != null ? (", accountId='" + accountId + "'") : "") +
				(oldAttributeValue != null ? (", oldAttributeValue='" + oldAttributeValue + "'") : "") +
				(attributeAction != null ? (", attributeAction=" + attributeAction) : "") +
				"]";
	}
}
