package in.fortytwo42.adapter.transferobj;

import in.fortytwo42.enterprise.extension.webentities.PolicyWE;
import lombok.ToString;

@ToString
public class PolicyWeTO {

    private PolicyWE policy;
    
    private String state;
    
    private String comments;
    
    private String status;

    private Long id;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PolicyWE getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyWE policyWE) {
        this.policy = policyWE;
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

    
    
}
