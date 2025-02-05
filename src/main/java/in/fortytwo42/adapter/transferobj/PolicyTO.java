/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

import in.fortytwo42.adapter.enums.PolicyType;
import in.fortytwo42.adapter.enums.State;
import in.fortytwo42.enterprise.extension.enums.AccountType;

/**
 * @author Amandeep Singh
 *
 */
public class PolicyTO extends BaseTO {

    private String policyVersion;

    private PolicyType policyType;

    private State policyState;

    private Boolean isDefault;

    private PasswordPolicyTO onlinePasswordPolicy;

    private PasswordPolicyTO offlinePasswordPolicy;

    private FileStoragePolicyTO fileStoragePolicy;

    private TokenPolicyTO tokenPolicy;

    private DevicePolicyTO devicePolicy;

    private AccountType accountType;
    
    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public State getPolicyState() {
        return policyState;
    }

    public void setPolicyState(State policyState) {
        this.policyState = policyState;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public PasswordPolicyTO getOnlinePasswordPolicy() {
        return onlinePasswordPolicy;
    }

    public void setOnlinePasswordPolicy(PasswordPolicyTO onlinePasswordPolicy) {
        this.onlinePasswordPolicy = onlinePasswordPolicy;
    }

    public PasswordPolicyTO getOfflinePasswordPolicy() {
        return offlinePasswordPolicy;
    }

    public void setOfflinePasswordPolicy(PasswordPolicyTO offlinePasswordPolicy) {
        this.offlinePasswordPolicy = offlinePasswordPolicy;
    }

    public FileStoragePolicyTO getFileStoragePolicy() {
        return fileStoragePolicy;
    }

    public void setFileStoragePolicy(FileStoragePolicyTO fileStoragePolicy) {
        this.fileStoragePolicy = fileStoragePolicy;
    }

    public TokenPolicyTO getTokenPolicy() {
        return tokenPolicy;
    }

    public void setTokenPolicy(TokenPolicyTO tokenPolicy) {
        this.tokenPolicy = tokenPolicy;
    }

    public DevicePolicyTO getDevicePolicy() {
        return devicePolicy;
    }

    public void setDevicePolicy(DevicePolicyTO devicePolicy) {
        this.devicePolicy = devicePolicy;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
   
}
