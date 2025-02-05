package in.fortytwo42.adapter.cam.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import in.fortytwo42.adapter.cam.config.CamAdminClientConfig;
import in.fortytwo42.adapter.cam.dto.CamAttribute;
import in.fortytwo42.adapter.cam.dto.Client;
import in.fortytwo42.adapter.cam.dto.ClientTO;
import in.fortytwo42.adapter.cam.dto.Credential;
import in.fortytwo42.adapter.cam.dto.EditUserRequest;
import in.fortytwo42.adapter.cam.dto.ResetPasswordUserRequest;
import in.fortytwo42.adapter.cam.dto.UserCreationRequest;
import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.cam.util.CamResponseUtil;
import in.fortytwo42.adapter.cam.util.MappingUtil;
import in.fortytwo42.adapter.exception.CamUserExistsException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.tos.enums.AttributeAction;


public class CamAdminServiceImpl implements CamAdminServiceIntf {


    private static final class InstanceHolder {
        private static final CamAdminServiceImpl INSTANCE = new CamAdminServiceImpl();
        private InstanceHolder() {
            super();
        }
    }

    public static CamAdminServiceImpl getInstance() {
        return CamAdminServiceImpl.InstanceHolder.INSTANCE;
    }
    private final Keycloak camRestInstance = CamAdminClientConfig.getCamRestInstance();
    private final String CAM_ADMIN_SERVICE_IMPL_LOG = "<<<<< CamAdminServiceImpl";
    /**
     * creation of log 4j object for each class
     */
    private  static Logger logger= LogManager.getLogger(CamAdminServiceImpl.class);


