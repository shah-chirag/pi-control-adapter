/**
 * 
 */

package  in.fortytwo42.adapter.util.handler;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;

// TODO: Auto-generated Javadoc
/**
 * The Class IamTimeoutHandler.
 *
 * @author ChiragShah
 */
public class IamTimeoutHandler implements TimeoutHandler {

    /** The iam timeout handler. */
    private String IAM_TIMEOUT_HANDLER = "<<<<< IamTimeoutHandler";

    /** The Constant logger. */
    private static Logger logger= LogManager.getLogger(IamTimeoutHandler.class);
    
    /**
     * Instantiates a new iam timeout handler.
     */
    private IamTimeoutHandler() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final IamTimeoutHandler INSTANCE = new IamTimeoutHandler();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of IamTimeoutHandler.
     *
     * @return single instance of IamTimeoutHandler
     */
    public static IamTimeoutHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Handle timeout.
     *
     * @param asyncResponse the async response
     */
    @Override
    public void handleTimeout(AsyncResponse asyncResponse) {
        final ErrorTO isError = new ErrorTO(Constant.TIMEOUT_ERROR_CODE, Constant.TIMEOUT_ERROR_HUMANIZED_MESSAGE, Constant.ASYNC_RESPONSE_TIMEOUT_IN_CREATE_AUTHATTEMPT);
        asyncResponse.resume(Response.status(Response.Status.REQUEST_TIMEOUT).entity(isError).build());
    }

}
