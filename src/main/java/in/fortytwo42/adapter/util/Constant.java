package in.fortytwo42.adapter.util;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Constants: This class defines constants used in this projects.
 * @author ChiragShah
 *
 */
public class Constant {

    public static final String CRYPTO_RETRIES="crypto.retries";
    
    public static final String CRYPTO_IP_VERSION ="crypto.ipVersion";
    
    public static final String CRYPTO_LOG_LEVEL="crypto.logLevel";
    
    public static final String CRYPTO_QR_TIME_OUT="cryto.timeOut";
    
    public static final String CRYPTO_NUM_TOKEN_TIMEOUT="cryto.numTokenTimeout";
    
    public static final String CRYPTO_PROXY_IP="cryto.proxyIp";
    
    public static final String CRYPTO_PROX_PORT="cryto.proxyPort";
    
    public static final String CRYPTO_SOCKS_VERSION="cryto.socksVersion";
    
    public static final String CRYPTO_SOCKS_USER_NAME="cryto.socksUsername";
    
    public static final String CRYPTO_SOCKS_PASSWORD="cryto.socksPassword";

    public static final String  CRYPTO_SESSION_TIMEOUT = "crypto.sessionTimeout";
    public static final String CREATED = "Created";

    public static final String RESPONSE_CODE_200 = "200";

    public static final String RESPONSE_SUB_CODE_100 = "100";

    public static final String CONTENT_HASH_HEADER = "Content-Hash";

    public static final String HTTP_METHOD_HEADER = "_HttpMethod";

    public static final String HTTP_METHOD_PATCH = "PATCH";

    public static final String CONFIG = "config";

    public static final String EXPIRY = "exp";

    public static final String CONFIG_FILE = "adapter.config";
    public static final String ERROR_CONFIG_FILE = "adapterError.config";

    public static final String PUBLIC_KEY_FILE_PATH = "public.key.path";

    public static final String PRIVATE_KEY_FILE_PATH = "private.key.path";

    public static final String PROPERTIES_FILE = CONFIG + File.separator + "iam.properties";

    public static final String DATABASE_PORT = "dbPort";

    public static final String DATABASE_NAME = "databaseName";

    public static final String ADFS = "ADFS";

    public static final long TIMEOUT_ERROR_CODE = 0x00001;

    public static final String TIMEOUT_ERROR_HUMANIZED_MESSAGE = "timeout on server";

    public static final long TIMEOUT_SPAN = 90l;

    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    public static final String ASYNC_RESPONSE_TIMEOUT_IN_CREATE_AUTHATTEMPT = "timeout of the request occured";

    public static final String CONSUMER_NULL = "consumer is null";

    public static final Long BAD_DATA_ERROR_CODE = 0x00020l;

    public static final String INTERNAL_SERVER_ERROR_MSG = "error occured at server";

    public static final Long INTERNAL_SERVER_ERROR_CODE = 0x00030l;

    public static final long BEARER_TOKEN_ERROR_CODE = 0x0010;

    public static final String BEARER = "Bearer";

    public static final String BEARER_SPACE = "Bearer ";

    public static final String USER_IS_NOT_AUTHORIZED_TO_ACCESS_RESOURCE = "User is not authorized to access resource";

    public static final String ROLE = "Role";

    public static final int USER_BAD_TOKEN_CODE = 0x00025;

    public static final String REPLICA_SERVER_ADDRESSES = "database.replica.server.address";

    public static final String LOCALHOST = "localhost";

    public static final String REPLICA_SET_NAME = "replica.set.name";

    public static final String ENVIRONMENT = "environment";

    public static final String LOCAL = "local";

    public static final String UAT = "uat";

    public static final String PRODUCTION = "production";

    public static final String PUSH_NOTIFICATION_FAILED_FOR_CONSUMER = "Push Notification failed to be sent to Consumer with Mobile No. : ";

    public static final String ADMIN_NOT_FOUND = "admin with given id is not present";

    public static final int ADMIN_NOT_FOUND_ERROR_CODE = 0x20000;

    public static final String BAD_DATA_EXCEPTION_MSG = "User sent invalid data.";

    public static final String ADMIN = "ADMIN";

    public static final String _NEW_LINE = "\n";

    public static final String _ARROW = " --> ";

    public static final String _COLON = " : ";

    public static final String SERVICE_NAME = "Service Name";
    
    public static final String SERVICE_NAME_HEADER = "service_name";


    public static final String ADMIN_ID = "Admin Id";

    public static final String ENTERPRISE_STATUS = "Enterprise Status";

    public static final String ENTERPRISE = "Enterprise";

    public static final String SYSTEM = "SYSTEM";

    public static final String IAM = "IAM";

    public static final String RUNNING_HASH = "runningHash";
    public static final String IDS_SIGNATURE = "IDS_SIGNATURE";

    public static final String ROLE_NOT_PRESENT_IN_CLAIMS = "Role claim is missing in token";

    public static final String TOKEN_VERIFICATION_FAILED = "JWT token is invalid";

    public static final int NOT_ADMIN_ERROR_CODE = 101;

    public static final String NOT_ADMIN_MESSAGE = "Only 'Admin' Role is allowed to perform operations on admin resource";

    public static final String IAM_APP = "iam";

    //Default log configurations if configuration file not found
    public static final String LOG_LEVEL = "loglevel";
    public static final Integer DEFAULT_LOG_LEVEL = 3;
    public static final String LOG_DIRECTORY = "logdir";
    //    public static final String DEFAULT_LOG_DIRECTORY = "/tmp";
    public static final String DEFAULT_LOG_DIRECTORY = "/weblogic/Oracle/adapter-logs";
    public static final String LOG_PROJECT_NAME = "projectname";
    public static final String DEFAULT_LOG_PROJECT_NAME = "pi-adapter";

    //Default DB and Server configuration if config file not found
    public static final String DATABASE_ADDRESS = "database.replica.server.address";
    public static final String LOCALHOST_URL = "localhostUrl";
    public static final String UAT_URL = "uatUrl";

    public static final String LIMIT = "limit";

    public static final String OFFSET = "offset";

    public static final String _COMMA = ",";

    public static final String _EMPTY = "";

    public static final String HEADER_CHARSET_IS_NOT_ASCII = "headers charset can be only ASCII";

    public static final String AUTHORIZATION_TOKEN_NULL = "Authorization token null/empty";

    public static final String CONTENT_HASH_VALIDATION_FAILED = "content hash do not match the value calculated on server";

    public static final String DATABASE_USERNAME = "database.username";

    public static final String DATABASE_PASSWORD = "database.password";

    public static final String SERVLET_CONTEXT_DESTROYED = "Servlet context destroyed.";

    public static final String SERVLET_CONTEXT_DESTROYER_CALLED = "Servlet Context Destroyer Called.";

    public static final String _AND = "&";

    public static final String _EQUAL = "=";

    public static final int NOT_FOUND_ERROR_CODE = 400;

    public static final String ENCRYPTION_FILE = "encryption";

    public static final String BROKER_URL = "broker.url";

    public static final String TOKEN_USER = "user";

    public static final String ISSUER = "issuer";

    public static final String TOKEN_VALIDITY = "token.validity.in.millis";

    public static final int SUCCESS = 1;

    public static final int FAILURE = 2;

    public static final String IAM_SERVICE_URL = "web.server.base.url";

    public static final String IAM_PINNING_HOST_NAME = "pinning.host.name";

    public static final String ENTERPRISE_ID = "enterprise.id";

    public static final String IAM_PINNING_HASH = "pinning.hash";

    public static final String ESC_FOLDER_PATH = "esc.folder.path";

    public static final String CRYPTO_SERVER_URL = "crypto.server.ip";

    public static final String CRYPTO_SERVER_PORT = "crypto.server.port";

    public static final String APPLICATION_ID = "applicationId";

    public static final String APPLICATION_ID_HEADER = "application_id";

    
    public static final String APPLICATION_PASSWORD = "applicationPassword";

    public static final String AD_IP = "adIp";

    public static final String AD_PORT = "adPort";

    public static final String AD_DOMAIN = "ad.domain";

    public static final String AD_CONNECTION_URL = "ad.connection.url";

    public static final String AD_ADMIN = "adAdmin";

    public static final String AD_PASSWORD = "adPassword";

    public static final String AD_AUTH_TYPE = "adAuthType";

    public static final String ADFS_CONNECTION_URL = "adfs.connection.url";

    public static final String ADFS_APPLICATION_IDENTIFIER = "adfs.application.identifier";

    public static final String ADFS_PORT = "adfs.port";

    public static final String ADFS_HOSTNAME = "adfs.hostname";

    public static final String ADFS_SSL_ENABLED = "adfs.ssl.enabled";

    public static final String SUCCESS_STATUS = "SUCCESS";

    public static final String FAILURE_STATUS = "FAILURE";

    public static final String BINDING_INITIATED = "Binding initiated";

    public static final String BINDING_FAILED = "USER BINDING FAILED";

    public static final Long TOKEN_ACTIVATION_TIME_SPAN = 3 * 1000l;

    public static final Long TOKEN_EXPIRY_TIME_SPAN = 5 * 60 * 1000l;

    public static final Long OTP_EXPIRY_TIME_SPAN = 15 * 60 * 1000l;

    public static final String SMS = "sms";

    public static final String SMS_URL = "url";

    public static final String SMS_API_KEY = "apikey";

    public static final String SMS_SENDER_ID = "senderid";

    public static final String SMS_SERVICE_NAME = "servicename";

    public static final String TEST_MODE = "testMode";

    public static final String DECRYPTION_KEY_TEST_MODE = "decryption.key.test.mode";

    public static final String DECRYPTION_KEY_FOR_TEST = "decryption.key.for.test";

    public static final String _DOT = ".";

    public static final String USER_NAME = "username";

    public static final String STATE = "state";

    public static final String NEXT_STATE = "next_state";

    public static final String SMS_CONTENT_1 = "smsContent1";

    public static final String SMS_CONTENT_2 = "smsContent2";

    public static final String SINGLE_APOSTROPHE = "'";

    public static final String _SPACE = " ";

    public static final String NOT_EQUAL_QUERY_PARAM = "not:";

    public static final String ERROR_GENERATING_2FA = "Error generating 2FA for user";

    public static final String ERROR_2FA_RESPONSE = "Error obtaining 2FA response for user";

    public static final String ERROR_GENERATING_APPLICATION_TOKEN = "Error generating application token";

    public static final String ERROR_APPLICATION_NOT_FOUND = "Application not found";

    public static final String CONSUMER_ID = "consumerId";

    public static final String ACTIVE = "ACTIVE";

    public static final String INACTIVE = "INACTIVE";

    public static final String SIGN_TRANSACTION_ID = "signTransactionId";

    public static final String TRANSACTION_ID = "transactionId";

    public static final String PAGE_NUMBER = "pageNo";

    public static final String VERIFIED = "VERIFIED";

    public static final String NOT_VERIFIED = "NOT VERIFIED";

    public static final String SIGNED = "SIGNED";

    public static final String REGULATORY = "REGULATORY";

    public static final String NORMAL = "NORMAL";

    public static final String ERROR_SIGNING_TRANSACTION = "Error signing transaction";

    public static final String AUDIT_LOG_LEVEL = "auditLoglevel";

    public static final String PENDING = "PENDING";

    public static final String THREAD_POOL_SIZE = "threadpool.size";

    public static final String MAX_THREAD_POOL_SIZE = "maxThreads";

    public static final String JOB_KEY = "job.key";

    public static final String TRIGGER_KEY = "trigger.key";

    public static final String GROUP_KEY = "group";

    public static final String HEALTH_CHECK_JOB_KEY = "healthJobKey";

    public static final String HEALTH_CHECK_TRIGGER_KEY = "healthTriggerKey";

    public static final String HEALTH_CHECK_GROUP_KEY = "healthGroup";

    public static final String CRON_EXPRESSION = "cron.expression";

    public static final String HEALTH_CHECK_CRON_EXPRESSION = "healthCheckCronExpression";

    public static final String TOKEN_DELETION_JOB_KEY = "token.job.key";

    public static final String TOKEN_DELETION_TRIGGER_KEY = "token.trigger.key";

    public static final String TOKEN_DELETION_GROUP_KEY = "token.group";

    public static final String TOKEN_DELETION_CRON_EXPRESSION = "token.deletion.cron.expression";

    public static final String APPROVAL_ATTEMPT_TIMEOUT_THREAD_INTERRUPTED = "auto approval thread interupted";

    public static final String SERVLET_CONTEXT_INITIALIZED = "Context Initialized";

    public static final String UPLOAD_ESC_FOLDER_PATH = "upload.esc.location";

    public static final String STAGING_ESC_FOLDER_PATH = "stagingESCLocation";

    public static final String FINAL_ESC_FOLDER_PATH = "ESCLocation";

    public static final String SERVICE_STATUS = "serviceStatus";

    public static final String BLOCK = "block";

    public static final String BLOCKED = "BLOCKED";

    public static final String BLOCKED_FOR_RESET_PIN = "User status is BLOCKED_FOR_RESET_PIN";

    public static final String SERVICE_UNAVAILABLE = "Service unavailable";

    public static final long SERVICE_UNAVAILABLE_ERROR_CODE = 0x00031l;

    public static final String AD_USERNAME_PATTERN = "ad.username.pattern";

    public static final String MODE = "mode";

    public static final String MODE_PRODUCTION = "production";

    public static final String USER_PRINCIPLE = "userPrincipalName";

    public static final String PROVISIONED = "PROVISIONED";
    
    public static final String PARTIALLY_ACTIVE = "PARTIALLY_ACTIVE";

    public static final String HIBERNATE_FILE = "hibernate.cfg.xml";

    public static final String HIBERNATE_SANDBOX_FILE = "hibernate-sandbox.cfg.xml";

