package in.fortytwo42.adapter.filter;
import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

// TODO: Auto-generated Javadoc
/**
 * The Class CORSResponseFilter.
 *
 * @author ChiragShah
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CORSResponseFilter implements ContainerResponseFilter {

    /** The cors response filter. */
    private String CORS_RESPONSE_FILTER = "<<<<< CORSResponseFilter";

    private static Logger logger= LogManager.getLogger(CORSResponseFilter.class);
    
    /**
     * Filter.
     *
     * @param requestContext the request context
     * @param responseContext the response context
     * @throws IOException Signals that an I/O exception has occurred.
     */
    /* (non-Javadoc)
     * @see javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        /*if (requestContext.getHeaderString("Authorization") != null) {
        	String authorizationHeader = requestContext.getHeaderString("Authorization");
        	String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
        	Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
        	String userName = payload.get(Constant.USER_NAME);
        	String role = payload.get(Constant.ROLE);
        	String permissions = payload.get(Constant.PERMISSIONS);
        	String responseToken = AdminProcessorImpl.getInstance().getToken(userName, role, permissions);
        	responseContext.getHeaders().add("Response-Token", responseToken);
        }*/

        String domainName = Config.getInstance().getProperty(Constant.DOMAIN_NAME) != null ? Config.getInstance().getProperty(Constant.DOMAIN_NAME) : Constant.DEFAULT_DOMAIN_NAME;
        logger.log(Level.DEBUG, CORS_RESPONSE_FILTER + " filter : start");
    	requestContext.getHeaders();
        responseContext.getHeaders().add("Access-Control-Allow-Origin", domainName);
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "false");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, Content-Hash, _HttpMethod, X-has-more, X-limit, X-offset, X-query, fileName, Application-Label, Application-Id, Application-Secret, Service-Name, fileType, UserName, Password, Server-IP");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT");
        responseContext.getHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate, precheck=0, Post-check=0");
        responseContext.getHeaders().add("X-Content-Type-Options", "nosniff");
        responseContext.getHeaders().add("X-XSS-Protection", "0");
        responseContext.getHeaders().add("Pragma", "no-cache");
        responseContext.getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        responseContext.getHeaders().add("X-Frame-Options", "DENY");
        responseContext.getHeaders().add("Content-Security-Policy", "default-src 'self'; script-src 'self'");
        logger.log(Level.DEBUG, CORS_RESPONSE_FILTER + " filter : end");
    }
}


