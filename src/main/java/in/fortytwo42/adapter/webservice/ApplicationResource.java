
package in.fortytwo42.adapter.webservice;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import in.fortytwo42.adapter.facade.ApplicationFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TotpSettingsTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMLogger;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.RemoteAccessSettingTO;
import in.fortytwo42.tos.transferobj.RunningHashTo;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

// TODO: Auto-generated Javadoc
/**
 * The Class ApplicationResource.
 */
@Path("/v3/applications")
public class ApplicationResource {

    /** The application facade. */
    private ApplicationFacadeIntf applicationFacade = FacadeFactory.getApplicationFacade();

    /** The application resource api log. */
    private String APPLICATION_RESOURCE_API_LOG = "<<<<< ApplicationResource";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    /** The logger. */
    private static Logger logger= LogManager.getLogger(ApplicationResource.class);

    private Config config = Config.getInstance();

    /**
     * Upload ESC file.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param fileName the file name
     * @param cryptoFileInputStream the crypto file input stream
     */
    @RolesAllowed(value = { "MAKER", "CHECKER" })
    @Secured
    @ResponseToken
    @PUT
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Timed(name = "timer.v3.applications.upload")
    @ExceptionMetered(name = "exceptions.v3.applications.upload")
    @ResponseMetered(name = "response.code.v3.applications.upload")
    public void uploadESCFile(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam("fileName") String fileName,
            final InputStream cryptoFileInputStream) {

    }

