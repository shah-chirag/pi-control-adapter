/**
 *
 */

package in.fortytwo42.adapter.filter;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.fortytwo42.adapter.service.ResourceLogServiceImpl;
import in.fortytwo42.adapter.transferobj.APILogTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.handler.ResourceLogHandler;

/**
 * @author ChiragShah
 *
 */
@ApiLogger
@Provider
@Priority(Priorities.ENTITY_CODER)
public class ResponseLoggingFilter implements ContainerResponseFilter {

    private static final String RESPONSE_LOGGING_FILTER = "<<<<< ResponseLoggingFilter";

    @Context
    private HttpServletRequest request;

    private static Logger logger= LogManager.getLogger(ResponseLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        logger.log(Level.DEBUG, RESPONSE_LOGGING_FILTER + " filter : start");
        try {
            String internalRequestReferenceNumber = (String) requestContext.getProperty(Constant.INTERNAL_REQUEST_REFERENCE_NUMBER);
            String requestReferenceNumber = (String) requestContext.getProperty(Constant.REQUEST_REFERENCE_NUMBER);
            responseContext.getHeaders().add(Constant.REQUEST_REFERENSE_NUMBER, requestReferenceNumber);
            Gson gson = new Gson();
            String responseData = gson.toJson(responseContext.getEntity());
            String headerData = gson.toJson(responseContext.getHeaders());
            if (headerData.length() < 4000) {
                String logDestination = Config.getInstance().getProperty(Constant.LOG_DESTINATION);
                logDestination = (logDestination == null || logDestination.isEmpty()) ? Constant.LOG_DESTINATION_FILE : logDestination;
                if(Constant.LOG_DESTINATION_DB.equals(logDestination)){
                    ResourceLogHandler.getInstance().updateResourceData(requestReferenceNumber, headerData, responseData);
                }else{
                    logger.log(Level.DEBUG, "ResponseLoggingFilter : responseData : "+gson.toJson(responseData));
                    logger.log(Level.DEBUG, "ResponseLoggingFilter : responseHeaderData : "+gson.toJson(headerData));
                }
            } else {
                logger.log(Level.INFO, Constant.REQUEST_REFERENCE_NUMBER + " : " + requestReferenceNumber + " Response Data : " + "Larger than 4000 chars. Not Logged in DB.");
            }
            //sendResourceLogToQueue(internalRequestReferenceNumber, responseData, headerData, responseContext.getStatus());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, RESPONSE_LOGGING_FILTER + " filter : end");
        }
    }

    private void sendResourceLogToQueue(String internalRequestReferenceNumber, String responseData, String headerData, int responseStatusCode) {
        logger.log(Level.DEBUG, RESPONSE_LOGGING_FILTER + " sendResourceLogToQueue : start");
        try {
            ResourceLogServiceImpl resourceLogService = ResourceLogServiceImpl.getInstance();
            APILogTO apiLog = resourceLogService.getResourceLog(internalRequestReferenceNumber);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
            String responseSentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            apiLog.setResponseData(responseData);
            apiLog.setResponseCode(responseStatusCode);
            apiLog.setResponseHeaderData(headerData);
            apiLog.setResponseSendOn(Timestamp.valueOf(responseSentTime));
            String resourceLogJson = gson.toJson(apiLog);
            resourceLogService.sendResourceLogToQueue(resourceLogJson);
            resourceLogService.removeResourceLog(internalRequestReferenceNumber);
            logger.log(Level.DEBUG, RESPONSE_LOGGING_FILTER + " resource log sent to QA : ");
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, RESPONSE_LOGGING_FILTER + " sendResourceLogToQueue : end");
        }
    }
}
