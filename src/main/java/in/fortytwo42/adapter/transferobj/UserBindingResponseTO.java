package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.ToString;

@JsonInclude(value = Include.NON_NULL)
@ToString
public class UserBindingResponseTO {

    private String consumerId;
    
    private String txnStatus;
    
    private List<String> usernames;
    
    private String userStatus;
    
    private String username;

    public UserBindingResponseTO() {
        super();
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String mobileNo) {
        this.consumerId = mobileNo;
    }

    public String getTxnStatus() {
		return txnStatus;
	}

	public void setTxnStatus(String txnStatus) {
		this.txnStatus = txnStatus;
	}

	public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public List<String> getUsernames() {
		return usernames;
	}

	public void setUsernames(List<String> usernames) {
		this.usernames = usernames;
	}

	public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    
    
    
}
