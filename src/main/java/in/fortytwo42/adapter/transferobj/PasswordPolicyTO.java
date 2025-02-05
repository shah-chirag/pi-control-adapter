
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.adapter.enums.PasswordCharacterType;

@JsonInclude(value = Include.NON_NULL)
public class PasswordPolicyTO extends BaseTO {

    private List<PasswordPolicyValidationRuleTO> validationRules;

    private int minPasswordLength;

    private PasswordCharacterType passwordCharacterType;

    private Long expiryDuration;

    /**
     * Default Constructor
     */
    public PasswordPolicyTO() {
        super();
    }

    public List<PasswordPolicyValidationRuleTO> getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(List<PasswordPolicyValidationRuleTO> validationRules) {
        this.validationRules = validationRules;
    }

    public int getMinPasswordLength() {
        return minPasswordLength;
    }

    public void setMinPasswordLength(int minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }

    public PasswordCharacterType getPasswordCharacterType() {
        return passwordCharacterType;
    }

    public void setPasswordCharacterType(PasswordCharacterType passwordCharacterType) {
        this.passwordCharacterType = passwordCharacterType;
    }

    public Long getExpiryDuration() {
        return expiryDuration;
    }

    public void setExpiryDuration(Long expiryDuration) {
        this.expiryDuration = expiryDuration;
    }

}
