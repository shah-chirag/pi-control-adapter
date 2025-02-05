
package in.fortytwo42.adapter.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import in.fortytwo42.adapter.transferobj.APILogTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.entities.bean.ResourceLog;
import in.fortytwo42.integration.exception.ActiveMQConectionException;
import in.fortytwo42.integration.producer.JMSProducerExtension;
import in.fortytwo42.tos.enums.Source;

public class ResourceLogServiceImpl implements ResourceLogServiceIntf {

    private static final String RESOURCE_LOG_SERVICE_LOG = "<<<<< ResourceLogServiceImpl";

    private static final String SEND_MESSAGE_TO_QUEUE_FAILED = ">>ResourceLog SendMsgToQueue Failed::";

    private static final String SEPARATOR = "::";

    private static Logger logger= LogManager.getLogger(ResourceLogServiceImpl.class);

    private Config config = Config.getInstance();

    private Map<String, APILogTO> resourceLogData;

    private final ExecutorService pool;

    private ResourceLogServiceImpl() {
        super();
        resourceLogData = new ConcurrentHashMap<>();
        int poolSize = 10;
        try {
            poolSize = Integer.parseInt(Config.getInstance().getProperty(Constant.RESOURCE_LOG_THREAD_POOL_SIZE));
        }
        catch (NumberFormatException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);
    }

    private static final class InstanceHolder {

        private static final ResourceLogServiceImpl INSTANCE = new ResourceLogServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static ResourceLogServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void sendResourceLogToQueue(String resourceLogJson) throws ActiveMQConectionException {
        logger.log(Level.INFO, RESOURCE_LOG_SERVICE_LOG + " sendResourceLogToQueue : start");
        try {
            JMSProducerExtension jmsProducerExtension;
            String brokerUrl = config.getProperty(Constant.BROKER_URL);
            String username = config.getProperty(Constant.QUEUE_USERNAME);
            String password = config.getProperty(Constant.QUEUE_PASSWORD);
            jmsProducerExtension = JMSProducerExtension.getInstance(brokerUrl, username, password);
            String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
            String queueName = config.getProperty(Constant.RESOURCE_LOG_QUEUE_NAME);
            pool.submit(() -> {
                ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
                boolean isSent = jmsProducerExtension.sendResourceLogToQueue(queueName, resourceLogJson);
                if (!isSent) {
                    logger.log(Level.DEBUG, Source.ADP.name() + SEPARATOR + SEND_MESSAGE_TO_QUEUE_FAILED + RESOURCE_LOG_SERVICE_LOG + SEPARATOR + resourceLogJson);
                }
            });
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            logger.log(Level.INFO, RESOURCE_LOG_SERVICE_LOG + " sendResourceLogToQueue : end");
        }

    }

    public APILogTO convertResourceLogToAPILog(ResourceLog resourceLog) {
        APILogTO apiLog = new APILogTO();
        apiLog.setRequestReferenceNumber(resourceLog.getRequestReferenceNumber());
        apiLog.setRemoteAddress(resourceLog.getRemoteAddress());
        apiLog.setRequestMethod(resourceLog.getRequestMethod());
        apiLog.setRequestUrl(resourceLog.getRequestUrl());
        apiLog.setxQuery(resourceLog.getxQuery());
        apiLog.setRequestData(resourceLog.getRequestData());
        apiLog.setResponseData(resourceLog.getResponseData());
        apiLog.setRequestHeaderData(resourceLog.getRequestHeaderData());
        apiLog.setResponseHeaderData(resourceLog.getResponseHeaderData());
        apiLog.setSource(Source.ADP.name());
        return apiLog;
    }

    public boolean addResourceLog(String internalRequestReferenceNumber, APILogTO resourceLog){
        this.resourceLogData.put(internalRequestReferenceNumber, resourceLog);
        return true;
    }

    public APILogTO getResourceLog(String internalRequestReferenceNumber) {
        return this.resourceLogData.get(internalRequestReferenceNumber);
    }

    public APILogTO removeResourceLog(String internalRequestReferenceNumber) {
        return this.resourceLogData.remove(internalRequestReferenceNumber);
    }

}
