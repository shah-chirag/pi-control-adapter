package in.fortytwo42.adapter.cam.service;

import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import in.fortytwo42.adapter.cam.dto.Client;
import in.fortytwo42.adapter.cam.dto.ClientTO;
import in.fortytwo42.adapter.cam.dto.EditUserRequest;
import in.fortytwo42.adapter.cam.dto.ResetPasswordUserRequest;
import in.fortytwo42.adapter.cam.dto.UserCreationRequest;
import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.CamUserExistsException;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;

public interface CamAdminServiceIntf {

    Client createClient(String realm, ClientTO clientTO);

    Client createClientWithRepresentation(String realm, ClientRepresentation clientRepresentation) throws CamUserExistsException;

    String getClientSecret(String realm, String clientId);

    ClientRepresentation getClient(String realm, String clientId);

    boolean createClientRole(String realm, String clientId, String roleName) throws AuthException;

    RoleRepresentation getRoleInfo(String realm, String clientId, String roleName);

    boolean removeClient(String realm, String clientId);

    UserResponseDto createUser(String realm, UserCreationRequest userCreationRequest);
    boolean updateClient(String realm, String clientId, ClientTO clientTO);

    boolean assignClientRoleToUser(String realm, String clientKcId, String userKcId, String roleName);

    boolean removeClientRoleFromUser(String realm, String clientKcId, String userKcId, String roleName);

    public boolean isBindingPresentOnCam(String realm, String clientKcId, String userKcId, String roleName);
    boolean editUser(String realm, EditUserRequest editUserRequest);
    boolean updateAllCamAttributes(String realm, String kc_id, Map<String,List<String>> updatedAttributes);

    List<String> getUserAttribute(String realm, String userKcId, String attributeName);

    boolean resetUserPassword(String realm, String userKcId, ResetPasswordUserRequest request);

    boolean deleteUser(String realm, String userKcId);

    UserRepresentation getUserDetails(String realm, String userKcId);

    UserRepresentation getUserDetailsWithUsername(String realm, String userName);

    boolean deleteUsers(String realm, String userKcId);
}
