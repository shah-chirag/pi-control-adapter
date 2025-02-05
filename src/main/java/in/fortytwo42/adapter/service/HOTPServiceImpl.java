package in.fortytwo42.adapter.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.processor.AuditLogProcessorImpl;
import in.fortytwo42.adapter.processor.AuditLogProcessorIntf;
import in.fortytwo42.adapter.util.*;
import in.fortytwo42.daos.dao.OtpAuditLogDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.entities.bean.*;
import in.fortytwo42.entities.enums.OtpAction;
import in.fortytwo42.entities.enums.OtpStatus;
import in.fortytwo42.tos.transferobj.OtpAuditLogRespTO;
import in.fortytwo42.tos.transferobj.OtpAuditLogSendTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.AuthTokenExpiredException;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.LogData;
import in.fortytwo42.adapter.util.SmsTemplate;
import in.fortytwo42.adapter.util.handler.HotpUtil;
import in.fortytwo42.daos.dao.ApplicationDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserTokenDaoIntf;
import in.fortytwo42.daos.exception.UserTokenNotFoundException;
import in.fortytwo42.enterprise.extension.enums.ActionType;
import in.fortytwo42.enterprise.extension.enums.IdType;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserToken;
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.integration.exception.ActiveMQConectionException;
import in.fortytwo42.integration.producer.JMSProducerExtension;
import in.fortytwo42.tos.enums.NotificationStatus;
import in.fortytwo42.tos.transferobj.HotpTO;
import in.fortytwo42.tos.transferobj.NotificationTO;


public class HOTPServiceImpl implements HOTPServiceIntf {

    private String HOTP_SERVICE_IMPL = "<<<<< HOTPServiceImpl";

    /** The logger. */
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private UserTokenDaoIntf userTokenDao = DaoFactory.getUserTokenDao();

    private static IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private ApplicationDaoIntf applicationDao = DaoFactory.getApplicationDao();
    private final OtpAuditLogDaoIntf otpAuditDao = DaoFactory.getOtpAuditDao();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private final AuditLogProcessorIntf auditLogProcessor = AuditLogProcessorImpl.getInstance();

    private OtpAuditLogDaoIntf otpAuditLogDao = DaoFactory.getOtpAuditDao();


