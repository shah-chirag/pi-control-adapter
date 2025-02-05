package in.fortytwo42.adapter.util.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.ResourceLogDaoIntf;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.ResourceLog;

/**
 * @author ChiragShah
 *
 */
public final class ResourceLogHandler {

    private String RESOURCE_LOG_HANDLER = "<<<<< ResourceLogHandler";

    private static Logger logger= LogManager.getLogger(ResourceLogHandler.class);
    
    private final ExecutorService pool;
    private ResourceLogDaoIntf resourceLogDao = DaoFactory.getResourceLogDao();

    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private Config config = Config.getInstance();

    
    private ResourceLogHandler() {
        super();
        int poolSize = 1;
        try {
        	poolSize = Integer.parseInt(Config.getInstance().getProperty(Constant.RESOURCE_LOG_THREAD_POOL_SIZE));
        } catch(Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);
    }

    private static final class InstanceHolder {
        private static final ResourceLogHandler INSTANCE = new ResourceLogHandler();

        private InstanceHolder() {
            super();
        }
    }

    public static ResourceLogHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void logResourceData(ResourceLog resourceLog) {
        boolean isSaveLocally = Constant.DISABLE.equalsIgnoreCase(config.getProperty(Constant.RESOURCE_LOG_LOCAL_DB)) ? false : true;
        if (isSaveLocally) {
            String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
            pool.submit(() -> {
                ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
                Session session = sessionFactoryUtil.getSession();
                try {
                    resourceLogDao.create(session,resourceLog);
                    sessionFactoryUtil.closeSession(session);
                }
                catch (Exception e) {
                    session.getTransaction().rollback();
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
                finally {
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            });
        }
    }

    public void updateResourceData(String requestReferenceNumber, String responseHeaderData, String responseData) {
        logger.log(Level.DEBUG, RESOURCE_LOG_HANDLER + " updateResourceData : start");
        String requestRefNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            try {
                ThreadContext.put(Constant.REQUEST_REFERENCE, requestRefNumber);
                resourceLogDao.updateByRequestReferenceNumber(requestReferenceNumber, responseHeaderData, responseData);
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        });
        logger.log(Level.DEBUG, RESOURCE_LOG_HANDLER + " updateResourceData : end");
    }

    public boolean shutdown() {
        pool.shutdown();
        while (!pool.isTerminated()) {
            try {
                Thread.sleep(1000l);
            }
            catch (InterruptedException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
        return true;
    }
}
