
package in.fortytwo42.adapter.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import dev.morphia.transactions.MorphiaSession;
import in.fortytwo42.adapter.jar.MongoConnectionManagerIam;
import in.fortytwo42.adapter.jar.entities.FcmNotificationDetails;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.cam.dto.Client;
import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.SRAApplicationSettingTO;
import in.fortytwo42.adapter.transferobj.StagingSRAProviderSettingTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.daos.dao.ApplicationDaoIntf;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.EnterpriseDaoIntf;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.dao.SRAApplicationGatewayRelDaoIntf;
import in.fortytwo42.daos.dao.SRAApplicationSettingDaoIntf;
import in.fortytwo42.daos.dao.SRAGatewaySettingDaoIntf;
import in.fortytwo42.daos.dao.ServiceDaoIntf;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.enums.SRAApplicationType;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.GatewaySettingsTO;
import in.fortytwo42.enterprise.extension.tos.RemoteAccessSettingsTO;
import in.fortytwo42.enterprise.extension.utils.RandomString;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.CallbackUrl;
import in.fortytwo42.entities.bean.Enterprise;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.bean.SRAApplicationGatewayRel;
import in.fortytwo42.entities.bean.SRAApplicationSetting;
import in.fortytwo42.entities.bean.SRAGatewaySetting;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.enums.ApplicationType;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.IAMStatus;
import in.fortytwo42.entities.enums.OnboardStatus;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.enums.ResetPinUserUnblockStatus;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.enums.Algorithm;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.RemoteAccessSettingTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;


public class ApplicationServiceImpl implements ApplicationServiceIntf {

    private static Logger logger= LogManager.getLogger(ApplicationServiceImpl.class);
    private UserDaoIntf userDao = DaoFactory.getUserDao();
    private ApplicationDaoIntf applicationDao = DaoFactory.getApplicationDao();
    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    private EnterpriseDaoIntf enterpriseDao = DaoFactory.getEnterpriseDao();
    private ServiceDaoIntf serviceDao = DaoFactory.getServiceDao();
    private SRAApplicationSettingDaoIntf sraApplicationSettingDao = DaoFactory.getSRAApplicationSetting();
    private SRAGatewaySettingDaoIntf sraGatewaySettingDao = DaoFactory.getSRAGatewaySettingDao();
    private UserApplicationRelDaoIntf userApplicationRelDaoIntf = DaoFactory.getUserApplicationRel();
    private SRAApplicationGatewayRelDaoIntf sraApplicationGatewayRelDaoIntf = DaoFactory.getSRAApplicationGatewayRelDoa();
    private AttributeStoreDaoIntf attributeStoreDaoIntf = DaoFactory.getAttributeStoreDao();
    private RequestDaoIntf requestDao = DaoFactory.getRequestDao();

    //TODO: Service to service
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private PermissionServiceIntf permissionService = ServiceFactory.getPermissionService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private Config config = Config.getInstance();
    private IAMUtil iamUtil = IAMUtil.getInstance();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();
    MongoConnectionManagerIam iamMongoConnectionManager = MongoConnectionManagerIam.getInstance();

    private ApplicationServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final ApplicationServiceImpl INSTANCE = new ApplicationServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static ApplicationServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private boolean isESCFileUploaded(String filename) {
        String escFolderPath = config.getProperty(Constant.UPLOAD_ESC_FOLDER_PATH) + "Fortytwo42" + File.separator + filename + ".iamcix";
        File file = new File(escFolderPath);
        return file.exists();
    }

    private void copyESCFile(String fromLocation, String toLocation) throws AuthException {
        try {
            Files.copy(Paths.get(fromLocation), Paths.get(toLocation), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_IO_EXCEPTION(), errorConstant.getERROR_MESSAGE_IO_EXCEPTION());
        }
    }

