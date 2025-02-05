package in.fortytwo42.adapter.webservice;

import java.util.ArrayList;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.HealthCheckFacadIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;

@Path("/v3/health-check")

public class HealthCheckResource {
    private static final String HEALTH_CHECK_RESOURCE_API_LOG = "<<<<< HealthCheckResource";
    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(HealthCheckResource.class);

    HealthCheckFacadIntf healthCheckFacade = FacadeFactory.getHealthCheckFacade();



    @GET
    @Path("/get-health")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void getHealthCheckOfConnections(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, HEALTH_CHECK_RESOURCE_API_LOG + " getHealthCheckOfConnections : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            try {
                String healthStatus = "healthy"/*healthCheckFacade.getHealthCheckofConnections()*/;
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(healthStatus);
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, HEALTH_CHECK_RESOURCE_API_LOG + " getHealthCheckOfConnections : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @Deprecated
    @ValidateSearchQuery
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.identity-providers")
    @ExceptionMetered(name = "exceptions.v3.identity-providers")
    @ResponseMetered(name = "response.code.v3.identity-providers")
  public void getFinalResultofHealthCheck(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam("request-reference-number") String reqRefNumber) throws AuthException {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, HEALTH_CHECK_RESOURCE_API_LOG + " getHealthCheckOfConnections : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
          int i=0;
          ArrayList<String> ar= new ArrayList<>();
         while(i<3) {
             try {
                 String response1 = healthCheckFacade.getHealthCheckofConnections();
                 ar.add(response1);
                 i++;
             }
             catch (AuthException e) {
                 logger.log(Level.ERROR, e);
                 ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getERROR_DEV_MESSAGE_INVALID_DATA(), e.getMessage());
                 responseStatus = Response.Status.BAD_REQUEST;
                 response = errorTO;
             }
             catch (Exception e) {
                 logger.log(Level.ERROR, e.getMessage(), e);
                 ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                 responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                 response = errorTO;
             }
         }
        if(ar.stream().distinct().count()==1){
           if( ar.get(0).equals("healthy")){
                response=" healthy";
               responseStatus = Response.Status.OK;
            }
           else if(ar.get(0).equals("unhealthy")){
                response="unhealthy";
               responseStatus = Response.Status.SERVICE_UNAVAILABLE;
           }
        }else {
            response = "healthy";
            responseStatus = Response.Status.OK;
        }

            asyncResponse.resume(Response.status(responseStatus).entity(response).build());

    });
    }


}
