
package in.fortytwo42.adapter.webservice;

import java.io.InputStream;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadPoolController;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.EnterpriseFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.tos.transferobj.EnterpriseTO;

@Path("/v3/enterprises")
public class EnterpriseResource {

    private static final String ENTERPRISE_RESOURCE_API_LOG = "<<<<< EnterpriseResource";

    private static Logger logger= LogManager.getLogger(EnterpriseResource.class);
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    EnterpriseFacadeIntf enterpriseFacade = FacadeFactory.getEnterpriseFacade();

    @RolesAllowed(value = { "SUPER_ADMIN" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.enterprises")
    @ExceptionMetered(name = "exceptions.v3.enterprises")
    @ResponseMetered(name = "response.code.v3.enterprises")
    public void onboardEnterprise(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, EnterpriseTO enterpriseTO,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ENTERPRISE_RESOURCE_API_LOG + " onboardEnterprise : start");
            Status responseStatus;
            Object response;
            try {
                String errorMessage = ValidationUtilV3.isRequestValidForOnboardEnterprise(enterpriseTO);
                if (errorMessage == null) {
                    EnterpriseTO enterprise = enterpriseFacade.onboardEnterprise(enterpriseTO);
                    response = enterprise;
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
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_ONBOARD_ENTERPRISE_FAILED(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ONBOARD_ENTERPRISE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, ENTERPRISE_RESOURCE_API_LOG + " onboardEnterprise : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "SUPER_ADMIN" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.enterprises")
    @ExceptionMetered(name = "exceptions.v3.enterprises")
    @ResponseMetered(name = "response.code.v3.enterprises")
    public void getEnterprise(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ENTERPRISE_RESOURCE_API_LOG + " onboardEnterprise : start");
            Status responseStatus;
            Object response;
            try {
                List<EnterpriseTO> enterprises = enterpriseFacade.getEnterprises();
                response = new Gson().toJson(enterprises);
                responseStatus = Response.Status.OK;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_ONBOARD_APPLICATION(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            finally {
                logger.log(Level.DEBUG, ENTERPRISE_RESOURCE_API_LOG + " onboardEnterprise : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "SUPER_ADMIN" })
    @ApiLogger
    @Secured
    @ResponseToken
    @PUT
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Timed(name = "timer.v3.enterprises.upload")
    @ExceptionMetered(name = "exceptions.v3.enterprises.upload")
    @ResponseMetered(name = "response.code.v3.enterprises.upload")
    public void uploadESCFile(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(Constant.FILE_NAME) String fileName,
            final InputStream cryptoFileInputStream, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ENTERPRISE_RESOURCE_API_LOG + " uploadESCFile : start");
            Status responseStatus;
            Object response;
            try {
                String errorMessage = ValidationUtil.isFileValid(fileName);
                if (errorMessage == null) {
                    enterpriseFacade.uploadFile(fileName, cryptoFileInputStream);
                    EnterpriseTO enterpriseTO = new EnterpriseTO();
                    enterpriseTO.setStatus(Constant.SUCCESS_STATUS);
                    responseStatus = Response.Status.OK;
                    response = enterpriseTO;
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_FILE_UPLOAD_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, ENTERPRISE_RESOURCE_API_LOG + " uploadESCFile : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

}
