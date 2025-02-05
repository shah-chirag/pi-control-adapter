
package in.fortytwo42.adapter.facade;

import javax.ws.rs.container.AsyncResponse;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.transferobj.BlockUserApplicationTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.ConsumerTO;
import in.fortytwo42.adapter.transferobj.TunnelingApplicationTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.transferobj.UserStatusTO;
import in.fortytwo42.entities.bean.AuthenticationAttempt;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

public interface NonADUserFacadeIntf {

    void bindConsumer(ConsumerBindingTO consumerBindingTO, String applicationId,  AsyncResponse asyncResponse) throws AuthException;

    boolean unbindConsumer(ConsumerBindingTO consumerBindingTO, String applicationId, String version) throws AuthException;

    ConsumerTO getConsumerStatus(String attributeName, String attributeValue, String applicationId, String serviceName) throws AuthException;

    ConsumerTO updateUserApplicationServiceRel(ConsumerTO userTO, String applicationId) throws AuthException;

    void updateMultipleUserApplicationServiceRelCopy(BlockUserApplicationTO blockUserApplicationTO) throws AuthException, UserBlockedException;

    UserApplicationRelTO updateUserApplicationRel(UserApplicationRelTO stagingUserApplicationRelSettingsTO, String role, String actor) throws AuthException;

    ConsumerBindingTO completeConsumerBinding(Session session,AuthenticationAttempt authenticationAttempt) throws AuthException;

    UserStatusTO updateUserStatus(UserStatusTO userStatusTO, String role, String actor) throws AuthException;

    UserStatusTO approveUserStatusUpdateRequest(UserStatusTO userStatusTO, String role, String actor) throws AuthException;

    TunnelingApplicationTO checkSubscription(String applicationId, TunnelingApplicationTO tunnelingApplicationTO) throws AuthException;

    void unbindServicesFromUser(UserBindingTO userBindingTO, String role, String actor,Long id,boolean saveRequest) throws AuthException, UserBlockedException;

}
