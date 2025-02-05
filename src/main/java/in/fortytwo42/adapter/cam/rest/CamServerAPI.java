package in.fortytwo42.adapter.cam.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.representations.AccessTokenResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import in.fortytwo42.adapter.cam.dto.CamError;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.tos.transferobj.TokenRequestTO;

public class CamServerAPI {


    private final GsonBuilder builder;
    private Gson gson;
    private Logger logger = LogManager.getLogger("CamServerAPI");

    private static final String REALMS = "realms";

    private static final String TOKEN_ENPOINT ="protocol/openid-connect/token";
    private static final String INVALID_GRANT = "invalid_grant";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();


    private final WebTarget webTarget;
    private final Client client;

    public CamServerAPI(String hostURL) {
        builder = new GsonBuilder().enableComplexMapKeySerialization();
        gson = builder.create();
        //client = new JerseyClientBuilder().build();
        client = (ResteasyClient) ClientBuilder.newClient();
        webTarget = client.target(hostURL);
    }

    public AccessTokenResponse getToken(String realm, TokenRequestTO tokenRequestTO, String accountId) throws AuthException {
        WebTarget target = webTarget.path(  REALMS + IAMConstants.URL_SEPARATOR + realm + IAMConstants.URL_SEPARATOR + TOKEN_ENPOINT);

        Map<String, String> customFieldsMap = tokenRequestTO.getCustomFields();
        String customFieldsString = null;

        Form form = new Form();
        form.param(Constant.CAM_GRANT_TYPE, tokenRequestTO.getGrantType());
        form.param(Constant.CAM_CLIENT_ID, tokenRequestTO.getClientId());
        form.param(Constant.CAM_CLIENT_SECRET, tokenRequestTO.getClientSecret());
        form.param(Constant.CAM_PASSWORD_FIELD,tokenRequestTO.getUserCredential());
        form.param(Constant.CAM_USERNAME_FIELD,accountId);
        form.param(Constant.CAM_SCOPE_FIELD,tokenRequestTO.getScope());

        Invocation.Builder invocationBuilder;

        if (customFieldsMap != null){
            customFieldsString = new Gson().toJson(customFieldsMap);
            invocationBuilder = target
                    .request(MediaType.APPLICATION_JSON)
                    .header(Constant.CAM_SEARCH_ATTR_KEY, tokenRequestTO.getSearchAttributes().get(0).getAttributeName())
                    .header(Constant.CAM_SEARCH_ATTR_VALUE, tokenRequestTO.getSearchAttributes().get(0).getAttributeValue())
                    .header(Constant.CAM_CUSTOM_FIELDS_VALUE, customFieldsString);
        }
        else{
            invocationBuilder = target
                    .request(MediaType.APPLICATION_JSON)
                    .header(Constant.CAM_SEARCH_ATTR_KEY, tokenRequestTO.getSearchAttributes().get(0).getAttributeName())
                    .header(Constant.CAM_SEARCH_ATTR_VALUE, tokenRequestTO.getSearchAttributes().get(0).getAttributeValue());
        }

        try {
            return invocationBuilder.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), AccessTokenResponse.class);
        }
        catch (WebApplicationException e) {
            handleWebExceptionFromCam(e,tokenRequestTO);
        }
        return null;
    }

    private void handleWebExceptionFromCam(WebApplicationException e, TokenRequestTO tokenRequestTO) throws AuthException {
        CamError error = e.getResponse().readEntity(CamError.class);
        if(error.getError().equals(INVALID_GRANT)){
               tokenRequestTO.setCamError(true);
        }
        else{
            throw new AuthException(e, errorConstant.getERROR_CODE_PERMISSION_DENIED(),error.getError_description());
        }
    }
}
