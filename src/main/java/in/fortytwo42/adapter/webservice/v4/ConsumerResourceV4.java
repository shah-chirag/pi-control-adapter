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

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.AttributeStoreFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.daos.util.SessionFactoryUtil;

@Path("/v4/consumers")
public class ConsumerResourceV4 {

    private static Logger logger= LogManager.getLogger(ConsumerResourceV4.class);
    private final static String CONSUMER_RESOURCE_LOG = "<<<<< ConsumerResourceV4 ";
    private AttributeStoreFacadeIntf attributeFacade = FacadeFactory.getAttributeFacade();
    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();


    @ApiLogger
    @InternalSecure
    @POST
    @Path("/attributes/verify-attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void verifyAttributes(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
            final AttributeDataRequestTO attributeDataRequestTO, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " verifyAttributes : start");
            Response.Status responseStatus;
            Object response;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForVerifyAttributesV4(attributeDataRequestTO);
                if (errorMessage.equals(Constant._EMPTY)) {
                    AttributeDataRequestTO attributeDataResponse = attributeFacade.verifyAttributeV4(attributeDataRequestTO);
                    attributeDataResponse.setApplicationId(applicationId);
                    attributeDataResponse.setApplicationSecrete(applicationSecret);
                    responseStatus = Response.Status.OK;
                    response = attributeDataResponse;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_VERIFICATION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_VERIFICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " verifyAttributes : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/attributes/modify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.consumers.attributes")
    @ExceptionMetered(name = "exceptions.v4.consumers.attributes")
    @ResponseMetered(name = "response.code.v4.consumers.attributes")
    public void editAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value =
            Constant.HEADER_APPLICATION_ID) final String applicationId,
                                  @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
                                  final AttributeDataRequestTO attributeDataRequestTO, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            long startTime=System.currentTimeMillis();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start ->ConsumerResource -> editAttribute |Epoch:"+startTime);

            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAttribute : start");
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Response.Status responseStatus;
            Object response;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForModifyAttribute(attributeDataRequestTO.getAttributeData());
                if (errorMessage == null) {
                    AttributeDataRequestTO attributeDataResponse = attributeFacade.sendAttributeEditRequest(attributeDataRequestTO);
                    responseStatus = Response.Status.OK;
                    response = attributeDataResponse;
                }else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_ATTRIBUTE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_ATTRIBUTE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAttribute : end");
                long endTimeProcess=System.currentTimeMillis();
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "END ->ConsumerResource -> editAttribute |Epoch:"+endTimeProcess);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "DIFF "+(endTimeProcess-startTime));

            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @ApiLogger
    @InternalSecure
    @POST
    @Path("/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v4.consumers.attributes")
    @ExceptionMetered(name = "exceptions.v4.consumers.attributes")
    @ResponseMetered(name = "response.code.v4.consumers.attributes")
    public void editAndTakeOverAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value =
            Constant.HEADER_APPLICATION_ID) final String applicationId, @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
                                             @HeaderParam(value = Constant.X_DIMFA_UNBIND) final String xDimfaUnbind,
                                             final AttributeDataRequestTO attributeDataRequestTO, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAndTakeOverAttribute : start");
            Response.Status responseStatus;
            Object response;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForModifyAttribute(attributeDataRequestTO.getAttributeData());
                if (errorMessage == null) {
                    attributeDataRequestTO.setApplicationId(applicationId);
                    attributeDataRequestTO.setxDimfaUnbind(Boolean.parseBoolean(xDimfaUnbind));
                    AttributeDataRequestTO attributeDataResponse = attributeFacade.attributeEditAndTakeOver(attributeDataRequestTO);
                    responseStatus = Response.Status.OK;
                    response = attributeDataResponse;
                }else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_ATTRIBUTE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_ATTRIBUTE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAndTakeOverAttribute : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
