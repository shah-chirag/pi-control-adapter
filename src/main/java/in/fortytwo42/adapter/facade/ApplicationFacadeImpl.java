package in.fortytwo42.adapter.facade;

import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.fortytwo42.adapter.jar.entities.FcmNotificationDetails;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;
import org.keycloak.representations.idm.ClientRepresentation;

import com.google.gson.Gson;

import dev.samstevens.totp.code.HashingAlgorithm;
import in.fortytwo42.adapter.cam.dto.Client;
import in.fortytwo42.adapter.cam.dto.ClientTO;
import in.fortytwo42.adapter.cam.facade.ClientFacadeImpl;
import in.fortytwo42.adapter.cam.facade.ClientFacadeIntf;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.PermissionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.SRAApplicationSettingTO;
import in.fortytwo42.adapter.transferobj.StagingSRAProviderSettingTO;
import in.fortytwo42.adapter.transferobj.TotpSettingsTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.FileDownloader;
import in.fortytwo42.adapter.util.FileUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.factory.OnboardApplicationCsv;
import in.fortytwo42.daos.dao.ApplicationDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.dao.SRAApplicationGatewayRelDaoIntf;
import in.fortytwo42.daos.dao.SRAApplicationSettingDaoIntf;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.enums.CryptoEntityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.EnterpriseWE;
import in.fortytwo42.enterprise.extension.tos.EscTO;
import in.fortytwo42.enterprise.extension.tos.QuestionAnswerTO;
import in.fortytwo42.enterprise.extension.tos.SequenceStoreTO;
import in.fortytwo42.enterprise.extension.tos.ServiceTO;
import in.fortytwo42.enterprise.extension.tos.Status;
import in.fortytwo42.enterprise.extension.utils.RandomString;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.CallbackUrl;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.bean.SRAApplicationGatewayRel;
import in.fortytwo42.entities.bean.SRAApplicationSetting;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.enums.ResetPinUserUnblockStatus;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.RemoteAccessSettingTO;
import in.fortytwo42.tos.transferobj.RequestTO;
import in.fortytwo42.tos.transferobj.RunningHashTo;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

// TODO: Auto-generated Javadoc
/**
 * The Class ApplicationFacadeImpl.
 */
public class ApplicationFacadeImpl implements ApplicationFacadeIntf {

    /** The application facade impl log. */
    private String APPLICATION_FACADE_IMPL_LOG = "<<<<< ApplicationFacadeImpl";

    /** The Constant logger. */
    private static Logger logger = LogManager.getLogger(ApplicationFacadeImpl.class);
    
    private static final String APPLICATION_NAME = "applicationName";

    /** The application processor intf. */
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    /** The request processor. */
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private PermissionServiceIntf permissionService = ServiceFactory.getPermissionService();
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private ApplicationDaoIntf applicationDao = DaoFactory.getApplicationDao();
    private SRAApplicationSettingDaoIntf sraApplicationSettingDao = DaoFactory.getSRAApplicationSetting();
    private RequestDaoIntf requestDao = DaoFactory.getRequestDao();

    private SRAApplicationGatewayRelDaoIntf sraApplicationGatewayRelDao = DaoFactory.getSRAApplicationGatewayRelDoa();

    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private Config config = Config.getInstance();
    private PermissionUtil permissionUtil = PermissionUtil.getInstance();
    private ClientFacadeIntf camClientFacade = ClientFacadeImpl.getInstance();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private final ExecutorService pool;

