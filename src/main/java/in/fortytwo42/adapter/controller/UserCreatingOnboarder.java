package in.fortytwo42.adapter.controller;

import java.util.ArrayList;
import java.util.List;

import in.fortytwo42.daos.exception.DatabaseError;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
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
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import org.hibernate.exception.ConstraintViolationException;

public class UserCreatingOnboarder implements Onboarder {

    private UserCreatingOnboarder() {
        super();
    }

    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(UserCreatingOnboarder.class);

    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private static final class InstanceHolder {
        private static final UserCreatingOnboarder INSTANCE = new UserCreatingOnboarder();
        private InstanceHolder() {
            super();
        }
    }

    public static UserCreatingOnboarder getInstance() {
        return UserCreatingOnboarder.InstanceHolder.INSTANCE;
    }

    private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private final UserServiceIntf userService = ServiceFactory.getUserService();

    private final Config config = Config.getInstance();

    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->UserCreatingOnboarder -> validate |Epoch:"+System.currentTimeMillis());
        AccountWE accountWE = null;
        try {
            token = iamExtensionService.getToken(iamExtension);
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
            User tempUser = null;
            try {
                tempUser = userService.getUserByAccountId(account.getId());
            } catch (UserNotFoundException e) {
                logger.log(Level.DEBUG, e.getMessage());
            }
            if (tempUser == null) {
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserCreatingOnboarder -> validate |Epoch:"+System.currentTimeMillis());
                return false;
            }
            setUserDetails(tempUser, user);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserCreatingOnboarder -> validate |Epoch:"+System.currentTimeMillis());
            return true;
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserCreatingOnboarder -> validate |Epoch:"+System.currentTimeMillis());
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session) throws AuthException {
        Transaction transaction = session.beginTransaction();
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
            User userLocal = userService.createUser(session, account.getId(), UserRole.valueOf(account.getAccountType().name()), TwoFactorStatus.ENABLED.name(), user);
            setUserDetails(userLocal, user);
            transaction.commit();
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (DatabaseError e){
            logger.log(Level.ERROR, e.getMessage(), e);
            transaction.rollback();
            if(e.getThrowable() instanceof ConstraintViolationException){
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_ONBOARD_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_ONBOARD_ALREADY_PRESENT());
            } else {
                throw new AuthException(e, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), e.getMessage());
            }
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            transaction.rollback();
            throw new AuthException(e, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->UserCreatingOnboarder -> process | ERROR |Epoch:"+System.currentTimeMillis());
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
        toUser.setUserState(fromUser.getUserState());
        toUser.setTwoFactorStatus(fromUser.getTwoFactorStatus());
        toUser.setDateTimeCreated(fromUser.getDateTimeCreated());
        toUser.setDateTimeModified(fromUser.getDateTimeModified());
        toUser.setAuthType(fromUser.getAuthType());
        toUser.setLastLockoutTime(fromUser.getLastLockoutTime());
        toUser.setRetriesLeft(fromUser.getRetriesLeft());
        toUser.setCredentialsThroughEmail(fromUser.getCredentialsThroughEmail());
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
