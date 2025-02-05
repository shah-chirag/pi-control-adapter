package in.fortytwo42.adapter.transferobj;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.entities.enums.UserRole;
import lombok.ToString;

@JsonInclude(value = Include.NON_NULL)
@ToString
public class PasswordTO {

    private String password;

    private String accountObjectId;

    private UserRole accountType;
    
    public PasswordTO() {
        super();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountObjectId() {
        return accountObjectId;
    }

    public void setAccountObjectId(String accountObjectId) {
        this.accountObjectId = accountObjectId;
    }

    public UserRole getAccountType() {
        return accountType;
    }

    public void setAccountType(UserRole accountType) {
        this.accountType = accountType;
    }

}
