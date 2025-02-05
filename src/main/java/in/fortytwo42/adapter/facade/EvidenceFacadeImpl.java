package  in.fortytwo42.adapter.facade;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.EvidenceStoreServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.FileUtil;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;

// TODO: Auto-generated Javadoc
/**
 * The Class EvidenceFacadeImpl.
 */
public class EvidenceFacadeImpl implements EvidenceFacadeIntf {

    /** The evidence facade impl log. */
    private String EVIDENCE_FACADE_IMPL_LOG = "<<<<< EvidenceFacadeImpl";

	private static Logger logger= LogManager.getLogger(EvidenceFacadeImpl.class);
    
	/** The evidence store processor intf. */
	private EvidenceStoreServiceIntf evidenceStoreService = ServiceFactory.getEvidenceStoreService();

	/**
	 * The Class InstanceHolder.
	 */
	private static final class InstanceHolder {
		
		/** The Constant INSTANCE. */
		private static final EvidenceFacadeImpl INSTANCE = new EvidenceFacadeImpl();

		/**
		 * Instantiates a new instance holder.
		 */
		private InstanceHolder() {
			super();
		}
	}

	/**
	 * Gets the single instance of EvidenceFacadeImpl.
	 *
	 * @return single instance of EvidenceFacadeImpl
	 */
	public static EvidenceFacadeImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	/**
	 * Gets the evidence.
	 *
	 * @param evidenceId the evidence id
	 * @return the evidence
	 * @throws AuthException the auth exception
	 */
	@Override
	public EvidenceStoreTO getEvidence(Long evidenceId) throws AuthException {
        logger.log(Level.DEBUG, EVIDENCE_FACADE_IMPL_LOG + " getEvidence : start");
		EvidenceStoreTO evidenceTO = evidenceStoreService.getEvidenceById(evidenceId);
		String filePath = evidenceTO.getFilePath();
		evidenceTO.setEvidence(FileUtil.getEncodedStringOfFile(filePath));
		evidenceTO.setFilePath(null);
        logger.log(Level.DEBUG, EVIDENCE_FACADE_IMPL_LOG + " getEvidence : end");
		return evidenceTO;
	}
	
	/**
	 * Download evidence.
	 *
	 * @param evidenceId the evidence id
	 * @return the file
	 * @throws AuthException the auth exception
	 */
	@Override
	public File downloadEvidence(Long evidenceId) throws AuthException {
        logger.log(Level.DEBUG, EVIDENCE_FACADE_IMPL_LOG + " downloadEvidence : start");
		EvidenceStoreTO evidenceStore = evidenceStoreService.getEvidenceById(evidenceId);
		logger.log(Level.DEBUG, "<<<<< evidenceStore : "+new Gson().toJson(evidenceStore));
		String filePath = evidenceStore.getFilePath();
        File evidence = new File(filePath);
        logger.log(Level.DEBUG, EVIDENCE_FACADE_IMPL_LOG + " downloadEvidence : end");
		return evidence;
	}
}
