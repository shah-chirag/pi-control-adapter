package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.AttributeDataTO;
import lombok.ToString;

@JsonInclude(value = Include.NON_NULL)
@ToString
public class EvidenceRequestTO {

	private Long id;
	
	private String userIdentifier;

	private String requestorIdentifier;

	private String requestorName;

	private String requestorId;
	
	private List<AttributeDataTO> attributes;

	private String approvalStatus;

	private String status;

	public String getUserIdentifier() {
		return userIdentifier;
	}

	public void setUserIdentifier(String userIdentifier) {
		this.userIdentifier = userIdentifier;
	}

	public String getRequestorIdentifier() {
		return requestorIdentifier;
	}

	public void setRequestorIdentifier(String requestorIdentifier) {
		this.requestorIdentifier = requestorIdentifier;
	}

	public String getRequestorName() {
		return requestorName;
	}

	public void setRequestorName(String requestorName) {
		this.requestorName = requestorName;
	}

	public String getRequestorId() {
		return requestorId;
	}

	public void setRequestorId(String requestorId) {
		this.requestorId = requestorId;
	}

	public List<AttributeDataTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDataTO> attributes) {
		this.attributes = attributes;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
