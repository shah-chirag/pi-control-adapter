
package in.fortytwo42.adapter.webservice;

import java.util.List;
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
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.ServerRegistryFacadeIntf;
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
import in.fortytwo42.tos.transferobj.ServerRegistryTO;

@Path("/servers")
public class ServerRegistryResource {

	/** The Constant ROLE_RESOURCE_LOG. */
	private static final String SERVER_REGISTRY_RESOURCE_LOG = "<<<<< ServerRegistryResource";

	private ServerRegistryFacadeIntf buildDetailsFacade = FacadeFactory.getServerRegistryFacade();
	private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

	private static Logger logger= LogManager.getLogger(ServerRegistryResource.class);

	/**
	 * Gets the admin roles.
	 *
	 * @param asyncResponse       the async response
	 * @param authorizationHeader the authorization header
	 * @return the admin roles
	 */
	@ApiLogger
	@Secured
	@ResponseToken
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.servers")
    @ExceptionMetered(name = "exceptions.v3.servers")
    @ResponseMetered(name = "response.code.v3.servers")
	public void getServers(@Suspended final AsyncResponse asyncResponse,
			@HeaderParam("Authorization") String authorizationHeader,
			@HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
			ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
			logger.log(Level.DEBUG, SERVER_REGISTRY_RESOURCE_LOG + " getServers : start");
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			Status responseStatus;
			Object response;
			String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
			Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
			String actor = payload.get(Constant.USER_NAME);
			// String role = payload.get(Constant.ROLE);
			IamThreadContext.setActor(actor);
			try {
				List<ServerRegistryTO> serverRegistryTOs = buildDetailsFacade.getServersRegistry();
				responseStatus = Response.Status.OK;
				response = new Gson().toJson(serverRegistryTOs);
			} catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(),
						errorConstant.getHUMANIZED_GET_ADMIN_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}
			logger.log(Level.DEBUG, SERVER_REGISTRY_RESOURCE_LOG + " getServers : end");
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}
}