    public static final String QUARTZ_FILE = "quartz.properties";

    public static final String HIBERNATE_PASSWORD = "hibernate.hikari.dataSource.password";

    public static final String HIBERNATE_DATASOURCE = "hibernate.hikari.dataSourceClassName";

    public static final String HIBERNATE_USER = "hibernate.hikari.dataSource.user";

    public static final String HIBERNATE_URL = "hibernate.hikari.dataSource.url";

    public static final String HIBERNATE_SCHEMA = "hibernate.default_schema";

    public static final String HIBERNATE_DIALECT = "hibernate.dialect";

    public static final String HIBERNATE_CONNECTION_PROVIDER = "hibernate.connection.provider_class";

    public static final String HEALTH_CHECK = "health-check";

    public static final String HEALTHY_COUNT = "healthyCount";

    public static final String UNHEALTHY_COUNT = "unhealthyCount";

    public static final int THREAD_COUNT = 5;

    public static final String ACTIVEMQ_URL = "activemqUrl";

    public static final String ACTIVEMQ_ADMIN = "activemqAdmin";

    public static final String ACTIVEMQ_PASSWORD = "activemqPassword";

    public static final String X_QUERY = "X-query";

    public static final String EMAIL_HOST = "email.host";

    public static final String EMAIL_SENDER = "email.sender";

    public static final String EMAIL_PASSWORD = "email.password";

    public static final String EMAIL_PORT = "email.port";

    public static final String EMAIL_ENABLE_TTLS = "email.enable.TTLS";

    public static final String COUNTRY_CODE = "+91";

    public static final String ENVIRONMENT_VARIABLE = "IAMCI2_HOME";

    public static final String AES_KEY = "encryption.key";

    public static final String HEADER_APPLICATION_ID = "Application-Id";

    public static final String HEADER_APPLICATION_SECRET = "Application-Secret";

    public static final String HEADER_SERVICE_NAME = "Service-Name";

    public static final String HEADER_APPLICATION_LABEL = "Application-Label";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final int CONSUMER_APPLICATION_BINDING_ACTIVE = 100;

    public static final int APPLICATION_INACTIVE = 101;

    public static final int SERVICE_INACTIVE = 102;

    public static final int CONSUMER_NOT_REGISTERED = 103;

    public static final int CONSUMER_INACTVE = 104;

    public static final int SERVICE_APPLICATION_BINDING_INACTIVE = 105;

    public static final int CONSUMER_APPLICATION_BINDING_INACTIVE = 106;

    public static final int CONSUMER_APPLICATION_BINDING_BLOCKED = 107;

    public static final int BLOCKED_FOR_RESET_PIN_CODE = 108;

    public static final int RESET_PIN_COMPLETED_CODE = 109;

    public static final int USER_BLOCKED = 110;

    public static final String COMPULSORY_FIELDS = "Invalid data. Following fields are empty/invalid - ";

    public static final String INVALID_SEARCH_FIELDS = "Invalid search attribute: ";

    public static final String OR = " OR ";
    
    public static final String RESOURCE_LOG_THREAD_POOL_SIZE = "resource-log.thread.size";

    public static final String ADFS_SEND_OTP_RETRY = "adfs.send.otp.retry";
    
    public static final String APPLICATON_ID = "ApplicationId";

    public static final String CACHE_COMPONENT = "CacheComponent";

    public static final String APPLICATION_SECRET = "Application Secret";

    public static final String USERNAME = "Username";

    public static final String FIRST_NAME = "FirstName";

    public static final String LAST_NAME = "LastName";

    public static final String LOCATION = "Location";

    public static final String COMSUMER_ID = "ConsumerId";

    public static final String EMAIL_CONSTANT = "Email";

    public static final String APPLICATION_NAME = "Application Name";

    public static final String DESCRIPTION = "Descrption";

    public static final String APPROVAL_STATUS = "Approval Status";

    public static final String COMMENTS = "Comments";

    public static final String COMMENTS_HEADER = "comment";

    
    public static final String PASSWORD = "Password";

    public static final String OLD_PASSWORD="OldPassword";
    public static final String USER_CREDENTIAL = "User Credential";

    public static final String USER_CREDENTIAL_HEADER = "user_credential";

    
    public static final String ACCOUNT_TYPE = "Account Type";
    
    public static final String ACCOUNT_TYPE_HEADER = "account_type";


    public static final String APPLICATON_TYPE = "Application Type";

    public static final String TWO_FACTOR_STATUS = "TwoFactor Status";

    public static final String BINDING_STATUS = "Binding Status";

    public static final String USER_STATUS = "User Status";

    public static final String SERVICES = "Services";

    public static final String TRANSACTION_TIMEOUT = "Transaction Timeout";

    public static final String ACTIVATE_ENCRYPTION = "Activate Encryption";

    public static final String PRESENCE = "Presence";

    public static final String ROLES = "Roles";

    public static final String MOBILE = "Mobile";

    public static final String TRANSACTION_SUMMARY = "Transaction Summary";

    public static final String TRANSACTION_DETAILS = "Transaction Details ";

    public static final String CRYPTO_TOKEN = "Crypto-token ";

    public static final String AUTHENTICATION_TOKEN= "authenticationToken";

    public static final String RESOURCE_APPLICATION_ID = "ApplicationId";

    public static final String RESOURCE_TRANSACTION_ID = "TransactionId";

    public static final String RESOURCE_ENTERPRISE_ID = "EnterpriseId";

    public static final String AUTHENTICATION_TYPE = "Authentication Type";

    public static final Object USER_LIST = "User list";

    public static final Object APPROVAL_ATTEMPT_TYPE = "Approval Attempt Type";

    public static final Object AUTHENTICATION_REQUIRED = "Authentication Required";

   // public static final String SERVER_ID = "server-id";

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    public static final String CRYPTO_TOKEN_GENERATED = "Crypto Token Generated";

    public static final String CRYPTO_TOKEN_IS = "Dear User, your I-AM Crypto Token is ";

    public static final String GROUP_NAME = "GroupName";

    public static final String MAKER_APPROVAL = "IsMakerApprovalRequired";

    public static final String MAX_HOST_ALLOWED = "MaxHostAllowedPerUser";

    public static final String DB_FIELD_USER_NAME = "username";

    public static final String AD_ENCRYPTION_KEY = "ad.encryption.key";

    public static final String ENCRYPTED = "encrypted";

    public static final String SIMPLE = "simple";

    public static final String INVALID = "INVALID";

    public static final String VALID = "VALID";

    public static final String INVALID_TIMEOUT = "Invalid data. Max allowed Timeout is ";

    public static final String INVALID_VALIDITY = "Invalid data. Max allowed Validity is ";

    public static final String INVALID_AUTHENTICATION_TIMEOUT = "Invalid data. Max allowed Authentication Timeout is ";

    public static final String INVALID_TRANSACTION_TIMEOUT = "Invalid data. Max allowed Transaction Timeout is ";

    public static final String MAX_TIMEOUT = "max.allowed.timeout";

    public static final String INVALID_LENGTH = "Invalid length of following fields - ";

    public static final String AD_AUTH = "adAuthentication";

    public static final String DISABLE = "disable";

    public static final String ENABLE = "enable";

    public static final String MOBILE_PATTERN = "mobile.pattern";

    public static final String VALIDATION_PATTERN = "input.pattern";

    public static final String STATUS = "Status";

    public static final String SEARCH_QUERY = "SearchQuery";

    public static final String RANDOM = "FT42-PT-LOGS~";

    public static final String TILT = "~";

    public static final String TOKEN_EXPIRY_BUFFER_IN_MIN = "token.expiry.buffer";

    public static final String DB_HEALTH_CHECK = "dBHealthCheck";

    public static final int TWO_FA_ENABLED_IAM_ENABLED_USER_REGISTERED = 100;

    public static final int TWO_FA_ENABLED_IAM_DISABLED_USER_REGISTERED = 101;

    public static final int TWO_FA_ENABLED_IAM_ENABLED_USER_NOT_REGISTERED = 102;

    public static final int TWO_FA_ENABLED_IAM_DISABLED_USER_NOT_REGISTERED = 103;

    public static final int TWO_FA_DISABLED_IAM_ENABLED_USER_REGISTERED = 104;

    public static final int TWO_FA_DISABLED_IAM_DISABLED_USER_REGISTERED = 105;

    public static final int TWO_FA_DISABLED_IAM_ENABLED_USER_NOT_REGISTERED = 106;

    public static final int TWO_FA_DISABLED_IAM_DISABLED_USER_NOT_REGISTERED = 107;

    public static final String INSERT = "INSERT";

    public static final String UPDATE = "UPDATE";

    public static final String DELETE = "DELETE";

    public static final String SERVICE_BINDING = "SERVICE_BINDING";

    public static final String SERVICE_UNBINDING = "SERVICE_UNBINDING";

    public static final String TWO_FA_SETTINGS = "TWO_FA_SETTINGS";

    public static final String PERMISSIONS = "Permission";

    public static final String IP_ADDRESS = "IpAddress";

    public static final String USER_AGENT = "UserAgent";

    public static final String APPLICATION = "Application";

    public static final String BINDING_REQUEST = "Binding Request";

    public static final String BINDING_REQUEST_DETAILS = "Binding request for Application ";

    public static final String USER_APPROVAL_PENDING = "USER_APPROVAL_PENDING";

    public static final String CHECKER_APPROVAL_PENDING = "CHECKER_APPROVAL_PENDING";

    //application callback url type

    public static final String UPDATE_USER_URL = "UPDATE_USER_URL";

    public static final String UPDATE_AUTH_ATTEMPT_URL = "UPDATE_AUTH_ATTEMPT_URL";

    public static final String RESET_PIN_COMPLETED = "User status is RESET_PIN_COMPLETED";

    public static final String TRUE = "true";

    public static final String FALSE = "false";

    public static final String APPLICATION_DOWNLOAD_LINK =
            "Download I-AM application using following link. Playstore: https://play.google.com/store/apps/details?id=in.fortytwo42.iam AppStore: https://itunes.apple.com/in/app/i-am/id1291286769?mt=8";

    public static final String EVENT_POOL_SIZE = "event.pool.size";

    public static final Object USERTYPE = "userType";

    public static final String EMAIL = "EMAIL";

    public static final long MAX_TIMEOUT_SPAN = 240l;

    public static final String APPLICATION_EMAIL_BODY = "applicationUpdateEmailBody";

    public static final String APPLICATION_SMS_BODY = "applicationUpdateSmsBody";

    public static final String USER_EMAIL_BODY = "user.update.email.body";

    public static final String USER_SMS_BODY = "user.update.sms.body";

    public static final String RAISED = "raised";

    public static final String TWO_FA_ENABLED_SMS_BODY = "TwoFAEnabledSmsBody";

    public static final String EMAIL_SUBJECT = "email.subject";

    public static final String USER_UPDATE = "User Update for username ";

    public static final String APPLICATION_UPDATE = "Application update for Application Id ";

    public static final String APPLICATION_CREATION = "Application creation for Application Id ";

    public static final String CREATION = "creation";

    public static final String UPDATION = "updation";

    public static final String SMS_NOTIFICATION = "SMS";

    public static final String BINDING_APPROVAL_TIMEOUT = "binding.approval.timeout";

    public static final int DEFAULT_BINDING_TIMEOUT = 3600;

    public static final String SMS_NOTIFICATION_MODE = "smsNotification";

    public static final String EMAIL_NOTIFICATION_MODE = "emailNotification";

    public static final String UPDATE_TEXT = "update";

    public static final String SERVICE_BINDING_TEXT = "service binding";

    public static final String SERVICE_UNBINDING_TEXT = "service unbinding";

    public static final String IAMCIX = "iamcix";

    public static final String NON_AD_USER = "NON_AD";

    public static final String AD_USER = "AD";

    public static final String EXPORT_USER_DATE_FORMAT = "dd/MM/yyyy";

    public static final String MONITORING_IP = "monitoring.server.ip";

    public static final String MONITORING_PORT = "monitoring.server.port";

    public static final String AD_SCHEDULAR_METRICS = "schedular.ADSync";

    public static final String HEALTH_CHECK_SCHEDULAR_METRICS = "schedular.HealthCheck";

    public static final String TOKEN_DELETION_SCHEDULAR_METRICS = "schedular.HealthCheck";

    public static final String SALT_SIZE = "salt.size";

    public static final String APPLICATION_ENCRYPTION_KEY = "application.encryption.key";

    public static final String APPLICATION_LABEL = "Application Label";

    public static final String GROUP_CODE = "Group Code";

    public static final String INTERNAL = "INTERNAL";

    public static final String EXTERNAL = "EXTERNAL";

    public static final String REVOKE = "REVOKE";

    public static final String GRANT = "GRANT";

    public static final String FED_APPLICATION_TYPE = "Application Type";

    public static final String _ASTERICKS = "*";

    public static final String APPROVED = "APPROVED";

    public static final String REJECTED = "REJECTED";

    public static final String ORACLE = "ORACLE";

    public static final String FED = "FED";

    public static final String GROUP_FETCH_TYPE = "Group Fetch Type";

    public static final String APPLICATION_TYPE = "Application Type";

    public static final String TYPE_GROUP_CODE = "GROUP_CODE";

    public static final String TYPE_GROUP_NAME = "GROUP_NAME";

    public static final String FED_SERVICE = "FED";

    public static final String ACCESS_TYPE = "Access Type";

    public static final String QUERY_GROUP_ID = ":QRYGROUPID";

    public static final String SUPERVISORS = "Supervisors";

    public static final String STAKEHOLDERS = "Stakeholders";

    public static final String FED_CACHE_TIMEOUT = "fedCacheTimeout";

    public static final String FED_AES_KEY = "fedEncryptionKey";

