package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.TotpAuditDaoIntf;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.TotpAuditTrail;
import in.fortytwo42.entities.enums.OtpStatus;
import in.fortytwo42.tos.transferobj.TotpAuditTrailTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TOTPServiceImpl implements TOTPServiceIntf {

    private static final String TOTP_SERVICE_IMPL_LOG = "<<<<< TOTPServiceImpl";

    private static Logger logger = LogManager.getLogger(TOTPServiceImpl.class);

    private TotpAuditDaoIntf totpAuditDaoIntf = DaoFactory.getTotpAuditDao();


    private final ExecutorService pool;

    private TOTPServiceImpl(){
        super();


        int poolSize = 10;
        try {
            poolSize = Integer.parseInt(Config.getInstance().getProperty(Constant.ADMIN_TOTP_LOG_THREAD_POOL_SIZE));
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);
    }

    private static final class InstanceHolder {

        private static final TOTPServiceImpl INSTANCE = new TOTPServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static TOTPServiceImpl getInstance() {

        return TOTPServiceImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public TotpAuditTrailTO createTotpAuditTrail(String applicationName, String status, String attributeName, String attributeValue, String accountId, String userSeed, String comment) {
        logger.log(Level.DEBUG, TOTP_SERVICE_IMPL_LOG + " createTotpAuditTrail : start");
        TotpAuditTrail totpAuditTrail = new TotpAuditTrail();
        totpAuditTrail.setApplicationName(applicationName.toUpperCase());
        String totpStatus = String.valueOf(status.charAt(0));
        totpAuditTrail.setTotpStatus(OtpStatus.valueOf(totpStatus.toUpperCase()));
        totpAuditTrail.setIdentifierName(attributeName);
        totpAuditTrail.setIdentifierValue(attributeValue.toUpperCase());
        totpAuditTrail.setUserAccountId(accountId);
        totpAuditTrail.setReferenceNumber(userSeed);
        totpAuditTrail.setComments(comment);
        Session session = SessionFactoryUtil.getInstance().openSessionWithoutTransaction();
        pool.submit(()->{
            session.beginTransaction();
            try {
                totpAuditDaoIntf.create(session, totpAuditTrail);
            } catch (Exception e) {
                if (session.isOpen() && session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                    session.close();
                }
                logger.log(Level.ERROR, e.getMessage());
            } finally {
                if (session.isOpen() && session.getTransaction().isActive()) {
                    session.getTransaction().commit();
                    session.close();
                }
            }
        });

        logger.log(Level.DEBUG, TOTP_SERVICE_IMPL_LOG + " createTotpAuditTrail : end");
        return totpAuditTrail.convertToTO();
    }

    @Override
    public List<TotpAuditTrailTO> getAllTotpAuditLogPaginatedList(int page, int pageSize, String attributeName, String searchQuery, String totpStatus) {
        logger.log(Level.DEBUG, TOTP_SERVICE_IMPL_LOG + " getAllTotpAuditLogPaginatedList : start");
        List<TotpAuditTrail> totpAuditLogList = totpAuditDaoIntf.getAllTOtpAuditLogPaginatedList(page, pageSize,attributeName, searchQuery, totpStatus);
        List<TotpAuditTrailTO> otpAuditLogTOList = totpAuditLogList.stream().map(TotpAuditTrail::convertToTO).collect(Collectors.toList());
        logger.log(Level.DEBUG, TOTP_SERVICE_IMPL_LOG + " getAllTotpAuditLogPaginatedList : end");
        return otpAuditLogTOList;
    }

    @Override
    public Long getAllTOtpAuditLogPaginatedCount(String attributeName,String searchQuery, String totoStatus) {
        logger.log(Level.DEBUG, TOTP_SERVICE_IMPL_LOG + " getAllTOtpAuditLogPaginatedCount : start");
        Long count = totpAuditDaoIntf.getAllTotpAuditLogPaginatedCount(attributeName,searchQuery, totoStatus);
        logger.log(Level.DEBUG, TOTP_SERVICE_IMPL_LOG + " getAllTOtpAuditLogPaginatedCount : end");
        return count;
    }
}
