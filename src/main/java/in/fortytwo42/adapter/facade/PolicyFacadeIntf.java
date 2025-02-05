package  in.fortytwo42.adapter.facade;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.PolicyWeTO;
import in.fortytwo42.enterprise.extension.webentities.PasswordPolicyValidationRuleWE;
import in.fortytwo42.enterprise.extension.webentities.PolicyWE;

public interface PolicyFacadeIntf {

    PolicyWeTO onboardPolicy(PolicyWeTO policyWE, String actor,Long id,boolean saveRequest) throws AuthException;
    
    PolicyWeTO approveOnboardPolicy(Session session, PolicyWeTO policyWE, String actor) throws AuthException;
    
    PolicyWeTO editPolicy(PolicyWeTO policyWE, String actor,Long id,boolean saveRequest) throws AuthException;
    
    PolicyWeTO approveEditPolicy(Session session, PolicyWeTO policyWE, String actor) throws AuthException;
    
    List<PolicyWE> getAllPolicies() throws AuthException;
    
    List<PasswordPolicyValidationRuleWE> getValidationRules() throws AuthException;

    PaginatedTO<PolicyWE> getAllPolicies(Integer page, Integer pageSize) throws AuthException;
}
