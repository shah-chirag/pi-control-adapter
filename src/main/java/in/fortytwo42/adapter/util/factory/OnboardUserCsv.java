
package in.fortytwo42.adapter.util.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.UserFacadeIntf;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;

public class OnboardUserCsv extends BaseCsv {

    private String ONBORAD_USER_CSV_LOG = "<<<<< OnboardUserCsv";

    private static Logger logger= LogManager.getLogger(OnboardUserCsv.class);

    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();

    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private static final class InstanceHolder {
        private static final OnboardUserCsv INSTANCE = new OnboardUserCsv();

        private InstanceHolder() {
            super();
        }
    }

    public static OnboardUserCsv getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Instantiates a new adds the attribute csv.
     */

    public OnboardUserCsv() {
        super(PermissionUtil.ONBORAD_ADD_ATTRIBUTE);
    }

    @Override
    protected List<String> getHeaderList() {
        logger.log(Level.DEBUG, ONBORAD_USER_CSV_LOG + " getHeaderList : start");
        List<String> headerList = new ArrayList<>();
        headerList.add(Constant.CSV_USER_ID);
        headerList.add(Constant.CSV_MOBILE_NO);
        headerList.add(Constant.CSV_EMAIL_ID);
        headerList.add(Constant.CSV_USER_CREDENTIAL);
        headerList.add(Constant.CSV_APPLICATION_ID);
        headerList.add(Constant.CSV_SERVICE_NAME);
        logger.log(Level.DEBUG, ONBORAD_USER_CSV_LOG + " getHeaderList : end");
        return headerList;
    }

    @Override
    protected void parseCSVandUpdateData(String[] record, String accountId, Session session, String fileName) {
        logger.log(Level.DEBUG, ONBORAD_USER_CSV_LOG + " parseCSVandUpdateData : start");
        String searchAttributeValue = record[0].trim();
        String attributeMobile = record[1].trim();
        String attributeEmail = record[2].trim();
        String userCredential = record[3].trim();
        String applicationId = record[4].trim();
        String serviceName = record[5].trim();
        String comments = null;
        try {
            Application application = applicationService.getApplicationByApplicationId(applicationId);
            if (application == null) {
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
            }
            if (application.getTwoFactorStatus().equals(TwoFactorStatus.ENABLED)) {
                UserIciciTO userTO = new UserIciciTO();
                List<AttributeDataTO> serchAttributes = new ArrayList<>();
                AttributeDataTO searchAttribute = new AttributeDataTO();
                searchAttribute.setAttributeName(Constant.USER_ID);
                searchAttribute.setAttributeValue(searchAttributeValue);
                searchAttribute.setIsRegistered(Boolean.TRUE);
                serchAttributes.add(searchAttribute);
                userTO.setSearchAttributes(serchAttributes);
                List<AttributeDataTO> attributesData = new ArrayList<>();
                if(!attributeMobile.isEmpty()) {
                    if(!attributeMobile.equalsIgnoreCase("null")) {
                        AttributeDataTO attributeData1 = new AttributeDataTO();
                        attributeData1.setAttributeName(Constant.MOBILE_NO);
                        attributeData1.setAttributeValue(attributeMobile);
                        attributeData1.setIsDefault(Boolean.TRUE);
                        attributesData.add(attributeData1);
                    }
                }

                if(!attributeEmail.isEmpty()) {
                    if(!attributeEmail.equalsIgnoreCase("null")) {
                        AttributeDataTO attributeData2 = new AttributeDataTO();
                        attributeData2.setAttributeName(Constant.EMAIL_ID);
                        attributeData2.setAttributeValue(attributeEmail);
                        attributeData2.setIsDefault(Boolean.TRUE);
                        attributesData.add(attributeData2);
                    }
                }

                userTO.setAttributeData(attributesData);
                List<ApplicationTO> subscribedApplications = new ArrayList<>();
                ApplicationTO applicationTO = new ApplicationTO();
                applicationTO.setApplicationId(applicationId);
                List<ServiceTO> services = new ArrayList<>();
                ServiceTO service = new ServiceTO();
                service.setServiceName(serviceName);
                services.add(service);
                applicationTO.setServices(services);
                subscribedApplications.add(applicationTO);
                userTO.setSubscribedApplications(subscribedApplications);
                if(!userCredential.isEmpty()){
                    if(!userCredential.equalsIgnoreCase("null")){
                        userTO.setUserCredential(userCredential);
                    }
                }
                if(application.getKcId() != null && userTO.getUserCredential() == null){
                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_CREDENTIALS_NOT_PRESENT(), errorConstant.getERROR_MESSAGE_USER_CREDENTIALS_NOT_PRESENT());
                }
                logger.log(Level.DEBUG, "CSV onboard user - User TO : "+new Gson().toJson(userTO));
                userFacade.onboardUserV4(userTO);
            }
        }
        catch (AuthException e){
            logger.log(Level.ERROR, e.getMessage());
            comments = e.getMessage();
        }
        catch (IllegalArgumentException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            comments = errorConstant.getERROR_MESSAGE_INVALID_VALUE();
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            comments = errorConstant.getERROR_MESSAGE_INVALID_DATA();
        }
        String status = null;
        if (comments == null) {
            status = Constant.SUCCESS_STATUS;
            comments = Constant.SUCCESS_COMMENT;
        }
        else {
            status = Constant.FAILURE_STATUS;
        }
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        String updatedRecord[] = { searchAttributeValue, attributeMobile, attributeEmail, userCredential, applicationId, serviceName, status, comments, requestReferenceNumber};
        writer.writeNext(updatedRecord);
        logger.log(Level.DEBUG, ONBORAD_USER_CSV_LOG + " parseCSVandUpdateData : end");
    }

}
