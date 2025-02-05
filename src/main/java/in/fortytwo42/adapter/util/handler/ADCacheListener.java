package  in.fortytwo42.adapter.util.handler;

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
 * The listener interface for receiving ADCache events.
 * The class that is interested in processing a ADCache
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addADCacheListener<code> method. When
 * the ADCache event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ADCacheEvent
 */
public class ADCacheListener implements CacheEventListener<String, AsyncResponse> {

    /** The ad caache listener. */
    private final String AD_CACHE_LISTENER = "<<<<< ADCacheListener";

	private static Logger logger= LogManager.getLogger(ADCacheListener.class);
    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
	/**
	 * On event.
	 *
	 * @param event the event
	 */
	@Override
	public void onEvent(CacheEvent<? extends String, ? extends AsyncResponse> event) {
        logger.log(Level.DEBUG, AD_CACHE_LISTENER + " onEvent : start");
		String transactionId = event.getKey();
		logger.log(Level.DEBUG,System.currentTimeMillis() + "Expired transaction with transaction Id " + transactionId);
		Session session = sessionFactoryUtil.getSession();
		try {
			AuthenticationAttempt authenticationAttempt = DaoFactory.getAuthenticationAttemptDao().getAuthAttemptByApplicationIdAndTransactionId("MyMoneyBk0001", transactionId);
			if(ApprovalStatus.PENDING.name().equals(authenticationAttempt.getAttemptStatus())) {
				authenticationAttempt.setAttemptStatus(ApprovalStatus.TIMEOUT.name());
		        DaoFactory.getAuthenticationAttemptDao().update(session, authenticationAttempt);
			}
            sessionFactoryUtil.closeSession(session);
		} catch (TransactionNotFoundException e) {
            session.getTransaction().rollback();
			logger.log(Level.DEBUG, e.getMessage());
		}finally {
		    if (session.isOpen()) {
                session.close();
            }
	        logger.log(Level.DEBUG, AD_CACHE_LISTENER + " onEvent : end");
		}
	}
}
