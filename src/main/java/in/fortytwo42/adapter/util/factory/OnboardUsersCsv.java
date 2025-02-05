
package in.fortytwo42.adapter.util.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.PolicyFacadeImpl;
import in.fortytwo42.adapter.facade.UserFacadeIntf;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.webentities.AccountPolicyWE;
import in.fortytwo42.enterprise.extension.webentities.PolicyWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class OnboardUsersCsv extends BaseCsv {

    /** The add attribute csv log. */
    private String ONBORAD_USERS_CSV_LOG = "<<<<< OnboardUsersCsv";

    private static Logger logger= LogManager.getLogger(OnboardUsersCsv.class);

    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
    
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private static final class InstanceHolder {
        private static final OnboardUsersCsv INSTANCE = new OnboardUsersCsv();

        private InstanceHolder() {
            super();
        }
    }

    public static OnboardUsersCsv getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Instantiates a new adds the attribute csv.
     */

    public OnboardUsersCsv() {
        super(PermissionUtil.ONBORAD_ADD_ATTRIBUTE);
    }

    /**
     * Gets the header list.
     *
     * @return the header list
     */
    public List<String> getHeaderList() {
        logger.log(Level.DEBUG, ONBORAD_USERS_CSV_LOG + " getHeaderList : start");
        List<String> headerList = new ArrayList<>();
        headerList.add(Constant.ACCOUNT_TYPE_HEADER);
//        headerList.add(Constant.STATE);
        headerList.add(Constant.USER_CREDENTIAL_HEADER);
        headerList.add(Constant.QUESTION_ONE);
        headerList.add(Constant.ANSWER_ONE);
        headerList.add(Constant.QUESTION_TWO);
        headerList.add(Constant.ANSWER_TWO);
        headerList.add(Constant.APPLICATION_ID_HEADER);
        headerList.add(Constant.SERVICE_NAME_HEADER);
        headerList.add(Constant.USER_ID);
        headerList.add(Constant.MOBILE_NO_HEADER);
        headerList.add(Constant.EMAIL_ID);
        headerList.add(Constant.COMMENTS_HEADER);
        logger.log(Level.DEBUG, ONBORAD_USERS_CSV_LOG + " getHeaderList : end");
        return headerList;
    }

    /**
     * Parses the CS vand update data.
     *
     * @param record
     *         the record
     * @param session
     * @param fileName
     */
    public void parseCSVandUpdateData(String[] record,String accountId, Session session, String fileName) {
        Long makerId = Long.parseLong(record[record.length-1]);
        logger.log(Level.DEBUG, ONBORAD_USERS_CSV_LOG + " parseCSVandUpdateData : start");
        String accountType = record[0].trim();
//        String state = record[1].trim();
        String userCredential = record[1].trim();
        String questionOne = record[2].trim();
        String answerOne = record[3].trim();
        String questionTwo = record[4].trim();
        String answerTwo = record[5].trim();
        String applicationId = record[6].trim();
        String serviceName = record[7].trim();
        String userId = record[8].trim();
        String mobile = record[9].trim();
        String emailId = record[10].trim();
        String comments = record[11].trim();
        logger.log(Level.DEBUG, "accountType : "+accountType);
//        logger.log(Level.DEBUG, "state : "+state);
        logger.log(Level.DEBUG, "userCredential : "+userCredential);
        logger.log(Level.DEBUG, "questionOne : "+questionOne);
        logger.log(Level.DEBUG, "answerOne : "+answerOne);
        logger.log(Level.DEBUG, "questionTwo : "+questionTwo);
        logger.log(Level.DEBUG, "answerTwo : "+answerTwo);
        logger.log(Level.DEBUG, "applicationId : "+applicationId);
        logger.log(Level.DEBUG, "serviceName : "+serviceName);
        logger.log(Level.DEBUG, "userId : "+userId);
        logger.log(Level.DEBUG, "mobile : "+mobile);
        logger.log(Level.DEBUG, "emailId : "+emailId);
        logger.log(Level.DEBUG, "comments : "+comments);
        String responseComments = null;
        String requestId = null;
        
        try {
            Application application = applicationService.getApplicationByApplicationId(applicationId);
            if (application == null) {
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
            }
            //if (application.getTwoFactorStatus().equals(TwoFactorStatus.ENABLED)) {
            UserTO userTO = new UserTO();
            userTO.setAccountType(accountType);
            userTO.setState(Constant.PARTIALLY_ACTIVE);
            userTO.setUserCredential(userCredential);
            List<in.fortytwo42.tos.transferobj.QuestionAnswerTO> questionAnswerTOs = new ArrayList<>();
            in.fortytwo42.tos.transferobj.QuestionAnswerTO questionAnswerTOOne = new in.fortytwo42.tos.transferobj.QuestionAnswerTO();
            questionAnswerTOOne.setQuestion(questionOne);
            questionAnswerTOOne.setAnswer(answerOne);
            questionAnswerTOs.add(questionAnswerTOOne);
            in.fortytwo42.tos.transferobj.QuestionAnswerTO questionAnswerTOTwo = new in.fortytwo42.tos.transferobj.QuestionAnswerTO();
            questionAnswerTOTwo.setQuestion(questionTwo);
            questionAnswerTOTwo.setAnswer(answerTwo);
            questionAnswerTOs.add(questionAnswerTOTwo);
            userTO.setQuestionAnswers(questionAnswerTOs);
            List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
            AttributeDataTO attributeDataTOUserId = new AttributeDataTO();
            attributeDataTOUserId.setAttributeName(Constant.USER_ID);
            attributeDataTOUserId.setAttributeValue(userId);
            attributeDataTOUserId.setIsRegistered(true);
            attributeDataTOUserId.setIsDefault(true);
            attributeDataTOs.add(attributeDataTOUserId);
            AttributeDataTO attributeDataTOEmailId = new AttributeDataTO();
            attributeDataTOEmailId.setAttributeName(Constant.EMAIL_ID);
            attributeDataTOEmailId.setAttributeValue(emailId);
            attributeDataTOEmailId.setIsDefault(true);
            attributeDataTOs.add(attributeDataTOEmailId);
            AttributeDataTO attributeDataTOMobileNumber = new AttributeDataTO();
            attributeDataTOMobileNumber.setAttributeName(Constant.MOBILE_NO);
            if (!Pattern.matches(Constant.MOBILE_REGEX, mobile)){
                throw new Exception("Invalid mobile No");
            }
            else{
                attributeDataTOMobileNumber.setAttributeValue(mobile);
            }
            attributeDataTOMobileNumber.setIsDefault(true);
            attributeDataTOs.add(attributeDataTOMobileNumber);
            userTO.setAttributes(attributeDataTOs);
            ArrayList<ApplicationTO> applicationTOs = new ArrayList<>();
            ApplicationTO applicationTO = new ApplicationTO();
            applicationTO.setApplicationId(applicationId);
            ArrayList<ServiceTO> services = new ArrayList<>();
            ServiceTO service = new ServiceTO();
            service.setServiceName(serviceName);
            services.add(service);
            applicationTO.setServices(services);
            applicationTOs.add(applicationTO);
            userTO.setSubscribedApplications(applicationTOs);
            userTO.setComments(comments);
            AccountPolicyWE accountPolicyWE= new AccountPolicyWE();
            PolicyFacadeImpl iamExtensionService= PolicyFacadeImpl.getInstance();
            in.fortytwo42.adapter.transferobj.PaginatedTO<PolicyWE> policyWEPaginatedTO = iamExtensionService.getAllPolicies(0,10);
            List<PolicyWE> policies = policyWEPaginatedTO.getList();
            String userRegex =null;
           long  maxUserIdLength=0;
            for(PolicyWE policyWE : policies){
                AccountType account=  policyWE.getAccountType();
                if(account.equals(AccountType.USER) && policyWE.getIsDefault().equals(true)){
                    userRegex=policyWE.getAccountPolicy().getUserIdFRegex();
                    logger.log(Level.DEBUG, "userIdRegix : "+userRegex);
                    maxUserIdLength=policyWE.getAccountPolicy().getMaxUserIdLength();
                }
            }
            if ( userRegex!=null && maxUserIdLength!=0) {
                if (userTO.getAttributes() != null || userTO.getAttributes().size() != 0) {
                    for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
                            if (attributeDataTO.getAttributeName().equals(Constant.USER_ID)) {
                                String value = attributeDataTO.getAttributeValue();
                                if (!Pattern.matches(userRegex, value)) {
                                    throw new Exception("invalid userID");
                                 }
                                if (value.length() > maxUserIdLength){
                                    throw new Exception("invalid userID length");
                                }

                            }
                    }
                }
            }
            else{
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(), "User_ID regex not found or User_ID length is 0");
            }
            userFacade.onboardUser(userTO, comments,accountId,makerId,false,false);
            //requestId = userTO.getId().toString();

        }
        catch (IllegalArgumentException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            responseComments = errorConstant.getERROR_MESSAGE_INVALID_VALUE();
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            responseComments = e.getMessage();
        }
        String status = null;
        if (responseComments == null) {
            status = Constant.SUCCESS_STATUS;
            responseComments = Constant.SUCCESS_COMMENT;
        }
        else {
            status = Constant.FAILURE_STATUS;
        }
        String updatedRecord[] = { accountType, userCredential, questionOne, answerOne, questionTwo, answerTwo, applicationId, serviceName, userId, mobile, emailId, comments, status, responseComments };
        writer.writeNext(updatedRecord);
        logger.log(Level.DEBUG, ONBORAD_USERS_CSV_LOG + " parseCSVandUpdateData : end");
    }
}
