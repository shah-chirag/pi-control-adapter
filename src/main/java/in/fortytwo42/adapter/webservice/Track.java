package in.fortytwo42.adapter.webservice;

import lombok.ToString;

@ToString
public class Track {

    String attributeName;
	String accountId;
	String accountNumber;
	String idsToken;
	String transactionDetails;
	String transactionId;
	
	
	
    
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getTransactionDetails() {
        return transactionDetails;
    }
    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }
    public String getAttributeName() {
        return attributeName;
    }
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getIdsToken() {
        return idsToken;
    }
    public void setIdsToken(String idsToken) {
        this.idsToken = idsToken;
    }

}
