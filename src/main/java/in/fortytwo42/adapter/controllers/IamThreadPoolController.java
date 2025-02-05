
package in.fortytwo42.adapter.controllers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

/**
 * 
 * @author ChiragShah
 *
 */
public class IamThreadPoolController {

    private ExecutorService fixedPool;
    private static Logger logger= LogManager.getLogger(IamThreadPoolController.class);

    /**
     * Default Constructor.
     */
    private IamThreadPoolController() {
        super();
        int nThreads = 500;
        int maxThreads = 1000;
        Config config = Config.getInstance();
        try {
            nThreads = Integer.parseInt(config.getProperty(Constant.THREAD_POOL_SIZE));
            maxThreads = Integer.parseInt(config.getProperty(Constant.MAX_THREAD_POOL_SIZE));
        }
        catch (NumberFormatException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        fixedPool = new IamThreadPoolExecutor(nThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    private static final class InstanceHolder {
        private static final IamThreadPoolController INSTANCE = new IamThreadPoolController();

        private InstanceHolder() {
            super();
        }
    }

    public static IamThreadPoolController getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * This method submits task for running to the thread pool
     * @param runnableObj
     * @return
     */
    public boolean submitTask(Runnable runnableObj) {
        fixedPool.execute(runnableObj);
        return true;
    }

    /**
     * destroying all the threads before the web app terminates
     */
    public void destroyExecutorService() {
        fixedPool.shutdown();
        try {
            fixedPool.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        destroyExecutorService();
        super.finalize();
    }

}
