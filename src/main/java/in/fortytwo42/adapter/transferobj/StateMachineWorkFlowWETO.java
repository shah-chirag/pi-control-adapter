package in.fortytwo42.adapter.transferobj;

import in.fortytwo42.enterprise.extension.webentities.StateMachineWorkFlowWE;
import lombok.ToString;

@ToString
public class StateMachineWorkFlowWETO {

    private StateMachineWorkFlowWE stateMachineWorkFlow;

    private String state;

    private String comments;

    private String status;

    private Long id;

    public StateMachineWorkFlowWE getStateMachineWorkFlow() {
        return stateMachineWorkFlow;
    }

    public void setStateMachineWorkFlow(StateMachineWorkFlowWE stateMachineWorkFlow) {
        this.stateMachineWorkFlow = stateMachineWorkFlow;
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
