package  in.fortytwo42.adapter.webservice;

import java.io.File;
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
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserDataTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.UUIDGenerator;
import in.fortytwo42.adapter.util.ValidationUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.annotation.ApiLogger;
import in.fortytwo42.adapter.util.annotation.ResponseToken;
import in.fortytwo42.adapter.util.annotation.Secured;
import in.fortytwo42.adapter.util.handler.IamTimeoutHandler;
import in.fortytwo42.adapter.util.handler.JWTokenImpl;
import in.fortytwo42.enterprise.extension.tos.EnterpriseTO;
import in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;
import in.fortytwo42.tos.transferobj.RequestTO;
import in.fortytwo42.tos.transferobj.UserTO;

// TODO: Auto-generated Javadoc
/**
 * The Class AttributeResource.
 */
@Path("/v3/users/attributes")
public class AttributeResource {
	
	/** The attribute facade. */
	private AttributeStoreFacadeIntf attributeFacade = FacadeFactory.getAttributeFacade();
	
	/** The user facade. */
	private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
	
	/** The evidence facade intf. */
	private EvidenceFacadeIntf evidenceFacadeIntf = FacadeFactory.getEvidenceFacade();
	private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

	private static Logger logger= LogManager.getLogger(AttributeResource.class);
	
    private Config config = Config.getInstance();

	/** The Constant ATTRIBUTE_RESOURCE_LOG. */
	private static final String ATTRIBUTE_RESOURCE_LOG = "AttributeResource";
	
	/**
	 * Adds the attribute.
	 *
	 * @param asyncResponse the async response
	 * @param authorizationHeader the authorization header
	 * @param userId the user id
	 * @param addAttributeTO the add attribute TO
	 * @param reqRefNumber the req ref number
	 */
	@ApiLogger
    
