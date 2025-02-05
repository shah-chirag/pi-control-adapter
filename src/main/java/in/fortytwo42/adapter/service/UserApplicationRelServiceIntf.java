package  in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.exception.UserApplicationRelNotFoundException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceCompositeKey;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;

public interface UserApplicationRelServiceIntf {

	UserApplicationServiceRel getUserApplicationRel(User user, Application application, Service service);

	UserApplicationServiceRel getUserApplicationRel(User user, Application application, Service service, Session session);

	UserApplicationServiceRel getUserApplicationRelForAccountId(User user, Application application, Service service);

	void validateUserApplicationRel(UserApplicationServiceRel userApplicationRel) throws AuthException;

	List<UserApplicationServiceRel> getUserApplicationRel(Long applicationId, Long userId);

	void bulkUpdateUserApplicationRel(Session session, List<UserApplicationServiceRel> userApplicationRels);

	UserApplicationServiceRel updateUserApplicationRel(Session session, UserApplicationServiceRel userApplicationRel, BindingStatus bindingStatus);

	List<UserApplicationServiceRel> getUserApplicationRels(Long applicationId, Long userId);

	UserApplicationServiceRel getUserApplicationRel(UserApplicationServiceCompositeKey userApplicationCompositeKey);

	List<UserApplicationServiceRel> getUserApplicationRel(Long userId);

	List<UserApplicationServiceRel> getUserApplicationRel(Long userId, Session session);

	List<UserApplicationServiceRel> getBlockedApplicationRelsForUser(Long userId, Long applicationId);

	List<ApplicationTO> getApplicationRelsForUser(Long id);

	UserApplicationServiceRel getUserApplicationForId(UserApplicationServiceCompositeKey adUserApplicationCompositeKey) throws UserApplicationRelNotFoundException;

	List<UserApplicationServiceRel> getApplicationRelsForUserId(Long userId);

	boolean isApplicationUserBindingPresent(Long applicationId, Long userId);
	
	UserApplicationServiceRel createUserApplicationRel(Session session, User user, Application application, Service service, BindingStatus bindingStatus);

    List<UserApplicationServiceRel> getApplicationRels(Long userId);

    UserApplicationServiceRel createUserApplicationRel(Session session, UserApplicationServiceRel userApplicationRel);

    Long getUserAndApplicationRelCount(Long applicationId, Long userId);
	Long getUserAndApplicationRelCount(Long applicationId, Long userId, Session session);


    boolean isServiceBindingPresent(UserApplicationServiceRel userApplicationRel);

    Long getUserServiceRelCount(Long serviceId, Long userId, Session session);
    
    List<ApplicationTO> getTunnelingApplicationRelsForUser(Long userId);
}
