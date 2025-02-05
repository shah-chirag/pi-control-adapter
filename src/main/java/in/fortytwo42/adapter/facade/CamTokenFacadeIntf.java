package in.fortytwo42.adapter.facade;

import org.keycloak.representations.AccessTokenResponse;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.tos.transferobj.TokenRequestTO;

public interface CamTokenFacadeIntf {
    AccessTokenResponse getToken(String realm, TokenRequestTO tokenRequestTO) throws AuthException, IAMException;
}
