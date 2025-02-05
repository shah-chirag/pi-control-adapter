
package in.fortytwo42.adapter.facade;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import dev.samstevens.totp.code.HashingAlgorithm;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AuthenticationAttemptServiceImpl;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.QRCodeDataTO;
import in.fortytwo42.adapter.transferobj.QRCodeTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.handler.AuthAttemptHistoryHandler;
import in.fortytwo42.daos.dao.ApplicationDaoImpl;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.ApprovalStatus;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.utils.RandomString;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.tos.enums.Algorithm;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class QRCodeFacadeImpl implements QRCodeFacadeIntf {

    private static final String QR_CODE_FACADE_LOG = "<<<<< QRCodeFacadeImpl";
    private static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
    private static Logger logger= LogManager.getLogger(QRCodeFacadeImpl.class);
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private QRCodeFacadeImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final QRCodeFacadeImpl INSTANCE = new QRCodeFacadeImpl();

        private InstanceHolder() {

        }
    }

    public static QRCodeFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public QRCodeDataTO getQRCode(QRCodeDataTO qrCodeDataTO, String applicationId) throws AuthException {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " getQRCode : start");
        Application application = ServiceFactory.getApplicationService().getApplicationByApplicationId(applicationId);
        validateAttributes(qrCodeDataTO);
        QRCodeDataTO qrCodeDataRespTO = new QRCodeDataTO();
        String authCode = UUIDGenerator.getRandomNumberString();
        logger.log(Level.DEBUG,"authCode : " + authCode);
        qrCodeDataRespTO.setAuthCode(authCode);
        String key = StringUtil.generateKey(authCode);
        logger.log(Level.DEBUG,"key : " + key);
        String encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(qrCodeDataTO.getCustomeAttributes()), key);
        QRCodeTO qrCodeTO = new QRCodeTO();
        qrCodeTO.setData(encryptedData);
        Config config = Config.getInstance();
        Algorithm algorithm = application.getAlgorithm() != null ? application.getAlgorithm() : Algorithm.valueOf(config.getProperty(Constant.DEFAULT_TOTP_HASH_ALGORITHM));
        String hashingAlgorithm = HashingAlgorithm.valueOf(algorithm.name()).getHmacAlgorithm();
        qrCodeTO.setHashAlgorithm(hashingAlgorithm);
        Integer numberOfDigits = application.getNumberOfDigits() != null ? application.getNumberOfDigits() : Integer.parseInt(config.getProperty(Constant.DEFAULT_TOTP_NUMBER_OF_DIGITS));
        qrCodeTO.setNumberOfDigits(numberOfDigits);
        Long expiry = application.getTotpExpiry() != null ? application.getTotpExpiry() : Long.parseLong(config.getProperty(Constant.DEFAULT_TOTP_EXPIRY_IN_SEC));
        qrCodeTO.setTotpExpiry(expiry);
        String imageData = generateQRCode(new Gson().toJson(qrCodeTO));
        qrCodeDataRespTO.setQrCode(imageData);
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " getQRCode : end");
        return qrCodeDataRespTO;
    }

    @Override
    public QRCodeDataTO getQRCodeV4(QRCodeDataTO qrCodeDataTO) throws AuthException {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " getQRCode : start");
        validateAttributesV4(qrCodeDataTO);
        QRCodeDataTO qrCodeDataRespTO = new QRCodeDataTO();
        String authCode = UUIDGenerator.getRandomNumberString();
        System.out.println("authCode : " + authCode);
        qrCodeDataRespTO.setAuthCode(authCode);
        String key = StringUtil.generateKey(authCode);
        System.out.println("key : " + key);
        String encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(qrCodeDataTO.getCustomeAttributes()), key);
        String imageData = generateQRCode(encryptedData);
        qrCodeDataRespTO.setQrCode(imageData);
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " getQRCode : end");
        return qrCodeDataRespTO;
    }

