package in.fortytwo42.adapter.facade;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.fortytwo42.adapter.util.ExternalConfigUtil;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.FalloutDataNotFound;
import in.fortytwo42.entities.bean.FalloutSyncData;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;
import org.keycloak.representations.idm.UserRepresentation;

import dev.morphia.transactions.MorphiaSession;
import in.fortytwo42.adapter.cam.dto.CamAttribute;
import in.fortytwo42.adapter.cam.dto.EditUserRequest;
import in.fortytwo42.adapter.cam.service.CamAdminServiceIntf;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.CAMUnknownException;
import in.fortytwo42.adapter.jar.MongoConnectionManager;
import in.fortytwo42.adapter.jar.exception.BadDataException;
import in.fortytwo42.adapter.service.AttributeMasterServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.FalloutServiceIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.ErrorTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.FalloutProcessJob;
import in.fortytwo42.adapter.util.FileUtil;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.adapter.util.ValidationUtilV3;
import in.fortytwo42.adapter.util.factory.CsvFactory;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.FalloutSyncDataDaoImpl;
import in.fortytwo42.daos.dao.FalloutSyncDataDaoIntf;
import in.fortytwo42.daos.exception.FalloutSyncDataNotFound;
import in.fortytwo42.daos.exception.UserNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.AttributeOperationStatus;
import in.fortytwo42.enterprise.extension.enums.AttributeSecurityType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.tos.GenerateAttributeClaimSelfSignedTO;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.AttributeState;
import in.fortytwo42.tos.enums.AttributeAction;
import in.fortytwo42.tos.enums.BulkUploadType;
import in.fortytwo42.tos.enums.Status;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.CsvFileDataTO;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;
import in.fortytwo42.tos.transferobj.FalloutTO;

public class FalloutProcessFacdeImpl implements FalloutProcessFacadeIntf {

    private static final String FALLOUT_FACDE_IMPL_LOG = "<<<<< FalloutProcessFacdeImpl";
    private static Logger logger = LogManager.getLogger(FalloutProcessFacdeImpl.class);
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private Config config = Config.getInstance();
    private final ExternalConfigUtil externalConfig=ExternalConfigUtil.getInstance();
    private AttributeMasterServiceIntf attributeMasterService = ServiceFactory.getAttributeMasterService();

    private UserServiceIntf userService = ServiceFactory.getUserService();
    private AttributeStoreServiceIntf attributeStoreService = ServiceFactory.getAttributeStoreService();

    private final CamAdminServiceIntf camAdminService = ServiceFactory.getCamAdminService();
    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();
    private final ExecutorService pool;

    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();
    FalloutServiceIntf falloutServiceIntf = ServiceFactory.getFalloutService();
    private MongoConnectionManager idsMongoConnectionManager = MongoConnectionManager.getInstance();

    private FalloutProcessFacdeImpl() {
        super();
        int poolSize = 10;
        try {
            poolSize = Integer.parseInt(config.getProperty(Constant.CSV_PROCESSING_THREAD_POOL_SIZE));
        } catch (NumberFormatException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);

    }