    public static final String DUPLICATE_SUPERVISOR = "Duplicate Supervisor";

    public static final String DUPLICATE_STAKEHOLDER = "Duplicate Stakeholders";

    public static final String BANK_CHECKERS = "Bank Checkers";

    public static final String DUPLICATE_BANK_CHECKERS = "Duplicate Bank Checkers";

    public static final String MAX_HOSTS_ALLOWED = "Max host allowed per users ";

    public static final String CNB_GROUP_PREFIX = "FCAT_";

    public static final String VERSION_V2 = "V2";

    public static final String USERS_NOT_ACTIVE_ON_IAM = "Users not active on I-AM: ";

    public static final String ADMIN_AUTHENTICATION = "adminAuthentication";

    public static final String ATTRIBUTE_NAME = "Attribute Name";

    public static final String ATTRIBUTE_VALUE = "Attribute Value";

    public static final String ATTRIBUTE_DATA = "Attribute Data";

    public static final String ATTRIBUTE_ACTION = "Attribute Action";

    public static final String IDENTIFIER = "identifier";

    public static final String ATTRIBUTES = "ATTRIBUTES";

    public static final String ACCOUNT_ID = "ACCOUNT_ID";

    public static final String AD_ID = "AD_ID";

    public static final String MOBILE_NO = "MOBILE_NO";

    public static final String MOBILE_NO_HEADER = "mobile_number";

    public static final String POLICY_UNIQUE_ID = "Policy unique id";
    
    public static final String CONTACT_UNIQUE_ID = "Contact unique id";


    public static final String POLICY_ID = "Policy Id";

    public static final String POLICY_ID_INVALID = "Policy Id invalid";

    public static final String GATEWAY_NAME = "Gateway Name";
    
    public static final String GATEWAY_ID = "Gateway Id";

    
    public static final String EMAIL_ID = "EMAIL_ID";

    public static final String FULL_NAME = "FULL_NAME";
    
    public static final String USER_NAME_ATTRIBUTE_NAME = "USERNAME";

    public static final String ATTRIBUTE_VERIFICATION = "ATTRIBUTE_VERIFICATION";

    public static final String ATTRIBUTE_ADDITION = "ATTRIBUTE_ADDITION";

    public static final String ATTRIBUTE_REQUEST = "ATTRIBUTE_REQUEST";

    public static final String EVIDENCE_REQUEST = "EVIDENCE_REQUEST";

    public static final String EVIDENCE_REQUEST_USER_CONSENT = "EVIDENCE_REQUEST_USER_CONSENT";

    public static final String ENTERPRISE_PASSWORD = "enterprise.password";

    public static final String EVIDENCE_STORE_PATH = "evidence.store.path";

    public static final String PEER_TO_ENTERPRISE = "PEER_TO_ENTERPRISE";

    public static final String APPROVED_BY_CHECKER = "APPROVED_BY_CHECKER";

    public static final String REJECTED_BY_CHECKER = "REJECTED_BY_CHECKER";

    public static final String VERIFICATION_SUCCESS = "VERIFICATION_SUCCESS";

    public static final String VERIFICATION_FAILED = "VERIFICATION_FAILED";

    public static final String ATTRIBUTE_VERIFICATION_SUCCESS = "ATTRIBUTE_VERIFICATION_SUCCESS";

    public static final String ATTRIBUTE_VERIFICATION_FAILED = "ATTRIBUTE_VERIFICATION_FAILED";

    public static final String TRANSACTION_PENDING = "TRANSACTION_PENDING";

    public static final String HEADER_ENTERPRISE_ID = "Enterprise-Id";

    public static final String HEADER_ENTERPRISE_ACCOUNT_ID = "EnterpriseAccountId";

    public static final String ENTERPRISE_ACCOUNT_ID = "enterprise.account.id";

    public static final String _PIPE = "|";

    public static final String HEADER_APPLICATION_NAME = "Application-Name";

    public static final String ATTRIBUTES_FIELD = "Attributes";

    public static final String CUST_ID = "CUST_ID";

    public static final String ATTRIBUTE_ADDITION_WITH_SERVICE_BINDING = "ATTRIBUTE_ADDITION_WITH_SERVICE_BINDING";

    public static final String APPLICATION_ENTITY = "APPLICATION";

    public static final String ENTERPRISE_ENTITY = "ENTERPRISE";

    public static final String MANUALLY_TRIGGERED = "MANUALLY-TRIGGERED";

    public static final String CREATE_APPROVAL_ATTEMPT = "Create Approval attempt~";

    public static final String APPLICATION_TO_PEER = "APPLICATION_TO_PEER";

    public static final String USER_IDENTIFIER = "User Identifier";

    public static final String DEFAULT = "DEFAULT";

    public static final String PUBLIC = "PUBLIC";

    public static final String ID = "ID";

    public static final String NAME = "NAME";

    public static final String PARENT_ID = "PARENT_ID";

    public static final String PARENT_NAME = "PARENT_NAME";

    public static final String ENTERPRISE_NAME = "enterprise.name";

    public static final String RECEIVED = "RECEIVED";

    public static final String ENTERPRISE_TYPE = "enterprise.type";

    public static final Long ATTRIBUTE_REQUEST_INVALID = 400l;

    public static final String DEBUG_MODE = "debugMode";

    public static final String DEFAULT_USERNAME = "ft42";

    public static final String DEFAULT_PASSWORD = "ft42@123";

    public static final String ATTRIBUTE_ADDITION_WITH_BINDING = "ATTRIBUTE_ADDITION_WITH_BINDING";

    public static final String HEADER_FILE_TYPE = "fileType";

    public static final String CSV_FILE_NAME_PATTERN = "csv.filename.pattern";

    public static final String FILE_NAME = "fileName";

    public static final String CSV_TYPE_ADD_ATTRIBUTE = "UPLOAD_ATTRIBUTE";

    public static final String CSV_TYPE_EDIT_USER_STATUS = "EDIT_USER_STATUS";

    public static final String CSV_TYPE_USER_APPLICATION_MAPPING = "USER_APPLICATION_MAPPING";

    public static final String CSV_OLD_ATTRIBUTE_NAME = "oldAttributeName";

    public static final String CSV_OLD_ATTRIBUTE_VALUE = "oldAttributeValue";

    public static final String IS_DEFAULT = "isDefault";

    public static final String ATTRIBUTE_UPDATION = "ATTRIBUTE_UPDATION";

    public static final String PUBLIC_ATTRIBUTE_NAME = "pulicAttributeName";

    public static final String PUBLIC_ATTRIBUTE_VALUE = "publicAttributeValue";

    public static final String CSV_ATTRIBUTE_NAME = "attributeName";

    public static final String CSV_ATTRIBUTE_VALUE = "attributeValue";

    public static final String CSV_TWO_FACTOR_STATUS = "twoFactorStaus";

    public static final String CSV_USER_STATUS = "userStatus";

    public static final String CSV_IAM_USER_STATUS = "iamUserStatus";

    public static final String REQUEST_ID = "REQUEST_ID";

    public static final String SUCCESS_COMMENT = "Success";

    public static final String EVIDENCE_REQUIRED = "evidenceRequired";

    public static final String CSV_PROCESSING_THREAD_POOL_SIZE = "csv.processing.threadpool.size";

    public static final String CRYPTO_ATTRIBUTES_ONBOARDER_THREADPOOL_SIZE = "crypto.attributes.onboarder.threadpool.size";

    public static final String ATTRIBUTE_EDITING_ONBOARDER_RETRY = "attribute.editing.onboarder.retry";

    public static final String CSV_FOLDER_PATH = "csv.folder.path";

    public static final String ATTRIBUTE_DEMAND = "ATTRIBUTE_DEMAND";

    public static final String ATTRIBUTE_TYPE = "Attribute Type";

    public static final String ATTRIBUTE_STRORE_SECURITY_POLICY = "Attribute Strore Security Policy";

    public static final String ATTRIBUTE_VALUE_MODEL = "Attribute Value Model";

    public static final String REQUEST_REFERENCE = "request_reference";

    public static final String REQUEST_REFERENCE_NUMBER = "X-ReqRefNum";

    public static final String REQUEST_REFERENSE_NUMBER = "request-reference-number";
    public static final String TIMEOUT = "TIMEOUT";
    public static final String EVIDENCE_COUNT = "evidence_count";

    public static final String FILE = "file";

    public static final Object REQUEST_TYPE = "requestType";

    public static final Object SENT = "SENT";

    public static final String USER_ID = "USER_ID";

    public static final String OPEN_ACCOUNT_ACCESS_STATUS = "OPEN";

    public static final String CALLBACK_URL = "callbackUrl";

    public static final String QUEUE_NAME = "queueName";

    public static final String ACTIVATION_DATE = "activationDate";

    public static final String EXPIRATION_DATE = "expirationDate";

    public static final String ENTERPRISE_TO_PEER_TIMEOUT = "enterprise.to.peer.timeout";

    public static final String SRA_APPLICATION_TYPE = "SRAApplicationType";

    public static final String EXTERNAL_ADDRESS = "External Address";

    public static final String REMOTE_APPLICATION_ID = "remoteApplicationId";

    public static final String REFERENCE_NUMBER = "referenceNumber";

    public static final String USER_AUTH_PRINCIPAL_SCHEDULAR_TIME = "user.auth.principal.schedular.time";

    public static final String QUESTION_ONE = "question_one";

    public static final String IS_MAKER_CHECKER_ENABLED = "is.maker.checker.enabled";

    public static final String ANSWER_ONE = "answer_one";

    public static final String QUESTION_TWO = "question_two";

    public static final String ANSWER_TWO = "answer_two";

    public static final String CSV_TYPE_ONBOARD_USERS = "UPLOAD_ONBOARD_USERS";
    public static final String HOSTNAME_IDS_IAM_ACTIVEMQ = "ids.iam.activemq.hostname";
    public static final String HOSTNAME_POSTGRES ="postgres.hostname" ;
    public static final String IP_PORT_IDS_IAM = "ids.iam.port";
    public static final String IP_PORT_POSTGRES = "postgres.port";
    public static final String IP_PORT_ACTIVEMQ = "activemq_port";
    public static final String HOSTNAME_ACTIVEMQ = "activemq.hostname";
    public static final String IS_FIPS_ENABLED ="fips.state" ;
    public static final String ERROR_MESSAGE_ATTRIBUTE_UPDATE_REQUEST_ALREADY_PRESENT1 = "error.message.attribute.update.request.already.present1";
    public static final String ERROR_CODE_INVALID_SERACH_ATTRIBUTE ="error.code.invalid.search.attribute" ;
    public static final String UPDATE_PASSWORD_FAILED ="update.password.failed" ;
    public static final String ERROR_MESSAGE_SAME_PASSWORD = "error.message.same.password";
    public static final String ERROR_CODE_INVALID_PASSWORD ="error.code.invalid.password" ;
    public static final String ERROR_CODE_FALLOUT_INVALID_DATA ="error.code.fallout.invalid.data" ;
    public static final String ERROR_MESSAGE_FALLOUT_INVALID_DATA = "error.message.fallout.invalid.data";
    public static final String ERROR_MESSAGE_CONSENT_REQUIRED_NOT_ALLOWED ="error.message.consent.required.not.allowed" ;
    public static final String ERROR_CODE_CONSENT_REQUIRED_NOT_ALLOWED ="error.code.consent.required.not.allowed" ;
    public static final String HUMANIZED_CHANGE_ENTERPRISE_PIN_FAILED = "humanized.change.enterprise.pin.failed";
    public static final String HUMANIZED_CHANGE_APPLICATION_PIN_FAILED ="humanized.change.application.pin.failed" ;
    public static final String HUMANIZED_RESET_APPLICATION_PIN_FAILED = "humanized.reset.application.pin.failed";
    public static final String ERROR_MESSAGE_INVALID_FALLOUT_OPERATION ="error.message.invalid.fallout.operation" ;
    public static final String HUMANIZED_RESET_ENTERPRISE_PIN_FAILED ="humanized.reset.enterprise.pin.failed" ;
    public static final String ERROR_CODE_INVALID_FALLOUT_OPERATION = "error.code.invalid.fallout.operation";
    public static final String ERROR_CODE_INVALID_SEARCH_ATTRIBUTE = "error.code.invalid.search.attribute";
    public static final String ACCOUNT_POLICY_FAILED ="account.policy.not.satisfied" ;
    public static final String ERROR_CODE_USER_NAME_TOKEN_INVALID = "error.code.user.name.token.invalid";
    public static final String ERROR_MESSAGE_USER_NAME_TOKEN_INVALID = "error.message.user.name.token.invalid";
    public static final String ERROR_MESSAGE_INVALID_USER_STATE ="error.message.invalid.user.state" ;
    public static final String LOG_DESTINATION = "log.destination";
    public static final String LOG_DESTINATION_DB = "db";
    public static final String LOG_DESTINATION_FILE = "file";
    public static final String FALLOUT_PROCESS_START_TIME_MILLIS = "fallout.process.start.time.millis";
    public static final String FALLOUT_PROCESS_CLIENT_ID = "fallout.process.client.id";
    public static final String FALLOUT_PROCESS_CLIENT_SECRET = "fallout.process.client.secret";
    public static final String FALLOUT_PROCESS_BANK_ID = "fallout.process.bank.id";
    public static final String FALLOUT_PROCESS_USERNAME = "fallout.process.username";
    public static final String FALLOUT_PROCESS_PASSWORD = "fallout.process.password";
    public static final String FALLOUT_PROCESS_LANGUAGE_ID = "fallout.process.language.id";
    public static final String FALLOUT_PROCESS_CHANNEL_ID = "fallout.process.channel.id";
    public static final String FALLOUT_PROCESS_GRANT_TYPE = "fallout.process.grant.type";
    public static final String FALLOUT_PROCESS_API_KEY = "fallout.process.api.key";
    public static final String FALLOUT_PROCESS_DEH_STATUS = "fallout.process.deh.status";
    public static final String FALLOUT_PROCESS_FT42_STATUS = "fallout.process.ft42.status";
    public static final String FALLOUT_PROCESS_NO_RECORDS_HEADER_NAME = "fallout.process.no.record.header.name";
    public static final String FALLOUT_PROCESS_NO_RECORD_MESSAGE_CODE = "fallout.process.no.record.message.code";
    public static final String FALLOUT_PROCESS_DEH_NO_MOBILE_NO = "fallout.process.no.mobile.number.message";
    public static final String ACTUAL_MOBILE_NO = "actualMobileNo";
    public static final String TITLE = "title";
    public static final String FALLOUT_PROCESS_RETRY_COUNT = "fallout.process.retry.count";
    public static final String FALLOUT_DATA_SYNC_LAST_LOGIN_TIME ="lastSyncTime" ;
    public static final String ERROR_MESSAGE_UPDATE_FALLOUTSYNCDATA_ALREADY_PRESENT ="error.message.update.falloutsyncdata.already.present" ;
    public static final String DEH_FALLOUT_DATA_PROCESS = "dehFalloutDataProcess";
    public static final String DEH_FALLOUT_DATA_SYNC = "dehFalloutDataSync";
    public static final String NUMBER_OF_RECORDS_TO_BE_PROCESSED = "numberOfRecordsToBeProcessed";
    public static final String DATA_FETCH_FREQUENCY = "dataFetchFrequency";
    public static final String ERROR_CODE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT = "error.code.edit.fallout.config.already.present";
    public static final String ERROR_MESSAGE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT = "error.message.edit.fallout.config.already.present";
    public static final String HUMANIZED_EDIT_FALLOUT_CONFIG = "error.message.edit.fallout.config";
    public static final String ERROR_MESSAGE_FALLOUT_SYNC_DATA_NOT_FOUND ="error.message.fallout.sync.data.not.found" ;
    public static final String FALLOUT_FIPS_ENABLED = "fallout.process.fips.enabled";
    public static final String ATTRIBUTE_VERIFIER_CACHE_TIMEOUT_IN_SECONDS = "attribute.verifier.cache.timeout.in.seconds";
	public static final String ONBOARD_USER_URL = "onboard.user.url";
    public static final String APIGEE_XAPI_KEY_HEADER_NAME = "apigee.xapi.key.header.name";
    public static final String APIGEE_XAPI_KEY_HEADER_VALUE = "apigee.xapi.key.header.value";
    public static final String ERROR_DEV_MESSAGE_INVALID_REQUEST_BODY = "error.dev.message.invalid.request.body";
    public static final String HUMANIZED_ADD_CONFIG = "humanized.add.config.failed";
    public static final String CONFIG_TYPE = "configType";
    public static final String CONFIG_KEY = "key";
    public static final String CONFIG_VALUE = "value";

