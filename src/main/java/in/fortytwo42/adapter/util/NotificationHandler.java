
package  in.fortytwo42.adapter.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class NotificationHandler.
 */
public class NotificationHandler {

    /** The notification handler log. */
    private static String NOTIFICATION_HANDLER_LOG = "<<<<< NotificationHandler";

	private static Logger logger= LogManager.getLogger(NotificationHandler.class);
    
	/** The event listener. */
	ExecutorService eventListener = Executors.newFixedThreadPool(Integer.parseInt(Config.getInstance().getProperty(Constant.EVENT_POOL_SIZE)));

	/**
	 * Instantiates a new notification handler.
	 */
	private NotificationHandler() {
		super();
	}

	/**
	 * The Class InstanceHolder.
	 */
	private static final class InstanceHolder {
		
		/** The Constant INSTANCE. */
		private static final NotificationHandler INSTANCE = new NotificationHandler();

		/**
		 * Instantiates a new instance holder.
		 */
		private InstanceHolder() {
			super();
		}
	}

	/**
	 * Gets the single instance of NotificationHandler.
	 *
	 * @return single instance of NotificationHandler
	 */
	public static NotificationHandler getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Push data to queue.
	 *
	 * @param event the event
	 * @return true, if successful
	 */
//	public boolean pushDataToQueue(Event event) {
//        logger.log(Level.DEBUG, NOTIFICATION_HANDLER_LOG + " pushDataToQueue : start");
//		eventListener.submit(new NotificationEventProcessorImpl(event));
//        logger.log(Level.DEBUG, NOTIFICATION_HANDLER_LOG + " pushDataToQueue : end");
//		return true;
//	}

	/**
	 * Shut down stop queue processing.
	 */
	public void shutDownStopQueueProcessing() {
        logger.log(Level.DEBUG, NOTIFICATION_HANDLER_LOG + " shutDownStopQueueProcessing : start");
		eventListener.shutdown();
        logger.log(Level.DEBUG, NOTIFICATION_HANDLER_LOG + " shutDownStopQueueProcessing : end");
	}

}
