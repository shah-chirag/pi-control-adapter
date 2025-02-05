
package in.fortytwo42.adapter.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.entities.enums.AdminSessionState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.dao.AdminLoginLogDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserAuthPrincipalDaoIntf;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.AdminAuditLog;
import in.fortytwo42.entities.enums.LoginStatus;

public class AdminLoginLogServiceImpl implements AdminLoginLogServiceIntf {

    /** The permission processor log. */
    private static final String ADMIN_LOGIN_SERVICE_IMPL_LOG = "<<<<< AdminLoginLogServiceImpl";

    private AdminLoginLogDaoIntf adminLoginLogDao = DaoFactory.getAdminLoginLogDao();
    private UserAuthPrincipalDaoIntf userAuthPrincipalDao = DaoFactory.getUserAuthPrincipalDao();
    private Logger logger = LogManager.getLogger(AdminLoginLogServiceImpl.class);
    private final ExecutorService pool;

    private AdminLoginLogServiceImpl(){
        super();


        int poolSize = 10;
        try {
            poolSize = Integer.parseInt(Config.getInstance().getProperty(Constant.ADMIN_TOTP_LOG_THREAD_POOL_SIZE));
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);
    }

    /** The session factory util. */
    private SessionFactoryUtil sessionFactoryUtil=SessionFactoryUtil.getInstance();

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final AdminLoginLogServiceImpl INSTANCE = new AdminLoginLogServiceImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of AdminLoginLogServiceImpl.
     *
     * @return single instance of AdminLoginLogServiceImpl
     */
    public static AdminLoginLogServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Admin Login log.
     *
     * @param username the username
     * @param loginStatus the login status
     */
    @Override
    public void adminLoginLog(String username, LoginStatus loginStatus, Timestamp loginTime, AdminSessionState sessionStatus, String role) {
        logger.log(Level.DEBUG, ADMIN_LOGIN_SERVICE_IMPL_LOG + " adminLoginLog : start");
        pool.submit(()->{
            Session session = sessionFactoryUtil.getSession();
            try {
                AdminAuditLog adminAuditLog = new AdminAuditLog();
                adminAuditLog.setUsername(username.toUpperCase());
                adminAuditLog.setLoginTime(loginTime);
                adminAuditLog.setLogoutTime(null);
                adminAuditLog.setStatus(loginStatus);
                adminAuditLog.setSessionStatus(sessionStatus);
                if(role!=null){
                    adminAuditLog.setRole(role);
                }
                adminLoginLogDao.create(session, adminAuditLog);
            }
            catch (Exception e) {
                session.getTransaction().rollback();
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            finally {
                if(session.isOpen() ) {
                    sessionFactoryUtil.closeSession(session);
                }

            }
        });
        logger.log(Level.DEBUG, ADMIN_LOGIN_SERVICE_IMPL_LOG + " adminLoginLog : end");
    }

    /**
     * Update admin login log.
     *
     * @param username the username
     * @param status the status
     */
    @Override
    public int updateAdminLoginLog(Session session, String username, LoginStatus status, AdminSessionState sessionStatus, Timestamp logoutTime, String role,Timestamp initialLoginTime) {
        logger.log(Level.DEBUG, ADMIN_LOGIN_SERVICE_IMPL_LOG + " updateAdminLoginLog : start");
        try {
           return adminLoginLogDao.updateAdminLoginLog(session, username, status, sessionStatus, logoutTime, role,initialLoginTime);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            logger.log(Level.DEBUG, ADMIN_LOGIN_SERVICE_IMPL_LOG + " updateAdminLoginLog : end");
        }
        return 1;
    }
    
    @Override
    public void updateAdminLogoutTime( String username, AdminSessionState sessionStatus, Timestamp logoutTime) {
        logger.log(Level.DEBUG, ADMIN_LOGIN_SERVICE_IMPL_LOG + " updateAdminLogoutTime : start");
        pool.submit(()->{
            Session session=sessionFactoryUtil.getSession();
            try {
                adminLoginLogDao.updateAdminLogoutTime(session, username, sessionStatus, logoutTime);
                session.getTransaction().commit();
            }
            catch (Exception e) {
                session.getTransaction().rollback();
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        });
        logger.log(Level.DEBUG, ADMIN_LOGIN_SERVICE_IMPL_LOG + " updateAdminLogoutTime : end");
    }

    @Override
    public List<AdminAuditLog> getPaginatedList(int pageNo, int limit, String searchText, String userRole, String status) {
        return adminLoginLogDao.getPaginatedList(pageNo, limit, searchText, userRole, status);

    }

    @Override
    public Long getTotalCountOfAdminLoginLog(String searchText, String userRole, String status) {
        return adminLoginLogDao.getTotalCountOfAdminLoginLog(searchText, userRole, status);
    }

}