    public static final String ERROR_MESSAGE_ADD_CONFIG_ALREADY_PRESENT ="error.message.add.config.already.present" ;

    public static final String ERROR_MESSAGE_UPDATE_CONFIG_ALREADY_PRESENT ="error.message.update.config.already.present" ;

    public static final String HUMANIZED_UPDATE_CONFIG = "humanized.update.config.failed";
    public static final String ERROR_CODE_CONFIG_NOT_FOUND = "error.code.config.not.found";
    public static final String ERROR_MESSAGE_CONFIG_NOT_FOUND = "error.message.config.not.found";
    public static final String ERROR_CODE_CONFIG_ALREADY_PRESENT = "error.code.config.already.present";
    public static final String ERROR_MESSAGE_CONFIG_ALREADY_PRESENT = "error.message.config.already.present";


    public static final String ACCOUNT_LOCK_DURATION_IN_MILLIS = "account.lock.duration.in.millis";
    public static final String ADMIN_LOGIN_RETRIES = "admin.login.retries";
	public static final String CAM_SYNC_THREAD_POOL_SIZE = "cam.sync.thread.pool.size";
    public static final String IS_TEMPLATE_FROM_DB = "is.template.from.db" ;
    public static final String TEMPLATE_DETAILS_TYPE ="TDT" ;
    public static final String HTML_REGEX ="html.validation.regex" ;
    public static final String HREF_REGEX ="href.validation.regex" ;
    public static final String TEMP_ID = "TEMP-";
    public static final String SCHEDULER_FREQUENCY = "schedulerFrequency";
    public static final String TEMPLATE_TYPE ="TEMPLATE_TYPE" ;
    public static final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    public static final String MESSAGE_BODY = "MESSAGE_BODY";
    public static boolean isMock = false;
    public static final String IS_ATTRIBUTE_IN_UPPER_CASE = "is.attribute.in.uppercase" ;

    public static final String ADDRESS = "address";

    public static String RAW_DATA = "raw-data";
    
    public static final String CSV_FILE = "user-onboard.csv";
    
    public static final String FILE_SPERATOR = "/";

    public static final String AUTH42 = "AUTH42";

    public static final String DEVICE_UDID = "deviceUDID";

    public static final String DEVICE_STATE = "deviceState";
    
    public static final String DEVICE_ID = "deviceId";

    public static final String TOKENS = "tokens";

    public static final String TOKEN_UDID = "tokenUDID";

    public static final String TOKEN_TYPE = "type";

    public static final String MAKER = "MAKER";

    public static final String CHECKER = "CHECKER";
    public static final String APPLICATION_MAKER = "APPLICATION_MAKER";
    public static final String APPLICATION_CHECKER = "APPLICATION_CHECKER";
    public static final String APPLICATION_VIEWONLY = "APPLICATION_VIEWONLY";

    public static final String ACTIVE_DIRECTORY = "Active Directory";
    public static final String USER_GROUP_UNIQUE_ID = "Group Name";

    public static final Object ADD = "ADD";

    public static final Object APPLICATION_LIST = "applications";

    public static final Object CONNECTION_URL = "connectionUrl";

    public static final Object ADMIN_DOMAIN = "adminDomain";

    public static final Object ADMIN_CREDENTIAL = "adminCredential";

    public static final Object VENDOR = "vendor";

    public static final Object TYPE = "type";

    public static final String IDENTITY_PROVIDER_UNIQUE_ID = "name";

    public static final String KEY = "Key";

    public static final String VALUE = "Value";
    
    public static final String DEFAULT_IP_ADDRESS = "-1.-1.-1.-1";

	public static final String BUILD_VERSION = "3.4.1_GA";

    public static final String VALIDATION_REGEX = "validation_regex";

    public static final String IAM_LOG_FILE = "i-am.log";

    public static final String ATTRIBUTE_EDIT = "ATTRIBUTE_EDIT";

    public static final String ATTRIBUTE_DELETE = "ATTRIBUTE_DELETE";

    public static final String SEARCH_ATTRIBUTE = "searchAttribute";

    public static final String CUSTOME_ATTRIBUTES = "customeAttributes";

    public static final String MASK_PATTERN = "maskPattern";

    public static final String MASK_CHARACTER = "maskCharacter";

    public static final String SEARCH_ATTRIBUTE_NAME = "searchAttributeName";

    public static final String SEARCH_ATTRIBUTE_VALUE = "searchAttributeValue";

    public static final String CSV_SERVICE_NAME = "SERVICE_NAME";

    public static final String CSV_USER_ID = "USER_ID";

    public static final String CSV_MOBILE_NO = "MOBILE_NO";

    public static final String CSV_EMAIL_ID = "EMAIL_ID";

    public static final String CSV_USER_CREDENTIAL = "USER_CREDENTIAL";

    public static final String CSV_APPLICATION_ID = "APPLICATION_ID";
    
    public static final String CSV_TYPE_ONBOARD_USER = "ONBOARD_USERS";

    public static final String VALIDATE_LICENSE = "validate.license";

    public static final String ENTERPRISE_SECRET = "enterpriseSecret";
    public static final String ORIGIN = "ADAPTER";

    public static final String IS_CAM_ENABLED = "is.cam.enabled";

    public static final String CAM_REALM = "cam.realm";

    public static final String CAM_ADMIN_URL = "cam.admin.url";

    public static final String CAM_PI_CONTROL_CLIENT_ID = "cam.pi-control.clientId";

    public static final String CAM_PI_CONTROL_SECRET = "cam.pi-control.secret";

    public static final String ACCESS_TOKEN_LIFESPAN = "access.token.lifespan";

    public static final String CLIENT_SESSION_IDLE_TIMEOUT = "client.session.idle.timeout";
    public static final String CLIENT_SESSION_MAX_LIFESPAN = "client.session.max.lifespan";

    public static final String USE_REFRESH_CLIENT_CREDENTIAL = "client_credentials.use_refresh_token";

    public static final String BIND_OPERATION = "BIND";

    public static final String UNBIND_OPERATION = "UNBIND";



    public static final String LOG4J_CONFIG_FILE_PATH = "log4j.config.file.path";

    public static final String LOG4J_CONFIG_FILE = "log4j2.xml";

    public static final String URL_REGEX = "url.regex";

    public static final String SRAGatewaySettingsName_Regex = "SRA.gateway.settings.name.regex";
    public static final String URL = "url";

    public static final String FOLDER_FORTYTWO = "Fortytwo42";

    public static final String HOTP_LENGTH = "hotp.length";

    public static final String ADD_CHECKSUM = "add.checksum";

    public static final String SEED_LENGTH = "seed.length";

    public static final int COUNTER = 0;

    public static final String TTL = "application.ttlinseconds";

    public static final String ATTEMPT_COUNT = "application.attempt.count";

    public static final String QUEUE_PASSWORD = "queue.password";

    public static final String QUEUE_USERNAME = "queue.username";

    public static final String SMS_QUEUE_NAME = "sms.queue.name";

    public static final String EMAIL_QUEUE_NAME = "email.queue.name";

    public static final String NOTIFICATION_TYPE_SMS= "SMS";

    public static final String NOTIFICATION_TYPE_EMAIL = "EMAIL";

    public static final String APPLICATION_IDENTITY_STORE = "identitystore";

    public static final String ATTRIBUTES_NOT_PRESENT = "Attribute not present";

    public static final String ATTRIBUTE_NOT_VALID = "Attribute not valid";

    public static final String INVALID_NOTIFICATION_TYPE = "Invalid notification type";

    public static final String INVALID_AUTHENTICATION_TOKEN = "Invalid authentication token";
    
    public static final String COMMENT_VALIDATION_PATTERN = "input.validation.pattern";
    
    public static final String EMAIL_PATTERN = "email.pattern";
    
    public static final String ADHAR_PATTERN = "adharPattern";
    
    public static final String NRIC_PATTERN = "nricPattern";
    
    public static final String DOMAIN_NAME = "domain.name";
    
    public static final String ATTRIBUTE_TITLE = "attributeTitle";
    
    public static final String ATTRIBUTE_VERIFIER_TYPE = "verifierType";
    
    public static final String ATTRIBUTE_VERIFIER_ID = "verifierId";
    
    public static final String ATTRIBUTE_SOURCE_TYPE = "sourceType";

    public static final String DEFAULT_DOMAIN_NAME = "*";
    public static final String APPLICATION_SECRETE_VALIDITY = "application.secrete.validity";


    // cam rest api call related fields
    public static final String CAM_GRANT_TYPE = "grant_type";

    public static final String CAM_CLIENT_ID = "client_id";

    public static final String CAM_CLIENT_SECRET = "client_secret";

    public static final String CAM_SEARCH_ATTR_KEY = "searchAttributeKey";

    public static final String CAM_SEARCH_ATTR_VALUE = "searchAttributeValue";

    public static final String CAM_PASSWORD_FIELD = "password";

    public static final String CAM_USERNAME_FIELD = "username";

    public static final String CAM_SCOPE_FIELD = "scope";

    public static final int POOL_SIZE = 1;

    public static final String TLS_ENABLED   = "tls.enabled";
    
    public static final String RESET_PIN_STATUS = "Reset Pin Unblock Settings";

    public static final String BULK_UPLOAD_TYPE = "bulkUploadType";

    public static final String CSV_APPLICATION_SERVICES = "services";

    public static final String CSV_ACTION = "action";

    public static final String CSV_USER_CONSENT = "userConsent";

    public static final String MOBILE_REGEX = "^\\+91[1-9]\\d{9}$";

    public static final String JNI_POOL_MAP_CLEAR = "jni.pool.map.clear";

    public static final String IS_CRYPTO_ENABLED = "crypto.enabled";

    public static final String CRYPTO_DB_HOST = "crypto.db.host";

    public static final String CRYPTO_DB_PORT = "crypto.db.port";

    public static final String CRYPTO_DB_NAME = "crypto.db.name";

    public static final String CRYPTO_DB_USER = "crypto.db.username";

    public static final String CRYPTO_DB_PASS = "crypto.db.pass";

    public static final String UPDATE_ENABLE   = "update.enable";

    public static final String QR_CRON_EXPRESSION = "QR.cron.expression";

    public static final Object QR_LOGIN = "QR_LOGIN";

    public static final Object PREFIX = "Prefix";

    public static final String QR_PREFIX_REGEX = "QR.prefix.regex";

    public static final String CAM_CUSTOM_FIELDS_VALUE = "customFields";

    public static final String FAILED = "FAILED";

    public static final String ONBOARD_USER_STEP = "onboard.user.failure.step";

    public static final String STATE_MACHINE_WORKFLOW_UNIQUE_ID = "State machine workflow unique id";

    public static final String ACCOUNT_CUSTOM_STATE_MACHINE_UNIQUE_ID = "Account custom state machine unique id";

