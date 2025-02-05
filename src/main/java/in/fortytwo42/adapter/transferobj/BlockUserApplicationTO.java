
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import lombok.ToString;
@ToString
public class BlockUserApplicationTO {

    private String consumerId;

    private List<String> enterprises;

    private String status;

    private String attributeName;

    private String attributeValue;
    
    private String accountId;

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public List<String> getEnterprises() {
        return enterprises;
    }

    public void setEnterprises(List<String> enterprises) {
        this.enterprises = enterprises;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

}
