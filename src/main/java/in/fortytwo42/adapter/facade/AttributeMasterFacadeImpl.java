
package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.PermissionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AttributeVerifierTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.webentities.AttributeMetaDataWE;
import in.fortytwo42.enterprise.extension.webentities.AttributeVerifierWE;
import in.fortytwo42.entities.bean.User;

// TODO: Auto-generated Javadoc
/**
 * The Class AttributeMasterFacadeImpl.
 */
public class AttributeMasterFacadeImpl implements AttributeMasterFacadeIntf {

    /** The attribute master facade impl log. */
    private String ATTRIBUTE_MASTER_FACADE_IMPL_LOG = "<<<<< AttributeMasterFacadeImpl";

    /** The iam extension processor intf. */
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    /** The attribute master processor intf. */
    private AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
    private UserServiceIntf userService = ServiceFactory.getUserService();
    private PermissionServiceIntf permissionService = ServiceFactory.getPermissionService();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();


    private Config config = Config.getInstance();
    private Logger logger=LogManager.getLogger(AttributeMasterFacadeImpl.class);
    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    /**
     * Instantiates a new attribute master facade impl.
     */
    private AttributeMasterFacadeImpl() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final AttributeMasterFacadeImpl INSTANCE = new AttributeMasterFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of AttributeMasterFacadeImpl.
     *
     * @return single instance of AttributeMasterFacadeImpl
     */
    public static AttributeMasterFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Gets the attribute master.
     *
     * @return the attribute master
     * @throws AuthException the auth exception
     */
    @Override
    public List<AttributeMetadataTO> getAttributeMaster() throws AuthException {
        return getAttributeMasterForAccountId(null, null, null);
    }

