package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.enterprise.extension.core.DecryptionDataV2;

public interface EncryptionFacadeIntf {

	DecryptionDataV2 getDecryptionKey(String signTransactionId, String clientId, String type) throws AuthException;

}
