package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.adapter.enums.OnboardUserType;
import in.fortytwo42.adapter.enums.VerificationStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.QuestionAnswerTO;

@JsonInclude(value = Include.NON_NULL)
public class UserIciciTO {

    private Long id;
    private String status;
    public String getAccountType() {
        return accountType;
    }
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    private String accountType;
    private List<AttributeDataTO> attributeData;
    private List<AttributeDataTO> searchAttributes;
    private String callStatus;
    private String applicationId;
    private String applicationSecrete;
    private VerificationStatus verificationStatus;
    private Boolean isCamEnabled;
    private List<ApplicationTO> applications;
    private List<ApplicationTO> subscribedApplications;
    private String userCredential;
    private List<QuestionAnswerTO> questionAnswers;
    private String errorMessage;
    private Boolean isCredentialsThroughEmail;
    private Long errorCode;
    private String authType;

    private OnboardUserType onboardUserType;

    private Boolean iskcIdUpdated;

    public UserIciciTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AttributeDataTO> getAttributeData() {
        return attributeData;
    }

    public void setAttributeData(List<AttributeDataTO> attributeData) {
        this.attributeData = attributeData;
    }

    public List<AttributeDataTO> getSearchAttributes() {
        return searchAttributes;
    }

    public void setSearchAttributes(List<AttributeDataTO> searchAttributes) {
        this.searchAttributes = searchAttributes;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
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

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public List<ApplicationTO> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationTO> applications) {
        this.applications = applications;
    }

    public List<ApplicationTO> getSubscribedApplications() {
        return subscribedApplications;
    }

    public void setSubscribedApplications(List<ApplicationTO> subscribedApplications) {
        this.subscribedApplications = subscribedApplications;
    }

    public String getUserCredential() {
        return userCredential;
    }

    public void setUserCredential(String userCredential) {
        this.userCredential = userCredential;
    }

    public Boolean getCamEnabled() {
        return isCamEnabled;
    }

    public void setCamEnabled(Boolean camEnabled) {
        isCamEnabled = camEnabled;
    }

    public List<QuestionAnswerTO> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(List<QuestionAnswerTO> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getCredentialsThroughEmail() {
        return isCredentialsThroughEmail;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Long errorCode) {
        this.errorCode = errorCode;
    }

    public void setCredentialsThroughEmail(Boolean credentialsThroughEmail) {
        isCredentialsThroughEmail = credentialsThroughEmail;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public OnboardUserType getOnboardUserType() {
        return onboardUserType;
    }

    public void setOnboardUserType(OnboardUserType onboardUserType) {
        this.onboardUserType = onboardUserType;
    }

    public Boolean isIskcIdUpdated() {
        return iskcIdUpdated;
    }

    public void setIskcIdUpdated(Boolean iskcIdUpdated) {
        this.iskcIdUpdated = iskcIdUpdated;
    }

    @Override
    public String toString() {
        return "UserIciciTO [" +
                (id != null ? ("id=" + id) : "") +
                (status != null ? (", status='" + status + "'") : "") +
                (accountType != null ? (", accountType='" + accountType + "'") : "") +
                (attributeData != null ? (", attributeData=" + attributeData) : "") +
                (searchAttributes != null ? (", searchAttributes=" + searchAttributes) : "") +
                (callStatus != null ? (", callStatus='" + callStatus + "'") : "") +
                (applicationId != null ? (", applicationId='" + applicationId + "'") : "") +
                (applicationSecrete != null ? (", applicationSecrete='" + applicationSecrete + "'") : "") +
                (verificationStatus != null ? (", verificationStatus=" + verificationStatus) : "") +
                (isCamEnabled != null ? (", isCamEnabled=" + isCamEnabled) : "") +
                (applications != null ? (", applications=" + applications) : "") +
                (subscribedApplications != null ? (", subscribedApplications=" + subscribedApplications) : "") +
                (userCredential != null ? (", userCredential='" + userCredential + "'") : "") +
                (questionAnswers != null ? (", questionAnswers=" + questionAnswers) : "") +
                (errorMessage != null ? (", errorMessage='" + errorMessage + "'") : "") +
                (isCredentialsThroughEmail != null ? (", isCredentialsThroughEmail=" + isCredentialsThroughEmail) : "") +
                (errorCode != null ? (", errorCode=" + errorCode) : "") +
                (authType != null ? (", authType='" + authType + "'") : "") +
                (onboardUserType != null ? (", onboardUserType=" + onboardUserType) : "") +
                "]";
    }
}
