package in.fortytwo42.adapter.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AttributeTempTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
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
import in.fortytwo42.enterprise.extension.exceptions.ValidationException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class AddAttributeAccountOnboarder implements Onboarder {

    private AddAttributeAccountOnboarder() {
        super();
    }

    private static final class InstanceHolder {
        private static final AddAttributeAccountOnboarder INSTANCE = new AddAttributeAccountOnboarder();
        private InstanceHolder() {
            super();
        }
    }

    public static AddAttributeAccountOnboarder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private final Config config = Config.getInstance();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(AddAttributeAccountOnboarder.class);


    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->AccountOnboarder -> validate |Epoch:"+System.currentTimeMillis());
        try {
            AccountWE accountWE = null;
            if (account.getId() == null || account.getId() != null) {
                List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
                List<AttributeTO> attributeTOs = new ArrayList<>();
//                boolean isRegisteredAttribute = isRegisteredAttribute(userTO);
                for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                    attributeDataTO.setIsDefault(true);
                    attributeTOs.add(getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs));
                }
                accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
//                return accountWE != null && accountWE.getId() != null && !accountWE.getId().isEmpty();
            } else {
                accountWE = account;
            }

            if(accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty()||  accountWE.getAttributes() == null){
                throw new AuthException(new Exception(), errorConstant.getUSER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            setAccountDetails(accountWE, account);
            List<AttributeTempTO> idsAttributes = new ArrayList<>();
            List<AttributeTempTO> adapterRequestAttributes = new ArrayList<>();

            for(AttributeTO attributeTO: accountWE.getAttributes()){
                if(attributeTO!=null && attributeTO.getStatus()!=null && attributeTO.getStatus().equals(Constant.ACTIVE)) {
                    AttributeTempTO attributeTempTO = new AttributeTempTO(attributeTO.getAttributeName().toUpperCase(), attributeTO.getAttributeValue().toUpperCase());
                    idsAttributes.add(attributeTempTO);
                }
            }

            for(AttributeDataTO attributeTO: userTO.getAttributeData()){
                AttributeTempTO attributeTempTO  = new AttributeTempTO(attributeTO.getAttributeName().toUpperCase(),attributeTO.getAttributeValue().toUpperCase());
                adapterRequestAttributes.add(attributeTempTO);
            }
            adapterRequestAttributes.removeAll(idsAttributes);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->AccountOnboarder -> validate |Epoch:"+System.currentTimeMillis());
            return adapterRequestAttributes.isEmpty();
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->AccountOnboarder -> validate |Epoch:"+System.currentTimeMillis());
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

//    private boolean isRegisteredAttribute(UserIciciTO userTO) {
//        boolean isRegisteredAttribute = false;
//        for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
//            if (attributeDataTO.getIsRegistered() != null && attributeDataTO.getIsRegistered()) {
//                isRegisteredAttribute = true;
//            }
//        }
//        return isRegisteredAttribute;
//    }

    @Override
    public void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session) throws AuthException, ValidationException {
        try {
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            List<AttributeTO> attributeTOs = new ArrayList<>();

            if(userTO.getAttributeData() != null) {
                for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
                    attributeTOs.add(getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs));
                }
            }
            account.setAttributes(attributeTOs);
            AccountWE accountWE = iamExtension.onboardAccountwithAttribute(account, account.getId(), true, token);
            setAccountDetails(accountWE, account);
            for (AttributeTO attribute:
                 accountWE.getAttributes()) {
                if(attribute.getOperationStatus().equals(AttributeOperationStatus.FAILED)){
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(), attribute.getAttributeName()+ " " +attribute.getErrorMessage());
                }
            }
            if(userTO.getUserCredential() != null && !userTO.getUserCredential().isEmpty()){
                if(userTO.getSubscribedApplications() != null && !userTO.getSubscribedApplications().isEmpty()){
                    accountWE.setApplicationId(userTO.getSubscribedApplications().get(0).getApplicationId());
                }
                String hashedPassword = StringUtil.getHex(
                        SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), userTO.getUserCredential()).getBytes()));
                accountWE.setUserCredential(hashedPassword);
                accountWE.setAttributes(null);
                iamExtension.editUserCredentials(accountWE, accountWE.getId(), token);
                logger.log(Level.DEBUG,"AddAttributeAccountOnborder : process edit credential success");
            }
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->AccountOnboarder -> process | ERROR |Epoch:"+System.currentTimeMillis());
            throw iamExceptionConvertorUtil.convertToAuthException(e);
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

    private AttributeTO getAttributeFromAttributeData(AttributeDataTO attributeDataTO, List<AttributeMetadataTO> attributeMetaDataTOs) throws AuthException {
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName("OTHERS");
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String securityType = attributeMetadataTO.getAttributeStoreSecurityPolicy();
        AttributeTO attribute = new AttributeTO();
        attribute.setAttributeName(attributeDataTO.getAttributeName());
        attribute.setIsDefault(attributeDataTO.getIsDefault());
        attribute.setAttributeValue(applySecurityPolicy(attributeDataTO.getAttributeValue(), AttributeSecurityType.valueOf(securityType)));
        if(attributeMetadataTO.getIsUnique() != null) {
            attribute.setIsUnique(attributeMetadataTO.getIsUnique());
            attributeDataTO.setIsUnique(attributeMetadataTO.getIsUnique());
        }
        return attribute;
    }

    private String applySecurityPolicy(String attributeValue, AttributeSecurityType attributeSecurityType) {
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
        return hashedAttributeValue.toUpperCase();
    }
}
