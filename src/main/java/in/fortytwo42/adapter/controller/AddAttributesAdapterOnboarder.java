package in.fortytwo42.adapter.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class AddAttributesAdapterOnboarder implements Onboarder {

    private AddAttributesAdapterOnboarder() {
        super();
    }

    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    private static final class InstanceHolder {
        private static final AddAttributesAdapterOnboarder INSTANCE = new AddAttributesAdapterOnboarder();

        private InstanceHolder() {
            super();
        }
    }

    public static AddAttributesAdapterOnboarder getInstance() {
        return AddAttributesAdapterOnboarder.InstanceHolder.INSTANCE;
    }

    private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private final UserServiceIntf userService = ServiceFactory.getUserService();

    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private final Config config = Config.getInstance();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(AddAttributesAdapterOnboarder.class);


    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->UserAttributesOnboarder -> validate |Epoch:" + System.currentTimeMillis());
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
            User tempUser = user;
            if (tempUser.getId() == null) {
                try {
                    tempUser = userService.getUserByAccountId(account.getId());
                } catch (UserNotFoundException e) {
                    logger.log(Level.DEBUG, e.getMessage());
                }
                if (tempUser == null) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
                }
            }
//            setUserDetails(tempUser, user);
            for (AttributeDataTO attributeDataTO :
                    userTO.getAttributeData()) {
                if (attributeStoreService.isAttributePresent(attributeDataTO, tempUser)) {
                    return true;
                }
            }
            return false;
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserAttributesOnboarder -> validate |Epoch:" + System.currentTimeMillis());
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session) throws AuthException {
        Transaction transaction = session.beginTransaction();
        try {
            AccountWE accountWE = null;
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            if (account.getId() == null) {
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
            User localUser = null;
            if (user.getId() == null) {
                user = userService.getActiveUser(session, account.getId());
                if (user == null) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
                }
//                setUserDetails(localUser, user);
            }
            List<AttributeStore> attributeStores = new ArrayList<>(user.getAttributeStores());
            if (userTO.getAttributeData() != null) {
                for (AttributeDataTO attributeDataTO : userTO.getAttributeData()) {
                    getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs);
                    AttributeStore attributeStore = attributeStoreService.saveAttributeData(session, attributeDataTO, user, false);
                    attributeStores.add(attributeStore);
                }
            }
            user.setAttributeStores(attributeStores);
            userService.updateUser(session, user);
            transaction.commit();
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            transaction.rollback();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserAttributesOnboarder -> process | ERROR |Epoch:" + System.currentTimeMillis());
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            transaction.rollback();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserAttributesOnboarder -> process | ERROR |Epoch:" + System.currentTimeMillis());
            throw new AuthException(e, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), e.getMessage());
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
