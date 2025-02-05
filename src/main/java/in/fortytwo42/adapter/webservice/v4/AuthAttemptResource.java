
package in.fortytwo42.adapter.webservice.v4;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import in.fortytwo42.tos.transferobj.HeaderParamsTO;
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
import in.fortytwo42.adapter.exception.UserBlockedException;
import in.fortytwo42.adapter.facade.AuthAttemptFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.QRCodeDataTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;

@Path("/v4/authentication-attempts")
public class AuthAttemptResource {

    private static final String CREATE_APPROVAL_ATTEMPT = "Create Approval attempt~";

    private AuthAttemptFacadeIntf authAttemptFacadeIntf = FacadeFactory.getAuthAttemptFacade();

    private static final String AUTH_ATTEMPT_RESOURCE_LOG = "<<<<< AuthAttemptResource V4";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(AuthAttemptResource.class);

    @ApiLogger

    @InternalSecure
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
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
                 //   String serverId = Config.getInstance().getProperty(Constant.SERVER_ID);
                  //  serverId = serverId != null ? serverId : Constant._ASTERICKS;
                    AuthenticationAttemptTO approvalAttemptTOResponse = authAttemptFacadeIntf.createApprovalAttempt(asyncResponse, approvalAttemptTO, applicationId);
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
                    asyncResponse.resume(Response.status(responseStatus).entity(response).build());
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
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
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
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            finally {
                logger.log(Level.DEBUG, AUTH_ATTEMPT_RESOURCE_LOG + " createApprovalAttempt : end");
            }
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
                    //  String serverId = Config.getInstance().getProperty(Constant.SERVER_ID);
                    //  serverId = serverId != null ? serverId : Constant._ASTERICKS;
                    QRCodeDataTO qrCodeData = authAttemptFacadeIntf.createQRBasedApprovalAttemptV4(null, approvalAttemptTO, applicationId);
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
    @InternalSecure
    @POST
    @Path("/approval-attempt")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.authentication-attempts.approval-attempt")
    @ExceptionMetered(name = "exceptions.v4.authentication-attempts.approval-attempt")
    @ResponseMetered(name = "response.code.v4.authentication-attempts.approval-attempt")
    public void getApprovalAttempt(@Suspended final AsyncResponse asyncResponse,
                                   @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId, @HeaderParam("Service-Name") String serviceName,
                                   @HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel, HeaderParamsTO  queryParams,
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
                String xQuery = queryParams.getXQuery();
                Map<String, String> queryParam = StringUtil.parseQueryParams(xQuery);
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

}
