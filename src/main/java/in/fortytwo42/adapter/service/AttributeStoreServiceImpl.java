
package in.fortytwo42.adapter.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import in.fortytwo42.enterprise.extension.enums.ApprovalStatus;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.FileUtil;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.EvidenceDaoIntf;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.EnterpriseTO;
import in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.EvidenceStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.entities.enums.AttributeType;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;

public class AttributeStoreServiceImpl implements AttributeStoreServiceIntf {

    private static final String ATTRIBUTE_STORE_SERVICE_LOG = "<<<<< AttributeStoreServiceImpl";
    private static Logger logger= LogManager.getLogger(AttributeStoreServiceImpl.class);

    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    private UserDaoIntf userDao = DaoFactory.getUserDao();
    private EvidenceDaoIntf evidenceDao = DaoFactory.getEvidenceDao();

    //TODO:Service to Service
    private EvidenceStoreServiceIntf evidenceStoreService = ServiceFactory.getEvidenceStoreService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private UserServiceIntf userService = ServiceFactory.getUserService();
    
    private Config config = Config.getInstance();
    private IAMUtil iamUtil = IAMUtil.getInstance();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private static final class InstanceHolder {
        private static final AttributeStoreServiceImpl INSTANCE = new AttributeStoreServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static AttributeStoreServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public AttributeStore saveAttributeData(Session session, AttributeDataTO attribute, Long authAttemptId, User user, Boolean isUserConsent) throws AuthException {
        logger.log(Level.INFO, "Adding attribute " + attribute.getAttributeName() + " " + authAttemptId);
        attributeNameToUpperCase(attribute);
        AttributeStore attributeStore = new AttributeStore();
        attributeStore.setAttributeName(attribute.getAttributeName());
//        String attributeValue = attribute.getOldattributeValue() != null ? attribute.getOldattributeValue() : attribute.getAttributeValue();
        String attributeValue = attribute.getAttributeValue() != null ? attribute.getAttributeValue() : attribute.getOldattributeValue();
         String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
         Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
         if(Boolean.TRUE.equals(isAttributeUpperCase)){
             attributeStore.setAttributeValue(attributeValue.toUpperCase());
         }else {
             attributeStore.setAttributeValue(attributeValue);
         }
        if (attribute.getAttributeType() != null) {
            try {
                attributeStore.setAttributeType(AttributeType.valueOf(attribute.getAttributeType()));
            }
            catch (IllegalArgumentException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_ATTRIBUTE_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_ATTRIBUTE_TYPE());
            }
        }
        attributeStore.setSignTransactionId(attribute.getSignTransactionId());
        if(authAttemptId !=null)
        {
            attributeStore.setAuthAttemptId(authAttemptId);
        }
        if(attribute.getIsDefault() !=null ) {
            attributeStore.setIsDefault(attribute.getIsDefault());
            if (attribute.getIsDefault().equals(Boolean.TRUE)) {
                int updatedRows = attributeStoreDao.updateDefaultFlagInAttribute(session, user.getId(),attribute.getAttributeName(),Boolean.FALSE);
                if (updatedRows > 0) {
                    logger.log(Level.DEBUG,"Rows with default attribute updated");
                }
            }
        }
        if(attribute.getIsRegistered() !=null) {
            attributeStore.setIsRegistered(attribute.getIsRegistered());
        }
        attributeStore.setAttributeHash(attribute.getEvidenceHash());
        attributeStore.setAttributeRelationId(attribute.getAttributeRelationId());
        attributeStore.setAttributeRelationEntity(attribute.getAttributeRelatonEntity());
        attributeStore.setUser(user);
        if(isUserConsent) {
            attributeStore.setAttributeState(AttributeState.PENDING);
        }
        else{
            attributeStore.setAttributeState(AttributeState.ACTIVE);
        }
        if((attribute.getIsUnique() != null && !attribute.getIsUnique()) || attributeStoreDao.getAttributesByattributeNameandValueWithoutType(attribute.getAttributeName(), attribute.getAttributeValue(), session).isEmpty()) {
            return attributeStoreDao.create(session, attributeStore);    
        } else {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(), attribute.getAttributeName()+ " " +errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
        }
    }
    @Override
    public boolean isAttributePresent(AttributeDataTO attribute, User user) {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " isAttributePresent: start");
        boolean isExist = false;
        attributeNameToUpperCase(attribute);
        try {
            AttributeStore attributeStore;
            String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
            Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
            if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                 attributeStore = attributeStoreDao.getAttributeByAttributeNameAndValueAndUserId(attribute.getAttributeName(), attribute.getAttributeValue(), user.getId());
            }else {
                 attributeStore = attributeStoreDao.getAttributeByAttributeNameAndValueAndUserIdIsUpper(attribute.getAttributeName(), attribute.getAttributeValue(), user.getId());
            }
            if(attributeStore!=null){
                isExist = true;
            }
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.ERROR, e);
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " isAttributePresent: end");
        }
        return isExist;
    }
    @Override
    public AttributeStore saveAttributeData(Session session, AttributeDataTO attribute, User user,  Boolean isUserConsent) throws AuthException {
        
        return saveAttributeData(session, attribute, null, user, isUserConsent);
    }

    @Override
    public AttributeDataTO getAttribute(Long authAttemptId) throws AuthException {
        AttributeStore attributeStore;
        try {
            attributeStore = attributeStoreDao.getAttributeByAuthId(authAttemptId);
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        AttributeDataTO attributeStoreTO = attributeStore.convertToTO();
        List<EvidenceStoreTO> evidenceStoreTOs = evidenceStoreService.getEvidenceByAttributeId(attributeStore.getId());
        for (EvidenceStoreTO evidenceStoreTO : evidenceStoreTOs) {
            File evidenceFile = new File(evidenceStoreTO.getFilePath());
            evidenceStoreTO.setFilePath(null);
            String evidenceData = encodeFileToBase64Binary(evidenceFile);
            evidenceStoreTO.setEvidenceData(evidenceData);
        }
        attributeStoreTO.setEvidences(evidenceStoreTOs.isEmpty() ? null : evidenceStoreTOs);
        return attributeStoreTO;
    }

    @Override
    public AttributeDataTO getAttributeWithEvidenceHash(Long authAttemptId) throws AuthException {
        AttributeStore attributeStore;
        try {
            attributeStore = attributeStoreDao.getAttributeByAuthId(authAttemptId);
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        AttributeDataTO attributeStoreTO = attributeStore.convertToTO();
        attributeStoreTO.setEvidenceHash(attributeStore.getAttributeHash());
        return attributeStoreTO;
    }

    @Override
    public List<AttributeDataTO> getAttributeList(Long authAttemptId) throws AuthException {
        List<AttributeStore> attributeStoreList;
        try {
            attributeStoreList = attributeStoreDao.getAttributeListByAuthId(authAttemptId);
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        List<AttributeDataTO> attributeDataTOs = convertToAttributeDataTO(attributeStoreList);
        return attributeDataTOs;
    }

    private List<AttributeDataTO> convertToAttributeDataTO(List<AttributeStore> attributeStoreList) {
        List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
        for (AttributeStore attributeStore : attributeStoreList) {
            attributeNameToUpperCase(attributeStore);
            List<EvidenceStoreTO> evidenceStoreTOs = evidenceStoreService.getEvidenceByAttributeId(attributeStore.getId());
            AttributeDataTO attributeStoreTO = attributeStore.convertToTO();
            attributeStoreTO.setEvidences(evidenceStoreTOs);
            attributeDataTOs.add(attributeStoreTO);
        }
        return attributeDataTOs;
    }

    @Override
    public void verifyAttribute(Session session, Long authAttemptId, String transactionId, String approvalStatus, String signTransactiId) throws AuthException {
        AttributeStore attributeStore;
        try {
            attributeStore = attributeStoreDao.getAttributeByAuthId(authAttemptId);
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        List<EvidenceStoreTO> evidenceStoreTOs = evidenceStoreService.getEvidenceByAttributeId(attributeStore.getId());
        String evidence = null;
        if (evidenceStoreTOs != null && !evidenceStoreTOs.isEmpty()) {
            String evidenceHash = null;
            for (EvidenceStoreTO evidenceTO : evidenceStoreTOs) {
                String newEvidenceHash = SHAImpl.sha256Hex(FileUtil.getBytesOfFile(evidenceTO.getFilePath()));
                if (evidenceHash == null) {
                    evidenceHash = newEvidenceHash;
                }
                else {
                    evidenceHash += newEvidenceHash;
                }
            }
            logger.log(Level.INFO, "Added Evidense HASH : " + evidenceHash);
            evidence = SHAImpl.sha256Hex(evidenceHash.getBytes());
            logger.log(Level.INFO, "calculated evidence hash " + evidence + " received evidence hash " + attributeStore.getAttributeHash());
        }
        String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
        String enterpriseId = config.getProperty(Constant.ENTERPRISE_ID);
        String enterprisePassword = config.getProperty(Constant.ENTERPRISE_PASSWORD);
        try {
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(enterpriseAccountId);
            Token token = iamUtil.authenticateV2(iamExtension, enterpriseId, AES128Impl.decryptData(enterprisePassword, KeyManagementUtil.getAESKey()));
			System.out.println("approvalStatus  >> " + approvalStatus);
            iamExtension.verifyAttribute(token, ApprovalStatus.valueOf(approvalStatus), attributeStore.getAttributeName(), attributeStore.getAttributeValue(), signTransactiId,
                    attributeStore.getSignTransactionId(), evidence, reqRefNum);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        attributeStoreDao.update(session, attributeStore);
    }

    public String getHashForEvidence(List<String> evidences) {
        if (evidences != null && !evidences.isEmpty()) {
            String evidenceHash = null;
            for (String evidence : evidences) {
                byte[] decodedEvidence = Base64.getMimeDecoder().decode(evidence);
                String newEvidenceHash = SHAImpl.sha256Hex(decodedEvidence);
                if (evidenceHash == null) {
                    evidenceHash = newEvidenceHash;
                }
                else {
                    evidenceHash += newEvidenceHash;
                }
            }
            return SHAImpl.sha256Hex(evidenceHash.getBytes());
        }
        return null;
    }

    @Override
    public List<AttributeDataTO> getAttributes(String userAccountId, String attributeName) {
        List<Object[]> attributeStores = attributeStoreDao.getAttributes(userAccountId, attributeName.toUpperCase());
        List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
        for (Object[] object : attributeStores) {
            AttributeDataTO attributeDataTO = new AttributeDataTO();
            attributeDataTO.setAttributeName(attributeName.toUpperCase());
            attributeDataTO.setAttributeValue((String) object[0]);
            attributeDataTO.setEvidenceHash((String) object[1]);
            attributeDataTOs.add(attributeDataTO);
        }
        return attributeDataTOs;
    }

    @Override
    public List<AttributeDataTO> getAttributes(String userAccountId) {
        List<Object[]> attributes = attributeStoreDao.getAttributes(userAccountId);
        List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
        for (Object[] object : attributes) {
            System.out.println("AAAAAAA : "+new Gson().toJson(object));
            AttributeDataTO attributeDataTO = new AttributeDataTO();
            attributeDataTO.setAttributeName((String) object[0]);
            attributeDataTO.setAttributeValue((String) object[1]);
            System.out.println("attributeName : "+(String) object[0]);
            System.out.println("attributeValue : "+(String) object[1]);
            System.out.println("attributeId : "+(Long) object[2]);
            List<EvidenceStoreTO> evidenceStoreTOs = evidenceStoreService.getEvidences((Long) object[2]);
            attributeDataTO.setEvidences(evidenceStoreTOs);
            attributeDataTOs.add(attributeDataTO);
        }
        return attributeDataTOs;
    }

    private static String encodeFileToBase64Binary(File file) {
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public AttributeStore saveEditAttributeData(Session session, AttributeDataTO attribute, Long authAttemptId, String userAccountId) throws AuthException {
        attributeNameToUpperCase(attribute);
        logger.log(Level.INFO, "Adding attribute " + attribute.getAttributeName() + " " + authAttemptId);
        AttributeStore attributeStore = new AttributeStore();
        attributeStore.setAttributeName(attribute.getAttributeName());
        String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
        Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
        if(Boolean.TRUE.equals(isAttributeUpperCase)){
            attributeStore.setAttributeValue(attribute.getAttributeValue().toUpperCase());
        }else {
            attributeStore.setAttributeValue(attribute.getAttributeValue());
        }
        if (attribute.getAttributeType() != null) {
            attributeStore.setAttributeType(AttributeType.valueOf(attribute.getAttributeType()));
        }
        attributeStore.setSignTransactionId(attribute.getSignTransactionId());
        attributeStore.setAuthAttemptId(authAttemptId);
        attributeStore.setAttributeHash(attribute.getEvidenceHash());
        attributeStore.setAttributeRelationId(attribute.getAttributeRelationId());
        attributeStore.setAttributeRelationEntity(attribute.getAttributeRelatonEntity());
        User user;
        try {
            user = userDao.getUserByAccountId(userAccountId);
            attributeStore.setUser(user);
        }
        catch (UserNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());

        }
        return attributeStoreDao.create(session, attributeStore);
    }

    @Override
    public List<AttributeDataTO> getUserAttributes(String userAccountId) {
        List<Object[]> attributes = attributeStoreDao.getAttributes(userAccountId);
        logger.log(Level.INFO, "attributes : " + new Gson().toJson(attributes));
        List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
        for (Object[] object : attributes) {
            AttributeDataTO attributeDataTO = new AttributeDataTO();
            attributeDataTO.setAttributeName((String) object[0]);
            attributeDataTO.setAttributeValue((String) object[1]);
            List<EvidenceStoreTO> evidenceStoreTOs = evidenceStoreService.getEvidenceWithDataByAttributeId((Long) object[2]);
            if (evidenceStoreTOs != null && !evidenceStoreTOs.isEmpty()) {
                attributeDataTO.setEvidences(evidenceStoreTOs);
            }
            attributeDataTO.setSignTransactionId((String) object[3]);
            AttributeType type = (AttributeType) object[4];
            if (type != null) {
                attributeDataTO.setAttributeType(type.name());
            }
            if(object[5] != null) {
                logger.log(Level.DEBUG, "Is default : "+(boolean)object[5]);
                attributeDataTO.setIsDefault((boolean)object[5]); 
            }
            attributeDataTOs.add(attributeDataTO);
        }
        return attributeDataTOs;
    }

    @Override
    public List<String> getUserAccountIdBySearchtext(String searchText) {

        List<String> list = attributeStoreDao.getMatchingUserAccountIds(searchText);
        return list;
    }

    @Override
    public List<AttributeDataTO> getAttributeListWithEvidence(Long authAttemptId) throws AuthException {
        List<AttributeStore> attributeStoreList;
        try {
            attributeStoreList = attributeStoreDao.getAttributeListByAuthId(authAttemptId);
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        List<AttributeDataTO> attributeDataTOs = convertToAttributeTO(attributeStoreList);
        return attributeDataTOs;
    }

    private List<AttributeDataTO> convertToAttributeTO(List<AttributeStore> attributeStoreList) {
        List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
        for (AttributeStore attributeStore : attributeStoreList) {
            attributeNameToUpperCase(attributeStore);
            List<EvidenceStoreTO> evidenceStoreTOs = evidenceStoreService.getEvidenceByAttributeId(attributeStore.getId());
            List<EvidenceStoreTO> evidences = new ArrayList<>();
            for (EvidenceStoreTO evidenceStore : evidenceStoreTOs) {
                EvidenceStoreTO evidenceStoreTO = new EvidenceStoreTO();
                File evidenceFile = new File(evidenceStore.getFilePath());
                evidenceStoreTO.setEvidenceData(FileUtil.encodeFileToBase64Binary(evidenceFile));
                evidenceStoreTO.setMediaType(evidenceStore.getMediaType());
                evidences.add(evidenceStoreTO);
            }
            AttributeDataTO attributeStoreTO = attributeStore.convertToTO();
            attributeStoreTO.setEvidences(evidences);
            attributeDataTOs.add(attributeStoreTO);
        }
        return attributeDataTOs;
    }

    @Override
    public List<AttributeDataTO> getAttributesWithEvidence(String userAccountId, String attributeName) throws AuthException {
        List<Object[]> attributeStores = getUserAttributes(userAccountId, attributeName.toUpperCase());
        List<AttributeDataTO> attributeDataTOs = new ArrayList<>();
        for (Object[] object : attributeStores) {
            AttributeDataTO attributeDataTO = new AttributeDataTO();
            attributeDataTO.setAttributeName(attributeName.toUpperCase());
            attributeDataTO.setAttributeValue((String) object[0]);
            attributeDataTO.setEvidenceHash((String) object[1]);
            List<String> attributeEvidences = new ArrayList<>();
            try {
                List<EvidenceStore> evidences = evidenceDao.getEvidenceByAttributeId((Long) object[2]);
                for (EvidenceStore evidenceStore : evidences) {
                    String evidenceData = FileUtil.getEncodedStringOfFile(evidenceStore.getFilePath());
                    attributeEvidences.add(evidenceData);
                }
                attributeDataTO.setEvidence(attributeEvidences);
            }
            catch (NotFoundException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            attributeDataTOs.add(attributeDataTO);
        }
        return attributeDataTOs;
    }

    @Override
    public PaginatedTO<ThirdPartyVerifierTO> getVerifiers(String verifierType, String attributeName, int page, int limit) throws AuthException {
        if (attributeName != null) {
            return iamExtensionService.getVerifiers(verifierType, attributeName.toUpperCase(), page, limit);
        }
        return iamExtensionService.getVerifiers(verifierType, attributeName, page, limit);
    }

    @Override
    public PaginatedTO<EnterpriseTO> getEnterprises(int page, int limit) throws AuthException {
        return iamExtensionService.getEnterprises(page, limit);
    }
    
    public List<Object[]> getUserAttributes(String userAccountId, String attributeName) throws AuthException {
        UserServiceIntf userServiceIntf = ServiceFactory.getUserService();
        User user = userServiceIntf.getActiveUser(userAccountId);
        List<Object[]> attributeStores = attributeStoreDao.getAttributes(user, attributeName.toUpperCase());
        return attributeStores;
    }
    
    public static void main(String[] args) throws AuthException {
        String pass = AES128Impl.encryptData("123456", KeyManagementUtil.getAESKey());
        System.out.println("pass : "+pass);
        String sec = AES128Impl.encryptData("nonad@iam", KeyManagementUtil.getAESKey());
        System.out.println("sec : "+sec);
        System.out.println(AES128Impl.decryptData("FtBHUU14vs3Lm0pneX49BA==", KeyManagementUtil.getAESKey()));
    }

    @Override
    public void checkAttributesPresent(AttributeDataTO attributeDataTO) throws AuthException {
        attributeNameToUpperCase(attributeDataTO);
        logger.log(Level.DEBUG, " ************checkAttributesPresent********** "+attributeStoreDao.getAttributesByattributeNameandValueWithoutType(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue()).isEmpty());
        if( !attributeStoreDao.getAttributesByattributeNameandValueWithoutType(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue()).isEmpty()) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(), attributeDataTO.getAttributeName()+ " " +errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
        }
    }

    @Override
    public AttributeStore getAttributeByAttributeData(String attributeName, String attributeValue) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeData : start");
        try {
            return attributeStoreDao.getAttributeByAttributeData(attributeName.toUpperCase(), attributeValue);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeData : end");
        }
    }

    @Override
    public AttributeStore getAttributeByAttributeData(String attributeName, String attributeValue, Session session) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeData : start");
        try {
            return attributeStoreDao.getAttributeByAttributeData(attributeName.toUpperCase(), attributeValue, session);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeData : end");
        }
    }
    
    @Override
    public AttributeStore getAttributeByAttributeData(String attributeName, String attributeValue, AttributeState state) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeData : start");
        try {
            return attributeStoreDao.getAttributeByAttributeData(attributeName.toUpperCase(), attributeValue, state);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeData : end");
        }
    }

    @Override
    public AttributeStore getAttributeByAttributeNameAndValue(String attributeName, String attributeValue) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : start");
        try {
            return attributeStoreDao.getAttributeByAttributeNameAndValue(attributeName.toUpperCase(), attributeValue);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : end");
        }
    }

    public AttributeStore getAttributeByAttributeNameAndValue(String attributeName, String attributeValue, Session session) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : start");
        try {
            return attributeStoreDao.getAttributeByAttributeNameAndValue(attributeName.toUpperCase(), attributeValue, session);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : end");
        }
    }

    @Override
    public AttributeStore getAttributeByAttributeNameAndValueAndUserId(String attributeName, String attributeValue, Long userId) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : start");
        try {
            String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
            Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
            if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                return attributeStoreDao.getAttributeByAttributeNameAndValueAndUserId(attributeName.toUpperCase(), attributeValue, userId);
            }else {
                return attributeStoreDao.getAttributeByAttributeNameAndValueAndUserIdIsUpper(attributeName.toUpperCase(), attributeValue, userId);
            }
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : end");
        }
    }

    @Override
    public AttributeStore getAttributeByAttributeNameAndValueAndUserIdWithoutStatus(String attributeName, String attributeValue, Long userId) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : start");
        try {
            return attributeStoreDao.getAttributeByAttributeNameAndValueAndUserIdWithoutStatus(attributeName.toUpperCase(), attributeValue, userId);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : end");
        }
    }


    @Override
    public AttributeStore getAttributeByAttributeNameAndValueAndUserId(String attributeName, String attributeValue, Long userId, Session session) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : start");
        try {
            String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
            Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
            if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                return attributeStoreDao.getAttributeByAttributeNameAndValueAndUserId(attributeName.toUpperCase(), attributeValue, userId, session);
            }else {
                return attributeStoreDao.getAttributeByAttributeNameAndValueAndUserIdWithCase(attributeName.toUpperCase(), attributeValue, userId, session);
            }

        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeNameAndValue : end");
        }
    }

    @Override
    public List<AttributeStore> getAttributeByUserIdAndState(Session session, AttributeState attributeState, Long userId) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByUserIdAndState : start");
        try {
            return attributeStoreDao.getAttributeByUserIdAndState(session, attributeState, userId);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByUserIdAndState : end");
        }
    }
    @Override
    public AttributeStore update(Session session, AttributeStore attributeTobeUpdate) {
        attributeNameToUpperCase(attributeTobeUpdate);
        return attributeStoreDao.update(session, attributeTobeUpdate);
    }

    //:TODO : delete attributestore

    @Override
    public void delete(Session session, AttributeStore attributeTobeUpdate) {
        attributeNameToUpperCase(attributeTobeUpdate);
        attributeStoreDao.remove(session, attributeTobeUpdate);
    }
    
    @Override
    public AttributeStore getActiveAttribute(String attributeName , String attributeValue) throws AttributeNotFoundException {
        return getActiveAttributeWithCaseCheck(attributeName.toUpperCase(), attributeValue);
    }

    private void attributeNameToUpperCase(AttributeDataTO attributeDataTO) {
        attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
    }

    private void attributeNameToUpperCase(AttributeStore attributeStore) {
        attributeStore.setAttributeName(attributeStore.getAttributeName().toUpperCase());
    }
    
    @Override
    public AttributeStore getAttributeByNameValue(String attributeName, String attributeValue) throws AuthException {
        logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeData : start");
        try {
            return getActiveAttributeWithCaseCheck(attributeName, attributeValue);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.DEBUG, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }finally {
            logger.log(Level.DEBUG, ATTRIBUTE_STORE_SERVICE_LOG + " getAttributeByAttributeData : end");
        }
    }
    private AttributeStore getActiveAttributeWithCaseCheck(String attributeName , String attributeValue) throws AttributeNotFoundException{
        String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
        Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
        if(Boolean.TRUE.equals(isAttributeUpperCase)) {
            return attributeStoreDao.getActiveAttributeWithUpperCase(attributeName.toUpperCase(), attributeValue);
        }else {
            return attributeStoreDao.getActiveAttribute(attributeName.toUpperCase(), attributeValue);
        }
    }
    @Override
    public AttributeStore getActiveAttributeWithUpperCase(String attributeName , String attributeValue) throws AttributeNotFoundException {
        String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
        Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
        if(Boolean.TRUE.equals(isAttributeUpperCase)) {
            return attributeStoreDao.getActiveAttributeWithUpperCase(attributeName.toUpperCase(), attributeValue);
        }else {
            return attributeStoreDao.getActiveAttributeWithUpperCaseISUpper(attributeName.toUpperCase(), attributeValue);
        }
    }

    @Override
    public User getUserByAttributeValueWithUpperCase(String attributeValue) throws AttributeNotFoundException {
        return getActiveAttributeWithCaseCheck(in.fortytwo42.daos.util.Constant.USER_ID, attributeValue).getUser();
    }
}
