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
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.enums.AttributeAction;

public class CamAttributeDeleter implements AttributeValidater {


    private CamAdminServiceIntf camAdminService = ServiceFactory.getCamAdminService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private final CamUserFacadeIntf camUserFacade = CamUserFacadeImpl.getInstance();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(CamAttributeDeleter.class);
    public  String  CAM_ATTRIBUTE_DELETER="CamAttributeDeleter";
    private static final class InstanceHolder {
        private static final CamAttributeDeleter INSTANCE = new CamAttributeDeleter();
        private InstanceHolder() {
            super();
        }
    }

    public static CamAttributeDeleter getInstance() {
        return CamAttributeDeleter.InstanceHolder.INSTANCE;
    }
    @Override
    public boolean validate(AttributeDataRequestTO attributeDataRequestTO, User user,AccountWE accountWE ) throws AuthException {
        boolean attributeExistonCam=true;
        if (user.getKcId() != null && !user.getKcId().isEmpty()) {
            String realm = Config.getInstance().getProperty(Constant.CAM_REALM);
             attributeExistonCam= camUserFacade.attributeExistOnCam(realm, user.getKcId(), attributeDataRequestTO.getAttributeData().getAttributeName().toUpperCase(), attributeDataRequestTO.getAttributeData().getAttributeValue()
                     ,user);
        }
        return !attributeExistonCam;
    }

    @Override
    public void process(AttributeDataRequestTO attributeDataRequestTO,User user, Session session, AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG, CAM_ATTRIBUTE_DELETER + " : start");
        String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
        if (user.getKcId() != null && !user.getKcId().isEmpty()) {
            EditUserRequest editUserRequest = new EditUserRequest();
            List<CamAttribute> camAttributeList = new ArrayList<>();
            CamAttribute camAttribute = new CamAttribute(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue.toUpperCase());
            camAttributeList.add(camAttribute);
            editUserRequest.setUserKcId(user.getKcId());
            editUserRequest.setAttributeAction(AttributeAction.DELETE);
            editUserRequest.setAttributes(camAttributeList);
            boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
            if (!camStatus) {
                throw  new  AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), " CAM Edit user failed for Delete Attribute.");
            }
        }

    }
}
