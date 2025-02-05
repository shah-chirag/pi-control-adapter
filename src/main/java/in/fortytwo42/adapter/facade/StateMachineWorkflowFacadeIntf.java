package in.fortytwo42.adapter.facade;
import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.StateMachineWorkFlowWETO;
import in.fortytwo42.enterprise.extension.webentities.AttemptTypeWE;
import in.fortytwo42.enterprise.extension.webentities.ChallengeWE;
import in.fortytwo42.enterprise.extension.webentities.StateMachineWorkFlowWE;

public interface StateMachineWorkflowFacadeIntf {
    StateMachineWorkFlowWETO onboardStateMachineWorkFlow(StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor,Long id, boolean saveRequest) throws AuthException;

    StateMachineWorkFlowWETO approveOnboardStateMachineWorkFlow(Session session, StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor) throws AuthException;

    StateMachineWorkFlowWETO updateStateMachineWorkFlow(StateMachineWorkFlowWETO stateMachineWorkFlowWETO,
                                                        String actor,Long id,boolean saveRequest) throws AuthException;

    StateMachineWorkFlowWETO approveUpdateStateMachineWorkFlow(Session session, StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor) throws AuthException;

    PaginatedTO<StateMachineWorkFlowWE> getAllStateMachineWorkflows(Integer page, Integer pageSize, String searchQuery, String accountId) throws AuthException;

    PaginatedTO<AttemptTypeWE> getAllAttemptTypes(Integer page, Integer pageSize) throws AuthException;


    List<ChallengeWE> getAllChallengeTypes() throws AuthException;
}
