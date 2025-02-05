package in.fortytwo42.adapter.util.factory;

import java.util.ArrayList;
import java.util.List;

import in.fortytwo42.adapter.util.Config;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.AttributeStoreFacadeImpl;
import in.fortytwo42.adapter.facade.AttributeStoreFacadeIntf;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceImpl;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.daos.dao.AttributeStoreDaoImpl;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

// TODO: Auto-generated Javadoc
/**
 * The Class AddAttributeCsv.
 */
public class AddAttributeCsv extends BaseCsv{

    /** The add attribute csv log. */
    private String ADD_ATTRIBUTE_CSV_LOG = "<<<<< AddAttributeCsv";

    private static Logger logger= LogManager.getLogger();
    
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    
    private AttributeStoreFacadeIntf attributeStoreFacade = AttributeStoreFacadeImpl.getInstance();

    private AttributeStoreServiceIntf attributeStoreService = AttributeStoreServiceImpl.getInstance();

    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private final static IamExtensionServiceIntf iamExtension = ServiceFactory.getIamExtensionService();
    /**
     * Instantiates a new adds the attribute csv.
     */
    public AddAttributeCsv() {
        super(PermissionUtil.UPLOAD_ADD_ATTRIBUTE);
    }
    
    /**
     * Gets the header list.
     *
     * @return the header list
     */
    @Override
    protected List<String> getHeaderList() {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " getUserAttributeNames : start");
        List<String> headerList = new ArrayList<>();
        headerList.add(Constant.PUBLIC_ATTRIBUTE_NAME);
        headerList.add(Constant.PUBLIC_ATTRIBUTE_VALUE);
        headerList.add(Constant.CSV_ATTRIBUTE_NAME); 
        headerList.add(Constant.CSV_ATTRIBUTE_VALUE); 
        headerList.add(Constant.EVIDENCE_REQUIRED);
        headerList.add(Constant.IS_DEFAULT);
        headerList.add(Constant.CSV_OLD_ATTRIBUTE_NAME);
        headerList.add(Constant.CSV_OLD_ATTRIBUTE_VALUE);
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " getUserAttributeNames : end");
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
    @Override
    protected void parseCSVandUpdateData(String[] record,String accountId, Session session, String fileName) {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " parseCSVandUpdateData : start");
        String publicAttributeName = record[0].trim().toUpperCase();
        String publicAttributeValue = record[1].trim();
        String attributeName = record[2].trim().toUpperCase();
        String attributeValue = record[3].trim();
        String evidenceRequired = record[4].trim();
        boolean isDefault = Boolean.parseBoolean(record[5].trim());
        String oldAttributeName = record[6].trim().toUpperCase();
        String oldAttributeValue = record[7].trim();
        String comments = null;
        //String requestId = "";
        try {
            if (!ValidationUtilV3.isAttributeValueValid(attributeName, attributeValue) && !ValidationUtilV3.isAttributeValueValid(publicAttributeName, publicAttributeValue)) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE());
            }

            AttributeDataTO attributeDataTO = new AttributeDataTO();
            attributeDataTO.setAttributeName(attributeName);
            attributeDataTO.setAttributeValue(attributeValue);
            attributeDataTO.setIsDefault(isDefault);
            attributeDataTO.setEvidence(null);
            attributeDataTO.setAttributeType("PUBLIC");
            attributeDataTO.setEvidenceHash(null);
            User user = null;
            try {
                user = attributeStoreService.getAttributeByAttributeNameAndValue(publicAttributeName, publicAttributeValue).getUser();
            } catch (AuthException e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            if (isOldAttributeValid(oldAttributeName, oldAttributeValue)) {
                AttributeDataTO oldAttributeDataTO = new AttributeDataTO();
                oldAttributeDataTO.setAttributeName(oldAttributeName);
                oldAttributeDataTO.setAttributeValue(oldAttributeValue);
                AttributeStore oldAttributeStore = getUserAttributeStoreForAttribute(oldAttributeDataTO, user);
                if (oldAttributeStore == null) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
                }
                if (!isDefault && oldAttributeStore.getIsDefault()) {
                    isDefault = true;
                }
                //handle unique-nonunique senario for new attribute
                List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
                AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
                attributeMetadataTO.setAttributeName(attributeName);
                int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
                if (index < 0) {
                    attributeMetadataTO.setAttributeName("OTHERS");
                    index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
                }
                attributeMetadataTO = attributeMetaDataTOs.get(index);
                if(attributeMetadataTO.getIsUnique()) {
                    //  String attributeValue = addAttributeTO.getAttributeValue();

                    AccountWE accountWE = iamExtension.getAccount(attributeName,
                            attributeValue);

                    if (accountWE.getId() != null) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(),
                                attributeName+ ":"+errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT_TO_OTHER());
                    }
                }
                //handling non-unique attribute already present in users attribute
                List<AttributeDataTO> attributeDataT = new EntityToTOConverter<AttributeStore, AttributeDataTO>().convertEntityListToTOList(AttributeStoreDaoImpl.getInstance().getUserAttributes(user.getId()));
                for (AttributeDataTO attribute : attributeDataT) {
                    if (attribute.getAttributeName().equalsIgnoreCase(attributeName) && attribute.getAttributeValue().equalsIgnoreCase(attributeValue)) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(),
                                attributeName+ ":"+errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
                    }
                }

