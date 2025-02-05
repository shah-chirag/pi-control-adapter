package in.fortytwo42.adapter.filter;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.service.UserServiceImpl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;

// TODO: Auto-generated Javadoc
/**
 * The Class ResponseTokenFilter.
 */
@ResponseToken
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class ResponseTokenFilter implements ContainerResponseFilter{

    /** The response token filter. */
    private String RESPONSE_TOKEN_FILTER = "<<<<< ResponseTokenFilter";

    private static Logger logger= LogManager.getLogger(ResponseTokenFilter.class);
    
    @Context
    private HttpServletRequest request;

    
    /**
     * Filter.
     *
     * @param requestContext the request context
     * @param responseContext the response context
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        logger.log(Level.DEBUG, RESPONSE_TOKEN_FILTER + " filter : start");
        if (requestContext.getHeaderString("Authorization") != null) {
            String authorizationHeader = requestContext.getHeaderString("Authorization");
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String userName = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long ID=Long.parseLong(payload.get(Constant.ID));
            String permissions = payload.get(Constant.PERMISSIONS);
            String userAgent = requestContext.getHeaderString("User-Agent");
            String clientIp = requestContext.getHeaderString("X-Forwarded-For");
            String ipAddress = "";
            if (clientIp != null) {
                ipAddress += clientIp;
            }
            ipAddress += request.getRemoteAddr();

            String enterpriseAccountId = Config.getInstance().getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
            String responseToken = UserServiceImpl.getInstance().getToken(ID,userName, role, permissions,
                    enterpriseAccountId,userAgent,ipAddress);
            responseContext.getHeaders().add("Response-Token", responseToken);
        }
        requestContext.getHeaders();
        responseContext.getHeaders().add("Access-Control-Expose-Headers","Response-Token"); 
        logger.log(Level.DEBUG, RESPONSE_TOKEN_FILTER + " filter : end");
    }

    

}
