package in.fortytwo42.adapter.webservice;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;
import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.LDAPDetailsFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

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
import java.util.Map;

@Path("/ldap/details")
public class LDAPDetailsResource {


    private static final String LDAP_DETAILS_RESOURCE = "<<<<< LdapDetailsResource";

    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private LDAPDetailsFacadeIntf ldapFacade = FacadeFactory.getLDAPDetailsFacade();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(LDAPDetailsResource.class);

    private Config config = Config.getInstance();


    @RolesAllowed(value = {"MAKER", "CHECKER","APPLICATION_MAKER","APPLICATION_CHECKER","APPLICATION_VIEWONLY","OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY"})
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.onboard")
    @ExceptionMetered(name = "exceptions.v3.users.onboard")
    @ResponseMetered(name = "response.code.v3.users.onboard")
    @ValidateLicense
    public void addLdapDetails(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, LdapDetailsTO ldapDetailsTO,
                            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);

            logger.log(Level.DEBUG, LDAP_DETAILS_RESOURCE + " addLdapDetails : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                logger.log(Level.DEBUG, "<<<<< ldapDetailsTO : " + new Gson().toJson(ldapDetailsTO));
                String errorMessage = ValidationUtilV3.validateAddLdap(ldapDetailsTO);
                if (errorMessage == null) {
                  LdapDetailsTO ldapDetailsresponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    ldapDetailsresponse = ldapFacade.createLdapDetailsRequest(id,role, actor,ldapDetailsTO, saveRequest);
                    response = ldapDetailsresponse;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, LDAP_DETAILS_RESOURCE + " addLdapDetails : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = {"MAKER", "CHECKER","APPLICATION_MAKER","APPLICATION_CHECKER","APPLICATION_VIEWONLY","OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY"})
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/edit/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.onboard")
    @ExceptionMetered(name = "exceptions.v3.users.onboard")
    @ResponseMetered(name = "response.code.v3.users.onboard")
    @ValidateLicense
    public void editLdapDetails(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, LdapDetailsTO ldapDetailsTO,
                               @HeaderParam("request-reference-number") String reqRefNumber,@PathParam("id") final Long ldapId) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);

            logger.log(Level.DEBUG, LDAP_DETAILS_RESOURCE + " editLdapDetails : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            IamThreadContext.setSessionWithoutTransaction(sessionFactoryUtil.openSessionWithoutTransaction());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            ldapDetailsTO.setId(ldapId);
            try {
                logger.log(Level.DEBUG, "<<<<< ldapDetailsTO : " + new Gson().toJson(ldapDetailsTO));
                String errorMessage = ValidationUtilV3.validateEditLdapDetails(ldapDetailsTO);
                if (errorMessage == null) {
                    LdapDetailsTO ldapDetailsresponse;
                    boolean saveRequest = config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_MAKER_CHECKER_ENABLED));
                    ldapDetailsresponse = ldapFacade.editLdapDetails( id,role, actor,ldapDetailsTO, saveRequest);
                    response = ldapDetailsresponse;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), errorConstant.getERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_FAILED());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, LDAP_DETAILS_RESOURCE + " editLdapDetails : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }


    @RolesAllowed(value = {"MAKER", "CHECKER","APPLICATION_MAKER","APPLICATION_CHECKER","APPLICATION_VIEWONLY","OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY"})
    @ValidateSearchQuery
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.all")
    @ExceptionMetered(name = "exceptions.v3.users.all")
    @ResponseMetered(name = "response.code.v3.users.all")
    public void getLdapDetails(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
                         @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, LDAP_DETAILS_RESOURCE + " getLdapDetails : start");
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            IamThreadContext.setActor(actor);
            String  searchText;
            Integer page;
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()), in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                    int pageNo = (page == null || page < 1) ? 1 : page;
                    PaginatedTO<LdapDetailsTO> paginatedUserTOs = ldapFacade.getLdapDetails( pageNo, searchText);
                    responseStatus = Response.Status.OK;
                    response = new Gson().toJson(paginatedUserTOs);
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_GET_LDAP_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, LDAP_DETAILS_RESOURCE + " getLdapDetails : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }


}
