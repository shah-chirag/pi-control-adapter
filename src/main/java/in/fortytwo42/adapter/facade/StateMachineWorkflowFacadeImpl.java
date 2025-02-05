package in.fortytwo42.adapter.facade;

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
import in.fortytwo42.adapter.transferobj.StateMachineWorkFlowWETO;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.webentities.AttemptTypeWE;
import in.fortytwo42.enterprise.extension.webentities.ChallengeWE;
import in.fortytwo42.enterprise.extension.webentities.StateMachineWorkFlowWE;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;

public class StateMachineWorkflowFacadeImpl implements StateMachineWorkflowFacadeIntf {

    private String STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG = "<<<<< StateMachineWorkflowFacadeImpl";

    private static Logger logger= LogManager.getLogger(StateMachineWorkflowFacadeImpl.class);

    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final StateMachineWorkflowFacadeImpl INSTANCE = new StateMachineWorkflowFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }
    public static StateMachineWorkflowFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public StateMachineWorkFlowWETO onboardStateMachineWorkFlow(StateMachineWorkFlowWETO stateMachineWorkFlowWETO,String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " onboardStateMachineWorkFlow : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            iamExtensionService.validateStateMachineWorkFlow(stateMachineWorkFlowWETO.getStateMachineWorkFlow());
            stateMachineWorkFlowWETO = requestService.createStateMachineWorkFlowOnboardRequest(session,stateMachineWorkFlowWETO, actor,id, saveRequest);
            if (!saveRequest) {
                stateMachineWorkFlowWETO = approveOnboardStateMachineWorkFlow(session, stateMachineWorkFlowWETO, actor);
            }
            stateMachineWorkFlowWETO.setStatus(Constant.SUCCESS_STATUS);
//            stateMachineWorkFlowWETO.setId(stateMachineWorkFlowWETO.getId());
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
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " onboardStateMachineWorkFlow : end");
        return stateMachineWorkFlowWETO;
    }

    @Override
    public StateMachineWorkFlowWETO approveOnboardStateMachineWorkFlow(Session session, StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor) throws AuthException {
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " approveOnboardStateMachineWorkFlow : start");
        AuditLogUtil.sendAuditLog(stateMachineWorkFlowWETO.getStateMachineWorkFlow().getAttemptType()+ "state machine workflow onboard request approved ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        stateMachineWorkFlowWETO.setStateMachineWorkFlow(iamExtensionService.onboardStateMachineWorkFlow(stateMachineWorkFlowWETO.getStateMachineWorkFlow()));
        stateMachineWorkFlowWETO.setStatus(Constant.SUCCESS_STATUS);
        AuditLogUtil.sendAuditLog(stateMachineWorkFlowWETO.getStateMachineWorkFlow().getAttemptType()+ "state machine workflow onboarded  successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " approveOnboardStateMachineWorkFlow : start");
        return stateMachineWorkFlowWETO;
    }

    @Override
    public StateMachineWorkFlowWETO updateStateMachineWorkFlow(StateMachineWorkFlowWETO stateMachineWorkFlowWETO,String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " updateStateMachineWorkFlow : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            iamExtensionService.validateStateMachineWorkFlow(stateMachineWorkFlowWETO.getStateMachineWorkFlow());
            stateMachineWorkFlowWETO = requestService.createStateMachineWorkFlowUpdateRequest(session,stateMachineWorkFlowWETO, actor,id, saveRequest);
            if(!saveRequest) {
                stateMachineWorkFlowWETO = approveUpdateStateMachineWorkFlow(session, stateMachineWorkFlowWETO, actor);
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
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " updateStateMachineWorkFlow : end");
        return stateMachineWorkFlowWETO;
    }

    @Override
    public StateMachineWorkFlowWETO approveUpdateStateMachineWorkFlow(Session session, StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor) throws AuthException {
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " approveUpdateStateMachineWorkFlow : start");
        AuditLogUtil.sendAuditLog(stateMachineWorkFlowWETO.getStateMachineWorkFlow().getAttemptType()+ "state machine  workflow edit request approved ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        stateMachineWorkFlowWETO.setStateMachineWorkFlow(iamExtensionService.updateStateMachineWorkFlow(stateMachineWorkFlowWETO.getStateMachineWorkFlow()));
        stateMachineWorkFlowWETO.setStatus(Constant.SUCCESS_STATUS);
        AuditLogUtil.sendAuditLog(stateMachineWorkFlowWETO.getStateMachineWorkFlow().getAttemptType()+ "state machine  workflow edited successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " approveUpdateStateMachineWorkFlow : start");
        return stateMachineWorkFlowWETO;
    }

    @Override
    public PaginatedTO<StateMachineWorkFlowWE> getAllStateMachineWorkflows(Integer page, Integer pageSize, String searchQuery, String accountId) throws AuthException {
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " getAllStateMachineWorkflows : start");
        PaginatedTO<StateMachineWorkFlowWE> paginatedList = iamExtensionService.getAllStateMachineWorkflows(page, pageSize, searchQuery, accountId);
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " getAllStateMachineWorkflows : end");
        return paginatedList;
    }

    @Override
    public PaginatedTO<AttemptTypeWE> getAllAttemptTypes(Integer page, Integer pageSize) throws AuthException {
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " getAllStateMachineWorkflows : start");
        PaginatedTO<AttemptTypeWE> paginatedList = iamExtensionService.getAllAttemptTypes(page, pageSize);
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " getAllStateMachineWorkflows : end");
        return paginatedList;
    }



    @Override
    public List<ChallengeWE> getAllChallengeTypes() throws AuthException {
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " getAllChallengeTypes : start");
        List<ChallengeWE> list = iamExtensionService.getAllChallengeTypes();
        logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_FACADE_IMPL_LOG + " getAllChallengeTypes : end");
        return list;
    }
}