    public static final String INFINISPAN_HOST = "infinispan.host";

    public static final String INFINISPAN_PORT = "infinispan.port";

    public static final String INFINISPAN_USERNAME = "infinispan.username";

    public static final String INFINISPAN_PASSWORD = "infinispan.password";

    public static final String AUDIT_LOG_THREAD_POOL_SIZE = "audit-log.thread.size";
    public static final String INFINISPAN_ENABLED = "infinispan.enabled";

    public static final String AUDIT_LOG_QUEUE_NAME = "audit-log.queue.name";
    public static final String INFINISPAN_CRON_EXPRESSION = "infinispan.cron.expression";

    public static final String REQUEST_START_TIME = "requestStartTime";

    public static final String RESOURCE_LOG_QUEUE_NAME = "resource-log.queue.name";
    
    public static final String INFINISPAN_KEEP_ALIVE = "infinispan.keepAlive";
    public static final String INFINISPAN_SOCKET_TIMEOUT = "infinispan.socketTimeout";
    public static final String INFINISPAN_CONNECTION_TIMEOUT = "infinispan.connectionTimeout";
    public static final String INFINISPAN_MAX_RETRIES = "infinispan.retry.count";
    public static final String INFINISPAN_REC_LIFETIME = "infinispan.rec.lifetime";

    public static final String SUBSCRIBED_APPLICATION = "subscribedApplications";

    public static final String SEARCH_ATTRIBUTES = "searchAttributes";

    public static final String INVALID_CSV_DATA_RECORD= "Csv File Data Record";
    public static final String CSV_DB_TS = "DB_TS";
    public static final String CSV_BANK_ID = "BANK_ID";
    public static final String CSV_ORG_ID = "ORG_ID";
    public static final String CSV_FALLOUT_USER_ID = "USER_ID";
    public static final String CSV_FT42_USER_ID = "FT42_USER_ID";
    public static final String CSV_OLD_MOBILE_NO = "OLD_MOBILE_NO";
    public static final String CSV_NEW_MOBILE_NO = "NEW_MOBILE_NO";
    public static final String CSV_OPERATION = "OPERATION";
    public static final String CSV_DEH_STATUS = "DEH_STATUS";
    public static final String CSV_FT42_STATUS = "FT42_STATUS";
    public static final String CSV_REMARKS = "REMARKS";
    public static final String CSV_FREE_TEXT1 = "FREE_TEXT1";
    public static final String CSV_FREE_TEXT2 = "FREE_TEXT2";
    public static final String CSV_DEL_FLG = "DEL_FLG";
    public static final String CSV_R_MOD_ID = "R_MOD_ID";
    public static final String CSV_R_MOD_TIME = "R_MOD_TIME";
    public static final String CSV_R_CRE_ID = "R_CRE_ID";
    public static final String CSV_R_CRE_TIME = "R_CRE_TIME";



    public static final String INTERNAL_REQUEST_REFERENCE_NUMBER = "X-InternalReqRefNum";
    public static final String REQUEST_ATTRIBUTES = "attributes";

    public static final String _HYPHEN = "-";

    public static final String RESOURCE_LOG_LOCAL_DB = "resource-log.local.db";


    //3.4.1_TOTP
    //=========================================================================================
    public static final String ALGORITHM = "algorithm";
    public static final String DEFAULT_TOTP_HASH_ALGORITHM = "default.totp.hash.algorithm";
    public static final String DEFAULT_TOTP_NUMBER_OF_DIGITS = "default.totp.number.of.digits";
    public static final String DEFAULT_TOTP_EXPIRY_IN_SEC = "default.totp.expiry.in.sec";
    //=========================================================================================


    //error codes

    public static final String ERROR_CODE_APPLICATION_NOT_FOUND="error.code.application.not.found";
    public static final String ERROR_CODE_APPLICATION_ALREADY_PRESENT="error.code.application.already.present";
    public static final String ERROR_CODE_USER_UPDATE_PENDING="error.code.user.update.pending";
    public static final String  ERROR_CODE_INVALID_TOKEN="error.code.invalid.token";
    public static final String ERROR_CODE_INVALID_ROLES="error.code.invalid.roles";
     public static final  String ERROR_CODE_PERMISSION_DENIED="error.code.permission.denied";
    public static final String ERROR_CODE_IO_EXCEPTION="error.code.io.exception";
    public static final String ERROR_CODE_INVALID_APPLICATION_ID_OR_PASSWORD="error.code.invalid.application.id.or.password";
    public static final String ERROR_CODE_USER_SERVICE_BINDING_FAILED="error.code.user.service.binding.failed";
    public static final String ERROR_CODE_INVALID_MOBILE ="error.code.invalid.mobile";
    public static final String  ERROR_CODE_INVALID_APPLICATION_FOR_USER="error.code.invalid.application.for.user";
    public static final String ERROR_CODE_CERTIFICATE_PINNING_FAILURE="error.code.certificate.pinning.failure";
    public static final String ERROR_CODE_SERVER_ERROR="error.code.server.error";
    public static final String ERROR_CODE_ACCOUNT_NOT_FOUND="error.code.account.not.found";
    public static final String ERROR_CODE_IAMCI2_CALL_FAILED="error.code.iamci2.call.failed";
    public static final String ERROR_CODE_LOOKUP_ID_COMPULSORY="error.code.lookup.id.compulsory";
    public static final String ERROR_CODE_APPROVAL_ATTEMPT_TYPE_INVALID="error.code.approval.attempt.type.invalid";
    public static final String ERROR_CODE_INVALID_CONSUMER_ID="error.code.invalid.consumer.id";
    //public static final String ERROR_CODE_APPROVAL_ATTEMPT_NOT_APPROVED="error.code.approval.attempt.not.approved";
    public static final String ERROR_CODE_APPROVAL_ATTEMPT_NOT_APPROVED="error.code.approval.attempt.not.approved";
    public static final String ERROR_CODE_INVALID_APPLICATION_ID="error.code.invalid.application.id";
    public static final String ERROR_CODE_INVALID_CLIENT_ID="error.code.invalid.client.id";
    public static final String ERROR_CODE_INVALID_DATA="error.code.invalid.data";
    public static final String ERROR_CODE_USER_BLOCK="error.code.user.block";
    public static final String ERROR_CODE_CONSUMER_NOT_PRESENT_DATA="error.code.consumer.not.present.data";
    public static final String ERROR_CODE_SERVICE_NOT_FOUND="error.code.service.not.found";
    public static final String ERROR_CODE_INVALID_SERVICE_FOR_APPLICATION="error.code.invalid.service.for.application";
    public static final String  ERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT="error.code.user.service.binding.already.present";
    public static final String ERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND="error.code.user.service.binding.not.found";
    public static final String ERROR_CODE_USER_NOT_FOUND="error.code.user.not.found";
    public static final String ERROR_CODE_TRANSACTION_NOT_FOUND="error.code.transaction.not.found";
    public static final String ERROR_CODE_INVALID_TRANSACTION_TYPE="error.code.invalid.transaction.type";
    public static final String ERROR_CODE_VERIFICATION_FAILED="error.code.verification.failed";
    public static final String ERROR_CODE_USER_NAME_PASSWORD_INVALID="error.code.user.name.password.invalid";
    public static final String ERROR_CODE_INVALID_APPLICATION_TYPE="error.code.invalid.application.type";
    public static final String ERROR_CODE_BLOCKED_FOR_RESET_PIN="error.code.blocked.for.reset.pin";
    public static final String ERROR_CODE_RESET_PIN_COMPLETED="error.code.reset.pin.completed";
    public static final String ERROR_CODE_USER_SERVICE_BINDING_NOT_ACTIVE="error.code.service.binding.not.active";
    public static final String ERROR_CODE_USER_SERVICE_ALREADY_PRESENT="error.code.user.service.already.present";
    public static final String ERROR_CODE_USER_SERVICE_NOT_FOUND="error.code.service.not.found";
    public static final String ERROR_CODE_USER_DATA_EMPTY="error.code.user.data.empty";
    public static final String ERROR_CODE_REQUEST_NOT_FOUND="error.code.request.not.found";
    public static final String ERROR_CODE_INVALID_APPLICATION_NAME="error.code.invalid.application.name";
    public static final String ERROR_CODE_USER_SERVICE_BINDING_BLOCKED="error.code.user.service.binding.blocked";
    public static final String ERROR_CODE_APPLICATION_PASSWORD="error.code.application.password";
    public static final String ERROR_CODE_SERVICES_ALREADY_ENABLED_FOR_USER="error.code.service.already.enabled.for.user";
    public static final String ERROR_CODE_USER_APPLICATION_BINDING_NOT_FOUND="error.code.user.application.binding.not.found";
    public static final String ERROR_CODE_ATTRIBUTE_NOT_FOUND="error.code.attribute.not.found";
    public static final String ERROR_CODE_EVIDENCE_NOT_FOUND="error.code.evidence.not.found";
    public static final String ERROR_CODE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND="error.code.pending.authentication.attempt.not.found";
    public static final String ERROR_CODE_ATTRIBUTE_ADDITION_ALREADY_PRESENT="error.code.attribute.addition.already.present";
    public static final String ERROR_CODE_INVALID_REQUEST="error.code.invalid.request";
    public static final String ERROR_CODE_ATTRIBUTE_ALREADY_PRESENT="error.code.attribute.already.present";
    public static final String ERROR_CODE_ALREADY_APPROVED="error.code.already.approved";
    public static final String ERROR_CODE_ATTRIBUTE_REQUEST_ALREADY_PRESENT="error.code.attribute.request.already.present";
    public static final String ERROR_CODE_REQUEST_IS_TIMEOUT="error.code.request.is.timeout";
    public static final String ERROR_CODE_ATTRIBUTE_UPDATION_ALREADY_PRESENT="error.code.attribute.updation.already.present";
    public static final String ERROR_CODE_ATTRIBUTE_DELETION_ALREADY_PRESENT="error.code.attribute.deletion.already.present";
    public static final String ERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME="error.code.existing.and.updation.data.is.same";
    public static final String ERROR_CODE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT="error.code.attribute,master.request.already.present";
    public static final String ERROR_CODE_REQUEST_ALREADY_SENT_TO_CHECKER="error.code.already.send.to.checker";
    public static final String ERROR_CODE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER="error.code.evidence.request.already.send.to.user";
    public static final String ERROR_CODE_ATTRIBUTE_ADDITION_ID_INVALID="error.code.attribute.addition.id.invalid";
    public static final String ERROR_CODE_INVALID_ATTRIBUTE_TYPE="error.code.invalid.attribute.type";
    public static final String ERROR_CODE_INVALID_ATTRIBUTE_NAME="error.code.invalid.attribute.name";
    public static final String ERROR_CODE_FILE_IS_EMPTY="error.code.file.is.empty";
    public static final String ERROR_CODE_FILE_NOT_SUPPORTED="error.code.file.not.supported";
    public static final String ERROR_CODE_APPLICATION_ONBOARD_ALREADY_PRESENT="error.code.attribute.addition.already.present";
    public static final String ERROR_CODE_EDIT_ACCOUNT_FAILED="error.code.edit.account.failed";
    public static final String ERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE="error.code.attribute.value.is.invalid";
    public static final String ERROR_CODE_EVIDENCE_REQUEST_ALREADY_REJECTED="error.code.evidence.request.already.rejected";
    public static final String ERROR_CODE_APPLICATION_EDIT_ALREADY_PRESENT="error.code.application.edit.already.present";
    public static final String ERROR_CODE_USER_ONBOARD_ALREADY_PRESENT="error.code.user.onboard.already.present";
    public static final String ERROR_CODE_CSV_DOWNLOAD_FAILED="error.code.csv.download.failed";
    public static final String ERROR_CODE_USER_SESSION_IS_ALREADY_ACTIVE="error.code.user.session.is.already.active";
    public static final String ERROR_CODE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND="error.code.sra.gateway.setting.request.data.not.found";
    public static final String ERROR_CODE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT="error.code.sra.gateway.setting.onboard.request.already.present";
    public static final String ERROR_CODE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT="error.code.sra.gateway.setting.update.request.already.present";
    public static final String ERROR_CODE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT="error.code.sra.gateway.setting.delete.request.already.present";
    public static final String ERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND="error.code.sra.gateway.setting.not.found";
    public static final String ERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND="error.code.sra.application.gateway.setting.rel.not.found";
    public static final String ERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND="error.code.application.gateway.setting.not.found";
    public static final String ERROR_CODE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT="error.code.sra.gateway.setting.data.already.present";
    public static final String ERROR_CODE_ENTERPRISE_NOT_FOUND="error.code.enterprise.not.found";
    public static final String ERROR_CODE_SRA_GATEWAY_SETTING_ALREADY_BINDED="error.code.sra.gateway.setting.already.binded";
    public static final String ERROR_CODE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT="error.code.policy.onboard.request.already.present";
    public static final String ERROR_CODE_POLICY_EDIT_REQUEST_ALREADY_PRESENT="error.code.policy.edit.request.already.present";
    public static final String ERROR_CODE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT="error.code.device.edit.request.already.present";
    public static final String ERROR_CODE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT="error.code.edit.request.already.present";
    public static final String ERROR_CODE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT="error.code.user.service.bind.request.already.present";
    public static final String ERROR_CODE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT="error.code.user.service.unbind.request.already.present";
    public static final String ERROR_CODE_USER_GROUP_ALREADY_EXISTS="error.code.user.group.already.exists";
    public static final String ERROR_CODE_USER_GROUP_NOT_FOUND="error.code.user.group.not.found";
    public static final String ERROR_CODE_USER_GROUP_DATA_SAME="error.code.user.group.data.same";
    public static final String ERROR_CODE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT="error.code.user.group.create.request.already.present";
    public static final String ERROR_CODE_USER_GROUP_BINDING_ALREADY_EXISTS="error.code.user.group.binding.already.exists";
    public static final String ERROR_CODE_USER_GROUP_BINDING_NOT_FOUND="error.code.user.group.binding.not.found";
    public static final String ERROR_CODE_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS="error.code.group.application.binding.already.exists";
    public static final String ERROR_CODE_USER_GROUP_APPLICATION_BINDING_NOT_FOUND="error.code.user.group.application.binding.not.found";
    public static final String ERROR_CODE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT="error.code.identity.provider.create.request.already.present";
    public static final String ERROR_CODE_APPLICATION_ALREDY_BINDED="error.code.application.already.binded";
    public static final String ERROR_CODE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT="error.code.user.group.update.request.already.present";
    public static final String ERROR_CODE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT="error.code.user.usergroup.mapping.request.already.present";
    public static final String ERROR_CODE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT="error.code.application.usergroup.mapping.request.already.present";
    public static final String ERROR_CODE_CONTACT_REQUEST_ALREADY_PRESENT="error.code.contact.request.already.present";
    public static final String ERROR_CODE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT="error.code.contact.edit.request.already.present";
    public static final String ERROR_CODE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT="error.code.mapper.create.request.already.present";
    public static final String ERROR_CODE_MAPPER_NOT_FOUND="error.code.mapper.not.found";
    public static final String ERROR_CODE_AD_SYNC_FAILED="error.code.ad.sync.failed";
    public static final String ERROR_CODE_SRA_DETAILS_NOT_MATCHED="error.code.sra.details.not.matched";
    public static final String ERROR_CODE_INVALID_FILE_NAME="error.code.invalid.file.name";
    public static final String ERROR_CODE_FILE_READ_FAILED="error.code.file.read.failed";
    public static final String ERROR_CODE_LICENSE_CHECK_FAILED="error.code.license.check.failed";
    public static final String ERROR_CODE_INPROGRESS="error.code.inprogress";
    public static final String ERROR_CODE_ENTERPRISE_ALREADY_ONBOARDED="error.code.enterprise.already.onboarded";
    public static final String ERROR_CODE_ENTERPRISE_NOT_ONBOARDED="error.code.enterprise.not.onboarded";
    public static final String ERROR_CODE_BINDING_FAILED="error.code.binding.failed";
    public static final String ERROR_CODE_NOTIFICATION_TYPE_NOT_FOUND="error.code.notification.type.not.found";
    public static final String  ERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_USER="error.code.two.factor.auth.disabled.for.user";
    public static final String ERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION="error.code.two.factor.auth.disabled.for.application";
    public static final String ERROR_CODE_INVALID_CONNECTION_SETTINGS="error.code.invalid.connection.setting";
    public static final String ERROR_CODE_ADFS_DETAIL_NOT_FOUND="error.adfs.detail.not.found";
    public static final String ERROR_CODE_AD_DETAIL_NOT_FOUND="error.code.ad.detail.not.found";
    public static final String ERROR_CODE_SAME_MAKER_REQUEST_APPROVED="error.code.same.maker.request.approved";
    public static final String ERROR_CODE_INVALID_USERID_PASSWORD="error.code.invalid.userid.password";
    public static final String ERROR_CODE_USER_CREDENTIALS_NOT_PRESENT="error.code.credentials.not.present";
    public static final String ERROR_CODE_EVIDENCE_IS_REQUIRED="error.code.evidence.is.required";
    public static final String ERROR_CODE_INVALID_TYPE="error.code.invalid.type";
    public static final String ERROR_CODE_APPLICATION_NOT_ACTIVE="error.code.application.not.active";
    public static final String ERROR_CODE_PARSING_CSV="error.code.parsing.csv";
    public static final String ERROR_CODE_REQUEST_NOT_PENDING="error.code.request.not.pending";
    public static final String ERROR_CODE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT="error.code.attribute.master.addition.already.present";
    public static final String ERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND="error.code.attribute.metadata.not.found";
    public static final String ERROR_CODE_VERIFIER_NOT_FOUND="error.code.verifier.not.found";
    public static final String ERROR_CODE_EVIDENCE_REQUEST_ALREADY_APPROVED="error.code.evidence.request.already.approved";
    public static final String ERROR_CODE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT="error.code.usergroup.delete.request.already.present";
    public static final String ERROR_CODE_ATTRIBUTE_NOT_PRESENT="error.code.attribute.not.present";
    public static final String ALREADY_PRESENT_IN_SYSTEM_CODE="already.present.in.system.code";
    public static final String VALIDATION_ERROR_CODE="validation.error.code";
    public static final String USER_NOT_FOUND="user.not.found";
    public static final String ERROR_CODE_INTERNAL_SERVER_ERROR="error.code.internal.server.error";
    public static final String ERROR_CODE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT="error.code.state.machine.workflow.onboard.request.already.present";
    public static final String ERROR_CODE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT="error.code.state.machine.workflow.update.request.already.present";
    public static final String ERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT="error.code.account.custom.state.machine.onboard.request.already.present";
    public static final String ERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT="error.code.account.custom.state.machine.update.request.already.present";
    public static final String ERROR_CODE_INVALID_ACCOUNT_ID="error.code.invalid.account.id";
    public static final String ERROR_CODE_TEMPLATE_NOT_FOUND = "error.code.template.not.found";

