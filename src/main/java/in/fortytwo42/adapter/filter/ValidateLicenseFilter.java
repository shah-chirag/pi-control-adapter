package in.fortytwo42.adapter.filter;

import static in.fortytwo42.adapter.util.Constant.VALIDATE_LICENSE;

import java.io.IOException;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.LicenseServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ClientLicenseTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;

/**
 * The Class ApiLoggingFilter.
 */
@ValidateLicense
@Provider
@Priority(Priorities.AUTHORIZATION)
public class ValidateLicenseFilter implements ContainerRequestFilter {

	private static Logger logger= LogManager.getLogger(ValidateLicenseFilter.class);

	/**
	 * The request.
	 */
	@Context
	private HttpServletRequest request;

	private LicenseServiceIntf licenseService = FacadeFactory.getLicenseFacade();
	private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();
	private final boolean validateLicense =
			Boolean.parseBoolean(Config.getInstance().getAllProperties().getOrDefault(VALIDATE_LICENSE, "false").toString());

	/**
	 * Filter.
	 *
	 * @param requestContext the request context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if (validateLicense) {
			LicenseServiceIntf licenseFacade = FacadeFactory.getLicenseFacade();
			ClientLicenseTO license = licenseFacade.getLicense();
			validateExpiry(license, requestContext);
			validateNumberOfApplications(license, requestContext);
			validateNumberOfUsers(license, requestContext);
		}
	}

	private void validateNumberOfUsers(ClientLicenseTO license, ContainerRequestContext requestContext) {
		if (licenseService.getTotalNumberOfOnboardedUsers() - license.getNumberOfUsers() > 0) {
			requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity(new ErrorTO(errorConstant.getERROR_CODE_LICENSE_CHECK_FAILED(), errorConstant.getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_USERS_EXCEEDED(), errorConstant.getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_USERS_EXCEEDED())).build());
		}
	}

	private void validateNumberOfApplications(ClientLicenseTO license, ContainerRequestContext requestContext) {
		if (licenseService.getTotalNumberOfApplications() - license.getNumberOfApplications() > 0) {
			requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity(new ErrorTO(errorConstant.getERROR_CODE_LICENSE_CHECK_FAILED(), errorConstant.getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_APPLICATION_EXCEEDED(), errorConstant.getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_APPLICATION_EXCEEDED())).build());
		}
	}

	private void validateExpiry(ClientLicenseTO license, ContainerRequestContext requestContext) {
		long expiryTimestampInMillis = license.getExpiryTimestamp();
		if (System.currentTimeMillis() - (expiryTimestampInMillis + license.getAdditionalGracePeriod()) > 0) {
			requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity(new ErrorTO(errorConstant.getERROR_CODE_LICENSE_CHECK_FAILED(), errorConstant.getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_EXPIRED_LICENSE(), errorConstant.getERROR_MESSAGE_LICENSE_CHECK_FAILED_DUE_TO_EXPIRED_LICENSE())).build());
		}

	}
}
