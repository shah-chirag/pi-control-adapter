package in.fortytwo42.adapter.util.handler;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.AuthAttemptFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.daos.dao.AuthenticationAttemptHistoryDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.AuthenticationAttemptHistory;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class QRtransactionJob implements Job {

    private String QR_TRANSACTION_JOB_LOG = "<<<<< QRTransactionJob";

    private String QR_LOGIN = "QR_LOGIN";

    private String TIMEOUT = "TIMEOUT";
    private static Logger logger= LogManager.getLogger(QRtransactionJob.class);

    private static final String COUNT = "count";

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    AuthenticationAttemptHistoryDaoIntf authAttemptHistoryDao = DaoFactory.getAuthenticationHistoryDao();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.log(Level.DEBUG, QR_TRANSACTION_JOB_LOG + " execute : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            String attemptType = QR_LOGIN;
            String attemptStatus = TIMEOUT;
            AuthAttemptFacadeIntf authAttemptFacadeIntf = FacadeFactory.getAuthAttemptFacade();
            authAttemptFacadeIntf.deleteQRTrxFromAuthAttempt();
            List<AuthenticationAttemptHistory> authAttemptHistory = authAttemptHistoryDao.getByAttemptTypeAndStatus(attemptStatus, attemptType, session);
            logger.log(Level.DEBUG, QR_TRANSACTION_JOB_LOG + " execute : authAttemptHistory" + new Gson().toJson(authAttemptHistory));
            if (authAttemptHistory != null && !authAttemptHistory.isEmpty()) {
                authAttemptHistoryDao.bulkDelete(session, authAttemptHistory);
            }
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, QR_TRANSACTION_JOB_LOG + " execute : end");
    }
}
