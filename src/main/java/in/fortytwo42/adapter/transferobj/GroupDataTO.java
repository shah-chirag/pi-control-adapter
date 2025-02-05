package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class GroupDataTO {

    private String groupName;
    
    private Boolean isMakerApprovalRequired;

    private Integer maximumAllowedHostsPerUser;
    
    private String status;

    public String getGroupName() {
        return groupName;
    }

    public Boolean getIsMakerApprovalRequired() {
        return isMakerApprovalRequired;
    }

    public Integer getMaximumAllowedHostsPerUser() {
        return maximumAllowedHostsPerUser;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setIsMakerApprovalRequired(Boolean isMakerApprovalRequired) {
        this.isMakerApprovalRequired = isMakerApprovalRequired;
    }

    public void setMaximumAllowedHostsPerUser(Integer maximumAllowedHostsPerUser) {
        this.maximumAllowedHostsPerUser = maximumAllowedHostsPerUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
 
}
