
package in.fortytwo42.adapter.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.google.gson.Gson;

import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.adapter.util.handler.ResourceLogHandler;
import in.fortytwo42.entities.bean.ResourceLog;

/**
 * The Class ApiLoggingFilter.
 */
@ApiLogger
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiLoggingFilter implements ContainerRequestFilter {

    private static final String API_LOGGING_FILTER = "<<<<< ApiLoggingFilter";
	private static Logger logger= LogManager.getLogger(ApiLoggingFilter.class);
	private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();
        
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
        logger.log(Level.DEBUG, API_LOGGING_FILTER + " filter : start");
        // checking body is empty or not for non get method
        Request request1= requestContext.getRequest();
        String sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
	    requestContext.setProperty(Constant.REQUEST_START_TIME, sdf);
	    String requestReferenceNumber = null;
        requestReferenceNumber = requestContext.getHeaders().getFirst(Constant.REQUEST_REFERENSE_NUMBER);
        if(requestReferenceNumber == null || requestReferenceNumber.isEmpty()) {
            requestReferenceNumber = UUID.randomUUID().toString();
            requestContext.getHeaders().add(Constant.REQUEST_REFERENSE_NUMBER, requestReferenceNumber);
        }
        // To set request reference for all other filters and for logging
        ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
        requestContext.setProperty(Constant.REQUEST_REFERENCE_NUMBER, requestReferenceNumber);
        Gson gson = new Gson();
	    InputStream requestInputStream = requestContext.getEntityStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[256];
        while ((nRead = requestInputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        ByteArrayOutputStream buffer1=buffer;
        String requestBody = buffer1.toString(StandardCharsets.UTF_8.name()).trim();
        if(!request1.getMethod().equals(HttpMethod.GET) ){
            if(buffer.size()==0|| requestBody.equals("null") || requestBody.isEmpty()|| Pattern.matches(Constant.NULL_CHECK_REGEX, requestBody  )) {
                    requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_REQUEST_BODY(),
                                    errorConstant.getERROR_DEV_MESSAGE_INVALID_REQUEST_BODY())).build());


            }
        }
        try {
            String authorization = requestContext.getHeaderString(Constant.HEADER_AUTHORIZATION);
            String applicationId = requestContext.getHeaderString(Constant.HEADER_APPLICATION_ID);
            String serviceName = requestContext.getHeaderString(Constant.HEADER_SERVICE_NAME);
            String applicationLabel = requestContext.getHeaderString(Constant.HEADER_APPLICATION_LABEL);
            String xQuery = requestContext.getHeaderString(Constant.X_QUERY);
           // String serverId = requestContext.getHeaderString(Constant.SERVER_ID);
			String clientIp = requestContext.getHeaderString(Constant.X_FORWARDED_FOR);
			String remoteAddress  = request.getRemoteAddr();

            String actor = null;
            if (authorization != null && !authorization.isEmpty()) {
            	try {
	                String authToken = authorization.substring(Constant.BEARER.length()).trim();
	                Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
	                actor = payload.get(Constant.USER_NAME);
            	} catch(Exception e) {
            		logger.log(Level.FATAL, e);
            	}
            }
            if(actor == null) {
                actor = applicationId;
            }
            ResourceLog resourceLog = new ResourceLog();
            if (applicationId != null) {
                resourceLog.setApplicationId(applicationId);
            }
            if(applicationLabel != null) {
                resourceLog.setApplicationLabel(applicationLabel);
            }
            if (serviceName != null) {
            	resourceLog.setServiceName(serviceName);
            }
            if (xQuery != null) {
            	resourceLog.setxQuery(xQuery);
            }
           // if (serverId != null) {
            //	resourceLog.setServerId(serverId);
           // }
            if(clientIp != null) {
            	resourceLog.setClientIp(clientIp);
            } 
            if(remoteAddress != null)
            {
                resourceLog.setRemoteAddress(remoteAddress);
            }
            if(requestReferenceNumber!=null) {
                resourceLog.setRequestReferenceNumber(requestReferenceNumber);
            }
            resourceLog.setRequestMethod(requestContext.getMethod());
            resourceLog.setRequestUrl(requestContext.getUriInfo().getPath());
            resourceLog.setActedOn(new Timestamp(System.currentTimeMillis()));
            String headerData = gson.toJson(requestContext.getHeaders());
            if (headerData != null && headerData.length() <= 4000) {
                resourceLog.setRequestHeaderData(headerData);
            }
            String requestData = new String(buffer.toByteArray()).trim();
            if (requestData != null) {
                resourceLog.setRequestData(requestData);
            }
            /*String requestReceivedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            ResourceLogServiceImpl resourceLogService = ResourceLogServiceImpl.getInstance();
            APILogTO apiLog = resourceLogService.convertResourceLogToAPILog(resourceLog);
            apiLog.setRequestReceivedOn(Timestamp.valueOf(requestReceivedTime));
            String internalRequestReferenceNumber = UUID.randomUUID().toString();
            requestContext.setProperty(Constant.INTERNAL_REQUEST_REFERENCE_NUMBER, internalRequestReferenceNumber);
            resourceLogService.addResourceLog(internalRequestReferenceNumber, apiLog);*/
            /**
             * Local DB Logging
             */
            String logDestination = Config.getInstance().getProperty(Constant.LOG_DESTINATION);
            logDestination = (logDestination == null || logDestination.isEmpty()) ? Constant.LOG_DESTINATION_FILE : logDestination;
            if(Constant.LOG_DESTINATION_DB.equals(logDestination)){
                ResourceLogHandler.getInstance().logResourceData(resourceLog);
            }else{
                logger.log(Level.DEBUG, "APILoggingFilter requestData: "+gson.toJson(resourceLog.getRequestData()));
                logger.log(Level.DEBUG, "APILoggingFilter requestUrl: "+resourceLog.getRequestUrl());
                logger.log(Level.DEBUG, "APILoggingFilter requestHeaderData: "+gson.toJson(resourceLog.getRequestHeaderData()));
                logger.log(Level.DEBUG, "APILoggingFilter applicationId: "+resourceLog.getApplicationId());
                logger.log(Level.DEBUG, "APILoggingFilter dateTimeCreated: "+resourceLog.getDateTimeCreated());
            }
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } 
        InputStream inputStream = new ByteArrayInputStream(buffer.toByteArray());
        requestContext.setEntityStream(inputStream);
        logger.log(Level.DEBUG, API_LOGGING_FILTER + " filter : end");
    }
}
