package  in.fortytwo42.adapter.facade;

import java.io.File;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;

public interface EvidenceFacadeIntf {

	/**
	 * Fetch Evidence File for provided Evidence Id.
	 * @param evidenceId the Evidence Id
	 * @return Evidence File
     * @throws AuthException If evidence is not present for the provided Evidence Id.
	 */
    File downloadEvidence(Long evidenceId) throws AuthException;

    /**
	 * Fetch Base64 encoded image string of the evidence and Evidence Metadata for the provided Evidence Id.
	 * @param evidenceId the Evidence Id
	 * @return Evidence Metadata
     * @throws AuthException If evidence is not present for the provided Evidence Id.
	 */
	EvidenceStoreTO getEvidence(Long evidenceId) throws AuthException;
}