    @Override
    public Client createClient(String realm, ClientTO clientTO) {
        try {
            RealmResource realResource = camRestInstance.realm(realm);
            ClientRepresentation clientRepresentation = MappingUtil.getClientRepresentation(clientTO);
            Response clientResponse = realResource.clients().create(clientRepresentation);
            String clientUuid = CamResponseUtil.getCreatedId(clientResponse);
            return Client
                    .builder()
                    .clientId(clientTO.getClientId())
                    .clientKcId(clientUuid)
                    .build();
        } catch (CamUserExistsException e){
            try{
                String clientUuid = getClient(realm,clientTO.getClientId()).getId();
                return Client
                        .builder()
                        .clientId(clientTO.getClientId())
                        .clientKcId(clientUuid)
                        .build();
            } catch (Exception e1) {
                logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " Create Client on CAM Failed for Client ID=" + clientTO.getClientId());
                logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e1.getStackTrace()));
                throw e1;
            }
        }
        catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " Create Client on CAM Failed for Client ID=" + clientTO.getClientId());
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

    @Override
    public Client createClientWithRepresentation(String realm, ClientRepresentation clientRepresentation) throws CamUserExistsException {
        try {
            RealmResource realResource = camRestInstance.realm(realm);
            Response clientResponse = realResource.clients().create(clientRepresentation);
            String clientUuid = CamResponseUtil.getCreatedId(clientResponse);
            return Client
                    .builder()
                    .clientId(clientRepresentation.getClientId())
                    .clientKcId(clientUuid)
                    .build();
        } catch (CamUserExistsException e){
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
            throw e;
        }
        catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " Create Client on CAM Failed for Client ID=" + clientRepresentation.getClientId());
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }
    @Override
    public String getClientSecret(String realm, String clientId) {
        try {
            CredentialRepresentation credentials = camRestInstance.realm(realm).
                    clients().get(clientId).getSecret();
            return credentials.getValue();
        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " Get Client Secret from CAM Failed for Client ID=" + clientId);
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

    @Override
    public ClientRepresentation getClient(String realm, String clientId) {
        try {
            ClientRepresentation clientRepresentation = camRestInstance.realm(realm).
                    clients().findByClientId(clientId).get(0);
            clientRepresentation.setSecret(getClientSecret(realm,clientRepresentation.getId()));
            return clientRepresentation;
        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " Get Client Secret from CAM Failed for Client ID=" + clientId);
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }
    @Override
    public boolean createClientRole(String realm, String clientId, String roleName) {
        try {
            RealmResource realResource = camRestInstance.realm(realm);
            RoleRepresentation roleRepresentation = new RoleRepresentation();
            roleRepresentation.setClientRole(true);
            roleRepresentation.setName(roleName);
            realResource.clients().get(clientId).roles().create(roleRepresentation);
            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " Role creation on CAM failed for roleName=" + roleName + " and clientId=" + clientId);
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

    @Override
    public RoleRepresentation getRoleInfo(String realm, String clientId, String roleName) {
        try {
            return camRestInstance.realm(realm)
                    .clients()
                    .get(clientId)
                    .roles()
                    .get(roleName).toRepresentation();

        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " Get Role Info from CAM failed for roleName=" + roleName + " and clientId=" + clientId);
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }
    @Override
    public boolean removeClient(String realm, String clientId) {
        try {
            camRestInstance.realm(realm).clients().get(clientId).remove();
            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " Remove Client from CAM failed for clientId=" + clientId);
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }
    @Override
    public UserResponseDto createUser(String realm, UserCreationRequest userCreationRequest) {
        UserRepresentation user = new UserRepresentation();
        try {
            user.setEnabled(true);
            user.setEmailVerified(true);
            user.setUsername(userCreationRequest.getUsername());
            user.setEmail(userCreationRequest.getEmail());

            Map<String, List<String>> attributes = new HashMap<>();
            List<CamAttribute> camAttributes = userCreationRequest.getAttributes();
            for (CamAttribute attribute : camAttributes) {
                if (attributes.get(attribute.getCustomAttributeName()) != null && !(attributes.get(attribute.getCustomAttributeName())).isEmpty()) {
                    List<String> existingAttributeValues = attributes.get(attribute.getCustomAttributeName());
                    List<String> updatedAttributeValues = existingAttributeValues != null ?
                            new ArrayList<>(existingAttributeValues) :
                            new ArrayList<>();
                    updatedAttributeValues.add(attribute.getCustomAttributeValue());// Update the user with the new attribute values
                    attributes.put(attribute.getCustomAttributeName(), updatedAttributeValues);
                }
                else {
                    attributes.put(attribute.getCustomAttributeName(), Collections.singletonList(attribute.getCustomAttributeValue()));
                }
            }
            user.setAttributes(attributes);

            if (userCreationRequest.getCredentials() != null && !userCreationRequest.getCredentials().isEmpty()) {
                List<CredentialRepresentation> credentials = new ArrayList<>();
                for (Credential credential : userCreationRequest.getCredentials()) {
                    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                    credentialRepresentation.setTemporary(credential.getTemporary());
                    credentialRepresentation.setType(credential.getType());
                    credentialRepresentation.setValue(credential.getValue());
                    credentials.add(credentialRepresentation);
                }
                user.setCredentials(credentials);
            }

            RealmResource realmResource = camRestInstance.realm(realm);
            UsersResource userResources = realmResource.users();
            Response userCreationResponse = userResources.create(user);
            logger.log(Level.DEBUG, CAM_ADMIN_SERVICE_IMPL_LOG + " cam----------------response " + userCreationResponse);
            UserResponseDto responseDto = new UserResponseDto();
            responseDto.setUserKcId(CamResponseUtil.getCreatedId(userCreationResponse));
            responseDto.setUsername(userCreationRequest.getUsername());
            responseDto.setAttributes(user.getAttributes());
            logger.log(Level.DEBUG, CAM_ADMIN_SERVICE_IMPL_LOG + " cam----------------responseDto " + responseDto);
            return responseDto;
        } catch (CamUserExistsException e){
            try {
                UserResponseDto responseDto = new UserResponseDto();
                responseDto.setUserKcId(getUserDetailsWithUsername(realm, userCreationRequest.getUsername()).getId());
                responseDto.setUsername(userCreationRequest.getUsername());
                responseDto.setAttributes(user.getAttributes());
                logger.log(Level.DEBUG, CAM_ADMIN_SERVICE_IMPL_LOG + " cam----------------responseDto " + responseDto);
                return responseDto;
            } catch (Exception e1){
                logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " CAM User Creation failed for username=" + userCreationRequest.getUsername());
                logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e1.getStackTrace()));
                throw e1;
            }
        }
        catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " CAM User Creation failed for username=" + userCreationRequest.getUsername());
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }
    public boolean editUser(String realm, EditUserRequest editUserRequest) {
        try {
            RealmResource realmResource = camRestInstance.realm(realm);
            UserResource userResource = realmResource.users().get(editUserRequest.getUserKcId());
            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(Objects.isNull(editUserRequest.getEnabled()) || editUserRequest.getEnabled());
            List<CamAttribute> camAttributes = editUserRequest.getAttributes();
            for (CamAttribute attribute : camAttributes) {
                List<String> existingAttributeValues = user.getAttributes().get(attribute.getCustomAttributeName());
                List<String> updatedAttributeValues = existingAttributeValues != null ?
                        new ArrayList<>(existingAttributeValues) :
                        new ArrayList<>();
                if (editUserRequest.getAttributeAction() != null && editUserRequest.getAttributeAction().equals(AttributeAction.DELETE)) {
                    updatedAttributeValues.remove(attribute.getCustomAttributeValue());
                    if (updatedAttributeValues.isEmpty()) {
                        user.getAttributes().remove(attribute.getCustomAttributeName());
                    }
                    else {
                        user.getAttributes().put(attribute.getCustomAttributeName(), updatedAttributeValues);
                    }
                }
                else {
                    updatedAttributeValues.add(attribute.getCustomAttributeValue());
                    user.getAttributes().put(attribute.getCustomAttributeName(), updatedAttributeValues);
                }
                // Update the user with the new attribute value
            }
            userResource.update(user);
            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " CAM Edit User failed for user kcId=" + editUserRequest.getUserKcId());
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    public boolean updateAllCamAttributes(String realm, String kc_id, Map<String,List<String>> updatedAttributes) {
        try {
            RealmResource realmResource = camRestInstance.realm(realm);
            UserResource userResource = realmResource.users().get(kc_id);
            UserRepresentation user = userResource.toRepresentation();
            user.setAttributes(updatedAttributes);
            userResource.update(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.ERROR,e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    @Override
    public List<String> getUserAttribute(String realm,String userKcId,String attributeName){
        RealmResource realmResource = camRestInstance.realm(realm);
        UserResource userResource = realmResource.users().get(userKcId);
        UserRepresentation user = userResource.toRepresentation();
        return user.getAttributes().get(attributeName);
    }


    @Override
    public boolean resetUserPassword(String realm, String userKcId, ResetPasswordUserRequest request) {
        try {
            RealmResource realmResource = camRestInstance.realm(realm);
            UserResource userResource = realmResource.users().get(userKcId);
//            UserRepresentation user = userResource.toRepresentation();

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            List<CredentialRepresentation> credentials = new ArrayList<>();
            credentialRepresentation.setTemporary(request.getTemporary());
            credentialRepresentation.setType(request.getType());
            credentialRepresentation.setValue(request.getValue());
            credentials.add(credentialRepresentation);
//            user.setCredentials(credentials);

            userResource.resetPassword(credentialRepresentation);
//            userResource.update(user);
            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " CAM Reset Password failed for user kcId=" + userKcId);
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    @Override
    public boolean updateClient(String realm, String clientId, ClientTO clientTO) {
        try {
            ClientResource existingClientResource =
                    camRestInstance.realm(realm).clients().get(clientId);
            ClientRepresentation existingClient = existingClientResource.toRepresentation();
            if (clientTO.getSecret() != null) {
                existingClient.setSecret(clientTO.getSecret());
            }
            if(clientTO.getClientId()!=null){
                existingClient.setClientId(clientTO.getClientId());
            }
            if(clientTO.getAttributes()!=null) {
                existingClient.setAttributes(clientTO.getAttributes());
            }
            existingClientResource.update(existingClient);
            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " CAM Update Client failed for clientId=" + clientId);
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    /** role name is same as client name or application name clientId field in cam**/
    @Override
    public boolean assignClientRoleToUser(String realm, String clientKcId, String userKcId, String roleName) {
        try {
            RealmResource realmResource = camRestInstance.realm(realm);
            ClientResource existingClientResource = realmResource.clients().get(clientKcId);
            RoleRepresentation role = existingClientResource.roles().get(roleName).toRepresentation();
            realmResource.users().get(userKcId).roles().clientLevel(clientKcId).add(Collections.singletonList(role));
            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + " CAM Assign Client Role to User failed for clientKcId=" + clientKcId + ", userKcId=" + userKcId + ", roleName=" + roleName);
            logger.log(Level.ERROR, CAM_ADMIN_SERVICE_IMPL_LOG + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    /**
     * role name is same as client name or application name clientId field in cam
     *
     * @return
     **/
    @Override
    public boolean removeClientRoleFromUser(String realm, String clientKcId, String userKcId, String roleName) {
        RealmResource realmResource = camRestInstance.realm(realm);
        UserResource userResource = realmResource.users().get(userKcId);
        ClientResource existingClientResource = realmResource.clients().get(clientKcId);
        RoleRepresentation role = existingClientResource.roles().get(roleName).toRepresentation();
        boolean ispresent=false;
        if(userResource!=null) {
            try {
                ispresent = userResource.roles().clientLevel(clientKcId).listAll().contains(role);
            }catch (NotFoundException e){
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            if(ispresent) {
                userResource.roles().clientLevel(clientKcId).remove(Collections.singletonList(role));
            }
        }
        return true;
    }
    public boolean isBindingPresentOnCam(String realm, String clientKcId, String userKcId, String roleName) {
        RealmResource realmResource = camRestInstance.realm(realm);
        UserResource userResource = realmResource.users().get(userKcId);
        ClientResource existingClientResource = realmResource.clients().get(clientKcId);
        RoleRepresentation role = existingClientResource.roles().get(roleName).toRepresentation();
        boolean ispresent=false;
        if(userResource!=null) {
            ispresent= userResource.roles().clientLevel(clientKcId).listAll().contains(role);
        }
        return ispresent;
    }

    public boolean deleteUser(String realm, String userKcId){
        RealmResource realmResource = camRestInstance.realm(realm);
        UsersResource usersResource = realmResource.users();
        boolean camUserDeleted = false;
        if(realmResource.users().get(userKcId).roles().getAll().getClientMappings() == null){
            usersResource.delete(userKcId);
            camUserDeleted = true;
        }
        return camUserDeleted;
    }

    public UserRepresentation getUserDetails(String realm, String userKcId){
        RealmResource realmResource = camRestInstance.realm(realm);
        UserResource userResource = realmResource.users().get(userKcId);
        return  userResource.toRepresentation();
    }

    @Override
    public UserRepresentation getUserDetailsWithUsername(String realm, String userName) {
        RealmResource realmResource = camRestInstance.realm(realm);
        List<UserRepresentation> userResource = realmResource.users().search(userName, true);
        return userResource.get(0);
    }

    @Override
    public boolean deleteUsers(String realm, String userKcId) {
        RealmResource realmResource = camRestInstance.realm(realm);
        UsersResource usersResource = realmResource.users();
        boolean camUserDeleted = false;
        try {
            usersResource.delete(userKcId);
            camUserDeleted = true;
        } catch (Exception e){
            throw e;
        }
        return camUserDeleted;
    }

}
