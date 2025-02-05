
package in.fortytwo42.adapter.jar.transferobj;


import in.fortytwo42.adapter.enums.SourceType;
import in.fortytwo42.adapter.enums.VerificationType;
import in.fortytwo42.adapter.enums.VerifierType;

public class AttributeVerifierTO {

    private VerificationType verificationType;
    private VerifierType verifierType;
    private String verifierId;
    private Boolean isDefault; //isDefault verifier for that attribute

    private SourceType sourceType;
    private String sourceId; //Enterprise id
    private int priority;
    private String verifierName;
    private Boolean isActive;

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getVerifierName() {
        return verifierName;
    }

    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    public VerificationType getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(VerificationType verificationType) {
        this.verificationType = verificationType;
    }

    public VerifierType getVerifierType() {
        return verifierType;
    }

    public void setVerifierType(VerifierType verifierType) {
        this.verifierType = verifierType;
    }

    public String getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(String verifierId) {
        this.verifierId = verifierId;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    

}
