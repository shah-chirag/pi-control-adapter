/**
 * 
 */

package in.fortytwo42.adapter.webservice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.security.RolesAllowed;
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

import in.fortytwo42.adapter.enums.QueryParam;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.tos.transferobj.TotpAuditTrailTO;
import in.fortytwo42.tos.transferobj.UserTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.AuditLogsFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import org.apache.logging.log4j.ThreadContext;

/**
 * @author Amandeep
 *
 */
@Path("/v3/audit-logs")
public class AuditLogResource {

    /** The user resource api log. */
    private String AUDIT_LOG_RESOURCE_API_LOG = "<<<<< AuditLogResource";

    private static Logger logger= LogManager.getLogger(AuditLogResource.class);

    private AuditLogsFacadeIntf auditLogsFacade = FacadeFactory.getAuditLogsFacade();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    /**
     * Get All Policies - ADMIN User API
     * @param asyncResponse
     * @param reqRefNumber
     */
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.audit-logs")
    @ExceptionMetered(name = "exceptions.v3.audit-logs")
    @ResponseMetered(name = "response.code.v3.audit-logs")
    public void getAuditLogs(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = "X-query") final String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, AUDIT_LOG_RESOURCE_API_LOG + " getAuditLogs : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE);
                String errorMessage = ValidationUtilV3.isDataValidSearchText(searchText);
                if (errorMessage == null) {
                    response = new Gson().toJson(auditLogsFacade.getAuditLogs(queryParams));
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_POLICY_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_POLICY_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, AUDIT_LOG_RESOURCE_API_LOG + " getAuditLogs : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.audit-logs.download")
    @ExceptionMetered(name = "exceptions.v3.audit-logs.download")
    @ResponseMetered(name = "response.code.v3.audit-logs.download")
    public void downloadAuditLogs(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = "X-query") final String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, AUDIT_LOG_RESOURCE_API_LOG + " getAuditLogs : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE);
                String errorMessage = ValidationUtilV3.isDataValidSearchText(searchText);
                if (errorMessage == null) {
                    String content = auditLogsFacade.downloadAuditLogs(queryParams);
                    Date date = new Date(System.currentTimeMillis());
                    DateFormat formatter = new SimpleDateFormat("YYYYMMddHHmmss");
                    formatter.setTimeZone(TimeZone.getTimeZone("IST"));
                    String dateFormatted = formatter.format(date);
                    String filename = "EventLogAuditTrail_" + dateFormatted + ".csv";
                    System.out.println("filename : " + filename);
                    if (content != null && !content.isEmpty()) {
                        asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", "application/octet-stream")
                                .header("Content-Disposition", "attachment;filename=" + filename).build());
                    }
                    else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_CSV_DOWNLOAD_FAILED(), errorConstant.getHUMANIZED_CSV_DOWNLOAD_FAILED(), errorConstant.getERROR_DEVELOPER_CSV_DOWNLOAD_FAILED());
                        asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
                    }
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).header("Content-Type", "application/json").entity(errorTO).build());
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_POLICY_FAILED(), e.getMessage());
                asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).header("Content-Type", "application/json").entity(errorTO).build());

            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_POLICY_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
            }
            finally {
                logger.log(Level.DEBUG, AUDIT_LOG_RESOURCE_API_LOG + " getAuditLogs : end");
            }
        });

    }
    
    @ApiLogger
    @ResponseToken
    @Secured
    @POST
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.audit-logs.verify")
    @ExceptionMetered(name = "exceptions.v3.audit-logs.verify")
    @ResponseMetered(name = "response.code.v3.audit-logs.verify")
    public void verifyAuditLog(@Suspended final AsyncResponse asyncResponse,
            final AuditLogTO auditLogTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(new Runnable() {
            @Override
            public void run() {
                logger.log(Level.DEBUG, AUDIT_LOG_RESOURCE_API_LOG + " verifyAuditLog : start");
                IamThreadContext.setCorelationId(UUIDGenerator.generate());
                Status responseStatus;
                Object response;

                try {
                    auditLogsFacade.verifyAuditLog(auditLogTO);
                    response = auditLogTO;
                    responseStatus = Response.Status.OK;
                }
                catch (AuthException e) {
                    logger.log(Level.ERROR, e);
                    ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_POLICY_FAILED(), e.getMessage());
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
                catch (Exception e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_POLICY_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                    responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                    response = errorTO;
                }
                finally {
                    logger.log(Level.DEBUG, AUDIT_LOG_RESOURCE_API_LOG + " verifyAuditLog : end");
                }
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
        });
    }

    @RolesAllowed(value = {"USER_MAKER", "USER_VIEWONLY", "CHECKER", "USER_CHECKER","OPERATIONAL_MAKER","OPERATIONAL_CHECKER","OPERATIONAL_VIEWONLY"})
    @ApiLogger
    @Secured
    @ValidateSearchQuery
    @ResponseToken
    @GET
    @Path("/totp-audit")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void getAllTotpAuditData(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_AUTHORIZATION) String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, AUDIT_LOG_RESOURCE_API_LOG + " getAllTotpAuditData : start");
            IamThreadContext.setSessionWithoutTransaction(SessionFactoryUtil.getInstance().openSessionWithoutTransaction());
            Response.Status responseStatus = null;
            Object response = null;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(QueryParam.PAGE.getKey()), QueryParam.PAGE);
                page = (page == null || page < 1) ? 1 : page;
                Integer pageSize = (Integer) StringUtil.parseQueryValue(queryParam.get(QueryParam.PAGE_SIZE.getKey()), QueryParam.PAGE_SIZE);
                pageSize = pageSize == null ? 10 : pageSize;
                String attributeName = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.ATTRIBUTE_NAME.getKey()), QueryParam.ATTRIBUTE_NAME);
                String searchQuery = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.SEARCH_QUERY.getKey()), QueryParam.SEARCH_QUERY);
                String totpStatus = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.STATUS.getKey()), QueryParam.STATUS);
                searchQuery = searchQuery != null ? searchQuery.toUpperCase() : searchQuery;
                PaginatedTO<TotpAuditTrailTO> paginatedUserTOs = FacadeFactory.getTOTPFacade().getAllTotpAuditLog(page, pageSize, attributeName, searchQuery, totpStatus);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(paginatedUserTOs);
            } catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                SessionFactoryUtil.getInstance().closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, AUDIT_LOG_RESOURCE_API_LOG + " getAllTotpAuditData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
