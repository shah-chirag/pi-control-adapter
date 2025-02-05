package in.fortytwo42.adapter.cam.facade;

import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.entities.bean.User;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.keycloak.representations.idm.UserRepresentation;

import in.fortytwo42.adapter.cam.dto.EditUserRequest;
import in.fortytwo42.adapter.cam.dto.ResetPasswordUserRequest;
import in.fortytwo42.adapter.cam.dto.UserCreationRequest;
import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.cam.service.CamAdminServiceIntf;
import in.fortytwo42.adapter.cam.util.TimeUtil;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Constant;

public class CamUserFacadeImpl implements CamUserFacadeIntf {

    private final String CAM_USER_FACADE_IMPL_LOG = "<<<<< CamUserFacadeImpl";

    TimeUtil timeUtil = new TimeUtil();

    private final CamAdminServiceIntf CamAdminService = ServiceFactory.getCamAdminService();
    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(CamUserFacadeImpl.class);

    private static final class InstanceHolder {
        private static final CamUserFacadeImpl INSTANCE = new CamUserFacadeImpl();
        private InstanceHolder() {
            super();
        }
    }

    public static CamUserFacadeImpl getInstance() {
        return CamUserFacadeImpl.InstanceHolder.INSTANCE;
    }


    @Override
    public UserResponseDto onboardCamUser(String realm, UserCreationRequest userCreationRequest) throws AuthException{
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " onboardCamUser : start");
        UserResponseDto user;
        try{

            long startTime = timeUtil.start();
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Create user started on " + startTime);

            user = CamAdminService.createUser(realm, userCreationRequest);

            long totalTime = timeUtil.stop();
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);

        }
        catch (WebApplicationException e) {
            logger.log(Level.ERROR, "cam user creation failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "cam user creation failed");
        }
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " onboardCamUser : end");
        return user;
    }

    @Override
    public void editCamUser(String realm, EditUserRequest editUserRequest) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " editCamUser : start");
        try{
            long startTime = timeUtil.start();
            logger.log(Level.DEBUG, "user_id : " + editUserRequest.getUserKcId());
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Edit user started on " + startTime);
            boolean camStatus = CamAdminService.editUser(realm, editUserRequest);
            if (!camStatus) {
                logger.log(Level.ERROR, CAM_USER_FACADE_IMPL_LOG + " CAM Edit user failed.");
                throw new Exception();
            }
            long  totalTime = timeUtil.stop();
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
        }
        catch(WebApplicationException e){
            logger.log(Level.ERROR, "cam edit user failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "cam edit user failed");
        }
        catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        }
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " editCamUser : end");
    }

    public void updateAllCamAttributes(String realm, String kc_id, Map<String,List<String>> updatedAttributes) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " editCamUser : start");
        try{
            long startTime = timeUtil.start();
            logger.log(Level.DEBUG, "user_id : " + kc_id);
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Edit user started on " + startTime);
            boolean camStatus = CamAdminService.updateAllCamAttributes(realm, kc_id, updatedAttributes);
            long  totalTime = timeUtil.stop();
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
        }
        catch(WebApplicationException e){
            logger.log(Level.ERROR, "cam edit user failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "cam edit user failed");
        }
        catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        }
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " editCamUser : end");
    }

    @Override
    public boolean bindUserToApplication(String realm, String clientId, String userId, String roleName, String operation) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " bindUserToApplication : start");
        boolean camUserDeleted = false;
        try {
            long startTime = timeUtil.start();
            logger.log(Level.DEBUG, "user_id : " + userId);
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Bind user started on " + startTime);
            if(operation.equalsIgnoreCase(Constant.BIND_OPERATION)) {
                boolean camStatus = CamAdminService.assignClientRoleToUser(realm, clientId, userId, roleName);
                if (!camStatus) {
                    logger.log(Level.ERROR, CAM_USER_FACADE_IMPL_LOG + " CAM Bind User to Application Failed.");
                    throw new AuthException(new Exception(), 500L, "CAM - bind user to application failed");
                }
            }
            else {
                camUserDeleted = CamAdminService.removeClientRoleFromUser(realm, clientId, userId, roleName);
            }
            long  totalTime = timeUtil.stop();
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);

        } catch (Exception e) {
            logger.log(Level.ERROR, "CAM - bind user to application failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "CAM - bind user to application failed");
        }
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " bindUserToApplication : end");
        return camUserDeleted;
    }

    @Override
    public boolean deleteUser(String realm, String userKcId) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " cam - deleteUser if there is no binding : start");
        boolean userDeleted = false;
        try
        {
            long startTime = timeUtil.start();
            logger.log(Level.DEBUG, "user_id : " + userKcId);
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Delete user started on " + startTime);
            userDeleted = CamAdminService.deleteUser(realm, userKcId);
            long  totalTime = timeUtil.stop();
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
        }
        catch(Exception e)
        {
            logger.log(Level.ERROR, "cam - delete user to failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "cam - delete user failed");
        }
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " cam - deleteUser if there is no binding : end");
        return userDeleted;
    }

    @Override
    public void resetPassword(String realm, String userKcId, ResetPasswordUserRequest request) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " resetPassword : start");
        try{
            long startTime = timeUtil.start();
            logger.log(Level.DEBUG, "user_id : " + userKcId);
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Reset Password started on " + startTime);
            boolean camStatus = CamAdminService.resetUserPassword(realm, userKcId, request);
            if (!camStatus) {
                logger.log(Level.ERROR, CAM_USER_FACADE_IMPL_LOG + " CAM Reset Password failed.");
                throw new AuthException(new Exception(), 500L, "Keycloak reset password failed");
            }
            long  totalTime = timeUtil.stop();
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
        }
        catch(WebApplicationException e){
            logger.log(Level.ERROR, "Keycloak reset password failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "Keycloak reset password failed");
        }
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " resetPassword : end");
    }


    @Override
    public UserRepresentation getUserDetails(String realm, String userKcId) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getUserDetails : start");
        try{
            return CamAdminService.getUserDetails(realm, userKcId);
        }
        catch(Exception e){
            logger.log(Level.ERROR, "cam get user detail failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "cam get user detail failed");
        } finally {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getUserDetails : end");
        }
    }

    @Override
    public List<String> getUserAttributes(String realm, String userKcId, String attributeName) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getUserAttributes : start");
        try{
            return CamAdminService.getUserAttribute(realm, userKcId,attributeName);
        }
        catch(Exception e){
            logger.log(Level.ERROR, "cam get user detail failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "cam get user detail failed");
        } finally {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getUserAttributes : end");
        }
    }

    @Override
    public boolean deleteUsers(String realm, String userKcId) throws AuthException {
        try {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " deleteUsers : start");
            return CamAdminService.deleteUsers(realm,userKcId);
        }catch(Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " deleteUsers : end");
        }

    }

        @Override
    public boolean attributeExistOnCam(String realm, String kcId, String attributeName, String attributeValue, User user) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " attributeExistOnCam : start");
        try {
            boolean isExist = false;
            UserRepresentation userRepresentation = getCAMUserDetails(user, realm, kcId);
            List<String> camAttributes = userRepresentation.getAttributes().get(attributeName);
            if (camAttributes == null || camAttributes.isEmpty()) {
                return isExist;
            }
            for (String number : camAttributes) {
                if (number.equalsIgnoreCase(attributeValue)) {
                    isExist = true;
                    break;
                }
            }
            return isExist;
        }
        catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        }
        finally {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " attributeExistOnCam : end");
        }
    }

    private UserRepresentation getCAMUserDetails(User user, String realm, String kcId) throws AuthException {
        logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getCAMUserDetails : start");
        try {
            return getUserDetails(realm, kcId);
        } catch (AuthException e) {
            if (user != null) {
                return  getUserRepresentation(user, realm, kcId);
            } else {
                throw e;
            }
        } finally {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getCAMUserDetails : end");
        }
    }

    private UserRepresentation getUserRepresentation(User user, String realm, String kcId) throws AuthException {
        try {
            UserRepresentation userRepresentation = getUserDetailsWithUsername(realm, user.getAccountId());
            if(!kcId.equals(userRepresentation.getId())){
                user.setKcId(userRepresentation.getId());
                updateUser(user.getAccountId(), userRepresentation.getId());
            }
            return userRepresentation;
        }catch (Exception e){
            logger.log(Level.DEBUG, e.getMessage());
            throw new AuthException(e, 500L, "cam get user detail failed");
        }
    }


    private void updateUser(String accountId, String kcId) {
        Session session = IamThreadContext.getSessionWithoutTransaction();
        Transaction transaction = session.beginTransaction();
        try {
            User user = ServiceFactory.getUserService().getActiveUser(session, accountId);
            user.setKcId(kcId);
            ServiceFactory.getUserService().updateUser(session, user);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            logger.log(Level.DEBUG, e.getMessage());
        }
    }

    @Override
    public UserRepresentation getUserDetailsWithUsername(String realm, String userName) throws AuthException {
        try {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getUserDetailsWithUsername : start");
            return CamAdminService.getUserDetailsWithUsername(realm,userName);
        }catch(Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getUserDetailsWithUsername : end");
        }
    }

    public boolean isBindingPresentOnCam(String realm,String clientKcId ,String userKcId,String roleName) throws AuthException {
        try {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getUserDetailsWithUsername : start");
            return CamAdminService.isBindingPresentOnCam(realm,clientKcId,userKcId,roleName);
        } catch(Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, CAM_USER_FACADE_IMPL_LOG + " getUserDetailsWithUsername : end");
        }
    }
}
