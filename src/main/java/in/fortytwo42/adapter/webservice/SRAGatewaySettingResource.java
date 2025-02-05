
package in.fortytwo42.adapter.webservice;

import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.SRAGatewaySettingFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.enterprise.extension.core.DecryptionDataV2;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;

@Path("/sra-gateway-setting")
public class SRAGatewaySettingResource {

    private String SRA_GATEWAY_SETTING_RESOURCE_API_LOG = "<<<<< SRAGatewaySettingResource";

    private SRAGatewaySettingFacadeIntf sraGatewaySettingFacade = FacadeFactory.getSRAGatewaySettingFacade();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(SRAGatewaySettingResource.class);

    private Config config = Config.getInstance();

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ValidateSearchQuery
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.sra-gateway-setting")
    @ExceptionMetered(name = "exceptions.v3.sra-gateway-setting")
    @ResponseMetered(name = "response.code.v3.sra-gateway-setting")
    public void getAllSRAGatewaySetting(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " getSRAGatewaySetting : start");
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Constant.TILT,
                            Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT, "Get SRAGateway Setting",
                            Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT,
                            "Get SRAGateway Setting Called"));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {

                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                PaginatedTO<SRAGatewaySettingTO> sraGatewaySettingTOs = sraGatewaySettingFacade.getAllSRAGatewaySetting(page, searchText);
                responseStatus = Response.Status.OK;
                response = sraGatewaySettingTOs;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_SRA_GATEWAY_SETTING(),
                        e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(),
                        errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " getSRAGatewaySetting : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());

        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @Secured
    @ResponseToken
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.sra-gateway-setting")
    @ExceptionMetered(name = "exceptions.v3.sra-gateway-setting")
    @ResponseMetered(name = "response.code.v3.sra-gateway-setting")
    public void addSRAGatwaySetting(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam("Authorization") String authorizationHeader, final SRAGatewaySettingTO sraGatewaySettingTO,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " addSRAGatwaySetting : start");
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Constant.TILT,
                            Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT, "AD SRAGatwaySetting",
                            Constant.TILT, System.currentTimeMillis() + Constant.TILT,
                            sraGatewaySettingTO.getAddress(), Constant.TILT, "AD SRAGatwaySetting called"));
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                String errorMessage = ValidationUtilV3.isSRAGatewaySetting(sraGatewaySettingTO);
                if (errorMessage == null) {
                    SRAGatewaySettingTO sraGatewaySettingTo =sraGatewaySettingFacade.addSRAGatewaySetting(sraGatewaySettingTO, actor,id, role, saveRequest);
                    responseStatus = Response.Status.OK;
                    response = sraGatewaySettingTo;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(),
                            errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ADD_SRA_GATEWAY_SETTING(),
                        e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(),
                        errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " addSRAGatwaySetting : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @Secured
    @ResponseToken
    @POST
    @Path("/edit/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.sra-gateway-setting.edit")
    @ExceptionMetered(name = "exceptions.v3.sra-gateway-setting.edit")
    @ResponseMetered(name = "response.code.v3.sra-gateway-setting.edit")
    public void editSRAGatewaySetting(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam("Authorization") String authorizationHeader, final SRAGatewaySettingTO sraGatewaySettingTO,
            @PathParam("id") final Long id,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " editSRAGatewaySetting : start");
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Constant.TILT,
                            Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT,
                            "Update SRAGatewaySetting", Constant.TILT, System.currentTimeMillis() + Constant.TILT,
                            sraGatewaySettingTO.getAddress(), Constant.TILT, "Update SRAGatewaySetting called"));
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long userId =Long.parseLong(payload.get(Constant.ID));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            sraGatewaySettingTO.setId(id);
            try {
            	String errorMessage = ValidationUtilV3.isRequestValidForEditSRAGatewaySettings(sraGatewaySettingTO);
            	if (errorMessage == null) {            		
            		boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
            		SRAGatewaySettingTO sraGatewaySettingTo =
                            sraGatewaySettingFacade.updateSRAGatewaySetting(sraGatewaySettingTO, actor,userId, role,saveRequest);
            		responseStatus = Response.Status.OK;
            		response = sraGatewaySettingTo;
            	}else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(),
                        e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(),
                        errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " editAttributeMetaData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @Secured
    @ResponseToken
    @DELETE
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.sra-gateway-setting.delete")
    @ExceptionMetered(name = "exceptions.v3.sra-gateway-setting.delete")
    @ResponseMetered(name = "response.code.v3.sra-gateway-setting.delete")
    public void deleteSRAGatewaySetting(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam("Authorization") String authorizationHeader, @PathParam("id") final Long id, final SRAGatewaySettingTO sraGatewaySettingTO,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " deleteSRAGatewaySetting : start");
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Constant.TILT,
                            Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT,
                            "Delete deleteSRAGatewaySetting called", Constant.TILT,
                            System.currentTimeMillis() + Constant.TILT, "", Constant.TILT,
                            "Delete deleteSRAGatewaySetting called"));
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long actorId =Long.parseLong(payload.get(Constant.ID));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            sraGatewaySettingTO.setId(id);
            try {
            	String errorMessage = ValidationUtilV3.isRequestValidForEditSRAGatewaySettings(sraGatewaySettingTO);
            	if (errorMessage == null) {             		
            		boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
            		SRAGatewaySettingTO sraGatewaySettingTo =sraGatewaySettingFacade.deleteSRAGatewaySetting(sraGatewaySettingTO, actor,actorId, role,saveRequest);
            		responseStatus = Response.Status.OK;
            		response = sraGatewaySettingTo;
            	}else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_DELETE_SRA_GATEWAY_SETTING(),
                        e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(),
                        errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " deleteSRAGatewaySetting : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    @ApiLogger
    @InternalSecure
    @GET
    @Path("/decryption-key")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.sra-gateway-setting.decryption-key")
    @ExceptionMetered(name = "exceptions.v3.sra-gateway-setting.decryption-key")
    @ResponseMetered(name = "response.code.v3.sra-gateway-setting.decryption-key")
    public void getDecryptionKey(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " getDecryptionKey : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String signTransactionId = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SIGN_TRANSACTION_ID.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SIGN_TRANSACTION_ID);
                String type = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.TYPE);
                String host = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TUNNEL_HOST.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.TUNNEL_HOST);
                Integer port = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TUNNEL_PORT.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.TUNNEL_PORT);
                String clientIp = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.CLIENT_IP.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.CLIENT_IP);
                
                if (ValidationUtilV3.isDataValid(signTransactionId)) {
                    DecryptionDataV2 decryptionData = sraGatewaySettingFacade.getDecryptionKey(signTransactionId,clientIp, applicationId, type, host, port);
                    //DecryptionDataV2 decryptionData = encryptionFacade.getDecryptionKey(signTransactionId, applicationId, type);
                    responseStatus = Response.Status.OK;
                    response = decryptionData;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID());
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_DECRYPTION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_DECRYPTION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_RESOURCE_API_LOG + " getDecryptionKey : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
