package in.fortytwo42.adapter.util.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.UserFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.NonADUserServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class UserApplicationMappingCsv extends BaseCsv{

    private String USER_APPLICATION_MAPPING_CSV_LOG = "<<<<< UserApplicationMappingCsv";

    private UserServiceIntf userService = ServiceFactory.getUserService();

    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private NonADUserServiceIntf nonADUserService = ServiceFactory.getNonADUserService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();

    private static Logger logger= LogManager.getLogger(UserApplicationMappingCsv.class);

    private static final class InstanceHolder {
        private static final UserApplicationMappingCsv INSTANCE = new UserApplicationMappingCsv();

        private InstanceHolder() {
            super();
        }
    }

    public static UserApplicationMappingCsv getInstance() {
        return UserApplicationMappingCsv.InstanceHolder.INSTANCE;
    }

    /**
     * Instantiates a new base csv.
     */
    protected UserApplicationMappingCsv() {
        super(PermissionUtil.USER_APPLICATION_MAPPING);
    }

    @Override
    protected void parseCSVandUpdateData(String[] record, String accountId, Session session, String fileName) {
        Long makerId = Long.parseLong(record[record.length-1]);
        String attributeName = record[0].trim().toUpperCase();
        String attributeValue = record[1].trim();
        String applicationId = record[2].trim();
        String services = record[3].trim();
        String action = record[4].trim();
        String userConsent = record[5].trim();

        logger.log(Level.DEBUG, "attributeName : "+attributeName);
        logger.log(Level.DEBUG, "attributeValue : "+attributeValue);
        logger.log(Level.DEBUG, "applicationId : "+applicationId);
        logger.log(Level.DEBUG, "services : "+services);
        logger.log(Level.DEBUG, "action : "+action);
        logger.log(Level.DEBUG, "userConsent : "+userConsent);

        String responseComments = null;
        Session session1 = sessionFactoryUtil.getSession();
        try {

            //getting user
            UserTO userTO = new UserTO();
            List<AttributeDataTO> searchAttributeList = new ArrayList<>();
            AttributeDataTO searchAttribute = new AttributeDataTO();
            searchAttribute.setAttributeName(attributeName);
            searchAttribute.setAttributeValue(attributeValue);
            searchAttributeList.add(searchAttribute);
            userTO.setSearchAttributes(searchAttributeList);
            String userAccountId = userService.getAccountId(userTO);
            User user = userService.getActiveUser(userAccountId);

            UserBindingTO userBindingTO = new UserBindingTO();
            userBindingTO.setId(user.getId());
            userBindingTO.setUsername(getUsername(user.getId()));

            // setting application and services
            ApplicationTO applicationTO = new ApplicationTO();
            applicationTO.setApplicationId(applicationId);
            String actualServices = services.replaceAll("\\[|\\]", "");
            String[] ser = actualServices.split(",");
            List<ServiceTO> serviceTOList = new ArrayList<>();
            for(String serviceName : ser){
                ServiceTO service = new ServiceTO();
                service.setServiceName(serviceName);
                serviceTOList.add(service);
            }
            applicationTO.setServices(serviceTOList);
            userBindingTO.setApplication(applicationTO);
            if(!userConsent.isEmpty()) {
                userBindingTO.setUserConsentRequired(Boolean.valueOf(userConsent));
            }


            if(action.equalsIgnoreCase("BIND")) {
                userFacade.approveBindServicesToUser(session1, userBindingTO, "MAKER", "maker1",makerId,false, false);
            }
            if(action.equalsIgnoreCase("UNBIND")) {
                nonADUserService.unbindServicesFromUser(session1, userBindingTO, "MAKER", null,null, false,true);
            }
            sessionFactoryUtil.closeSession(session1);
        }
        catch (AuthException e){
            session1.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage());
            responseComments = e.getMessage();
        }
        catch (IllegalArgumentException e) {
            session1.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            responseComments = errorConstant.getERROR_MESSAGE_INVALID_VALUE();
        }
        catch (Exception e) {
            session1.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            responseComments = errorConstant.getERROR_MESSAGE_INVALID_DATA();
        } finally {
            if (session1.isOpen()) {
                session1.close();
            }
        }
        String status = null;
        if (responseComments == null) {
            status = Constant.SUCCESS_STATUS;
            responseComments = Constant.SUCCESS_COMMENT;
        }
        else {
            status = Constant.FAILURE_STATUS;
        }
        String updatedRecord[] = { attributeName, attributeValue, applicationId, services, action, userConsent, status, responseComments };
        writer.writeNext(updatedRecord);
        logger.log(Level.DEBUG, USER_APPLICATION_MAPPING_CSV_LOG + " parseCSVandUpdateData : end");
    }

    @Override
    protected List<String> getHeaderList() {
        logger.log(Level.DEBUG, USER_APPLICATION_MAPPING_CSV_LOG + " getHeaderList : start");
        List<String> headerList = new ArrayList<>();
        headerList.add(Constant.CSV_ATTRIBUTE_NAME);
        headerList.add(Constant.CSV_ATTRIBUTE_VALUE);
        headerList.add(Constant.APPLICATION_ID);
        headerList.add(Constant.CSV_APPLICATION_SERVICES);
        headerList.add(Constant.CSV_ACTION);
        headerList.add(Constant.CSV_USER_CONSENT);
        logger.log(Level.DEBUG, USER_APPLICATION_MAPPING_CSV_LOG + " getHeaderList : end");
        return headerList;
    }

    public String getUsername(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.USER_ID);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }
}