    /**
     * Gets the attribute master for user id.
     *
     * @param userId the user id
     * @param accountType the account type
     * @param role the role
     * @return the attribute master for user id
     * @throws AuthException the auth exception
     */
    @Override
    public List<AttributeMetadataTO> getAttributeMasterForUserId(Long userId, String accountType, String role) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAttributeMasterForUserId : start");
        User user = userService.getActiveUser(userId);
        String accountId = user.getAccountId();
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAttributeMasterForUserId : end");
        return getAttributeMasterForAccountId(accountId, accountType, "");
    }

    /**
     * Gets the attribute master for account id.
     *
     * @param accountId the account id
     * @param accountType the account type
     * @param role the role
     * @return the attribute master for account id
     * @throws AuthException the auth exception
     */
    @Override
    public List<AttributeMetadataTO> getAttributeMasterForAccountId(String accountId, String accountType, String role) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAttributeMasterForAccountId : start");
        if (role != null) {
            //TODO: check permission
        }
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAttributeMasterForAccountId : end");
        return iamExtensionService.getDifferentialAttributeMetaData(accountId, accountType, role != null);
    }

    /**
     * Gets the attribute master for attribute type.
     *
     * @param attributeType the attribute type
     * @param accountType the account type
     * @param role the role
     * @return the attribute master for attribute type
     * @throws AuthException the auth exception
     */
    @Override
    public List<AttributeMetadataTO> getAttributeMasterForAttributeType(String attributeType, String accountType, String role) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAttributeMasterForAttributeType : start");
        if (role != null) {
            //TODO: check permission
        }
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAttributeMasterForAttributeType : end");
        return iamExtensionService.getAttributeMetaDataForAttributeType(attributeType, accountType, role != null);
    }

    /**
     * Gets the attribute master for attribute name and value.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     * @param role the role
     * @return the attribute master for attribute name and value
     * @throws AuthException the auth exception
     */
    @Override
    public List<AttributeMetadataTO> getAttributeMasterForAttributeNameAndValue(String attributeName, String attributeValue, String role) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAttributeMasterForAttributeNameAndValue : start");
        if (role != null) {
            //TODO: check permission
        }
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        String accountId = iamExtensionService.getAccountId(attributeName.toUpperCase(), attributeValue, iamExtension);
        //TODO: throw user not present error
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAttributeMasterForAttributeNameAndValue : end");
        return iamExtensionService.getDifferentialAttributeMetaData(accountId, null, role != null);
    }

    /**
     * Adds the attribute meta data.
     *
     * @param attributeMetadataTO the attribute metadata TO
     * @param actor the actor
     * @param role the role
     * @return the attribute metadata TO
     * @throws AuthException the auth exception
     */
    @Override
    public AttributeMetadataTO addAttributeMetaData(AttributeMetadataTO attributeMetadataTO, String actor, Long id, String role, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " addAttributeMetaData : start");
        attributeNameToUpperCase(attributeMetadataTO);
        if (!permissionService.isPermissionValidForRole(PermissionUtil.ATTRIBUTE_MASTER_ADDITION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        AttributeMetadataTO attributeMetadataTO2 = iamExtensionService.getAttributeMetadataForAttributeName(attributeMetadataTO.getAttributeName());
        if (attributeMetadataTO2 != null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT());
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            attributeMetadataTO = requestService.createAttributeMetadataAdditionRequest(session, attributeMetadataTO,actor,id, saveRequest);
//          attributeMetadataTO = attributeMasterService.createAttributeMetaDataRequest(session, attributeMetadataTO, actor);
            
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " addAttributeMetaData : end");
        return attributeMetadataTO;
    }

    /**
     * Updated attribute meta data.
     *
     * @param attributeMetadataTO the attribute metadata TO
     * @param actor the actor
     * @param role the role
     * @return the attribute metadata TO
     * @throws AuthException the auth exception
     */
    @Override
    public AttributeMetadataTO updatedAttributeMetaData(AttributeMetadataTO attributeMetadataTO, String actor,Long id, String role, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " updatedAttributeMetaData : start");
        attributeNameToUpperCase(attributeMetadataTO);
        PermissionServiceIntf permissionProcessorIntf = permissionService;
        if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.ATTRIBUTE_MASTER_UPDATION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        AttributeMetadataTO attributeMetadataTO2 = iamExtensionService.getAttributeMetadataForAttributeName(attributeMetadataTO.getAttributeName());
        if (attributeMetadataTO2 == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND());
        }
        attributeMasterService.validUpdationAttributeMetaData(attributeMetadataTO, attributeMetadataTO2);
        
        Session session = sessionFactoryUtil.getSession();
        try {
            attributeMetadataTO = requestService.createAttributeMetadataUpdateRequest(session, attributeMetadataTO,actor,id, saveRequest);
//            attributeMetadataTO = attributeMasterService.editAttributeMetaDataRequest(session, attributeMetadataTO, actor);
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " updatedAttributeMetaData : end");
        return attributeMetadataTO;
    }

    /**
     * Delete attribute meta data.
     *
     * @param attributeMetadataTO the attribute metadata TO
     * @param actor the actor
     * @param role the role
     * @return the attribute metadata TO
     * @throws AuthException the auth exception
     */
    @Override
    public AttributeMetadataTO deleteAttributeMetaData(AttributeMetadataTO attributeMetadataTO, String actor,Long id, String role, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " deleteAttributeMetaData : start");
        attributeNameToUpperCase(attributeMetadataTO);
        if (!permissionService.isPermissionValidForRole(PermissionUtil.ATTRIBUTE_MASTER_DELETION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        AttributeMetadataTO attributeMetadataTO2 = iamExtensionService.getAttributeMetadataForAttributeName(attributeMetadataTO.getAttributeName());
        if (attributeMetadataTO2 == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND());
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            attributeMetadataTO.setAttributeType(attributeMetadataTO2.getAttributeType());
            attributeMetadataTO = requestService.createAttributeMetadataDeleteRequest(session, attributeMetadataTO,actor,id, saveRequest);
//            attributeMetadataTO = attributeMasterService.deleteAttributeMetaDataRequest(session, attributeMetadataTO, actor);
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " deleteAttributeMetaData : end");
        return attributeMetadataTO;
    }

    /**
     * Gets the all attribute master.
     *
     * @return the all attribute master
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<AttributeMetadataTO> getAllAttributeMaster(int page, String searchText) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAllAttributeMaster : start");
        int limit = Integer.parseInt(config.getProperty(Constant.LIMIT));
        PaginatedTO<AttributeMetadataTO> attributeMetadataTOs = attributeMasterService.getAllAttributeMetaData(page, limit, searchText);
        logger.log(Level.DEBUG, ATTRIBUTE_MASTER_FACADE_IMPL_LOG + " getAllAttributeMaster : end");
        return attributeMetadataTOs;
    }

    @Override
    public AttributeMetadataTO getAttributeMaster(String attributeName) throws AuthException {
        AttributeMetaDataWE attributeMetadataWE = iamExtensionService.getAttributeMetadata(attributeName.toUpperCase());
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeMetadataWE.getAttributeName());
        attributeMetadataTO.setAttributeSettings(attributeMetadataWE.getAttributeSettings());
        attributeMetadataTO.setApplicableAccountTypes(attributeMetadataWE.getApplicableAccountTypes());
        attributeMetadataTO.setAttributeStoreSecurityPolicy(attributeMetadataWE.getAttributeStoreSecurityPolicy().name());
        attributeMetadataTO.setAttributeType(attributeMetadataWE.getAttributeType().name());
        attributeMetadataTO.setAttributeValueModel(attributeMetadataWE.getAttributeValueModel().name());
        attributeMetadataTO.setStatus(attributeMetadataWE.getStatus());
        if(attributeMetadataWE.getIsUnique()!=null){
          attributeMetadataTO.setIsUnique(attributeMetadataWE.getIsUnique());
        }
        List<AttributeVerifierTO> attributeVerifiers = new ArrayList<>();
        for (AttributeVerifierWE verifierWE : attributeMetadataWE.getAttributeVerifiers()) {
            AttributeVerifierTO attributeVerifierTO = new AttributeVerifierTO();
            attributeVerifierTO.setIsDefault(verifierWE.getIsDefault());
            attributeVerifierTO.setIsActive(verifierWE.getIsActive());
            attributeVerifierTO.setPriority(verifierWE.getPriority());
            attributeVerifierTO.setSourceId(verifierWE.getSourceId());
            attributeVerifierTO.setSourceType(verifierWE.getSourceType());
            attributeVerifierTO.setVerifierType(verifierWE.getVerifierType());
            attributeVerifierTO.setVerifierId(verifierWE.getVerifierId());
            attributeVerifierTO.setVerifierName(verifierWE.getVerifierName());
            attributeVerifierTO.setVerificationType(verifierWE.getVerificationType());
            attributeVerifiers.add(attributeVerifierTO);
        }
        attributeMetadataTO.setAttributeVerifiers(attributeVerifiers);
        return attributeMetadataTO;
    }

    private void attributeNameToUpperCase(AttributeMetadataTO attributeMetadataTO) {
        attributeMetadataTO.setAttributeName(attributeMetadataTO.getAttributeName().toUpperCase());
    }
}
