
package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class QRCodeTO {

    private String approvalAttemptId;

    private String data;

    private String hashAlgorithm;

    private Integer numberOfDigits;

    private Long totpExpiry;

    public String getApprovalAttemptId() {
        return approvalAttemptId;
    }

    public void setApprovalAttemptId(String approvalAttemptId) {
        this.approvalAttemptId = approvalAttemptId;
    }

    public String getData() {
        return data;
    }

    public Integer getNumberOfDigits() {
        return numberOfDigits;
    }

    public Long getTotpExpiry() {
        return totpExpiry;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setNumberOfDigits(Integer numberOfDigits) {
        this.numberOfDigits = numberOfDigits;
    }

    public void setTotpExpiry(Long totpExpiry) {
        this.totpExpiry = totpExpiry;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

}
