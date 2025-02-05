package in.fortytwo42.adapter.facade;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AccountCustomStateMachineWETO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.enterprise.extension.webentities.AccountCustomStateMachineWE;


public interface AccountCustomStateMachineFacadeIntf {
    AccountCustomStateMachineWETO onboardAccountCustomStateMachine(AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor,Long id, boolean saveRequest) throws AuthException;

    AccountCustomStateMachineWETO approveOnboardAccountCustomStateMachine(Session session, AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor) throws AuthException;

    AccountCustomStateMachineWETO updateAccountCustomStateMachine(AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor,Long id, boolean saveRequest) throws AuthException;

    AccountCustomStateMachineWETO approveUpdateAccountCustomStateMachine(Session session, AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor) throws AuthException;

    PaginatedTO<AccountCustomStateMachineWE> getAllAccountCustomStateMachines(Integer page, Integer pageSize, String searchQuery, String accountId) throws AuthException;


}
