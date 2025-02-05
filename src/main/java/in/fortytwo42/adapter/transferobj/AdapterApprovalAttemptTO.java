package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class AdapterApprovalAttemptTO {

    private String acknowledgementStatus;

    private String approvalAttemptType;

    private String approvalStatus;

    private Boolean authenticated;

    private String consumerId;

    private String signTransactionId;

    private String transactionDetails;

    private String transactionId;

    private String transactionSummary;

    private Integer validtill;

    private String tansactiongroupid1;

    private String tansactiongroupid2;
    
    private String status;
    
    private String serviceName;

    private String username;

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

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTansactiongroupid1() {
        return tansactiongroupid1;
    }

    public void setTansactiongroupid1(String tansactiongroupid1) {
        this.tansactiongroupid1 = tansactiongroupid1;
    }

    public String getTansactiongroupid2() {
        return tansactiongroupid2;
    }

    public void setTansactiongroupid2(String tansactiongroupid2) {
        this.tansactiongroupid2 = tansactiongroupid2;
    }
    
}
