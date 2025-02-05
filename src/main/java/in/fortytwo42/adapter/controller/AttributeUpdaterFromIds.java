package in.fortytwo42.adapter.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AttributeOperationStatus;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.tos.GenerateAttributeClaimSelfSignedTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.enums.Status;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class AttributeUpdaterFromIds implements AttributeUpdater {
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger = LogManager.getLogger(AttributeUpdaterFromIds.class);

    private static final String ATTRIBUTE_IDS_FROM_ADAPTER_LOG = "<<<<< AttributeUpdaterFromIds";
    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
    private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private final ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();
    private final Config config = Config.getInstance();
    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();


    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private static final class InstanceHolder {
        private static final AttributeUpdaterFromIds INSTANCE = new AttributeUpdaterFromIds();

        private InstanceHolder() {
            super();
        }
    }

    public static AttributeUpdaterFromIds getInstance() {
        return AttributeUpdaterFromIds.InstanceHolder.INSTANCE;
    }

    @Override
    public boolean validate(AttributeDataRequestTO attributeDataRequestTO, User user, AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " attributeExistOnAdaptor : start");
        List<AttributeTO> attributeTOList = accountWE.getAttributes();
        String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
        boolean isExist = false;
        for (AttributeTO attribute : attributeTOList) {
            if (attribute.getAttributeValue().equalsIgnoreCase(attributeValue) && attribute.getStatus().equals(Constant.ACTIVE)) {
                isExist = true;
                break;
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " attributeExistOnAdaptor : end");
        return isExist;
    }

    private boolean oldAttributeExistOnIds(AccountWE accountWE, String oldAttributeValue) {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " oldAttributeExistOnIds : start");
        List<AttributeTO> attributeTOList = accountWE.getAttributes();
        boolean isExist = false;
        for (AttributeTO attribute : attributeTOList) {
            if (attribute.getAttributeValue().equals(oldAttributeValue) && attribute.getStatus().equals(Constant.DELETE)) {
                isExist = true;
                break;
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " oldAttributeExistOnIds : end");
        return isExist;
    }

    private AttributeTO getAttributeFromAttributeData(AttributeDataTO attributeDataTO, List<AttributeMetadataTO> attributeMetaDataTOs, boolean isEncrypted) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " getAttributeFromAttributeData : start");
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName(Constant.OTHERS);
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String securityType = attributeMetadataTO.getAttributeStoreSecurityPolicy();
        String maskPattern = (String) attributeMetadataTO.getAttributeSettings().get(Constant.MASK_PATTERN);
        AttributeTO attribute = new AttributeTO();
        attribute.setAttributeName(attributeDataTO.getAttributeName());
        attribute.setIsDefault(attributeDataTO.getIsDefault());
        String decryptedAttributeValue = attributeDataTO.getAttributeValue();
        if (isEncrypted) {
            try {
                decryptedAttributeValue = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), attributeDataTO.getAttributeValue());
            }
            catch (Exception e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_PASSWORD());
            }
        }
        if (maskPattern != null) {
            //attribute.setMaskAttributeValue(decryptedAttributeValue.replaceAll(config.getProperty(Constant.MASK_PATTERN), config.getProperty(Constant.MASK_CHARACTER)));
        }
        if (attributeMetadataTO.getIsUnique() != null) {
            attribute.setIsUnique(attributeMetadataTO.getIsUnique());
            attributeDataTO.setIsUnique(attributeMetadataTO.getIsUnique());
        }
        attribute.setAttributeValue(applySecurityPolicy(decryptedAttributeValue, AttributeSecurityType.valueOf(securityType)));
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " getAttributeFromAttributeData : end");
        return attribute;
    }

    private String applySecurityPolicy(String attributeValue, AttributeSecurityType attributeSecurityType) {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " applySecurityPolicy : start");
        String hashedAttributeValue;
        if (attributeSecurityType == AttributeSecurityType.SHA512) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData512(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        }
        else if (attributeSecurityType == AttributeSecurityType.SHA256) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData256(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        }
        else {
            hashedAttributeValue = attributeValue;
        }
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " applySecurityPolicy : end");
        return hashedAttributeValue.toUpperCase();
    }

    private void addAttributeOnIds(AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs, IAMExtensionV2 iamExtension, Token token, AccountWE account,
                                  Map<String, String> attributeValueWithPlainValue) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " addAttributeOnIds : start");
        try {
            List<AttributeTO> attributeDataTOs = new ArrayList<>();
            AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataRequestTO.getAttributeData(), attributeMetaDataWEs, false);
            attributeDataTOs.add(tempAttribute);
            attributeValueWithPlainValue.put(tempAttribute.getAttributeValue(), attributeDataRequestTO.getAttributeData().getAttributeValue());

            account.setAttributes(attributeDataTOs);
            AccountWE accountWE = iamExtension.onboardAccountwithAttribute(account, account.getId(), true, token);
            if (accountWE.getStatus() != null && Status.FAILED.name().equals(accountWE.getStatus())) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            accountWE.setUserCredential(account.getUserCredential());
            Map<String, Object> attributeValueWithKey = null;
            boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
            try {
                if (enableCrypto) {
                    attributeValueWithKey = registerAttributesOnCrypto(iamExtension, token, attributeValueWithPlainValue, accountWE, account, attributeDataTOs);
                }
                else {
                    attributeValueWithKey = new HashMap<>();
                }
            }
            catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                attributeValueWithKey = new HashMap<>();
                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = new GenerateAttributeClaimSelfSignedTO();
                generateAttributeClaimSelfSignedTO.setTransactionID("6c1f17951a9f89df82cf25980f41bcb32720dde11b9698039d31a151091a371f01c0163c034badef");
                generateAttributeClaimSelfSignedTO.setKey("753c2355163b9bb7c71065d0b26427fd");
                for (AttributeTO attributeTO : accountWE.getAttributes()) {
                    String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
                    attributeValueWithKey.put(plainValue, generateAttributeClaimSelfSignedTO);
                }
            }
            updateAttributesWithTransactionId(enableCrypto, accountWE, attributeValueWithPlainValue, attributeValueWithKey, attributeDataRequestTO);
            iamExtension.editAttributes(accountWE.getAttributes(), accountWE.getId(), token);
        }
        catch (IAMException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " addAttributeOnIds : end");
        }
    }

    private void updateAttributesWithTransactionId(boolean enableCrypto, AccountWE accountWE, Map<String, String> attributeValueWithPlainValue, Map<String, Object> attributeValueWithKey,
                                                  AttributeDataRequestTO attributeDataRequestTO) {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " updateAttributesWithTransactionId : start");
        updateAccountAttributes(enableCrypto, accountWE, attributeValueWithPlainValue, attributeValueWithKey);
        updateRequestAttributes(enableCrypto, attributeValueWithKey, attributeDataRequestTO);
        updateRequestSearchAttributes(enableCrypto, attributeValueWithKey, attributeDataRequestTO);
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " updateAttributesWithTransactionId : end");
    }

    public void updateRequestSearchAttributes(boolean enableCrypto, Map<String, Object> attributeValueWithKey, AttributeDataRequestTO attributeDataRequestTO) {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " updateRequestSearchAttributes : start");
        for (AttributeDataTO attributeDataTOTemp : attributeDataRequestTO.getSearchAttributes()) {
            if (enableCrypto) {
                if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                    if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                        GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                                (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey
                                        .get(attributeDataTOTemp.getAttributeValue());
                        attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                    }
                    else {
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                        attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                    }
                }
            }
            else {
                if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                    attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                }
                else {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                }
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " updateRequestSearchAttributes : end");
    }

    private void updateRequestAttributes(boolean enableCrypto, Map<String, Object> attributeValueWithKey, AttributeDataRequestTO attributeDataRequestTO) {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " updateRequestAttributes : start");
        if (attributeDataRequestTO.getAttributeData() != null) {
            AttributeDataTO attributeDataTOTemp = attributeDataRequestTO.getAttributeData();
            if (enableCrypto) {
                if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                    GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue());
                    attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                }
                else {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                    attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                }
            }
            else {
                if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                    attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                }
                else {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                }
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " updateRequestAttributes : end");
    }
    private void updateAccountAttributes(boolean enableCrypto, AccountWE accountWE, Map<String, String> attributeValueWithPlainValue, Map<String, Object> attributeValueWithKey) {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " updateAccountAttributes : start");
        for (AttributeTO attributeTO : accountWE.getAttributes()) {
            String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
            if (AttributeOperationStatus.SUCCESSFUL == attributeTO.getOperationStatus()) {
                if (enableCrypto) {
                    if (attributeValueWithKey.containsKey(plainValue)) {
                        GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                                (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(plainValue);
                        attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                        attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                        attributeTO.setOperationStatus(null);
                    }
                }
                else {
                    attributeTO.setOperationStatus(null);
                }
            }
            else {
                if (!enableCrypto) {
                    attributeValueWithKey.put(plainValue, attributeTO.getErrorMessage());
                }
            }
        }
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " updateAccountAttributes : end");
    }

    public Map<String, Object> registerAttributesOnCrypto(IAMExtensionV2 iamExtension, Token token, Map<String, String> attributeValueWithPlainValue, AccountWE accountWE, AccountWE account,
                                                          List<AttributeTO> attributeDataTOs) throws IAMException {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " registerAttributesOnCrypto : start");
        AccountWE accountWE1 = new AccountWE();
        accountWE1.setAttributes(attributeDataTOs);
        accountWE1.setCryptoDID(accountWE.getCryptoDID());
        accountWE1.setUserCredential(account.getUserCredential());
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " registerAttributesOnCrypto : end");
        return iamExtension.registerAttributesOnCrypto(accountWE1, attributeValueWithPlainValue, token, reqRefNum);
    }

    private void editAttributeOnIds(AccountWE accountWE, User user, AttributeDataRequestTO attributeDataRequestTO, IAMExtensionV2 iamExtension, Token token,
                                   List<AttributeMetadataTO> attributeMetaDatas) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " editAttributeOnIds : start");
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        int index = attributeMetaDatas.indexOf(attributeMetadataTO);
        attributeMetadataTO = attributeMetaDatas.get(index);
        attributeDataRequestTO.getAttributeData().setIsUnique(attributeMetadataTO.getIsUnique());
        String attributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
        try {
            AttributeStore oldAttribute =
                    attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId());
            if (oldAttribute.getIsDefault() != null && oldAttribute.getIsDefault().equals(Boolean.TRUE)) {
                attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.TRUE);
            }
            else {
                attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.FALSE);
            }
        }
        catch (AuthException e) {
            logger.log(Level.WARN, e);
        }
        AttributeTO attributeTO = new AttributeTO();
        attributeTO.setUpdatedAttributeValue(attributeDataRequestTO.getAttributeData().getAttributeValue());
        attributeTO.setAttributeValue(attributeValue);
        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        attributeTO.setIsDefault(attributeDataRequestTO.getAttributeData().getIsDefault());
        boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
        if (enableCrypto) {
            if (accountWE.getCryptoDID() == null || accountWE.getCryptoDID().isEmpty()) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), "Account not onboarded on crypto.");
            }
            editAttributeOnCrypto(iamExtension, token, accountWE, attributeTO);
        }
        try {
            iamExtensionService.editAttribute(attributeTO, accountWE.getId());
        }
        catch (AuthException e) {
            logger.log(Level.WARN, e);
            throw e;
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " editAttributeOnIds : end");
        }
    }
    private void editAttributeOnCrypto(IAMExtensionV2 iamExtension, Token token, AccountWE accountWE, AttributeTO attributeTO) {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " editAttributeOnCrypto : start");
        try {
            Map<String, Object> attributeValueWithKey = null;
            Map<String, String> attributeValueWithPlainValue = new HashMap<>();
            attributeValueWithPlainValue.put(attributeTO.getUpdatedAttributeValue(), attributeTO.getUpdatedAttributeValue());
            AccountWE updatedAttributeAccount = new AccountWE();
            updatedAttributeAccount.setId(accountWE.getId());
            AttributeTO updatedAttribute = new AttributeTO();
            updatedAttribute.setAttributeName(attributeTO.getAttributeName());
            updatedAttribute.setAttributeValue(attributeTO.getUpdatedAttributeValue().toUpperCase());
            updatedAttribute.setIsDefault(attributeTO.getIsDefault());
            List<AttributeTO> updatedAttributeList = new ArrayList<>();
            updatedAttributeList.add(updatedAttribute);
            updatedAttributeAccount.setAttributes(updatedAttributeList);
            updatedAttributeAccount.setCryptoDID(accountWE.getCryptoDID());
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            attributeValueWithKey = iamExtension.registerAttributesOnCrypto(updatedAttributeAccount, attributeValueWithPlainValue, token, reqRefNum);
            if (attributeValueWithKey.containsKey(updatedAttribute.getAttributeValue())) {
                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                        (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(updatedAttribute.getAttributeValue());
                attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                attributeTO.setOperationStatus(null);
            }
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + " editAttributeOnCrypto : end");
        }
    }
    @Override
    public void process(AttributeDataRequestTO attributeDataRequestTO, User user, Session session, AccountWE accountWE) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + "process : " + "start");
