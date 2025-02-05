
package in.fortytwo42.adapter.facade;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserGroupServiceIntf;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserGroupApplicationRelTO;
import in.fortytwo42.adapter.transferobj.UserUserGroupRelTO;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.ApplicationDaoImpl;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.UserGroupNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.UserGroupApplicationRel;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.UserGroupTO;

public class UserGroupFacadeImpl implements UserGroupFacadeIntf {

    private static final String USER_GROUP_FACADE_LOG = "<<<<< UserGroupFacadeImpl";

    private static Logger logger= LogManager.getLogger(UserGroupFacadeImpl.class);

    private UserGroupServiceIntf userGroupServiceIntf = ServiceFactory.getUserGroupService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private UserGroupFacadeImpl() {
        super();
    }

    private static final class InstanceHolder {

        private static final UserGroupFacadeImpl INSTANCE = new UserGroupFacadeImpl();

        private InstanceHolder() {
            super();
        }

    }

    public static UserGroupFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public UserGroupTO getUserGroup(String groupName) throws UserGroupNotFoundException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " getUserGroup : start");
        UserGroupTO userGroupTO = userGroupServiceIntf.getUserGroup(groupName);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " getUserGroup : end");
        return userGroupTO;
    }

    @Override
    public UserGroupTO createUserGroup(UserGroupTO userGroupTO, String role, String actor,Long id,boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " createUserGroup : start");
        try {
            userGroupServiceIntf.getUserGroup(userGroupTO.getGroupname());
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_ALREADY_EXISTS(), errorConstant.getHUMANIZED_USER_GROUP_ALREADY_EXISTS());
        }
        catch (UserGroupNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            userGroupTO = requestService.createUserGroupCreateRequest(session, userGroupTO, actor,id, saveRequest);
            if (!saveRequest) {
                userGroupTO = approveCreateUserGroupRequest(session, userGroupTO, actor);
            }
            logger.log(Level.DEBUG, "&&&&& userGroup : " + new Gson().toJson(userGroupTO));
            userGroupTO.setStatus(Constant.SUCCESS_STATUS);
            userGroupTO.setId(userGroupTO.getId());
            sessionFactoryUtil.closeSession(session);
            return userGroupTO;
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " createUserGroup : end");
        }
    }

    @Override
    public UserGroupTO approveCreateUserGroupRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveCreateUserGroupRequest : start");
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "create user group request approved successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);

        userGroupTO = userGroupServiceIntf.createUserGroup(session, userGroupTO);
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "user group created successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveCreateUserGroupRequest : end");
        return userGroupTO;
    }

    @Override
    public UserGroupTO updateUserGroup(UserGroupTO userGroupTO, String role, String actor,Long id,boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " updateUserGroup : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            validateUpdateRequest(userGroupTO);
            userGroupTO = requestService.createUserGroupUpdateRequest(session, userGroupTO, actor,id, saveRequest);
            if (!saveRequest) {
                userGroupTO = approveUpdateUserGroupRequest(session, userGroupTO, actor);
            }
            userGroupTO.setStatus(Constant.SUCCESS_STATUS);
            userGroupTO.setId(userGroupTO.getId());
            sessionFactoryUtil.closeSession(session);
            return userGroupTO;
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " updateUserGroup : end");
        }
    }

    private void validateUpdateRequest(UserGroupTO userGroupTO) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " validateUpdateRequest : start");
        UserGroupTO userGroup = null;
        try {
            userGroup = userGroupServiceIntf.getUserGroup(userGroupTO.getGroupname());
        }
        catch (UserGroupNotFoundException e1) {
            logger.log(Level.FATAL, e1.getMessage(), e1);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_GROUP_NOT_FOUND());
        }
        boolean isTwoFaUpdated = false;
        boolean isUserStatusUpdated = false;
        if (!userGroupTO.getTwoFactorStatus().equals(userGroup.getTwoFactorStatus())) {
            isTwoFaUpdated = true;
        }
        if (!userGroupTO.getUserStatus().equals(userGroup.getUserStatus())) {
            isUserStatusUpdated = true;
        }
        if (!isTwoFaUpdated && !isUserStatusUpdated) {
            throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
        }
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " validateUpdateRequest : end");
    }

    @Override
    public UserGroupTO approveUpdateUserGroupRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveUpdateUserGroupRequest : start");
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "edit user group request approved ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        userGroupTO = userGroupServiceIntf.updateUserGroup(session, userGroupTO);
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + " edit user group successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveUpdateUserGroupRequest : end");
        return userGroupTO;
    }

    @Override
    public PaginatedTO<UserGroupTO> getUserGroups(int pageNo, String searchText) {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " getUserGroups : start");
        int limit = Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT));
        PaginatedTO<UserGroupTO> usergroupTOs = userGroupServiceIntf.getUserGroups(pageNo, limit, searchText);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " getUserGroups : end");
        return usergroupTOs;
    }

    @Override
    public UserGroupTO addUserGroupMapping(UserGroupTO userGroupTO, String role, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " addUserGroupMapping : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            userGroupTO = requestService.createUserGroupMappingRequest(session, userGroupTO, actor,id, saveRequest);
            if (!saveRequest) {
                userGroupTO = approveUserGroupMappingRequest(session, userGroupTO, actor);
            }
            userGroupTO.setStatus(Constant.SUCCESS_STATUS);
            userGroupTO.setId(userGroupTO.getId());
            sessionFactoryUtil.closeSession(session);
            return userGroupTO;
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " addUserGroupMapping : end");
        }
    }

    @Override
    public UserGroupTO approveUserGroupMappingRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveUserGroupMappingRequest : start");
        String userGroupName=userGroupTO.getGroupname();
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "user to usergroup request approved ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        userGroupTO = userGroupServiceIntf.addUserGroupMapping(session, userGroupTO);
        AuditLogUtil.sendAuditLog(userGroupName + "user to usergroup mapped successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveUserGroupMappingRequest : end");
        return userGroupTO;
    }

    @Override
    public UserGroupTO addUserGroupApplicationMapping(UserGroupTO userGroupTO, String role, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " addUserGroupApplicationMapping : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            validateGroup(userGroupTO);
            userGroupTO = requestService.createApplicationUserGroupMappingRequest(session, userGroupTO, actor,id, saveRequest);
            if (!saveRequest) {
                userGroupTO = approveApplicationUserGroupMappingRequest(session, userGroupTO, actor);
            }
            userGroupTO.setStatus(Constant.SUCCESS_STATUS);
            userGroupTO.setId(userGroupTO.getId());
            sessionFactoryUtil.closeSession(session);
            return userGroupTO;
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " addUserGroupApplicationMapping : end");
        }
    }

    private void validateGroup(UserGroupTO userGroupTO) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " isApplicationAlreadyMappedToGroup : start");
        StringBuilder mappedAppliations = new StringBuilder();
        for (ApplicationTO applicationTO : userGroupTO.getApplications()) {
            if (Constant.ADD.equals(applicationTO.getStatus())) {
                Application application = null;
                try {
                    application = ApplicationDaoImpl.getInstance().getApplicationByApplicationId(applicationTO.getApplicationId());
                }
                catch (ApplicationNotFoundException e) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
                }
                List<UserGroupApplicationRel> userGroupApplicationRel = DaoFactory.getUserApplicationServiceRelDao().getUserGroupApplicationRels(application.getId());
                if (userGroupApplicationRel != null && !userGroupApplicationRel.isEmpty()) {
                    mappedAppliations.append(application.getApplicationName()).append(Constant._COMMA);
                }
            }
        }
        if (mappedAppliations.length() > 0) {
            String errorMessage = mappedAppliations.subSequence(0, mappedAppliations.length() - 1) + " - "+errorConstant.getERROR_MESSAGE_APPLICATION_ALREADY_BINDED();
            logger.log(Level.DEBUG, "<<<<< : "+errorMessage);
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_ALREDY_BINDED(), errorMessage);
        }
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " isApplicationAlreadyMappedToGroup : end");
    }

    @Override
    public UserGroupTO approveApplicationUserGroupMappingRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveApplicationUserGroupMappingRequest : start");
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "application to usergroup mapping  request approved ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        userGroupTO = ServiceFactory.getUserApplicationServiceRelService().addUserGroupApplicationMapping(session, userGroupTO);
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "application to usergroup mapped successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveApplicationUserGroupMappingRequest : end");
        return userGroupTO;
    }

    @Override
    public PaginatedTO<UserUserGroupRelTO> getUserUserGroupMapping(Long groupId, int pageNo, String searchText) {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveApplicationUserGroupMappingRequest : start");
        PaginatedTO<UserUserGroupRelTO> userUserGroupRelTOs = ServiceFactory.getUserUserGroupRelService().getUserUserGroupMapping(groupId, pageNo, searchText);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveApplicationUserGroupMappingRequest : end");
        return userUserGroupRelTOs;
    }

    @Override
    public PaginatedTO<UserGroupApplicationRelTO> getUserGroupApplicationMapping(long groupId, int pageNo, String searchText) {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " getUserGroupApplicationMapping : start");
        int limit = Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT));
        PaginatedTO<UserGroupApplicationRelTO> userGroupApplicationRelTO = ServiceFactory.getUserApplicationServiceRelService().getUserGroupApplicationMapping(groupId, pageNo, limit, searchText);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " getUserGroupApplicationMapping : end");
        return userGroupApplicationRelTO;
    }

    @Override
    public UserGroupTO removeUserGroup(String role, String actor,Long id, UserGroupTO userGroupTO,boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " removeUserGroup : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            userGroupTO = requestService.removeUserGroupRequest(session, userGroupTO, actor,id, saveRequest);
            if (!saveRequest) {
                userGroupTO = approveUserGroupDeleteRequest(session, userGroupTO, actor);
            }
            userGroupTO.setStatus(Constant.SUCCESS_STATUS);
            userGroupTO.setId(userGroupTO.getId());
            sessionFactoryUtil.closeSession(session);
            return userGroupTO;
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " removeUserGroup : end");
        }
    }

    @Override
    public UserGroupTO approveUserGroupDeleteRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveUserGroupDeleteRequest : start");
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + " usergroup delete request approved ", "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", "", null);
        userGroupTO = userGroupServiceIntf.removeUserGroup(session, userGroupTO);
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + " usergroup deleted successfully ", "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " approveUserGroupDeleteRequest : end");
        return userGroupTO;
    }
    
    @Override
    public PaginatedTO<UserGroupTO> getGroupsForUser(int pageNo, String searchText, Long userId) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " getGroupsForUser : start");
        int limit = Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT));
        PaginatedTO<UserGroupTO> usergroupTOs = userGroupServiceIntf.getGroupsForUser(pageNo, limit, searchText, userId);
        logger.log(Level.DEBUG, USER_GROUP_FACADE_LOG + " getGroupsForUser : end");
        return usergroupTOs;
    } 
}
