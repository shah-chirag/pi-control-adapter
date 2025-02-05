
package in.fortytwo42.adapter.service;

import java.util.*;

import in.fortytwo42.adapter.util.*;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;

import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import in.fortytwo42.tos.transferobj.TemplateDetailsTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AccountCustomStateMachineWETO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.AttributeTO;
import in.fortytwo42.adapter.transferobj.ContactWeTO;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.PolicyWeTO;
import in.fortytwo42.adapter.transferobj.StateMachineWorkFlowWETO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.adapter.transferobj.UserBindingTO;
import in.fortytwo42.adapter.util.AuditLogConstant;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.RequestNotFoundException;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.RequestSubType;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ConfigTO;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;
import in.fortytwo42.tos.transferobj.MapperTO;
import in.fortytwo42.tos.transferobj.QuestionAnswerTO;
import in.fortytwo42.tos.transferobj.RequestTO;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;
import in.fortytwo42.tos.transferobj.UserGroupTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class RequestServiceImpl implements RequestServiceIntf {

    private RequestDaoIntf requestDao = DaoFactory.getRequestDao();
    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();
    private UserDaoIntf userDao=DaoFactory.getUserDao();

    private static Logger logger= LogManager.getLogger(RequestServiceImpl.class);
    ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private static final String REQUEST_SERVICE_IMPL_LOG = "<<<<< RequestServiceImpl";

    private AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private static final class InstanceHolder {
        private static final RequestServiceImpl INSTANCE = new RequestServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static RequestServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public AttributeTO createAttributeVerificationRequest(Session session, AttributeTO verifyAttributeTO, String actor, Long id) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(verifyAttributeTO));
        request.setRequestorComments(verifyAttributeTO.getComments());
        request.setRequestType(RequestType.ATTRIBUTE_VERIFICATION);
        request.setRequestSubType(RequestSubType.ATTRIBUTE_VERIFICATION);
        try {
            request.setRequestor(userDao.getActiveUserById(session,id));
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        verifyAttributeTO.setStatus(Constant.SUCCESS_STATUS);
        verifyAttributeTO.setId(createdRequest.getId());
        return verifyAttributeTO;
    }

    @Override
    public AttributeTO createAttributeAdditionRequest(Session session, AttributeTO addAttributeTO, String actor, Long id) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(addAttributeTO));
        request.setRequestorComments(addAttributeTO.getComments());
        request.setRequestType(RequestType.ATTRIBUTE_ADDITION);
        request.setRequestSubType(RequestSubType.ATTRIBUTE_ADDITION);
        try {
            request.setRequestor(userDao.getActiveUserById(session,id));
        }
        catch ( NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setMaker(actor);
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        addAttributeTO.setStatus(Constant.SUCCESS_STATUS);
        addAttributeTO.setId(createdRequest.getId());
        AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_ADDITION_REQUEST_SENT + AuditLogConstant.FOR_ATTRIBUTE + addAttributeTO.getAttributeName() + " = " + addAttributeTO.getAttributeValue() + AuditLogConstant.BY + actor, "ENTERPRISE", ActionType.ADD_ATTRIBUTE_TO_ACCOUNT, request.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", request.getRequestor().getAccountId(), null);
        return addAttributeTO;
    }
    @Override
    public AttributeTO createAttributeUpdationRequest(Session session, AttributeTO addAttributeTO, String actor,Long id) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(addAttributeTO));
        request.setRequestorComments(null);
        request.setRequestType(RequestType.ATTRIBUTE_UPDATION);
        request.setRequestSubType(RequestSubType.ATTRIBUTE_UPDATION);
        try {
            request.setRequestor(userDao.getActiveUserById(session,id));
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setMaker(actor);
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        addAttributeTO.setStatus(Constant.SUCCESS_STATUS);
        addAttributeTO.setId(createdRequest.getId());
        return addAttributeTO;
    }
    


    @Override
    public List<Request> getRequests(RequestType requestType, ApprovalStatus approvalStatus) {
        return requestDao.getRequests(requestType, approvalStatus);
    }

    @Override
    public void createRequest(Session session, Request request) {
        requestDao.create(session, request);
    }

    @Override
    public Request getRequestById(Long requestId, RequestType requestType) throws RequestNotFoundException {
        return requestDao.getPendingAttributeRequestById(requestId, requestType);
    }

    @Override
    public PaginatedTO<RequestTO> getPendingAttributeVerificationRequests(int page, int limit) {
        List<Request> requests = requestDao.getAttributeVerificationRequest(page, limit);
        long count = requestDao.getPaginatedCountForAttributeVerification();
        PaginatedTO<RequestTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(new EntityToTOConverter<Request, RequestTO>().convertEntityListToTOList(requests));
        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    @Override
    public void updateRequest(Session session, Request request) throws RequestNotFoundException {
        requestDao.update(session, request);
    }

    @Override
    public PaginatedTO<RequestTO> getPendingAttributeAdditionRequests(int page, int limit) {
        List<Request> requests = requestDao.getAttributeAdditionRequest(page, limit);
        long count = requestDao.getPaginatedCountForAttributeAddition();
        PaginatedTO<RequestTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(new EntityToTOConverter<Request, RequestTO>().convertEntityListToTOList(requests));
        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    @Override
    public Request getPendingRequestById(Long id) throws AuthException {
        try {
            return requestDao.getPendingRequestById(id);
        }
        catch (RequestNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_REQUEST_NOT_FOUND(), errorConstant.getERROR_MESSAGE_REQUEST_NOT_FOUND());
        }
    }

    @Override
    public PaginatedTO<RequestTO> getPendingAttributeRequests(int page, int limit, String requestType, String actionType) {
        /*RequestType pendingRequestType = requestType != null && requestType.equals(Constant.ATTRIBUTE_ADDITION) ? 
        		RequestType.ATTRIBUTE_ADDITION : RequestType.ATTRIBUTE_VERIFICATION;*/
        List<RequestType> requestTypeList = new ArrayList<>();
        boolean isRequestTypeInclusive = false;
        if (requestType != null && !requestType.isEmpty()) {
            List<String> requestTypeListString;
            if (requestType.contains(Constant.NOT_EQUAL_QUERY_PARAM)) {
                isRequestTypeInclusive = false;
                String notParamRemovedRequestType = requestType.replace(Constant.NOT_EQUAL_QUERY_PARAM, Constant._EMPTY);
                requestTypeListString = Arrays.asList(notParamRemovedRequestType.split(Constant._COMMA));
            }
            else {
                isRequestTypeInclusive = true;
                requestTypeListString = Arrays.asList(requestType.split(Constant._COMMA));
            }
            for (Iterator<String> iterator = requestTypeListString.iterator(); iterator.hasNext();) {
                String string = iterator.next();
                requestTypeList.add(RequestType.valueOf(string));
            }
        }

        List<ApprovalStatus> actionTypeList = new ArrayList<>();
        boolean isactionTypeInclusive = false;
        if (actionType != null && !actionType.isEmpty()) {
            List<String> actionTypeListString;
            if (actionType.contains(Constant.NOT_EQUAL_QUERY_PARAM)) {
                isactionTypeInclusive = false;
                String notParamRemovedActionType = actionType.replace(Constant.NOT_EQUAL_QUERY_PARAM, Constant._EMPTY);
                actionTypeListString = Arrays.asList(notParamRemovedActionType.split(Constant._COMMA));
            }
            else {
                isactionTypeInclusive = true;
                actionTypeListString = Arrays.asList(actionType.split(Constant._COMMA));
            }
            for (Iterator<String> iterator = actionTypeListString.iterator(); iterator.hasNext();) {
                String string = iterator.next();
                actionTypeList.add(ApprovalStatus.valueOf(string));
            }
        }

        List<Request> requests = requestDao.getAttributeRequest(page, limit, requestTypeList, isRequestTypeInclusive, actionTypeList, isactionTypeInclusive);
        long count = requestDao.getRequestCountForAttribute(requestTypeList, isRequestTypeInclusive, actionTypeList, isactionTypeInclusive);
        PaginatedTO<RequestTO> paginatedTO = new PaginatedTO<>();
        List<RequestTO> requestTOs = new ArrayList<>();
        List<AttributeStore> attributeStores = null;
        for (Request req : requests) {
            AttributeTO addAttributeTORequest = new Gson().fromJson(req.getRequestJSON(), AttributeTO.class);
            try {
                attributeStores = attributeStoreDao.getAttributesByattributeNameandValue(addAttributeTORequest.getAttributeName(), addAttributeTORequest.getAttributeValue());
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            RequestTO requestTO = req.convertToTO();
            if (attributeStores != null && !attributeStores.isEmpty()) {
                requestTO.setUserName(attributeStores.get(0).getAttributeValue());
            }
            logger.log(Level.DEBUG, "Request Type   "+req.getRequestType());
            if(req.getRequestType().equals(RequestType.USER_ONBOARD)) {
                try {
                    UserTO userTORequest = new Gson().fromJson(req.getRequestJSON(), UserTO.class);
                    userTORequest.setUserCredential(null);
                    if(userTORequest.getQuestionAnswers() !=null) {
                        for(QuestionAnswerTO questionAnswerTO: userTORequest.getQuestionAnswers()) {
                            questionAnswerTO.setAnswer(null);
                        }
                    }
                    requestTO.setRequestJSON(new Gson().toJson(userTORequest));
                    logger.log(Level.DEBUG, "Request Type   Result"+requestTO.getRequestJSON());
                } catch (Exception e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            } else if (req.getRequestType().equals(RequestType.APPLICATION_ONBOARD) || req.getRequestType().equals(RequestType.APPLICATION_EDIT)) {
                try {
                    ApplicationTO applicationTORequest = new Gson().fromJson(req.getRequestJSON(), ApplicationTO.class);
                    applicationTORequest.setApplicationSecret(null);
                    requestTO.setRequestJSON(new Gson().toJson(applicationTORequest));
                } catch (Exception e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
                
            }
            requestTOs.add(requestTO);
        }
        paginatedTO.setList(requestTOs);
        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    @Override
    public List<Request> getPendingPaginatedRequests(int page, int limit, Long toDate, Long fromDate) {
        return requestDao.getPendingPaginatedRequests(page, limit, toDate, fromDate);
    }

    @Override
    public Long getPendingTotalCount(int page, int limit, Long toDate, Long fromDate) {
        return requestDao.getPendingTotalCount(page, limit, toDate, fromDate);
    }

    @Override
    public List<Request> getNonPendingPaginatedRequests(int page, int limit, Long toDate, Long fromDate) {
        return requestDao.getNonPendingPaginatedRequests(page, limit, toDate, fromDate);
    }

    @Override
    public Long getNonPendingTotalCount(int page, int limit, Long toDate, Long fromDate) {
        return requestDao.getNonPendingTotalCount(page, limit, toDate, fromDate);
    }

    @Override
    public List<Request> getPendingPaginatedRequests(int page, int limit, Long toDate, Long fromDate, RequestType requestType) {
        return requestDao.getPendingPaginatedRequests(page, limit, toDate, fromDate, requestType);
    }

    @Override
    public Long getPendingTotalCount(int page, int limit, Long toDate, Long fromDate, RequestType requestType) {
        return requestDao.getPendingTotalCount(page, limit, toDate, fromDate, requestType);
    }

    @Override
    public List<Request> getNonPendingPaginatedRequests(int page, int limit, Long toDate, Long fromDate, RequestType requestType) {
        return requestDao.getNonPendingPaginatedRequests(page, limit, toDate, fromDate, requestType);
    }

    @Override
    public Long getNonPendingTotalCount(int page, int limit, Long toDate, Long fromDate, RequestType requestType) {
        return requestDao.getNonPendingTotalCount(page, limit, toDate, fromDate, requestType);
    }

    @Override
    public PaginatedTO<RequestTO> getPaginatedApproveAndRejectedRequests(int page, int limit, RequestType requestType) {
        List<Request> requests = requestDao.getApprovedAndRejectedAttributeRequest(page, limit, requestType);
        long count = requestDao.getApprovedAndRejectedAttributeRequestCount(requestType);
        PaginatedTO<RequestTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(new EntityToTOConverter<Request, RequestTO>().convertEntityListToTOList(requests));
        paginatedTO.setTotalCount(count);
        return paginatedTO;
    }

    @Override
    public AttributeTO createEditAttributeAdditionRequest(Session session, AttributeTO addAttributeTO, String actor, Long id) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(addAttributeTO));
        request.setRequestorComments(addAttributeTO.getComments());
        request.setRequestType(RequestType.ATTRIBUTE_UPDATION);
        request.setRequestSubType(RequestSubType.ATTRIBUTE_UPDATION);
        try {
            request.setRequestor(userDao.getActiveUserById(session,id));
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        addAttributeTO.setStatus(Constant.SUCCESS_STATUS);
        addAttributeTO.setId(createdRequest.getId());
        return addAttributeTO;
    }

    @Override
    public UserTO createRequestAttributeRequest(Session session, UserTO userTO, String actor, Long id, RequestType requestType) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(userTO));
        request.setMaker(actor);
        request.setRequestorComments(userTO.getComments());
        request.setRequestType(requestType);
        if (requestType.equals(RequestType.ATTRIBUTE_REQUEST)) {
            request.setRequestSubType(RequestSubType.ATTRIBUTE_REQUEST);
        }
        if (requestType.equals(RequestType.ATTRIBUTE_DEMAND)) {
            request.setRequestSubType(RequestSubType.ATTRIBUTE_DEMAND);
        }
        try {
            request.setRequestor(userDao.getActiveUserById(session,id));
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        userTO.setStatus(Constant.SUCCESS_STATUS);
        userTO.setId(createdRequest.getId());
        return userTO;
    }

    @Override
    public ApplicationTO createApplicationOnboardRequest(Session session, ApplicationTO applicationTO, String actor, Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createApplicationOnboardRequest : start");
        Request createdRequest = requestDao.create(session,
                createRequest(applicationTO, actor,id, applicationTO.getComments(), RequestType.APPLICATION_ONBOARD,isSaveRequest, Constant.APPLICATION,
                        applicationTO.getApplicationName() ,
                        errorConstant.getERROR_CODE_APPLICATION_ONBOARD_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_APPLICATION_ONBOARD_ALREADY_PRESENT()));
        applicationTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            applicationTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(AuditLogConstant.APPLICATION_ONBOARD_REQUEST_SENT + AuditLogConstant.BY + createdRequest.getRequestor().getAccountId() + AuditLogConstant.FOR_APPLICATION + applicationTO.getApplicationName(),
                "ENTERPRISE",
                ActionType.ONBOARD,
                createdRequest.getRequestor().getAccountId(),
                IdType.ACCOUNT, "",
                applicationTO.getEnterpriseAccountId(),
                "",
                null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createApplicationOnboardRequest : end");
        return applicationTO;
    }

    @Override
    public ApplicationTO createApplicationEditRequest(Session session, ApplicationTO applicationTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createApplicationEditRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(applicationTO, actor,id,applicationTO.getComments(), RequestType.APPLICATION_EDIT, isSaveRequest,
                Constant.APPLICATION, applicationTO.getApplicationId(), errorConstant.getERROR_CODE_APPLICATION_EDIT_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_APPLICATION_EDIT_ALREADY_PRESENT()));
        applicationTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            applicationTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(applicationTO.getApplicationName()  + "application edit request generated successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", applicationTO.getEnterpriseAccountId(), "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createApplicationEditRequest : end");
        return applicationTO;
    }
    @Override
    public FalloutSyncDataTo createUpdateFalloutSyncData(Session session,FalloutSyncDataTo falloutSyncDataTo,String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUpdateFalloutSyncData : start");
        Request createdRequest = requestDao.create(session, createRequest(falloutSyncDataTo, actor,id,falloutSyncDataTo.getComment(), RequestType.UPDATE_FALLOUT_SYNC_DATA, isSaveRequest,
                Constant.FALLOUT_DATA_SYNC_LAST_LOGIN_TIME, String.valueOf(falloutSyncDataTo.getId()), errorConstant.getERROR_CODE_INVALID_DATA(),
                errorConstant.getERROR_MESSAGE_UPDATE_FALLOUTSYNCDATA_ALREADY_PRESENT()));
        falloutSyncDataTo.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            falloutSyncDataTo.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUpdateFalloutSyncData : end");
        return falloutSyncDataTo;
    }

    @Override
    public ConfigTO createAddConfigRequest(Session session, ConfigTO configTO, String actor, Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAddConfigRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(configTO, actor,id, configTO.getComments(),
                RequestType.ADD_CONFIG, isSaveRequest,
                Constant.CONFIG_KEY, String.valueOf(configTO.getKey()), errorConstant.getERROR_CODE_INVALID_DATA(),
                errorConstant.getERROR_MESSAGE_ADD_CONFIG_ALREADY_PRESENT()));
        configTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            configTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAddConfigRequest : end");
        return configTO;
    }

    @Override
    public ConfigTO createUpdateConfigRequest(Session session, ConfigTO configTO, String actor, Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUpdateConfigRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(configTO, actor,id, configTO.getComments(), RequestType.UPDATE_CONFIG, isSaveRequest,
                Constant.CONFIG_KEY, String.valueOf(configTO.getKey()), errorConstant.getERROR_CODE_INVALID_DATA(),
                errorConstant.getERROR_MESSAGE_UPDATE_CONFIG_ALREADY_PRESENT()));
        configTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            configTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUpdateConfigRequest : end");
        return configTO;
    }

    public ConfigTO createDeleteConfigRequest(Session session, ConfigTO configTO, String actor, Long id,
                                         boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createDeleteConfigRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(configTO, actor,id, configTO.getComments(), RequestType.DELETE_CONFIG, isSaveRequest,
                Constant.CONFIG_KEY, String.valueOf(configTO.getKey()), errorConstant.getERROR_CODE_INVALID_DATA(),
                errorConstant.getERROR_MESSAGE_UPDATE_CONFIG_ALREADY_PRESENT()));
        configTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            configTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createDeleteConfigRequest : end");
        return configTO;
    }

    @Override
    public UserTO createUserOnboardRequest(Session session, UserTO userTO,boolean isEncrypted, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserOnboardRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userTO, actor,id, userTO.getComments(), RequestType.USER_ONBOARD, isSaveRequest,
                Constant.USER_ID, getUsername(userTO.getAttributes(),isEncrypted), errorConstant.getERROR_CODE_USER_ONBOARD_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_ONBOARD_ALREADY_PRESENT()));
        userTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserOnboardRequest : end");
        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_CREATION_REQUEST_SENT + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + getUsername(userTO.getAttributes(),isEncrypted), "ENTERPRISE", ActionType.ONBOARD, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
        return userTO;
    }

    @Override
    public UserTO createUserEditRequest(Session session, UserTO userTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserEditRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userTO, actor,id, userTO.getComments(),RequestType.USER_EDIT, isSaveRequest,
                Constant.USER_ID, userTO.getUserId().toString(), errorConstant.getERROR_CODE_USER_UPDATE_PENDING(), errorConstant.getERROR_MESSAGE_USER_UPDATE_PENDING()));
        userTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserEditRequest : end");
        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_EDIT_REQUEST_SENT + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.AUTHENTICATION, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", userTO.getAccountId(), null);
        return userTO;

    }
    @Override
    public UserTO createUserRoleEditRequest(Session session, UserTO userTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserRoleEditRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userTO, actor,id, userTO.getComments(),
                RequestType.UPDATE_USER_ROLE, isSaveRequest,
                Constant.USER_ID, userTO.getUserId().toString(), errorConstant.getERROR_CODE_USER_UPDATE_PENDING(), errorConstant.getERROR_MESSAGE_USER_UPDATE_PENDING()));
        userTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserRoleEditRequest : end");
        return userTO;

    }

    @Override
    public UserTO createUserEditLastLogInTimeRequest(Session session, UserTO userTO, String actor,Long id,boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserEditLastLogInTimeRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userTO, actor,id, userTO.getComments(),RequestType.UPDATE_USER_LAST_LOGIN_TIME, isSaveRequest,
                Constant.USER_ID, userTO.getUserId().toString(), errorConstant.getERROR_CODE_USER_UPDATE_PENDING(), errorConstant.getERROR_MESSAGE_USER_UPDATE_PENDING()));
        userTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserEditLastLogInTimeRequest : end");
        return userTO;

    }

    public UserTO createUserEditAttributesRequest(Session session, UserTO userTO, String actor, Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserEditAttributesRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userTO, actor,id, userTO.getComments(),RequestType.USER_EDIT_ATTRIBUTE, isSaveRequest,
                Constant.USER_ID, userTO.getUserId().toString(), errorConstant.getERROR_CODE_USER_UPDATE_PENDING(), errorConstant.getERROR_MESSAGE_USER_UPDATE_PENDING()));
        userTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserEditAttributesRequest : end");
        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_DEFAULT_ATTRIBUTE_REQUEST_SENT + AuditLogConstant.FOR_ATTRIBUTE + userTO.getAttributes().get(0).getAttributeName() + " = " + userTO.getAttributes().get(0).getAttributeValue() + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getUsername(), "ENTERPRISE", ActionType.AUTHENTICATION, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
        return userTO;
    }

    @Override
    public UserBindingTO createUserServiceUnbindRequest(Session session, UserBindingTO userBindingTO, String actor,Long id,boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserServiceUnbindRequest : start");
        Request request = createRequest(userBindingTO, actor,id,userBindingTO.getComments(), RequestType.UNBIND_SERVICE, isSaveRequest,
                Constant.USER_IDENTIFIER, userBindingTO.getId().toString() + userBindingTO.getApplication().getApplicationId(),
                errorConstant.getERROR_CODE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_SERVICE_UNBIND_REQUEST_ALREADY_PRESENT());
        Request createdRequest = requestDao.create(session, request);
        userBindingTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userBindingTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserServiceUnbindRequest : end");
        AuditLogUtil.sendAuditLog( AuditLogConstant.APPLICATION_SERVICE_UNBIND_REQUEST_SENT + AuditLogConstant.BY + actor + AuditLogConstant.FOR_APPLICATION + userBindingTO.getApplication().getApplicationName() + AuditLogConstant.FOR_USER + userBindingTO.getUsername(), "ENTERPRISE", ActionType.AUTHENTICATION, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
        return userBindingTO;
    }

    @Override
    public UserBindingTO createUserServiceBindRequest(Session session, UserBindingTO userBindingTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserServiceBindRequest : start");
        Request request = createRequest(userBindingTO, actor,id, userBindingTO.getComments(), RequestType.BIND_SERVICE,isSaveRequest,
                Constant.USER_IDENTIFIER, userBindingTO.getId().toString() + userBindingTO.getApplication().getApplicationId(),
                errorConstant.getERROR_CODE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BIND_REQUEST_ALREADY_PRESENT());
        if (!isSaveRequest) {
            request.setApprovalStatus(ApprovalStatus.USER_APPROVAL_PENDING);
        }
        Request createdRequest = requestDao.create(session, request);
        userBindingTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userBindingTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserServiceBindRequest : end");
        AuditLogUtil.sendAuditLog( AuditLogConstant.APPLICATION_SERVICE_BIND_REQUEST_SENT + AuditLogConstant.BY + actor + AuditLogConstant.FOR_APPLICATION + userBindingTO.getApplication().getApplicationName() + AuditLogConstant.FOR_USER + userBindingTO.getUsername(), "ENTERPRISE", ActionType.AUTHENTICATION, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
        return userBindingTO;
    }

    @Override
    public PolicyWeTO createPolicyOnboardRequest(Session session, PolicyWeTO policyWE, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createPolicyOnboardRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(policyWE, actor,id,policyWE.getComments(),RequestType.POLICY_ONBOARD, isSaveRequest,
                Constant.POLICY_UNIQUE_ID, policyWE.getPolicy().getPolicyVersion()+policyWE.getPolicy().getAccountType()+policyWE.getPolicy().getPolicyType(), errorConstant.getERROR_CODE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_POLICY_ONBOARD_REQUEST_ALREADY_PRESENT()));
        policyWE.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            policyWE.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(policyWE.getPolicy().getPolicyName() + " policy onboard request generated ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createPolicyOnboardRequest : end");
        return policyWE;
    }

    @Override
    public PolicyWeTO createPolicyEditRequest(Session session, PolicyWeTO policyWE, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createPolicyEditRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(policyWE, actor,id, policyWE.getComments(),RequestType.POLICY_EDIT, isSaveRequest,
                Constant.POLICY_UNIQUE_ID, policyWE.getPolicy().getPolicyVersion()+policyWE.getPolicy().getAccountType()+policyWE.getPolicy().getPolicyType(), errorConstant.getERROR_CODE_POLICY_EDIT_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_POLICY_EDIT_REQUEST_ALREADY_PRESENT()));
        policyWE.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            policyWE.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(policyWE.getPolicy().getPolicyName() + " policy edit request generated successfully", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createPolicyEditRequest : end");
        return policyWE;
    }

    @Override
    public ContactWeTO createContactOnboardRequest(Session session, ContactWeTO contactWE, String actor,Long id,boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createContactOnboardRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(contactWE, actor,id, contactWE.getComments(),RequestType.CONTACT_ONBOARD, isSaveRequest,
                Constant.CONTACT_UNIQUE_ID, contactWE.getContactTO().getAccountId(), errorConstant.getERROR_CODE_CONTACT_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_CONTACT_ONBOARD_REQUEST_ALREADY_PRESENT()));
        contactWE.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            contactWE.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createContactOnboardRequest : end");
        return contactWE;
    }

    @Override
    public ContactWeTO createContactEditRequest(Session session, ContactWeTO contactWE, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createContactEditRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(contactWE, actor,id, contactWE.getComments(),RequestType.CONTACT_EDIT, isSaveRequest,
                Constant.CONTACT_UNIQUE_ID, contactWE.getContactTO().getAccountId(), errorConstant.getERROR_CODE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_CONTACT_EDIT_REQUEST_ALREADY_PRESENT()));
        contactWE.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            contactWE.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createContactEditRequest : end");
        return contactWE;
    }
    
    @Override
    public List<Request> getPaginatedRequests(RequestType requestType, int page, int limit, String searchText, Long fromDate, Long toDate) {
        return requestDao.getRequests(requestType, page, limit, searchText, fromDate, toDate);
    }

    @Override
    public List<Request> getPendingPaginatedRequests(RequestType requestType, int page, int limit, String searchText, Long fromDate, Long toDate) {
        return requestDao.getPendingRequests(requestType, page, limit, searchText, fromDate, toDate);
    }

    @Override
    public ApplicationTO createDeleteSRAApplicationSettingRequest(Session session, ApplicationTO applicationTO,String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createDeleteSRAApplicationSettingRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(applicationTO, actor,id, applicationTO.getComments(), RequestType.SRA_APPLICATION_SETTING_DELETE, isSaveRequest,
                Constant.APPLICATION, applicationTO.getApplicationAccountId(),
                errorConstant.getERROR_CODE_APPLICATION_EDIT_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_APPLICATION_EDIT_ALREADY_PRESENT()));
        applicationTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            applicationTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createDeleteSRAApplicationSettingRequest : end");
        return applicationTO;
    }

    @Override
    public DeviceTO createEditDeviceRequest(Session session, DeviceTO deviceTO, String actor, Long id, RequestType requestType, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createEditDeviceRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(deviceTO, actor,id, deviceTO.getComments(), requestType, isSaveRequest,
                Constant.DEVICE_UDID, deviceTO.getDeviceUDID(), errorConstant.getERROR_CODE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_DEVICE_EDIT_REQUEST_ALREADY_PRESENT()));
        deviceTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            deviceTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(deviceTO.getDeviceName()+ " edit device request generated successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createEditDeviceRequest : end");
        return deviceTO;
    }

    @Override
    public TokenTO createEditTokenRequest(Session session, TokenTO tokenTO, String actor, Long id, RequestType requestType, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createEditTokenRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(tokenTO, actor,id,tokenTO.getComments(), requestType, isSaveRequest,
                Constant.TOKEN_UDID, tokenTO.getTokenUDID(), errorConstant.getERROR_CODE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_TOKEN_EDIT_REQUEST_ALREADY_PRESENT()));
        tokenTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            tokenTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(tokenTO.getTokenName()+ " edit token request generated successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createEditTokenRequest : end");
        return tokenTO;
    }

    @Override
    public SRAGatewaySettingTO createSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createSRAGatewaySetting : start");
        Request createdRequest = requestDao.create(session, createRequest(sraGatewaySettingTO, actor,id, sraGatewaySettingTO.getComments(), RequestType.SRA_GATEWAY_SETTING_ONBOARD, isSaveRequest,
                Constant.GATEWAY_NAME, sraGatewaySettingTO.getName(), errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT()));
        sraGatewaySettingTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            sraGatewaySettingTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + " add SRAGateway settings  request generated ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createSRAGatewaySetting : end");
        return sraGatewaySettingTO;
    }

    @Override
    public SRAGatewaySettingTO editSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO,String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " editSRAGatewaySetting : start");
        Request createdRequest = requestDao.create(session, createRequest(sraGatewaySettingTO, actor,id,sraGatewaySettingTO.getComments(), RequestType.SRA_GATEWAY_SETTING_UPDATE, isSaveRequest,
                Constant.GATEWAY_NAME, sraGatewaySettingTO.getName(), errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_UPDATE_REQUEST_ALREADY_PRESENT()));
        sraGatewaySettingTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            sraGatewaySettingTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + " edit SRAGateway setting request generated successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " editSRAGatewaySetting : end");
        return sraGatewaySettingTO;
    }

    @Override
    public SRAGatewaySettingTO deleteSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " deleteSRAGatewaySetting : start");
        Request createdRequest = requestDao.create(session, createRequest(sraGatewaySettingTO, actor,id,sraGatewaySettingTO.getComments(), RequestType.SRA_GATEWAY_SETTING_DELETION, isSaveRequest,
                Constant.GATEWAY_ID, sraGatewaySettingTO.getId().toString(), errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_DELETE_REQUEST_ALREADY_PRESENT()));
        sraGatewaySettingTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            sraGatewaySettingTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + " delete SRAGateway setting request generated successfully ", "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " deleteSRAGatewaySetting : end");
        return sraGatewaySettingTO;
    }

    private Request createRequest(Object src, String actor,Long id, String comments, RequestType requestType,
                                  boolean isSaveRequest, String uniqueKey, String uniqueValue,
            Long errorCode, String errorMessage) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createRequest : start"+ uniqueKey + " "+uniqueValue);
        Request presentRequest = null;
        try {
            presentRequest = requestDao.getRequestByUniqueKeyAndValue(uniqueKey, uniqueValue, ApprovalStatus.CHECKER_APPROVAL_PENDING, ApprovalStatus.USER_APPROVAL_PENDING, requestType);
            //done by me
           List<Request> requestPendingBeforeChecker= requestDao.getRequests(requestType,ApprovalStatus.CHECKER_APPROVAL_PENDING);
           for(Request request :requestPendingBeforeChecker) {
                 String applicationName= request.getUniqueFieldValue();
                           if (applicationName.equalsIgnoreCase(uniqueValue)) {
                   throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_APPLICATION_NAME(), errorConstant.getERROR_MESSAGE_INVALID_APPLICATION_NAME());

               }
           }
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        if (presentRequest == null) {
            logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createRequest : start");
            Request request = new Request();
            request.setRequestJSON(new Gson().toJson(src));
            request.setRequestorComments(comments);
            request.setRequestType(requestType);
            request.setUniqueFieldkey(uniqueKey);
            request.setUniqueFieldValue(uniqueValue);
            try {
                request.setRequestor(userDao.getActiveUserById(id));
                request.setMaker(actor);
            }catch (UserNotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            try {
                //request.setRequestor(attributeStoreDao.getUserByAttributeValue(actor));
                if (!isSaveRequest) {
                    request.setApprovalStatus(ApprovalStatus.APPROVED_BY_CHECKER);
                    request.setApprover(userDao.getActiveUserById(id));
                    request.setChecker(actor);
                    request.setApproverComments(comments);
                }
                else {
                    request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
                }
            }catch (UserNotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }finally {
                logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createRequest : end");
            }
            return request;

        }
        else {
            throw new AuthException(null, errorCode,
                    errorMessage);
        }
    }

    private String getUsername(List<AttributeDataTO> attributeDataTOs,boolean isEncrypted) throws AuthException {
          Config config = Config.getInstance();
        for (AttributeDataTO attributeDataTO : attributeDataTOs) {
            if (Constant.USER_ID.equals(attributeDataTO.getAttributeName())) {
                String decryptedAttributeValue = attributeDataTO.getAttributeValue();
                if (isEncrypted) {
                    try {
                        decryptedAttributeValue = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), attributeDataTO.getAttributeValue());
                        String isAttributesInUpperCase = config.getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
                        Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
                        if(Boolean.TRUE.equals(isAttributeUpperCase)) {
                            decryptedAttributeValue = decryptedAttributeValue.toUpperCase();
                        }
                    }
                    catch (Exception e) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_PASSWORD());
                    }
                }
                return decryptedAttributeValue;
            }
        }
        return "";
    }

    @Override
    public AttributeMetadataTO createAttributeMetadataAdditionRequest(Session session, AttributeMetadataTO addAttributeMetadataTO,String actor,Long id,boolean isSaveRequest) throws AuthException {
        Request createdRequest = requestDao.create(session, 
                createRequest(addAttributeMetadataTO, 
                        actor,id,
                        addAttributeMetadataTO.getComments(), 
                        RequestType.ATTRIBUTE_MASTER_ADDITION, 
                        isSaveRequest,
                Constant.ATTRIBUTE_NAME,
                addAttributeMetadataTO.getAttributeName(), 
                errorConstant.getERROR_CODE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT()));
        addAttributeMetadataTO.setStatus(Constant.SUCCESS_STATUS);
        addAttributeMetadataTO.setId(createdRequest.getId());
        AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_CREATION_REQUEST_SENT + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + addAttributeMetadataTO.getAttributeName(), "ENTERPRISE", ActionType.AUTHENTICATION, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", null, null);
        return addAttributeMetadataTO;
    }
    
    @Override
    public AttributeMetadataTO createAttributeMetadataUpdateRequest(Session session,AttributeMetadataTO addAttributeMetadataTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        Request createdRequest = requestDao.create(session, 
                createRequest(addAttributeMetadataTO, 
                        actor,id,
                        addAttributeMetadataTO.getComments(), 
                        RequestType.ATTRIBUTE_MASTER_UPDATION, 
                        isSaveRequest,
                Constant.ATTRIBUTE_NAME,
                addAttributeMetadataTO.getAttributeName(), 
                errorConstant.getERROR_CODE_ATTRIBUTE_UPDATION_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_ATTRIBUTE_UPDATION_ALREADY_PRESENT()));
        addAttributeMetadataTO.setStatus(Constant.SUCCESS_STATUS);
        addAttributeMetadataTO.setId(createdRequest.getId());
        AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_UPDATE_REQUEST_SENT + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + addAttributeMetadataTO.getAttributeName(), "ENTERPRISE", ActionType.AUTHENTICATION, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", null, null);
        return addAttributeMetadataTO;
    }
    
    @Override
    public AttributeMetadataTO createAttributeMetadataDeleteRequest(Session session,AttributeMetadataTO addAttributeMetadataTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        Request createdRequest = requestDao.create(session, 
                createRequest(addAttributeMetadataTO, 
                        actor,id,
                        addAttributeMetadataTO.getComments(), 
                        RequestType.ATTRIBUTE_MASTER_DELETION, 
                        isSaveRequest,
                Constant.ATTRIBUTE_NAME,
                addAttributeMetadataTO.getAttributeName(), 
                errorConstant.getERROR_CODE_ATTRIBUTE_DELETION_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_ATTRIBUTE_DELETION_ALREADY_PRESENT()));
        addAttributeMetadataTO.setStatus(Constant.SUCCESS_STATUS);
        addAttributeMetadataTO.setId(createdRequest.getId());
        AuditLogUtil.sendAuditLog(AuditLogConstant.ATTRIBUTE_MASTER_DELETE_REQUEST_SENT + AuditLogConstant.BY + actor + AuditLogConstant.FOR_ATTRIBUTE_MASTER + addAttributeMetadataTO.getAttributeName(), "ENTERPRISE", ActionType.AUTHENTICATION, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", null, null);
        return addAttributeMetadataTO;
    }
    
    @Override
    public UserGroupTO createUserGroupCreateRequest(Session session, UserGroupTO userGroupTO, String actor,Long id,boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserGroupCreateRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userGroupTO, actor,id,userGroupTO.getComments(), RequestType.USER_GROUP_CREATE, isSaveRequest,
                Constant.USER_GROUP_UNIQUE_ID, userGroupTO.getGroupname(), errorConstant.getERROR_CODE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_USER_GROUP_CREATE_REQUEST_ALREADY_PRESENT()));
        userGroupTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userGroupTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + " create user group request generated successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserGroupCreateRequest : end");
        return userGroupTO;
    }

    @Override
    public UserGroupTO createUserGroupUpdateRequest(Session session, UserGroupTO userGroupTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserGroupUpdateRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userGroupTO, actor,id, userGroupTO.getComments(), RequestType.USER_GROUP_UPDATE, isSaveRequest,
                Constant.USER_GROUP_UNIQUE_ID, userGroupTO.getGroupname(), errorConstant.getERROR_CODE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_USER_GROUP_UPDATE_REQUEST_ALREADY_PRESENT()));
        userGroupTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userGroupTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "update user group request generated successfully", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserGroupUpdateRequest : end");
        return userGroupTO;
    }

    @Override
    public UserGroupTO createUserGroupMappingRequest(Session session, UserGroupTO userGroupTO, String actor,Long id,boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserGroupMappingRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userGroupTO, actor,id, userGroupTO.getComments(), RequestType.USER_GROUP_MAPPING, isSaveRequest,
                Constant.USER_GROUP_UNIQUE_ID, userGroupTO.getGroupname(), errorConstant.getERROR_CODE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_USER_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT()));
        userGroupTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userGroupTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "User to user-group mapping request created  successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserGroupMappingRequest : end");
        return userGroupTO;
    }

    @Override
    public UserGroupTO createApplicationUserGroupMappingRequest(Session session, UserGroupTO userGroupTO,String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserGroupMappingRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userGroupTO, actor,id, userGroupTO.getComments(), RequestType.USER_GROUP_APPLICATION_MAPPING, isSaveRequest,
                Constant.USER_GROUP_UNIQUE_ID, userGroupTO.getGroupname(), errorConstant.getERROR_CODE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_APPLICATION_USER_GROUP_MAPPING_REQUEST_ALREADY_PRESENT()));
        userGroupTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userGroupTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "application to usergroup mapping  request generated successfully ", "ENTERPRISE", ActionType.BIND_USER_APPLCATION, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createUserGroupMappingRequest : end");
        return userGroupTO;
    }

    @Override
    public UserGroupTO removeUserGroupRequest(Session session, UserGroupTO userGroupTO, String actor,Long id,boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " removeUserGroupRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userGroupTO, actor,id, userGroupTO.getComments(), RequestType.USER_GROUP_DELETE, isSaveRequest,
                Constant.USER_GROUP_UNIQUE_ID, userGroupTO.getGroupname(), errorConstant.getERROR_CODE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_USER_GROUP_DELETE_REQUEST_ALREADY_PRESENT()));
        userGroupTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userGroupTO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(userGroupTO.getGroupname()  + "User-group delete request generated successfully ", "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " removeUserGroupRequest : end");
        return userGroupTO;
    }

