
package in.fortytwo42.adapter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.google.gson.Gson;

import in.fortytwo42.adapter.enums.Component;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AttributeVerifierTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.daos.enums.SRAApplicationType;
import in.fortytwo42.enterprise.extension.core.ApprovalAttemptV2;
import in.fortytwo42.enterprise.extension.core.BindingInfoV2;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.ApprovalAttemptMode;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.enums.AttributeType;
import in.fortytwo42.enterprise.extension.enums.AttributeValueModel;
import in.fortytwo42.enterprise.extension.enums.CryptoEntityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.exceptions.IAMWebException;
import in.fortytwo42.enterprise.extension.tos.ApplicationTO;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.enterprise.extension.tos.ContactTO;
import in.fortytwo42.enterprise.extension.tos.EnterpriseTO;
import in.fortytwo42.enterprise.extension.tos.EnterpriseWE;
import in.fortytwo42.enterprise.extension.tos.GatewaySettingsTO;
import in.fortytwo42.enterprise.extension.tos.MessageTO;
import in.fortytwo42.enterprise.extension.tos.PaginatedTO;
import in.fortytwo42.enterprise.extension.tos.PasswordTO;
import in.fortytwo42.enterprise.extension.tos.RemoteAccessSettingsTO;
import in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO;
import in.fortytwo42.enterprise.extension.tos.VerifierTO;
import in.fortytwo42.enterprise.extension.webentities.AccountCustomStateMachineWE;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.enterprise.extension.webentities.AttemptTypeWE;
import in.fortytwo42.enterprise.extension.webentities.AttributeMetaDataWE;
import in.fortytwo42.enterprise.extension.webentities.AttributeVerifierWE;
import in.fortytwo42.enterprise.extension.webentities.ChallengeWE;
import in.fortytwo42.enterprise.extension.webentities.DeviceWE;
import in.fortytwo42.enterprise.extension.webentities.GatewaySettingsWE;
import in.fortytwo42.enterprise.extension.webentities.PasswordPolicyWE;
import in.fortytwo42.enterprise.extension.webentities.PolicyWE;
import in.fortytwo42.enterprise.extension.webentities.StateMachineWorkFlowWE;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class IamExtensionServiceImpl implements IamExtensionServiceIntf {

    private static final String IAM_EXTENSION_SERVICE_IMPL_LOG = "<<<<< IamExtensionServiceImpl";
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private Config config = Config.getInstance();
    private IAMUtil iamUtil = IAMUtil.getInstance();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(IamExtensionServiceImpl.class);


    private static final class InstanceHolder {
        private static final IamExtensionServiceImpl INSTANCE = new IamExtensionServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static IamExtensionServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public IAMExtensionV2 getIAMExtension() throws AuthException {
        try {
            String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
            return iamUtil.getIAMExtensionV2(enterpriseAccountId);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public IAMExtensionV2 getIAMExtensionWithoutCrypto() throws AuthException {
        try {
            String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
            return iamUtil.getIAMExtensionV2WithoutCrypto(enterpriseAccountId);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public Token getToken(IAMExtensionV2 iamExtension) throws IAMException {
        String id = config.getProperty(Constant.ENTERPRISE_ID);
        String password = config.getProperty(Constant.ENTERPRISE_PASSWORD);
        return iamUtil.authenticateV2(iamExtension, id, AES128Impl.decryptData(password, KeyManagementUtil.getAESKey()));
    }

    @Override
    public Token getTokenWithoutCrypto(IAMExtensionV2 iamExtension) throws IAMException {
        String id = config.getProperty(Constant.ENTERPRISE_ID);
        String password = config.getProperty(Constant.ENTERPRISE_PASSWORD);
        return iamUtil.authenticateV2WithoutCrypto(iamExtension, id, AES128Impl.decryptData(password, KeyManagementUtil.getAESKey()));
    }

    @Override
    public Token getToken(IAMExtensionV2 iamExtension, Application application) throws IAMException {
        String id = application.getApplicationId();
        String password = application.getPassword();
        System.out.println("Pass " + application.getPassword() + " Application " + id);
        return iamUtil.authenticateV2(iamExtension, id, AES128Impl.decryptData(password, KeyManagementUtil.getAESKey()));
    }

    @Override
    public AccountWE searchAccount(List<AttributeDataTO> attributes, IAMExtensionV2 iamExtension, Token token) throws AuthException {
        List<AttributeTO> atttributeList = new ArrayList<>();
        for (AttributeDataTO attribute : attributes) {
            AttributeTO attribute1 = new AttributeTO();
            attribute1.setAttributeName(attribute.getAttributeName());
            attribute1.setAttributeValue(attribute.getAttributeValue());
            atttributeList.add(attribute1);
        }
        AccountWE accountWE = null;
        try {
            accountWE = iamExtension.searchAccount(atttributeList, token);
        }
        catch (IAMException e) {
        }
        if (accountWE == null || accountWE.getId() == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND());
        }
        return accountWE;
    }

    @Override
    public boolean isPublicAttributePresent(AttributeDataTO publicAttribute, AccountWE accountWE) {
        if (accountWE.getAttributes() != null && publicAttribute != null) {
            for (AttributeTO attribute : accountWE.getAttributes()) {
                if (attribute.getAttributeName().equals(publicAttribute.getAttributeName()) &&
                    attribute.getAttributeValue().equalsIgnoreCase(publicAttribute.getAttributeValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ApprovalAttemptTO createApprovalAttemptOnIAM(String consumerAccountId, AuthenticationAttemptTO authenticationAttemptTO, Application application) throws IAMException {
        ApprovalAttemptV2.Builder attemptBuilder = new ApprovalAttemptV2.Builder();
        attemptBuilder.consumerAccountId(consumerAccountId);
        attemptBuilder.timeOut(authenticationAttemptTO.getValidtill());
        attemptBuilder.transactionDetails(authenticationAttemptTO.getTransactionDetails());
        attemptBuilder.transactionId(authenticationAttemptTO.getTransactionId());
        attemptBuilder.transactionSummary(authenticationAttemptTO.getTransactionSummary());
        attemptBuilder.approvalAttemptType(authenticationAttemptTO.getApprovalAttemptType());
        attemptBuilder.serviceName(authenticationAttemptTO.getServiceName());
        attemptBuilder.isAuthenticationRequired(authenticationAttemptTO.getAuthenticated());
        attemptBuilder.service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL);
       // attemptBuilder.serverId(serverId);
        attemptBuilder.approvalAttemptMode(ApprovalAttemptMode.APPLICATION_TO_PEER);
        attemptBuilder.lookupId1(consumerAccountId);
        attemptBuilder.lookupId2(application.getApplicationAccountId() + "|" + authenticationAttemptTO.getServiceName());
        ApprovalAttemptV2 approvalAttempt = attemptBuilder.build();

        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
        Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
        logger.log(Level.DEBUG,
                StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, Constant.CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT,
                        application.getApplicationId(), Constant.TILT, authenticationAttemptTO.getTransactionId(), Constant.TILT, authenticationAttemptTO.getApprovalAttemptType(),
                        "~generate approval attempt called on I-AM server"));
        return iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
    }

    @Override
    public ApprovalAttemptTO createApprovalAttemptOnIAM(String consumerAccountId, ConsumerBindingTO consumerBindingTO, Application application, String approvalAttemptType,
            String encryptedData, String signTransactionId) throws IAMException {
        ApprovalAttemptV2.Builder attemptBuilder = new ApprovalAttemptV2.Builder();
        attemptBuilder.consumerAccountId(consumerAccountId);
        attemptBuilder.timeOut(consumerBindingTO.getTimeOut());
        attemptBuilder.transactionDetails(consumerBindingTO.getTransactionDetails());
        attemptBuilder.transactionId(consumerBindingTO.getTransactionId());
        attemptBuilder.transactionSummary(consumerBindingTO.getTransactionSummary());
        attemptBuilder.approvalAttemptType(approvalAttemptType);
        attemptBuilder.serviceName(consumerBindingTO.getServiceName());
        attemptBuilder.isAuthenticationRequired(true);
        attemptBuilder.service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL);
      //  attemptBuilder.serverId(serverId);
        attemptBuilder.encryptedData(encryptedData);
        attemptBuilder.signTransactionId(signTransactionId);
        attemptBuilder.approvalAttemptMode(ApprovalAttemptMode.APPLICATION_TO_PEER);
        attemptBuilder.lookupId1(consumerAccountId);
        attemptBuilder.lookupId2(application.getApplicationAccountId() + "|" + consumerBindingTO.getServiceName());
        ApprovalAttemptV2 approvalAttempt = attemptBuilder.build();
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
        Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
        return iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
    }

    @Override
    public ApprovalAttemptTO createBindingApprovalAttempt(String consumerAccountId, ConsumerBindingTO consumerBindingTO, Application application, String approvalAttemptType,
            String signTransactionId, String encryptedData) throws IAMException {
        BindingInfoV2.Builder builder = new BindingInfoV2.Builder();
        builder.consumerAccountId(consumerAccountId);
        builder.timeOut(consumerBindingTO.getTimeOut());
        builder.transactionDetails(consumerBindingTO.getTransactionDetails());
        builder.transactionSummary(consumerBindingTO.getTransactionSummary());
        builder.transactionId(consumerBindingTO.getTransactionId());
        builder.serviceName(consumerBindingTO.getServiceName());
       // builder.serverId(serverId);
        builder.approvalAttemptType(approvalAttemptType);
        builder.signTransactionId(signTransactionId);
        builder.encryptedData(encryptedData);
        BindingInfoV2 consumerRegistrationInfo = builder.build();
        IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
        Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
        return iamExtension.initConsumerApplicationBinding(token, consumerRegistrationInfo);
    }

    @Override
    public void completeUserBinding(Application application, String consumerAccountId, String transactionId, String serviceName) throws IAMException {
        System.out.println("completeUserBinding..>>");
        BindingInfoV2.Builder builder = new BindingInfoV2.Builder();
        builder.consumerAccountId(consumerAccountId);
        builder.transactionId(transactionId);
        builder.serviceName(serviceName);
        BindingInfoV2 consumerRegistrationInfo = builder.build();
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
        Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
        System.out.println("completeConsumerApplicationBinding..>>");
        iamExtension.completeConsumerApplicationBinding(token, consumerRegistrationInfo, reqRefNum);
    }

    @Override
    public String getAccountId(String attributeName, String attributeValue, IAMExtensionV2 iamExtension) {
        try {
            AccountWE accountWE = iamExtension.getAccount(attributeName, attributeValue);
            logger.log(Level.INFO, "accountWE : " + new Gson().toJson(accountWE));
            return accountWE.getId();
        }
        catch (IAMException e) {
            return null;
        }
    }

    @Override
    public AccountWE createAccountIfNotExist(String attributeName, String attributeValue, IAMExtensionV2 iamExtension) throws AuthException {
        try {
            AccountWE accountWE = iamExtension.createAccountIfNotExist(attributeName, attributeValue);
            logger.log(Level.INFO, "accountWE : " + new Gson().toJson(accountWE));
            return accountWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AccountWE getAllUserAttributesNames(String attributeName, String attributeValue) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            logger.log(Level.INFO, "tokentoken : " + token.toString());
            return iamExtension.getAllUserAttributesNames(attributeName, attributeValue, token);
        }
        catch (IAMException e) {
            return null;
        }
    }

    @Override
    public AccountWE getAllAttributesForAccount(String accountId) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            return iamExtension.getAllUserAttributesNames(accountId, token);
        }
        catch (IAMException e) {
            return null;
        }
    }

    @Override
    public UserTO convertToUserTO(AccountWE accountWE) {
        UserTO userTO = new UserTO();
        userTO.setUserIdentifier(accountWE.getId());
        List<AttributeDataTO> attributes = new ArrayList<>();
        if (accountWE.getAttributes() != null) {
            for (AttributeTO attributeTO : accountWE.getAttributes()) {
                AttributeDataTO attributeDataTO = new AttributeDataTO();
                attributeDataTO.setAttributeName(attributeTO.getAttributeName());
                attributeDataTO.setAttributeType(attributeTO.getAttributeType());
                attributeDataTO.setAttributeState(attributeTO.getAttributeState());
                attributeDataTO.setEvidences(getEvidenceList(attributeTO));
                attributeDataTO.setVerifiers(convertToVerifierTO(attributeTO.getVerifiers()));
                attributes.add(attributeDataTO);
            }
            userTO.setAttributes(attributes);
        }
        return userTO;
    }

    @Override
    public List<in.fortytwo42.tos.transferobj.VerifierTO> convertToVerifierTO(List<VerifierTO> verifierTOs) {
        List<in.fortytwo42.tos.transferobj.VerifierTO> verifiers = null;
        if (verifierTOs != null) {
            verifiers = new ArrayList<>();
            for (in.fortytwo42.enterprise.extension.tos.VerifierTO verifierTO : verifierTOs) {
                in.fortytwo42.tos.transferobj.VerifierTO verifier = new in.fortytwo42.tos.transferobj.VerifierTO();
                verifier.setVerifierAccountId(verifierTO.getVerifierAccountId());
                verifier.setEnterpriseId(verifierTO.getEnterpriseId());
                verifier.setEnterpriseName(verifierTO.getEnterpriseName());
                verifier.setVerificationStatus(verifierTO.getVerificationStatus());
                verifiers.add(verifier);
            }
        }
        return verifiers;
    }

    private List<EvidenceStoreTO> getEvidenceList(AttributeTO attributeTO) {
        List<EvidenceStoreTO> evidences = null;
        if (attributeTO.getEvidences() != null) {
            evidences = new ArrayList<>();
            for (in.fortytwo42.enterprise.extension.tos.EvidenceTO evidenceTO : attributeTO.getEvidences()) {
                EvidenceStoreTO evidence = new EvidenceStoreTO();
                evidence.setMediaType(evidenceTO.getMediaType());
                evidence.setEvidenceName(evidenceTO.getEvidenceName());
                evidences.add(evidence);
            }
        }
        return evidences;
    }

    @Override
    public List<AttributeMetadataTO> getDifferentialAttributeMetaData(String accountId, String accountType, boolean isSettingsRequired) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            List<AttributeMetaDataWE> attributeMetaDataWEs = iamExtension.getAttributeMetadata(accountId, accountType);
            return getAttributeTOs(attributeMetaDataWEs, isSettingsRequired);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public List<AttributeMetadataTO> getAttributeMetaDataForAttributeType(String attributeType, String accountType, boolean isSettingsRequired) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            List<AttributeMetaDataWE> attributeMetaDataWEs = iamExtension.getAttributeMetadataForAttributeType(attributeType, accountType);
            return getAttributeTOs(attributeMetaDataWEs, isSettingsRequired);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public List<AttributeMetaDataWE> getAttributeMetaDataWEForAttributeType(String attributeType, String accountType, boolean isSettingsRequired) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            return iamExtension.getAttributeMetadataForAttributeType(attributeType, accountType);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    private List<AttributeMetadataTO> getAttributeTOs(List<AttributeMetaDataWE> attributeMetaDataWEs, boolean isSettingsRequired) {
        List<AttributeMetadataTO> attributeMetaDataTOs = new ArrayList<>();
        for (AttributeMetaDataWE attributeMetaDataWE : attributeMetaDataWEs) {
            AttributeMetadataTO attributeMetaDataTO = new AttributeMetadataTO();
            attributeMetaDataTO.setAttributeName(attributeMetaDataWE.getAttributeName());
            attributeMetaDataTO.setAttributeType(attributeMetaDataWE.getAttributeType().name());
            attributeMetaDataTO.setAttributeStoreSecurityPolicy(attributeMetaDataWE.getAttributeStoreSecurityPolicy().name());
            attributeMetaDataTO.setAttributeValueModel(attributeMetaDataWE.getAttributeValueModel().name());
            if (isSettingsRequired) {
                attributeMetaDataTO.setAttributeSettings(attributeMetaDataWE.getAttributeSettings());
            }
            if (attributeMetaDataWE.getAttributeVerifiers() != null) {
                attributeMetaDataTO.setAttributeVerifiers(getAttributeVerifierTO(attributeMetaDataWE.getAttributeVerifiers()));
            }
            attributeMetaDataTOs.add(attributeMetaDataTO);
        }
        return attributeMetaDataTOs;
    }

    @Override
    public AttributeMetaDataWE getAttributeMetadata(String attributeName) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            List<AttributeMetaDataWE> attributeMetaDataWEs = iamExtension.getAttributeMetadata(null, null);
            AttributeMetaDataWE attributeMetaDataWE = new AttributeMetaDataWE();
            attributeMetaDataWE.setAttributeName(attributeName);
            AttributeMetaDataWE attributeMetaDataWE2 = null;
            int index = attributeMetaDataWEs.indexOf(attributeMetaDataWE);
            if (index > -1) {
                attributeMetaDataWE2 = attributeMetaDataWEs.get(index);
            }
            if (attributeMetaDataWE2 == null) {
                attributeMetaDataWE.setAttributeName("OTHERS");
                attributeMetaDataWE2 = attributeMetaDataWEs.get(attributeMetaDataWEs.indexOf(attributeMetaDataWE));
            }
            return attributeMetaDataWE2;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AttributeMetadataTO createAttributeMetadata(AttributeMetadataTO attributeMetaDataTO) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            AttributeMetaDataWE attributeMetaDataWE = new AttributeMetaDataWE();
            attributeMetaDataWE.setAttributeName(attributeMetaDataTO.getAttributeName());
            attributeMetaDataWE.setAttributeType(AttributeType.valueOf(attributeMetaDataTO.getAttributeType()));
            attributeMetaDataWE.setAttributeStoreSecurityPolicy(AttributeSecurityType.valueOf(attributeMetaDataTO.getAttributeStoreSecurityPolicy()));
            attributeMetaDataWE.setAttributeValueModel(AttributeValueModel.valueOf(attributeMetaDataTO.getAttributeValueModel()));
            if ((attributeMetaDataTO.getAttributeSettings().get(Constant.MASK_PATTERN)) == null) {
                attributeMetaDataTO.getAttributeSettings().put(Constant.MASK_PATTERN, config.getProperty(Constant.MASK_PATTERN));
            }
            if(attributeMetaDataTO.getIsUnique()!=null){
                attributeMetaDataWE.setIsUnique(attributeMetaDataTO.getIsUnique());
            }
            attributeMetaDataWE.setAttributeSettings(attributeMetaDataTO.getAttributeSettings());
            attributeMetaDataWE.setPriority(attributeMetaDataTO.getPriority());
            attributeMetaDataWE.setApplicableAccountTypes(attributeMetaDataTO.getApplicableAccountTypes());
            String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
            attributeMetaDataWE.setEnterpriseAccountId(enterpriseAccountId);
            /*if (attributeMetaDataTO.getComments() != null) {
                attributeMetaDataWE.setComments(attributeMetaDataTO.getComments());
            }*/
            if (attributeMetaDataTO.getAttributeVerifiers() != null) {
                attributeMetaDataWE.setAttributeVerifiers(getAttributeVerifierWE(attributeMetaDataTO.getAttributeVerifiers(), enterpriseAccountId));
            }
            iamExtension.createAttributeMetadata(attributeMetaDataWE, token);
            attributeMetaDataTO.setStatus(Constant.SUCCESS_STATUS);
            return attributeMetaDataTO;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    private List<AttributeVerifierWE> getAttributeVerifierWE(List<AttributeVerifierTO> attributeVerifierTOs, String enterpriseAccountId) {
        List<AttributeVerifierWE> attributeVerifierWEs = new ArrayList<>();
        if (attributeVerifierTOs != null) {
            for (AttributeVerifierTO attributeVerifierTO : attributeVerifierTOs) {
                AttributeVerifierWE attributeVerifierWE = new AttributeVerifierWE();
                attributeVerifierWE.setIsDefault(attributeVerifierTO.getIsDefault());
                attributeVerifierWE.setPriority(attributeVerifierTO.getPriority());
                logger.log(Level.DEBUG, " addAttributeMetaData : start " + attributeVerifierTO.getSourceId());
                if (attributeVerifierTO.getSourceId() == null || attributeVerifierTO.getSourceId().isEmpty()) {
                    logger.log(Level.DEBUG, " addAttributeMetaData : start " + enterpriseAccountId);
                    attributeVerifierTO.setSourceId(enterpriseAccountId);
                }
                logger.log(Level.DEBUG, " addAttributeMetaData : start " + attributeVerifierTO.getSourceId());
                attributeVerifierWE.setIsActive(attributeVerifierTO.getIsActive());
                attributeVerifierWE.setSourceId(attributeVerifierTO.getSourceId());
                attributeVerifierWE.setSourceType(attributeVerifierTO.getSourceType());
                attributeVerifierWE.setVerificationType(attributeVerifierTO.getVerificationType());
                attributeVerifierWE.setVerifierType(attributeVerifierTO.getVerifierType());
                attributeVerifierWE.setVerifierId(attributeVerifierTO.getVerifierId());
                attributeVerifierWE.setVerifierName(attributeVerifierTO.getVerifierName());
                attributeVerifierWEs.add(attributeVerifierWE);
            }
            return attributeVerifierWEs;
        }
        return null;
    }

    private List<AttributeVerifierTO> getAttributeVerifierTO(List<AttributeVerifierWE> attributeVerifierWEs) {
        List<AttributeVerifierTO> attributeVerifierTOs = new ArrayList<>();
        if (attributeVerifierWEs != null) {
            for (AttributeVerifierWE attributeVerifierWE : attributeVerifierWEs) {
                AttributeVerifierTO attributeVerifierTO = new AttributeVerifierTO();
                attributeVerifierTO.setIsDefault(attributeVerifierWE.getIsDefault());
                attributeVerifierTO.setPriority(attributeVerifierWE.getPriority());
                attributeVerifierTO.setIsActive(attributeVerifierWE.getIsActive());
                attributeVerifierTO.setSourceId(attributeVerifierWE.getSourceId());
                attributeVerifierTO.setSourceType(attributeVerifierWE.getSourceType());
                attributeVerifierTO.setVerificationType(attributeVerifierWE.getVerificationType());
                attributeVerifierTO.setVerifierType(attributeVerifierWE.getVerifierType());
                attributeVerifierTO.setVerifierId(attributeVerifierWE.getVerifierId());
                attributeVerifierTO.setVerifierName(attributeVerifierWE.getVerifierName());
                attributeVerifierTOs.add(attributeVerifierTO);
            }
            return attributeVerifierTOs;
        }
        return null;
    }

    @Override
    public AttributeMetadataTO editAttributeMetadata(String attributeName, AttributeMetadataTO attributeMetaDataTO) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            AttributeMetaDataWE attributeMetaDataWE = new AttributeMetaDataWE();
            attributeMetaDataWE.setAttributeName(attributeMetaDataTO.getAttributeName());
            attributeMetaDataWE.setAttributeType(AttributeType.valueOf(attributeMetaDataTO.getAttributeType()));
            attributeMetaDataWE.setAttributeStoreSecurityPolicy(AttributeSecurityType.valueOf(attributeMetaDataTO.getAttributeStoreSecurityPolicy()));
            attributeMetaDataWE.setAttributeValueModel(AttributeValueModel.valueOf(attributeMetaDataTO.getAttributeValueModel()));
            attributeMetaDataWE.setAttributeSettings(attributeMetaDataTO.getAttributeSettings());
            attributeMetaDataWE.setPriority(attributeMetaDataTO.getPriority());
            attributeMetaDataWE.setApplicableAccountTypes(attributeMetaDataTO.getApplicableAccountTypes());
            if(attributeMetaDataTO.getIsUnique()!=null){
                attributeMetaDataWE.setIsUnique(attributeMetaDataTO.getIsUnique());
            }

            //            if (attributeMetaDataTO.getComments() != null) {
            //                attributeMetaDataWE.setComments(attributeMetaDataTO.getComments());
            //            }
            if (attributeMetaDataTO.getAttributeVerifiers() != null) {
                String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
                attributeMetaDataWE.setAttributeVerifiers(getAttributeVerifierWE(attributeMetaDataTO.getAttributeVerifiers(), enterpriseAccountId));
            }
            iamExtension.editAttributeMetadata(attributeName, attributeMetaDataWE, token);
            attributeMetaDataTO.setStatus(Constant.SUCCESS_STATUS);
            return attributeMetaDataTO;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
            //throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AttributeMetadataTO deleteAttributeMetadata(AttributeMetadataTO attributeMetadataTO) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            AttributeMetaDataWE attributeMetaDataWE = new AttributeMetaDataWE();
            attributeMetaDataWE.setAttributeName(attributeMetadataTO.getAttributeName());
            //            if (attributeMetadataTO.getComments() != null) {
            //                attributeMetaDataWE.setComments(attributeMetadataTO.getComments());
            //            }
            iamExtension.deleteAttributeMetadata(attributeMetaDataWE, token);
            attributeMetadataTO.setStatus(Constant.SUCCESS_STATUS);
            return attributeMetadataTO;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AttributeMetadataTO getAttributeMetadataForAttributeName(String attributeName) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            AttributeMetaDataWE attributeMetaDataWE = iamExtension.getAttributeMetadataForAttributeName(attributeName, token);
            if (attributeMetaDataWE == null) {
                return null;
            }
            AttributeMetadataTO attributeMetaDataTO = new AttributeMetadataTO();
            attributeMetaDataTO.setAttributeName(attributeMetaDataWE.getAttributeName());
            attributeMetaDataTO.setAttributeType(attributeMetaDataWE.getAttributeType().name());
            attributeMetaDataTO.setAttributeStoreSecurityPolicy(attributeMetaDataWE.getAttributeStoreSecurityPolicy().name());
            attributeMetaDataTO.setAttributeValueModel(attributeMetaDataWE.getAttributeValueModel().name());
            attributeMetaDataTO.setAttributeSettings(attributeMetaDataWE.getAttributeSettings());
            attributeMetaDataTO.setApplicableAccountTypes(attributeMetaDataWE.getApplicableAccountTypes());
            if(attributeMetaDataWE.getIsUnique()!=null){
                attributeMetaDataTO.setIsUnique(attributeMetaDataWE.getIsUnique());
            }
            attributeMetaDataTO.setPriority(attributeMetaDataWE.getPriority());
            if (attributeMetaDataWE.getAttributeVerifiers() != null) {
                attributeMetaDataTO.setAttributeVerifiers(getAttributeVerifierTO(attributeMetaDataWE.getAttributeVerifiers()));
            }
            return attributeMetaDataTO;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND());
            //throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AttributeMetadataTO getAttributeMetadataForAttributeNameWithoutCrypto(String attributeName) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtensionWithoutCrypto();
            Token token = getTokenWithoutCrypto(iamExtension);
            AttributeMetaDataWE attributeMetaDataWE = iamExtension.getAttributeMetadataForAttributeName(attributeName, token);
            if (attributeMetaDataWE == null) {
                return null;
            }
            AttributeMetadataTO attributeMetaDataTO = new AttributeMetadataTO();
            attributeMetaDataTO.setAttributeName(attributeMetaDataWE.getAttributeName());
            attributeMetaDataTO.setAttributeType(attributeMetaDataWE.getAttributeType().name());
            attributeMetaDataTO.setAttributeStoreSecurityPolicy(attributeMetaDataWE.getAttributeStoreSecurityPolicy().name());
            attributeMetaDataTO.setAttributeValueModel(attributeMetaDataWE.getAttributeValueModel().name());
            attributeMetaDataTO.setAttributeSettings(attributeMetaDataWE.getAttributeSettings());
            attributeMetaDataTO.setApplicableAccountTypes(attributeMetaDataWE.getApplicableAccountTypes());
            if(attributeMetaDataWE.getIsUnique()!=null){
                attributeMetaDataTO.setIsUnique(attributeMetaDataWE.getIsUnique());
            }
            attributeMetaDataTO.setPriority(attributeMetaDataWE.getPriority());
            if (attributeMetaDataWE.getAttributeVerifiers() != null) {
                attributeMetaDataTO.setAttributeVerifiers(getAttributeVerifierTO(attributeMetaDataWE.getAttributeVerifiers()));
            }
            return attributeMetaDataTO;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND());
            //throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public List<AttributeMetadataTO> getAllAttributeMetaData(String accountId, String accountType) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            List<AttributeMetaDataWE> attributeMetaDataWEs = iamExtension.getAttributeMetadata(accountId, accountType);
            return getAttributeMetaDataTOs(attributeMetaDataWEs);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<AttributeMetadataTO> getAllAttributeMetaData(int page, int limit, String searchText) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            PaginatedTO<AttributeMetaDataWE> attributeMetaDataWEs = iamExtension.getAttributeMetadata(page, limit, searchText);
            List<AttributeMetadataTO> attributeMetadataTOs = getAttributeMetaDataTOs(attributeMetaDataWEs.getList());
            Long count = attributeMetaDataWEs.getCount();
            in.fortytwo42.adapter.transferobj.PaginatedTO<AttributeMetadataTO> attributeMetaData = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
            attributeMetaData.setList(attributeMetadataTOs);
            attributeMetaData.setTotalCount(count);
            return attributeMetaData;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    private List<AttributeMetadataTO> getAttributeMetaDataTOs(List<AttributeMetaDataWE> attributeMetaDataWEs) {
        List<AttributeMetadataTO> attributeMetaDataTOs = new ArrayList<>();
        for (AttributeMetaDataWE attributeMetaDataWE : attributeMetaDataWEs) {
            AttributeMetadataTO attributeMetaDataTO = new AttributeMetadataTO();
            attributeMetaDataTO.setAttributeName(attributeMetaDataWE.getAttributeName());
            attributeMetaDataTO.setAttributeType(attributeMetaDataWE.getAttributeType().name());
            attributeMetaDataTO.setAttributeStoreSecurityPolicy(attributeMetaDataWE.getAttributeStoreSecurityPolicy().name());
            attributeMetaDataTO.setAttributeValueModel(attributeMetaDataWE.getAttributeValueModel().name());
            attributeMetaDataTO.setAttributeSettings(attributeMetaDataWE.getAttributeSettings());
            attributeMetaDataTO.setApplicableAccountTypes(attributeMetaDataWE.getApplicableAccountTypes());
            attributeMetaDataTO.setStatus(attributeMetaDataWE.getStatus());
            if(attributeMetaDataWE.getIsUnique()!=null){
               attributeMetaDataTO.setIsUnique(attributeMetaDataWE.getIsUnique());
            }
            if (attributeMetaDataWE.getAttributeVerifiers() != null) {
                attributeMetaDataTO.setAttributeVerifiers(getAttributeVerifierTO(attributeMetaDataWE.getAttributeVerifiers()));
            }
            attributeMetaDataTOs.add(attributeMetaDataTO);
        }
        return attributeMetaDataTOs;
    }

    @Override
    public void updateUserStatus(String accountId, String state) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            iamExtension.updateUserStatus(accountId, state, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, Long.valueOf(e.getErrorCode()), e.getMessage());
        }

    }

    @Override
    public void updateUserStatus(String accountId, String state, String accountAccessStatus) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            state = state.equals(UserStatus.BLOCK.name()) ? "BLOCKED" : state;
            iamExtension.updateUserStatus(accountId, state, accountAccessStatus, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, Long.valueOf(e.getErrorCode()), e.getMessage());
        }

    }
    @Override
    public void updateUserRoleOnIds(String accountId, String state,String accountType) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            state = state.equals(UserStatus.BLOCK.name()) ? "BLOCKED" : state;
            iamExtension.updateUserRole(accountId, state,  token,accountType);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, Long.valueOf(e.getErrorCode()), e.getMessage());
        }

    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<ThirdPartyVerifierTO> getVerifiers(String verifierType, String attributeName, int page, int limit) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<ThirdPartyVerifierTO> verifiers = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<ThirdPartyVerifierTO> verifiersTOs = iamExtension.getVerifiers(verifierType, attributeName, page, limit, token);
            verifiers.setTotalCount(verifiersTOs.getCount());
            verifiers.setList(verifiersTOs.getList());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        catch (IAMWebException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch(Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return verifiers;
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<EnterpriseTO> getEnterprises(int page, int limit) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        List<EnterpriseTO> enterprises = new ArrayList<>();
        in.fortytwo42.adapter.transferobj.PaginatedTO<EnterpriseTO> enterpriseTOs = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            //PaginatedTO<EnterpriseWE> paginatedEnterpriseWEs = iamExtension.getEnterprises(token, page, limit);
            List<EnterpriseWE> enterpriseWEs = iamExtension.getEnterprises(token, page, limit);
            for (EnterpriseWE enterpriseWE : enterpriseWEs) {
                EnterpriseTO enterpriseTO = new EnterpriseTO();
                enterpriseTO.setEnterpriseId(enterpriseWE.getEnterpriseView().getEnterpriseId());
                enterpriseTO.setEnterpriseName(enterpriseWE.getEnterpriseView().getEnterpriseName());
                enterpriseTO.setEnterpriseAccountId(enterpriseWE.getEnterpriseView().getEnterpriseAccountId());
                enterprises.add(enterpriseTO);
            }
            enterpriseTOs.setTotalCount(Long.valueOf(enterpriseWEs.size()));
            enterpriseTOs.setList(enterprises);
            logger.log(Level.DEBUG, "<<<<< enterprises " + new Gson().toJson(enterpriseTOs));
        }
        catch (IAMWebException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return enterpriseTOs;
    }

    @Override
    public EnterpriseWE getEnterprise() throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        EnterpriseWE enterprise = new EnterpriseWE();
        Token token;
        try {
            token = getToken(iamExtension);
            List<EnterpriseWE> enterpriseWEs = iamExtension.getEnterprises(token, 0, 0);
            enterprise = enterpriseWEs.get(0);
        }
        catch (IAMWebException | IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return enterprise;
    }

    @Override
    public String onboardApplication(String applicationId, String applicationAccountId, String password, CryptoEntityType cryptoEntityType) throws AuthException {
        System.out.println("onboardApplication..>>start..");
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        IAMExtensionV2 iamExtension = getIAMExtension();
        //Token token;
        try {
            //token = getToken(iamExtension);
            return iamExtension.registerUserOnCryptoServerV2(applicationId, applicationAccountId, password, cryptoEntityType, reqRefNum);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            System.out.println("errrporo...>>" + e.getMessage());
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void updateSRAConsumerSettings(Application application, RemoteAccessSettingsTO remoteAccessSettingsTO) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            iamExtension.updateSRAConsumerSettings(token, application.getEnterprise().getEnterpriseId(), application.getApplicationId(), application.getApplicationAccountId(),
                    SRAApplicationType.CONSUMER.name(), remoteAccessSettingsTO);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void updateSRAProviderSettings(Application application, GatewaySettingsTO gatewaySettingsTO) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            iamExtension.updateSRAProviderSettings(token, application.getEnterprise().getEnterpriseId(), application.getApplicationId(), application.getApplicationAccountId(),
                    SRAApplicationType.PROVIDER.name(), gatewaySettingsTO);
        }
        catch (IAMException e) {
            // TODO Auto-generated catch block
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public void removeSRASettings(Application application) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            iamExtension.removeSRASettings(token, application.getEnterprise().getEnterpriseId(), application.getApplicationId(), application.getApplicationAccountId());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void validatePassword(PasswordTO passwordTO) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;

        try {
            token = getToken(iamExtension);
            iamExtension.validatePassword(token, passwordTO);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void verifyPassword(PasswordTO passwordTO) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;

        try {
            token = getToken(iamExtension);
            iamExtension.verifyPassword(token, passwordTO);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public Map<String, Object> getQuestions() throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.getQuestions(token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public PasswordPolicyWE getPasswordPolicies(String userType) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.getPasswordPolicies(token, userType);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public GatewaySettingsTO createGatewaySetttings(SRAGatewaySettingTO gatewaySettingsTO) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            GatewaySettingsWE sraGatewaySettingsWE = iamExtension.createGatewaySettings(convertToTO(gatewaySettingsTO), token);
            return convertToTO(sraGatewaySettingsWE);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public GatewaySettingsTO editGatewaySetttings(SRAGatewaySettingTO gatewaySettingsTO) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            GatewaySettingsWE sraGatewaySettingsWE = iamExtension.editGatewaySettings(convertToTO(gatewaySettingsTO), token);
            return convertToTO(sraGatewaySettingsWE);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void deleteGatewaySetttings(String gatewaySettingname) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            iamExtension.deleteGatewaySetting(gatewaySettingname, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public PolicyWE onboardPolicy(PolicyWE policyWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.onboardPolicy(policyWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public ContactTO onboardContact(ContactTO contactTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " onboardContact : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            contactTO = iamExtension.onboardContact(contactTO, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " onboardContact : end");
        return contactTO;
    }

    @Override
    public ContactTO editContact(ContactTO contactTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editContact : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            contactTO = iamExtension.editContact(contactTO, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editContact : end");
        return contactTO;
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<AccountWE> getAllContacts(String accountId, String attributeValue, Integer page, Integer limit) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllContacts : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<AccountWE> contacts = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<AccountWE> response = iamExtension.getContacts(accountId, attributeValue, page, limit, token);
            contacts.setList(response.getList());
            contacts.setTotalCount(response.getCount());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllContacts : end");
        return contacts;
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<AccountWE> getAccountsWithAttributeValue(String accountId, String attributeValue, Integer page, Integer limit) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllContacts : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<AccountWE> contacts = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<AccountWE> response = iamExtension.getAccountsWithAttributeValue(accountId, attributeValue, page, limit, null, null, token);
            contacts.setList(response.getList());
            contacts.setTotalCount(response.getCount());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllContacts : end");
        return contacts;
    }

    @Override
    public PolicyWE validatePolicy(PolicyWE policyWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.validatePolicy(policyWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public PolicyWE editPolicy(PolicyWE policyWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.editPolicy(policyWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public List<PolicyWE> getAllPolicies() throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.getAllPolicies(token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    private static GatewaySettingsTO convertToTO(GatewaySettingsWE sraGatewaySettingsWE) {
        GatewaySettingsTO sraGatewaySettingTO = new GatewaySettingsTO();
        sraGatewaySettingTO.setName(sraGatewaySettingsWE.getName());
        sraGatewaySettingTO.setAddress(sraGatewaySettingsWE.getAddress());
        sraGatewaySettingTO.setPort(sraGatewaySettingsWE.getPort());
        sraGatewaySettingTO.setClientProxyPort(sraGatewaySettingsWE.getClientProxyPort());
        return sraGatewaySettingTO;
    }

    private static GatewaySettingsTO convertToTO(SRAGatewaySettingTO sraGatewaySettingsTO) {
        GatewaySettingsTO sraGatewaySettingTO = new GatewaySettingsTO();
        sraGatewaySettingTO.setName(sraGatewaySettingsTO.getName());
        sraGatewaySettingTO.setAddress(sraGatewaySettingsTO.getAddress());
        sraGatewaySettingTO.setPort(sraGatewaySettingsTO.getPort());
        sraGatewaySettingTO.setClientProxyPort(sraGatewaySettingsTO.getClientProxyPort());
        return sraGatewaySettingTO;
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<TokenWE> getTokens(Integer page, Integer limit) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokens : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<TokenWE> tokens = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<TokenWE> tokenTOs = iamExtension.getTokens(page, limit, token);
            tokens.setTotalCount(tokenTOs.getCount());
            tokens.setList(tokenTOs.getList());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokens : end");
        return tokens;
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<DeviceWE> getDevices(Integer page, Integer limit) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevices : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<DeviceWE> devices = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<DeviceWE> deviceTOs = iamExtension.getDevices(page, limit, null,token);
            devices.setTotalCount(deviceTOs.getCount());
            devices.setList(deviceTOs.getList());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevices : end");
        return devices;
    }

    @Override
    public DeviceTO editDevice(DeviceTO deviceTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editDevice : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            DeviceWE deviceRequest = new DeviceWE();
            deviceRequest.setId(deviceTO.getDeviceId());
            deviceRequest.setDeviceUDID(deviceTO.getDeviceUDID());
            deviceRequest.setDeviceState(deviceTO.getDeviceState().name());
            if (deviceTO.getDeviceName() != null) {
                deviceRequest.setDeviceName(deviceTO.getDeviceName());
            }
            iamExtension.editDevice(deviceRequest, token);
            deviceTO.setStatus(Constant.SUCCESS_STATUS);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editDevice : end");
        return deviceTO;
    }

    @Override
    public DeviceTO editDeviceBindToken(DeviceTO deviceTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editDeviceBindToken : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            DeviceWE deviceRequest = new DeviceWE();
            deviceRequest.setId(deviceTO.getDeviceId());
            deviceRequest.setDeviceUDID(deviceTO.getDeviceUDID());
            deviceRequest.setDeviceState(deviceTO.getDeviceState().name());
            if (deviceTO.getDeviceName() != null) {
                deviceRequest.setDeviceName(deviceTO.getDeviceName());
            }
            List<TokenWE> tokenWEs = new ArrayList<>();
            for (TokenTO tokenTO : deviceTO.getTokens()) {
                TokenWE tokenWE = new TokenWE();
                tokenWE.setTokenUDID(tokenTO.getTokenUDID());
                tokenWE.setType(tokenTO.getType());
                tokenWE.setState(tokenTO.getState().name());
                if (tokenTO.getTokenName() != null) {
                    tokenWE.setTokenName(tokenTO.getTokenName());
                }
                tokenWEs.add(tokenWE);
            }
            deviceRequest.setTokens(tokenWEs);
            iamExtension.editDeviceBindToken(deviceRequest, token);
            deviceTO.setStatus(Constant.SUCCESS_STATUS);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editDeviceBindToken : end");
        return deviceTO;
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<TokenWE> getDeviceTokens(String deviceId, Integer page, Integer limit) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDeviceTokens : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<TokenWE> tokens = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<TokenWE> tokenTOs = iamExtension.getDeviceTokens(deviceId, page, limit, token);
            tokens.setTotalCount(tokenTOs.getCount());
            tokens.setList(tokenTOs.getList());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDeviceTokens : end");
        return tokens;
    }

    @Override
    public PaginatedTO<AuditLogTO> getAuditLogs(String queryParams) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAuditLogs : start");
        IAMExtensionV2 iamExtension = getIAMExtension();

        try {
            Token token = getToken(iamExtension);
            PaginatedTO<AuditLogTO> auditLogs = iamExtension.getAuditLogs(queryParams, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAuditLogs : end");
            return auditLogs;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public TokenTO editToken(TokenTO tokenTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editToken : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            TokenWE tokenRequest = new TokenWE();
            tokenRequest.setId(tokenTO.getTokenId());
            tokenRequest.setTokenUDID(tokenTO.getTokenUDID());
            tokenRequest.setState(tokenTO.getState().name());
            if (tokenTO.getTokenName() != null) {
                tokenRequest.setTokenName(tokenTO.getTokenName());
            }
            iamExtension.editToken(tokenRequest, token);
            tokenTO.setStatus(Constant.SUCCESS_STATUS);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editToken : end");
        return tokenTO;
    }

    @Override
    public TokenTO editTokenRemoteWipe(TokenTO tokenTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editTokenRemoteWipe : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            TokenWE tokenRequest = new TokenWE();
            tokenRequest.setId(tokenTO.getTokenId());
            iamExtension.tokenRemoteWipe(tokenRequest, token);
            tokenTO.setStatus(Constant.SUCCESS_STATUS);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editTokenRemoteWipe : end");
        return tokenTO;
    }

    @Override
    public DeviceWE getDevice(String deviceId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevice : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            DeviceWE deviceWE = iamExtension.getDevice(deviceId, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevice : end");
            return deviceWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevice : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public TokenWE getToken(String tokenId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getToken : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            TokenWE tokenWE = iamExtension.getToken(tokenId, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getToken : end");
            return tokenWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getToken : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public Token authenticateUser(String userId, String password) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " authenticateUser : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = iamExtension.authenticateUser(userId, password);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " authenticateUser : end");
            return token;
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " authenticateUser : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public AuditLogTO getDefaultAttributes(AuditLogTO auditLog) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDefaultAttributes : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        AuditLogTO auditLogTO;
        try {
            Token token = getToken(iamExtension);
            auditLogTO = iamExtension.getDefaultAttributes(auditLog, token);
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDefaultAttributes : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDefaultAttributes : end");
        return auditLogTO;
    }

    @Override
    public DeviceWE getDeviceByUdid(String deviceUdid) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevice : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            DeviceWE deviceWE = iamExtension.getDeviceByUdid(deviceUdid, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevice : end");
            return deviceWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevice : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public TokenWE getTokenByUdid(String tokenUdid) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokenByUdid : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            TokenWE tokenWE = iamExtension.getTokenByUdid(tokenUdid, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokenByUdid : end");
            return tokenWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokenByUdid : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public Map<String, DeviceWE> getDevices(List<String> deviceIds) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevices : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            Map<String, DeviceWE> devices = iamExtension.getDevices(deviceIds, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevices : end");
            return devices;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getDevices : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public Map<String, TokenWE> getTokens(List<String> tokenIds) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokens : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            Map<String, TokenWE> tokens = iamExtension.getTokens(tokenIds, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokens : end");
            return tokens;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokens : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AccountWE editUserCredentials(AccountWE weEditAccount, String accountId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editUserCredentials : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            AccountWE accountWE = iamExtension.editUserCredentials(weEditAccount, accountId, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editUserCredentials : end");
            return accountWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editUserCredentials : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public List<String> getAccountIds(String attributeName) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            return iamExtension.getAccountIds(attributeName, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AuditLogTO verifyAuditLog(AuditLogTO auditLogTO) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            return iamExtension.verifyAuditLog(auditLogTO, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<PolicyWE> getAllPolicies(Integer page, Integer limit) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllPolicies : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<PolicyWE> policies = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<PolicyWE> response = iamExtension.getAllPolicies(page, limit, token);
            policies.setList(response.getList());
            policies.setTotalCount(response.getCount());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllPolicies : end");
        return policies;
    }

    @Override
    public AccountWE getTokensByAccountId(String accountId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokensbyaccountId : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            AccountWE accountWE = iamExtension.getTokensbyaccountId(accountId, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokensbyaccountId : end");
            return accountWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getTokensbyaccountId : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public AccountWE getDevicesByAccountId(String accountId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getdevicesbyaccountId : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            AccountWE accountWE = iamExtension.getdevicesbyaccountId(accountId, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getdevicesbyaccountId : end");
            return accountWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + "getdevicesbyaccountId : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public AuditLogTO postAuditLogs(AuditLogTO auditLogTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getdevicesbyaccountId : start");
        IAMExtensionV2 iamExtension = getIAMExtensionWithoutCrypto();
        try {
            Token token = getTokenWithoutCrypto(iamExtension);
            AuditLogTO auditLog = iamExtension.postAuditLogs(token, auditLogTO);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getdevicesbyaccountId : end");
            return auditLog;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + "getdevicesbyaccountId : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public AccountWE editAttribute(AttributeTO attributeTO, String accountId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editAttribute : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            AccountWE accountWE = iamExtension.editAttribute(attributeTO, accountId, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " editAttribute : end");
            return accountWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + "editAttribute : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public AccountWE deleteAttribute(AttributeTO attributeTO, String accountId,String cryptoDID) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + "deleteAttribute : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            AccountWE accountWE = iamExtension.deleteAttribute(attributeTO, accountId,cryptoDID, token);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " deleteAttribute : end");
            return accountWE;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + "deleteAttribute : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    public void addAttribute(String attributeName, String attributeValue, String accountId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " addAttribute : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            iamExtension.addAttribute(token, accountId, attributeName, attributeValue, null);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " addAttribute : end");
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + "addAttribute : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }

    }

    @Override
    public MessageTO sendNotification(MessageTO messageTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " sendNotification : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try{
            Token token = getToken(iamExtension);
            MessageTO notification = iamExtension.sendNotification(token, messageTO);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " sendNotification : end");
            return notification;
        }catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " sendNotification : end");
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }
    
    @Override
    public DeviceTO unbindUsersFromDevice(DeviceTO deviceTO) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " unbindUsersFromDevice : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        try {
            Token token = getToken(iamExtension);
            DeviceWE deviceRequest = new DeviceWE();
            deviceRequest.setId(deviceTO.getDeviceId());
            deviceRequest.setDeviceUDID(deviceTO.getDeviceUDID());
            deviceRequest.setDeviceState(deviceTO.getDeviceState().name());
            if (deviceTO.getDeviceName() != null) {
                deviceRequest.setDeviceName(deviceTO.getDeviceName());
            }
            if(deviceTO.getAccountIds() != null || deviceTO.getAccountIds().size() != 0) {
                deviceRequest.setAccountIds(deviceTO.getAccountIds());
            }
            iamExtension.unbindUsersFromDevice(deviceRequest,token);
            deviceTO.setStatus(Constant.SUCCESS_STATUS);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " unbindUsersFromDevice : end");
        return deviceTO;
    }

    @Override
    public AccountWE getUserDeviceState(String accountId, String deviceId) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            return iamExtension.getUserDeviceState(accountId, token, deviceId);
        }
        catch (IAMException e) {
            return null;
        }
    }

    @Override
    public AccountWE getAccount(String attributeName, String attributeValue) throws AuthException {
        try {
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            return iamExtension.getAccount(attributeName, attributeValue, token);
        }
        catch (IAMException e) {
            return null;
        }
    }

    @Override
    public ApplicationTO validateApplication(String applicationId) throws AuthException{
        try{
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            return iamExtension.validateApplication(applicationId, token);
        }
        catch(IAMException e){
            return null;
        }
    }

    @Override
    public void deleteQRTrxFromAuthAttempt() throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " deleteQRTrxFromAuthAttempt : start");
        try{
            IAMExtensionV2 iamExtension = getIAMExtension();
            Token token = getToken(iamExtension);
            iamExtension.deleteQRTrxFromAuthAttempt(token);
        }
        catch(IAMException e){
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " deleteQRTrxFromAuthAttempt : end");
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<StateMachineWorkFlowWE> getAllStateMachineWorkflows(Integer page, Integer pageSize, String searchQuery, String accountId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllStateMachineWorkflows : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<StateMachineWorkFlowWE> paginatedList = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<StateMachineWorkFlowWE> response = iamExtension.getAllStateMachineWorkflows(page, pageSize, searchQuery, accountId, token);
            paginatedList.setList(response.getList());
            paginatedList.setTotalCount(response.getCount());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllStateMachineWorkflows : end");
        return paginatedList;
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<AttemptTypeWE> getAllAttemptTypes(Integer page, Integer pageSize) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllAttemptTypes : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<AttemptTypeWE> paginatedList = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<AttemptTypeWE> response = iamExtension.getAllAttemptTypes(page, pageSize, token);
            paginatedList.setList(response.getList());
            paginatedList.setTotalCount(response.getCount());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllAttemptTypes : end");
        return paginatedList;
    }

    @Override
    public StateMachineWorkFlowWE onboardStateMachineWorkFlow(StateMachineWorkFlowWE stateMachineWorkFlowWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.onboardStateMachineWorkFlow(stateMachineWorkFlowWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public StateMachineWorkFlowWE updateStateMachineWorkFlow(StateMachineWorkFlowWE stateMachineWorkFlowWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.updateStateMachineWorkFlow(stateMachineWorkFlowWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public StateMachineWorkFlowWE validateStateMachineWorkFlow(StateMachineWorkFlowWE stateMachineWorkFlowWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.validateStateMachineWorkFlow(stateMachineWorkFlowWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }


    @Override
    public List<ChallengeWE> getAllChallengeTypes() throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllChallengeTypes : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        List<ChallengeWE> list = new ArrayList<>();
        try {
            Token token = getToken(iamExtension);
            list = iamExtension.getAllChallengeTypes(token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllChallengeTypes : end");
        return list;
    }

    @Override
    public in.fortytwo42.adapter.transferobj.PaginatedTO<AccountCustomStateMachineWE> getAllAccountCustomStateMachine(Integer page, Integer pageSize, String searchQuery, String accountId) throws AuthException {
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllStateMachineWorkflows : start");
        IAMExtensionV2 iamExtension = getIAMExtension();
        in.fortytwo42.adapter.transferobj.PaginatedTO<AccountCustomStateMachineWE> paginatedList = new in.fortytwo42.adapter.transferobj.PaginatedTO<>();
        try {
            Token token = getToken(iamExtension);
            PaginatedTO<AccountCustomStateMachineWE> response = iamExtension.getAllAccountCustomStateMachines(page, pageSize, searchQuery, accountId, token);
            paginatedList.setList(response.getList());
            paginatedList.setTotalCount(response.getCount());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
        logger.log(Level.DEBUG, IAM_EXTENSION_SERVICE_IMPL_LOG + " getAllStateMachineWorkflows : end");
        return paginatedList;
    }

    @Override
    public AccountCustomStateMachineWE onboardAccountCustomStateMachine(AccountCustomStateMachineWE accountCustomStateMachineWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.onboardAccountCustomStateMachine(accountCustomStateMachineWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AccountCustomStateMachineWE updateAccountCustomStateMachine(AccountCustomStateMachineWE accountCustomStateMachineWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.updateAccountCustomStateMachine(accountCustomStateMachineWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public AccountCustomStateMachineWE validateAccountCustomStateMachine(AccountCustomStateMachineWE accountCustomStateMachineWE) throws AuthException {
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            return iamExtension.validateAccountCustomStateMachine(accountCustomStateMachineWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public void clearCache(Component cacheComponent) throws AuthException{
        IAMExtensionV2 iamExtension = getIAMExtension();
        Token token;
        try {
            token = getToken(iamExtension);
            iamExtension.clearCache(cacheComponent.name(), token);
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }


}