//    @Override
//    public QRCodeDataTO generateGenericQRCode(String applicationId, QRCodeDataTO qrCodeDataHeaderTO) throws AuthException, ApplicationNotFoundException {
//        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " generateGenericQRCode : start");
//        QRCodeDataTO qrCodeDataRespTO = new QRCodeDataTO();
//        String randomString = RandomString.nextString(20);
//        String key = SHAImpl.hashData256(randomString);
//        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " key : " + key);
//        qrCodeDataRespTO.setApplicationId(applicationId);
//        qrCodeDataRespTO.setTransactionId(qrCodeDataHeaderTO.getTransactionId());
//        Long expiryTime = qrCodeDataHeaderTO.getExpiryTime() != null ? System.currentTimeMillis() + (qrCodeDataHeaderTO.getExpiryTime() * 1000) : (System.currentTimeMillis() + (ApplicationDaoImpl.getInstance().getApplicationByApplicationId(applicationId).getTransactionTimeout() * 1000));
//        qrCodeDataRespTO.setExpiryTime(expiryTime);
//        String salt = RandomString.nextString(8);
//        String encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(qrCodeDataRespTO), key);
//        String decryptedData = AES128Impl.decryptDataWithMD5(encryptedData, key);
//        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " decryptedData : " + decryptedData);
//        if (qrCodeDataHeaderTO.getPrefix() == null && qrCodeDataHeaderTO.getPrefix().isEmpty()) {
//            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA, "No prefix provided");
//        }
//        key = qrCodeDataHeaderTO.getPrefix()+salt+key;
//        encryptedData = key+","+encryptedData;
//        String imageData = QRCodeFacadeImpl.getInstance().generateQRCode(encryptedData);
//        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " imageData : " + imageData);
//        qrCodeDataRespTO.setQrCode(imageData);
//        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " generateGenericQRCode : end");
//        return qrCodeDataRespTO;
//    }

    @Override
    public QRCodeDataTO generateGenericQRCode(String applicationId, QRCodeDataTO qrCodeDataHeaderTO) throws Exception {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " generateGenericQRCode : start");
        QRCodeDataTO qrCodeDataRespTO = new QRCodeDataTO();
        Boolean isFipsEnabled= Boolean.valueOf((Config.getInstance().getProperty(Constant.IS_FIPS_ENABLED)));
        KeyGenerator keyGenerator;
        if(isFipsEnabled) {
             keyGenerator = KeyGenerator.getInstance("AES","BCFIPS");
        }
        else {
            keyGenerator = KeyGenerator.getInstance("AES");
        }
        if(keyGenerator==null){
            throw new AuthException(new NoSuchAlgorithmException(),errorConstant.getERROR_CODE_INVALID_DATA(),errorConstant.getERROR_MESSAGE_INVALID_VALUE());
        }
        keyGenerator.init(AES_KEY_SIZE);
        // Generate Key
        SecretKey key = keyGenerator.generateKey();
        byte[] IV = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(IV);
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " key : " + key.toString());
        qrCodeDataRespTO.setApplicationId(applicationId);
        qrCodeDataRespTO.setTransactionId(qrCodeDataHeaderTO.getTransactionId());
        Long expiryTime = qrCodeDataHeaderTO.getExpiryTime() != null ? System.currentTimeMillis() + (qrCodeDataHeaderTO.getExpiryTime() * 1000) : (System.currentTimeMillis() + (ApplicationDaoImpl.getInstance().getApplicationByApplicationId(applicationId).getTransactionTimeout() * 1000));
        qrCodeDataRespTO.setExpiryTime(expiryTime);
        String encryptedData = Base64.getEncoder().encodeToString(encrypt(new Gson().toJson(qrCodeDataRespTO).getBytes(), key, IV));
        String decryptedData = decrypt(Base64.getDecoder().decode(encryptedData), key, IV);
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " decryptedData : " + decryptedData);
        if (qrCodeDataHeaderTO.getPrefix() == null && qrCodeDataHeaderTO.getPrefix().isEmpty()) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), "No prefix provided");
        }
        String encodedIV = Base64.getEncoder().encodeToString(IV);
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        String finalkey = qrCodeDataHeaderTO.getPrefix()+encodedIV+encodedKey;
        encryptedData = finalkey+","+encryptedData;
        String imageData = generateQRCode(encryptedData);
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " imageData : " + imageData);
        qrCodeDataRespTO.setQrCode(imageData);
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " generateGenericQRCode : end");
        return qrCodeDataRespTO;
    }

    //AES_GCM Encrypt
    private byte[] encrypt(byte[] plaintext, SecretKey key, byte[] IV) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
        // Perform Encryption
        byte[] cipherText = cipher.doFinal(plaintext);
        return cipherText;
    }

    private String decrypt(byte[] cipherText, SecretKey key, byte[] IV) throws Exception  {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        // Perform Decryption
        byte[] decryptedText = cipher.doFinal(cipherText);
        return new String(decryptedText);
    }

    @Override
    public String generateQRCode(String encryptedData) throws AuthException {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " generateQRCode : start");
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(encryptedData, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "png", bos);
            String image = Base64.getEncoder().encodeToString(bos.toByteArray());
            return image;
        }
        catch (WriterException e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        }finally {
            logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " generateQRCode : end");
        }
    }

    public AccountWE validateAttributes(QRCodeDataTO qrCodeDataTO) throws AuthException {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " validateAttributes : start");
        try {
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            List<AttributeDataTO> attributes = new ArrayList<>();
            AttributeDataTO attributeDataTO = new AttributeDataTO();
            attributeDataTO.setAttributeName(qrCodeDataTO.getSearchAttribute().getAttributeName().toUpperCase());
            attributeDataTO.setAttributeValue(applySecurityPolicy(qrCodeDataTO.getSearchAttribute()));
            attributes.add(attributeDataTO);
            logger.log(Level.DEBUG, "<<<<< attributes : "+new Gson().toJson(attributes));
            AccountWE accountWE = iamExtensionService.searchAccount(attributes, iamExtension, token);
            boolean isAttributePresent = false;
            if (accountWE.getId() != null && !accountWE.getId().isEmpty()) {
                for (AttributeTO attributeTO : accountWE.getAttributes()) {
                    for (in.fortytwo42.adapter.transferobj.AttributeTO attributeData : qrCodeDataTO.getCustomeAttributes()) {
                        String attributeValue = applySecurityPolicy(attributeData);
                        if (attributeTO.getAttributeName().equals(attributeData.getAttributeName().toUpperCase()) && attributeTO.getAttributeValue().equals(attributeValue) && !attributeTO.getStatus().equals("DELETE")) {
                            isAttributePresent = true;
                        }
                    }
                }
            }
            if (!isAttributePresent) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
            }
            return accountWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        }
        finally {
            logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " validateAttributes : end");
        }
    }

    public AccountWE validateAttributesV4(QRCodeDataTO qrCodeDataTO) throws AuthException {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " validateAttributes : start");
        try {
            ValidationUtilV3.validateSearchAttributes(qrCodeDataTO.getSearchAttributes());
            IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            List<AttributeTO> attributes = new ArrayList<>();
            for (AttributeDataTO attributeList : qrCodeDataTO.getSearchAttributes()) {
                AttributeTO attributeTO = new AttributeTO();
                attributeTO.setAttributeName(attributeList.getAttributeName());
                attributeTO.setAttributeValue(attributeList.getAttributeValue());
                attributes.add(attributeTO);
            }
            logger.log(Level.DEBUG, "<<<<< attributes : " + new Gson().toJson(attributes));
            AccountWE accountWE = iamExtension.getAccountByAttributes(attributes, token);
            boolean isAttributePresent = false;
            if (accountWE.getId() != null && !accountWE.getId().isEmpty()) {
                for (AttributeTO attributeTO : accountWE.getAttributes()) {
                    for (in.fortytwo42.adapter.transferobj.AttributeTO attributeData : qrCodeDataTO.getCustomeAttributes()) {
                        String attributeValue = applySecurityPolicy(attributeData);
                        if (attributeTO.getAttributeName().equals(attributeData.getAttributeName()) && attributeTO.getAttributeValue().equals(attributeValue) && !attributeTO.getStatus().equals("DELETE")) {
                            isAttributePresent = true;
                        }
                    }
                }
            }
            if (!isAttributePresent) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            return accountWE;
        } catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        } finally {
            logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " validateAttributes : end");
        }
    }

    private String applySecurityPolicy(in.fortytwo42.adapter.transferobj.AttributeTO attributeTO) throws AuthException {
        List<AttributeMetadataTO> attributeMetaDataTOs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeTO.getAttributeName());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName("OTHERS");
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String securityType = attributeMetadataTO.getAttributeStoreSecurityPolicy();
        AttributeSecurityType attributeSecurityType = AttributeSecurityType.valueOf(securityType);
        String attributeValue = attributeTO.getAttributeValue();
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

    @Override
    public String createGenericApprovalAttempt(String applicationId, QRCodeDataTO qrCodeDataHeaderTO) throws AuthException {
        try {
            Application application = ApplicationDaoImpl.getInstance().getApplicationByApplicationId(applicationId);
            AuthenticationAttemptTO authenticationAttemptTO = new AuthenticationAttemptTO();
            String transactionId = (qrCodeDataHeaderTO.getTransactionId() != null && !qrCodeDataHeaderTO.getTransactionId().isEmpty()) ? qrCodeDataHeaderTO.getTransactionId() : RandomString.nextString(20);
            authenticationAttemptTO.setTransactionId(transactionId);
            qrCodeDataHeaderTO.setTransactionId(transactionId);
            String transactionDetails = (qrCodeDataHeaderTO.getTransactionDetails() != null && !qrCodeDataHeaderTO.getTransactionDetails().isEmpty()) ? qrCodeDataHeaderTO.getTransactionDetails() : "QR web login - Get generic QR code";
            authenticationAttemptTO.setTransactionDetails(transactionDetails);
            String transactionSummary = (qrCodeDataHeaderTO.getTransactionSummary() != null && !qrCodeDataHeaderTO.getTransactionSummary().isEmpty()) ? qrCodeDataHeaderTO.getTransactionSummary() : "QR web login";
            authenticationAttemptTO.setTransactionSummary(transactionSummary);
            authenticationAttemptTO.setApprovalAttemptType("QR_LOGIN");
            authenticationAttemptTO.setSenderAccountId(application.getApplicationAccountId());
            Long expiryTime = qrCodeDataHeaderTO.getExpiryTime() != null ? qrCodeDataHeaderTO.getExpiryTime() : application.getTransactionTimeout();
            authenticationAttemptTO.setValidtill(expiryTime.intValue());
            authenticationAttemptTO.setApprovalStatus(ApprovalStatus.PENDING.name());
            authenticationAttemptTO.setServiceName("APPROVAL");
            Boolean authentication = qrCodeDataHeaderTO.getPinCheckRequired() != null ? qrCodeDataHeaderTO.getPinCheckRequired() : application.getAuthenticationRequired();
            authenticationAttemptTO.setAuthenticated(authentication);
            String errorMessage = ValidationUtilV3.isValidForCreateApproval(authenticationAttemptTO);
            if (errorMessage == null) {
                generateGenericQRApprovalAttempt(authenticationAttemptTO, application);
            }
            return errorMessage;
        } catch (ApplicationNotFoundException e) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        } catch (Exception e) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        }
    }

    @Override
    public void generateGenericQRApprovalAttempt(AuthenticationAttemptTO authenticationAttemptTO, Application application) throws AuthException {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " generateGenericQRApprovalAttempt : start");
        try {
            ApprovalAttemptTO approvalAttemptTO = iamExtensionService.createApprovalAttemptOnIAM(null, authenticationAttemptTO, application);
            if (approvalAttemptTO != null) {
                logger.log(Level.DEBUG,
                        StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, Constant.CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT,
                                application.getApplicationId(), Constant.TILT, authenticationAttemptTO.getTransactionId(), Constant.TILT, authenticationAttemptTO.getApprovalAttemptType(),
                                "~approval attempt generation on I-AM server complete"));
                SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
                Session session = sessionFactoryUtil.getSession();
                try {

                    AuthenticationAttempt authenticationAttempt = AuthenticationAttemptServiceImpl.getInstance().createAuthenticationAttempt(session, approvalAttemptTO, application);
                    AuthAttemptHistoryHandler.getInstance().logAuthAttemptHistoryData(authenticationAttempt);
                    if(authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.QR)) {
                        authenticationAttemptTO.setApprovalAttemptId(approvalAttemptTO.getId());
                    }
                    authenticationAttemptTO.setApprovalStatus(ApprovalStatus.PENDING.name());
                    authenticationAttemptTO.setSignTransactionId(approvalAttemptTO.getSignTransactionId());
                    sessionFactoryUtil.closeSession(session);
                }
                catch (Exception e) {
                    session.getTransaction().rollback();
                    throw e;
                }
                finally {
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            }
            else {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
            }
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        } finally {
            logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " generateGenericQRApprovalAttempt : end");
        }
    }

    /*@Override
    public AuthenticationAttemptTO getUpdatedApprovalAttempt(String applicationId, String transactionId) throws AuthException {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " getUpdatedApprovalAttempt : start");
        SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
        Session session = sessionFactoryUtil.getSession();
        try {
            Application application = ApplicationDaoImpl.getInstance().getApplicationByApplicationId(applicationId);
            IAMExtensionV2 iamExtension = IAMUtil.getInstance().getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = IAMUtil.getInstance().authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, "Async Get call~", Constant.TILT, applicationId, Constant.TILT, transactionId, "~call IAM Get"));
            ApprovalAttemptTO approvalAttemptResponse = iamExtension.getAppprovalAttemptTO(token, transactionId);
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, "Async Get call~", Constant.TILT, applicationId, Constant.TILT, transactionId,
                            "~call IAM Get Done"));
            logger.log(Level.DEBUG, "approvalAttemptResponse : "+new Gson().toJson(approvalAttemptResponse));
            if (approvalAttemptResponse.getApprovalAttemptType().equals("QR_LOGIN")) {
                if (approvalAttemptResponse.getConsumerAccountId() != null) {
                    AuthenticationAttemptHistory authenticationAttemptHistory = AuthenticationAttemptHistoryDaoImpl.getInstance().getAuthAttemptByApplicationIdAndTransactionId(application.getApplicationAccountId(), transactionId);
                    authenticationAttemptHistory.setReceiverAccountId(approvalAttemptResponse.getConsumerAccountId());
                    AuthenticationAttemptTO authenticationAttemptTO = convertToAdapterApprovalAttemptTO(convertAuthAttemptHistory(authenticationAttemptHistory));
                    logger.log(Level.DEBUG, "%%%%% authenticationAttemptTO : "+new Gson().toJson(authenticationAttemptTO));
                    AuthenticationAttemptHistoryDaoImpl.getInstance().update(session, authenticationAttemptHistory);
                    sessionFactoryUtil.closeSession(session);
                    return authenticationAttemptTO;
                } else {
                    return AuthAttemptFacadeImpl.getInstance().getApprovalAttempt(applicationId, transactionId);
                }
            }
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_APPROVAL_ATTEMPT_TYPE_INVALID, errorConstant.getERROR_MESSAGE_APPROVAL_ATTEMPT_TYPE_INVALID);
        }
        catch (TransactionNotFoundException e) {
            e.printStackTrace();
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw new UndeclaredThrowableException(e);
        }
        catch (IAMException e) {
            e.printStackTrace();
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        }catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.ERROR, e);
            throw new UndeclaredThrowableException(e);
        }
        finally {
            if(session.isOpen()){
                session.close();
            }
            logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " getUpdatedApprovalAttempt : end");
        }
    }*/

    /*private AuthenticationAttempt convertAuthAttemptHistory(AuthenticationAttemptHistory authenticationAttemptHistory) {
        AuthenticationAttempt authenticationAttempt = new AuthenticationAttempt();

        if (authenticationAttemptHistory.getTransactionId() != null) {
            authenticationAttempt.setTransactionId(authenticationAttemptHistory.getTransactionId());
        }
        if (authenticationAttemptHistory.getTransactionSummary() != null) {
            authenticationAttempt.setTransactionSummary(authenticationAttemptHistory.getTransactionSummary());
        }
        if (authenticationAttemptHistory.getTransactionDetails() != null) {
            authenticationAttempt.setTransactionDetails(authenticationAttemptHistory.getTransactionDetails());
        }
        if (authenticationAttemptHistory.getAttemptType() != null) {
            authenticationAttempt.setAttemptType(authenticationAttemptHistory.getAttemptType());
        }
        if (authenticationAttemptHistory.getAttemptStatus() != null) {
            authenticationAttempt.setAttemptStatus(authenticationAttemptHistory.getAttemptStatus());
        }
        if (authenticationAttemptHistory.getSignTransactionId() != null) {
            authenticationAttempt.setSignTransactionId(authenticationAttemptHistory.getSignTransactionId());
        }
        if (authenticationAttemptHistory.getIsPinCheckRequired() != null) {
            authenticationAttempt.setIsPinCheckRequired(authenticationAttemptHistory.getIsPinCheckRequired());
        }
        if (authenticationAttemptHistory.getServiceName() != null) {
            Service service = new Service();
            service.setServiceName(authenticationAttemptHistory.getServiceName());
            authenticationAttempt.setService(service);
        }
        if (authenticationAttemptHistory.getTimeout() != null) {
            authenticationAttempt.setTimeout(authenticationAttemptHistory.getTimeout());
        }
        if(authenticationAttemptHistory.getSenderAccountId() != null) {
            authenticationAttempt.setSenderAccountId(authenticationAttemptHistory.getSenderAccountId());
        }
        if (authenticationAttemptHistory.getReceiverAccountId() != null) {
            authenticationAttempt.setReceiverAccountId(authenticationAttemptHistory.getReceiverAccountId());
        }
        return authenticationAttempt;
    }

    private AuthenticationAttemptTO convertToAdapterApprovalAttemptTO(AuthenticationAttempt authenticationAttempt) {
        AuthenticationAttemptTO approvalAttemptTO = new AuthenticationAttemptTO();
        approvalAttemptTO.setTransactionId(authenticationAttempt.getTransactionId());
        approvalAttemptTO.setTransactionSummary(authenticationAttempt.getTransactionSummary());
        approvalAttemptTO.setTransactionDetails(authenticationAttempt.getTransactionDetails());
        approvalAttemptTO.setApprovalAttemptType(authenticationAttempt.getAttemptType());
        approvalAttemptTO.setApprovalStatus(authenticationAttempt.getAttemptStatus());
        approvalAttemptTO.setSignTransactionId(authenticationAttempt.getSignTransactionId());
        approvalAttemptTO.setAuthenticated(authenticationAttempt.getIsPinCheckRequired());
        approvalAttemptTO.setServiceName(authenticationAttempt.getService().getServiceName());
        if (authenticationAttempt.getTimeout() != null) {
            approvalAttemptTO.setValidtill((int) (long) authenticationAttempt.getTimeout());
        }
        if(authenticationAttempt.getSenderAccountId() != null) {
            approvalAttemptTO.setSenderAccountId(authenticationAttempt.getSenderAccountId());
        }
        if(authenticationAttempt.getReceiverAccountId() != null) {
            approvalAttemptTO.setReceiverAccountId(authenticationAttempt.getReceiverAccountId());
        }
        return approvalAttemptTO;
    }*/

    public static void main(String[] args) {

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES","BCFIPS");
            keyGenerator.init(256);
            SecretKey key = keyGenerator.generateKey();
            byte[] IV = new byte[12];
            SecureRandom random = new SecureRandom();
            random.nextBytes(IV);
            String plainText = "{\"applicationId\":\"15000000010001\",\"transactionId\":\"C34k1pSNEzKVxxRSFsxb\",\"expiryTime\":1685516698918}";
            // Perform Encryption
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, IV);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            String encodedCipherText = Base64.getEncoder().encodeToString(cipherText);
            String encodedIV = Base64.getEncoder().encodeToString(IV);
            String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
            String prefix = "ABCDE";
            String finalkey = prefix+encodedIV+encodedKey;
            encodedCipherText = finalkey+","+encodedCipherText;
            BitMatrix matrix = new MultiFormatWriter().encode(encodedCipherText, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "png", bos);
            String image = Base64.getEncoder().encodeToString(bos.toByteArray());
            // Perform Decryption
            String qrData = "ABCDEOwz+BZOhR3SxjTP2k2iv9StUNwiY1cjaiRnYn8IKd6f6TpROA3YY5dXVA7Q=,tuuFg5KAzKHC7wL0lzn8lCH12ECOVGyzkBi2X0kKJxqzoiM7oAFw7juW7nXn6khIzPUzfQ2Gw1CRpZwVrekG4a9HRLcD/gxan6YRHIb1JSp+BzY85eYVip17vnJiYpA6Gk6IRJPUu7V19k6TEIiNgXhQv1w=";
            String[] data = qrData.split(",");
            String stuffedKey = data[0];
            String encodedEncryptedData = data[1];
            byte[] encryptedData = Base64.getDecoder().decode(encodedEncryptedData);
            String encodedQrKey = stuffedKey.substring(21);
            String encodedQrIV = stuffedKey.substring(5, 21);
            byte[] qrKey = Base64.getDecoder().decode(encodedQrKey);
            byte[] qrIV = Base64.getDecoder().decode(encodedQrIV);
            SecretKeySpec qrSecretKey = new SecretKeySpec(qrKey, 0, qrKey.length, "AES");
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            keySpec = new SecretKeySpec(qrSecretKey.getEncoded(), "AES");
            gcmParameterSpec = new GCMParameterSpec(16 * 8, qrIV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] decryptedText = cipher.doFinal(encryptedData);
            String finalDecryptedText = new String(decryptedText);
            System.out.println(finalDecryptedText);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        /*QRCodeDataTO qrCodeDataTO = new QRCodeDataTO();
        in.fortytwo42.adapter.transferobj.AttributeTO searchAttribute = new in.fortytwo42.adapter.transferobj.AttributeTO();
        searchAttribute.setAttributeName("USER_ID");
        searchAttribute.setAttributeValue("8805713942");
        qrCodeDataTO.setSearchAttribute(searchAttribute);
        List<in.fortytwo42.adapter.transferobj.AttributeTO> customeAttributes = new ArrayList<>();
        customeAttributes.add(searchAttribute);
        qrCodeDataTO.setCustomeAttributes(customeAttributes);
        try {
            qrCodeDataTO = QRCodeFacadeImpl.getInstance().getQRCode(qrCodeDataTO);
            System.out.println("resp : " + new Gson().toJson(qrCodeDataTO));
            byte[] decoded = Base64.getDecoder().decode(qrCodeDataTO.getQrCode());
            String path = "C:\\Users\\FT42\\Desktop\\QR\\qr_Code.png";
            System.out.println("path : "+path);
            FileUtil.saveImageToPath(decoded, path);
        }
        catch (AuthException e) {
            e.printStackTrace();
        }*/

