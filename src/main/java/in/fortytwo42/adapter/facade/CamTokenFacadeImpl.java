package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.fortytwo42.adapter.cam.dto.CamAttribute;
import in.fortytwo42.adapter.cam.dto.Credential;
import in.fortytwo42.adapter.cam.dto.UserCreationRequest;
import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.util.handler.CAMUtil;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import org.apache.logging.log4j.Level;
import org.hibernate.Session;
import org.keycloak.representations.AccessTokenResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import in.fortytwo42.adapter.cam.dto.ResetPasswordUserRequest;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeImpl;
import in.fortytwo42.adapter.cam.facade.CamUserFacadeIntf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keycloak.representations.idm.UserRepresentation;

import in.fortytwo42.adapter.cam.rest.CamServerAPI;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.util.AttributeValidationUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.TokenRequestTO;

public class CamTokenFacadeImpl implements CamTokenFacadeIntf {

    private static Logger logger= LogManager.getLogger(CamTokenFacadeImpl.class);
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private final String ADHOTP_FACADE_IMPL_LOG = "<<<<< CamTokenFacadeImpl";
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    Config config = Config.getInstance();

    CamServerAPI camServerAPI = new CamServerAPI(config.getProperty(Constant.CAM_ADMIN_URL));
    private final UserServiceIntf userService = ServiceFactory.getUserService();
    private final CamUserFacadeIntf camUserFacade = CamUserFacadeImpl.getInstance();
    private final AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();
    private final AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
    private static final String CAM_USER_ID = "CAM_USER_ID";
    private static final String CAM_MOBILE_NO = "CAM_MOBILE_NO";
    private static final String CAM_EMAIL_ID = "CAM_EMAIL_ID";



    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final CamTokenFacadeImpl INSTANCE = new CamTokenFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    public static CamTokenFacadeImpl getInstance() {
        return CamTokenFacadeImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public AccessTokenResponse getToken(String realm,TokenRequestTO tokenRequestTO) throws AuthException, IAMException {

        attributeNameToUpperCase(tokenRequestTO);
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " getToken : start");
//        validateSearchAttributes(tokenRequestTO.getSearchAttributes());
        for (AttributeDataTO attributeDataTO : tokenRequestTO.getSearchAttributes()) {
            AttributeValidationUtil.validateSearchAttributeValueAndUniqueness(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());
        }
        AccountWE accountWE = getAccountId(tokenRequestTO);
        String hashedUserPassword = StringUtil.getHex(
                SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), tokenRequestTO.getUserCredential()).getBytes()));
        in.fortytwo42.enterprise.extension.tos.PasswordTO passwordTO2 = new in.fortytwo42.enterprise.extension.tos.PasswordTO();
        passwordTO2.setPassword(hashedUserPassword);
        passwordTO2.setAccountObjectId(accountWE.getId());
        passwordTO2.setApplicationId(tokenRequestTO.getClientId());
        String hashedCamPassword;
        //verify password on ids
        try {
            iamExtensionService.verifyPassword(passwordTO2);
        }
        catch (Exception e){
            logger.log(Level.ERROR,e.getMessage(),e);
            throw new AuthException(null,errorConstant.getERROR_CODE_PERMISSION_DENIED(),errorConstant.getERROR_MESSAGE_INVALID_USER_CREDENTIALS());
        }
        AccessTokenResponse accessTokenResponse = null;
        boolean isKcIdUpdated = false;
        if(accountWE.getKcId() != null){
            hashedCamPassword = StringUtil.getHex(SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), (accountWE.getId() + accountWE.getKcId())).getBytes()));
            tokenRequestTO.setUserCredential(hashedCamPassword);
            accessTokenResponse = camServerAPI.getToken(realm, tokenRequestTO,accountWE.getId());
        }
        User user = userService.getActiveUser(accountWE.getId());
        if(accountWE.getKcId() == null || (tokenRequestTO.getCamError()!=null && tokenRequestTO.getCamError())){
            accessTokenResponse = processGetCAMToken(tokenRequestTO,accountWE,realm);
            isKcIdUpdated = true;
        }
        if(isKcIdUpdated || !accountWE.getKcId().equals(user.getKcId())){
            updateKcIdOnAdapter(user, accountWE);
        }
        String finalResponseToken = accessTokenResponse.getToken();
        CAMUtil.getInstance().submitTask(()->{
            try {
                camAttributeSync(realm,finalResponseToken,accountWE);
            } catch (Exception e) {
                logger.log(Level.ERROR,ADHOTP_FACADE_IMPL_LOG,e.getMessage(),e);
            }
            try {
                adapterAttributeSync(user,accountWE);
            } catch (Exception e) {
                logger.log(Level.ERROR,ADHOTP_FACADE_IMPL_LOG,e.getMessage(),e);
            }
        });
        return accessTokenResponse;

    }

    private void updateKcIdOnAdapter(User user, AccountWE accountWE) throws AuthException {
        Session session = SessionFactoryUtil.getInstance().openSessionWithoutTransaction();
        try{
            session.beginTransaction();
            user.setKcId(accountWE.getKcId());
            userService.update(session, user);
            session.getTransaction().commit();
        }
        catch (Exception e){
            logger.log(Level.ERROR,e.getMessage(),e);
            session.getTransaction().rollback();
        }
        finally {
            if(session.isOpen()){
                session.close();
            }
        }
    }

    public void camAttributeSync(String realm, String token, AccountWE account) {
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " camAttributeSync : start");
        Map<String, List<String>> camAttributes=getCAMUserDetails(token);
        Map<String, List<String>> updatedCamAttributes=new HashMap<>(camAttributes);
        boolean isDiffFound=false;
        int count=0;
        for(AttributeTO idsAttribute:account.getAttributes()){
            if(idsAttribute.getStatus().equals(Constant.ACTIVE)){
                List<String> camAttributeValues=camAttributes.get(idsAttribute.getAttributeName());
                boolean isAttributePresentOnCAM = camAttributeValues.stream().anyMatch(value -> value.equalsIgnoreCase(idsAttribute.getAttributeValue()));
                if(!isAttributePresentOnCAM){
                    isDiffFound=true;
                    count++;
                    updatedCamAttributes.computeIfAbsent(idsAttribute.getAttributeName(), k -> new ArrayList<>()).add(idsAttribute.getAttributeValue());
                }
            }
        }
        if(isDiffFound){
            logger.log(Level.INFO,ADHOTP_FACADE_IMPL_LOG+ " ======CAM_ATTRIBUTE_DIFF====== : ACCOUNT_ID: "+account.getId().toString()+" DIFF FOUND : "+count);
            try{
                camUserFacade.updateAllCamAttributes(realm,account.getKcId(), updatedCamAttributes);
                logger.log(Level.INFO,ADHOTP_FACADE_IMPL_LOG + " ======CAM_ATTRIBUTE_DIFF====== : ACCOUNT_ID: " + account.getId().toString() + " DIFF RESOLUTION SUCCESS");
            }
            catch(AuthException e){
                logger.log(Level.INFO,ADHOTP_FACADE_IMPL_LOG+ " ======CAM_ATTRIBUTE_DIFF====== : ACCOUNT_ID: "+account.getId().toString()+" DIFF RESOLUTION FAILED : "+e.getMessage());
                logger.log(Level.ERROR,e.getMessage(),e);
            }
        }
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " camAttributeSync : end");
    }

    public void adapterAttributeSync(User user, AccountWE account) {
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " adapterAttributeSync : start");
        Session session = SessionFactoryUtil.getInstance().openSessionWithoutTransaction();
        try{
            List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
            List<AttributeStore> attributeStore = attributeStoreService.getAttributeByUserIdAndState(session,AttributeState.ACTIVE,user.getId());
            List<AttributeDataTO> attributeDataTOList = new ArrayList<>();
            int count=0;
            boolean isDiffFound=false;
            for(AttributeTO idsAttribute:account.getAttributes()){
                boolean attributePresent = false;
                for(AttributeStore attributeStoreAttributes: attributeStore){
                    if(idsAttribute.getAttributeName().equalsIgnoreCase(attributeStoreAttributes.getAttributeName()) && idsAttribute.getAttributeValue().equalsIgnoreCase(attributeStoreAttributes.getAttributeValue())){
                        attributePresent = true;
                    }
                }
                if(!attributePresent){
                    isDiffFound = true;
                    count++;
                    AttributeDataTO newAttributeData = new AttributeDataTO();
                    newAttributeData.setAttributeName(idsAttribute.getAttributeName());
                    newAttributeData.setAttributeValue(idsAttribute.getAttributeValue());
                    newAttributeData.setAttributeType(idsAttribute.getAttributeType());
                    newAttributeData.setIsDefault(idsAttribute.getIsDefault());
                    newAttributeData.setIsRegistered(idsAttribute.getIsRegistered());
                    newAttributeData.setSignTransactionId(idsAttribute.getSignTransactionId());
                    setIsUniqueForAttributeData(newAttributeData, attributeMetaDataWEs);
                    attributeDataTOList.add(newAttributeData);
                }
            }
            if(isDiffFound){
                logger.log(Level.INFO,ADHOTP_FACADE_IMPL_LOG+ " ======ADAPTER_ATTRIBUTE_DIFF====== : ACCOUNT_ID: "+account.getId().toString()+" DIFF FOUND : "+count);
                try{
                    session.beginTransaction();
                    User updatedUser = userService.getActiveUser(session,user.getId());
                    updatedUser.setAttributeStores(attributeStore);
                    for(AttributeDataTO attributeDataTO:attributeDataTOList){
                        attributeStoreService.saveAttributeData(session,attributeDataTO,updatedUser,false);
                    }
                    session.getTransaction().commit();
                    logger.log(Level.INFO,ADHOTP_FACADE_IMPL_LOG + " ======ADAPTER_ATTRIBUTE_DIFF====== : ACCOUNT_ID: " + account.getId().toString() + " DIFF RESOLUTION SUCCESS");
                }
                catch(Exception e){
                    session.getTransaction().rollback();
                    logger.log(Level.INFO,ADHOTP_FACADE_IMPL_LOG+ " ======ADAPTER_ATTRIBUTE_DIFF====== : ACCOUNT_ID: "+account.getId().toString()+" DIFF RESOLUTION FAILED : "+e.getMessage());
                    logger.log(Level.ERROR,e.getMessage(),e);
                }
            }
        }
        catch (AuthException e){
            logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " adapterAttributeSync : "+ e.getMessage()+" end");
            logger.log(Level.ERROR,e.getMessage(),e);
        }
        finally {
            if(session.isOpen()){
                session.close();
            }
        }
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " adapterAttributeSync : end");
    }

    public Map<String, List<String>> getCAMUserDetails(String token){
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " getUserDetails : start");
        String accessToken = token.substring(Constant.BEARER.length()).trim();
        Map<String, Object> claims = JWTokenImpl.getALlClaimsWithoutValidationCAMAccessToken(accessToken);
        Map<String,List<String>> attributes=new HashMap<>();
        attributes.put(Constant.USER_ID, claims.get(CAM_USER_ID)!=null?getValues(claims.get(CAM_USER_ID).toString()):new ArrayList<>());
        attributes.put(Constant.MOBILE_NO, claims.get(CAM_MOBILE_NO)!=null?getValues(claims.get(CAM_MOBILE_NO).toString()): new ArrayList<>());
        attributes.put(Constant.EMAIL_ID, claims.get(CAM_EMAIL_ID)!=null?getValues(claims.get(CAM_EMAIL_ID).toString()): new ArrayList<>());
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " getUserDetails : end");
        return attributes;
    }
    public static List<String> getValues(String attributeValues) {
        String[] attributeValuesArray = attributeValues.split("\\|\\|");
        return new ArrayList<>(Arrays.asList(attributeValuesArray));
    }

    private void validateSearchAttributes(List<AttributeDataTO> searchAttributes) throws AuthException {
        List<AttributeMetadataTO> attributeMetaDataWEs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        for (AttributeDataTO attributeDataTO : searchAttributes) {
            attributeNameToUpperCase(attributeDataTO);
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
            int index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
            if (index < 0) {
                attributeMetadataTO.setAttributeName("OTHERS");
                index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
            }
            attributeMetadataTO = attributeMetaDataWEs.get(index);
            if(!attributeMetadataTO.getIsUnique()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SEARCH_ATTRIBUTE(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE()+": "+attributeMetadataTO.getAttributeName());
            }
        }

    }

    private AccountWE getAccountId(TokenRequestTO tokenRequestTO) throws AuthException, IAMException {
        String searchAttributeValue = null;
        String attributeName = null;
        List<AttributeTO> attributes = new ArrayList<>();
        for (AttributeDataTO attributeDataTO : tokenRequestTO.getSearchAttributes()) {
            attributeNameToUpperCase(attributeDataTO);
            AttributeTO attributeTO = new AttributeTO();
            attributeTO.setAttributeName(attributeDataTO.getAttributeName());
            attributeTO.setAttributeValue(attributeDataTO.getAttributeValue());
            attributes.add(attributeTO);
        }
        IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
        Token token = iamExtensionService.getToken(iamExtensionV2);
        AccountWE accountWE = iamExtensionV2.getAccountByAttributes(attributes, token);
        if(accountWE != null && accountWE.getId() != null) {
            logger.log(Level.INFO, "accountWE : " + new Gson().toJson(accountWE));
            return accountWE;
        }else {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        
        
    }

    private AccessTokenResponse processGetCAMToken(TokenRequestTO tokenRequestTO, AccountWE account, String realm) throws AuthException, IAMException {
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " processGetCAMToken : start");
        AccessTokenResponse responseToken=null;
        String kcId = "";
        UserRepresentation userRepresentation=null;
        try{
            userRepresentation = camUserFacade.getUserDetailsWithUsername(realm, account.getId().toString());
        }
        catch (AuthException e){
            logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " processGetCAMToken : user detail not found for account_id : "+account.getId().toString());
        }
        if (userRepresentation == null) {
            //To-do : check application is cam enabled or not
            kcId = onboardUserOnCam(realm,account,tokenRequestTO.getUserCredential());
            logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " validateAndOnboardUserOnCAM - onboardUserOnCam kcId : " + kcId);
        }
        else{
            kcId = userRepresentation.getId();
        }
        if (kcId != null && !kcId.isEmpty()) {
            account.setKcId(kcId);
            responseToken = resetCAMPasswordAndGetToken(realm, account, kcId, tokenRequestTO);
            logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " processGetCAMToken : responseToken - " + responseToken);
        }
        if(tokenRequestTO.getCamError() != null){
            tokenRequestTO.setCamError(null);
        }
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " processGetCAMToken : end");
        return responseToken;
    }

    private String onboardUserOnCam(String realm, AccountWE accountWE,String userCredentialReq) throws AuthException{
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " onboardUserOnCam : start");
        UserCreationRequest userCreationRequest = new UserCreationRequest();
        userCreationRequest.setUsername(accountWE.getId());
        List<CamAttribute> camAttributes = new ArrayList<>();
        for (AttributeTO attributeStore : accountWE.getAttributes()) {
            CamAttribute camAttribute = new CamAttribute();
            camAttribute.setCustomAttributeName(attributeStore.getAttributeName());
            camAttribute.setCustomAttributeValue(attributeStore.getAttributeValue());
            camAttributes.add(camAttribute);
        }
        userCreationRequest.setAttributes(camAttributes);

        //credentials ->  to support multiple credentials
        List<Credential> credentials = new ArrayList<>();
        Credential credential = new Credential();
        credential.setTemporary(false);
        credential.setType("password");
        String userCredential = accountWE.getUserCredential()!=null ? accountWE.getUserCredential() : userCredentialReq;
        credential.setValue(userCredential);
        credentials.add(credential);
        userCreationRequest.setCredentials(credentials);
        UserResponseDto userResponseDto = camUserFacade.onboardCamUser(realm,userCreationRequest);
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " onboardUserOnCam : end");
        return userResponseDto.getUserKcId();
    }

    private AccessTokenResponse resetCAMPasswordAndGetToken(String realm, AccountWE accountWE, String kcId, TokenRequestTO tokenRequestTO) throws AuthException, IAMException {
        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " resetCAMPasswordAndGetToken : start");
        String  userCredential = StringUtil.getHex(SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountWE.getId(), (accountWE.getId() + kcId)).getBytes()));
        ResetPasswordUserRequest request = new ResetPasswordUserRequest("password", userCredential, false);
        camUserFacade.resetPassword(Config.getInstance().getProperty(Constant.CAM_REALM), kcId, request);

        tokenRequestTO.setUserCredential(userCredential);
        AccessTokenResponse tokenFromCam = camServerAPI.getToken(realm, tokenRequestTO, accountWE.getId());
        //store kcId on ids
        AccountWE camAccountForKcId = new AccountWE();
        camAccountForKcId.setId(accountWE.getId());
        camAccountForKcId.setKcId(kcId);
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        Token token = iamExtensionService.getToken(iamExtension);
        try {
            iamExtension.editAccount(camAccountForKcId, accountWE.getId(), token);
        } catch (Exception e){
            logger.log(Level.ERROR,e.getMessage(),e);
            throw new AuthException(null,errorConstant.getERROR_CODE_PERMISSION_DENIED(),errorConstant.getERROR_MESSAGE_INVALID_USER_CREDENTIALS());
        }

        logger.log(Level.DEBUG, ADHOTP_FACADE_IMPL_LOG + " resetCAMPasswordAndGetToken : end");
        return tokenFromCam;
    }

    private void attributeNameToUpperCase(AttributeDataTO attribute) {
        if (attribute.getAttributeName() != null) {
            attribute.setAttributeName(attribute.getAttributeName().toUpperCase());
        }
    }

    private void attributeNameToUpperCase(TokenRequestTO tokenRequestTO) {
        if (tokenRequestTO.getSearchAttributes() != null) {
            for (AttributeDataTO attribute : tokenRequestTO.getSearchAttributes()) {
                if (attribute.getAttributeName() != null) {
                    attribute.setAttributeName(attribute.getAttributeName().toUpperCase());
                    attribute.setAttributeValue(attribute.getAttributeValue().toUpperCase());
                }
            }
        }
    }

    private void setIsUniqueForAttributeData(
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
        if (attributeMetadataTO.getIsUnique() != null) {
            attributeDataTO.setIsUnique(attributeMetadataTO.getIsUnique());
        }
    }

}
