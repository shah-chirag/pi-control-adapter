package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class TwoFactorVerificationTO {
	
	private String cryptoToken;

	public String getCryptoToken() {
		return cryptoToken;
	}

	public void setCryptoToken(String cryptoToken) {
		this.cryptoToken = cryptoToken;
	}
}
