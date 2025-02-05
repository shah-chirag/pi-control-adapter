
package in.fortytwo42.adapter.webservice;

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
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.UserGroupFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserGroupApplicationRelTO;
import in.fortytwo42.adapter.transferobj.UserUserGroupRelTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.tos.transferobj.UserGroupTO;

@Path("/usergroups")
public class UserGroupResource {

    private static final String USER_GROUP_RESURCE_LOG = "<<<<< UserGroupResource";

    private static Logger logger= LogManager.getLogger(UserGroupResource.class);
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private UserGroupFacadeIntf userGroupFacade = FacadeFactory.getUserGroupFacade();

    @RolesAllowed(value = { "MAKER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @ResponseToken
    @Secured
    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups")
    @ExceptionMetered(name = "exceptions.v3.usergroups")
    @ResponseMetered(name = "response.code.v3.usergroups")
    public void createUserGroup(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, UserGroupTO userGroupTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " createUserGroup : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_CREATE_USER_GROUP();
            try {
                String errorMessage = ValidationUtil.isValidForUserGroupCreate(userGroupTO);
                if (errorMessage == null) {
                    boolean saveRequest = Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null
                                          && Boolean.parseBoolean(Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    UserGroupTO userGroupTo = userGroupFacade.createUserGroup(userGroupTO, role, actor,id, saveRequest);
                    logger.log(Level.DEBUG, "&&&&& userGroupTo : " + new Gson().toJson(userGroupTo));
                    userGroupTo.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = userGroupTo;
                    logger.log(Level.DEBUG, "&&&&& response : " + new Gson().toJson(response));
                    asyncResponse.resume(Response.status(responseStatus).entity(response).build());
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).entity(response).build());
                }
            }
            catch (AuthException e) {
               logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            finally {
                logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " createUserGroup : end");
            }
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups")
    @ExceptionMetered(name = "exceptions.v3.usergroups")
    @ResponseMetered(name = "response.code.v3.usergroups")
    public void getUserGroup(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " getUserGroup : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            String searchText;
            Integer page;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                String errorMessage = ValidationUtil.isDataValidForApplicationAuditTrailSearch(searchText);
                if (errorMessage == null) {
                    int pageNo = (page == null || page < 1) ? 1 : page;
                    PaginatedTO<UserGroupTO> userGroupTO = userGroupFacade.getUserGroups(pageNo, searchText);
                    responseStatus = Response.Status.OK;
                    response = userGroupTO;

                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_DATA());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USERGROUP_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " getUserGroup : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @ResponseToken
    @POST
    @Path("/edit/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups.edit.groupId")
    @ExceptionMetered(name = "exceptions.v3.usergroups.edit.groupId")
    @ResponseMetered(name = "response.code.v3.usergroups.edit.groupId")
    public void updateUserGroup(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("groupId") final Long groupId,
            UserGroupTO userGroupTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " updateUserGroup : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_UPDATE_USER_GROUP();
            try {
                userGroupTO.setId(groupId);
                String errorMessage = ValidationUtil.isValidForUserGroupCreate(userGroupTO);
                if (errorMessage == null) {
                    boolean saveRequest = Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null
                                          && Boolean.parseBoolean(Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    UserGroupTO userGroupTo = userGroupFacade.updateUserGroup(userGroupTO, role, actor,id, saveRequest);
                    userGroupTo.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = userGroupTo;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
               logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_UPDATE_USER_GROUP(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " updateUserGroup : start");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @ResponseToken
    @POST
    @Path("/user-to-usergroup")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups.user-to-usergroup")
    @ExceptionMetered(name = "exceptions.v3.usergroups.user-to-usergroup")
    @ResponseMetered(name = "response.code.v3.usergroups.user-to-usergroup")
    public void userUserGroupMapping(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, UserGroupTO userGroupTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " userUserGroupMapping : start");
            Status responseStatus;
            Object response;
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authorizationHeader);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtil.isValidForUserUserGroupMapping(userGroupTO);
                if (errorMessage == null) {
                    boolean saveRequest = Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null
                                          && Boolean.parseBoolean(Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    UserGroupTO userGroupMappingTo = userGroupFacade.addUserGroupMapping(userGroupTO, role, actor,id, saveRequest);
                    userGroupMappingTo.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = userGroupMappingTo;

                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
               logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getERROR_MESSAGE_USER_BINDING_ALREDY_PRESENT(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_USER_USER_GROUP_MAPPING(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.INFO, "UserGroupResource  :  userUserGroupMapping end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @ResponseToken
    @POST
    @Path("/usergroup-to-application")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups.usergroup-to-application")
    @ExceptionMetered(name = "exceptions.v3.usergroups.usergroup-to-application")
    @ResponseMetered(name = "response.code.v3.usergroups.usergroup-to-application")
    public void userGroupApplicationMapping(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, UserGroupTO userGroupTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " userGroupApplicationMapping : start");
            Status responseStatus;
            Object response;
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authorizationHeader);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtil.isValidForUserGroupApplicationMapping(userGroupTO);
                if (errorMessage == null) {
                    boolean saveRequest = Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null
                                          && Boolean.parseBoolean(Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    UserGroupTO userGroupMappingTo = userGroupFacade.addUserGroupApplicationMapping(userGroupTO, role, actor,id, saveRequest);
                    userGroupMappingTo.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = userGroupMappingTo;

                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
               logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_USER_GROUP_APPLICATION_MAPPING(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_UNBIND_SERVICES(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " userGroupApplicationMapping : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @ResponseToken
    @GET
    @Path("/user-to-usergroup")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups.user-to-usergroup")
    @ExceptionMetered(name = "exceptions.v3.usergroups.user-to-usergroup")
    @ResponseMetered(name = "response.code.v3.usergroups.user-to-usergroup")
    public void getUserUserGroupMapping(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = Constant.X_QUERY) String queryParams) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " getUserUserGroupMapping : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            String searchText;
            Integer page;
            Long groupId;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                groupId = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.GROUP_ID.getKey()), in.fortytwo42.adapter.enums.QueryParam.GROUP_ID);
                String errorMessage = ValidationUtil.isDataValidForApplicationAuditTrailSearch(searchText);
                if (errorMessage == null) {
                    int pageNo = (page == null || page < 1) ? 1 : page;
                    PaginatedTO<UserUserGroupRelTO> userUserGroupRelTO = userGroupFacade.getUserUserGroupMapping(groupId, pageNo, searchText);
                    responseStatus = Response.Status.OK;
                    response = new Gson().toJson(userUserGroupRelTO);
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_DATA());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USERGROUP_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " getUserUserGroupMapping : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @ResponseToken
    @GET
    @Path("/usergroup-to-application")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups.usergroup-to-application")
    @ExceptionMetered(name = "exceptions.v3.usergroups.usergroup-to-application")
    @ResponseMetered(name = "response.code.v3.usergroups.usergroup-to-application")
    public void getUserGroupApplicationMapping(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = Constant.X_QUERY) String queryParams) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " getUserGroupApplicationMapping : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            String searchText;
            Integer page;
            long groupId;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                groupId = (Long) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.GROUP_ID.getKey()), in.fortytwo42.adapter.enums.QueryParam.GROUP_ID);
                String errorMessage = ValidationUtil.isDataValidForApplicationAuditTrailSearch(searchText);
                if (errorMessage == null) {
                    int pageNo = (page == null || page < 1) ? 1 : page;
                    PaginatedTO<UserGroupApplicationRelTO> userGroupApplicationRelTO = userGroupFacade.getUserGroupApplicationMapping(groupId, pageNo, searchText);
                    responseStatus = Response.Status.OK;
                    response = new Gson().toJson(userGroupApplicationRelTO);
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }

            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_DATA());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USERGROUP_APPLICATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " getUserGroupApplicationMapping : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());

        });

    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER" })
    @ApiLogger
    @ResponseToken
    @POST
    @Path("/{groupId}/remove-usergroup")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups.groupId.remove-usergroup")
    @ExceptionMetered(name = "exceptions.v3.usergroups.groupId.remove-usergroup")
    @ResponseMetered(name = "response.code.v3.usergroups.groupId.remove-usergroup")
    public void removeUserGroup(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("groupId") final Long groupId,
            UserGroupTO userGroupTO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " removeUserGroup : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_REMOVE_USER_GROUP();
            try {
                if (groupId != null) {
                    userGroupTO.setId(groupId);
                    boolean saveRequest = Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null
                                          && Boolean.parseBoolean(Config.getInstance().getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    UserGroupTO userGroupRespTO = userGroupFacade.removeUserGroup(role, actor,id,userGroupTO,saveRequest);
                    responseStatus = Response.Status.OK;
                    response = userGroupRespTO;

                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), "gropName is Empty");
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
               logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_REMOVE_USER_GROUP(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), humanizedMessage, errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " removeUserGroup : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }
    
    @RolesAllowed(value = { "MAKER", "CHECKER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @ResponseToken
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.usergroups.userId")
    @ExceptionMetered(name = "exceptions.v3.usergroups.userId")
    @ResponseMetered(name = "response.code.v3.usergroups.userId")
    public void getUserGroups(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId , @HeaderParam(value = Constant.X_QUERY) String queryParams) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " getUserGroups : start");
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            String searchText;
            Integer page;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                String errorMessage = ValidationUtil.isDataValidForApplicationAuditTrailSearch(searchText);
                if (errorMessage == null) {
                    int pageNo = (page == null || page < 1) ? 1 : page;
                    PaginatedTO<UserGroupTO> userGroupTO = userGroupFacade.getGroupsForUser(pageNo, searchText, userId);
                    responseStatus = Response.Status.OK;
                    response = userGroupTO;

                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (in.fortytwo42.adapter.exception.QueryFormatException e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorConstant.getERROR_DEV_MESSAGE_INVALID_DATA());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            catch (Exception e) {
               logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_USERGROUP_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.INFO, USER_GROUP_RESURCE_LOG + " getUserGroups : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }

}
