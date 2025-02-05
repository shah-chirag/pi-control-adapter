package in.fortytwo42.adapter.facade;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.PermissionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.SRAGatewaySettingServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.FileDownloader;
import in.fortytwo42.adapter.util.FileUtil;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.factory.OnboardSRAGatewaySettingsCsv;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.DecryptionDataV2;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.dataconverter.DataConverterFactory;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.enums.CI2Type;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptWE;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.RequestSubType;
import in.fortytwo42.tos.enums.TunnelStatus;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;
import in.fortytwo42.tos.transferobj.TunnelLogTO;

public class SRAGatewaySettingFacadeImpl implements SRAGatewaySettingFacadeIntf {

	private String SRA_GATEWAY_SETTING_FACADE_IMPL_LOG = "<<<<< SRAGatewaySettingFacadeImpl";

	private SRAGatewaySettingServiceIntf sraGatewaySettingService = ServiceFactory.getSRAGatewaySettingService();
	
    private PermissionServiceIntf permissionService = ServiceFactory.getPermissionService();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
	private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private RequestDaoIntf requestDao = DaoFactory.getRequestDao();

	private static Logger logger= LogManager.getLogger(SRAGatewaySettingFacadeImpl.class);
    
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    
    private Config config = Config.getInstance();

	private final ExecutorService pool;

	private SRAGatewaySettingFacadeImpl() {
		super();
		int poolSize = 10;
		try {
			poolSize = Integer.parseInt(config.getProperty(Constant.CSV_PROCESSING_THREAD_POOL_SIZE));
		}
		catch (NumberFormatException e) {
			logger.log(Level.ERROR, e.getMessage(), e);
		}
		pool = Executors.newFixedThreadPool(poolSize);
	}

	private static final class InstanceHolder {
		private static final SRAGatewaySettingFacadeImpl INSTANCE = new SRAGatewaySettingFacadeImpl();

		private InstanceHolder() {
			super();
		}
	}

	public static SRAGatewaySettingFacadeImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}

	@Override
	public SRAGatewaySettingTO addSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id,String role, boolean saveRequest)throws AuthException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " addSRAGatewaySetting : start");
		/*
		 * if (permissionService.isPermissionValidForRole(RequestSubType.
		 * SRA_GATEWAY_SETTING_ONBOARD.name(), role)) { throw new AuthException(null,
		 * errorConstant.getERROR_CODE_PERMISSION_DENIED,
		 * errorConstant.getERROR_MESSAGE_PERMISSION_DENIED); }
		 */
		if (!Pattern.matches(Config.getInstance().getProperty(Constant.URL_REGEX), sraGatewaySettingTO.getAddress())) {
			throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), "Invalid address value");
		}
		if (!Pattern.matches(Config.getInstance().getProperty(Constant.SRAGatewaySettingsName_Regex), sraGatewaySettingTO.getName())) {
			throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), "Invalid name value");
		}
		SRAGatewaySettingTO sraGatewaySettingTO1 = null;
		try {
			sraGatewaySettingTO1 = sraGatewaySettingService.getSRAGatewaySettingByName(sraGatewaySettingTO.getName());
		} catch (NotFoundException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
		}
		if (sraGatewaySettingTO1 != null) {
			throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT());
		}
		List<Request> requests = requestDao.getRequestsBySubType(RequestSubType.SRA_GATEWAY_SETTING_ONBOARD, ApprovalStatus.CHECKER_APPROVAL_PENDING, ApprovalStatus.USER_APPROVAL_PENDING);
		if (requests != null && !requests.isEmpty()) {
			for (Request request : requests) {
				SRAGatewaySettingTO sraGatewaySettingTo = new Gson().fromJson(request.getRequestJSON(), SRAGatewaySettingTO.class);
				if (sraGatewaySettingTo.getName().equals(sraGatewaySettingTO.getName())) {
					throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_ONBOARD_REQUEST_ALREADY_PRESENT());
				}
			}
		}
		Session session = sessionFactoryUtil.getSession();
		try {
			sraGatewaySettingTO = requestService.createSRAGatewaySetting(session, sraGatewaySettingTO, actor,id,saveRequest);
			if(!saveRequest) {
                sraGatewaySettingService.approveAddSRAGatewaySetting(session, sraGatewaySettingTO);
            }
			sessionFactoryUtil.closeSession(session);
		} catch (AuthException e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " addSRAGatewaySetting : end");
		return sraGatewaySettingTO;
	}

	@Override
	public CSVUploadTO uploadSRAGatewaySettings(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
		Date date = new Date(System.currentTimeMillis());
		DateFormat formatter = new SimpleDateFormat("YYYYMMddHHmmss");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		String dateFormatted = formatter.format(date);
		String requestId = UUID.randomUUID().toString();
		String filename = fileName.split(".csv")[0] + "_" + dateFormatted + "_" + requestId + ".csv";
		CSVUploadTO csvUploadTO = new CSVUploadTO();
		csvUploadTO.setRequestId(requestId);
		csvUploadTO.setFileName(filename);
		String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
		pool.submit(() -> {
			ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
			try {
				OnboardSRAGatewaySettingsCsv.getInstance().processCSV(inputStream, role, username,id, filename);
			} catch (AuthException e) {
				new FileDownloader().writeFile(fileName, e);
				throw new RuntimeException(e);
			}
		});
		return csvUploadTO;
	}

	@Override
	public SRAGatewaySettingTO updateSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id,String role, boolean saveRequest) throws AuthException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " updateSRAGatewaySetting : start");
		if (!Pattern.matches(Config.getInstance().getProperty(Constant.URL_REGEX), sraGatewaySettingTO.getAddress())) {
			throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), "Invalid address value");
		}
		PermissionServiceIntf permissionProcessorIntf = permissionService;
		/*
		 * if (permissionProcessorIntf.isPermissionValidForRole(RequestSubType.
		 * SRA_GATEWAY_SETTING_UPDATE.name(), role)) { throw new AuthException(null,
		 * errorConstant.getERROR_CODE_PERMISSION_DENIED,
		 * errorConstant.getERROR_MESSAGE_PERMISSION_DENIED); }
		 */
		SRAGatewaySettingTO sraGatewaySettingTO1 = null;
		try {
			sraGatewaySettingTO1 = sraGatewaySettingService.getSRAGatewaySettingById(sraGatewaySettingTO.getId());
		} catch (NotFoundException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND());
		}
		isvalidSRAGatewaySetting(sraGatewaySettingTO, sraGatewaySettingTO1);
		SRAGatewaySettingTO sraGatewaySettingTO2 = null;
		try {
			 sraGatewaySettingTO2 = sraGatewaySettingService.getSRAGatewaySetting(sraGatewaySettingTO);
		} catch (NotFoundException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
		}
		if (sraGatewaySettingTO2 != null) {
			throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_DATA_ALREADY_PRESENT());
		}
		Session session = sessionFactoryUtil.getSession();
		try {
			sraGatewaySettingTO = requestService.editSRAGatewaySetting(session, sraGatewaySettingTO, actor,id,saveRequest);
			if(!saveRequest) {
                sraGatewaySettingService.approveUpdateSRAGatewaySetting(session, sraGatewaySettingTO);
            }
			sessionFactoryUtil.closeSession(session);
		} catch (AuthException e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " updateSRAGatewaySetting : end");
		return sraGatewaySettingTO;
	}
	
	private void isvalidSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO, SRAGatewaySettingTO sraGatewaySettingTo) throws AuthException {
	        boolean address = sraGatewaySettingTO.getAddress().equals(sraGatewaySettingTo.getAddress());
	        boolean port = sraGatewaySettingTO.getPort().equals(sraGatewaySettingTo.getPort());
	        boolean clientProxyPort = sraGatewaySettingTO.getClientProxyPort().equals(sraGatewaySettingTo.getClientProxyPort());
	        if (address && port && clientProxyPort) {
	            throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
	        }
	}

	@Override
	public SRAGatewaySettingTO deleteSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id,String role, boolean saveRequest) throws AuthException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " deleteSRAGatewaySetting : start");
		/*
		 * if (permissionService.isPermissionValidForRole(RequestSubType.
		 * SRA_GATEWAY_SETTING_DELETION.name(), role)) { throw new AuthException(null,
		 * errorConstant.getERROR_CODE_PERMISSION_DENIED,
		 * errorConstant.getERROR_MESSAGE_PERMISSION_DENIED); }
		 */
		try {
			 sraGatewaySettingService.getSRAGatewaySettingById(sraGatewaySettingTO.getId());
		} catch (NotFoundException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND());
		}
	
		Session session = sessionFactoryUtil.getSession();
		try {
			sraGatewaySettingTO = requestService.deleteSRAGatewaySetting(session, sraGatewaySettingTO, actor,id,saveRequest);
			if(!saveRequest) {
                sraGatewaySettingService.approveDeleteSRAGatewaySetting(session, sraGatewaySettingTO);
			}
			sessionFactoryUtil.closeSession(session);
		} catch (AuthException e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " deleteSRAGatewaySetting : end");
		return sraGatewaySettingTO;
	}
	
	@Override
	public PaginatedTO<SRAGatewaySettingTO> getAllSRAGatewaySetting(Integer page, String searchText) throws AuthException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " getSRAGatewaySetting : start");
		PaginatedTO<SRAGatewaySettingTO> sraGatewaySettingTOs;
		try {
			sraGatewaySettingTOs = sraGatewaySettingService.getAllSRAGatewaySetting(page, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText);
		} catch (NotFoundException e) {
			throw new AuthException(null, errorConstant.getERROR_CODE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SRA_GATEWAY_SETTING_REQUEST_DATA_NOT_FOUND());
		}
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " getSRAGatewaySetting : end");
		return sraGatewaySettingTOs;
	}

    @Override
    public DecryptionDataV2 getDecryptionKey(String signTransactionId,String clientIp, String applicationId, String type, String host, Integer port) throws AuthException {
        logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " getDecryptionKey : start");
        IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
        IAMExtensionV2 iamExtension;
        
        try {
			String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            iamExtension = iamExtensionService.getIAMExtension();
            Token token = iamExtensionService.getToken(iamExtension);
            ApprovalAttemptWE approvalAttemptWE = iamExtension.getApprovalAttemptBySignTransactionIdV2(token, signTransactionId);
            TunnelLogTO tunnelLogTO = new TunnelLogTO();
            tunnelLogTO.setSignTransactionId(signTransactionId);
            tunnelLogTO.setTargetHost(host);
            tunnelLogTO.setTargetPort(port);
            tunnelLogTO.setClientIpAddress(clientIp);
            if (approvalAttemptWE != null) {
                AccountType accountType = AccountType.valueOf(token.getRole());
                ApprovalAttemptTO approvalAttemptTO = DataConverterFactory.getDataFilter(accountType)
                        .getApprovalAttemptTO(approvalAttemptWE);
                String senderAccountId = approvalAttemptTO.getCreatorConsumerAccountId();
                logger.log(Level.DEBUG, "senderAccountId : "+senderAccountId);
                tunnelLogTO.setAccountId(senderAccountId);
                boolean isMatch = FacadeFactory.getApplicationFacade().isSRADetailsMatch(senderAccountId, host, port);
                
                if (isMatch) {
                    CI2Type ci2Type = CI2Type.SYNC_VERIFY;
                    DecryptionDataV2 decryptionDataV2 = iamExtension.getDecryptionKey(token, signTransactionId, ci2Type, approvalAttemptTO, reqRefNum);
                    logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " getDecryptionKey : end");
                    tunnelLogTO.setStatus(TunnelStatus.SUCCESS.name());
                    FacadeFactory.getTunnelLogFacade().createTunnelLog(tunnelLogTO);
                    return decryptionDataV2;
                }
                else {
                    logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " getDecryptionKey : end");
                    tunnelLogTO.setStatus(TunnelStatus.FAILURE.name());
                    FacadeFactory.getTunnelLogFacade().createTunnelLog(tunnelLogTO);
                    throw new AuthException(null, errorConstant.getERROR_CODE_SRA_DETAILS_NOT_MATCHED(), errorConstant.getERROR_MESSAGE_SRA_DETAILS_NOT_MATCHED());
                }
            }else {
                logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " getDecryptionKey : end");
                throw new AuthException(null, errorConstant.getERROR_CODE_TRANSACTION_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TRANSACTION_NOT_FOUND());
            }
        }
        catch (IAMException e) {
            logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " getDecryptionKey : end");
            logger.log(Level.ERROR, e);
            throw IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        }
    }

	@Override
	public String readSampleCsvFile(String fileName) {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " readSampleCsvFile : start");
		fileName = fileName != null ? fileName : "SRAGatewaySettings-onboard.csv";
		String content = FileUtil.getSampleUserOnboardCsv(fileName);
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " readSampleCsvFile : end");
		return content;
	}

	@Override
	public String downloadUpdateSRAGatewaySettingStatus(String fileName, String role) throws AuthException {
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " downloadUpdateSRAGatewaySettingStatus : start");
		String content = new FileDownloader().downloadCSVStatusFile(fileName, role);
		logger.log(Level.DEBUG, SRA_GATEWAY_SETTING_FACADE_IMPL_LOG + " downloadUpdateSRAGatewaySettingStatus : end");
		return content;
	}
}
