package in.fortytwo42.adapter.webservice.v4;

import javax.ws.rs.Consumes;
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
import org.apache.logging.log4j.ThreadContext;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.QRCodeFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.QRCodeDataTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;

@Path("/v4/qr-codes")
public class QRCodeResource {

    private static final String OR_CODE_RESOURCE_API_LOG = "<<<<< QRCodeResourceV4";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(QRCodeResource.class);
    private QRCodeFacadeIntf qrCodeFacade = FacadeFactory.getQRCodeFacade();

    @InternalSecure
    @ApiLogger
    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void createQRCode(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam("request-reference-number") String reqRefNumber,
                             QRCodeDataTO qrCodeDataTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, OR_CODE_RESOURCE_API_LOG + " createQRCode : start");
            Response.Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForQRCodeV4(qrCodeDataTO);
                if (errorMessage == null) {
                    QRCodeDataTO qrCodeDataResponseTO = qrCodeFacade.getQRCodeV4(qrCodeDataTO);
                    response = qrCodeDataResponseTO;
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_QR_CODE_GENERATION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_QR_CODE_GENERATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, OR_CODE_RESOURCE_API_LOG + " createQRCode : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
