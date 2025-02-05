package in.fortytwo42.adapter.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.exceptions.UserFoundOnIDSException;
import in.fortytwo42.enterprise.extension.exceptions.ValidationException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class AddCredentialsToAccountOnboarder implements Onboarder {

    private AddCredentialsToAccountOnboarder() {
        super();
    }

    private static final class InstanceHolder {
        private static final AddCredentialsToAccountOnboarder INSTANCE = new AddCredentialsToAccountOnboarder();
        private InstanceHolder() {
            super();
        }
    }

    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    public static AddCredentialsToAccountOnboarder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private final UserServiceIntf userService = ServiceFactory.getUserService();

    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(AddCredentialsToAccountOnboarder.class);


    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> AddCredentialsToAccountOnboarder -> validate |Epoch:" + System.currentTimeMillis());
        AccountWE accountWE = null;
        try {
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
                setAccountDetails(accountWE, account);
            }
            accountWE = account;
            return !(userTO.getUserCredential() != null && accountWE.getUserCredential() == null);
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> AddCredentialsToAccountOnboarder -> validate |Epoch:" + System.currentTimeMillis());
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session)
            throws AuthException, ValidationException, UserFoundOnIDSException {
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> AddCredentialsToAccountOnboarder -> validate |Epoch:"+System.currentTimeMillis());
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
                setAccountDetails(accountWE, account);
            }
            accountWE = account;
            if (userTO.getUserCredential() != null && !userTO.getUserCredential().isEmpty() && (account.getUserCredential() == null || account.getUserCredential().isEmpty())) {
                String hashedPassword = StringUtil.getHex(
                        SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), userTO.getUserCredential()).getBytes()));
                accountWE.setUserCredential(hashedPassword);
                accountWE.setAttributes(null);
                iamExtension.editUserCredentials(accountWE, accountWE.getId(), token);
                logger.log(Level.DEBUG, " AddCredentialsToAccountOnboarder : process edit credential success");
            }
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> AddCredentialsToAccountOnboarder -> validate |Epoch:"+System.currentTimeMillis());
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    private void setUserDetails(User fromUser, User toUser) {
        toUser.setId(fromUser.getId());
        toUser.setVersion(fromUser.getVersion());
        toUser.setUserStatus(fromUser.getUserStatus());
        toUser.setAccountId(fromUser.getAccountId());
        toUser.setAttributeStores(fromUser.getAttributeStores());
        toUser.setOnboardStatus(fromUser.getOnboardStatus());
        toUser.setIamStatus(fromUser.getIamStatus());
        toUser.setRoles(fromUser.getRoles());
        toUser.setKcId(fromUser.getKcId());
        toUser.setTwoFactorStatus(fromUser.getTwoFactorStatus());
        toUser.setDateTimeCreated(fromUser.getDateTimeCreated());
        toUser.setDateTimeModified(fromUser.getDateTimeModified());
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
        AttributeTO attribute = new AttributeTO();
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