//        String encryptedData = "iwZ+RlsZ85uW/adzhP/lPoijlVBxXxXlo84rchWi/Um+S7xiahys+x3QUI5uBIxFgjgARwfkecyl\r\n"
//                + "K3EgH3IGBJbfvBzQkwLfwdF4dHdnUmmnkdpxzTtEd3hPUXxDoQd/wP1/VPi4jvDBGbMN6n6UTDBj\r\n"
//                + "mxj8RouLcTWeOiIjwl3Ow4HH1ZoOd1bQ5FuFZRW5FmwDwtZeNjBpmh3D98Fd7+86vTBkivgs4bjr\r\n"
//                + "BnUAHlpfa91CA6s62SgL6HeCsGSjv2yZsckaCJBeViIJA+7tMtf/1x1EYzSY/Uj4V0poylIPESV8\r\n"
//                + "ex9HhhTr4y3ikfw7t1AbkaXsAOJuZeu72gIA2lcvV28bcoFXcOo9XmV/DJtXXCUsSIfuBWvZ3Cjg\r\n"
//                + "s1mt3Icptj2KEc3qKLi94doBCKxiS8xzS399ZlrqyTGAZ1dbcuPOVzIt/prm8EzCcoKpojr8zZG6\r\n"
//                + "/5hA3HC6GxmpVkJiNoAPsfVuJ9N6zJ8LgQLIYMToyN1Nkp7INdy8OBSwCxSQlX9cV9TPlDQ8kwmU\r\n"
//                + "UyBidNQwyiLobuVnOd+XBuhXyVy3S8GhJLJ0qY1CXHzcc8fIA0/rQXjrDrv4PEBoeCzbPuoPG2F7\r\n"
//                + "Zh408GhfyTl/6TXDs6tbdZPQg0YKVzmvzIYrg4JlIMgeags63DZhK6ymR0Q0QBrx/Bp2z5n1Yk1B\r\n"
//                + "EiXgbZgVbDl2gT8kBV1rPZt7sKUI9geOK6IeniqIyIUBRg+f/JCrXAET5M+MZdbq4IoqL9L85goy\r\n"
//                + "WDf5WVkXlh8Ax2W6lMcwODdhbhidOkt/WdaJrepZE3cPs/r4UQy28UuP84kwSSkhiJXtDy2TrsaL\r\n"
//                + "AA+eHeZwfgjBXwOcLw==";
//        /*byte[] s = Base64.getDecoder().decode(encryptedData);
//        encryptedData = new String(s, StandardCharsets.UTF_8);*/
//        String key = "z$C7w9JBppBhl90pfll20j5OK6jXnr5u";
//        String decryptedData = AES128Impl.decryptDataWithMD5(encryptedData, key);
//        System.out.println("decryptedData : "+decryptedData);
        
        /*String imagedataEncoded = "iVBORw0KGgoAAAANSUhEUgAAAPoAAAD6AQAAAACgl2eQAAACOklEQVR4Xu2YTW6DUAyEjVi8JUfgJnAxJJC4GNyEI7BkgXBn/JIImirbjCq8oMF8lbA1/nmYf7bJfnt+2Q1ku4FsN5DtPwEGq9NhvVtTjYtP1U5XLwUk973eCh89HdVeu68dL2LAZPDMq9UIYPTNcGuFJOAAtmItF2tXVWCDFNxaH0gpAtQDUh1us3z7JpgvA7myCLwuf5Xed4GwRL2mOdfYy3QAa6gHQ3uCHnaLUK6pFgAgALRPVpbPzvKPRlrxkRIAAfiBKBBAuQSAX70WMLsvW2NoTyz6EMW1/AWArUXRE8BIss54eSpXB4CndHgGcwYAL6w9h6kAQK/QA1PdoFthDYm+JQZM8axybkghjx3xnKMQAIxZxgX1hM60OPWgCHDNNI5MlL+z0BQBBIAssz0hHojCLuUvAbA9cQOJO3YrpPo4dXsFACNpqNHe2ahCtPgV5aUEhDvyjWk5eugh8YESgD/Y2rGwW4x5KGOAPM67nACAZ9HtMS0hBWMUDCr/qwpAvXJQQhQof49DUPiVAO5tC0LpsYGAgrekcnstoOCBIkTbVAAOiNfPelAAKFCedB+HtBDtddlTANijnN9hnDXGJZ6mBYQ9OicOGbHGcVOWAvjK9fPdO87NLlNKQIrPBXEIQr45N3v2ADGA32GMnwW5y/nGYC6plgGih8ZW7DHmL6pWAeLrQSwfiT3qPdXfBqgH+OKQEcpAN7g0UgGAL825jkFEPfB0mZWhBHywG8h2A9luINsNZJt+ALpv+MUDhc2UAAAAAElFTkSuQmCC";
        byte[] decoded = Base64.getDecoder().decode(imagedataEncoded);
        String path = "C:\\Users\\FT42\\Desktop\\QR\\qr_Code2.png";
        System.out.println("path : "+path);
        FileUtil.saveImageToPath(decoded, path);*/
    }

    @Override
    public QRCodeDataTO fetchQRStatus(QRCodeDataTO qrCodeDataTO) throws AuthException {
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " fetchQRStatus : start");
        AccountWE account = validateAttributes(qrCodeDataTO);
        String status = Constant.PENDING;
        List<in.fortytwo42.adapter.transferobj.AttributeTO> customeAttributes = qrCodeDataTO.getCustomeAttributes();
        List<AttributeTO> accountAttributes = account.getAttributes();
        List<AttributeMetadataTO> attributeMetaDataWEs = ServiceFactory.getAttributeMasterService().getAllAttributeMetaData();
        for (in.fortytwo42.adapter.transferobj.AttributeTO attributeTO : customeAttributes) {
            boolean attributeMatched = false;
            attributeTO = getAttributeFromAttributeData(attributeTO, attributeMetaDataWEs, false);
            for (AttributeTO attribute : accountAttributes) {
                if (attribute.getAttributeName().equals(attributeTO.getAttributeName()) && attribute.getAttributeValue().equals(attributeTO.getAttributeValue())) {
                    if (attribute.getProfileName() != null) {
                        status = Constant.SUCCESS_STATUS;
                        qrCodeDataTO.setProfileName(attribute.getProfileName());
                    }
                    attributeMatched = true;
                    break;
                }
            }
            if (attributeMatched) {
                break;
            }
        }
        qrCodeDataTO.setStatus(status);
        logger.log(Level.DEBUG, QR_CODE_FACADE_LOG + " fetchQRStatus : end");
        return qrCodeDataTO;
    }

    private in.fortytwo42.adapter.transferobj.AttributeTO getAttributeFromAttributeData(in.fortytwo42.adapter.transferobj.AttributeTO attributeDataTO, List<AttributeMetadataTO> attributeMetaDataTOs, boolean isEncrypted)
            throws AuthException {
        attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName("OTHERS");
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String securityType = attributeMetadataTO.getAttributeStoreSecurityPolicy();
        String maskPattern = (String) attributeMetadataTO.getAttributeSettings().get(Constant.MASK_PATTERN);
        in.fortytwo42.adapter.transferobj.AttributeTO attribute = new in.fortytwo42.adapter.transferobj.AttributeTO();
        attribute.setAttributeName(attributeDataTO.getAttributeName());
        attribute.setIsDefault(attributeDataTO.getIsDefault());
        String decryptedAttributeValue = attributeDataTO.getAttributeValue();
        if (isEncrypted) {
            try {
                decryptedAttributeValue = CryptoJS.decryptData(Config.getInstance().getProperty(Constant.AD_ENCRYPTION_KEY), attributeDataTO.getAttributeValue());
            }
            catch (Exception e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_PASSWORD());
            }
        }
        if (maskPattern != null) {
            //attribute.setMaskAttributeValue(decryptedAttributeValue.replaceAll(config.getProperty(Constant.MASK_PATTERN), config.getProperty(Constant.MASK_CHARACTER)));
        }
        attribute.setAttributeValue(applySecurityPolicy(decryptedAttributeValue, AttributeSecurityType.valueOf(securityType)));
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
    /*
    public static void main(String[] args) {
        List<AttributeTO> attributes = new ArrayList<>();
        String attributeName = (args.length > 0 &&  args[0] !=null) ? args[0]:"EMAIL_ID";
        String attributeValue = (args.length > 1 && args[1] != null) ? args[1] :"test42.test@gmail.com";
        AttributeTO attributeTO = new AttributeTO();
        attributeTO.setAttributeName(attributeName);
        attributeTO.setAttributeName(attributeValue);
        attributes.add(attributeTO);

        QRCodeDataTO qrCodeDataRespTO = new QRCodeDataTO();
        String authCode = UUIDGenerator.getRandomNumberString();
        System.out.println("authCode : " + authCode);
        qrCodeDataRespTO.setAuthCode(authCode);
        String key = StringUtil.generateKey(authCode);
        System.out.println("key : " + key);
        String encryptedData = AES128Impl.encryptDataWithMD5(new Gson().toJson(attributes), key);
        QRCodeTO qrCodeTO = new QRCodeTO();
        qrCodeTO.setData(encryptedData);
        qrCodeTO.setHashAlgorithm("SHA256");
        qrCodeTO.setNumberOfDigits(6);
        qrCodeTO.setTotpExpiry(30l);
        String data = new Gson().toJson(qrCodeTO);
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "png", bos);
            String image = Base64.getEncoder().encodeToString(bos.toByteArray());

            System.out.println("image : "+image);
            qrCodeDataRespTO.setQrCode(image);
            System.out.println("qrCodeDataRespTO : "+new Gson().toJson(qrCodeDataRespTO));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
