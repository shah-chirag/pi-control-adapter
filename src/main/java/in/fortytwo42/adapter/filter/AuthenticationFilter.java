
package in.fortytwo42.adapter.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Priority;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserAuthPrincipalServiceIntf;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.exception.NotFoundException;

// TODO: Auto-generated Javadoc
/**
 * Filter on all resources.
 *
 * @author ChiragShah
 */
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    /** The authentication filter. */
    private String AUTHENTICATION_FILTER = "<<<<< AuthenticationFilter";

    private static Logger logger= LogManager.getLogger(AuthenticationFilter.class);
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();
    private UserAuthPrincipalServiceIntf userAuthPrincipalService = ServiceFactory.getUserAuthPrincipalService();

    /** The resource info. */
    @Context
    private ResourceInfo resourceInfo;
    
    /** The request. */
    @Context
    private HttpServletRequest request;

    /**
     * Filter.
     *
     * @param requestContext the request context
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        requestReferenceNumber = requestReferenceNumber != null ? requestReferenceNumber : UUID.randomUUID().toString();
        ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
        logger.log(Level.DEBUG, AUTHENTICATION_FILTER + " filter : start");
        Method method = resourceInfo.getResourceMethod();
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(Constant.BEARER_SPACE)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_TOKEN(), errorConstant.getERROR_MESSAGE_INVALID_TOKEN(), errorConstant.getERROR_DEV_MESSAGE_INVALID_TOKEN())).build());
        }
        else {
            // Extract the token from the HTTP Authorization header
            String token = authorizationHeader.substring(Constant.BEARER.length()).trim();
            String role = (String) JWTokenImpl.getClaimWithoutValidation(token, "Role");
            boolean isValid = JWTokenImpl.parseAndVerifyJWT(token, KeyManagementUtil.getAESKey());
            isValid = validateIPAddress(requestContext, token, isValid);
            if (!isValid) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_TOKEN(), errorConstant.getERROR_MESSAGE_INVALID_TOKEN(), errorConstant.getERROR_DEV_MESSAGE_INVALID_TOKEN())).build());
            } else {
            	try {
					DaoFactory.getUserSessionTokenDao().getByToken(token);
					requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
	                        .entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_TOKEN(), errorConstant.getERROR_MESSAGE_INVALID_TOKEN(), errorConstant.getERROR_DEV_MESSAGE_INVALID_TOKEN())).build());
				} catch (NotFoundException e) {

				}
            }
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAllowed.value()));
                if (!rolesSet.contains(role)) {
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_TOKEN(), errorConstant.getERROR_MESSAGE_INVALID_TOKEN(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED())).build());
                }
            }
        }
        logger.log(Level.DEBUG, AUTHENTICATION_FILTER + " filter : end");
    }
    
    /**
     * Validate IP address.
     *
     * @param requestContext the request context
     * @param token the token
     * @param isValid the is valid
     * @return true, if successful
     */
    private boolean validateIPAddress(ContainerRequestContext requestContext, String token, boolean isValid) {
        logger.log(Level.DEBUG, AUTHENTICATION_FILTER + " validateIPAddress : start");
        String userAgent = requestContext.getHeaderString("User-Agent");
        String clientIp = requestContext.getHeaderString("X-Forwarded-For");
        String ipAddress = "";
        if (clientIp != null) {
            ipAddress += clientIp;
        }
        ipAddress += request.getRemoteAddr();
        Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(token);
        logger.log(Level.INFO, "AuthenticationFilter" + "IP : {0} "+ !ipAddress.equals(payload.get("IpAddress")));
        logger.log(Level.INFO, "AuthenticationFilter" + "Agent : {0} "+ !userAgent.equals(payload.get("UserAgent")));
        
        if (/*!ipAddress.equals(payload.get("IpAddress")) || */!userAgent.equals(payload.get("UserAgent"))) {
            isValid = false;
        }
        logger.log(Level.DEBUG, AUTHENTICATION_FILTER + " validateIPAddress : end");
        return isValid;
    }
}
