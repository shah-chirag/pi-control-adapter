package in.fortytwo42.adapter.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.fortytwo42.adapter.cam.dto.*;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeImpl;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeIntf;
import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.*;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.*;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.OnboardStatus;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import org.apache.logging.log4j.Level;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class CamUserOnboarder implements Onboarder {

    private CamUserOnboarder() {
        super();
    }

    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(CamUserOnboarder.class);

    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private static final class InstanceHolder {
        private static final CamUserOnboarder INSTANCE = new CamUserOnboarder();
        private InstanceHolder() {
            super();
        }
    }

    public static CamUserOnboarder getInstance() {
        return CamUserOnboarder.InstanceHolder.INSTANCE;
    }

    private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private final UserServiceIntf userService = ServiceFactory.getUserService();

    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private final CamUserFacadeIntf camUserFacade = CamUserFacadeImpl.getInstance();

    private final Config config = Config.getInstance();

    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> validate |Epoch:"+System.currentTimeMillis());
        boolean isCamEnabled = userTO.getCamEnabled() != null && userTO.getCamEnabled();
        if (isCamEnabled) {
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
                String kcId = null;
                try {
                    kcId= camUserFacade.getUserDetailsWithUsername(Config.getInstance().getProperty(Constant.CAM_REALM), account.getId()).getId();
                }catch (Exception e){
                    logger.log(Level.DEBUG, e.getMessage(), e);
                }
                User tempUser = user;
                if (tempUser.getId() == null) {
                    try {
                        tempUser = userService.getUserByAccountId(account.getId(), IamThreadContext.getSessionWithoutTransaction());
                    } catch (UserNotFoundException e) {
                        logger.log(Level.DEBUG, e.getMessage());
                    }
                    if (tempUser == null) {
                        throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
                    }
                }
                if (kcId == null || kcId.isEmpty()) {
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> validate |Epoch:"+System.currentTimeMillis());
                    return false;
                }else if (tempUser.getKcId() != null && !kcId.equals(tempUser.getKcId())) {
                    userTO.setIskcIdUpdated(true);
                    user.setKcId(kcId);
                    return false;
                }
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> validate |Epoch:"+System.currentTimeMillis());
                return true;
            } catch (IAMException e) {
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> validate |Epoch:"+System.currentTimeMillis());
                logger.log(Level.ERROR, e.getMessage(), e);
                throw iamExceptionConvertorUtil.convertToAuthException(e);
            }
        }
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> validate |Epoch:"+System.currentTimeMillis());
        return true;
    }

    @Override
    public void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session) throws AuthException {
        Transaction transaction = session.beginTransaction();
        try {
            AccountWE accountWE = null;
            if (account.getId() == null) {
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - getAllAttributeMetaData |Epoch:"+System.currentTimeMillis());
                List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process - getAllAttributeMetaData |Epoch:"+System.currentTimeMillis());
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
//            User tempUser = user;
            accountWE = account;
            User localUser = null;
            if (user.getId() == null) {
                try {
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - getUserByAccountId |Epoch:"+System.currentTimeMillis());
                    localUser = userService.getUserByAccountId(account.getId());
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process - getUserByAccountId |Epoch:"+System.currentTimeMillis());
                } catch (UserNotFoundException e) {
                    logger.log(Level.DEBUG, e.getMessage());
                }
                if (localUser == null) {
                    throw new AuthException(new Exception(), errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
                }
                setUserDetails(localUser, user);
            }
            if(userTO.isIskcIdUpdated() == null || user.getKcId() == null) {
                UserCreationRequest userCreationRequest = new UserCreationRequest();
                List<Object[]> attributes = DaoFactory.getAttributeStoreDao().getAttributes(user.getAccountId(), session);
                boolean isUsername = false;
                List<CamAttribute> camAttributes = new ArrayList<>();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - getAllAttributeMetaData |Epoch:"+System.currentTimeMillis());
                List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process - getAllAttributeMetaData |Epoch:"+System.currentTimeMillis());
                List<AttributeTO> attributeTOs = new ArrayList<>(); 
                userCreationRequest.setUsername(accountWE.getId());
                for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
                    //TODO: Remove security policy for CAM attributes
                   AttributeTO attributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs);
                   attributeTOs.add(attributeTO);
                   CamAttribute camAttribute = new CamAttribute();
                    camAttribute.setCustomAttributeName(attributeTO.getAttributeName());
                    camAttribute.setCustomAttributeValue(attributeTO.getAttributeValue());
                    camAttributes.add(camAttribute);
                    /*if("USER_ID".equals(attributeTO.getAttributeName())){
                        isUsername = true;
                        userCreationRequest.setUsername(attributeTO.getAttributeValue());
                    }*/
                }

                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - getAccountByAttributes |Epoch:"+System.currentTimeMillis());
                accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process - getAccountByAttributes |Epoch:"+System.currentTimeMillis());
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - for-loop |Epoch:"+System.currentTimeMillis());
                if(userTO.getAttributeData() != null && !userTO.getAttributeData().isEmpty()){
                    for (AttributeDataTO attributeDataTO : userTO.getAttributeData() ) {
                        AttributeTO attributeTO = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs);
                        CamAttribute camAttribute = new CamAttribute();
                        camAttribute.setCustomAttributeName(attributeTO.getAttributeName());
                        camAttribute.setCustomAttributeValue(attributeTO.getAttributeValue());
                        camAttributes.add(camAttribute);
                    }
                /*if(!isUsername){
                    AttributeDataTO attributeData = userTO.getSearchAttributes().get(0);
                    AttributeTO attributeTO = getAttributeFromAttributeData(attributeData, attributeMetaDataWEs);
                    userCreationRequest.setUsername(attributeTO.getAttributeValue());
                }*/
                    
                }
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process - for-loop |Epoch:"+System.currentTimeMillis());



                //credentials ->  to support multiple credentials
                userCreationRequest.setAttributes(camAttributes);
                List<Credential> credentials = new ArrayList<>();
                Credential credential = new Credential();
                credential.setTemporary(false);
                credential.setType("password");
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - getHex |Epoch:"+System.currentTimeMillis());
                String userCredential = StringUtil.getHex(SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), accountWE.getId()).getBytes()));
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process - getHex |Epoch:"+System.currentTimeMillis());
                //String userCredential = accountWE.getUserCredential() != null ? accountWE.getUserCredential() : userTO.getUserCredential();
                credential.setValue(userCredential);
                credentials.add(credential);
                userCreationRequest.setCredentials(credentials);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - amUserFacade.onboardCamUser |Epoch:"+System.currentTimeMillis());
                UserResponseDto camUser = camUserFacade.onboardCamUser(Config.getInstance().getProperty(Constant.CAM_REALM), userCreationRequest);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process - amUserFacade.onboardCamUser |Epoch:"+System.currentTimeMillis());

                if (camUser.getUserKcId() != null) {
                    user.setKcId(camUser.getUserKcId());
                    user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_COMPLETE.name());
                    userCredential = StringUtil.getHex(SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), (accountWE.getId() + camUser.getUserKcId())).getBytes()));
                    ResetPasswordUserRequest request = new ResetPasswordUserRequest("password", userCredential, false);
                    camUserFacade.resetPassword(Config.getInstance().getProperty(Constant.CAM_REALM), user.getKcId(), request);
                    AccountWE camAccountForKcId = new AccountWE();
                    camAccountForKcId.setId(accountWE.getId());
                    camAccountForKcId.setKcId(camUser.getUserKcId());
                    iamExtension.editAccount(camAccountForKcId, accountWE.getId(), token);
                } else {
                    user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_FAILED.name());
                }
                userService.updateUser(session, user);
            }else if(userTO.isIskcIdUpdated() !=null && userTO.isIskcIdUpdated()){
                userService.updateUser(session, user);
                if(accountWE.getKcId() != null && !user.getKcId().equals(account.getKcId())){
                    AccountWE camAccountForKcId = new AccountWE();
                    camAccountForKcId.setId(accountWE.getId());
                    camAccountForKcId.setKcId(user.getKcId());
                    iamExtension.editAccount(camAccountForKcId, accountWE.getId(), token);
                }
            }
            transaction.commit();
        } catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process | ERROR |Epoch:"+System.currentTimeMillis());
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            transaction.rollback();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End ->CamUserOnboarder -> process | ERROR |Epoch:"+System.currentTimeMillis());
            throw new AuthException(e, errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        }
    }

    private User getUser(AccountWE account){
        try {
            return userService.getUserByAccountId(account.getId());
        } catch (UserNotFoundException e) {
            logger.log(Level.DEBUG, e.getMessage());
        }
        return null;
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
        if(toUser.getKcId() == null){
            toUser.setKcId(fromUser.getKcId());
        }
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
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - getAttributeFromAttributeData |Epoch:"+System.currentTimeMillis());
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
        logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->CamUserOnboarder -> process - getAttributeFromAttributeData |Epoch:"+System.currentTimeMillis());
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
