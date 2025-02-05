
package in.fortytwo42.adapter.webservice;

import java.io.IOException;
import java.util.List;

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
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.facade.BuildDetailsFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.BuildDetailsTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;

@Path("/v3/build-details")
public class BuildDetailsResource {
    
    private BuildDetailsFacadeIntf buildDetailsFacade = FacadeFactory.getBuildDetailsFacade();
    
    /** The build details resource api log. */
    private static final String BUILD_DETAILS_RESOURCE_API_LOG = "<<<<< BuildDetailsResource";


    private static final String BUILD_DETAILS_RESOURCE_LOG = "BuildDetailsResource";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(BuildDetailsResource.class);
    
    @ApiLogger
    //@Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.build-details")
    @ExceptionMetered(name = "exceptions.v3.build-details")
    @ResponseMetered(name = "response.code.v3.build-details")
    public void getBuildDetails(@Suspended final AsyncResponse asyncResponse, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, BUILD_DETAILS_RESOURCE_LOG + " getBuildDetails : start");
            Status responseStatus = null;
            Object response = null;
            try {
                List<BuildDetailsTO> buildDetails = buildDetailsFacade.getBuildDetails();
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(buildDetails);
            }
            catch (IOException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, BUILD_DETAILS_RESOURCE_LOG + " getBuildDetails : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    /**
     * Get the build version
     *
     * @param asyncResponse the async response
     * @return the builds the details
     */
    @ApiLogger
    @Secured
    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.build-details.version")
    @ExceptionMetered(name = "exceptions.v3.build-details.version")
    @ResponseMetered(name = "response.code.v3.build-details.version")
    public void getBuildVersion(@Suspended final AsyncResponse asyncResponse, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, BUILD_DETAILS_RESOURCE_API_LOG + " getBuildVersion : start");
            Status responseStatus = null;
            Object response = null;
            try {
                BuildDetailsTO buildDetailsTO = new BuildDetailsTO();
                buildDetailsTO.setVersion(Constant.BUILD_VERSION);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(buildDetailsTO);
            }
            catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            logger.log(Level.DEBUG, BUILD_DETAILS_RESOURCE_API_LOG + " getBuildVersion : end");
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

}
