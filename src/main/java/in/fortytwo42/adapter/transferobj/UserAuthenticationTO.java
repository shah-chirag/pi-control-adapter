
package in.fortytwo42.adapter.transferobj;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.AttributeDataTO;
import lombok.ToString;

@JsonInclude(value = Include.NON_NULL)
@ToString
public class UserAuthenticationTO implements Serializable {

    private static final long serialVersionUID = 1L;

    List<AttributeDataTO> attributes;
    
    private String password;

    /*
     * property to define whether user is online or offline
     */
    private String presence;

    /*
     * Authentication type can be simple, strong or any other standard hashing algo used by AD. 
     */
    private String authenticationType;
    
    private Integer authenticationTimeout;
        
    private String transactionSummary;
    
    private String transactionDetails;
    
    private String cryptoToken;
    
    private Boolean authenticationRequired;
    
    private String status;

    private String token;
    
    private String username;

    private  boolean isCredentialsEncrypted;

    public UserAuthenticationTO() {
        super();
    }

	public List<AttributeDataTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDataTO> attributes) {
		this.attributes = attributes;
	}

	public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public Integer getAuthenticationTimeout() {
        return authenticationTimeout;
    }

    public void setAuthenticationTimeout(Integer authenticationTimeout) {
        this.authenticationTimeout = authenticationTimeout;
    }

    public String getTransactionSummary() {
        return transactionSummary;
    }

    public void setTransactionSummary(String transactionSummary) {
        this.transactionSummary = transactionSummary;
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public String getCryptoToken() {
        return cryptoToken;
    }

    public void setCryptoToken(String cryptoToken) {
        this.cryptoToken = cryptoToken;
    }

    public Boolean getAuthenticationRequired() {
        return authenticationRequired;
    }

    public void setAuthenticationRequired(Boolean authenticationRequired) {
        this.authenticationRequired = authenticationRequired;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getIsCredentialsEncrypted() {
        return isCredentialsEncrypted;
    }

    public void setIsCredentialsEncrypted(boolean credentialsEncrypted) {
        isCredentialsEncrypted = credentialsEncrypted;
    }
}
