package  in.fortytwo42.adapter.facade;

import java.util.List;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;

public interface AttributeMasterFacadeIntf {

	/**
	 * Get list of attributes metadata for which attribute is not added by the user.
	 * @param userId user id
	 * @param accountType Type of account: DEFAULT, APPLICATION, ENTERPRISE
	 * @param role Admin role
	 * @return list of attribute metadata 
	 * @throws AuthException Incase of failure fetching attribute metadata.
	 */
    List<AttributeMetadataTO> getAttributeMasterForUserId(Long userId, String accountType, String role) throws AuthException;
    
    /**
	 * Get list of attributes metadata for which attribute is not added by the user.
     * @param userAccountId user account id
     * @param accountType Type of account: DEFAULT, APPLICATION, ENTERPRISE
     * @param role Admin role
     * @return list of attribute metadata
     * @throws AuthException Incase of failure fetching attribute metadata.
     */
    List<AttributeMetadataTO> getAttributeMasterForAccountId(String userAccountId, String accountType, String role) throws AuthException;

    /**
     * Get list of attributes metadata for which attribute is not added by the user.
     * @param attributeName attribute name to find the user
     * @param attributeValue attribute value to find the user
     * @param role Admin role
     * @return list of attribute metadata
     * @throws AuthException Incase of failure fetching attribute metadata.
     */
    List<AttributeMetadataTO> getAttributeMasterForAttributeNameAndValue(String attributeName, String attributeValue, String role) throws AuthException;

    /**
     * Fetch list of all attribute metadata 
     * @return list of attribute metadata
     * @throws AuthException Incase of failure fetching attribute metadata.
     */
	List<AttributeMetadataTO> getAttributeMaster() throws AuthException;

	/**
	 * Fetch list of attribute metadata for attributeType and accountType.
	 * @param attributeType Type of attribute: PUBLIC, PRIVATE, DERIVED
	 * @param accountType Type of account: DEFAULT, APPLICATION, ENTERPRISE
	 * @param role Admin role
	 * @return
	 * @throws AuthException Incase of failure fetching attribute metadata.
	 */
	List<AttributeMetadataTO> getAttributeMasterForAttributeType(String attributeType, String accountType, String role) throws AuthException;

    AttributeMetadataTO addAttributeMetaData(AttributeMetadataTO attributeMetadataTO, String actor,Long id, String role,boolean saveRequest) throws AuthException;
    
    AttributeMetadataTO updatedAttributeMetaData(AttributeMetadataTO attributeMetadataTO, String actor,Long id, String role, boolean saveRequest) throws AuthException;
    
    AttributeMetadataTO deleteAttributeMetaData(AttributeMetadataTO attributeMetadataTO, String actor,Long id, String role, boolean saveRequest) throws AuthException;
    
    PaginatedTO<AttributeMetadataTO> getAllAttributeMaster(int page, String searchText) throws AuthException;

    AttributeMetadataTO getAttributeMaster(String attributeName) throws AuthException;
}
