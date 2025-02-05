
package in.fortytwo42.adapter.util;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import in.fortytwo42.entities.enums.AdminSessionState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserAuthPrincipalDaoIntf;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.UserAuthPrincipal;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;

// TODO: Auto-generated Javadoc
/**
 * The Class UserAuthPrincipalJob.
 */
public class UserAuthPrincipalJob implements Job {

    /** The user auth principal job. */
    private static String USER_AUTH_PRINCIPAL_JOB = "<<<<< UserAuthPrincipalJob";

    private static Logger logger= LogManager.getLogger(UserAuthPrincipalJob.class);

    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    UserAuthPrincipalDaoIntf userAuthPrincipalDao = DaoFactory.getUserAuthPrincipalDao();

    /**
     * Execute.
     *
     * @param context the context
     * @throws JobExecutionException the job execution exception
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        requestReferenceNumber = requestReferenceNumber != null ? requestReferenceNumber : UUID.randomUUID().toString();
        ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
        logger.log(Level.DEBUG, USER_AUTH_PRINCIPAL_JOB + " execute : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            List<UserAuthPrincipal> userAuthPrincipals = userAuthPrincipalDao.getUserAuthPrincipalslessThanOrEqualToCurrentTime();
            if (userAuthPrincipals != null && !userAuthPrincipals.isEmpty()) {
                userAuthPrincipalDao.bulkDelete(session, userAuthPrincipals);
                for (UserAuthPrincipal userAuthPrincipal : userAuthPrincipals) {
                     ServiceFactory.getAdminLoginLogService().updateAdminLogoutTime( userAuthPrincipal.getUsername(), AdminSessionState.C, new Timestamp(System.currentTimeMillis()));
                    AuditLogUtil.sendAuditLog(AuditLogConstant.USER_SESSION_EXPIRED + AuditLogConstant.FOR_USER + userAuthPrincipal.getUsername(), "ENTERPRISE", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, requestReferenceNumber, "", "", null);
                }
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (UserNotFoundException e) {
            session.getTransaction().rollback();
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, "AD Authenticate~", System.currentTimeMillis() + Constant.TILT, "AD user not found"));
            logger.log(Level.DEBUG, e.getMessage(), e);
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, USER_AUTH_PRINCIPAL_JOB + " execute : end");
    }
}
