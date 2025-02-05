
package in.fortytwo42.adapter.webservice.v4;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import in.fortytwo42.adapter.processor.AuditLogProcessorImpl;
import in.fortytwo42.adapter.processor.AuditLogProcessorIntf;
import in.fortytwo42.adapter.util.RSAUtil;
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
import in.fortytwo42.adapter.facade.HOTPFacadeIntf;
import in.fortytwo42.adapter.facade.UserFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.UserAuthenticationTO;
import in.fortytwo42.adapter.transferobj.UserIciciStatusTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.transferobj.UserResponseTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.enums.OtpAction;
import in.fortytwo42.entities.enums.OtpStatus;
import in.fortytwo42.tos.transferobj.HotpTO;

@Path("/v4/users")
public class UserResourceV4 {

    private static final String USER_TO_JSON_LOG = "<<<<< userTO : ";

    private static final String USER_RESOURCE_API_LOG = "<<<<< UserResourceV4";

    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();
    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private final AuditLogProcessorIntf auditLogProcessor = AuditLogProcessorImpl.getInstance();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(UserResourceV4.class);


    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER" })
    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/onboard_user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.users.change_user_password")
    @ExceptionMetered(name = "exceptions.v4.users.change_user_password")
    @ResponseMetered(name = "response.code.v4.users.change_user_password")
    @ValidateLicense
    public void onboardUser(@Suspended final AsyncResponse asyncResponse, UserIciciTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            long onboardUserStart = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> onboardUser |Epoch:"+ onboardUserStart);

            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Response.Status responseStatus;
            Object response;
            boolean addHeader = false;
            String headerValue = "";
            try {
                logger.log(Level.DEBUG, USER_TO_JSON_LOG );
                long isRequestValidForOnboardUserV4Start = System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> ValidationUtilV3.isRequestValidForOnboardUserV4 |Epoch:"+isRequestValidForOnboardUserV4Start);
                String errorMessage = ValidationUtilV3.isRequestValidForOnboardUserV4(userTO);
                long isRequestValidForOnboardUserV4End = System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> ValidationUtilV3.isRequestValidForOnboardUserV4 |Epoch:"+ isRequestValidForOnboardUserV4End);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> ValidationUtilV3.isRequestValidForOnboardUserV4 |Epoch:"+ (isRequestValidForOnboardUserV4End - isRequestValidForOnboardUserV4Start));

                if (errorMessage == null) {
                    UserIciciTO userTOResponse;
                    long onboardUserV4 = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> userFacade.onboardUserV4 |Epoch:"+ onboardUserV4);
                    userTOResponse = userFacade.onboardUserV4(userTO);
                    long onboardUserV4End = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> userFacade.onboardUserV4 |Epoch:"+onboardUserV4End);
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> userFacade.onboardUserV4 |Epoch:"+(onboardUserV4End-onboardUserV4));

                    response = userTOResponse;
                    responseStatus = Response.Status.OK;
                    if (userTOResponse.getStatus().equals(Constant.FAILED) && userTOResponse.getErrorCode().equals(errorConstant.getVALIDATION_ERROR_CODE())) {
                        String errorMessage1 = userTOResponse.getErrorMessage();
                        if (errorMessage1 != null) {
                            String[] parts = errorMessage1.split(":");
                            if (parts.length == 2) {
                                String attributeName = parts[0].trim();
                                if (attributeName.equals(Constant.USER_ID)) {
                                    addHeader = true;
                                    headerValue = userTOResponse.getErrorCode() + "U";
                                } else if (attributeName.equals(Constant.MOBILE_NO)) {
                                    addHeader = true;
                                    headerValue = userTOResponse.getErrorCode() + "M";
                                }
                            }
                        }
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
                ErrorTO errorTO ;
                if(e.getMessage()!=null){
                    errorTO= new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getHUMANIZED_USER_ONBOARD_FAILED());
                }
                else errorTO=new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_USER_ONBOARD_FAILED(), errorConstant.getHUMANIZED_USER_ONBOARD_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_USER_ONBOARD_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUser : end");
            }

            long onboardUserEnd = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> onboardUser |Epoch:"+onboardUserEnd);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> onboardUser |Epoch:"+(onboardUserEnd-onboardUserStart));
            if (addHeader) {
                asyncResponse.resume(Response.status(responseStatus).entity(response).header(Constant.X_DIMFA_RESPONSE_ERROR_CODE, headerValue).build());
            } else {
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER" })
    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/change_password")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.users.change_user_password")
    @ExceptionMetered(name = "exceptions.v4.users.change_user_password")
    @ResponseMetered(name = "response.code.v4.users.change_user_password")
    @ValidateLicense
    public void changeUserPassword(@Suspended final AsyncResponse asyncResponse, UserIciciTO userTO, @HeaderParam("Application-Id") String applicationId, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            long changeUserPassword = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> changeUserPassword |Epoch:"+changeUserPassword);

            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " changeUserPassword : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());

            Response.Status responseStatus;
            Object response;
            try {
                logger.log(Level.DEBUG, USER_TO_JSON_LOG );
                long isRequestValidForPasswordChangeStart = System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> ValidationUtilV3.isRequestValidForPasswordChange |Epoch:"+isRequestValidForPasswordChangeStart);
                String errorMessage = ValidationUtilV3.isRequestValidForPasswordChange(userTO);
                long isRequestValidForPasswordChangeEnd = System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> ValidationUtilV3.isRequestValidForPasswordChange |Epoch:"+isRequestValidForPasswordChangeEnd);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> ValidationUtilV3.isRequestValidForPasswordChange |Epoch:"+(isRequestValidForPasswordChangeEnd-isRequestValidForPasswordChangeStart));

                if (errorMessage == null) {
                    UserIciciTO userTOResponse;
                    long changeUserPasswordStart = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> userFacade.changeUserPassword |Epoch:"+changeUserPasswordStart);
                    userTO.setApplicationId(applicationId);
                    userTOResponse = userFacade.changeUserPassword(userTO);
                    long changeUserPasswordEND = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> userFacade.changeUserPassword |Epoch:"+changeUserPasswordEND);
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> userFacade.changeUserPassword |Epoch:"+(changeUserPasswordEND-changeUserPasswordStart));

                    response = userTOResponse;
                    responseStatus = Response.Status.OK;

                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorMessage, errorConstant.getUPDATE_PASSWORD_FAILED());
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getHUMANIZED_CHANGE_PASSWORD_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " changeUserPassword : end");
            }
            long changeUserPasswordEnd = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> changeUserPassword |Epoch:"+changeUserPasswordEnd);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> changeUserPassword |Epoch:"+(changeUserPasswordEnd-changeUserPassword));

            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER" })
    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/add_attribute")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.users.add_attribute")
    @ExceptionMetered(name = "exceptions.v4.users.add_attribute")
    @ResponseMetered(name = "response.code.v4.users.add_attribute")
    @ValidateLicense
    public void addUserAttributes(@Suspended final AsyncResponse asyncResponse, UserIciciTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            long addUserAttributes = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> addUserAttributes |Epoch:"+addUserAttributes);

            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " addUserAttributes : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());

            Response.Status responseStatus;
            Object response;
            try {
                logger.log(Level.DEBUG, USER_TO_JSON_LOG );
                long isRequestValidForAttributeAddition = System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> ValidationUtilV3.isRequestValidForAttributeAddition |Epoch:"+isRequestValidForAttributeAddition);
                String errorMessage = ValidationUtilV3.isRequestValidForAttributeAddition(userTO);
                long isRequestValidForAttributeAdditionEnd = System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> ValidationUtilV3.isRequestValidForAttributeAddition |Epoch:"+isRequestValidForAttributeAdditionEnd);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> ValidationUtilV3.isRequestValidForAttributeAddition |Epoch:"+(isRequestValidForAttributeAdditionEnd-isRequestValidForAttributeAddition));

                if (errorMessage == null) {
                    UserIciciTO userTOResponse;

                    long addAttributes = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> userFacade.addAttributes |Epoch:"+addAttributes);
                    userTOResponse = userFacade.addAttributes(userTO);
                    long addAttributesEnd = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> userFacade.addAttributes |Epoch:"+addAttributesEnd);
                    logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> userFacade.addAttributes |Epoch:"+(addAttributesEnd-addAttributes));

                    response = userTOResponse;
                    responseStatus = Response.Status.OK;

                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getHUMANIZED_ATTRIBUTE_ADDITION_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ATTRIBUTE_ADDITION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " addUserAttributes : end");
            }

            long addUserAttributesEND = System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> addUserAttributes |Epoch:"+addUserAttributesEND);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF ->UserResource -> addUserAttributes |Epoch:"+(addUserAttributesEND-addUserAttributes));
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers")
    @ExceptionMetered(name = "exceptions.v3.consumers")
    @ResponseMetered(name = "response.code.v3.consumers")
    public void userStatus(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_SERVICE_NAME) String serviceName,
            UserIciciStatusTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " userStatus : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus = null;
            Object response = null;
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForUserStatus(userTO);
                if (errorMessage == null) {
                    UserIciciStatusTO userIciciStatusTO = userFacade.userStatus(userTO, applicationId, serviceName);
                    responseStatus = Response.Status.OK;
                    response = userIciciStatusTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_STATUS_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USER_STATUS_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " userStatus : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    /**
     * Login AD or No AD user.
     *
     * @param asyncResponse the async response
     * @param request the request
     * @param user the user
     * @param clientIp the client ip
     * @param userAgent the user agent
     */
    @ApiLogger
    @POST
    @Path("/authenticate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.users.authenticate")
    @ExceptionMetered(name = "exceptions.v4.users.authenticate")
    @ResponseMetered(name = "response.code.v4.users.authenticate")
    @ValidateLicense
    public void login(@Suspended final AsyncResponse asyncResponse, @Context HttpServletRequest request, UserAuthenticationTO user, @HeaderParam(Constant.X_FORWARDED_FOR) String clientIp,
            @HeaderParam("User-Agent") String userAgent, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " login : start");
            Response.Status responseStatus;
            Object response;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            String userName = CryptoJS.decryptData(Config.getInstance().getProperty(Constant.AD_ENCRYPTION_KEY), user.getUsername());
            user.setUsername(userName);
            try {
                String errorMessage = ValidationUtil.isAdminValidForAuthentication(user);
                if (errorMessage == null) {
                    String ipAddress = "";
                    if (clientIp != null) {
                        ipAddress += clientIp;
                    }
                    ipAddress += request.getRemoteAddr();
                    logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " login : debuglogs" + ipAddress + userAgent + clientIp);
                    UserResponseTO userResponseTO = userFacade.authenticateADorNonADUser(user, ipAddress, userAgent);
                    userResponseTO.setUsername(user.getUsername());
                    responseStatus = Response.Status.OK;
                    response = userResponseTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_AUTHENTICATION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO =
                        new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " login : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @POST
    @Path("/token_validate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.users.token_validate")
    @ExceptionMetered(name = "exceptions.v4.users.token_validate")
    @ResponseMetered(name = "response.code.v4.users.token_validate")
    @ValidateLicense
    public void tokenValidation(@Suspended final AsyncResponse asyncResponse, @Context HttpServletRequest request, @HeaderParam("request-reference-number") String reqRefNumber,
            UserAuthenticationTO user, @HeaderParam(Constant.X_FORWARDED_FOR) String clientIp, @HeaderParam("User-Agent") String userAgent,
            @HeaderParam(value = HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " tokenValidation : start");
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Response.Status responseStatus;
            Object response;
            try {
                String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
                boolean isTokenValid = JWTokenImpl.parseAndVerifyJWT(authToken, KeyManagementUtil.getAESKey());
                String grantType = (String) JWTokenImpl.getClaimWithoutValidation(authToken, Constant.GRANT_TYPE);
                if (!isTokenValid || !StringUtil.isNotNullOrEmpty(grantType) || !grantType.equals(Constant.MFA)) {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_TOKEN(), errorConstant.getERROR_MESSAGE_INVALID_TOKEN(), errorConstant.getERROR_DEV_MESSAGE_INVALID_TOKEN());
                    responseStatus = Response.Status.UNAUTHORIZED;
                    response = errorTO;
                }
                else {
                    String errorMessage = ValidationUtilV3.isAdminValidForTokenValidation(user);
                    if (errorMessage == null) {
                        String ipAddress = "";
                        if (clientIp != null) {
                            ipAddress += clientIp;
                        }
                        ipAddress += request.getRemoteAddr();
                        String userName = CryptoJS.decryptData(Config.getInstance().getProperty(Constant.AD_ENCRYPTION_KEY), user.getUsername());
                        if(user.getIsCredentialsEncrypted()){
                            user.setToken(CryptoJS.decryptData(Config.getInstance().getProperty(Constant.AD_ENCRYPTION_KEY), user.getToken()));
                        }
                        UserResponseTO userResponseTO = FacadeFactory.getHOTPFacade().tokenValidation(userName, ipAddress, userAgent, user.getToken());
                        if (Constant.SUCCESS_STATUS.equals(userResponseTO.getStatus())) {
                            userResponseTO.setUsername(userName);
                            responseStatus = Response.Status.OK;
                            response = userResponseTO;
                        }
                        else {
                            ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_USER_NAME_TOKEN_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_TOKEN_INVALID(),
                                    errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED());
                            responseStatus = Response.Status.BAD_REQUEST;
                            response = errorTO;
                        }
                    }
                    else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " tokenValidation : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/authentication_token/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.users.authentication_token/validate")
    @ExceptionMetered(name = "exceptions.v4.users.authentication_token/validate")
    @ResponseMetered(name = "response.code.v4.users.authentication_token/validate")
    public void validateHOTP(@Suspended final AsyncResponse asyncResponse,
                             @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
                             @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecret,
                             HotpTO hotpTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validateHOTP : start");
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Response.Status responseStatus;
            Object response;
            try {
                String token = hotpTO.getAuthenticationToken();
                ErrorTO decrError =  null;

                try {
                    hotpTO.setAuthenticationToken(RSAUtil.decryptData(token));
                } catch (Exception e){
                    logger.log(Level.ERROR, e.getMessage(), e);
                    decrError = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
                }
                if(decrError == null){
                    HOTPFacadeIntf hotpFacade = FacadeFactory.getHOTPFacade();
                    HotpTO otp = hotpFacade.validateOtp(hotpTO, applicationId);
                    otp.setAuthenticationToken(token);
                    response = new Gson().toJson(otp);
                    responseStatus = Response.Status.OK;
                } else {
                    response = decrError;
                    responseStatus = Response.Status.BAD_REQUEST;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED());
                auditLogProcessor.addOtpAuditValidateLogs(applicationId, hotpTO.getSearchAttributes(), OtpAction.VO, OtpStatus.F, "");
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                auditLogProcessor.addOtpAuditValidateLogs(applicationId, hotpTO.getSearchAttributes(), OtpAction.VO, OtpStatus.F, "");
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validateHOTP : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });


    }
}