    private static final class InstanceHolder {
        private static final HOTPServiceImpl INSTANCE = new HOTPServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static HOTPServiceImpl getInstance() {
        return HOTPServiceImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public HotpTO generateOtp(HotpTO hotpTO, Session session, Application application, User user, String attributeValue) throws AuthException, ActiveMQConectionException {

        String randomSeed = HotpUtil.generateRandomSeed(Integer.parseInt(Config.getInstance().getProperty(Constant.SEED_LENGTH)));

        String otp = HotpUtil.generateOTP(randomSeed,0,
                Integer.parseInt(Config.getInstance().getProperty(Constant.HOTP_LENGTH)),
                Boolean.parseBoolean(Config.getInstance().getProperty(Constant.ADD_CHECKSUM)));
        boolean testMode = Boolean.parseBoolean(Config.getInstance().getProperty(Constant.TEST_MODE));
        String response = "";
        boolean notificationSent =  false;
        for(NotificationTO notificationTO : hotpTO.getNotification())
        {
            AdHotpServiceImpl.getInstance().validateNotification(notificationTO,session);
            switch (notificationTO.getNotificationType()){
                case Constant.NOTIFICATION_TYPE_EMAIL:
                    String email = "";
                    try {
                        email = user.getAttributeStores().stream().filter(p -> p.getAttributeName().equals("EMAIL_ID") && p.getIsDefault()&&p.getAttributeState().equals(AttributeState.ACTIVE)).findAny().orElse(null).getAttributeValue();
                    }catch (NullPointerException e){
                        email = null;
                    }
                    if(email != null) {
                        String message = notificationTO.getMessageBody() != null ? notificationTO.getMessageBody().replace("<OTP>", otp) : String.format(SmsTemplate.SMS_TEMPLATE, otp);

                        JMSProducerExtension jmsProducerExtension;
                        String brokerUrl = Config.getInstance().getProperty(Constant.BROKER_URL);
                        String username = Config.getInstance().getProperty(Constant.QUEUE_USERNAME);
                        String password = Config.getInstance().getProperty(Constant.QUEUE_PASSWORD);
                        jmsProducerExtension = JMSProducerExtension.getInstance(brokerUrl,username,password);

                        boolean isNotificationPushed;
                        String subject = notificationTO.getSubject() == null || notificationTO.getSubject().isEmpty() ? Config.getInstance().getProperty(Constant.EMAIL_SUBJECT) : notificationTO.getSubject();
                        isNotificationPushed = jmsProducerExtension.sendEmail(Config.getInstance().getProperty(Constant.EMAIL_QUEUE_NAME),
                                Constant.APPLICATION_IDENTITY_STORE, email, message, subject,
                                ThreadContext.get(Constant.REQUEST_REFERENCE));

                        if(isNotificationPushed) {
                            notificationSent = true;
                            notificationTO.setStatus(NotificationStatus.SUCCESS);
                        } else {
                            notificationTO.setStatus(NotificationStatus.FAILED);
                            notificationTO.setErrorMessage(response);
                        }

                    }else{
                            notificationTO.setStatus(NotificationStatus.FAILED);
                            notificationTO.setErrorMessage(Constant.ATTRIBUTES_NOT_PRESENT);
                    }
                    break;
                case Constant.NOTIFICATION_TYPE_SMS:
                    String mobile = "";
                    try {
                        mobile = user.getAttributeStores().stream().filter(p -> p.getAttributeName().equals("MOBILE_NO") && p.getIsDefault()&&p.getAttributeState().equals(AttributeState.ACTIVE)).findAny().orElse(null).getAttributeValue();
                    }catch (NullPointerException e){
                        mobile = null;
                    }
                    if(mobile != null) {
                        String message = notificationTO.getMessageBody() != null ? notificationTO.getMessageBody().replace("<OTP>", otp) : String.format(SmsTemplate.SMS_TEMPLATE, otp);

                        JMSProducerExtension jmsProducerExtension;
                        String brokerUrl = Config.getInstance().getProperty(Constant.BROKER_URL);
                        String username = Config.getInstance().getProperty(Constant.QUEUE_USERNAME);
                        String password = Config.getInstance().getProperty(Constant.QUEUE_PASSWORD);
                        jmsProducerExtension = JMSProducerExtension.getInstance(brokerUrl,username,password);

                        boolean isNotificationPushed;
                        isNotificationPushed = jmsProducerExtension.sendSms(Config.getInstance().getProperty(Constant.SMS_QUEUE_NAME),
                                Constant.APPLICATION_IDENTITY_STORE, mobile, message, ThreadContext.get(Constant.REQUEST_REFERENCE));

                        if(isNotificationPushed) {
                            notificationSent = true;
                            notificationTO.setStatus(NotificationStatus.SUCCESS);
                        } else {
                            notificationTO.setStatus(NotificationStatus.FAILED);
                            notificationTO.setErrorMessage(response);
                        }

                    }else{
                            notificationTO.setStatus(NotificationStatus.FAILED);
                            notificationTO.setErrorMessage(Constant.ATTRIBUTES_NOT_PRESENT);
                    }
                    break;
                default:
                    notificationTO.setStatus(NotificationStatus.FAILED);
                    notificationTO.setErrorMessage(Constant.INVALID_NOTIFICATION_TYPE);
            }
        }
        if(notificationSent){
            UserToken userToken = new UserToken();
            userToken.setUserAccountId(attributeValue);
            userToken.setApplicationId(application.getApplicationId());
            int attemptCount = application.getAttemptCount() != null ? application.getAttemptCount() : Integer.parseInt(Config.getInstance().getProperty(Constant.ATTEMPT_COUNT));
            userToken.setAttemptCount(attemptCount);
            try {
                userToken = userTokenDao.getUserTokenByUserAccountIdAndApplicationId(attributeValue, application.getApplicationId(), session);
                userToken.setRandomSeed(randomSeed);
                userToken.setAttemptCount(attemptCount);
                userTokenDao.update(session, userToken);
            } catch (NoResultException | UserTokenNotFoundException e) {
                userToken.setRandomSeed(randomSeed);
                userTokenDao.create(session, userToken);
            }
        }

        if(testMode){
            hotpTO.setAuthenticationToken(otp);
        }

        OtpStatus otpStatus = notificationSent ? OtpStatus.S : OtpStatus.F;
        auditLogProcessor.addOtpAuditSendLogs(application.getApplicationName(), hotpTO.getSearchAttributes(), OtpAction.GO,otpStatus, randomSeed);
        return hotpTO;
    }

    @Override
    public HotpTO validateOtp(HotpTO hotpTO, Session session, String applicationId, String applicationName, String userAccountId, Integer tokenTtl, String enterpriseAccountId) throws AuthException {
        boolean isValid = false;
        UserToken userToken = null;
        try {
            userToken = userTokenDao.getUserTokenByUserAccountIdAndApplicationId(userAccountId, applicationId, session);
        } catch (NoResultException | UserTokenNotFoundException e) {
            hotpTO.setStatus(NotificationStatus.FAILED);
            hotpTO.setErrorMessage(Constant.INVALID_AUTHENTICATION_TOKEN);
            hotpTO.setAuthenticationToken(null);
            auditLogProcessor.addOtpAuditValidateLogs(applicationName, hotpTO.getSearchAttributes(), OtpAction.VO, OtpStatus.F, "");
            return hotpTO;
        }
        String randomSeed = userToken.getRandomSeed();
        String otpLastGeneratedOn = userToken.getDateTimeCreated().toString();
        String logData = "";
        int attemptCount = userToken.getAttemptCount();
        Timestamp currentTimestamp = Timestamp.from(Instant.now());
        String failedLogData = String.format(LogData.HOTP_LOG_DATA, otpLastGeneratedOn, currentTimestamp,
                NotificationStatus.FAILED, randomSeed, 0, userAccountId, applicationId);

        if (attemptCount > 0) {
            try {
                int validTill = tokenTtl != null ? tokenTtl : Integer.parseInt(Config.getInstance().getProperty(Constant.TTL));
                isValid = HotpUtil.validateOtp(randomSeed, 0,
                        Integer.parseInt(Config.getInstance().getProperty(Constant.HOTP_LENGTH)),
                        hotpTO.getAuthenticationToken(), userToken.getDateTimeCreated(),
                        validTill,
                        Boolean.parseBoolean(Config.getInstance().getProperty(Constant.ADD_CHECKSUM)));

                //:TODO : format the logdata using string format

                if(isValid){
                    hotpTO.setStatus(NotificationStatus.SUCCESS);
                    logData = String.format(LogData.HOTP_LOG_DATA, otpLastGeneratedOn, currentTimestamp,
                            NotificationStatus.SUCCESS, randomSeed, 0, userAccountId, applicationId);

                    userTokenDao.remove(session, userToken);

                }else{
                    attemptCount = attemptCount - 1;
                    userToken.setAttemptCount(attemptCount);
                    if(attemptCount == 0){
                        userTokenDao.remove(session, userToken);
                    }else {
                        userTokenDao.update(session, userToken);
                    }
                    hotpTO.setStatus(NotificationStatus.FAILED);
                    hotpTO.setErrorMessage(Constant.INVALID_AUTHENTICATION_TOKEN);
                    hotpTO.setAuthenticationToken(null);
                    logData = failedLogData;
                }
            }
            catch(AuthTokenExpiredException e){
                hotpTO.setStatus(NotificationStatus.FAILED);
                hotpTO.setErrorMessage(Constant.INVALID_AUTHENTICATION_TOKEN);
                hotpTO.setAuthenticationToken(null);
                logData = failedLogData;
                userTokenDao.remove(session, userToken);
            }
        }
        else {
            hotpTO.setStatus(NotificationStatus.FAILED);
            hotpTO.setErrorMessage(Constant.INVALID_AUTHENTICATION_TOKEN);
            hotpTO.setAuthenticationToken(null);
            logData = failedLogData;
        }

        AuditLogTO auditLogTO = new AuditLogTO();
        auditLogTO.setActionType(ActionType.AUTHENTICATE_ONLINE);
        auditLogTO.setCreatorId(enterpriseAccountId);
        auditLogTO.setCreatorIdType(IdType.ACCOUNT);
        auditLogTO.setOrigin(Constant.ENTERPRISE_ENTITY);
        auditLogTO.setLogData(logData);
        auditLogTO.setCreatedTime(Instant.now().toEpochMilli());

        iamExtensionService.postAuditLogs(auditLogTO);

        OtpStatus otpStatus = isValid ? OtpStatus.S : OtpStatus.F;
        auditLogProcessor.addOtpAuditValidateLogs(applicationName, hotpTO.getSearchAttributes(), OtpAction.VO, otpStatus, randomSeed);
        return hotpTO;
    }

    private void validateNotification(NotificationTO notificationTO) throws AuthException {
        if (notificationTO.getMessageBody() != null) {
            String messageBody = notificationTO.getMessageBody();
            if (!ValidationUtilV3.isValid(messageBody)|| !messageBody.contains("<OTP>")) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA_OTP());
            }
        }
    }

