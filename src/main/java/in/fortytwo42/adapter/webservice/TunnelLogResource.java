package  in.fortytwo42.adapter.webservice;

import java.util.Map;

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
import in.fortytwo42.adapter.facade.TunnelLogFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.tos.transferobj.TunnelLogTO;

@Path("/v3/tunnel-logs")
public class TunnelLogResource {
    
    /** The tunnel log facade intf. */
    private TunnelLogFacadeIntf tunnelLogFacade = FacadeFactory.getTunnelLogFacade();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

     /** The tunnel log resource api log. */
    private final String TUNNELLOGS_RESOURCE_API_LOG = "<<<<< TunnelLogsResource";

    private static Logger logger= LogManager.getLogger(TunnelLogResource.class);

    /**
     * Gets the user audit trails.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the user audit trails
     */
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/get-audit-trail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.tunnel-logs.get-audit-trail")
    @ExceptionMetered(name = "exceptions.v3.tunnel-logs.get-audit-trail")
    @ResponseMetered(name = "response.code.v3.tunnel-logs.get-audit-trail")
    public void getAuditTrails(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, TUNNELLOGS_RESOURCE_API_LOG + " getAuditTrails : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.PAGE);
                Long fromDate = (Long) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.FROM_DATE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.FROM_DATE);
                Long toDate = (Long) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.TO_DATE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.TO_DATE);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),
                         in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                String errorMessage = ValidationUtil.isDataValidForUserAuditTrailSearch(searchText);
                if (errorMessage == null) {
                    int pageNo = (page == null || page < 1) ? 1 : page;
                    PaginatedTO<TunnelLogTO> paginatedUserTOs = tunnelLogFacade.getAuditTrails(pageNo, searchText, fromDate, toDate);
                    responseStatus = Response.Status.OK;
                    response = new Gson().toJson(paginatedUserTOs);
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getHUMANIZED_ERROR_AUDIT_TRAIL(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            } catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ERROR_AUDIT_TRAIL(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ERROR_AUDIT_TRAIL() + " - " + e.getMessage(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, TUNNELLOGS_RESOURCE_API_LOG + " getAuditTrails : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    
    @ApiLogger
    @ResponseToken
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.tunnel-logs")
    @ExceptionMetered(name = "exceptions.v3.tunnel-logs")
    @ResponseMetered(name = "response.code.v3.tunnel-logs")
    public void addLogs(@Suspended final AsyncResponse asyncResponse,TunnelLogTO tunnelLogTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, TUNNELLOGS_RESOURCE_API_LOG + " addLogs : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
                response = tunnelLogFacade.createTunnelLog(tunnelLogTO);
                
                responseStatus = Response.Status.OK;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ADD_TUNNEL_LOG(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ADD_TUNNEL_LOG(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, TUNNELLOGS_RESOURCE_API_LOG + " onboardApplication : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
}
