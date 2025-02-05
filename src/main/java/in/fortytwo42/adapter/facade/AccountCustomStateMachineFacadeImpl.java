package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AccountCustomStateMachineWETO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.webentities.AccountCustomStateMachineWE;

public class AccountCustomStateMachineFacadeImpl implements AccountCustomStateMachineFacadeIntf {


    private String ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG = "<<<<< AccountCustomStateMachineFacadeImpl";

    private static Logger logger= LogManager.getLogger(AccountCustomStateMachineFacadeImpl.class.getName());

    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    @Override
    public AccountCustomStateMachineWETO onboardAccountCustomStateMachine(AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " onboardAccountCustomStateMachine : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            iamExtensionService.validateAccountCustomStateMachine(accountCustomStateMachineWETO.getAccountCustomStateMachine());
            accountCustomStateMachineWETO = requestService.createAccountCustomStateMachineOnboardRequest(session,accountCustomStateMachineWETO, actor,id, saveRequest);
            if (!saveRequest) {
                accountCustomStateMachineWETO = approveOnboardAccountCustomStateMachine(session, accountCustomStateMachineWETO, actor);
            }
            accountCustomStateMachineWETO.setStatus(Constant.SUCCESS_STATUS);
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
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " onboardAccountCustomStateMachine : end");
        return accountCustomStateMachineWETO;
    }

    @Override
    public AccountCustomStateMachineWETO approveOnboardAccountCustomStateMachine(Session session, AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor) throws AuthException {
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " approveOnboardAccountCustomStateMachine : start");
        accountCustomStateMachineWETO.setAccountCustomStateMachine(iamExtensionService.onboardAccountCustomStateMachine(accountCustomStateMachineWETO.getAccountCustomStateMachine()));
        accountCustomStateMachineWETO.setStatus(Constant.SUCCESS_STATUS);
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " approveOnboardAccountCustomStateMachine : start");
        return accountCustomStateMachineWETO;
    }

    @Override
    public AccountCustomStateMachineWETO updateAccountCustomStateMachine(AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " updateAccountCustomStateMachine : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            iamExtensionService.validateAccountCustomStateMachine(accountCustomStateMachineWETO.getAccountCustomStateMachine());
            accountCustomStateMachineWETO = requestService.createAccountCustomStateMachineUpdateRequest(session,accountCustomStateMachineWETO, actor,id, saveRequest);
            if(!saveRequest) {
                accountCustomStateMachineWETO = approveUpdateAccountCustomStateMachine(session, accountCustomStateMachineWETO, actor);
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
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " updateAccountCustomStateMachine : end");
        return accountCustomStateMachineWETO;
    }

    @Override
    public AccountCustomStateMachineWETO approveUpdateAccountCustomStateMachine(Session session, AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor) throws AuthException {
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " approveUpdateAccountCustomStateMachine : start");
        accountCustomStateMachineWETO.setAccountCustomStateMachine(iamExtensionService.updateAccountCustomStateMachine(accountCustomStateMachineWETO.getAccountCustomStateMachine()));
        accountCustomStateMachineWETO.setStatus(Constant.SUCCESS_STATUS);
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " approveUpdateAccountCustomStateMachine : start");
        return accountCustomStateMachineWETO;
    }

    @Override
    public PaginatedTO<AccountCustomStateMachineWE> getAllAccountCustomStateMachines(Integer page, Integer pageSize, String searchQuery, String accountId) throws AuthException {
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " getAllAccountCustomStateMachines : start");
        PaginatedTO<AccountCustomStateMachineWE> paginatedList = iamExtensionService.getAllAccountCustomStateMachine(page, pageSize, searchQuery, accountId);
        logger.log(Level.DEBUG, ACCOUNT_CUSTOM_STATE_MACHINE_FACADE_IMPL_LOG + " getAllAccountCustomStateMachines : end");
        return paginatedList;
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final AccountCustomStateMachineFacadeImpl INSTANCE = new AccountCustomStateMachineFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }
    public static AccountCustomStateMachineFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

}
