
package in.fortytwo42.adapter.webservice;

import java.io.File;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.AttributeStoreFacadeIntf;
import in.fortytwo42.adapter.facade.EvidenceFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.UserFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.EvidenceRequestTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.AuditLogConstant;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;
import in.fortytwo42.tos.transferobj.UserTO;

// TODO: Auto-generated Javadoc
/**
 * The Class ConsumerResource.
 */
@Path("/v3/consumers")
public class ConsumerResource {

    /** The Constant GET_EVIDENCE_CALLED. */
    private static final String GET_EVIDENCE_CALLED = "Get Evidence called";
    
    /** The Constant GET_EVIDENCE. */
    private static final String GET_EVIDENCE = "Get Evidence";
    
    /** The Constant CONTENT_TYPE. */
    private static final String CONTENT_TYPE = "Content-Type";
    
    /** The Constant GET_ATTRIBUTE. */
    private static final String GET_ATTRIBUTE = "Get Attribute";
    
    /** The user facade. */
    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
    
    /** The attribute facade. */
    private AttributeStoreFacadeIntf attributeFacade = FacadeFactory.getAttributeFacade();
    
    /** The evidence facade intf. */
    private EvidenceFacadeIntf evidenceFacadeIntf = FacadeFactory.getEvidenceFacade();

    /** The Constant CONSUMER_RESOURCE_LOG. */
    private static final String CONSUMER_RESOURCE_LOG = "ConsumerResource";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private Logger logger= LogManager.getLogger(ConsumerResource.class);

    private Config config = Config.getInstance();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();


