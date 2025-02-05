
package in.fortytwo42.adapter.webservice;

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
import in.fortytwo42.adapter.facade.RequestFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.tos.transferobj.RequestTO;

// TODO: Auto-generated Javadoc
/**
 * The Class RequestResource.
 */
@Path("/v3/requests")
public class RequestResource {
    
    /** The request facade. */
    private RequestFacadeIntf requestFacade = FacadeFactory.getRequestFacade();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    /** The request resource log. */
    private String REQUEST_RESOURCE_LOG = "<<<<< RequestResource";

    private static Logger logger= LogManager.getLogger(RequestResource.class);
   
    private Config config = Config.getInstance();
    

    /**
     * Gets the requests.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the requests
     */
    @ApiLogger
    
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER", "APPLICATION_MAKER", "APPLICATION_CHECKER", "USER_MAKER", "USER_CHECKER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.requests")
    @ExceptionMetered(name = "exceptions.v3.requests")
    @ResponseMetered(name = "response.code.v3.requests")
    public void getRequests(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, REQUEST_RESOURCE_LOG + " getRequests : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                String actionType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ACTION_TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ACTION_TYPE);
                Long fromDate = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.FROM_DATE.getKey()), in.fortytwo42.adapter.enums.QueryParam.FROM_DATE);
                Long toDate = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TO_DATE.getKey()), in.fortytwo42.adapter.enums.QueryParam.TO_DATE);
                int pageNo = (page == null || page < 1) ? 1 : page;
                String requestType = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE);
                PaginatedTO<RequestTO> paginatedRequestTO = requestFacade.getPendingRequests(pageNo, role, requestType,actionType);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(paginatedRequestTO); 
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_REQUEST_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_REQUEST_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, REQUEST_RESOURCE_LOG + " getRequests : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Approve request.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param requestId the request id
     * @param requestTO the request TO
     * @param reqRefNumber the req ref number
     */
    @RolesAllowed(value = { "CHECKER", "SUPER_USER", "APPLICATION_CHECKER", "USER_CHECKER","SUPER_ADMIN", "OPERATIONAL_CHECKER" })
    @Secured
    @ResponseToken
    @ApiLogger
    @POST
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.requests.requestId")
    @ExceptionMetered(name = "exceptions.v3.requests.requestId")
    @ResponseMetered(name = "response.code.v3.requests.requestId")
    public void approveRequest(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("requestId") final Long requestId,
            RequestTO requestTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, REQUEST_RESOURCE_LOG + " approveRequest : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtil.isValidForApproveRequest(requestTO);
                if (errorMessage == null) {
                    requestTO.setId(requestId);
                    RequestTO requestResponseTO = requestFacade.approveRequest(requestTO, authToken);
                    requestResponseTO.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = requestResponseTO;
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }  
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_REQUEST_APPROVAL_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_REQUEST_APPROVAL_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, REQUEST_RESOURCE_LOG + " approveRequest : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    /**
     * Gets the all requests.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the all requests
     */
    @ApiLogger
    
    @Secured
    @ResponseToken
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.requests.all")
    @ExceptionMetered(name = "exceptions.v3.requests.all")
    @ResponseMetered(name = "response.code.v3.requests.all")
    public void getAllRequests(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, REQUEST_RESOURCE_LOG + " getAllRequests : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                String actionType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ACTION_TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ACTION_TYPE);
                Long fromDate = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.FROM_DATE.getKey()), in.fortytwo42.adapter.enums.QueryParam.FROM_DATE);
                Long toDate = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TO_DATE.getKey()), in.fortytwo42.adapter.enums.QueryParam.TO_DATE);
                String requestType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE);
                int pageNo = (page == null || page < 1) ? 1 : page;
                PaginatedTO<RequestTO> paginatedRequestTO = requestFacade.getRequests(actionType, pageNo, authToken, toDate, fromDate,requestType);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(paginatedRequestTO); 
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_REQUEST_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_REQUEST_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, REQUEST_RESOURCE_LOG + " getAllRequests : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    @ApiLogger
    @ResponseToken
    @GET
    @Path("/enabled")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.requests.enabled")
    @ExceptionMetered(name = "exceptions.v3.requests.enabled")
    @ResponseMetered(name = "response.code.v3.requests.enabled")
    public void getMakerCheckerEnabled(@Suspended final AsyncResponse asyncResponse, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, REQUEST_RESOURCE_LOG + " getBuildDetails : start");
            Status responseStatus = null;
            Object response = null;
            try {
                responseStatus = Response.Status.OK;
                response = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
            }
            catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, REQUEST_RESOURCE_LOG + " getBuildDetails : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
