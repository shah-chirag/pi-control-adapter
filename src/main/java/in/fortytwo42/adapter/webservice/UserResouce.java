
package in.fortytwo42.adapter.webservice;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.management.AttributeNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import in.fortytwo42.adapter.enums.QueryParam;
import in.fortytwo42.adapter.processor.AuditLogProcessorImpl;
import in.fortytwo42.adapter.processor.AuditLogProcessorIntf;
import in.fortytwo42.entities.enums.OtpAction;
import in.fortytwo42.entities.enums.OtpStatus;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.AdHotpFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.HOTPFacadeIntf;
import in.fortytwo42.adapter.facade.NonADUserFacadeIntf;
import in.fortytwo42.adapter.facade.UserFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ADUserBindingTO;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.CryptoTokenTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.PasswordTO;
import in.fortytwo42.adapter.transferobj.UserAuthenticationTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.transferobj.UserResponseTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.ApprovalAttemptV2;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.ApprovalAttemptMode;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.RandomString;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;
import in.fortytwo42.entities.enums.UserRole;
import in.fortytwo42.tos.transferobj.AdHotpTO;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.HotpTO;
import in.fortytwo42.tos.transferobj.UserTO;

@Path("/v3/users")
public class UserResouce {

    private static final String USER_RESOURCE_API_LOG = "<<<<< UserResource";
    private static final String APPLICATION_OCTET_STREAM_HEADER = "application/octet-stream";
    private static final String ATTACHMENT_FILENAME_HEADER = "attachment;filename=";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String VERIFY_CRYPTO_TOKEN_END_LOG = " verifyCryptoToken : end";
    private static final String VERIFY_CRYPTO_TOKEN_START_LOG = " verifyCryptoToken : start";
    private static final String ONBOARD_USER = "ONBOARD_USER";
    private static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";

    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();
    private static final String OTHER = "OTHER";
    /** The user facade intf. */
    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
    private NonADUserFacadeIntf nonADUserFacade = FacadeFactory.getNonADUserFacade();
    private Config config = Config.getInstance();
    private HOTPFacadeIntf hotpFacade = FacadeFactory.getHOTPFacade();
    private AdHotpFacadeIntf adHotpFacade = FacadeFactory.getAdHOTPFacade();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    /**
     * creation of log 4j object for each class
    */
   private static Logger logger=LogManager.getLogger(UserResouce.class);
    private final AuditLogProcessorIntf auditLogProcessor = AuditLogProcessorImpl.getInstance();


    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER", "USER_MAKER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/passwords")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users")
    @ExceptionMetered(name = "exceptions.v3.users")
    @ResponseMetered(name = "response.code.v3.users")
    @ValidateLicense
    public void validatePassword(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, final PasswordTO passwordTO,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validatePassword : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            //            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                userFacade.validatePassword(passwordTO);
                responseStatus = Response.Status.OK;
                response = passwordTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ERROR_VALIDATE_PASSWORD(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ERROR_VALIDATE_PASSWORD() + " - " + e.getMessage(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validatePassword : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER", "USER_MAKER","USER_CHECKER","USER_VIEWONLY", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/password-policies/{accountType}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.password-policies.accountType")
    @ExceptionMetered(name = "exceptions.v3.users.password-policies.accountType")
    @ResponseMetered(name = "response.code.v3.users.password-policies.accountType")
    public void getPasswordPolicies(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber, @PathParam(value = "accountType") final String accountType) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validatePassword : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            //            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                responseStatus = Response.Status.OK;
                response = userFacade.getPasswordPolicies(accountType);
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ERROR_VALIDATE_PASSWORD(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ERROR_VALIDATE_PASSWORD() + " - " + e.getMessage(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validatePassword : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER","USER_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/questions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.questions")
    @ExceptionMetered(name = "exceptions.v3.users.questions")
    @ResponseMetered(name = "response.code.v3.users.questions")
    public void getQuestions(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getQueuestions : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            //            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                responseStatus = Response.Status.OK;
                response = userFacade.getQuestions();
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ERROR_AUDIT_TRAIL(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ERROR_AUDIT_TRAIL() + " - " + e.getMessage(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getQueuestions : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/onboard_users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Timed(name = "timer.v3.users.onboard_users")
    @ExceptionMetered(name = "exceptions.v3.users.onboard_users")
    @ResponseMetered(name = "response.code.v3.users.onboard_users")
    @ValidateLicense
    public void uploadOnboardUsers(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(Constant.FILE_NAME) String fileName, @HeaderParam(Constant.HEADER_FILE_TYPE) String fileType, final InputStream attributeInputStream,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " uploadOnboardUsers : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            try {
                String errorMessage = ValidationUtil.isValidCSV(fileName);
                if (errorMessage == null) {
                    CSVUploadTO csvUploadTO = userFacade.uploadOnboardUsers(fileType, attributeInputStream, role,actor,id, fileName);
                    response = csvUploadTO;
                    responseStatus = Response.Status.OK;
                    /*asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", "application/octet-stream")
                            .header("Content-Disposition", "attachment;filename=" + filename).build());*/
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_FILE_TYPE(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            }
            catch (AuthException e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            catch (Exception e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " uploadOnboardUsers : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER", "USER_MAKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/onboard")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.onboard")
    @ExceptionMetered(name = "exceptions.v3.users.onboard")
    @ResponseMetered(name = "response.code.v3.users.onboard")
    @ValidateLicense
    public void onboardUser(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, UserTO userTO,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);

            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id=Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                logger.log(Level.DEBUG, "<<<<< userTO : " + new Gson().toJson(userTO));
                String errorMessage = ValidationUtilV3.isRequestValidForOnboardUser(userTO);
                if (errorMessage == null) {
                    UserTO userTOResponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    userTOResponse = userFacade.onboardUser(userTO, role, actor,id, true, saveRequest);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getHUMANIZED_USER_ONBOARD_FAILED());
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
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUser : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER" })
    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/onboard_user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.onboard_user")
    @ExceptionMetered(name = "exceptions.v3.users.onboard_user")
    @ResponseMetered(name = "response.code.v3.users.onboard_user")
    @ValidateLicense
    public void onboardUserAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Application-Id") String applicationId, UserIciciTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            long startTime=System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "START ->UserResource -> onboardUserAttribute |Epoch:"+startTime);

            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUserAttributeonboardUserAttribute : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Session session = sessionFactoryUtil.openSessionWithoutTransaction();
            IamThreadContext.setSessionWithoutTransaction(session);

            Status responseStatus;
            Object response;
            boolean addHeader = false;
            String headerValue = "";
            try {
                logger.log(Level.DEBUG, "<<<<< userTO : " + new Gson().toJson(userTO));
                String requestType = ValidationUtilV3.validateAndgetRequestType(userTO);
                userTO.setApplicationId(applicationId);
                UserIciciTO userTOResponse = null;
                String errorMessage = null;
                switch (requestType) {
                    case ONBOARD_USER:
                        errorMessage = ValidationUtilV3.isRequestValidForOnboardUserV4(userTO);
                        if (errorMessage == null) {
                            userTOResponse = userFacade.onboardUserV4(userTO);
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
                        break;
                    case CHANGE_PASSWORD:
                        errorMessage = ValidationUtilV3.isRequestValidForPasswordChange(userTO);
                        if (errorMessage == null) {
                            userTOResponse = userFacade.changeUserPassword(userTO);
                            response = userTOResponse;
                            responseStatus = Response.Status.OK;
                        }
                        else {
                            ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                            responseStatus = Response.Status.BAD_REQUEST;
                            response = errorTO;
                        }
                        break;
                    default:
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), "Request format not matched for Onboard or Change password");
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                        break;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getHUMANIZED_USER_ONBOARD_FAILED());
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
                sessionFactoryUtil.closeSessionWithoutCommit(session);
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUserAttributeonboardUserAttribute : end");
                long endTimeProcess=System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->UserResource -> onboardUserAttribute |Epoch:"+endTimeProcess);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF "+(endTimeProcess-startTime));

            }
            if (addHeader) {
                asyncResponse.resume(Response.status(responseStatus).entity(response).header(Constant.X_DIMFA_RESPONSE_ERROR_CODE, headerValue).build());
            } else {
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
        });
    }

    /**
     * Login.
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
    @Timed(name = "timer.v3.users.authenticate")
    @ExceptionMetered(name = "exceptions.v3.users.authenticate")
    @ResponseMetered(name = "response.code.v3.users.authenticate")
    @ValidateLicense
    public void login(@Suspended final AsyncResponse asyncResponse, @Context HttpServletRequest request, UserAuthenticationTO user, @HeaderParam(Constant.X_FORWARDED_FOR) String clientIp,
            @HeaderParam("User-Agent") String userAgent, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " login : start");
            Status responseStatus;
            Object response;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
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
                    UserResponseTO userResponseTO = userFacade.authenticate(user, ipAddress, userAgent);
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
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " login : end");
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /*    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/onboard")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void onboardUser(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, UserTO userTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                logger.log(Level.DEBUG, "<<<<< userTO : "+new Gson().toJson(userTO));
                String errorMessage = ValidationUtilV3.isRequestValidForOnboardUser(userTO);
                if (errorMessage == null) {
                    UserTO userTOResponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    userTOResponse = userFacade.onboardUser(userTO, role, actor, true,saveRequest);
                    response = userTOResponse;
                    responseStatus = Response.Status.OK;
    
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA, errorConstant.getERROR_MESSAGE_INVALID_DATA, errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
    
            }
            catch (AuthException e) {
                e.printStackTrace();
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getHUMANIZED_USER_ONBOARD_FAILED);
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR, errorConstant.getHUMANIZED_USER_ONBOARD_FAILED, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR);
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUser : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }*/

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/user-onboard-csv")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.user-onboard-csv")
    @ExceptionMetered(name = "exceptions.v3.users.user-onboard-csv")
    @ResponseMetered(name = "response.code.v3.users.user-onboard-csv")
    public void downloadUserOnboardCsv(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber, @HeaderParam(Constant.HEADER_FILE_TYPE) String fileType) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " downloadUserOnboardCsv : start");
            try {
                String content = userFacade.readSampleCsvFile(fileType);
                if (content != null && !content.isEmpty()) {
                    asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", APPLICATION_OCTET_STREAM_HEADER)
                            .header(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME_HEADER + "user-upload.csv").build());
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_CSV_DOWNLOAD_FAILED(), errorConstant.getHUMANIZED_CSV_DOWNLOAD_FAILED(), errorConstant.getERROR_DEVELOPER_CSV_DOWNLOAD_FAILED());
                    asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
                }
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_CSV_DOWNLOAD_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " downloadUserOnboardCsv : end");
            }
        });
    }

    @ApiLogger
    @POST
    @Secured
    @ResponseToken
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.logout")
    @ExceptionMetered(name = "exceptions.v3.users.logout")
    @ResponseMetered(name = "response.code.v3.users.logout")
    public void logout(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, UserAuthenticationTO user) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(new Runnable() {
            @Override
            public void run() {
                Status responseStatus;
                Object response;
                String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
                Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
                String actor = payload.get(Constant.USER_NAME);
                Long expiry = Long.parseLong(payload.get(Constant.EXPIRY)) * 1000;
                String userName = CryptoJS.decryptData(Config.getInstance().getProperty(Constant.AD_ENCRYPTION_KEY), user.getUsername());
                user.setUsername(userName.toUpperCase());

                IamThreadContext.setCorelationId(UUIDGenerator.generate());
                try {
                    String errorMessage = ValidationUtil.isAdminValidForLogout(user);
                    if (errorMessage == null && user.getUsername().equalsIgnoreCase(actor)) {
                        userFacade.logout(user.getUsername(), authToken, expiry);
                        UserResponseTO userResponseTO = new UserResponseTO();
                        userResponseTO.setUsername(user.getUsername());
                        userResponseTO.setStatus(Constant.SUCCESS_STATUS);
                        responseStatus = Response.Status.OK;
                        response = userResponseTO;
                    }
                    else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
                }
                catch (Exception e) {
                   logger.log(Level.ERROR, e.getMessage(), e);
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_LOGOUT_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                    responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                    response = errorTO;
                }
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
        });
    }

    /**
     * Gets the users.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the users
     */
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "VIEW_ONLY", "SUPER_USER","USER_MAKER","USER_CHECKER","USER_VIEWONLY", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ValidateSearchQuery
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.all")
    @ExceptionMetered(name = "exceptions.v3.users.all")
    @ResponseMetered(name = "response.code.v3.users.all")
    public void getUsers(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getUsers : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            String status, searchText, searchType, attributeName;
            String iamStatusFilter, twoFAStatusFilter, userStatusFilter, approvalStatus, userType;
            String userState;
            Integer page;
            String export;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                userType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.USER_TYPE.getKey()), in.fortytwo42.adapter.enums.QueryParam.USER_TYPE);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                searchType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_TYPE.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_TYPE);
                attributeName = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.ATTRIBUTE_NAME.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_TYPE);
                status = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.STATUS.getKey()), in.fortytwo42.adapter.enums.QueryParam.STATUS);

                iamStatusFilter = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.IAMSTATUS.getKey()), in.fortytwo42.adapter.enums.QueryParam.IAMSTATUS);
                userStatusFilter = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.USER_STATUS.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.USER_STATUS);
                twoFAStatusFilter = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam._2FASTATUS.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam._2FASTATUS);
                approvalStatus = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.APPROVAL_STATUS.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.APPROVAL_STATUS);

                userState = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.USER_STATE.getKey()), in.fortytwo42.adapter.enums.QueryParam.USER_STATE);

                Long userGroupId = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.GROUP_ID.getKey()), in.fortytwo42.adapter.enums.QueryParam.GROUP_ID);
                export = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.EXPORT.getKey()),in.fortytwo42.adapter.enums.QueryParam.EXPORT);
                boolean exportbool = false;
                if(export!=null && export.equalsIgnoreCase(Constant.TRUE)){
                    exportbool = true;
                }
                String errorMessage = ValidationUtil.isDataValidForGetUsers(status, approvalStatus);
                if (errorMessage == null) {
                    int pageNo = (page == null || page < 1) ? 1 : page;
                    PaginatedTO<UserTO> paginatedUserTOs = userFacade.getUsers(UserRole.USER, status, pageNo, searchText,attributeName, iamStatusFilter, userStatusFilter, twoFAStatusFilter, approvalStatus, userState,
                            role, userType, userGroupId, exportbool);
                    responseStatus = Response.Status.OK;
                    response = new Gson().toJson(paginatedUserTOs);
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_STATUS());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USERS_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getUsers : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "VIEW_ONLY", "SUPER_USER","USER_MAKER", "USER_CHECKER", "USER_VIEWONLY" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/user-details")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.user-details")
    @ExceptionMetered(name = "exceptions.v3.users.user-details")
    @ResponseMetered(name = "response.code.v3.users.user-details")
    public void getUserDetails(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getUserDetails : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String accountId = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ACCOUNT_ID.getKey()), in.fortytwo42.adapter.enums.QueryParam.ACCOUNT_ID);
                String errorMessage = ValidationUtil.isDataValidForUserDetails(accountId);
                if (errorMessage == null) {
                    UserTO userTO = userFacade.getUserDetails(accountId, role);
                    responseStatus = Response.Status.OK;
                    response = userTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getHUMANIZED_ERROR_AUDIT_TRAIL(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_USER_UDATE_FAILED(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getUserDetails : end");
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
    @RolesAllowed(value = { "MAKER", "CHECKER", "VIEW_ONLY", "SUPER_USER","USER_MAKER","USER_CHECKER","USER_VIEWONLY" })
    @ApiLogger

    @Secured
    @ResponseToken
    @GET
    @Path("/{userId}/subscribed-applications")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId.subscribed-applications")
    @ExceptionMetered(name = "exceptions.v3.users.userId.subscribed-applications")
    @ResponseMetered(name = "response.code.v3.users.userId.subscribed-applications")
    public void getSubscribedApplications(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getSubscribedApplications : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authorizationHeader);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                List<ApplicationTO> applicationTOs = userFacade.getSubscribedApplicationsByUser(userId, role);
                responseStatus = Response.Status.OK;
                response = applicationTOs;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getSubscribedApplications : end");
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
    @RolesAllowed(value = { "MAKER", "CHECKER", "VIEW_ONLY", "SUPER_USER" })
    @ApiLogger

    @Secured
    @ResponseToken
    @GET
    @Path("/{userId}/tunneling-subscribed-applications")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId.tunneling-subscribed-applications")
    @ExceptionMetered(name = "exceptions.v3.users.userId.tunneling-subscribed-applications")
    @ResponseMetered(name = "response.code.v3.users.userId.tunneling-subscribed-applications")
    public void getTunnelingSubscribedApplications(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getSubscribedApplications : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authorizationHeader);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                List<ApplicationTO> applicationTOs = userFacade.getTunnelingSubscribedApplicationsByUser(userId, role);
                responseStatus = Response.Status.OK;
                response = applicationTOs;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getSubscribedApplications : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Bind AD user.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param userId the user id
     * @param adUserBindingTO the ad user binding TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{userId}/bind")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId.bind")
    @ExceptionMetered(name = "exceptions.v3.users.userId.bind")
    @ResponseMetered(name = "response.code.v3.users.userId.bind")
    public void bindADUser(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            final ADUserBindingTO adUserBindingTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " bindADUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                adUserBindingTO.setUserId(userId);
                String errorMessage = ValidationUtil.isADUserValidForBinding(adUserBindingTO);
                if (errorMessage == null) {
                    boolean isConsumerBindingInitiated = userFacade.autoBindADUser(adUserBindingTO);
                    if (isConsumerBindingInitiated) {
                        adUserBindingTO.setStatus(Constant.SUCCESS_STATUS);
                        responseStatus = Response.Status.OK;
                        response = adUserBindingTO;
                    }
                    else {
                        adUserBindingTO.setStatus(Constant.FAILURE_STATUS);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = adUserBindingTO;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_USER_BINDING_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_USER_BINDING_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " bindADUser : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Unbind AD user.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param userId the user id
     * @param adUserBindingTO the ad user binding TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{userId}/unbind")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId.unbind")
    @ExceptionMetered(name = "exceptions.v3.users.userId.unbind")
    @ResponseMetered(name = "response.code.v3.users.userId.unbind")
    public void unbindADUser(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            ADUserBindingTO adUserBindingTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " unbindADUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                adUserBindingTO.setUserId(userId);
                String errorMessage = ValidationUtil.isADUserValidForBinding(adUserBindingTO);
                if (errorMessage == null) {
                    boolean isConsumerBindingComplete = userFacade.unbindADUser(adUserBindingTO);
                    if (isConsumerBindingComplete) {
                        adUserBindingTO.setStatus(Constant.SUCCESS_STATUS);
                        responseStatus = Response.Status.OK;
                        response = adUserBindingTO;
                    }
                    else {
                        adUserBindingTO.setStatus(Constant.FAILURE_STATUS);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = adUserBindingTO;
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
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " unbindADUser : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Bind service.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param userId the user id
     * @param userBindingTO the user binding TO
     * @param reqRefNumber the req ref number
     */
    @RolesAllowed(value = { "MAKER", "SUPER_USER", "USER_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{userId}/bind-services")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId.bind-services")
    @ExceptionMetered(name = "exceptions.v3.users.userId.bind-services")
    @ResponseMetered(name = "response.code.v3.users.userId.bind-services")
    public void bindService(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            UserBindingTO userBindingTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " bindService : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authorizationHeader);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtilV3.isValidForUserBinding(userBindingTO);
                if (errorMessage == null) {
                    userBindingTO.setId(userId);
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    userFacade.bindServicesToUser(userBindingTO, role, actor,id, saveRequest);
                    userBindingTO.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = userBindingTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_BIND_SERVICES(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_BIND_SERVICES(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " bindService : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Unbind service.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param userId the user id
     * @param userBindingTO the user binding TO
     * @param reqRefNumber the req ref number
     */
    @RolesAllowed(value = { "MAKER", "SUPER_USER", "USER_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{userId}/unbind-services")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId.unbind-services")
    @ExceptionMetered(name = "exceptions.v3.users.userId.unbind-services")
    @ResponseMetered(name = "response.code.v3.users.userId.unbind-services")
    public void unbindService(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            UserBindingTO userBindingTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " unbindService : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authorizationHeader);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtil.isValidForUserBinding(userBindingTO);
                if (errorMessage == null) {
                    userBindingTO.setId(userId);
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    nonADUserFacade.unbindServicesFromUser(userBindingTO, role, actor,id, saveRequest);
                    userBindingTO.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = userBindingTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_UNBIND_SERVICES(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_UNBIND_SERVICES(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " unbindService : start");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Edits the user.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param userId the user id
     * @param userTO the user TO
     * @param reqRefNumber the req ref number
     */
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER","USER_MAKER", "SUPER_ADMIN" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId")
    @ExceptionMetered(name = "exceptions.v3.users.userId")
    @ResponseMetered(name = "response.code.v3.users.userId")
    @ValidateLicense
    public void editUser(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            UserTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            Long id=Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_USER_UDATE_FAILED();
            try {
                userTO.setUserId(userId);
                String errorMessage = ValidationUtilV3.isConsumerValidForUpdate(userTO);
                if (errorMessage == null) {
                    UserTO userTOResponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    userTOResponse = userFacade.editUser(userTO, role, actor,id, saveRequest);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUser : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "SUPER_ADMIN" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/last-login-time/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId")
    @ExceptionMetered(name = "exceptions.v3.users.userId")
    @ResponseMetered(name = "response.code.v3.users.userId")
    @ValidateLicense
    public void editUserLastLogInTime(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            UserTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUserLastLogInTime : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_USER_UDATE_FAILED();
            try {
                userTO.setUserId(userId);
                String errorMessage = ValidationUtilV3.isRequestValidForUpdateUserTimestamp(userTO);
                if (errorMessage == null) {
                    UserTO userTOResponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    userTOResponse = userFacade.editUserLastLogInTime(userTO, role, actor,id, saveRequest);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUserLastLogInTime : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "SUPER_ADMIN" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/user-role/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId")
    @ExceptionMetered(name = "exceptions.v3.users.userId")
    @ResponseMetered(name = "response.code.v3.users.userId")
    @ValidateLicense
    public void editUserRole(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            UserTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUserRole : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_USER_UDATE_FAILED();
            try {
                userTO.setUserId(userId);
                String errorMessage = ValidationUtilV3.isConsumerValidForUpdateRole(userTO);
                if (errorMessage == null) {
                    UserTO userTOResponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    userTOResponse = userFacade.editUserRole(userTO, role, actor,id, saveRequest);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUserRole : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }



    @RolesAllowed(value = { "MAKER", "SUPER_USER", "USER_MAKER", "USER_CHECKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{userId}/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.userId")
    @ExceptionMetered(name = "exceptions.v3.users.userId.attributes")
    @ResponseMetered(name = "response.code.v3.users.userId.attributes")
    @ValidateLicense
    public void editUserAttributes(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            UserTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUserAttributes : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_USER_UDATE_FAILED();
            try {
                String errorMessage = ValidationUtilV3.isAttributeDataValid(userTO);
                if (errorMessage == null) {
                    userTO.setUserId(userId);
                    UserTO userTOResponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    userTOResponse = userFacade.editUserAttributes(userTO, role, actor, id,saveRequest);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUserAttributes : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/edit")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.edit")
    @ExceptionMetered(name = "exceptions.v3.users.edit")
    @ResponseMetered(name = "response.code.v3.users.edit")
    @ValidateLicense
    public void editUser(@Suspended final AsyncResponse asyncResponse, UserTO userTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String humanizedMessage = errorConstant.getHUMANIZED_USER_UDATE_FAILED();
            try {
                UserTO userTOResponse;
                userTOResponse = userFacade.editUser(userTO);
                response = userTOResponse;
                responseStatus = Response.Status.OK;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " editUser : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Specific for ICICI
     * @param track
     * @return
     */
    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.post")
    @ExceptionMetered(name = "exceptions.v3.users.post")
    @ResponseMetered(name = "response.code.v3.users.post")
    public Response validateDebitInformation(Track track) {

        String result = " Authorization failed";
        try {
            IAMUtil iamUtil = IAMUtil.getInstance();
            String enterpriseAccountId = config.getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(enterpriseAccountId);

            String id = config.getProperty(Constant.ENTERPRISE_ID);
            String password = config.getProperty(Constant.ENTERPRISE_PASSWORD);
            Token token = iamUtil.authenticateV2(iamExtension, id, AES128Impl.decryptData(password, KeyManagementUtil.getAESKey()));
            AccountWE accountWe = iamExtension.getAllUserAttributesNames(track.getAccountId(), token);
            for (AttributeTO attributeTO : accountWe.getAttributes()) {
                if (attributeTO.getAttributeName().equalsIgnoreCase(track.getAttributeName()) && attributeTO.getAttributeValue().equalsIgnoreCase(track.getAccountNumber())) {
                    // User account is fetched using Search Attribute List.
                    String transactionId = RandomString.nextString(20);
                    if (track.getTransactionId() != null && !track.getTransactionId().isEmpty()) {
                        transactionId = track.getTransactionId();
                    }

                    String transactionSummary = "Multi Factor Authentication";
                    String transactionDetails = transactionSummary + " for Fund transfer " + track.getTransactionDetails();
                    // Attribute Data is encrypted using Encrypted key received from Crypto. This encrypted data will be sent to user in Approval attempt.
                    if (track.getTransactionId() == null || track.getTransactionId().isEmpty()) {
                        //Attribute addition Attempt with encrypted data is generated for user. 
                        ApprovalAttemptV2 approvalAttempt = new ApprovalAttemptV2.Builder().approvalAttemptType(Constant.NORMAL)
                                .transactionDetails(transactionDetails).transactionSummary(transactionSummary).transactionId(transactionId)
                                .approvalStatus(in.fortytwo42.enterprise.extension.enums.ApprovalStatus.PENDING).service(in.fortytwo42.enterprise.extension.enums.Service.APPROVAL)
                                .consumerAccountId(track.getAccountId())
                                .isAuthenticationRequired(true).approvalAttemptMode(ApprovalAttemptMode.ENTERPRISE_TO_PEER).build();
                        iamExtension.generateApprovalAttempt(token, approvalAttempt, reqRefNum);
                    }
                    return Response.status(200).entity(track).build();
                }
            }
            throw new AttributeNotFoundException("The debit information provided is not associated to this account");
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return Response.status(409).entity(result).build();
        }

    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/verify-token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.verify-token")
    @ExceptionMetered(name = "exceptions.v3.users.verify-token")
    @ResponseMetered(name = "response.code.v3.users.verify-token")
    public void verifyCryptoToken(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecrete, CryptoTokenTO cryptoTokenTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + VERIFY_CRYPTO_TOKEN_START_LOG);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String humanizedMessage = errorConstant.getERROR_MESSAGE_VERIFICATION_FAILED();
            try {
                String errorMessage = ValidationUtilV3.isValidForCryptoVerification(cryptoTokenTO);
                if (errorMessage == null) {
                    CryptoTokenTO cryptoTokenTOResponse = userFacade.verifyCryptoToken(applicationId, cryptoTokenTO, false);
                    response = cryptoTokenTOResponse;
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + VERIFY_CRYPTO_TOKEN_END_LOG);
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/verify-crypto-token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void verifyToken(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId, @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecrete, CryptoTokenTO cryptoTokenTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + VERIFY_CRYPTO_TOKEN_START_LOG);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String humanizedMessage = errorConstant.getERROR_MESSAGE_VERIFICATION_FAILED();
            try {
                String errorMessage = ValidationUtilV3.isValidForCryptoTokenVerification(cryptoTokenTO);
                if (errorMessage == null) {
                    CryptoTokenTO cryptoTokenTOResponse = userFacade.verifyCryptoTokenTOTP(applicationId, cryptoTokenTO);
                    response = cryptoTokenTOResponse;
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + VERIFY_CRYPTO_TOKEN_END_LOG);
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/create-crypto-token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.verify-token")
    @ExceptionMetered(name = "exceptions.v3.users.verify-token")
    @ResponseMetered(name = "response.code.v3.users.verify-token")
    public void createCryptoToken(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecrete, CryptoTokenTO cryptoTokenTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + VERIFY_CRYPTO_TOKEN_START_LOG);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String humanizedMessage = errorConstant.getERROR_MESSAGE_VERIFICATION_FAILED();
            try {
                String errorMessage = ValidationUtilV3.isValidForCryptoVerification(cryptoTokenTO);
                if (errorMessage == null) {
                    response = userFacade.generateToken(applicationId, cryptoTokenTO);
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + VERIFY_CRYPTO_TOKEN_END_LOG);
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/verify-enterprise-crypto-token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.verify-token")
    @ExceptionMetered(name = "exceptions.v3.users.verify-token")
    @ResponseMetered(name = "response.code.v3.users.verify-token")
    public void verifyEnterpriseCryptoToken(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecrete, CryptoTokenTO cryptoTokenTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + VERIFY_CRYPTO_TOKEN_START_LOG);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String humanizedMessage = errorConstant.getERROR_MESSAGE_VERIFICATION_FAILED();
            try {
                String errorMessage = ValidationUtilV3.isValidForCryptoVerification(cryptoTokenTO);
                if (errorMessage == null) {
                    CryptoTokenTO cryptoTokenTOResponse = userFacade.verifyCryptoToken(applicationId, cryptoTokenTO, true);
                    response = cryptoTokenTOResponse;
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + VERIFY_CRYPTO_TOKEN_END_LOG);
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/download-csv")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.download-csv")
    @ExceptionMetered(name = "exceptions.v3.users.download-csv")
    @ResponseMetered(name = "response.code.v3.users.download-csv")
    public void downloadCsv(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(Constant.FILE_NAME) String fileName, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " downloadCsv : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                String content = userFacade.downloadUpdateUserStatus(fileName, role);
                if (content != null && !content.isEmpty()) {
                    logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " downloadCsv : end");
                    asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", APPLICATION_OCTET_STREAM_HEADER)
                            .header(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME_HEADER + fileName).build());
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INPROGRESS(), errorConstant.getERROR_MESSAGE_INPROGRESS(), errorConstant.getERROR_MESSAGE_INPROGRESS());
                    responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            }
            catch (AuthException e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_LOGS_DOWNLOAD_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
            }
        });
    }

    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/bulk-upload-users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Timed(name = "timer.v3.users.bulk_upload_users")
    @ExceptionMetered(name = "exceptions.v3.users.bulk_upload_users")
    @ResponseMetered(name = "response.code.v3.users.bulk_upload_users")
    @ValidateLicense
    public void bulkUploadUsers(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.FILE_NAME) String fileName, @HeaderParam(Constant.HEADER_FILE_TYPE) String fileType,
            final InputStream attributeInputStream, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " bulkUploadUsers : start");
            Status responseStatus;
            Object response;
            try {
                String errorMessage = ValidationUtil.isValidCSV(fileName);
                if (errorMessage == null) {
                    CSVUploadTO csvUploadTO = userFacade.uploadOnboardUsers(fileType, attributeInputStream, null,null,null, fileName);
                    response = csvUploadTO;
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_FILE_TYPE(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            }
            catch (AuthException e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            catch (Exception e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " bulkUploadUsers : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/bulk-onboard-user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.bulk_onboard_user")
    @ExceptionMetered(name = "exceptions.v3.users.bulk_onboard_user")
    @ResponseMetered(name = "response.code.v3.users.bulk_onboard_user")
    @ValidateLicense
    public void bulkOnboardUser(@Suspended final AsyncResponse asyncResponse, List<UserIciciTO> userTOs, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " bulkOnboardUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Status responseStatus;
            Object response;
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForOnboardUsers(userTOs);
                if (errorMessage == null) {
                    List<UserIciciTO> userTOResponses = userFacade.onboardUsers(userTOs);
                    response = new Gson().toJson(userTOResponses);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_USER_ONBOARD_FAILED(), e.getMessage());
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
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " bulkOnboardUser : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @InternalSecure
    @ResponseToken
    @GET
    @Path("/download-sample-csv")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.download-sample-csv")
    @ExceptionMetered(name = "exceptions.v3.users.download-sample-csv")
    @ResponseMetered(name = "response.code.v3.users.download-sample-csv")
    public void downloadSampleCsv(@Suspended final AsyncResponse asyncResponse, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " downloadUserOnboardCsv : start");
            try {
                String fileName = "bulk_user-onboard.csv";
                String content = userFacade.readSampleCsvFile(fileName);
                if (content != null && !content.isEmpty()) {
                    asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", APPLICATION_OCTET_STREAM_HEADER)
                            .header(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME_HEADER + "user-upload.csv").build());
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_CSV_DOWNLOAD_FAILED(), errorConstant.getHUMANIZED_CSV_DOWNLOAD_FAILED(), errorConstant.getERROR_DEVELOPER_CSV_DOWNLOAD_FAILED());
                    asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
                }
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_CSV_DOWNLOAD_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " downloadUserOnboardCsv : end");
            }
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "USER_MAKER", "USER_CHECKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/{accountId}/tokens")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void getTokens(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @PathParam("accountId") final String accountId, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getTokens : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_GET_TOKENS_FAILED();
            try {
                AccountWE accountWE = userFacade.getTokensByAccountId(accountId);
                responseStatus = Response.Status.OK;
                List<TokenWE> token = accountWE.getToken();
                response = token;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_DEVICE_TOKENS_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getTokens : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "USER_MAKER", "USER_CHECKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/{accountId}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void getdevices(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @PathParam("accountId") final String accountId, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getdevices : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_GET_TOKENS_FAILED();
            try {
                AccountWE accountWE = userFacade.getDevicesByAccountId(accountId);
                responseStatus = Response.Status.OK;
                List<in.fortytwo42.enterprise.extension.webentities.DeviceTO> devices = accountWE.getDevises();
                response = devices;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_DEVICE_TOKENS_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getdevices : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @InternalSecure
    @ResponseToken
    @GET
    @Path("/bulk-upload-status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.bulk-upload-status")
    @ExceptionMetered(name = "exceptions.v3.users.bulk-upload-status")
    @ResponseMetered(name = "response.code.v3.users.bulk-upload-status")
    public void downloadCsvStatus(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.FILE_NAME) String fileName, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " downloadCsv : start");
            Status responseStatus;
            Object response;
            try {
                String content = userFacade.downloadUpdateUserStatus(fileName, null);
                if (content != null && !content.isEmpty()) {
                    logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " downloadCsv : end");
                    asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", APPLICATION_OCTET_STREAM_HEADER)
                            .header(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME_HEADER + fileName).build());
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INPROGRESS(), errorConstant.getERROR_MESSAGE_INPROGRESS(), errorConstant.getERROR_MESSAGE_INPROGRESS());
                    responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            }
            catch (AuthException e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_LOGS_DOWNLOAD_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
            }
        });
    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/generate_authentication_token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.generate_authentication_token")
    @ExceptionMetered(name = "exceptions.v3.users.generate_authentication_token")
    @ResponseMetered(name = "response.code.v3.users.generate_authentication_token")
    @ValidateLicense
    public void generateHOTP(@Suspended final AsyncResponse asyncResponse,
                             @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
                             @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecret, @HeaderParam("request-reference-number") String reqRefNumber,
                             HotpTO hotpTO, @HeaderParam(Constant.HEADER_APPLICATION_NAME) String applicationName) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " generateHOTP : start");
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Status responseStatus;
            Object response;
            try {
                //verify request
                String errorMessage = ValidationUtilV3.isRequestValidForGenerateOtp(hotpTO);
                if (errorMessage == null) {
                    HotpTO otp = hotpFacade.generateOtp(hotpTO, applicationId);
                    response = new Gson().toJson(otp);
                    responseStatus = Response.Status.OK;

                }else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getERROR_MESSAGE_HOTP_GENERATION_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                auditLogProcessor.addOtpAuditSendLogs(applicationName, hotpTO.getSearchAttributes(), OtpAction.GO,OtpStatus.F, "");
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_HOTP_GENERATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
                auditLogProcessor.addOtpAuditSendLogs(applicationName, hotpTO.getSearchAttributes(), OtpAction.GO,OtpStatus.F, "");
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " generateHOTP : end");
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
    @Timed(name = "timer.v3.users.authentication_token/validate")
    @ExceptionMetered(name = "exceptions.v3.users.authentication_token/validate")
    @ResponseMetered(name = "response.code.v3.users.authentication_token/validate")
    @ValidateLicense
    public void validateHOTP(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecret, @HeaderParam("request-reference-number") String reqRefNumber,
            HotpTO hotpTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validateHOTP : start");
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Status responseStatus;
            Object response;
            try {
                //verify request
                String errorMessage = ValidationUtilV3.isRequestValidForValidateHOTP(hotpTO);
                if(errorMessage != null){
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
                else{
                    HotpTO otp = hotpFacade.validateOtp(hotpTO, applicationId);
                    response = new Gson().toJson(otp);
                    responseStatus = Response.Status.OK;
                }

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                auditLogProcessor.addOtpAuditValidateLogs(applicationId, hotpTO.getSearchAttributes(), OtpAction.VO, OtpStatus.F, "");
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
                auditLogProcessor.addOtpAuditValidateLogs(applicationId, hotpTO.getSearchAttributes(), OtpAction.VO, OtpStatus.F, "");
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validateHOTP : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/send_authentication_token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.send_authentication_token")
    @ExceptionMetered(name = "exceptions.v3.users.send_authentication_token")
    @ResponseMetered(name = "response.code.v3.users.send_authentication_token")
    @ValidateLicense
    public void generateAdHOTP(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecret, @HeaderParam(Constant.SERVICES) String service, AdHotpTO adHotpTO, @HeaderParam("request-reference-number") String reqRefNumber,
                               @HeaderParam(Constant.HEADER_APPLICATION_NAME) String applicationName) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " generateAdHOTP : start");
            Status responseStatus;
            Object response;
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForSendADFSOtp(adHotpTO);
                if(errorMessage==null){
                    AdHotpTO otp = adHotpFacade.generateAdOtp(adHotpTO, applicationId, service);
                    response = new Gson().toJson(otp);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getERROR_MESSAGE_HOTP_GENERATION_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                auditLogProcessor.addOtpAuditSendLogs(applicationName, adHotpTO.getSearchAttributes(), OtpAction.SO, OtpStatus.F, "");
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_HOTP_GENERATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
                auditLogProcessor.addOtpAuditSendLogs(applicationName, adHotpTO.getSearchAttributes(), OtpAction.SO, OtpStatus.F, "");
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " generateAdHOTP : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/authentication_token/validate-enc-otp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ValidateLicense
    public void validateEncryptedHOTP(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecret, HotpTO hotpTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validateHOTP : start");
            Response.Status responseStatus;
            Object response;
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForValidateHOTP(hotpTO);
                if (errorMessage == null) {
                    String token = hotpTO.getAuthenticationToken();
                    HotpTO otp = hotpFacade.validateOtp(hotpTO, applicationId, true);
                    otp.setAuthenticationToken(token);
                    response = new Gson().toJson(otp);
                    responseStatus = Response.Status.OK;
                }
                else {
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
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
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " validateHOTP : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "SUPER_ADMIN" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/disable/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.onboard")
    @ExceptionMetered(name = "exceptions.v3.users.disable")
    @ResponseMetered(name = "response.code.v3.users.disable")
    @ValidateLicense
    public void disableUser(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, UserTO userTO, @PathParam("userId") final Long userId,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);

            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUser : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                userTO.setUserId(userId);
                logger.log(Level.DEBUG, "<<<<< userTO : " + new Gson().toJson(userTO));
                String errorMessage = ValidationUtilV3.isRequestValidForDisableUser(userTO);
                if (errorMessage == null) {
                    UserTO userTOResponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    userTOResponse = userFacade.disableUser(userTO, role, actor,id, true, saveRequest);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getERROR_MESSAGE_DISABLE_USER_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_DISABLE_USER_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " onboardUser : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "USER_MAKER", "USER_VIEWONLY", "CHECKER", "USER_CHECKER","OPERATIONAL_MAKER", "OPERATIONAL_VIEWONLY", "OPERATIONAL_CHECKER"  })
    @ApiLogger
    @Secured
    @ResponseToken
    @ValidateSearchQuery
    @GET
    @Path("/otp-audit-search")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void getAllOtpAuditSearchData(@Suspended final AsyncResponse asyncResponse,
                                    @HeaderParam(Constant.HEADER_AUTHORIZATION) String authorizationHeader,
                                   @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getAllOtpAuditSearchData : start");
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Response.Status responseStatus = null;
            Object response = null;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(QueryParam.PAGE.getKey()), QueryParam.PAGE);
                page = (page == null || page < 1) ? 1 : page;
                Integer pageSize = (Integer) StringUtil.parseQueryValue(queryParam.get(QueryParam.PAGE_SIZE.getKey()), QueryParam.PAGE_SIZE);
                pageSize = pageSize == null ? 10 : pageSize;
                String attributeName = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.ATTRIBUTE_NAME.getKey()), QueryParam.ATTRIBUTE_NAME);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.SEARCH_QUERY.getKey()), QueryParam.SEARCH_QUERY);
                searchText = searchText != null ? searchText.toUpperCase() : searchText;
                response = hotpFacade.getAllOtpAuditSearch1Log(page, pageSize, attributeName, searchText);
                responseStatus = Response.Status.OK;
            }
            catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getAllOtpAuditSearchData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
