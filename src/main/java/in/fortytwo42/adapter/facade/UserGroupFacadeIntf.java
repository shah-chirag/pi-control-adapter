package in.fortytwo42.adapter.facade;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserGroupApplicationRelTO;
import in.fortytwo42.adapter.transferobj.UserUserGroupRelTO;
import in.fortytwo42.daos.exception.UserGroupNotFoundException;
import in.fortytwo42.tos.transferobj.UserGroupTO;

public interface UserGroupFacadeIntf {

    UserGroupTO getUserGroup(String groupName) throws UserGroupNotFoundException;

    UserGroupTO createUserGroup(UserGroupTO userGroupTO, String role, String actor,Long id, boolean saveRequest) throws AuthException;

    PaginatedTO<UserGroupTO> getUserGroups(int pageNo, String searchText);

    UserGroupTO updateUserGroup(UserGroupTO userGroupTO, String role, String actor,Long id, boolean saveRequest) throws AuthException;

    UserGroupTO approveCreateUserGroupRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException;

    UserGroupTO addUserGroupMapping(UserGroupTO userGroupTO, String role, String actor,Long id, boolean saveRequest) throws AuthException;

    UserGroupTO approveUserGroupMappingRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException;

    UserGroupTO approveUpdateUserGroupRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException;

    UserGroupTO addUserGroupApplicationMapping(UserGroupTO userGroupTO, String role, String actor,Long id, boolean saveRequest) throws AuthException;

    UserGroupTO approveApplicationUserGroupMappingRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException;

    PaginatedTO<UserUserGroupRelTO> getUserUserGroupMapping(Long groupId, int pageNo, String searchText);

    PaginatedTO<UserGroupApplicationRelTO> getUserGroupApplicationMapping(long groupId, int pageNo, String searchText);

    UserGroupTO removeUserGroup(String role, String actor,Long id, UserGroupTO userGroupTO, boolean saveRequest) throws AuthException;

    UserGroupTO approveUserGroupDeleteRequest(Session session, UserGroupTO userGroupTO, String actor) throws AuthException;

    PaginatedTO<UserGroupTO> getGroupsForUser(int pageNo, String searchText, Long userId) throws AuthException;
}
