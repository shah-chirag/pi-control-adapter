package in.fortytwo42.adapter.webservice;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.NonADUserFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.BlockUserApplicationTO;
import in.fortytwo42.adapter.transferobj.ConsumerBindingTO;
import in.fortytwo42.adapter.transferobj.ConsumerTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.EsbResponseTO;
import in.fortytwo42.adapter.transferobj.TunnelingApplicationTO;
import in.fortytwo42.adapter.transferobj.UserBindingResponseTO;
import in.fortytwo42.adapter.transferobj.UserStatusTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

// TODO: Auto-generated Javadoc
/**
 * The Class NonADUserResource.
 */
@Path("/v3/consumers")
public class NonADUserResource {

	/** The Constant USER_BINDING. */
	private static final String USER_BINDING = "User binding~";
	
	/** The non AD user facade. */
	private NonADUserFacadeIntf nonADUserFacade = FacadeFactory.getNonADUserFacade();
	private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

	/** The Constant NONAD_USER_RESOURCE_LOG. */
	private static final String NONAD_USER_RESOURCE_LOG = "NonADUserResource";

	private static Logger logger= LogManager.getLogger(NonADUserResource.class);
    
    private Config config = Config.getInstance();
	private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();


	/**
     * Bind consumer.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationLabel the application label
     * @param serviceName the service name
     * @param consumerBindingTO the consumer binding TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    @InternalSecure
    @POST
    @Path("/bind")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.bind")
    @ExceptionMetered(name = "exceptions.v3.consumers.bind")
    @ResponseMetered(name = "response.code.v3.consumers.bind")
    public void bindConsumer(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSeceret, @HeaderParam(Constant.HEADER_SERVICE_NAME) String serviceName, final ConsumerBindingTO consumerBindingTO,
            @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " bindConsumer : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, USER_BINDING, System.currentTimeMillis() + Constant.TILT,
                    applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), "~user binding called"));
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
              // String serverId = config.getProperty(Constant.SERVER_ID);
                consumerBindingTO.setServiceName(serviceName);
                String errorMessage = ValidationUtilV3.isConsumerValidForInitiateBinding(consumerBindingTO);
                if (errorMessage == null) {
                    consumerBindingTO.setApplicationId(applicationId);
                    consumerBindingTO.setApplicationSecrete(applicationSeceret);
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, USER_BINDING, System.currentTimeMillis() + Constant.TILT,
                            applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), "~user binding validation successful"));
                    nonADUserFacade.bindConsumer(consumerBindingTO, applicationId, asyncResponse);
                    consumerBindingTO.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = consumerBindingTO;
                    asyncResponse.resume(Response.status(responseStatus).entity(response).build());
                }
                else {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, USER_BINDING, System.currentTimeMillis() + Constant.TILT,
                            applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), "~user binding vallidation failed"));
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).entity(response).build());
                }
            }
            catch (AuthException e) {
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, USER_BINDING, System.currentTimeMillis() + Constant.TILT,
                        applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), Constant.TILT, e.getMessage()));
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_USER_BINDING_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            catch (Exception e) {
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId() + Constant.TILT, USER_BINDING, System.currentTimeMillis() + Constant.TILT,
                        applicationId, Constant.TILT, consumerBindingTO.getTransactionId(), "~internal server error"));
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_USER_BINDING_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            finally {
                logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " bindConsumer : end");
            }
        });
    }

    /**
     * Unbind consumer.
     *
     * @param asyncResponse the async response
     * @param clientId the client id
     * @param applicationLabel the application label
     * @param serviceName the service name
     * @param consumerBindingTO the consumer binding TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    @InternalSecure
    @POST
    @Path("/unbind")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.unbind")
    @ExceptionMetered(name = "exceptions.v3.consumers.unbind")
    @ResponseMetered(name = "response.code.v3.consumers.unbind")
    public void unbindConsumer(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String clientId,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSeceret, @HeaderParam(Constant.HEADER_SERVICE_NAME) String serviceName, final ConsumerBindingTO consumerBindingTO,
            @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " unbindConsumer : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
			IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());

			Status responseStatus;
            Object response;
            try {
                consumerBindingTO.setServiceName(serviceName);
                String errorMessage = ValidationUtilV3.isConsumerValidForUnbinding(consumerBindingTO);
                if (errorMessage == null) {
                    consumerBindingTO.setApplicationId(clientId);
                    consumerBindingTO.setApplicationSecrete(applicationSeceret);
                    boolean isConsumerBindingComplete = nonADUserFacade.unbindConsumer(consumerBindingTO, clientId, null);
                    if (isConsumerBindingComplete) {
                        consumerBindingTO.setStatus(Constant.SUCCESS_STATUS);
                        responseStatus = Response.Status.OK;
                        response = consumerBindingTO;
                    }
                    else {
                        consumerBindingTO.setStatus(Constant.FAILURE_STATUS);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = consumerBindingTO;
                    }
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_USER_UNBINDING_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_USER_UNBINDING_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
				sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
				logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " unbindConsumer : start");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

	/**
	 * Gets the consumer.
	 *
	 * @param asyncResponse the async response
	 * @param clientId the client id
	 * @param serviceName the service name
	 * @param applicationLabel the application label
	 * @param queryParams the query params
	 * @param reqRefNumber the req ref number
	 * @return the consumer
	 */
	@ApiLogger
	