@Override
    public IdentityProviderTO createIdentityProviderCreateRequest(Session session,IdentityProviderTO identityProviderTO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createIdentityProviderCreateRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(identityProviderTO, actor,id, identityProviderTO.getComments(), RequestType.IDENTITY_PROVIDER_CREATE, isSaveRequest,
                Constant.IDENTITY_PROVIDER_UNIQUE_ID, identityProviderTO.getName(), errorConstant.getERROR_CODE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_IDENTITY_PROVIDER_CREATE_REQUEST_ALREADY_PRESENT()));
        identityProviderTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            identityProviderTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createIdentityProviderCreateRequest : end");
        return identityProviderTO;
    }

@Override
public MapperTO createMapperCreateRequest(Session session, MapperTO mapperTO, String actor,Long id,boolean isSaveRequest) throws AuthException {
    logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createMapperCreateRequest : start");
    Request createdRequest = requestDao.create(session, createRequest(mapperTO, actor,id, mapperTO.getComments(),RequestType.MAPPER_CREATE, isSaveRequest,
            Constant.KEY, mapperTO.getKey(), errorConstant.getERROR_CODE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT(),
            errorConstant.getERROR_MESSAGE_MAPPER_CREATE_REQUEST_ALREADY_PRESENT()));
    mapperTO.setStatus(Constant.SUCCESS_STATUS);
    if (isSaveRequest) {
        mapperTO.setId(createdRequest.getId());
    }
    logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createMapperCreateRequest : end");
    return mapperTO;
}

    @Override
    public StateMachineWorkFlowWETO createStateMachineWorkFlowOnboardRequest(Session session,StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createStateMachineWorkFlowOnboardRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(stateMachineWorkFlowWETO, actor,id, stateMachineWorkFlowWETO.getComments(), RequestType.STATE_MACHINE_WORKFLOW_ONBOARD, isSaveRequest,
                Constant.STATE_MACHINE_WORKFLOW_UNIQUE_ID,  stateMachineWorkFlowWETO.getStateMachineWorkFlow().getAttemptType().getId() + stateMachineWorkFlowWETO.getStateMachineWorkFlow().getInitialAccountState() + stateMachineWorkFlowWETO.getStateMachineWorkFlow().isActive(), errorConstant.getERROR_CODE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_STATE_MACHINE_WORKFLOW_ONBOARD_REQUEST_ALREADY_PRESENT()));
        stateMachineWorkFlowWETO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            stateMachineWorkFlowWETO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(stateMachineWorkFlowWETO.getStateMachineWorkFlow().getAttemptType()+ "state machine workflow onboard request generated successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);

        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createStateMachineWorkFlowOnboardRequest : end");
        return stateMachineWorkFlowWETO;
    }

    @Override
    public StateMachineWorkFlowWETO createStateMachineWorkFlowUpdateRequest(Session session,StateMachineWorkFlowWETO stateMachineWorkFlowWETO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createStateMachineWorkFlowUpdateRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(stateMachineWorkFlowWETO, actor,id, stateMachineWorkFlowWETO.getComments(), RequestType.STATE_MACHINE_WORKFLOW_UPDATE, isSaveRequest,
                Constant.STATE_MACHINE_WORKFLOW_UNIQUE_ID, stateMachineWorkFlowWETO.getStateMachineWorkFlow().getId(),
                errorConstant.getERROR_CODE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_STATE_MACHINE_WORKFLOW_UPDATE_REQUEST_ALREADY_PRESENT()));
        stateMachineWorkFlowWETO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            stateMachineWorkFlowWETO.setId(createdRequest.getId());
        }
        AuditLogUtil.sendAuditLog(stateMachineWorkFlowWETO.getStateMachineWorkFlow().getAttemptType()+ "state machine  workflow edit request generated successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createStateMachineWorkFlowUpdateRequest : end");
        return stateMachineWorkFlowWETO;
    }

    @Override
    public AccountCustomStateMachineWETO createAccountCustomStateMachineOnboardRequest(Session session,AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAccountCustomStateMachineOnboardRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(accountCustomStateMachineWETO, actor,id,accountCustomStateMachineWETO.getComments(), RequestType.ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD, isSaveRequest,
                Constant.ACCOUNT_CUSTOM_STATE_MACHINE_UNIQUE_ID,  accountCustomStateMachineWETO.getAccountCustomStateMachine().getStateMachineWorkFlow().getId() + accountCustomStateMachineWETO.getAccountCustomStateMachine().getAccount().getId(),
                errorConstant.getERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_ONBOARD_REQUEST_ALREADY_PRESENT()));
        accountCustomStateMachineWETO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            accountCustomStateMachineWETO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAccountCustomStateMachineOnboardRequest : end");
        return accountCustomStateMachineWETO;
    }

    @Override
    public AccountCustomStateMachineWETO createAccountCustomStateMachineUpdateRequest(Session session,AccountCustomStateMachineWETO accountCustomStateMachineWETO, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAccountCustomStateMachineUpdateRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(accountCustomStateMachineWETO, actor,id, accountCustomStateMachineWETO.getComments(), RequestType.ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE,isSaveRequest,
                Constant.ACCOUNT_CUSTOM_STATE_MACHINE_UNIQUE_ID, accountCustomStateMachineWETO.getAccountCustomStateMachine().getStateMachineWorkFlow().getId() + accountCustomStateMachineWETO.getAccountCustomStateMachine().getAccount().getId(),
                errorConstant.getERROR_CODE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT(),
                errorConstant.getERROR_MESSAGE_ACCOUNT_CUSTOM_STATE_MACHINE_UPDATE_REQUEST_ALREADY_PRESENT()));
        accountCustomStateMachineWETO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            accountCustomStateMachineWETO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAccountCustomStateMachineUpdateRequest : end");
        return accountCustomStateMachineWETO;
    }

    @Override
    public UserTO createDisableUserRequest(Session session, UserTO userTO, boolean isEncrypted, String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createDisableUserRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(userTO, actor,id, userTO.getComments(),RequestType.DISABLE_USER, isSaveRequest,
                Constant.USER_ID, userTO.getUserId().toString(), errorConstant.getERROR_CODE_DISABLE_USER_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_DISABLE_USER_ALREADY_PRESENT()));
        userTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            userTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createDisableUserRequest : end");
        AuditLogUtil.sendAuditLog(AuditLogConstant.USER_DISABLE_REQUEST_SENT + AuditLogConstant.BY + actor + AuditLogConstant.FOR_USER + userTO.getUserId().toString(), "ENTERPRISE", ActionType.ONBOARD, createdRequest.getRequestor().getAccountId(), IdType.ACCOUNT, "", "", "", null);
        return userTO;
    }

    @Override
    public FalloutConfigTO createFalloutConfigEditRequest(Session session, FalloutConfigTO falloutConfigTO,String actor,Long id, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createFalloutConfigEditRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(falloutConfigTO, actor,id, falloutConfigTO.getComments(), RequestType.UPDATE_FALLOUT_CONFIG, isSaveRequest,
                Constant.ID, falloutConfigTO.getId().toString(), errorConstant.getERROR_CODE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_EDIT_FALLOUT_CONFIG_ALREADY_PRESENT()));
        falloutConfigTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            falloutConfigTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createFalloutConfigEditRequest : start");
        return falloutConfigTO;
    }

    @Override
    public LdapDetailsTO createAddLdapDetailsRequest(Long id,Session session, LdapDetailsTO ldapDetailsTO, String actor, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAddLdapDetailsRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(ldapDetailsTO, actor, id,ldapDetailsTO.getComments(), RequestType.ADD_LDAP_DETAILS, isSaveRequest,
                Constant.ID, ldapDetailsTO.getUserDomainName(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_LDAP_DETAILS_ADD_REQUEST_ALREADY_PRESENT()));
        ldapDetailsTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            ldapDetailsTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAddLdapDetailsRequest : start");
        return ldapDetailsTO;
    }
    @Override
    public LdapDetailsTO createEditLdapDetailsRequest(Long id,Session session, LdapDetailsTO ldapDetailsTO, String actor, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAddLdapDetailsRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(ldapDetailsTO, actor,id, ldapDetailsTO.getComments(), RequestType.EDIT_LDAP_DETAILS, isSaveRequest,
                Constant.ID, ldapDetailsTO.getId().toString(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_LDAP_DETAILS_EDIT_REQUEST_ALREADY_PRESENT()));
        ldapDetailsTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            ldapDetailsTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createAddLdapDetailsRequest : start");
        return ldapDetailsTO;
    }

    @Override
    public TemplateDetailsTO createOnboardTemplateDetailsRequest(Long id, Session session, TemplateDetailsTO templateDetailsTO, String actor, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createOnboardTemplateDetailsRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(templateDetailsTO, actor, id,templateDetailsTO.getComments(), RequestType.ONBOARD_TEMP_DETAILS, isSaveRequest,
                Constant.ID, templateDetailsTO.getTemplateId(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_CREATE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT()));
        templateDetailsTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            templateDetailsTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createOnboardTemplateDetailsRequest : start");
        return templateDetailsTO;
    }

    @Override
    public TemplateDetailsTO createEditTemplateDetailsRequest(Long id, Session session, TemplateDetailsTO templateDetailsTO, String actor, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createEditTemplateDetailsRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(templateDetailsTO, actor, id,templateDetailsTO.getComments(), RequestType.EDIT_TEMP_DETAILS, isSaveRequest,
                Constant.ID, templateDetailsTO.getTemplateId()+templateDetailsTO.getTemplateType(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_EDIT_TEMP_DETAILS_REQUEST_ALREADY_PRESENT()));
        templateDetailsTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            templateDetailsTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createEditTemplateDetailsRequest : start");
        return templateDetailsTO;
    }

    @Override
    public TemplateDetailsTO createDeleteTemplateDetailsRequest(Long id, Session session, TemplateDetailsTO templateDetailsTO, String actor, boolean isSaveRequest) throws AuthException {
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createDeleteTemplateDetailsRequest : start");
        Request createdRequest = requestDao.create(session, createRequest(templateDetailsTO, actor, id,templateDetailsTO.getComments(), RequestType.DELETE_TEMP_DETAILS, isSaveRequest,
                Constant.ID, templateDetailsTO.getTemplateId()+templateDetailsTO.getTemplateType(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_DELETE_TEMP_DETAILS_REQUEST_ALREADY_PRESENT()));
        templateDetailsTO.setStatus(Constant.SUCCESS_STATUS);
        if (isSaveRequest) {
            templateDetailsTO.setId(createdRequest.getId());
        }
        logger.log(Level.DEBUG, REQUEST_SERVICE_IMPL_LOG + " createDeleteTemplateDetailsRequest : start");
        return templateDetailsTO;
    }


}
