/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

/**
 * @author ChiragShah
 *
 */
public class TokenPolicyTO extends BaseTO {

    private Integer maxTokensPerDevice;

    private Integer maxTokensPerUser;

    public Integer getMaxTokensPerDevice() {
        return maxTokensPerDevice;
    }

    public void setMaxTokensPerDevice(Integer maxTokensPerDevice) {
        this.maxTokensPerDevice = maxTokensPerDevice;
    }

    public Integer getMaxTokensPerUser() {
        return maxTokensPerUser;
    }

    public void setMaxTokensPerUser(Integer maxTokensPerUser) {
        this.maxTokensPerUser = maxTokensPerUser;
    }

}
