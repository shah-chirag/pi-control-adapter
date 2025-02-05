package in.fortytwo42.adapter.service;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.dao.SRAApplicationGatewayRelDaoIntf;
import in.fortytwo42.daos.dao.SRAGatewaySettingDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.bean.SRAGatewaySetting;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.RequestSubType;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;

public class SRAGatewaySettingServiceImpl implements SRAGatewaySettingServiceIntf {

	private String SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG = "<<<<< SRAGatewaySettingServiceImpl";
	private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

	private RequestDaoIntf requestDao = DaoFactory.getRequestDao();
	
    private AttributeStoreDaoIntf attributeStoreDaoIntf = DaoFactory.getAttributeStoreDao();
    
    private SRAGatewaySettingDaoIntf sraGatewaySettingDao = DaoFactory.getSRAGatewaySettingDao();
    private SRAApplicationGatewayRelDaoIntf sraApplicationGatewayRelDao = DaoFactory.getSRAApplicationGatewayRelDoa();
    
    private IamExtensionServiceIntf iamExtensionServiceImpl = ServiceFactory.getIamExtensionService();

	private static Logger logger= LogManager.getLogger(SRAGatewaySettingServiceImpl.class);

	private static final class InstanceHolder {
		private static final SRAGatewaySettingServiceImpl INSTANCE = new SRAGatewaySettingServiceImpl();

		private InstanceHolder() {
			super();
		}
	}

	public static SRAGatewaySettingServiceImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}

	@Override
	public SRAGatewaySettingTO createSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor) throws AuthException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " createSRAGatewaySetting : start");
		Request request = new Request();
		request.setRequestJSON(new Gson().toJson(sraGatewaySettingTO));
		request.setRequestorComments(sraGatewaySettingTO.getComments());
		request.setRequestType(RequestType.SRA_GATEWAY_SETTING_ONBOARD);
		request.setRequestSubType(RequestSubType.SRA_GATEWAY_SETTING_ONBOARD);
		
		try {
			request.setRequestor(attributeStoreDaoIntf.getUserByAttributeValue(actor));
		} catch (AttributeNotFoundException e) {
			throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(),
					errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
		}

		request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
		Request createdRequest = requestDao.create(session, request);
		sraGatewaySettingTO.setStatus(Constant.SUCCESS_STATUS);
		sraGatewaySettingTO.setId(createdRequest.getId());
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " createSRAGatewaySetting : end");
		return sraGatewaySettingTO;
	}

	@Override
	public SRAGatewaySettingTO editSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor) throws AuthException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " editSRAGatewaySetting : start");
		Request request = new Request();
		request.setRequestJSON(new Gson().toJson(sraGatewaySettingTO));
		request.setRequestorComments(sraGatewaySettingTO.getComments());
		request.setRequestType(RequestType.SRA_GATEWAY_SETTING_UPDATE);
		request.setRequestSubType(RequestSubType.SRA_GATEWAY_SETTING_UPDATE);
		
		try {
			request.setRequestor(attributeStoreDaoIntf.getUserByAttributeValue(actor));
		} catch (AttributeNotFoundException e) {
			throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(),
					errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
		}

		request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
		Request createdRequest = requestDao.create(session, request);
		sraGatewaySettingTO.setStatus(Constant.SUCCESS_STATUS);
		sraGatewaySettingTO.setId(createdRequest.getId());
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " editSRAGatewaySetting : end");
		return sraGatewaySettingTO;
	}

	@Override
	public SRAGatewaySettingTO deleteSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor) throws AuthException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " deleteSRAGatewaySetting : start");
		Request request = new Request();
		request.setRequestJSON(new Gson().toJson(sraGatewaySettingTO));
		request.setRequestorComments(sraGatewaySettingTO.getComments());
		request.setRequestType(RequestType.SRA_GATEWAY_SETTING_DELETION);
		request.setRequestSubType(RequestSubType.SRA_GATEWAY_SETTING_DELETION);
		
		try {
			request.setRequestor(attributeStoreDaoIntf.getUserByAttributeValue(actor));
		} catch (AttributeNotFoundException e) {
			throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(),
					errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
		}

		request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
		Request createdRequest = requestDao.create(session, request);
		sraGatewaySettingTO.setStatus(Constant.SUCCESS_STATUS);
		sraGatewaySettingTO.setId(createdRequest.getId());
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " deleteSRAGatewaySetting : end");
		return sraGatewaySettingTO;
	}
	
	@Override
	public SRAGatewaySettingTO getSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO) throws NotFoundException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " getSRAGatewaySetting : start");
		SRAGatewaySetting sraGatewaySetting = sraGatewaySettingDao.getSRAGatewaySetting(sraGatewaySettingTO.getAddress(), sraGatewaySettingTO.getPort(), sraGatewaySettingTO.getClientProxyPort());
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " getSRAGatewaySetting : end");
		return sraGatewaySetting.convertToTO();
	}
	
	@Override
	public SRAGatewaySettingTO getSRAGatewaySettingByName(String gatewaySettingsName) throws NotFoundException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " getSRAGatewaySetting : start");
		SRAGatewaySetting sraGatewaySetting = sraGatewaySettingDao.getSRAGatewaySettingByName(gatewaySettingsName);
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " getSRAGatewaySetting : end");
		return sraGatewaySetting.convertToTO();
	}
	
	@Override
	public SRAGatewaySettingTO getSRAGatewaySettingById(Long sraGatewaySettingId) throws NotFoundException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " getSRAGatewaySetting : start");
		SRAGatewaySetting sraGatewaySetting = sraGatewaySettingDao.getById(sraGatewaySettingId);
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " getSRAGatewaySetting : end");
		return sraGatewaySetting.convertToTO();
	}
	
	@Override
	public PaginatedTO<SRAGatewaySettingTO> getAllSRAGatewaySetting(Integer page, Integer limit, String searchText) throws NotFoundException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " getAllSRAGatewaySetting : start");
		List<SRAGatewaySetting> sraGatewaySettings = sraGatewaySettingDao.getAllSRAGatewaySetting(page, limit, searchText);
		Long count = sraGatewaySettingDao.getCountAllSRAGatewaySetting(page, limit, searchText);
		PaginatedTO<SRAGatewaySettingTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(new EntityToTOConverter<SRAGatewaySetting, SRAGatewaySettingTO>().convertEntityListToTOList(sraGatewaySettings));
        paginatedTO.setTotalCount(count);
        return paginatedTO;
	}
	
	@Override
	public SRAGatewaySettingTO approveAddSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO) {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " approveAddSRAGatewaySetting : start");
		AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + " add SRAGateway request approved ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
		SRAGatewaySetting sraGatewaySetting = new SRAGatewaySetting();
		sraGatewaySetting.setName(sraGatewaySettingTO.getName());
		sraGatewaySetting.setAddress(sraGatewaySettingTO.getAddress());
		sraGatewaySetting.setPort(sraGatewaySettingTO.getPort());
		sraGatewaySetting.setClientProxyPort(sraGatewaySettingTO.getClientProxyPort());
	    sraGatewaySetting = sraGatewaySettingDao.create(session, sraGatewaySetting);
	    try {
			iamExtensionServiceImpl.createGatewaySetttings(sraGatewaySettingTO);
		} catch (AuthException e) {
			logger.log(Level.ERROR, e.getMessage(), e);
		}
		AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + "  SRAGateway onboarded successfully ", "ENTERPRISE", ActionType.ONBOARD, "", IdType.ACCOUNT, "", "", "", null);
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " approveAddSRAGatewaySetting : end");
		return sraGatewaySetting.convertToTO();
	}
	
	@Override
	public SRAGatewaySettingTO approveUpdateSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO) throws AuthException{
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " approveUpdateSRAGatewaySetting : start");
		AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + " edit SRAGateway setting request approved ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
		SRAGatewaySetting sraGatewaySetting = null;
		try {
			 sraGatewaySetting = sraGatewaySettingDao.getById(sraGatewaySettingTO.getId());
		} catch (NotFoundException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND());
		}
		if(sraGatewaySettingTO.getName() !=null) {
			sraGatewaySetting.setName(sraGatewaySettingTO.getName());
		}
		if (sraGatewaySettingTO.getAddress() != null) {
			sraGatewaySetting.setAddress(sraGatewaySettingTO.getAddress());
		}
		if (sraGatewaySettingTO.getPort() != 0) {
			sraGatewaySetting.setPort(sraGatewaySettingTO.getPort());
		}
		if (sraGatewaySettingTO.getClientProxyPort() != 0) {
			sraGatewaySetting.setClientProxyPort(sraGatewaySettingTO.getClientProxyPort());
		}
		sraGatewaySetting = sraGatewaySettingDao.update(session, sraGatewaySetting);
		iamExtensionServiceImpl.editGatewaySetttings(sraGatewaySettingTO);
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " approveUpdateSRAGatewaySetting : end");
		AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + " edit SRAGateway setting successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
		return sraGatewaySetting.convertToTO();
	}
	
	@Override
	public void approveDeleteSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO) throws AuthException{
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " approveDeleteSRAGatewaySetting : start" + sraGatewaySettingTO.getId());
		AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + " delete SRAGateway setting request approved ", "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", "", null);
		SRAGatewaySetting sraGatewaySetting = null;
		try {
			 sraGatewaySetting = sraGatewaySettingDao.getById(sraGatewaySettingTO.getId());
		} catch (NotFoundException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_NOT_FOUND());
		}
		if (sraApplicationGatewayRelDao.getSRAApplicationGatewayRel(sraGatewaySetting) !=null && !sraApplicationGatewayRelDao.getSRAApplicationGatewayRel(sraGatewaySetting).isEmpty()) {
		    throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_ALREADY_BINDED(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_ALREADY_BINDED());
		}
		sraGatewaySettingDao.remove(session, sraGatewaySetting);
		iamExtensionServiceImpl.deleteGatewaySetttings(sraGatewaySetting.getName());
		AuditLogUtil.sendAuditLog(sraGatewaySettingTO.getName() + "SRAGateway setting deleted successfully ", "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", "", null);
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_SERVICE_IMPL_LOG + " approveDeleteSRAGatewaySetting : end");
	}
}
