package in.fortytwo42.adapter.util;

import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class TokenDeletionSchedular {

    private static Logger logger= LogManager.getLogger(TokenDeletionSchedular.class);

    private Scheduler scheduler;

    private TokenDeletionSchedular() {
        super();
    }

    private static final class InstanceHolder {
        private static final TokenDeletionSchedular INSTANCE = new TokenDeletionSchedular();

        private InstanceHolder() {
            super();
        }
    }
    
    public static TokenDeletionSchedular getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void startSchedular() {
        try {
            String group = Config.getInstance().getProperty(Constant.TOKEN_DELETION_GROUP_KEY);
            String jobKey = Config.getInstance().getProperty(Constant.TOKEN_DELETION_JOB_KEY);
            String triggerKey = Config.getInstance().getProperty(Constant.TOKEN_DELETION_TRIGGER_KEY);
            
            Properties properties = QuartzSchedularUtil.getInstance().getProperties();
            scheduler = new StdSchedulerFactory(properties).getScheduler();
            scheduler.start();
            
            JobKey job = JobKey.jobKey(jobKey,group);
            JobDetail jobDetail;
            if(!scheduler.checkExists(job)) {
                jobDetail = JobBuilder.newJob(TokenDeletionJob.class)
                        .withIdentity(job)
                        .storeDurably()
                        .build();
                scheduler.addJob(jobDetail, true);
            } else {
                jobDetail = scheduler.getJobDetail(job);
            }
            TriggerKey trigger = TriggerKey.triggerKey(triggerKey,group);
            Trigger trigger1 = TriggerBuilder.newTrigger()
                    .withIdentity(trigger)
                    .withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getProperty(Constant.TOKEN_DELETION_CRON_EXPRESSION)))
                    .forJob(jobDetail)
                    .build();
            if(scheduler.checkExists(trigger)) {
                scheduler.rescheduleJob(trigger, trigger1);
            } else {
                scheduler.scheduleJob(trigger1);
            }
        }
        catch (SchedulerException e) {
            logger.log(Level.ERROR,e.getMessage());
        }
    }
    
    public void shutdownSchedular() {
        if(scheduler != null) {
            try {
                scheduler.shutdown(true);
            }
            catch (SchedulerException e) {
                logger.log(Level.ERROR,e.getMessage());
            }
        }
    }
}
