package in.fortytwo42.adapter.controller;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.AttributeState;

public class AttributeDeleterFromIds implements AttributeValidater {
    private  String ATTRIBUT_DELETER_FROM_IDS_LOG = "<<<<< AttributeDeleterFromIds";
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(AttributeDeleterFromIds.class);


    private static final class InstanceHolder {
        private static final AttributeDeleterFromIds INSTANCE = new AttributeDeleterFromIds();
        private InstanceHolder() {
            super();
        }
    }

    public static AttributeDeleterFromIds getInstance() {
        return AttributeDeleterFromIds.InstanceHolder.INSTANCE;
    }
    @Override
    public boolean validate(AttributeDataRequestTO attributeDataRequestTO, User user,AccountWE accountWE ) {
        List<AttributeTO> accountAttributes= accountWE.getAttributes();
        for(AttributeTO attribute:accountAttributes){
            if(attribute.getAttributeName().equals(attributeDataRequestTO.getAttributeData().getAttributeName()) && attribute.getAttributeValue().equalsIgnoreCase(
                    attributeDataRequestTO.getAttributeData().getAttributeValue())&& attribute.getStatus().equals(String.valueOf(AttributeState.ACTIVE))){
                return false;
            }
        }
       return true;
    }

    @Override
    public void process(AttributeDataRequestTO attributeDataRequestTO, User user, Session session,AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG,ATTRIBUT_DELETER_FROM_IDS_LOG+"process : "+"start");
        String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
        in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        attributeTO.setAttributeValue(attributeValue);
        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        iamExtensionService.deleteAttribute(attributeTO, accountWE.getId(), accountWE.getCryptoDID());
        logger.log(Level.DEBUG,ATTRIBUT_DELETER_FROM_IDS_LOG+"process : "+"end");

    }
}
