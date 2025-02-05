package in.fortytwo42.adapter.util.handler;

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

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.QuartzSchedularUtil;
import in.fortytwo42.daos.util.SessionFactoryUtil;

public class QRtransactionScheduler {

    private String QR_TRANSACTION_SCHEDULAR_LOG = "<<<<< QRtransactionScheduler";

    private static Logger logger= LogManager.getLogger(QRtransactionScheduler.class);

    private Scheduler scheduler;

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private QRtransactionScheduler(){
        super();
    }

    private static final class InstanceHolder{

        private static final QRtransactionScheduler INSTANCE = new QRtransactionScheduler();

        private InstanceHolder() {
            super();
        }
    }

    public static QRtransactionScheduler getInstance() {
        return QRtransactionScheduler.InstanceHolder.INSTANCE;
    }

    public void startSchedular() {
        logger.log(Level.DEBUG, QR_TRANSACTION_SCHEDULAR_LOG + " startSchedular : start");
        try{
            String group = "qr_group";
            String jobKey = "qr_key";
            String triggerKey = "qr_trigger";

            Properties properties = QuartzSchedularUtil.getInstance().getProperties();
            scheduler = new StdSchedulerFactory(properties).getScheduler();
            scheduler.start();

            JobKey job = JobKey.jobKey(jobKey, group);
            JobDetail jobDetail;
            if (!scheduler.checkExists(job)) {
                jobDetail = JobBuilder.newJob(QRtransactionJob.class).withIdentity(job).storeDurably().build();
                scheduler.addJob(jobDetail, true);
            }
            else {
                jobDetail = scheduler.getJobDetail(job);
            }

            TriggerKey trigger = TriggerKey.triggerKey(triggerKey, group);

            Trigger trigger1 = TriggerBuilder.newTrigger().withIdentity(trigger).withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getProperty(Constant.QR_CRON_EXPRESSION)))
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
        logger.log(Level.DEBUG, QR_TRANSACTION_SCHEDULAR_LOG + " startSchedular : end");
    }

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