//                changeDefaultStatusForOtherAttributes(attributeName, user, isDefault);
                updateAttributeAndManageIsDefault(user, attributeDataTO, oldAttributeDataTO, user.getAccountId(), isDefault);
                AuditLogUtil.sendAuditLog(attributeName + " attribute with value " + oldAttributeValue + " updated to value " + attributeValue + " successfully", "ENTERPRISE", ActionType.AUTHENTICATION, accountId, IdType.ACCOUNT, "", "", user.getAccountId(), null);
            } else {
                AttributeStore userAttributeStore = getUserAttributeStoreForAttribute(attributeDataTO, user);
                if (userAttributeStore == null) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
                }
                if (userAttributeStore.getIsDefault()) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), "Attribute is already set as default");
                }
                updateAttributeAndManageIsDefault(user, attributeDataTO, attributeDataTO, user.getAccountId(), isDefault);
                AuditLogUtil.sendAuditLog(attributeName + " attribute with value " + attributeValue + " is default value updated to " + isDefault + " successfully", "ENTERPRISE", ActionType.AUTHENTICATION, accountId, IdType.ACCOUNT, "", "", user.getAccountId(), null);
            }
            //requestId = requestId+attributeDataRequestTO.getId();
        } catch (IllegalArgumentException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            comments = errorConstant.getERROR_MESSAGE_INVALID_VALUE();
        } catch (AuthException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            comments = e.getMessage();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            comments = errorConstant.getERROR_MESSAGE_INVALID_DATA();
        }
        String status = null;
        if(comments == null) {
            status = Constant.SUCCESS_STATUS;
            comments = Constant.SUCCESS_COMMENT;
        }else {
            status = Constant.FAILURE_STATUS;
        }
        String updatedRecord[] = { publicAttributeName, publicAttributeValue, attributeName, attributeValue, evidenceRequired, Boolean.toString(isDefault), oldAttributeName, oldAttributeValue, status, comments };
        writer.writeNext(updatedRecord);
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " parseCSVandUpdateData : end");
    }

    private boolean isOldAttributeValid(String attributeName, String attributeValue) {
        return attributeName != null && attributeValue != null && !attributeName.isEmpty() && !attributeValue.isEmpty();
    }

    private AttributeStore getUserAttributeStoreForAttribute(AttributeDataTO attributeDataTO, User user) {
        List<AttributeStore> attributeStores = AttributeStoreDaoImpl.getInstance().getUserAttributes(user.getId());
        if (user != null && attributeStores != null && !attributeStores.isEmpty()) {
            for (AttributeStore attributeStore : attributeStores) {
                if (attributeStore.getAttributeName().equalsIgnoreCase(attributeDataTO.getAttributeName()) && attributeStore.getAttributeValue().equals(attributeDataTO.getAttributeValue())) {
                    return attributeStore;
                }
            }
        }
        return null;
    }

    private void updateAttributeAndManageIsDefault(User user, AttributeDataTO attributeDataTO, AttributeDataTO oldAttributeDataTO, String accountId, boolean isDefault) {
        Session session = sessionFactoryUtil.getSession();
        try {
            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " updateAttributes : start");

            AccountWE accountWE = iamExtension.getAllAttributesForAccount(accountId);
            // update attributes if present in ids
            if(accountWE.getAttributes() != null) {
                logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " update attribute ids initiated");
                for (AttributeTO attributeTO : accountWE.getAttributes()) {
                    if (attributeDataTO.getAttributeName().equalsIgnoreCase(attributeTO.getAttributeName())) {
                        if (isDefault && attributeTO.getIsDefault() != null && attributeTO.getIsDefault()) {
                            attributeTO.setIsDefault(false);
                            iamExtension.editAttribute(attributeTO, accountId);
                        }
                        if (oldAttributeDataTO.getAttributeValue().equalsIgnoreCase(attributeTO.getAttributeValue()) && !attributeTO.getStatus().equals("DELETE")) {
                            attributeTO.setIsDefault(isDefault);
                            attributeTO.setUpdatedAttributeValue(attributeDataTO.getAttributeValue());
                            iamExtension.editAttribute(attributeTO, accountId);
                        }
                    }
                }
                logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " update attribute ids finished");
            }

            //update attributes if present in adapter
            List<AttributeStore> attributeStores = AttributeStoreDaoImpl.getInstance().getUserAttributes(user.getId());
            for (AttributeStore attributeStore : attributeStores) {
                if (attributeDataTO.getAttributeName().equalsIgnoreCase(attributeStore.getAttributeName())) {
                    if (isDefault && attributeStore.getIsDefault()) {
                        attributeStore.setIsDefault(false);
                        attributeStoreService.update(session, attributeStore);
                    }
                    if (oldAttributeDataTO.getAttributeValue().equals(attributeStore.getAttributeValue())) {
                        attributeStore.setIsDefault(isDefault);
                        String isAttributesInUpperCase = Config.getInstance().getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
                        Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
                        if(Boolean.TRUE.equals(isAttributeUpperCase)){
                            attributeStore.setAttributeValue(attributeDataTO.getAttributeValue().toUpperCase());
                        }
                        attributeStore.setAttributeValue(attributeDataTO.getAttributeValue());
                        attributeStoreService.update(session, attributeStore);
                    }
                }
            }

            sessionFactoryUtil.closeSession(session);
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " updateAttributes : end");
        }
    }

