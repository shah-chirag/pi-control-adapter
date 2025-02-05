
package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientLicenseTO {

	private int licenseVersion;
	private int numberOfApplications;
	private int numberOfUsers;
	private long expiryTimestamp;

	public long getAdditionalGracePeriod() {
		return additionalGracePeriod;
	}

	public void setAdditionalGracePeriod(long additionalGracePeriod) {
		this.additionalGracePeriod = additionalGracePeriod;
	}

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private long additionalGracePeriod;

	public void setLicenseVersion(int licenseVersion) {
		this.licenseVersion = licenseVersion;
	}

	public void setNumberOfApplications(int numberOfApplications) {
		this.numberOfApplications = numberOfApplications;
	}

	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}

	public void setExpiryTimestamp(long expiryTimestamp) {
		this.expiryTimestamp = expiryTimestamp;
	}

	public int getLicenseVersion() {
		return licenseVersion;
	}

	public int getNumberOfApplications() {
		return numberOfApplications;
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	public long getExpiryTimestamp() {
		return expiryTimestamp;
	}
}
