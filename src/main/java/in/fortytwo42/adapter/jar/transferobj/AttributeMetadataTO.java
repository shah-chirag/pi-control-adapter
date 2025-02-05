
package in.fortytwo42.adapter.jar.transferobj;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.fortytwo42.ids.entities.enums.AttributeSecurityType;
import in.fortytwo42.ids.entities.enums.AttributeType;
import in.fortytwo42.ids.entities.enums.AttributeValueModel;
import in.fortytwo42.ids.entities.enums.AttributeVerificationType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeMetadataTO {

    private AttributeType attributeType;

    private String attributeName;

    private AttributeSecurityType attributeStoreSecurityPolicy;

    private AttributeValueModel attributeValueModel;

    private Map<String, Object> attributeSettings;

    private String status;

    private Long priority;

    private List<String> applicableAccountTypes;

    private String enterpriseAccountId;

    private AttributeVerificationType attributeVerificationType;

    private List<String> defaultVerifierEnterprises;

    private List<AttributeVerifierTO> attributeVerifiers;

    private Boolean isUnique;

    public AttributeVerificationType getAttributeVerificationType() {
        return attributeVerificationType;
    }

    public void setAttributeVerificationType(AttributeVerificationType attributeVerificationType) {
        this.attributeVerificationType = attributeVerificationType;
    }

    public List<String> getDefaultVerifierEnterprises() {
        return defaultVerifierEnterprises;
    }

    public void setDefaultVerifierEnterprises(List<String> defaultVerifierEnterprises) {
        this.defaultVerifierEnterprises = defaultVerifierEnterprises;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Map<String, Object> getAttributeSettings() {
        return attributeSettings;
    }

    public void setAttributeSettings(Map<String, Object> attributeSettings) {
        this.attributeSettings = attributeSettings;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public AttributeSecurityType getAttributeStoreSecurityPolicy() {
        return attributeStoreSecurityPolicy;
    }

    public void setAttributeStoreSecurityPolicy(AttributeSecurityType attributeStoreSecurityPolicy) {
        this.attributeStoreSecurityPolicy = attributeStoreSecurityPolicy;
    }

    public AttributeValueModel getAttributeValueModel() {
        return attributeValueModel;
    }

    public void setAttributeValueModel(AttributeValueModel attributeValueModel) {
        this.attributeValueModel = attributeValueModel;
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

    public String getEnterpriseAccountId() {
        return enterpriseAccountId;
    }

    public void setEnterpriseAccountId(String enterpriseAccountId) {
        this.enterpriseAccountId = enterpriseAccountId;
    }

    public List<AttributeVerifierTO> getAttributeVerifiers() {
        return attributeVerifiers;
    }

    public void setAttributeVerifiers(List<AttributeVerifierTO> attributeVerifiers) {
        this.attributeVerifiers = attributeVerifiers;
    }

    public Boolean getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Boolean unique) {
        isUnique = unique;
    }
}
