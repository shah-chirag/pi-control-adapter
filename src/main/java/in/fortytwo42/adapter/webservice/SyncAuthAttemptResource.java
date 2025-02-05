package in.fortytwo42.adapter.webservice;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.AuthAttemptFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncAuthAttemptResource.
 */
@Path("/v3/sync-authentication-attempts")
public class SyncAuthAttemptResource {

	/** The Constant CREATE_APPROVAL_ATTEMPT. */
	private static final String CREATE_APPROVAL_ATTEMPT = "Create Approval attempt~";

	/** The sync auth attempt resource log. */
	private String SYNC_AUTH_ATTEMPT_RESOURCE_LOG = "<<<<< SyncAuthAttemptResource";
	private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

	private static Logger logger= LogManager.getLogger(SyncAuthAttemptResource.class);

	private AuthAttemptFacadeIntf authAttemptFacade = FacadeFactory.getAuthAttemptFacade();
    private Config config = Config.getInstance();

	/**
	 * Synchronous Create Txn API Call will Respond with updated status - actioned
	 * by User.
	 *
	 * @param asyncResponse the async response
	 * @param applicationId the application id
	 * @param serviceName the service name
	 * @param applicationLabel the application label
	 * @param authenticationAttemptTO the authentication attempt TO
	 * @param reqRefNumber the req ref number
	 */
	@ApiLogger
	
	@InternalSecure
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.sync-authentication-attempts")
    @ExceptionMetered(name = "exceptions.v3.sync-authentication-attempts")
    @ResponseMetered(name = "response.code.v3.sync-authentication-attempts")
	public void createApprovalAttempt(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId, @HeaderParam("Service-Name") String serviceName,
			@HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecrete, final AuthenticationAttemptTO authenticationAttemptTO, @HeaderParam("request-reference-number") String reqRefNumber) {

		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, SYNC_AUTH_ATTEMPT_RESOURCE_LOG + " createApprovalAttempt : start");
			logger.log(Level.DEBUG,
					StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT,
							authenticationAttemptTO.getTransactionId(), Constant.TILT, authenticationAttemptTO.getApprovalAttemptType(), "~Approval attempt generation called"));
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus;
			Object response;
			try {
				authenticationAttemptTO.setServiceName(serviceName);
				String errorMessage = ValidationUtilV3.isValidForCreateApproval(authenticationAttemptTO);
				if (errorMessage == null) {
					logger.log(Level.DEBUG,
							StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId,
									Constant.TILT, authenticationAttemptTO.getTransactionId(), Constant.TILT, authenticationAttemptTO.getApprovalAttemptType(), "~Approval attempt validation successful"));
					//String serverId = config.getProperty(Constant.SERVER_ID);
	                AuthenticationAttemptTO approvalAttemptTOResponse = authAttemptFacade.createApprovalAttempt(asyncResponse, authenticationAttemptTO, applicationId);
	                approvalAttemptTOResponse.setApplicationId(applicationId);
	                approvalAttemptTOResponse.setApplicationSecrete(applicationSecrete);
	                responseStatus = Response.Status.OK;
					response = approvalAttemptTOResponse;
				} else {
					logger.log(Level.DEBUG,
							StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId,
									Constant.TILT, authenticationAttemptTO.getTransactionId(), Constant.TILT, authenticationAttemptTO.getApprovalAttemptType(), "~Approval attempt validation failed"));
					ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
					responseStatus = Response.Status.BAD_REQUEST;
					response = errorTO;
					asyncResponse.resume(Response.status(responseStatus).entity(response).build());
				}
			} catch (AuthException e) {
				logger.log(Level.DEBUG,
						StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId,
								Constant.TILT, authenticationAttemptTO.getTransactionId(), Constant.TILT, authenticationAttemptTO.getApprovalAttemptType(), Constant.TILT, e.getMessage()));
				logger.log(Level.ERROR, e);
				ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED(), e.getMessage());
				responseStatus = Response.Status.BAD_REQUEST;
				response = errorTO;
				asyncResponse.resume(Response.status(responseStatus).entity(response).build());
			} catch (Exception e) {
				logger.log(Level.DEBUG,
						StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId,
								Constant.TILT, authenticationAttemptTO.getTransactionId(), Constant.TILT, authenticationAttemptTO.getApprovalAttemptType(), "~internal server error"));

				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED(),
						errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
				asyncResponse.resume(Response.status(responseStatus).entity(response).build());
			}finally {
	            logger.log(Level.DEBUG, SYNC_AUTH_ATTEMPT_RESOURCE_LOG + " createApprovalAttempt : end");
			}
		});
	}

	/**
	 * Synchronous Get Txn Call respond when User action on Txn Designed with Poller
	 * Callback.
	 *
	 * @param asyncResponse the async response
	 * @param applicationId the application id
	 * @param applicationLabel the application label
	 * @param queryParams the query params
	 * @param reqRefNumber the req ref number
	 * @return the approval attempt
	 */
	@ApiLogger
	
	@InternalSecure
	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.sync-authentication-attempts")
    @ExceptionMetered(name = "exceptions.v3.sync-authentication-attempts")
    @ResponseMetered(name = "response.code.v3.sync-authentication-attempts")
	public void getApprovalAttempt(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
			@HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {

		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, SYNC_AUTH_ATTEMPT_RESOURCE_LOG + " getApprovalAttempt : start");
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus;
			Object response;
			try {
				Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
				String transactionId = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TRANSACTION_ID.getKey()),
						in.fortytwo42.adapter.enums.QueryParam.TRANSACTION_ID);
				String errorMessage = ValidationUtil.isValidForGetApprovalStatus(applicationLabel, transactionId);
				if (errorMessage == null) {
					AuthenticationAttemptTO approvalAttemptTOResponse = authAttemptFacade.getApprovalAttemptDBPoll(transactionId, applicationId);
					responseStatus = Response.Status.OK;
					response = approvalAttemptTOResponse;
					asyncResponse.resume(Response.status(responseStatus).entity(response).build());
				} else {
					ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
					responseStatus = Response.Status.BAD_REQUEST;
					response = errorTO;
					asyncResponse.resume(Response.status(responseStatus).entity(response).build());
				}
			} catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_TRANSACTION_ID());
				responseStatus = Response.Status.BAD_REQUEST;
				response = errorTO;
				asyncResponse.resume(Response.status(responseStatus).entity(response).build());
			} catch (AuthException e) {
				logger.log(Level.ERROR, e);
				ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_APPROVAL_ATTEMPT_FAILED(), e.getMessage());
				responseStatus = Response.Status.BAD_REQUEST;
				response = errorTO;
				asyncResponse.resume(Response.status(responseStatus).entity(response).build());
			} catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPROVAL_ATTEMPT_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
				asyncResponse.resume(Response.status(responseStatus).entity(response).build());
			}finally {
	            logger.log(Level.DEBUG, SYNC_AUTH_ATTEMPT_RESOURCE_LOG + " getApprovalAttempt : ends");
			}
		});
	}

}
