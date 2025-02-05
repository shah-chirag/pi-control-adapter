
package in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.cam.dto.UserResponseDto;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserStatusTO;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.UserTO;

public interface UserServiceIntf {

    public User createUser(Session session, String accountId, UserRole role, String twoFactorStatus, User user) throws AuthException;

    public User createUserV2(Session session, String accountId, UserRole role, String twoFactorStatus, UserResponseDto camUser) throws AuthException;

    public boolean isMobileApplicationBindingPresent(String mobile, Application application);

    public void saveAuthAttempt(Session session, AuthenticationAttempt authenticationAttempt);

    public User updateUser(Session session, User user);

    public void bulkUpdateUserApplicationRel(Session session, List<UserApplicationServiceRel> userApplicationRelsToUpdate);

    User getActiveUser(Long userId) throws AuthException;

    User getActiveUser(Session session, Long userId) throws AuthException;

    User getActiveUser(String accountId) throws AuthException;

    User getActiveUserForAuthAttempt(String accountId) throws AuthException;

    User getActiveUser(Session session, String accountId) throws AuthException;

    void validateUser(User user) throws AuthException;

    List<User> getActiveUsers(String userTypeFilter, String iamStatusFilter, String userStatusFilter, String _2faStatusFilter, int page, int limit, Long fromDate, Long toDate) throws AuthException;

    //	User updateUser(User user, UserStaging userStaging);

    void updateIAMStatusToDisabled(Session session, User user);

    void updateIamStatusToEnabled(Session session, User user);

    void updateAllUsers(Session session, TwoFactorStatus twoFactorStatus);

    boolean isUserGroupRelPresent(Long userId, Long groupId);

    User getNonADUser(String username, String mobile) throws UserNotFoundException;

    public List<User> getNonADUsersByAccountId(String accountId) throws AuthException;

    public UserStatusTO createUserStatusUpdateRequest(Session session, UserStatusTO userStatusTO, String actor) throws AuthException;

    UserStatusTO approveUserStatusUpdateRequest(Session session, Request request, UserStatusTO userStatusTO) throws AuthException;

    String getMobileNo(Long userId);

    String getUsername(Long userId);

    String getEmail(Long userId);

    User authenticate(String userId, String password) throws AuthException;
    AdfsDetailsTO authenticateADUser(String userId, String password) throws AuthException;
    void authenticateNonADUser(User user, String userId, String password) throws AuthException;

    void autoBindUserToApplication(Session session, List<ApplicationTO> applications, User user, String camPassword) throws AuthException;
    
    void logout(Session session, String username, String token, Long expiry);
    
    PaginatedTO<UserTO> getUsers(UserRole userRole, String userUpdateStatus, int page, String searchText, String attributeName,String iamStatusFilter, String userStatusFilter, String _2faStatusFilter,
            String approvalStatus, String userState, String role, String userType , Long userGroupId, Boolean export) throws AuthException;
    
    UserTO getUserDetails(String accountId, String role) throws AuthException;
    
    User getUserByAccountId(String accountId) throws UserNotFoundException;

    User getUserByAccountId(String accountId, Session session) throws UserNotFoundException;

    List<User> getUserByAccountIdList(String accountId) throws UserNotFoundException;

    void enableToken(String accountId, Application application, Boolean isTokenEnabled);

    /**
     * 
     * @param accountIds
     * @return
     * @throws AuthException
     */
    List<UserTO> getUsersInfo(List<String> accountIds, String deviceId) throws AuthException;

    List<UserTO> getUsersInfo(List<String> accountIds) throws AuthException;

    void updateUserBindingOnApproval(Session session, AuthenticationAttempt authenticationAttempt) throws AuthException;

    public User update(Session session, User user);

    UserTO getUserDetails(String userAccountId) throws AuthException;

    String getAccountId(UserTO userTO) throws AuthException;

    AdfsDetailsTO authenticateUserWithAdfs(String userId, String password) throws AuthException;
    public User getUserByAttributeValueWithoutCase(String attributeValue) throws AttributeNotFoundException;

}
