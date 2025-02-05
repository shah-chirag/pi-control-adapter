package in.fortytwo42.adapter.service;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserGroupApplicationRelTO;
import in.fortytwo42.tos.transferobj.UserGroupTO;

public interface UserGroupApplicationRelServiceIntf {

    UserGroupTO addUserGroupApplicationMapping(Session session, UserGroupTO userGroupTO) throws AuthException;

    PaginatedTO<UserGroupApplicationRelTO> getUserGroupApplicationMapping(long groupId, int pageNo, int limit, String searchText);

    String getApplicationIds(Long userGroupId);

}
