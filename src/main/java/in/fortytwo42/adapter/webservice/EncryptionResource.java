
package in.fortytwo42.adapter.webservice;

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

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.EncryptionFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.enterprise.extension.core.DecryptionDataV2;

// TODO: Auto-generated Javadoc
/**
 * The Class EncryptionResource.
 */
@Path("/v3/encryption")
public class EncryptionResource {

    /** The Constant ENCRYPTION_RESOURCE_LOG. */
    private static final String ENCRYPTION_RESOURCE_LOG = "EncryptionResource";

    private static Logger logger= LogManager.getLogger(EncryptionResource.class);

    private EncryptionFacadeIntf encryptionFacade = FacadeFactory.getEncryptionFacade();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    /**
     * Gets the decryption key.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationLabel the application label
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the decryption key
     */
    @ApiLogger
    @InternalSecure
    @GET
    @Path("/decryption-key")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.encryption.decryption-key")
    @ExceptionMetered(name = "exceptions.v3.encryption.decryption-key")
    @ResponseMetered(name = "response.code.v3.encryption.decryption-key")
    public void getDecryptionKey(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(Constant.HEADER_APPLICATION_LABEL) String applicationLabel, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ENCRYPTION_RESOURCE_LOG + " getDecryptionKey : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String signTransactionId = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SIGN_TRANSACTION_ID.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SIGN_TRANSACTION_ID);
                String type = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.TYPE);
                if (ValidationUtilV3.isDataValid(signTransactionId)) {
                    DecryptionDataV2 decryptionData = encryptionFacade.getDecryptionKey(signTransactionId, applicationId, type);
                    responseStatus = Response.Status.OK;
                    response = decryptionData;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID());
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_SIGN_TRANSACTION_ID());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_DECRYPTION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_DECRYPTION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, ENCRYPTION_RESOURCE_LOG + " getDecryptionKey : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
