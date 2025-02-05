
package in.fortytwo42.adapter.util;

import java.sql.Timestamp;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;

import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.JobExecutionDetailsDAOIntf;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.JobExecutionDetails;
import in.fortytwo42.tos.enums.JobExecutionStatus;

public class ADsyncSchedular {

    private String ADSYNC_SCHEDULAR_LOG = "<<<<< ADSyncSchedular";

    private static Logger logger= LogManager.getLogger(ADsyncSchedular.class);

    public static final String AD_SYNC = "AD-SYNC";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final String API_TRIGGER = "API-TRIGGER";

    private static final String MANUAL = "MANUAL";

    private Scheduler scheduler;

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private ADsyncSchedular() {
        super();
    }

    private static final class InstanceHolder {

        private static final ADsyncSchedular INSTANCE = new ADsyncSchedular();

        private InstanceHolder() {
            super();
        }
    }

    public static ADsyncSchedular getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void startSchedular() {
        logger.log(Level.DEBUG, ADSYNC_SCHEDULAR_LOG + " startSchedular : start");
        try {
            String group = Config.getInstance().getProperty(Constant.GROUP_KEY);
            String jobKey = Config.getInstance().getProperty(Constant.JOB_KEY);
            String triggerKey = Config.getInstance().getProperty(Constant.TRIGGER_KEY);

            Properties properties = QuartzSchedularUtil.getInstance().getProperties();
            scheduler = new StdSchedulerFactory(properties).getScheduler();
            scheduler.start();

            JobDataMap jData = new JobDataMap();
            jData.put("jobClass", "ADSYNC");
            JobKey job = JobKey.jobKey(jobKey, group);
            JobDetail jobDetail;
            if (!scheduler.checkExists(job)) {
                jobDetail = JobBuilder.newJob(ADsyncJob.class).usingJobData(jData).withIdentity(job).storeDurably().build();
                scheduler.addJob(jobDetail, true);
            }
            else {
                jobDetail = scheduler.getJobDetail(job);
            }
            scheduler.getListenerManager().addJobListener(new ADSyncJobListener("SCHEDULAR"),
                    KeyMatcher.keyEquals(job));
            // Trigger the job to run at second :00, at minute :00, every 4 hours starting
            // at 00am, of every day
            TriggerKey trigger = TriggerKey.triggerKey(triggerKey, group);

            Trigger trigger1 = TriggerBuilder.newTrigger().withIdentity(trigger).withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getProperty(Constant.CRON_EXPRESSION)))
                    .forJob(jobDetail).build();
            if (scheduler.checkExists(trigger)) {
                scheduler.rescheduleJob(trigger, trigger1);
            }
            else {
                scheduler.scheduleJob(trigger1);
            }
        }
        catch (SchedulerException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
        logger.log(Level.DEBUG, ADSYNC_SCHEDULAR_LOG + " startSchedular : end");
    }

    public void shutdownSchedular() {
        logger.log(Level.DEBUG, ADSYNC_SCHEDULAR_LOG + " shutdownSchedular : start");
        if (scheduler != null) {
            try {
                scheduler.getListenerManager().removeJobListener(new ADSyncJobListener().getName());
                scheduler.shutdown(true);

            }
            catch (SchedulerException e) {
                logger.log(Level.ERROR, e.getMessage());
            }
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        logger.log(Level.DEBUG, ADSYNC_SCHEDULAR_LOG + " shutdownSchedular : end");
    }

    public void triggerADSync(String createdBy) {
        logger.log(Level.DEBUG, ADSYNC_SCHEDULAR_LOG + " triggerADSync : start");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                JobExecutionDetails jobExecutionDetails = new JobExecutionDetails();
                JobExecutionDetailsDAOIntf job = DaoFactory.getJobExecutionDetailsDao();
                Session session = sessionFactoryUtil.getSession();
                try {
                    jobExecutionDetails.setStartTime(new Timestamp(System.currentTimeMillis()));
                    jobExecutionDetails.setEndTime(new Timestamp(System.currentTimeMillis()));
                    jobExecutionDetails.setJobType(API_TRIGGER);
                    jobExecutionDetails.setJobName("AD sync");
                    jobExecutionDetails.setCreatedBy(createdBy);
                    jobExecutionDetails.setJobExecutionStatus(JobExecutionStatus.INPROGRESS);
                    jobExecutionDetails = job.create(session, jobExecutionDetails);
                    //ADSyncProcessorImpl.getInstance().syncADUsers();
                    jobExecutionDetails.setEndTime(new Timestamp(System.currentTimeMillis()));
                    jobExecutionDetails.setJobExecutionStatus(JobExecutionStatus.SUCCESSFUL);
                    job.update(session, jobExecutionDetails);
                    sessionFactoryUtil.closeSession(session);
                }
                catch (Exception e) {
                    jobExecutionDetails.setJobExecutionStatus(JobExecutionStatus.FAILURE);
                    jobExecutionDetails.setErrorMessage(e.getMessage());
                    jobExecutionDetails.setEndTime(new Timestamp(System.currentTimeMillis()));
                    job.update(session, jobExecutionDetails);
                    sessionFactoryUtil.closeSession(session);
                    logger.log(Level.ERROR, e.getMessage());
                }
                finally {
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            }
        });
        logger.log(Level.DEBUG, ADSYNC_SCHEDULAR_LOG + " triggerADSync : end");
    }

    public void triggerJobSync(String createdBy, String jobName) {
        logger.log(Level.DEBUG, ADSYNC_SCHEDULAR_LOG + " triggerJobSync : start");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                JobExecutionDetails jobExecutionDetails = new JobExecutionDetails();
                JobExecutionDetailsDAOIntf job = DaoFactory.getJobExecutionDetailsDao();
                Session session = sessionFactoryUtil.getSession();
                try {
                    jobExecutionDetails.setStartTime(new Timestamp(System.currentTimeMillis()));
                    jobExecutionDetails.setEndTime(new Timestamp(System.currentTimeMillis()));
                    jobExecutionDetails.setJobType(MANUAL);
                    jobExecutionDetails.setJobName(jobName);
                    jobExecutionDetails.setCreatedBy(createdBy);
                    jobExecutionDetails.setJobExecutionStatus(JobExecutionStatus.INPROGRESS);
                    jobExecutionDetails = job.create(session, jobExecutionDetails);
                    switch (jobName) {
                        case AD_SYNC:
                            logger.log(Level.INFO, "AD Sync Started");
                            //ADSyncProcessorImpl.getInstance().syncADUsers();
                        default:
                    }
                    jobExecutionDetails.setEndTime(new Timestamp(System.currentTimeMillis()));
                    jobExecutionDetails.setJobExecutionStatus(JobExecutionStatus.SUCCESSFUL);
                    job.update(session, jobExecutionDetails);
                    sessionFactoryUtil.closeSession(session);
                }
                catch (Exception e) {
                    jobExecutionDetails.setJobExecutionStatus(JobExecutionStatus.FAILURE);
                    jobExecutionDetails.setErrorMessage(e.getMessage());
                    jobExecutionDetails.setEndTime(new Timestamp(System.currentTimeMillis()));
                    job.update(session, jobExecutionDetails);
                    sessionFactoryUtil.closeSession(session);
                    logger.log(Level.ERROR, e.getMessage());
                }
                finally {
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            }
        });
        logger.log(Level.DEBUG, ADSYNC_SCHEDULAR_LOG + " triggerJobSync : end");
    }

}
