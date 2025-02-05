
package in.fortytwo42.adapter.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.dao.UserGroupApplicationRelDaoIntf;
import in.fortytwo42.daos.dao.UserGroupDaoIntf;
import in.fortytwo42.daos.dao.UserUserGroupRelDaoInf;
import in.fortytwo42.daos.exception.UserApplicationRelNotFoundException;
import in.fortytwo42.daos.exception.UserGroupNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.exception.UserUserGroupRelNotFoundException;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceCompositeKey;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.bean.UserGroup;
import in.fortytwo42.entities.bean.UserGroupApplicationRel;
import in.fortytwo42.entities.bean.UserUserGroupCompositeKey;
import in.fortytwo42.entities.bean.UserUserGroupRel;
import in.fortytwo42.entities.enums.UserGroupStatus;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.UserGroupTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class UserGroupServiceImpl implements UserGroupServiceIntf {

    private static final String USER_GROUP_SERVICE_LOG = "<<<<< UserGroupServiceImpl";

    private static Logger logger= LogManager.getLogger(UserGroupServiceImpl.class);

    private UserGroupDaoIntf userGroupDaoIntf = DaoFactory.getUserGroupDao();

    private UserServiceIntf userServiceIntf = ServiceFactory.getUserService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private final ExecutorService pool;

    private UserGroupServiceImpl() {
        super();
        int poolSize = 10;
        Config config = Config.getInstance();
        try {
            poolSize = Integer.parseInt(config.getProperty(Constant.CSV_PROCESSING_THREAD_POOL_SIZE));
        }
        catch (NumberFormatException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);
    }

    private static final class InstanceHolder {

        private static UserGroupServiceImpl INSTANCE = new UserGroupServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static UserGroupServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public UserGroupTO getUserGroup(String groupName) throws UserGroupNotFoundException {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " getUserGroup : start");
        UserGroupTO userGroupTO = userGroupDaoIntf.getUserGroup(groupName).convertToTO();
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " getUserGroup : end");
        return userGroupTO;
    }

    @Override
    public UserGroupTO createUserGroup(Session session, UserGroupTO userGroupTO) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " createUserGroup : start");
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupName(userGroupTO.getGroupname());
        userGroup.setTwoFactorStatus(TwoFactorStatus.valueOf(userGroupTO.getTwoFactorStatus()));
        userGroup.setUserStatus(UserStatus.valueOf(userGroupTO.getUserStatus()));
        userGroup.setUserGroupStatus(UserGroupStatus.ACTIVE);
        userGroup = userGroupDaoIntf.create(session, userGroup);
        logger.log(Level.DEBUG, "&&&&& userGroup service : " + new Gson().toJson(userGroup));
        if (userGroupTO.getUsers() != null && !userGroupTO.getUsers().isEmpty()) {
            for (UserTO userTO : userGroupTO.getUsers()) {
                boolean isUpdatUser = false;
                User user;
                try {
                    user = userServiceIntf.getUserByAccountId(userTO.getAccountId());
                }
                catch (UserNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
                }
                if (!userTO.getTwoFactorStatus().equals(user.getTwoFactorStatus().name())) {
                    user.setTwoFactorStatus(TwoFactorStatus.valueOf(userGroupTO.getTwoFactorStatus()));
                    isUpdatUser = true;
                }
                if (!userTO.getUserStatus().equals(user.getUserStatus().name())) {
                    user.setUserStatus(UserStatus.valueOf(userGroupTO.getUserStatus()));
                    isUpdatUser = true;
                }
                if (isUpdatUser) {
                    userServiceIntf.update(session, user);
                }
                UserUserGroupCompositeKey userGroupCompositeKey = new UserUserGroupCompositeKey();
                userGroupCompositeKey.setUser(user);
                userGroupCompositeKey.setUserGroup(userGroup);
                UserUserGroupRel userGroupRel = new UserUserGroupRel();
                userGroupRel.setId(userGroupCompositeKey);
                ServiceFactory.getUserUserGroupRelService().create(session, userGroupRel);
            }
        }
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " createUserGroup : end");
        return userGroup.convertToTO();
    }

    @Override
    public UserGroupTO updateUserGroup(Session session, UserGroupTO userGroupTO) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " updateUserGroup : start");
        boolean isUserGroupupdate = false;
        UserGroup userGroup = null;
        try {
            userGroup = userGroupDaoIntf.getUserGroup(userGroupTO.getGroupname());
        }
        catch (UserGroupNotFoundException e1) {
            logger.log(Level.FATAL, e1.getMessage(), e1);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_GROUP_NOT_FOUND());
        }
        UserUserGroupRelServiceIntf userUserGroupRelServiceIntf = ServiceFactory.getUserUserGroupRelService();
        if (userGroupTO.getUsers() != null && !userGroupTO.getUsers().isEmpty()) {
            for (UserTO userTO : userGroupTO.getUsers()) {
                boolean isUpdatUser = false;
                User user;
                try {
                    user = userServiceIntf.getUserByAccountId(userTO.getAccountId());
                }
                catch (UserNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
                }
                UserUserGroupCompositeKey userGroupCompositeKey = new UserUserGroupCompositeKey();
                userGroupCompositeKey.setUser(user);
                userGroupCompositeKey.setUserGroup(userGroup);
                UserUserGroupRel isUserGroupRel = null;
                try {
                    isUserGroupRel = userUserGroupRelServiceIntf.getUserUserGroupForId(userGroupCompositeKey);
                }
                catch (UserUserGroupRelNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
                if (isUserGroupRel == null || isUserGroupRel.getBindingStatus().equals(BindingStatus.INACTIVE)) {
                    if (!userTO.getTwoFactorStatus().equals(user.getTwoFactorStatus().name())) {
                        user.setTwoFactorStatus(TwoFactorStatus.valueOf(userGroupTO.getTwoFactorStatus()));
                        isUpdatUser = true;
                    }
                    if (!userTO.getUserStatus().equals(user.getUserStatus().name())) {
                        user.setUserStatus(UserStatus.valueOf(userGroupTO.getUserStatus()));
                        isUpdatUser = true;
                    }
                    if (isUpdatUser) {
                        userServiceIntf.update(session, user);
                    }
                    UserUserGroupRel userGroupRel = new UserUserGroupRel();
                    userGroupRel.setId(userGroupCompositeKey);
                    userUserGroupRelServiceIntf.create(session, userGroupRel);
                }

            }
        }
        if (!userGroup.getTwoFactorStatus().name().equals(userGroupTO.getTwoFactorStatus())) {
            userGroup.setTwoFactorStatus(TwoFactorStatus.valueOf(userGroupTO.getTwoFactorStatus()));
            isUserGroupupdate = true;
        }
        if (!userGroup.getUserStatus().name().equals(userGroupTO.getUserStatus())) {
            userGroup.setUserStatus(UserStatus.valueOf(userGroupTO.getUserStatus()));
            isUserGroupupdate = true;
        }
        if (isUserGroupupdate) {
            List<UserGroupApplicationRel> userGroupApplicationRels = DaoFactory.getUserApplicationServiceRelDao().getUserGroupApplicationRel(userGroup.getId());
            updateApplication(session, userGroupApplicationRels, userGroup);
            List<UserUserGroupRel> userGroupRels = userUserGroupRelServiceIntf.getUserUserGroupRel(userGroup.getId());
            for (UserUserGroupRel userUserGroupRel : userGroupRels) {
                boolean isUpdatdUser = false;
                User user = userUserGroupRel.getId().getUser();
                if (!user.getTwoFactorStatus().name().equals(userGroupTO.getTwoFactorStatus())) {
                    user.setTwoFactorStatus(TwoFactorStatus.valueOf(userGroupTO.getTwoFactorStatus()));
                    isUpdatdUser = true;
                }
                if (!user.getUserStatus().name().equals(userGroupTO.getUserStatus())) {
                    user.setUserStatus(UserStatus.valueOf(userGroupTO.getUserStatus()));
                    isUpdatdUser = true;
                }
                if (isUpdatdUser) {
                    userServiceIntf.update(session, user);
                }
            }
            logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " updateUserGroup : end");
            return userGroupDaoIntf.update(session, userGroup).convertToTO();
        }
        else {
            logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " updateUserGroup : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_DATA_SAME(), errorConstant.getERROR_MESSAGE_USER_GROUP_DATA_SAME());
        }
    }
    
    private void updateApplication(Session session , List<UserGroupApplicationRel> userGroupApplicationRels, UserGroup userGroup) {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " updateApplication : start");
        for(UserGroupApplicationRel userGroupApplicationRel : userGroupApplicationRels) {
            Application application = userGroupApplicationRel.getId().getApplication();
            if(!application.getTwoFactorStatus().equals(userGroup.getTwoFactorStatus())) {
                application.setTwoFactorStatus(userGroup.getTwoFactorStatus());
                DaoFactory.getApplicationDao().update(session, application);
            }
        }
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " updateApplication : end");
    }

    @Override
    public PaginatedTO<UserGroupTO> getUserGroups(int page, int limit, String searchText) {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " getUserGroups : start");
        List<UserGroup> userGroups = userGroupDaoIntf.getPaginatedList(page, limit, searchText);
        Long count = userGroupDaoIntf.getTotalCount(searchText);
        List<UserGroupTO> userGroupTos = new EntityToTOConverter<UserGroup, UserGroupTO>().convertEntityListToTOList(userGroups);
        PaginatedTO<UserGroupTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(userGroupTos);
        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " getUserGroups : end");
        return paginatedTO;
    }

    @Override
    public UserGroupTO addUserGroupMapping(Session session, UserGroupTO userGroupTO) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " addUserGroupMapping : start");
        UserGroup userGroup = null;
        try {
            userGroup = userGroupDaoIntf.getUserGroup(userGroupTO.getGroupname());
        }
        catch (UserGroupNotFoundException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_GROUP_NOT_FOUND());
        }
        UserUserGroupRelServiceIntf userUserGroupRelServiceIntf = ServiceFactory.getUserUserGroupRelService();
        List<UserGroupApplicationRel> userGroupApplicationRels = DaoFactory.getUserApplicationServiceRelDao().getUserGroupApplicationRel(userGroup.getId());
        if (userGroupTO.getUsers() != null && !userGroupTO.getUsers().isEmpty()) {
            
            for (UserTO userTO : userGroupTO.getUsers()) {
                
                User user;
                try {
                    user = userServiceIntf.getUserByAccountId(userTO.getAccountId());
                    logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " addUserGroupMapping1: start"+user.getAccountId());
                }
                catch (UserNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
                }
                UserUserGroupCompositeKey userGroupCompositeKey = new UserUserGroupCompositeKey();
                userGroupCompositeKey.setUser(user);
                userGroupCompositeKey.setUserGroup(userGroup);
                logger.log(Level.DEBUG, "user Id : "+user.getId());
                logger.log(Level.DEBUG, "userGroup Id "+userGroup.getId());
                UserUserGroupRel userGroupRel = null;
                try {
                    userGroupRel = userUserGroupRelServiceIntf.getUserUserGroupForId(userGroupCompositeKey);
                }
                catch (UserUserGroupRelNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
//                logger.log(Level.DEBUG, "userGroupRel : "+userGroupRel.getBindingStatus());
                if (Constant.ADD.equals(userTO.getStatus())) {
                    if (userGroupRel != null) {
                        if (userGroupRel.getBindingStatus().equals(BindingStatus.ACTIVE)) {
                            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_BINDING_ALREADY_EXISTS(), errorConstant.getHUMANIZED_USER_GROUP_BINDING_ALREADY_EXISTS());
                        }
                        else {
                            userGroupRel.setBindingStatus(BindingStatus.ACTIVE);
                            userUserGroupRelServiceIntf.update(session, userGroupRel);
                        }
                    }
                    else {
                        userGroupRel = new UserUserGroupRel();
                        userGroupRel.setId(userGroupCompositeKey);
                        userGroupRel.setBindingStatus(BindingStatus.ACTIVE);
                        userUserGroupRelServiceIntf.create(session, userGroupRel);
                    }
                    /*boolean isUpdatdUser = false;
                    if (!user.getTwoFactorStatus().equals(userGroup.getTwoFactorStatus())) {
                        user.setTwoFactorStatus(userGroup.getTwoFactorStatus());
                        isUpdatdUser = true;
                    }
                    if (!user.getUserStatus().equals(userGroup.getUserStatus())) {
                        user.setUserStatus(userGroup.getUserStatus());
                        isUpdatdUser = true;
                    }
                    if (isUpdatdUser) {
                        userServiceIntf.update(session, user);
                    }*/

                }
                else if(Constant.DELETE.equals(userTO.getStatus())){
                    logger.log(Level.DEBUG, "mapping status DELETE");
                    if (userGroupRel == null) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_BINDING_NOT_FOUND(), errorConstant.getHUMANIZED_USER_GROUP_BINDING_NOT_FOUND());
                    }
                    logger.log(Level.DEBUG, "userGroupRel not null");
                    userGroupRel.setBindingStatus(BindingStatus.INACTIVE);
                    userUserGroupRelServiceIntf.update(session, userGroupRel);
                    logger.log(Level.DEBUG, "userGroupRel updated");
                }
                logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " addUserGroupMapping2 : start"+user.getAccountId());
                //bindUserToGroupApplications(userGroupApplicationRels, user, userGroup, session, userTO.getStatus());
            }

        }
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " addUserGroupMapping : end");
        return null;
    }

    private void bindUserToGroupApplications(List<UserGroupApplicationRel> userGroupApplicationRels, User user, UserGroup userGroup, Session session, String status) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " bindUserToGroupApplications : start " );
        UserApplicationRelDaoIntf userApplicationRelDaoIntf = DaoFactory.getUserApplicationRel();
        Set<String> userApplicationBinded = new HashSet<>();
        for (UserGroupApplicationRel userGroupApplicationRel : userGroupApplicationRels) {
            logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " bindUserToGroupApplications : starting "+user.getAccountId() +" Name "+userGroupApplicationRel.getId().getApplication().getApplicationName());
            Application application = userGroupApplicationRel.getId().getApplication();
            Service service = userGroupApplicationRel.getId().getService();
            UserApplicationServiceCompositeKey userApplicationServiceCompositeKey = new UserApplicationServiceCompositeKey();
            userApplicationServiceCompositeKey.setApplication(application);
            userApplicationServiceCompositeKey.setService(service);
            userApplicationServiceCompositeKey.setUser(user);
            UserApplicationServiceRel userApplicationServiceRel = null;
            try {
                IAMExtensionV2 iamExtension = IAMUtil.getInstance().getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
                Token token = IAMUtil.getInstance().authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
                if (!userApplicationRelDaoIntf.isApplicationUserBindingPresent(application.getId(), user.getId())) {
                    boolean isADUserBindingDone = iamExtension.bindUserToApplication(user.getAccountId(), token);
                    if (!isADUserBindingDone) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_FAILED(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_FAILED());
                    }
                }
                if(Constant.DELETE.equals(status)) {
                    if (userApplicationRelDaoIntf.isApplicationUserBindingPresent(application.getId(), user.getId()) && !userApplicationBinded.contains(application.getId()+" "+user.getId())) {
                        
                        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " bindUserToGroupApplications : start"+user.getAccountId());
                        iamExtension.unbindConsumerApplication(token, user.getAccountId());
                        userApplicationBinded.add(application.getId()+" "+user.getId());// added this since unbind consumer was being called for the number of service of the application present in usergroup_application_rel
                        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " bindUserToGroupApplications : completed"+user.getAccountId());
                    }
                }
            }
            catch (IAMException e) {
                session.getTransaction().rollback();
                logger.log(Level.ERROR, e);
                throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
            }
            try {
                userApplicationServiceRel = userApplicationRelDaoIntf.getUserApplicationForId(userApplicationServiceCompositeKey);
            }
            catch (UserApplicationRelNotFoundException e) {
                logger.log(Level.FATAL, e.getMessage(), e);
            }

            if (userApplicationServiceRel != null) {
                BindingStatus bindingStatus = Constant.ADD.equals(status)?BindingStatus.ACTIVE:BindingStatus.INACTIVE;
                userApplicationServiceRel.setBindingStatus(bindingStatus);
                userApplicationServiceRel.setTwoFactorStatus(userGroup.getTwoFactorStatus());
                userApplicationRelDaoIntf.update(session, userApplicationServiceRel);
            }
            else {
                userApplicationServiceRel = new UserApplicationServiceRel();
                userApplicationServiceRel.setBindingStatus(BindingStatus.ACTIVE);
                userApplicationServiceRel.setTwoFactorStatus(userGroup.getTwoFactorStatus());
                userApplicationServiceRel.setId(userApplicationServiceCompositeKey);
                userApplicationRelDaoIntf.create(session, userApplicationServiceRel);
            }
        }
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " bindUserToGroupApplications : end");
    }

    @Override
    public UserGroupTO removeUserGroup(Session session, UserGroupTO userGroupTO) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " removeUserGroup : start");
        UserGroup userGroup = null;
        try {
            userGroup = userGroupDaoIntf.getUserGroup(userGroupTO.getGroupname());
        }
        catch (UserGroupNotFoundException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_GROUP_NOT_FOUND());
        }
        userGroup.setUserGroupStatus(UserGroupStatus.INACTIVE);
        List<UserUserGroupRel> userUserGroupRelList = DaoFactory.getUserUserGroupRelDao().getUserUserGroupRel(userGroup.getId());
        List<UserGroupApplicationRel> userGroupApplicationRelList = DaoFactory.getUserApplicationServiceRelDao().getUserGroupApplicationRel(userGroup.getId());
        removeUserGroupMapping(session, userGroupApplicationRelList, userUserGroupRelList);
        userGroupDaoIntf.update(session, userGroup);
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " removeUserGroup : end");
        return null;
    }

    private void removeUserGroupMapping(Session session, List<UserGroupApplicationRel> userGroupApplicationRelList, List<UserUserGroupRel> userUserGroupRelList) {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " removeUserApplicationMapping : start");
        UserGroupApplicationRelDaoIntf userGroupApplicationRelDaoIntf = DaoFactory.getUserApplicationServiceRelDao();
        UserUserGroupRelDaoInf userUserGroupRelDaoInf = DaoFactory.getUserUserGroupRelDao();
        if (!userGroupApplicationRelList.isEmpty()) {
            for (UserGroupApplicationRel userGroupApplicationRel : userGroupApplicationRelList) {
                userGroupApplicationRel.setBindingStatus(BindingStatus.INACTIVE);
                if (!userUserGroupRelList.isEmpty()) {
                    for (UserUserGroupRel userUserGroupRel : userUserGroupRelList) {
                        userUserGroupRel.setBindingStatus(BindingStatus.INACTIVE);
                        unbindUserApplication(session, userGroupApplicationRel.getId().getApplication(), userGroupApplicationRel.getId().getService(), userUserGroupRel.getId().getUser());
                        userUserGroupRelDaoInf.update(session, userUserGroupRel);
                    }
                }
                userGroupApplicationRelDaoIntf.update(session, userGroupApplicationRel);
            }
        }
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " removeUserGroup : end");
    }

    @Override
    public void unbindUserApplication(Session session, Application application, Service srvice, User user) {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " unbindUserApplication : start");
        UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
        userApplicationCompositeKey.setApplication(application);
        userApplicationCompositeKey.setService(srvice);
        userApplicationCompositeKey.setUser(user);
        UserApplicationRelDaoIntf userApplicationRelDaoIntf = DaoFactory.getUserApplicationRel();
        UserApplicationServiceRel userApplicationRel = null;
        try {
            userApplicationRel = userApplicationRelDaoIntf.getUserApplicationForId(userApplicationCompositeKey);
        }
        catch (UserApplicationRelNotFoundException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        if (userApplicationRel != null) {
            userApplicationRel.setBindingStatus(BindingStatus.INACTIVE);
            boolean isSingleBindingPresent = userApplicationRelDaoIntf.getUserAndApplicationRelCount(application.getId(), user.getId()) == 1;
            boolean isConsumerUnbind = true;
            if (isSingleBindingPresent) {
                try {
                    IAMExtensionV2 iamExtension = IAMUtil.getInstance().getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
                    Token token = IAMUtil.getInstance().authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
                    isConsumerUnbind = iamExtension.unbindConsumerApplication(token, user.getAccountId());
                }
                catch (IAMException e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                }
            }
            if (isConsumerUnbind) {
                userApplicationRelDaoIntf.update(session, userApplicationRel);
            }
        }
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " unbindUserApplication : end");
    }

    @Override
    public PaginatedTO<UserGroupTO> getGroupsForUser(int page, int limit, String searchText, Long userId) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " getGroupsForUser : start");
        List<UserUserGroupRel> userGroupRels = DaoFactory.getUserUserGroupRelDao().getUserUserGroupRelForUser(page, limit, userId);
        logger.log(Level.DEBUG,"*** userGroupRels : "+userGroupRels.size());
        List<UserGroup> userGroups = getUserGroups(userGroupRels);
        Long count = DaoFactory.getUserUserGroupRelDao().getUserUserGroupRelForUserCount(userId);
        List<UserGroupTO> userGroupTos = new EntityToTOConverter<UserGroup, UserGroupTO>().convertEntityListToTOList(userGroups);
        PaginatedTO<UserGroupTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(userGroupTos);
        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " getGroupsForUser : end");
        return paginatedTO;
    }

    private List<UserGroup> getUserGroups(List<UserUserGroupRel> userGroupRels) {
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " getUserGroups : start");
        List<UserGroup> userGroups = new ArrayList<>();
        for(UserUserGroupRel userUserGroupRel : userGroupRels) {
            logger.log(Level.DEBUG,"**** User Group : "+new Gson().toJson(userUserGroupRel.getId().getUserGroup()));
            userGroups.add(userUserGroupRel.getId().getUserGroup());
        }
        logger.log(Level.DEBUG, USER_GROUP_SERVICE_LOG + " getUserGroups : end");
        return userGroups;
    }
}
