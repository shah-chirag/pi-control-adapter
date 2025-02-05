package in.fortytwo42.adapter.util.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;


public class CAMUtil {
	private static Logger logger = LogManager.getLogger(CAMUtil.class);
	private ExecutorService executorService;
	private CAMUtil() {
		super();
		int nThreads = 50;
		Config config = Config.getInstance();
		try {
			nThreads = Integer.parseInt(config.getProperty(Constant.CAM_SYNC_THREAD_POOL_SIZE));
		}
		catch (NumberFormatException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
		}
		executorService= Executors.newFixedThreadPool(nThreads);
	}
	private static final class InstanceHolder {

		private static final CAMUtil INSTANCE = new CAMUtil();

		private InstanceHolder() {
			super();
		}
	}

	public static CAMUtil getInstance() {
		return CAMUtil.InstanceHolder.INSTANCE;
	}

	public boolean submitTask(Runnable runnableObj) {
		executorService.submit(runnableObj);
		return true;
	}
	public void shutDownExecutorService(){
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
	}
}
