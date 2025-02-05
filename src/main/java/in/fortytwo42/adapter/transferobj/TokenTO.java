
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.fortytwo42.adapter.enums.State;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class TokenTO {

    private Long id;
    
    private String tokenId;

    private String type;

    private String tokenUDID;

    private State state;

    private List<String> accountIds;

    private String comments;

    private String status;
    
    private Long dateTimeCreated;
    
    private Long dateTimeModified;
    
    private String tokenName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTokenUDID() {
        return tokenUDID;
    }

    public void setTokenUDID(String tokenUDID) {
        this.tokenUDID = tokenUDID;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
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

    public Long getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(Long dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }

    public Long getDateTimeModified() {
        return dateTimeModified;
    }

    public void setDateTimeModified(Long dateTimeModified) {
        this.dateTimeModified = dateTimeModified;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

}
