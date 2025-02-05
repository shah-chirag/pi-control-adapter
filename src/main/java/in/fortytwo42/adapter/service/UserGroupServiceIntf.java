package in.fortytwo42.adapter.service;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.daos.exception.UserGroupNotFoundException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.tos.transferobj.UserGroupTO;

public interface UserGroupServiceIntf {

    UserGroupTO getUserGroup(String groupName) throws UserGroupNotFoundException;

    UserGroupTO createUserGroup(Session session, UserGroupTO userGroupTO) throws AuthException;

    UserGroupTO updateUserGroup(Session session, UserGroupTO userGroupTO) throws AuthException;

    PaginatedTO<UserGroupTO> getUserGroups(int pageNo, int limit, String searchText);

    UserGroupTO addUserGroupMapping(Session session, UserGroupTO userGroupTO) throws AuthException;

    UserGroupTO removeUserGroup(Session session, UserGroupTO userGroupTO) throws AuthException;

    void unbindUserApplication(Session session, Application application, Service srvice, User user) throws AuthException;

    PaginatedTO<UserGroupTO> getGroupsForUser(int page, int limit, String searchText, Long userId) throws AuthException;

}
