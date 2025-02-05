package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.util.ConfigError;
import in.fortytwo42.adapter.util.Constant;

public class ErrorConstantsFromConfig implements ErrorConstantsFromConfigIntf {

    private ErrorConstantsFromConfig() {
        super();
    }

    private static final class InstanceHolder {
        private static final ErrorConstantsFromConfig INSTANCE = new ErrorConstantsFromConfig();

        private InstanceHolder() {
            super();
        }
    }

    public static ErrorConstantsFromConfig getInstance() {
        return InstanceHolder.INSTANCE;
    }


    private final Long ERROR_CODE_APPLICATION_NOT_FOUND = Long.valueOf(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPLICATION_NOT_FOUND));
    private final Long ERROR_CODE_INVALID_SERACH_ATTRIBUTE = Long.valueOf(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_SERACH_ATTRIBUTE));
    private final Long ERROR_CODE_APPLICATION_ALREADY_PRESENT = Long.valueOf(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPLICATION_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_UPDATE_PENDING = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_UPDATE_PENDING));
    private final long ERROR_CODE_INVALID_TOKEN = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_TOKEN));
    private final long ERROR_CODE_INVALID_ROLES = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_ROLES));
    private final long ERROR_CODE_PERMISSION_DENIED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_PERMISSION_DENIED));
    private final long ERROR_CODE_IO_EXCEPTION = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_IO_EXCEPTION));
    private final long ERROR_CODE_INVALID_APPLICATION_ID_OR_PASSWORD = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_APPLICATION_ID_OR_PASSWORD));
    private final long ERROR_CODE_USER_SERVICE_BINDING_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_BINDING_FAILED));
    private final long ERROR_CODE_INVALID_MOBILE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_MOBILE));
    private final long ERROR_CODE_INVALID_APPLICATION_FOR_USER = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_APPLICATION_FOR_USER));
    private final long ERROR_CODE_CERTIFICATE_PINNING_FAILURE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_CERTIFICATE_PINNING_FAILURE));
    private final long ERROR_CODE_SERVER_ERROR = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SERVER_ERROR));
    private final long ERROR_CODE_ACCOUNT_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ACCOUNT_NOT_FOUND));
    private final long ERROR_CODE_IAMCI2_CALL_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_IAMCI2_CALL_FAILED));
    private final long ERROR_CODE_LOOKUP_ID_COMPULSORY = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_LOOKUP_ID_COMPULSORY));
    private final long ERROR_CODE_APPROVAL_ATTEMPT_TYPE_INVALID = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPROVAL_ATTEMPT_TYPE_INVALID));
    private final long ERROR_CODE_INVALID_CONSUMER_ID = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_CONSUMER_ID));
    private final long ERROR_CODE_APPROVAL_ATTEMPT_NOT_APPROVED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPROVAL_ATTEMPT_NOT_APPROVED));
    private final long ERROR_CODE_INVALID_APPLICATION_ID = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_APPLICATION_ID));
    private final long ERROR_CODE_INVALID_CLIENT_ID = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_CLIENT_ID));
    private final long ERROR_CODE_INVALID_DATA = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_DATA));
    private final long ERROR_CODE_USER_BLOCK = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_BLOCK));
    private final long ERROR_CODE_CONSUMER_NOT_PRESENT_DATA = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_CONSUMER_NOT_PRESENT_DATA));
    private final long ERROR_CODE_SERVICE_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SERVICE_NOT_FOUND));
    private final long ERROR_CODE_INVALID_SERVICE_FOR_APPLICATION = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_SERVICE_FOR_APPLICATION));
    private final long ERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND));
    private final long ERROR_CODE_USER_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_NOT_FOUND));
    private final long ERROR_CODE_TRANSACTION_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_TRANSACTION_NOT_FOUND));
    private final long ERROR_CODE_INVALID_TRANSACTION_TYPE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_TRANSACTION_TYPE));
    private final long ERROR_CODE_VERIFICATION_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_VERIFICATION_FAILED));
    private final long ERROR_CODE_USER_NAME_PASSWORD_INVALID = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_NAME_PASSWORD_INVALID));
    private final long ERROR_CODE_USER_NAME_TOKEN_INVALID = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_NAME_TOKEN_INVALID));
    private final long ERROR_CODE_INVALID_APPLICATION_TYPE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_APPLICATION_TYPE));
    private final long ERROR_CODE_BLOCKED_FOR_RESET_PIN = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_BLOCKED_FOR_RESET_PIN));
    private final long ERROR_CODE_RESET_PIN_COMPLETED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_RESET_PIN_COMPLETED));
    private final long ERROR_CODE_USER_SERVICE_BINDING_NOT_ACTIVE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_BINDING_NOT_ACTIVE));
    private final long ERROR_CODE_USER_SERVICE_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_SERVICE_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_NOT_FOUND));
    private final long ERROR_CODE_USER_DATA_EMPTY = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_DATA_EMPTY));
    private final long ERROR_CODE_REQUEST_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_REQUEST_NOT_FOUND));
    private final long ERROR_CODE_INVALID_APPLICATION_NAME = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_APPLICATION_NAME));
    private final long ERROR_CODE_USER_SERVICE_BINDING_BLOCKED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_BINDING_BLOCKED));
    private final long ERROR_CODE_APPLICATION_PASSWORD = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPLICATION_PASSWORD));
    private final long ERROR_CODE_SERVICES_ALREADY_ENABLED_FOR_USER = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SERVICES_ALREADY_ENABLED_FOR_USER));
    private final long ERROR_CODE_USER_APPLICATION_BINDING_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_APPLICATION_BINDING_NOT_FOUND));
    private final long ERROR_CODE_ATTRIBUTE_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_NOT_FOUND));
    private final long ERROR_CODE_EVIDENCE_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_EVIDENCE_NOT_FOUND));
    private final long ERROR_CODE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND));
    private final long ERROR_CODE_ATTRIBUTE_ADDITION_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_ADDITION_ALREADY_PRESENT));
    private final long ERROR_CODE_INVALID_REQUEST = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_REQUEST));
    private final long ERROR_CODE_ATTRIBUTE_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_ALREADY_PRESENT));
    private final long ERROR_CODE_ALREADY_APPROVED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ALREADY_APPROVED));
    private final long ERROR_CODE_ATTRIBUTE_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_REQUEST_IS_TIMEOUT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_REQUEST_IS_TIMEOUT));
    private final long ERROR_CODE_ATTRIBUTE_UPDATION_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_UPDATION_ALREADY_PRESENT));
    private final long ERROR_CODE_ATTRIBUTE_DELETION_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_DELETION_ALREADY_PRESENT));
    private final long ERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME));
    private final long ERROR_CODE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_REQUEST_ALREADY_SENT_TO_CHECKER = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_REQUEST_ALREADY_SENT_TO_CHECKER));
    private final long ERROR_CODE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER));
    ;
    private final long ERROR_CODE_ATTRIBUTE_ADDITION_ID_INVALID = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_ADDITION_ID_INVALID));
    ;
    private final long ERROR_CODE_INVALID_ATTRIBUTE_TYPE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_ATTRIBUTE_TYPE));
    private final long ERROR_CODE_INVALID_ATTRIBUTE_NAME = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_ATTRIBUTE_NAME));
    private final long ERROR_CODE_FILE_IS_EMPTY = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_FILE_IS_EMPTY));
    private final long ERROR_CODE_FILE_NOT_SUPPORTED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_FILE_NOT_SUPPORTED));
    private final long ERROR_CODE_APPLICATION_ONBOARD_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPLICATION_ONBOARD_ALREADY_PRESENT));
    private final long ERROR_CODE_EDIT_ACCOUNT_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_EDIT_ACCOUNT_FAILED));
    private final long ERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE));
    private final long ERROR_CODE_EVIDENCE_REQUEST_ALREADY_REJECTED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_EVIDENCE_REQUEST_ALREADY_REJECTED));
    private final long ERROR_CODE_APPLICATION_EDIT_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPLICATION_EDIT_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_ONBOARD_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_ONBOARD_ALREADY_PRESENT));
    private final long ERROR_CODE_CSV_DOWNLOAD_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_CSV_DOWNLOAD_FAILED));
    private final long ERROR_CODE_USER_SESSION_IS_ALREADY_ACTIVE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SESSION_IS_ALREADY_ACTIVE));
    private final long ERROR_CODE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND));
    private final long ERROR_CODE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT =
            Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT =
            Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT =
            Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND));
    private final long ERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND =
            Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND));
    private final long ERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND));
    private final long ERROR_CODE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT));
    private final long ERROR_CODE_ENTERPRISE_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ENTERPRISE_NOT_FOUND));
    private final long ERROR_CODE_SRA_GATEWAY_SETTING_ALREADY_BINDED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_GATEWAY_SETTING_ALREADY_BINDED));
    private final long ERROR_CODE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_POLICY_EDIT_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_POLICY_EDIT_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_GROUP_ALREADY_EXISTS = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_ALREADY_EXISTS));
    private final long ERROR_CODE_USER_GROUP_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_NOT_FOUND));
    private final long ERROR_CODE_USER_GROUP_DATA_SAME = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_DATA_SAME));
    private final long ERROR_CODE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_GROUP_BINDING_ALREADY_EXISTS = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_BINDING_ALREADY_EXISTS));
    private final long ERROR_CODE_USER_GROUP_BINDING_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_BINDING_NOT_FOUND));
    private final long ERROR_CODE_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS =
            Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS));
    private final long ERROR_CODE_USER_GROUP_APPLICATION_BINDING_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_APPLICATION_BINDING_NOT_FOUND));
    private final long ERROR_CODE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT =
            Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_APPLICATION_ALREDY_BINDED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPLICATION_ALREDY_BINDED));
    private final long ERROR_CODE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT =
            Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT =
            Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_CONTACT_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_CONTACT_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_MAPPER_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_MAPPER_NOT_FOUND));
    private final long ERROR_CODE_AD_SYNC_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_AD_SYNC_FAILED));
    private final long ERROR_CODE_SRA_DETAILS_NOT_MATCHED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SRA_DETAILS_NOT_MATCHED));
    private final long ERROR_CODE_INVALID_FILE_NAME = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_FILE_NAME));
    private final long ERROR_CODE_FILE_READ_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_FILE_READ_FAILED));
    private final long ERROR_CODE_LICENSE_CHECK_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_LICENSE_CHECK_FAILED));
    private final long ERROR_CODE_INPROGRESS = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INPROGRESS));
    private final long ERROR_CODE_ENTERPRISE_ALREADY_ONBOARDED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ENTERPRISE_ALREADY_ONBOARDED));
    private final long ERROR_CODE_ENTERPRISE_NOT_ONBOARDED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ENTERPRISE_NOT_ONBOARDED));
    private final long ERROR_CODE_BINDING_FAILED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_BINDING_FAILED));
    private final long ERROR_CODE_NOTIFICATION_TYPE_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_NOTIFICATION_TYPE_NOT_FOUND));
    private final long ERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_USER = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_USER));
    private final long ERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION));
    private final long ERROR_CODE_INVALID_CONNECTION_SETTINGS = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_CONNECTION_SETTINGS));
    private final long ERROR_CODE_ADFS_DETAIL_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ADFS_DETAIL_NOT_FOUND));
    private final long ERROR_CODE_AD_DETAIL_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_AD_DETAIL_NOT_FOUND));
    private final long ERROR_CODE_SAME_MAKER_REQUEST_APPROVED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_SAME_MAKER_REQUEST_APPROVED));
    private final long ERROR_CODE_INVALID_USERID_PASSWORD = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_USERID_PASSWORD));
    private final long ERROR_CODE_USER_CREDENTIALS_NOT_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_CREDENTIALS_NOT_PRESENT));
    private final long ERROR_CODE_EVIDENCE_IS_REQUIRED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_EVIDENCE_IS_REQUIRED));
    private final long ERROR_CODE_INVALID_TYPE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_TYPE));
    private final long ERROR_CODE_APPLICATION_NOT_ACTIVE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_APPLICATION_NOT_ACTIVE));
    private final long ERROR_CODE_PARSING_CSV = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_PARSING_CSV));
    private final long ERROR_CODE_REQUEST_NOT_PENDING = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_REQUEST_NOT_PENDING));
    private final long ERROR_CODE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT));
    private final long ERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND));
    private final long ERROR_CODE_VERIFIER_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_VERIFIER_NOT_FOUND));
    private final long ERROR_CODE_EVIDENCE_REQUEST_ALREADY_APPROVED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_EVIDENCE_REQUEST_ALREADY_APPROVED));
    private final long ERROR_CODE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_ATTRIBUTE_NOT_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ATTRIBUTE_NOT_PRESENT));
    private final long ALREADY_PRESENT_IN_SYSTEM_CODE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ALREADY_PRESENT_IN_SYSTEM_CODE));
    private final long VALIDATION_ERROR_CODE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.VALIDATION_ERROR_CODE));
    private final long USER_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.USER_NOT_FOUND));

    private final long ERROR_CODE_INTERNAL_SERVER_ERROR = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INTERNAL_SERVER_ERROR));
    private final long ERROR_CODE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT));
    private final long ERROR_CODE_INVALID_ACCOUNT_ID = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_ACCOUNT_ID));
    private final long ERROR_CODE_INVALID_SEARCH_ATTRIBUTE = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_SEARCH_ATTRIBUTE));
    private final long ERROR_CODE_GENERATE_RUNNING_HASH_FAILED= Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_GENERATE_RUNNING_HASH_FAILED));
    private final long ERROR_CODE_VERIFY_RUNNING_HASH_FAILED= Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_VERIFY_RUNNING_HASH_FAILED));
    private final long ERROR_CODE_MULTIPLE_IDENTITIES_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_MULTIPLE_IDENTITIES_FOUND));
    private final long ERROR_CODE_DISABLE_USER_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_DISABLE_USER_ALREADY_PRESENT));
    private final long ERROR_CODE_USER_LOGIN_TIME_EXPIRED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_USER_LOGIN_TIME_EXPIRED));

    private final long ERROR_CODE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT));

    private final long ERROR_CODE_TEMPLATE_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_TEMPLATE_NOT_FOUND));

    //error messages
    private final String ERROR_MESSAGE_INVALID_DATA = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_DATA);
    private final String ERROR_MESSAGE_TRANSACTION_ALREADY_EXISTS = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_TRANSACTION_ALREADY_EXISTS);
    private final String ERROR_MESSAGE_USER_BLOCK = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_BLOCK);
    private final String ERROR_MESSAGE_CONSUMER_NOT_PRESENT_DATA = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_CONSUMER_NOT_PRESENT_DATA);
    private final String ERROR_MESSAGE_SERVICE_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SERVICE_NOT_FOUND);
    private final String ERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION);
    private final String ERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND);
    private final String ERROR_MESSAGE_USER_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_NOT_FOUND);
    private final String ERROR_MESSAGE_TRANSACTION_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_TRANSACTION_NOT_FOUND);
    private final String ERROR_MESSAGE_INVALID_TRANSACTION_TYPE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_TRANSACTION_TYPE);
    private final String ERROR_MESSAGE_VERIFICATION_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_VERIFICATION_FAILED);
    private final String ERROR_MESSAGE_USER_NAME_PASSWORD_INVALID = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_NAME_PASSWORD_INVALID);
    private final String ERROR_MESSAGE_INVALID_APPLICATION_TYPE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_APPLICATION_TYPE);
    private final String ERROR_MESSAGE_BLOCKED_FOR_RESET_PIN = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_BLOCKED_FOR_RESET_PIN);
    private final String ERROR_MESSAGE_RESET_PIN_COMPLETED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_RESET_PIN_COMPLETED);
    private final String ERROR_MESSAGE_USER_SERVICE_BINDING_NOT_ACTIVE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_BINDING_NOT_ACTIVE);
    private final String ERROR_MESSAGE_USER_SERVICE_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_USER_SERVICE_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_NOT_FOUND);
    private final String ERROR_MESSAGE_USER_DATA_EMPTY = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_DATA_EMPTY);
    private final String ERROR_MESSAGE_REQUEST_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_REQUEST_NOT_FOUND);
    private final String ERROR_MESSAGE_INVALID_APPLICATION_NAME = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_APPLICATION_NAME);
    private final String ERROR_MESSAGE_USER_SERVICE_BINDING_BLOCKED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_BINDING_BLOCKED);
    private final String ERROR_MESSAGE_APPLICATION_PASSWORD = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPLICATION_PASSWORD);
    private final String ERROR_MESSAGE_SERVICES_ALREADY_ENABLED_FOR_USER = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SERVICES_ALREADY_ENABLED_FOR_USER);
    private final String ERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND);
    private final String ERROR_MESSAGE_ATTRIBUTE_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_NOT_FOUND);
    private final String ERROR_MESSAGE_EVIDENCE_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EVIDENCE_NOT_FOUND);
    private final String ERROR_MESSAGE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND);
    private final String ERROR_MESSAGE_ATTRIBUTE_ADDITION_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_ADDITION_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_INVALID_REQUEST = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_REQUEST);
    private final String ERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ATTRIBUTE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_REQUEST_IS_TIMEOUT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_REQUEST_IS_TIMEOUT);
    private final String ERROR_MESSAGE_ATTRIBUTE_UPDATION_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_UPDATION_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ATTRIBUTE_DELETION_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_DELETION_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME);
    private final String ERROR_MESSAGE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_REQUEST_ALREADY_SENT_TO_CHECKER = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_REQUEST_ALREADY_SENT_TO_CHECKER);
    private final String ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER);
    private final String ERROR_MESSAGE_ATTRIBUTE_ADDITION_ID_INVALID = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_ADDITION_ID_INVALID);
    private final String ERROR_MESSAGE_INVALID_ATTRIBUTE_TYPE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_ATTRIBUTE_TYPE);
    private final String ERROR_MESSAGE_INVALID_ATTRIBUTE_NAME = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_ATTRIBUTE_NAME);
    private final String ERROR_MESSAGE_FILE_IS_EMPTY = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_FILE_IS_EMPTY);
    private final String ERROR_MESSAGE_APPLICATION_ONBOARD_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPLICATION_ONBOARD_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_EDIT_ACCOUNT_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EDIT_ACCOUNT_FAILED);
    private final String ERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE);
    private final String ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_REJECTED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_REJECTED);
    private final String ERROR_MESSAGE_APPLICATION_EDIT_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPLICATION_EDIT_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_USER_ONBOARD_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_ONBOARD_ALREADY_PRESENT);
    private final String ERROR_DEVELOPER_CSV_DOWNLOAD_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_DEVELOPER_CSV_DOWNLOAD_FAILED);
    private final String ERROR_MESSAGE_USER_SESSION_IS_ALREADY_ACTIVE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SESSION_IS_ALREADY_ACTIVE);
    private final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND);
    private final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND);
    private final String ERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND);
    private final String ERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND);
    private final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ENTERPRISE_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ENTERPRISE_NOT_FOUND);
    private final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_ALREADY_BINDED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_GATEWAY_SETTING_ALREADY_BINDED);
    private final String ERROR_MESSAGE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_POLICY_EDIT_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_POLICY_EDIT_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_USER_GROUP_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_GROUP_NOT_FOUND);
    private final String ERROR_MESSAGE_USER_GROUP_DATA_SAME = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_GROUP_DATA_SAME);
    private final String ERROR_MESSAGE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_CONTACT_ONBOARD_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_CONTACT_ONBOARD_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_MAPPER_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_MAPPER_NOT_FOUND);
    private final String ERROR_MESSAGE_SRA_DETAILS_NOT_MATCHED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SRA_DETAILS_NOT_MATCHED);
    private final String ERROR_MESSAGE_INVALID_FILE_NAME = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_FILE_NAME);
    private final String ERROR_MESSAGE_INPROGRESS = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INPROGRESS);
    private final String ERROR_MESSAGE_ENTERPRISE_ALREADY_ONBOARDED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ENTERPRISE_ALREADY_ONBOARDED);
    private final String ERROR_MESSAGE_NOTIFICATION_TYPE_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_NOTIFICATION_TYPE_NOT_FOUND);
    private final String ERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_USER = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_USER);
    private final String ERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION);
    private final String ERROR_MESSAGE_INVALID_CONNECTION_SETTINGS = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_CONNECTION_SETTINGS);
    private final String ERROR_MESSAGE_ADFS_DETAIL_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ADFS_DETAIL_NOT_FOUND);
    private final String ERROR_MESSAGE_AD_DETAIL_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_AD_DETAIL_NOT_FOUND);
    private final String ERROR_MESSAGE_SAME_MAKER_REQUEST_APPROVED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SAME_MAKER_REQUEST_APPROVED);
    private final String ERROR_MESSAGE_INVALID_USERID_PASSWORD = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_USERID_PASSWORD);
    private final String ERROR_MESSAGE_INVALID_TYPE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_TYPE);
    private final String ERROR_MESSAGE_PARSING_CSV = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_PARSING_CSV);
    private final String ERROR_MESSAGE_REQUEST_NOT_PENDING = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_REQUEST_NOT_PENDING);
    private final String ERROR_MESSAGE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND);
    private final String ERROR_MESSAGE_VERIFIER_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_VERIFIER_NOT_FOUND);
    private final String ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_APPROVED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_APPROVED);
    private final String ERROR_MESSAGE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT);
    private final String ERROR_MESSAGE_INTERNAL_SERVER_ERROR = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INTERNAL_SERVER_ERROR);
    private final String ERROR_MESSAGE_USER_BINDING_ALREDY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_BINDING_ALREDY_PRESENT);
    private final String ERROR_MESSAGE_INVALID_DATA_OTP = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_DATA_OTP);
    private final String ERROR_DEV_MESSAGE_INVALID_DATA = ConfigError.getInstance().getProperty(Constant.ERROR_DEV_MESSAGE_INVALID_DATA);
    private final String HUMANIZED_RESET_ENTERPRISE_PIN_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_RESET_ENTERPRISE_PIN_FAILED);
    private final long ERROR_CODE_INVALID_FALLOUT_OPERATION = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_FALLOUT_OPERATION));
    private final String ERROR_DEV_MESSAGE_INVALID_TRANSACTION_ID = ConfigError.getInstance().getProperty(Constant.ERROR_DEV_MESSAGE_INVALID_TRANSACTION_ID);
    private final String ERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID = ConfigError.getInstance().getProperty(Constant.ERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID);
    private final String ERROR_DEV_MESSAGE_INVALID_STATUS = ConfigError.getInstance().getProperty(Constant.ERROR_DEV_MESSAGE_INVALID_STATUS);
    private final String ERROR_DEV_MESSAGE_INVALID_TOKEN = ConfigError.getInstance().getProperty(Constant.ERROR_DEV_MESSAGE_INVALID_TOKEN);
    private final String ERROR_MESSAGE_USER_PROVIDED_INVALID_CLIENT_ID_OR_SECRET = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_PROVIDED_INVALID_CLIENT_ID_OR_SECRET);
    private final String ERROR_MESSAGE_INVALID_ESC_FILE_NAME = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_ESC_FILE_NAME);
    private final String ERROR_MESSAGE_INVALID_FILE_TYPE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_FILE_TYPE);
    private final String ERROR_MESSAGE_ATTRIBUTE_PASSWORD = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_PASSWORD);
    private final String ERROR_MESSAGE_USER_PASSWORD = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_PASSWORD);
    private final String ERROR_MESSAGE_UPDATE_APPROVAL_ATTEMPT_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_UPDATE_APPROVAL_ATTEMPT_FAILED);
    private final String ERROR_MESSAGE_INVALID_VALUE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_VALUE);
    private final String ERROR_MESSAGE_EMPTY_CSV = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EMPTY_CSV);
    private final String ERROR_MESSAGE_NOT_SUPPORTED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_NOT_SUPPORTED);
    private final String ERROR_MESSAGE_EVIDENCE_EXPORT_EVIDENCE_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EVIDENCE_EXPORT_EVIDENCE_FAILED);
    private final String ERROR_MESSAGE_EDIT_CREDENTIAL_FALIED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EDIT_CREDENTIAL_FALIED);
    private final String ERROR_MESSAGE_APPLICATION_ALREADY_BINDED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPLICATION_ALREADY_BINDED);
    private final String ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_EXPIRED_LICENSE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_EXPIRED_LICENSE);
    private final String ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_APPLICATION_EXCEEDED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_APPLICATION_EXCEEDED);
    private final String ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_USERS_EXCEEDED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_USERS_EXCEEDED);
    private final String ERROR_MESSAGE_ENTERPRISE_NOT_ONBOARDED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ENTERPRISE_NOT_ONBOARDED);
    private final String ERROR_MESSAGE_HOTP_GENERATION_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_HOTP_GENERATION_FAILED);
    private final String ERROR_MESSAGE_HOTP_VALIDATION_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_HOTP_VALIDATION_FAILED);
    private final long ERROR_CODE_FALLOUT_INVALID_DATA = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_FALLOUT_INVALID_DATA));
    private final String ERROR_MESSAGE_FALLOUT_INVALID_DATA = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_FALLOUT_INVALID_DATA);
    private final String ERROR_MESSAGE_CONSENT_REQUIRED_NOT_ALLOWED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_CONSENT_REQUIRED_NOT_ALLOWED);

    private final String ERROR_MESSAGE_TEMPLATE_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_TEMPLATE_NOT_FOUND);

    private final long ERROR_CODE_CONSENT_REQUIRED_NOT_ALLOWED = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_CONSENT_REQUIRED_NOT_ALLOWED));
    private final String HUMANIZED_CHANGE_ENTERPRISE_PIN_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_CHANGE_ENTERPRISE_PIN_FAILED);
    private final String HUMANIZED_CHANGE_APPLICATION_PIN_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_CHANGE_APPLICATION_PIN_FAILED);
    private final String UPDATE_PASSWORD_FAILED = ConfigError.getInstance().getProperty(Constant.UPDATE_PASSWORD_FAILED);
    private final String HUMANIZED_MESSAGE_AUTHENTICATION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_MESSAGE_AUTHENTICATION_FAILED);
    private final String HUMANIZED_AUTHENTICATION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_AUTHENTICATION_FAILED);
    private final String HUMANIZED_GET_ADMIN_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_ADMIN_FAILED);
    private final String HUMANIZED_GET_USERS_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_USERS_FAILED);
    private final String HUMANIZED_GET_USER_STATUS_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_USER_STATUS_FAILED);
    private final String HUMANIZED_GET_SERVICES_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_SERVICES_FAILED);
    private final String HUMANIZED_GET_APPLICATION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_APPLICATION_FAILED);
    private final String HUMANIZED_GET_APPROVAL_ATTEMPT_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_APPROVAL_ATTEMPT_FAILED);
    private final String HUMANIZED_USER_UDATE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_UDATE_FAILED);
    private final String HUMANIZED_USER_ONBOARD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_ONBOARD_FAILED);
    private final String HUMANIZED_CHANGE_PASSWORD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_CHANGE_PASSWORD_FAILED);
    private final String HUMANIZED_POLICY_ONBOARD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_POLICY_ONBOARD_FAILED);
    private final String HUMANIZED_CONTACT_ONBOARD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_CONTACT_ONBOARD_FAILED);
    private final String HUMANIZED_GET_POLICY_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_POLICY_FAILED);
    private final String HUMANIZED_GET_CONTACT_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_CONTACT_FAILED);
    private final String HUMANIZED_POLICY_EDIT_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_POLICY_EDIT_FAILED);
    private final String HUMANIZED_CONTACT_EDIT_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_CONTACT_EDIT_FAILED);
    private final String HUMANIZED_USER_APPROVAL_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_APPROVAL_FAILED);
    private final String HUMANIZED_USER_BINDING_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_BINDING_FAILED);
    private final String HUMANIZED_USER_UNBINDING_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_UNBINDING_FAILED);
    private final String HUMANIZED_FILE_UPLOAD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_FILE_UPLOAD_FAILED);
    private final String HUMANIZED_APPLICATION_SECRET_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_APPLICATION_SECRET_FAILED);
    private final String HUMANIZED_GET_DECRYPTION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_DECRYPTION_FAILED);
    private final String HUMANIZED_APPROVAL_ATTEMPT_UPDATE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_APPROVAL_ATTEMPT_UPDATE_FAILED);
    private final String HUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED);
    private final String HUMANIZED_APPLICATION_DELETION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_APPLICATION_DELETION_FAILED);
    private final String HUMANIZED_GET_USER_SUBSCRIPTION = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_USER_SUBSCRIPTION);
    private final String HUMANIZED_UNBIND_SERVICES = ConfigError.getInstance().getProperty(Constant.HUMANIZED_UNBIND_SERVICES);
    private final String HUMANIZED_BIND_SERVICES = ConfigError.getInstance().getProperty(Constant.HUMANIZED_BIND_SERVICES);
    private final String HUMANIZED_ERROR_AUDIT_TRAIL = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ERROR_AUDIT_TRAIL);
    private final String HUMANIZED_ERROR_VALIDATE_PASSWORD = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ERROR_VALIDATE_PASSWORD);
    private final String HUMANIZED_LOGOUT_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_LOGOUT_FAILED);
    private final String HUMANIZED_REQUEST_APPROVAL_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_REQUEST_APPROVAL_FAILED);
    private final String HUMANIZED_GET_REQUEST_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_REQUEST_FAILED);
    private final String HUMANIZED_VERIFICATION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_VERIFICATION_FAILED);
    private final String HUMANIZED_ATTRIBUTE_ADDITION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ATTRIBUTE_ADDITION_FAILED);
    private final String HUMANIZED_GET_ATTRIBUTE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_ATTRIBUTE_FAILED);
    private final String HUMANIZED_GET_CONFIG_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_CONFIG_FAILED);
    private final String HUMANIZED_UPLOAD_ATTRIBUTE_REQUEST = ConfigError.getInstance().getProperty(Constant.HUMANIZED_UPLOAD_ATTRIBUTE_REQUEST);
    private final String HUMANIZED_GET_ATTRIBUTE = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_ATTRIBUTE);
    private final String HUMANIZED_REQUEST_ATTRIBUTE = ConfigError.getInstance().getProperty(Constant.HUMANIZED_REQUEST_ATTRIBUTE);
    private final String HUMANIZED_GET_EVIDENCE_STATUS = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_EVIDENCE_STATUS);
    private final String HUMANIZED_APPROVE_EVIDENCE_REQUEST = ConfigError.getInstance().getProperty(Constant.HUMANIZED_APPROVE_EVIDENCE_REQUEST);
    private final String HUMANIZED_EVIDENCE_REQUEST = ConfigError.getInstance().getProperty(Constant.HUMANIZED_EVIDENCE_REQUEST);
    private final String HUMANIZED_GET_ENTERPRISE = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_ENTERPRISE);
    private final String HUMANIZED_GET_VERIFIERS = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_VERIFIERS);
    private final String HUMANIZED_GET_ATTRIBUTE_REQUEST = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_ATTRIBUTE_REQUEST);
    private final String HUMANIZED_ONBOARD_APPLICATION = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ONBOARD_APPLICATION);
    private final String HUMANIZED_EDIT_APPLICATION = ConfigError.getInstance().getProperty(Constant.HUMANIZED_EDIT_APPLICATION);
    private final String HUMANIZED_DELETE_APPLICATION = ConfigError.getInstance().getProperty(Constant.HUMANIZED_DELETE_APPLICATION);
    private final String HUMANIZED_ADD_TUNNEL_LOG = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ADD_TUNNEL_LOG);
    private final String HUMANIZED_GET_REMOTE_SETTINGS = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_REMOTE_SETTINGS);
    private final String HUMANIZED_CSV_DOWNLOAD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_CSV_DOWNLOAD_FAILED);
    private final String HUMANIZED_GET_SRA_GATEWAY_SETTING = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_SRA_GATEWAY_SETTING);
    private final String HUMANIZED_ADD_SRA_GATEWAY_SETTING = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ADD_SRA_GATEWAY_SETTING);
    private final String HUMANIZED_DELETE_SRA_GATEWAY_SETTING = ConfigError.getInstance().getProperty(Constant.HUMANIZED_DELETE_SRA_GATEWAY_SETTING);
    private final String HUMANIZED_GET_DEVICES_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_DEVICES_FAILED);
    private final String HUMANIZED_GET_TOKENS_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_TOKENS_FAILED);
    private final String HUMANIZED_GET_DEVICE_TOKENS_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_DEVICE_TOKENS_FAILED);
    private final String HUMANIZED_EDIT_DEVICE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_EDIT_DEVICE_FAILED);
    private final String HUMANIZED_EDIT_TOKEN_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_EDIT_TOKEN_FAILED);
    private final String HUMANIZED_USER_GROUP_ALREADY_EXISTS = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_GROUP_ALREADY_EXISTS);
    private final String HUMANIZED_CREATE_USER_GROUP = ConfigError.getInstance().getProperty(Constant.HUMANIZED_CREATE_USER_GROUP);
    private final String HUMANIZED_GET_USERGROUP_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_USERGROUP_FAILED);
    private final String HUMANIZED_UPDATE_USER_GROUP = ConfigError.getInstance().getProperty(Constant.HUMANIZED_UPDATE_USER_GROUP);
    private final String HUMANIZED_USER_USER_GROUP_MAPPING = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_USER_GROUP_MAPPING);
    private final String HUMANIZED_USER_GROUP_BINDING_ALREADY_EXISTS = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_GROUP_BINDING_ALREADY_EXISTS);
    private final String HUMANIZED_USER_GROUP_BINDING_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_GROUP_BINDING_NOT_FOUND);
    private final String HUMANIZED_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS);
    private final String HUMANIZED_USER_GROUP_APPLICATION_BINDING_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_GROUP_APPLICATION_BINDING_NOT_FOUND);
    private final String HUMANIZED_USER_GROUP_APPLICATION_MAPPING = ConfigError.getInstance().getProperty(Constant.HUMANIZED_USER_GROUP_APPLICATION_MAPPING);
    private final String HUMANIZED_GET_USERGROUP_APPLICATION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_USERGROUP_APPLICATION_FAILED);
    private final String HUMANIZED_REMOVE_USER_GROUP = ConfigError.getInstance().getProperty(Constant.HUMANIZED_REMOVE_USER_GROUP);
    private final String HUMANIZED_EDIT_ATTRIBUTE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_EDIT_ATTRIBUTE_FAILED);
    private final String HUMANIZED_DELETE_ATTRIBUTE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_DELETE_ATTRIBUTE_FAILED);
    private final String HUMANIZED_QR_CODE_GENERATION_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_QR_CODE_GENERATION_FAILED);
    private final String HUMANIZED_LOGS_DOWNLOAD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_LOGS_DOWNLOAD_FAILED);
    private final String HUMANIZED_ONBOARD_ENTERPRISE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ONBOARD_ENTERPRISE_FAILED);
    private final String HUMANIZED_GET_STATE_MACHINE_WORKFLOW_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_STATE_MACHINE_WORKFLOW_FAILED);
    private final String HUMANIZED_GET_ACCOUNT_CUSTOM_STATE_MACHINE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_ACCOUNT_CUSTOM_STATE_MACHINE_FAILED);
    private final String HUMANIZED_STATE_MACHINE_WORKFLOW_ONBOARD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_STATE_MACHINE_WORKFLOW_ONBOARD_FAILED);
    private final String HUMANIZED_STATE_MACHINE_WORKFLOW_EDIT_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_STATE_MACHINE_WORKFLOW_EDIT_FAILED);
    private final String HUMANIZED_GET_ATTEMPT_TYPES_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_ATTEMPT_TYPES_FAILED);
    private final String HUMANIZED_GET_CHALLENGE_TYPES_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_GET_CHALLENGE_TYPES_FAILED);
    private final String HUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_FAILED);
    private final String HUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_EDIT_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_EDIT_FAILED);
    private final String ERROR_MESSAGE_USER_CREDENTIALS_NOT_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_CREDENTIALS_NOT_PRESENT);
    private final String ERROR_MESSAGE_EVIDENCE_IS_REQUIRED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EVIDENCE_IS_REQUIRED);
    private final String ERROR_MESSAGE_INVALID_BULK_UPLOAD_TYPE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_BULK_UPLOAD_TYPE);
    private final String HUMANIZED_UPLOAD_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_UPLOAD_FAILED);
    private final String HUMANIZED_DOWNLOAD_STATUS_FILE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_DOWNLOAD_STATUS_FILE_FAILED);
    private final String HUMANIZED_DOWNLOAD_SAMPLE_CSV_FILE_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_DOWNLOAD_SAMPLE_CSV_FILE_FAILED);
    private final String ERROR_MESSAGE_APPLICATION_NOT_ACTIVE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPLICATION_NOT_ACTIVE);
    private final String HUMANIZED_FETCH_QR_STATUS_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_FETCH_QR_STATUS_FAILED);
    private final String ERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE);
    private final String ERROR_MESSAGE_DUPLICATE_SEARCH_ATTRIBUTE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_DUPLICATE_SEARCH_ATTRIBUTE);
    private final String ERROR_MESSAGE_DUPLICATE_ATTRIBUTE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_DUPLICATE_ATTRIBUTE);
    private final String ERROR_MESSAGE_INVALID_ACCOUNT_ID = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_ACCOUNT_ID);
    private final String ERROR_MESSAGE_APPLICATION_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPLICATION_NOT_FOUND);
    private final String ERROR_MESSAGE_APPLICATION_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPLICATION_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT_TO_OTHER = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT_TO_OTHER);
    private final String ERROR_MESSAGE_USER_UPDATE_PENDING = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_UPDATE_PENDING);
    private final String ERROR_MESSAGE_INVALID_TOKEN = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_TOKEN);
    private final String ERROR_MESSAGE_PERMISSION_DENIED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_PERMISSION_DENIED);
    private final String ERROR_MESSAGE_IO_EXCEPTION = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_IO_EXCEPTION);
    private final String ERROR_MESSAGE_INVALID_APPLICATION_ID_OR_PASSWORD = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_APPLICATION_ID_OR_PASSWORD);
    private final String ERROR_MESSAGE_USER_SERVICE_BINDING_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_SERVICE_BINDING_FAILED);
    private final String ERROR_MESSAGE_INVALID_MOBILE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_MOBILE);
    private final String ERROR_MESSAGE_INVALID_APPLICATION_FOR_USER = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_APPLICATION_FOR_USER);
    private final String ERROR_MESSAGE_SERVER_ERROR = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SERVER_ERROR);
    private final String ERROR_MESSAGE_ACCOUNT_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ACCOUNT_NOT_FOUND);
    private final String ERROR_MESSAGE_LOOKUP_ID_COMPULSORY = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LOOKUP_ID_COMPULSORY);
    private final String ERROR_MESSAGE_APPROVAL_ATTEMPT_TYPE_INVALID = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPROVAL_ATTEMPT_TYPE_INVALID);
    private final String ERROR_MESSAGE_INVALID_CONSUMER_ID = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_CONSUMER_ID);
    private final String ERROR_MESSAGE_APPROVAL_ATTEMPT_NOT_APPROVED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_APPROVAL_ATTEMPT_NOT_APPROVED);
    private final String ERROR_MESSAGE_INVALID_APPLICATION_ID = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_APPLICATION_ID);
    private final String ERROR_MESSAGE_INVALID_ADMIN_ROLE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_ADMIN_ROLE);
    private final String ERROR_MESSAGE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT);
    private final String ERROR_MESSAGE_ATTRIBUTE_UPDATE_REQUEST_ALREADY_PRESENT1 = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ATTRIBUTE_UPDATE_REQUEST_ALREADY_PRESENT1);
    private final String ERROR_MESSAGE_SAME_PASSWORD = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SAME_PASSWORD);
    private final long ERROR_CODE_INVALID_PASSWORD = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_INVALID_PASSWORD));
    private final String HUMANIZED_RESET_APPLICATION_PIN_FAILED = ConfigError.getInstance().getProperty(Constant.HUMANIZED_RESET_APPLICATION_PIN_FAILED);
    private final String ERROR_MESSAGE_INVALID_FALLOUT_OPERATION = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_FALLOUT_OPERATION);
    private final String ACCOUNT_POLICY_FAILED = ConfigError.getInstance().getProperty(Constant.ACCOUNT_POLICY_FAILED);
    private final  String HUMANIZED_GENERATE_RUNNING_HASH_FAILED=ConfigError.getInstance().getProperty(Constant.HUMANIZED_GENERATE_RUNNING_HASH_FAILED);
    private final  String HUMANIZED_VERIFY_RUNNING_HASH_FAILED=ConfigError.getInstance().getProperty(Constant.HUMANIZED_VERIFY_RUNNING_HASH_FAILED);
    private final  String ERROR_MESSAGE_INVALID_RUNNING_HASH=ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_RUNNING_HASH);
    private final long ERROR_CODE_THEME_NOT_FOUND = Long.parseLong(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_THEME_NOT_FOUND));
    private final String ERROR_MESSAGE_THEME_NOT_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_THEME_NOT_FOUND);
    private final String ERROR_MESSAGE_INVALID_USER_CREDENTIALS = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_USER_CREDENTIALS);
    private final String ERROR_MESSAGE_DISABLE_USER_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_DISABLE_USER_FAILED);
    private final String ERROR_MESSAGE_DISABLE_USER_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_DISABLE_USER_ALREADY_PRESENT);

    private static final String ERROR_MESSAGE_USER_NAME_TOKEN_INVALID = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_NAME_TOKEN_INVALID);
    private final  String ERROR_MESSAGE_USER_LOGIN_TIME_EXPIRED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_USER_LOGIN_TIME_EXPIRED);
    private final  String ERROR_MESSAGE_INVALID_USER_STATE = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_USER_STATE);
    private final String ERROR_MESSAGE_UPDATE_FALLOUTSYNCDATA_ALREADY_PRESENT=ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_UPDATE_FALLOUTSYNCDATA_ALREADY_PRESENT);

    private final String ERROR_MESSAGE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT);

    private final String HUMANIZED_EDIT_FALLOUT_CONFIG = ConfigError.getInstance().getProperty(Constant.HUMANIZED_EDIT_FALLOUT_CONFIG);
    private  final String ERROR_MESSAGE_FALLOUT_SYNC_DATA_NOT_FOUND=ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_FALLOUT_SYNC_DATA_NOT_FOUND);

    private final String HUMANIZED_ADD_CONFIG = ConfigError.getInstance().getProperty(Constant.HUMANIZED_ADD_CONFIG);

    private  final String ERROR_MESSAGE_ADD_CONFIG_ALREADY_PRESENT=ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_ADD_CONFIG_ALREADY_PRESENT);

    private  final String ERROR_MESSAGE_UPDATE_CONFIG_ALREADY_PRESENT=ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_UPDATE_CONFIG_ALREADY_PRESENT);

    private  final String ERROR_MESSAGE_CONFIG_NOT_FOUND=ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_CONFIG_NOT_FOUND);

    private final Long ERROR_CODE_CONFIG_NOT_FOUND = Long.valueOf(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_CONFIG_NOT_FOUND));

    private static final Long ERROR_CODE_CONFIG_ALREADY_PRESENT = Long.valueOf(ConfigError.getInstance().getProperty(Constant.ERROR_CODE_CONFIG_ALREADY_PRESENT));
    private static final String ERROR_MESSAGE_CONFIG_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_CONFIG_ALREADY_PRESENT);

    private final String ERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_ALREADY_PRESENT);

    private final String ERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_ALREADY_PRESENT=ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_ALREADY_PRESENT);

    private final String ERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_FAILED);

    private final String ERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_FAILED);

    private final String ERROR_MESSAGE_GET_LDAP_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_GET_LDAP_FAILED);

    private static final String ERROR_MESSAGE_LDAP_DETAILS_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_LDAP_DETAILS_ALREADY_PRESENT);

    private  final  String ERROR_MESSAGE_SELF_BLOCKING_NON_PERMITTED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_SELF_BLOCKING_NOT_PERMITTED);

    private final String ERROR_MESSAGE_CREATE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_CREATE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT);

    private final String ERROR_MESSAGE_EDIT_TEMP_DETAILS_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_EDIT_TEMP_DETAILS_REQUEST_ALREADY_PRESENT);

    private final String ERROR_MESSAGE_CREATE_TEMP_DETAILS_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_CREATE_TEMP_DETAILS_FAILED);

    private final String ERROR_MESSAGE_GET_TEMP_DETAILS_REQUEST_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_GET_TEMP_DETAILS_REQUEST_FAILED);

    private final String ERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_FAILED);

    private final String ERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT);

    private final String ERROR_MESSAGE_TEMP_NAME_ALREADY_PRESENT = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_TEMP_NAME_ALREADY_PRESENT);

    private final String ERROR_MESSAGE_UPDATE_TEMP_DETAILS_REQUEST_FAILED = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_UPDATE_TEMP_DETAILS_REQUEST_FAILED);

    private final String ERROR_MESSAGE_INVALID_TEMPLATE_FOR_APPLICATION = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_INVALID_TEMPLATE_FOR_APPLICATION);

    private final String ERROR_MESSAGE_VALIDATION_RULE_ALREADY_EXIST = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_VALIDATION_RULE_ALREADY_EXIST);






    public String getERROR_MESSAGE_FALLOUT_SYNC_DATA_NOT_FOUND() {
        return ERROR_MESSAGE_FALLOUT_SYNC_DATA_NOT_FOUND;
    }

    public long getERROR_CODE_THEME_NOT_FOUND() {
        return ERROR_CODE_THEME_NOT_FOUND;
    }

    public String getERROR_MESSAGE_THEME_NOT_FOUND() {
        return ERROR_MESSAGE_THEME_NOT_FOUND;
    }


    private final String ERROR_MESSAGE_MULTIPLE_IDENTITIES_FOUND = ConfigError.getInstance().getProperty(Constant.ERROR_MESSAGE_MULTIPLE_IDENTITIES_FOUND);

    public Long getERROR_CODE_APPLICATION_NOT_FOUND() {
        return ERROR_CODE_APPLICATION_NOT_FOUND;
    }
    private final String ERROR_DEV_MESSAGE_INVALID_REQUEST_BODY=ConfigError.getInstance().getProperty(Constant.ERROR_DEV_MESSAGE_INVALID_REQUEST_BODY);


    public String getERROR_DEV_MESSAGE_INVALID_REQUEST_BODY() {
        return ERROR_DEV_MESSAGE_INVALID_REQUEST_BODY;
    }

    @Override
    public Long getERROR_CODE_CONFIG_ALREADY_PRESENT() {
        return ERROR_CODE_CONFIG_ALREADY_PRESENT;
    }

    @Override
    public String getERROR_MESSAGE_CONFIG_ALREADY_PRESENT() {
        return ERROR_MESSAGE_CONFIG_ALREADY_PRESENT;
    }

    private final String HUMANIZED_UPDATE_CONFIG = ConfigError.getInstance().getProperty(Constant.HUMANIZED_UPDATE_CONFIG);

    public Long getERROR_CODE_APPLICATION_ALREADY_PRESENT() {
        return ERROR_CODE_APPLICATION_ALREADY_PRESENT;

    }

    public long getERROR_CODE_USER_UPDATE_PENDING() {
        return ERROR_CODE_USER_UPDATE_PENDING;
    }

    public long getERROR_CODE_INVALID_TOKEN() {
        return ERROR_CODE_INVALID_TOKEN;
    }

    public long getERROR_CODE_INVALID_ROLES() {
        return ERROR_CODE_INVALID_ROLES;
    }

    public long getERROR_CODE_PERMISSION_DENIED() {
        return ERROR_CODE_PERMISSION_DENIED;
    }

    public long getERROR_CODE_IO_EXCEPTION() {
        return ERROR_CODE_IO_EXCEPTION;
    }

    public long getERROR_CODE_INVALID_APPLICATION_ID_OR_PASSWORD() {
        return ERROR_CODE_INVALID_APPLICATION_ID_OR_PASSWORD;
    }

    public long getERROR_CODE_USER_SERVICE_BINDING_FAILED() {
        return ERROR_CODE_USER_SERVICE_BINDING_FAILED;
    }

    public long getERROR_CODE_INVALID_MOBILE() {
        return ERROR_CODE_INVALID_MOBILE;
    }

    public long getERROR_CODE_INVALID_APPLICATION_FOR_USER() {
        return ERROR_CODE_INVALID_APPLICATION_FOR_USER;
    }

    public long getERROR_CODE_CERTIFICATE_PINNING_FAILURE() {
        return ERROR_CODE_CERTIFICATE_PINNING_FAILURE;
    }

    public long getERROR_CODE_SERVER_ERROR() {
        return ERROR_CODE_SERVER_ERROR;
    }

    public long getERROR_CODE_ACCOUNT_NOT_FOUND() {
        return ERROR_CODE_ACCOUNT_NOT_FOUND;
    }

    public long getERROR_CODE_IAMCI2_CALL_FAILED() {
        return ERROR_CODE_IAMCI2_CALL_FAILED;
    }

    public long getERROR_CODE_LOOKUP_ID_COMPULSORY() {
        return ERROR_CODE_LOOKUP_ID_COMPULSORY;
    }

    public long getERROR_CODE_APPROVAL_ATTEMPT_TYPE_INVALID() {
        return ERROR_CODE_APPROVAL_ATTEMPT_TYPE_INVALID;
    }

    public long getERROR_CODE_INVALID_CONSUMER_ID() {
        return ERROR_CODE_INVALID_CONSUMER_ID;
    }

    public long getERROR_CODE_APPROVAL_ATTEMPT_NOT_APPROVED() {
        return ERROR_CODE_APPROVAL_ATTEMPT_NOT_APPROVED;
    }

    public long getERROR_CODE_INVALID_APPLICATION_ID() {
        return ERROR_CODE_INVALID_APPLICATION_ID;
    }

    public long getERROR_CODE_INVALID_CLIENT_ID() {
        return ERROR_CODE_INVALID_CLIENT_ID;
    }

    public long getERROR_CODE_INVALID_DATA() {
        return ERROR_CODE_INVALID_DATA;
    }

    public long getERROR_CODE_USER_BLOCK() {
        return ERROR_CODE_USER_BLOCK;
    }

    public long getERROR_CODE_CONSUMER_NOT_PRESENT_DATA() {
        return ERROR_CODE_CONSUMER_NOT_PRESENT_DATA;
    }

    public long getERROR_CODE_SERVICE_NOT_FOUND() {
        return ERROR_CODE_SERVICE_NOT_FOUND;
    }

    public long getERROR_CODE_INVALID_SERVICE_FOR_APPLICATION() {
        return ERROR_CODE_INVALID_SERVICE_FOR_APPLICATION;
    }

    public long getERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT() {
        return ERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT;
    }

    public long getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND() {
        return ERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND;
    }

    public long getERROR_CODE_USER_NOT_FOUND() {
        return ERROR_CODE_USER_NOT_FOUND;
    }

    public long getERROR_CODE_TRANSACTION_NOT_FOUND() {
        return ERROR_CODE_TRANSACTION_NOT_FOUND;
    }

    public long getERROR_CODE_INVALID_TRANSACTION_TYPE() {
        return ERROR_CODE_INVALID_TRANSACTION_TYPE;
    }

    public long getERROR_CODE_VERIFICATION_FAILED() {
        return ERROR_CODE_VERIFICATION_FAILED;
    }

    public long getERROR_CODE_USER_NAME_PASSWORD_INVALID() {
        return ERROR_CODE_USER_NAME_PASSWORD_INVALID;
    }

    @Override
    public long getERROR_CODE_USER_NAME_TOKEN_INVALID() {
        return ERROR_CODE_USER_NAME_TOKEN_INVALID;
    }

    public long getERROR_CODE_INVALID_APPLICATION_TYPE() {
        return ERROR_CODE_INVALID_APPLICATION_TYPE;
    }

    public long getERROR_CODE_BLOCKED_FOR_RESET_PIN() {
        return ERROR_CODE_BLOCKED_FOR_RESET_PIN;
    }

    public long getERROR_CODE_RESET_PIN_COMPLETED() {
        return ERROR_CODE_RESET_PIN_COMPLETED;
    }

    public long getERROR_CODE_USER_SERVICE_BINDING_NOT_ACTIVE() {
        return ERROR_CODE_USER_SERVICE_BINDING_NOT_ACTIVE;
    }

    public long getERROR_CODE_USER_SERVICE_ALREADY_PRESENT() {
        return ERROR_CODE_USER_SERVICE_ALREADY_PRESENT;
    }

    public long getERROR_CODE_USER_SERVICE_NOT_FOUND() {
        return ERROR_CODE_USER_SERVICE_NOT_FOUND;
    }

    public long getERROR_CODE_USER_DATA_EMPTY() {
        return ERROR_CODE_USER_DATA_EMPTY;
    }

    public long getERROR_CODE_REQUEST_NOT_FOUND() {
        return ERROR_CODE_REQUEST_NOT_FOUND;
    }

    public long getERROR_CODE_INVALID_APPLICATION_NAME() {
        return ERROR_CODE_INVALID_APPLICATION_NAME;
    }

    public long getERROR_CODE_USER_SERVICE_BINDING_BLOCKED() {
        return ERROR_CODE_USER_SERVICE_BINDING_BLOCKED;
    }

    public long getERROR_CODE_APPLICATION_PASSWORD() {
        return ERROR_CODE_APPLICATION_PASSWORD;
    }

    public long getERROR_CODE_SERVICES_ALREADY_ENABLED_FOR_USER() {
        return ERROR_CODE_SERVICES_ALREADY_ENABLED_FOR_USER;
    }

    public long getERROR_CODE_USER_APPLICATION_BINDING_NOT_FOUND() {
        return ERROR_CODE_USER_APPLICATION_BINDING_NOT_FOUND;
    }

    public long getERROR_CODE_ATTRIBUTE_NOT_FOUND() {
        return ERROR_CODE_ATTRIBUTE_NOT_FOUND;
    }

    public long getERROR_CODE_EVIDENCE_NOT_FOUND() {
        return ERROR_CODE_EVIDENCE_NOT_FOUND;
    }

    public long getERROR_CODE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND() {
        return ERROR_CODE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND;
    }

    public long getERROR_CODE_ATTRIBUTE_ADDITION_ALREADY_PRESENT() {
        return ERROR_CODE_ATTRIBUTE_ADDITION_ALREADY_PRESENT;
    }

    public long getERROR_CODE_INVALID_REQUEST() {
        return ERROR_CODE_INVALID_REQUEST;
    }

    public long getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT() {
        return ERROR_CODE_ATTRIBUTE_ALREADY_PRESENT;
    }

    public long getERROR_CODE_ALREADY_APPROVED() {
        return ERROR_CODE_ALREADY_APPROVED;
    }

    public long getERROR_CODE_ATTRIBUTE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_ATTRIBUTE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_REQUEST_IS_TIMEOUT() {
        return ERROR_CODE_REQUEST_IS_TIMEOUT;
    }

    public long getERROR_CODE_ATTRIBUTE_UPDATION_ALREADY_PRESENT() {
        return ERROR_CODE_ATTRIBUTE_UPDATION_ALREADY_PRESENT;
    }

    public long getERROR_CODE_ATTRIBUTE_DELETION_ALREADY_PRESENT() {
        return ERROR_CODE_ATTRIBUTE_DELETION_ALREADY_PRESENT;
    }

    public long getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME() {
        return ERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME;
    }

    public long getERROR_CODE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_REQUEST_ALREADY_SENT_TO_CHECKER() {
        return ERROR_CODE_REQUEST_ALREADY_SENT_TO_CHECKER;
    }

    public long getERROR_CODE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER() {
        return ERROR_CODE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER;
    }

    public long getERROR_CODE_ATTRIBUTE_ADDITION_ID_INVALID() {
        return ERROR_CODE_ATTRIBUTE_ADDITION_ID_INVALID;
    }

    public long getERROR_CODE_INVALID_ATTRIBUTE_TYPE() {
        return ERROR_CODE_INVALID_ATTRIBUTE_TYPE;
    }

    public long getERROR_CODE_INVALID_ATTRIBUTE_NAME() {
        return ERROR_CODE_INVALID_ATTRIBUTE_NAME;
    }

    public long getERROR_CODE_FILE_IS_EMPTY() {
        return ERROR_CODE_FILE_IS_EMPTY;
    }

    public long getERROR_CODE_FILE_NOT_SUPPORTED() {
        return ERROR_CODE_FILE_NOT_SUPPORTED;
    }

    public long getERROR_CODE_APPLICATION_ONBOARD_ALREADY_PRESENT() {
        return ERROR_CODE_APPLICATION_ONBOARD_ALREADY_PRESENT;
    }

    public long getERROR_CODE_EDIT_ACCOUNT_FAILED() {
        return ERROR_CODE_EDIT_ACCOUNT_FAILED;
    }

    public long getERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE() {
        return ERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE;
    }

    public long getERROR_CODE_EVIDENCE_REQUEST_ALREADY_REJECTED() {
        return ERROR_CODE_EVIDENCE_REQUEST_ALREADY_REJECTED;
    }

    public long getERROR_CODE_APPLICATION_EDIT_ALREADY_PRESENT() {
        return ERROR_CODE_APPLICATION_EDIT_ALREADY_PRESENT;
    }

    public long getERROR_CODE_USER_ONBOARD_ALREADY_PRESENT() {
        return ERROR_CODE_USER_ONBOARD_ALREADY_PRESENT;
    }

    public long getERROR_CODE_CSV_DOWNLOAD_FAILED() {
        return ERROR_CODE_CSV_DOWNLOAD_FAILED;
    }

    public long getERROR_CODE_USER_SESSION_IS_ALREADY_ACTIVE() {
        return ERROR_CODE_USER_SESSION_IS_ALREADY_ACTIVE;
    }

    public long getERROR_CODE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND() {
        return ERROR_CODE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND;
    }

    public long getERROR_CODE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND() {
        return ERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND;
    }

    public long getERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND() {
        return ERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND;
    }

    public long getERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND() {
        return ERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND;
    }

    public long getERROR_CODE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT() {
        return ERROR_CODE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT;
    }

    public long getERROR_CODE_ENTERPRISE_NOT_FOUND() {
        return ERROR_CODE_ENTERPRISE_NOT_FOUND;
    }

    public long getERROR_CODE_SRA_GATEWAY_SETTING_ALREADY_BINDED() {
        return ERROR_CODE_SRA_GATEWAY_SETTING_ALREADY_BINDED;
    }

    public long getERROR_CODE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_POLICY_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_POLICY_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_USER_GROUP_ALREADY_EXISTS() {
        return ERROR_CODE_USER_GROUP_ALREADY_EXISTS;
    }

    public long getERROR_CODE_USER_GROUP_NOT_FOUND() {
        return ERROR_CODE_USER_GROUP_NOT_FOUND;
    }

    public long getERROR_CODE_USER_GROUP_DATA_SAME() {
        return ERROR_CODE_USER_GROUP_DATA_SAME;
    }

    public long getERROR_CODE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_USER_GROUP_BINDING_ALREADY_EXISTS() {
        return ERROR_CODE_USER_GROUP_BINDING_ALREADY_EXISTS;
    }

    public long getERROR_CODE_USER_GROUP_BINDING_NOT_FOUND() {
        return ERROR_CODE_USER_GROUP_BINDING_NOT_FOUND;
    }

    public long getERROR_CODE_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS() {
        return ERROR_CODE_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS;
    }

    public long getERROR_CODE_USER_GROUP_APPLICATION_BINDING_NOT_FOUND() {
        return ERROR_CODE_USER_GROUP_APPLICATION_BINDING_NOT_FOUND;
    }

    public long getERROR_CODE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_APPLICATION_ALREDY_BINDED() {
        return ERROR_CODE_APPLICATION_ALREDY_BINDED;
    }

    public long getERROR_CODE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_CONTACT_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_CONTACT_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_MAPPER_NOT_FOUND() {
        return ERROR_CODE_MAPPER_NOT_FOUND;
    }

    public long getERROR_CODE_AD_SYNC_FAILED() {
        return ERROR_CODE_AD_SYNC_FAILED;
    }

    public long getERROR_CODE_SRA_DETAILS_NOT_MATCHED() {
        return ERROR_CODE_SRA_DETAILS_NOT_MATCHED;
    }

    public long getERROR_CODE_INVALID_FILE_NAME() {
        return ERROR_CODE_INVALID_FILE_NAME;
    }

    public long getERROR_CODE_FILE_READ_FAILED() {
        return ERROR_CODE_FILE_READ_FAILED;
    }

    public long getERROR_CODE_LICENSE_CHECK_FAILED() {
        return ERROR_CODE_LICENSE_CHECK_FAILED;
    }

    public long getERROR_CODE_INPROGRESS() {
        return ERROR_CODE_INPROGRESS;
    }

    public long getERROR_CODE_ENTERPRISE_ALREADY_ONBOARDED() {
        return ERROR_CODE_ENTERPRISE_ALREADY_ONBOARDED;
    }

    public long getERROR_CODE_ENTERPRISE_NOT_ONBOARDED() {
        return ERROR_CODE_ENTERPRISE_NOT_ONBOARDED;
    }

    public long getERROR_CODE_BINDING_FAILED() {
        return ERROR_CODE_BINDING_FAILED;
    }

    public long getERROR_CODE_NOTIFICATION_TYPE_NOT_FOUND() {
        return ERROR_CODE_NOTIFICATION_TYPE_NOT_FOUND;
    }

    public long getERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_USER() {
        return ERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_USER;
    }

    public long getERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION() {
        return ERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION;
    }

    public long getERROR_CODE_INVALID_CONNECTION_SETTINGS() {
        return ERROR_CODE_INVALID_CONNECTION_SETTINGS;
    }

    public long getERROR_CODE_ADFS_DETAIL_NOT_FOUND() {
        return ERROR_CODE_ADFS_DETAIL_NOT_FOUND;
    }

    public long getERROR_CODE_AD_DETAIL_NOT_FOUND() {
        return ERROR_CODE_AD_DETAIL_NOT_FOUND;
    }

    public long getERROR_CODE_SAME_MAKER_REQUEST_APPROVED() {
        return ERROR_CODE_SAME_MAKER_REQUEST_APPROVED;
    }

    public long getERROR_CODE_INVALID_USERID_PASSWORD() {
        return ERROR_CODE_INVALID_USERID_PASSWORD;
    }

    public long getERROR_CODE_USER_CREDENTIALS_NOT_PRESENT() {
        return ERROR_CODE_USER_CREDENTIALS_NOT_PRESENT;
    }

    public long getERROR_CODE_EVIDENCE_IS_REQUIRED() {
        return ERROR_CODE_EVIDENCE_IS_REQUIRED;
    }

    public long getERROR_CODE_INVALID_TYPE() {
        return ERROR_CODE_INVALID_TYPE;
    }

    public long getERROR_CODE_APPLICATION_NOT_ACTIVE() {
        return ERROR_CODE_APPLICATION_NOT_ACTIVE;
    }

    public long getERROR_CODE_PARSING_CSV() {
        return ERROR_CODE_PARSING_CSV;
    }

    public long getERROR_CODE_REQUEST_NOT_PENDING() {
        return ERROR_CODE_REQUEST_NOT_PENDING;
    }

    public long getERROR_CODE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT() {
        return ERROR_CODE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT;
    }

    public long getERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND() {
        return ERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND;
    }

    public long getERROR_CODE_VERIFIER_NOT_FOUND() {
        return ERROR_CODE_VERIFIER_NOT_FOUND;
    }

    public long getERROR_CODE_EVIDENCE_REQUEST_ALREADY_APPROVED() {
        return ERROR_CODE_EVIDENCE_REQUEST_ALREADY_APPROVED;
    }

    public long getERROR_CODE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_ATTRIBUTE_NOT_PRESENT() {
        return ERROR_CODE_ATTRIBUTE_NOT_PRESENT;
    }

    public long getALREADY_PRESENT_IN_SYSTEM_CODE() {
        return ALREADY_PRESENT_IN_SYSTEM_CODE;
    }

    public long getVALIDATION_ERROR_CODE() {
        return VALIDATION_ERROR_CODE;
    }

    public long getUSER_NOT_FOUND() {
        return USER_NOT_FOUND;
    }

    public long getERROR_CODE_INTERNAL_SERVER_ERROR() {
        return ERROR_CODE_INTERNAL_SERVER_ERROR;
    }

    public long getERROR_CODE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_INVALID_ACCOUNT_ID() {
        return ERROR_CODE_INVALID_ACCOUNT_ID;
    }

    public String getERROR_MESSAGE_INVALID_DATA() {
        return ERROR_MESSAGE_INVALID_DATA;
    }

    public String getERROR_MESSAGE_TRANSACTION_ALREADY_EXISTS() {
        return ERROR_MESSAGE_TRANSACTION_ALREADY_EXISTS;
    }

    public String getERROR_MESSAGE_USER_BLOCK() {
        return ERROR_MESSAGE_USER_BLOCK;
    }

    public String getERROR_MESSAGE_CONSUMER_NOT_PRESENT_DATA() {
        return ERROR_MESSAGE_CONSUMER_NOT_PRESENT_DATA;
    }

    public String getERROR_MESSAGE_SERVICE_NOT_FOUND() {
        return ERROR_MESSAGE_SERVICE_NOT_FOUND;
    }

    public String getERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION() {
        return ERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION;
    }

    public String getERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND() {
        return ERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND;
    }

    public String getERROR_MESSAGE_USER_NOT_FOUND() {
        return ERROR_MESSAGE_USER_NOT_FOUND;
    }

    public String getERROR_MESSAGE_TRANSACTION_NOT_FOUND() {
        return ERROR_MESSAGE_TRANSACTION_NOT_FOUND;
    }

    public String getERROR_MESSAGE_INVALID_TRANSACTION_TYPE() {
        return ERROR_MESSAGE_INVALID_TRANSACTION_TYPE;
    }

    public String getERROR_MESSAGE_VERIFICATION_FAILED() {
        return ERROR_MESSAGE_VERIFICATION_FAILED;
    }

    public String getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID() {
        return ERROR_MESSAGE_USER_NAME_PASSWORD_INVALID;
    }

    public String getERROR_MESSAGE_INVALID_APPLICATION_TYPE() {
        return ERROR_MESSAGE_INVALID_APPLICATION_TYPE;
    }

    public String getERROR_MESSAGE_BLOCKED_FOR_RESET_PIN() {
        return ERROR_MESSAGE_BLOCKED_FOR_RESET_PIN;
    }

    public String getERROR_MESSAGE_RESET_PIN_COMPLETED() {
        return ERROR_MESSAGE_RESET_PIN_COMPLETED;
    }

    public String getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_ACTIVE() {
        return ERROR_MESSAGE_USER_SERVICE_BINDING_NOT_ACTIVE;
    }

    public String getERROR_MESSAGE_USER_SERVICE_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_SERVICE_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_USER_SERVICE_NOT_FOUND() {
        return ERROR_MESSAGE_USER_SERVICE_NOT_FOUND;
    }

    public String getERROR_MESSAGE_USER_DATA_EMPTY() {
        return ERROR_MESSAGE_USER_DATA_EMPTY;
    }

    public String getERROR_MESSAGE_REQUEST_NOT_FOUND() {
        return ERROR_MESSAGE_REQUEST_NOT_FOUND;
    }

    public String getERROR_MESSAGE_INVALID_APPLICATION_NAME() {
        return ERROR_MESSAGE_INVALID_APPLICATION_NAME;
    }

    public String getERROR_MESSAGE_USER_SERVICE_BINDING_BLOCKED() {
        return ERROR_MESSAGE_USER_SERVICE_BINDING_BLOCKED;
    }

    public String getERROR_MESSAGE_APPLICATION_PASSWORD() {
        return ERROR_MESSAGE_APPLICATION_PASSWORD;
    }

    public String getERROR_MESSAGE_SERVICES_ALREADY_ENABLED_FOR_USER() {
        return ERROR_MESSAGE_SERVICES_ALREADY_ENABLED_FOR_USER;
    }

    public String getERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND() {
        return ERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_NOT_FOUND() {
        return ERROR_MESSAGE_ATTRIBUTE_NOT_FOUND;
    }

    public String getERROR_MESSAGE_EVIDENCE_NOT_FOUND() {
        return ERROR_MESSAGE_EVIDENCE_NOT_FOUND;
    }

    public String getERROR_MESSAGE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND() {
        return ERROR_MESSAGE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_ADDITION_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ATTRIBUTE_ADDITION_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_INVALID_REQUEST() {
        return ERROR_MESSAGE_INVALID_REQUEST;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ATTRIBUTE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_REQUEST_IS_TIMEOUT() {
        return ERROR_MESSAGE_REQUEST_IS_TIMEOUT;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_UPDATION_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ATTRIBUTE_UPDATION_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_DELETION_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ATTRIBUTE_DELETION_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME() {
        return ERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_REQUEST_ALREADY_SENT_TO_CHECKER() {
        return ERROR_MESSAGE_REQUEST_ALREADY_SENT_TO_CHECKER;
    }

    public String getERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER() {
        return ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_ADDITION_ID_INVALID() {
        return ERROR_MESSAGE_ATTRIBUTE_ADDITION_ID_INVALID;
    }

    public String getERROR_MESSAGE_INVALID_ATTRIBUTE_TYPE() {
        return ERROR_MESSAGE_INVALID_ATTRIBUTE_TYPE;
    }

    public String getERROR_MESSAGE_INVALID_ATTRIBUTE_NAME() {
        return ERROR_MESSAGE_INVALID_ATTRIBUTE_NAME;
    }

    public String getERROR_MESSAGE_FILE_IS_EMPTY() {
        return ERROR_MESSAGE_FILE_IS_EMPTY;
    }

    public String getERROR_MESSAGE_APPLICATION_ONBOARD_ALREADY_PRESENT() {
        return ERROR_MESSAGE_APPLICATION_ONBOARD_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_EDIT_ACCOUNT_FAILED() {
        return ERROR_MESSAGE_EDIT_ACCOUNT_FAILED;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE() {
        return ERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE;
    }

    public String getERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_REJECTED() {
        return ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_REJECTED;
    }

    public String getERROR_MESSAGE_APPLICATION_EDIT_ALREADY_PRESENT() {
        return ERROR_MESSAGE_APPLICATION_EDIT_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_USER_ONBOARD_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_ONBOARD_ALREADY_PRESENT;
    }

    public String getERROR_DEVELOPER_CSV_DOWNLOAD_FAILED() {
        return ERROR_DEVELOPER_CSV_DOWNLOAD_FAILED;
    }

    public String getERROR_MESSAGE_USER_SESSION_IS_ALREADY_ACTIVE() {
        return ERROR_MESSAGE_USER_SESSION_IS_ALREADY_ACTIVE;
    }

    public String getERROR_MESSAGE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND() {
        return ERROR_MESSAGE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND;
    }

    public String getERROR_MESSAGE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND() {
        return ERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND;
    }

    public String getERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND() {
        return ERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND;
    }

    public String getERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND() {
        return ERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND;
    }

    public String getERROR_MESSAGE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT() {
        return ERROR_MESSAGE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_ENTERPRISE_NOT_FOUND() {
        return ERROR_MESSAGE_ENTERPRISE_NOT_FOUND;
    }

    public String getERROR_MESSAGE_SRA_GATEWAY_SETTING_ALREADY_BINDED() {
        return ERROR_MESSAGE_SRA_GATEWAY_SETTING_ALREADY_BINDED;
    }

    public String getERROR_MESSAGE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_POLICY_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_POLICY_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_USER_GROUP_NOT_FOUND() {
        return ERROR_MESSAGE_USER_GROUP_NOT_FOUND;
    }

    public String getERROR_MESSAGE_USER_GROUP_DATA_SAME() {
        return ERROR_MESSAGE_USER_GROUP_DATA_SAME;
    }

    public String getERROR_MESSAGE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_CONTACT_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_CONTACT_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_MAPPER_NOT_FOUND() {
        return ERROR_MESSAGE_MAPPER_NOT_FOUND;
    }

    public String getERROR_MESSAGE_SRA_DETAILS_NOT_MATCHED() {
        return ERROR_MESSAGE_SRA_DETAILS_NOT_MATCHED;
    }

    public String getERROR_MESSAGE_INVALID_FILE_NAME() {
        return ERROR_MESSAGE_INVALID_FILE_NAME;
    }

    public String getERROR_MESSAGE_INPROGRESS() {
        return ERROR_MESSAGE_INPROGRESS;
    }

    public String getERROR_MESSAGE_ENTERPRISE_ALREADY_ONBOARDED() {
        return ERROR_MESSAGE_ENTERPRISE_ALREADY_ONBOARDED;
    }

    public String getERROR_MESSAGE_NOTIFICATION_TYPE_NOT_FOUND() {
        return ERROR_MESSAGE_NOTIFICATION_TYPE_NOT_FOUND;
    }

    public String getERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_USER() {
        return ERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_USER;
    }

    public String getERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION() {
        return ERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION;
    }

    public String getERROR_MESSAGE_INVALID_CONNECTION_SETTINGS() {
        return ERROR_MESSAGE_INVALID_CONNECTION_SETTINGS;
    }

    public String getERROR_MESSAGE_ADFS_DETAIL_NOT_FOUND() {
        return ERROR_MESSAGE_ADFS_DETAIL_NOT_FOUND;
    }

    public String getERROR_MESSAGE_AD_DETAIL_NOT_FOUND() {
        return ERROR_MESSAGE_AD_DETAIL_NOT_FOUND;
    }

    public String getERROR_MESSAGE_SAME_MAKER_REQUEST_APPROVED() {
        return ERROR_MESSAGE_SAME_MAKER_REQUEST_APPROVED;
    }

    public String getERROR_MESSAGE_INVALID_USERID_PASSWORD() {
        return ERROR_MESSAGE_INVALID_USERID_PASSWORD;
    }

    public String getERROR_MESSAGE_INVALID_TYPE() {
        return ERROR_MESSAGE_INVALID_TYPE;
    }

    public String getERROR_MESSAGE_PARSING_CSV() {
        return ERROR_MESSAGE_PARSING_CSV;
    }

    public String getERROR_MESSAGE_REQUEST_NOT_PENDING() {
        return ERROR_MESSAGE_REQUEST_NOT_PENDING;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND() {
        return ERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND;
    }

    public String getERROR_MESSAGE_VERIFIER_NOT_FOUND() {
        return ERROR_MESSAGE_VERIFIER_NOT_FOUND;
    }

    public String getERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_APPROVED() {
        return ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_APPROVED;
    }

    public String getERROR_MESSAGE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT() {
        return ERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT;
    }

    public String getERROR_MESSAGE_INTERNAL_SERVER_ERROR() {
        return ERROR_MESSAGE_INTERNAL_SERVER_ERROR;
    }

    public String getERROR_MESSAGE_USER_BINDING_ALREDY_PRESENT() {
        return ERROR_MESSAGE_USER_BINDING_ALREDY_PRESENT;
    }

    public String getERROR_MESSAGE_INVALID_DATA_OTP() {
        return ERROR_MESSAGE_INVALID_DATA_OTP;
    }

    public String getERROR_DEV_MESSAGE_INVALID_DATA() {
        return ERROR_DEV_MESSAGE_INVALID_DATA;
    }

    public String getERROR_DEV_MESSAGE_INVALID_TRANSACTION_ID() {
        return ERROR_DEV_MESSAGE_INVALID_TRANSACTION_ID;
    }

    public String getERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID() {
        return ERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID;
    }

    public String getERROR_DEV_MESSAGE_INVALID_STATUS() {
        return ERROR_DEV_MESSAGE_INVALID_STATUS;
    }

    public String getERROR_DEV_MESSAGE_INVALID_TOKEN() {
        return ERROR_DEV_MESSAGE_INVALID_TOKEN;
    }

    public String getERROR_MESSAGE_USER_PROVIDED_INVALID_CLIENT_ID_OR_SECRET() {
        return ERROR_MESSAGE_USER_PROVIDED_INVALID_CLIENT_ID_OR_SECRET;
    }

    public String getERROR_MESSAGE_INVALID_ESC_FILE_NAME() {
        return ERROR_MESSAGE_INVALID_ESC_FILE_NAME;
    }

    public String getERROR_MESSAGE_INVALID_FILE_TYPE() {
        return ERROR_MESSAGE_INVALID_FILE_TYPE;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_PASSWORD() {
        return ERROR_MESSAGE_ATTRIBUTE_PASSWORD;
    }

    public String getERROR_MESSAGE_USER_PASSWORD() {
        return ERROR_MESSAGE_USER_PASSWORD;
    }

    public String getERROR_MESSAGE_UPDATE_APPROVAL_ATTEMPT_FAILED() {
        return ERROR_MESSAGE_UPDATE_APPROVAL_ATTEMPT_FAILED;
    }

    public String getERROR_MESSAGE_INVALID_VALUE() {
        return ERROR_MESSAGE_INVALID_VALUE;
    }

    public String getERROR_MESSAGE_EMPTY_CSV() {
        return ERROR_MESSAGE_EMPTY_CSV;
    }

    public String getERROR_MESSAGE_NOT_SUPPORTED() {
        return ERROR_MESSAGE_NOT_SUPPORTED;
    }

    public String getERROR_MESSAGE_EVIDENCE_EXPORT_EVIDENCE_FAILED() {
        return ERROR_MESSAGE_EVIDENCE_EXPORT_EVIDENCE_FAILED;
    }

    public String getERROR_MESSAGE_EDIT_CREDENTIAL_FALIED() {
        return ERROR_MESSAGE_EDIT_CREDENTIAL_FALIED;
    }

    public String getERROR_MESSAGE_APPLICATION_ALREADY_BINDED() {
        return ERROR_MESSAGE_APPLICATION_ALREADY_BINDED;
    }

    public String getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_EXPIRED_LICENSE() {
        return ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_EXPIRED_LICENSE;
    }

    public String getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_APPLICATION_EXCEEDED() {
        return ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_APPLICATION_EXCEEDED;
    }

    public String getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_USERS_EXCEEDED() {
        return ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_USERS_EXCEEDED;
    }

    public String getERROR_MESSAGE_ENTERPRISE_NOT_ONBOARDED() {
        return ERROR_MESSAGE_ENTERPRISE_NOT_ONBOARDED;
    }

    public String getERROR_MESSAGE_HOTP_GENERATION_FAILED() {
        return ERROR_MESSAGE_HOTP_GENERATION_FAILED;
    }

    public String getERROR_MESSAGE_HOTP_VALIDATION_FAILED() {
        return ERROR_MESSAGE_HOTP_VALIDATION_FAILED;
    }

    public String getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED() {
        return HUMANIZED_MESSAGE_AUTHENTICATION_FAILED;
    }

    public String getHUMANIZED_AUTHENTICATION_FAILED() {
        return HUMANIZED_AUTHENTICATION_FAILED;
    }

    public String getHUMANIZED_GET_ADMIN_FAILED() {
        return HUMANIZED_GET_ADMIN_FAILED;
    }

    public String getHUMANIZED_GET_USERS_FAILED() {
        return HUMANIZED_GET_USERS_FAILED;
    }

    public String getHUMANIZED_GET_USER_STATUS_FAILED() {
        return HUMANIZED_GET_USER_STATUS_FAILED;
    }

    public String getHUMANIZED_GET_SERVICES_FAILED() {
        return HUMANIZED_GET_SERVICES_FAILED;
    }

    public String getHUMANIZED_GET_APPLICATION_FAILED() {
        return HUMANIZED_GET_APPLICATION_FAILED;
    }

    public String getHUMANIZED_GET_APPROVAL_ATTEMPT_FAILED() {
        return HUMANIZED_GET_APPROVAL_ATTEMPT_FAILED;
    }

    public String getHUMANIZED_USER_UDATE_FAILED() {
        return HUMANIZED_USER_UDATE_FAILED;
    }

    public String getHUMANIZED_USER_ONBOARD_FAILED() {
        return HUMANIZED_USER_ONBOARD_FAILED;
    }

    public String getHUMANIZED_CHANGE_PASSWORD_FAILED() {
        return HUMANIZED_CHANGE_PASSWORD_FAILED;
    }

    public String getHUMANIZED_POLICY_ONBOARD_FAILED() {
        return HUMANIZED_POLICY_ONBOARD_FAILED;
    }

    public String getHUMANIZED_CONTACT_ONBOARD_FAILED() {
        return HUMANIZED_CONTACT_ONBOARD_FAILED;
    }

    public String getHUMANIZED_GET_POLICY_FAILED() {
        return HUMANIZED_GET_POLICY_FAILED;
    }

    public String getHUMANIZED_GET_CONTACT_FAILED() {
        return HUMANIZED_GET_CONTACT_FAILED;
    }

    public String getHUMANIZED_POLICY_EDIT_FAILED() {
        return HUMANIZED_POLICY_EDIT_FAILED;
    }

    public String getHUMANIZED_CONTACT_EDIT_FAILED() {
        return HUMANIZED_CONTACT_EDIT_FAILED;
    }

    public String getHUMANIZED_USER_APPROVAL_FAILED() {
        return HUMANIZED_USER_APPROVAL_FAILED;
    }

    public String getHUMANIZED_USER_BINDING_FAILED() {
        return HUMANIZED_USER_BINDING_FAILED;
    }

    public String getHUMANIZED_USER_UNBINDING_FAILED() {
        return HUMANIZED_USER_UNBINDING_FAILED;
    }

    public String getHUMANIZED_FILE_UPLOAD_FAILED() {
        return HUMANIZED_FILE_UPLOAD_FAILED;
    }

    public String getHUMANIZED_APPLICATION_SECRET_FAILED() {
        return HUMANIZED_APPLICATION_SECRET_FAILED;
    }

    public String getHUMANIZED_GET_DECRYPTION_FAILED() {
        return HUMANIZED_GET_DECRYPTION_FAILED;
    }

    public String getHUMANIZED_APPROVAL_ATTEMPT_UPDATE_FAILED() {
        return HUMANIZED_APPROVAL_ATTEMPT_UPDATE_FAILED;
    }

    public String getHUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED() {
        return HUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED;
    }

    public String getHUMANIZED_APPLICATION_DELETION_FAILED() {
        return HUMANIZED_APPLICATION_DELETION_FAILED;
    }

    public String getHUMANIZED_GET_USER_SUBSCRIPTION() {
        return HUMANIZED_GET_USER_SUBSCRIPTION;
    }

    public String getHUMANIZED_UNBIND_SERVICES() {
        return HUMANIZED_UNBIND_SERVICES;
    }

    public String getHUMANIZED_BIND_SERVICES() {
        return HUMANIZED_BIND_SERVICES;
    }

    public String getHUMANIZED_ERROR_AUDIT_TRAIL() {
        return HUMANIZED_ERROR_AUDIT_TRAIL;
    }

    public String getHUMANIZED_ERROR_VALIDATE_PASSWORD() {
        return HUMANIZED_ERROR_VALIDATE_PASSWORD;
    }

    public String getHUMANIZED_LOGOUT_FAILED() {
        return HUMANIZED_LOGOUT_FAILED;
    }

    public String getHUMANIZED_REQUEST_APPROVAL_FAILED() {
        return HUMANIZED_REQUEST_APPROVAL_FAILED;
    }

    public String getHUMANIZED_GET_REQUEST_FAILED() {
        return HUMANIZED_GET_REQUEST_FAILED;
    }

    public String getHUMANIZED_VERIFICATION_FAILED() {
        return HUMANIZED_VERIFICATION_FAILED;
    }

    public String getHUMANIZED_ATTRIBUTE_ADDITION_FAILED() {
        return HUMANIZED_ATTRIBUTE_ADDITION_FAILED;
    }

    public String getHUMANIZED_GET_ATTRIBUTE_FAILED() {
        return HUMANIZED_GET_ATTRIBUTE_FAILED;
    }

    public String getHUMANIZED_GET_CONFIG_FAILED() {
        return HUMANIZED_GET_CONFIG_FAILED;
    }

    public String getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST() {
        return HUMANIZED_UPLOAD_ATTRIBUTE_REQUEST;
    }

    public String getHUMANIZED_GET_ATTRIBUTE() {
        return HUMANIZED_GET_ATTRIBUTE;
    }

    public String getHUMANIZED_REQUEST_ATTRIBUTE() {
        return HUMANIZED_REQUEST_ATTRIBUTE;
    }

    public String getHUMANIZED_GET_EVIDENCE_STATUS() {
        return HUMANIZED_GET_EVIDENCE_STATUS;
    }

    public String getHUMANIZED_APPROVE_EVIDENCE_REQUEST() {
        return HUMANIZED_APPROVE_EVIDENCE_REQUEST;
    }

    public String getHUMANIZED_EVIDENCE_REQUEST() {
        return HUMANIZED_EVIDENCE_REQUEST;
    }

    public String getHUMANIZED_GET_ENTERPRISE() {
        return HUMANIZED_GET_ENTERPRISE;
    }

    public String getHUMANIZED_GET_VERIFIERS() {
        return HUMANIZED_GET_VERIFIERS;
    }

    public String getHUMANIZED_GET_ATTRIBUTE_REQUEST() {
        return HUMANIZED_GET_ATTRIBUTE_REQUEST;
    }

    public String getHUMANIZED_ONBOARD_APPLICATION() {
        return HUMANIZED_ONBOARD_APPLICATION;
    }

    public String getHUMANIZED_EDIT_APPLICATION() {
        return HUMANIZED_EDIT_APPLICATION;
    }

    public String getHUMANIZED_DELETE_APPLICATION() {
        return HUMANIZED_DELETE_APPLICATION;
    }

    public String getHUMANIZED_ADD_TUNNEL_LOG() {
        return HUMANIZED_ADD_TUNNEL_LOG;
    }

    public String getHUMANIZED_GET_REMOTE_SETTINGS() {
        return HUMANIZED_GET_REMOTE_SETTINGS;
    }

    public String getHUMANIZED_CSV_DOWNLOAD_FAILED() {
        return HUMANIZED_CSV_DOWNLOAD_FAILED;
    }

    public String getHUMANIZED_GET_SRA_GATEWAY_SETTING() {
        return HUMANIZED_GET_SRA_GATEWAY_SETTING;
    }

    public String getHUMANIZED_ADD_SRA_GATEWAY_SETTING() {
        return HUMANIZED_ADD_SRA_GATEWAY_SETTING;
    }

    public String getHUMANIZED_DELETE_SRA_GATEWAY_SETTING() {
        return HUMANIZED_DELETE_SRA_GATEWAY_SETTING;
    }

    public String getHUMANIZED_GET_DEVICES_FAILED() {
        return HUMANIZED_GET_DEVICES_FAILED;
    }

    public String getHUMANIZED_GET_TOKENS_FAILED() {
        return HUMANIZED_GET_TOKENS_FAILED;
    }

    public String getHUMANIZED_GET_DEVICE_TOKENS_FAILED() {
        return HUMANIZED_GET_DEVICE_TOKENS_FAILED;
    }

    public String getHUMANIZED_EDIT_DEVICE_FAILED() {
        return HUMANIZED_EDIT_DEVICE_FAILED;
    }

    public String getHUMANIZED_EDIT_TOKEN_FAILED() {
        return HUMANIZED_EDIT_TOKEN_FAILED;
    }

    public String getHUMANIZED_USER_GROUP_ALREADY_EXISTS() {
        return HUMANIZED_USER_GROUP_ALREADY_EXISTS;
    }

    public String getHUMANIZED_CREATE_USER_GROUP() {
        return HUMANIZED_CREATE_USER_GROUP;
    }

    public String getHUMANIZED_GET_USERGROUP_FAILED() {
        return HUMANIZED_GET_USERGROUP_FAILED;
    }

    public String getHUMANIZED_UPDATE_USER_GROUP() {
        return HUMANIZED_UPDATE_USER_GROUP;
    }

    public String getHUMANIZED_USER_USER_GROUP_MAPPING() {
        return HUMANIZED_USER_USER_GROUP_MAPPING;
    }

    public String getHUMANIZED_USER_GROUP_BINDING_ALREADY_EXISTS() {
        return HUMANIZED_USER_GROUP_BINDING_ALREADY_EXISTS;
    }

    public String getHUMANIZED_USER_GROUP_BINDING_NOT_FOUND() {
        return HUMANIZED_USER_GROUP_BINDING_NOT_FOUND;
    }

    public String getHUMANIZED_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS() {
        return HUMANIZED_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS;
    }

    public String getHUMANIZED_USER_GROUP_APPLICATION_BINDING_NOT_FOUND() {
        return HUMANIZED_USER_GROUP_APPLICATION_BINDING_NOT_FOUND;
    }

    public String getHUMANIZED_USER_GROUP_APPLICATION_MAPPING() {
        return HUMANIZED_USER_GROUP_APPLICATION_MAPPING;
    }

    public String getHUMANIZED_GET_USERGROUP_APPLICATION_FAILED() {
        return HUMANIZED_GET_USERGROUP_APPLICATION_FAILED;
    }

    public String getHUMANIZED_REMOVE_USER_GROUP() {
        return HUMANIZED_REMOVE_USER_GROUP;
    }

    public String getHUMANIZED_EDIT_ATTRIBUTE_FAILED() {
        return HUMANIZED_EDIT_ATTRIBUTE_FAILED;
    }

    public String getHUMANIZED_DELETE_ATTRIBUTE_FAILED() {
        return HUMANIZED_DELETE_ATTRIBUTE_FAILED;
    }

    public String getHUMANIZED_QR_CODE_GENERATION_FAILED() {
        return HUMANIZED_QR_CODE_GENERATION_FAILED;
    }

    public String getHUMANIZED_LOGS_DOWNLOAD_FAILED() {
        return HUMANIZED_LOGS_DOWNLOAD_FAILED;
    }

    public String getHUMANIZED_ONBOARD_ENTERPRISE_FAILED() {
        return HUMANIZED_ONBOARD_ENTERPRISE_FAILED;
    }

    public String getHUMANIZED_GET_STATE_MACHINE_WORKFLOW_FAILED() {
        return HUMANIZED_GET_STATE_MACHINE_WORKFLOW_FAILED;
    }

    public String getHUMANIZED_GET_ACCOUNT_CUSTOM_STATE_MACHINE_FAILED() {
        return HUMANIZED_GET_ACCOUNT_CUSTOM_STATE_MACHINE_FAILED;
    }

    public String getHUMANIZED_STATE_MACHINE_WORKFLOW_ONBOARD_FAILED() {
        return HUMANIZED_STATE_MACHINE_WORKFLOW_ONBOARD_FAILED;
    }

    public String getHUMANIZED_STATE_MACHINE_WORKFLOW_EDIT_FAILED() {
        return HUMANIZED_STATE_MACHINE_WORKFLOW_EDIT_FAILED;
    }

    public String getHUMANIZED_GET_ATTEMPT_TYPES_FAILED() {
        return HUMANIZED_GET_ATTEMPT_TYPES_FAILED;
    }

    public String getHUMANIZED_GET_CHALLENGE_TYPES_FAILED() {
        return HUMANIZED_GET_CHALLENGE_TYPES_FAILED;
    }

    public String getHUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_FAILED() {
        return HUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_FAILED;
    }

    public String getHUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_EDIT_FAILED() {
        return HUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_EDIT_FAILED;
    }

    public String getERROR_MESSAGE_USER_CREDENTIALS_NOT_PRESENT() {
        return ERROR_MESSAGE_USER_CREDENTIALS_NOT_PRESENT;
    }

    public String getERROR_MESSAGE_EVIDENCE_IS_REQUIRED() {
        return ERROR_MESSAGE_EVIDENCE_IS_REQUIRED;
    }

    public String getERROR_MESSAGE_INVALID_BULK_UPLOAD_TYPE() {
        return ERROR_MESSAGE_INVALID_BULK_UPLOAD_TYPE;
    }

    public String getHUMANIZED_UPLOAD_FAILED() {
        return HUMANIZED_UPLOAD_FAILED;
    }

    public String getHUMANIZED_DOWNLOAD_STATUS_FILE_FAILED() {
        return HUMANIZED_DOWNLOAD_STATUS_FILE_FAILED;
    }

    public String getHUMANIZED_DOWNLOAD_SAMPLE_CSV_FILE_FAILED() {
        return HUMANIZED_DOWNLOAD_SAMPLE_CSV_FILE_FAILED;
    }

    public String getERROR_MESSAGE_APPLICATION_NOT_ACTIVE() {
        return ERROR_MESSAGE_APPLICATION_NOT_ACTIVE;
    }

    public String getHUMANIZED_FETCH_QR_STATUS_FAILED() {
        return HUMANIZED_FETCH_QR_STATUS_FAILED;
    }

    public String getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE() {
        return ERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE;
    }

    public String getERROR_MESSAGE_DUPLICATE_SEARCH_ATTRIBUTE() {
        return ERROR_MESSAGE_DUPLICATE_SEARCH_ATTRIBUTE;
    }

    public String getERROR_MESSAGE_DUPLICATE_ATTRIBUTE() {
        return ERROR_MESSAGE_DUPLICATE_ATTRIBUTE;
    }

    public String getERROR_MESSAGE_INVALID_ACCOUNT_ID() {
        return ERROR_MESSAGE_INVALID_ACCOUNT_ID;
    }

    public String getERROR_MESSAGE_APPLICATION_NOT_FOUND() {
        return ERROR_MESSAGE_APPLICATION_NOT_FOUND;
    }

    public String getERROR_MESSAGE_APPLICATION_ALREADY_PRESENT() {
        return ERROR_MESSAGE_APPLICATION_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT_TO_OTHER() {
        return ERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT_TO_OTHER;
    }

    public String getERROR_MESSAGE_USER_UPDATE_PENDING() {
        return ERROR_MESSAGE_USER_UPDATE_PENDING;
    }

    public String getERROR_MESSAGE_INVALID_TOKEN() {
        return ERROR_MESSAGE_INVALID_TOKEN;
    }

    public String getERROR_MESSAGE_PERMISSION_DENIED() {
        return ERROR_MESSAGE_PERMISSION_DENIED;
    }

    public String getERROR_MESSAGE_IO_EXCEPTION() {
        return ERROR_MESSAGE_IO_EXCEPTION;
    }

    public String getERROR_MESSAGE_INVALID_APPLICATION_ID_OR_PASSWORD() {
        return ERROR_MESSAGE_INVALID_APPLICATION_ID_OR_PASSWORD;
    }

    public String getERROR_MESSAGE_USER_SERVICE_BINDING_FAILED() {
        return ERROR_MESSAGE_USER_SERVICE_BINDING_FAILED;
    }

    public String getERROR_MESSAGE_INVALID_MOBILE() {
        return ERROR_MESSAGE_INVALID_MOBILE;
    }

    public String getERROR_MESSAGE_INVALID_APPLICATION_FOR_USER() {
        return ERROR_MESSAGE_INVALID_APPLICATION_FOR_USER;
    }

    public String getERROR_MESSAGE_SERVER_ERROR() {
        return ERROR_MESSAGE_SERVER_ERROR;
    }

    public String getERROR_MESSAGE_ACCOUNT_NOT_FOUND() {
        return ERROR_MESSAGE_ACCOUNT_NOT_FOUND;
    }

    public String getERROR_MESSAGE_LOOKUP_ID_COMPULSORY() {
        return ERROR_MESSAGE_LOOKUP_ID_COMPULSORY;
    }

    public String getERROR_MESSAGE_APPROVAL_ATTEMPT_TYPE_INVALID() {
        return ERROR_MESSAGE_APPROVAL_ATTEMPT_TYPE_INVALID;
    }

    public String getERROR_MESSAGE_INVALID_CONSUMER_ID() {
        return ERROR_MESSAGE_INVALID_CONSUMER_ID;
    }

    public String getERROR_MESSAGE_APPROVAL_ATTEMPT_NOT_APPROVED() {
        return ERROR_MESSAGE_APPROVAL_ATTEMPT_NOT_APPROVED;
    }

    public String getERROR_MESSAGE_INVALID_APPLICATION_ID() {
        return ERROR_MESSAGE_INVALID_APPLICATION_ID;
    }

    public String getERROR_MESSAGE_INVALID_ADMIN_ROLE() {
        return ERROR_MESSAGE_INVALID_ADMIN_ROLE;
    }

    public String getERROR_MESSAGE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT;
    }

    @Override
    public String getERROR_MESSAGE_ATTRIBUTE_UPDATE_REQUEST_ALREADY_PRESENT1() {
        return ERROR_MESSAGE_ATTRIBUTE_UPDATE_REQUEST_ALREADY_PRESENT1;
    }

    public String getERROR_MESSAGE_SAME_PASSWORD() {
        return ERROR_MESSAGE_SAME_PASSWORD;
    }

    public String getERROR_MESSAGE_FALLOUT_INVALID_DATA() {
        return ERROR_MESSAGE_FALLOUT_INVALID_DATA;
    }

    public long getERROR_CODE_INVALID_PASSWORD() {
        return ERROR_CODE_INVALID_PASSWORD;
    }


    public long getERROR_CODE_FALLOUT_INVALID_DATA() {
        return ERROR_CODE_FALLOUT_INVALID_DATA;
    }

    public String getERROR_MESSAGE_CONSENT_REQUIRED_NOT_ALLOWED() {
        return ERROR_MESSAGE_CONSENT_REQUIRED_NOT_ALLOWED;
    }

    public long getERROR_CODE_CONSENT_REQUIRED_NOT_ALLOWED() {
        return ERROR_CODE_CONSENT_REQUIRED_NOT_ALLOWED;
    }

    public String getHUMANIZED_CHANGE_ENTERPRISE_PIN_FAILED() {
        return HUMANIZED_CHANGE_ENTERPRISE_PIN_FAILED;
    }

    public String getHUMANIZED_CHANGE_APPLICATION_PIN_FAILED() {
        return HUMANIZED_CHANGE_APPLICATION_PIN_FAILED;
    }

    public String getHUMANIZED_RESET_APPLICATION_PIN_FAILED() {
        return HUMANIZED_RESET_APPLICATION_PIN_FAILED;
    }

    public String getERROR_MESSAGE_INVALID_FALLOUT_OPERATION() {
        return ERROR_MESSAGE_INVALID_FALLOUT_OPERATION;
    }

    public Long getERROR_CODE_INVALID_SERACH_ATTRIBUTE() {
        return ERROR_CODE_INVALID_SERACH_ATTRIBUTE;
    }

    public String getHUMANIZED_RESET_ENTERPRISE_PIN_FAILED() {
        return HUMANIZED_RESET_ENTERPRISE_PIN_FAILED;
    }

    @Override
    public long getERROR_CODE_INVALID_FALLOUT_OPERATION() {
        return ERROR_CODE_INVALID_FALLOUT_OPERATION;
    }

    public long getERROR_CODE_INVALID_SEARCH_ATTRIBUTE() {
        return ERROR_CODE_INVALID_SEARCH_ATTRIBUTE;
    }

    @Override
    public String getUPDATE_PASSWORD_FAILED() {
        return UPDATE_PASSWORD_FAILED;
    }

    public String getACCOUNT_POLICY_FAILED() {
        return ACCOUNT_POLICY_FAILED;
    }

    public String getERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT;
    }

    public long getERROR_CODE_GENERATE_RUNNING_HASH_FAILED() {
        return ERROR_CODE_GENERATE_RUNNING_HASH_FAILED;
    }

    public long getERROR_CODE_VERIFY_RUNNING_HASH_FAILED() {
        return ERROR_CODE_VERIFY_RUNNING_HASH_FAILED;
    }

    public String getHUMANIZED_GENERATE_RUNNING_HASH_FAILED() {
        return HUMANIZED_GENERATE_RUNNING_HASH_FAILED;
    }

    public String getHUMANIZED_VERIFY_RUNNING_HASH_FAILED() {
        return HUMANIZED_VERIFY_RUNNING_HASH_FAILED;
    }

    public String getERROR_MESSAGE_INVALID_RUNNING_HASH() {
        return ERROR_MESSAGE_INVALID_RUNNING_HASH;
    }
    public Long getERROR_CODE_MULTIPLE_IDENTITIES_FOUND() {
        return ERROR_CODE_MULTIPLE_IDENTITIES_FOUND;
    }

    public String getERROR_MESSAGE_MULTIPLE_IDENTITIES_FOUND() {
        return ERROR_MESSAGE_MULTIPLE_IDENTITIES_FOUND;
    }

    @Override
    public String getERROR_MESSAGE_USER_NAME_TOKEN_INVALID() {
        return ERROR_MESSAGE_USER_NAME_TOKEN_INVALID;
    }

    @Override
    public String getERROR_MESSAGE_INVALID_USER_CREDENTIALS() {
        return ERROR_MESSAGE_INVALID_USER_CREDENTIALS;
    }

    @Override
    public String getERROR_MESSAGE_DISABLE_USER_FAILED() {
        return ERROR_MESSAGE_DISABLE_USER_FAILED;
    }

    @Override
    public long getERROR_CODE_DISABLE_USER_ALREADY_PRESENT() {
        return ERROR_CODE_DISABLE_USER_ALREADY_PRESENT;
    }

    @Override
    public String getERROR_MESSAGE_DISABLE_USER_ALREADY_PRESENT() {
        return ERROR_MESSAGE_DISABLE_USER_ALREADY_PRESENT;
    }

    @Override
    public long getERROR_CODE_USER_LOGIN_TIME_EXPIRED() {
        return ERROR_CODE_USER_LOGIN_TIME_EXPIRED;
    }

    public String getERROR_MESSAGE_INVALID_USER_STATE() {
        return ERROR_MESSAGE_INVALID_USER_STATE;
    }

    @Override
    public String getERROR_MESSAGE_USER_LOGIN_TIME_EXPIRED() {
        return ERROR_MESSAGE_USER_LOGIN_TIME_EXPIRED;
    }

    @Override
    public Long getERROR_CODE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT() {
        return ERROR_CODE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT;
    }

    @Override
    public String getERROR_MESSAGE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT() {
        return ERROR_MESSAGE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT;
    }
    @Override
    public String getHUMANIZED_EDIT_FALLOUT_CONFIG() {
        return HUMANIZED_EDIT_FALLOUT_CONFIG;
    }
    @Override
    public String getERROR_MESSAGE_UPDATE_FALLOUTSYNCDATA_ALREADY_PRESENT() {
        return ERROR_MESSAGE_UPDATE_FALLOUTSYNCDATA_ALREADY_PRESENT;
    }

    @Override
    public String getHUMANIZED_ADD_CONFIG() {
        return HUMANIZED_ADD_CONFIG;
    }

    @Override
    public String getERROR_MESSAGE_ADD_CONFIG_ALREADY_PRESENT() {
        return ERROR_MESSAGE_ADD_CONFIG_ALREADY_PRESENT;
    }

    @Override
    public String getHUMANIZED_UPDATE_CONFIG() {
        return HUMANIZED_UPDATE_CONFIG;
    }

    @Override
    public String getERROR_MESSAGE_UPDATE_CONFIG_ALREADY_PRESENT() {
        return ERROR_MESSAGE_UPDATE_CONFIG_ALREADY_PRESENT;
    }

    @Override
    public Long getERROR_CODE_CONFIG_NOT_FOUND() {
        return ERROR_CODE_CONFIG_NOT_FOUND;
    }

    @Override
    public String getERROR_MESSAGE_CONFIG_NOT_FOUND() {
        return ERROR_MESSAGE_CONFIG_NOT_FOUND;
    }

    public String getERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_FAILED() {
        return ERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_FAILED;
    }

    public String getERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_FAILED() {
        return ERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_FAILED;
    }

    public String getERROR_MESSAGE_GET_LDAP_FAILED() {
        return ERROR_MESSAGE_GET_LDAP_FAILED;
    }

    @Override
    public String getERROR_MESSAGE_LDAP_DETAILS_ALREADY_PRESENT() {
        return ERROR_MESSAGE_LDAP_DETAILS_ALREADY_PRESENT;
    }

    public String getERROR_MESSAGE_SELF_BLOCKING_NON_PERMITTED() {
        return ERROR_MESSAGE_SELF_BLOCKING_NON_PERMITTED;
    }

    public String getERROR_MESSAGE_CREATE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_CREATE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT;
    }
    public String getERROR_MESSAGE_EDIT_TEMP_DETAILS_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_EDIT_TEMP_DETAILS_REQUEST_ALREADY_PRESENT;
    }
    public String getERROR_MESSAGE_CREATE_TEMP_DETAILS_FAILED() {
        return ERROR_MESSAGE_CREATE_TEMP_DETAILS_FAILED;
    }
    public String getERROR_MESSAGE_GET_TEMP_DETAILS_REQUEST_FAILED() {
        return ERROR_MESSAGE_GET_TEMP_DETAILS_REQUEST_FAILED;
    }
    public String getERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_FAILED() {
        return ERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_FAILED;
    }
    public String getERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT() {
        return ERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT;
    }
    public String getERROR_MESSAGE_TEMP_NAME_ALREADY_PRESENT() {
        return ERROR_MESSAGE_TEMP_NAME_ALREADY_PRESENT;
    }
    public String getERROR_MESSAGE_UPDATE_TEMP_DETAILS_REQUEST_FAILED() {
        return ERROR_MESSAGE_UPDATE_TEMP_DETAILS_REQUEST_FAILED;
    }

    public String getERROR_MESSAGE_INVALID_TEMPLATE_FOR_APPLICATION() {
        return ERROR_MESSAGE_INVALID_TEMPLATE_FOR_APPLICATION;
    }

    public String getERROR_MESSAGE_VALIDATION_RULE_ALREADY_EXIST() {
        return ERROR_MESSAGE_VALIDATION_RULE_ALREADY_EXIST;
    }

    @Override
    public Long getERROR_CODE_TEMPLATE_NOT_FOUND() {
        return ERROR_CODE_TEMPLATE_NOT_FOUND;
    }

    @Override
    public String getERROR_MESSAGE_TEMPLATE_NOT_FOUND() {
        return ERROR_MESSAGE_TEMPLATE_NOT_FOUND;
    }
}
