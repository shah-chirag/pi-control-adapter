package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.*;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.CsvFileData;
import in.fortytwo42.entities.bean.Fallout;
import in.fortytwo42.entities.bean.FalloutSyncData;
import in.fortytwo42.tos.enums.BulkUploadType;
import in.fortytwo42.tos.enums.Status;
import in.fortytwo42.tos.transferobj.CsvFileDataTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;
import in.fortytwo42.tos.transferobj.FalloutTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

public class FalloutServiceImpl implements FalloutServiceIntf {
    private static final class InstanceHolder {
        private static final FalloutServiceImpl INSTANCE = new FalloutServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static FalloutServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final String FALLOUT_SERVICE_IMPL = "<<<<< FalloutServiceImpl";
    private static Logger logger = LogManager.getLogger(FalloutServiceImpl.class);
    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();
    FalloutSyncDataDaoIntf falloutSyncDataDao = FalloutSyncDataDaoImpl.getInstance();

    private FalloutDaoIntf falloutDao = DaoFactory.getFalloutDao();

    private CsvFileDataDaoIntf csvFileDataDao = DaoFactory.getCsvFileDataDao();

    public FalloutTO getFallout(Session session) {
        return falloutDao.getFallout(session).convertToTO();
    }

    @Override
    public Status updateFallout(Session session, Long id, Status status, String message, Long processingDuration) {
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " updateFalloutStatus : start");
        session.beginTransaction();
        try {
            Fallout fallout = falloutDao.getFalloutById(session, id);
            fallout.setStatus(status);
            fallout.setFt42Message(message);
            fallout.setProcessingDuration(processingDuration);
            String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
            if (requestReferenceNumber != null && !requestReferenceNumber.isEmpty()) {
                fallout.setFreeText2(requestReferenceNumber);
            }
            falloutDao.update(session, fallout);
            session.getTransaction().commit();
            return status;
        } catch (ConcurrentModificationException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            return Status.FAILED;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " updateFalloutStatus : end");
        }
    }

    @Override
    public CsvFileDataTO getCsvData(Session session, String csvFileName) {
        return csvFileDataDao.getCsvData(session, csvFileName, BulkUploadType.FALLOUT_PROCESS).convertToTO();
    }

    @Override
    public Status updateCsvDataStatusAndMessage(Session session, Long id, Status status, String message) {
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " updateCsvDataStatusAndMessage : start");
        session.beginTransaction();
        try {
            CsvFileData csvFileData = csvFileDataDao.getCsvDataById(session, id);
            csvFileData.setStatus(status);
            csvFileData.setMessages(message);
            csvFileDataDao.update(session, csvFileData);
            session.getTransaction().commit();
            return status;
        } catch (ConcurrentModificationException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            return Status.FAILED;
        } finally {
            logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " updateCsvDataStatusAndMessage : end");
        }
    }

    @Override
    public FalloutSyncDataTo updateFalloutSyncData(FalloutSyncDataTo falloutSyncDataTo) throws AuthException {
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " updateFalloutSyncData : start");
        Session session = sessionFactoryUtil.openSessionWithoutTransaction();
        session.beginTransaction();
        FalloutSyncData falloutSyncData1;
        FalloutSyncDataTo falloutSyncDataTo1 = new FalloutSyncDataTo();
        try {
            FalloutSyncData falloutSyncDataFromDB = falloutSyncDataDao.getFalloutSyncRecord(falloutSyncDataTo.getId(), session);
            falloutSyncDataFromDB.setLastSyncTime(new Timestamp(falloutSyncDataTo.getLastSyncTime()));
            falloutSyncDataFromDB.setId(falloutSyncDataTo.getId());
            falloutSyncData1 = falloutSyncDataDao.update(session, falloutSyncDataFromDB);
            session.getTransaction().commit();
            falloutSyncDataTo1.setLastSyncTime(falloutSyncData1.getLastSyncTime().getTime());
            falloutSyncDataTo1.setId(falloutSyncData1.getId());
        } catch (Exception e) {
            if (session.isOpen() && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " updateFalloutSyncData : end");
        }

        return falloutSyncDataTo1;
    }

    @Override
    public List<FalloutSyncDataTo> getFalloutSyncData() {
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " getConfigs : start");
        Session session = sessionFactoryUtil.getSession();
        List<FalloutSyncDataTo> falloutSyncDataToList = new ArrayList<>();
        try {
            List<FalloutSyncData> falloutSyncData = falloutSyncDataDao.getAllFalloutSyncData(session);
            if (falloutSyncData != null) {
                for (FalloutSyncData falloutsyncdata : falloutSyncData) {
                    FalloutSyncDataTo falloutSyncDataTo = new FalloutSyncDataTo();
                    falloutSyncDataTo.setId(falloutsyncdata.getId());
                    falloutSyncDataTo.setVersion(falloutsyncdata.getVersion());
                    falloutSyncDataTo.setLastSyncTime(falloutsyncdata.getLastSyncTime().getTime());
                    falloutSyncDataToList.add(falloutSyncDataTo);
                }
            }

        } finally {
            if (session.isOpen())
                session.close();
        }
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " getConfigs : end");
        return falloutSyncDataToList;
    }

    @Override
    public List<FalloutTO> getAllFalloutDataList(int page, int pageSize, String attributeName, String attributeValue, String operations, String status, Long fromDate, Long toDate) throws AttributeNotFoundException {
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " getAllFalloutDataList : start");
        List<Fallout> falloutList = falloutDao.getAllFalloutDataPaginatedList(IamThreadContext.getSessionWithoutTransaction(), page, pageSize, attributeName, attributeValue, operations, status, fromDate, toDate);
        List<FalloutTO> falloutTOList = falloutList.stream().map(Fallout::convertToTO).collect(Collectors.toList());
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " getAllFalloutDataList : end");
        return falloutTOList;
    }

    @Override
    public Long getAllFalloutDataCount(String attributeName, String attributeValue, String operations, String status, Long fromDate, Long toDate) throws AttributeNotFoundException {
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " getAllFalloutDataCount : start");
        Long count = falloutDao.getAllFalloutDataCount(IamThreadContext.getSessionWithoutTransaction(), attributeName, attributeValue, operations, status, fromDate, toDate);
        logger.log(Level.DEBUG, FALLOUT_SERVICE_IMPL + " getAllFalloutDataCount : end");
        return count;
    }

}
