/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.fortytwo42.adapter.enums.ThirdPartyVerifierStatus;
import in.fortytwo42.adapter.enums.VerifierType;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThirdPartyVerifierTO {

    private String verifierName;

    private String postDataApi;

    private String token;

    private ThirdPartyVerifierStatus status;

    private VerifierType verifierType;

    private String verifierId;

    public ThirdPartyVerifierTO() {
        super();
    }

    public String getVerifierName() {
        return verifierName;
    }

    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    public String getPostDataApi() {
        return postDataApi;
    }

    public void setPostDataApi(String postDataApi) {
        this.postDataApi = postDataApi;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ThirdPartyVerifierStatus getStatus() {
        return status;
    }

    public void setStatus(ThirdPartyVerifierStatus status) {
        this.status = status;
    }

    public VerifierType getVerifierType() {
        return verifierType;
    }

    public void setVerifierType(VerifierType verifierType) {
        this.verifierType = verifierType;
    }

    public String getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(String verifierId) {
        this.verifierId = verifierId;
    }

}
