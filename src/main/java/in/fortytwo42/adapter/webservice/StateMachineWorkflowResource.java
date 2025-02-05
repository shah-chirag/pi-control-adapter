package in.fortytwo42.adapter.webservice;

import java.util.List;
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
import in.fortytwo42.adapter.enums.QueryParam;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.StateMachineWorkflowFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.StateMachineWorkFlowWETO;
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
import in.fortytwo42.enterprise.extension.webentities.AttemptTypeWE;
import in.fortytwo42.enterprise.extension.webentities.ChallengeWE;
import in.fortytwo42.enterprise.extension.webentities.StateMachineWorkFlowWE;

@Path("/v1/state-machine-workflow")
public class StateMachineWorkflowResource {
    /** The user resource api log. */
    private String STATE_MACHINE_WORKFLOW_RESOURCE_LOG = "<<<<< StateMachineWorkflowResource";

    private static Logger logger= LogManager.getLogger(StateMachineWorkflowResource.class);

    private Config config = Config.getInstance();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private StateMachineWorkflowFacadeIntf stateMachineWorkflowFacade = FacadeFactory.getStateMachineWorkflowFacade();

    /**
     * Get All state machine workflows
     * @param asyncResponse
     * @param reqRefNumber
     */
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v1.state-machine-workflow.all")
    @ExceptionMetered(name = "exceptions.v1.state-machine-workflow.all")
    @ResponseMetered(name = "response.code.v1.state-machine-workflow.all")
    public void getAllStateMachineWorkflows(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " getAllStateMachineWorkflows : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                Integer pageSize = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE);
                String searchQuery = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()), QueryParam.SEARCH_QUERY);
                String accountId = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.ACCOUNTID.getKey()), QueryParam.ACCOUNTID);
                String errorMessage = ValidationUtil.isSearchValidForStateMachineWorkFlow(searchQuery);
                if (errorMessage == null) {

                    if (page == null) {
                        page = 1;
                    }
                    if (pageSize == null) {
                        pageSize = 10;
                    }
                    PaginatedTO<StateMachineWorkFlowWE> paginatedList = stateMachineWorkflowFacade.getAllStateMachineWorkflows(page, pageSize, searchQuery, accountId);
                    response = new Gson().toJson(paginatedList);
                    responseStatus = Response.Status.OK;
                }else{
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_STATE_MACHINE_WORKFLOW_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_STATE_MACHINE_WORKFLOW_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " getAllStateMachineWorkflows : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_ADMIN", "SUPER_USER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/onboard")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v1.state-machine-workflow.onboard")
    @ExceptionMetered(name = "exceptions.v1.state-machine-workflow.onboard")
    @ResponseMetered(name = "response.code.v1.state-machine-workflow.onboard")
    public void onboardStateMachineWorkFlow(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, StateMachineWorkFlowWETO stateMachineWorkFlowWETO) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " onboardStateMachineWorkFlow : start");
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
                StateMachineWorkFlowWETO stateMachineWorkFlowResponse;
                boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                stateMachineWorkFlowResponse =
                        stateMachineWorkflowFacade.onboardStateMachineWorkFlow(stateMachineWorkFlowWETO, actor,id, saveRequest);
                response = stateMachineWorkFlowResponse;
                responseStatus = Response.Status.OK;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_STATE_MACHINE_WORKFLOW_ONBOARD_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_STATE_MACHINE_WORKFLOW_ONBOARD_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " onboardStateMachineWorkFlow : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @Secured
    @ResponseToken
    @POST
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.state-machine-workflow.id")
    @ExceptionMetered(name = "exceptions.v3.state-machine-workflow.id")
    @ResponseMetered(name = "response.code.v3.state-machine-workflow.id")
    public void updateStateMachineWorkFlow(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("id") final String id,
                                           StateMachineWorkFlowWETO stateMachineWorkFlowWETO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " updateStateMachineWorkFlow : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String role = payload.get(Constant.ROLE);
            String actor = payload.get(Constant.USER_NAME);
            Long actorId =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            String humanizedMessage = errorConstant.getHUMANIZED_STATE_MACHINE_WORKFLOW_EDIT_FAILED();
            try {
                StateMachineWorkFlowWETO stateMachineWorkFlowWEResponse;
                boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                stateMachineWorkFlowWEResponse =stateMachineWorkflowFacade.updateStateMachineWorkFlow(stateMachineWorkFlowWETO, actor,actorId, saveRequest);
                response = stateMachineWorkFlowWEResponse;
                responseStatus = Response.Status.OK;
            }

            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), humanizedMessage, e.getMessage());
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
                logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " updateStateMachineWorkFlow : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    /**
     * Get All attempt types
     * @param asyncResponse
     * @param reqRefNumber
     */
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/allAttemptTypes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v1.state-machine-workflow.allAttemptTypes")
    @ExceptionMetered(name = "exceptions.v1.state-machine-workflow.allAttemptTypes")
    @ResponseMetered(name = "response.code.v1.state-machine-workflow.allAttemptTypes")
    public void getAllAttemptTypes(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " getAllAttemptTypes : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                Integer pageSize = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE_SIZE);
                PaginatedTO<AttemptTypeWE> paginatedList = stateMachineWorkflowFacade.getAllAttemptTypes(page, pageSize);
                response = new Gson().toJson(paginatedList);
                responseStatus = Response.Status.OK;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_ATTEMPT_TYPES_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_ATTEMPT_TYPES_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " getAllAttemptTypes : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }



    /**
     * Get All challenge types
     * @param asyncResponse
     * @param reqRefNumber
     */
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/allChallengeTypes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v1.state-machine-workflow.allAttemptTypes")
    @ExceptionMetered(name = "exceptions.v1.state-machine-workflow.allAttemptTypes")
    @ResponseMetered(name = "response.code.v1.state-machine-workflow.allAttemptTypes")
    public void getAllChallengeTypes(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " getAllChallengeTypes : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            try {
                List<ChallengeWE> list = stateMachineWorkflowFacade.getAllChallengeTypes();
                response = new Gson().toJson(list);
                responseStatus = Response.Status.OK;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_CHALLENGE_TYPES_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_CHALLENGE_TYPES_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, STATE_MACHINE_WORKFLOW_RESOURCE_LOG + " getAllChallengeTypes : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
