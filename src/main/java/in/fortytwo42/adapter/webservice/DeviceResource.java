
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
import in.fortytwo42.adapter.facade.DeviceFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.tos.transferobj.UserTO;

@Path("/v3/devices")
public class DeviceResource {

    private static final String DEVICE_RESOURCE_API_LOG = "<<<<< DeviceResource";

    private static Logger logger= LogManager.getLogger(DeviceResource.class);

    private DeviceFacadeIntf deviceFacadeIntf = FacadeFactory.getDeviceFacade();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private Config config = Config.getInstance();

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.devices")
    @ExceptionMetered(name = "exceptions.v3.devices")
    @ResponseMetered(name = "response.code.v3.devices")
    public void getDevices(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " getDevices : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                if (queryParam.get(QueryParam.DEVICE_ID.getKey()) != null && !queryParam.get(QueryParam.DEVICE_ID.getKey()).isEmpty()) {
                    DeviceTO deviceTO = deviceFacadeIntf.getDevice(queryParam.get(QueryParam.DEVICE_ID.getKey()));
                    response = deviceTO;
                }
                else {
                    Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                    Integer pageSize = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE);
                    PaginatedTO<DeviceTO> deviceTOs = deviceFacadeIntf.getDevices(page, pageSize);
                    response = new Gson().toJson(deviceTOs);
                }
                responseStatus = Response.Status.OK;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_DEVICES_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_DEVICES_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " getDevices : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/{deviceId}/tokens")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.devices.deviceId.tokens")
    @ExceptionMetered(name = "exceptions.v3.devices.deviceId.tokens")
    @ResponseMetered(name = "response.code.v3.devices.deviceId.tokens")
    public void getTokens(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @PathParam("deviceId") final String deviceId, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " getTokens : start");
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
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                pageSize = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE);
                PaginatedTO<TokenTO> tokenTOs = deviceFacadeIntf.getTokens(deviceId, page, pageSize);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(tokenTOs);
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_DEVICE_TOKENS_FAILED(), e.getMessage());
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
                logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " getTokens : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.devices.deviceId")
    @ExceptionMetered(name = "exceptions.v3.devices.deviceId")
    @ResponseMetered(name = "response.code.v3.devices.deviceId")
    public void editDevice(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("deviceId") final String deviceId, @HeaderParam("request-reference-number") String reqRefNumber, DeviceTO deviceTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " editDevice : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                deviceTO.setDeviceId(deviceId);
                String errorMessage = ValidationUtilV3.isRequestValidForeditDevice(deviceTO);
                if (errorMessage == null) {
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    DeviceTO deviceResponseTO = deviceFacadeIntf.editDevice(deviceTO, actor,id, saveRequest);
                    response = deviceResponseTO;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " editDevice : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{deviceId}/bind-token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.devices.deviceId.bind-token")
    @ExceptionMetered(name = "exceptions.v3.devices.deviceId.bind-token")
    @ResponseMetered(name = "response.code.v3.devices.deviceId.bind-token")
    public void bindTokenToDevice(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("deviceId") final String deviceId, @HeaderParam("request-reference-number") String reqRefNumber, DeviceTO deviceTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " editDevice : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                deviceTO.setDeviceId(deviceId);
                String errorMessage = ValidationUtilV3.isRequestValidForbindTokenToDevice(deviceTO);
                if (errorMessage == null) {
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    DeviceTO deviceResponseTO = deviceFacadeIntf.editDeviceBindToken(deviceTO, actor,id, saveRequest);
                    response = deviceResponseTO;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " editDevice : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/{deviceId}/user-info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.devices.deviceId.user-info")
    @ExceptionMetered(name = "exceptions.v3.devices.deviceId.user-info")
    @ResponseMetered(name = "response.code.v3.devices.deviceId.user-info")
    public void getUsers(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @PathParam("deviceId") final String deviceId, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " getUsers : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                List<UserTO> users = deviceFacadeIntf.getUserInfo(deviceId);
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
                logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " getUsers : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }
    
    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/unbind-users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.devices.unbind-users")
    @ExceptionMetered(name = "exceptions.v3.devices.unbind-users")
    @ResponseMetered(name = "response.code.v3.devices.unbind-users")
    public void unbindUsers(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber, DeviceTO deviceTO) {
        
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " undindUsers : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForUnbindUsersFromDevice(deviceTO);
                if (errorMessage == null) {
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    DeviceTO deviceResponseTO = deviceFacadeIntf.unbindUsersFromDevice(deviceTO, actor,id, saveRequest);
                    response = deviceResponseTO;
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " undindUsers : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
        
    }
    
    
    @ApiLogger
    @InternalSecure
    @POST
    @Path("/user-binding/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.devices.unbind-users")
    @ExceptionMetered(name = "exceptions.v3.devices.unbind-users")
    @ResponseMetered(name = "response.code.v3.devices.unbind-users")
    public void unbindUsersFromDevice(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId, @HeaderParam(Constant.HEADER_SERVICE_NAME) String serviceName,
            @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecrete, @HeaderParam("request-reference-number") String reqRefNumber, DeviceTO deviceTO) {
        
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " unbindUsersFromDevice : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForUnbindUsersFromDevice(deviceTO);
                if (errorMessage == null) {
                    DeviceTO deviceResponseTO = deviceFacadeIntf.unbindUsersFromDevice(deviceTO);
                    response = deviceResponseTO;
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_DEVICE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, DEVICE_RESOURCE_API_LOG + " unbindUsersFromDevice : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
        
    }

}
