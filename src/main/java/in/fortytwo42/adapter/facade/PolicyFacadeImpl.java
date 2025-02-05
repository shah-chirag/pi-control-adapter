
package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.PolicyWeTO;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.enums.PasswordPolicyRuleType;
import in.fortytwo42.enterprise.extension.webentities.PasswordPolicyValidationRuleWE;
import in.fortytwo42.enterprise.extension.webentities.PolicyWE;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;

// TODO: Auto-generated Javadoc
/**
 * The Class PolicyFacadeImpl.
 */
public class PolicyFacadeImpl implements PolicyFacadeIntf {

    /** The build details facade impl log. */
    private String POLICY_FACADE_IMPL_LOG = "<<<<< PolicyFacadeImpl";

    private static Logger logger= LogManager.getLogger(PolicyFacadeImpl.class);

    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final PolicyFacadeImpl INSTANCE = new PolicyFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of BuildDetailsFacadeImpl.
     *
     * @return single instance of BuildDetailsFacadeImpl
     */
    public static PolicyFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public PolicyWeTO onboardPolicy(PolicyWeTO policyWE, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " onboardPolicy : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            iamExtensionService.validatePolicy(policyWE.getPolicy());
            policyWE = requestService.createPolicyOnboardRequest(session, policyWE, actor,id,saveRequest);
            if (!saveRequest) {
                policyWE = approveOnboardPolicy(session, policyWE, actor);
            }
            policyWE.setStatus(Constant.SUCCESS_STATUS);
            policyWE.setId(policyWE.getId());
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " onboardPolicy : end");
        return policyWE;
    }

    @Override
    public PolicyWeTO approveOnboardPolicy(Session session, PolicyWeTO policyWE, String actor) throws AuthException {
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " onboardPolicy : start");
        AuditLogUtil.sendAuditLog(policyWE.getPolicy().getPolicyName() + " policy onboard request approved ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        policyWE.setPolicy(iamExtensionService.onboardPolicy(policyWE.getPolicy()));
        policyWE.setStatus(Constant.SUCCESS_STATUS);
        policyWE.setId(policyWE.getId());
        AuditLogUtil.sendAuditLog(policyWE.getPolicy().getPolicyName() + " policy onboarded successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " onboardPolicy : start");
        return policyWE;
    }

    @Override
    public PolicyWeTO editPolicy(PolicyWeTO policyWE, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " editPolicy : start");
        Session session = sessionFactoryUtil.getSession();
        try {        
            iamExtensionService.validatePolicy(policyWE.getPolicy());
            policyWE = requestService.createPolicyEditRequest(session, policyWE, actor,id, saveRequest);
            if(!saveRequest) {
                policyWE = approveEditPolicy(session, policyWE, actor);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " editPolicy : end");
        return policyWE;
    }

    @Override
    public PolicyWeTO approveEditPolicy(Session session, PolicyWeTO policyWE, String actor) throws AuthException {
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " editPolicy : start");
        AuditLogUtil.sendAuditLog(policyWE.getPolicy().getPolicyName() + " policy edit request approved ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        policyWE.setPolicy(iamExtensionService.editPolicy(policyWE.getPolicy()));
        policyWE.setStatus(Constant.SUCCESS_STATUS);
        policyWE.setId(policyWE.getId());
        AuditLogUtil.sendAuditLog(policyWE.getPolicy().getPolicyName() + "  edit policy  successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " editPolicy : start");
        return policyWE;
    }

    @Override
    public List<PolicyWE> getAllPolicies() throws AuthException {
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " getAllPolicies : start");
        List<PolicyWE> policies = iamExtensionService.getAllPolicies();
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " getAllPolicies : end");
        return policies;
    }
    
    @Override
    public List<PasswordPolicyValidationRuleWE> getValidationRules() throws AuthException {
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " getValidationRules : start");
        List<PasswordPolicyValidationRuleWE> passwordPolicyValidationRules = new ArrayList<>();
        for (PasswordPolicyRuleType passwordPolicyRuleType : PasswordPolicyRuleType.values()) {
            PasswordPolicyValidationRuleWE passwordPolicyValidationRule = new PasswordPolicyValidationRuleWE();
            passwordPolicyValidationRule.setType(passwordPolicyRuleType);
            passwordPolicyValidationRule.setDescription(passwordPolicyRuleType.getDescription());
            passwordPolicyValidationRules.add(passwordPolicyValidationRule); 
        }
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " getValidationRules : end");
        return passwordPolicyValidationRules;
    }

    @Override
    public PaginatedTO<PolicyWE> getAllPolicies(Integer page, Integer pageSize) throws AuthException {
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " getAllPolicies : start");
        PaginatedTO<PolicyWE> policies = iamExtensionService.getAllPolicies(page, pageSize);
        logger.log(Level.DEBUG, POLICY_FACADE_IMPL_LOG + " getAllPolicies : end");
        return policies;
    }
    
}
