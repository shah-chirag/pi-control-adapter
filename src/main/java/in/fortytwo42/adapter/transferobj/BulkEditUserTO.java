package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.UserTO;
import lombok.ToString;

@JsonInclude(value = Include.NON_NULL)
@ToString
public class BulkEditUserTO {

    private String approvalStatus;
    
    private List<UserTO> userList;

    private String twoFactorStatus;

    private String userStatus;

    private int updateCount;

    private int failureCount;
    
    private String comments;
    
    private String status;

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public List<UserTO> getUserList() {
        return userList;
    }

    public void setUserList(List<UserTO> userList) {
        this.userList = userList;
    }

    public String getTwoFactorStatus() {
        return twoFactorStatus;
    }

    public void setTwoFactorStatus(String twoFactorStatus) {
        this.twoFactorStatus = twoFactorStatus;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

    
}
