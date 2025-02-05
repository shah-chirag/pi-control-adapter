
package in.fortytwo42.adapter.util;

import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class UserAuthPrincipalSchedular.
 */
public class FalloutProcessScheduler {

    /** The user auth principal job. */
    private static final String FALLOUT_PROCESS_SCHEDULER = "<<<<< FalloutProcessScheduler";

    private static Logger logger= LogManager.getLogger(FalloutProcessScheduler.class);

    /** The scheduler. */
    private Scheduler scheduler;

    /**
     * Instantiates a new user auth principal schedular.
     */
    private FalloutProcessScheduler() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final FalloutProcessScheduler INSTANCE = new FalloutProcessScheduler();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of UserAuthPrincipalSchedular.
     *
     * @return single instance of UserAuthPrincipalSchedular
     */
    public static FalloutProcessScheduler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Start schedular.
     */
    public void startSchedular() {
        //TODO: Configure Each Text and Cron Exp in Next Release
        try {
            String requestReferenceNumber = UUID.randomUUID().toString();
            System.out.println("requestReferenceNumber : "+requestReferenceNumber);
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            logger.log(Level.DEBUG, FALLOUT_PROCESS_SCHEDULER + " startSchedular : start");
            String group = "fallout_group";
            String jobKey = "fallout_key";
            String triggerKey = "fallout_trigger";

            Properties properties = QuartzSchedularUtil.getInstance().getProperties();
            scheduler = new StdSchedulerFactory(properties).getScheduler();
            scheduler.start();

            JobKey job = JobKey.jobKey(jobKey, group);
            JobDetail jobDetail;
            if (!scheduler.checkExists(job)) {
                jobDetail = JobBuilder.newJob(FalloutProcessJob.class)
                        .withIdentity(job)
                        .storeDurably()
                        .build();
                scheduler.addJob(jobDetail, true);
            }
            else {
                jobDetail = scheduler.getJobDetail(job);
            }
            TriggerKey trigger = TriggerKey.triggerKey(triggerKey, group);
            Trigger trigger1 = TriggerBuilder.newTrigger()
                    .withIdentity(trigger)
                    .withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getProperty(Constant.FALLOUT_PROCESS_SCHEDULAR_TIME)))
                    .forJob(jobDetail)
                    .build();
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
        logger.log(Level.DEBUG, FALLOUT_PROCESS_SCHEDULER + " startSchedular : end");
    }

    /**
     * Shutdown schedular.
     */
    public void shutdownSchedular() {
        if (scheduler != null) {
            try {
                scheduler.shutdown(true);
            }
            catch (SchedulerException e) {
                logger.log(Level.ERROR, e.getMessage());
            }
        }
    }
}
