package in.fortytwo42.adapter.webservice;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import com.google.gson.GsonBuilder;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.ServiceFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.tos.transferobj.ServiceTO;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceReseource.
 */
@Path("/v3/services")
public class ServiceReseource {

    /** The service facade intf. */
    private ServiceFacadeIntf serviceFacadeIntf = FacadeFactory.getServiceFacade();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    /** The service resource log. */
    private String SERVICE_RESOURCE_LOG = "<<<<< ServiceReseource";

    private static Logger logger= LogManager.getLogger(ServiceReseource.class);
    
    /**
     * Gets the services.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param reqRefNumber the req ref number
     * @return the services
     */
    @RolesAllowed(value= {"MAKER","CHECKER","VIEW_ONLY","SUPER_USER", "APPLICATION_MAKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.services")
    @ExceptionMetered(name = "exceptions.v3.services")
    @ResponseMetered(name = "response.code.v3.services")
    public void getServices(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, SERVICE_RESOURCE_LOG + " getServices : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                List<ServiceTO> services = serviceFacadeIntf.getServices();
                responseStatus = Response.Status.OK;
                response =  new GsonBuilder().serializeNulls()  // bcoz gson removes start/end date fields from json if they are null
                		.create().toJson(services);
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(),errorConstant.getHUMANIZED_GET_SERVICES_FAILED(),errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, SERVICE_RESOURCE_LOG + " getServices : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
