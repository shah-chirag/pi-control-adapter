package in.fortytwo42.adapter.transferobj;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(value = Include.NON_NULL)
public class AttributeMetadataTO {

    private Long id;
    
    private String attributeType;

    private String attributeName;

//  private String attributeValidationRegex;

    private String attributeStoreSecurityPolicy;

    private String attributeValueModel;
    
    private Map<String, Object> attributeSettings;
    
    private String status;
    
    private Long priority;
    
    private List<String> applicableAccountTypes;
    
    private String comments;
    
    private String approvalStatus;
    private Boolean isUnique;
    
    private List<AttributeVerifierTO> attributeVerifiers;

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeStoreSecurityPolicy() {
        return attributeStoreSecurityPolicy;
    }

    public void setAttributeStoreSecurityPolicy(String attributeStoreSecurityPolicy) {
        this.attributeStoreSecurityPolicy = attributeStoreSecurityPolicy;
    }

    public String getAttributeValueModel() {
        return attributeValueModel;
    }

    public void setAttributeValueModel(String attributeValueModel) {
        this.attributeValueModel = attributeValueModel;
    }

    public Map<String, Object> getAttributeSettings() {
        return attributeSettings;
    }

    public void setAttributeSettings(Map<String, Object> attributeSettings) {
        this.attributeSettings = attributeSettings;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public List<String> getApplicableAccountTypes() {
        return applicableAccountTypes;
    }

    public void setApplicableAccountTypes(List<String> applicableAccountTypes) {
        this.applicableAccountTypes = applicableAccountTypes;
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

    public List<AttributeVerifierTO> getAttributeVerifiers() {
        return attributeVerifiers;
    }

    public void setAttributeVerifiers(List<AttributeVerifierTO> attributeVerifiers) {
        this.attributeVerifiers = attributeVerifiers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeName);
    }

    public Boolean getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Boolean unique) {
        isUnique = unique;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AttributeMetadataTO other = (AttributeMetadataTO) obj;
        return Objects.equals(attributeName, other.attributeName);
    }

    @Override
    public String toString() {
        return "AttributeMetadataTO [" +
                (id != null ? ("id=" + id) : "") +
                (attributeType != null ? (", attributeType='" + attributeType + "'") : "") +
                (attributeName != null ? (", attributeName='" + attributeName + "'") : "") +
                (attributeStoreSecurityPolicy != null ? (", attributeStoreSecurityPolicy='" + attributeStoreSecurityPolicy + "'") : "") +
                (attributeValueModel != null ? (", attributeValueModel='" + attributeValueModel + "'") : "") +
                (attributeSettings != null ? (", attributeSettings=" + attributeSettings) : "") +
                (status != null ? (", status='" + status + "'") : "") +
                (priority != null ? (", priority=" + priority) : "") +
                (applicableAccountTypes != null ? (", applicableAccountTypes=" + applicableAccountTypes) : "") +
                (comments != null ? (", comments='" + comments + "'") : "") +
                (approvalStatus != null ? (", approvalStatus='" + approvalStatus + "'") : "") +
                (isUnique != null ? (", isUnique=" + isUnique) : "") +
                (attributeVerifiers != null ? (", attributeVerifiers=" + attributeVerifiers) : "") +
                "]";
    }
}
