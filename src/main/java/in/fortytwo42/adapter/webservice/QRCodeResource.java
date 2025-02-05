
package in.fortytwo42.adapter.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
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
import in.fortytwo42.daos.util.SessionFactoryUtil;


@Path("/v3/qr-codes")
public class QRCodeResource {

    private static final String OR_CODE_RESOURCE_API_LOG = "<<<<< QRCodeResource";
    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(QRCodeResource.class);
    private QRCodeFacadeIntf qrCodeFacade = FacadeFactory.getQRCodeFacade();
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    @InternalSecure
    @ApiLogger
    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.qr-codes")
    @ExceptionMetered(name = "exceptions.v3.qr-codes")
    @ResponseMetered(name = "response.code.v3.qr-codes")
    public void createQRCode(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId, @HeaderParam("request-reference-number") String reqRefNumber,
            QRCodeDataTO qrCodeDataTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            logger.log(Level.DEBUG, OR_CODE_RESOURCE_API_LOG + " createQRCode : start");
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForQRCode(qrCodeDataTO);
                if (errorMessage == null) {
                    QRCodeDataTO qrCodeDataResponseTO = qrCodeFacade.getQRCode(qrCodeDataTO, applicationId);
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
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, OR_CODE_RESOURCE_API_LOG + " createQRCode : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @InternalSecure
    @ApiLogger
    @POST
    @Path("/generic-qr")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.qr-codes")
    @ExceptionMetered(name = "exceptions.v3.qr-codes")
    @ResponseMetered(name = "response.code.v3.qr-codes")
    public void generateGenericQrCode(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId, @HeaderParam("request-reference-number") String reqRefNumber, QRCodeDataTO qrCodeDataTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, OR_CODE_RESOURCE_API_LOG + " generateGenericQrCode : start");
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = FacadeFactory.getQRCodeFacade().createGenericApprovalAttempt(applicationId, qrCodeDataTO);
                errorMessage = ValidationUtilV3.validateQrRequest(qrCodeDataTO);
                if (errorMessage == null) {
                    QRCodeDataTO qrCodeDataResponseTO = qrCodeFacade.generateGenericQRCode(applicationId, qrCodeDataTO);
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
                logger.log(Level.DEBUG, OR_CODE_RESOURCE_API_LOG + " generateGenericQrCode : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }


    @InternalSecure
    @ApiLogger
    @POST
    @Path("/fetch-status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.qr-codes")
    @ExceptionMetered(name = "exceptions.v3.qr-codes")
    @ResponseMetered(name = "response.code.v3.qr-codes")
    public void fetchQRStatus(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam("request-reference-number") String reqRefNumber, QRCodeDataTO qrCodeDataTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, OR_CODE_RESOURCE_API_LOG + " fetchQRStatus : start");
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForQRCode(qrCodeDataTO);
                if (errorMessage == null) {
                    QRCodeDataTO qrCodeDataResponseTO = qrCodeFacade.fetchQRStatus(qrCodeDataTO);
                    response = qrCodeDataResponseTO;
                    responseStatus = Response.Status.OK;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_FETCH_QR_STATUS_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_FETCH_QR_STATUS_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, OR_CODE_RESOURCE_API_LOG + " fetchQRStatus : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

}
