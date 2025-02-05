package  in.fortytwo42.adapter.util.handler;

import java.sql.Timestamp;

import javax.ws.rs.container.AsyncResponse;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.hibernate.Session;

import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.exception.TransactionNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.enums.ApprovalStatus;
import in.fortytwo42.entities.bean.AuthenticationAttempt;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving nonADCache events.
 * The class that is interested in processing a nonADCache
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addNonADCacheListener<code> method. When
 * the nonADCache event occurs, that object's appropriate
 * method is invoked.
 *
 * @see NonADCacheEvent
 */
public class NonADCacheListener implements CacheEventListener<String, AsyncResponse> {

    /** The nonad cache listener. */
    private static String NONAD_CACHE_LISTENER = "<<<<< NonADCacheListener";

	private static Logger logger= LogManager.getLogger(NonADCacheListener.class);


    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
	/**
	 * On event.
	 *
	 * @param event the event
	 */
	@Override
	public void onEvent(CacheEvent<? extends String, ? extends AsyncResponse> event) {
	    Session session = sessionFactoryUtil.getSession();
        logger.log(Level.DEBUG, NONAD_CACHE_LISTENER + " onEvent : start");
	    String transactionId = event.getKey();
	    logger.log(Level.INFO, System.currentTimeMillis() + "Expired transaction with transaction Id " + transactionId);
	    logger.log(Level.DEBUG, System.currentTimeMillis() + "Expired transaction with transaction Id " + transactionId);
		try {
			AuthenticationAttempt authenticationAttempt = DaoFactory.getAuthenticationAttemptDao().getAuthAttemptByApplicationIdAndTransactionId("MyMoneyBk0002", transactionId);
			if(ApprovalStatus.PENDING.name().equals(authenticationAttempt.getAttemptStatus())) {
				authenticationAttempt.setAttemptStatus(ApprovalStatus.TIMEOUT.name());
		        authenticationAttempt.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
		        DaoFactory.getAuthenticationAttemptDao().update(session, authenticationAttempt);
			}
		} catch (TransactionNotFoundException e) {
            session.getTransaction().rollback();
			logger.log(Level.DEBUG, e.getMessage());
		}finally {
		    if (session.isOpen()) {
                session.close();
            }
	        logger.log(Level.DEBUG, NONAD_CACHE_LISTENER + " onEvent : end");
		}
	}
}
