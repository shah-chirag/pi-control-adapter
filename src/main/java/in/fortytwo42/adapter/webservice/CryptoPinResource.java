package in.fortytwo42.adapter.webservice;

import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.CryptoPinFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.CryptoPinTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;

@Path("/v1/crypto-pin")
public class CryptoPinResource {

    private static final String CRYPTO_PIN_LOG = "<<<<< CryptoPinResource";

    private static Logger logger= LogManager.getLogger(CryptoPinResource.class);
    ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private CryptoPinFacadeIntf cryptoPinFacadeIntf = FacadeFactory.getCryptoPinFacade();

    @RolesAllowed(value = {"SUPER_ADMIN"})
    @ApiLogger
    @Secured
    @ResponseToken
    @PUT
    @Path("/reset/application")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void resetApplicationPin(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, CryptoPinTO cryptoPinTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CRYPTO_PIN_LOG + " resetApplicationPin : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                CryptoPinTO cryptoPinTOResponse;
                String errorMessage= ValidationUtilV3.validateResetPin(cryptoPinTO,Constant.APPLICATION);
                if(errorMessage.equals(Constant._EMPTY)){
                    cryptoPinTOResponse =cryptoPinFacadeIntf.resetApplicationPin(cryptoPinTO);
                    response = cryptoPinTOResponse;
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
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_RESET_APPLICATION_PIN_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_RESET_APPLICATION_PIN_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, CRYPTO_PIN_LOG + " resetApplicationPin : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = {"SUPER_ADMIN"})
    @ApiLogger
    @Secured
    @ResponseToken
    @PUT
    @Path("/change/application")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void changeApplicationPin(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, CryptoPinTO cryptoPinTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CRYPTO_PIN_LOG + " changeApplicationPin : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                CryptoPinTO cryptoPinTOResponse;
                String errorMessage= ValidationUtilV3.validateChangePin(cryptoPinTO,Constant.APPLICATION);
                if(errorMessage.equals(Constant._EMPTY)){
                    cryptoPinTOResponse =cryptoPinFacadeIntf.changeApplicationPin(cryptoPinTO);
                    response = cryptoPinTOResponse;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_CHANGE_APPLICATION_PIN_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_CHANGE_APPLICATION_PIN_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, CRYPTO_PIN_LOG + " changeApplicationPin : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }


    @RolesAllowed(value = {"SUPER_ADMIN"})
    @ApiLogger
    @Secured
    @ResponseToken
    @PUT
    @Path("/reset/enterprise")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void resetEnterprisePin(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, CryptoPinTO cryptoPinTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CRYPTO_PIN_LOG + " resetEnterprisePin : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                CryptoPinTO cryptoPinTOResponse;
                String errorMessage= ValidationUtilV3.validateResetPin(cryptoPinTO,Constant.ENTERPRISE);
                if(errorMessage.equals(Constant._EMPTY)){
                    cryptoPinTOResponse =cryptoPinFacadeIntf.resetEnterprisePin(cryptoPinTO);
                    response = cryptoPinTOResponse;
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
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_RESET_ENTERPRISE_PIN_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_RESET_APPLICATION_PIN_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, CRYPTO_PIN_LOG + " resetEnterprisePin : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = {"SUPER_ADMIN"})
    @ApiLogger
    @Secured
    @ResponseToken
    @PUT
    @Path("/change/enterprise")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void changeEnterprisePin(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, CryptoPinTO cryptoPinTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CRYPTO_PIN_LOG + " changeApplicationPin : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                CryptoPinTO cryptoPinTOResponse;
                String errorMessage= ValidationUtilV3.validateChangePin(cryptoPinTO,Constant.ENTERPRISE);
                if(errorMessage.equals(Constant._EMPTY)){
                    cryptoPinTOResponse =cryptoPinFacadeIntf.changeEnterprisePin(cryptoPinTO);
                    response = cryptoPinTOResponse;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_CHANGE_ENTERPRISE_PIN_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_CHANGE_ENTERPRISE_PIN_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, CRYPTO_PIN_LOG + " changeApplicationPin : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

}