    @Override
    public void processFallout(String startTime, String endTime) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFallout : start");
        Session session = sessionFactoryUtil.openSessionWithoutTransaction();
        try {
            new FalloutProcessJob().processFallout(startTime, endTime, session);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFallout : end");
        }
    }

    private static final class InstanceHolder {

        private static final FalloutProcessFacdeImpl INSTANCE = new FalloutProcessFacdeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static FalloutProcessFacdeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String processCSV(String filename, InputStream inputStream) {
        return null;
    }

    @Override
    public CSVUploadTO uploadFalloutProcessCSV(BulkUploadType fileType, InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " uploadFalloutProcessCSV : start");
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat(Constant.DATE_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone(Constant.TIMEZONE));
        String dateFormatted = formatter.format(date);
        String requestId = UUID.randomUUID().toString();
        String filename = fileName.split(Constant.CSV_FILE_EXTENSION)[0] + Constant._UNDERSCORE + dateFormatted + Constant._UNDERSCORE + requestId + Constant.CSV_FILE_EXTENSION;
        CSVUploadTO csvUploadTO = new CSVUploadTO();
        csvUploadTO.setRequestId(requestId);
        csvUploadTO.setFileName(filename);
        CsvFactory.processCsv(fileType.name(), inputStream, role,id, filename);
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(() -> {
            ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
            fallOutProcess(filename);
        });
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " uploadFalloutProcessCSV : end");
        return csvUploadTO;
    }

    @Override
    public String readSampleCsvFile(String fileName) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " readSampleCsvFile : start");
        fileName = fileName != null ? fileName : "fallout-process-sample.csv";
        String content = FileUtil.getSampleUserOnboardCsv(fileName);
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " readSampleCsvFile : end");
        return content;
    }

    @Override
    public void fallOutProcess(String fileName) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " fallOutProcess : start");
        CsvFileDataTO csvFileDataTO = null;
        Session session = sessionFactoryUtil.openSessionWithoutTransaction();
        try {
            while ((csvFileDataTO = falloutServiceIntf.getCsvData(session, fileName)) != null) {
                if (!falloutServiceIntf.updateCsvDataStatusAndMessage(session, csvFileDataTO.getId(), Status.INPROGRESS, "").equals(Status.INPROGRESS)) {
                    continue;
                }
                FalloutTO falloutRecord = StringUtil.fromJson(csvFileDataTO.getRecord(), FalloutTO.class);
                String errorMessage = null;
                try {
                    errorMessage = ValidationUtilV3.isFalloutRecordValid(falloutRecord);
                } catch (AuthException e) {
                    falloutServiceIntf.updateCsvDataStatusAndMessage(session, csvFileDataTO.getId(), Status.FAILED, StringUtil.toJson(e));
                    return;
                }
                if (!ValidationUtilV3.isValid(errorMessage)) {
                    try {
                        processFalloutRecord(session, falloutRecord);
                        falloutServiceIntf.updateCsvDataStatusAndMessage(session, csvFileDataTO.getId(), Status.SUCCESS, "");
                    } catch (AuthException e) {
                        ErrorTO error = new ErrorTO(e.getErrorCode(), errorConstant.getERROR_MESSAGE_FALLOUT_INVALID_DATA(), e.getMessage());
                        falloutServiceIntf.updateCsvDataStatusAndMessage(session, csvFileDataTO.getId(), Status.FAILED, StringUtil.toJson(error));
                        logger.log(Level.ERROR, e.getMessage(), e);
                    } catch (IAMException e) {
                        ErrorTO error = new ErrorTO((long) e.getErrorCode(), errorConstant.getERROR_MESSAGE_FALLOUT_INVALID_DATA(), e.getMessage());
                        falloutServiceIntf.updateCsvDataStatusAndMessage(session, csvFileDataTO.getId(), Status.FAILED, StringUtil.toJson(error));
                        logger.log(Level.ERROR, e.getMessage(), e);
                    } catch (Exception e) {
                        ErrorTO error = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), e.getMessage());
                        falloutServiceIntf.updateCsvDataStatusAndMessage(session, csvFileDataTO.getId(), Status.FAILED, StringUtil.toJson(error));
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                } else {
                    ErrorTO error = new ErrorTO(errorConstant.getERROR_CODE_FALLOUT_INVALID_DATA(), errorConstant.getERROR_MESSAGE_FALLOUT_INVALID_DATA(), errorMessage);
                    falloutServiceIntf.updateCsvDataStatusAndMessage(session, csvFileDataTO.getId(), Status.FAILED, StringUtil.toJson(error));
                }
            }
        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " fallOutProcess : end");
        }
    }

    public void processFalloutRecord(Integer numberOfRecordsToBeProcessed) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecord : start");
        Session session = null;
        Long processingStartTime = System.currentTimeMillis();
        try {
            session = sessionFactoryUtil.openSessionWithoutTransaction();
            FalloutTO falloutTO = null;
            while ((falloutTO = falloutServiceIntf.getFallout(session)) != null && numberOfRecordsToBeProcessed > 0) {
                logger.log(Level.TRACE, FALLOUT_FACDE_IMPL_LOG + " processing record with id - " + falloutTO.getId() + " : start");
                if (!falloutServiceIntf.updateFallout(session, falloutTO.getId(), Status.INPROGRESS, "", 0L).equals(Status.INPROGRESS)) {
                    continue;
                }
                if (isFalloutRecordValid(falloutTO, session, falloutTO.getId())) {
                    processRecord(falloutTO, session, falloutTO.getId(), processingStartTime);
                }
                numberOfRecordsToBeProcessed--;
                logger.log(Level.TRACE, FALLOUT_FACDE_IMPL_LOG + " processing record with id - " + falloutTO.getId() + " : end");
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecord : end");
        }
    }

    @Override
    public FalloutSyncDataTo updateFalloutSyncData(FalloutSyncDataTo falloutSyncDataTo) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateFalloutSyncData : start");
        return falloutServiceIntf.updateFalloutSyncData(falloutSyncDataTo);
    }

    @Override
    public FalloutSyncDataTo createUpdateSyncDataRequest( String role, String actor,Long id,
                                                          FalloutSyncDataTo falloutSyncDataTo, boolean saveRequest)
            throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " createUpdateSyncDataRequest : start");
        FalloutSyncDataDaoIntf falloutSyncDataDao = FalloutSyncDataDaoImpl.getInstance();
         RequestServiceIntf requestService = ServiceFactory.getRequestService();
        Session session = sessionFactoryUtil.getSession();
        FalloutSyncData falloutSyncData = null;
        try {
            falloutSyncData = falloutSyncDataDao.getFalloutSyncRecord(falloutSyncDataTo.getId(), session);
        }catch (FalloutSyncDataNotFound e){

            throw  new AuthException(new Exception(),errorConstant.getERROR_CODE_INVALID_DATA(),errorConstant.getERROR_MESSAGE_FALLOUT_SYNC_DATA_NOT_FOUND());
        }
        try {
            isExistingAndUpdatedDataSame(falloutSyncData, falloutSyncDataTo);
            falloutSyncDataTo = requestService.createUpdateFalloutSyncData(session, falloutSyncDataTo, actor,id,
                    saveRequest);
            session.getTransaction().commit();

            if (!saveRequest) {
                falloutSyncDataTo = updateFalloutSyncData(falloutSyncDataTo);
            }
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw  new AuthException(new Exception(),errorConstant.getERROR_CODE_INVALID_DATA(),e.getMessage());
        }finally {
            if(session.isOpen()){
                session.close();
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " createUpdateSyncDataRequest : end");
        return falloutSyncDataTo;
    }

    private void isExistingAndUpdatedDataSame(FalloutSyncData falloutSyncData, FalloutSyncDataTo falloutSyncDataTo) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " isExistingAndUpdatedDataSame : start");
        Long updatedLastSyncTime = falloutSyncDataTo.getLastSyncTime();
        Long lastSyncTime = falloutSyncData.getLastSyncTime().getTime();
        if (lastSyncTime.equals(updatedLastSyncTime)) {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " isExistingAndUpdatedDataSame : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " isExistingAndUpdatedDataSame : end");
    }

    private void processRecord(FalloutTO falloutRecord, Session session, Long id, Long processingStartTime) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processRecord : start");
        try {
            processFalloutRecord(session, falloutRecord, id);
            falloutServiceIntf.updateFallout(session, id, Status.SUCCESS, "", System.currentTimeMillis() - processingStartTime);
        } catch (AuthException e) {
            ErrorTO error = new ErrorTO(e.getErrorCode(), errorConstant.getERROR_MESSAGE_FALLOUT_INVALID_DATA(), e.getMessage());
            falloutServiceIntf.updateFallout(session, id, Status.FAILED, StringUtil.toJson(error), System.currentTimeMillis() - processingStartTime);
            logger.log(Level.ERROR, e.getMessage(), e);
        } catch (IAMException e) {
            ErrorTO error = new ErrorTO((long) e.getErrorCode(), errorConstant.getERROR_MESSAGE_FALLOUT_INVALID_DATA(), e.getMessage());
            falloutServiceIntf.updateFallout(session, id, Status.FAILED, StringUtil.toJson(error), System.currentTimeMillis() - processingStartTime);
            logger.log(Level.ERROR, e.getMessage(), e);
        } catch (Exception e) {
            ErrorTO error = new ErrorTO(errorConstant.getERROR_CODE_INTERNAL_SERVER_ERROR(), errorConstant.getERROR_MESSAGE_INTERNAL_SERVER_ERROR(), e.getMessage());
            falloutServiceIntf.updateFallout(session, id, Status.FAILED, StringUtil.toJson(error), System.currentTimeMillis() - processingStartTime);
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processRecord : end");
        }
    }

    public void processFalloutRecord(Session session, FalloutTO falloutRecord, long id) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecord : start");
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        Token token = iamExtensionService.getToken(iamExtension);
        List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
        List<AttributeTO> searchAttributeTOs = new ArrayList<>();
        AttributeTO searchAttributeTO = new AttributeTO();
        searchAttributeTO.setAttributeName(Constant.USER_ID);
        searchAttributeTO.setAttributeValue(falloutRecord.getFt42UserId().toUpperCase());
        searchAttributeTOs.add(searchAttributeTO);
        AccountWE account = iamExtension.getAccountByAttributes(searchAttributeTOs, token);
        if (account == null || account.getId() == null || account.getId().isEmpty()) {
            throw new AuthException(null, null, "Account not found");
        }
        User user = userService.getActiveUser(session, account.getId());
        if (user == null) {
            throw new AuthException(null, null, "User not found");
        }
        String actualMobileNumber = falloutRecord.getActualMobileNo();
        String oldMobileNumber = null;
        AttributeTO oldMobileNumberTO = null;
        if (falloutRecord.getOldMobileNo() != null && !falloutRecord.getOldMobileNo().isEmpty()) {
            oldMobileNumber = falloutRecord.getOldMobileNo();
            oldMobileNumberTO = new AttributeTO();
            oldMobileNumberTO.setAttributeName(Constant.MOBILE_NO);
            oldMobileNumberTO.setAttributeValue(oldMobileNumber);
        }
        String newMobileNumber = falloutRecord.getNewMobileNo();
        if (!actualMobileNumber.equals(externalConfig.getProperty(Constant.FALLOUT_PROCESS_DEH_NO_MOBILE_NO,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT)))
            processFalloutRecordByActualMobileNumber(iamExtension, token, account, user, oldMobileNumberTO, actualMobileNumber, newMobileNumber, falloutRecord.getOperation().name(), falloutRecord, session);
        else {
            //mobile number does not exist in DEH -  Delete old mobile number
            processFalloutRecordWithoutActualMobileNumber(iamExtension, token, account, user, oldMobileNumberTO, newMobileNumber, falloutRecord.getOperation().name(), falloutRecord, session);

        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecord : end");
    }

    private void processFalloutRecordByActualMobileNumber(IAMExtensionV2 iamExtension, Token token, AccountWE account, User user, AttributeTO oldMobileNumberTO, String actualMobileNumber, String newMobileNumber, String operation, FalloutTO falloutRecord, Session session) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecordByActualMobileNumber : start");
        List<AttributeTO> searchAttributeTOs = new ArrayList<>();
        AttributeTO searchActualMobileNoAttributeTO = new AttributeTO();
        searchActualMobileNoAttributeTO.setAttributeName(Constant.MOBILE_NO);
        searchActualMobileNoAttributeTO.setAttributeValue(actualMobileNumber.toUpperCase());
        searchAttributeTOs.add(searchActualMobileNoAttributeTO);
        AccountWE accountByActualMobileNumber = iamExtension.getAccountByAttributes(searchAttributeTOs, token);
        if (accountByActualMobileNumber != null && accountByActualMobileNumber.getId() != null && !accountByActualMobileNumber.getId().isEmpty()) {
            if (!account.getId().equals(accountByActualMobileNumber.getId())) {
                //actual mobile number present for other user
                // Delete from other user
                User otherUser = userService.getActiveUser(session, accountByActualMobileNumber.getId());
                deleteAttribute(accountByActualMobileNumber, otherUser, searchActualMobileNoAttributeTO, falloutRecord, session);
                // add to current user
                addAttributebyDBConnection(searchActualMobileNoAttributeTO, account, user, session);
                if (oldMobileNumberTO != null && !actualMobileNumber.equals(oldMobileNumberTO.getAttributeValue())) {
                    // Delete old mobile number
                    deleteAttribute(account, user, oldMobileNumberTO, falloutRecord, session);
                }
            }
            //delete new mobile number if operation is DELETE
            processDeleteOperationForNewMobileNumber(iamExtension, token, account, user, newMobileNumber, operation, falloutRecord, session, searchAttributeTOs, actualMobileNumber);
            // Actual Mobile number present for current user - Nothing to do
            processOldMobileNumber(iamExtension, token, account, user, oldMobileNumberTO, actualMobileNumber, falloutRecord, session);
        } else {
            // Add actual mobile number to current user
            addAttributebyDBConnection(searchActualMobileNoAttributeTO, account, user, session);
            //delete old mobile number
            if (oldMobileNumberTO != null && !actualMobileNumber.equals(oldMobileNumberTO.getAttributeValue())) {
                // Delete old mobile number
                deleteAttribute(account, user, oldMobileNumberTO, falloutRecord, session);
            }
            //delete new mobile number if operation is DELETE
            processDeleteOperationForNewMobileNumber(iamExtension, token, account, user, newMobileNumber, operation, falloutRecord, session, searchAttributeTOs, actualMobileNumber);
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecordByActualMobileNumber : end");
    }

    private void processOldMobileNumber(IAMExtensionV2 iamExtension, Token token, AccountWE account, User user, AttributeTO oldMobileNumberTO, String actualMobileNumber, FalloutTO falloutRecord, Session session) throws Exception {
        // check old mobile number already present with current user
        // if yes - delete it
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processOldMobileNumber : start");
        if (oldMobileNumberTO != null && !actualMobileNumber.equals(oldMobileNumberTO.getAttributeValue())) {
            List<AttributeTO> searchAttributeTOs = new ArrayList<>();
            searchAttributeTOs.add(oldMobileNumberTO);
            AccountWE accountByOldMobileNumber = iamExtension.getAccountByAttributes(searchAttributeTOs, token);
            if (accountByOldMobileNumber != null && accountByOldMobileNumber.getId() != null && !accountByOldMobileNumber.getId().isEmpty()) {
                if (account.getId().equals(accountByOldMobileNumber.getId())) {
                    deleteAttribute(account, user, oldMobileNumberTO, falloutRecord, session);
                }
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processOldMobileNumber : end");
    }

    private void processFalloutRecordWithoutActualMobileNumber(IAMExtensionV2 iamExtension, Token token, AccountWE account, User user, AttributeTO oldMobileNumberTO, String newMobileNumber, String operation, FalloutTO falloutRecord, Session session) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecordWithoutActualMobileNumber : start");
        List<AttributeTO> searchAttributeTOs = new ArrayList<>();
        if (oldMobileNumberTO != null) {
            searchAttributeTOs.add(oldMobileNumberTO);
            AccountWE accountByOldMobileNumber = iamExtension.getAccountByAttributes(searchAttributeTOs, token);
            if (accountByOldMobileNumber != null && accountByOldMobileNumber.getId() != null && !accountByOldMobileNumber.getId().isEmpty()) {
                if (account.getId().equals(accountByOldMobileNumber.getId())) {
                    deleteAttribute(account, user, oldMobileNumberTO, falloutRecord, session);
                }
                //nothing to do
            }
        }
        processDeleteOperationForNewMobileNumber(iamExtension, token, account, user, newMobileNumber, operation, falloutRecord, session, searchAttributeTOs, "");
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecordWithoutActualMobileNumber : end");
    }

    private void deleteAttribute(AccountWE otherUsersAccount, User otherUser, AttributeTO actualMobileNoAttributeTO, FalloutTO falloutRecord, Session session) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttribute : start");
        MorphiaSession morphiaSession = MongoConnectionManager.getInstance().getDatastore().startSession();
        try {
            morphiaSession.startTransaction();
            session.beginTransaction();
            deleteAttributeFromAdaptor(session, otherUser, Constant.MOBILE_NO, actualMobileNoAttributeTO.getAttributeValue());
            deleteCAMAttribute(otherUser, actualMobileNoAttributeTO);
            deleteAttributeFromIds(actualMobileNoAttributeTO, otherUsersAccount, morphiaSession);
            session.getTransaction().commit();
            morphiaSession.commitTransaction();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            if (session.isOpen() && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            if (morphiaSession != null && morphiaSession.hasActiveTransaction()) {
                morphiaSession.abortTransaction();
            }
            throw e;
        }finally {
            if(morphiaSession != null){
                morphiaSession.close();
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttribute : end");
    }

    private void deleteCAMAttribute(User otherUser, AttributeTO actualMobileNoAttributeTO) throws AuthException {
        int retryCount = config.getProperty(Constant.FALLOUT_PROCESS_RETRY_COUNT) != null ? Integer.parseInt(config.getProperty(Constant.FALLOUT_PROCESS_RETRY_COUNT)) : 5;
        while (retryCount > 0) {
            try {
                deleteAttributeFromCam(otherUser, Constant.MOBILE_NO, actualMobileNoAttributeTO.getAttributeValue());
                break;
            } catch (CAMUnknownException e) {
                if (--retryCount == 0) {
                    logger.log(Level.WARN, "Failed to delete attribute : " + Constant.MOBILE_NO + " - " + actualMobileNoAttributeTO.getAttributeValue() + "for account : " + otherUser.getAccountId() + " from CAM ");
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
        }
    }

    private void addAttribute(AttributeTO actualMobileNoAttributeTO, AccountWE currentAccount, Token token, IAMExtensionV2 iamExtension, User user, Session session) throws AuthException {
        Map<String, String> attributeValueWithPlainValue = new HashMap<String, String>();
        manageAttributeIsDefault(session, actualMobileNoAttributeTO, user);
        attributeValueWithPlainValue.put(actualMobileNoAttributeTO.getAttributeValue(), actualMobileNoAttributeTO.getAttributeValue());
        addAttributeOnIds(actualMobileNoAttributeTO, iamExtension, currentAccount, token, attributeValueWithPlainValue);
        attributeAddOnCAM(actualMobileNoAttributeTO, user);
        //addAttributeOnCam(actualMobileNoAttributeTO, user);
        addAttributeOnAdapter(session, actualMobileNoAttributeTO, user);
    }

    private void addAttributebyDBConnection(AttributeTO actualMobileNoAttributeTO, AccountWE currentAccount, User user, Session session) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributebyDBConnection : start");
        MorphiaSession morphiaSession = MongoConnectionManager.getInstance().getDatastore().startSession();;
        try {
            morphiaSession.startTransaction();
            session.beginTransaction();
            Map<String, String> attributeValueWithPlainValue = new HashMap<String, String>();
            manageAttributeIsDefault(session, actualMobileNoAttributeTO, user);
            attributeValueWithPlainValue.put(actualMobileNoAttributeTO.getAttributeValue(), actualMobileNoAttributeTO.getAttributeValue());
            addAttributeOnAdapter(session, actualMobileNoAttributeTO, user);
            attributeAddOnCAM(actualMobileNoAttributeTO, user);
            //addAttributeOnCam(actualMobileNoAttributeTO, user);
            addAttributeOnIds(actualMobileNoAttributeTO, currentAccount, morphiaSession);
            session.getTransaction().commit();
            morphiaSession.commitTransaction();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            if (session.isOpen() && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            if (morphiaSession != null && morphiaSession.hasActiveTransaction()) {
                morphiaSession.abortTransaction();
            }
            throw e;
        }finally {
            if(morphiaSession != null){
                morphiaSession.close();
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributebyDBConnection : end");

    }

    private void addAttributeOnIds(AttributeTO attributeTO, IAMExtensionV2 iamExtension, AccountWE account, Token token, Map<String, String> attributeValueWithPlainValue) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnIds : start");
        try {
            List<AttributeTO> attributeDataTOs = new ArrayList<>();
            attributeDataTOs.add(attributeTO);
            account.setAttributes(attributeDataTOs);
            AccountWE accountWE = iamExtension.onboardAccountwithAttribute(account, account.getId(), true, token);
            if (accountWE.getStatus() != null && Status.FAILED.name().equals(accountWE.getStatus())) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            accountWE.setUserCredential(account.getUserCredential());
            Map<String, Object> attributeValueWithKey = null;
            boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
            try {
                if (enableCrypto) {
                    attributeValueWithKey = registerAttributesOnCrypto(iamExtension, token, attributeValueWithPlainValue, accountWE, account, attributeDataTOs);
                } else {
                    attributeValueWithKey = new HashMap<>();
                }
            } catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                attributeValueWithKey = new HashMap<>();
                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = new GenerateAttributeClaimSelfSignedTO();
                generateAttributeClaimSelfSignedTO.setTransactionID("6c1f17951a9f89df82cf25980f41bcb32720dde11b9698039d31a151091a371f01c0163c034badef");
                generateAttributeClaimSelfSignedTO.setKey("753c2355163b9bb7c71065d0b26427fd");
                for (AttributeTO attributeDataTO : accountWE.getAttributes()) {
                    String plainValue = attributeValueWithPlainValue.get(attributeDataTO.getAttributeValue());
                    attributeValueWithKey.put(plainValue, generateAttributeClaimSelfSignedTO);
                }
            }
            iamExtension.editAttributes(accountWE.getAttributes(), accountWE.getId(), token);
        } catch (IAMException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnIds : end");
        }

    }

    private void addAttributeOnIds(AttributeTO attributeTO, AccountWE account, MorphiaSession session) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnIds : start");
        try {
            idsMongoConnectionManager.addAttribute(attributeTO, account.getId(), session);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnIds : end");
        }
    }


    public void deleteAttributeFromIds(AttributeTO attributeTO, AccountWE accountWE, MorphiaSession session) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeFromIds : start");
        try {
            idsMongoConnectionManager.deleteAttribute(attributeTO, accountWE.getId(), session);
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } catch (BadDataException e) {
            if (e.getErrorCode() == 1406L) {
                logger.log(Level.WARN, e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeFromIds : end");
        }
    }

    public void deleteAttributeFromCam(User user, String attributeName, String attributeValue) throws AuthException, CAMUnknownException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeFromCam : start");
        try {
            if (user.getKcId() != null && !user.getKcId().isEmpty()) {
                UserRepresentation userRepresentation = camAdminService.getUserDetails(config.getProperty(Constant.CAM_REALM), user.getKcId());
                List<String> camAttributes = userRepresentation.getAttributes().get(attributeName);
                if (camAttributes != null) {
                    for (String number : camAttributes) {
                        if (number.equals(attributeValue)) {
                            EditUserRequest editUserRequest = new EditUserRequest();
                            List<CamAttribute> camAttributeList = new ArrayList<>();
                            CamAttribute camAttribute = new CamAttribute(attributeName, attributeValue);
                            camAttributeList.add(camAttribute);
                            editUserRequest.setUserKcId(user.getKcId());
                            editUserRequest.setAttributeAction(AttributeAction.DELETE);
                            editUserRequest.setAttributes(camAttributeList);
                            boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                            if (!camStatus) {
                                logger.log(Level.ERROR, FALLOUT_FACDE_IMPL_LOG + " CAM Edit user failed for DELETE Attribute.");
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARN, "Failed to delete attribute from CAM");
            logger.log(Level.WARN, e.getMessage(), e);
            throw new CAMUnknownException(e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeFromCam : end");
        }
    }

    public void deleteAttributeFromAdaptor(Session session, User user, String attributeName, String attributeValue) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteForTakeOverAttributeOnAdaptor : start");
        try {
            AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName, attributeValue, user.getId(), session);
            attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
            attributeStoreService.update(session, attributeTobeUpdate);
        } catch (AuthException e) {
            if (e.getErrorCode() == errorConstant.getERROR_CODE_ATTRIBUTE_NOT_FOUND()) {
                logger.log(Level.INFO, "No Attribute found.");
            } else {
                throw e;
            }
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteForTakeOverAttributeOnAdaptor : end");
        }
    }


    private boolean isFalloutRecordValid(FalloutTO falloutRecord, Session session, Long id) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " validateFalloutRecord : start");
        try {
            String errorMessage = ValidationUtilV3.isFalloutRecordValid(falloutRecord);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                ErrorTO error = new ErrorTO(errorConstant.getERROR_CODE_FALLOUT_INVALID_DATA(), errorConstant.getERROR_MESSAGE_FALLOUT_INVALID_DATA(), errorMessage);
                falloutServiceIntf.updateFallout(session, id, Status.FAILED, StringUtil.toJson(error), 0L);
                return false;
            }
        } catch (AuthException e) {
            falloutServiceIntf.updateFallout(session, id, Status.FAILED, StringUtil.toJson(e), 0L);
            return false;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " validateFalloutRecord : stop");
        }
        return true;
    }

    @Override
    public void processFalloutRecord(Session session, FalloutTO falloutRecord) throws AuthException, IAMException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecord : start");
        IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
        Token token = iamExtensionService.getToken(iamExtension);
        List<AttributeMetadataTO> attributeMetaDataWEs = attributeMasterService.getAllAttributeMetaData();
        // get action specific request
        AttributeDataRequestTO attributeDataRequestTO = getAttributeDataRequestTO(falloutRecord);
        attributeNameToUpperCase(attributeDataRequestTO);

        List<AttributeTO> attributeTOs = new ArrayList<>();
        for (AttributeDataTO attributeDataTO : attributeDataRequestTO.getSearchAttributes()) {
            AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
            attributeTOs.add(tempAttribute);
        }
        //check inconsistency - if found clear it
        AccountWE account = iamExtension.getAccountByAttributes(attributeTOs, token);
        userService.getActiveUser(session, account.getId()); // added check to identify the use is present or not before the processing of data

        switch (falloutRecord.getOperation()) {
            case ADD:
                addAttribute(session, attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token);
                break;
            case UPDATE:
                updateAndTakeOverAttribute(session, attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token);
                break;
            case DELETE:
                deleteAttribute(session, attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token);
                break;
            default:
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_FALLOUT_OPERATION(), errorConstant.getERROR_MESSAGE_INVALID_FALLOUT_OPERATION());
        }

        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processFalloutRecord : end");

    }

    public void updateAndTakeOverAttribute(Session session, AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs, IAMExtensionV2 iamExtension, Token token)
            throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAndTakeOverAttribute : start");
        takeOverAttribute(session, attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token);
        updateAttribute(session, iamExtension, token, attributeDataRequestTO, attributeMetaDataWEs);
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAndTakeOverAttribute : end");
    }

    public void addAttribute(Session session, AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs, IAMExtensionV2 iamExtension, Token token)
            throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttribute : start");
        try {
            takeOverAttribute(session, attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token);
            Map<String, String> attributeValueWithPlainValue = new HashMap<String, String>();
            List<AttributeTO> attributeTOs = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : attributeDataRequestTO.getSearchAttributes()) {
                AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                attributeTOs.add(tempAttribute);
                attributeValueWithPlainValue.put(tempAttribute.getAttributeValue(), attributeDataTO.getAttributeValue());
            }
            AccountWE account = iamExtension.getAccountByAttributes(attributeTOs, token);
            User user = userService.getActiveUser(session, account.getId());

            if (!attributeExistOnIds(account, attributeDataRequestTO.getAttributeData().getAttributeValue())) {
                addAttributeOnIds(attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token, account, attributeValueWithPlainValue);
            }
            if (!attributeExistOnAdaptor(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue(), user.getId())) {
                addAttributeOnAdaptor(session, attributeDataRequestTO, user);
            }
            if (user.getKcId() != null && !user.getKcId().isEmpty() && !attributeExistOnCam(attributeDataRequestTO.getAttributeData().getAttributeValue(), user.getKcId())) {
                addAttributeOnCam(attributeDataRequestTO.getAttributeData(), user);
            }
        } catch (IAMException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttribute : end");
        }

    }

    public void deleteAttribute(Session session, AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs, IAMExtensionV2 iamExtension, Token token)
            throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttribute : start");
        try {
            List<AttributeTO> attributeTOs = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : attributeDataRequestTO.getSearchAttributes()) {
                AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                attributeTOs.add(tempAttribute);
            }
            AccountWE accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty()) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }

            User user = userService.getUserByAccountId(accountWE.getId());

            String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
            AttributeTO attributeTO = new AttributeTO();
            attributeTO.setAttributeValue(attributeValue);
            attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());

            deleteAttributeOnIds(attributeTO, accountWE);
            deleteAttributeOnAdaptor(session, attributeDataRequestTO, attributeValue, user);
            deleteAttributeOnCam(attributeDataRequestTO, attributeValue, user);
        } catch (IAMException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } catch (UserNotFoundException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttribute : end");
        }
    }

    public void addAttributeOnIds(AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs, IAMExtensionV2 iamExtension, Token token, AccountWE account,
                                  Map<String, String> attributeValueWithPlainValue) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnIds : start");
        try {
            List<AttributeTO> attributeDataTOs = new ArrayList<>();
            AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataRequestTO.getAttributeData(), attributeMetaDataWEs, false);
            attributeDataTOs.add(tempAttribute);
            attributeValueWithPlainValue.put(tempAttribute.getAttributeValue(), attributeDataRequestTO.getAttributeData().getAttributeValue());

            account.setAttributes(attributeDataTOs);
            AccountWE accountWE = iamExtension.onboardAccountwithAttribute(account, account.getId(), true, token);
            if (accountWE.getStatus() != null && Status.FAILED.name().equals(accountWE.getStatus())) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
            }
            accountWE.setUserCredential(account.getUserCredential());
            Map<String, Object> attributeValueWithKey = null;
            boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
            try {
                if (enableCrypto) {
                    attributeValueWithKey = registerAttributesOnCrypto(iamExtension, token, attributeValueWithPlainValue, accountWE, account, attributeDataTOs);
                } else {
                    attributeValueWithKey = new HashMap<>();
                }
            } catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                attributeValueWithKey = new HashMap<>();
                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = new GenerateAttributeClaimSelfSignedTO();
                generateAttributeClaimSelfSignedTO.setTransactionID("6c1f17951a9f89df82cf25980f41bcb32720dde11b9698039d31a151091a371f01c0163c034badef");
                generateAttributeClaimSelfSignedTO.setKey("753c2355163b9bb7c71065d0b26427fd");
                for (AttributeTO attributeTO : accountWE.getAttributes()) {
                    String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
                    attributeValueWithKey.put(plainValue, generateAttributeClaimSelfSignedTO);
                }
            }
            updateAttributesWithTransactionId(enableCrypto, accountWE, attributeValueWithPlainValue, attributeValueWithKey, attributeDataRequestTO);
            iamExtension.editAttributes(accountWE.getAttributes(), accountWE.getId(), token);
        } catch (IAMException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnIds : end");
        }
    }

    public void updateAttributesWithTransactionId(boolean enableCrypto, AccountWE accountWE, Map<String, String> attributeValueWithPlainValue, Map<String, Object> attributeValueWithKey,
                                                  AttributeDataRequestTO attributeDataRequestTO) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributesWithTransactionId : start");
        updateAccountAttributes(enableCrypto, accountWE, attributeValueWithPlainValue, attributeValueWithKey);
        updateRequestAttributes(enableCrypto, attributeValueWithKey, attributeDataRequestTO);
        updateRequestSearchAttributes(enableCrypto, attributeValueWithKey, attributeDataRequestTO);
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributesWithTransactionId : end");
    }

    public void updateAccountAttributes(boolean enableCrypto, AccountWE accountWE, Map<String, String> attributeValueWithPlainValue, Map<String, Object> attributeValueWithKey) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAccountAttributes : start");
        for (AttributeTO attributeTO : accountWE.getAttributes()) {
            String plainValue = attributeValueWithPlainValue.get(attributeTO.getAttributeValue());
            if (AttributeOperationStatus.SUCCESSFUL == attributeTO.getOperationStatus()) {
                if (enableCrypto) {
                    if (attributeValueWithKey.containsKey(plainValue)) {
                        GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                                (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(plainValue);
                        attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                        attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                        attributeTO.setOperationStatus(null);
                    }
                } else {
                    attributeTO.setOperationStatus(null);
                }
            } else {
                if (!enableCrypto) {
                    attributeValueWithKey.put(plainValue, attributeTO.getErrorMessage());
                }
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAccountAttributes : end");
    }

    public void updateRequestAttributes(boolean enableCrypto, Map<String, Object> attributeValueWithKey, AttributeDataRequestTO attributeDataRequestTO) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateRequestAttributes : start");
        if (attributeDataRequestTO.getAttributeData() != null) {
            AttributeDataTO attributeDataTOTemp = attributeDataRequestTO.getAttributeData();
            if (enableCrypto) {
                if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                    GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO = (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue());
                    attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                } else {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                    attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                }
            } else {
                if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                    attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                } else {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                }
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateRequestAttributes : end");
    }

    public void updateRequestSearchAttributes(boolean enableCrypto, Map<String, Object> attributeValueWithKey, AttributeDataRequestTO attributeDataRequestTO) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateRequestSearchAttributes : start");
        for (AttributeDataTO attributeDataTOTemp : attributeDataRequestTO.getSearchAttributes()) {
            if (enableCrypto) {
                if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                    if (attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()) instanceof GenerateAttributeClaimSelfSignedTO) {
                        GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                                (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey
                                        .get(attributeDataTOTemp.getAttributeValue());
                        attributeDataTOTemp.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                    } else {
                        attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                        attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                    }
                }
            } else {
                if (attributeValueWithKey.containsKey(attributeDataTOTemp.getAttributeValue())) {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.FAILED.toString());
                    attributeDataTOTemp.setErrorMessage((String) attributeValueWithKey.get(attributeDataTOTemp.getAttributeValue()));
                } else {
                    attributeDataTOTemp.setStatus(AttributeOperationStatus.SUCCESSFUL.toString());
                }
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateRequestSearchAttributes : end");
    }

    public Map<String, Object> registerAttributesOnCrypto(IAMExtensionV2 iamExtension, Token token, Map<String, String> attributeValueWithPlainValue, AccountWE accountWE, AccountWE account,
                                                          List<AttributeTO> attributeDataTOs) throws IAMException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " registerAttributesOnCrypto : start");
        AccountWE accountWE1 = new AccountWE();
        accountWE1.setAttributes(attributeDataTOs);
        accountWE1.setCryptoDID(accountWE.getCryptoDID());
        accountWE1.setUserCredential(account.getUserCredential());
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " registerAttributesOnCrypto : end");
        return iamExtension.registerAttributesOnCrypto(accountWE1, attributeValueWithPlainValue, token, reqRefNum);
    }

    public void addAttributeOnAdaptor(Session session, AttributeDataRequestTO attributeDataRequestTO, User user) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnAdaptor : start");
        session.clear();
        session.beginTransaction();
        try {
            if (attributeDataRequestTO.getAttributeData() != null) {
                attributeStoreService.saveAttributeData(session, attributeDataRequestTO.getAttributeData(), user, false);
            }
            session.getTransaction().commit();
        } catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnAdaptor : end");
        }
    }

    public void addAttributeOnAdapter(Session session, AttributeTO actualMobileNoAttributeTO, User otherUser) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnAdaptor : start");
        try {
            AttributeDataRequestTO attributeDataRequestTO = new AttributeDataRequestTO();
            AttributeDataTO attributeData = new AttributeDataTO();
            attributeData.setAttributeName(actualMobileNoAttributeTO.getAttributeName());
            attributeData.setAttributeValue(actualMobileNoAttributeTO.getAttributeValue());
            if (actualMobileNoAttributeTO.getIsDefault() != null) {
                attributeData.setIsDefault(actualMobileNoAttributeTO.getIsDefault());
            }
            attributeDataRequestTO.setAttributeData(attributeData);
            List<AttributeStore> attributeStores = DaoFactory.getAttributeStoreDao().getUserActiveAttributes(otherUser.getId());
            //List<AttributeStore> attributeStores = new ArrayList<>(otherUser.getAttributeStores());
            if (attributeDataRequestTO.getAttributeData() != null) {
                attributeStoreService.saveAttributeData(session, attributeDataRequestTO.getAttributeData(), otherUser, false);
            }
            otherUser.setAttributeStores(attributeStores);
            userService.updateUser(session, otherUser);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnAdaptor : end");
        }
    }

    public void addAttributeOnCam(AttributeDataTO attributeData, User user) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnCam : start");
        try {
            EditUserRequest editUserRequest = new EditUserRequest();
            editUserRequest.setUserKcId(user.getKcId());
            List<CamAttribute> camAttributes = new ArrayList<>();
            CamAttribute camAttribute = new CamAttribute(attributeData.getAttributeName(), attributeData.getAttributeValue());
            camAttributes.add(camAttribute);
            editUserRequest.setAttributes(camAttributes);
            boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
            if (!camStatus) {
                logger.log(Level.ERROR, FALLOUT_FACDE_IMPL_LOG + " CAM Edit user failed for ADD Attribute.");
            }
        } catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnCam : end");
        }
    }

    public void attributeAddOnCAM(AttributeTO actualMobileNoAttributeTO, User currentUser) throws AuthException {
        int retryCount = config.getProperty(Constant.FALLOUT_PROCESS_RETRY_COUNT) != null ? Integer.parseInt(config.getProperty(Constant.FALLOUT_PROCESS_RETRY_COUNT)) : 5;
        while (retryCount > 0) {
            try {
                addAttributeOnCam(actualMobileNoAttributeTO, currentUser);
                break;
            } catch (CAMUnknownException e) {
                if (--retryCount == 0) {
                    logger.log(Level.WARN, "Failed to Add attribute : " + Constant.MOBILE_NO + " - " + actualMobileNoAttributeTO.getAttributeValue() + "for account : " + currentUser.getAccountId() + " from CAM ");
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
        }
    }

    public void addAttributeOnCam(AttributeTO actualMobileNoAttributeTO, User currentUser) throws AuthException, CAMUnknownException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnCam : start");
        try {
            EditUserRequest editUserRequest = new EditUserRequest();
            editUserRequest.setUserKcId(currentUser.getKcId());
            List<CamAttribute> camAttributes = new ArrayList<>();
            CamAttribute camAttribute = new CamAttribute(actualMobileNoAttributeTO.getAttributeName(), actualMobileNoAttributeTO.getAttributeValue());
            camAttributes.add(camAttribute);
            editUserRequest.setAttributes(camAttributes);
            boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
            if (!camStatus) {
                logger.log(Level.ERROR, FALLOUT_FACDE_IMPL_LOG + " CAM Edit user failed for ADD Attribute.");
            }
        } catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new CAMUnknownException(e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " addAttributeOnCam : end");
        }
    }

    public void takeOverAttribute(Session session, AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs, IAMExtensionV2 iamExtension, Token token)
            throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " takeOverAttribute : start");
        try {
            AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
            attributeMetadataTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
            int index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
            attributeMetadataTO = attributeMetaDataWEs.get(index);
            //add else condition and add proper logs into it
            // proper names for variables
            if (attributeMetadataTO.getIsUnique() != null && Boolean.TRUE.equals(attributeMetadataTO.getIsUnique())) {
                String attributeValue = attributeDataRequestTO.getAttributeData().getAttributeValue();
                String attributeName = attributeDataRequestTO.getAttributeData().getAttributeName();
                AttributeTO attributeTO = new AttributeTO();
                attributeTO.setAttributeValue(attributeValue);
                attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
                List<AttributeTO> attributeTOs = new ArrayList<>();
                AttributeDataTO attributeDataTO = attributeDataRequestTO.getAttributeData();
                AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                attributeTOs.add(tempAttribute);
                AccountWE accountWE = iamExtension.getAccountByAttributes(attributeTOs, token);
                User user = null;
                if (accountWE != null && accountWE.getId() != null && !accountWE.getId().isEmpty()) {
                    deleteForTakeOverAttributeOnIds(attributeTO, accountWE);
                    user = userService.getUserByAccountId(accountWE.getId());
                } else {
                    AttributeStore attributeStore = ServiceFactory.getAttributeStoreService().getAttributeByAttributeNameAndValue(attributeName, attributeValue);
                    user = attributeStore.getUser();
                }
                if (user != null) {
                    deleteForTakeOverAttributeOnCam(user, attributeName, attributeValue);
                    deleteForTakeOverAttributeOnAdaptor(session, user, attributeName, attributeValue);
                }
            }
        } catch (IAMException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (AuthException | UserNotFoundException e) {
            logger.log(Level.WARN, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " takeOverAttribute : end");
        }
    }

    public void deleteForTakeOverAttributeOnIds(AttributeTO attributeTO, AccountWE accountWE) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteForTakeOverAttributeOnIds : start");
        try {
            iamExtensionService.deleteAttribute(attributeTO, accountWE.getId(), accountWE.getCryptoDID());
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteForTakeOverAttributeOnIds : end");
        }
    }

    public void deleteForTakeOverAttributeOnAdaptor(Session session, User user, String attributeName, String attributeValue) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteForTakeOverAttributeOnAdaptor : start");
        session.clear();
        session.beginTransaction();
        try {
            AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName, attributeValue, user.getId());
            attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
            attributeStoreService.update(session, attributeTobeUpdate);
            session.getTransaction().commit();
        } catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.WARN, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteForTakeOverAttributeOnAdaptor : end");
        }
    }

    public void deleteForTakeOverAttributeOnCam(User user, String attributeName, String attributeValue) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteForTakeOverAttributeOnCam : start");
        try {
            if (user.getKcId() != null && !user.getKcId().isEmpty()) {
                UserRepresentation userRepresentation = camAdminService.getUserDetails(config.getProperty(Constant.CAM_REALM), user.getKcId());
                List<String> camAttributes = userRepresentation.getAttributes().get(attributeName);
                for (String number : camAttributes) {
                    if (number.equals(attributeValue)) {
                        EditUserRequest editUserRequest = new EditUserRequest();
                        List<CamAttribute> camAttributeList = new ArrayList<>();
                        CamAttribute camAttribute = new CamAttribute(attributeName, attributeValue);
                        camAttributeList.add(camAttribute);
                        editUserRequest.setUserKcId(user.getKcId());
                        editUserRequest.setAttributeAction(AttributeAction.DELETE);
                        editUserRequest.setAttributes(camAttributeList);
                        boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                        if (!camStatus) {
                            logger.log(Level.ERROR, FALLOUT_FACDE_IMPL_LOG + " CAM Edit user failed for DELETE Attribute.");
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteForTakeOverAttributeOnCam : end");
        }
    }

    public void updateAttribute(Session session, IAMExtensionV2 iamExtension, Token token, AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs)
            throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttribute : start");
        try {
            List<AttributeTO> attributeTOs = new ArrayList<>();
            for (AttributeDataTO attributeDataTO : attributeDataRequestTO.getSearchAttributes()) {
                AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                attributeTOs.add(tempAttribute);
            }
            AccountWE account = iamExtension.getAccountByAttributes(attributeTOs, token);
            User user = userService.getActiveUser(session, account.getId());

            updateAttributeOnIds(iamExtension, token, user, account, attributeDataRequestTO, attributeMetaDataWEs);
            updateAttributeOnAdaptor(user, attributeDataRequestTO, attributeMetaDataWEs, session);
            updateAttributeOnCam(user, attributeDataRequestTO);

        } catch (IAMException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttribute : end");
        }
    }

    public void updateAttributeOnIds(IAMExtensionV2 iamExtension, Token token, User user, AccountWE accountWE, AttributeDataRequestTO attributeDataRequestTO,
                                     List<AttributeMetadataTO> attributeMetaDataWEs) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributeOnIds : start");

        if (!attributeExistOnIds(accountWE, attributeDataRequestTO.getAttributeData().getAttributeValue())) {

            if (oldAttributeExistOnIds(accountWE, attributeDataRequestTO.getAttributeData().getOldattributeValue())) {
                Map<String, String> attributeValueWithPlainValue = new HashMap<String, String>();
                List<AttributeTO> attributeTOs = new ArrayList<>();
                for (AttributeDataTO attributeDataTO : attributeDataRequestTO.getSearchAttributes()) {
                    AttributeTO tempAttribute = getAttributeFromAttributeData(attributeDataTO, attributeMetaDataWEs, false);
                    attributeTOs.add(tempAttribute);
                    attributeValueWithPlainValue.put(tempAttribute.getAttributeValue(), attributeDataTO.getAttributeValue());
                }
                addAttributeOnIds(attributeDataRequestTO, attributeMetaDataWEs, iamExtension, token, accountWE, attributeValueWithPlainValue);
            } else {
                try {
                    editAttributeOnIds(accountWE, user, attributeDataRequestTO, iamExtension, token, attributeMetaDataWEs);
                } catch (AuthException e) {
                    logger.log(Level.WARN, e.getMessage(), e);
                    throw e;
                }
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributeOnIds : end");
    }

    public boolean attributeExistOnIds(AccountWE accountWE, String attributeValue) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnIds : start");
        List<AttributeTO> attributeTOList = accountWE.getAttributes();
        boolean isExist = false;
        for (AttributeTO attribute : attributeTOList) {
            if (attribute.getAttributeName().equals(Constant.MOBILE_NO) && attribute.getAttributeValue().equals(attributeValue) && attribute.getStatus().equals(Constant.ACTIVE)) {
                isExist = true;
                break;
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnIds : end");
        return isExist;
    }

    public boolean oldAttributeExistOnIds(AccountWE accountWE, String oldAttributeValue) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " oldAttributeExistOnIds : start");
        List<AttributeTO> attributeTOList = accountWE.getAttributes();
        boolean isExistWithDeleteStatus = false;
        boolean isExistWithActiveStatus = false;
        for (AttributeTO attribute : attributeTOList) {
            if (attribute.getAttributeName().equals(Constant.MOBILE_NO) && attribute.getAttributeValue().equals(oldAttributeValue) && attribute.getStatus().equals(Constant.DELETE)) {
                isExistWithDeleteStatus = true;
                //break;
            }
            if (attribute.getAttributeName().equals(Constant.MOBILE_NO) && attribute.getAttributeValue().equals(oldAttributeValue) && attribute.getStatus().equals(Constant.ACTIVE)) {
                isExistWithActiveStatus = true;
                break;
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " oldAttributeExistOnIds : end");
        if (isExistWithActiveStatus) {
            return false;
        }
        return isExistWithDeleteStatus;
    }

    public boolean oldAttributeExistOnAdaptor(String attributeName, String oldAttributeValue, Long id) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnAdaptor : start");
        boolean isExist = false;
        try {
            AttributeStore attributeStore = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName, oldAttributeValue, id);
            if (attributeStore != null) {
                isExist = true;
            }
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnAdaptor : end");
        return isExist;
    }

    public void updateAttributeOnAdaptor(User user, AttributeDataRequestTO attributeDataRequestTO, List<AttributeMetadataTO> attributeMetaDataWEs, Session session) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributeOnAdaptor : start");
        if (attributeExistOnAdaptor(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue(), user.getId())) {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributeOnAdaptor : end");
            return;
        }
        if (oldAttributeExistOnAdaptor(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getOldattributeValue(), user.getId())) {
            session.clear();
            session.beginTransaction();
            try {
                AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
                attributeMetadataTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
                int index = attributeMetaDataWEs.indexOf(attributeMetadataTO);
                attributeMetadataTO = attributeMetaDataWEs.get(index);
                attributeDataRequestTO.getAttributeData().setIsUnique(attributeMetadataTO.getIsUnique());
                String attributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
                try {
                    AttributeStore oldAttribute =
                            attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue,
                                    user.getId());
                    if (oldAttribute.getIsDefault() != null && oldAttribute.getIsDefault().equals(Boolean.TRUE)) {
                        attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.TRUE);
                    } else {
                        attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.FALSE);
                    }
                } catch (AuthException e) {
                    logger.log(Level.WARN, e);
                }
                attributeStoreService.saveAttributeData(session, attributeDataRequestTO.getAttributeData(), null, user, false);
                AttributeStore attributeTobeUpdate = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue,
                        user.getId());
                attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
                attributeStoreService.update(session, attributeTobeUpdate);
                session.getTransaction().commit();
            } catch (AuthException e) {
                session.getTransaction().rollback();
                logger.log(Level.WARN, e.getMessage(), e);
                throw e;
            } finally {
                logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributeOnAdaptor : end");
            }
        } else {
            addAttributeOnAdaptor(session, attributeDataRequestTO, user);
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributeOnAdaptor : end");
        }
    }

    public boolean attributeExistOnAdaptor(String attributeName, String attributeValue, Long id) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnAdaptor : start");
        boolean isExist = false;
        try {
            AttributeStore attributeStore = attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeName, attributeValue, id);
            if (attributeStore != null) {
                isExist = true;
            }
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnAdaptor : end");
        return isExist;
    }

    public void editAttributeOnIds(AccountWE accountWE, User user, AttributeDataRequestTO attributeDataRequestTO, IAMExtensionV2 iamExtension, Token token,
                                   List<AttributeMetadataTO> attributeMetaDatas) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " editAttributeOnIds : start");
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        int index = attributeMetaDatas.indexOf(attributeMetadataTO);
        attributeMetadataTO = attributeMetaDatas.get(index);
        attributeDataRequestTO.getAttributeData().setIsUnique(attributeMetadataTO.getIsUnique());
        String attributeValue = attributeDataRequestTO.getAttributeData().getOldattributeValue();
        try {
            AttributeStore oldAttribute =
                    attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue, user.getId());
            if (oldAttribute.getIsDefault() != null && oldAttribute.getIsDefault().equals(Boolean.TRUE)) {
                attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.TRUE);
            } else {
                attributeDataRequestTO.getAttributeData().setIsDefault(Boolean.FALSE);
            }
        } catch (AuthException e) {
            logger.log(Level.WARN, e);
        }
        AttributeTO attributeTO = new AttributeTO();
        attributeTO.setUpdatedAttributeValue(attributeDataRequestTO.getAttributeData().getAttributeValue());
        attributeTO.setAttributeValue(attributeValue);
        attributeTO.setAttributeName(attributeDataRequestTO.getAttributeData().getAttributeName());
        attributeTO.setIsDefault(attributeDataRequestTO.getAttributeData().getIsDefault());
        boolean enableCrypto = config.getProperty(Constant.IS_CRYPTO_ENABLED) != null && Boolean.parseBoolean(config.getProperty(Constant.IS_CRYPTO_ENABLED));
        if (enableCrypto) {
            if (accountWE.getCryptoDID() == null || accountWE.getCryptoDID().isEmpty()) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_EDIT_ACCOUNT_FAILED(), "Account not onboarded on crypto.");
            }
            editAttributeOnCrypto(iamExtension, token, accountWE, attributeTO);
        }
        try {
            iamExtensionService.editAttribute(attributeTO, accountWE.getId());
        } catch (AuthException e) {
            logger.log(Level.WARN, e);
            throw e;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " editAttributeOnIds : end");
        }
    }

    public void editAttributeOnCrypto(IAMExtensionV2 iamExtension, Token token, AccountWE accountWE, AttributeTO attributeTO) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " editAttributeOnCrypto : start");
        try {
            Map<String, Object> attributeValueWithKey = null;
            Map<String, String> attributeValueWithPlainValue = new HashMap<>();
            attributeValueWithPlainValue.put(attributeTO.getUpdatedAttributeValue(), attributeTO.getUpdatedAttributeValue());
            AccountWE updatedAttributeAccount = new AccountWE();
            updatedAttributeAccount.setId(accountWE.getId());
            AttributeTO updatedAttribute = new AttributeTO();
            updatedAttribute.setAttributeName(attributeTO.getAttributeName());
            updatedAttribute.setAttributeValue(attributeTO.getUpdatedAttributeValue().toUpperCase());
            updatedAttribute.setIsDefault(attributeTO.getIsDefault());
            List<AttributeTO> updatedAttributeList = new ArrayList<>();
            updatedAttributeList.add(updatedAttribute);
            updatedAttributeAccount.setAttributes(updatedAttributeList);
            updatedAttributeAccount.setCryptoDID(accountWE.getCryptoDID());
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            attributeValueWithKey = iamExtension.registerAttributesOnCrypto(updatedAttributeAccount, attributeValueWithPlainValue, token, reqRefNum);
            if (attributeValueWithKey.containsKey(updatedAttribute.getAttributeValue())) {
                GenerateAttributeClaimSelfSignedTO generateAttributeClaimSelfSignedTO =
                        (GenerateAttributeClaimSelfSignedTO) attributeValueWithKey.get(updatedAttribute.getAttributeValue());
                attributeTO.setSignTransactionId(generateAttributeClaimSelfSignedTO.getTransactionID());
                attributeTO.setEncryptedAttributeValue(AES128Impl.encryptData(attributeTO.getAttributeValue(), generateAttributeClaimSelfSignedTO.getKey()));
                attributeTO.setOperationStatus(null);
            }
        } catch (IAMException e) {
            logger.log(Level.ERROR, e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " editAttributeOnCrypto : end");
        }
    }

    public void updateAttributeOnCam(User user, AttributeDataRequestTO attributeDataRequestTO) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributeOnCam : start");
        if (user.getKcId() != null && !user.getKcId().isEmpty()
                && attributeDataRequestTO.getAttributeData().getOldattributeValue() != null
                &&
                !attributeExistOnCam(attributeDataRequestTO.getAttributeData().getAttributeValue(), user.getKcId())) {
            if (oldAttributeExistOnCam(attributeDataRequestTO.getAttributeData().getOldattributeValue(), user.getKcId())) {
                editCamUsersAttribute(AttributeAction.UPDATE, attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue(), user);
                editCamUsersAttribute(AttributeAction.DELETE, attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getOldattributeValue(), user);
            } else {
                addAttributeOnCam(attributeDataRequestTO.getAttributeData(), user);
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " updateAttributeOnCam : end");
    }

    public boolean attributeExistOnCam(String attributeValue, String kcId) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnCam : start");
        try {
            boolean isExist = false;
            UserRepresentation userRepresentation = camAdminService.getUserDetails(config.getProperty(Constant.CAM_REALM), kcId);
            List<String> camAttributes = userRepresentation.getAttributes().get(Constant.MOBILE_NO);
            if (camAttributes == null || camAttributes.isEmpty()) {
                return isExist;
            }
            for (String number : camAttributes) {
                if (number.equals(attributeValue)) {
                    isExist = true;
                    break;
                }
            }
            return isExist;
        } catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnCam : end");
        }
    }

    public boolean oldAttributeExistOnCam(String oldAttributeValue, String kcId) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnCam : start");
        try {
            boolean isExist = false;
            UserRepresentation userRepresentation = camAdminService.getUserDetails(config.getProperty(Constant.CAM_REALM), kcId);
            List<String> camAttributes = userRepresentation.getAttributes().get(Constant.MOBILE_NO);
            if (camAttributes == null || camAttributes.isEmpty()) {
                return isExist;
            }
            for (String number : camAttributes) {
                if (number.equals(oldAttributeValue)) {
                    isExist = true;
                    break;
                }
            }
            return isExist;
        } catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeExistOnCam : end");
        }
    }

    public void deleteAttributeOnIds(AttributeTO attributeTO, AccountWE accountWE) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeOnIds : start");
        if (!attributeExistOnIds(accountWE, attributeTO.getAttributeValue())) {
            return;
        }
        try {
            iamExtensionService.deleteAttribute(attributeTO, accountWE.getId(), accountWE.getCryptoDID());
        } catch (AuthException e) {
            logger.log(Level.WARN, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeOnIds : end");
        }
    }

    public void deleteAttributeOnAdaptor(Session session, AttributeDataRequestTO attributeDataRequestTO, String attributeValue, User user) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeOnAdaptor : start");

        if (!attributeExistOnAdaptor(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeDataRequestTO.getAttributeData().getAttributeValue(), user.getId())) {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeOnAdaptor : end");
            return;
        }
        session.beginTransaction();
        try {
            AttributeStore attributeTobeUpdate =
                    attributeStoreService.getAttributeByAttributeNameAndValueAndUserId(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue,
                            user.getId());
            attributeTobeUpdate.setAttributeState(AttributeState.DELETE);
            attributeStoreService.update(session, attributeTobeUpdate);
            session.getTransaction().commit();
        } catch (AuthException e) {
            session.getTransaction().rollback();
            logger.log(Level.WARN, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeOnAdaptor : end");
        }
    }

    public void deleteAttributeOnCam(AttributeDataRequestTO attributeDataRequestTO, String attributeValue, User user) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeOnCam : start");
        try {
            if (user.getKcId() != null && !user.getKcId().isEmpty()) {
                if (!attributeExistOnCam(attributeDataRequestTO.getAttributeData().getAttributeValue(), user.getKcId())) {
                    return;
                }
                EditUserRequest editUserRequest = new EditUserRequest();
                List<CamAttribute> camAttributeList = new ArrayList<>();
                CamAttribute camAttribute = new CamAttribute(attributeDataRequestTO.getAttributeData().getAttributeName(), attributeValue);
                camAttributeList.add(camAttribute);
                editUserRequest.setUserKcId(user.getKcId());
                editUserRequest.setAttributeAction(AttributeAction.DELETE);
                editUserRequest.setAttributes(camAttributeList);
                boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
                if (!camStatus) {
                    logger.log(Level.ERROR, FALLOUT_FACDE_IMPL_LOG + " CAM Edit user failed for DELETE Attribute.");
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " deleteAttributeOnCam : end");
        }
    }

    private void editCamUsersAttribute(AttributeAction action, String attributeName, String attributeValue, User user) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " editCamUsersAttribute : start");
        try {
            EditUserRequest editUserRequest = new EditUserRequest();
            List<CamAttribute> camAttributeList = new ArrayList<>();
            CamAttribute camAttribute = new CamAttribute(attributeName, attributeValue);
            camAttributeList.add(camAttribute);
            editUserRequest.setUserKcId(user.getKcId());
            editUserRequest.setAttributes(camAttributeList);
            if (AttributeAction.DELETE.equals(action)) {
                editUserRequest.setAttributeAction(AttributeAction.DELETE);
            }
            boolean camStatus = camAdminService.editUser(Config.getInstance().getProperty(Constant.CAM_REALM), editUserRequest);
            if (!camStatus) {
                logger.log(Level.ERROR, FALLOUT_FACDE_IMPL_LOG + " CAM Edit user failed for EDIT Attribute.");
            }
        } catch (Exception e) {
            logger.log(Level.WARN, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_REQUEST_IS_TIMEOUT(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " editCamUsersAttribute : end");
        }
    }

    private AttributeDataRequestTO getAttributeDataRequestTO(FalloutTO falloutRecord) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getAttributeDataRequestTO : start");
        AttributeDataTO searchAttribute = new AttributeDataTO();
        searchAttribute.setAttributeName(Constant.USER_ID);
        searchAttribute.setAttributeValue(falloutRecord.getFt42UserId());

        List<AttributeDataTO> searchAttributeList = new ArrayList<>();
        searchAttributeList.add(searchAttribute);
        AttributeDataTO attributeData = new AttributeDataTO();
        attributeData.setAttributeName(Constant.MOBILE_NO);
        attributeData.setAttributeValue(falloutRecord.getNewMobileNo());
        attributeData.setOldattributeValue(falloutRecord.getOldMobileNo());

        AttributeDataRequestTO attributeDataRequestTO = new AttributeDataRequestTO();
        attributeDataRequestTO.setSearchAttributes(searchAttributeList);
        attributeDataRequestTO.setAttributeData(attributeData);
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getAttributeDataRequestTO : end");
        return attributeDataRequestTO;
    }

    private AttributeTO getAttributeFromAttributeData(AttributeDataTO attributeDataTO, List<AttributeMetadataTO> attributeMetaDataTOs, boolean isEncrypted) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getAttributeFromAttributeData : start");
        AttributeMetadataTO attributeMetadataTO = new AttributeMetadataTO();
        attributeMetadataTO.setAttributeName(attributeDataTO.getAttributeName());
        int index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        if (index < 0) {
            attributeMetadataTO.setAttributeName(Constant.OTHERS);
            index = attributeMetaDataTOs.indexOf(attributeMetadataTO);
        }
        attributeMetadataTO = attributeMetaDataTOs.get(index);
        String securityType = attributeMetadataTO.getAttributeStoreSecurityPolicy();
        String maskPattern = (String) attributeMetadataTO.getAttributeSettings().get(Constant.MASK_PATTERN);
        AttributeTO attribute = new AttributeTO();
        attribute.setAttributeName(attributeDataTO.getAttributeName());
        attribute.setIsDefault(attributeDataTO.getIsDefault());
        String decryptedAttributeValue = attributeDataTO.getAttributeValue();
        if (isEncrypted) {
            try {
                decryptedAttributeValue = CryptoJS.decryptData(config.getProperty(Constant.AD_ENCRYPTION_KEY), attributeDataTO.getAttributeValue());
            } catch (Exception e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_APPLICATION_PASSWORD(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_PASSWORD());
            }
        }
        if (maskPattern != null) {
            //attribute.setMaskAttributeValue(decryptedAttributeValue.replaceAll(config.getProperty(Constant.MASK_PATTERN), config.getProperty(Constant.MASK_CHARACTER)));
        }
        if (attributeMetadataTO.getIsUnique() != null) {
            attribute.setIsUnique(attributeMetadataTO.getIsUnique());
            attributeDataTO.setIsUnique(attributeMetadataTO.getIsUnique());
        }
        attribute.setAttributeValue(applySecurityPolicy(decryptedAttributeValue, AttributeSecurityType.valueOf(securityType)));
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getAttributeFromAttributeData : end");
        return attribute;
    }

    private void attributeNameToUpperCase(AttributeDataRequestTO attributeDataRequest) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeNameToUpperCase : start");
        AttributeDataTO attributeDataTO = attributeDataRequest.getAttributeData();
        if (attributeDataTO.getAttributeName() != null) {
            attributeDataTO.setAttributeName(attributeDataTO.getAttributeName().toUpperCase());
        }
        if (attributeDataRequest.getSearchAttributes() != null || !attributeDataRequest.getSearchAttributes().isEmpty()) {
            for (AttributeDataTO attributeData : attributeDataRequest.getSearchAttributes()) {
                attributeData.setAttributeName(attributeData.getAttributeName().toUpperCase());
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " attributeNameToUpperCase : end");
    }

    private String applySecurityPolicy(String attributeValue, AttributeSecurityType attributeSecurityType) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " applySecurityPolicy : start");
        String hashedAttributeValue;
        if (attributeSecurityType == AttributeSecurityType.SHA512) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData512(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        } else if (attributeSecurityType == AttributeSecurityType.SHA256) {
            hashedAttributeValue = StringUtil.getHex(SHAImpl.hashData256(IAMConstants.SALT + attributeValue.toLowerCase()).getBytes());
        } else {
            hashedAttributeValue = attributeValue;
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " applySecurityPolicy : end");
        return hashedAttributeValue.toUpperCase();
    }

    private void manageAttributeIsDefault(Session session, AttributeTO actualMobileNoAttributeTO, User user) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " manageAttributeIsDefault : start");
        boolean isMobileNumberPresent = false;
        List<AttributeStore> attributeStores = attributeStoreService.getAttributeByUserIdAndState(session, AttributeState.ACTIVE, user.getId());
        for (AttributeStore attributeTO : attributeStores) {
            if (attributeTO.getAttributeName().equalsIgnoreCase(Constant.MOBILE_NO) && attributeTO.getAttributeState().equals(Constant.ACTIVE) && attributeTO.getIsDefault()) {
                isMobileNumberPresent = true;
                break;
            }
        }
        if (!isMobileNumberPresent) {
            actualMobileNoAttributeTO.setIsDefault(true);
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " manageAttributeIsDefault : end");
    }

    private void processDeleteOperationForNewMobileNumber(IAMExtensionV2 iamExtension, Token token, AccountWE account, User user, String newMobileNumber, String operation, FalloutTO falloutRecord, Session session,
                                                          List<AttributeTO> searchAttributeTOs, String actualMobileNumber) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processDeleteOperationForNewMobileNumber : start");
        if (Constant.DELETE.equals(operation)) {
            //check if actualMobileNumber and new mobile number is same
            //if not - check new mobile number present for current user.
            // if yes - delete mobile number.
            if (newMobileNumber != null && !newMobileNumber.equals(actualMobileNumber)) {
                searchAttributeTOs.clear();
                AttributeTO searchNewMobileNoAttributeTO = new AttributeTO();
                searchNewMobileNoAttributeTO.setAttributeName(Constant.MOBILE_NO);
                searchNewMobileNoAttributeTO.setAttributeValue(newMobileNumber.toUpperCase());
                searchAttributeTOs.add(searchNewMobileNoAttributeTO);
                AccountWE accountByNewMobileNumber = iamExtension.getAccountByAttributes(searchAttributeTOs, token);
                if (accountByNewMobileNumber != null && accountByNewMobileNumber.getId() != null && !accountByNewMobileNumber.getId().isEmpty()) {
                    if (account.getId().equals(accountByNewMobileNumber.getId())) {
                        //new mobile number TO
                        AttributeTO newMobileNumberTO = new AttributeTO();
                        newMobileNumberTO.setAttributeName(Constant.MOBILE_NO);
                        newMobileNumberTO.setAttributeValue(newMobileNumber);
                        deleteAttribute(account, user, newMobileNumberTO, falloutRecord, session);
                    }
                    //nothing to do
                }
            }
            //nothing to do
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " processDeleteOperationForNewMobileNumber : end");
    }

    @Override
    public PaginatedTO<FalloutConfigTO> getFalloutconfigs(int pageNo) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getFalloutconfigs : start");
        List<FalloutConfigTO> falloutConfigTOs = ServiceFactory.getFalloutConfigService().getConfigs();
        PaginatedTO<FalloutConfigTO> paginatedTO = new PaginatedTO<FalloutConfigTO>();
        paginatedTO.setList(falloutConfigTOs);
        paginatedTO.setTotalCount((long) falloutConfigTOs.size());
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getFalloutconfigs : end");
        return paginatedTO;
    }
    @Override
    public PaginatedTO<FalloutSyncDataTo> getAllFalloutSyncData(int pageNo) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getFalloutconfigs : start");
        List<FalloutSyncDataTo> FalloutSyncDataToS = falloutServiceIntf.getFalloutSyncData();
        PaginatedTO<FalloutSyncDataTo> paginatedTO = new PaginatedTO<FalloutSyncDataTo>();
        paginatedTO.setList(FalloutSyncDataToS);
        paginatedTO.setTotalCount((long) FalloutSyncDataToS.size());
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getFalloutconfigs : end");
        return paginatedTO;
    }

    @Override
    public FalloutConfigTO editFalloutConfig(String role, String actor,Long id, boolean saveRequest, FalloutConfigTO falloutConfigTO) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " editFalloutConfig : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            FalloutConfigTO falloutConfig = ServiceFactory.getFalloutConfigService().getConfigById(falloutConfigTO.getId());
            isExistingAndUpdatedDataSame(falloutConfigTO, falloutConfig);
            falloutConfigTO = ServiceFactory.getRequestService().createFalloutConfigEditRequest(session,falloutConfigTO, actor,id, saveRequest);
            sessionFactoryUtil.closeSession(session);
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " editFalloutConfig : end");
        return falloutConfigTO;
    }

    @Override
    public void approveEditRequest(Session session, FalloutConfigTO falloutConfigTO, String role, String actor) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " approveEditRequest : start");
        ServiceFactory.getFalloutConfigService().editFalloutConfig(session, falloutConfigTO, role, actor);
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " approveEditRequest : end");
    }

    private void isExistingAndUpdatedDataSame(FalloutConfigTO requestData, FalloutConfigTO data) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " isExistingandUpdatedDataSame : start");
        boolean isSyncUpdated = requestData.getDehFalloutDataSync().equals(data.getDehFalloutDataSync());
        boolean isProcessUpdated = requestData.getDehFalloutDataProcess().equals(data.getDehFalloutDataProcess());
        boolean isNumberOfRecordsUpdated = requestData.getNumberOfRecordsToBeProcessed().equals(data.getNumberOfRecordsToBeProcessed());
        boolean isFrequencyUpdated = requestData.getDataFetchFrequency().equals(data.getDataFetchFrequency());
        boolean isSchedulerFrequencyUpdated = true;
        if (requestData.getSchedulerFrequency() != null) {
            isSchedulerFrequencyUpdated = requestData.getSchedulerFrequency().equals(data.getSchedulerFrequency());
        } else {
            if (data.getSchedulerFrequency() != null) {
                isSchedulerFrequencyUpdated = false;
            }
        }
        if (isSyncUpdated && isProcessUpdated && isNumberOfRecordsUpdated && isFrequencyUpdated && isSchedulerFrequencyUpdated) {
            logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " isExistingandUpdatedDataSame : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " isExistingandUpdatedDataSame : end");
    }

    @Override
    public PaginatedTO<FalloutTO> getAllFalloutData(int page, int pageSize, String attributeName, String searchText, String operations ,String status,Long fromDate,Long toDate) {
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getAllFalloutData : start");
        PaginatedTO<FalloutTO> paginatedTO = new PaginatedTO<>();
        try {
            List<FalloutTO> falloutTOList = falloutServiceIntf.getAllFalloutDataList(page, pageSize, attributeName, searchText,operations,status,fromDate,toDate);
            Long count = falloutServiceIntf.getAllFalloutDataCount(attributeName, searchText,operations,status,fromDate,toDate);
            paginatedTO.setList(falloutTOList);
            paginatedTO.setTotalCount(count);
        } catch (AttributeNotFoundException e){
            logger.log(Level.ERROR, e.getMessage(), e);
            paginatedTO.setList(new ArrayList<>());
            paginatedTO.setTotalCount(0l);
        }
        logger.log(Level.DEBUG, FALLOUT_FACDE_IMPL_LOG + " getAllFalloutData : end");
        return paginatedTO;
    }

}
