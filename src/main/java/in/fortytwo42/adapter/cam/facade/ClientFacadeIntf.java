package in.fortytwo42.adapter.cam.facade;

import org.keycloak.representations.idm.ClientRepresentation;

import in.fortytwo42.adapter.cam.dto.Client;
import in.fortytwo42.adapter.cam.dto.ClientTO;
import in.fortytwo42.adapter.exception.AuthException;

public interface ClientFacadeIntf {
    Client onboardClient(String realm, ClientTO clientTO) throws AuthException;

    Client onboardClientWithCLientRepresentation(String realm, ClientRepresentation clientRepresentation) throws AuthException;

    ClientRepresentation getClient(String realm, String clientId) throws AuthException;

    void editClient(String realm, String clientId, ClientTO clientTO) throws AuthException;
}
