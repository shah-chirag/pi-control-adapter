package in.fortytwo42.adapter.transferobj;

import in.fortytwo42.enterprise.extension.webentities.AccountCustomStateMachineWE;
import lombok.ToString;

@ToString
public class AccountCustomStateMachineWETO {


    private AccountCustomStateMachineWE accountCustomStateMachine;

    private String state;

    private String comments;

    private String status;

    private Long id;

    public AccountCustomStateMachineWE getAccountCustomStateMachine() {
        return accountCustomStateMachine;
    }

    public void setAccountCustomStateMachine(AccountCustomStateMachineWE accountCustomStateMachine) {
        this.accountCustomStateMachine = accountCustomStateMachine;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
