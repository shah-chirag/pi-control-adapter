package in.fortytwo42.adapter.webservice;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import in.fortytwo42.adapter.facade.BulkUploadTypeResolver;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.annotation.ValidateLicense;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;

@Path("/v3/bulk")
public class BulkActionResource {

    private static Logger logger= LogManager.getLogger(BulkActionResource.class);

    private final String BULK_ACTION_RESOURCE_API_LOG = "<<<<< BulkActionResource";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    @RolesAllowed(value = { "MAKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Timed(name = "timer.v3.bulk.upload")
    @ExceptionMetered(name = "exceptions.v3..bulk.upload")
    @ResponseMetered(name = "response.code.v3.bulk.upload")
    @ValidateLicense
    public void upload(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
                       @HeaderParam(Constant.FILE_NAME) String fileName, final InputStream attributeInputStream,
                       @HeaderParam(Constant.BULK_UPLOAD_TYPE) String bulkUploadType, @HeaderParam("request-reference-number") String reqRefNumber) {

        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);

        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, BULK_ACTION_RESOURCE_API_LOG + " upload : start");
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);

            try {
                String errorMessage =  ValidationUtil.isValidBulkUploadType(fileName, bulkUploadType);
                if (errorMessage == null) {
                    response = BulkUploadTypeResolver.getBulkUpload(bulkUploadType).upload(attributeInputStream, role, actor,id, fileName);
                    responseStatus = Response.Status.OK;
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            } catch (AuthException e) {
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_UPLOAD_FAILED(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } catch (Exception e) {
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_UPLOAD_FAILED(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            } finally {
                logger.log(Level.DEBUG, BULK_ACTION_RESOURCE_API_LOG + " upload : end");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }

    @RolesAllowed(value = { "MAKER", "SUPER_USER", "OPERATIONAL_MAKER" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/download-status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.bulk.download-status")
    @ExceptionMetered(name = "exceptions.v3.bulk.download-status")
    @ResponseMetered(name = "response.code.v3.bulk.download-status")
    public void downloadCsv(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
                            @HeaderParam(Constant.FILE_NAME) String fileName, @HeaderParam(Constant.BULK_UPLOAD_TYPE) String bulkUploadType, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, BULK_ACTION_RESOURCE_API_LOG + " downloadCsv : start");
            Response.Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);

            try{
                String errorMessage =  ValidationUtil.isValidBulkUploadType(null, bulkUploadType);
                if(errorMessage == null) {
                    String content = BulkUploadTypeResolver.getBulkUpload(bulkUploadType).downloadUpdateStatus(fileName, role);
                    if (content != null && !content.isEmpty()) {
                        logger.log(Level.DEBUG, BULK_ACTION_RESOURCE_API_LOG + " downloadCsv : end");
                        asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", "application/octet-stream")
                                .header("Content-Disposition", "attachment;filename=" + fileName).build());
                    } else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INPROGRESS(), errorConstant.getERROR_MESSAGE_INPROGRESS(), errorConstant.getERROR_MESSAGE_INPROGRESS());
                        responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                        response = errorTO;
                        asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                    }
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), e.getMessage(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).entity(response).build());
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_DOWNLOAD_STATUS_FILE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
                asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
            }
        });
    }

    @RolesAllowed(value = { "MAKER", "CHECKER", "SUPER_USER", "OPERATIONAL_MAKER", "OPERATIONAL_CHECKER", "OPERATIONAL_VIEWONLY" })
    @ApiLogger
    @Secured
    @ResponseToken
    @GET
    @Path("/download-sample")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.bulk.download-sample")
    @ExceptionMetered(name = "exceptions.v3.bulk.download-sample")
    @ResponseMetered(name = "response.code.v3.users.download-sample")
    public void downloadSampleCSV(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
                                  @HeaderParam("request-reference-number") String reqRefNumber, @HeaderParam(Constant.HEADER_FILE_TYPE) String fileType,
                                  @HeaderParam(Constant.BULK_UPLOAD_TYPE) String bulkUploadType) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, BULK_ACTION_RESOURCE_API_LOG + " downloadUserOnboardCsv : start");
            try {
                String errorMessage =  ValidationUtil.isValidBulkUploadType(null, bulkUploadType);
                if(errorMessage == null) {
                    String content = BulkUploadTypeResolver.getBulkUpload(bulkUploadType).getSampleCsv(fileType);
                    if (content != null && !content.isEmpty()) {
                        asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", "application/octet-stream")
                                .header("Content-Disposition", "attachment;filename=" + "user-upload.csv").build());
                    }
                    else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_CSV_DOWNLOAD_FAILED(), errorConstant.getHUMANIZED_DOWNLOAD_SAMPLE_CSV_FILE_FAILED(), errorConstant.getERROR_DEVELOPER_CSV_DOWNLOAD_FAILED());
                        asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
                    }
                } else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).header("Content-Type", "application/json").entity(errorTO).build());
                }
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_DOWNLOAD_SAMPLE_CSV_FILE_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(errorTO).build());
            }
            finally {
                logger.log(Level.DEBUG, BULK_ACTION_RESOURCE_API_LOG + " downloadUserOnboardCsv : end");
            }
        });
    }

}

