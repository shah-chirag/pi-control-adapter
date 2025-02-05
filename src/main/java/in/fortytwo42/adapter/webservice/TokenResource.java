
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
import in.fortytwo42.adapter.enums.QueryParam;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.TokenFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.tos.transferobj.UserTO;

@Path("/v3/tokens")
public class TokenResource {

    private static final String TOKEN_RESOURCE_API_LOG = "<<<<< TokenResource";

    private static Logger logger= LogManager.getLogger(TokenResource.class);

    private TokenFacadeIntf tokenFacadeIntf = FacadeFactory.getTokenFacade();
    ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private Config config = Config.getInstance();

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.tokens")
    @ExceptionMetered(name = "exceptions.v3.tokens")
    @ResponseMetered(name = "response.code.v3.tokens")
    public void getTokens(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {

        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, TOKEN_RESOURCE_API_LOG + " getTokens : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            Integer page;
            Integer pageSize;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                if (queryParam.get(QueryParam.TOKEN_ID.getKey()) != null && !queryParam.get(QueryParam.TOKEN_ID.getKey()).isEmpty()) {
                    TokenTO tokenTO = tokenFacadeIntf.getToken(queryParam.get(QueryParam.TOKEN_ID.getKey()));
                    response = tokenTO;
                }
                else {
                    page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                    pageSize = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE);
                    PaginatedTO<TokenTO> tokenTOs = tokenFacadeIntf.getTokens(page, pageSize);
                    response = new Gson().toJson(tokenTOs);
                }
                responseStatus = Response.Status.OK;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_TOKENS_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_TOKENS_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, TOKEN_RESOURCE_API_LOG + " getTokens : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{tokenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.tokens.tokenId")
    @ExceptionMetered(name = "exceptions.v3.tokens.tokenId")
    @ResponseMetered(name = "response.code.v3.tokens.tokenId")
    public void editToken(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("tokenId") final String tokenId, @HeaderParam("request-reference-number") String reqRefNumber, TokenTO tokenTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, TOKEN_RESOURCE_API_LOG + " editToken : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForeditToken(tokenTO);
                if (errorMessage == null) {
                    tokenTO.setTokenId(tokenId);
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    TokenTO tokenResponseTO = tokenFacadeIntf.editToken(tokenTO, actor,id, saveRequest);
                    response = tokenResponseTO;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_TOKEN_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_TOKEN_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, TOKEN_RESOURCE_API_LOG + " editToken : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{tokenId}/remote-wipe")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.tokens.tokenId.remote-wipe")
    @ExceptionMetered(name = "exceptions.v3.tokens.tokenId.remote-wipe")
    @ResponseMetered(name = "response.code.v3.tokens.tokenId.remote-wipe")
    public void editTokenRemoteWipe(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("tokenId") final String tokenId, @HeaderParam("request-reference-number") String reqRefNumber,TokenTO tokenTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, TOKEN_RESOURCE_API_LOG + " editTokenRemoteWipe : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForTokenRemoteWipe(tokenId);
                if (errorMessage == null) {
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    TokenTO tokenResponseTO = tokenFacadeIntf.editTokenRemoteWipe(tokenTO, actor,id, saveRequest);
                    logger.log(Level.DEBUG, "<<<<< tokenResponseTO: "+new Gson().toJson(tokenResponseTO));
                    response = tokenResponseTO;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_TOKEN_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_TOKEN_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, TOKEN_RESOURCE_API_LOG + " editTokenRemoteWipe : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/{tokenId}/user-info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.tokens.tokenId.user-info")
    @ExceptionMetered(name = "exceptions.v3.tokens.tokenId.user-info")
    @ResponseMetered(name = "response.code.v3.tokens.tokenId.user-info")
    public void getUsers(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @PathParam("tokenId") final String tokenId, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, TOKEN_RESOURCE_API_LOG + " getUsers : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                List<UserTO> users = tokenFacadeIntf.getUserInfo(tokenId);
                response =  users;
                responseStatus = Response.Status.OK;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, TOKEN_RESOURCE_API_LOG + " getUsers : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }


}
