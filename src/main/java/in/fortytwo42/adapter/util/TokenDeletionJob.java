package in.fortytwo42.adapter.util;

import java.util.UUID;

import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import in.fortytwo42.adapter.service.WebTokenServiceImpl;
import in.fortytwo42.daos.util.SessionFactoryUtil;

@DisallowConcurrentExecution
public class TokenDeletionJob implements Job {

    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Session session = sessionFactoryUtil.getSession();
        try {

            String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
            requestReferenceNumber = requestReferenceNumber != null ? requestReferenceNumber : UUID.randomUUID().toString();
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            WebTokenServiceImpl.getInstance().deleteExpiredTokens(session);
            sessionFactoryUtil.closeSession(session);
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
}

