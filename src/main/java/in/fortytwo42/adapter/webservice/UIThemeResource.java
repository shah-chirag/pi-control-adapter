package in.fortytwo42.adapter.webservice;

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

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.UIThemeFacadeImpl;
import in.fortytwo42.adapter.facade.UIThemeFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;

@Path("/v3/theme")
public class UIThemeResource {

    private static final String UI_THEME_RESOURCE_LOG = "<<<<< UIThemeResource";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();
    private static Logger logger= LogManager.getLogger(UIThemeResource.class);
    private Config config = Config.getInstance();
    private UIThemeFacadeIntf uiThemeFacade = UIThemeFacadeImpl.getInstance();

    @ApiLogger
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.theme")
    @ExceptionMetered(name = "exceptions.v3.theme")
    @ResponseMetered(name = "response.code.v3.theme")
    public void getDefaultTheme(@Suspended final AsyncResponse asyncResponse, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, UI_THEME_RESOURCE_LOG + " getDefaultTheme : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            try {
                responseStatus = Response.Status.OK;
                response = uiThemeFacade.getDefaultTheme();
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getERROR_MESSAGE_THEME_NOT_FOUND(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_THEME_NOT_FOUND(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, UI_THEME_RESOURCE_LOG + " getDefaultTheme : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

}