    //error code messages
    public static final String ERROR_MESSAGE_INVALID_DATA="error.messages.invalid.data";
    public static final String ERROR_MESSAGE_TRANSACTION_ALREADY_EXISTS="error.message.transaction.already.exists";
    public static final String ERROR_MESSAGE_USER_BLOCK="error.message.user.block";
    public static final String ERROR_MESSAGE_CONSUMER_NOT_PRESENT_DATA="error.message.consumer.not.present.data";
    public static final String ERROR_MESSAGE_SERVICE_NOT_FOUND="error.message.service.not.found";
    public static final String ERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION="error.message.invalid.service.for.application";
    public static final String ERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT="error.message.user.service.binding.already.present";
    public static final String ERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND="error.message.user.service.binding.not.found";
    public static final String ERROR_MESSAGE_USER_NOT_FOUND="error.message.user.not.found";
    public static final String ERROR_MESSAGE_TRANSACTION_NOT_FOUND="error.message.transaction.not.found";
    public static final String ERROR_MESSAGE_INVALID_TRANSACTION_TYPE="error.message.invalid.transaction.type";
    public static final String ERROR_MESSAGE_VERIFICATION_FAILED="error.message.verification.failed";
    public static final String ERROR_MESSAGE_USER_NAME_PASSWORD_INVALID="error.message.user.name.password.invalid";
    public static final String ERROR_MESSAGE_INVALID_APPLICATION_TYPE="error.message.invalid.application.type";
    public static final String ERROR_MESSAGE_BLOCKED_FOR_RESET_PIN="error.message.blocked.for.reset.pin";
    public static final String ERROR_MESSAGE_RESET_PIN_COMPLETED="error.message.reset.pin.completed";
    public static final String ERROR_MESSAGE_USER_SERVICE_BINDING_NOT_ACTIVE="error.message.user.service.binding.not.active";
    public static final String ERROR_MESSAGE_USER_SERVICE_ALREADY_PRESENT="error.message.user.service.already.present";
    public static final String ERROR_MESSAGE_USER_SERVICE_NOT_FOUND="error.message.user.service.not.found";
    public static final String ERROR_MESSAGE_USER_DATA_EMPTY="error.message.user.data.empty";
    public static final String ERROR_MESSAGE_REQUEST_NOT_FOUND="error.message.request.not.found";
    public static final String ERROR_MESSAGE_INVALID_APPLICATION_NAME="error.message.invalid.application.name";
    public static final String ERROR_MESSAGE_USER_SERVICE_BINDING_BLOCKED="error.message.user.service.binding.blocked";
    public static final String ERROR_MESSAGE_APPLICATION_PASSWORD="error.message.application.password";
    public static final String ERROR_MESSAGE_SERVICES_ALREADY_ENABLED_FOR_USER="error.message.services.already.enabled.for.user";
    public static final String ERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND="error.message.user.application.binding.not.found";
    public static final String ERROR_MESSAGE_ATTRIBUTE_NOT_FOUND="error.message.attribute.not.found";
    public static final String ERROR_MESSAGE_EVIDENCE_NOT_FOUND="error.message.evidence.not.found";
    public static final String ERROR_MESSAGE_PENDING_AUTHENTICATION_ATTEMPT_NOT_FOUND="error.message.pending.authentication.attempt.not.found";
    public static final String ERROR_MESSAGE_ATTRIBUTE_ADDITION_ALREADY_PRESENT="error.message.attribute.addition.already.present";
    public static final String ERROR_MESSAGE_INVALID_REQUEST="error.message.invalid.request";
    public static final String ERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT="error.message.attribute.already.present";
    public static final String ERROR_MESSAGE_ATTRIBUTE_REQUEST_ALREADY_PRESENT="error.message.attribute.request.already.present";
    public static final String ERROR_MESSAGE_REQUEST_IS_TIMEOUT="error.message.request.is.timeout";
    public static final String ERROR_MESSAGE_ATTRIBUTE_UPDATION_ALREADY_PRESENT="error.message.attribute.updation.already.present";
    public static final String ERROR_MESSAGE_ATTRIBUTE_DELETION_ALREADY_PRESENT="error.message.attribute.deletion.already.present";
    public static final String ERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME="error.message.existing.and.updation.data.is.same";
    public static final String ERROR_MESSAGE_ATTRIBUTE_MASTER_REQUEST_ALREADY_PRESENT="error.message.attribute.master.request.already.present";
    public static final String ERROR_MESSAGE_REQUEST_ALREADY_SENT_TO_CHECKER="error.message.request.already.send.to.checker";
    public static final String ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_SENT_TO_USER="error.message.evidence.request.already.send.to.user";
    public static final String ERROR_MESSAGE_ATTRIBUTE_ADDITION_ID_INVALID="error.message.attribute.addition.id.invalid";
    public static final String ERROR_MESSAGE_INVALID_ATTRIBUTE_TYPE="error.message.invalid.attribute.type";
    public static final String ERROR_MESSAGE_INVALID_ATTRIBUTE_NAME="error.message.invalid.attribute.name";
    public static final String ERROR_MESSAGE_FILE_IS_EMPTY="error.message.file.is.empty";
    public static final String ERROR_MESSAGE_APPLICATION_ONBOARD_ALREADY_PRESENT="error.message.application.onboard.already.present";
    public static final String ERROR_MESSAGE_EDIT_ACCOUNT_FAILED="error.message.edit.account.failed";
    public static final String ERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE="error.message.attribute.value.is.invalid";
    public static final String ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_REJECTED="error.message.evidence.request.already.rejected";
    public static final String ERROR_MESSAGE_APPLICATION_EDIT_ALREADY_PRESENT="error.message.application.edit.already.present";
    public static final String ERROR_MESSAGE_USER_ONBOARD_ALREADY_PRESENT="error.message.user.onboard.already.present";
    public static final String ERROR_DEVELOPER_CSV_DOWNLOAD_FAILED="error.developer.csv.download.failed";
    public static final String ERROR_MESSAGE_USER_SESSION_IS_ALREADY_ACTIVE="error.message.user.session.is.already.active";
    public static final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND="error.message.sra.gateway.setting.request.data.not.found";
    public static final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT="error.message.sra.gateway.setting.onboard.request.already.present";
    public static final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT="error.message.sra.gateway.setting.update.request.already.present";
    public static final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT="error.message.sra.gateway.setting.delete.request.already.present";
    public static final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND="error.message.sra.gateway.setting.not.found";
    public static final String ERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND="error.message.sra.application.gateway.setting.rel.not.found";
    public static final String ERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND="error.message.sra.application.gateway.setting.not.found";
    public static final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT="error.message.sra.gateway.setting.data.already.present";
    public static final String ERROR_MESSAGE_ENTERPRISE_NOT_FOUND="error.message.enterprise.not.found";
    public static final String ERROR_MESSAGE_SRA_GATEWAY_SETTING_ALREADY_BINDED="error.message.sra.gateway.setting.already.binded";
    public static final String ERROR_MESSAGE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT="error.message.policy.onboard.request.already.present";
    public static final String ERROR_MESSAGE_POLICY_EDIT_REQUEST_ALREADY_PRESENT="error.message.policy.edit.request.already.present";
    public static final String ERROR_MESSAGE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT="error.message.device.edit.request.already.present";
    public static final String ERROR_MESSAGE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT="error.message.token.edit.request.already.present";
    public static final String ERROR_MESSAGE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT="error.message.user.service.bind.request.already.present";
    public static final String ERROR_MESSAGE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT="error.message.user.service.unbind.request.already.present";
    public static final String ERROR_MESSAGE_USER_GROUP_NOT_FOUND="error.message.user.group.not.found";
    public static final String ERROR_MESSAGE_USER_GROUP_DATA_SAME="error.message.user.group.data.same";
    public static final String ERROR_MESSAGE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT="error.message.user.group.create.request.already.present";
    public static final String ERROR_MESSAGE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT="error.message.identity.provider.create.request.already.present";
    public static final String ERROR_MESSAGE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT="error.message.user.group.update.request.already.present";
    public static final String ERROR_MESSAGE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT="error.message.user.usergroup.mapping.request.already.present";
    public static final String ERROR_MESSAGE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT="error.message.application.usergroup.mapping.request.already.present";
    public static final String ERROR_MESSAGE_CONTACT_ONBOARD_REQUEST_ALREADY_PRESENT="error.message.contact.onboard.already.present";
    public static final String ERROR_MESSAGE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT="error.message.contact.edit.request.already.present";
    public static final String ERROR_MESSAGE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT="error.message.mapper.create.request.already.present";
    public static final String ERROR_MESSAGE_MAPPER_NOT_FOUND="error.message.mapper.not.found";
    public static final String ERROR_MESSAGE_SRA_DETAILS_NOT_MATCHED="error.message.sra.details.not.matched";
    public static final String ERROR_MESSAGE_INVALID_FILE_NAME="error.message.invalid.file.name";
    public static final String ERROR_MESSAGE_INPROGRESS="error.message.inprogress";
    public static final String ERROR_MESSAGE_ENTERPRISE_ALREADY_ONBOARDED="error.message.enterprise.already.onboarded";
    public static final String ERROR_MESSAGE_NOTIFICATION_TYPE_NOT_FOUND="error.message.notification.type.not.found";
    public static final String ERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_USER="error.message.two.factor.auth.disabled.for.user";
    public static final String ERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION="error.message.two.factor.auth.disabled.for.application";
    public static final String ERROR_MESSAGE_INVALID_CONNECTION_SETTINGS="error.message.invalid.connection.settings";
    public static final String ERROR_MESSAGE_ADFS_DETAIL_NOT_FOUND="error.message.adfs.detail.not.found";
    public static final String ERROR_MESSAGE_AD_DETAIL_NOT_FOUND="error.message.ad.detail.not.found";
    public static final String ERROR_MESSAGE_SAME_MAKER_REQUEST_APPROVED="error.message.same.maker.request.approved";
    public static final String ERROR_MESSAGE_INVALID_USERID_PASSWORD="error.message.invalid.userid.password";
    public static final String ERROR_MESSAGE_INVALID_TYPE="error.message.invalid.type";
    public static final String ERROR_MESSAGE_PARSING_CSV="error.message.parsing.csv";
    public static final String ERROR_MESSAGE_REQUEST_NOT_PENDING="error.message.request.not.found";
    public static final String ERROR_MESSAGE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT="error.message.attribute.master.addition.already.present";
    public static final String ERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND="error.message.attribute.metadata.not.found";
    public static final String ERROR_MESSAGE_VERIFIER_NOT_FOUND="error.message.verifier.not.found";
    public static final String ERROR_MESSAGE_EVIDENCE_REQUEST_ALREADY_APPROVED="error.message.evidence.request.already.approved";
    public static final String ERROR_MESSAGE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT="error.message.user.group.delete.request.already.present";
    public static final String ERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT="error.message.attribute.not.present";
    public static final String ERROR_MESSAGE_INTERNAL_SERVER_ERROR="error.message.internal.server.error";
    public static final String ERROR_MESSAGE_USER_BINDING_ALREDY_PRESENT="error.message.user.binding.already.present";
    public static final String ERROR_MESSAGE_INVALID_DATA_OTP="error.message.invalid.data.otp";
    public static final String ERROR_DEV_MESSAGE_INVALID_DATA="error.dev.message.invalid.data";
    public static final String ERROR_DEV_MESSAGE_INVALID_TRANSACTION_ID="error.message.invalid.transaction.id";
    public static final String ERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID="error.dev.message.invalid.sign.transaction.id";
    public static final String ERROR_DEV_MESSAGE_INVALID_STATUS="error.dev.message.invalid.status";
    public static final String ERROR_DEV_MESSAGE_INVALID_TOKEN="error.dev.message.invalid.token";
    public static final String ERROR_MESSAGE_USER_PROVIDED_INVALID_CLIENT_ID_OR_SECRET="error.message.user.provided.invalid.client.id.or.secrete";
    public static final String ERROR_MESSAGE_INVALID_ESC_FILE_NAME="error.message.invalid.esc.file.name";
    public static final String ERROR_MESSAGE_INVALID_FILE_TYPE="error.message.invalid.file.type";
    public static final String ERROR_MESSAGE_ATTRIBUTE_PASSWORD="error.message.attribute.password";
    public static final String ERROR_MESSAGE_USER_PASSWORD="error.message.user.password";
    public static final String ERROR_MESSAGE_UPDATE_APPROVAL_ATTEMPT_FAILED="error.message.update.approval.attempt.failed";
    public static final String ERROR_MESSAGE_INVALID_VALUE="error.message.invalid.value";
    public static final String ERROR_MESSAGE_EMPTY_CSV="error.message.empty.csv";
    public static final String ERROR_MESSAGE_NOT_SUPPORTED="error.message.not.supported";
    public static final String ERROR_MESSAGE_EVIDENCE_EXPORT_EVIDENCE_FAILED="error.message.evidence.export.evidence.failed";
    public static final String ERROR_MESSAGE_EDIT_CREDENTIAL_FALIED="error.message.edit.credential.failed";
    public static final String ERROR_MESSAGE_APPLICATION_ALREADY_BINDED="error.message.application.already.binded";
    public static final String ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_EXPIRED_LICENSE="error.message.licence.check.failed.due.to.expired.license";
    public static final String ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_APPLICATION_EXCEEDED="error.message.license.check.failed.due.to.application.exceeded";
    public static final String ERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_USERS_EXCEEDED="error.message.license.check.failed.due.to.user.exceed";
    public static final String ERROR_MESSAGE_ENTERPRISE_NOT_ONBOARDED="error.message.enterprise.not.onboarded";
    public static final String ERROR_MESSAGE_HOTP_GENERATION_FAILED="error.message.hotp.generation.failed";
    public static final String ERROR_MESSAGE_HOTP_VALIDATION_FAILED="error.message.hotp.validation.failed";

