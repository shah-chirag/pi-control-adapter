package in.fortytwo42.adapter.transferobj;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class UserResponseTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String username;

    private String status;

    private String token;
    
    private String fullName;
    
    private String approvalStatus;
    private Boolean mfaEnabled;
    private Integer tokenTtl;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}
	
	public String getFullName() {
		return fullName;
	}

    public Boolean getMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(Boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public Integer getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Integer tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    @Override
    public String toString() {
        return "UserTO [username=" + username + ", token=" + token + ", status=" + status + ", mfaEnabled=" + mfaEnabled + ", tokenTtl=" + tokenTtl +"]";
    }    
}
