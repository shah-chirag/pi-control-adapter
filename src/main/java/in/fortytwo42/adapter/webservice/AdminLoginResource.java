
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

import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateSearchQuery;
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
import in.fortytwo42.adapter.facade.AdminLoginLogFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.tos.transferobj.AdminAuditLogTO;

@Path("/v3/admin")
public class AdminLoginResource {

    AdminLoginLogFacadeIntf adminLoginLogFacade = FacadeFactory.getAdminLoginLogFacade();

    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    private static Logger logger = LogManager.getLogger(AdminLoginResource.class);

    /** The Constant ADMIN_LOGIN_RESOURCE_LOG. */
    private static final String ADMIN_LOGIN_RESOURCE_LOG = "AdminLoginResource";

    @Secured
    @ResponseToken
    @GET
    @Path("/admin-audit-trail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ValidateSearchQuery
    @Timed(name = "timer.v3.admin.admin-login-trail")
    @ExceptionMetered(name = "exceptions.v3.admin.admin-login-trail")
    @ResponseMetered(name = "response.code.v3.admin.admin-login-trail")
    public void getAdminLoginLog(@Suspended final AsyncResponse asyncResponse, 
            @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ADMIN_LOGIN_RESOURCE_LOG + " getAdminLoginLog : start");

            Status responseStatus = null;
            Object response = null;

            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()), in.fortytwo42.adapter.enums.QueryParam.PAGE);
                String userRole = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.USER_ROLE.getKey()), in.fortytwo42.adapter.enums.QueryParam.USER_ROLE);
                String status = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.STATUS.getKey()), in.fortytwo42.adapter.enums.QueryParam.STATUS);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get(in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),
                        in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                int pageNo = (page == null || page < 1) ? 1 : page;
                PaginatedTO<AdminAuditLogTO> adminAuditLogTO = adminLoginLogFacade.getPaginatedAdminLoginLogList(pageNo, userRole, status, searchText);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(adminAuditLogTO);
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(),
                        errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, ADMIN_LOGIN_RESOURCE_LOG + " getAdminLoginLog : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
