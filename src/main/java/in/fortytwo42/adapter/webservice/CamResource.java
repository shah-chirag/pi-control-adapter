package in.fortytwo42.adapter.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.keycloak.representations.AccessTokenResponse;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.CamTokenFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.tos.transferobj.TokenRequestTO;

@Path("/v3/cam")
public class CamResource {

    /** The user resource api log. */
    private final String USER_RESOURCE_API_LOG = "<<<<< CamResource";

    private Logger logger= LogManager.getLogger(CamResource.class);
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private final CamTokenFacadeIntf camTokenFacade = FacadeFactory.getCamTokenFacade();

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/auth/{realm}/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.cam.auth.token")
    @ExceptionMetered(name = "exceptions.v3.cam.auth.token")
    @ResponseMetered(name = "response.code.v3.cam.auth.token")
    @ValidateLicense
    public void getToken(@Suspended final AsyncResponse asyncResponse,
                             @PathParam("realm") String  realm,
                             @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
                             @HeaderParam(Constant.HEADER_APPLICATION_SECRET) String applicationSecret,
                             TokenRequestTO tokenRequestTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getToken : start");
            Response.Status responseStatus;
            Object response;
            try {
                String message = ValidateTokenRequest(tokenRequestTO);
                if(message != null) {
                    throw new AuthException(null,errorConstant.getERROR_CODE_INVALID_DATA(), message);
                }
                AccessTokenResponse token = camTokenFacade.getToken(realm, tokenRequestTO);
                response = new Gson().toJson(token);
                responseStatus = Response.Status.OK;

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), "get token failed", errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, USER_RESOURCE_API_LOG + " getToken : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });


    }

    private String ValidateTokenRequest(TokenRequestTO tokenRequestTO) throws AuthException {

        String message = null;

        if( tokenRequestTO.getSearchAttributes() == null) {
            message = " Search attribute cannot be null";
            return message;
        }
        if(tokenRequestTO.getSearchAttributes().isEmpty()) {
            message=" Search attribute cannot be empty";
            return message;
        }

        if(tokenRequestTO.getClientId() == null) {
            message = "Client Id cannot be null";
            return message;
        }

        if(tokenRequestTO.getClientId().isEmpty()) {
            message = " Client Id cannot be empty";
            return message;
        }

        if(tokenRequestTO.getGrantType() == null ) {
            message = " grant type cannot be null";
            return message;
        }


        if(tokenRequestTO.getGrantType().isEmpty() ) {
            message = " grant type cannot be empty";
            return message;
        }


        if(tokenRequestTO.getScope() ==  null) {
            message = " scope cannot be null";
            return message;
        }

        if(tokenRequestTO.getScope().isEmpty()) {
          message = " scope cannot be empty";
            return message;
        }


        if(tokenRequestTO.getClientSecret() == null ) {
            message = " client secret cannot be null";
            return message;
        }

        if(tokenRequestTO.getClientSecret().isEmpty()) {
            message = " client secret cannot be empty";
            return message;
        }

        if(tokenRequestTO.getUserCredential() == null ) {
            message = "user credential cannot be null";
            return message;
        }

        if(tokenRequestTO.getUserCredential().isEmpty()) {
            message = "user credential cannot be empty";
            return message;
        }
        return message;



    }
}
