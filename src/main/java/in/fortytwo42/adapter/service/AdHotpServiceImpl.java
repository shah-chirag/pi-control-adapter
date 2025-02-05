package in.fortytwo42.adapter.service;

import javax.persistence.NoResultException;

import in.fortytwo42.adapter.processor.AuditLogProcessorImpl;
import in.fortytwo42.adapter.util.ExternalConfigUtil;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.TemplateDetails;
import in.fortytwo42.entities.bean.ValidationRule;
import in.fortytwo42.entities.enums.OtpAction;
import in.fortytwo42.entities.enums.OtpStatus;
import in.fortytwo42.tos.enums.NotificationType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.SmsTemplate;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.handler.HotpUtil;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserTokenDaoIntf;
import in.fortytwo42.daos.exception.UserTokenNotFoundException;
import in.fortytwo42.entities.bean.UserToken;
import in.fortytwo42.integration.exception.ActiveMQConectionException;
import in.fortytwo42.integration.producer.JMSProducerExtension;
import in.fortytwo42.tos.enums.NotificationStatus;
import in.fortytwo42.tos.transferobj.AdHotpTO;
import in.fortytwo42.tos.transferobj.NotificationTO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdHotpServiceImpl implements AdHotpServiceIntf{

    private TemplateDetailsServiceIntf templateDetailsDaoIntf = ServiceFactory.getTemPlateDetailsService();

    private ExternalConfigUtil externalConfig=ExternalConfigUtil.getInstance();

    private Logger logger = LogManager.getLogger(AdHotpServiceImpl.class);

    private static final class InstanceHolder {
        private static final AdHotpServiceImpl INSTANCE = new AdHotpServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static AdHotpServiceImpl getInstance() {
        return AdHotpServiceImpl.InstanceHolder.INSTANCE;
    }
    private ErrorConstantsFromConfigIntf errorConstant =ServiceFactory.getErrorConstant();

    private UserTokenDaoIntf userTokenDao = DaoFactory.getUserTokenDao();

    public AdHotpTO generateHotp(AdHotpTO adHotpTO, Session session, String applicationId, String applicationName, String searchAttributeValue, AdfsDetailsTO adfsDetailsTO, Integer attempts) throws AuthException, ActiveMQConectionException {
        String randomSeed = HotpUtil.generateRandomSeed(Integer.parseInt(Config.getInstance().getProperty(Constant.SEED_LENGTH)));

        String otp = HotpUtil.generateOTP(randomSeed,0,
                Integer.parseInt(Config.getInstance().getProperty(Constant.HOTP_LENGTH)),
                Boolean.parseBoolean(Config.getInstance().getProperty(Constant.ADD_CHECKSUM)));

        String response = "";
        boolean notificationSent =  false;
        for(NotificationTO notificationTO : adHotpTO.getNotification())
        {
            validateNotification(notificationTO,session);
            switch (notificationTO.getNotificationType()){
                case Constant.NOTIFICATION_TYPE_EMAIL:
                    String email = "";
                    String errorMessage=Constant.ATTRIBUTES_NOT_PRESENT;
                    try {
                        email = adfsDetailsTO.getEmail();
                        if(!ValidationUtilV3.isEmailValid(email)){
                            email=null;
                            errorMessage = Constant.ATTRIBUTE_NOT_VALID;
                        }
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
                        notificationTO.setErrorMessage(errorMessage);
                    }
                    break;
                case Constant.NOTIFICATION_TYPE_SMS:
                    String mobile = "";
                    errorMessage=Constant.ATTRIBUTES_NOT_PRESENT;

                    try {
                        mobile = adfsDetailsTO.getMobile();
                        if(!ValidationUtilV3.isMobileValid(mobile)){
                            mobile=null;
                            errorMessage = Constant.ATTRIBUTE_NOT_VALID;
                        }
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
                        notificationTO.setErrorMessage(errorMessage);
                    }
                    break;
                default:
                    notificationTO.setStatus(NotificationStatus.FAILED);
                    notificationTO.setErrorMessage(Constant.INVALID_NOTIFICATION_TYPE);
            }
        }
        if(notificationSent){
            UserToken userToken = new UserToken();
            userToken.setUserAccountId(searchAttributeValue.toUpperCase());
            userToken.setApplicationId(applicationId);
            int attemptCount = attempts != null ? attempts : Integer.parseInt(Config.getInstance().getProperty(Constant.ATTEMPT_COUNT));
            userToken.setAttemptCount(attemptCount);
            try {
                userToken = userTokenDao.getUserTokenByUserAccountIdAndApplicationId(searchAttributeValue.toUpperCase(), applicationId);
                userToken.setRandomSeed(randomSeed);
                userToken.setAttemptCount(attemptCount);
                userTokenDao.update(session, userToken);
            } catch (NoResultException | UserTokenNotFoundException e) {
                userToken.setRandomSeed(randomSeed);
                userTokenDao.create(session, userToken);
            }
        }

        OtpStatus otpStatus = notificationSent ? OtpStatus.S : OtpStatus.F;
        AuditLogProcessorImpl.getInstance().addOtpAuditSendLogs(applicationName, adHotpTO.getSearchAttributes(), OtpAction.SO,otpStatus, randomSeed);
        return adHotpTO;

    }
    
   public String validateNotification(NotificationTO notificationTO,Session session) throws AuthException {
        boolean isTemplateFromDb= Boolean.parseBoolean(externalConfig.getProperty(Constant.IS_TEMPLATE_FROM_DB,Constant.TEMPLATE_DETAILS_TYPE));
       TemplateDetails templateDetails = null;
        if(isTemplateFromDb) {
            if(notificationTO.getTemplateId()==null || notificationTO.getTemplateId().isEmpty()){
                notificationTO.setTemplateId(Constant.DEFAULT);
            }
            try {
                templateDetails = templateDetailsDaoIntf.getTemplateDetailsByTemplateIdAndType(notificationTO.getTemplateId(), NotificationType.valueOf(notificationTO.getNotificationType()),session);
            } catch (NotFoundException e) {
                try {
                    templateDetails = templateDetailsDaoIntf.getTemplateDetailsByTemplateIdAndType(Constant.DEFAULT, NotificationType.valueOf(notificationTO.getNotificationType()),session);
                }catch (NotFoundException e1 ){
                    logger.log(Level.ERROR,e1);
                    throw  new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), e1.getMessage());
                }
            }
            if (templateDetails != null) {
                if (!isTemplateExactMatch(notificationTO.getMessageBody(),templateDetails.getTemplate())) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_TEMPLATE_FOR_APPLICATION());
                }
            }
        }
        if (notificationTO.getMessageBody() != null) {
            String messageBody = notificationTO.getMessageBody();
            if (!ValidationUtilV3.isValid(messageBody) || !messageBody.contains("<OTP>" ) || !validateTemplateContentBasedOnFlag(messageBody,templateDetails) ) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA_OTP());
            }
        }
         return null;
        }
        private boolean isTemplateExactMatch(String templateFromUser,String templateFromDb){
            String regexPattern = templateFromDb.replaceAll("%s", ".*");
            Pattern pattern = Pattern.compile("^" + regexPattern + "$");
            Matcher matcher = pattern.matcher(templateFromUser);
            return matcher.matches();
        }

        public boolean validateTemplateContentBasedOnFlag(String template, TemplateDetails templateDetails) {

            if (templateDetails != null && templateDetails.getValidationRules()!=null && !templateDetails.getValidationRules().isEmpty() ) {
                List<ValidationRule> validationRules = templateDetails.getValidationRules();
                if(validationRules!=null && !validationRules.isEmpty()) {
                    for (ValidationRule validationRule : validationRules) {
                        boolean isPatternFound = Pattern.compile(validationRule.getRule(), Pattern.CASE_INSENSITIVE).matcher(template).find();
                        if (validationRule.getIsValidationReturn()) {
                            if (isPatternFound) {
                                return false;
                            }
                        } else {
                            if (!isPatternFound) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
}
