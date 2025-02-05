package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.AttributeDataTO;

@JsonInclude(value = Include.NON_NULL)
public class AuthenticationAttemptTO {
    private String acknowledgementStatus;

    private String approvalAttemptType;

    private String approvalStatus;

    private Boolean authenticated;

    private String signTransactionId;

    private String transactionDetails;

    private String transactionId;

    private String transactionSummary;

    private Integer validtill;

    private String status;

    private String serviceName;

   // private AttributeDataTO publicAttribute;

    private List<AttributeDataTO> searchAttributes;

    private String applicationId;
    private String applicationSecrete;

    private String senderAccountId;

    private String approvalAttemptId;

    private String senderIdDetails;

    private String receiverIdDetails;

    private String receiverAccountId;
    
    private List<AttributeDataTO> attributes;

    public String getAcknowledgementStatus() {
        return acknowledgementStatus;
    }

    public void setAcknowledgementStatus(String acknowledgementStatus) {
        this.acknowledgementStatus = acknowledgementStatus;
    }

    public String getApprovalAttemptType() {
        return approvalAttemptType;
    }

    public void setApprovalAttemptType(String approvalAttemptType) {
        this.approvalAttemptType = approvalAttemptType;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getSignTransactionId() {
        return signTransactionId;
    }

    public void setSignTransactionId(String signTransactionId) {
        this.signTransactionId = signTransactionId;
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
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

    public Integer getValidtill() {
        return validtill;
    }

    public void setValidtill(Integer validtill) {
        this.validtill = validtill;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /*public AttributeDataTO getPublicAttribute() {
        return publicAttribute;
    }

    public void setPublicAttribute(AttributeDataTO publicAttribute) {
        this.publicAttribute = publicAttribute;
    }
*/
    public List<AttributeDataTO> getSearchAttributes() {
        return searchAttributes;
    }

    public void setSearchAttributes(List<AttributeDataTO> searchAttributes) {
        this.searchAttributes = searchAttributes;
    }

    public String getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(String senderAccountId) {
        this.senderAccountId = senderAccountId;
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

    public String getApprovalAttemptId() {
        return approvalAttemptId;
    }

    public void setApprovalAttemptId(String approvalAttemptId) {
        this.approvalAttemptId = approvalAttemptId;
    }

    public String getSenderIdDetails() {
        return senderIdDetails;
    }

    public void setSenderIdDetails(String senderIdDetails) {
        this.senderIdDetails = senderIdDetails;
    }

    public String getReceiverIdDetails() {
        return receiverIdDetails;
    }

    public void setReceiverIdDetails(String receiverIdDetails) {
        this.receiverIdDetails = receiverIdDetails;
    }

    public String getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(String receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public List<AttributeDataTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDataTO> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "AuthenticationAttemptTO [" +
                (acknowledgementStatus != null ? ("acknowledgementStatus='" + acknowledgementStatus + "'") : "") +
                (approvalAttemptType != null ? (", approvalAttemptType='" + approvalAttemptType + "'") : "") +
                (approvalStatus != null ? (", approvalStatus='" + approvalStatus + "'") : "") +
                (authenticated != null ? (", authenticated=" + authenticated) : "") +
                (signTransactionId != null ? (", signTransactionId='" + signTransactionId + "'") : "") +
                (transactionDetails != null ? (", transactionDetails='" + transactionDetails + "'") : "") +
                (transactionId != null ? (", transactionId='" + transactionId + "'") : "") +
                (transactionSummary != null ? (", transactionSummary='" + transactionSummary + "'") : "") +
                (validtill != null ? (", validtill=" + validtill) : "") +
                (status != null ? (", status='" + status + "'") : "") +
                (serviceName != null ? (", serviceName='" + serviceName + "'") : "") +
                (searchAttributes != null ? (", searchAttributes=" + searchAttributes) : "") +
                (applicationId != null ? (", applicationId='" + applicationId + "'") : "") +
                (applicationSecrete != null ? (", applicationSecrete='" + applicationSecrete + "'") : "") +
                (senderAccountId != null ? (", senderAccountId='" + senderAccountId + "'") : "") +
                (approvalAttemptId != null ? (", approvalAttemptId='" + approvalAttemptId + "'") : "") +
                (senderIdDetails != null ? (", senderIdDetails='" + senderIdDetails + "'") : "") +
                (receiverIdDetails != null ? (", receiverIdDetails='" + receiverIdDetails + "'") : "") +
                (receiverAccountId != null ? (", receiverAccountId='" + receiverAccountId + "'") : "") +
                (attributes != null ? (", attributes=" + attributes) : "") +
                "]";
    }
}
