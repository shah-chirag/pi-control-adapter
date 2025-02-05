
package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.ToString;

@JsonInclude(value = Include.NON_NULL)
@ToString
public class AttributeVerifierTO {

    private String verificationType;//moved back to string since TO and WE conversion issues with IAMExtension
    private String verifierType;//moved back to string since TO and WE conversion issues with IAMExtension
    private String verifierId;
    private Boolean isDefault; //isDefault verifier for that attribute
    private String sourceType; //moved back to string since TO and WE conversion issues with IAMExtension
    private String sourceId; //Enterprise id
    private int priority;
    private String verifierName;
    private Boolean isActive;

    public String getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(String verifierId) {
        this.verifierId = verifierId;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public String getVerifierType() {
        return verifierType;
    }

    public void setVerifierType(String verifierType) {
        this.verifierType = verifierType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getVerifierName() {
        return verifierName;
    }

    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((isDefault == null) ? 0 : isDefault.hashCode());
        result = prime * result + priority;
        result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
        result = prime * result + ((verificationType == null) ? 0 : verificationType.hashCode());
        result = prime * result + ((verifierId == null) ? 0 : verifierId.hashCode());
        result = prime * result + ((verifierName == null) ? 0 : verifierName.hashCode());
        result = prime * result + ((verifierType == null) ? 0 : verifierType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AttributeVerifierTO other = (AttributeVerifierTO) obj;
        if (isDefault == null) {
            if (other.isDefault != null)
                return false;
        }
        else if (!isDefault.equals(other.isDefault))
            return false;
        if (priority != other.priority)
            return false;
        if (sourceType == null) {
            if (other.sourceType != null)
                return false;
        }
        else if (!sourceType.equals(other.sourceType))
            return false;
        if (verificationType == null) {
            if (other.verificationType != null)
                return false;
        }
        else if (!verificationType.equals(other.verificationType))
            return false;
        if (verifierId == null) {
            if (other.verifierId != null)
                return false;
        }
        else if (!verifierId.equals(other.verifierId))
            return false;
        if (verifierName == null) {
            if (other.verifierName != null)
                return false;
        }
        else if (!verifierName.equals(other.verifierName))
            return false;
        if (verifierType == null) {
            if (other.verifierType != null)
                return false;
        }
        else if (!verifierType.equals(other.verifierType))
            return false;
        return true;
    }
}
