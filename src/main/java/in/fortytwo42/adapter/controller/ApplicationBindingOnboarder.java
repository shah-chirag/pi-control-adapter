package in.fortytwo42.adapter.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import in.fortytwo42.adapter.controllers.IamThreadContext;
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
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserApplicationRelDaoImpl;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;

public class ApplicationBindingOnboarder implements Onboarder {

    private ApplicationBindingOnboarder() {
        super();
    }
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();


    private static final class InstanceHolder {
        private static final ApplicationBindingOnboarder INSTANCE = new ApplicationBindingOnboarder();
        private InstanceHolder() {
            super();
        }
    }

    public static ApplicationBindingOnboarder getInstance() { return ApplicationBindingOnboarder.InstanceHolder.INSTANCE; }

    private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private final UserServiceIntf userService = ServiceFactory.getUserService();

    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private final Config config = Config.getInstance();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(ApplicationBindingOnboarder.class);


    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->ApplicationBindingOnboarder -> validate |Epoch:"+System.currentTimeMillis());
        Session session = IamThreadContext.getSessionWithoutTransaction();
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
            for (ApplicationTO applicationTO : userTO.getSubscribedApplications()) {
                try {
                    Application application = DaoFactory.getApplicationDao().getApplicationByApplicationId(applicationTO.getApplicationId());
                    if (UserApplicationRelDaoImpl.getInstance().isApplicationUserBindingPresent(application.getId(), tempUser.getId(), session)) {
                        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->ApplicationBindingOnboarder -> validate |Epoch:"+System.currentTimeMillis());
                        return true;
                    }
                    for (ServiceTO serviceTO : applicationTO.getServices()) {
                        Service service = null;
                        try {
                            service = ServiceFactory.getServiceProcessor().getServiceByServiceName(serviceTO.getServiceName());
                        } catch (ServiceNotFoundException e) {
                            logger.log(Level.ERROR, e.getMessage(), e);
                            throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
                        }
                        if (service == null || service.getServiceName().isEmpty()) {
                            throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
                        }
                        if (!application.getServices().contains(service)) {
                            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SERVICE_FOR_APPLICATION(), errorConstant.getERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION());
                        }
                    }
                }
                catch (ApplicationNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
                
            }
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->ApplicationBindingOnboarder -> validate |Epoch:"+System.currentTimeMillis());
            return false;
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->ApplicationBindingOnboarder -> validate |Epoch:"+System.currentTimeMillis());
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
            User localUser = null;
            if (user.getId() == null) {
                try {
                    localUser = userService.getUserByAccountId(account.getId());
                } catch (UserNotFoundException e) {
                    logger.log(Level.DEBUG, e.getMessage());
                }
                if (localUser == null) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
                }
                setUserDetails(localUser, user);
            }
            userService.autoBindUserToApplication(session, userTO.getSubscribedApplications(), user, null);
            transaction.commit();
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            transaction.rollback();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->ApplicationBindingOnboarder -> process | ERROR |Epoch:"+System.currentTimeMillis());
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
