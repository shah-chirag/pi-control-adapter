
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.adapter.enums.State;
import in.fortytwo42.tos.transferobj.UserTO;

@JsonInclude(value = Include.NON_NULL)
public class DeviceTO {

    private Long id;
    
    private String deviceId;

    private State deviceState;

    //private String deviceDetails;
    private List<KeyValueTO> deviceDetails;

    private String deviceUDID;

    private String policyId;

    private List<TokenTO> tokens;

    private String state;

    private String comments;

    private String status;
    
    private List<String> accountIds;
    
    private String deviceName;
  
    private List<UserTO> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public State getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(State deviceState) {
        this.deviceState = deviceState;
    }

    public List<KeyValueTO> getDeviceDetails() {
        return deviceDetails;
    }

    public void setDeviceDetails(List<KeyValueTO> deviceDetails) {
        this.deviceDetails = deviceDetails;
    }

    public String getDeviceUDID() {
        return deviceUDID;
    }

    public void setDeviceUDID(String deviceUDID) {
        this.deviceUDID = deviceUDID;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public List<TokenTO> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenTO> tokens) {
        this.tokens = tokens;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public List<UserTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserTO> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "DeviceTO [" +
                (id != null ? ("id=" + id) : "") +
                (deviceId != null ? (", deviceId='" + deviceId + "'") : "") +
                (deviceState != null ? (", deviceState=" + deviceState) : "") +
                (deviceDetails != null ? (", deviceDetails=" + deviceDetails) : "") +
                (deviceUDID != null ? (", deviceUDID=" + deviceUDID) : "") +
                (policyId != null ? (", policyId=" + policyId) : "") +
                (tokens != null ? (", tokens='" + tokens + "'") : "") +
                (state != null ? (", state='" + state + "'") : "") +
                (comments != null ? (", comments='" + comments + "'") : "") +
                (status != null ? (", status='" + status + "'") : "") +
                (accountIds != null ? (", accountIds='" + accountIds + "'") : "") +
                (deviceName != null ? (", deviceName='" + deviceName + "'") : "") +
                (users != null ? (", users='" + users + "'") : "") +
                "]";
    }
}