	@InternalSecure
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.consumers")
    @ExceptionMetered(name = "exceptions.v3.consumers")
    @ResponseMetered(name = "response.code.v3.consumers")
	public void getConsumer(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String clientId, @HeaderParam(Constant.HEADER_SERVICE_NAME) String serviceName,
			@HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " getConsumer : start");
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus = null;
			Object response = null;
			String attributeName = null;
			String attributeValue = null;
			try {
				Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
				attributeName = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME.getKey()),
						in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME);
				attributeValue = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE.getKey()),
						in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE);
				String errorMessage = ValidationUtilV3.isConsumerValid(attributeName, attributeValue, serviceName, applicationLabel);
				if (errorMessage == null) {
					ConsumerTO consumerTO = nonADUserFacade.getConsumerStatus(attributeName, attributeValue, clientId, serviceName);
					responseStatus = Response.Status.OK;
					response = consumerTO;
				} else {
					ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
					responseStatus = Response.Status.BAD_REQUEST;
					response = errorTO;
				}
			} catch (AuthException e) {
				logger.log(Level.ERROR, e);
				ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_STATUS_FAILED(), e.getMessage());
				responseStatus = Response.Status.BAD_REQUEST;
				response = errorTO;
			} catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_DATA());
				responseStatus = Response.Status.BAD_REQUEST;
				response = errorTO;
			} catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USER_STATUS_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
	            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " getConsumer : end");
			}
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});

	}

	/**
	 * Enable user services.
	 *
	 * @param asyncResponse the async response
	 * @param applicationId the application id
	 * @param applicationLabel the application label
	 * @param consumerTO the consumer TO
	 * @param reqRefNumber the req ref number
	 */
	@ApiLogger
	@InternalSecure
	
	@POST
	@Path("/enable-services")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.consumers.enable-services")
    @ExceptionMetered(name = "exceptions.v3.consumers.enable-services")
    @ResponseMetered(name = "response.code.v3.consumers.enable-services")
	public void enableUserServices(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
			@HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel, ConsumerTO consumerTO, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " enableUserServices : start");
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus;
			Object response;
			String humanizedMessage = errorConstant.getHUMANIZED_USER_UDATE_FAILED();
			try {
				String errorMessage = ValidationUtilV3.isConsumerValidForEnablingServices(consumerTO, applicationLabel);
				if (errorMessage == null) {
					ConsumerTO userResponseTO = nonADUserFacade.updateUserApplicationServiceRel(consumerTO, applicationId);
					responseStatus = Response.Status.OK;
					response = userResponseTO;
				} else {
					ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
					responseStatus = Response.Status.BAD_REQUEST;
					response = errorTO;
				}
			} catch (AuthException e) {
				logger.log(Level.ERROR, e);
				ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
				responseStatus = Response.Status.BAD_REQUEST;
				response = errorTO;
			} catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
	            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " enableUserServices : end");
			}
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}

	/**
	 * Send user update status.
	 *
	 * @param asyncResponse the async response
	 * @param blockUserApplicationTO the block user application TO
	 * @param reqRefNumber the req ref number
	 */
	@ApiLogger
	
	@POST
	@Path("/update-user-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.consumers.update-user-status")
    @ExceptionMetered(name = "exceptions.v3.consumers.update-user-status")
    @ResponseMetered(name = "response.code.v3.consumers.update-user-status")
	public void sendUserUpdateStatus(@Suspended final AsyncResponse asyncResponse, BlockUserApplicationTO blockUserApplicationTO, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " sendUserUpdateStatus : start");
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus;
			Object response;
			String humanizedMessage = errorConstant.getHUMANIZED_USER_UDATE_FAILED();
			try {
				String errorMessage = ValidationUtil.isConsumerValidForUserUpdateV3(blockUserApplicationTO);
				if (errorMessage == null) {
					nonADUserFacade.updateMultipleUserApplicationServiceRelCopy(blockUserApplicationTO);
					EsbResponseTO esbResponseTO = new EsbResponseTO();
					esbResponseTO.setStatusCode(Constant.RESPONSE_CODE_200);
					esbResponseTO.setSubStatusCode(Constant.RESPONSE_SUB_CODE_100);
					esbResponseTO.setStatusDesc(Constant.SUCCESS_STATUS);
					responseStatus = Response.Status.OK;
					response = esbResponseTO;
				} else {
					ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
					responseStatus = Response.Status.BAD_REQUEST;
					response = errorTO;
				}
			} catch (AuthException e) {
				logger.log(Level.ERROR, e);
				ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
				responseStatus = Response.Status.BAD_REQUEST;
				response = errorTO;
			} catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
	            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " sendUserUpdateStatus : end");
			}
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}
	
	/**
	 * Bulk edit user.
	 *
	 * @param asyncResponse the async response
	 * @param authorizationHeader the authorization header
	 * @param bulkEditUserTO the bulk edit user TO
	 * @param reqRefNumber the req ref number
	 */
	@RolesAllowed(value = { "MAKER", "CHECKER" })
	@ApiLogger
	
	@Secured
	@ResponseToken
	@POST
	@Path("/edit")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.consumers.edit")
    @ExceptionMetered(name = "exceptions.v3.consumers.edit")
    @ResponseMetered(name = "response.code.v3.consumers.edit")
	public void bulkEditUser(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
			in.fortytwo42.adapter.transferobj.BulkEditUserTO bulkEditUserTO, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " bulkEditUser : start");
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus;
			Object response;
			String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
			Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
			String role = payload.get(Constant.ROLE);
			String actor = payload.get(Constant.USER_NAME);
			IamThreadContext.setActor(actor);
			String humanizedMessage = errorConstant.getHUMANIZED_USER_UDATE_FAILED();
			try {
				if (ValidationUtilV3.isValid(bulkEditUserTO.getApprovalStatus())) {
					humanizedMessage = errorConstant.getHUMANIZED_USER_APPROVAL_FAILED();
					String errorMessage = ValidationUtilV3.isUserValidForBulkApproval(bulkEditUserTO);
					if (errorMessage == null) {
						in.fortytwo42.adapter.transferobj.BulkEditUserTO bulkEditUserTOResponse =null; //TODO: adUserFacadeIntf.bulkApproveUser(UserType.USER, bulkEditUserTO, actor, role);
						responseStatus = Response.Status.OK;
						response = bulkEditUserTOResponse;
					} else {
						ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
						responseStatus = Response.Status.BAD_REQUEST;
						response = errorTO;
					}
				} else {
					String errorMessage = ValidationUtilV3.isUserValidForBulkEdit(bulkEditUserTO);
					if (errorMessage == null) {
						in.fortytwo42.adapter.transferobj.BulkEditUserTO bulkEditUserTOResponse = null;//TODO: adUserFacadeIntf.bulkEditUser(UserType.USER, bulkEditUserTO, actor, role);
						responseStatus = Response.Status.OK;
						response = bulkEditUserTOResponse;
					} else {
						ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
						responseStatus = Response.Status.BAD_REQUEST;
						response = errorTO;
					}
				}
			}