    /**
     * Gets the applications.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the applications
     */
    @RolesAllowed(value = { "MAKER", "CHECKER", "VIEW_ONLY","SUPER_USER","SUPER_ADMIN","APPLICATION_MAKER","APPLICATION_CHECKER","APPLICATION_VIEWONLY","USER_MAKER","USER_CHECKER","USER_VIEWONLY", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ValidateSearchQuery
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications")
    @ExceptionMetered(name = "exceptions.v3.applications")
    @ResponseMetered(name = "response.code.v3.applications")
    public void getApplications(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " getApplications : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            String updateStatus, searchText1, applicationType;
            String _2faStatusFilter;
            Integer page;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
//                updateStatus = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.STATUS.getKey()), in.fortytwo42.adapter.enums.QueryParam.STATUS);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                _2faStatusFilter = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam._2FASTATUS.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam._2FASTATUS);
                applicationType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.APPLICATION_TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.APPLICATION_TYPE);
                String sraApplicationType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SRA_APPLICATION_TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SRA_APPLICATION_TYPE);
                Long userGroupId = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.GROUP_ID.getKey()), in.fortytwo42.adapter.enums.QueryParam.GROUP_ID);

                if (sraApplicationType != null) {
                    String errorMessage = ValidationUtilV3.isDataValidForGetSRAApplication(sraApplicationType);
                    if (errorMessage == null) {
                        List<ApplicationTO> applicationTOs = applicationFacade.getSRAApplications(sraApplicationType, role);
                        responseStatus = Response.Status.OK;
                        response = new Gson().toJson(applicationTOs);
                    }
                    else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
                }
                else {
                    //String errorMessage = null;
                   // if (errorMessage == null) {
                        int pageNo = (page == null || page < 1) ? 1 : page;
                        PaginatedTO<ApplicationTO> applicationTOs = applicationFacade.getApplications(pageNo, searchText, _2faStatusFilter, applicationType, role, userGroupId);
                        responseStatus = Response.Status.OK;
                        response = new Gson().toJson(applicationTOs);
                    //}
//                    else {
//                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA, errorConstant.getERROR_MESSAGE_INVALID_DATA, errorMessage);
//                        responseStatus = Response.Status.BAD_REQUEST;
//                        response = errorTO;
//                    }
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " getApplications : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the application secret.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param reqRefNumber the req ref number
     * @return the application secret
     */
    @RolesAllowed(value = { "MAKER", "SUPER_USER", "SUPER_ADMIN", "APPLICATION_MAKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/application-secret")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.application-secret")
    @ExceptionMetered(name = "exceptions.v3.applications.application-secret")
    @ResponseMetered(name = "response.code.v3.applications.application-secret")
    public void getApplicationSecret(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " getApplicationSecret : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                String applicationSecret = applicationFacade.generateApplicationSecret();
                ApplicationTO applicationTO = new ApplicationTO();
                applicationTO.setApplicationSecret(applicationSecret);
                responseStatus = Response.Status.OK;
                response = applicationTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_APPLICATION_SECRET_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " getApplicationSecret : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /* @ApiLogger
     
    @POST
    @Path("/authenticate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @WebMethod(operationName = "login")
    public void authenticate(@Suspended final AsyncResponse asyncResponse, ApplicationTO applicationTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
                auditLoggingProcessorIntf.log(Application.class, Constant.SYSTEM, AuditLogConstant.APPLICATION,
                        StringUtil.build(AuditLogConstant.APPLICATION_AUTHENTICATION_CALLED, applicationTO.getApplicationId()), AuditLogType.INFO);
                String errorMessage = ValidationUtil.isApplicationValidForLogin(applicationTO);
                if (errorMessage == null) {
                    ApplicationTO applicationResponseTO = applicationFacade.authenticateApplication(applicationTO.getApplicationId(), applicationTO.getApplicationSecret());
                    auditLoggingProcessorIntf.log(Application.class, Constant.SYSTEM, AuditLogConstant.APPLICATION,
                            StringUtil.build(AuditLogConstant.APPLICATION_AUTHENTICATION_SUCCESSFUL, applicationTO.getApplicationId()), AuditLogType.INFO);
                    responseStatus = Response.Status.CREATED;
                    response = applicationResponseTO;
                } else {
                    auditLoggingProcessorIntf.log(Application.class, Constant.SYSTEM, AuditLogConstant.APPLICATION,
                            StringUtil.build(AuditLogConstant.VALIDATION_FAILED_FOR_APPLICATION_ID, applicationTO.getApplicationId()), AuditLogType.ERROR);
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA, errorConstant.getERROR_MESSAGE_INVALID_DATA, errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                auditLoggingProcessorIntf.log(Application.class, Constant.SYSTEM, AuditLogConstant.APPLICATION, e.getMessage(), AuditLogType.ERROR);
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_AUTHENTICATION_FAILED, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            } catch (Exception e) {
                auditLoggingProcessorIntf.log(Application.class, Constant.SYSTEM, AuditLogConstant.APPLICATION, e.getMessage(), AuditLogType.ERROR);
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR, errorConstant.getHUMANIZED_AUTHENTICATION_FAILED, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR);
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        }); 
    }*/

    /**
     * Delete application.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param applicationTo the application to
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/deleteApplication/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.deleteApplication")
    @ExceptionMetered(name = "exceptions.v3.applications.deleteApplication")
    @ResponseMetered(name = "response.code.v3.applications.deleteApplication")
    public void deleteApplication(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, ApplicationTO applicationTo,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " deleteApplication : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtil.isApplicationValidForDeletion(applicationTo);
                if (errorMessage == null) {
                    ApplicationTO applicatonDeletionResponse = applicationFacade.deleteApplication(applicationTo, role, actor);
                    responseStatus = Response.Status.OK;
                    response = applicatonDeletionResponse;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_APPLICATION_DELETION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_APPLICATION_DELETION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " deleteApplication : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the user application rels.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the user application rels
     */
    @RolesAllowed(value = { "MAKER", "CHECKER", "VIEW_ONLY","SUPER_USER" })
    @ApiLogger

    @Secured
    @ResponseToken
    @GET
    @Path("/user-bindings")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.user-bindings")
    @ExceptionMetered(name = "exceptions.v3.applications.user-bindings")
    @ResponseMetered(name = "response.code.v3.applications.user-bindings")
    public void getUserApplicationRels(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " getUserApplicationRels : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            String applicationId, searchText, updateStatus;
            Integer page;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                applicationId = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.APPLICATION_ID.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.APPLICATION_ID);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                updateStatus = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.STATUS.getKey()), in.fortytwo42.adapter.enums.QueryParam.STATUS);
                String errorMessage = ValidationUtil.isDataValidForGetApplicationLabels(applicationId, searchText, updateStatus);
                if (errorMessage == null) {
                    int pageNo = (page == null || page < 1) ? 1 : page;
                    PaginatedTO<UserApplicationRelTO> applicationTOs = applicationFacade.getUserApplicationRels(applicationId, updateStatus, searchText, pageNo, role, actor);
                    responseStatus = Response.Status.OK;
                    response = new Gson().toJson(applicationTOs);
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " getUserApplicationRels : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER","APPLICATION_MAKER"})
    @ApiLogger

    @Secured
    @ResponseToken
    @POST
    @Path("/onboard")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.onboard")
    @ExceptionMetered(name = "exceptions.v3.applications.onboard")
    @ResponseMetered(name = "response.code.v3.applications.onboard")
    public void onboardApplication(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            ApplicationTO applicationTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " onboardApplication : start");
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
                String errorMessage = ValidationUtilV3.isRequestValidForOnboardApplication(applicationTO);
                if (errorMessage == null) {
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    applicationTO.setIsCredentialsEncrypted(true);
                    ApplicationTO application = applicationFacade.onboardApplication(role, actor,id, applicationTO,
                            saveRequest);
                    response = application;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ONBOARD_APPLICATION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (ServiceNotFoundException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getHUMANIZED_ONBOARD_APPLICATION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ONBOARD_APPLICATION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " onboardApplication : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/clearCache")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.onboard")
    @ExceptionMetered(name = "exceptions.v3.applications.onboard")
    @ResponseMetered(name = "response.code.v3.applications.onboard")
    public void clearCache(@Suspended final AsyncResponse asyncResponse,
            ApplicationTO applicationTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " onboardApplication : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForClearCache(applicationTO);
                if (errorMessage == null) {
                    ApplicationTO application = applicationFacade.clearCache(applicationTO);
                    response = application;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ONBOARD_APPLICATION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (ServiceNotFoundException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getHUMANIZED_ONBOARD_APPLICATION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ONBOARD_APPLICATION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " onboardApplication : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }


    /**
     * Edits the application.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param applicationId the application id
     * @param applicationTO the application TO
     * @param reqRefNumber the req ref number
     */
    @RolesAllowed(value = { "MAKER", "APPLICATION_MAKER"})
    @ApiLogger

    @Secured
    @ResponseToken
    @POST
    @Path("/edit/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.edit")
    @ExceptionMetered(name = "exceptions.v3.applications.edit")
    @ResponseMetered(name = "response.code.v3.applications.edit")
    public void editApplicationv2(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("applicationId") final Long applicationId,
            ApplicationTO applicationTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " editApplicationv2 : start");
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

                String errorMessage = ValidationUtilV3.isApplicationValidForUpdate(applicationTO);
                if (errorMessage == null) {
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    ApplicationTO application = applicationFacade.editApplicationv2(role, actor,id, applicationTO,
                            saveRequest);
                    response = application;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_APPLICATION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (ServiceNotFoundException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getHUMANIZED_ONBOARD_APPLICATION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_APPLICATION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " editApplicationv2 : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @Secured
    @POST
    @Path("/remote-access-settings")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.remote-access-settings")
    @ExceptionMetered(name = "exceptions.v3.applications.remote-access-settings")
    @ResponseMetered(name = "response.code.v3.applications.remote-access-settings")
    public void getRemoteAccessSettings(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel, @HeaderParam(Constant.HEADER_SERVICE_NAME) String serviceName, final RemoteAccessSettingTO remoteAccessSettingTO,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            IamThreadContext.setActor(applicationId);
            try {
                String errorMessage = ValidationUtilV3.isDataValidForGetRemoteAccessSettings(remoteAccessSettingTO);
                if (errorMessage == null) {
                    RemoteAccessSettingTO remoteAccessSettingResponseTO = applicationFacade.getRemoteAccessSettings(remoteAccessSettingTO);
                    responseStatus = Response.Status.OK;
                    response = remoteAccessSettingResponseTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_REMOTE_SETTINGS(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_REMOTE_SETTINGS(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{applicationId}/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.delete")
    @ExceptionMetered(name = "exceptions.v3.applications.delete")
    @ResponseMetered(name = "response.code.v3.applications.delete")
    public void deleteSRAApplicationSetting(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("applicationId") final String applicationId, final ApplicationTO applicationTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " deleteSRAApplicationSetting : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                applicationTO.setApplicationId(applicationId);
                boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                ApplicationTO application = applicationFacade.deleteSRAApplicationSetting(actor, role, applicationTO,saveRequest);
                response = application;
                responseStatus = Response.Status.OK;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_DELETE_APPLICATION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_DELETE_APPLICATION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " onboardApplication : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    /**
     * generate running hash.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param reqRefNumber the req ref number
     * @param applicationId the application id
     * @return the running hash of application secret
     */
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "SUPER_ADMIN", "APPLICATION_MAKER", "APPLICATION_CHECKER", "APPLICATION_VIEWONLY"})
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/{applicationId}/generate-running-hash")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.generate-running-hash")
    @ExceptionMetered(name = "exceptions.v3.applications.generate-running-hash")
    @ResponseMetered(name = "response.code.v3.applications.generate-running-hash")
    public void generateRunningHash(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber, @PathParam("applicationId") final String applicationId) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " generateRunningHash : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Status responseStatus;
            Object response;
            try {
                String applicationSecret = applicationFacade.generateRunningHash(applicationId);
                RunningHashTo runningHash = new RunningHashTo();
                runningHash.setRunningHash(applicationSecret);
                runningHash.setApplicationId(applicationId);
                responseStatus = Response.Status.OK;
                response = runningHash;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GENERATE_RUNNING_HASH_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GENERATE_RUNNING_HASH_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " generateRunningHash : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * verify running hash.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param reqRefNumber the req ref number
     * @param applicationId the application id
     * @return the same request packet if running hash in the request is valid
     */
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "SUPER_ADMIN", "APPLICATION_MAKER", "APPLICATION_CHECKER", "APPLICATION_VIEWONLY"})
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{applicationId}/verify-running-hash")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.verify-running-hash")
    @ExceptionMetered(name = "exceptions.v3.applications.verify-running-hash")
    @ResponseMetered(name = "response.code.v3.applications.verify-running-hash")
    public void verifyRunningHash(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber, @PathParam("applicationId") final String applicationId, final RunningHashTo runningHashTo) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " verifyRunningHash : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Status responseStatus;
            Object response;
            try {
                String error = ValidationUtilV3.isRequestValidForVerifyRunningHash(runningHashTo);
                if(error.isEmpty()){
                    if(applicationFacade.verifyRunningHash(applicationId, runningHashTo)){
                        responseStatus = Response.Status.OK;
                        runningHashTo.setApplicationId(applicationId);
                        response = runningHashTo;
                    }
                    else{
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_VERIFY_RUNNING_HASH_FAILED(), errorConstant.getHUMANIZED_VERIFY_RUNNING_HASH_FAILED(), errorConstant.getERROR_MESSAGE_INVALID_RUNNING_HASH());
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getHUMANIZED_VERIFY_RUNNING_HASH_FAILED(), error);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_VERIFY_RUNNING_HASH_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_VERIFY_RUNNING_HASH_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " verifyRunningHash : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @GET
    @Path("/totp-settings/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.applications.totp-settings")
    @ExceptionMetered(name = "exceptions.v3.applications.totp-settings")
    @ResponseMetered(name = "response.code.v3.applications.totp-settings")
    public void getTotpSettingsByApplicationId(@Suspended final AsyncResponse asyncResponse,@PathParam("applicationId") final String applicationId,
                                     @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " getApplicationSecret : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;

            try {
                TotpSettingsTO totpSettingsTO = applicationFacade.getTotpSettingsByApplicationId(applicationId);
                responseStatus = Response.Status.OK;
                response = totpSettingsTO;
            }
            catch (ApplicationNotFoundException e){
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_APPLICATION_NOT_FOUND());
                responseStatus = Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_APPLICATION_SECRET_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, APPLICATION_RESOURCE_API_LOG + " getApplicationSecret : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
