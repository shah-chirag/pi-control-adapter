package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.fortytwo42.tos.enums.Algorithm;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class TotpSettingsTO {
	private String applicationId;
	private String totpNoOfDigits;
	private String totpExpiry;
	private String totpHashingAlgorithm;

	public String getTotpNoOfDigits() {
		return totpNoOfDigits;
	}

	public void setTotpNoOfDigits(String totpNoOfDigits) {
		this.totpNoOfDigits = totpNoOfDigits;
	}

	public String getTotpExpiry() {
		return totpExpiry;
	}

	public void setTotpExpiry(String totpExpiry) {
		this.totpExpiry = totpExpiry;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getTotpHashingAlgorithm() {
		return totpHashingAlgorithm;
	}

	public void setTotpHashingAlgorithm(String totpHashingAlgorithm) {
		this.totpHashingAlgorithm = totpHashingAlgorithm;
	}
}
