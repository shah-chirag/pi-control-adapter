
package in.fortytwo42.adapter.processor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.OtpAuditSendLog;
import in.fortytwo42.entities.bean.OtpAuditValidateLog;
import in.fortytwo42.entities.enums.OtpAction;
import in.fortytwo42.entities.enums.OtpStatus;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.*;

import com.google.gson.Gson;

import in.fortytwo42.adapter.transferobj.AdapterAuditLogTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.integration.exception.ActiveMQConectionException;
import in.fortytwo42.integration.producer.JMSProducerExtension;
import org.hibernate.Session;

public class AuditLogProcessorImpl implements AuditLogProcessorIntf {

    private static Logger logger= LogManager.getLogger(AuditLogProcessorImpl.class);
    private static final String AUDIT_LOG_PROCESSOR_LOG = "AuditLogProcessorImpl";
    private final ExecutorService pool;
    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private AuditLogProcessorImpl() {
        super();


        int poolSize = 10;
        try {
            poolSize = Integer.parseInt(Config.getInstance().getProperty(Constant.AUDIT_LOG_THREAD_POOL_SIZE));
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);
    }

    private static final class InstanceHolder {

        private static final AuditLogProcessorImpl INSTANCE = new AuditLogProcessorImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static AuditLogProcessorImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public AdapterAuditLogTO addAuditLog(AdapterAuditLogTO auditLogTO) {
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addAuditLog : start");
            sendAuditLogToQueue(auditLogTO);
            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addAuditLog : end");
        });
        return auditLogTO;
    }
    @Override
    public void addAuditLogs(List<AdapterAuditLogTO> auditLogTO) {
        logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addAuditLogs : start");
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addAuditLog : start");
            sendAuditLogsToQueue(auditLogTO);
            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addAuditLog : end");
        });
    }

