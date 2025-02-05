
package in.fortytwo42.adapter.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import in.fortytwo42.adapter.enums.Presence;
import in.fortytwo42.adapter.enums.TransactionApprovalStatus;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ADUserBindingTO;
import in.fortytwo42.adapter.transferobj.AdapterApprovalAttemptTO;
import in.fortytwo42.adapter.transferobj.AdminTO;
import in.fortytwo42.adapter.transferobj.BlockUserApplicationTO;
import in.fortytwo42.adapter.transferobj.BulkEditUserTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.ConsumerTO;
import in.fortytwo42.adapter.transferobj.CryptoTokenTO;
import in.fortytwo42.adapter.transferobj.GroupCheckerTO;
import in.fortytwo42.adapter.transferobj.GroupDataTO;
import in.fortytwo42.adapter.transferobj.SupervisorUserTO;
import in.fortytwo42.adapter.transferobj.TwoFactorVerification;
import in.fortytwo42.adapter.transferobj.UserAuthenticationTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.entities.enums.ApplicationType;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.BulkUploadType;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.RequestTO;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;
import in.fortytwo42.tos.transferobj.UserGroupTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class ValidationUtil {

    private static int LABEL_LENGTH = 10;

    public static boolean isValid(String value) {
        return value != null && !value.trim().isEmpty();
    }
    private static ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    public static boolean isDataValid(String value) {
        return value != null && !value.trim().isEmpty() && Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), value);
    }

    public static boolean isValid(String value, int maxAllowedLength) {
        return value != null && !value.trim().isEmpty() && value.length() <= maxAllowedLength && Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), value);
    }

    public static boolean isConsumerIdValid(String value) {
        return value != null && !value.trim().isEmpty() && value.length() <= 50 && Pattern.matches(Config.getInstance().getProperty(Constant.MOBILE_PATTERN), value);
    }

    public static boolean isValidIfExist(String value, int maxAllowedLength) {
        if (value != null && !value.trim().isEmpty()) {
            return value.length() <= maxAllowedLength && Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), value);
        }
        return true;
    }

    public static String isDataValidForSearch(String status) {
        StringBuilder errorMessage = new StringBuilder();

        if (!ValidationUtil.isDataValid(status)) {
            errorMessage.append(Constant.STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForApplicationAuditTrailSearch(String searchQuery) {
        StringBuilder errorMessage = new StringBuilder();
        if (searchQuery != null && !Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), searchQuery)) {
            errorMessage.append(Constant.SEARCH_QUERY).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForGetUsers(String status, String approvalStatus) {
        StringBuilder errorMessage = new StringBuilder();

        if (!ValidationUtil.isDataValid(status)) {
            errorMessage.append(Constant.STATUS).append(Constant._COMMA);
        }
        try {
            if (approvalStatus != null && ApprovalStatus.valueOf(approvalStatus) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForApplicationSearch(String status, String applicationType) {
        StringBuilder errorMessage = new StringBuilder();

        if (!ValidationUtil.isDataValid(status)) {
            errorMessage.append(Constant.STATUS).append(Constant._COMMA);
        }
        try {
            if (applicationType != null && ApplicationType.valueOf(applicationType) == null) {
                errorMessage.append(Constant.APPLICATON_TYPE).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPLICATON_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isApplicationValidForLogin(ApplicationTO applicationTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationTO.getApplicationId(), 15)) {
            errorMessage.append(Constant.RESOURCE_APPLICATION_ID).append(Constant._COMMA);
        }
        if (!isValid(applicationTO.getApplicationSecret())) {
            errorMessage.append(Constant.APPLICATION_SECRET).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isApplicationValidForCreation(ApplicationTO applicationTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationTO.getApplicationId(), 15)) {
            errorMessage.append(Constant.RESOURCE_APPLICATION_ID).append(Constant._COMMA);
        }
        if (!isValid(applicationTO.getApplicationName(), 50)) {
            errorMessage.append(Constant.APPLICATION_NAME).append(Constant._COMMA);
        }
        if (!isValid(applicationTO.getDescription(), 255)) {
            errorMessage.append(Constant.DESCRIPTION).append(Constant._COMMA);
        }
        /*if (!isValid(applicationTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (!isValid(applicationTO.getApplicationSecret())) {
            errorMessage.append(Constant.APPLICATION_SECRET).append(Constant._COMMA);
        }
        if (!isValid(applicationTO.getPassword())) {
            errorMessage.append(Constant.PASSWORD).append(Constant._COMMA);
        }
        if (!isValid(applicationTO.getEnterpriseId(), 20)) {
            errorMessage.append(Constant.RESOURCE_ENTERPRISE_ID).append(Constant._COMMA);
        }
        try {
            if (applicationTO.getTwoFactorStatus() == null || TwoFactorStatus.valueOf(applicationTO.getTwoFactorStatus()) == null) {
                errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (applicationTO.getServices() == null || applicationTO.getServices().isEmpty()) {
            errorMessage.append(Constant.SERVICES).append(Constant._COMMA);
        }
        if (applicationTO.getTransactionTimeout() == null) {
            errorMessage.append(Constant.TRANSACTION_TIMEOUT).append(Constant._COMMA);
        }
        try {
            if (applicationTO.getApplicationType() == null || ApplicationType.valueOf(applicationTO.getApplicationType()) == null) {
                errorMessage.append(Constant.APPLICATON_TYPE).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPLICATON_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        else if (applicationTO.getTransactionTimeout() != null && applicationTO.getTransactionTimeout() > Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT))) {
            return Constant.INVALID_TRANSACTION_TIMEOUT + Config.getInstance().getProperty(Constant.MAX_TIMEOUT);
        }
        return null;
    }

    public static String isApplicationValidForDeletion(ApplicationTO applicationTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationTO.getApplicationId(), 15)) {
            errorMessage.append(Constant.RESOURCE_APPLICATION_ID).append(Constant._COMMA);
        }
        if (!isValid(applicationTO.getApplicationName(), 50)) {
            errorMessage.append(Constant.APPLICATION_NAME).append(Constant._COMMA);
        }

        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isApplicationValidForUpdate(ApplicationTO applicationTO) {
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
        }
        catch (IllegalArgumentException e) {
            isTwoFactorPresent = false;
        }
        if (!isValid(applicationTO.getDescription(), 255) && applicationTO.getAuthenticationRequired() == null
            && (applicationTO.getServices() == null || applicationTO.getServices().isEmpty())
            && !isTwoFactorPresent) {
            errorMessage.append(Constant.DESCRIPTION).append(Constant.OR).append(Constant.SERVICES).append(Constant.OR).append(Constant.TWO_FACTOR_STATUS).append(Constant.OR)
                    .append(Constant.AUTHENTICATION_REQUIRED);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage;
        }
        else if (applicationTO.getTransactionTimeout() != null && applicationTO.getTransactionTimeout() > Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT))) {
            return Constant.INVALID_TRANSACTION_TIMEOUT + Config.getInstance().getProperty(Constant.MAX_TIMEOUT);
        }
        return null;
    }

    public static String isUserValidForAuthentication(UserAuthenticationTO user, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(user.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isValid(user.getPassword())) {
            errorMessage.append(Constant.PASSWORD).append(Constant._COMMA);
        }
        if (!isValid(user.getAuthenticationType(), 15)) {
            errorMessage.append(Constant.AUTHENTICATION_TYPE).append(Constant._COMMA);
        }
        try {
            if (user.getPresence() != null && !user.getPresence().trim().isEmpty()) {
                if (Presence.valueOf(user.getPresence()) == null) {
                    errorMessage.append(Constant.PRESENCE).append(Constant._COMMA);
                }
                else {
                    if (Presence.OFFLINE_CRYPTO.name().equals(user.getPresence())) {
                        if (!isValid(user.getCryptoToken(), 10)) {
                            errorMessage.append(Constant.CRYPTO_TOKEN).append(Constant._COMMA);
                        }
                    }
                    else if (Presence.ONLINE.name().equals(user.getPresence())) {
                        if (!isValid(user.getTransactionDetails(), 255)) {
                            errorMessage.append(Constant.TRANSACTION_DETAILS).append(Constant._COMMA);
                        }
                        if (!isValid(user.getTransactionSummary(), 40)) {
                            errorMessage.append(Constant.TRANSACTION_SUMMARY).append(Constant._COMMA);
                        }
                    }
                }
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.PRESENCE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        else if (user.getAuthenticationTimeout() != null && user.getAuthenticationTimeout() > Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT))) {
            return Constant.INVALID_AUTHENTICATION_TIMEOUT + Config.getInstance().getProperty(Constant.MAX_TIMEOUT);
        }
        return null;
    }

    public static String isAdminValidForAuthentication(UserAuthenticationTO user) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(user.getUsername())) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isValid(user.getPassword())) {
            errorMessage.append(Constant.PASSWORD).append(Constant._COMMA);
        }
        if (!isValid(user.getAuthenticationType(), 15)) {
            errorMessage.append(Constant.AUTHENTICATION_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    /*public static String isAdminValidForAuthentication( in.fortytwo42.adapter.idc.transferobj.UserAuthenticationTO user) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(user.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isValid(user.getPassword())) {
            errorMessage.append(Constant.PASSWORD).append(Constant._COMMA);
        }
        if (!isValid(user.getAuthenticationType(), 15)) {
            errorMessage.append(Constant.AUTHENTICATION_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }*/

    public static String isAdminValidForCreation(AdminTO adminTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(adminTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (adminTO.getRoles() == null || adminTO.getRoles().size() <= 0) {
            errorMessage.append(Constant.ROLES).append(Constant._COMMA);
        }
        /*if (!isValid(adminTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isAdminValidForDeletion(AdminTO adminTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(adminTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        /*if (!isValid(adminTO.getComments(), 255)) {
            errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        }*/
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isAdminValidForApproval(AdminTO adminTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(adminTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        try {
            if (!isValid(adminTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(adminTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (adminTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(adminTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isApplicationValidForApproval(ApplicationTO applicationTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationTO.getApplicationName(), 50)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        try {
            if (!isValid(applicationTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(applicationTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (applicationTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(applicationTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForUpdate(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(user.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        boolean isUserStatusPresent = false;
        boolean isTwoFactorStatusPresent = false;
        try {
            isUserStatusPresent = isValid(user.getUserStatus()) && UserStatus.valueOf(user.getUserStatus()) != null;
        }
        catch (IllegalArgumentException e) {
            isUserStatusPresent = true;
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            isTwoFactorStatusPresent = isValid(user.getTwoFactorStatus()) && TwoFactorStatus.valueOf(user.getTwoFactorStatus()) != null;
        }
        catch (IllegalArgumentException e) {
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
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForEnablingServices(ConsumerTO consumerTO, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(consumerTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(consumerTO.getConsumerId())) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isUserValidForUpdate(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(user.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        boolean isUserStatusPresent = false;
        boolean isTwoFactorStatusPresent = false;
        try {
            isUserStatusPresent = isValid(user.getUserStatus()) && UserStatus.valueOf(user.getUserStatus()) != null;
        }
        catch (IllegalArgumentException e) {
            isUserStatusPresent = true;
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            isTwoFactorStatusPresent = isValid(user.getTwoFactorStatus()) && TwoFactorStatus.valueOf(user.getTwoFactorStatus()) != null;
        }
        catch (IllegalArgumentException e) {
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

    public static String isUserValidForApproval(UserTO user) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(user.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        try {
            if (!isValid(user.getApprovalStatus()) || TransactionApprovalStatus.valueOf(user.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (user.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(user.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
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
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        try {
            if (isUserStatusValid && UserStatus.valueOf(bulkEditUserTO.getUserStatus()) == null) {
                errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
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
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }

        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isUserValidForCrypto2FA(TwoFactorVerification twoFactorVerification, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(twoFactorVerification.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isValid(twoFactorVerification.getCryptoToken(), 10)) {
            errorMessage.append(Constant.CRYPTO_TOKEN).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForInitiateBinding(ConsumerBindingTO consumerBindingTO, String serviceName, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(serviceName, 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getTransactionId(), 20)) {
            errorMessage.append(Constant.RESOURCE_TRANSACTION_ID).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getTransactionDetails(), 255)) {
            errorMessage.append(Constant.TRANSACTION_DETAILS).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getTransactionSummary(), 40)) {
            errorMessage.append(Constant.TRANSACTION_SUMMARY).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(consumerBindingTO.getConsumerId())) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        try {
            if (consumerBindingTO.getTwoFactorStatus() != null && TwoFactorStatus.valueOf(consumerBindingTO.getTwoFactorStatus()) == null) {
                errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (!isValidIfExist(consumerBindingTO.getFirstName(), 128)) {
            errorMessage.append(Constant.FIRST_NAME).append(Constant._COMMA);
        }
        if (!isValidIfExist(consumerBindingTO.getLastName(), 128)) {
            errorMessage.append(Constant.LAST_NAME).append(Constant._COMMA);
        }
        if (!isValidIfExist(consumerBindingTO.getLocation(), 128)) {
            errorMessage.append(Constant.LOCATION).append(Constant._COMMA);
        }
        if (!isValidIfExist(consumerBindingTO.getEmail(), 128)) {
            errorMessage.append(Constant.EMAIL_CONSTANT).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        else if (consumerBindingTO.getTimeOut() != null && consumerBindingTO.getTimeOut() > Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT))) {
            return Constant.INVALID_TIMEOUT + Config.getInstance().getProperty(Constant.MAX_TIMEOUT);
        }
        return null;

    }

    public static String isConsumerValid(String username, String consumerId, String serviceName, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(serviceName, 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(consumerId)) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        if (!isValid(username, 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValid(String username, String consumerId) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isConsumerIdValid(consumerId)) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        if (!isValid(username, 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValid(String username, String consumerId, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(consumerId)) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        if (!isValid(username, 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForUnbinding(ConsumerBindingTO consumerBindingTO, String serviceName, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(serviceName, 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(consumerBindingTO.getConsumerId())) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForV1Unbinding(ConsumerBindingTO consumerBindingTO, String serviceName, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(serviceName, 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(consumerBindingTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForActivateEncryption(ConsumerTO consumerTO, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(consumerTO.getConsumerId())) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        if (!isValid(consumerTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForGetDecryptionKey(String signTransactionId, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isDataValid(signTransactionId)) {
            errorMessage.append(Constant.SIGN_TRANSACTION_ID).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isADUserValidForBinding(ADUserBindingTO adUserBindingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(adUserBindingTO.getApplicationId(), 15)) {
            errorMessage.append(Constant.RESOURCE_APPLICATION_ID).append(Constant._COMMA);
        }
        if (!isValid(adUserBindingTO.getServiceName(), 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (!isValid(adUserBindingTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForAllADUsersBinding(ADUserBindingTO adUserBindingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(adUserBindingTO.getApplicationId(), 15)) {
            errorMessage.append(Constant.RESOURCE_APPLICATION_ID).append(Constant._COMMA);
        }
        if (!isValid(adUserBindingTO.getServiceName(), 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForCreateApproval(AdapterApprovalAttemptTO approvalAttemptTO, String serviceName, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(serviceName, 50)) {
            errorMessage.append(Constant.SERVICE_NAME).append(Constant._COMMA);
        }
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(approvalAttemptTO.getTransactionId(), 20)) {
            errorMessage.append(Constant.RESOURCE_TRANSACTION_ID).append(Constant._COMMA);
        }
        if (!isValid(approvalAttemptTO.getTransactionDetails(), 255)) {
            errorMessage.append(Constant.TRANSACTION_DETAILS).append(Constant._COMMA);
        }
        if (!isValid(approvalAttemptTO.getTransactionSummary(), 40)) {
            errorMessage.append(Constant.TRANSACTION_SUMMARY).append(Constant._COMMA);
        }
        if (!isConsumerIdValid(approvalAttemptTO.getConsumerId())) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        if (!isValid(approvalAttemptTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (!isValid(approvalAttemptTO.getApprovalAttemptType(), 50)) {
            errorMessage.append(Constant.APPROVAL_ATTEMPT_TYPE).append(Constant._COMMA);
        }
        else if (!approvalAttemptTO.getApprovalAttemptType().equals(IAMConstants.AUTHENTICATION) && !approvalAttemptTO.getApprovalAttemptType().equals(IAMConstants.INFORMATION)
                 && !approvalAttemptTO.getApprovalAttemptType().equals(IAMConstants.NORMAL)
                 && !approvalAttemptTO.getApprovalAttemptType().equals(IAMConstants.SIGN)
                 && !approvalAttemptTO.getApprovalAttemptType().equals(IAMConstants.ENCRYPTION)
                 && !approvalAttemptTO.getApprovalAttemptType().equals(IAMConstants.REGULATORY)) {
            errorMessage.append(Constant.APPROVAL_ATTEMPT_TYPE).append(Constant._COMMA);
        }

        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        else if (approvalAttemptTO.getValidtill() != null && approvalAttemptTO.getValidtill() > Integer.parseInt(Config.getInstance().getProperty(Constant.MAX_TIMEOUT))) {
            return Constant.INVALID_VALIDITY + Config.getInstance().getProperty(Constant.MAX_TIMEOUT);
        }
        return null;
    }

    public static String isValidForGetApprovalStatus(String applicationLabel, String transactionId) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(transactionId, 20)) {
            errorMessage.append(Constant.RESOURCE_TRANSACTION_ID).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForCryptoVerification(CryptoTokenTO cryptoTokenTO, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(cryptoTokenTO.getCryptoToken(), 10)) {
            errorMessage.append(Constant.CRYPTO_TOKEN).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForGetADUser(String username, String applicationLabel) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(applicationLabel, LABEL_LENGTH)) {
            errorMessage.append(Constant.APPLICATION_LABEL).append(Constant._COMMA);
        }
        if (!isValid(username, 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
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
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForAssignGroups(SupervisorUserTO supervisorUserTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(supervisorUserTO.getConsumerId())) {
            errorMessage.append(Constant.COMSUMER_ID).append(Constant._COMMA);
        }
        if (!isValid(supervisorUserTO.getGroupName())) {
            errorMessage.append(Constant.GROUP_NAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidSupervisorGroup(GroupDataTO groupDataTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(groupDataTO.getGroupName())) {
            errorMessage.append(Constant.GROUP_NAME).append(Constant._COMMA);
        }
        if (groupDataTO.getMaximumAllowedHostsPerUser() == null) {
            errorMessage.append(Constant.MAX_HOST_ALLOWED).append(Constant._COMMA);
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
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForUserBinding(UserBindingTO userBindingTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(userBindingTO.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (userBindingTO.getApplication() == null) {
            errorMessage.append(Constant.APPLICATION).append(Constant._COMMA);
        }
        else {
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
        if(userBindingTO.getComments() != null && !userBindingTO.getComments().isEmpty()) {
        	if(!isValidComment(userBindingTO.getComments(), 255)) {
        		errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
        	}
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForUserUpdate(BlockUserApplicationTO user) {
        StringBuilder errorMessage = new StringBuilder();

        if (!isConsumerIdValid(user.getConsumerId())) {
            errorMessage.append(Constant.CONSUMER_ID).append(Constant._COMMA);
        }
        /*
         * if (user.getEnterprises() == null || user.getEnterprises().isEmpty()) {
         * errorMessage.append(Constant.ENTERPRISE).append(Constant._COMMA); }
         */
        try {
            if (!isValid(user.getStatus()) || BindingStatus.valueOf(user.getStatus()) == null) {
                errorMessage.append(Constant.STATUS).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForUserAuditTrailSearch(String searchText) {
        StringBuilder errorMessage = new StringBuilder();
        if (searchText != null && !Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), searchText)) {
            errorMessage.append(Constant.SEARCH_QUERY).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isSearchValidForStateMachineWorkFlow(String searchText) {
        StringBuilder errorMessage = new StringBuilder();
        if (searchText != null && !Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), searchText)) {
            errorMessage.append(Constant.SEARCH_QUERY).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isFileValid(String fileName) {
        if (!isValid(fileName)) {
            return errorConstant.getERROR_MESSAGE_INVALID_ESC_FILE_NAME();
        }
        if (!Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), fileName)) {
            return errorConstant.getERROR_MESSAGE_INVALID_ESC_FILE_NAME();
        }
        if (fileName.contains(".") && fileName.lastIndexOf(".") != 0) {
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (!Constant.IAMCIX.equalsIgnoreCase(fileExtension)) {
                return errorConstant.getERROR_MESSAGE_INVALID_FILE_TYPE();
            }
        }
        else {
            return errorConstant.getERROR_MESSAGE_INVALID_FILE_TYPE();
        }
        return null;
    }

    public static String isAdminValidForLogout(UserAuthenticationTO user) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(user.getUsername(), 64)) {
            errorMessage.append(Constant.USERNAME).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }


    public static String isValidForApproveRequest(RequestTO requestTO) {
        StringBuilder errorMessage = new StringBuilder();
        try {
            if (!isValid(requestTO.getApprovalStatus()) || TransactionApprovalStatus.valueOf(requestTO.getApprovalStatus()) == null) {
                errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
            }
            /*else if (requestTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(requestTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if(requestTO.getComments() != null && !requestTO.getComments().isEmpty()) {
        	if (!isValidComment(requestTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForGetGroup(String groupFetchType, String applicationType, String applicationName) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(groupFetchType) || (!groupFetchType.equals(Constant.INTERNAL) && !groupFetchType.equals(Constant.EXTERNAL))) {
            errorMessage.append(Constant.GROUP_FETCH_TYPE).append(Constant._COMMA);
        }
        if (!isValid(applicationType, 64)) {
            errorMessage.append(Constant.APPLICATION_TYPE).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }


    public static boolean checkForDuplicateCheckers(List<GroupCheckerTO> checkers) {
        boolean flag = true;
        Set<String> checkerList = new HashSet<String>();
        if (checkers != null && !checkers.isEmpty()) {
            for (GroupCheckerTO groupCheckerTO : checkers) {
                flag = checkerList.add(groupCheckerTO.getMobile());
                if (!flag) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String isDataValidForGetApplicationLabels(String applicationId, String searchQuery, String updateStatus) {
        StringBuilder errorMessage = new StringBuilder();
        if (!Constant.PENDING.equalsIgnoreCase(updateStatus) && !isValid(applicationId, 15)) {
            errorMessage.append(Constant.APPLICATION_ID).append(Constant._COMMA);
        }
        if (searchQuery != null && !Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), searchQuery)) {
            errorMessage.append(Constant.SEARCH_QUERY).append(Constant._COMMA);
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
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        try {
            if (userApplicationRelTO.getBindingStatus() == null || BindingStatus.valueOf(userApplicationRelTO.getBindingStatus()) == null) {
                errorMessage.append(Constant.BINDING_STATUS).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.BINDING_STATUS).append(Constant._COMMA);
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
            /*else if (userApplicationRelTO.getApprovalStatus().equals(TransactionApprovalStatus.REJECTED.name())) {
                if (!isValid(userApplicationRelTO.getComments(), 255)) {
                    errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
                }
            }*/
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.APPROVAL_STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isCredentialsValid(String userName, String password) {
        try {
            if (!Constant.DEFAULT_USERNAME.equals(userName) || password == null
                || password.trim().isEmpty()
                || !Constant.DEFAULT_PASSWORD.equals(AES128Impl.decryptData(password, KeyManagementUtil.getAESKey()))) {
                return errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID();
            }
        }
        catch (Exception e) {
            return errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID();

        }
        return null;
    }

    public static String isValidCSV(String fileName) {
        if (!isValid(fileName)) {
            return errorConstant.getERROR_MESSAGE_INVALID_ESC_FILE_NAME();
        }
        if (!Pattern.matches(Config.getInstance().getProperty(Constant.CSV_FILE_NAME_PATTERN), fileName)) {
            return errorConstant.getERROR_MESSAGE_INVALID_FILE_TYPE();
        }
        return null;
    }

    public static String isValidBulkUploadType(String fileName, String type){
        StringBuilder errorMessage = new StringBuilder();
        if(fileName != null) {
            String error1 = isValidCSV(fileName);
            if (error1 != null) {
                errorMessage.append(error1).append(Constant._COMMA);
            }
        }
        boolean isBulkTypePresent = false;
        for(BulkUploadType bulkUploadType : BulkUploadType.values()){
            if(bulkUploadType.name().equalsIgnoreCase(type)){
                isBulkTypePresent = true;
            }
        }
        if(!isBulkTypePresent){
            errorMessage.append(errorConstant.getERROR_MESSAGE_INVALID_BULK_UPLOAD_TYPE()).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return (String) errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isConsumerValidForUserUpdateV3(in.fortytwo42.adapter.transferobj.BlockUserApplicationTO user) {
        StringBuilder errorMessage = new StringBuilder();

        if (!isConsumerIdValid(user.getAccountId())) {
            errorMessage.append(Constant.ACCOUNT_ID).append(Constant._COMMA);
        }
        try {
            if (!isValid(user.getStatus()) || BindingStatus.valueOf(user.getStatus()) == null) {
                errorMessage.append(Constant.STATUS).append(Constant._COMMA);
            }
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(Constant.STATUS).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isDataValidForUserDetails(String accountId) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(accountId)) {
            errorMessage.append(Constant.ACCOUNT_ID).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForGetAuthAttemptAuditTrail(String applicationId, String searchQuery) {
        StringBuilder errorMessage = new StringBuilder();
        if (applicationId != null && !applicationId.trim().isEmpty() && !isValid(applicationId, 15)) {
            errorMessage.append(Constant.APPLICATION_ID).append(Constant._COMMA);
        }
        if (searchQuery != null && !Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), searchQuery)) {
            errorMessage.append(Constant.SEARCH_QUERY).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForUserGroupCreate(UserGroupTO userGroupTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (!isValid(userGroupTO.getGroupname(), 64)) {
            errorMessage.append(Constant.GROUP_NAME).append(Constant._COMMA);
        }
        boolean isUserStatusPresent = false;
        boolean isTwoFactorStatusPresent = false;
        try {
            isUserStatusPresent = isValid(userGroupTO.getUserStatus()) && UserStatus.valueOf(userGroupTO.getUserStatus()) != null;
        }
        catch (IllegalArgumentException e) {
            isUserStatusPresent = true;
            errorMessage.append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        try {
            isTwoFactorStatusPresent = isValid(userGroupTO.getTwoFactorStatus()) && TwoFactorStatus.valueOf(userGroupTO.getTwoFactorStatus()) != null;
        }
        catch (IllegalArgumentException e) {
            isTwoFactorStatusPresent = true;
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA);
        }
        if (!isUserStatusPresent && !isTwoFactorStatusPresent) {
            errorMessage.append(Constant.TWO_FACTOR_STATUS).append(Constant._COMMA).append(Constant.USER_STATUS).append(Constant._COMMA);
        }
        if(userGroupTO.getComments() != null && !userGroupTO.getComments().isEmpty()) {
        	if (!isValidComment(userGroupTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForUserUserGroupMapping(UserGroupTO userGroupTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (userGroupTO.getUsers() == null && userGroupTO.getUsers().isEmpty()) {
            errorMessage.append(Constant.USER_LIST).append(Constant._COMMA);
        }
        if (!isValid(userGroupTO.getGroupname(), 64)) {
            errorMessage.append(Constant.GROUP_NAME).append(Constant._COMMA);
        }
        if(userGroupTO.getComments() != null && !userGroupTO.getComments().isEmpty()) {
        	if (!isValidComment(userGroupTO.getComments(), 255)) {
                errorMessage.append(Constant.COMMENTS).append(Constant._COMMA);
            }
        }
        if (errorMessage.length() > 0) {
            return Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

    public static String isValidForUserGroupApplicationMapping(UserGroupTO userGroupTO) {
        StringBuilder errorMessage = new StringBuilder();
        if (userGroupTO.getApplications() == null && userGroupTO.getApplications().isEmpty()) {
            errorMessage.append(Constant.APPLICATION_LIST).append(Constant._COMMA);
        }
        if (!isValid(userGroupTO.getGroupname(), 64)) {
            errorMessage.append(Constant.GROUP_NAME).append(Constant._COMMA);
        }
        if(userGroupTO.getComments() != null && !userGroupTO.getComments().isEmpty()) {
        	if (!isValidComment(userGroupTO.getComments(), 255)) {
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
        return comments != null && !comments.trim().isEmpty()&& comments.length() <= maxAllowedLength&& Pattern.matches(Config.getInstance().getProperty(Constant.COMMENT_VALIDATION_PATTERN),comments);     
                  
    }

    public static String validateCsvFileNameAndType(String fileName, String fileType) {
        StringBuilder errorMessage = new StringBuilder();
        String error = isValidCSV(fileName);
        if (error != null) {
            errorMessage.append(error).append(Constant._COMMA);
        }
        try {
            BulkUploadType.valueOf(fileType);
        }
        catch (IllegalArgumentException e) {
            errorMessage.append(errorConstant.getERROR_MESSAGE_INVALID_FILE_TYPE()).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            return (String) errorMessage.toString().subSequence(0, errorMessage.length() - 1);
        }
        return null;
    }

}
