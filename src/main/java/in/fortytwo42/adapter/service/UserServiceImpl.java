
package in.fortytwo42.adapter.service;

import javax.ws.rs.ProcessingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.itzmeds.adfs.client.SignOnException;

import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.cam.util.CamUtil;
import in.fortytwo42.adapter.enums.TransactionApprovalStatus;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.adapter.transferobj.BlockUserApplicationTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.transferobj.UserStatusTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AdfsUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.AuthenticationAttemptDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.dao.RoleDaoIntf;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.dao.UserAuthPrincipalDaoImpl;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.dao.UserSessionTokenDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.exception.UserApplicationRelNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.enterprise.extension.core.BindingInfoV2;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.bean.Role;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceCompositeKey;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.bean.UserAuthPrincipal;
import in.fortytwo42.entities.bean.UserSessionToken;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.IAMStatus;
import in.fortytwo42.entities.enums.OnboardStatus;
import in.fortytwo42.entities.enums.RequestSubType;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.entities.enums.UserState;
import in.fortytwo42.entities.enums.UserStatus;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class UserServiceImpl implements UserServiceIntf {

    private static final String USER_BINDING = "User binding~";
    private UserApplicationRelDaoIntf userApplicationRelDao = DaoFactory.getUserApplicationRel();
    private UserDaoIntf userDao = DaoFactory.getUserDao();
    private AuthenticationAttemptDaoIntf authenticationAttemptDao = DaoFactory.getAuthenticationAttemptDao();
    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    private RequestDaoIntf requestDao = DaoFactory.getRequestDao();
    private RoleDaoIntf roleDao = DaoFactory.getRoleDao();
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    private ServiceProcessorIntf serviceProcessor = ServiceFactory.getServiceProcessor();
    private UserSessionTokenDaoIntf userSessionTokenDao = DaoFactory.getUserSessionTokenDao();
    private PermissionServiceIntf permissionService = ServiceFactory.getPermissionService();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private static final String USER_SERVICE_IMPL_LOG = "<<<<< UserServiceImpl";
    private Logger logger=LogManager.getLogger(UserServiceImpl.class);
    /**
     * creation of log 4j object for each class
     */


    private IAMUtil iamUtil = IAMUtil.getInstance();


    //TODO: Service to Service
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private Config config = Config.getInstance();

    private static final class InstanceHolder {
        private static final UserServiceImpl INSTANCE = new UserServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static UserServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public boolean isMobileApplicationBindingPresent(String mobile, Application application) {
        return userApplicationRelDao.getMobileAndApplicationRelCount(application.getId(), mobile) > 0;
    }

    @Override
    public void bulkUpdateUserApplicationRel(Session session, List<UserApplicationServiceRel> userApplicationRelsToUpdate) {
        userApplicationRelDao.bulkUpdate(session, userApplicationRelsToUpdate);
    }

    @Override
    public User createUser(Session session, String accountId, UserRole role, String twoFactorStatus, User user) throws AuthException {
        TwoFactorStatus userTwoFactorStatus = twoFactorStatus != null
                                              && TwoFactorStatus.valueOf(twoFactorStatus) != null ? TwoFactorStatus.valueOf(twoFactorStatus)
                                                                                                  : TwoFactorStatus.ENABLED;
        user.setTwoFactorStatus(userTwoFactorStatus);
        user.setUserStatus(UserStatus.ACTIVE);
        user.setIamStatus(IAMStatus.DISABLED);
        user.setAccountId(accountId);
        if(!role.equals(UserRole.USER) ){
            user.setUserState(UserState.A);
        }else {
            user.setUserState(UserState.D);
        }
        Role roleDb;
        try {
            roleDb = roleDao.getRoleByName(role, session);
            Set<Role> roles = new HashSet<>();
            roles.add(roleDb);
            user.setRoles(roles);
            user = userDao.create(session, user);
            return user;
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_ROLES(), errorConstant.getERROR_MESSAGE_INVALID_ADMIN_ROLE());
        }
    }

    public User createUserV2(Session session, String accountId, UserRole role, String twoFactorStatus, UserResponseDto camUser) throws AuthException {
        User user = new User();
        TwoFactorStatus userTwoFactorStatus = twoFactorStatus != null
                                              && TwoFactorStatus.valueOf(twoFactorStatus) != null ? TwoFactorStatus.valueOf(twoFactorStatus)
                                                                                                  : TwoFactorStatus.ENABLED;
        user.setTwoFactorStatus(userTwoFactorStatus);
        user.setUserStatus(UserStatus.ACTIVE);
        user.setIamStatus(IAMStatus.DISABLED);
        user.setAccountId(accountId);
        Role roleDb;
        try {
            roleDb = roleDao.getRoleByName(role, session);
            Set<Role> roles = new HashSet<>();
            roles.add(roleDb);

            if(camUser.getUserKcId() != null){
                user.setKcId(camUser.getUserKcId());
                user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_COMPLETE.name());
            }
            else {
                user.setOnboardStatus(OnboardStatus.CAM_ONBOARD_FAILED.name());
            }

            user = userDao.create(session, user);
            user.setRoles(roles);
            user = updateUser(session, user);
            return user;
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_ROLES(), errorConstant.getERROR_MESSAGE_INVALID_ADMIN_ROLE());
        }
    }

    @Override
    public void saveAuthAttempt(Session session, AuthenticationAttempt authenticationAttempt) {
        authenticationAttemptDao.create(session, authenticationAttempt);
        logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, USER_BINDING, System.currentTimeMillis() + Constant.TILT,
                authenticationAttempt.getTransactionId(), "~Binding request saved db"));
    }

    @Override
    public User updateUser(Session session, User user) {
        return userDao.update(session, user);
    }

    @Override
    public void validateUser(User user) throws AuthException {
        if (user.getUserStatus() == UserStatus.BLOCK) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_BLOCK(), errorConstant.getERROR_MESSAGE_USER_BLOCK());
        }
    }

    @Override
    public User getActiveUser(String accountId) throws AuthException {
        try {
            return userDao.getActiveUserByAccountId(accountId);
        }
        catch (UserNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
    }

    @Override
    public User getActiveUserForAuthAttempt(String accountId) throws AuthException {
        try {
            return userDao.getActiveUserByAccountIdForAuthAttempt(accountId);
        }
        catch (UserNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
    }
    @Override
    public User getActiveUser(Session session, String accountId) throws AuthException {
        try {
            return userDao.getActiveUserByAccountId(session, accountId);
        }
        catch (UserNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
    }

    @Override
    public List<User> getActiveUsers(String userTypeFilter, String iamStatusFilter, String userStatusFilter, String _2faStatusFilter, int page, int limit, Long fromDate, Long toDate)
            throws AuthException {
        List<User> users = userDao.getActiveUsers(userTypeFilter, iamStatusFilter, userStatusFilter, _2faStatusFilter, page, Integer.parseInt(config.getProperty(Constant.LIMIT)),
                fromDate, toDate);
        if (users.isEmpty() && page == 1) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_DATA_EMPTY(), errorConstant.getERROR_MESSAGE_USER_DATA_EMPTY());
        }
        return users;
    }

    public String getToken(Long ID,String username, String roles, String permissions, String enterpriseAccountId,
                            String userAgent, String ipAddress) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(Constant.USER_NAME, username);
        payload.put(Constant.ID,ID);
        payload.put(Constant.STATE, Constant.SUCCESS_STATUS);
        payload.put(Constant.ROLE, roles);
        payload.put(Constant.PERMISSIONS, permissions);
        payload.put(Constant.HEADER_ENTERPRISE_ACCOUNT_ID, enterpriseAccountId);
        payload.put(Constant.USER_AGENT, userAgent);
        payload.put(Constant.IP_ADDRESS, ipAddress);
        String issuer = config.getProperty(Constant.ISSUER);
        String user = config.getProperty(Constant.TOKEN_USER);
        String tokenValidity = config.getProperty(Constant.TOKEN_VALIDITY);
        return JWTokenImpl.generateToken(KeyManagementUtil.getAESKey(), Integer.toString(new Random().nextInt()), issuer, user, payload, Long.valueOf(tokenValidity));
    }

    @Override
    public User getActiveUser(Long userId) throws AuthException {
        try {
            return userDao.getActiveById(userId);
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
    }

    @Override
    public User getActiveUser(Session session, Long userId) throws AuthException {
        try {
            return userDao.getActiveUserById(session, userId);
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
    }

    @Override
    public void updateIAMStatusToDisabled(Session session, User user) {
        List<UserApplicationServiceRel> adUserUpdatedApplicationRels = userApplicationRelDao.getApplicationRelsForUser(user.getId());
        if (adUserUpdatedApplicationRels == null || adUserUpdatedApplicationRels.isEmpty()) {
            user.setIamStatus(IAMStatus.DISABLED);
            userDao.update(session, user);
        }
    }

    @Override
    public void updateIamStatusToEnabled(Session session, User user) {
        if (user.getIamStatus() != IAMStatus.ENABLED) {
            user.setIamStatus(IAMStatus.ENABLED);
            userDao.update(session, user);
        }
    }

    @Override
    public boolean isUserGroupRelPresent(Long userId, Long groupId) {
        return userDao.isUserGroupRelPresent(userId, groupId);
    }

    @Override
    public void updateAllUsers(Session session, TwoFactorStatus twoFactorStatus) {
        // TODO Auto-generated method stub

    }

    @Override
    public User getNonADUser(String username, String mobile) throws UserNotFoundException {
        return userDao.getNonADUser(username, mobile);
    }

    @Override
    public List<User> getNonADUsersByAccountId(String accountId) throws AuthException {
        try {
            return userDao.getUsersByAccountId(accountId);
        }
        catch (UserNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
    }

    @Override
    public User authenticate(String userId, String password) throws AuthException {
        try {
            User user = attributeStoreDao.getUserByAttributeValue(userId);
            String decryptedUserPassword;
            try {
                decryptedUserPassword = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), password);
                System.out.println("Decrypted User password " + decryptedUserPassword);
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                decryptedUserPassword = password+"guhgwd23623";
            }
            if (!Constant.isMock) {

                String hashedPassword = StringUtil.getHex(
                        SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, user.getAccountId(), decryptedUserPassword).getBytes()));
                iamExtensionService.authenticateUser(userId, hashedPassword);

                //                if (!AES128Impl.encryptData(decryptedUserPassword, KeyManagementUtil.getAESKey()).equals(userCredential.getPassword())) {
                //                    throw new AuthException(null, errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID, errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID);
                //                }
            }
            return user;
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID());
        }
    }

    @Override
    public AdfsDetailsTO authenticateADUser(String userId, String password) throws AuthException {
        String decryptedUserPassword;
        try {
            decryptedUserPassword = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), password);
            System.out.println("Decrypted User password " + decryptedUserPassword);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            decryptedUserPassword = password + "guhgwd23623";
        }
        if (!Constant.isMock) {
            return authenticateUserWithAdfs(userId, decryptedUserPassword);
        }
        return null;
    }

    @Override
    public void authenticateNonADUser(User user, String userId, String password) throws AuthException {
        String decryptedUserPassword;
        try {
            decryptedUserPassword = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), password);
            System.out.println("Decrypted User password " + decryptedUserPassword);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            decryptedUserPassword = password + "guhgwd23623";
        }
        if (!Constant.isMock) {
            String hashedPassword = StringUtil.getHex(
                    SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, user.getAccountId(), decryptedUserPassword).getBytes()));
            iamExtensionService.authenticateUser(userId, hashedPassword);
        }
    }

    @Override
    public UserStatusTO createUserStatusUpdateRequest(Session session, UserStatusTO userStatusTO, String actor) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(userStatusTO));
        request.setRequestorComments(userStatusTO.getComments());
        request.setRequestType(RequestType.USER);
        request.setRequestSubType(RequestSubType.USER_STATUS_UPDATE);
        try {
            request.setRequestor(attributeStoreDao.getUserByAttributeValue(actor));
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        userStatusTO.setStatus(Constant.SUCCESS_STATUS);
        userStatusTO.setId(createdRequest.getId());
        return userStatusTO;
    }

    @Override
    public UserStatusTO approveUserStatusUpdateRequest(Session session, Request request, UserStatusTO userStatusTO) throws AuthException {
        UserStatusTO userStatusRequestTO = new Gson().fromJson(request.getRequestJSON(), UserStatusTO.class);
        if (userStatusTO.getApprovalStatus().equals(Constant.APPROVED)) {
            iamExtensionService.updateUserStatus(userStatusRequestTO.getAccountId(), userStatusRequestTO.getState());
            request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
            BlockUserApplicationTO blockUserApplicationTO = new BlockUserApplicationTO();
            blockUserApplicationTO.setAccountId(userStatusRequestTO.getAccountId());
            blockUserApplicationTO.setStatus(userStatusRequestTO.getState());
            iamExtensionService.updateUserStatus(userStatusRequestTO.getAccountId(), userStatusRequestTO.getState());
        }
        else {
            request.setApprovalStatus(ApprovalStatus.REJECTED_BY_CHECKER);
        }
        requestDao.update(session, request);
        return userStatusRequestTO;
    }

    @Override
    public String getMobileNo(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.MOBILE_NO);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    @Override
    public String getUsername(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.USER_ID);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    @Override
    public String getEmail(Long userId) {
        AttributeStore attributeStore = attributeStoreDao.getAttribute(userId, Constant.EMAIL);
        if (attributeStore != null) {
            return attributeStore.getAttributeValue();
        }
        return null;
    }

    @Override
    public void autoBindUserToApplication(Session session, List<ApplicationTO> applications, User user, String camPassword) throws AuthException {
        try {
            for (ApplicationTO applicationTO : applications) {
                Application application = applicationService.getApplicationByApplicationId(applicationTO.getApplicationId());
                try {
                    CamUtil.onboardUserAndBind(application, user, camPassword, session);
                }catch (Exception e){
                    logger.log(Level.DEBUG, e.getMessage(), e);
                }

                boolean isAuth42ServicePresent = false;
                for (ServiceTO ServiceTO : applicationTO.getServices()) {
                    Service service = null;
                    try {
                        application = applicationService.getApplicationByApplicationId(applicationTO.getApplicationId());
                        service = serviceProcessor.getServiceByServiceName(ServiceTO.getServiceName());
                    }
                    catch (ServiceNotFoundException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                        throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
                    }
                    try {
                        System.out.println(application.getEnterprise());
                        IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
                        Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(),
                                AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
                        iamExtension.bindUserToApplication(user.getAccountId(), token);
                    }
                    catch (IAMException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                        throw new AuthException(e, errorConstant.getERROR_CODE_BINDING_FAILED(), e.getMessage());
                    }
                    UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
                    userApplicationCompositeKey.setApplication(application);
                    userApplicationCompositeKey.setService(service);
                    userApplicationCompositeKey.setUser(user);
                    UserApplicationServiceRel adUserApplicationRel = null;
                    try {
                        adUserApplicationRel = userApplicationRelDao.getUserApplicationForId(userApplicationCompositeKey, session);
                    }
                    catch (Exception e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                    boolean userUpdated = false;
                    if (adUserApplicationRel == null) {
                        adUserApplicationRel = new UserApplicationServiceRel();
                        adUserApplicationRel.setId(userApplicationCompositeKey);
                        adUserApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
                        adUserApplicationRel.setTwoFactorStatus(TwoFactorStatus.ENABLED);
                        userApplicationRelDao.create(session, adUserApplicationRel);
                    }
                    else if (BindingStatus.INACTIVE.equals(adUserApplicationRel.getBindingStatus())){
                        adUserApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
                        userApplicationRelDao.update(session, adUserApplicationRel);
                        User tempUser = adUserApplicationRel.getId().getUser();
                        tempUser.setIamStatus(IAMStatus.ENABLED);
                        userDao.update(session,tempUser);
                        userUpdated = true;
                    }
                    if(!userUpdated){
                        user.setIamStatus(IAMStatus.ENABLED);
                        userDao.update(session, user);
                    }
                    if (Constant.AUTH42.equals(service.getServiceName())) {
                        isAuth42ServicePresent = true;
                    }
                }
                logger.log(Level.DEBUG, "isAuth42ServicePresent : " + isAuth42ServicePresent);
                if (isAuth42ServicePresent) {
                    enableToken(user.getAccountId(), application, true);
                }
            }
        }
        catch (Exception e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_BINDING_FAILED(), e.getMessage());
        }

    }



    @Override
    public void logout(Session session, String username, String token, Long expiry) {
        UserSessionToken tokenToStore = new UserSessionToken();
        tokenToStore.setToken(token);
        tokenToStore.setExpiryTime(new Timestamp(expiry));
        userSessionTokenDao.create(session, tokenToStore);
        List<UserAuthPrincipal> userAuthPrincipals = UserAuthPrincipalDaoImpl.getInstance().expireUserSessions(username, Constant.ORIGIN);
        UserAuthPrincipalDaoImpl.getInstance().bulkDelete(session, userAuthPrincipals);
    }

    /**
     * Gets the users.
     *
     * @param userRole the user type
     * @param userUpdateStatus the user update status
     * @param page the page
     * @param searchText the search text
     * @param iamStatusFilter the iam status filter
     * @param userStatusFilter the user status filter
     * @param _2faStatusFilter the 2 fa status filter
     * @param approvalStatus the approval status
     * @param role the role
     * @return the users
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<UserTO> getUsers(UserRole userRole, String userUpdateStatus, int page, String searchText,String attributeName, String iamStatusFilter, String userStatusFilter, String _2faStatusFilter,
            String approvalStatus, String userState, String role, String userTypeFilter, Long userGroupId, Boolean export) throws AuthException {
        List<UserTO> userList;
        Long count;
        List<User> users = null;
        String accountIds = null;
        String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
        Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);

        if (userGroupId != null) {
            accountIds = ServiceFactory.getUserUserGroupRelService().getAccountIds(userGroupId);
        }
        logger.log(Level.DEBUG, " getTotalActiveCount : start " + accountIds);
        boolean useAttributeSearch = true;
        if(Config.getInstance().getProperty(Constant.USE_ATTRIBUTE_SEARCH)!=null) {
            try {
                useAttributeSearch = Boolean.parseBoolean(Config.getInstance().getProperty(Constant.USE_ATTRIBUTE_SEARCH));
            }
            catch (Exception e) {
                logger.log(Level.ERROR, USER_SERVICE_IMPL_LOG + " Use attribute search Not Found ");
            }
        }
        if(export){
            if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                users = userDao.getActivePaginatedList(null, null, searchText, iamStatusFilter, userStatusFilter,
                        _2faStatusFilter, userTypeFilter, accountIds, userState);
            }else{
                users = userDao.getActivePaginatedListWithLower(null, null, searchText, iamStatusFilter, userStatusFilter,
                        _2faStatusFilter, userTypeFilter, accountIds, userState);
            }
        }
        else{

            if(useAttributeSearch){
                List<User> usersT= new ArrayList<>();
                if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                    if(searchText != null && !searchText.isEmpty()){
                        searchText=searchText.toUpperCase();
                    }
                    usersT = attributeStoreDao.getAttributesBySearchUser(page, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)), searchText, attributeName, userTypeFilter, userState);
                }else {
                    usersT = attributeStoreDao.getAttributesBySearchUser(page, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)), searchText, attributeName, userTypeFilter, userState);
                }
                users = new ArrayList<>(usersT);
                //                List<AttributeStore> attributeStores = attributeStoreDao.getAttributesBySearch(page, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)), searchText);
               /* if(attributeStores!=null) {
                    for (AttributeStore attributeStore : attributeStores) {
                        User user = attributeStore.getUser();
                        if (!users.contains(user)) {
                            users.add(user);
                        }
                    }
                }*/
            } else {
                if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                    users = userDao.getActivePaginatedList(page, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)), searchText, iamStatusFilter, userStatusFilter,
                            _2faStatusFilter, userTypeFilter, accountIds, userState);
                }else {
                    users = userDao.getActivePaginatedListWithLower(page, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)), searchText, iamStatusFilter, userStatusFilter,
                            _2faStatusFilter, userTypeFilter, accountIds, userState);
                }
            }
        }

        if(useAttributeSearch) {
            if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                if(searchText != null && !searchText.isEmpty()){
                    searchText=searchText.toUpperCase();
                }
                count = attributeStoreDao.getTotalAttributesCount(searchText, attributeName, userTypeFilter, userState);
            }else {
                count = attributeStoreDao.getTotalAttributesCount(searchText, attributeName, userTypeFilter, userState);
            }
        } else {
            if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                count = userDao.getTotalActiveCount(searchText, iamStatusFilter, userStatusFilter, _2faStatusFilter, userTypeFilter, accountIds, userState);
            }else {
                count = userDao.getTotalActiveCountWithLowerCase(searchText, iamStatusFilter, userStatusFilter, _2faStatusFilter, userTypeFilter, accountIds, userState);
            }
        }
        //        }else {
        //            users = userDao.getActivePaginatedList(page, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)), searchText, iamStatusFilter, userStatusFilter,
        //                    _2faStatusFilter);
        //            count = userDao.getTotalActiveCount(searchText, iamStatusFilter, userStatusFilter, _2faStatusFilter);
        //        }
        userList = convertToTO(users);
        PaginatedTO<UserTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(userList);
        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    private List<UserTO> convertToTO(List<User> users) {
        List<UserTO> userTOs = new ArrayList<>();
        Long loginExpiry = (long) 30 * 24 * 12 * 60 * 60 * 1000;
        try {
            loginExpiry = Long.parseLong(config.getProperty(Constant.LOGIN_EXPIRY_TIME_IN_MILLIS));
        } catch (Exception e) {
            logger.log(Level.ERROR, USER_SERVICE_IMPL_LOG + " Login Expiry Time Not Found ");
        }
        for (User user : users) {
            UserTO userTO = user.convertToTO();
            AttributeStore attributeStore = DaoFactory.getAttributeStoreDao().getRegisteredByAttribute(user.getId());
            if (attributeStore != null) {
                userTO.setUsername(attributeStore.getAttributeValue());
            }
            if (user.getUserStatus() == UserStatus.ACTIVE) {
                if (user.getLastLoginTime() != null) {
                    long userLoginTime = user.getLastLoginTime().getTime() + loginExpiry;
                    if (userLoginTime < System.currentTimeMillis()) {
                        userTO.setUserStatus(UserStatus.DISABLED.name());
                    }
                }
            }
            userTOs.add(userTO);
        }
        return userTOs;
    }

    @Override
    public void updateUserBindingOnApproval(Session session, AuthenticationAttempt authenticationAttempt) throws AuthException {
        try {

            if (authenticationAttempt.getUser() != null) {
                User user = getActiveUser(session,authenticationAttempt.getUser().getAccountId());
                Request request = requestDao.getRequestByUniqueKeyAndValue(Constant.USER_IDENTIFIER, user.getId().toString() + authenticationAttempt.getSenderIdDetails(),
                        ApprovalStatus.USER_APPROVAL_PENDING,
                        ApprovalStatus.USER_APPROVAL_PENDING, RequestType.BIND_SERVICE);
                if (request == null) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(), errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
                }
                UserBindingTO userBindingTO = new Gson().fromJson(request.getRequestJSON(), UserBindingTO.class);
                ApplicationTO applicationTO = userBindingTO.getApplication();
                List<UserApplicationServiceRel> userApplicationRels = userApplicationRelDao.getApplicationRelsForUser(user.getId());
                Application application = applicationService.getApplicationByApplicationId(applicationTO.getApplicationId());
                List<UserApplicationServiceRel> userApplicationStagingRels = stageUserApplicationServiceBindingInDB(user, applicationTO, userApplicationRels, application);
                if (!userApplicationStagingRels.isEmpty()) {
                    UserApplicationServiceRel userStagingRel = userApplicationStagingRels.get(0);
                    if (userStagingRel.getId().getApplication().getApplicationAccountId().equals(authenticationAttempt.getSenderAccountId())) {
                        if (TransactionApprovalStatus.APPROVED.name().equals(authenticationAttempt.getAttemptStatus())) {
                            CamUtil.onboardUserAndBind(application,user,null, null);
                            completeUserBinding(authenticationAttempt, userStagingRel.getId().getApplication());
                            creatApplicationRelsForUser(session, userApplicationStagingRels);
                            request.setApprovalStatus(ApprovalStatus.APPROVED_BY_USER);
                            updateIamStatusToEnabled(session, user);
                        }
                        else {
                            request.setApprovalStatus(ApprovalStatus.REJECTED_BY_USER);
                        }
                    }
                }
                else {
                    if (TransactionApprovalStatus.APPROVED.name().equals(authenticationAttempt.getAttemptStatus())) {
                        request.setApprovalStatus(ApprovalStatus.APPROVED_BY_USER);
                    }
                    else {
                        request.setApprovalStatus(ApprovalStatus.REJECTED_BY_USER);
                    }
                }
                requestDao.update(session, request);
            }
        }
        catch (ServiceNotFoundException e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
        }
    }

    private void creatApplicationRelsForUser(Session session, List<UserApplicationServiceRel> userApplicationStagingRels) {
        for (UserApplicationServiceRel userApplicationStagingRel : userApplicationStagingRels) {

            UserApplicationServiceRel userApplicationServiceRel = null;
            try {
                userApplicationServiceRel = userApplicationRelDao.getUserApplicationForId(userApplicationStagingRel.getId(), session);
                userApplicationServiceRel.setBindingStatus(BindingStatus.ACTIVE);
                userApplicationRelDao.update(session, userApplicationServiceRel);
            }
            catch (UserApplicationRelNotFoundException e) {
            }
            if (userApplicationServiceRel == null) {
                userApplicationServiceRel = new UserApplicationServiceRel();
                userApplicationServiceRel.setId(userApplicationStagingRel.getId());
                userApplicationServiceRel.setBindingStatus(BindingStatus.ACTIVE);
                userApplicationServiceRel.setTwoFactorStatus(TwoFactorStatus.ENABLED);
                userApplicationRelDao.create(session, userApplicationServiceRel);
            }
        }
    }

    private List<UserApplicationServiceRel> stageUserApplicationServiceBindingInDB(User user, ApplicationTO applicationTO,
            List<UserApplicationServiceRel> userApplicationRels, Application application) throws ServiceNotFoundException, AuthException {
        List<ServiceTO> userServiceTOs = applicationTO.getServices();
        List<UserApplicationServiceRel> userApplicationStagingRels = new ArrayList<>();
        List<Service> applicationServices = application.getServices();
        for (ServiceTO serviceTO : userServiceTOs) {
            Service service = serviceProcessor.getServiceByServiceName(serviceTO.getServiceName());
            if (!applicationServices.contains(service)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_SERVICE_FOR_APPLICATION(), errorConstant.getERROR_MESSAGE_INVALID_SERVICE_FOR_APPLICATION());
            }
            UserApplicationServiceCompositeKey userApplicationServiceCompositeKey = new UserApplicationServiceCompositeKey();
            userApplicationServiceCompositeKey.setUser(user);
            userApplicationServiceCompositeKey.setApplication(application);
            userApplicationServiceCompositeKey.setService(service);
            UserApplicationServiceRel userApplicationServiceRel = new UserApplicationServiceRel();
            userApplicationServiceRel.setId(userApplicationServiceCompositeKey);
            if (!userApplicationRels.contains(userApplicationServiceRel)) {
                userApplicationStagingRels.add(userApplicationServiceRel);
            }
        }
        return userApplicationStagingRels;
    }

    private void completeUserBinding(AuthenticationAttempt authenticationAttempt, Application application) throws AuthException {
        try {

            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            BindingInfoV2.Builder builder = new BindingInfoV2.Builder();
            builder.consumerAccountId(authenticationAttempt.getUser().getAccountId());
            builder.transactionId(authenticationAttempt.getTransactionId());
            builder.serviceName(authenticationAttempt.getService().getServiceName());
            BindingInfoV2 consumerRegistrationInfo = builder.build();

            iamExtension.completeConsumerApplicationBinding(token, consumerRegistrationInfo, reqRefNum);
        }
        catch (IAMException e) {
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }
    }

    @Override
    public UserTO getUserDetails(String accountId, String role) throws AuthException {
        if (!permissionService.isPermissionValidForRole(PermissionUtil.GET_USER_ACCOUNT_DETAILS, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        AccountWE accountWE = iamExtensionService.getAllAttributesForAccount(accountId);
        UserTO userTO = new UserTO();
        userTO.setIamUserStatus(accountWE.getState());
        // userTO.setIamAccountAccessStatus(accountWE.getAccountAccessStatus());
        return userTO;
    }

    public UserTO getUserDetails(String accountId) throws AuthException {
        AccountWE accountWE = iamExtensionService.getAllAttributesForAccount(accountId);
        UserTO userTO = new UserTO();
        userTO.setIamUserStatus(accountWE.getState());
        return userTO;
    }

    public String getAccountId(UserTO userTO) throws AuthException {
        String searchAttributeValue = null;
        String attributeName = null;
        for (AttributeDataTO attributeDataTO : userTO.getSearchAttributes()) {
            attributeName = attributeDataTO.getAttributeName();
            searchAttributeValue = attributeDataTO.getAttributeValue();
        }
        IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
        return iamExtensionService.getAccountId(attributeName, searchAttributeValue, iamExtensionV2);
    }

    @Override
    public User getUserByAccountId(String accountId) throws UserNotFoundException {
        return userDao.getUserByAccountId(accountId);
    }

    @Override
    public User getUserByAccountId(String accountId, Session session) throws UserNotFoundException {
        return userDao.getUserByAccountId(accountId, session);
    }

    @Override
    public List<User> getUserByAccountIdList(String accountId) throws UserNotFoundException {
        return userDao.getUserByAccountIdList(accountId);
    }

    @Override
    public void enableToken(String accountId, Application application, Boolean isTokenEnabled) {
        try {
            IAMUtil iamUtil = IAMUtil.getInstance();
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
            Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            AccountWE accountWE = iamExtension.getAllUserAttributesNames(accountId, token);
            logger.log(Level.DEBUG, "Enable token accountWE : " + new Gson().toJson(accountWE));
            if (accountWE.getIsTokenEnabled() == null || !accountWE.getIsTokenEnabled()) {
                AccountWE weEditAccount = new AccountWE();
                weEditAccount.setIsTokenEnabled(isTokenEnabled);
                iamExtension.editAccount(weEditAccount, accountId, token);
            }
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public List<UserTO> getUsersInfo(List<String> accountIds, String deviceId) throws AuthException {
        List<UserTO> users = new ArrayList<>();
        for (String accountId : accountIds) {
            UserTO userTO = new UserTO();
            userTO.setAccountId(accountId);
            User user = getActiveUser(accountId);
            if (user != null) {
                userTO = user.convertToTO();
                AttributeStore attributeStore = DaoFactory.getAttributeStoreDao().getRegisteredByAttribute(user.getId());
                if (attributeStore != null) {
                    userTO.setUsername(attributeStore.getAttributeValue());
                }
                AccountWE accountWE = iamExtensionService.getUserDeviceState(accountId, deviceId);
                userTO.setUserDeviceState(accountWE.getUserDeviceState());
            }
            users.add(userTO);
        }
        return users;
    }


    public List<UserTO> getUsersInfo(List<String> accountIds) throws AuthException {
        List<UserTO> users = new ArrayList<>();
        for (String accountId : accountIds) {
            UserTO userTO = new UserTO();
            userTO.setAccountId(accountId);
            User user = getActiveUser(accountId);
            if (user != null) {
                userTO = user.convertToTO();
                AttributeStore attributeStore = DaoFactory.getAttributeStoreDao().getRegisteredByAttribute(user.getId());
                if (attributeStore != null) {
                    userTO.setUsername(attributeStore.getAttributeValue());
                }
            }
            users.add(userTO);
        }
        return users;
    }

    @Override
    public User update(Session session, User user) {
        user = userDao.update(session, user);
        return user;
    }

    @Override
    public AdfsDetailsTO authenticateUserWithAdfs(String userId, String password) throws AuthException {
        logger.log(Level.DEBUG, USER_SERVICE_IMPL_LOG + " authenticateUserWithAdfs : start");
        try {
            return AdfsUtil.getInstance().getAdfs(userId, password);
        }
        catch (SignOnException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NAME_PASSWORD_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_PASSWORD_INVALID());
        }
        catch (JsonProcessingException | ProcessingException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_SERVER_ERROR());
        }
        finally {
            logger.log(Level.DEBUG, USER_SERVICE_IMPL_LOG + " authenticateUserWithAdfs : start");
        }
    }

    @Override
    public User getUserByAttributeValueWithoutCase(String attributeValue) throws AttributeNotFoundException {
        return attributeStoreDao.getActiveAttributeWithoutCase(Constant.USER_ID,attributeValue).getUser();
    }
}
