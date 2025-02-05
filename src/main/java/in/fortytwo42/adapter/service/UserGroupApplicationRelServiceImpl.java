
package in.fortytwo42.adapter.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserGroupApplicationRelTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.daos.dao.ApplicationDaoImpl;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserGroupApplicationRelDaoIntf;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.exception.UserApplicationRelNotFoundException;
import in.fortytwo42.daos.exception.UserGroupApplicationRelNotFoundException;
import in.fortytwo42.daos.exception.UserGroupNotFoundException;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceCompositeKey;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.bean.UserGroup;
import in.fortytwo42.entities.bean.UserGroupApplicationCompositeKey;
import in.fortytwo42.entities.bean.UserGroupApplicationRel;
import in.fortytwo42.entities.bean.UserUserGroupRel;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserGroupTO;

public class UserGroupApplicationRelServiceImpl implements UserGroupApplicationRelServiceIntf {

    private static final String USER_GROUP_APPLICATION_REL_LOG = "<<<<< UserGroupApplicationRelServiceImpl";

    private static Logger logger= LogManager.getLogger(UserGroupApplicationRelServiceImpl.class);

    private UserGroupApplicationRelDaoIntf userGroupApplicationRelDaoIntf = DaoFactory.getUserApplicationServiceRelDao();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private final ExecutorService pool;
    
    private UserGroupApplicationRelServiceImpl() {
        super();
        int poolSize = 1;
        pool = Executors.newFixedThreadPool(poolSize);
    }

