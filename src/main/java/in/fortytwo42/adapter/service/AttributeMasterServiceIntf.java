package  in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;

public interface AttributeMasterServiceIntf {

     AttributeMetadataTO createAttributeMetaDataRequest(Session session, AttributeMetadataTO attributeMetadataTO,String actor)throws AuthException ;
    
     AttributeMetadataTO editAttributeMetaDataRequest(Session session, AttributeMetadataTO attributeMetadataTO,String actor)throws AuthException ;
     
     AttributeMetadataTO deleteAttributeMetaDataRequest(Session session, AttributeMetadataTO attributeMetadataTO,String actor)throws AuthException ;
     
     AttributeMetadataTO approveCreateAddAttributeMetaDataRequest(AttributeMetadataTO attributeMetadataTO)throws AuthException ;
     
     AttributeMetadataTO approveEditAddAttributeMetaDataRequest(AttributeMetadataTO attributeMetadataTO)throws AuthException ;
     
     AttributeMetadataTO approveDeleteAddAttributeMetaDataRequest(AttributeMetadataTO attributeMetadataTO)throws AuthException ;
     
     List<AttributeMetadataTO> getAllAttributeMetaData()throws AuthException;

    PaginatedTO<AttributeMetadataTO> getAllAttributeMetaData(int page, int limit, String searchText) throws AuthException;
    
    void validUpdationAttributeMetaData(AttributeMetadataTO attributeMetadataTO, AttributeMetadataTO attributeMetadataTO2) throws AuthException;

}