//			catch (AuthException e) {
//				logger.log(Level.ERROR, e);
//				ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
//				responseStatus = Response.Status.BAD_REQUEST;
//				response = errorTO;
//			}
			catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
	            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " bulkEditUser : end");
			}
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}

	/**
	 * Gets the subscribed applications.
	 *
	 * @param asyncResponse the async response
	 * @param authorizationHeader the authorization header
	 * @param userId the user id
	 * @param reqRefNumber the req ref number
	 * @return the subscribed applications
	 */
	@RolesAllowed(value = { "MAKER", "CHECKER", "VIEW_ONLY" })
	@ApiLogger
	
	@Secured
	@ResponseToken
	@GET
	@Path("/{userId}/subscribed-applications")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.consumers.userId.subscribed-applications")
    @ExceptionMetered(name = "exceptions.v3.consumers.userId.subscribed-applications")
    @ResponseMetered(name = "response.code.v3.consumers.userId.subscribed-applications")
	public void getSubscribedApplications(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " getSubscribedApplications : start");
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus;
			Object response;
			Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authorizationHeader);
			String actor = payload.get(Constant.USER_NAME);
			String role = payload.get(Constant.ROLE);
			IamThreadContext.setActor(actor);
			try {
				List<ApplicationTO> applicationTOs = null;//TODO: adUserFacadeIntf.getSubscribedApplicationsByUser(UserType.USER, userId, role);
				responseStatus = Response.Status.OK;
				response = applicationTOs;
			} 
