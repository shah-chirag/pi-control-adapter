
package in.fortytwo42.adapter.util;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.fortytwo42.adapter.enums.TransactionApprovalStatus;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.jar.MongoConnectionManagerIam;
import in.fortytwo42.adapter.jar.entities.FcmNotificationDetails;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.ServiceProcessorIntf;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.AttributeVerifierTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.BulkEditUserTO;
import in.fortytwo42.adapter.transferobj.CacheTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.ConsumerTO;
import in.fortytwo42.adapter.transferobj.CryptoPinTO;
import in.fortytwo42.adapter.transferobj.CryptoTokenTO;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.EvidenceRequestTO;
import in.fortytwo42.adapter.transferobj.PolicyTO;
import in.fortytwo42.adapter.transferobj.PolicyWeTO;
import in.fortytwo42.adapter.transferobj.QRCodeDataTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.adapter.transferobj.TunnelingApplicationTO;
import in.fortytwo42.adapter.transferobj.TwoFactorVerificationTO;
import in.fortytwo42.adapter.transferobj.UserAuthenticationTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.transferobj.UserIciciStatusTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.transferobj.UserStatusTO;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.enums.ResetPinUserUnblockStatus;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.tos.enums.Algorithm;
import in.fortytwo42.tos.enums.AttributeAction;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.EnterpriseTO;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;
import in.fortytwo42.tos.transferobj.FalloutTO;
import in.fortytwo42.tos.transferobj.HotpTO;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;
import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import in.fortytwo42.tos.transferobj.MapperTO;
import in.fortytwo42.tos.transferobj.RemoteAccessSettingTO;
import in.fortytwo42.tos.transferobj.RunningHashTo;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;
import in.fortytwo42.tos.transferobj.UserTO;
import in.fortytwo42.tos.transferobj.TransactionReportRequestTO;
import in.fortytwo42.tos.transferobj.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ValidationUtilV3 {


    private static final int LABEL_LENGTH = 10;
    private static final String ONBOARD_USER = "ONBOARD_USER";
    private static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    private static final String OTHER = "OTHER";

    private static ServiceProcessorIntf serviceProcessor = ServiceFactory.getServiceProcessor();
    private static ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();
    private static AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    public static boolean isValid(String value, int maxAllowedLength) {
        return value != null && !value.trim().isEmpty()
                && value.length() <= maxAllowedLength
                && Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), value);
    }

    public static boolean isValid(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isEmailValid(String email) {
        return isValid(email) && Pattern.matches(Config.getInstance().getProperty(Constant.EMAIL_PATTERN), email);
    }

    public static boolean isMobileValid(String mobile) {
        return isValid(mobile) && Pattern.matches(Config.getInstance().getProperty(Constant.MOBILE_PATTERN), mobile);
    }

    public static boolean isConsumerIdValid(String value) {
        return value != null && !value.trim().isEmpty() && value.length() <= 50 && Pattern.matches(Config.getInstance().getProperty(Constant.MOBILE_PATTERN), value);
    }

    public static boolean isAttributeValueValid(String attributeName, String attributeValue) throws AuthException {
        Logger logger= LogManager.getLogger(ValidationUtilV3.class);
        AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
        List<AttributeMetadataTO> attributeMetaDataTOs = attributeMasterService.getAllAttributeMetaData();
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        if (attributeName == null || attributeName.isEmpty()) {
            return false;
        }
        attributeMetadataTO.setAttributeName(attributeName.toUpperCase());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            logger.log(Level.ERROR,attributeName + " : "+attributeValue);
            logger.log(Level.ERROR,attributeMetaDataTOs.toString());
            logger.log(Level.ERROR,attributeMetadataTO.toString());
            return false;
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String pattern = (String) attributeMetadataTO.getAttributeSettings().get(Constant.VALIDATION_REGEX);
        if (attributeValue == null || attributeValue.isEmpty()) {
            return false;
        }
        if (pattern == null) {
            return true;
        }
        logger.log(Level.DEBUG,"Pattern "+pattern + " attributeValue "+ attributeValue);

        return Pattern.matches(pattern, attributeValue);
    }

    public static boolean isValidIfExist(String value, int maxAllowedLength) {
        if (value != null && !value.trim().isEmpty()) {
            return value.length() <= maxAllowedLength && Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), value);
        }
        return true;
    }

    public static boolean isDataValid(String value) {
        return value != null && !value.trim().isEmpty() && Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), value);
    }

    public static String isConsumerValidForInitiateBinding(ConsumerBindingTO consumerBindingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(consumerBindingTO.getServiceName(), 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (consumerBindingTO.getTransactionId() != null && (!isValid(consumerBindingTO.getTransactionId(), 20))) {
            errorMessage.append(Constant.RESOURCE_TRANSACTION_ID).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getTransactionDetails(), 255)) {
            errorMessage.append(Constant.TRANSACTION_DETAILS).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getTransactionSummary(), 40)) {
            errorMessage.append(Constant.TRANSACTION_SUMMARY).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getCustomAttribute().getAttributeName())) {
            errorMessage.append(Constant.CUSTOME_ATTRIBUTES).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getCustomAttribute().getAttributeValue())) {
            errorMessage.append(Constant.CUSTOME_ATTRIBUTES).append(Constant._COMMA);
        }
        if (!nullValidationOfSearchAttribute(consumerBindingTO.getSearchAttributes())) {
            errorMessage.append(Constant.INVALID_SEARCH_FIELDS).append(Constant._COMMA);
        }
        try {
            if (consumerBindingTO.getTwoFactorStatus() != null && TwoFactorStatus.valueOf(consumerBindingTO.getTwoFactorStatus()) == null) {
                errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        } else if (consumerBindingTO.getTimeOut() != null && consumerBindingTO.getTimeOut() > Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT))) {
            return Constant.INVALID_TIMEOUT + Config.getInstance().getProperty(Constant.MAX_TIMEOUT);
        }
        return null;

    }

    public static String isConsumerValidForUnbinding(ConsumerBindingTO consumerBindingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(consumerBindingTO.getServiceName(), 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (consumerBindingTO.getCustomAttribute() != null && !isValid(consumerBindingTO.getCustomAttribute().getAttributeValue())) {
            errorMessage.append(Constant.CUSTOME_ATTRIBUTES).append(Constant._COMMA);
        }
        if (consumerBindingTO.getCustomAttribute() != null && !isValid(consumerBindingTO.getCustomAttribute().getAttributeName())) {
            errorMessage.append(Constant.CUSTOME_ATTRIBUTES).append(Constant._COMMA);
        }
        if (!nullValidationOfSearchAttribute(consumerBindingTO.getSearchAttributes())) {
            errorMessage.append(Constant.INVALID_SEARCH_FIELDS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValid(String attributeName, String attributeValue, String serviceName, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(serviceName, 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (!isValid(applicationLabel, 6)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(attributeValue)) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (!isValid(attributeName, 64)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForEnablingServices(ConsumerTO consumerTO, String applicationLabel) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(consumerTO.getAttributeName(), 64)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (!isAttributeValueValid(consumerTO.getAttributeName(), consumerTO.getAttributeValue())) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForUpdate(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        boolean isUserStatusPresent = false;
        boolean isTwoFactorStatusPresent = false;
        try {
            isUserStatusPresent = isValid(user.getUserStatus()) && UserStatus.valueOf(user.getUserStatus()) != null;
        } catch (IllegalArgumentException e) {
            isUserStatusPresent = true;
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            isTwoFactorStatusPresent = isValid(user.getTwoFactorStatus()) && TwoFactorStatus.valueOf(user.getTwoFactorStatus()) != null;
        } catch (IllegalArgumentException e) {
            isTwoFactorStatusPresent = true;
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (!isUserStatusPresent && !isTwoFactorStatusPresent) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA).append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        /*if (!isValid(user.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (user.getComments() != null && !user.getComments().isEmpty()) {
            if (!isValidComment(user.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForUpdateRole(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        boolean isUserStatusPresent = false;
        boolean isTwoFactorStatusPresent = false;
        try {
            isUserStatusPresent = isValid(user.getUserStatus()) && UserStatus.valueOf(user.getUserStatus()) != null;
        } catch (IllegalArgumentException e) {
            isUserStatusPresent = true;
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            isTwoFactorStatusPresent = isValid(user.getTwoFactorStatus()) && TwoFactorStatus.valueOf(user.getTwoFactorStatus()) != null;
        } catch (IllegalArgumentException e) {
            isTwoFactorStatusPresent = true;
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (!isUserStatusPresent && !isTwoFactorStatusPresent) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA).append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        if (user.getComments() != null && !user.getComments().isEmpty()) {
            if (!isValidComment(user.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (user.getAccountType().equalsIgnoreCase(String.valueOf(AccountType.USER))) {
            errorMessage.append(Constant.ACCOUNT_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForUpdateUserTimestamp(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        if (user.getComments() != null && !user.getComments().isEmpty()) {
            if (!isValidComment(user.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForGenerateOtp(HotpTO hOtp) {
        StringBuilder errorMessage = new StringBuilder();
        if (!nullValidationOfSearchAttribute(hOtp.getSearchAttributes())) {
            errorMessage.append(Constant.INVALID_SEARCH_FIELDS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForSendADFSOtp(AdHotpTO adHOtp) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        for(AttributeDataTO attributeData:adHOtp.getSearchAttributes()){
            if(!isAttributeNameValid(attributeData.getAttributeName())){
                errorMessage.append(Constant.ATTRIBUTE_NAME).append(" : ").append(attributeData.getAttributeName());
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isAdminValidForTokenValidation(UserAuthenticationTO user) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(user.getUsername())) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isValid(user.getToken())) {
            errorMessage.append(Constant.TOKEN).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForApproval(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(user.getApprovalStatus()) || TransactionApprovalStatus.valueOf(user.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (user.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(user.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValid(String attributeName, String attributeValue) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isConsumerIdValid(attributeName)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (!isValid(attributeValue)) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForEditUserApplicationRel(UserApplicationRelTO userApplicationRelTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(userApplicationRelTO.getApplicationId(), 15)) {
            errorMessage.append(Constant.APPLICATION_ID).append(Constant._COMMA);
        }
        try {
            if (userApplicationRelTO.getTwoFactorStatus() == null || TwoFactorStatus.valueOf(userApplicationRelTO.getTwoFactorStatus()) == null) {
                errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        try {
            if (userApplicationRelTO.getBindingStatus() == null || BindingStatus.valueOf(userApplicationRelTO.getBindingStatus()) == null) {
                errorMessage.append(Constant.BINDING_STATUS).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.BINDING_STATUS).append(Constant._COMMA);
        }
        if (userApplicationRelTO.getComments() != null && !userApplicationRelTO.getComments().isEmpty()) {
            if (!isValidComment(userApplicationRelTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        /*if (!isValid(userApplicationRelTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForApproveUserApplicationRel(UserApplicationRelTO userApplicationRelTO) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(userApplicationRelTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(userApplicationRelTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            if (userApplicationRelTO.getComments() != null && !userApplicationRelTO.getComments().isEmpty()) {
                if (!isValidComment(userApplicationRelTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }
            /*else if (userApplicationRelTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(userApplicationRelTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isAttributeValid(String attributeName, String attributeValue) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(attributeValue)) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (!isValid(attributeName, 64)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isAttributeDataValid(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        for (AttributeDataTO attributeDataTO : user.getAttributes()) {
            if (!isValid(attributeDataTO.getAttributeValue())) {
                errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
            }
            if (!isValid(attributeDataTO.getAttributeName(), 64)) {
                errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
            }
            if (!AttributeAction.UPDATE.equals(attributeDataTO.getAttributeAction())) {
                errorMessage.append("Attribute Action,");
            }
            if (attributeDataTO.getIsDefault() == null) {
                errorMessage.append("Is Default,");
            }
            if (errorMessage.length() > 0) {
                return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
            }
        }
        return null;
    }

    public static String isAttributeValidForFetchUserDetails(String attributeName, String attributeValue) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(attributeName, 64)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (!isValid(attributeValue)) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        /*else {
            List<AttributeMetaDataWE> attributeMetaDataWEs = ServiceFactory.getIamExtensionService().getAttributeMetaDataWEForAttributeType(Constant.PUBLIC, Constant.DEFAULT, false);
            AttributeMetaDataWE attributeMetaDataWE = new AttributeMetaDataWE();
            attributeMetaDataWE.setAttributeName(attributeName);
            List<String> publicAttribute = new ArrayList<>();
            if (!attributeMetaDataWEs.contains(attributeMetaDataWE)) {
                for (AttributeMetaDataWE attributeMetaData : attributeMetaDataWEs) {
                    publicAttribute.add(attributeMetaData.getAttributeName());
                }
                return "Expected Values for Attribute Name are : " + String.join(", ", publicAttribute);
            }
        }*/
        return null;
    }

    public static String isValidForCreateApproval(AuthenticationAttemptTO authenticationAttemptTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(authenticationAttemptTO.getServiceName(), 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (!isValid(authenticationAttemptTO.getTransactionId(), 20)) {
            errorMessage.append(Constant.RESOURCE_TRANSACTION_ID).append(Constant._COMMA);
        }
        if (!isValid(authenticationAttemptTO.getTransactionDetails(), 255)) {
            errorMessage.append(Constant.TRANSACTION_DETAILS).append(Constant._COMMA);
        }
        if (!isValid(authenticationAttemptTO.getTransactionSummary(), 40)) {
            errorMessage.append(Constant.TRANSACTION_SUMMARY).append(Constant._COMMA);
        }
        if (!isValid(authenticationAttemptTO.getApprovalAttemptType(), 50)) {
            errorMessage.append(Constant.APPROVAL_ATTEMPT_TYPE).append(Constant._COMMA);
        }
        if (!nullValidationOfSearchAttribute(authenticationAttemptTO.getSearchAttributes())) {
            errorMessage.append(Constant.INVALID_SEARCH_FIELDS).append(Constant._COMMA);
        } else if (!authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.AUTHENTICATION) && !authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.INFORMATION)
                && !authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.NORMAL)
                && !authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.SIGN)
                && !authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.ENCRYPTION)
                && !authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.REGULATORY)
                && !authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.QR)
                && !authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.QR_LOGIN)
                && !authenticationAttemptTO.getApprovalAttemptType().equals(IAMConstants.EMV)) {
            errorMessage.append(Constant.APPROVAL_ATTEMPT_TYPE).append(Constant._COMMA);
        }

        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        } else if (authenticationAttemptTO.getValidtill() != null) {
            if (authenticationAttemptTO.getValidtill() <= 0) {
                return Constant.INVALID_TRANSACTION_VALIDITY;
            }
            if (authenticationAttemptTO.getValidtill() > Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT))) {
                return Constant.INVALID_VALIDITY + Config.getInstance().getProperty(Constant.MAX_TIMEOUT);
            }
        }
        return null;
    }

    public static String isUserValidForAuthentication(UserAuthenticationTO userAuthenticationTO, String applicationLabel) {
        // TODO Auto-generated method stub
        return null;
    }

    //validate search attribute name
    public static boolean nullValidationOfSearchAttribute(List<AttributeDataTO> searchAttributes) {
        if (searchAttributes == null) {
            return false;
        } else {
            for (AttributeDataTO searchAttribute : searchAttributes) {
                if (!isValid(searchAttribute.getAttributeName())) {
                    return false;
                }
                if (!isValid(searchAttribute.getAttributeValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String isUserValidForCrypto2FA(TwoFactorVerificationTO twoFactorVerification, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(twoFactorVerification.getCryptoToken(), 10)) {
            errorMessage.append(Constant.CRYPTO_TOKEN).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForCryptoVerification(CryptoTokenTO cryptoTokenTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (cryptoTokenTO.getSearchAttributes() != null && !cryptoTokenTO.getSearchAttributes().isEmpty()) {
            for (AttributeTO attributeTO : cryptoTokenTO.getSearchAttributes()) {
                if (!isValid(attributeTO.getAttributeName())) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                }
                if (!isAttributeNameValid(attributeTO.getAttributeName())) {
                    errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                }
                if (!isValid(attributeTO.getAttributeValue())) {
                    errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
                }
            }
        } else {
            errorMessage.append(Constant.SEARCH_ATTRIBUTE).append(Constant._COMMA);
        }
        if (!isValid(cryptoTokenTO.getCryptoToken(), 10)) {
            errorMessage.append(Constant.CRYPTO_TOKEN).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForCryptoGeneration(CryptoTokenTO cryptoTokenTO, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        /*if (!isValid(cryptoTokenTO.getAttributeName())) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (!isValid(cryptoTokenTO.getAttributeValue())) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForGetADUser(String attributeName, String attributeValue, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(attributeName)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (!isValid(attributeValue)) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForUserBinding(UserBindingTO userBindingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (userBindingTO.getApplication() == null) {
            errorMessage.append(Constant.APPLICATION).append(Constant._COMMA);
        } else {
            if (!isValid(userBindingTO.getApplication().getApplicationId(), 15)) {
                errorMessage.append(Constant.RESOURCE_APPLICATION_ID).append(Constant._COMMA);
            }
            if (userBindingTO.getApplication().getServices() == null || userBindingTO.getApplication().getServices().isEmpty()) {
                errorMessage.append(Constant.SERVICES).append(Constant._COMMA);
            }
        }
        /*if (!isValid(userBindingTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (userBindingTO.getComments() != null && !userBindingTO.getComments().isEmpty()) {
            if (!isValidComment(userBindingTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isUserValidForApproval(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        /*if (!isValid(user.getUsername(), 64)) {
        	errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(user.getMobile())) {
        	errorMessage.append(Constant.MOBILE).append(Constant._COMMA);
        }*/
        try {
            if (!isValid(user.getApprovalStatus()) || TransactionApprovalStatus.valueOf(user.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (user.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(user.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isUserValidForUpdate(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        /*if (!isValid(user.getUsername(), 64)) {
        	errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(user.getMobile())) {
        	errorMessage.append(Constant.MOBILE).append(Constant._COMMA);
        }*/
        boolean isUserStatusPresent = false;
        boolean isTwoFactorStatusPresent = false;
        try {
            isUserStatusPresent = isValid(user.getUserStatus()) && UserStatus.valueOf(user.getUserStatus()) != null;
        } catch (IllegalArgumentException e) {
            isUserStatusPresent = true;
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            isTwoFactorStatusPresent = isValid(user.getTwoFactorStatus()) && TwoFactorStatus.valueOf(user.getTwoFactorStatus()) != null;
        } catch (IllegalArgumentException e) {
            isTwoFactorStatusPresent = true;
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (!isUserStatusPresent && !isTwoFactorStatusPresent) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA).append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        /*if (!isValid(user.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForBulkEdit(BulkEditUserTO bulkEditUserTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (bulkEditUserTO.getUserList() == null || bulkEditUserTO.getUserList().isEmpty()) {
            errorMessage.append(Constant.USER_LIST).append(Constant._COMMA);
        }
        boolean is2FAValid = bulkEditUserTO.getTwoFactorStatus() != null;
        boolean isUserStatusValid = bulkEditUserTO.getUserStatus() != null;
        if (!is2FAValid && !isUserStatusValid) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA).append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            if (is2FAValid && TwoFactorStatus.valueOf(bulkEditUserTO.getTwoFactorStatus()) == null) {
                errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        try {
            if (isUserStatusValid && UserStatus.valueOf(bulkEditUserTO.getUserStatus()) == null) {
                errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        /*if (!isValid(bulkEditUserTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForBulkApproval(BulkEditUserTO bulkEditUserTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (bulkEditUserTO.getUserList() == null || bulkEditUserTO.getUserList().isEmpty()) {
            errorMessage.append(Constant.USER_LIST).append(Constant._COMMA);
        }
        try {
            if (!isValid(bulkEditUserTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(bulkEditUserTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (bulkEditUserTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(bulkEditUserTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }

        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForUpdateAllUser(BulkEditUserTO bulkEditUserTO) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(bulkEditUserTO.getTwoFactorStatus()) || TwoFactorStatus.valueOf(bulkEditUserTO.getTwoFactorStatus()) == null) {
                errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidConsumerForApproval(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        /*	if (!isValid(user.getUsername(), 64)) {
        		errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        	}
        	if (!isConsumerIdValid(user.getMobile())) {
        		errorMessage.append(Constant.MOBILE).append(Constant._COMMA);
        	}*/
        try {
            if (!isValid(user.getApprovalStatus()) || TransactionApprovalStatus.valueOf(user.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (user.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(user.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidConsumerForUpdate(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        /*if (!isValid(user.getUsername(), 64)) {
        	errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(user.getMobile())) {
        	errorMessage.append(Constant.MOBILE).append(Constant._COMMA);
        }*/
        boolean isUserStatusPresent = false;
        boolean isTwoFactorStatusPresent = false;
        try {
            isUserStatusPresent = isValid(user.getUserStatus()) && UserStatus.valueOf(user.getUserStatus()) != null;
        } catch (IllegalArgumentException e) {
            isUserStatusPresent = true;
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            isTwoFactorStatusPresent = isValid(user.getTwoFactorStatus()) && TwoFactorStatus.valueOf(user.getTwoFactorStatus()) != null;
        } catch (IllegalArgumentException e) {
            isTwoFactorStatusPresent = true;
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (!isUserStatusPresent && !isTwoFactorStatusPresent) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA).append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        /*if (!isValid(user.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isUserValidForBulkApproval(BulkEditUserTO bulkEditUserTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (bulkEditUserTO.getUserList() == null || bulkEditUserTO.getUserList().isEmpty()) {
            errorMessage.append(Constant.USER_LIST).append(Constant._COMMA);
        }
        try {
            if (!isValid(bulkEditUserTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(bulkEditUserTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            if (bulkEditUserTO.getComments() != null && !bulkEditUserTO.getComments().isEmpty()) {
                if (!isValidComment(bulkEditUserTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }
            /*else if (bulkEditUserTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(bulkEditUserTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }

        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isUserValidForBulkEdit(BulkEditUserTO bulkEditUserTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (bulkEditUserTO.getUserList() == null || bulkEditUserTO.getUserList().isEmpty()) {
            errorMessage.append(Constant.USER_LIST).append(Constant._COMMA);
        }
        boolean is2FAValid = bulkEditUserTO.getTwoFactorStatus() != null;
        boolean isUserStatusValid = bulkEditUserTO.getUserStatus() != null;
        if (!is2FAValid && !isUserStatusValid) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA).append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            if (is2FAValid && TwoFactorStatus.valueOf(bulkEditUserTO.getTwoFactorStatus()) == null) {
                errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        try {
            if (isUserStatusValid && UserStatus.valueOf(bulkEditUserTO.getUserStatus()) == null) {
                errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        /*if (!isValid(bulkEditUserTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidApplicationId(String applicationId) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationId, 64)) {
            errorMessage.append(Constant.APPLICATION_ID).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidTransactionAndIdentifier(String transactionId, String identifier) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(transactionId, 64)) {
            errorMessage.append(Constant.TRANSACTION_ID).append(Constant._COMMA);
        }
        if (!isValid(transactionId, 64)) {
            errorMessage.append(Constant.IDENTIFIER).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForApprove(AttributeTO verifyAttributeTO) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(verifyAttributeTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(verifyAttributeTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            if (verifyAttributeTO.getComments() != null && !verifyAttributeTO.getComments().isEmpty()) {
                if (!isValidComment(verifyAttributeTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        /*if(!isValid(verifyAttributeTO.getComments(),255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForUpdate(AttributeTO verifyAttributeTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(verifyAttributeTO.getAttributeState(), 20)
                || !(verifyAttributeTO.getAttributeState().equals(Constant.VERIFICATION_SUCCESS) || verifyAttributeTO.getAttributeState().equals(Constant.VERIFICATION_FAILED))) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        /*if (!isValid(verifyAttributeTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (verifyAttributeTO.getComments() != null && !verifyAttributeTO.getComments().isEmpty()) {
            if (!isValidComment(verifyAttributeTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForApproveAddAttribute(AttributeTO addAttributeTO) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(addAttributeTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(addAttributeTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            if (addAttributeTO.getComments() != null && !addAttributeTO.getComments().isEmpty()) {
                if (!isValidComment(addAttributeTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }
            /*else if (addAttributeTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(addAttributeTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForAddAttribute(AttributeTO addAttributeTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(addAttributeTO.getAttributeName(), 255)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (addAttributeTO.getAttributeName().equalsIgnoreCase(Constant.MOBILE_NO)) {
            if (!isConsumerIdValid(addAttributeTO.getAttributeValue())) {
                errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
            }
        } else {
            if (!isValid(addAttributeTO.getAttributeValue(), 255)) {
                errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
            }
        }
        /*if (!isValid(addAttributeTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (addAttributeTO.getComments() != null && !addAttributeTO.getComments().isEmpty()) {
            if (!isValidComment(addAttributeTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForAddAttribute(AttributeDataTO addAttributeTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(addAttributeTO.getAttributeName(), 255)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (!isValid(addAttributeTO.getAttributeValue())) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (!isAttributeNameValid(addAttributeTO.getAttributeName())) {
            errorMessage.append(addAttributeTO.getAttributeName()).append(Constant._COMMA);
        }
        if (!isAttributeValueValid(addAttributeTO.getAttributeName(), addAttributeTO.getAttributeValue())) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (addAttributeTO.getOldattributeValue() != null && addAttributeTO.getOldattributeValue().equalsIgnoreCase(addAttributeTO.getAttributeValue())) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForVerifyAttributesV4(AttributeDataRequestTO attributeDataTO) throws AuthException {
        StringJoiner errorMessage = new StringJoiner(Constant._COMMA);
        if (attributeDataTO.getSearchAttributes() != null &&
                !attributeDataTO.getSearchAttributes().isEmpty()) {
            for (AttributeDataTO attributeTO : attributeDataTO.getSearchAttributes()) {
                if (!isValid(attributeTO.getAttributeName())) {
                    errorMessage.add(Constant.ATTRIBUTE_NAME);
                }
                if (!isValid(attributeTO.getAttributeValue())) {
                    errorMessage.add(Constant.ATTRIBUTE_VALUE);
                }
            }
        } else {
            errorMessage.add(Constant.SEARCH_ATTRIBUTES);
        }
        if (attributeDataTO.getAttributes() != null &&
                !attributeDataTO.getAttributes().isEmpty()) {
            for (AttributeDataTO attributeTO : attributeDataTO.getAttributes()) {
                if (!isValid(attributeTO.getAttributeName())) {
                    errorMessage.add(Constant.ATTRIBUTE_NAME);
                }
                if (!isValid(attributeTO.getAttributeValue())) {
                    errorMessage.add(Constant.ATTRIBUTE_VALUE);
                }
            }
        } else {
            errorMessage.add(Constant.REQUEST_ATTRIBUTES);
        }
        return errorMessage.length() > 0 ? Constant.COMPULSORY_FIELDS + errorMessage : Constant._EMPTY;
    }

    public static String isRequestValidForModifyAttribute(AttributeDataTO attribute) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (attribute == null) {
            errorMessage.append(Constant.ATTRIBUTE_DATA).append(Constant._COMMA);
        } else {
            if (!isValid(attribute.getAttributeName(), 255)) {
                errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
            }
            if (!isValid(attribute.getAttributeValue())) {
                errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
            }
            if (!isAttributeNameValid(attribute.getAttributeName())) {
                errorMessage.append(attribute.getAttributeName()).append(Constant._COMMA);
            }
            if (!isAttributeValueValid(attribute.getAttributeName(), attribute.getAttributeValue())) {
                errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
            }
            if (attribute.getAttributeAction() == null) {
                errorMessage.append(Constant.ATTRIBUTE_ACTION).append(Constant._COMMA);
            }
            if (attribute.getAttributeAction() != null) {
                if (!attribute.getAttributeAction().equals(AttributeAction.DELETE) && !attribute.getAttributeAction().equals(AttributeAction.UPDATE)) {
                    errorMessage.append(Constant.ATTRIBUTE_ACTION).append(Constant._COMMA);
                }
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForAddAttributeByApplication(AttributeTO addAttributeTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(addAttributeTO.getAttributeName(), 255)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (!isValid(addAttributeTO.getAttributeValue(), 255)) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
        }
        /*if (!isValid(addAttributeTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (addAttributeTO.getIdentifier() != null && addAttributeTO.getIdentifier().getAttributeName() != null && addAttributeTO.getAttributeValue() != null) {
            errorMessage.append(Constant.IDENTIFIER).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForAttributeRequest(UserTO userTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(userTO.getUserIdentifier())) {
            errorMessage.append(Constant.USER_IDENTIFIER).append(Constant._COMMA);
        }
        if (userTO.getAttributes() == null || userTO.getAttributes().isEmpty()) {
            errorMessage.append(Constant.ATTRIBUTES_FIELD).append(Constant._COMMA);
        } else {
            for (AttributeDataTO attributeData : userTO.getAttributes()) {
                if (!isValid(attributeData.getAttributeName())) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                }
            }
        }
        if (userTO.getComments() != null && !userTO.getComments().isEmpty()) {
            if (!isValidComment(userTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForPolicyOnboard(PolicyTO policyTO) {
        StringBuilder errorMessage = new StringBuilder();

        if (!isValid(policyTO.getPolicyVersion())) {
            errorMessage.append(Constant.POLICY_UNIQUE_ID).append(Constant._COMMA);
        }

        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForPolicyOnboard(PolicyWeTO policyWE) {
        StringBuilder errorMessage = new StringBuilder();

        if (!isValid(policyWE.getPolicy().getPolicyVersion())) {
            errorMessage.append(Constant.POLICY_UNIQUE_ID).append(Constant._COMMA);
        }
        if (policyWE.getComments() != null && !policyWE.getComments().isEmpty()) {
            if (!isValidComment(policyWE.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForPolicyEdit(PolicyWeTO policyWE, String policyId) {
        StringBuilder errorMessage = new StringBuilder();

        if (!isValid(policyWE.getPolicy().getPolicyVersion())) {
            errorMessage.append(Constant.POLICY_UNIQUE_ID).append(Constant._COMMA);
        }
        if (!isValid(policyWE.getPolicy().getId())) {
            errorMessage.append(Constant.POLICY_ID).append(Constant._COMMA);
        }
        if (!isValid(policyId)) {
            errorMessage.append(Constant.POLICY_ID).append(Constant._COMMA);
        }
        if (policyWE.getComments() != null && !policyWE.getComments().isEmpty()) {
            if (!isValidComment(policyWE.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() == 0 && !(policyWE.getPolicy().getId().equals(policyId))) {
            errorMessage.append(Constant.POLICY_ID_INVALID).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForApprove(UserTO userTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(userTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(userTO.getApprovalStatus()) == null) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (userTO.getComments() != null && !userTO.getComments().isEmpty()) {
            if (!isValidComment(userTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        /*else if (userTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
            if (!isValid(userTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }


    public static String isRequestValidForAddAttributeMetaData(AttributeMetadataTO attributeMetadataTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(attributeMetadataTO.getAttributeName(), 255)) {
            errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
        }
        if (!isValid(attributeMetadataTO.getAttributeType(), 255)) {
            errorMessage.append(Constant.ATTRIBUTE_TYPE).append(Constant._COMMA);
        }
        if (!isValid(attributeMetadataTO.getAttributeStoreSecurityPolicy(), 255)) {
            errorMessage.append(Constant.ATTRIBUTE_STRORE_SECURITY_POLICY).append(Constant._COMMA);
        }
        if (!isValid(attributeMetadataTO.getAttributeValueModel(), 255)) {
            errorMessage.append(Constant.ATTRIBUTE_VALUE_MODEL).append(Constant._COMMA);
        }
        /*if(!isValid(addAttributeTO.getComments(),255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (attributeMetadataTO.getComments() != null && !attributeMetadataTO.getComments().isEmpty()) {
            if (!isValidComment(attributeMetadataTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        Map<String, Object> attributeSettings = attributeMetadataTO.getAttributeSettings();
        String title = (String) attributeSettings.get("title");
        if (!isValidComment(title, 255)) {
            errorMessage.append(Constant.ATTRIBUTE_TITLE).append(Constant._COMMA);
        }
        List<AttributeVerifierTO> attributeVerifiers = attributeMetadataTO.getAttributeVerifiers();
        String verifierType = null;
        String verifierId = null;
        String sourceType = null;
        for (AttributeVerifierTO attributeVerifier : attributeVerifiers) {
            verifierType = attributeVerifier.getVerifierType();
            verifierId = attributeVerifier.getVerifierId();
            sourceType = attributeVerifier.getSourceType();
        }
        if (verifierType != null && (!isValid(verifierType, 255))) {
            errorMessage.append(Constant.ATTRIBUTE_VERIFIER_TYPE).append(Constant._COMMA);
        }
        if (sourceType != null && (!isValid(sourceType, 255))) {
            errorMessage.append(Constant.ATTRIBUTE_SOURCE_TYPE).append(Constant._COMMA);

        }
        if (verifierId != null && (!isValid(verifierId, 255))) {
            errorMessage.append(Constant.ATTRIBUTE_VERIFIER_ID).append(Constant._COMMA);

        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForApproveAttributeMaster(AttributeMetadataTO attributeMetadataTO) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(attributeMetadataTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(attributeMetadataTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (attributeMetadataTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(attributeMetadataTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        /*if (!isValid(attributeMetadataTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForUserStatusUpdate(UserStatusTO userStatusTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(userStatusTO.getAccountId())) {
            errorMessage.append(Constant.ACCOUNT_ID).append(Constant._COMMA);
        }
        if (!isValid(userStatusTO.getState())) {
            errorMessage.append(Constant.STATE).append(Constant._COMMA);
        }
        if (userStatusTO.getComments() != null && !userStatusTO.getComments().isEmpty()) {
            if (!isValidComment(userStatusTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForApproval(UserStatusTO userStatusTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(userStatusTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(userStatusTO.getApprovalStatus()) == null) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (userStatusTO.getComments() != null && !userStatusTO.getComments().isEmpty()) {
            if (!isValidComment(userStatusTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        /*else if (userStatusTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
            if (!isValid(userStatusTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForEvidence(InputStream inputeStream, String fileName) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(fileName)) {
            errorMessage.append(Constant.FILE_NAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForApprove(AttributeTO attributeTo) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(attributeTo.getApprovalStatus()) || TransactionApprovalStatus.valueOf(attributeTo.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            if (attributeTo.getComments() != null && !attributeTo.getComments().isEmpty()) {
                if (!isValidComment(attributeTo.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }
        } catch (Exception e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForApprove(EvidenceRequestTO evidenceRequestTo) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(evidenceRequestTo.getApprovalStatus()) || TransactionApprovalStatus.valueOf(evidenceRequestTo.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
        } catch (Exception e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String validateRequestType(String requestType) {
        StringBuilder errorMessage = new StringBuilder();
        if (requestType == null || (!requestType.equals(Constant.RECEIVED) && !requestType.equals(Constant.SENT))) {
            errorMessage.append(Constant.REQUEST_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForClearCache(ApplicationTO applicationTO) throws ServiceNotFoundException {
        StringBuilder errorMessage = new StringBuilder();

        if (!isValid(applicationTO.getApplicationId())) {
            errorMessage.append(Constant.APPLICATION_NAME).append(Constant._COMMA);
        }
        if (applicationTO.getPassword() == null) {
            errorMessage.append(Constant.PASSWORD).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForOnboardApplication(ApplicationTO applicationTO) throws ServiceNotFoundException {
        StringBuilder errorMessage = new StringBuilder();
        if (!Config.getInstance().getProperty(Constant.ENTERPRISE_ID).equals(applicationTO.getEnterpriseId())) {
            errorMessage.append(Constant.ENTERPRISE_ID).append(Constant._COMMA);
        }
        if (!isValid(applicationTO.getApplicationName())) {
            errorMessage.append(Constant.APPLICATION_NAME).append(Constant._COMMA);
        }
        if (applicationTO.getIsNotificationEnabled() != null && applicationTO.getIsNotificationEnabled()) {
            if (!isValid(applicationTO.getCallbackUrl())) {
                errorMessage.append(Constant.CALLBACK_URL).append(Constant._COMMA);
            }
            if (!isValid(applicationTO.getQueueName())) {
                errorMessage.append(Constant.QUEUE_NAME).append(Constant._COMMA);
            }
        }
        if (applicationTO.getActivationDate() == null) {
            errorMessage.append(Constant.ACTIVATION_DATE).append(Constant._COMMA);
        }
        if (applicationTO.getExpirationDate() == null) {
            errorMessage.append(Constant.EXPIRATION_DATE).append(Constant._COMMA);
        }
        if (applicationTO.getPassword() == null) {
            errorMessage.append(Constant.PASSWORD).append(Constant._COMMA);
        }
        if (applicationTO.getComments() != null && !applicationTO.getComments().isEmpty()) {
            if (!isValidComment(applicationTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (!isValidTwoFa(applicationTO.getTwoFactorStatus())) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (applicationTO.getResetPinUserUnblockSetting() != null && !applicationTO.getResetPinUserUnblockSetting().isEmpty()) {
            if (!isValidResetPinUserUnblockSetting(applicationTO.getResetPinUserUnblockSetting())) {
                errorMessage.append(Constant.RESET_PIN_STATUS).append(Constant._COMMA);
            }
        }
        if (applicationTO.getUrl() != null) {
            if (!Pattern.matches(Config.getInstance().getProperty(Constant.URL_REGEX), applicationTO.getUrl())) {
                errorMessage.append(Constant.URL).append(Constant._COMMA);
            }
        }
        if (!isValidServiceName(applicationTO)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (applicationTO.getAlgorithm() != null) {
            Algorithm algorithm = Algorithm.valueOf(applicationTO.getAlgorithm());
            if (algorithm == null) {
                errorMessage.append(Constant.ALGORITHM).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForOnboardUser(UserIciciTO userTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (userTO.getSearchAttributes() != null) {
            for (AttributeDataTO attributeTO : userTO.getSearchAttributes()) {
                if (attributeTO.getAttributeName() == null || attributeTO.getAttributeName().isEmpty()) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                }
                if (attributeTO.getAttributeValue() == null || attributeTO.getAttributeValue().isEmpty()) {
                    errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                }
                if (attributeTO.getAttributeName() != null && attributeTO.getAttributeValue() != null && !isAttributeValueValid(attributeTO.getAttributeName(), attributeTO.getAttributeValue())) {
                    errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                }
            }
        }
        if (userTO.getAttributeData() != null) {
            for (AttributeDataTO attributeData : userTO.getAttributeData()) {
                if (attributeData.getAttributeName() == null || attributeData.getAttributeName().isEmpty()) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                } else if (attributeData.getAttributeValue() == null || attributeData.getAttributeValue().isEmpty()) {
                    errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                } else {
                    if (!isAttributeNameValid(attributeData.getAttributeName())) {
                        errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                    } else if (!isAttributeValueValid(attributeData.getAttributeName(), attributeData.getAttributeValue())) {
                        errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                    }
                }
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;

    }

    public static String isRequestValidForOnboardUserV4(UserIciciTO userTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        Map<String, String> attributeMap = new HashMap<>();
        if (userTO.getSearchAttributes() != null) {
            for (AttributeDataTO attributeTO : userTO.getSearchAttributes()) {
                if (attributeMap.get(attributeTO.getAttributeName()) != null && attributeTO.getAttributeValue().equals(attributeMap.get(attributeTO.getAttributeName()))) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_REQUEST(), errorConstant.getERROR_MESSAGE_DUPLICATE_SEARCH_ATTRIBUTE() + " : " + attributeTO.getAttributeName());
                }
                if (attributeTO.getAttributeName() == null || attributeTO.getAttributeName().isEmpty()) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                } else if (attributeTO.getAttributeValue() == null || attributeTO.getAttributeValue().isEmpty()) {
                    errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                } else {
                    if (!isAttributeNameValid(attributeTO.getAttributeName())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    } else if (!isAttributeValueValid(attributeTO.getAttributeName(), attributeTO.getAttributeValue())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    }
                }
                attributeMap.put(attributeTO.getAttributeName(), attributeTO.getAttributeValue());
            }
        }
        if (userTO.getAttributeData() != null) {
            for (AttributeDataTO attributeData : userTO.getAttributeData()) {
                if (attributeMap.get(attributeData.getAttributeName()) != null && attributeData.getAttributeValue().equals(attributeMap.get(attributeData.getAttributeName()))) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_REQUEST(), errorConstant.getERROR_MESSAGE_DUPLICATE_ATTRIBUTE() + " : " + attributeData.getAttributeName());
                }
                if (attributeData.getAttributeName() == null || attributeData.getAttributeName().isEmpty()) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                } else if (attributeData.getAttributeValue() == null || attributeData.getAttributeValue().isEmpty()) {
                    errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                } else {
                    if (!isAttributeNameValid(attributeData.getAttributeName())) {
                        errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                    } else if (!isAttributeValueValid(attributeData.getAttributeName(), attributeData.getAttributeValue())) {
                        errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                    }
                }
                attributeMap.put(attributeData.getAttributeName(), attributeData.getAttributeValue());
            }
        }
        if (userTO.getSubscribedApplications() == null || userTO.getSubscribedApplications().isEmpty()) {
            errorMessage.append(Constant.SUBSCRIBED_APPLICATION).append(Constant._COMMA);
        }

        if ((userTO.getCamEnabled() != null && userTO.getCamEnabled()) && (userTO.getUserCredential() == null || userTO.getUserCredential().isEmpty() || userTO.getUserCredential().trim().isEmpty())) {
            errorMessage.append(Constant.USER_CREDENTIAL).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;

    }

    public static String isRequestValidForAttributeAddition(UserIciciTO userTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (userTO.getSearchAttributes() != null) {
            for (AttributeDataTO attributeTO : userTO.getSearchAttributes()) {
                if (attributeTO.getAttributeName() == null || attributeTO.getAttributeName().isEmpty()) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                } else if (attributeTO.getAttributeValue() == null || attributeTO.getAttributeValue().isEmpty()) {
                    errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                } else {
                    if (!isAttributeNameValid(attributeTO.getAttributeName())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    } else if (!isAttributeValueValid(attributeTO.getAttributeName(), attributeTO.getAttributeValue())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    }
                }
            }
        }
        if (userTO.getAttributeData() != null) {
            for (AttributeDataTO attributeData : userTO.getAttributeData()) {
                if (attributeData.getAttributeName() == null || attributeData.getAttributeName().isEmpty()) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                } else if (attributeData.getAttributeValue() == null || attributeData.getAttributeValue().isEmpty()) {
                    errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                } else {
                    if (!isAttributeNameValid(attributeData.getAttributeName())) {
                        errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                    } else if (!isAttributeValueValid(attributeData.getAttributeName(), attributeData.getAttributeValue())) {
                        errorMessage.append(attributeData.getAttributeName()).append(Constant._COMMA);
                    }
                }
            }
        }
        if (userTO.getCamEnabled() == null) {
            errorMessage.append(Constant.IS_CAM_ENABLED).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;

    }

    public static String isRequestValidForPasswordChange(UserIciciTO userTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (userTO.getSearchAttributes() != null) {
            for (AttributeDataTO attributeTO : userTO.getSearchAttributes()) {
                if (attributeTO.getAttributeName() == null || attributeTO.getAttributeName().isEmpty()) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                } else if (attributeTO.getAttributeValue() == null || attributeTO.getAttributeValue().isEmpty()) {
                    errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                } else {
                    if (!isAttributeNameValid(attributeTO.getAttributeName())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    } else if (!isAttributeValueValid(attributeTO.getAttributeName(), attributeTO.getAttributeValue())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    }
                }
            }
        }
        if (userTO.getAttributeData() != null) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_REQUEST(), "Attribute data is not required for change pin");
        }
        if (userTO.getUserCredential() == null || userTO.getUserCredential().isEmpty()) {
            errorMessage.append(Constant.USER_CREDENTIAL).append(Constant._COMMA);
        }
        if (userTO.getUserCredential() != null && !userTO.getUserCredential().isEmpty()) {
            in.fortytwo42.enterprise.extension.tos.PasswordTO passwordTO2 = new in.fortytwo42.enterprise.extension.tos.PasswordTO();
            passwordTO2.setPassword(userTO.getUserCredential());
            passwordTO2.setAccountType(UserRole.USER.getAccountType());
            IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
            iamExtensionService.validatePassword(passwordTO2);
        }
        if (errorMessage.length() > 0) {
            return Constant.INVALID_SEARCH_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForUserStatus(UserIciciStatusTO userTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (userTO.getSearchAttributes() != null) {
            for (AttributeDataTO attributeTO : userTO.getSearchAttributes()) {
                if (attributeTO.getAttributeName() == null || attributeTO.getAttributeName().isEmpty()) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                } else if (attributeTO.getAttributeValue() == null || attributeTO.getAttributeValue().isEmpty()) {
                    errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                } else {
                    if (!isAttributeNameValid(attributeTO.getAttributeName())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    } else if (!isAttributeValueValid(attributeTO.getAttributeName(), attributeTO.getAttributeValue())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    } else if (!isAttributeUnique(attributeTO.getAttributeName())) {
                        errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                    }
                }
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForValidateHOTP(HotpTO hotpTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (hotpTO.getSearchAttributes() != null && !hotpTO.getSearchAttributes().isEmpty()) {
            for (AttributeDataTO attributeDataTO : hotpTO.getSearchAttributes()) {
                if (!isValid(attributeDataTO.getAttributeName())) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                }
                if ((attributeDataTO.getAttributeName() != null && isValid(attributeDataTO.getAttributeName())) &&
                        !isAttributeNameValid(attributeDataTO.getAttributeName())) {
                    errorMessage.append(attributeDataTO.getAttributeName()).append(Constant._COMMA);
                }
                if (!isValid(attributeDataTO.getAttributeValue())) {
                    errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
                }
            }
        } else {
            errorMessage.append(Constant.SEARCH_ATTRIBUTE).append(Constant._COMMA);
        }
        if (!isValid(hotpTO.getAuthenticationToken())) {
            errorMessage.append(Constant.AUTHENTICATION_TOKEN).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }


    public static String isRequestValidForOnboardUser(UserTO userTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!Constant.ADFS.equals(userTO.getAuthType()) && !isValid(userTO.getUserCredential())) {
            errorMessage.append(Constant.USER_CREDENTIAL).append(Constant._COMMA);
        }
        if (!isValid(userTO.getAccountType())) {
            errorMessage.append(Constant.ACCOUNT_TYPE).append(Constant._COMMA);
        } else {
            if (userTO.getAccountType() == UserRole.ADMIN.getAccountType()) {
                boolean attributesPresent = true;
                if (userTO.getAttributes() == null || userTO.getAttributes().size() == 0) {
                    attributesPresent = false;
                    errorMessage.append(Constant.ATTRIBUTES_FIELD).append(Constant._COMMA);
                }
                if (attributesPresent) {
                    List<String> attributeNames = new ArrayList<>();
                    for (AttributeDataTO attributeDataTO : userTO.getAttributes()) {
                        attributeNames.add(attributeDataTO.getAttributeName().toUpperCase());
                    }
                    if (!attributeNames.contains(Constant.USER_ID)) {
                        errorMessage.append(Constant.USER_ID).append(Constant._COMMA);
                    }
                    /*if (!attributeNames.contains(Constant.EMAIL_ID)) {
                        errorMessage.append(Constant.EMAIL_ID).append(Constant._COMMA);
                    }
                    if (!attributeNames.contains(Constant.MOBILE_NO)) {
                        errorMessage.append(Constant.MOBILE_NO).append(Constant._COMMA);
                    }*/
                }
                if (!Constant.ADFS.equals(userTO.getAuthType()) && userTO.getUserCredential() == null) {
                    errorMessage.append(Constant.PASSWORD).append(Constant._COMMA);
                }
            }
        }
        if (userTO.getCredentialsThroughEmail() != null && userTO.getCredentialsThroughEmail() == true) {
            List<AttributeDataTO> attributres = userTO.getAttributes();
            List<String> attributeNames = new ArrayList<>();
            if (!attributres.isEmpty()) {
                for (AttributeDataTO attribute : attributres) {
                    attributeNames.add(attribute.getAttributeName());
                    if (attribute.getAttributeName().equals(Constant.EMAIL_ID) && attribute.getAttributeValue() == null) {
                        errorMessage.append(Constant.EMAIL_ID).append(Constant._COMMA);
                    }
                }
                int number = attributeNames.indexOf(Constant.EMAIL_ID);
                if (number == -1) {
                    errorMessage.append(Constant.EMAIL_ID).append(Constant._COMMA);
                }

            }
        }

        if (userTO.getAttributes() == null || userTO.getAttributes().size() == 0) {
            errorMessage.append(Constant.ATTRIBUTES_FIELD).append(Constant._COMMA);
        }
        if (!isValid(userTO.getState())) {
            errorMessage.append(Constant.STATE).append(Constant._COMMA);
        }
        if (userTO.getAccountType() == UserRole.USER.getAccountType() || userTO.getAccountType() == UserRole.SUPER_USER.getAccountType()) {
            if (userTO.getSubscribedApplications() == null || userTO.getSubscribedApplications().size() < 1) {
                errorMessage.append(Constant.APPLICATION).append(Constant._COMMA);
            }
        }
        if (userTO.getComments() != null && !userTO.getComments().isEmpty()) {
            if (!isValidComment(userTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }
    public static String validateAddLdap(LdapDetailsTO ldapDetailsTO) {
        StringBuilder errorMessage = new StringBuilder();

        if (ldapDetailsTO.getDomainName()==null || ldapDetailsTO.getDomainName().isEmpty()) {
            errorMessage.append(Constant.LDAP_DOMAIN_NAME).append(Constant._COMMA);
        }
        if (ldapDetailsTO.getConnectionUrl()==null || ldapDetailsTO.getConnectionUrl().isEmpty()) {
            errorMessage.append(Constant.LDAP_CONNECTION_URL).append(Constant._COMMA);
        }else {
            try {
                URL url = new URL(ldapDetailsTO.getConnectionUrl());
                if(url.getPort()==-1){
                    errorMessage.append(Constant.LDAP_CONNECTION_URL).append(Constant._COMMA);
                }
            } catch (MalformedURLException e) {
                try {
                    URI uri = new URI(ldapDetailsTO.getConnectionUrl());
                    if(uri.getPort()==-1){
                        errorMessage.append(Constant.LDAP_CONNECTION_URL).append(Constant._COMMA);
                    }
                } catch (URISyntaxException ex) {
                    errorMessage.append(Constant.LDAP_CONNECTION_URL).append(Constant._COMMA);
                }
            }
        }
        if(ldapDetailsTO.getUserDomainName()==null || ldapDetailsTO.getUserDomainName().isEmpty()){
            errorMessage.append(Constant.LDAP_USER_DOMAIN_NAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String validateEditLdapDetails(LdapDetailsTO ldapDetailsTO) {
        StringBuilder errorMessage = new StringBuilder();

        if (ldapDetailsTO.getConnectionUrl()!=null && !ldapDetailsTO.getConnectionUrl().isEmpty()) {
            try {
                URL url = new URL(ldapDetailsTO.getConnectionUrl());
                if(url.getPort()==-1){
                    errorMessage.append(Constant.LDAP_CONNECTION_URL).append(Constant._COMMA);
                }
            } catch (MalformedURLException e) {
                try {
                    URI uri = new URI(ldapDetailsTO.getConnectionUrl());
                    if(uri.getPort()==-1){
                        errorMessage.append(Constant.LDAP_CONNECTION_URL).append(Constant._COMMA);
                    }
                } catch (URISyntaxException ex) {
                    errorMessage.append(Constant.LDAP_CONNECTION_URL).append(Constant._COMMA);
                }
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }



    public static String isDataValidForGetSRAApplication(String SRAApplicationType) {
        StringBuilder errorMessage = new StringBuilder();

        if (!ValidationUtil.isDataValid(SRAApplicationType)) {
            errorMessage.append(Constant.SRA_APPLICATION_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForSubscriptionCheck(TunnelingApplicationTO tunnelingApplicationTO) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isConsumerIdValid(tunnelingApplicationTO.getConsumerId())) {
                errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
            }
            if (!isValid(tunnelingApplicationTO.getRemoteApplicationId()) && !tunnelingApplicationTO.getRemoteApplicationId().isEmpty()) {
                errorMessage.append(Constant.REMOTE_APPLICATION_ID).append(Constant._COMMA);
            }
            if (!isValid(tunnelingApplicationTO.getReferenceNumber()) && !tunnelingApplicationTO.getReferenceNumber().isEmpty()) {
                errorMessage.append(Constant.REFERENCE_NUMBER).append(Constant._COMMA);
            }
        } catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForGetRemoteAccessSettings(RemoteAccessSettingTO remoteAccessSettingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(remoteAccessSettingTO.getExternalAddress(), 50)) {
            errorMessage.append(Constant.EXTERNAL_ADDRESS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidSearchText(String searchText) {
        StringBuilder errorMessage = new StringBuilder();
        if (searchText != null && !Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), searchText)) {
            errorMessage.append(Constant.SEARCH_QUERY).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isApplicationValidForUpdate(ApplicationTO applicationTO) throws ServiceNotFoundException {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationTO.getApplicationName(), 50)) {
            errorMessage.append(Constant.APPLICATION_NAME).append(Constant._COMMA);
        }
        /*if (!isValid(applicationTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        boolean isTwoFactorPresent = false;
        try {
            isTwoFactorPresent = isValid(applicationTO.getTwoFactorStatus()) && TwoFactorStatus.valueOf(applicationTO.getTwoFactorStatus()) != null;
        } catch (IllegalArgumentException e) {
            isTwoFactorPresent = false;
        }
        if (!isValid(applicationTO.getDescription(), 255) && applicationTO.getAuthenticationRequired() == null
                && (applicationTO.getServices() == null || applicationTO.getServices().isEmpty())
                && !isTwoFactorPresent) {
            errorMessage.append(Constant.DESCRIPTION).append(Constant.OR).append(Constant.SERVICES).append(Constant.OR).append(Constant.TWO_FACTOR_STATUS).append(Constant.OR)
                    .append(Constant.AUTHENTICATION_REQUIRED);
        }
        if (applicationTO.getUrl() != null) {
            if (!Pattern.matches(Config.getInstance().getProperty(Constant.URL_REGEX), applicationTO.getUrl())) {
                errorMessage.append(Constant.URL).append(Constant._COMMA);
            }
        }
        if (applicationTO.getComments() != null && !applicationTO.getComments().isEmpty()) {
            if (!isValidComment(applicationTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (!Config.getInstance().getProperty(Constant.ENTERPRISE_ID).equals(applicationTO.getEnterpriseId())) {
            errorMessage.append(Constant.ENTERPRISE_ID).append(Constant._COMMA);
        }
        if (!isValidTwoFa(applicationTO.getTwoFactorStatus())) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (applicationTO.getResetPinUserUnblockSetting() != null && !applicationTO.getResetPinUserUnblockSetting().isEmpty()) {
            if (!isValidResetPinUserUnblockSetting(applicationTO.getResetPinUserUnblockSetting())) {
                errorMessage.append(Constant.RESET_PIN_STATUS).append(Constant._COMMA);
            }
        }
        if (!isValidServiceName(applicationTO)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (applicationTO.getTransactionTimeout() != null && applicationTO.getTransactionTimeout() > Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT))) {
            return Constant.INVALID_TRANSACTION_TIMEOUT + Config.getInstance().getProperty(Constant.MAX_TIMEOUT);
        }
        if (applicationTO.getIsFcmMultiDeviceAllowed() !=null && applicationTO.getIsFcmMultiDeviceAllowed() ) {
            FcmNotificationDetails fcmNotificationDetails = null;
            try {
                fcmNotificationDetails = MongoConnectionManagerIam.getInstance().getFcmNotificationDetailsByID(applicationTO.getApplicationId());
            } catch (Exception e) {
                fcmNotificationDetails = null;
            }
            if (applicationTO.getIsFcmMultiDeviceAllowed() !=null && applicationTO.getIsFcmMultiDeviceAllowed() && fcmNotificationDetails == null) {
                if (applicationTO.getProjectId() == null || applicationTO.getProjectId().isEmpty() || applicationTO.getServiceAccountJson() == null || applicationTO.getServiceAccountJson().isEmpty()
                        || applicationTO.getBundleId() == null || applicationTO.getBundleId().isEmpty() || applicationTO.getPackageName() == null || applicationTO.getPackageName().isEmpty()) {
                    errorMessage.append(Constant.PROJECT_ID + Constant.OR + Constant.SERVICE_ACCOUNT_JSON + Constant.OR + Constant.PACKAGE_NAME + Constant.OR + Constant.BUNDLE_ID);
                }
            }
        }

        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage;
        }
        return null;
    }

    public static String isSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(sraGatewaySettingTO.getAddress(), 255)) {
            errorMessage.append(Constant.ADDRESS).append(Constant._COMMA);
        }
        if (sraGatewaySettingTO.getComments() != null && !sraGatewaySettingTO.getComments().isEmpty()) {
            if (!isValidComment(sraGatewaySettingTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForeditDevice(DeviceTO deviceTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(deviceTO.getDeviceUDID())) {
            errorMessage.append(Constant.DEVICE_UDID).append(Constant._COMMA);
        }
        if (deviceTO.getDeviceState() == null) {
            errorMessage.append(Constant.DEVICE_STATE).append(Constant._COMMA);
        }
        if (deviceTO.getComments() != null && !deviceTO.getComments().isEmpty()) {
            if (!isValidComment(deviceTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForbindTokenToDevice(DeviceTO deviceTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(deviceTO.getDeviceUDID())) {
            errorMessage.append(Constant.DEVICE_UDID).append(Constant._COMMA);
        }
        if (deviceTO.getDeviceState() == null) {
            errorMessage.append(Constant.DEVICE_STATE).append(Constant._COMMA);
        }
        if (deviceTO.getTokens() == null && deviceTO.getTokens().isEmpty()) {
            errorMessage.append(Constant.TOKENS).append(Constant._COMMA);
        } else {
            for (TokenTO token : deviceTO.getTokens()) {
                if (!isValid(token.getTokenUDID())) {
                    errorMessage.append(Constant.TOKEN_UDID).append(Constant._COMMA);
                }
                if (!isValid(token.getType())) {
                    errorMessage.append(Constant.TOKEN_TYPE).append(Constant._COMMA);
                }
            }
        }
        if (deviceTO.getComments() != null && !deviceTO.getComments().isEmpty()) {
            if (!isValidComment(deviceTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForeditToken(TokenTO tokenTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(tokenTO.getTokenUDID())) {
            errorMessage.append(Constant.TOKEN_UDID).append(Constant._COMMA);
        }
        if (tokenTO.getState() == null) {
            errorMessage.append(Constant.STATE).append(Constant._COMMA);
        }
        if (tokenTO.getComments() != null && !tokenTO.getComments().isEmpty()) {
            if (!isValidComment(tokenTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForTokenRemoteWipe(String tokenId) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(tokenId)) {
            errorMessage.append(Constant.TOKEN_UDID).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForOnboardProvider(IdentityProviderTO identityProviderTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(identityProviderTO.getName())) {
            errorMessage.append(Constant.NAME).append(Constant._COMMA);
        }
        if (!isValid(identityProviderTO.getConnectionUrl())) {
            errorMessage.append(Constant.CONNECTION_URL).append(Constant._COMMA);
        }
        if (!isValid(identityProviderTO.getAdminDomain())) {
            errorMessage.append(Constant.ADMIN_DOMAIN).append(Constant._COMMA);
        }
        if (!isValid(identityProviderTO.getAdminCredential())) {
            errorMessage.append(Constant.ADMIN_CREDENTIAL).append(Constant._COMMA);
        }
        if (!isValid(identityProviderTO.getVendor())) {
            errorMessage.append(Constant.VENDOR).append(Constant._COMMA);
        }
        if (!isValid(identityProviderTO.getType())) {
            errorMessage.append(Constant.TYPE).append(Constant._COMMA);
        }
        if (identityProviderTO.getComments() != null && !identityProviderTO.getComments().isEmpty()) {
            if (!isValidComment(identityProviderTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForSync(IdentityProviderTO identityProviderTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(identityProviderTO.getType())) {
            errorMessage.append(Constant.TYPE).append(Constant._COMMA);
        }
        if (identityProviderTO.getComments() != null && !identityProviderTO.getComments().isEmpty()) {
            if (!isValidComment(identityProviderTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForOnboardMapper(MapperTO mapperTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(mapperTO.getKey())) {
            errorMessage.append(Constant.KEY).append(Constant._COMMA);
        }
        if (!isValid(mapperTO.getValue())) {
            errorMessage.append(Constant.VALUE).append(Constant._COMMA);
        }
        if (mapperTO.getComments() != null && !mapperTO.getComments().isEmpty()) {
            if (!isValidComment(mapperTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForQRCodeV4(QRCodeDataTO qrCodeDataTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (qrCodeDataTO.getSearchAttributes() != null &&
                !qrCodeDataTO.getSearchAttributes().isEmpty()) {
            for (AttributeDataTO attributeTO : qrCodeDataTO.getSearchAttributes()) {
                if (!isValid(attributeTO.getAttributeName())) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                }
                if (!isValid(attributeTO.getAttributeValue())) {
                    errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
                }
            }
        } else {
            errorMessage.append(Constant.SEARCH_ATTRIBUTES).append(Constant._COMMA);
        }
        if (qrCodeDataTO.getCustomeAttributes() != null && !qrCodeDataTO.getCustomeAttributes().isEmpty()) {
            for (AttributeTO attributeTO : qrCodeDataTO.getCustomeAttributes()) {
                if (!isValid(attributeTO.getAttributeName())) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                }
                if (!isValid(attributeTO.getAttributeValue())) {
                    errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
                }
            }
        } else {
            errorMessage.append(Constant.CUSTOME_ATTRIBUTES).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }


    public static String isRequestValidForQRCode(QRCodeDataTO qrCodeDataTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (qrCodeDataTO.getSearchAttribute() != null) {
            if (!isValid(qrCodeDataTO.getSearchAttribute().getAttributeName())) {
                errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
            }
            if (!isValid(qrCodeDataTO.getSearchAttribute().getAttributeValue())) {
                errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
            }
        } else {
            errorMessage.append(Constant.SEARCH_ATTRIBUTE).append(Constant._COMMA);
        }
        if (qrCodeDataTO.getCustomeAttributes() != null && !qrCodeDataTO.getCustomeAttributes().isEmpty()) {
            for (AttributeTO attributeTO : qrCodeDataTO.getCustomeAttributes()) {
                if (!isValid(attributeTO.getAttributeName())) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                }
                if (!isValid(attributeTO.getAttributeValue())) {
                    errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
                }
            }
        } else {
            errorMessage.append(Constant.CUSTOME_ATTRIBUTES).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForOnboardUsers(List<UserIciciTO> userTOs) throws AuthException {
        String errorMessage = null;
        for (UserIciciTO userIciciTO : userTOs) {
            errorMessage = ValidationUtilV3.isRequestValidForOnboardUser(userIciciTO);
        }
        return errorMessage;
    }

    public static String isRequestValidForOnboardEnterprise(EnterpriseTO enterpriseTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(enterpriseTO.getEnterpriseName())) {
            errorMessage.append(Constant.ENTERPRISE_NAME).append(Constant._COMMA);
        }
        if (!isValid(enterpriseTO.getEnterpriseId())) {
            errorMessage.append(Constant.ENTERPRISE_ID).append(Constant._COMMA);
        }
        if (!isValid(enterpriseTO.getEnterpriseAccountId())) {
            errorMessage.append(Constant.ENTERPRISE_ACCOUNT_ID).append(Constant._COMMA);
        }
        if (!isValid(enterpriseTO.getEnterprisePassword())) {
            errorMessage.append(Constant.ENTERPRISE_PASSWORD).append(Constant._COMMA);
        }
        if (!isValid(enterpriseTO.getEnterpriseSecret())) {
            errorMessage.append(Constant.ENTERPRISE_SECRET).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForUnbindUsersFromDevice(DeviceTO deviceTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(deviceTO.getDeviceUDID())) {
            errorMessage.append(Constant.DEVICE_UDID).append(Constant._COMMA);
        }
        if (deviceTO.getDeviceState() == null) {
            errorMessage.append(Constant.DEVICE_STATE).append(Constant._COMMA);
        }
        if (deviceTO.getDeviceId() == null) {
            errorMessage.append(Constant.DEVICE_ID).append(Constant._COMMA);
        }
        if (deviceTO.getComments() != null && !deviceTO.getComments().isEmpty()) {
            if (!isValidComment(deviceTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static boolean isValidComment(String comments, int maxAllowedLength) {
        //String regex = "^[^-_.@\\s\\W][A-Za-z0-9-.@\\s\\.]*$";
        return comments != null && !comments.trim().isEmpty()
                && comments.length() <= maxAllowedLength
                && Pattern.matches(Config.getInstance().getProperty(Constant.COMMENT_VALIDATION_PATTERN), comments);

    }

    public static String isRequestValidForEditSRAGatewaySettings(SRAGatewaySettingTO sraGatewaySettingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (sraGatewaySettingTO.getComments() != null && !sraGatewaySettingTO.getComments().isEmpty()) {
            if (!isValidComment(sraGatewaySettingTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isRequestValidForDeleteAttributeMetaData(AttributeMetadataTO attributeMetadataTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (attributeMetadataTO.getComments() != null && !attributeMetadataTO.getComments().isEmpty()) {
            if (!isValidComment(attributeMetadataTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static boolean isValidTwoFa(String twoFactorStatus) {
        try {
            TwoFactorStatus enumValue = TwoFactorStatus.valueOf(twoFactorStatus.toUpperCase());
            return enumValue == TwoFactorStatus.ENABLED || enumValue == TwoFactorStatus.DISABLED;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidResetPinUserUnblockSetting(String resetPinUserUnblockSetting) {
        try {
            ResetPinUserUnblockStatus enumValue = ResetPinUserUnblockStatus.valueOf(resetPinUserUnblockSetting.toUpperCase());
            return enumValue == ResetPinUserUnblockStatus.APPLICATION_CUSTOMER_SERVICE_UNBLOCK
                    || enumValue == ResetPinUserUnblockStatus.APPLICATION_SELF_UNBLOCK
                    || enumValue == ResetPinUserUnblockStatus.AUTO_UNBLOCK;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidServiceName(ApplicationTO applicationTO) throws ServiceNotFoundException {
        try {
            List<ServiceTO> services = applicationTO.getServices();
            for (ServiceTO service : services) {
                String serviceName = service.getServiceName();
                Service service1 = serviceProcessor.getService(serviceName);
                if (service1 == null) {
                    return false;
                }
            }
        } catch (AuthException e) {
            return false;
        }
        return true;
    }

    public static boolean isAttributeNameValid(String attributeName) throws AuthException {
        AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
        List<AttributeMetadataTO> attributeMetaDataTOs = attributeMasterService.getAllAttributeMetaData();

        if (attributeName == null || attributeName.isEmpty()) {
            return false;
        }

        // Check if the attributeName exists in the attributeMetaDataTOs list
        boolean attributeNameValid = false;
        for (AttributeMetadataTO attributeMetadataTO : attributeMetaDataTOs) {
            if (attributeMetadataTO.getAttributeName().equalsIgnoreCase(attributeName)) {
                attributeNameValid = true;
                break;
            }
        }
        return attributeNameValid;
    }

    public static String validateQrRequest(QRCodeDataTO qrCodeDataTO) {
        StringBuilder errorMessage = new StringBuilder();
        String pattern = Config.getInstance().getProperty(Constant.QR_PREFIX_REGEX) != null ? Config.getInstance().getProperty(Constant.QR_PREFIX_REGEX) : "^[a-zA-Z0-9]{5}+$";
        if (qrCodeDataTO.getPrefix() == null || qrCodeDataTO.getPrefix().isEmpty() || !Pattern.matches(pattern, qrCodeDataTO.getPrefix())) {
            errorMessage.append(Constant.PREFIX).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    private static boolean isAttributeUnique(String attributeName) throws AuthException {
        AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();
        List<AttributeMetadataTO> attributeMetaDataTOs = attributeMasterService.getAllAttributeMetaData();

        for (AttributeMetadataTO attributeMetadataTO : attributeMetaDataTOs) {
            if (attributeMetadataTO.getAttributeName().equalsIgnoreCase(attributeName)) {
                if (!attributeMetadataTO.getIsUnique().equals(Boolean.TRUE)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String validateAndgetRequestType(UserIciciTO userTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (userTO.getSearchAttributes() != null && userTO.getSubscribedApplications() != null) {
            if (userTO.getSearchAttributes().isEmpty()) {
                errorMessage.append(Constant.SEARCH_ATTRIBUTES).append(Constant._COMMA);
            }
            if (userTO.getSubscribedApplications().isEmpty()) {
                errorMessage.append(Constant.SUBSCRIBED_APPLICATION).append(Constant._COMMA);
            }
            if (errorMessage.length() > 0) {
                String error = Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), error);
            }
            return ONBOARD_USER;
        } else if (userTO.getSearchAttributes() != null && userTO.getUserCredential() != null) {
            if (userTO.getSearchAttributes().isEmpty()) {
                errorMessage.append(Constant.SEARCH_ATTRIBUTES).append(Constant._COMMA);
            }
            if (errorMessage.length() > 0) {
                String error = Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), error);
            }
            return CHANGE_PASSWORD;
        } else {
            return OTHER;
        }
    }

    public static String isValidForCryptoTokenVerification(CryptoTokenTO cryptoTokenTO) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (cryptoTokenTO.getSearchAttributes() != null && !cryptoTokenTO.getSearchAttributes().isEmpty()) {
            for (AttributeTO attributeTO : cryptoTokenTO.getSearchAttributes()) {
                if (!isValid(attributeTO.getAttributeName())) {
                    errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
                }
                if (!isAttributeNameValid(attributeTO.getAttributeName())) {
                    errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
                }
                if (!isValid(attributeTO.getAttributeValue())) {
                    errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
                }
            }
        } else {
            errorMessage.append(Constant.SEARCH_ATTRIBUTE).append(Constant._COMMA);
        }
        if (cryptoTokenTO.getAttributeData() != null) {
            AttributeDataTO attributeTO = cryptoTokenTO.getAttributeData();
            if (!isValid(attributeTO.getAttributeName())) {
                errorMessage.append(Constant.ATTRIBUTE_NAME).append(Constant._COMMA);
            }
            if (!isAttributeNameValid(attributeTO.getAttributeName())) {
                errorMessage.append(attributeTO.getAttributeName()).append(Constant._COMMA);
            }
            if (!isValid(attributeTO.getAttributeValue())) {
                errorMessage.append(Constant.ATTRIBUTE_VALUE).append(Constant._COMMA);
            }
        } else {
            errorMessage.append(Constant.ATTRIBUTE_DATA).append(Constant._COMMA);
        }
        if (!isValid(cryptoTokenTO.getCryptoToken(), 10)) {
            errorMessage.append(Constant.CRYPTO_TOKEN).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }


    public static String isFalloutRecordValid(FalloutTO falloutRecord) throws AuthException {
        StringBuilder errorMessage = new StringBuilder();
        if (falloutRecord == null) {
            return Constant.COMPULSORY_FIELDS + Constant.INVALID_CSV_DATA_RECORD;
        }
        if (!isValid(falloutRecord.getFt42UserId())) {
            errorMessage.append(Constant.USER_ID).append(Constant._COMMA);
        }
        if (!isValid(falloutRecord.getOperation().name())) {
            errorMessage.append(Constant.CSV_OPERATION).append(Constant._COMMA);
        }
        if (!Constant.DELETE.equals(falloutRecord.getOperation().name()) && !isAttributeValueValid(Constant.MOBILE_NO, falloutRecord.getNewMobileNo())) {
            errorMessage.append(Constant.CSV_NEW_MOBILE_NO).append(Constant._COMMA);
        }
        if(falloutRecord.getOldMobileNo() != null && !falloutRecord.getOldMobileNo().isEmpty() && !isAttributeValueValid(Constant.MOBILE_NO, falloutRecord.getOldMobileNo())){
            errorMessage.append(Constant.CSV_OLD_MOBILE_NO).append(Constant._COMMA);
        }
        if(falloutRecord.getNewMobileNo() != null && !falloutRecord.getNewMobileNo().isEmpty() && !isAttributeValueValid(Constant.MOBILE_NO, falloutRecord.getNewMobileNo())){
            errorMessage.append(Constant.CSV_NEW_MOBILE_NO).append(Constant._COMMA);
        }
        if (falloutRecord.getOperation().equals(AttributeAction.UPDATE) && !isAttributeValueValid(Constant.MOBILE_NO, falloutRecord.getOldMobileNo())) {
            errorMessage.append(Constant.CSV_OLD_MOBILE_NO).append(Constant._COMMA);
        }
        if ((falloutRecord.getOperation().equals(AttributeAction.ADD)) && (falloutRecord.getOldMobileNo() != null && !falloutRecord.getOldMobileNo().isEmpty())) {
            errorMessage.append(Constant.CSV_OLD_MOBILE_NO).append(" FOR OPERATION ").append(Constant.CSV_OPERATION).append(Constant._COMMA);
        }
        String actualMobileNo = falloutRecord.getActualMobileNo();
        if (actualMobileNo == null || actualMobileNo.isEmpty()) {
            errorMessage.append(Constant.ACTUAL_MOBILE_NO).append(Constant._COMMA);
        } else if (!actualMobileNo.equals(Config.getInstance().getProperty(Constant.FALLOUT_PROCESS_DEH_NO_MOBILE_NO)) && !isAttributeValueValid(Constant.MOBILE_NO, actualMobileNo)) {
            errorMessage.append(Constant.ACTUAL_MOBILE_NO).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.toString().subSequence(0, errorMessage.length() - 1);
        }
        return "";
    }

    public static String validateResetPin(CryptoPinTO cryptoPinTO, String type) {
        StringJoiner errorMessage = new StringJoiner(Constant._COMMA);
        if (type.equals(Constant.APPLICATION) && !isValid(cryptoPinTO.getApplicationId())) {
            errorMessage.add(Constant.APPLICATON_ID);
        }
        if (!isValid(cryptoPinTO.getPassword())) {
            errorMessage.add(Constant.PASSWORD);
        }
        return errorMessage.length() > 0 ? Constant.COMPULSORY_FIELDS + errorMessage : Constant._EMPTY;
    }

    public static String validateChangePin(CryptoPinTO cryptoPinTO, String type) {
        StringJoiner errorMessage = new StringJoiner(Constant._COMMA);

        if (type.equals(Constant.APPLICATION) && !isValid(cryptoPinTO.getApplicationId())) {
            errorMessage.add(Constant.APPLICATON_ID);
        }
        if (!isValid(cryptoPinTO.getPassword())) {
            errorMessage.add(Constant.PASSWORD);
        }
        if (!isValid(cryptoPinTO.getOldPassword())) {
            errorMessage.add(Constant.OLD_PASSWORD);
        }
        return errorMessage.length() > 0 ? Constant.COMPULSORY_FIELDS + errorMessage : Constant._EMPTY;
    }

    public static void attributeNameToUpperCase(AttributeDataTO attributeDataTO) {
        attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
    }

    public static void validateSearchAttributes(List<AttributeDataTO> searchAttributes) throws AuthException {
        List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
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
            if (!attributeMetadataTO.getIsUnique()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE() + ": " + attributeMetadataTO.getAttributeName());
            }
        }
    }

    public static String validateCacheClearRequest(CacheTO cacheTO) {
        StringJoiner errorMessage = new StringJoiner(Constant._COMMA);
        if (cacheTO == null) {
            errorMessage.add(Constant.CACHE_COMPONENT);
        }
        return errorMessage.length() > 0 ? Constant.COMPULSORY_FIELDS + errorMessage : Constant._EMPTY;
    }

    public static String isRequestValidForVerifyRunningHash(RunningHashTo runningHashTo) {
        if (runningHashTo == null || !isValid(runningHashTo.getRunningHash())) {
            return Constant.COMPULSORY_FIELDS+Constant.RUNNING_HASH;
        }
        return "";
    }
    public static String isRequestValidForDisableUser(UserTO userTO) {
        StringBuilder errorMessage = new StringBuilder(Constant.COMPULSORY_FIELDS);
        if (userTO.getUserId() == null) {
            errorMessage.append(Constant.USER_ID).append(Constant._COMMA);
        }
        if (!isValid(userTO.getUsername())) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (userTO.getComments() != null && !userTO.getComments().isEmpty()) {
            if (!isValidComment(userTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > Constant.COMPULSORY_FIELDS.length()) {
            return errorMessage.subSequence(0, errorMessage.length() - 1).toString();
        }
        return null;
    }
    public static String validateEditFalloutSyncData(FalloutSyncDataTo falloutSyncDataTo) {
        StringBuilder errorMessage = new StringBuilder();
        if(falloutSyncDataTo.getLastSyncTime() == null){
            errorMessage.append(Constant.FALLOUT_DATA_SYNC_LAST_LOGIN_TIME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return (String) errorMessage.toString().subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }



    public static String isRequestValidForUpdateFalloutConfig(FalloutConfigTO falloutConfigTO) {
        StringBuilder errorMessage = new StringBuilder(Constant.COMPULSORY_FIELDS);
        if (falloutConfigTO.getDehFalloutDataProcess() == null) {
            errorMessage.append(Constant.DEH_FALLOUT_DATA_PROCESS).append(Constant._COMMA);
        }
        if (falloutConfigTO.getDehFalloutDataSync() == null) {
            errorMessage.append(Constant.DEH_FALLOUT_DATA_SYNC).append(Constant._COMMA);
        }
        if (falloutConfigTO.getNumberOfRecordsToBeProcessed() == null) {
            errorMessage.append(Constant.NUMBER_OF_RECORDS_TO_BE_PROCESSED).append(Constant._COMMA);
        }
        if (falloutConfigTO.getDataFetchFrequency() == null) {
            errorMessage.append(Constant.DATA_FETCH_FREQUENCY).append(Constant._COMMA);
        }
        if(falloutConfigTO.getSchedulerFrequency() != null && !falloutConfigTO.getSchedulerFrequency().isEmpty()){
            if(!isValidCommaSeparatedNumbers(falloutConfigTO.getSchedulerFrequency())){
                errorMessage.append(Constant.SCHEDULER_FREQUENCY).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > Constant.COMPULSORY_FIELDS.length()) {
            return errorMessage.subSequence(0, errorMessage.length() - 1).toString();
        }


        return null;
    }

    public static boolean isValidCommaSeparatedNumbers(String input) {
        // Regular expression to match numbers separated by commas
        String regex = "^\\d+(,\\d+)*$";
        // Compile the regex
        Pattern pattern = Pattern.compile(regex);
        // Check if input matches the regex
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public static String validateTransactionDetailsTo(TransactionReportRequestTO requestTO) {
        StringBuilder errorMessage = new StringBuilder(Constant.COMPULSORY_FIELDS);

        if (requestTO.getStartTimeInMillis() == 0 || requestTO.getEndTimeInMillis() == 0) {
            errorMessage.append(Constant.KEY).append(Constant._COMMA);
        }
        if (errorMessage.length() > Constant.COMPULSORY_FIELDS.length()) {
            return errorMessage.subSequence(0, errorMessage.length() - 1).toString();
        }
        return null;
    }
    public static String isRequestValidForConfig(ConfigTO configTO) {
        StringBuilder errorMessage = new StringBuilder(Constant.COMPULSORY_FIELDS);
        if (configTO.getConfigType() == null) {
            errorMessage.append(Constant.CONFIG_TYPE).append(Constant._COMMA);
        }
        if (configTO.getKey() == null || configTO.getKey().isEmpty()) {
            errorMessage.append(Constant.KEY).append(Constant._COMMA);
        }
        if (configTO.getValue() == null || configTO.getValue().isEmpty()) {
            errorMessage.append(Constant.VALUE).append(Constant._COMMA);
        }
        if (errorMessage.length() > Constant.COMPULSORY_FIELDS.length()) {
            return errorMessage.subSequence(0, errorMessage.length() - 1).toString();
        }
        return null;
    }

    public static String isRequestValidForOnboardTempDetails(TemplateDetailsTO templateDetailsTO) {
        StringBuilder errorMessage = new StringBuilder(Constant.COMPULSORY_FIELDS);

        if (!ValidationUtilV3.isValid(templateDetailsTO.getTemplateId()) || !templateDetailsTO.getTemplateId().startsWith(Constant.TEMP_ID)) {
            errorMessage.append(Constant.TEMPLATE_ID).append(Constant._COMMA);
        }
        if( !ValidationUtilV3.isValid(templateDetailsTO.getTemplate())|| !templateDetailsTO.getTemplate().contains("<OTP>")){
               errorMessage.append(Constant.TEMPLATE).append(Constant._COMMA);
        }
        if( !ValidationUtilV3.isValid(templateDetailsTO.getTemplateType().name())){
            errorMessage.append(Constant.TEMPLATE_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > Constant.COMPULSORY_FIELDS.length()) {
            return errorMessage.subSequence(0, errorMessage.length() - 1).toString();
        }
        return null;
    }


    public static String isRequestValidForEditTempDetails(TemplateDetailsTO templateDetailsTO) {
        StringBuilder errorMessage = new StringBuilder(Constant.COMPULSORY_FIELDS);

        if( !ValidationUtilV3.isValid(templateDetailsTO.getTemplate())|| !templateDetailsTO.getTemplate().contains("<OTP>")){
            errorMessage.append(Constant.TEMPLATE).append(Constant._COMMA);
        }
        if( !ValidationUtilV3.isValid(templateDetailsTO.getTemplateType().name())){
            errorMessage.append(Constant.TEMPLATE_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > Constant.COMPULSORY_FIELDS.length()) {
            return errorMessage.subSequence(0, errorMessage.length() - 1).toString();
        }
        return null;
    }



}
