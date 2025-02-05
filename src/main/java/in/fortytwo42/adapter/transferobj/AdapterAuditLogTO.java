package in.fortytwo42.adapter.transferobj;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdapterAuditLogTO {

    private String origin;
    private ActionType actionType;
    private String logData;
    private String creatorId;
    private String reqRefNumber;
    private IdType creatorIdType;
    private String actedOnAccountId;
    private String attemptId;
    private Long createdTime;
    private List<String> accountIds;
    private Map<String, String> attributes;
    private String clientSignature;
    private Boolean isRequiredVerification;
    private String enterpriseAccountId;
    
    
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getLogData() {
        return logData;
    }

    public void setLogData(String logData) {
        this.logData = logData;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getReqRefNumber() {
        return reqRefNumber;
    }

    public void setReqRefNumber(String reqRefNumber) {
        this.reqRefNumber = reqRefNumber;
    }

    public IdType getCreatorIdType() {
        return creatorIdType;
    }

    public void setCreatorIdType(IdType creatorIdType) {
        this.creatorIdType = creatorIdType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getActedOnAccountId() {
        return actedOnAccountId;
    }

    public void setActedOnAccountId(String actedOnAccountId) {
        this.actedOnAccountId = actedOnAccountId;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getClientSignature() {
        return clientSignature;
    }

    public void setClientSignature(String clientSignature) {
        this.clientSignature = clientSignature;
    }

    public Boolean getIsRequiredVerification() {
        return isRequiredVerification;
    }

    public void setIsRequiredVerification(Boolean isRequiredVerification) {
        this.isRequiredVerification = isRequiredVerification;
    }

    public String getEnterpriseAccountId() {
        return enterpriseAccountId;
    }

    public void setEnterpriseAccountId(String enterpriseAccountId) {
        this.enterpriseAccountId = enterpriseAccountId;
    }
    
    
    
}
