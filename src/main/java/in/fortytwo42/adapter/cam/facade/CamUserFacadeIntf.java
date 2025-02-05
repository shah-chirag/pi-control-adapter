package in.fortytwo42.adapter.cam.facade;

import java.util.List;
import java.util.Map;

import in.fortytwo42.entities.bean.User;
import org.keycloak.representations.idm.UserRepresentation;

import in.fortytwo42.adapter.cam.dto.EditUserRequest;
import in.fortytwo42.adapter.cam.dto.ResetPasswordUserRequest;
import in.fortytwo42.adapter.cam.dto.UserCreationRequest;
import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.exception.AuthException;

public interface CamUserFacadeIntf {

    UserResponseDto onboardCamUser(String realm, UserCreationRequest userCreationRequest) throws AuthException;

    void editCamUser(String realm, EditUserRequest editUserRequest) throws AuthException;

    boolean bindUserToApplication(String realm, String clientId, String userId, String roleName, String operation) throws AuthException;

    boolean deleteUser(String realm, String userKcId) throws AuthException;
    void updateAllCamAttributes(String realm, String kc_id, Map<String,List<String>> updatedAttributes) throws AuthException;

    void resetPassword(String realm, String userKcId, ResetPasswordUserRequest request) throws AuthException;
    UserRepresentation getUserDetails(String realm, String userKcId) throws AuthException;

    List<String> getUserAttributes(String realm, String userKcId, String attributeName) throws AuthException;

    boolean deleteUsers(String realm, String userKcId) throws AuthException;

    boolean attributeExistOnCam(String realm, String kcId, String attributeName, String attributeValue, User user) throws AuthException;

    UserRepresentation getUserDetailsWithUsername(String realm, String userName) throws AuthException;
    public boolean isBindingPresentOnCam(String realm,String clientKcId ,String userKcId,String roleName) throws AuthException;
}
