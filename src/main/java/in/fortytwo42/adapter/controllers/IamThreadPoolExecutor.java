/**
 * 
 */

package in.fortytwo42.adapter.controllers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hibernate.Session;

import in.fortytwo42.daos.util.SessionFactoryUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class IamThreadPoolExecutor.
 *
 * @author ChiragShah
 */
public class IamThreadPoolExecutor extends ThreadPoolExecutor {
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    /**
     * Instantiates a new iam thread pool executor.
     *
     * @param corePoolSize the core pool size
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime the keep alive time
     * @param unit the unit
     * @param workQueue the work queue
     */
    public IamThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }
 
    /**
     * Before execute.
     *
     * @param t the Thread
     * @param r the Runnable
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (t == null || r == null) {
            throw new NullPointerException();
        }
        IamThreadContext.setCorelationId(null);
        IamThreadContext.setSessionWithoutTransaction(null);
        IamThreadContext.setActor(null);
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Session session = IamThreadContext.getSessionWithoutTransaction();
        if(session != null && session.isOpen()){
            sessionFactoryUtil.closeSessionWithoutCommit(session);
        }
        super.afterExecute(r, t);
    }

    /*@Override
    protected void afterExecute(Runnable r, Throwable t) {
        AuditLoggingProcessorIntf auditLoggingProcessor = ProcessorFactory.getAuditLoggingProcessor();
        auditLoggingProcessor.logIntoDatabase();
        super.afterExecute(r, t);
    }*/
}
