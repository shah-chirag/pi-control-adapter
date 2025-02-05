
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.AttributeDataTO;
import lombok.ToString;

@JsonInclude(value = Include.NON_NULL)
@ToString
public class QRCodeDataTO {

    private AttributeTO searchAttribute;

    private List<AttributeTO> customeAttributes;

    private String qrCode;

    private String authCode;

    private String applicationId;

    private String applicationSecrete;
    
    private String transactionId;

    private String transactionDetails;

    private String transactionSummary;

    private String transactionNote;

    private Long expiryTime;

    private String prefix;

    private Boolean isPinCheckRequired;

    private String status;

    private String profileName;

    private List<AttributeDataTO> searchAttributes;

    private String algorithm;

    private Integer numberOfDigits;

    private Long totpExpiry;

    public List<AttributeDataTO> getSearchAttributes() {
        return searchAttributes;
    }

    public void setSearchAttributes(List<AttributeDataTO> searchAttributes) {
        this.searchAttributes = searchAttributes;
    }

    public AttributeTO getSearchAttribute() {
        return searchAttribute;
    }

    public void setSearchAttribute(AttributeTO searchAttribute) {
        this.searchAttribute = searchAttribute;
    }

    public List<AttributeTO> getCustomeAttributes() {
        return customeAttributes;
    }

    public void setCustomeAttributes(List<AttributeTO> customeAttributes) {
        this.customeAttributes = customeAttributes;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public String getTransactionSummary() {
        return transactionSummary;
    }

    public void setTransactionSummary(String transactionSummary) {
        this.transactionSummary = transactionSummary;
    }

    public String getTransactionNote() {
        return transactionNote;
    }

    public void setTransactionNote(String transactionNote) {
        this.transactionNote = transactionNote;
    }

    public Boolean getPinCheckRequired() {
        return isPinCheckRequired;
    }

    public void setPinCheckRequired(Boolean pinCheckRequired) {
        isPinCheckRequired = pinCheckRequired;
    }

    public String getStatus() {
        return status;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Integer getNumberOfDigits() {
        return numberOfDigits;
    }

    public Long getTotpExpiry() {
        return totpExpiry;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setNumberOfDigits(Integer numberOfDigits) {
        this.numberOfDigits = numberOfDigits;
    }

    public void setTotpExpiry(Long totpExpiry) {
        this.totpExpiry = totpExpiry;
    }

}
