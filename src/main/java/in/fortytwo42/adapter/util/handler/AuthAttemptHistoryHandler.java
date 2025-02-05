package  in.fortytwo42.adapter.util.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.AuthenticationAttemptHistoryDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.AuthenticationAttemptHistory;

// TODO: Auto-generated Javadoc
/**
 * The Class AuthAttemptHistoryHandler.
 */
public class AuthAttemptHistoryHandler {

    /** The auth attempt history handler. */
    private String AUTH_ATTEMPT_HISTORY_HANDLER = "<<<<< AuthAttemptHistoryHandler";

    private Logger logger= LogManager.getLogger(AuthAttemptHistoryHandler.class);

    /** The authentication history dao. */
    private AuthenticationAttemptHistoryDaoIntf authenticationHistoryDao = DaoFactory.getAuthenticationHistoryDao();

    /** The pool. */
    private final ExecutorService pool;
    

    /**
     * The Class InstanceHolder.
     */
    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    /**
     * Instantiates a new auth attempt history handler.
     */
    private AuthAttemptHistoryHandler() {
        super();
        int poolSize = 1;
        pool = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final AuthAttemptHistoryHandler INSTANCE = new AuthAttemptHistoryHandler();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of AuthAttemptHistoryHandler.
     *
     * @return single instance of AuthAttemptHistoryHandler
     */
    public static AuthAttemptHistoryHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Log auth attempt history data.
     *
     * @param authenticationAttempt the authentication attempt
     */
    public void logAuthAttemptHistoryData(AuthenticationAttempt authenticationAttempt) {
        logger.log(Level.DEBUG, AUTH_ATTEMPT_HISTORY_HANDLER + " logAuthAttemptHistoryData : start");
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            Session session = sessionFactoryUtil.getSessionFactory().openSession();
            session.beginTransaction();
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            try {
                AuthenticationAttemptHistory authenticationAttemptHistory = new AuthenticationAttemptHistory(authenticationAttempt);
                authenticationHistoryDao.create(session, authenticationAttemptHistory);
                System.out.println("Session object "+session);
                System.out.println("******* Close session called from logAuthAttemptHistoryData******");
                sessionFactoryUtil.closeSession(session);
            }
            catch (Exception e) {
                session.getTransaction().rollback();
                logger.log(Level.ERROR, e.getMessage(), e);
            }finally {

                if (session.isOpen()) {
                    session.close();
                }
                logger.log(Level.DEBUG, AUTH_ATTEMPT_HISTORY_HANDLER + " logAuthAttemptHistoryData : end");
            }
        });
    }

    /**
     * Update auth attempt history data.
     *
     * @param authenticationAttempt the authentication attempt
     */
    public void updateAuthAttemptHistoryData(Session session, AuthenticationAttempt authenticationAttempt) {
        logger.log(Level.DEBUG, AUTH_ATTEMPT_HISTORY_HANDLER + " updateAuthAttemptHistoryData : start");
        try {
            authenticationHistoryDao.updateStatusByTransactionId(session, authenticationAttempt.getSenderAccountId(), authenticationAttempt.getTransactionId(), authenticationAttempt.getAttemptStatus());
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, AUTH_ATTEMPT_HISTORY_HANDLER + " updateAuthAttemptHistoryData : end");
    }

    /**
     * Update auth attempt history data by trasaction id.
     *
     * @param authenticationAttempt the authentication attempt
     */
    public void updateAuthAttemptHistoryDataByTrasactionId(Session session,AuthenticationAttempt authenticationAttempt) {
        logger.log(Level.DEBUG, AUTH_ATTEMPT_HISTORY_HANDLER + " updateAuthAttemptHistoryDataByTrasactionId : start");
        try {
            authenticationHistoryDao.authUpdateStatusByTransactionId(session, authenticationAttempt.getTransactionId(), authenticationAttempt.getAttemptStatus());
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, AUTH_ATTEMPT_HISTORY_HANDLER + " updateAuthAttemptHistoryDataByTrasactionId : end");
    }
    
    /**
     * Shutdown.
     *
     * @return true, if successful
     */
    public boolean shutdown() {
        logger.log(Level.DEBUG, AUTH_ATTEMPT_HISTORY_HANDLER + " shutdown : start");
        pool.shutdown();
        while (!pool.isTerminated()) {
            try {
                Thread.sleep(1000l);
            }
            catch (InterruptedException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
        logger.log(Level.DEBUG, AUTH_ATTEMPT_HISTORY_HANDLER + " shutdown : end");
        return true;
    }

}
