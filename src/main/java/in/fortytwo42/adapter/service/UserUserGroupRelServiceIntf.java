
package in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserUserGroupRelTO;
import in.fortytwo42.daos.exception.UserUserGroupRelNotFoundException;
import in.fortytwo42.entities.bean.UserUserGroupCompositeKey;
import in.fortytwo42.entities.bean.UserUserGroupRel;

public interface UserUserGroupRelServiceIntf {

    UserUserGroupRel create(Session session, UserUserGroupRel userGroupRel);

    UserUserGroupRel getUserUserGroupForId(UserUserGroupCompositeKey userGroupCompositeKey) throws UserUserGroupRelNotFoundException;

    List<UserUserGroupRel> getUserUserGroupRel(Long id);

    UserUserGroupRel update(Session session, UserUserGroupRel userGroupRel);

    PaginatedTO<UserUserGroupRelTO> getUserUserGroupMapping(long userGoupId, int pageNo, String searchText);

    String getAccountIds(Long userGroupId);

}
