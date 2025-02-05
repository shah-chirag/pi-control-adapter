
package in.fortytwo42.adapter.webservice;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
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

import in.fortytwo42.adapter.enums.QueryParam;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.tos.transferobj.FalloutTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.BulkUploadTypeResolver;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.FalloutProcessFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.InternalSecure;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;

@Path("/v3/fallout-process")
public class FalloutProcessingResource {

    private static Logger logger= LogManager.getLogger(FalloutProcessingResource.class);
    private static final String FALLOUT_PROCESSING_API_LOG = "<<<<< FalloutProcessingResource";

    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();
    private FalloutProcessFacadeIntf falloutProcessFacadeIntf = FacadeFactory.getFalloutFacade();
    private Config config = Config.getInstance();
    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    @RolesAllowed(value = {"MAKER", "SUPER_USER", "OPERATIONAL_MAKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void processFalloutData(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_AUTHORIZATION) String authorizationHeader,
                                   @HeaderParam(Constant.FILE_NAME) String fileName, @HeaderParam(Constant.HEADER_FILE_TYPE) String fileType, final InputStream falloutProcessInputStream,
                                   @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " processFalloutData : start");
            Response.Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtil.validateCsvFileNameAndType(fileName, fileType);
                if (errorMessage == null) {
                    response = BulkUploadTypeResolver.getBulkUpload(fileType).upload(falloutProcessInputStream, role,actor,id, fileName);
                    responseStatus = Response.Status.OK;
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            } catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " processFalloutData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @ApiLogger
    @InternalSecure
    @ResponseToken
    @POST
    @Path("/process")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void processFallout(@Suspended final AsyncResponse asyncResponse,
                               @HeaderParam(Constant.START_DATE) String startDate, @HeaderParam(Constant.END_DATE) String endDate, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " processFalloutData : start");
            Response.Status responseStatus = null;
            Object response = null;
            try {
                FacadeFactory.getFalloutFacade().processFallout(startDate, endDate);
                response = "SUCCESS";
                responseStatus = Response.Status.OK;
            } catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " processFalloutData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = {"MAKER", "OPERATIONAL_MAKER", "OPERATIONAL_VIEWONLY", "CHECKER", "OPERATIONAL_CHECKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void getFalloutConfigs(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " getFalloutConfigs : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                int pageNo = (page == null || page < 1) ? 1 : page;
                PaginatedTO<FalloutConfigTO> falloutConfigTOs = falloutProcessFacadeIntf.getFalloutconfigs(pageNo);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(falloutConfigTOs);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " getFalloutConfigs : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = {"MAKER", "OPERATIONAL_MAKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/{falloutConfigId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void editFalloutConfig(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("falloutConfigId") final Long falloutConfigId, FalloutConfigTO falloutConfigTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " editFalloutConfig : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForUpdateFalloutConfig(falloutConfigTO);
                if (errorMessage == null) {
                    boolean saveRequest = Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    falloutConfigTO.setId(falloutConfigId);
                    FalloutConfigTO falloutConfig = falloutProcessFacadeIntf.editFalloutConfig(role, actor,id, saveRequest, falloutConfigTO);
                    response = falloutConfig;
                    responseStatus = Response.Status.OK;
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            } catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_EDIT_FALLOUT_CONFIG(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_EDIT_FALLOUT_CONFIG(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " editFalloutConfig : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = {"MAKER", "OPERATIONAL_MAKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/update_fallout_sync_data/{falloutSyncDataId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void updatefalloutSyncData(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_AUTHORIZATION) String authorizationHeader,
                                      final FalloutSyncDataTo falloutSyncDataTo, @PathParam("falloutSyncDataId") final Long falloutSyncDataId,
                                      @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " processFalloutData : start");
            Response.Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id=Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            falloutSyncDataTo.setId(falloutSyncDataId);
            try {
                String errorMessage = ValidationUtilV3.validateEditFalloutSyncData(falloutSyncDataTo);
                boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                if (errorMessage == null) {
                    response = falloutProcessFacadeIntf.createUpdateSyncDataRequest(role, actor,id,falloutSyncDataTo,
                            saveRequest);
                    responseStatus = Response.Status.OK;
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_DATA() + errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            } catch (AuthException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_REQUEST(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " processFalloutData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @RolesAllowed(value = {"MAKER", "OPERATIONAL_MAKER", "OPERATIONAL_VIEWONLY", "CHECKER", "OPERATIONAL_CHECKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void getAllFalloutSyncData(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " getFalloutConfigs : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                int pageNo = (page == null || page < 1) ? 1 : page;
                PaginatedTO<FalloutSyncDataTo> falloutSyncDataToList = falloutProcessFacadeIntf.getAllFalloutSyncData(pageNo);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(falloutSyncDataToList);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_APPLICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " getFalloutConfigs : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = {"MAKER", "OPERATIONAL_MAKER", "OPERATIONAL_VIEWONLY", "CHECKER", "OPERATIONAL_CHECKER"})
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/fallout-data")
    @ValidateSearchQuery
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void getFalloutData(@Suspended final AsyncResponse asyncResponse, @HeaderParam(Constant.HEADER_AUTHORIZATION) String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam(Constant.REQUEST_REFERENSE_NUMBER) String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " getFalloutData : start");
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Response.Status responseStatus = null;
            Object response = null;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(QueryParam.PAGE.getKey()), QueryParam.PAGE);
                page = (page == null || page < 1) ? 1 : page;
                Integer pageSize = (Integer) StringUtil.parseQueryValue(queryParam.get(QueryParam.PAGE_SIZE.getKey()), QueryParam.PAGE_SIZE);
                pageSize = pageSize == null ? 10 : pageSize;
                String attributeName = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.ATTRIBUTE_NAME.getKey()), QueryParam.ATTRIBUTE_NAME);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.SEARCH_QUERY.getKey()), QueryParam.SEARCH_QUERY);
                String operation = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.OPERATION.getKey()), QueryParam.OPERATION);
                String status = (String) StringUtil.parseQueryValue(queryParam.get(QueryParam.STATUS.getKey()), QueryParam.STATUS);
                Long fromDate = (Long) StringUtil.parseQueryValue(queryParam.get(QueryParam.FROM_DATE.getKey()), QueryParam.FROM_DATE);
                Long toDate = (Long) StringUtil.parseQueryValue(queryParam.get(QueryParam.TO_DATE.getKey()), QueryParam.TO_DATE);
                searchText = searchText != null ? searchText.toUpperCase() : searchText;
                if ((!StringUtil.isNotNullOrEmpty(attributeName) && !StringUtil.isNotNullOrEmpty(searchText)) ||
                        ((StringUtil.isNotNullOrEmpty(attributeName) && StringUtil.isNotNullOrEmpty(searchText)) && (attributeName.equals(QueryParam.MOBILE_NO.getKey()) || attributeName.equals(QueryParam.USER_ID.getKey())))) {
                    response = falloutProcessFacadeIntf.getAllFalloutData(page, pageSize, attributeName, searchText, operation, status, fromDate, toDate);
                    responseStatus = Response.Status.OK;
                } else {
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = new ErrorTO(errorConstant.getERROR_CODE_INVALID_SERACH_ATTRIBUTE(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE(), errorConstant.getERROR_MESSAGE_INVALID_SERACH_ATTRIBUTE());
                }
            } catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                sessionFactoryUtil.closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
                logger.log(Level.DEBUG, FALLOUT_PROCESSING_API_LOG + " getFalloutData : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

}
