
package in.fortytwo42.adapter.service;

import java.sql.Timestamp;
import java.util.Map;

import in.fortytwo42.entities.enums.AdminSessionState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserAuthPrincipalDaoIntf;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.UserAuthPrincipal;
import in.fortytwo42.entities.enums.AuthenticationStatus;
import in.fortytwo42.entities.enums.SessionState;

// TODO: Auto-generated Javadoc
/**
 * The Class UserAuthPrincipalProcessorImpl.
 */
public class UserAuthPrincipalServiceImpl implements UserAuthPrincipalServiceIntf {

    /** The permission processor log. */
    private String PERMISSION_PROCESSOR_LOG = "<<<<< PermissionProcessorImpl";

    
    private UserAuthPrincipalDaoIntf userAuthPrincipalDao = DaoFactory.getUserAuthPrincipalDao();
    private AdminLoginLogServiceIntf adminLoginLogService = ServiceFactory.getAdminLoginLogService();
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private Logger logger=LogManager.getLogger(UserAuthPrincipalServiceImpl.class);
    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final UserAuthPrincipalServiceImpl INSTANCE = new UserAuthPrincipalServiceImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of UserAuthPrincipalProcessorImpl.
     *
     * @return single instance of UserAuthPrincipalProcessorImpl
     */
    public static UserAuthPrincipalServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * User audit log.
     *
     * @param username the username
     * @param authenticationStatus the authentication status
     * @param sessionState the session state
     * @param token the token
     */
    @Override
    public void userAuditLog(Session session,String username, AuthenticationStatus authenticationStatus, SessionState sessionState, String token) {
        logger.log(Level.DEBUG, PERMISSION_PROCESSOR_LOG + " userAuditLog : start");
        try {
            Map<String, String> tokenData = JWTokenImpl.getAllClaimsWithoutValidation(token);
            UserAuthPrincipal userAuthPrincipal = new UserAuthPrincipal();
            userAuthPrincipal.setUsername(username.toLowerCase());
            userAuthPrincipal.setAuthenticationStartTime(new Timestamp((Long.parseLong(tokenData.get("iat")) * 1000l)));
            userAuthPrincipal.setAuthenticationEndTime(new Timestamp((Long.parseLong(tokenData.get("exp")) * 1000l)));
            userAuthPrincipal.setAuthenticationStatus(authenticationStatus);
            userAuthPrincipal.setSessionStatus(sessionState);
            userAuthPrincipal.setOrigin(Constant.ORIGIN);
            userAuthPrincipalDao.create(session,userAuthPrincipal);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, PERMISSION_PROCESSOR_LOG + " userAuditLog : end");
    }

    /**
     * Update audit log.
     *
     * @param username the username
     * @param token the token
     */
    @Override
    public void updateAuditLog(Session session,String username, String token) {
        logger.log(Level.DEBUG, PERMISSION_PROCESSOR_LOG + " updateAuditLog : start");
        try {
            Map<String, String> tokenData = JWTokenImpl.getAllClaimsWithoutValidation(token);
            Timestamp tokenEndTime = new Timestamp((Long.parseLong(tokenData.get("exp")) * 1000l));
            userAuthPrincipalDao.updateUserSessions(session, username,Constant.ORIGIN, tokenEndTime);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, PERMISSION_PROCESSOR_LOG + " updateAuditLog : end");
    }

    @Override
    public void updateAuditLog(String username, String token) {
        Session session = sessionFactoryUtil.getSession();
        logger.log(Level.DEBUG, PERMISSION_PROCESSOR_LOG + " updateAuditLog : start");
        try {
            Map<String, String> tokenData = JWTokenImpl.getAllClaimsWithoutValidation(token);
            Timestamp tokenEndTime = new Timestamp((Long.parseLong(tokenData.get("exp")) * 1000l));
            adminLoginLogService.updateAdminLogoutTime( username, AdminSessionState.C, tokenEndTime);
          //  userAuthPrincipalDao.updateUserSessions(session, username, Constant.ORIGIN, tokenEndTime);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, PERMISSION_PROCESSOR_LOG + " updateAuditLog : end");
        }
    }

}