    @Override
    public List<OtpAuditLogSendTO> getAllOtpAuditLogSearchPaginatedList(int page, int pageSize, String applicationName, String userId) throws AttributeNotFoundException {
        logger.log(Level.DEBUG, HOTP_SERVICE_IMPL + " getAllOtpAuditLogSearchPaginatedList : start");
        List<OtpAuditSendLog> otpAuditLogList = otpAuditLogDao.getAllOtpAuditLogSearchPaginatedList(IamThreadContext.getSessionWithoutTransaction(), page, pageSize, applicationName, userId);
        List<OtpAuditLogSendTO> otpAuditLogTOList = otpAuditLogList.stream().map(OtpAuditSendLog::convertToTO).collect(Collectors.toList());
        logger.log(Level.DEBUG, HOTP_SERVICE_IMPL + " getAllOtpAuditLogSearchPaginatedList : end");
        return otpAuditLogTOList;
    }

    @Override
    public List<OtpAuditLogRespTO> getAllOtpAuditLogSearch1PaginatedList(int page, int pageSize, String attributeName, String searchText, Session session) throws AttributeNotFoundException {
        logger.log(Level.DEBUG, HOTP_SERVICE_IMPL + " getAllOtpAuditLogSearchPaginatedList : start");
        List<OtpAuditSendLog> otpAuditLogList = otpAuditLogDao.getAllOtpAuditLogSearchPaginatedList(session, page, pageSize, attributeName, searchText);
        List<OtpAuditLogRespTO> otpAuditLogTOList = new ArrayList<>();
        for(OtpAuditSendLog log:otpAuditLogList){
            otpAuditLogTOList.addAll(log.convertToRespTOList());
        }
        logger.log(Level.DEBUG, HOTP_SERVICE_IMPL + " getAllOtpAuditLogSearchPaginatedList : end");
        return otpAuditLogTOList;
    }

    @Override
    public Long getAllOtpAuditLogSearchPaginatedCount(String attributeName, String searchText, Session session) throws AttributeNotFoundException {
        logger.log(Level.DEBUG, HOTP_SERVICE_IMPL + " getAllOtpAuditLogSearchPaginatedCount : start");
        Long count = otpAuditLogDao.getAllOtpAuditLogSearchPaginatedCount(session, attributeName,searchText);
        logger.log(Level.DEBUG, HOTP_SERVICE_IMPL + " getAllOtpAuditLogSearchPaginatedCount : end");
        return count;
    }
}
