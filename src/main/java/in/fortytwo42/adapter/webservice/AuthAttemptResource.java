
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

import in.fortytwo42.tos.transferobj.TransactionReportRequestTO;
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
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.facade.AuthAttemptFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ApprovalAttemptPollerTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.EsbResponseTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.QRCodeDataTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.tos.transferobj.AuthenticationAttemptHistoryTO;

// TODO: Auto-generated Javadoc
/**
 * The Class AuthAttemptResource.
 */
@Path("/v3/authentication-attempts")
public class AuthAttemptResource {

    /** The Constant CREATE_APPROVAL_ATTEMPT. */
    private static final String CREATE_APPROVAL_ATTEMPT = "Create Approval attempt~";

    /** The auth attempt facade intf. */
    private AuthAttemptFacadeIntf authAttemptFacadeIntf = FacadeFactory.getAuthAttemptFacade();

    /** The Constant AUTH_ATTEMPT_RESOURCE_LOG. */
    private static final String AUTH_ATTEMPT_RESOURCE_LOG = "AuthAttemptResource";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(AuthAttemptResource.class);

    /**
     * Update auth attempt.
     *
     * @param asyncResponse the async response
     * @param pollerTO the poller TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger

    @POST
    @Path("/poller")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.authentication-attempts.poller")
    @ExceptionMetered(name = "exceptions.v3.authentication-attempts.poller")
    @ResponseMetered(name = "response.code.v3.authentication-attempts.poller")
    public void updateAuthAttempt(@Suspended final AsyncResponse asyncResponse, final String pollerTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " updateAuthAttempt : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
                Gson gson = new Gson();
                ApprovalAttemptPollerTO approvalAttemptPollerData = gson.fromJson(pollerTO, ApprovalAttemptPollerTO.class);
               logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                           + Constant.TILT,
                        "Poller callback~", System.currentTimeMillis() + Constant.TILT, approvalAttemptPollerData.getApplicationId(), Constant.TILT, approvalAttemptPollerData.getTransactionId(),
                        "~poller callback received"));
                EsbResponseTO esbResponseTO = authAttemptFacadeIntf.processPollerCallback(approvalAttemptPollerData);
                String responseBody = gson.toJson(esbResponseTO);
                responseStatus = Response.Status.OK;
                response = responseBody;
            }
            catch (AuthException e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_UPDATE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO =
                                new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_UPDATE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " updateAuthAttempt : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the approval attempt.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param serviceName the service name
     * @param applicationLabel the application label
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the approval attempt
     */
    @Deprecated
    @ApiLogger
    @InternalSecure
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.authentication-attempts")
    @ExceptionMetered(name = "exceptions.v3.authentication-attempts")
    @ResponseMetered(name = "response.code.v3.authentication-attempts")
    public void getApprovalAttempt(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId, @HeaderParam("Service-Name") String serviceName,
            @HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {

        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " getApprovalAttempt : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String transactionId = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TRANSACTION_ID.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.TRANSACTION_ID);
                String errorMessage = ValidationUtil.isValidForGetApprovalStatus(applicationLabel, transactionId);
                if (errorMessage == null) {
                    AuthenticationAttemptTO approvalAttemptTOResponse = authAttemptFacadeIntf.getApprovalAttempt(applicationId, transactionId);
                    responseStatus = Response.Status.OK;
                    response = approvalAttemptTOResponse;
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                               + Constant.TILT,
                            "Aync Get call~", Constant.TILT, applicationId, Constant.TILT, transactionId, "~get call Done"));

                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_TRANSACTION_ID());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (AuthException e) {
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_APPROVAL_ATTEMPT_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPROVAL_ATTEMPT_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " getApprovalAttempt : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }


    /**
     * Creates the approval attempt.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param serviceName the service name
     * @param applicationLabel the application label
     * @param approvalAttemptTO the approval attempt TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    @InternalSecure
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.authentication-attempts")
    @ExceptionMetered(name = "exceptions.v3.authentication-attempts")
    @ResponseMetered(name = "response.code.v3.authentication-attempts")
    @ValidateLicense
    public void createApprovalAttempt(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId, @HeaderParam("Service-Name") String serviceName,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecrete, final AuthenticationAttemptTO approvalAttemptTO, @HeaderParam("request-reference-number") String reqRefNumber) {

        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " createApprovalAttempt : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                       + Constant.TILT,
                    CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                    approvalAttemptTO.getApprovalAttemptType(), "~Approval attempt generation called"));
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {

                approvalAttemptTO.setServiceName(serviceName);
                String errorMessage = ValidationUtilV3.isValidForCreateApproval(approvalAttemptTO);
                if (errorMessage == null) {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                               + Constant.TILT,
                            CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                            approvalAttemptTO.getApprovalAttemptType(), "~Approval attempt validation successful"));
                  //  String serverId = Config.getInstance().getProperty(Constant.SERVER_ID);
                   // serverId = serverId != null ? serverId : Constant._ASTERICKS;
                    AuthenticationAttemptTO approvalAttemptTOResponse = authAttemptFacadeIntf.createApprovalAttempt(null, approvalAttemptTO, applicationId);
                    approvalAttemptTOResponse.setApplicationId(applicationId);
                    approvalAttemptTOResponse.setApplicationSecrete(applicationSecrete);
                    responseStatus = Response.Status.OK;
                    response = approvalAttemptTOResponse;
                }
                else {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                               + Constant.TILT,
                            CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                            approvalAttemptTO.getApprovalAttemptType(), "~Approval attempt validation failed"));
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (UserBlockedException e) {
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                           + Constant.TILT,
                        CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                        approvalAttemptTO.getApprovalAttemptType(), "~user is blocked"));
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_USER_BLOCK(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED(), errorConstant.getERROR_MESSAGE_USER_BLOCK());
                responseStatus = Response.Status.FORBIDDEN;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            catch (AuthException e) {
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                           + Constant.TILT,
                        CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                        approvalAttemptTO.getApprovalAttemptType(), Constant.TILT, e.getMessage()));
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                           + Constant.TILT,
                        CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                        approvalAttemptTO.getApprovalAttemptType(), "~internal server error"));
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " createApprovalAttempt : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger

    @Secured
    @ResponseToken
    @GET
    @Path("/audit-trail")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.authentication-attempts.audit-trail")
    @ExceptionMetered(name = "exceptions.v3.authentication-attempts.audit-trail")
    @ResponseMetered(name = "response.code.v3.authentication-attempts.audit-trail")
    public void getAuthAttemptAuditTrail(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = Constant.X_QUERY) String queryParams) {

        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            String applicationId, searchText;
            Long startDate, endDate;
            Integer page;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                applicationId = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.APPLICATION_ID.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.APPLICATION_ID);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                Long fromDate = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.FROM_DATE.getKey()), in.fortytwo42.adapter.enums.QueryParam.FROM_DATE);
                Long toDate = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TO_DATE.getKey()), in.fortytwo42.adapter.enums.QueryParam.TO_DATE);
                String errorMessage = ValidationUtil.isValidForGetAuthAttemptAuditTrail(applicationId, searchText);
                if (errorMessage == null) {
                    PaginatedTO<AuthenticationAttemptHistoryTO> approvalAttemptTOResponse = authAttemptFacadeIntf.getAuthAttemptAuditTrail(page, applicationId, searchText, fromDate, toDate);
                    responseStatus = Response.Status.OK;
                    response = approvalAttemptTOResponse;
                    //    				IAMLogger.getInstance().log(Level.DEBUG, StringUtil.build(Constant.RANDOM,Thread.currentThread().getId()
                    //    						+Constant.TILT,"Sync Get call~",Constant.TILT,applicationId,Constant.TILT,transactionId,"~get call Done"));

                }
                else {
                    //    				auditLoggingProcessorIntf.log(AuthenticationAttempt.class, applicationId, AuditLogConstant.AUTHENTICATION_ATTEMPTS,
                    //    						StringUtil.build(AuditLogConstant.ERROR_INVALID_DATA_FOR_GET_APPROVAL_ATTEMPT, transactionId), AuditLogType.ERROR);
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                //    			auditLoggingProcessorIntf.log(AuthenticationAttempt.class, applicationId, AuditLogConstant.AUTHENTICATION_ATTEMPTS,
                //    					StringUtil.build(AuditLogConstant.ERROR_INVALID_DATA_FOR_GET_APPROVAL_ATTEMPT, null), AuditLogType.ERROR);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_TRANSACTION_ID());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            //    		catch (AuthException e) {
            ////    			auditLoggingProcessorIntf.log(AuthenticationAttempt.class, applicationId, AuditLogConstant.AUTHENTICATION_ATTEMPTS, e.getMessage(), AuditLogType.ERROR);
            //    			IAMLogger.getInstance().log(Level.ERROR, e);
            //    			ErrorTO errorTO = new ErrorTO(e.getErrorCode(),errorConstant.getHUMANIZED_GET_APPROVAL_ATTEMPT_FAILED,e.getMessage());
            //    			responseStatus = Response.Status.BAD_REQUEST;
            //    			response = errorTO;
            //    		}
            catch (Exception e) {
                //    			auditLoggingProcessorIntf.log(AuthenticationAttempt.class, applicationId, AuditLogConstant.AUTHENTICATION_ATTEMPTS, e.getMessage(), AuditLogType.ERROR);
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPROVAL_ATTEMPT_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/qr")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.authentication-attempts.qr")
    @ExceptionMetered(name = "exceptions.v3.authentication-attempts.qr")
    @ResponseMetered(name = "response.code.v3.authentication-attempts.qr")
    @ValidateLicense
    public void createQRBasedApprovalAttempt(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam("Service-Name") String serviceName,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecrete, final AuthenticationAttemptTO approvalAttemptTO, @HeaderParam("request-reference-number") String reqRefNumber) {

        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " createQRBasedApprovalAttempt : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                       + Constant.TILT,
                    CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                    approvalAttemptTO.getApprovalAttemptType(), "~Approval attempt generation called"));
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {

                approvalAttemptTO.setServiceName(serviceName);
                String errorMessage = ValidationUtilV3.isValidForCreateApproval(approvalAttemptTO);
                if (errorMessage == null) {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                               + Constant.TILT,
                            CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                            approvalAttemptTO.getApprovalAttemptType(), "~Approval attempt validation successful"));
                    QRCodeDataTO qrCodeData = authAttemptFacadeIntf.createQRBasedApprovalAttempt(null, approvalAttemptTO, applicationId);
                    qrCodeData.setApplicationId(applicationId);
                    qrCodeData.setApplicationSecrete(applicationSecrete);
                    responseStatus = Response.Status.OK;
                    response = qrCodeData;
                }
                else {
                    logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                               + Constant.TILT,
                            CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                            approvalAttemptTO.getApprovalAttemptType(), "~Approval attempt validation failed"));
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                           + Constant.TILT,
                        CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                        approvalAttemptTO.getApprovalAttemptType(), Constant.TILT, e.getMessage()));
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Thread.currentThread().getId()
                                                                                           + Constant.TILT,
                        CREATE_APPROVAL_ATTEMPT, System.currentTimeMillis() + Constant.TILT, applicationId, Constant.TILT, approvalAttemptTO.getTransactionId(), Constant.TILT,
                        approvalAttemptTO.getApprovalAttemptType(), "~internal server error"));
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_APPROVAL_ATTEMPT_CREATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " createQRBasedApprovalAttempt : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/details")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.report-view")
    @ExceptionMetered(name = "exceptions.v3.report-view")
    @ResponseMetered(name = "response.code.v3.report-view")
    public void getTransactionDetails(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam("Authorization") String authorizationHeader,
            TransactionReportRequestTO requestTO,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " getTransactionDetails : start");
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            Status responseStatus = null;
            Object response = null;
            String errorMessage;
            in.fortytwo42.tos.transferobj.ErrorTO errorTO = null;
            try {
                errorMessage = ValidationUtilV3.validateTransactionDetailsTo(requestTO);
                if (requestTO.getTransactionId() != null) {
                    errorMessage = ValidationUtilV3.isDataValidSearchText(requestTO.getTransactionId());
                }
                if (errorMessage != null) {
                    errorTO = new in.fortytwo42.tos.transferobj.ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                }
                if (errorTO == null) {
                    response = authAttemptFacadeIntf.getTransactionDetails(requestTO);
                    responseStatus = Status.OK;
                }
                else {
                    response = errorTO;
                    responseStatus = Status.BAD_REQUEST;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage());
                errorTO = new in.fortytwo42.tos.transferobj.ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_POLICY_FAILED(), e.getMessage());
                asyncResponse.resume(Response.status(Status.BAD_REQUEST).header("Content-Type", "application/json").entity(errorTO).build());

            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                errorTO = new in.fortytwo42.tos.transferobj.ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), e.getMessage(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                asyncResponse.resume(Response.status(Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
            }
            finally {
                logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " getTransactionDetails : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }
}