//        String attributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
//        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
//        Token token = null;
//        try {
//            token = iamExtensionService.getToken(iamExtension);
//        }
//        catch (IAMException e){
//            throw iamExceptionConvertorUtil.convertToAuthException(e);
//        }
//        List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
//        AttributeTO attributeTO = new AttributeTO();
//        attributeTO.setAttributeValue(attributeValue);
//        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
//        try {
//            iamExtensionService.deleteAttribute(attributeTO, accountWE.getId(), accountWE.getCryptoDID());
//        }
//        catch (AuthException e) {
//            logger.log(Level.WARN, e.getMessage(), e);
//        }
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        Token token = null;
        try {
            token = iamExtensionService.getToken(iamExtension);
        }
        catch (IAMException e){
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
        in.fortytwo42.enterprise.extension.tos.AttributeTO attributeTO = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        int index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
        attributeMetadataTO = attributeMetaDataWEs.get(index);
        attributeDataRequestTO.getAttributeData().setIsUnique(attributeMetadataTO.getIsUnique());
        String attributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
        AttributeTO oldAttribute = null;
        for (AttributeTO attribute : accountWE.getAttributes()) {
            if (attribute.getAttributeValue().equalsIgnoreCase(attributeValue)) {
                oldAttribute = attribute;
                break;
            }
        }
        if (oldAttribute == null) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        if (oldAttribute.getIsDefault() != null && oldAttribute.getIsDefault().equals(Boolean.TRUE)) {
            attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.TRUE);
        }
        else {
            attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.FALSE);
        }
        attributeTO.setUpdatedAttributeValue(attributeDataRequestTO.getAttributeData().getAttributeValue().toUpperCase());
        attributeTO.setAttributeValue(attributeValue);
        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        attributeTO.setIsDefault(attributeDataRequestTO.getAttributeData().getIsDefault());
        boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
        if (enableCrypto) {
            if (accountWE.getCryptoDID() == null || accountWE.getCryptoDID().isEmpty()) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), "Account not onboarded on crypto.");
            }

            Map<String, String> attributeValueWithPlainValue = new HashMap<>();
            attributeValueWithPlainValue.put(attributeTO.getUpdatedAttributeValue(), attributeTO.getUpdatedAttributeValue());
            AccountWE updatedAttributeAccount = new AccountWE();
            updatedAttributeAccount.setId(accountWE.getId());
            in.fortytwo42.enterprise.extension.tos.AttributeTO updatedAttribute = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
            updatedAttribute.setAttributeName(attributeTO.getAttributeName());
            updatedAttribute.setAttributeValue(attributeTO.getUpdatedAttributeValue().toUpperCase());
            updatedAttribute.setIsDefault(attributeTO.getIsDefault());
            List<in.fortytwo42.enterprise.extension.tos.AttributeTO> updatedAttributeList = new ArrayList<>();
            updatedAttributeList.add(updatedAttribute);
            updatedAttributeAccount.setAttributes(updatedAttributeList);
            updatedAttributeAccount.setCryptoDID(accountWE.getCryptoDID());
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            Map<String, Object> attributeValueWithKey;
            try {
                attributeValueWithKey = iamExtension.registerAttributesOnCrypto(updatedAttributeAccount, attributeValueWithPlainValue, token, reqRefNum);
            }
            catch (IAMException ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                ex.printStackTrace();
                if (ex.getErrorCode() == IAMConstants.ERR_AVMC_DUPLICATION_FAILED) {
                    attributeDataRequestTO.setStatus(Constant.SUCCESS_STATUS);
                }
                attributeValueWithKey = new HashMap<>();
                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = new GenerateAttributeClaimSelfSignedTO();
                generateAttributeClaimSelfSignedTO.setTransactionID("6c1f17951a9f89df82cf25980f41bcb32720dde11b9698039d31a151091a371f01c0163c034badef");
                generateAttributeClaimSelfSignedTO.setKey("753c2355163b9bb7c71065d0b26427fd");
                for (in.fortytwo42.enterprise.extension.tos.AttributeTO attributeToRec : updatedAttributeAccount.getAttributes()) {
                    String plainValue = attributeValueWithPlainValue.get(attributeToRec.getAttributeValue());
                    attributeValueWithKey.put(plainValue, generateAttributeClaimSelfSignedTO);
                }
            }
            if (attributeValueWithKey.containsKey(updatedAttribute.getAttributeValue())) {
                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(updatedAttribute.getAttributeValue());
                attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                attributeTO.setOperationStatus(null);
            }
        }
        iamExtensionService.editAttribute(attributeTO, accountWE.getId());
//        if (!oldAttributeExistOnIds(accountWE, attributeDataRequestTO.getAttributeData().getOldattributeValue())) {
//            Map<String, String> attributeValueWithPlainValue = new HashMap<String, String>();
//            List<AttributeTO> attributeTOs = new ArrayList<>();
//            for (AttributeDataTO attributeDataTO : attributeDataRequestTO.getSearchAttributes()) {
//                AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
//                attributeTOs.add(tempAttribute);
//                attributeValueWithPlainValue.put(tempAttribute.getAttributeValue(), attributeDataTO.getAttributeValue());
//            }
//            addAttributeOnIds(attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token, accountWE, attributeValueWithPlainValue);
//        }
//        else {
//            try {
//                editAttributeOnIds(accountWE, user, attributeDataRequestTO, iamExtension, token, attributeMetaDataWEs);
//            }
//            catch (AuthException e) {
//                logger.log(Level.WARN, e.getMessage(), e);
//                throw e;
//            }
//        }
        logger.log(Level.DEBUG, ATTRIBUTE_IDS_FROM_ADAPTER_LOG + "process : " + "end");

    }
}
