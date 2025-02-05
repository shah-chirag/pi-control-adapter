
package in.fortytwo42.adapter.facade;

import java.io.InputStream;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.EnterpriseTO;
import in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;
import in.fortytwo42.tos.transferobj.RequestTO;
import in.fortytwo42.tos.transferobj.UserTO;

public interface AttributeStoreFacadeIntf {

    /**
     * Verifies the ATTRIBUTE_VERIFICATION request received by Enterprise from the user. The verification status will be pending for checker approval. 
     * If approved by checker, attribute verification status on I-AM cloud will be updated.
     * @param verifyAttributeTO Attribute details containing request id and verification status
     * @param actor Admin username
     * @throws AuthException In case of any error in verifying the request such as permission denied, request is not present or it is already approved.
     */
    void verifyAttribute(AttributeTO verifyAttributeTO, String actor, Long id) throws AuthException;

    //TODO: sagar to check if this function is required. Delete if not required.
    PaginatedTO<RequestTO> getPendingAttributeVerificationRequests(int page, int limit, String role) throws AuthException;

    /**
     * Approves or reject checker approval pending request. This function is used by checker to approve/reject any request created by the maker.
     * @param verifyAttributeTO Request details containing id, request type and approval status
     * @param role Admin role
     * @param actor Admin username
     * @return Object with success status
     * @throws AuthException In case of any error in approving the request such as permission denied, request is not present or it is already approved.
     */
    AttributeTO approvePendingRequest(AttributeTO verifyAttributeTO, String role, String actor, Long id) throws AuthException;

    /**
     * Fetch Attribute details for the provided id. It also returns the evidence added for the attribute if present.
     * @param authAttemptId Authentication attempt id
     * @return Attribute details
     * @throws AuthException If attribute data is not present for the provided id.
     */
    AttributeDataTO getAttribute(Long authAttemptId) throws AuthException;

    //TODO: sagar to check if this function is required. Delete if not required.
    PaginatedTO<RequestTO> getPendingAttributeAdditionRequests(int page, int limit, String role) throws AuthException;

    /**
     * Returns list of checker approval pending requests based on provided request type.
     * @param page page no for fetching the request
     * @param role Admin role
     * @param requestType Request type to return
     * @return list of checker approval pending requests
     * @throws AuthException If admin does not have the required permissions.
     */
    PaginatedTO<RequestTO> getPendingAttributeRequests(int page, String role, String requestType) throws AuthException;

    /**
     * Create Attribute addition request for the user. The request will be pending for checker approval. 
     * If approved by checker, ATTRIBUTE_ADDITION request will be sent to user for approval.
     * @param addAttributeTO Object containing user id and Attribute data to be added for the user
     * @param actor Admin username
     * @param role Admin role
     * @return Request details with request id
     * @throws AuthException If admin does not have permission to add attribute for the user or if the provided Attribute is already added for any other user.
     */
    AttributeTO addAttribute(AttributeTO addAttributeTO, String actor, String role,Long id) throws AuthException;

    /**
     * Returns list of pending Attribute verification requests.
     * @param page page no for fetching the request
     * @param searchText search query to filter the requests
     * @return list of pending Attribute verification requests.
     */
    PaginatedTO<AttributeTO> getAttributeVerificationRequests(int page, String searchText);

    /**
     * Returns list of requests pending for Checker approval.
     * @param page page no for fetching the request
     * @param role Admin role
     * @param requestType return type to be fetched
     * @return List of non pending requests.
     */
    PaginatedTO<RequestTO> getPaginatedNonPendingRequests(int page, String role, String requestType);

    /**
     * Verify the Attribute Verification Request sent by User. On approval, the User Attribute is verified on Crypto and I-AM.
     * @param attributeTO Request details and Approval status 
     * @return Response data with status
     * @throws AuthException Incase of any error in request verification.
     */
    AttributeTO verifyAttributeRequest(AttributeTO attributeTO) throws AuthException;

    /**
     * Create Attribute Addition Request for User. The attribute and evidence data is also stored in data store.
     * @param attributeDataRequestTO Attribute list to fetch user and Attribute data to be added for the user
     * @return Response data with request identifier
     * @throws AuthException Incase of any error in request creation.
     */
    AttributeDataRequestTO sendAttributeAdditionRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException;

    /**
     * Fetch Approval Status of Attribute Addition request sent by the Enterprise to the user. 
     * @param requestId Id of Attribute Addition request
     * @return Attribute Addition request details with Approval status
     * @throws AuthException If Attribute addition request is not present for provided requestId.
     */
    AttributeDataRequestTO getAttributeAdditionStatus(Long requestId) throws AuthException;

    /**
     * Fetch Pending Attribute Verification requests received by the Enterprise.
     * @param limit  limit for Attribute Verification requests
     * @param offset  offset for Attribute Verification requests
     * @return List of Attribute verification requests
     * @throws AuthException Incase of any failure in fetching the requests.
     */
    PaginatedTO<UserTO> getPendingAttributeVerificationRequests(int limit, int offset) throws AuthException;

    CSVUploadTO uploadAttributes(String fileType, InputStream inputStream, String role,Long id, String fileName) throws AuthException;

    AttributeTO editAttribute(AttributeTO addAttributeTO, String actor, Long id, String role) throws AuthException;

    UserTO requestAttribute(UserTO userTO, String actor, String role, Long id) throws AuthException;

    EvidenceStoreTO uploadAttributeEvidence(InputStream inputStream, String role, String fileName) throws AuthException;

    PaginatedTO<EnterpriseTO> getEnterprises(int page) throws AuthException;

    PaginatedTO<ThirdPartyVerifierTO> getVerifiers(String role, String verifierType, String attributeName, int page) throws AuthException;

    AttributeDataRequestTO approveRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException;

    AttributeDataRequestTO sendAttributeEditRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException, IAMException;

    AttributeDataRequestTO sendAttributeUpdateOrDeleteRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException, IAMException;

    AttributeDataRequestTO verifyAttribute(AttributeDataRequestTO attributeDataRequestTO) throws AuthException;

    String readSampleAttributeUpdateCsvFile(String fileName);

    String downloadAttributeUpdateStatus(String fileName, String role) throws AuthException;

    AttributeDataRequestTO sendAttributeUpdateRequest(AttributeDataRequestTO attributeDataRequestTO) throws AuthException, IAMException;

    AttributeTO addAttributeupdateRequest(AttributeTO addAttributeTO, String actor, String role,Long id) throws AuthException, AttributeNotFoundException;

    AttributeDataRequestTO updateAttributeOfUser(AttributeTO attributeTO) throws AuthException, IAMException;

    AttributeDataRequestTO attributeEditAndTakeOver(AttributeDataRequestTO attributeDataRequestTO) throws AuthException, IAMException;

    AttributeDataRequestTO verifyAttributeV4(AttributeDataRequestTO attributeDataRequestTO) throws AuthException;

}
