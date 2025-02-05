/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

import in.fortytwo42.adapter.enums.PasswordPolicyRuleType;

/**
 * @author ChiragShah
 *
 */
public class PasswordPolicyValidationRuleTO extends BaseTO {

    private String policyDescription;

    private PasswordPolicyRuleType passwordPolicyType;

    public String getPolicyDescription() {
        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
    }

    public PasswordPolicyRuleType getPasswordPolicyType() {
        return passwordPolicyType;
    }

    public void setPasswordPolicyType(PasswordPolicyRuleType passwordPolicyType) {
        this.passwordPolicyType = passwordPolicyType;
    }

}