    private static final class InstanceHolder {
        private static UserGroupApplicationRelServiceImpl INSTANCE = new UserGroupApplicationRelServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static UserGroupApplicationRelServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public UserGroupTO addUserGroupApplicationMapping(Session session, UserGroupTO userGroupTO) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : start : "+System.currentTimeMillis());
        UserGroup userGroup = null;
        try {
            userGroup = DaoFactory.getUserGroupDao().getUserGroup(userGroupTO.getGroupname());
        }
        catch (UserGroupNotFoundException e) {
            logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : end : "+System.currentTimeMillis());
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_GROUP_NOT_FOUND());
        }
        for (ApplicationTO applicationTO : userGroupTO.getApplications()) {
            Application application = null;
            try {
                application = ApplicationDaoImpl.getInstance().getApplicationByApplicationId(applicationTO.getApplicationId());
            }
            catch (ApplicationNotFoundException e) {
                logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : end : "+System.currentTimeMillis());
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
            }
            if (applicationTO.getServices() == null && application.getServices() != null && !application.getServices().isEmpty()) {
                applicationTO.setServices(new EntityToTOConverter<Service, ServiceTO>().convertEntityListToTOList(application.getServices()));
            }
            for(ServiceTO serviceTO : applicationTO.getServices()) {
                Service service = null;
                try {
                    service = DaoFactory.getServiceDao().getServiceByServiceName(serviceTO.getServiceName());
                }
                catch (ServiceNotFoundException e) {
                    logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : end : "+System.currentTimeMillis());
                    throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
                }
                UserGroupApplicationCompositeKey userGroupApplicationCompositeKey = new UserGroupApplicationCompositeKey();
                userGroupApplicationCompositeKey.setUsergroup(userGroup);
                userGroupApplicationCompositeKey.setApplication(application);
                userGroupApplicationCompositeKey.setService(service);
                UserGroupApplicationRel userGroupApplicationRel = null;
                try {
                    userGroupApplicationRel = userGroupApplicationRelDaoIntf.getUserGroupApplicationForId(userGroupApplicationCompositeKey);
                }
                catch (UserGroupApplicationRelNotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage());
                }
                if (Constant.ADD.equals(applicationTO.getStatus())) {
                    if (userGroupApplicationRel != null) {
                        if (userGroupApplicationRel.getBindingStatus().equals(BindingStatus.ACTIVE)) {
                            logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : end : "+System.currentTimeMillis());
                            throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS(),
                                    errorConstant.getHUMANIZED_USER_GROUP_APPLICATION_BINDING_ALREADY_EXISTS());
                        }
                        else {
                            userGroupApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
                            userGroupApplicationRelDaoIntf.update(session, userGroupApplicationRel);
                        }
                    }
                    else {
                        userGroupApplicationRel = new UserGroupApplicationRel();
                        userGroupApplicationRel.setId(userGroupApplicationCompositeKey);
                        userGroupApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
                        userGroupApplicationRelDaoIntf.create(session, userGroupApplicationRel);
                    }
                }
                else {
                    if (userGroupApplicationRel == null) {
                        logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : end : "+System.currentTimeMillis());
                        throw new AuthException(null, errorConstant.getERROR_CODE_USER_GROUP_APPLICATION_BINDING_NOT_FOUND(),
                                errorConstant.getHUMANIZED_USER_GROUP_APPLICATION_BINDING_NOT_FOUND());
                    }
                    else {
                        userGroupApplicationRel.setBindingStatus(BindingStatus.INACTIVE);
                        userGroupApplicationRelDaoIntf.update(session, userGroupApplicationRel);
                    }
                }
                bindUserToApplicationService(userGroup, application, service, session);
                if(!userGroup.getTwoFactorStatus().equals(application.getTwoFactorStatus())) {
                    application.setTwoFactorStatus(userGroup.getTwoFactorStatus());
                    DaoFactory.getApplicationDao().update(session, application);
                }
            }
        }
        logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : end : "+System.currentTimeMillis());
        return userGroupTO;

    }

    private void bindUserToApplicationService(UserGroup userGroup, Application application, Service service, Session session) throws AuthException {
        pool.submit(()->{
            logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " bindUserToApplicationService : start");
            List<UserUserGroupRel> userUserGroupRels = DaoFactory.getUserUserGroupRelDao().getUserUserGroupRel(userGroup.getId());
            for (UserUserGroupRel userUserGroupRel : userUserGroupRels) {
                User user = userUserGroupRel.getId().getAdUser();
                UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
                userApplicationCompositeKey.setUser(user);
                userApplicationCompositeKey.setApplication(application);
                userApplicationCompositeKey.setService(service);
                UserApplicationServiceRel userApplicationRel = null;
                try {
                    userApplicationRel = DaoFactory.getUserApplicationRel().getUserApplicationForId(userApplicationCompositeKey);
                }
                catch (UserApplicationRelNotFoundException e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                }
                if (userApplicationRel != null) {
                    userApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
                    userApplicationRel.setTwoFactorStatus(userGroup.getTwoFactorStatus());
                    DaoFactory.getUserApplicationRel().update(session, userApplicationRel);
                    try {
                        addBindingToCloud(session, application, user);
                    }
                    catch (AuthException e) {
                        logger.log(Level.FATAL, e.getMessage(), e);   
                    }
                }
                else {
                    userApplicationRel = new UserApplicationServiceRel();
                    userApplicationRel.setId(userApplicationCompositeKey);
                    userApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
                    userApplicationRel.setTwoFactorStatus(userGroup.getTwoFactorStatus());
                    DaoFactory.getUserApplicationRel().create(session, userApplicationRel);
                    try {
                        addBindingToCloud(session, application, user);
                    }
                    catch (AuthException e) {
                        logger.log(Level.FATAL, e.getMessage(), e);   
                    }
                }
                
            }
            logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " bindUserToApplicationService : end");
        });
        
    }

    
    private void addBindingToCloud(Session session, Application application, User user) throws AuthException {
        logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " bindUserToApplicationService : start");
        try {
            IAMExtensionV2 iamExtension = IAMUtil.getInstance().getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = IAMUtil.getInstance().authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            if (!DaoFactory.getUserApplicationRel().isApplicationUserBindingPresent(application.getId(), user.getId())) {
                boolean isADUserBindingDone = iamExtension.bindUserToApplication(user.getAccountId(), token);
                if (!isADUserBindingDone) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_FAILED(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_FAILED());
                }
            }
        }
        catch (IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e);
            logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " bindUserToApplicationService : end");
            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        } finally {
            logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " bindUserToApplicationService : end");
        }
    }
    @Override
    public PaginatedTO<UserGroupApplicationRelTO> getUserGroupApplicationMapping(long userGoupId, int pageNo, int limit, String searchText) {
        logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : start");
        List<Object[]> userGroupRels = userGroupApplicationRelDaoIntf.getUserGroupApplicationRel(userGoupId, searchText, pageNo, limit);
        List<UserGroupApplicationRelTO> userUserGrouprelTos = new ArrayList<>();
        for (Iterator<Object[]> iterator = userGroupRels.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            UserGroupApplicationRelTO userGroupApplicationRelTO = new UserGroupApplicationRelTO();
            userGroupApplicationRelTO.setEnterpriseId(String.valueOf(row[4]));
            userGroupApplicationRelTO.setTwoFactorStatus(String.valueOf(row[3]));
            userGroupApplicationRelTO.setApplicationName(String.valueOf(row[2]));
            userGroupApplicationRelTO.setApplicationId(String.valueOf(row[1]));
            long id = Long.parseLong(String.valueOf(row[0]));
            userGroupApplicationRelTO.setId(id);

            userUserGrouprelTos.add(userGroupApplicationRelTO);
        }
        long count = userGroupApplicationRelDaoIntf.getUserGroupApplicationRelCount(userGoupId, searchText);
        PaginatedTO<UserGroupApplicationRelTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(userUserGrouprelTos);
        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " addUserGroupApplicationMapping : end");
        return paginatedTO;
    }
    
    @Override
    public String getApplicationIds(Long userGroupId) {
        logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " getApplicationIds : start");
        StringBuilder applicationIds = new StringBuilder();
        String applicationids = "";
        List<Object[]> userGroupRels = userGroupApplicationRelDaoIntf.getUserGroupApplications(userGroupId);
        for (Iterator<Object[]> iterator = userGroupRels.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            applicationIds.append(row[0]).append(Constant._COMMA);
        }
        if (applicationIds.length() > 0) {
            applicationids =  applicationIds.subSequence(0, applicationIds.length() - 1).toString();
        }
        logger.log(Level.DEBUG, USER_GROUP_APPLICATION_REL_LOG + " getApplicationIds : end");
        return applicationids;
    }
}