	@Secured
	@ResponseToken
	@POST
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.users.attributes.userId")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.userId")
    @ResponseMetered(name = "response.code.v3.users.attributes.userId")
    public void addAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId,
            final AttributeTO addAttributeTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " addAttribute : start"); 
			logger.log(Level.DEBUG,
					StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT,
							"AD Authenticate", Constant.TILT, System.currentTimeMillis() + Constant.TILT, addAttributeTO.getAttributeName(), Constant.TILT, "AD authentication called"));
			String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
			Long id=Long.parseLong(payload.get(Constant.ID));
			Status responseStatus = null;
			Object response = null;
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			try {
			    addAttributeTO.setId(userId);
			    if (ValidationUtil.isValid(addAttributeTO.getApprovalStatus())) {
			        String errorMessage = ValidationUtilV3.isRequestValidForApproveAddAttribute(addAttributeTO);
                    if (errorMessage == null) {
                        AttributeTO addAttribute = attributeFacade.approvePendingRequest(addAttributeTO,role, actor,id);
                        responseStatus = Response.Status.OK;
                        response = addAttribute;
                    } else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
			    }else {
			        String errorMessage = ValidationUtilV3.isRequestValidForAddAttribute(addAttributeTO);
                    if(errorMessage == null) {
                        AttributeTO addAttribute = attributeFacade.addAttribute(addAttributeTO,actor,role,id);
                        responseStatus = Response.Status.OK;
                        response = addAttribute;
                    } else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
			    }
			} catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " addAttribute : end"); 
            }
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}

	/**
	 * Gets the attribute verification request.
	 *
	 * @param asyncResponse the async response
	 * @param queryParams the query params
	 * @param reqRefNumber the req ref number
	 * @return the attribute verification request
	 */
	@ApiLogger
	
	@Secured
	@ResponseToken
	@GET
	@Path("/requests")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.users.attributes.requests")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.requests")
    @ResponseMetered(name = "response.code.v3.users.attributes.requests")
    public void getAttributeVerificationRequest(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttributeVerificationRequest : start"); 
			logger.log(Level.DEBUG,
					StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT, Thread.currentThread().getId() + "", Constant.TILT,
							"AD Authenticate", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "AD authentication called"));
			Status responseStatus = null;
			Object response = null;
			Integer page;
			String searchText;
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                page = (Integer) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.PAGE);
                searchText = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),  in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                int pageNo = (page == null || page < 1) ? 1 : page;

				String errorMessage = ValidationUtilV3.isDataValidSearchText(searchText);
				if(errorMessage == null) {
					PaginatedTO<AttributeTO> pendingAuthenticationAttempt = attributeFacade.getAttributeVerificationRequests(pageNo, searchText );
					responseStatus = Response.Status.OK;
					response = new Gson().toJson(pendingAuthenticationAttempt);
				}
				else {
					ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
					responseStatus = Response.Status.BAD_REQUEST;
					response = errorTO;
				}
            } 
            catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttributeVerificationRequest : end"); 
            }
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}
	
	/**
	 * Export evidence.
	 *
	 * @param asyncResponse the async response
	 * @param evidenceId the evidence id
	 * @param reqRefNumber the req ref number
	 */
	@RolesAllowed(value = { "MAKER", "CHECKER" })
	@ApiLogger
	
	@Secured
	@ResponseToken
	@GET
	@Path("/requests/evidences/{evidenceId}/export")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.users.attributes.requests.evidences.evidenceId.export")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.requests.evidences.evidenceId.export")
    @ResponseMetered(name = "response.code.v3.users.attributes.requests.evidences.evidenceId.export")
	public void exportEvidence(@Suspended final AsyncResponse asyncResponse, @PathParam("evidenceId") final Long evidenceId, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttributeMaster : start"); 
			logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
					Thread.currentThread().getId() + "", Constant.TILT, "Get Evidence", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "Get Evidence called"));
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			try {
				File file = evidenceFacadeIntf.downloadEvidence(evidenceId);
				String contentType = "image/jpeg";
				String filename = "evidence_" + System.currentTimeMillis() + ".jpeg";
				asyncResponse.resume(Response.status(Response.Status.OK).entity(file).header("Content-Type", contentType)
						.header("Content-Disposition", "attachment;filename=" + filename).build());
			} catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), e.getMessage());
                asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorTO).header("Content-Type", "application/json").build());
            } catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorTO).header("Content-Type", "application/json").build());
			}
		});
	}

	/**
	 * Gets the verfication requests.
	 *
	 * @param asyncResponse the async response
	 * @param authorizationHeader the authorization header
	 * @param queryParams the query params
	 * @param reqRefNumber the req ref number
	 * @return the verfication requests
	 */
	@ApiLogger
	
	@Secured
	@ResponseToken
	@GET
	@Path("/all-requests")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.users.attributes.all-requests")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.all-requests")
    @ResponseMetered(name = "response.code.v3.users.attributes.all-requests")
	public void getVerficationRequests(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
			@HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getVerficationRequests : start"); 
			logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT,
					Thread.currentThread().getId() + "", Constant.TILT, "Get Evidence", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "Get Evidence called"));
			Status responseStatus = null;
			Object response = null;
			String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
			Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
			String actor = payload.get(Constant.USER_NAME);
			String role = payload.get(Constant.ROLE);
			IamThreadContext.setCorelationId(UUIDGenerator.generate());
			try {
				Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
				Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.PAGE);
				String requestType = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE);
				int pageNo = (page == null || page < 1) ? 1 : page;
				PaginatedTO<RequestTO> requestTOs = attributeFacade.getPendingAttributeRequests(pageNo, role,requestType);
				responseStatus = Response.Status.OK;
				response = new Gson().toJson(requestTOs);
			}catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            } catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
				responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
				response = errorTO;
			}finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getVerficationRequests : end"); 
            }
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}

	/**
	 * Edits the attribute verification.
	 *
	 * @param asyncResponse the async response
	 * @param authorizationHeader the authorization header
	 * @param requestId the request id
	 * @param verifyAttributeTO the verify attribute TO
	 * @param reqRefNumber the req ref number
	 */
	@ApiLogger
	
	@Secured
	@ResponseToken
	@POST
	@Path("/requests/{requestId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.users.attributes.requests.requestId")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.requests.requestId")
    @ResponseMetered(name = "response.code.v3.users.attributes.requests.requestId")
	public void editAttributeVerification(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("requestId") final Long requestId,
			final AttributeTO verifyAttributeTO, @HeaderParam("request-reference-number") String reqRefNumber) {
		asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
		asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
		IamThreadPoolController.getInstance().submitTask(() -> {
		    ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " editAttributeVerification : start"); 
			logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT,
					Thread.currentThread().getId() + "", Constant.TILT, "Get Evidence", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "Get Evidence called"));
			Status responseStatus = null;
			Object response = null;
			String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
			Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
			String actor = payload.get(Constant.USER_NAME);
			String role = payload.get(Constant.ROLE);
			Long id=Long.parseLong(payload.get(Constant.ID));
			try {
				verifyAttributeTO.setId(requestId);
				if (ValidationUtil.isValid(verifyAttributeTO.getApprovalStatus())) {
					///TODO: chek only approval status and comments. ReferisUserValidForApproval
				    String errorMessage = ValidationUtilV3.isRequestValidForApprove(verifyAttributeTO);
				    if (errorMessage == null) {
						attributeFacade.approvePendingRequest(verifyAttributeTO, role, actor,id);
						responseStatus = Response.Status.OK;
						response = verifyAttributeTO;
					} else {
						ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
						responseStatus = Response.Status.BAD_REQUEST;
						response = errorTO;
					}
				} else {
					//TODO: only check verification status SUCCESS or FAILED
					String errorMessage = ValidationUtilV3.isRequestValidForUpdate(verifyAttributeTO);
					if(errorMessage == null) {
						attributeFacade.verifyAttribute(verifyAttributeTO, actor, id);
						responseStatus = Response.Status.OK;
						response = verifyAttributeTO;
					} else {
						ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
						responseStatus = Response.Status.BAD_REQUEST;
						response = errorTO;
					}
				}
			} catch (AuthException e) {
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
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " editAttributeVerification : end"); 
            }
			asyncResponse.resume(Response.status(responseStatus).entity(response).build());
		});
	}
	
	/**
	 * Gets the attribute.
	 *
	 * @param asyncResponse the async response
	 * @param requestId the request id
	 * @param reqRefNumber the req ref number
	 * @return the attribute
	 */
	@ApiLogger
    
    @Secured
    @ResponseToken
    @GET
    @Path("/requests/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.users.attributes.requests.requestId")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.requests.requestId")
    @ResponseMetered(name = "response.code.v3.users.attributes.requests.requestId")
	public void getAttribute(@Suspended final AsyncResponse asyncResponse, @PathParam("requestId") final Long requestId, @HeaderParam("request-reference-number") String reqRefNumber) {
	    asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttribute : start"); 
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT, Constant.TILT,
                    Thread.currentThread().getId() + "", Constant.TILT, "Get Attribute", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "Get Evidence called"));
            Status responseStatus = null;
            Object response = null;
            try {
            	AttributeDataTO attributeTO = attributeFacade.getAttribute(requestId);
               responseStatus = Response.Status.OK;
               response = attributeTO;
            }catch (AuthException e) {
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
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttribute : end"); 
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });  
	}
	
	/**
	 * Gets the adds the attribute requests.
	 *
	 * @param asyncResponse the async response
	 * @param authorizationHeader the authorization header
	 * @param queryParams the query params
	 * @param reqRefNumber the req ref number
	 * @return the adds the attribute requests
	 */
	@ApiLogger
    
    @Secured
    @ResponseToken
    @GET
    @Path("/add-requests")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.users.attributes.add-requests")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.add-requests")
    @ResponseMetered(name = "response.code.v3.users.attributes.add-requests")
    public void getAddAttributeRequests(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
	    asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAddAttributeRequests : start"); 
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    Thread.currentThread().getId() + "", Constant.TILT, "Get Evidence", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "Get Evidence called"));
            Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.PAGE);
                String requestType = "ATTRIBUTE_ADDITION";
                int pageNo = (page == null || page < 1) ? 1 : page;
                PaginatedTO<RequestTO> requestTOs = attributeFacade.getPendingAttributeRequests(pageNo, role,requestType);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(requestTOs); 
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_USER_SUBSCRIPTION(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAddAttributeRequests : end"); 
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
	}
	
	/**
	 * Gets the attribute trail.
	 *
	 * @param asyncResponse the async response
	 * @param authorizationHeader the authorization header
	 * @param queryParams the query params
	 * @param reqRefNumber the req ref number
	 * @return the attribute trail
	 */
	@ApiLogger
    
    @Secured
    @ResponseToken
    @GET
    @Path("/attribute-trail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	@Timed(name = "timer.v3.users.attributesr.attribute-trail")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.attribute-trail")
    @ResponseMetered(name = "response.code.v3.users.attributes.attribute-trail")
    public void getAttributeTrail(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
	    asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttributeTrail : start"); 
            logger.log(Level.DEBUG, StringUtil.build(Constant.RANDOM, Constant.TILT,  Constant.TILT,
                    Thread.currentThread().getId() + "", Constant.TILT, "Get Evidence", Constant.TILT, System.currentTimeMillis() + Constant.TILT, "", Constant.TILT, "Get Evidence called"));
            Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.PAGE);
                String requestType = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.REQUEST_TYPE);
                Long fromDate = (Long) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.FROM_DATE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.FROM_DATE);
                Long toDate = (Long) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.TO_DATE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.TO_DATE);
                String searchText = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY.getKey()),  in.fortytwo42.adapter.enums.QueryParam.SEARCH_QUERY);
                int pageNo = (page == null || page < 1) ? 1 : page;
                PaginatedTO<RequestTO> requestTOs = attributeFacade.getPaginatedNonPendingRequests(page, role, requestType);
                responseStatus = Response.Status.OK;
                response = new Gson().toJson(requestTOs); 
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_MESSAGE_AUTHENTICATION_FAILED(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttributeTrail : end"); 
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
	}
	
    /**
     * Upload attribute.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param fileName the file name
     * @param fileType the file type
     * @param attributeInputStream the attribute input stream
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @Secured
    @ResponseToken
    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Timed(name = "timer.v3.users.attributes.upload")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.upload")
    @ResponseMetered(name = "response.code.v3.users.attributes.upload")
    public void uploadAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(Constant.FILE_NAME) String fileName, @HeaderParam(Constant.HEADER_FILE_TYPE) String fileType, final InputStream attributeInputStream, @HeaderParam("request-reference-number") String reqRefNumber) {
	    asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " uploadAttribute : start"); 
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
	        Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtil.isValidCSV(fileName);
                if (errorMessage == null) {
                    CSVUploadTO csvUploadTO = attributeFacade.uploadAttributes(fileType, attributeInputStream, role,id, fileName);
                    response = csvUploadTO;
                    responseStatus = Response.Status.OK;
                    /*asyncResponse.resume(Response.status(Response.Status.OK).entity(content).header("Content-Type", "application/octet-stream")
                            .header("Content-Disposition", "attachment;filename=" + filename).build());*/
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_FILE_TYPE(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO; 
                    asyncResponse.resume(Response.status(responseStatus).header("Content-Type", "application/json").entity(response).build());
                }
            } catch (AuthException e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            catch (Exception e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " uploadAttribute : end"); 
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
	}
	
    /**
     * Gets the attributes.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param queryParams the query params
     * @param reqRefNumber the req ref number
     * @return the attributes
     */
    @ApiLogger
    
    @Secured
    @ResponseToken
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.attributes")
    @ExceptionMetered(name = "exceptions.v3.users.attributes")
    @ResponseMetered(name = "response.code.v3.users.attributes")
    public void getAttributes(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttributes : start"); 
            Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String userIdentifier = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.USER_IDENTIFIER.getKey()),
                         in.fortytwo42.adapter.enums.QueryParam.USER_IDENTIFIER);
                UserDataTO userTO = userFacade.getUserAttributesFromDb(userIdentifier, role, actor);
                responseStatus = Response.Status.OK;
                response = userTO;
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_ATTRIBUTE(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_ATTRIBUTE(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getAttributes : end"); 
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });

    }
    
    /**
     * Request attribute.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param userId the user id
     * @param userTO the user TO
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @Secured
    @ResponseToken
    @POST
    @Path("/request-attributes/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.attributes.request-attributes.userId")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.request-attributes.userId")
    @ResponseMetered(name = "response.code.v3.users.attributes.request-attributes.userId")
    public void requestAttribute(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") final Long userId, UserTO userTO, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " requestAttribute : Start");
            Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
	        Long id =Long.parseLong(payload.get(Constant.ID));
            IamThreadContext.setCorelationId(UUIDGenerator.generate());
            try {
                userTO.setId(userId);
                if (ValidationUtil.isValid(userTO.getApprovalStatus())) {
                    String errorMessage = ValidationUtilV3.isRequestValidForApprove(userTO);
                    if(errorMessage == null) {
                        UserTO userResponseTO = userFacade.approveRequestAttribute(userTO, actor, role, id);
                        responseStatus = Response.Status.OK;
                        response = userResponseTO;
                    }else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
                }else {
                    String errorMessage = ValidationUtilV3.isRequestValidForAttributeRequest(userTO);
                    if(errorMessage == null) {
                        UserTO userResponseTO = attributeFacade.requestAttribute(userTO, actor, role, id);
                        responseStatus = Response.Status.OK;
                        response = userResponseTO;
                    }else {
                        ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                        responseStatus = Response.Status.BAD_REQUEST;
                        response = errorTO;
                    }
                }
            }catch (AuthException e) {
                logger.log(Level.ERROR, e);
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_REQUEST_ATTRIBUTE(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_REQUEST_ATTRIBUTE(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " requestAttribute : End");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    /**
     * Upload attribute evidence.
     *
     * @param asyncResponse the async response
     * @param authorizationHeader the authorization header
     * @param fileName the file name
     * @param attributeInputStream the attribute input stream
     * @param reqRefNumber the req ref number
     */
    @ApiLogger
    
    @Secured
    @ResponseToken
    @POST
    @Path("/upload_evidence")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Timed(name = "timer.v3.users.attributes.upload_evidence")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.upload_evidence")
    @ResponseMetered(name = "response.code.v3.users.attributes.upload_evidence")
    public void uploadAttributeEvidence(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader,
            @HeaderParam(Constant.FILE_NAME) String fileName, final InputStream attributeInputStream, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " uploadAttributeEvidence : start"); 
            Status responseStatus;
            Object response;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                String errorMessage = ValidationUtilV3.isDataValidForEvidence(attributeInputStream, fileName);
                if(errorMessage == null) {
                EvidenceStoreTO evidenceStoreTO = attributeFacade.uploadAttributeEvidence(attributeInputStream, role, fileName);
                responseStatus = Response.Status.OK;
                response = evidenceStoreTO;
                }
                else {
                    ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_INVALID_DATA(), errorMessage);
                    responseStatus = Response.Status.BAD_REQUEST;
                    response = errorTO;
                }
            }
            catch (AuthException e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.BAD_REQUEST;
                response = errorTO;
            }
            catch (Exception e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_UPLOAD_ATTRIBUTE_REQUEST(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }finally {
                logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " uploadAttributeEvidence : End");
            }
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    @ApiLogger
    
    @Secured
    @ResponseToken
    @GET
    @Path("/enterprises")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.attributes.enterprises")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.enterprises")
    @ResponseMetered(name = "response.code.v3.users.attributes.enterprises")
    public void getEnterprises(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams, @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getEnterprises : start"); 
            Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.PAGE);
                int pageNo = (page == null || page < 1) ? 1 : page;
                PaginatedTO<EnterpriseTO> enterprises = attributeFacade.getEnterprises(pageNo);
                response = enterprises;
                responseStatus = Response.Status.OK;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_ENTERPRISE(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getEnterprises : end"); 
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
    
    @ApiLogger
    
    @Secured
    @ResponseToken
    @GET
    @Path("/verifiers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(name = "timer.v3.users.attributes.verifiers")
    @ExceptionMetered(name = "exceptions.v3.users.attributes.verifiers")
    @ResponseMetered(name = "response.code.v3.users.attributes.verifiers")
    public void getVerifiers(@Suspended final AsyncResponse asyncResponse, @HeaderParam("Authorization") String authorizationHeader, @HeaderParam(value = Constant.X_QUERY) String queryParams,
            @HeaderParam("request-reference-number") String reqRefNumber) {
        asyncResponse.setTimeoutHandler(IamTimeoutHandler.getInstance());
        asyncResponse.setTimeout(Constant.TIMEOUT_SPAN, Constant.DEFAULT_TIME_UNIT);
        IamThreadPoolController.getInstance().submitTask(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getVerifiers : start"); 
            Status responseStatus = null;
            Object response = null;
            String authToken = authorizationHeader.substring(Constant.BEARER.length()).trim();
            Map<String, String> payload = JWTokenImpl.getAllClaimsWithoutValidation(authToken);
            String actor = payload.get(Constant.USER_NAME);
            String role = payload.get(Constant.ROLE);
            IamThreadContext.setActor(actor);
            try {
                Map<String, String> queryParam = StringUtil.parseQueryParams(queryParams);
                String verifierType = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.VERIFIER_TYPE.getKey()),
                         in.fortytwo42.adapter.enums.QueryParam.VERIFIER_TYPE);
                String attributeName = (String) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME.getKey()),
                         in.fortytwo42.adapter.enums.QueryParam.ATTRIBUTE_NAME);
                Integer page = (Integer) StringUtil.parseQueryValue(queryParam.get( in.fortytwo42.adapter.enums.QueryParam.PAGE.getKey()),  in.fortytwo42.adapter.enums.QueryParam.PAGE);
                page = (page == null || page < 1) ? 1 : page;
                PaginatedTO<ThirdPartyVerifierTO> verifiers = attributeFacade.getVerifiers(role, verifierType, attributeName, page);
                response = verifiers;
                responseStatus = Response.Status.OK;
            }catch (AuthException e) {
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                ErrorTO errorTO = new ErrorTO(e.getErrorCode(), errorConstant.getHUMANIZED_GET_VERIFIERS(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                ErrorTO errorTO = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getHUMANIZED_GET_VERIFIERS(), e.getMessage());
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                response = errorTO;
            }
            logger.log(Level.DEBUG, ATTRIBUTE_RESOURCE_LOG + " getVerifiers : end"); 
            asyncResponse.resume(Response.status(responseStatus).entity(response).build());
        });
    }
}
