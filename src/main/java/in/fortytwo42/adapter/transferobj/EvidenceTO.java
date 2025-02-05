
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.EvidenceStoreTO;
@JsonInclude(value = Include.NON_NULL)
public class EvidenceTO {

    private String transactionId;
    private String userIdentifier;
    private List<EvidenceStoreTO> evidences;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
  
    public String getUserIdentifier() {
		return userIdentifier;
	}

	public void setUserIdentifier(String userIdentifier) {
		this.userIdentifier = userIdentifier;
	}

	public List<EvidenceStoreTO> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<EvidenceStoreTO> evidences) {
        this.evidences = evidences;
    }

}