    public ApplicationFacadeImpl() {
        super();
        int poolSize = 10;
        try {
            poolSize = Integer.parseInt(config.getProperty(Constant.CSV_PROCESSING_THREAD_POOL_SIZE));
        }
        catch (NumberFormatException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);
    }


    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final ApplicationFacadeImpl INSTANCE = new ApplicationFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of ApplicationFacadeImpl.
     *
     * @return single instance of ApplicationFacadeImpl
     */
    public static ApplicationFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Gets the user application rels.
     *
     * @param applicationId the application id
     * @param status the status
     * @param searchQuery the search query
     * @param page the page
     * @param role the role
     * @param actor the actor
     * @return the user application rels
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<UserApplicationRelTO> getUserApplicationRels(String applicationId, String status, String searchQuery, int page, String role, String actor)
            throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " getUserApplicationRels : start");
        PaginatedTO<UserApplicationRelTO> userApplicationRel = null;
        if (!permissionService.isPermissionValidForRole(PermissionUtil.GET_USER_APPLICATION_REL, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        Application application = applicationService.getApplicationByApplicationId(applicationId);
        userApplicationRel = applicationService.getUserApplicationRels(application, searchQuery, page);
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " getUserApplicationRels : end");
        return userApplicationRel;
    }

    /**

    /**
     * Delete application.
     *
     * @param applicationTo the application to
     * @param role the role
     * @param actor the actor
     * @return the application TO
     * @throws AuthException the auth exception
     */
    @Override
    public ApplicationTO deleteApplication(ApplicationTO applicationTo, String role, String actor) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " deleteApplication : start");
        if (!permissionService.isPermissionValidForRole(PermissionUtil.DELETE_APPLICATION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        Application application = applicationService.getApplicationByApplicationId(applicationTo.getApplicationId());
        if (!application.getApplicationName().equals(applicationTo.getApplicationName())) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
        }
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " deleteApplication : end");
        Session session = sessionFactoryUtil.getSession();
        try {
            ApplicationTO applicationTO2 = applicationService.deleteApplication(session, applicationTo, application, actor);
            sessionFactoryUtil.closeSession(session);
            return applicationTO2;
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Generate application secret.
     *
     * @return the string
     */
    @Override
    public String generateApplicationSecret() {
        return StringUtil.getRandomString(8);
    }

    /**
     * Gets the applications.
     *
     * @return the applications
     */
    @Override
    public List<Application> getApplications() {
        return applicationService.getApplications();
    }

    /**
     * Gets the applications.
     *
     * @param applicationUpdateStatus the application update status
     * @param page the page
     * @param searchText the search text
     * @param _2faStatusFilter the 2 fa status filter
     * @param applicationType the application type
     * @param role the role
     * @return the applications
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<ApplicationTO> getApplications(int page, String searchText, String _2faStatusFilter, String applicationType, String role, Long userGroupId)
            throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " getApplications : start");
        List<ApplicationTO> applicationList = new ArrayList<>();
        Long count;

//        if (!permissionService.isPermissionValidForRole(PermissionUtil.VIEW_APPLICATIONS, role)) {
//            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
//        }
        List<Application> applications = null;
        String applicationIds = null;
        if (userGroupId != null) {
            applicationIds = ServiceFactory.getUserApplicationServiceRelService().getApplicationIds(userGroupId);
        }
        System.out.println("******** applicationIds : " + applicationIds);
        applications = applicationService.getPaginatedList(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText, _2faStatusFilter,
                applicationType, applicationIds);
        //applications = applicationService.getPaginatedList(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText);
        count = applicationService.getTotalActiveApplicationCount(searchText, _2faStatusFilter, applicationType, applicationIds);

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
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
            try {
                System.out.println("CONSUMER App id " + application.getApplicationId());
                SRAApplicationSetting sraApplicationSetting = sraApplicationSettingDao.getSettingsByApplicationId(application);
                applicationTO.setUrl(sraApplicationSetting.getUrl());
                applicationTO.setExternalAddress(sraApplicationSetting.getExternalAddress());
                applicationTO.setExternalPort(sraApplicationSetting.getExternalPort());
                applicationTO.setInternalAddress(sraApplicationSetting.getInternalAddress());
                applicationTO.setInternalPort(sraApplicationSetting.getInternalPort());
                applicationTO.setProtocol(sraApplicationSetting.getProtocol());
                SRAApplicationGatewayRel sraApplicationGatewayRel = sraApplicationGatewayRelDao.getSRAApplicationGatewayRel(application);
                applicationTO.setGatewayName(sraApplicationGatewayRel.getSraGatewaySetting().getName());
                applicationTO.setPortForwardingFacade(sraApplicationSetting.getPortForwardingFacade());
                if (sraApplicationSetting.getPortForwardingFacadeLocalPort() != null) {
                    applicationTO.setDefaultLocalPort(sraApplicationSetting.getPortForwardingFacadeLocalPort());
                }
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            if(application.isFcmMultiDeviceAllowed()!=null && application.isFcmMultiDeviceAllowed()){
                try {
                  FcmNotificationDetails fcmNotificationDetails = applicationService.getFcmDetailsByApplicationId(application.getApplicationId());
                  applicationTO.setProjectId(fcmNotificationDetails.getProjectId());
                  applicationTO.setBundleId(fcmNotificationDetails.getBundleId());
                  applicationTO.setPackageName(fcmNotificationDetails.getPackageName());
                }catch (Exception e){
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
            applicationList.add(applicationTO);
        }
        PaginatedTO<ApplicationTO> paginatedTO = new PaginatedTO<ApplicationTO>();
        paginatedTO.setList(applicationList);
        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " getApplications : end");
        return paginatedTO;
    }

    /**
     * Validate permission.
     *
     * @param actionType the action type
     * @param role the role
     * @throws AuthException the auth exception
     */
    private void validatePermission(String actionType, String role) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " validatePermission : start");
        switch (actionType) {
            case Constant.INSERT:
                if (!permissionService.isPermissionValidForRole(PermissionUtil.CREATE_APPLICATION_LABEL, role)) {
                    logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " validatePermission : end");
                    throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
                }
                break;
            case Constant.UPDATE:
                if (!permissionService.isPermissionValidForRole(PermissionUtil.EDIT_APPLICATION_LABEL, role)) {
                    logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " validatePermission : end");
                    throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
                }
                break;
            case Constant.DELETE:
                if (!permissionService.isPermissionValidForRole(PermissionUtil.DELETE_APPLICATION_LABEL, role)) {
                    logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " validatePermission : end");
                    throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
                }
                break;
            default:
                break;
        }
    }

    /**
     * Checks if is application secret valid.
     *
     * @param applicationSecret the application secret
     * @param receivedApplicationSecret the received application secret
     * @return true, if is application secret valid
     */
    private boolean isApplicationSecretValid(String applicationSecret, String receivedApplicationSecret) {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " isApplicationSecretValid : start");
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
            logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " isApplicationSecretValid : end");
            return hashedSecret.equals(generatedHash);
        }
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " isApplicationSecretValid : end");
        return false;
    }

    /**
     * Inactivate user binding.
     *
     * @param application the application
     * @param servicesToRemove the services to remove
     * @throws AuthException the auth exception
     */
    private void inactivateUserBinding(Application application, List<Service> servicesToRemove) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " inactivateUserBinding : start");
        //        List<UserApplicationRel> userApplicationRels;
        //        UserApplicationRelDaoIntf userApplicationRelDaoIntf = DaoFactory.getUserApplicationRel();
        //        UserApplicationStagingRelDaoIntf userApplicationStagingRelDaoIntf = DaoFactory.getUserApplicationStagingRel();
        //        IAMExtension iamExtension;
        //        try {
        //            iamExtension = IAMUtil.getInstance().getIAMExtension(application.getEnterpriseId());
        //            Token token = IAMUtil.getInstance().authenticate(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
        //            Set<User> users = new HashSet<>();
        //            for (Service service : servicesToRemove) {
        //                userApplicationStagingRelDaoIntf.inactivateUserApplicationStagingRels(service.getId(), application.getId());
        //                userApplicationRels = userApplicationRelDaoIntf.getApplicationRelsForServices(service.getId(), application.getId());
        //                if (!userApplicationRels.isEmpty()) {
        //                    for (UserApplicationRel userApplicationRel : userApplicationRels) {
        //                        User user = userApplicationRel.getId().getAdUser();
        //                        iamExtension.forceTimeoutApprovalAttemptsByLookupId(token, user.getUsername() + "|" + user.getMobile(), service.getServiceName());
        //                        boolean isSingleMobileBindingPresent = DaoFactory.getUserApplicationRel().getMobileAndApplicationRelCount(application.getId(), user.getMobile()) == 1;
        //                        if (isSingleMobileBindingPresent) {
        //                            iamExtension.unbindConsumerApplication(token, user.getMobile());
        //                        }
        //                        userApplicationRel.setBindingStatus(BindingStatus.INACTIVE);
        //                        userApplicationRelDaoIntf.update(userApplicationRel);
        //                        users.add(user);
        //                    }
        //                }
        //            }
        //            if (!users.isEmpty()) {
        //                for (User user : users) {
        //                    List<UserApplicationRel> activeUserApplicationRels = userApplicationRelDaoIntf.getApplicationRelsForUser(user.getId());
        //                    if (activeUserApplicationRels == null || activeUserApplicationRels.isEmpty()) {
        //                        user.setIamStatus(IAMStatus.DISABLED);
        //                        //userDaoIntf.update(user);
        //                    }
        //                }
        //            }
        //        }
        //        catch (IAMException e) {
        //            logger.log(Level.ERROR, e);
        //            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        //        }
        //        finally {
        //            logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " inactivateUserBinding : end");
        //        }
    }

    @Override
    public ApplicationTO onboardApplication(String role, String actor,Long id, ApplicationTO applicationTO,
                                            boolean saveRequest) throws AuthException {
        /*if (permissionProcessorIntf.isPermissionValidForRole(RequestType.APPLICATION_ONBOARD.name(), role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }*/

        Session session = sessionFactoryUtil.getSession();
        try {
            onboardApplication(session, role, actor,id, applicationTO, saveRequest);
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return applicationTO;
    }

    @Override
    public ApplicationTO clearCache( ApplicationTO applicationTO) throws AuthException {
        /*if (permissionProcessorIntf.isPermissionValidForRole(RequestType.APPLICATION_ONBOARD.name(), role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }*/
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " clearCache : start");

        try {
            String realm = Config.getInstance().getProperty(Constant.CAM_REALM);
            ClientRepresentation clientRepresentation = camClientFacade.getClient(realm,applicationTO.getApplicationId());
            ClientTO clientTO = new ClientTO();
            clientTO.setClientId(applicationTO.getApplicationId()+"-temp");
            camClientFacade.editClient(realm,clientRepresentation.getId(),clientTO);
            clientRepresentation.setClientId(applicationTO.getApplicationId());
            clientRepresentation.setId(null);
            clientRepresentation.setProtocolMappers(null);
            camClientFacade.onboardClientWithCLientRepresentation(realm,clientRepresentation);
        }
        catch (AuthException e) {
            throw e;
        }
        finally {
            logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " clearCache: end");
        }

        return applicationTO;
    }


    @Override
    public ApplicationTO onboardApplicationBulk(String role, String actor, ApplicationTO applicationTO) throws AuthException {
        /*if (permissionProcessorIntf.isPermissionValidForRole(RequestType.APPLICATION_ONBOARD.name(), role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }*/

        Session session = sessionFactoryUtil.getSession();
        try {
            approveApplicationOnboard(session, role, actor, applicationTO);
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return applicationTO;
    }

    @Override
    public ApplicationTO onboardApplication(Session session, String role, String actor,Long id,ApplicationTO applicationTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " onboardApplication : start");
        validateApplicationName(applicationTO.getApplicationName());

        applicationTO = requestService.createApplicationOnboardRequest(session, applicationTO, actor,id, saveRequest);
        if (!saveRequest) {
            AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "  application onboard request approved ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);

            applicationTO = approveApplicationOnboard(session, role, actor, applicationTO);
        }
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " onboardApplication : end");
        return applicationTO;
    }
    
    private void validateApplicationName(String name) throws AuthException {
        try {
            applicationDao.getActiveByUniqueNonPrimaryKey(APPLICATION_NAME, name);
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_APPLICATION_ALREADY_PRESENT());
        }
        catch (NotFoundException e) {
            logger.log(Level.DEBUG, e);
        }
    }

    @Override
    public ApplicationTO editApplicationv2(String role, String actor,Long id, ApplicationTO applicationTO,
                                           boolean saveRequest) throws AuthException {
        /*if (permissionProcessorIntf.isPermissionValidForRole(RequestType.APPLICATION_ONBOARD.name(), role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }*/

        Session session = sessionFactoryUtil.getSession();
        try {
            editApplicationv2(session, role, actor,id, applicationTO, saveRequest);
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return applicationTO;
    }

    @Override
    public ApplicationTO editApplicationv2(Session session, String role, String actor,Long id,
                                           ApplicationTO applicationTO, boolean saveRequest)
            throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " editApplicationv2 : start");

        try {
            Application application;
            application = applicationDao.getApplicationByIdWithCallbackUrl(applicationTO.getApplicationId());
            if (!application.getApplicationName().equals(applicationTO.getApplicationName())) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
            }
            applicationTO = requestService.createApplicationEditRequest(session, applicationTO, actor,id, saveRequest);

            if (!saveRequest) {
                AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "application edit request approved successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);

                applicationTO = approveApplicationEdit(session, role, actor, applicationTO);
            }
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " editApplicationv2 : end");
        return applicationTO;
    }

    //    private boolean isSRATypeChanged(Application application, ApplicationTO applicationTO) throws AuthException {
    //        if (application.getSraApplicationType() != null && application.getSraApplicationType() == SRAApplicationType.PROVIDER && applicationTO.getSraApplicationType() == null
    //            || applicationTO.getSraApplicationType() == SRAApplicationType.CONSUMER) {
    //            List<SRAConsumerSetting> consumerSettings = DaoFactory.getSRAConsumerSetting().getSettingsByProviderId(application.getApplicationId());
    //            if (consumerSettings == null || consumerSettings.isEmpty()) {
    //                return true;
    //            }
    //            else {
    //                throw new AuthException(null, errorConstant.getERROR_CODE_GATEWAY_PROVIDER_IN_USE, errorConstant.getERROR_MESSAGE_GATEWAY_PROVIDER_IN_USE);
    //            }
    //        }
    //        return false;
    //    }
    //
    //    private boolean isSRATypeRemove(ApplicationTO applicationTO, Application application) {
    //        return application.getSraApplicationType() != null && applicationTO.getSraApplicationType() == null ? true : false;
    //    }

    @Override
    public ApplicationTO approveApplicationOnboard(Session session, String role, String actor, ApplicationTO applicationTO)
            throws AuthException {
        Map<String, String> camClientAttr = new HashMap<>();
        Application application;
        SRAApplicationSettingTO stagingSRAConsumerSetting = null;
        System.out.println("<<<<< Approved");
        System.out.println("<<<<< Approved ApplicationTO" + new Gson().toJson(applicationTO));
        if(applicationTO.getIsCredentialsEncrypted()!= null && applicationTO.getIsCredentialsEncrypted().equals(Boolean.TRUE)) {
            try {
                String  decryptedApplicationPassword = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), applicationTO.getPassword());
                applicationTO.setPassword(decryptedApplicationPassword);
            }catch (Exception e){
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_ID_OR_PASSWORD());
            }
        }
        applicationTO.setAlgorithm(applicationTO.getAlgorithm() != null ? applicationTO.getAlgorithm() : Config.getInstance().getProperty(Constant.DEFAULT_TOTP_HASH_ALGORITHM));
        applicationTO.setNumberOfDigits(applicationTO.getNumberOfDigits() != null ? applicationTO.getNumberOfDigits() : Integer.parseInt(Config.getInstance().getProperty(Constant.DEFAULT_TOTP_NUMBER_OF_DIGITS)));
        applicationTO.setTotpExpiry(applicationTO.getTotpExpiry() != null ? applicationTO.getTotpExpiry() : Long.parseLong(Config.getInstance().getProperty(Constant.DEFAULT_TOTP_EXPIRY_IN_SEC)));
        onboardApplication(applicationTO,session);
        boolean isCamEnabled = applicationTO.getCamEnabled() != null && applicationTO.getCamEnabled();
        if(isCamEnabled){
            ClientTO clientTO = new ClientTO();
            clientTO.setClientId(applicationTO.getApplicationId());
            String decryptedApplicationSecret = applicationTO.getApplicationSecret();

            if(applicationTO.getIsCredentialsEncrypted()!= null && applicationTO.getIsCredentialsEncrypted().equals(Boolean.TRUE)) {
                decryptedApplicationSecret = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), applicationTO.getApplicationSecret());
            }

            clientTO.setSecret(SHAImpl.hashData256(decryptedApplicationSecret));
            clientTO.setClientName(applicationTO.getApplicationName());
            clientTO.setClientDescription(applicationTO.getDescription());
            clientTO.setIsEnabled(true);
            //take it from config if null
            String accessTokenLifeSpan = Objects.isNull(applicationTO.getAccessTokenTimeoutInSeconds()) ?
                    Config.getInstance().getProperty(Constant.ACCESS_TOKEN_LIFESPAN) : String.valueOf(applicationTO.getAccessTokenTimeoutInSeconds());

            camClientAttr.put(Constant.ACCESS_TOKEN_LIFESPAN, accessTokenLifeSpan);
            camClientAttr.put(Constant.USE_REFRESH_CLIENT_CREDENTIAL, "true");
            if(applicationTO.getRefreshTokenTimeoutInSeconds()!=null){
                camClientAttr.put(Constant.CLIENT_SESSION_MAX_LIFESPAN,String.valueOf(applicationTO.getRefreshTokenTimeoutInSeconds()));
                camClientAttr.put(Constant.CLIENT_SESSION_IDLE_TIMEOUT,String.valueOf(applicationTO.getRefreshTokenTimeoutInSeconds()));
            }
            clientTO.setAttributes(camClientAttr);
            Client createdClient = camClientFacade.onboardClient(Config.getInstance().getProperty(Constant.CAM_REALM), clientTO);
            application = applicationService.onboardApplicationV2(session, applicationTO, createdClient);
        }
        else {
            application = applicationService.onboardApplication(session, applicationTO);
        }
        stagingSRAConsumerSetting = updateSRAApplicationSettings(applicationTO, application);
        System.out.println(stagingSRAConsumerSetting);
        if (applicationTO.getGatewayName() != null) {
            System.out.println("<<<<< stagingSRAConsumerSetting not null");
            applicationService.updateSRAConsumerSettings(session, stagingSRAConsumerSetting, application);
        }
        if(applicationTO.getIsFcmMultiDeviceAllowed()!=null && applicationTO.getIsFcmMultiDeviceAllowed()){
            applicationService.addFCMMultiDeviceDetails(applicationTO);
        }
        applicationTO.setStatus(Constant.SUCCESS_STATUS);
        AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "application onboarded  successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);


        return applicationTO;
    }

    @Override
    public ApplicationTO approveApplicationEdit(Session session, String role, String actor, ApplicationTO applicationTO) throws AuthException {
        Application application;
        try {
            AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "application edit request approved ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);
            Application applicationTemp;
            applicationTemp = applicationDao.getApplicationByIdWithCallbackUrl(applicationTO.getApplicationId());
            if (!applicationTemp.getApplicationName().equals(applicationTO.getApplicationName())) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
            }

            if (applicationTemp.getKcId() != null) {
                if(applicationTO.getApplicationSecret() != null || applicationTO.getAccessTokenTimeoutInSeconds() != null || applicationTO.getRefreshTokenTimeoutInSeconds()!=null) {
                    Map<String, String> camClientAttr = new HashMap<>();
                    ClientTO clientTO = new ClientTO();
                    clientTO.setClientId(applicationTO.getApplicationId());
                    clientTO.setClientName(applicationTO.getApplicationName());
                    if(applicationTO.getApplicationSecret() != null) {
                        clientTO.setSecret(applicationTO.getApplicationSecret());
                    }
                    clientTO.setIsEnabled(true);
                    if(applicationTO.getAccessTokenTimeoutInSeconds() != null) {
                        camClientAttr.put(Constant.ACCESS_TOKEN_LIFESPAN, String.valueOf(applicationTO.getAccessTokenTimeoutInSeconds()));
                    }
                    if(applicationTO.getRefreshTokenTimeoutInSeconds()!=null){
                        camClientAttr.put(Constant.CLIENT_SESSION_MAX_LIFESPAN,String.valueOf(applicationTO.getRefreshTokenTimeoutInSeconds()));
                        camClientAttr.put(Constant.CLIENT_SESSION_IDLE_TIMEOUT,String.valueOf(applicationTO.getRefreshTokenTimeoutInSeconds()));
                    }
                    camClientAttr.put(Constant.USE_REFRESH_CLIENT_CREDENTIAL, "true");
                    clientTO.setAttributes(camClientAttr);
                    camClientFacade.editClient(Config.getInstance().getProperty(Constant.CAM_REALM), applicationTemp.getKcId(), clientTO);
                }
            }
            application = applicationService.editApplicationv2(session, applicationTO);
        }
        catch (ApplicationNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        }
        StagingSRAProviderSettingTO stagingSRAProviderSetting = null;
        SRAApplicationSettingTO stagingSRAConsumerSetting = null;
        if (applicationTO.getActionType() != null && !applicationTO.getActionType().equals(Constant.DELETE)) {
            stagingSRAConsumerSetting = updateSRAApplicationSettings(applicationTO, application);
        }
        applicationService.editSRASettings(session, stagingSRAConsumerSetting, stagingSRAProviderSetting, application, applicationTO);
        if(applicationTO.getIsFcmMultiDeviceAllowed()!=null){
            applicationService.editFcmNotificationDetails(applicationTO);
        }
        applicationTO.setStatus(Constant.SUCCESS_STATUS);
        AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "application edited successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);

        return applicationTO;
    }

    private SRAApplicationSettingTO updateSRAApplicationSettings(ApplicationTO applicationTO, Application application) throws AuthException {
        SRAApplicationSettingTO sraApplicationSettingTO = null;
        if (applicationTO.getUrl() != null && !applicationTO.getUrl().isEmpty()) {
            sraApplicationSettingTO = new SRAApplicationSettingTO();
            sraApplicationSettingTO.setApplicationAccountId(applicationTO.getApplicationAccountId());
            sraApplicationSettingTO.setReferenceId(applicationTO.getId());
            sraApplicationSettingTO.setGatewayName(applicationTO.getGatewayName());
            sraApplicationSettingTO.setUrl(applicationTO.getUrl());
            sraApplicationSettingTO.setExternalAddress(applicationTO.getExternalAddress());
            sraApplicationSettingTO.setExternalPort(applicationTO.getExternalPort());
            sraApplicationSettingTO.setInternalAddress(applicationTO.getInternalAddress());
            sraApplicationSettingTO.setInternalPort(applicationTO.getInternalPort());
            sraApplicationSettingTO.setProtocol(applicationTO.getProtocol());
           sraApplicationSettingTO.setWhiteListedURLs(applicationTO.getWhiteListedURLs());

            if (Boolean.TRUE.equals(applicationTO.getPortForwardingFacade())) {
                sraApplicationSettingTO.setDefaultLocalPort(applicationTO.getDefaultLocalPort());
            }
            sraApplicationSettingTO.setPortForwardingFacade(applicationTO.getPortForwardingFacade());
            applicationService.updateSRAConsumerSettingsOnServer(sraApplicationSettingTO, application);
            sraApplicationSettingTO.setApplicationAccountId(application.getApplicationAccountId());
        }
        return sraApplicationSettingTO;
    }

    private void onboardApplication(ApplicationTO applicationRequestTO, Session session) throws AuthException {
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        Token token = null;
        SequenceStoreTO sequenceStoreTO = null;
        try {
            token = iamExtensionService.getToken(iamExtension);
            sequenceStoreTO = iamExtension.createSequence(applicationRequestTO.getEnterpriseId(), token);
            System.out.println("<<<<< sequenceStoreTO : " + new Gson().toJson(sequenceStoreTO));
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, Long.valueOf(e.getErrorCode()), e.getMessage());
        }
        try {
            String applicationId = generateApplicationId(applicationRequestTO.getEnterpriseId(), sequenceStoreTO.getCount());
            System.out.println("<<<<< applicationId : " + applicationId);
            applicationRequestTO.setApplicationId(applicationId);
            EnterpriseWE enterpriseWE = iamExtensionService.getEnterprise();
            System.out.println("<<<<< enterpriseWE : " + new Gson().toJson(enterpriseWE));
            applicationRequestTO.setEnterpriseAccountId(enterpriseWE.getEnterpriseView().getEnterpriseAccountId());
            AccountWE accountWE = createAccount(Constant.USER_ID, applicationId, AccountType.APPLICATION, iamExtension);
            System.out.println("<<<<< enterpriseWE : " + new Gson().toJson(accountWE));
            if (accountWE.getId() != null) {
                applicationRequestTO.setApplicationAccountId(accountWE.getId());
                editEnterprise(enterpriseWE, applicationRequestTO, accountWE.getId(), iamExtension, token);
                System.out.println("<<<<< Edit Enterprise Success ");
                editAccount(accountWE.getId(), applicationRequestTO, iamExtension, token);
                System.out.println("<<<<< Edit account Success ");

                accountWE.setCryptoDID(iamExtensionService.onboardApplication(applicationId, accountWE.getId(), applicationRequestTO.getPassword(), CryptoEntityType.ENTITY_APPLICATION));
                logger.log(Level.DEBUG, "Application Did = " + accountWE.getCryptoDID());
                activateAccount(accountWE, applicationRequestTO, iamExtension, token);
                System.out.println("<<<<< activate account Success ");
                //                generateEsc(accountWE.getId(), applicationRequestTO, iamExtension);
                System.out.println("<<<<< generate esc Success ");
            }
        } finally {
            try {
                iamExtension.markSequenceAllocated(sequenceStoreTO, token);
                logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " Sequence: " + sequenceStoreTO.getCount() + " allocated ");
            }
            catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
    }

    private AccountWE createAccount(String atributeName, String attributeValue, AccountType accountType, IAMExtensionV2 iamExtension) throws AuthException {
        try {
            return iamExtension.createAccountIfNotExist(atributeName, attributeValue, accountType);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), e.getMessage());
        }
    }

    private AccountWE editAccount(String accountId, ApplicationTO applicationRequestTO, IAMExtensionV2 iamExtension, Token token) throws AuthException {
        AccountWE weEditAccount = new AccountWE();
        List<in.fortytwo42.enterprise.extension.tos.AttributeTO> identifiers = new ArrayList<>();
        in.fortytwo42.enterprise.extension.tos.AttributeTO attribute = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        attribute.setAttributeName(Constant.USER_ID);
        attribute.setAttributeValue(applicationRequestTO.getApplicationId());
        identifiers.add(attribute);
        weEditAccount.setAttributes(identifiers);
        weEditAccount.setAccountType(AccountType.APPLICATION);
        //weEditAccount.setAccountAccessStatus(Constant.OPEN_ACCOUNT_ACCESS_STATUS);
        weEditAccount.setUserCredential(applicationRequestTO.getPassword());
        List<QuestionAnswerTO> questionAnswers = new ArrayList<>();
        QuestionAnswerTO cityQuestion = new QuestionAnswerTO();
        cityQuestion.setQuestion("What city were you born in?");
        cityQuestion.setAnswer("Pune");
        questionAnswers.add(cityQuestion);
        QuestionAnswerTO middleNameQuestion = new QuestionAnswerTO();
        middleNameQuestion.setQuestion("What is your mothers maiden name?");
        middleNameQuestion.setAnswer("Pune");
        questionAnswers.add(middleNameQuestion);
        weEditAccount.setQuestionAnswers(questionAnswers);
        try {
            return iamExtension.editAccount(weEditAccount, accountId, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), errorConstant.getERROR_MESSAGE_EDIT_ACCOUNT_FAILED());
        }
    }

    private AccountWE activateAccount(AccountWE accountWE, ApplicationTO applicationRequestTO, IAMExtensionV2 iamExtension, Token token) throws AuthException {
        AccountWE weEditAccount = new AccountWE();
        List<in.fortytwo42.enterprise.extension.tos.AttributeTO> identifiers = new ArrayList<>();
        in.fortytwo42.enterprise.extension.tos.AttributeTO attribute = new in.fortytwo42.enterprise.extension.tos.AttributeTO();
        attribute.setAttributeName(Constant.USER_ID);
        attribute.setAttributeValue(applicationRequestTO.getApplicationId());
        identifiers.add(attribute);
        weEditAccount.setAttributes(identifiers);
        weEditAccount.setState(Constant.ACTIVE);
        weEditAccount.setCryptoDID(accountWE.getCryptoDID());
        try {
            return iamExtension.editAccount(weEditAccount, accountWE.getId(), token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), errorConstant.getERROR_MESSAGE_EDIT_ACCOUNT_FAILED());
        }
    }

    private void generateEsc(String accountId, ApplicationTO applicationRequestTO, IAMExtensionV2 iamExtension) throws AuthException {
        EscTO generateEscRequest = new EscTO();
        generateEscRequest.setAccountId(accountId);
        generateEscRequest.setEnterpriseId(applicationRequestTO.getEnterpriseId());
        generateEscRequest.setAccountType(AccountType.APPLICATION.name());
        System.out.println("Generate Esc password " + applicationRequestTO.getPassword());
        generateEscRequest.setPassword(applicationRequestTO.getPassword());
        generateEscRequest.setApplicationId(applicationRequestTO.getApplicationId());
        try {
            iamExtension.generateEsc(generateEscRequest);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            //throw new AuthException(e, errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED, errorConstant.getERROR_MESSAGE_EDIT_ACCOUNT_FAILED);
        }
    }

    private String generateApplicationId(String enterpriseId, int sequenceId) {
        return enterpriseId + StringUtil.paddLeft(String.valueOf(sequenceId), 4, '0');
    }

    private EnterpriseWE editEnterprise(EnterpriseWE enterpriseWE, ApplicationTO applicationRequestTO, String accountId, IAMExtensionV2 iamExtension,
            Token token) {
        EnterpriseWE editEnterpriseWE = new EnterpriseWE();
        editEnterpriseWE.setId(enterpriseWE.getId());
        editEnterpriseWE.setVersion((enterpriseWE.getVersion() + 1));
        EnterpriseWE.EnterpriseData enterpriseData = new EnterpriseWE.EnterpriseData();
        enterpriseData.setDomain(enterpriseWE.getEnterpriseView().getDomain());
        enterpriseData.setActivationDate(enterpriseWE.getEnterpriseView().getActivationDate());
        enterpriseData.setExpirationDate(enterpriseWE.getEnterpriseView().getExpirationDate());
        enterpriseData.setEnterpriseId(enterpriseWE.getEnterpriseView().getEnterpriseId());
        enterpriseData.setEnterpriseName(enterpriseWE.getEnterpriseView().getEnterpriseName());
        enterpriseData.setEnterpriseAccountId(enterpriseWE.getEnterpriseView().getEnterpriseAccountId());
        enterpriseData.setStatus(enterpriseWE.getEnterpriseView().getStatus());

        List<in.fortytwo42.enterprise.extension.tos.ApplicationTO> applications = new ArrayList<>();
        in.fortytwo42.enterprise.extension.tos.ApplicationTO application = new in.fortytwo42.enterprise.extension.tos.ApplicationTO();
        application.setActivationDate(applicationRequestTO.getActivationDate());
        application.setStatus(Status.ACTIVE.name());
        application.setExpirationDate(applicationRequestTO.getExpirationDate());
        application.setApplicationName(applicationRequestTO.getApplicationName());
        application.setApplicationId(applicationRequestTO.getApplicationId());
        application.setApplicationAccountId(accountId);
        application.setCallBackUrl(applicationRequestTO.getCallbackUrl());
        application.setIsNotificationEnabled(applicationRequestTO.getIsNotificationEnabled());
        application.setQueueName(applicationRequestTO.getQueueName());
        if(ResetPinUserUnblockStatus.AUTO_UNBLOCK.toString().equals(applicationRequestTO.getResetPinUserUnblockSetting())) {
            application.setIsAutoResetEnabled(Boolean.TRUE);
        }
        List<ServiceTO> services = new ArrayList<>();
        for (in.fortytwo42.tos.transferobj.ServiceTO adapterServiceTO : applicationRequestTO.getServices()) {
            ServiceTO serviceTO = new ServiceTO();
            serviceTO.setServiceName(adapterServiceTO.getServiceName());
            int index = enterpriseWE.getEnterpriseView().getServices().indexOf(serviceTO);
            if (index >= 0) {
                ServiceTO service = enterpriseWE.getEnterpriseView().getServices().get(index);
                services.add(service);
            }
        }
        application.setServices(services);
        applications.add(application);
        enterpriseData.setApplications(applications);
        editEnterpriseWE.setEnterpriseView(enterpriseData);
        EnterpriseWE editEnterprise = null;
        try {
            editEnterprise = iamExtension.editEnterprise(editEnterpriseWE, token);
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return editEnterprise;
    }

    @Override
    public PaginatedTO<RequestTO> getApplicationAuditTrails(int page, String searchText, Long fromDate, Long toDate, String role) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " getApplicationAuditTrails : start");
        permissionUtil.validateGetApplicationAuditTrailPermission(role);
        List<Request> requests = requestService.getPaginatedRequests(RequestType.APPLICATION, page, Integer.parseInt(config.getProperty(Constant.LIMIT)),
                searchText,
                fromDate, toDate);
        List<RequestTO> applicationList = new EntityToTOConverter<Request, RequestTO>().convertEntityListToTOList(requests);
        PaginatedTO<RequestTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(applicationList);
        //        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " getApplicationAuditTrails : end");
        return paginatedTO;
    }

    @Override
    public List<ApplicationTO> getSRAApplications(String SRAApplicationType, String role) throws AuthException {
        return applicationService.getSRAApplications(SRAApplicationType, role);
    }

    @Override
    public RemoteAccessSettingTO getRemoteAccessSettings(RemoteAccessSettingTO remoteAccessSettingTO) throws AuthException {
        return applicationService.getRemoteAccessSettings(remoteAccessSettingTO);
    }

    @Override
    public ApplicationTO deleteSRAApplicationSetting(String actor, String role, ApplicationTO applicationTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " deleteSRAApplicationSetting : start");
        SRAApplicationGatewayRel sraApplicationGatewayRel = null;
        Application application = applicationService.getApplicationByApplicationId(applicationTO.getApplicationId());
        sraApplicationGatewayRel = sraApplicationGatewayRelDao.getSRAApplicationGatewayRel(application);
        if (sraApplicationGatewayRel == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SRA_APPLICATION_GATEWAY_SETTING_REL_NOT_FOUND());
        }
        Session session = sessionFactoryUtil.getSession();
        try {
            if (saveRequest) {

                applicationTO = applicationService.createSRAApplicationGatewayRelDeleteRequest(session, applicationTO, actor);
            }
            else {
                applicationTO = applicationService.approveDeleteSRAApplicationSetting(session, applicationTO);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " deleteSRAApplicationSetting : end");
        return applicationTO;
    }

    @Override
    public boolean isSRADetailsMatch(String senderAccountId, String host, Integer port) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " isSRADetailsMatch : start");
        boolean isSRADetailsMatch = false;
        try {
            User sender = ServiceFactory.getUserService().getUserByAccountId(senderAccountId);
            List<UserApplicationServiceRel> userApplicationServiceRels = ServiceFactory.getUserApplicationRelService().getUserApplicationRel(sender.getId());
            for (UserApplicationServiceRel userApplicationServiceRel : userApplicationServiceRels) {
                Application application = userApplicationServiceRel.getId().getApplication();
                logger.log(Level.DEBUG, "Id : " + application.getId());
                try {
                    List<SRAApplicationSetting> sraApplicationSettings = DaoFactory.getSRAApplicationSetting().getSettingsByApplicationIdHostAndPort(application, host, port);
                    if (sraApplicationSettings != null && !sraApplicationSettings.isEmpty()) {
                        isSRADetailsMatch = true;
                        for (SRAApplicationSetting sraApplicationSetting : sraApplicationSettings) {
                            logger.log(Level.DEBUG, "<<<<< isSRADetailsMatch : " + sraApplicationSetting.getId());
                        }
                        logger.log(Level.DEBUG, "<<<<< isSRADetailsMatch : " + application.getId() + " - " + isSRADetailsMatch);
                        break;
                    }
                }
                catch (NotFoundException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }

        }
        catch (UserNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " isSRADetailsMatch : end");
        return isSRADetailsMatch;
    }

    @Override
    public CSVUploadTO uploadOnboardApplication(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("YYYYMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("IST"));
        String dateFormatted = formatter.format(date);
        String requestId = UUID.randomUUID().toString();
        String filename = fileName.split(".csv")[0] + "_" + dateFormatted + "_" + requestId + ".csv";
        CSVUploadTO csvUploadTO = new CSVUploadTO();
        csvUploadTO.setRequestId(requestId);
        csvUploadTO.setFileName(filename);
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            try {
                OnboardApplicationCsv.getInstance().processCSV(inputStream, role, username,id, filename);
            } catch (AuthException e) {
                new FileDownloader().writeFile(fileName, e);
                throw new RuntimeException(e);
            }
        });
        return csvUploadTO;
    }
    @Override
    public String readSampleCsvFile(String fileName) {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " readSampleCsvFile : start");
        fileName = fileName != null ? fileName : "application-onboard.csv";
        String content = FileUtil.getSampleUserOnboardCsv(fileName);
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " readSampleCsvFile : end");
        return content;
    }

    @Override
    public String downloadUpdateApplicationStatus(String fileName, String role) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " downloadUpdateApplicationStatus : start");
        String content = new FileDownloader().downloadCSVStatusFile(fileName, role);
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " downloadUpdateApplicationStatus : end");
        return content;
    }
    @Override
    public String generateRunningHash(String applicationId) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " generateRunningHash : start");
        try {
            Application application = applicationDao.getApplicationByApplicationId(applicationId);
            String applicationSecretePlainText = AES128Impl.decryptData(application.getApplicationSecret(), KeyManagementUtil.getAESKey());
            String randomSalt = RandomString.nextString(20);
            applicationSecretePlainText = applicationSecretePlainText + randomSalt;
            MessageDigest messageDigest;
            try {
                messageDigest = MessageDigest.getInstance("SHA-256");
            }
            catch (NoSuchAlgorithmException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                throw new UndeclaredThrowableException(e);
            }
            String generatedHash = Base64.getEncoder().encodeToString(messageDigest.digest(applicationSecretePlainText.getBytes()));
            String runningHash = randomSalt + generatedHash;
            logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " RunningHash : "+ runningHash);
            return runningHash;
        }
        catch (ApplicationNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        } finally {
            logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " generateRunningHash : end");
        }
    }

    @Override
    public boolean verifyRunningHash(String applicationId, RunningHashTo runningHashTo) throws AuthException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " verifyRunningHash : start");
        try {
            Application application = applicationDao.getApplicationByApplicationId(applicationId);
            int saltSize;
            try {
                saltSize = Integer.parseInt(Config.getInstance().getProperty(Constant.SALT_SIZE));
            } catch (NumberFormatException e) {
                saltSize = 20;
            }
            String receivedApplicationSecret = runningHashTo.getRunningHash();
            if(receivedApplicationSecret.length() > saltSize) {
                String salt = receivedApplicationSecret.substring(0, saltSize);
                String hashedSecret = receivedApplicationSecret.substring(saltSize);
                String decryptedSecret = AES128Impl.decryptData(application.getApplicationSecret(), KeyManagementUtil.getAESKey());
                logger.log(Level.DEBUG, "salt : " + salt + " decryptedSecret :" + decryptedSecret);
                String generatedHash = SHAImpl.hashData256(decryptedSecret + salt);
                logger.log(Level.DEBUG, "hashedSecret : " + hashedSecret + " generatedHash :" + generatedHash);
                boolean isSecreteValid = hashedSecret.equals(generatedHash);
                logger.log(Level.DEBUG, "isSecreteValid >>" + isSecreteValid);
                if(!isSecreteValid){
                    throw new AuthException(null, errorConstant.getERROR_CODE_VERIFY_RUNNING_HASH_FAILED(), errorConstant.getERROR_MESSAGE_INVALID_RUNNING_HASH());
                }
                return true;
            } else {
                throw new AuthException(null, errorConstant.getERROR_CODE_VERIFY_RUNNING_HASH_FAILED(), errorConstant.getERROR_MESSAGE_INVALID_RUNNING_HASH());
            }
        }
        catch (ApplicationNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
        } finally {
            logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " verifyRunningHash : end");
        }
    }

    @Override
    public TotpSettingsTO getTotpSettingsByApplicationId(String applicationId) throws ApplicationNotFoundException {
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " getTotpSettingsByApplicationId : start");
        Application application = applicationDao.getApplicationByApplicationId(applicationId);
        TotpSettingsTO totpSettings=new TotpSettingsTO();
        totpSettings.setTotpExpiry(application.getTotpExpiry().toString());
        totpSettings.setTotpNoOfDigits(application.getNumberOfDigits().toString());
        String hashingAlgorithm = HashingAlgorithm.valueOf(application.getAlgorithm().name()).getHmacAlgorithm();
        totpSettings.setTotpHashingAlgorithm(hashingAlgorithm);
        totpSettings.setApplicationId(applicationId);
        logger.log(Level.DEBUG, APPLICATION_FACADE_IMPL_LOG + " getTotpSettingsByApplicationId : end");
        return totpSettings;
    }
}
