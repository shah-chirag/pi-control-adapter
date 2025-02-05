
package in.fortytwo42.adapter.util;

import java.sql.Timestamp;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.JobExecutionDetailsDAOIntf;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.JobExecutionDetails;
import in.fortytwo42.tos.enums.JobExecutionStatus;

public class ADSyncJobListener implements JobListener {

    private String ADSYNC_JOB_LISTENER_LOG = "<<<<< ADSyncJobListener";

    private static Logger logger= LogManager.getLogger(ADSyncJobListener.class);

    private static final String SYSTEM_SCHEDULAR = "SYSTEM-SCHEDULAR";

    private String createdBy;

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    public ADSyncJobListener(String createdBy) {
        this.createdBy = createdBy;
    }

    public ADSyncJobListener() {}

    @Override
    public String getName() {
        return ADSyncJobListener.class.getSimpleName();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        logger.log(Level.DEBUG, ADSYNC_JOB_LISTENER_LOG + " jobToBeExecuted : start");
        String className = (String) context.getJobDetail().getJobDataMap().get("jobClass");

        JobExecutionDetailsDAOIntf job = DaoFactory.getJobExecutionDetailsDao();
        JobExecutionDetails jobExecutionDetail = new JobExecutionDetails();
        jobExecutionDetail.setStartTime(new Timestamp(System.currentTimeMillis()));
        jobExecutionDetail.setEndTime(new Timestamp(System.currentTimeMillis()));
        jobExecutionDetail.setJobType(SYSTEM_SCHEDULAR);
        jobExecutionDetail.setJobName("AD sync");
        jobExecutionDetail.setCreatedBy(createdBy);
        jobExecutionDetail.setJobExecutionStatus(JobExecutionStatus.INPROGRESS);
        Session session = sessionFactoryUtil.getSession();
        JobExecutionDetails jobExecutionDetails = null;
        try {
            jobExecutionDetails = job.create(session, jobExecutionDetail);
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
        context.put("insertObject", jobExecutionDetails);
        logger.log(Level.DEBUG, ADSYNC_JOB_LISTENER_LOG + " jobToBeExecuted : end");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {}

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        logger.log(Level.DEBUG, ADSYNC_JOB_LISTENER_LOG + " jobWasExecuted : start");
        JobExecutionDetailsDAOIntf job = DaoFactory.getJobExecutionDetailsDao();
        JobExecutionDetails jobexecutionDetails = (JobExecutionDetails) context.get("insertObject");
        jobexecutionDetails.setEndTime(new Timestamp(System.currentTimeMillis()));
        if (jobException == null) {
            jobexecutionDetails.setJobExecutionStatus(JobExecutionStatus.SUCCESSFUL);
        }
        else {
           logger.log(Level.ERROR, jobException.getMessage(), jobException);
            jobexecutionDetails.setJobExecutionStatus(JobExecutionStatus.FAILURE);
            jobexecutionDetails.setErrorMessage(jobException.getMessage());
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            job.update(session, jobexecutionDetails);
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
        logger.log(Level.DEBUG, ADSYNC_JOB_LISTENER_LOG + " jobWasExecuted : end");
    }

}
