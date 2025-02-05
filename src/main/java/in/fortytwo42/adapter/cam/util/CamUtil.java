package in.fortytwo42.adapter.cam.util;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.cam.dto.CamAttribute;
import in.fortytwo42.adapter.cam.dto.Credential;
import in.fortytwo42.adapter.cam.dto.UserCreationRequest;
import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeImpl;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeIntf;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.OnboardStatus;

public class CamUtil {

    private static CamUserFacadeIntf CamUserFacade = CamUserFacadeImpl.getInstance();

    public static void onboardUserAndBind(Application application, User user, String camPassword, Session session) throws AuthException {
        if(application.getKcId() == null){
            return; //if application is not present on cam no further actions are required
        }
        if(application.getKcId() != null && user.getKcId() == null)
        {
            UserCreationRequest userCreationRequest = new UserCreationRequest();
            //List<AttributeStore> attributeStores = user.getAttributeStores();
            List<Object[]> attributeStores = null;
            if (session == null) {
                attributeStores = DaoFactory.getAttributeStoreDao().getAttributes(user.getAccountId(), session, true);
            } else {
                attributeStores = DaoFactory.getAttributeStoreDao().getAttributes(user.getAccountId(), session, false);
            }
            //AttributeStore attribute = DaoFactory.getAttributeStoreDao().getRegisteredByAttribute(user.getId());
            /*String userName = attribute.getAttributeValue();
            userCreationRequest.setUsername(userName);*/
            userCreationRequest.setUsername(user.getAccountId());
            List<CamAttribute> camAttributes = new ArrayList<>();
            for (Object[] object : attributeStores) {
                /*if(object[5] != null && (boolean)object[5]) {
                    String userName = (String) object[1];
                    userCreationRequest.setUsername(userName);
                }*/
                CamAttribute camAttribute = new CamAttribute();
                camAttribute.setCustomAttributeName((String) object[0]);
                camAttribute.setCustomAttributeValue((String) object[1]);
                camAttributes.add(camAttribute);
            }
            /*for(AttributeStore attributeStore : attributeStores){
                CamAttribute camAttribute = new CamAttribute();
                camAttribute.setCustomAttributeName(attributeStore.getAttributeName());
                camAttribute.setCustomAttributeValue(attributeStore.getAttributeValue());
                camAttributes.add(camAttribute);
            }*/
            userCreationRequest.setAttributes(camAttributes);

            //credentials ->  to support multiple credentials
            if(camPassword != null) {
                List<Credential> credentials = new ArrayList<>();
                Credential credential = new Credential();
                credential.setTemporary(false);
                credential.setType("password");
                credential.setValue(camPassword);
                credentials.add(credential);
                userCreationRequest.setCredentials(credentials);
            }

            UserResponseDto camUser = CamUserFacade.onboardCamUser(Config.getInstance().getProperty(Constant.CAM_REALM), userCreationRequest);

            if(camUser.getUserKcId() != null){
                user.setKcId(camUser.getUserKcId());
                user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_COMPLETE.name());
            }
            else {
                user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_FAILED.name());
            }
        }

        if(application.getKcId() != null && user.getKcId() != null) {
            CamUserFacade.bindUserToApplication(Config.getInstance().getProperty(Constant.CAM_REALM),
                    application.getKcId(), user.getKcId(), application.getApplicationId(), Constant.BIND_OPERATION);
        }
    }
}
