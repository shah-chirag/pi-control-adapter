package in.fortytwo42.adapter.util;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;

public class IAMExceptionConvertorUtil {

    ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private IAMExceptionConvertorUtil() {
        super();
    }

    private static final class InstanceHolder {
        private static final IAMExceptionConvertorUtil INSTANCE = new IAMExceptionConvertorUtil();

        private InstanceHolder() {
            super();
        }
    }

    public static IAMExceptionConvertorUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    public AuthException convertToAuthException(IAMException iamException) {
        switch(iamException.getErrorCode()) {
            case IAMConstants.CERTIFICATE_PINNING_FAILED:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_CERTIFICATE_PINNING_FAILURE(), iamException.getMessage());
            case IAMConstants.ERROR_CODE_UNAUTHORIZED:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_INVALID_TOKEN(), errorConstant.getERROR_MESSAGE_INVALID_TOKEN());
            case IAMConstants.ERROR_CODE_INVALID_DATA:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_INVALID_DATA(), iamException.getMessage());
            case IAMConstants.ERROR_CODE_SERVER:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_SERVER_ERROR());
            case IAMConstants.ERROR_CODE_CONSUMER_NOT_PRESENT_DATA:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_CONSUMER_NOT_PRESENT_DATA(), errorConstant.getERROR_MESSAGE_CONSUMER_NOT_PRESENT_DATA());
            case IAMConstants.ERROR_CODE_CONSUMER_BINDING_NOT_PRESENT:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND());
            case IAMConstants.ERROR_CODE_PASSWORD:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID());
            case IAMConstants.ERROR_CODE_ACCOUNT_NOT_PRESENT:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
            case IAMConstants.ERROR_CODE_IAMCI2_CALL_FAILED:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_IAMCI2_CALL_FAILED(), iamException.getMessage());
            case IAMConstants.ERROR_CODE_INVALID_TRANSACTION_TYPE:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_INVALID_TRANSACTION_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_TRANSACTION_TYPE());
            case IAMConstants.ERROR_CODE_TRANSACTION_NOT_PRESENT:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
            case IAMConstants.LOOKUP_ID_COMPULSORY_CODE:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_LOOKUP_ID_COMPULSORY(), errorConstant.getERROR_MESSAGE_LOOKUP_ID_COMPULSORY());
            case IAMConstants.ERROR_CODE_VERIFICATION_FAILED:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_VERIFICATION_FAILED(), errorConstant.getERROR_MESSAGE_VERIFICATION_FAILED());
            case IAMConstants.ERROR_CODE_CONSUMER_BINDING_ALREADY_PRESENT:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_USER_SERVICE_BINDING_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_ALREADY_PRESENT());
            case IAMConstants.APPROVAL_ATTEMPT_TYPE_INVALID_ERROR_CODE:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_APPROVAL_ATTEMPT_TYPE_INVALID(), errorConstant.getERROR_MESSAGE_APPROVAL_ATTEMPT_TYPE_INVALID());
            case IAMConstants.CONSUMER_ID_INVALID_ERROR_CODE:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_INVALID_CONSUMER_ID(), errorConstant.getERROR_MESSAGE_INVALID_CONSUMER_ID());
            case IAMConstants.APPROVAL_ATTEMPT_NOT_APPROVED_ERROR_CODE:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_APPROVAL_ATTEMPT_NOT_APPROVED(), errorConstant.getERROR_MESSAGE_APPROVAL_ATTEMPT_NOT_APPROVED());
            case IAMConstants.APPLICATION_ID_INVALID_ERROR_CODE:
                return new AuthException(iamException.getCause(), errorConstant.getERROR_CODE_INVALID_APPLICATION_ID(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_ID());
            default:
                return new AuthException(iamException.getCause(), (long)iamException.getErrorCode(), iamException.getMessage());               
        }
    }
}
