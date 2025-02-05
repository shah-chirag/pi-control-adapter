package in.fortytwo42.adapter.util;

import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceImpl;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;

public class AttributeValidationUtil {

    private static final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private static ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private static Logger logger= LogManager.getLogger(AttributeValidationUtil.class);

    //TODO: Add search account functionality
    public static void validateSearchAttributeValueAndUniqueness(String attributeName, String attributeValue) throws AuthException {
        AttributeMetadataTO attributeMetadataTO = iamExtensionService.getAttributeMetadataForAttributeName(attributeName.toUpperCase());
        if (attributeMetadataTO != null) {
            if(!attributeMetadataTO.getIsUnique()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SERACH_ATTRIBUTE(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE()+": "+attributeMetadataTO.getAttributeName());
            }
            String attributeValueRegex = (String) attributeMetadataTO.getAttributeSettings().get(Constant.VALIDATION_REGEX);
            if (attributeValueRegex != null) {
                boolean isAttributeValueRegex = Pattern.matches(attributeValueRegex, attributeValue);
                if (!isAttributeValueRegex) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE());
                }
            }
        }
    }


    public static void validateSearchAttributeValueAndUniquenessWithoutCrypto(String attributeName, String attributeValue) throws AuthException {
        AttributeMetadataTO attributeMetadataTO = iamExtensionService.getAttributeMetadataForAttributeNameWithoutCrypto(attributeName.toUpperCase());
        if (attributeMetadataTO != null) {
            if(!attributeMetadataTO.getIsUnique()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SERACH_ATTRIBUTE(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE()+": "+attributeMetadataTO.getAttributeName());
            }
            String attributeValueRegex = (String) attributeMetadataTO.getAttributeSettings().get(Constant.VALIDATION_REGEX);
            if (attributeValueRegex != null) {
                boolean isAttributeValueRegex = Pattern.matches(attributeValueRegex, attributeValue);
                if (!isAttributeValueRegex) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_VALUE_IS_INVALIDE(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE());
                }
            }
        }
    }

    public static void isAttributePresentOnAdapter(String attributeName, String attributeValue, Long userId) throws AuthException {
        if (attributeName == null || attributeName.isEmpty()) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_ATTRIBUTE_NAME());
        }
        if (attributeValue == null || attributeValue.isEmpty()) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_VALUE_IS_INVALIDE());
        }
        AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();
        AttributeStore newAttribute = null;
        AttributeMetadataTO attributeMetadataTO = iamExtensionService.getAttributeMetadataForAttributeName(attributeName.toUpperCase());
        if (attributeMetadataTO != null) {
            if (attributeMetadataTO.getIsUnique()) {
                try {
                    newAttribute = attributeStoreService.getAttributeByAttributeNameAndValue(attributeName,
                            attributeValue);
                }
                catch (AuthException e) {
                    logger.log(Level.DEBUG, " attribute value not found in the system");
                }
            }
            else {
                User user = UserServiceImpl.getInstance().getActiveUser(userId);
                try {
                    newAttribute = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName,
                            attributeValue, user.getId());
                }
                catch (AuthException e) {
                    logger.log(Level.DEBUG, " attribute value not found in the system");
                }
            }
            if (newAttribute != null) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_ATTRIBUTE_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_ALREADY_PRESENT());
            }
        }
    }
}
