
package in.fortytwo42.adapter.jar.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.ids.entities.beans.EvidenceMetadata;
import in.fortytwo42.ids.entities.beans.Verifier;
import in.fortytwo42.ids.entities.enums.AttributeOperationStatus;
import in.fortytwo42.ids.entities.enums.AttributeSecurityType;
import in.fortytwo42.ids.entities.enums.AttributeState;
import in.fortytwo42.ids.entities.enums.AttributeStatus;
import in.fortytwo42.ids.entities.enums.AttributeType;
import in.fortytwo42.ids.entities.enums.AttributeValueModel;

@JsonInclude(value = Include.NON_NULL)
public class AttributeTO {

    private String attributeName;

    private String attributeValue;

    private String encryptedAttributeValue;

    private String attributeRelationId;

    private String attributeRelationEntity;

    private AttributeSecurityType attributeStoreSecurityPolicy;

    private AttributeValueModel attributeValueModel;

    private String attributeValueProviderAccountId;

    private String signTransactionId;

    private AttributeType attributeType;

    private List<Verifier> verifiers;

    private AttributeState attributeState;

    private String attributeTitle;

    private Long dateTimeCreated;

    private Long dateTimeModified;

    private List<EvidenceMetadata> evidences;

    private AttributeStatus status;
    
    private String updatedAttributeValue;

    private AttributeOperationStatus operationStatus;
    
    private String errorMessage;

    private String profileName;
    private Boolean isUnique;

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    private Boolean isDefault;

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

    public String getAttributeRelationId() {
        return attributeRelationId;
    }

    public void setAttributeRelationId(String attributeRelationId) {
        this.attributeRelationId = attributeRelationId;
    }

    public String getAttributeRelationEntity() {
        return attributeRelationEntity;
    }

    public void setAttributeRelationEntity(String attributeRelationEntity) {
        this.attributeRelationEntity = attributeRelationEntity;
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

    public String getAttributeValueProviderAccountId() {
        return attributeValueProviderAccountId;
    }

    public void setAttributeValueProviderAccountId(String attributeValueProviderAccountId) {
        this.attributeValueProviderAccountId = attributeValueProviderAccountId;
    }

    public String getSignTransactionId() {
        return signTransactionId;
    }

    public void setSignTransactionId(String signTransactionId) {
        this.signTransactionId = signTransactionId;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public List<Verifier> getVerifiers() {
        return verifiers;
    }

    public void setVerifiers(List<Verifier> verifiers) {
        this.verifiers = verifiers;
    }

    public AttributeState getAttributeState() {
        return attributeState;
    }

    public void setAttributeState(AttributeState attributeState) {
        this.attributeState = attributeState;
    }

    public String getAttributeTitle() {
        return attributeTitle;
    }

    public void setAttributeTitle(String attributeTitle) {
        this.attributeTitle = attributeTitle;
    }

    public Long getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(Long dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }

    public Long getDateTimeModified() {
        return dateTimeModified;
    }

    public void setDateTimeModified(Long dateTimeModified) {
        this.dateTimeModified = dateTimeModified;
    }

    public List<EvidenceMetadata> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<EvidenceMetadata> evidences) {
        this.evidences = evidences;
    }

    public AttributeStatus getStatus() {
        return status;
    }

    public void setStatus(AttributeStatus status) {
        this.status = status;
    }

    public String getUpdatedAttributeValue() {
        return updatedAttributeValue;
    }

    public void setUpdatedAttributeValue(String updatedAttributeValue) {
        this.updatedAttributeValue = updatedAttributeValue;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    
    public String getEncryptedAttributeValue() {
        return encryptedAttributeValue;
    }

    public void setEncryptedAttributeValue(String encryptedAttributeValue) {
        this.encryptedAttributeValue = encryptedAttributeValue;
    }

    public AttributeOperationStatus getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(AttributeOperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public Boolean getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Boolean isUnique) {
        this.isUnique = isUnique;
    }
    
    
    
}
