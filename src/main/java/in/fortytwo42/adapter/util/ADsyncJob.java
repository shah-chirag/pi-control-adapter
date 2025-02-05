
package in.fortytwo42.adapter.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ADsyncJob implements Job {
    /** The ad sync job log. */
    private String AD_SYNC_JOB_LOG = "<<<<< ADSyncJob";

    private static Logger logger= LogManager.getLogger(ADsyncJob.class);

    /** The Constant COUNT. */
    private static final String COUNT = "count";

    /**
     * Execute.
     *
     * @param context the context
     * @throws JobExecutionException the job execution exception
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.log(Level.DEBUG, AD_SYNC_JOB_LOG + " execute : start");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        try {
            //ProcessorFactory.getADSyncProcessor().syncADUsers();
            dataMap.putAsString(COUNT, 0);
        }
        catch (Exception e) {
           logger.log(Level.ERROR, e.getMessage(), e);
            try {
                Thread.sleep(6000);
            }
            catch (InterruptedException e1) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            String countValue = dataMap.getString(COUNT);
            int count = 0;
            if (countValue != null) {
                count = Integer.parseInt(countValue);
            }
            count++;
            if (count < 3) {
                dataMap.putAsString(COUNT, count);
                JobExecutionException e2 = new JobExecutionException(e);
                throw e2;
            }
        }
        logger.log(Level.DEBUG, AD_SYNC_JOB_LOG + " execute : end");
    }

}
