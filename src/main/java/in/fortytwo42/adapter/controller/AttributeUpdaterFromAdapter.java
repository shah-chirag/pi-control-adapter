package in.fortytwo42.adapter.controller;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.AttributeState;

public class AttributeUpdaterFromAdapter implements AttributeUpdater {
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(AttributeUpdaterFromAdapter.class);

    private static final String ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG = "<<<<< AttributeUpdaterFromAdapter";
    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();


    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private static final class InstanceHolder {
        private static final AttributeUpdaterFromAdapter INSTANCE = new AttributeUpdaterFromAdapter();

        private InstanceHolder() {
            super();
        }
    }

    public static AttributeUpdaterFromAdapter getInstance() {
        return AttributeUpdaterFromAdapter.InstanceHolder.INSTANCE;
    }

    @Override
    public boolean validate(AttributeDataRequestTO attributeDataRequestTO, User user, AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG + " attributeExistOnAdaptor : start");
        boolean isExist = false;
        try {
            AttributeStore attributeStore = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue(), user.getId());
            if (attributeStore != null) {
                isExist = true;
            }
        }
        catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG + " attributeExistOnAdaptor : end");
        return isExist;
    }
    private void validateOldAttributeValue(String attributeName, String oldAttributeValue, Long userId) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG + " validateOldAttributeValue : start");
        attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName, oldAttributeValue, userId);
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG + " validateOldAttributeValue : end");
    }

    private void deleteForTakeOverAttributeOnAdaptor(Session session, User user, String attributeName, String attributeValue) {
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG + " deleteForTakeOverAttributeOnAdaptor : start");
        session.beginTransaction();
        try {
            AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName, attributeValue, user.getId());
            attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
            attributeStoreService.update(session, attributeTobeUpdate);
            session.getTransaction().commit();
            session.clear();
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.WARN, e.getMessage(), e);
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG + " deleteForTakeOverAttributeOnAdaptor : end");
        }
    }
    @Override
    public void process(AttributeDataRequestTO attributeDataRequestTO, User user, Session session, AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG + "process : " + "start");
        session.beginTransaction();
        try {
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            String oldattributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
            String attributeName = attributeDataRequestTO.getAttributeData().getAttributeName();
            attributeMetadataTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            int index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
            attributeMetadataTO = attributeMetaDataWEs.get(index);
            attributeDataRequestTO.getAttributeData().setIsUnique(attributeMetadataTO.getIsUnique());
            if (attributeMetadataTO .getIsUnique() != null && Boolean.TRUE.equals(attributeMetadataTO.getIsUnique())) {
                try {
                    AttributeStore oldAttribute =
                            attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), oldattributeValue,
                                    user.getId());
                    if (oldAttribute.getIsDefault() != null && oldAttribute.getIsDefault().equals(Boolean.TRUE)) {
                        attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.TRUE);
                    }
                    else {
                        attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.FALSE);
                    }
                }
                catch (AuthException e) {
                    logger.log(Level.WARN, e);
                    throw e;
                }
            }
            attributeStoreService.saveAttributeData(session, attributeDataRequestTO.getAttributeData(), null, user, false);
            AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), oldattributeValue,
                    user.getId());
            attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
            attributeStoreService.update(session, attributeTobeUpdate);
            session.getTransaction().commit();
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        }
        logger.log(Level.DEBUG, ATTRIBUTE_UPDATER_FROM_ADAPTER_LOG + "process : " + "end");

    }
}