//			catch (AuthException e) {
//				logger.log(Level.ERROR, e);
//				ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION, e.getMessage());
//				responseStatus = Response.Status.BAD_REQUEST;
//				response = errorTO;
//			}
			catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
	            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " getSubscribedApplications : end");
			}
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}

	/**
	 * Test callback.
	 *
	 * @param asyncResponse the async response
	 * @param json the json
	 * @param reqRefNumber the req ref number
	 */
	@ApiLogger
	
	@POST
	@Path("/test-callback")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.consumers.test-callback")
    @ExceptionMetered(name = "exceptions.v3.consumers.test-callback")
    @ResponseMetered(name = "response.code.v3.consumers.test-callback")
	public void testCallback(@Suspended final AsyncResponse asyncResponse, String json, @HeaderParam("request-reference-number") String reqRefNumber) {

		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " testCallback : start");
			logger.log(Level.INFO, "Request received " + json);
			UserBindingResponseTO userBindingResponseTO = new Gson().fromJson(json, UserBindingResponseTO.class);
			userBindingResponseTO.setTxnStatus(Constant.SUCCESS_STATUS);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " testCallback : end");
			asyncResponse.resume(Response.status(Response.Status.OK).entity(userBindingResponseTO).build());
		});
	}

	/**
	 * Edits the user application rels.
	 *
	 * @param asyncResponse the async response
	 * @param authorizationHeader the authorization header
	 * @param userId the user id
	 * @param userApplicationRelTO the user application rel TO
	 * @param reqRefNumber the req ref number
	 */
	@RolesAllowed(value = { "MAKER", "CHECKER" })
	@ApiLogger
	
	@Secured
	@ResponseToken
	@POST
	@Path("/{userId}/user-bindings")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.consumers.userId.user-bindings")
    @ExceptionMetered(name = "exceptions.v3.consumers.userId.user-bindings")
    @ResponseMetered(name = "response.code.v3.consumers.userId.user-bindings")
	public void editUserApplicationRels(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
			UserApplicationRelTO userApplicationRelTO, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " editUserApplicationRels : start");
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus;
			Object response;
			String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
			Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
			String actor = payload.get(Constant.USER_NAME);
			String role = payload.get(Constant.ROLE);
			IamThreadContext.setActor(actor);
			try {
				userApplicationRelTO.setUserId(userId);
				if (ValidationUtilV3.isValid(userApplicationRelTO.getApprovalStatus())) {
					String errorMessage = ValidationUtilV3.isDataValidForApproveUserApplicationRel(userApplicationRelTO);
					if (errorMessage == null) {
						UserApplicationRelTO userApplicationRelTOResponse = null;//TODO: nonADUserFacade.approveUserApplicationRelBinding(userApplicationRelTO, role, actor);
						userApplicationRelTOResponse.setStatus(Constant.SUCCESS_STATUS);
						responseStatus = Response.Status.OK;
						response = new Gson().toJson(userApplicationRelTOResponse);
					} else {
						ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
						responseStatus = Response.Status.BAD_REQUEST;
						response = errorTO;
					}
				} else {
					String errorMessage = ValidationUtilV3.isDataValidForEditUserApplicationRel(userApplicationRelTO);
					if (errorMessage == null) {
						UserApplicationRelTO stagingUserApplicationRelSettings = nonADUserFacade.updateUserApplicationRel(userApplicationRelTO, role, actor);
						stagingUserApplicationRelSettings.setStatus(Constant.SUCCESS_STATUS);
						responseStatus = Response.Status.OK;
						response = new Gson().toJson(stagingUserApplicationRelSettings);
					} else {
						ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
						responseStatus = Response.Status.BAD_REQUEST;
						response = errorTO;
					}
				}
			} catch (AuthException e) {
				logger.log(Level.ERROR, e);
				ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), e.getMessage());
				responseStatus = Response.Status.BAD_REQUEST;
				response = errorTO;
			} catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
	            logger.log(Level.DEBUG, NONAD_USER_RESOURCE_LOG + " editUserApplicationRels : start");
			}
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}
	
	@RolesAllowed(value = { "MAKER", "CHECKER" })
    @ApiLogger
    
    @Secured
    @ResponseToken
    @POST
    @Path("/update-status/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.consumers.update-status.userId")
    @ExceptionMetered(name = "exceptions.v3.consumers.update-status.userId")
    @ResponseMetered(name = "response.code.v3.consumers.update-status.userId")
    public void editUserStatus(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            UserStatusTO userStatusTO) {
	    asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                if (ValidationUtilV3.isValid(userStatusTO.getApprovalStatus())) {
                    String errorMessage = ValidationUtilV3.isDataValidForApproval(userStatusTO);
                    if (errorMessage == null) {
                        userStatusTO.setId(userId);
                        UserStatusTO userStatus = nonADUserFacade.approveUserStatusUpdateRequest(userStatusTO, role, actor);
                        responseStatus = Response.Status.OK;
                        response = userStatus;
                    }else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
                    
                }else {
                    String errorMessage = ValidationUtilV3.isDataValidForUserStatusUpdate(userStatusTO);
                    if (errorMessage == null) {
                        UserStatusTO userStatus =  nonADUserFacade.updateUserStatus(userStatusTO, role, actor);
                        responseStatus = Response.Status.OK;
                        response = new Gson().toJson(userStatus);
                    }else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
                }
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
	}
	
	   
    @ApiLogger
    
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.subscriptions")
    @ExceptionMetered(name = "exceptions.v3.consumers.subscriptions")
    @ResponseMetered(name = "response.code.v3.consumers.subscriptions")
    public void checkSubscription(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_SERVICE_NAME) String serviceName,@HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel,
            TunnelingApplicationTO tunnelingApplicationTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus = null;
            Object response = null;
            try {
                String errorMessage = ValidationUtilV3.isDataValidForSubscriptionCheck(tunnelingApplicationTO);
                if (errorMessage == null) {
                    TunnelingApplicationTO tunnelingApplication = nonADUserFacade.checkSubscription(applicationId, tunnelingApplicationTO);
                    responseStatus = Response.Status.OK;
                    response = tunnelingApplication;
                }else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO; 
                }
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

}
