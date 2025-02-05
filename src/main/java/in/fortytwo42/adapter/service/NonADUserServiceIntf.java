package in.fortytwo42.adapter.service;

import javax.ws.rs.container.AsyncResponse;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.transferobj.BlockUserApplicationTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.ConsumerTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

public interface NonADUserServiceIntf {

	void initConsumerBinding(Session session, ConsumerBindingTO consumerBindingTO, String applicationId, String serviceName, String serverId, AsyncResponse asyncResponse) throws AuthException, UserBlockedException;

	boolean unbindConsumer(Session session, ConsumerBindingTO consumerBindingTO, String applicationId, String serviceName, String version) throws AuthException, UserBlockedException;

	ConsumerTO getConsumerStatus(String username, String consumerId, String applicationId, String serviceName) throws AuthException;

	ConsumerBindingTO completeConsumerBinding(Session session, String applicationId, String transactionId, String approvalStatus) throws AuthException, UserBlockedException;

	void bindServicesToUser(Session session, UserBindingTO userBindingTO, String role, String actor) throws AuthException, UserBlockedException;

	void unbindServicesFromUser(Session session, UserBindingTO userBindingTO, String role, String actor,Long id,
	                            boolean saveRequest,boolean checkerApproved) throws AuthException;

	ConsumerTO updateUserApplicationServiceRel(Session session, ConsumerTO userTO, String applicationId) throws AuthException;

	void updateMultipleUserApplicationServiceRelCopy(Session session, BlockUserApplicationTO blockUserApplicationTO) throws AuthException, UserBlockedException;

	UserApplicationRelTO updateUserApplicationRel(Session session, UserApplicationRelTO stagingUserApplicationRelSettingsTO, String role, String actor) throws AuthException;

	UserApplicationRelTO approveUserApplicationRelBinding(Session session, UserApplicationRelTO userApplicationRelTO, String role, String actor) throws AuthException;

}