    /**
     * Find user attributes names.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationSecret the application secret
     * @param applicationLabel the application label
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @InternalSecure
    @GET
    @Path("/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes")
    @ResponseMetered(name = "response.code.v3.consumers.attributes")
    public void findUserAttributesNames(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret, @HeaderParam(value = Constant.HEADER_APPLICATION_LABEL) final String applicationLabel,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " findUserAttributesNames : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_EVIDENCE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, Constant.TILT, GET_EVIDENCE_CALLED));
            Status responseStatus = null;
            Object response = null;
            String attributeName = null;
            String attributeValue;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                attributeName = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME);
                attributeValue = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE);
                String errorMessage = ValidationUtilV3.isAttributeValidForFetchUserDetails(attributeName, attributeValue);
                if (errorMessage == null) {
                    UserTO userTO = userFacade.getUserAttributeNames(attributeName, attributeValue);
                    responseStatus = Response.Status.OK;
                    response = userTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " findUserAttributesNames : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());

        });
    }

    /**
     * Gets the user attributes.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationSecret the application secret
     * @param applicationLabel the application label
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the user attributes
     */
    @ApiLogger
    @InternalSecure
    @GET
    @Path("/attribute-list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attribute-list")
    @ExceptionMetered(name = "exceptions.v3.consumers.attribute-list")
    @ResponseMetered(name = "response.code.v3.consumers.attribute-list")
    public void getUserAttributes(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret, @HeaderParam(value = Constant.HEADER_APPLICATION_LABEL) final String applicationLabel,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getUserAttributes : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    Thread.currentThread().getId() + "", Constant.TILT, "Get Evidence", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "Get Evidence called"));
            Status responseStatus = null;
            Object response = null;
            String attributeName = null;
            String attributeValue;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                attributeName = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME);
                attributeValue = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_VALUE);
                String errorMessage = ValidationUtilV3.isAttributeValidForFetchUserDetails(attributeName, attributeValue);
                if (errorMessage == null) {
                    UserTO userTO = userFacade.getUserAttributesFromDb(attributeName, attributeValue);
                    responseStatus = Response.Status.OK;
                    response = userTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getUserAttributes : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());

        });
    }

    /**
     * Adds the attribute.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationSecret the application secret
     * @param applicationLabel the application label
     * @param attributeDataRequestTO the attribute data request TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    @InternalSecure
    @POST
    @Path("/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes")
    @ResponseMetered(name = "response.code.v3.consumers.attributes")
    public void addAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
            final AttributeDataRequestTO attributeDataRequestTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " addAttribute : start");
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT, String.valueOf(Thread.currentThread().getId()), Constant.TILT,
                            AuditLogConstant.ADD_ATTRIBUTE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, attributeDataRequestTO.getAttributeData().getAttributeName(), Constant.TILT,
                            AuditLogConstant.ADD_ATTRIBUTE));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForAddAttribute(attributeDataRequestTO.getAttributeData());
                if (errorMessage == null) {

                    AttributeDataRequestTO attributeDataResponse = attributeFacade.sendAttributeAdditionRequest(attributeDataRequestTO);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ATTRIBUTE_ADDITION_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ATTRIBUTE_ADDITION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " addAttribute : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the attribute.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationSecret the application secret
     * @param applicationLabel the application label
     * @param requestId the request id
     * @param reqRefNumber the req ref number
     * @return the attribute
     */
    @ApiLogger
    
    @InternalSecure
    @GET
    @Path("/attributes/{request-id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes.request-id")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes.request-id")
    @ResponseMetered(name = "response.code.v3.consumers.attributes.request-id")
    public void getAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret, @HeaderParam(value = Constant.HEADER_APPLICATION_LABEL) final String applicationLabel,
            @PathParam(value = "request-id") final Long requestId, @HeaderParam("request-reference-number") String reqRefNumber) {
        ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
        logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getAttribute : start");
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                AttributeDataRequestTO attributeDataResponse = attributeFacade.getAttributeAdditionStatus(requestId);
                responseStatus = Response.Status.OK;
                response = attributeDataResponse;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_ATTRIBUTE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_ATTRIBUTE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getAttribute : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Verify attribute.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationSecret the application secret
     * @param applicationLabel the application label
     * @param requestId the request id
     * @param attributeTO the attribute TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @InternalSecure
    @POST
    @Path("/attributes/verification-requests/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes.verification-requests.request-id")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes.verification-requests.request-id")
    @ResponseMetered(name = "response.code.v3.consumers.attributes.verification-requests.request-id")
    public void verifyAttribute(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret, @HeaderParam(value = Constant.HEADER_APPLICATION_LABEL) final String applicationLabel,
            @PathParam("requestId") final Long requestId,
            final AttributeTO attributeTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " verifyAttribute : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, AuditLogConstant.VERIFY_ATTRIBUTE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT,
                    AuditLogConstant.VERIFY_ATTRIBUTE_CALLED));
            Status responseStatus = null;
            Object response = null;
            try {
                String errorMessage = ValidationUtilV3.isDataValidForApprove(attributeTO);
                if (errorMessage == null) {
                    attributeTO.setId(requestId);
                    AttributeTO attributeResponseTO = attributeFacade.verifyAttributeRequest(attributeTO);
                    responseStatus = Response.Status.OK;
                    response = attributeResponseTO;
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
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " verifyAttribute : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the attribute verification requests.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationSecret the application secret
     * @param applicationLabel the application label
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the attribute verification requests
     */
    @ApiLogger
    
    @InternalSecure
    @GET
    @Path("/attributes/verification-requests")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes.verification-requests")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes.verification-requests")
    @ResponseMetered(name = "response.code.v3.consumers.attributes.verification-requests")
    public void getAttributeVerificationRequests(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret, @HeaderParam(value = Constant.HEADER_APPLICATION_LABEL) final String applicationLabel,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getAttributeVerificationRequests : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_EVIDENCE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer limit = null, offset = null;
                if (StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.LIMIT.getKey()), in.fortytwo42.adapter.enums.QueryParam.LIMIT) != null) {
                	limit = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.LIMIT.getKey()), in.fortytwo42.adapter.enums.QueryParam.LIMIT);
                } else {
                	limit = Integer.parseInt(config.getProperty(Constant.LIMIT));
                }
                if (StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.OFFSET.getKey()), in.fortytwo42.adapter.enums.QueryParam.OFFSET) != null) {
                	offset = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.OFFSET.getKey()), in.fortytwo42.adapter.enums.QueryParam.OFFSET);
                } else {
                	offset = 0;
                }
                PaginatedTO<UserTO> userResponse = attributeFacade.getPendingAttributeVerificationRequests(limit, offset);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(userResponse);
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getAttributeVerificationRequests : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    /**
     * Request attribute.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param userTO the user TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @InternalSecure
    @POST
    @Path("/request-attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.request-attributes")
    @ExceptionMetered(name = "exceptions.v3.consumers.request-attributes")
    @ResponseMetered(name = "response.code.v3.consumers.request-attributes")
    public void requestAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId, UserTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " requestAttribute : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_ATTRIBUTE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            Status responseStatus = null;
            Object response = null;
            try {
                UserTO userResponseTO = userFacade.requestAttributeFromUser(userTO);
                responseStatus = Response.Status.OK;
                response = userResponseTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " requestAttribute : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the attribute request status.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param requestId the request id
     * @param reqRefNumber the req ref number
     * @return the attribute request status
     */
    @ApiLogger
    
    @InternalSecure
    @GET
    @Path("/request-attributes/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.request-attributes.requestId")
    @ExceptionMetered(name = "exceptions.v3.consumers.request-attributes.requestId")
    @ResponseMetered(name = "response.code.v3.consumers.request-attributes.requestId")
    public void getAttributeRequestStatus(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @PathParam(value = "requestId") Long requestId, @HeaderParam("request-reference-number") String reqRefNumber) {
        ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
        logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getAttributeRequestStatus : start");
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_ATTRIBUTE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            Status responseStatus = null;
            Object response = null;
            try {
                UserTO userResponseTO = userFacade.getAttributeRequest(requestId);
                responseStatus = Response.Status.OK;
                response = userResponseTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getAttributeRequestStatus : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the evidence.
     *
     * @param asyncResponse the async response
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the evidence
     */
    @ApiLogger
    
    @InternalSecure
    @ResponseToken
    @GET
    @Path("/attributes/evidences")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes.evidences")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes.evidences")
    @ResponseMetered(name = "response.code.v3.consumers.attributes.evidences")
    public void getEvidence(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.X_QUERY) final String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getEvidence : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_EVIDENCE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Long evidenceId = null;
                EvidenceStoreTO evidenceTO = new EvidenceStoreTO();
                if (StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.EVIDENCE_ID.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.EVIDENCE_ID) != null) {
                    evidenceId = Long.parseLong((String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.EVIDENCE_ID.getKey()),
                            in.fortytwo42.adapter.enums.QueryParam.EVIDENCE_ID));
                    evidenceTO = evidenceFacadeIntf.getEvidence(evidenceId);
                }
                asyncResponse.resume(Response.status(Response.Status.OK).entity(evidenceTO).build());
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_EVIDENCE_STATUS(), e.getMessage());
                asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).entity(errorTO).build());
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorTO).build());
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getEvidence : end");
            }
        });
    }

    /**
     * Export evidence.
     *
     * @param asyncResponse the async response
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @InternalSecure
    @ResponseToken
    @GET
    @Path("/attributes/evidences/export")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes.evidences.export")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes.evidences.export")
    @ResponseMetered(name = "response.code.v3.consumers.attributes.evidences.export")
    public void exportEvidence(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.X_QUERY) final String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " exportEvidence : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_EVIDENCE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Long evidenceId = null;
                if (StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.EVIDENCE_ID.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.EVIDENCE_ID) != null) {
                    evidenceId = Long.parseLong((String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.EVIDENCE_ID.getKey()),
                            in.fortytwo42.adapter.enums.QueryParam.EVIDENCE_ID));
                }
                File file = evidenceFacadeIntf.downloadEvidence(evidenceId);
                String contentType = "image/jpeg";
                String filename = "evidence_" + System.currentTimeMillis() + ".jpeg";
                asyncResponse.resume(Response.status(Response.Status.OK).entity(file).header(CONTENT_TYPE, contentType)
                        .header("Content-Disposition", "attachment;filename=" + filename).build());
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getERROR_MESSAGE_EVIDENCE_EXPORT_EVIDENCE_FAILED(), e.getMessage());
                asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).entity(errorTO).header(CONTENT_TYPE, "application/json").build());
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorTO).header(CONTENT_TYPE, "application/json").build());
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " exportEvidence : end");
            }
        });
    }

    /**
     * Request evidence.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param userTO the user TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @InternalSecure
    @POST
    @Path("/request-evidence")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.request-evidence")
    @ExceptionMetered(name = "exceptions.v3.consumers.request-evidence")
    @ResponseMetered(name = "response.code.v3.consumers.request-evidence")
    public void requestEvidence(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId, UserTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " requestEvidence : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_ATTRIBUTE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            Status responseStatus = null;
            Object response = null;
            try {
                UserTO userResponseTO = userFacade.requestEvidenceFromUserVerifier(userTO);
                responseStatus = Response.Status.OK;
                response = userResponseTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EVIDENCE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " requestEvidence : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Approve evidence request.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param requestId the request id
     * @param evidenceRequestTO the evidence request TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @InternalSecure
    @POST
    @Path("/request-evidence/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.request-evidence.requestId")
    @ExceptionMetered(name = "exceptions.v3.consumers.request-evidence.requestId")
    @ResponseMetered(name = "response.code.v3.consumers.request-evidence.requestId")
    public void approveEvidenceRequest(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @PathParam("requestId") final Long requestId, EvidenceRequestTO evidenceRequestTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " approveEvidenceRequest : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_ATTRIBUTE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            Status responseStatus = null;
            Object response = null;
            try {
                String errorMessage = ValidationUtilV3.isDataValidForApprove(evidenceRequestTO);
                if (errorMessage == null) {
                    evidenceRequestTO.setId(requestId);
                    EvidenceRequestTO evidenceRequestTO1 = userFacade.approveOrRejectEvidenceRequest(evidenceRequestTO);
                    responseStatus = Response.Status.OK;
                    response = evidenceRequestTO1;
                }else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_APPROVE_EVIDENCE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " approveEvidenceRequest : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the evidence requests.
     *
     * @param asyncResponse the async response
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the evidence requests
     */
    @ApiLogger
    
    @InternalSecure
    @GET
    @Path("/request-evidence")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.request-evidence")
    @ExceptionMetered(name = "exceptions.v3.consumers.request-evidence")
    @ResponseMetered(name = "response.code.v3.consumers.request-evidence")
    public void getEvidenceRequests(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.X_QUERY) final String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getEvidenceRequests : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_ATTRIBUTE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            Status responseStatus = null;
            Object response = null;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String requestType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE);
                Integer limit = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.LIMIT.getKey()), in.fortytwo42.adapter.enums.QueryParam.LIMIT);
                Integer offset = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.OFFSET.getKey()), in.fortytwo42.adapter.enums.QueryParam.OFFSET);
                limit = (limit == null || limit < 0) ? Integer.parseInt(config.getProperty(Constant.LIMIT)) : limit;
                offset = (offset == null || offset < 0) ? Integer.parseInt(config.getProperty(Constant.OFFSET)) : offset;
                String errorMessage = ValidationUtilV3.validateRequestType(requestType);
                if(errorMessage == null) {
                    PaginatedTO<EvidenceRequestTO> evidenceTOs = userFacade.getPendingEvidenceRequest(requestType, limit, offset);
                    responseStatus = Response.Status.OK;
                    response = new Gson().toJson(evidenceTOs);
                }else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getEvidenceRequests : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the evidence request status.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param requestId the request id
     * @param reqRefNumber the req ref number
     * @return the evidence request status
     */
    @ApiLogger
    
    @InternalSecure
    @GET
    @Path("/request-evidence/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.request-evidence.requestId")
    @ExceptionMetered(name = "exceptions.v3.consumers.request-evidence.requestId")
    @ResponseMetered(name = "response.code.v3.consumers.request-evidence.requestId")
    public void getEvidenceRequestStatus(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @PathParam("requestId") final Long requestId, @HeaderParam("request-reference-number") String reqRefNumber) {
        ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
        logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getEvidenceRequestStatus : start");
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    String.valueOf(Thread.currentThread().getId()), Constant.TILT, GET_ATTRIBUTE, Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, GET_EVIDENCE_CALLED));
            Status responseStatus = null;
            Object response = null;
            try {
                EvidenceRequestTO evidenceRequestTO = userFacade.getEvidenceRequest(requestId);
                responseStatus = Response.Status.OK;
                response = evidenceRequestTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_EVIDENCE_STATUS(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " getEvidenceRequestStatus : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @Deprecated
    @ApiLogger
    @InternalSecure
    @PUT
    @Path("/attributes/modify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes")
    @ResponseMetered(name = "response.code.v3.consumers.attributes")
    public void editAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
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
            Status responseStatus;
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
    @PUT
    @Path("/attributes/cam")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes.cam")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes.cam")
    @ResponseMetered(name = "response.code.v3.consumers.attributes.cam")
    public void updateOrDeleteAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
                              @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
                              final AttributeDataRequestTO attributeDataRequestTO, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAttribute : start");
            Status responseStatus;
            Object response;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForAddAttribute(attributeDataRequestTO.getAttributeData());
                if (errorMessage == null) {
                    AttributeDataRequestTO attributeDataResponse = attributeFacade.sendAttributeUpdateOrDeleteRequest(attributeDataRequestTO);
                    attributeDataResponse.setApplicationId(applicationId);
                    attributeDataResponse.setApplicationSecrete(applicationSecret);
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
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAttribute : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    @ApiLogger
    @InternalSecure
    @POST
    @Path("/attributes/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes.delete")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes.delete")
    @ResponseMetered(name = "response.code.v3.consumers.attributes.delete")
    public void deleteAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
            final AttributeDataRequestTO attributeDataRequestTO, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAttribute : start");
            Status responseStatus;
            Object response;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForAddAttribute(attributeDataRequestTO.getAttributeData());
                if (errorMessage == null) {
                    AttributeDataRequestTO attributeDataResponse = attributeFacade.sendAttributeEditRequest(attributeDataRequestTO);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_DELETE_ATTRIBUTE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_DELETE_ATTRIBUTE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAttribute : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    @ApiLogger
    @InternalSecure
    @POST
    @Path("/attributes/verify-attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes.verify-attributes")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes.verify-attributes")
    @ResponseMetered(name = "response.code.v3.consumers.attributes.verify-attributes")
    public void verifyAttributes(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
            final AttributeDataRequestTO attributeDataRequestTO, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " verifyAttributes : start");
            Status responseStatus;
            Object response;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForAddAttribute(attributeDataRequestTO.getAttributeData());
                if (errorMessage == null) {
                    AttributeDataRequestTO attributeDataResponse = attributeFacade.verifyAttribute(attributeDataRequestTO);
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_DELETE_ATTRIBUTE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_DELETE_ATTRIBUTE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " verifyAttributes : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @Deprecated
    @ApiLogger
    @InternalSecure
    @PUT
    @Path("/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.consumers.attributes")
    @ExceptionMetered(name = "exceptions.v3.consumers.attributes")
    @ResponseMetered(name = "response.code.v3.consumers.attributes")
    public void editAndTakeOverAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.HEADER_APPLICATION_ID) final String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
            @HeaderParam(value = Constant.X_DIMFA_UNBIND) final String xDimfaUnbind,
            final AttributeDataRequestTO attributeDataRequestTO, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, CONSUMER_RESOURCE_LOG + " editAndTakeOverAttribute : start");
            Status responseStatus;
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
