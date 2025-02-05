package in.fortytwo42.adapter.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.cam.dto.CamAttribute;
import in.fortytwo42.adapter.cam.dto.EditUserRequest;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeImpl;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeIntf;
import in.fortytwo42.adapter.cam.service.CamAdminServiceIntf;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.enums.AttributeAction;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class AttributeUpdaterFromCam implements AttributeUpdater {
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger = LogManager.getLogger(AttributeUpdaterFromCam.class);

    private static final String ATTRIBUTE_UPDATER_FROM_CAM_LOG = "<<<<< AttributeUpdaterFromCam";
    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
    private final CamAdminServiceIntf camAdminService = ServiceFactory.getCamAdminService();
    private final ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    private final Config config = Config.getInstance();
    private final CamUserFacadeIntf camUserFacade = CamUserFacadeImpl.getInstance();


    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private static final class InstanceHolder {
        private static final AttributeUpdaterFromCam INSTANCE = new AttributeUpdaterFromCam();

        private InstanceHolder() {
            super();
        }
    }

    public static AttributeUpdaterFromCam getInstance() {
        return AttributeUpdaterFromCam.InstanceHolder.INSTANCE;
    }

    @Override
    public boolean validate(AttributeDataRequestTO attributeDataRequestTO, User user, AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_CAM_LOG + " validate : start");
        boolean attributeExistonCam = true;
        if (user.getKcId() != null && !user.getKcId().isEmpty()) {
            String realm = Config.getInstance().getProperty(Constant.CAM_REALM);
            attributeExistonCam = camUserFacade.attributeExistOnCam(realm, user.getKcId(), attributeDataRequestTO.getAttributeData().getAttributeName().toUpperCase(), attributeDataRequestTO.getAttributeData().getAttributeValue()
            , user);
        }
        return attributeExistonCam;
    }

    private void editCamUsersAttribute(AttributeAction action, String attributeName, String attributeValue, User user) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_CAM_LOG + " editCamUsersAttribute : start");
        try {
            EditUserRequest editUserRequest = new EditUserRequest();
            List<CamAttribute> camAttributeList = new ArrayList<>();
            CamAttribute camAttribute = new CamAttribute(attributeName, attributeValue);
            camAttributeList.add(camAttribute);
            editUserRequest.setUserKcId(user.getKcId());
            editUserRequest.setAttributes(camAttributeList);
            if (AttributeAction.DELETE.equals(action)) {
                editUserRequest.setAttributeAction(AttributeAction.DELETE);
            }
            boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
            if (!camStatus) {
                logger.log(Level.ERROR, ATTRIBUTE_UPDATER_FROM_CAM_LOG + " CAM Edit user failed for EDIT Attribute.");
                throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), errorConstant.getERROR_MESSAGE_REQUEST_IS_TIMEOUT());
            }
        }
        catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_CAM_LOG + " editCamUsersAttribute : end");
        }
    }

    public void addAttributeOnCam(AttributeDataTO attributeData, User user) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_CAM_LOG + " addAttributeOnCam : start");
        try {
            EditUserRequest editUserRequest = new EditUserRequest();
            editUserRequest.setUserKcId(user.getKcId());
            List<CamAttribute> camAttributes = new ArrayList<>();
            CamAttribute camAttribute = new CamAttribute(attributeData.getAttributeName(), attributeData.getAttributeValue());
            camAttributes.add(camAttribute);
            editUserRequest.setAttributes(camAttributes);
            boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
            if (!camStatus) {
                logger.log(Level.ERROR, ATTRIBUTE_UPDATER_FROM_CAM_LOG + " CAM Edit user failed for ADD Attribute.");
                throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), errorConstant.getERROR_MESSAGE_REQUEST_IS_TIMEOUT());
            }
        }
        catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_CAM_LOG + " addAttributeOnCam : end");
        }
    }

    @Override
    public void process(AttributeDataRequestTO attributeDataRequestTO, User user, Session session, AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_CAM_LOG + "process : " + "start");
        String realm = Config.getInstance().getProperty(Constant.CAM_REALM);
        boolean oldAttributeExistonCam = camUserFacade.attributeExistOnCam(realm, user.getKcId(), attributeDataRequestTO.getAttributeData().getAttributeName().toUpperCase(), attributeDataRequestTO.getAttributeData().getOldattributeValue(), user);
        if (oldAttributeExistonCam) {
            editCamUsersAttribute(AttributeAction.UPDATE, attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue(), user);
            editCamUsersAttribute(AttributeAction.DELETE, attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getOldattributeValue(), user);
        }
        else {
            addAttributeOnCam(attributeDataRequestTO.getAttributeData(), user);
        }
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_CAM_LOG + "process : " + "end");
    }
}