    public static final String ERROR_MESSAGE_TEMPLATE_NOT_FOUND = "error.message.template.not.found";

    public static final String HUMANIZED_MESSAGE_AUTHENTICATION_FAILED="humanised.message.authentication.failed";
    public static final String HUMANIZED_AUTHENTICATION_FAILED="humanised.authentication.failed";
    public static final String HUMANIZED_GET_ADMIN_FAILED="humanised.get.admin.failed";
    public static final String HUMANIZED_GET_USERS_FAILED="humanized.get.users.failed";
    public static final String HUMANIZED_GET_USER_STATUS_FAILED="humanised.get.user.status.failed";
    public static final String HUMANIZED_GET_SERVICES_FAILED="humanised.get.service.failed";
    public static final String HUMANIZED_GET_APPLICATION_FAILED="humanised.get.application.failed";
    public static final String HUMANIZED_GET_APPROVAL_ATTEMPT_FAILED="humanised.get.approval.attempt.failed";
    public static final String HUMANIZED_USER_UDATE_FAILED="humanised.user.update.failed";
    public static final String HUMANIZED_USER_ONBOARD_FAILED="humanised.user.onboard.failed";
    public static final String HUMANIZED_CHANGE_PASSWORD_FAILED="humanized.change.password.failed";
    public static final String HUMANIZED_POLICY_ONBOARD_FAILED="humanized.policy.onboard.failed";
    public static final String HUMANIZED_CONTACT_ONBOARD_FAILED="humanized.contact.onboard.failed";
    public static final String HUMANIZED_GET_POLICY_FAILED="humanized.get.policy.failed";
    public static final String HUMANIZED_GET_CONTACT_FAILED="humanized.get.contact.failed";
    public static final String HUMANIZED_POLICY_EDIT_FAILED="humanized.policy.edit.failed";
    public static final String HUMANIZED_CONTACT_EDIT_FAILED="humanized.contact.edit.failed";
    public static final String HUMANIZED_USER_APPROVAL_FAILED="humanized.user.approval.failed";
    public static final String HUMANIZED_USER_BINDING_FAILED="humanized.user.binding.failed";
    public static final String HUMANIZED_USER_UNBINDING_FAILED="humanized.user.unbinding.failed";
    public static final String HUMANIZED_FILE_UPLOAD_FAILED="humanized.file.upload.failed";
    public static final String HUMANIZED_APPLICATION_SECRET_FAILED="humanized.application.secrete.failed";
    public static final String HUMANIZED_GET_DECRYPTION_FAILED="humanized.get.decryption.failed";
    public static final String HUMANIZED_APPROVAL_ATTEMPT_UPDATE_FAILED="humanized.approval.attempt.update.failed";
    public static final String HUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED="humanized.approval.attempt.creation.failed";
    public static final String HUMANIZED_APPLICATION_DELETION_FAILED="humanized.application.deletion.failed";
    public static final String HUMANIZED_GET_USER_SUBSCRIPTION="humanized.get.user.subscription";
    public static final String HUMANIZED_UNBIND_SERVICES="humanized.unbind.services";
    public static final String HUMANIZED_BIND_SERVICES="humanized.bind.service";
    public static final String HUMANIZED_ERROR_AUDIT_TRAIL="humanized.error.audit.trail";
    public static final String HUMANIZED_ERROR_VALIDATE_PASSWORD="humanized.error.validation.password";
    public static final String HUMANIZED_LOGOUT_FAILED="humanized.logout.failed";
    public static final String HUMANIZED_REQUEST_APPROVAL_FAILED="humanized.request.approval.failed";
    public static final String HUMANIZED_GET_REQUEST_FAILED="humanized.get.request.failed";
    public static final String HUMANIZED_VERIFICATION_FAILED="humanized.verification.failed";
    public static final String HUMANIZED_ATTRIBUTE_ADDITION_FAILED="humanized.attribute.addition.failed";
    public static final String HUMANIZED_GET_ATTRIBUTE_FAILED="humanized.get.attribute.failed";
    public static final String HUMANIZED_GET_CONFIG_FAILED="humanized.get.config.failed";
    public static final String HUMANIZED_UPLOAD_ATTRIBUTE_REQUEST="humanized.upload.attribute.request";
    public static final String HUMANIZED_GET_ATTRIBUTE="humanized.get.attribute";
    public static final String HUMANIZED_REQUEST_ATTRIBUTE="humanized.request.attribute";
    public static final String HUMANIZED_GET_EVIDENCE_STATUS="humanized.get.evidence.status";
    public static final String HUMANIZED_APPROVE_EVIDENCE_REQUEST="humanized.approve.evidence.request";
    public static final String HUMANIZED_EVIDENCE_REQUEST="humanized.evidence.request";
    public static final String HUMANIZED_GET_ENTERPRISE="humanized.get.enterprise";
    public static final String HUMANIZED_GET_VERIFIERS="humanized.get.verifiers";
    public static final String HUMANIZED_GET_ATTRIBUTE_REQUEST="humanized.get.attribute.request";
    public static final String HUMANIZED_ONBOARD_APPLICATION="humanized.onboard.application";
    public static final String HUMANIZED_EDIT_APPLICATION="humanized.edit.application";
    public static final String HUMANIZED_DELETE_APPLICATION="humanized.delete.application";
    public static final String HUMANIZED_ADD_TUNNEL_LOG="humanized.add.tunnel.log";
    public static final String HUMANIZED_GET_REMOTE_SETTINGS="humanized.get.remote.settings";
    public static final String HUMANIZED_CSV_DOWNLOAD_FAILED="humanized.csv.download.failed";
    public static final String HUMANIZED_GET_SRA_GATEWAY_SETTING="humanized.get.sra.gateway.setting";
    public static final String HUMANIZED_ADD_SRA_GATEWAY_SETTING="humanized.add.sra.gateway.setting";
    public static final String HUMANIZED_DELETE_SRA_GATEWAY_SETTING="humanized.delete.sra.gateway.setting";
    public static final String HUMANIZED_GET_DEVICES_FAILED="humanized.get.devices.failed";
    public static final String HUMANIZED_GET_TOKENS_FAILED="humanized.get.tokens.failed";
    public static final String HUMANIZED_GET_DEVICE_TOKENS_FAILED="humanized.get.device.tokens.failed";
    public static final String HUMANIZED_EDIT_DEVICE_FAILED="humanized.edit.device.failed";
    public static final String HUMANIZED_EDIT_TOKEN_FAILED="humanized.edit.token.failed";
    public static final String HUMANIZED_USER_GROUP_ALREADY_EXISTS="humanized.user.group.already.exists";
    public static final String HUMANIZED_CREATE_USER_GROUP="humanized.create.user.group";
    public static final String HUMANIZED_GET_USERGROUP_FAILED="humanized.get.usergroup.failed";
    public static final String HUMANIZED_UPDATE_USER_GROUP="humanized.update.user.group";
    public static final String HUMANIZED_USER_USER_GROUP_MAPPING="humanized.user.usergroup.mapping";
    public static final String HUMANIZED_USER_GROUP_BINDING_ALREADY_EXISTS="humanized.user.group.binding.already.exists";
    public static final String HUMANIZED_USER_GROUP_BINDING_NOT_FOUND="humanized.usergroup.binding.not.found";
    public static final String HUMANIZED_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS="humanized.usergroup.application.binding.already.exists";
    public static final String HUMANIZED_USER_GROUP_APPLICATION_BINDING_NOT_FOUND="humanized.usergroup.application.binding.not.found";
    public static final String HUMANIZED_USER_GROUP_APPLICATION_MAPPING="humanized.usergroup.application.mapping";
    public static final String HUMANIZED_GET_USERGROUP_APPLICATION_FAILED="humanized.get.usergroup.application.failed";
    public static final String HUMANIZED_REMOVE_USER_GROUP="humanized.remove.usergroup";
    public static final String HUMANIZED_EDIT_ATTRIBUTE_FAILED="humanized.edit.attribute.failed";
    public static final String HUMANIZED_DELETE_ATTRIBUTE_FAILED="humanized.delete.attribute.failed";
    public static final String HUMANIZED_QR_CODE_GENERATION_FAILED="humanized.qr.code.generation.failed";
    public static final String HUMANIZED_LOGS_DOWNLOAD_FAILED="humanized.logs.download.failed";
    public static final String HUMANIZED_ONBOARD_ENTERPRISE_FAILED="humanized.onboard.enterprise.failed";
    public static final String HUMANIZED_GET_STATE_MACHINE_WORKFLOW_FAILED="humanized.get.state.machine.workflow.failed";
    public static final String HUMANIZED_GET_ACCOUNT_CUSTOM_STATE_MACHINE_FAILED="humanized.get.account.custom.state.machine.failed";
    public static final String HUMANIZED_STATE_MACHINE_WORKFLOW_ONBOARD_FAILED="humanized.state.machine.workflow.onboard.failed";
    public static final String HUMANIZED_STATE_MACHINE_WORKFLOW_EDIT_FAILED="humanized.state.machine.workflow.edit.failed";
    public static final String HUMANIZED_GET_ATTEMPT_TYPES_FAILED="humanized.get.attempt.type.failed";
    public static final String HUMANIZED_GET_CHALLENGE_TYPES_FAILED="humanized.get.challenge.type.failed";
    public static final String HUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_FAILED="humanized.account.custom.state.machine.onboard.failed";
    public static final String HUMANIZED_ACCOUNT_CUSTOM_STATE_MACHINE_EDIT_FAILED="humanized.account.custom.state.machine.edit.failed";
    public static final String ERROR_MESSAGE_USER_CREDENTIALS_NOT_PRESENT="error.message.user.credentials.not.present";
    public static final String ERROR_MESSAGE_EVIDENCE_IS_REQUIRED="error.message.evidence.is.required";
    public static final String ERROR_MESSAGE_INVALID_BULK_UPLOAD_TYPE="error.message.invalid.bulk.upload.type";
    public static final String HUMANIZED_UPLOAD_FAILED="humanized.upload.failed";
    public static final String HUMANIZED_DOWNLOAD_STATUS_FILE_FAILED="humanized.download.status.file.failed";
    public static final String HUMANIZED_DOWNLOAD_SAMPLE_CSV_FILE_FAILED="humanized.download.sample.csv.file.failed";
    public static final String ERROR_MESSAGE_APPLICATION_NOT_ACTIVE="error.message.application.not.active";
    public static final String HUMANIZED_FETCH_QR_STATUS_FAILED="humanized.fetch.qr.status.failed";
    public static final String ERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE="error.message.invalid.search.attribute";
    public static final String ERROR_MESSAGE_DUPLICATE_SEARCH_ATTRIBUTE="error.message.duplicate.search.attribute";
    public static final String ERROR_MESSAGE_DUPLICATE_ATTRIBUTE="error.message.duplicate.attribute";
    public static final String ERROR_MESSAGE_INVALID_ACCOUNT_ID="error.message.invalid.account.id";
    public static final String ERROR_MESSAGE_APPLICATION_NOT_FOUND="error.message.application.not.found";
    public static final String ERROR_MESSAGE_APPLICATION_ALREADY_PRESENT="error.message.application.already.present";
    public static final String ERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT_TO_OTHER="error.message.attribute.already.present.to.other";
    public static final String ERROR_MESSAGE_USER_UPDATE_PENDING="error.message.user.update.pending";
    public static final String ERROR_MESSAGE_INVALID_TOKEN="error.message.invalid.token";
    public static final String ERROR_MESSAGE_PERMISSION_DENIED="error.message.permission.denied";
    public static final String ERROR_MESSAGE_IO_EXCEPTION="error.message.io.exception";
    public static final String ERROR_MESSAGE_INVALID_APPLICATION_ID_OR_PASSWORD="error.message.invalid.application.id.or.password";
    public static final String ERROR_MESSAGE_USER_SERVICE_BINDING_FAILED="error.message.user.service.binding.failed";
    public static final String ERROR_MESSAGE_INVALID_MOBILE="error.message.invalid.mobile";
    public static final String ERROR_MESSAGE_INVALID_APPLICATION_FOR_USER="error.message.invalid.application.for.user";
    public static final String ERROR_MESSAGE_SERVER_ERROR="error.message.server.error";
    public static final String ERROR_MESSAGE_ACCOUNT_NOT_FOUND="error.message.account.not.found";
    public static final String ERROR_MESSAGE_LOOKUP_ID_COMPULSORY="error.message.lookup.id.compulsory";
    public static final String ERROR_MESSAGE_APPROVAL_ATTEMPT_TYPE_INVALID="error.message.approval.attempt.type.invalid";
    public static final String ERROR_MESSAGE_INVALID_CONSUMER_ID="error.message.invalid.consumer.id";
    public static final String ERROR_MESSAGE_APPROVAL_ATTEMPT_NOT_APPROVED="error.message.approval.attempt.not.approved";
    public static final String ERROR_MESSAGE_INVALID_APPLICATION_ID="error.message.invalid.application.id";
    public static final String ERROR_MESSAGE_INVALID_ADMIN_ROLE="error.message.invalid.admin.role";
    public static final String ERROR_MESSAGE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT="error.message.state.machine.workflow.onboard.request.already.present";
    public static final String ERROR_MESSAGE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT="error.message.state.machine.workflow.update.request.already.present";
    public static final String ERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT="error.message.account.custom.state.machine.onboard.request.already.present";
    public static final String ERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT="error.message.account.custom.state.machine.workflow.onboard.request.already.present";

