package in.fortytwo42.adapter.controller;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.AttributeState;

public class AttributeDeleterFromAdapter implements AttributeValidater {
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(AttributeDeleterFromAdapter.class);

    private static final String    ATTRIBUT_DELETER_FROM_ADAPTER_LOG = "<<<<< AttributeDeleterFromAdapter";


    private AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private static final class InstanceHolder {
        private static final AttributeDeleterFromAdapter INSTANCE = new AttributeDeleterFromAdapter();
        private InstanceHolder() {
            super();
        }
    }

    public static AttributeDeleterFromAdapter getInstance() {
        return AttributeDeleterFromAdapter.InstanceHolder.INSTANCE;
    }

    @Override
    public boolean validate(AttributeDataRequestTO attributeDataRequestTO, User user,AccountWE accountWE ) throws AuthException {
        String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();

       try {
           attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId());
           return false;
       }catch (AuthException e ){
           return  true;
       }


    }

    @Override
    public void process(AttributeDataRequestTO attributeDataRequestTO,User user, Session session, AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG,ATTRIBUT_DELETER_FROM_ADAPTER_LOG+"process : "+"start");
        Transaction transaction=session.beginTransaction();
        String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
        try {
            AttributeStore attributeTobeUpdate =
                    attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId());
            attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
            attributeStoreService.update(session, attributeTobeUpdate);
            transaction.commit();
        }catch (AuthException e){
            transaction.rollback();
            throw e;
        }
        logger.log(Level.DEBUG,ATTRIBUT_DELETER_FROM_ADAPTER_LOG+"process : "+"end");

    }
}
