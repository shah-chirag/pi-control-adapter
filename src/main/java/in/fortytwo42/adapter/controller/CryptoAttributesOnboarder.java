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
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
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
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class CryptoAttributesOnboarder implements Onboarder {

    private CryptoAttributesOnboarder() {
        super();
    }

    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(CryptoAttributesOnboarder.class);

    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private static final class InstanceHolder {
        private static final CryptoAttributesOnboarder INSTANCE = new CryptoAttributesOnboarder();

        private InstanceHolder() {
            super();
        }
    }

    public static CryptoAttributesOnboarder getInstance() {
        return CryptoAttributesOnboarder.InstanceHolder.INSTANCE;
    }

    private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();
    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private final Config config = Config.getInstance();

    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CryptoAttributesOnboarder -> validate |Epoch:"+System.currentTimeMillis());
        boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
        if (enableCrypto) {
            try {
                AccountWE accountWE = null;
                if (account.getId() == null) {
                    List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
                    List<AttributeTO> attributeTOs = new ArrayList<>();
                    for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                        attributeTOs.add(getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs));
                    }
                    accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
                    if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty()) {
                        throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
                    }
                    if (accountWE.getCryptoDID() == null || accountWE.getCryptoDID().isEmpty()) {
                        throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), "Account not present on crypto");
                    }
                    setAccountDetails(accountWE, account);
                }
                accountWE = account;
                for (AttributeTO attribute : account.getAttributes()) {
                    if ("ACTIVE".equals(attribute.getStatus()) && (attribute.getSignTransactionId() == null || attribute.getSignTransactionId().isEmpty())) {
                        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CryptoAttributesOnboarder -> validate |Epoch:"+System.currentTimeMillis());
                        return false;
                    }
                }
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CryptoAttributesOnboarder -> validate |Epoch:"+System.currentTimeMillis());
                return true;
            } catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CryptoAttributesOnboarder -> validate |Epoch:"+System.currentTimeMillis());
                throw iamExceptionConvertorUtil.convertToAuthException(e);
            }
        }
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CryptoAttributesOnboarder -> validate |Epoch:"+System.currentTimeMillis());
        return true;
    }

    @Override
    public void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session) throws AuthException {
        session.beginTransaction();
        try {
            AccountWE accountWE = null;
            Map<String, String> attributeValueWithPlainValue = new HashMap<String, String>();
            if (account.getId() == null) {
                List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
                List<AttributeTO> attributeTOs = new ArrayList<>();
                for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                    attributeTOs.add(getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs));
                }
                accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
                if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty()) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
                }
                if (accountWE.getCryptoDID() == null || accountWE.getCryptoDID().isEmpty()) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), "Account not present on crypto");
                }
                setAccountDetails(accountWE, account);
            }
            accountWE = account;
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            List<AttributeTO> attributeTOs = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs);
                attributeValueWithPlainValue.put(tempAttribute.getAttributeValue(), attributeDataTO.getAttributeValue());
            }
            if(userTO.getAttributeData() != null) {
                for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
                    AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs);
                    attributeValueWithPlainValue.put(tempAttribute.getAttributeValue(), attributeDataTO.getAttributeValue());
                } 
            }
            long cryptoStartTimeProcess = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> CryptoAttributesOnboarder -> registerAttributesOnCrypto | Epoch:"+cryptoStartTimeProcess);
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            Map<String, Object> attributeValueWithKey = iamExtension.registerAttributesOnCryptov2(accountWE, attributeValueWithPlainValue, token, reqRefNum);
            long cryptoEndTimeProcess = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> CryptoAttributesOnboarder -> registerAttributesOnCrypto | Epoch:"+cryptoEndTimeProcess);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "-> DIFF -> CryptoAttributesOnboarder -> registerAttributesOnCrypto"+(cryptoStartTimeProcess-cryptoEndTimeProcess));
            for (AttributeTO attributeTO : accountWE.getAttributes()) {
                String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
                if (attributeValueWithKey.containsKey(plainValue)) {
                    GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                            (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(plainValue);
                    attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                    attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                    attributeTO.setOperationStatus(AttributeOperationStatus.SUCCESSFUL);
                }
            }
            Map<String, AttributeDataTO> attributeStoreMap = new HashMap<>();
            if (userTO.getAttributeData() != null) {
                for (AttributeDataTO attributeDataTOTemp : userTO.getAttributeData()) {
                    if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                        GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue());
                        attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                        attributeStoreMap.put(attributeDataTOTemp.getAttributeName()+attributeDataTOTemp.getAttributeValue(), attributeDataTOTemp);
                    } else {
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                        attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                    }
                }
            }
            for (AttributeDataTO attributeDataTOTemp : userTO.getSearchAttributes()) {
                if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                    if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                        GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue());
                        attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                        attributeStoreMap.put(attributeDataTOTemp.getAttributeName()+attributeDataTOTemp.getAttributeValue(), attributeDataTOTemp);
                    } else {
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                        attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                    }
                }
            }
            List<AttributeStore> attributeStoreList = attributeStoreService.getAttributeByUserIdAndState(session, AttributeState.ACTIVE, user.getId());
            for (AttributeStore attributeStore: attributeStoreList) {
                AttributeDataTO attributeDataTOTemp = attributeStoreMap.get(attributeStore.getAttributeName()+attributeStore.getAttributeValue());
                if(attributeDataTOTemp != null){
                    attributeStore.setSignTransactionId(attributeDataTOTemp.getSignTransactionId());
                    attributeStoreService.update(session, attributeStore);
                }
            }
            session.getTransaction().commit();
        } catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CryptoAttributesOnboarder -> process | ERROR |Epoch:"+System.currentTimeMillis());
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (AuthException e){
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CryptoAttributesOnboarder -> process | ERROR |Epoch:"+System.currentTimeMillis());
            throw e;
        }
    }

    private void setAccountDetails(AccountWE fromAccount, AccountWE toAccount) {
        toAccount.setAttributes(fromAccount.getAttributes());
        toAccount.setAccountType(fromAccount.getAccountType());
        toAccount.setId(fromAccount.getId());
        toAccount.setIdentityScore(fromAccount.getIdentityScore());
        toAccount.setIdentityVersion(fromAccount.getIdentityVersion());
        toAccount.setParentAccountId(fromAccount.getParentAccountId());
        toAccount.setUserCredential(fromAccount.getUserCredential());
        toAccount.setCryptoDID(fromAccount.getCryptoDID());
        toAccount.setDevises(fromAccount.getDevises());
        toAccount.setErrorMessage(fromAccount.getErrorMessage());
        toAccount.setState(fromAccount.getState());
        toAccount.setStatus(fromAccount.getStatus());
        toAccount.setIsTokenEnabled(fromAccount.getIsTokenEnabled());
        toAccount.setToken(fromAccount.getToken());
        toAccount.setQuestionAnswers(fromAccount.getQuestionAnswers());
        toAccount.setUserDeviceState(fromAccount.getUserDeviceState());
        toAccount.setKcId(fromAccount.getKcId());
    }

    private AttributeTO getAttributeFromAttributeData(
            AttributeDataTO attributeDataTO,
            List<AttributeMetadataTO> attributeMetaDataTOs) throws AuthException {
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName("OTHERS");
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String securityType = attributeMetadataTO.getAttributeStoreSecurityPolicy();
        in.fortytwo42.enterprise.extension.tos.AttributeTO attribute = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        attribute.setAttributeName(attributeDataTO.getAttributeName());
        attribute.setIsDefault(attributeDataTO.getIsDefault());
        attribute.setAttributeValue(applySecurityPolicy(attributeDataTO.getAttributeValue(), AttributeSecurityType.valueOf(securityType)));
        if (attributeMetadataTO.getIsUnique() != null) {
            attribute.setIsUnique(attributeMetadataTO.getIsUnique());
            attributeDataTO.setIsUnique(attributeMetadataTO.getIsUnique());
        }
        return attribute;
    }

    private String applySecurityPolicy(String attributeValue, AttributeSecurityType attributeSecurityType) {
        String hashedAttributeValue;
        if (attributeSecurityType == AttributeSecurityType.SHA512) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData512(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        } else if (attributeSecurityType == AttributeSecurityType.SHA256) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData256(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        } else {
            hashedAttributeValue = attributeValue;
        }
        return hashedAttributeValue.toUpperCase();
    }
}