//    @Override
//    public void sendAuditLog(String log, String origin, ActionType actionType, String creatorId, IdType creatorIdType, String reqRefNumber, String enterpriseAccountId, String actedOnAccountId,
//            String attemptId) {
//        sendAuditLogToQueue(log, origin, actionType, creatorId, creatorIdType, reqRefNumber, enterpriseAccountId, actedOnAccountId, attemptId, null);
//    }
    
    
    private void sendAuditLogsToQueue(List<AdapterAuditLogTO> auditLogTO) {
        logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " sendAuditLog : start");
        String brokerUrl = Config.getInstance().getProperty(Constant.BROKER_URL);
        String auditLogQueueName = Config.getInstance().getProperty(Constant.AUDIT_LOG_QUEUE_NAME);
        String username = Config.getInstance().getProperty(Constant.QUEUE_USERNAME);
        String password = Config.getInstance().getProperty(Constant.QUEUE_PASSWORD);
        try {
            JMSProducerExtension jmsProducer = JMSProducerExtension.getInstance(brokerUrl, username, password);
            
            jmsProducer.sendAuditLogQueue(auditLogQueueName, new Gson().toJson(auditLogTO));
        }
        catch (ActiveMQConectionException e) {
            logger.log(Level.FATAL, e);
        }
        finally {
            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " sendAuditLog : end");
        }
    }
    private void sendAuditLogToQueue(AdapterAuditLogTO auditLogTO) {
        logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " sendAuditLog : start");
        String brokerUrl = Config.getInstance().getProperty(Constant.BROKER_URL);
        String auditLogQueueName = Config.getInstance().getProperty(Constant.AUDIT_LOG_QUEUE_NAME);
        System.out.println("service bus - auditLogQueueName : "+auditLogQueueName);
        String username = Config.getInstance().getProperty(Constant.QUEUE_USERNAME);
        String password = Config.getInstance().getProperty(Constant.QUEUE_PASSWORD);
        String origin = auditLogTO.getOrigin();
        String creatorId = auditLogTO.getCreatorId();
        try {
            JMSProducerExtension jmsProducer = JMSProducerExtension.getInstance(brokerUrl, username, password);
            if (auditLogTO.getEnterpriseAccountId() != null && "USER".equals(origin)) {
                origin = "ENTERPRISE";
                creatorId = auditLogTO.getEnterpriseAccountId();
            } 
            jmsProducer.sendAuditLogQueue(auditLogQueueName, auditLogTO.getLogData(), origin, auditLogTO.getActionType(), creatorId, auditLogTO.getCreatorIdType(), auditLogTO.getReqRefNumber(),auditLogTO.getActedOnAccountId(),auditLogTO.getAttemptId(),auditLogTO.getCreatedTime(),auditLogTO.getClientSignature(),auditLogTO.getIsRequiredVerification());
            System.out.println("service bus - auditLogQueueName : "+auditLogQueueName+" - success");
        }
        catch (ActiveMQConectionException e) {
            logger.log(Level.FATAL, e);
        }
        finally {
            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " sendAuditLog : end");
        }
    }
    
    public static void main(String[] args) {
        String brokerUrl = Config.getInstance().getProperty(Constant.BROKER_URL);
        brokerUrl = "Endpoint=sb://e2e-idc-mesagging.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=qrhVOGRk/NwM6Jzb0LFcGv+xmZsnQrq324aDrD1uvtM=";
        System.out.println("brokerUrl : "+brokerUrl);
        String auditLogQueueName = Config.getInstance().getProperty(Constant.AUDIT_LOG_QUEUE_NAME);
        auditLogQueueName = "iam.audit";
        System.out.println("service bus - auditLogQueueName : "+auditLogQueueName);
        String username = Config.getInstance().getProperty(Constant.QUEUE_USERNAME);
        String password = Config.getInstance().getProperty(Constant.QUEUE_PASSWORD);
        try {
            JMSProducerExtension jmsProducer = JMSProducerExtension.getInstance(brokerUrl, username, password);
            String content = "User updated successfully from Active To Partially Active";
            jmsProducer.sendAuditLogQueue("iam.audit", content, "ENTERPRISE", ActionType.ACCOUNT_STATE_CHANGED, "62e39024ac28823b1bfcd45a", IdType.ACCOUNT, "","62e93e16935fb834879a1bad","",null,"",true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    
//    private void sendAuditLogToQueue(String log, String origin, ActionType actionType, String creatorId, IdType creatorIdType, String reqRefNumber, String enterpriseAccountId, String actedOnAccountId,
//            String attemptId,Long timestamp) {
//        logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " sendAuditLog : start");
//        String brokerUrl = Config.getInstance().getProperty(Constants.BROKER_URL);
//        String auditLogQueueName = Config.getInstance().getProperty(Constants.AUDIT_LOG_QUEUE_NAME);
//        String username = Config.getInstance().getProperty(Constants.QUEUE_USERNAME);
//        String password = Config.getInstance().getProperty(Constants.QUEUE_PASSWORD);
//        try {
//            JMSProducerExtension jmsProducer = JMSProducerExtension.getInstance(brokerUrl, username, password);
//            if (enterpriseAccountId != null && "USER".equals(origin)) {
//                origin = "ENTERPRISE";
//                creatorId = enterpriseAccountId;
//            }
//            jmsProducer.sendAuditLogQueue(auditLogQueueName, log, origin, actionType, creatorId, creatorIdType, reqRefNumber, actedOnAccountId, attemptId,timestamp);
//        }
//        catch (ActiveMQConectionException e) {
//            logger.log(Level.FATAL, e);
//        }
//        finally {
//            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " sendAuditLog : end");
//        }
//    }

    public void addOtpAuditSendLogs(String applicationName, List<AttributeDataTO> attributeList, OtpAction action, OtpStatus status, String referenceNumber){
        logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addOtpAuditSendLogs : start");
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addOtpAuditSendLogs submit: start");
            Session session = sessionFactoryUtil.getSession();
            OtpAuditSendLog otpAuditLog = new OtpAuditSendLog();
            try{
                String userId = null;
                for (AttributeDataTO attributeData : attributeList) {
                    if (attributeData.getAttributeName().equalsIgnoreCase(Constant.USER_ID)) {
                        userId = attributeData.getAttributeValue();
                    }
                }
                if (!StringUtil.isNotNullOrEmpty(userId)) {
                    userId = attributeList.get(0).getAttributeValue();
                }
                otpAuditLog.setApplicationName(applicationName);
                otpAuditLog.setUserId(userId);
                otpAuditLog.setReferenceNumber(referenceNumber);
                otpAuditLog.setStatus(status);
                otpAuditLog.setAction(action);
                DaoFactory.getOtpAuditDao().addOtpSendLog(session,otpAuditLog);
            }
            catch (Exception e){
                logger.log(Level.ERROR, e);
                session.getTransaction().rollback();
            }
            finally {
                sessionFactoryUtil.closeSession(session);
                logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addOtpAuditSendLogs submit: end");
            }
        });
        logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addOtpAuditSendLogs : end");
    }

    public void addOtpAuditValidateLogs(String applicationName, List<AttributeDataTO> attributeList, OtpAction action, OtpStatus status, String seed){
        logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addOtpAuditValidateLogs : start");
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addOtpAuditValidateLogs submit: start");
            Session session = sessionFactoryUtil.getSession();
            OtpAuditValidateLog otpAuditLog = new OtpAuditValidateLog();
            OtpAuditSendLog otpAuditSendLog=null;
	        try {
                if (seed == null || seed.isEmpty()) {
                    String userId = null;
                    for (AttributeDataTO attributeData : attributeList) {
                        if (attributeData.getAttributeName().equalsIgnoreCase(Constant.USER_ID)) {
                            userId = attributeData.getAttributeValue();
                            break;
                        }
                    }
                    if (!StringUtil.isNotNullOrEmpty(userId)) {
                        userId = attributeList.get(0).getAttributeValue();
                    }
                    if (userId != null && applicationName != null) {
                        otpAuditSendLog = DaoFactory.getOtpAuditDao().getRecentOtpSendForUserAndApplication(session, userId.toUpperCase(), applicationName.toUpperCase());
                    }
                } else {
                    otpAuditSendLog = DaoFactory.getOtpAuditDao().getOtpSendLogFromSeed(session, seed);
                }
                otpAuditLog.setReferenceNumber(seed);
                otpAuditLog.setStatus(status);
                otpAuditLog.setSendId(otpAuditSendLog);
                DaoFactory.getOtpAuditDao().addOtpValidateLog(session,otpAuditLog);
	        } catch (AttributeNotFoundException e) {
                logger.log(Level.ERROR, e);
                session.getTransaction().rollback();
	        }
	        catch (Exception e){
                logger.log(Level.ERROR, e);
                session.getTransaction().rollback();
            }
            finally {
                sessionFactoryUtil.closeSession(session);
                logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addOtpAuditValidateLogs submit: end");
            }
        });
        logger.log(Level.DEBUG, AUDIT_LOG_PROCESSOR_LOG + " addOtpAuditValidateLogs : end");
    }
}
