
package in.fortytwo42.adapter.webservice;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
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

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.AttributeMasterFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;

// TODO: Auto-generated Javadoc
/**
 * The Class AttributeMasterResource.
 */
@Path("/v3/attribute-master")
public class AttributeMasterResource {

    /** The attribute facade. */
    private AttributeMasterFacadeIntf attributeFacade = FacadeFactory.getAttributeMasterFacade();

    /** The attribute master resource api log. */
    private String ATTRIBUTE_MASTER_RESOURCE_API_LOG = "<<<<< AttributeMasterResource";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();


    private Config config = Config.getInstance();
    private Logger logger=LogManager.getLogger(AttributeMasterResource.class);
    /**
     * Gets the attribute master.
     *
     * @param asyncResponse the async response
     * @param applicationId the application id
     * @param applicationName the application name
     * @param applicationSecret the application secret
     * @param applicationLabel the application label
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the attribute master
     */
    @ApiLogger

    @InternalSecure
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.attribute-master")
    @ExceptionMetered(name = "exceptions.v3.attribute-master")
    @ResponseMetered(name = "response.code.v3.attribute-master")
    public void getAttributeMaster(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_APPLICATION_ID) String applicationId,
            @HeaderParam(value = Constant.HEADER_APPLICATION_NAME) final String applicationName, @HeaderParam(value = Constant.HEADER_APPLICATION_SECRET) final String applicationSecret,
            @HeaderParam(value = Constant.HEADER_APPLICATION_LABEL) final String applicationLabel, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " getAttributeMaster : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT,
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
                String errorMessage = ValidationUtilV3.isAttributeValid(attributeName, attributeValue);
                List<AttributeMetadataTO> attributeMetadata = null;
                if (errorMessage == null) {
                    attributeMetadata = attributeFacade.getAttributeMasterForAttributeNameAndValue(attributeName, attributeValue, applicationId);
                }
                else {
                    attributeMetadata = attributeFacade.getAttributeMaster();
                }
                responseStatus = Response.Status.OK;
                response = attributeMetadata;
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
            }
            finally {
                logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " getAttributeMaster : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());

        });
    }

    /**
     * Gets the attribute master.
     *
     * @param asyncResponse the async response
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the attribute master
     */
    @ApiLogger

    @Secured
    @ResponseToken
    @GET
    @Path("/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.attribute-master.attributes")
    @ExceptionMetered(name = "exceptions.v3.attribute-master.attributes")
    @ResponseMetered(name = "response.code.v3.attribute-master.attributes")
    public void getAttributeMaster(@Suspended final AsyncResponse asyncResponse, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " getAttributeMaster : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    Thread.currentThread().getId() + "", Constant.TILT, "Get Evidence", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "Get Evidence called"));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Long userId = null;
                if (StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.USER_ID.getKey()), in.fortytwo42.adapter.enums.QueryParam.USER_ID) != null) {
                    userId = Long.parseLong(
                            (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.USER_ID.getKey()), in.fortytwo42.adapter.enums.QueryParam.USER_ID));
                }
                String accountType = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ACCOUNT_TYPE.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ACCOUNT_TYPE);
                List<AttributeMetadataTO> attributeMetadata = attributeFacade.getAttributeMasterForUserId(userId, accountType, null);
                responseStatus = Response.Status.OK;
                response = attributeMetadata;
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
            }
            finally {
                logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " getAttributeMaster : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());

        });
    }

    /**
     * Adds the attribute meta data.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param attributeMetadataTO the attribute metadata TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger

    @Secured
    @ResponseToken
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.attribute-master")
    @ExceptionMetered(name = "exceptions.v3.attribute-master")
    @ResponseMetered(name = "response.code.v3.attribute-master")
    public void addAttributeMetaData(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, final AttributeMetadataTO attributeMetadataTO,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " addAttributeMetaData : start");
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT,
                            "AD AttributeMetaData", Constant.TILT, System.currentTimeMillis() + Constant.TILT, attributeMetadataTO.getAttributeName(), Constant.TILT, "AD AttributeMetaData called"));
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForAddAttributeMetaData(attributeMetadataTO);
                if (errorMessage == null) {
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    AttributeMetadataTO addAttributeMetaData =
                            attributeFacade.addAttributeMetaData(attributeMetadataTO, actor,id, role, true);
                    responseStatus = Response.Status.OK;
                    response = addAttributeMetaData;
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
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " addAttributeMetaData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Edits the attribute meta data.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param attributeMetadataTO the attribute metadata TO
     * @param attributeName the attribute name
     * @param reqRefNumber the req ref number
     */
    @ApiLogger

    @Secured
    @ResponseToken
    @POST
    @Path("/{attributeName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.attribute-master.attributeName")
    @ExceptionMetered(name = "exceptions.v3.attribute-master.attributeName")
    @ResponseMetered(name = "response.code.v3.attribute-master.attributeName")
    public void editAttributeMetaData(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, final AttributeMetadataTO attributeMetadataTO,
            @PathParam("attributeName") final String attributeName, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " editAttributeMetaData : start");
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT,
                            "Update AttributeMetaData", Constant.TILT, System.currentTimeMillis() + Constant.TILT, attributeMetadataTO.getAttributeName(), Constant.TILT,
                            "Update AttributeMetaData called"));
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForAddAttributeMetaData(attributeMetadataTO);
                if (errorMessage == null) {
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    AttributeMetadataTO addAttributeMetaData =
                            attributeFacade.updatedAttributeMetaData(attributeMetadataTO, actor,id, role,true);
                    responseStatus = Response.Status.OK;
                    response = addAttributeMetaData;
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
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " editAttributeMetaData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Delete attribute meta data.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param attributeName the attribute name
     * @param attributeMetadataTO the attribute metadata TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger

    @Secured
    @ResponseToken
    @POST
    @Path("/delete/{attributeName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.attribute-master.delete.attributeName")
    @ExceptionMetered(name = "exceptions.v3.attribute-master.delete.attributeName")
    @ResponseMetered(name = "response.code.v3.attribute-master.delete.attributeName")
    public void deleteAttributeMetaData(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("attributeName") final String attributeName, final AttributeMetadataTO attributeMetadataTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " deleteAttributeMetaData : start");
            logger.log(Level.DEBUG,
                    StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT,
                            "Delete AttributeMetaData called", Constant.TILT, System.currentTimeMillis() + Constant.TILT, attributeName, Constant.TILT, "Delete AttributeMetaData called"));
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
            	String errorMessage = ValidationUtilV3.isRequestValidForDeleteAttributeMetaData(attributeMetadataTO);
            	if (errorMessage == null) {            		
            		attributeMetadataTO.setAttributeName(attributeName);
            		boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
            		AttributeMetadataTO addAttributeMetaData =
                            attributeFacade.deleteAttributeMetaData(attributeMetadataTO, actor,id, role, true);
            		responseStatus = Response.Status.OK;
            		response = addAttributeMetaData;
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
            }
            finally {
                logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " deleteAttributeMetaData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Gets the attribute masters.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param reqRefNumber the req ref number
     * @return the attribute masters
     */
    @ApiLogger
    @Secured
    @GET
    @Path("/attributes-metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.attribute-master.attributes-metadata")
    @ExceptionMetered(name = "exceptions.v3.attribute-master.attributes-metadata")
    @ResponseMetered(name = "response.code.v3.attribute-master.attributes-metadata")
    public void getAttributeMasters(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " getAttributeMasters : start");
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " getAttributeMasters : start");
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    Thread.currentThread().getId() + "", Constant.TILT, "Get attribute metadata call", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT,
                    "Get attribute meta-data called"));
            Status responseStatus = null;
            Object response = null;
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                int pageNo = (page == null || page < 1) ? 1 : page;
                String errorMessage = ValidationUtilV3.isDataValidSearchText(searchText);
                String attributeName = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME);
                if (errorMessage == null) {
                    if (attributeName != null && !attributeName.isEmpty()) {
                        AttributeMetadataTO attributeMetadataTO = attributeFacade.getAttributeMaster(attributeName);
                        responseStatus = Response.Status.OK;
                        response = attributeMetadataTO;
                    }
                    else {
                        PaginatedTO<AttributeMetadataTO> attributeMetadata = attributeFacade.getAllAttributeMaster(pageNo, searchText);
                        responseStatus = Response.Status.OK;
                        response = attributeMetadata;
                    }
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
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, ATTRIBUTE_MASTER_RESOURCE_API_LOG + " getAttributeMasters : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());

        });
    }
}