    public static final String ORM_MAX_POOL_SIZE = "orm.max.pool.size";
    public static final String ORM_MIN_POOL_SIZE = "orm.min.pool.size";
    public static final String ORM_CLEANUP_INTERVAL_SECONDS = "orm.cleanup.interval.seconds";
    public static final String ORM_TTL_SECONDS = "orm.ttl.seconds";
    public static final String ORM_KEEPALIVE_INTERVAL_SECONDS = "orm.keepalive.interval.seconds";
    public static final String CI0_AUDIT_INTERVAL_SECONDS = "ci0.audit.interval.seconds";
    public static final String ORM_RETRIES = "orm.retries";

    public static final int ALLOWED_NUMBER_OF_PREVIOUS_OTP_FOR_TOTP = 1;

    public static final String DATE_FORMAT = "YYYYMMddHHmmss";
    public static final String TIMEZONE = "IST";
    public static final String CSV_FILE_EXTENSION = ".csv";
    public static final String _UNDERSCORE = "_";
    public static final String FALLOUT_PROCESS = "FALLOUT_PROCESS";
    public static final String OTHERS = "OTHERS";

    public static final String ESC_CACHE_SIZE = "esc.cache.size";

    public static final String INVALID_TRANSACTION_VALIDITY = "Invalid data. validity should be grater than 0";
    public static final String ERROR_CODE_GENERATE_RUNNING_HASH_FAILED = "error.code.generate.running.hash.failed";
    public static final String ERROR_CODE_VERIFY_RUNNING_HASH_FAILED = "error.code.verify.running.hash.failed";
    public static final String HUMANIZED_GENERATE_RUNNING_HASH_FAILED="humanized.generate.running.hash.failed";
    public static final String HUMANIZED_VERIFY_RUNNING_HASH_FAILED="humanized.verify.running.hash.failed";
    public static final String ERROR_MESSAGE_INVALID_RUNNING_HASH ="error.message.invalid.running.hash" ;
    public static final String CAM_READ_TIMEOUT = "cam.read.timeout";
    public static final String CAM_CONNECT_TIMEOUT = "cam.connect.timeout";
    public static final String CAM_CHECKOUT_TIMEOUT = "cam.checkout.timeout";
    public static final String ERROR_CODE_THEME_NOT_FOUND = "error.code.theme.not.found";
    public static final String ERROR_MESSAGE_THEME_NOT_FOUND = "error.message.theme.not.found";
    public static final String ERROR_MESSAGE_INVALID_USER_CREDENTIALS ="error.message.invalid.user.credentials" ;

    public static final String DEFAULT_SERVICE_NAME = "default.service.name";

    public static final String APPROVAL = "APPROVAL";

    public static final String ERROR_CODE_MULTIPLE_IDENTITIES_FOUND = "error.code.multiple.identities.found";
    public static final String ERROR_MESSAGE_MULTIPLE_IDENTITIES_FOUND = "error.message.multiple.identities.found";

    public static final String GRANT_TYPE = "GrantType";
    public static final String TOKEN = "token";
    public static final String MFA_EENABLED = "mfa.enabled";

    public static final String USE_ATTRIBUTE_SEARCH = "use.attribute.search";

    public static final String MFA = "MFA";
    public static final String DDUP_TEST_MODE = "ddup.test.mode";

    public static final String DDUP_MODE = "ddup.mode";

    public static final String USER_LOCK_MODE = "user.lock.mode";

    public static final String ERROR_CODE_POLICY_NOT_FOUND = "error.code.policy.not.found";
    public static final String ERROR_MESSAGE_POLICY_NOT_FOUND = "error.message.policy.not.found";
    public static final String ERROR_MESSAGE_DISABLE_USER_FAILED = "error.message.disable.user.failed";
    public static final String ERROR_CODE_DISABLE_USER_ALREADY_PRESENT = "error.code.disable.user.already.present";
    public static final String ERROR_MESSAGE_DISABLE_USER_ALREADY_PRESENT = "error.message.disable.user.already.present";
    public static final String ERROR_CODE_USER_LOGIN_TIME_EXPIRED = "error.code.user.login.time.expired";
    public static final String ERROR_MESSAGE_USER_LOGIN_TIME_EXPIRED = "error.message.user.login.time.expired";
    public static final String LOGIN_EXPIRY_TIME_IN_MILLIS = "login.expiry.time.in.millis";

    public static final String FALLOUT_PROCESS_SCHEDULAR_TIME = "fallout.process.schedular.time";
    public static final String FALLOUT_PROCESS_DEH_URL = "fallout.process.deh.url";
    public static final String FALLOUT_PROCESS_VT_TOKEN_URL = "fallout.process.vt.token.url";
    public static final String FALLOUT_PROCESS_DATA_SYNC = "fallout.process.data.sync";
    public static final String FALLOUT_PROCESS_DATA_PROCESSING = "fallout.process.data.processing";

    public static final String START_DATE = "startDateTime";
    public static final String END_DATE = "endDateTime";
    public static final String ATTRIBUTE_METADATA_CACHE_TIMEOUT_IN_SECONDS = "attribute.metadata.cache.timeout.in.seconds";
    public static final String NULL_CHECK_REGEX = "^\\{\\s*\\}$";

    public static final String DDUP_MODE_WITH_38 = "ddup.mode.with.38";
    public static final String DDUP_MODE_WITH_813 = "ddup.mode.with.813";
    public static final String BUNDLE_ID="Bundle_ID";
    public static final String PACKAGE_NAME="package_name";
    public static final String SERVICE_ACCOUNT_JSON = "serviceAccountJson";
    public static final String PROJECT_ID = "project_ID";
    public static final String EXTERNAL_CONFIG_TYPE_DEH_FALLOUT = "DF";
    public static final String EXTERNAL_CONFIG_CACHE_TIMEOUT_IN_SECONDS = "external.config.cache.timeout.in.seconds";

    public static final String  ERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_ALREADY_PRESENT = "error.message.ldap.details.add.request.already.present";

    public static final String  ERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_ALREADY_PRESENT = "error.message.ldap.details.edit.request.already.present";

    public static final String ERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_FAILED = "error.message.ldap.details.edit.request.failed";

    public static final String ERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_FAILED = "error.message.ldap.details.add.request.failed" ;

    public static final String ERROR_MESSAGE_GET_LDAP_FAILED = "error.message.get.ldap.details.failed" ;

    public static final String  LDAP_DOMAIN_NAME = "domain_name";

    public static final String LDAP_CONNECTION_URL = "ldap_connection_url";

    public static final String LDAP_USER_DOMAIN_NAME = "user_domain_name";
    public static final String LDAP_CLIENT_ADDRESS = "client_address" ;

    public static final String ERROR_MESSAGE_LDAP_DETAILS_ALREADY_PRESENT = "error.message.ldap.details.already.present";

    public static final String ERROR_MESSAGE_SELF_BLOCKING_NOT_PERMITTED = "error.message.self.blocking.not.permitted" ;
    public static final String ADMIN_TOTP_LOG_THREAD_POOL_SIZE = "admin.totp.log.thread.size";

    public static final String ERROR_MESSAGE_CREATE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT = "error.message.create.temp.details.request.already.present";

    public static final String ERROR_MESSAGE_EDIT_TEMP_DETAILS_REQUEST_ALREADY_PRESENT = "error.message.edit.temp.details.request.already.present";

    public static final String ERROR_MESSAGE_CREATE_TEMP_DETAILS_FAILED = "error.message.create.temp.details.failed";

    public static final String ERROR_MESSAGE_GET_TEMP_DETAILS_REQUEST_FAILED = "error.message.get.temp.details.request.failed";

    public static final String ERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_FAILED = "error.message.delete.temp.details.request.failed" ;

    public static final String ERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT = "error.message.delete.temp.details.request.already.present";

    public static final String ERROR_MESSAGE_TEMP_NAME_ALREADY_PRESENT = "error.message.temp.name.already.present";

    public static final String ERROR_MESSAGE_UPDATE_TEMP_DETAILS_REQUEST_FAILED = "error.message.update.temp.details.request.failed";

    public static final String ERROR_MESSAGE_INVALID_TEMPLATE_FOR_APPLICATION = "error.message.invalid.template.for.application";
    public static final String ERROR_MESSAGE_VALIDATION_RULE_ALREADY_EXIST = "error.message.validation.rule.already.exist";
    public static final String TEMPLATE_ID = "template_Id " ;

    public static final String TEMPLATE = "template or template must contain <OTP>" ;
    public static final String X_DIMFA_RESPONSE_ERROR_CODE = "X-DIMFA.response.error.code";
    public static final String X_DIMFA_UNBIND = "X-DIMFA.unbind";
    private Constant() {
        super();
    }
}