    private void deleteESCFile(String fromLocation) throws AuthException {
        try {
            Files.delete(Paths.get(fromLocation));
        }
        catch (IOException e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_IO_EXCEPTION(), errorConstant.getERROR_MESSAGE_IO_EXCEPTION());
        }
    }

    public void saveCryptoFile(String path, InputStream cryptoFile) throws AuthException {
        try {
            OutputStream out = null;
            int read = 0;
            byte[] bytes = new byte[1024];
            out = new FileOutputStream(new File(path));
            while ((read = cryptoFile.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        }
        catch (IOException e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_IO_EXCEPTION(), errorConstant.getERROR_MESSAGE_IO_EXCEPTION());
        }
    }

    private void inactivateUserBinding(Session session, Application application, List<Service> servicesToRemove) throws AuthException {
        List<UserApplicationServiceRel> userApplicationRels;
        IAMExtensionV2 iamExtension;
        try {
            iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            Set<User> users = new HashSet<>();
            for (Service service : servicesToRemove) {
                userApplicationRels = userApplicationRelDaoIntf.getApplicationRelsForServices(service.getId(), application.getId());
                if (!userApplicationRels.isEmpty()) {
                    for (UserApplicationServiceRel userApplicationRel : userApplicationRels) {
                        User user = userApplicationRel.getId().getUser();
                        iamExtension.forceTimeoutApprovalAttemptsByLookupId(token, getUsername(user.getId()) + "|" + getMobile(user.getId()), service.getServiceName());
                        boolean isSingleMobileBindingPresent = userApplicationRelDaoIntf.getUserAndApplicationRelCount(application.getId(), user.getId()) == 1;
                        if (isSingleMobileBindingPresent) {
                            logger.log(Level.DEBUG, "User  : "+user.getId());
                            iamExtension.unbindConsumerApplication(token, user.getAccountId());
                        }
                        userApplicationRel.setBindingStatus(BindingStatus.INACTIVE);
                        userApplicationRelDaoIntf.update(session, userApplicationRel);
                        users.add(user);
                    }
                }
            }
            if (!users.isEmpty()) {
                for (User user : users) {
                    List<UserApplicationServiceRel> activeUserApplicationRels = userApplicationRelDaoIntf.getApplicationRelsForUser(user.getId());
                    if (activeUserApplicationRels == null || activeUserApplicationRels.isEmpty()) {
                        user.setIamStatus(IAMStatus.DISABLED);
                        userDao.update(session, user);
                    }
                }
            }
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public PaginatedTO<ApplicationTO> getApplications(String applicationUpdateStatus, int page, String searchText, String _2faStatusFilter, String applicationType, String role) throws AuthException {
        List<ApplicationTO> applicationList = new ArrayList<>();
        Long count;
        if (!permissionService.isPermissionValidForRole(PermissionUtil.VIEW_APPLICATIONS, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        List<Application> applications = applicationDao.getPaginatedList(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText, _2faStatusFilter,
                applicationType);
        for (Iterator<Application> iterator = applications.iterator(); iterator.hasNext();) {
            Application application = (Application) iterator.next();
            ApplicationTO applicationTO = application.convertToTO();
            if (application.getUnblockSettings() == ResetPinUserUnblockStatus.APPLICATION_SELF_UNBLOCK) {
                try {
                    Application application2 = applicationDao.getApplicationWithCallbackUrl(application.getApplicationId());
                    for (CallbackUrl callbackUrl : application2.getCallbackUrls()) {
                        if (Constant.UPDATE_USER_URL.equals(callbackUrl.getCallbackType())) {
                            applicationTO.setCallbackUrl(callbackUrl.getUrl());
                        }
                    }
                }
                catch (ApplicationNotFoundException e) {
                }
            }
            applicationList.add(applicationTO);
        }
        count = applicationDao.getTotalActiveCount(searchText, _2faStatusFilter, applicationType);

        PaginatedTO<ApplicationTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(applicationList);
        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    @Override
    public PaginatedTO<ApplicationTO> getApplicationAuditTrails(int page, String searchText, Long fromDate, Long toDate, String role) throws AuthException {
        //        PermissionUtil.getInstance().validateGetApplicationAuditTrailPermission(role);
        //        ApplicationStagingDaoIntf applicationStagingDaoIntf = DaoFactory.getApplicationStagingDao();
        //        List<ApplicationStaging> applications = applicationStagingDaoIntf.getApplicationAuditTrail(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText, fromDate,
        //                toDate);
        //        List<ApplicationTO> applicationList = new EntityToTOConverter<ApplicationStaging, ApplicationTO>().convertEntityListToTOList(applications);
        //        Long count = applicationStagingDaoIntf.getTotalCountAuditTrail(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText, fromDate, toDate);
        PaginatedTO<ApplicationTO> paginatedTO = new PaginatedTO<ApplicationTO>();
        //        paginatedTO.setList(applicationList);
        //        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    @Override
    public String generateApplicationSecret() {
        return RandomString.nextString(8);
    }

    @Override
    public void uploadFile(String fileName, InputStream cryptoFile) throws AuthException {
        String escFolderPath = config.getProperty(Constant.UPLOAD_ESC_FOLDER_PATH) + "Fortytwo42";
        File file = new File(escFolderPath);
        if (!file.exists()) {
            file.mkdir();
        }
        String filePath = escFolderPath + File.separator + fileName;
        saveCryptoFile(filePath, cryptoFile);
    }

    @Override
    public ApplicationTO authenticateApplication(String applicationId, String applicationSecret) throws AuthException {
        try {
            Application application = applicationDao.getApplicationByApplicationId(applicationId);
            try {
                if (application.getAuthenticationRequired() && !isApplicationSecretValid(application.getApplicationSecret(), applicationSecret)) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_ID_OR_PASSWORD(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_ID_OR_PASSWORD());
                }
            }
            catch (Exception e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_ID_OR_PASSWORD(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_ID_OR_PASSWORD());
            }
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
            iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            ApplicationTO applicationTO = new ApplicationTO();
            applicationTO.setApplicationId(applicationId);
            applicationTO.setStatus(Constant.SUCCESS_STATUS);
            return applicationTO;
        }
        catch (ApplicationNotFoundException e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    private boolean isApplicationSecretValid(String applicationSecret, String receivedApplicationSecret) {
        String decryptedSecret = AES128Impl.decryptData(applicationSecret, KeyManagementUtil.getAESKey());
        if (receivedApplicationSecret.equals(decryptedSecret)) {
            return true;
        }
        int saltSize;
        try {
            saltSize = Integer.parseInt(config.getProperty(Constant.SALT_SIZE));
        }
        catch (NumberFormatException e) {
            saltSize = 20;
        }
        if (receivedApplicationSecret.length() > saltSize) {
            String salt = receivedApplicationSecret.substring(0, saltSize);
            String hashedSecret = receivedApplicationSecret.substring(saltSize);
            String generatedHash = SHAImpl.hashData256(decryptedSecret + salt);
            return hashedSecret.equals(generatedHash);
        }
        return false;
    }

    @Override
    public ApplicationTO deleteApplication(Session session, ApplicationTO applicationTo, String role, String actor) throws AuthException {
        if (!permissionService.isPermissionValidForRole(PermissionUtil.DELETE_APPLICATION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }

        Application application;
        try {
            application = applicationDao.getApplicationByApplicationId(applicationTo.getApplicationId());
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        if (!application.getApplicationName().equals(applicationTo.getApplicationName())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
        }

        //        ApplicationStagingDaoIntf applicationStagingDaoIntf = DaoFactory.getApplicationStagingDao();
        //        ApplicationStaging applicationStaging = new ApplicationStaging();
        //        applicationStaging.setApplicationId(applicationTo.getApplicationId());
        //        applicationStaging.setActive(true);
        //        applicationStaging.setActor(actor);
        //        applicationStaging.setApplicationName(applicationTo.getApplicationName());
        //        applicationStaging.setApplicationSecret(applicationTo.getApplicationSecret());
        //        applicationStaging.setApplicationType(ApplicationType.valueOf(applicationTo.getApplicationType()));
        //        applicationStaging.setAuthenticationRequired(applicationTo.getAuthenticationRequired());
        //        applicationStaging.setMakerComments(applicationTo.getComments());
        //        applicationStaging.setDateTimeCreated(new Timestamp(System.currentTimeMillis()));
        //        applicationStaging.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
        //        applicationStaging.setDescription(applicationTo.getDescription());
        //        applicationStaging.setEnterpriseId(applicationTo.getEnterpriseId());
        //        applicationStaging.setPassword(applicationTo.getPassword());
        //
        //        applicationStaging.setActionType(Constant.DELETE);
        //        ServiceDaoIntf serviceDaoIntf = DaoFactory.getServiceDao();
        //        List<Service> services = new ArrayList<>();
        //        /*
        //         * for (Service service : application.getServices()) { Service service = null;
        //         * try { service =
        //         * serviceDaoIntf.getServiceByServiceName(service.getServiceName()); } catch
        //         * (ServiceNotFoundException e) { throw new AuthException(null,
        //         * errorConstant.getERROR_CODE_SERVICE_NOT_FOUND,
        //         * errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND); }
        //         * services.add(application.getServices()); }
        //         */
        //        services.addAll(application.getServices());
        //
        //        applicationStaging.setServices(services);
        //        applicationStaging.setTransactionTimeout(applicationTo.getTransactionTimeout());
        //        applicationStaging.setTwoFactorStatus(TwoFactorStatus.valueOf(applicationTo.getTwoFactorStatus()));
        //        applicationStaging.setVersion(0);
        //
        //        applicationStagingDaoIntf.create(applicationStaging);

        //        ApplicationTO applicationResponseTo = applicationStaging.convertToTO();
        //
        //        return applicationResponseTo;
        return null;
    }

    @Override
    public ApplicationTO deleteApplication(Session session, ApplicationTO applicationTo, Application application, String actor) throws AuthException {
        //        ApplicationStagingDaoIntf applicationStagingDaoIntf = DaoFactory.getApplicationStagingDao();
        //        ApplicationStaging applicationStaging = new ApplicationStaging();
        //        applicationStaging.setApplicationId(applicationTo.getApplicationId());
        //        applicationStaging.setActive(true);
        //        applicationStaging.setActor(actor);
        //        applicationStaging.setApplicationName(applicationTo.getApplicationName());
        //        applicationStaging.setApplicationSecret(applicationTo.getApplicationSecret());
        //        applicationStaging.setApplicationType(ApplicationType.valueOf(applicationTo.getApplicationType()));
        //        applicationStaging.setAuthenticationRequired(applicationTo.getAuthenticationRequired());
        //        applicationStaging.setMakerComments(applicationTo.getComments());
        //        applicationStaging.setDateTimeCreated(new Timestamp(System.currentTimeMillis()));
        //        applicationStaging.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
        //        applicationStaging.setDescription(applicationTo.getDescription());
        //        applicationStaging.setEnterpriseId(applicationTo.getEnterpriseId());
        //        applicationStaging.setPassword(applicationTo.getPassword());
        //        applicationStaging.setActionType(Constant.DELETE);
        //        List<Service> services = new ArrayList<>();
        //        services.addAll(application.getServices());
        //        applicationStaging.setServices(services);
        //        applicationStaging.setTransactionTimeout(applicationTo.getTransactionTimeout());
        //        applicationStaging.setTwoFactorStatus(TwoFactorStatus.valueOf(applicationTo.getTwoFactorStatus()));
        //        applicationStaging.setVersion(0);
        //        applicationStagingDaoIntf.create(applicationStaging);
        //        return applicationStaging.convertToTO();
        return null;

    }

    private void validatePermission(String actionType, String role) throws AuthException {
        switch (actionType) {
            case Constant.INSERT:
                if (!permissionService.isPermissionValidForRole(PermissionUtil.CREATE_APPLICATION_LABEL, role)) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
                }
                break;
            case Constant.UPDATE:
                if (!permissionService.isPermissionValidForRole(PermissionUtil.EDIT_APPLICATION_LABEL, role)) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
                }
                break;
            case Constant.DELETE:
                if (!permissionService.isPermissionValidForRole(PermissionUtil.DELETE_APPLICATION_LABEL, role)) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public PaginatedTO<UserApplicationRelTO> getUserApplicationRels(String applicationId, String status, String searchQuery, int page, String role, String actor)
            throws AuthException {
        Long count = null;
        List<UserApplicationRelTO> userApplicationRelTOs = null;
        if (!permissionService.isPermissionValidForRole(PermissionUtil.GET_USER_APPLICATION_REL, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        Application application;
        try {
            application = applicationDao.getApplicationByApplicationId(applicationId);
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        List<Object[]> userApplicationRels = userApplicationRelDaoIntf.getUserApplicationRels(application.getId(), searchQuery, page,
                Integer.parseInt(config.getProperty(Constant.LIMIT)));

        userApplicationRelTOs = new ArrayList<UserApplicationRelTO>();
        for (Iterator<Object[]> iterator = userApplicationRels.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            long userId = Long.parseLong(String.valueOf(row[2]));
            User user;
            try {
                user = userDao.getById(userId);
            }
            catch (NotFoundException e) {
                continue;
            }

            UserApplicationRelTO userApplicationRel = new UserApplicationRelTO();
            userApplicationRel.setApplicationId(applicationId);
            userApplicationRel.setFullname(getFullName(userId));
            userApplicationRel.setMobile(getMobile(userId));
            userApplicationRel.setUsername(getUsername(userId));
            userApplicationRel.setUserId(userId);
            userApplicationRel.setTwoFactorStatus(String.valueOf(row[1]));
            userApplicationRel.setBindingStatus(String.valueOf(row[0]));
            userApplicationRelTOs.add(userApplicationRel);
        }
        count = userApplicationRelDaoIntf.getTotalUserApplicationRelCount(application.getId(), searchQuery);
        PaginatedTO<UserApplicationRelTO> paginatedTO = new PaginatedTO<UserApplicationRelTO>();
        paginatedTO.setList(userApplicationRelTOs);
        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    @Override
    public List<Application> getApplications() {
        return applicationDao.getApplications();
    }

    @Override
    public Application getApplicationByApplicationId(String applicationId) throws AuthException {

        Session session = IamThreadContext.getSessionWithoutTransaction();
        boolean isSessionCreatedHere = false;
        if(session == null || !session.isOpen()) {
            isSessionCreatedHere = true;
            session = SessionFactoryUtil.getInstance().openSessionWithoutTransaction();
        }

        try {
            return applicationDao.getApplicationByApplicationId(applicationId, session);
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());

        } finally {
            if(isSessionCreatedHere && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Application getApplicationByApplicationAccountId(String applicationAccountId) throws AuthException {
        try {
            return applicationDao.getApplicationByApplicationAccountId(applicationAccountId);
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
    }

    @Override
    public Application getApplicationWithCallbackUrl(String applicationId) throws AuthException {
        try {
            return applicationDao.getApplicationWithCallbackUrl(applicationId);
        }
        catch (Exception e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
    }

    @Override
    public Application getNonADApplicationByApplicationId(String applicationId) throws AuthException {
        Application application;
        try {
            application = applicationDao.getApplicationByApplicationId(applicationId);
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        if (!ApplicationType.NON_AD.equals(application.getApplicationType())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_FOR_USER(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_FOR_USER());
        }
        return application;
    }


    @Override
    public Application getNonADApplicationByApplicationId(String applicationId, Session session) throws AuthException {
        Application application;
        try {
            application = applicationDao.getApplicationByApplicationId(applicationId, session);
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        if (!ApplicationType.NON_AD.equals(application.getApplicationType())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_FOR_USER(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_FOR_USER());
        }
        return application;
    }

    @Override
    public PaginatedTO<UserApplicationRelTO> getUserApplicationRels(Application application, String searchQuery, int page) {
        List<Object[]> userApplicationRels = userApplicationRelDaoIntf.getUserApplicationRels(application.getId(), searchQuery, page,
                Integer.parseInt(config.getProperty(Constant.LIMIT)));

        List<UserApplicationRelTO> userApplicationRelTOs = new ArrayList<UserApplicationRelTO>();
        for (Iterator<Object[]> iterator = userApplicationRels.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            long userId = Long.parseLong(String.valueOf(row[2]));
            User user;
            try {
                user = userDao.getById(userId);
            }
            catch (NotFoundException e) {
                continue;
            }
            UserApplicationRelTO userApplicationRel = new UserApplicationRelTO();
            userApplicationRel.setApplicationId(application.getApplicationId());
            userApplicationRel.setFullname(getFullName(userId));
            userApplicationRel.setMobile(getMobile(userId));
            userApplicationRel.setUsername(getUsername(userId));
            userApplicationRel.setUserId(userId);
            userApplicationRel.setTwoFactorStatus(String.valueOf(row[1]));
            userApplicationRel.setBindingStatus(String.valueOf(row[0]));
            userApplicationRelTOs.add(userApplicationRel);
        }
        Long count = userApplicationRelDaoIntf.getTotalUserApplicationRelCount(application.getId(), searchQuery);
        PaginatedTO<UserApplicationRelTO> paginatedTO = new PaginatedTO<UserApplicationRelTO>();
        paginatedTO.setList(userApplicationRelTOs);
        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    @Override
    public Application getActiveById(Long applicationId) throws NotFoundException {
        return applicationDao.getActiveById(applicationId);
    }

    @Override
    public List<Application> getPaginatedList(int page, int limit, String searchText, String _2faStatusFilter, String applicationTypeFilter) {
        return applicationDao.getPaginatedList(page, limit, searchText, _2faStatusFilter, applicationTypeFilter);
    }

    @Override
    public Long getTotalActiveApplicationCount(String searchText, String _2faStatusFilter, String applicationTypeFilter) {
        return applicationDao.getTotalActiveCount(searchText, _2faStatusFilter, applicationTypeFilter);
    }

    @Override
    public Application updateApplication(Session session, Application application) {
        return applicationDao.update(session, application);
    }

    @Override
    public Application editApplicationv2(Session session, ApplicationTO applicationTO) throws ApplicationNotFoundException, AuthException {

        Application application = applicationDao.getApplicationByIdWithCallbackUrl(applicationTO.getApplicationId());

        if (!application.getApplicationName().equals(applicationTO.getApplicationName())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
        }
        if (applicationTO.getApplicationSecret() != null && application.getApplicationSecret() != applicationTO.getApplicationSecret()) {
            String decryptedApplicationSecret;
            try {
                decryptedApplicationSecret = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), applicationTO.getApplicationSecret());
            }
            catch (Exception e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_APPLICATION_PASSWORD());
            }
            application.setApplicationSecret(AES128Impl.encryptData(decryptedApplicationSecret, KeyManagementUtil.getAESKey()));
        }
        List<Service> existingServices = new ArrayList<>();
        existingServices.addAll(application.getServices());
        if (applicationTO.getServices() != null && !applicationTO.getServices().isEmpty()) {
            List<Service> servicesToAdd = new ArrayList<>();
            for (ServiceTO serviceTO : applicationTO.getServices()) {
                Service service = null;
                try {
                    service = serviceDao.getServiceByServiceName(serviceTO.getServiceName());
                }
                catch (ServiceNotFoundException e) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
                }
                if (existingServices.contains(service)) {
                    existingServices.remove(service);
                } else {
                    servicesToAdd.add(service);
                }
            }
            application.getServices().addAll(servicesToAdd);
        }
        
        inactivateUserBinding(session, application, existingServices);
        application.getServices().removeAll(existingServices);

        if(applicationTO.getTokenTtl() != null) {
            application.setTokenTtl(applicationTO.getTokenTtl());
        }

        if(applicationTO.getAttemptCount() != null) {
            application.setAttemptCount(applicationTO.getAttemptCount());
        }
        Boolean currentIsPlaintextPasswordAllowed = application.getIsPlaintextPasswordAllowed();
        Boolean newIsPlaintextPasswordAllowed = applicationTO.isPlaintextPasswordAllowed();
        if (currentIsPlaintextPasswordAllowed == null || currentIsPlaintextPasswordAllowed != newIsPlaintextPasswordAllowed) {
            application.setIsPlaintextPasswordAllowed(newIsPlaintextPasswordAllowed);
        }
        if(applicationTO.getAlgorithm() != null) {
            application.setAlgorithm(Algorithm.valueOf(applicationTO.getAlgorithm()));
        }
        if(applicationTO.getNumberOfDigits() != null) {
            application.setNumberOfDigits(applicationTO.getNumberOfDigits());
        }
        if(applicationTO.getTotpExpiry() != null) {
            application.setTotpExpiry(applicationTO.getTotpExpiry());
        }
        if(applicationTO.getDescription() !=null && !applicationTO.getDescription().isEmpty()){
            application.setDescription(applicationTO.getDescription());
        }
        Boolean currentIsAuthenticationRequired = application.getAuthenticationRequired();
        Boolean newIsAuthenticationAllowed = applicationTO.getAuthenticationRequired();
        if (currentIsAuthenticationRequired == null || currentIsAuthenticationRequired != newIsAuthenticationAllowed) {
            application.setAuthenticationRequired(newIsAuthenticationAllowed);
        }
        if(applicationTO.getTransactionTimeout()!=null && !Objects.equals(applicationTO.getTransactionTimeout(), application.getTransactionTimeout())){
            application.setTransactionTimeout(applicationTO.getTransactionTimeout());
        }
        if (StringUtil.isNotNullOrEmpty(applicationTO.getApplicationType())) {
            ApplicationType applicationType = ApplicationType.valueOf(applicationTO.getApplicationType());
            application.setApplicationType(applicationType);
        }
        if(applicationTO.getIsFcmMultiDeviceAllowed()!=null){
            application.setFcmMultiDeviceAllowed(applicationTO.getIsFcmMultiDeviceAllowed());
        }
        if(applicationTO.getAccessTokenTimeoutInSeconds() != null){
            application.setAccessTokenLifeSpan(applicationTO.getAccessTokenTimeoutInSeconds());
        }
        if(applicationTO.getRefreshTokenTimeoutInSeconds() !=null){
            application.setRefreshTokenLifeSpan(applicationTO.getRefreshTokenTimeoutInSeconds());
        }
        updateApplication(session, application);
        return application;
    }



    @Override
    public Application onboardApplication(Session session, ApplicationTO applicationTO) throws AuthException {
        Application application = new Application();
        try {
            application.setApplicationId(applicationTO.getApplicationId());
            try {
                applicationDao.getApplicationByApplicationName(applicationTO.getApplicationName());
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_APPLICATION_ALREADY_PRESENT());
            } catch (ApplicationNotFoundException ignore) {
            }
            if (applicationTO.getApplicationName() != null && !Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), applicationTO.getApplicationName())) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_NAME(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_NAME());
            }
            application.setApplicationName(applicationTO.getApplicationName());
            application.setApplicationAccountId(applicationTO.getApplicationAccountId());
            System.out.println("applicationTO.getEnterpriseAccountId()..>>"+applicationTO.getEnterpriseAccountId());
            Enterprise enterprise = enterpriseDao.getEnterpriseByAccountId(applicationTO.getEnterpriseAccountId());
            System.out.println("enterprise..>>"+new Gson().toJson(enterprise));
            application.setEnterprise(enterprise);
            List<Service> services = new ArrayList<>();
            for (ServiceTO serviceTO : applicationTO.getServices()) {
                Service service = null;
                try {
                    service = serviceDao.getServiceByServiceName(serviceTO.getServiceName());
                }
                catch (ServiceNotFoundException e) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
                }
                services.add(service);
            }
            application.setServices(services);
            application.setPassword(AES128Impl.encryptData(applicationTO.getPassword(), KeyManagementUtil.getAESKey()));
            String decryptedApplicationSecret = applicationTO.getApplicationSecret();

            if(applicationTO.getIsCredentialsEncrypted()!= null && applicationTO.getIsCredentialsEncrypted().equals(Boolean.TRUE)) {
                try {
                    decryptedApplicationSecret = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), applicationTO.getApplicationSecret());
                }
                catch (Exception e) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_APPLICATION_PASSWORD());
                }
            }

            application.setApplicationSecret(AES128Impl.encryptData(decryptedApplicationSecret, KeyManagementUtil.getAESKey()));
            if (applicationTO.getAuthenticationRequired() != null) {
                application.setAuthenticationRequired(applicationTO.getAuthenticationRequired());
            }
            else {
                application.setAuthenticationRequired(false);
            }
            application.setDescription(applicationTO.getDescription());
            application.setTransactionTimeout(applicationTO.getTransactionTimeout());
            application.setTwoFactorStatus(TwoFactorStatus.valueOf(applicationTO.getTwoFactorStatus()));
            application.setDateTimeCreated(new Timestamp(System.currentTimeMillis()));
            application.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
            application.setApplicationType(ApplicationType.valueOf(applicationTO.getApplicationType()));

            if(applicationTO.getTokenTtl() != null) {
                application.setTokenTtl(applicationTO.getTokenTtl());
            }

            if(applicationTO.getAttemptCount() != null) {
                application.setAttemptCount(applicationTO.getAttemptCount());
            }

            if (applicationTO.getResetPinUserUnblockSetting() != null) {
                application.setUnblockSettings(ResetPinUserUnblockStatus.valueOf(applicationTO.getResetPinUserUnblockSetting()));
            }
            application.setIsPlaintextPasswordAllowed(applicationTO.isPlaintextPasswordAllowed());
            if(applicationTO.getTotpExpiry() != null) {
                application.setTotpExpiry(applicationTO.getTotpExpiry());
            }
            if(applicationTO.getNumberOfDigits() != null) {
                application.setNumberOfDigits(applicationTO.getNumberOfDigits());
            }
            if(applicationTO.getAlgorithm() != null) {
                application.setAlgorithm(Algorithm.valueOf(applicationTO.getAlgorithm()));
            }
            if(applicationTO.getIsFcmMultiDeviceAllowed()!=null){
                application.setFcmMultiDeviceAllowed(applicationTO.getIsFcmMultiDeviceAllowed());
            }
            application = applicationDao.create(session, application);
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_FOUND());
        }
        
        return application;
    }

    @Override
    public Application onboardApplicationV2(Session session, ApplicationTO applicationTO, Client camClient) throws AuthException {
        Application application = new Application();
        try {
            application.setApplicationId(applicationTO.getApplicationId());
            try {
                applicationDao.getApplicationByApplicationName(applicationTO.getApplicationName());
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_APPLICATION_ALREADY_PRESENT());
            } catch (ApplicationNotFoundException ignore) {
            }
            application.setApplicationName(applicationTO.getApplicationName());
            application.setApplicationAccountId(applicationTO.getApplicationAccountId());
            System.out.println("applicationTO.getEnterpriseAccountId()..>>"+applicationTO.getEnterpriseAccountId());
            Enterprise enterprise = enterpriseDao.getEnterpriseByAccountId(applicationTO.getEnterpriseAccountId());
            System.out.println("enterprise..>>"+new Gson().toJson(enterprise));
            application.setEnterprise(enterprise);
            List<Service> services = new ArrayList<>();
            for (ServiceTO serviceTO : applicationTO.getServices()) {
                Service service = null;
                try {
                    service = serviceDao.getServiceByServiceName(serviceTO.getServiceName());
                }
                catch (ServiceNotFoundException e) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
                }
                services.add(service);
            }
            application.setServices(services);
            application.setPassword(AES128Impl.encryptData(applicationTO.getPassword(), KeyManagementUtil.getAESKey()));
            String decryptedApplicationSecret = applicationTO.getApplicationSecret();

            if(applicationTO.getIsCredentialsEncrypted()!= null && applicationTO.getIsCredentialsEncrypted().equals(Boolean.TRUE)) {
                try {
                    decryptedApplicationSecret = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), applicationTO.getApplicationSecret());
                }
                catch (Exception e) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_APPLICATION_PASSWORD());
                }
            }
            application.setApplicationSecret(AES128Impl.encryptData(decryptedApplicationSecret, KeyManagementUtil.getAESKey()));
            if (applicationTO.getAuthenticationRequired() != null) {
                application.setAuthenticationRequired(applicationTO.getAuthenticationRequired());
            }
            else {
                application.setAuthenticationRequired(false);
            }
            application.setDescription(applicationTO.getDescription());
            application.setTransactionTimeout(applicationTO.getTransactionTimeout());
            application.setTwoFactorStatus(TwoFactorStatus.valueOf(applicationTO.getTwoFactorStatus()));
            application.setDateTimeCreated(new Timestamp(System.currentTimeMillis()));
            application.setDateTimeModified(new Timestamp(System.currentTimeMillis()));
            application.setApplicationType(ApplicationType.valueOf(applicationTO.getApplicationType()));

            if(applicationTO.getTokenTtl() != null) {
                application.setTokenTtl(applicationTO.getTokenTtl());
            }

            if(applicationTO.getAttemptCount() != null) {
                application.setAttemptCount(applicationTO.getAttemptCount());
            }

            if (applicationTO.getResetPinUserUnblockSetting() != null) {
                application.setUnblockSettings(ResetPinUserUnblockStatus.valueOf(applicationTO.getResetPinUserUnblockSetting()));
            }

            //TODO: Till here call the previous onboard function and just call update on line number 741 after setting the details
            if(camClient.getClientKcId() != null) {
                application.setKcId(camClient.getClientKcId());
                application.setKcClientSecret(camClient.getClientSecret());
                application.setAccessTokenLifeSpan(applicationTO.getAccessTokenTimeoutInSeconds());
                application.setRefreshTokenLifeSpan(applicationTO.getRefreshTokenTimeoutInSeconds());
                application.setOnboardStatus(OnboardStatus.CAM_ONBOARD_COMPLETE.name());
            }
            else {
                application.setOnboardStatus(OnboardStatus.CAM_ONBOARD_FAILED.name());
            }
            application.setIsPlaintextPasswordAllowed(applicationTO.isPlaintextPasswordAllowed());
            if(applicationTO.getTotpExpiry() != null) {
                application.setTotpExpiry(applicationTO.getTotpExpiry());
            }
            if(applicationTO.getNumberOfDigits() != null) {
                application.setNumberOfDigits(applicationTO.getNumberOfDigits());
            }
            if(applicationTO.getAlgorithm() != null) {
                application.setAlgorithm(Algorithm.valueOf(applicationTO.getAlgorithm()));
            }
            if(applicationTO.getIsFcmMultiDeviceAllowed()!=null){
                application.setFcmMultiDeviceAllowed(applicationTO.getIsFcmMultiDeviceAllowed());
            }
            application = applicationDao.create(session, application);
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_FOUND());
        }

        return application;
    }

    @Override
    public RemoteAccessSettingTO getRemoteAccessSettings(RemoteAccessSettingTO remoteAccessSettingTO) throws AuthException {
        //        SRAApplicationSetting sraConsumerSetting = DaoFactory.getSRAConsumerSetting().getSettingsByExternalAddressAndPort(remoteAccessSettingTO.getExternalAddress(),
        //                remoteAccessSettingTO.getExternalPort());
        //        if (sraConsumerSetting == null) {
        //            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND, errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND);
        //        }
        //        remoteAccessSettingTO = sraConsumerSetting.convertToTO();
        //        if (sraConsumerSetting.getProviderApplicationId() != null) {
        //            try {
        //                SRAGatewaySetting sraProviderSetting = DaoFactory.getSRAProviderSetting().getSettingsByApplicationId(sraConsumerSetting.getProviderApplicationId());
        //                remoteAccessSettingTO.setSraProviderAddress(sraProviderSetting.getAddress());
        //                remoteAccessSettingTO.setSraProviderPort(sraProviderSetting.getPort());
        //            }
        //            catch (NotFoundException e) {
        //                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND, errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND);
        //            }
        //        }
        return remoteAccessSettingTO;
    }

    @Override
    public List<ApplicationTO> getSRAApplications(String sraApplicationType, String role) throws AuthException {
        List<ApplicationTO> applicationsTO = new ArrayList<>();
        if (!permissionService.isPermissionValidForRole(PermissionUtil.VIEW_APPLICATIONS, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        List<Application> applications = applicationDao.getSRAApplications(SRAApplicationType.valueOf(sraApplicationType));
        for (Application application : applications) {
            ApplicationTO applicationTO = convertToTO(application);
            applicationsTO.add(applicationTO);
        }
        return applicationsTO;
    }

    private ApplicationTO convertToTO(Application application) {
        ApplicationTO applicationTO = new ApplicationTO();
        applicationTO.setId(application.getId());
        applicationTO.setApplicationId(application.getApplicationId());
        applicationTO.setApplicationName(application.getApplicationName());
        return applicationTO;
    }

    private boolean isTunnelingSettingsUpdated(Application application, ApplicationTO applicationTO) {
        SRAApplicationSetting sraConsumerSetting = null;
        try {
            sraConsumerSetting = sraApplicationSettingDao.getSettingsByApplicationId(application);
        }
        catch (NotFoundException e) {
        }
        return sraConsumerSetting == null || !applicationTO.getUrl().equals(sraConsumerSetting.getUrl())
               || !applicationTO.getInternalAddress().equals(sraConsumerSetting.getInternalAddress())
               || !applicationTO.getExternalAddress().equals(sraConsumerSetting.getExternalAddress())
               || applicationTO.getExternalPort() != sraConsumerSetting.getExternalPort()
               || applicationTO.getInternalPort() != sraConsumerSetting.getInternalPort()
               || (sraConsumerSetting.getPortForwardingFacadeLocalPort() != null && applicationTO.getDefaultLocalPort() != null
                   && applicationTO.getDefaultLocalPort() != sraConsumerSetting.getPortForwardingFacadeLocalPort());
    }

    @Override
    public void updateSRAProviderSettingsOnServer(StagingSRAProviderSettingTO sraProviderSetting, Application application) throws AuthException {
        GatewaySettingsTO gatewaySettingsTO = new GatewaySettingsTO();
        gatewaySettingsTO.setAddress(sraProviderSetting.getAddress());
        gatewaySettingsTO.setPort(sraProviderSetting.getPort());
        gatewaySettingsTO.setClientProxyPort(sraProviderSetting.getDefaultLocalPort());
        iamExtensionService.updateSRAProviderSettings(application, gatewaySettingsTO);
    }

    @Override
    public void updateSRAConsumerSettingsOnServer(SRAApplicationSettingTO sraConsumerSetting, Application application) throws AuthException {
        RemoteAccessSettingsTO remoteAccessSettingTO = new RemoteAccessSettingsTO();
        remoteAccessSettingTO.setUrl(sraConsumerSetting.getUrl());
        remoteAccessSettingTO.setExternalAddress(sraConsumerSetting.getExternalAddress());
        remoteAccessSettingTO.setInternalAddress(sraConsumerSetting.getInternalAddress());
        remoteAccessSettingTO.setExternalPort(sraConsumerSetting.getExternalPort());
        remoteAccessSettingTO.setInternalPort(sraConsumerSetting.getInternalPort());
        remoteAccessSettingTO.setProtocol(sraConsumerSetting.getProtocol());
        if (Boolean.TRUE.equals(sraConsumerSetting.getPortForwardingFacade())) {
            remoteAccessSettingTO.setDefaultLocalPort(sraConsumerSetting.getDefaultLocalPort());
        }
        remoteAccessSettingTO.setIsPortForwardingFacade(sraConsumerSetting.getPortForwardingFacade());
        remoteAccessSettingTO.setGatewayName(sraConsumerSetting.getGatewayName());
        remoteAccessSettingTO.setWhiteListedURLs(sraConsumerSetting.getWhiteListedURLs());
        iamExtensionService.updateSRAConsumerSettings(application, remoteAccessSettingTO);

    }

    @Override
    public void updateSRAConsumerSettings(Session session, SRAApplicationSettingTO stagingSRAConsumerSetting, Application application) throws AuthException {
        SRAApplicationSetting sraApplicationSetting = null;
        boolean isSRAConsumerSettingPresent = false;

        try {
            sraApplicationSetting = sraApplicationSettingDao.getSettingsByApplicationId(application);
            isSRAConsumerSettingPresent = true;
        }
        catch (Exception e) {
            isSRAConsumerSettingPresent = false;
        }
        if (sraApplicationSetting == null) {
            sraApplicationSetting = new SRAApplicationSetting();
        }
        sraApplicationSetting.setExternalAddress(stagingSRAConsumerSetting.getExternalAddress());
        sraApplicationSetting.setExternalPort(stagingSRAConsumerSetting.getExternalPort());
        sraApplicationSetting.setInternalAddress(stagingSRAConsumerSetting.getInternalAddress());
        sraApplicationSetting.setInternalPort(stagingSRAConsumerSetting.getInternalPort());
        sraApplicationSetting.setProtocol(stagingSRAConsumerSetting.getProtocol());
        sraApplicationSetting.setUrl(stagingSRAConsumerSetting.getUrl());
        sraApplicationSetting.setWhiteListedURLs(stagingSRAConsumerSetting.getWhiteListedURLs());
        if (Boolean.TRUE.equals(stagingSRAConsumerSetting.getPortForwardingFacade())) {
            sraApplicationSetting.setPortForwardingFacadeLocalPort(stagingSRAConsumerSetting.getDefaultLocalPort());
        }
        System.out.println("updateSRAConsumerSettings " + isSRAConsumerSettingPresent);
        sraApplicationSetting.setPortForwardingFacade(stagingSRAConsumerSetting.getPortForwardingFacade());

        SRAApplicationGatewayRel sraApplicationGatewayRel = sraApplicationGatewayRelDaoIntf.getSRAApplicationGatewayRel(application);
        if (sraApplicationGatewayRel == null) {
            System.out.println("sraApplicationGatewayRel...Create>>");
            SRAGatewaySetting sraGatewaySetting = null;
            try {
                sraGatewaySetting = sraGatewaySettingDao.getSRAGatewaySettingByName(stagingSRAConsumerSetting.getGatewayName());
            }
            catch (NotFoundException e) {
                //throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND, errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND);
            }
            SRAApplicationGatewayRel sraApplicationGatewayRel2 = new SRAApplicationGatewayRel();
            System.out.println("Settings application "+ application.getId());
            sraApplicationGatewayRel2.setApplication(application);
            sraApplicationGatewayRel2.setSraGatewaySetting(sraGatewaySetting);
            sraApplicationGatewayRelDaoIntf.create(session, sraApplicationGatewayRel2);
        }
        else {
            if (!sraApplicationGatewayRel.getSraGatewaySetting().getName().equals(stagingSRAConsumerSetting.getGatewayName())) {
                System.out.println("sraApplicationGatewayRel...update>>");
                SRAGatewaySetting sraGatewaySetting = null;
                try {
                    sraGatewaySetting = sraGatewaySettingDao.getSRAGatewaySettingByName(stagingSRAConsumerSetting.getGatewayName());
                }
                catch (NotFoundException e) {
                    //throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND, errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND);
                }
                sraApplicationGatewayRel.setSraGatewaySetting(sraGatewaySetting);
                sraApplicationGatewayRelDaoIntf.update(session, sraApplicationGatewayRel);
            }
        }

        if (isSRAConsumerSettingPresent) {
            sraApplicationSettingDao.update(session, sraApplicationSetting);
        }
        else {
            sraApplicationSetting.setApplication(application);
            sraApplicationSettingDao.create(session, sraApplicationSetting);
        }

    }

    @Override
    public void updateSRAProviderSettings(StagingSRAProviderSettingTO stagingSRAProviderSetting, String applicationId) throws AuthException {
        //        boolean isSRAProviderSettingPresent = false;
        //        SRAGatewaySetting sraProviderSetting = null;
        //        try {
        //            sraProviderSetting = DaoFactory.getSRAProviderSetting().getSettingsByApplicationId(applicationId);
        //            isSRAProviderSettingPresent = true;
        //        }
        //        catch (Exception e) {
        //        }
        //        if (sraProviderSetting == null) {
        //            sraProviderSetting = new SRAGatewaySetting();
        //        }
        //        sraProviderSetting.setAddress(stagingSRAProviderSetting.getAddress());
        //        sraProviderSetting.setPort(stagingSRAProviderSetting.getPort());
        //        sraProviderSetting.setDefaultLocalPort(stagingSRAProviderSetting.getDefaultLocalPort());
        //        if (isSRAProviderSettingPresent) {
        //            DaoFactory.getSRAProviderSetting().update(sraProviderSetting);
        //        }
        //        else {
        //            sraProviderSetting.setApplicationId(applicationId);
        //            DaoFactory.getSRAProviderSetting().create(sraProviderSetting);
        //        }
    }

    private void removeSRAProviderSettings(Session session, Application application) {
        try {
            SRAGatewaySetting sraProviderSetting = sraGatewaySettingDao.getSettingsByApplicationId(application.getApplicationId());
            sraGatewaySettingDao.remove(session, sraProviderSetting);
        }
        catch (NotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private void removeSRASettings(Session session, Application application) throws AuthException {
        SRAGatewaySetting sraProviderSetting = null;
        try {
            sraProviderSetting = sraGatewaySettingDao.getSettingsByApplicationId(application.getApplicationId());
        }
        catch (NotFoundException e) {
        }
        SRAApplicationSetting sraConsumerSetting = null;
        try {
            sraConsumerSetting = sraApplicationSettingDao.getSettingsByApplicationId(application);
        }
        catch (NotFoundException e) {
        }
        if (sraProviderSetting != null) {
            sraGatewaySettingDao.remove(session, sraProviderSetting);
        }
        if (sraConsumerSetting != null) {
            sraApplicationSettingDao.remove(session, sraConsumerSetting);
        }
    }

    public String getMobile(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.MOBILE_NO);
        logger.log(Level.DEBUG, "userId : "+userId);
        if (attributeStore != null) {
            logger.log(Level.DEBUG, "userId : "+userId+" - AttributeValue : "+attributeStore.getAttributeValue());
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    public String getUsername(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.USER_ID);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    public String getFullName(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.FULL_NAME);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    public String getEmail(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.EMAIL);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    @Override
    public ApplicationTO editSRASettings(Session session, SRAApplicationSettingTO stagingSRAConsumerSetting, StagingSRAProviderSettingTO stagingSRAProviderSetting, Application application,
            ApplicationTO applicationTO) throws AuthException {

        boolean isSRATypeRemoved = false;//applicationTO.getSraApplicationType() == null && application.getSraApplicationType() != null;
        if (isSRATypeRemoved) {
            System.out.println("Inside removed");
            iamExtensionService.removeSRASettings(application);
            removeSRASettings(session, application);
        }
        else if (stagingSRAConsumerSetting != null) {
            System.out.println("updateSRAConsumerSettings");
            updateSRAConsumerSettings(session, stagingSRAConsumerSetting, application);
        }
        else if (stagingSRAProviderSetting != null) {
            System.out.println("updateSRAProviderSettings");
            updateSRAProviderSettings(stagingSRAProviderSetting, application.getApplicationId());
        }
        return null;
    }

    @Override
    public ApplicationTO createSRAApplicationGatewayRelDeleteRequest(Session session, ApplicationTO applicationTO, String actor) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(applicationTO));
        request.setRequestorComments(applicationTO.getComments());
        request.setRequestType(RequestType.SRA_APPLICATION_SETTING_DELETE);
        request.setMaker(actor);
        try {
            request.setRequestor(attributeStoreDaoIntf.getUserByAttributeValue(actor));
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(),
                    errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        applicationTO.setStatus(Constant.SUCCESS_STATUS);
        applicationTO.setId(createdRequest.getId());
        AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "delete sraApplicationSetting  request generated successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);

        return applicationTO;
    }

    @Override
    public ApplicationTO approveDeleteSRAApplicationSetting(Session session, ApplicationTO applicationTO) throws AuthException {
        Application application = null;
        AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "deleted sraApplicationSettings  of application approved successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);

        try {
            application = applicationDao.getApplicationByApplicationId(applicationTO.getApplicationId());
        }
        catch (ApplicationNotFoundException e1) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(),
                    errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        iamExtensionService.removeSRASettings(application);

        SRAApplicationGatewayRel sraApplicationGatewayRel = sraApplicationGatewayRelDaoIntf
                .getSRAApplicationGatewayRel(application);
        if (sraApplicationGatewayRel == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND(),
                    errorConstant.getERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND());
        }
        SRAApplicationSetting sraApplicationSetting = null;

        try {
            sraApplicationSetting = sraApplicationSettingDao.getSettingsByApplicationId(application);
        }
        catch (NotFoundException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        if (sraApplicationSetting == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND(),
                    errorConstant.getERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_NOT_FOUND());
        }
        sraApplicationSettingDao.remove(session, sraApplicationSetting);
        sraApplicationGatewayRelDaoIntf.remove(session, sraApplicationGatewayRel);
        AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "deleted sraApplicationSettings  of application successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);

        return applicationTO;
    }

    @Override
    public List<Application> getPaginatedList(int page, int limit, String searchText, String _2faStatusFilter, String applicationTypeFilter, String applicationIds) {
        return applicationDao.getPaginatedList(page, limit, searchText, _2faStatusFilter, applicationTypeFilter, applicationIds);
    }
    
    @Override
    public Long getTotalActiveApplicationCount(String searchText, String _2faStatusFilter, String applicationTypeFilter, String applicationIds) {
        return applicationDao.getTotalActiveCount(searchText, _2faStatusFilter, applicationTypeFilter, applicationIds);
    }

    @Override
    public List<Application> getPaginatedList(int page, int limit, String searchText) {
        return applicationDao.getPaginatedList(page, limit, searchText);
    }

    @Override
    public List<Application> getAllApplications() {
        return applicationDao.getAllApplications();
    }

    @Override
    public void addFCMMultiDeviceDetails(ApplicationTO applicationTO) {
        FcmNotificationDetails fcmNotificationDetails= getFcmDetailsFromApplicationTO(applicationTO,null);
         iamMongoConnectionManager.createFcmNotificationDetails(fcmNotificationDetails);
    }

    @Override
    public void editFcmNotificationDetails(ApplicationTO applicationTO) throws AuthException {
        FcmNotificationDetails fcmNotificationDetails=null;
        MorphiaSession morphiaSession=iamMongoConnectionManager.getDatastore().startSession();
        morphiaSession.startTransaction();
        try {
             fcmNotificationDetails = iamMongoConnectionManager.getFcmNotificationDetailsByID(applicationTO.getApplicationId());
        }catch (NotFoundException e){
            if(applicationTO.getIsFcmMultiDeviceAllowed()!=null && applicationTO.getIsFcmMultiDeviceAllowed()){
                addFCMMultiDeviceDetails(applicationTO);
            }
        }
        catch (Exception e){
            throw new AuthException(new Exception(),errorConstant.getERROR_CODE_INVALID_DATA(),e.getMessage());
        }
        try {
            if (fcmNotificationDetails != null && applicationTO.getIsFcmMultiDeviceAllowed()) {
                fcmNotificationDetails = getFcmDetailsFromApplicationTO(applicationTO,fcmNotificationDetails);
                iamMongoConnectionManager.update(fcmNotificationDetails, morphiaSession);
            }
            if (fcmNotificationDetails != null && !applicationTO.getIsFcmMultiDeviceAllowed()) {
                iamMongoConnectionManager.delete(fcmNotificationDetails,morphiaSession);
            }
            morphiaSession.commitTransaction();
        }catch (Exception e){
            morphiaSession.abortTransaction();
            logger.log(Level.ERROR,e.getMessage(),e);
        }finally {
            if (morphiaSession != null && morphiaSession.hasActiveTransaction()) {
                morphiaSession.abortTransaction();
            }
        }
    }

    public  FcmNotificationDetails getFcmDetailsFromApplicationTO(ApplicationTO applicationTO,FcmNotificationDetails fcmNotificationDetails){
        if(fcmNotificationDetails==null) {
            fcmNotificationDetails = new FcmNotificationDetails();
        }
        if(applicationTO.getBundleId()!=null && !applicationTO.getBundleId().isEmpty()){
            fcmNotificationDetails.setBundleId(applicationTO.getBundleId());
        }
        if(applicationTO.getPackageName()!=null && !applicationTO.getPackageName().isEmpty()) {
            fcmNotificationDetails.setPackageName(applicationTO.getPackageName());
        }
        if(applicationTO.getProjectId()!=null && !applicationTO.getProjectId().isEmpty()) {
            fcmNotificationDetails.setProjectId(applicationTO.getProjectId());
        }
        if(applicationTO.getApplicationId()!=null && !applicationTO.getApplicationId().isEmpty()) {
            fcmNotificationDetails.setApplicationId(applicationTO.getApplicationId());
        }
        if(applicationTO.getServiceAccountJson()!=null && !applicationTO.getServiceAccountJson().isEmpty()) {
            fcmNotificationDetails.setServiceAccountJson(applicationTO.getServiceAccountJson());
        }
        return fcmNotificationDetails;
    }

    @Override
    public FcmNotificationDetails getFcmDetailsByApplicationId(String applicationId) throws Exception {
      return iamMongoConnectionManager.getFcmNotificationDetailsByID(applicationId);

    }
}
