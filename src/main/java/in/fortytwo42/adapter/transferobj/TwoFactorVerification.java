package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class TwoFactorVerification {

    private String cryptoToken;
    
    private String username;

    public String getCryptoToken() {
        return cryptoToken;
    }

    public String getUsername() {
        return username;
    }

    public void setCryptoToken(String cryptoToken) {
        this.cryptoToken = cryptoToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    
}
