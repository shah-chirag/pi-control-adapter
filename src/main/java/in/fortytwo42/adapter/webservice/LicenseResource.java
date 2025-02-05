
package in.fortytwo42.adapter.webservice;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.LicenseServiceIntf;
import in.fortytwo42.adapter.transferobj.ClientLicenseTO;
import in.fortytwo42.adapter.transferobj.LicenseTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;

@Path("/v4/license")
public class LicenseResource {
	private static Logger logger= LogManager.getLogger(LicenseResource.class);

    /**
     * The request resource log.
     */
    private String LICENSE_RESOURCE_LOG = "<<<<< LicenseResource";

    LicenseServiceIntf licenseServiceIntf = FacadeFactory.getLicenseFacade();

    @ApiLogger
    @Secured
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void uploadLicense(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, LicenseTO license) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        logger.log(Level.INFO, LICENSE_RESOURCE_LOG + " Received request to upload license.");
        licenseServiceIntf.uploadLicense(license.getLicense());
        license.setStatus(Constant.SUCCESS_STATUS);
        asyncResponse.resume(Response.status(Response.Status.OK).entity(license).build());
    }

    @ApiLogger
    @Secured
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getLicense(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        logger.log(Level.INFO, LICENSE_RESOURCE_LOG + " Received request to retrieve license.");
        ClientLicenseTO license = licenseServiceIntf.getLicense();
        asyncResponse.resume(Response.status(Response.Status.OK).entity(license).build());
    }

	@ValidateLicense
	@ApiLogger
	@Secured
	@GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public void test(@Suspended final AsyncResponse asyncResponse) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		logger.log(Level.INFO, LICENSE_RESOURCE_LOG + " Received request to test validateLicense annotation.");
		asyncResponse.resume(Response.status(OK).entity("Test successful").build());
	}

}
