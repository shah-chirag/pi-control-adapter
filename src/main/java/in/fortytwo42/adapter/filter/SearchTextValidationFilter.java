
package in.fortytwo42.adapter.filter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import in.fortytwo42.adapter.exception.QueryFormatException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;

// TODO: Auto-generated Javadoc
/**
 * Filter on all resources.
 *
 * @author Amandeep
 */
@ValidateSearchQuery
@Provider
@Priority(Priorities.AUTHORIZATION)
public class SearchTextValidationFilter implements ContainerRequestFilter {

    /** The search text filter. */
    private String SEARCH_TEXT_VALIDATION_FILTER = "<<<<< SearchTextValidationFilter";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(SearchTextValidationFilter.class);
    
    /** The resource info. */
    @Context
    private ResourceInfo resourceInfo;

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
        logger.log(Level.DEBUG, SEARCH_TEXT_VALIDATION_FILTER + " filter : start");
        String queryParams = requestContext.getHeaderString(Constant.X_QUERY);
        Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
        String searchText = "";
        try {
            searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
        }
        catch (QueryFormatException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_STATUS())).build());
        }
        StringBuilder errorMessage = new StringBuilder();

        if (searchText != null && !Pattern.matches(Config.getInstance().getProperty(Constant.VALIDATION_PATTERN), searchText)) {
            errorMessage.append(Constant.SEARCH_QUERY).append(Constant._COMMA);
        }
        if (errorMessage.length() > 0) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), Constant.COMPULSORY_FIELDS + errorMessage.subSequence(0, errorMessage.length() - 1))).build());
        }
        logger.log(Level.DEBUG, SEARCH_TEXT_VALIDATION_FILTER + " filter : end");
    }
}
