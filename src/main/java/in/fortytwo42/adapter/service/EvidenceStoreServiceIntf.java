package  in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.EvidenceStore;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;

public interface EvidenceStoreServiceIntf {
	
	/**
	 * Fetch the list of Evidence Metadata for provided Attribute Id. 
	 * @param attributeId Attribute Id
	 * @return list of Evidence Metadata
	 */
	List<EvidenceStoreTO> getEvidenceByAttributeId(Long attributeId);

	/**
	 * Store Evidences for the provided Attribute Id.
	 * @param attributeId  Attribute Id
	 * @param evidences List of Base64 encoded image string
	 */
    void storeEvidence(Session session, Long attributeId, List<String> evidences) throws AuthException;

    /**
     * Fetch Evidence Metadata for provided Evidence Id. 
     * @param evidenceId Evidence Id
     * @return Evidence Metadata
     * @throws AuthException If evidence is not present for the provided Evidence Id.
     */
    EvidenceStoreTO getEvidenceById(Long evidenceId) throws AuthException;

    List<EvidenceStoreTO> getEvidenceWithDataByAttributeId(Long attributeId);

    List<EvidenceStoreTO> getEvidences(Long attributeId);
    
    void storeEvidence(Session session, Long attributeId, Long evidenceId) throws AuthException;

	void storeEvidenceUsingAttributeStore(Session session, AttributeStore attributeStore, Long evidenceId) throws AuthException;

    EvidenceStore getEvidenceByEvidenceId(Long evidenceId) throws AuthException;
    
    EvidenceStoreTO storeEvidence(Session session, String fileName, byte[] evidenceData);

    void saveEvidence(Session session, AttributeStore attributeStore, List<String> evidences) throws AuthException;
}
