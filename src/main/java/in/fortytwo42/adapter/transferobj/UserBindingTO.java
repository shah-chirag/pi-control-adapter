/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.ApplicationTO;
import lombok.ToString;

/**
 * @author ChiragShah
 *
 */
@JsonInclude(value = Include.NON_NULL)
@ToString
public class UserBindingTO {

    private Long id;

	private String mobile;

	private String username;

	private String status;

	private ApplicationTO application;
	
	private String comments;

	private String userKcId;

	private String clientKcId;

	private String roleName;// it is same as application name

	private Boolean userConsentRequired;

	public UserBindingTO() {
		super();
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ApplicationTO getApplication() {
		return application;
	}

	public void setApplication(ApplicationTO application) {
		this.application = application;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getUserKcId() {
		return userKcId;
	}

	public void setUserKcId(String userKcId) {
		this.userKcId = userKcId;
	}

	public String getClientKcId() {
		return clientKcId;
	}

	public void setClientKcId(String clientKcId) {
		this.clientKcId = clientKcId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Boolean getUserConsentRequired() {
		return userConsentRequired;
	}

	public void setUserConsentRequired(Boolean userConsentRequired) {
		this.userConsentRequired = userConsentRequired;
	}
}
