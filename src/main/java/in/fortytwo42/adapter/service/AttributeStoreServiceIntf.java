
package in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.enterprise.extension.tos.EnterpriseTO;
import in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public interface AttributeStoreServiceIntf {

    /**
     * Saves the attribute data in the database.
     * @param attribute Attribute data
     * @param authAttemptId Authentication attempt id
     * @param userAccountId Account id of the user for which attribute is to be added
     * @param approvalStatus status of the attribute
     * @return Attribute data
     */
    AttributeStore saveAttributeData(Session session, AttributeDataTO attribute, Long authAttemptId, User user, Boolean isUserConsent) throws AuthException;

    boolean isAttributePresent(AttributeDataTO attribute, User user);

    /**
     * Saves the attribute data in the database.
     * @param attribute Attribute data
     * @param userAccountId Account id of the user for which attribute is to be added
     * @param approvalStatus status of the attribute
     * @return Attribute data
     */
    AttributeStore saveAttributeData(Session session, AttributeDataTO attribute, User user, Boolean isUserConsent) throws AuthException;
    
    /**
     * Fetch Attribute details for the provided id. It also returns the evidence added for the attribute if present.
     * @param authAttemptId Authentication attempt id
     * @return Attribute details
     * @throws AuthException If attribute data is not present for the provided id.
     */
    AttributeDataTO getAttribute(Long authAttemptId) throws AuthException;

    /**
     * Verify the Attribute on Crypto and I-AM server.
     * @param authAttemptId Authentication attempt id
     * @param approvalStatus Approval status
     * @throws AuthException Incase of any error in verifying the request
     */
    //void verifyAttribute(Long authAttemptId, String approvalStatus) throws AuthException;
    void verifyAttribute(Session session, Long authAttemptId, String transactionId, String approvalStatus, String signTransactiId) throws AuthException;

    /**
     * Returns all the Attribute details present for the provided authentication attempt id. It also returns the evidence metadata added for the attribute if present.
     * @param authAttemptId Authentication attempt id
     * @return list of Attribute and evidence metadata
     * @throws AuthException If attribute data is not present for the provided id.
     */
    List<AttributeDataTO> getAttributeList(Long authAttemptId) throws AuthException;

    /**
     * Returns all the approved Attributes having provided attribute name present for the user. The attributes are fetched from local database.
     * @param userAccountId account identifier of the user for which attributes should be fetched
     * @param attributeName Attribute name that should be fetched
     * @return list of Attribute and evidence metadata
     */
    List<AttributeDataTO> getAttributes(String userAccountId, String attributeName);

    /**
     * Returns all the approved Attributes present for the user. The attributes are fetched from local database.
     * @param userAccountId account identifier of the user for which attributes should be fetched
     * @return list of Attribute and evidence metadata
     */
    List<AttributeDataTO> getAttributes(String userAccountId);

    /**
     * Fetch Attribute details for the provided id. It also returns evidence hash if present.
     * @param authAttemptId Authentication attempt id
     * @return Attribute details
     * @throws AuthException If attribute is not present for the provided id.
     */
    AttributeDataTO getAttributeWithEvidenceHash(Long authAttemptId) throws AuthException;

    /**
     * Update the approval status of the Attribute. 
     * @param authAttemptId Authentication attempt id
     * @param approvalStatus Approval status
     * @return Attribute data
     * @throws AuthException If attribute is not present for the provided id.
     */

    AttributeStore saveEditAttributeData(Session session, AttributeDataTO attribute, Long authAttemptId, String userAccountId) throws AuthException;

    List<AttributeDataTO> getUserAttributes(String userAccountId);

    List<String> getUserAccountIdBySearchtext(String searchText);

    List<AttributeDataTO> getAttributeListWithEvidence(Long authAttemptId) throws AuthException;

    List<AttributeDataTO> getAttributesWithEvidence(String userAccountId, String attributeName) throws AuthException;
    
    void checkAttributesPresent(AttributeDataTO attributeDataTO) throws AuthException;

    in.fortytwo42.adapter.transferobj.PaginatedTO<EnterpriseTO> getEnterprises(int page, int limit) throws AuthException;

    in.fortytwo42.adapter.transferobj.PaginatedTO<ThirdPartyVerifierTO> getVerifiers(String verifierType, String attributeName, int page, int limit) throws AuthException;

    AttributeStore getAttributeByAttributeData(String attributeName, String attributeValue) throws AuthException;

    AttributeStore getAttributeByAttributeData(String attributeName, String attributeValue, Session session) throws AuthException;

    AttributeStore update(Session session, AttributeStore attributeTobeUpdate);

    void delete(Session session, AttributeStore attributeTobeUpdate);

    AttributeStore getAttributeByAttributeNameAndValue(String attributeName, String attributeValue) throws AuthException;

    AttributeStore getAttributeByAttributeNameAndValue(String attributeName, String attributeValue, Session session) throws AuthException;

    AttributeStore getActiveAttribute(String attributeName, String attributeValue) throws AttributeNotFoundException;

    AttributeStore getAttributeByAttributeData(String attributeName, String attributeValue, AttributeState state) throws AuthException;

    AttributeStore getAttributeByAttributeNameAndValueAndUserId(String attributeName, String attributeValue,Long userId) throws AuthException;

    AttributeStore getAttributeByAttributeNameAndValueAndUserIdWithoutStatus(String attributeName, String attributeValue, Long userId) throws AuthException;

    AttributeStore getAttributeByAttributeNameAndValueAndUserId(String attributeName, String attributeValue, Long userId, Session session) throws AuthException;

    List<AttributeStore> getAttributeByUserIdAndState(Session session ,AttributeState attributeState, Long userId) throws AuthException;

    AttributeStore getAttributeByNameValue(String attributeName, String attributeValue) throws AuthException;

    AttributeStore getActiveAttributeWithUpperCase(String attributeName, String attributeValue) throws AttributeNotFoundException;

    User getUserByAttributeValueWithUpperCase(String attributeValue) throws AttributeNotFoundException;
}
