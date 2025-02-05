/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author ChiragShah
 *
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ApprovalAttemptPollerTO {

    private long Version;

    private String EnterpriseId;

    private String ConsumerId;

    private String IamTransaction;

    private String ApplicationId;

    private String TransactionId;

    private String ApprovalStatus;

    private String TxnSummary;

    private String TxnDetails;

    private String DateTimeCreated;

    private String DateTimeModified;

    private String AcknowledgementStatus;
    
    private String ApprovalAttemptType;
    
    private String SignTransactionId;
    
    private String EncryptedData;
    
    private String ApprovalAttemptMode;
    
    private String SenderAccountId;
    
    private String ReceiverAccountId;

    private String SenderIdDetails;

    private String ReceiverIdDetails;
    
    public ApprovalAttemptPollerTO() {
        super();
    }

    public long getVersion() {
        return Version;
    }

    public void setVersion(long version) {
        Version = version;
    }

    public String getEnterpriseId() {
        return EnterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        EnterpriseId = enterpriseId;
    }

    public String getConsumerId() {
        return ConsumerId;
    }

    public void setConsumerId(String consumerId) {
        ConsumerId = consumerId;
    }

    public String getIamTransaction() {
        return IamTransaction;
    }

    public void setIamTransaction(String iamTransaction) {
        IamTransaction = iamTransaction;
    }

    public String getApplicationId() {
        return ApplicationId;
    }

    public void setApplicationId(String applicationId) {
        ApplicationId = applicationId;
    }

    public String getTransactionId() {
        return TransactionId;
    }

    public void setTransactionId(String transactionId) {
        TransactionId = transactionId;
    }

    public String getApprovalStatus() {
        return ApprovalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        ApprovalStatus = approvalStatus;
    }

    public String getTxnSummary() {
        return TxnSummary;
    }

    public void setTxnSummary(String txnSummary) {
        TxnSummary = txnSummary;
    }

    public String getTxnDetails() {
        return TxnDetails;
    }

    public void setTxnDetails(String txnDetails) {
        TxnDetails = txnDetails;
    }

    public String getDateTimeCreated() {
        return DateTimeCreated;
    }

    public void setDateTimeCreated(String dateTimeCreated) {
        DateTimeCreated = dateTimeCreated;
    }

    public String getDateTimeModified() {
        return DateTimeModified;
    }

    public void setDateTimeModified(String dateTimeModified) {
        DateTimeModified = dateTimeModified;
    }

    public String getAcknowledgementStatus() {
        return AcknowledgementStatus;
    }

    public void setAcknowledgementStatus(String acknowledgementStatus) {
        AcknowledgementStatus = acknowledgementStatus;
    }
  
    public String getApprovalAttemptType() {
		return ApprovalAttemptType;
	}

	public void setApprovalAttemptType(String approvalAttemptType) {
		ApprovalAttemptType = approvalAttemptType;
	}

	public String getSignTransactionId() {
        return SignTransactionId;
    }

    public void setSignTransactionId(String signTransactionId) {
        SignTransactionId = signTransactionId;
    }

    public String getEncryptedData() {
        return EncryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        EncryptedData = encryptedData;
    }
    
    public String getApprovalAttemptMode() {
        return ApprovalAttemptMode;
    }

    public void setApprovalAttemptMode(String approvalAttemptMode) {
        ApprovalAttemptMode = approvalAttemptMode;
    }

	public String getSenderAccountId() {
		return SenderAccountId;
	}

	public void setSenderAccountId(String senderAccountId) {
		SenderAccountId = senderAccountId;
	}
	
	public String getReceiverAccountId() {
		return ReceiverAccountId;
	}

	public void setReceiverAccountId(String receiverAccountId) {
		ReceiverAccountId = receiverAccountId;
	}

	public String getSenderIdDetails() {
		return SenderIdDetails;
	}

	public void setSenderIdDetails(String senderIdDetails) {
		SenderIdDetails = senderIdDetails;
	}

	public String getReceiverIdDetails() {
		return ReceiverIdDetails;
	}

	public void setReceiverIdDetails(String receiverIdDetails) {
		ReceiverIdDetails = receiverIdDetails;
	}
	
}