//        private void changeDefaultStatusForOtherAttributes(String attributeName, User user, boolean isDefault) {
//        if (isDefault) {
//            for (AttributeStore attributeStore : user.getAttributeStores()) {
//                if (attributeStore.getAttributeName().equalsIgnoreCase(attributeName)) {
//                    if (attributeStore.getIsDefault()) {
//                        updateAttributeIsDefaultAdapter(attributeStore);
//                    }
//                }
//            }
//            updateAttributeIsDefaultIds(attributeName, user.getAccountId());
//        }
//    }
//
//    private void updateAttributeIsDefaultAdapter(AttributeStore attributeStore) {
//        Session session = sessionFactoryUtil.getSession();
//        try {
//            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " updateAttributeIsDefaultAdapter : start");
//            //update attributes if present in adapter
//            attributeStore.setIsDefault(false);
//            attributeStoreService.update(session, attributeStore);
//
//            sessionFactoryUtil.closeSession(session);
//        } catch (Exception e) {
//            session.getTransaction().rollback();
//            IAMLogger.getInstance().log(Level.ERROR, e.getMessage(), e);
//        }
//        finally {
//            if (session.isOpen()) {
//                session.close();
//            }
//            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " updateAttributeIsDefaultAdapter : end");
//        }
//    }
//
//    private void updateAttributeIsDefaultIds(String attributeName, String accountId) {
//        try {
//            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " updateAttributeIsDefaultIds : start");
//            AccountWE accountWE = iamExtension.getAllAttributesForAccount(accountId);
//            // update attributes if present in ids
//            if(accountWE.getAttributes() != null) {
//                for (AttributeTO attributeTO : accountWE.getAttributes()) {
//                    if (attributeName.equalsIgnoreCase(attributeTO.getAttributeName())) {
//                        attributeTO.setIsDefault(false);
//                        iamExtension.editAttribute(attributeTO, accountId);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            IAMLogger.getInstance().log(Level.ERROR, e.getMessage(), e);
//        }
//        finally {
//            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " updateAttributeIsDefaultIds : end");
//        }
//    }

}
