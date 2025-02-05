package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.TOTPServiceIntf;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.tos.transferobj.TotpAuditTrailTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TOTPFacadeImpl implements TOTPFacadeIntf {

    private static final String TOTP_FACADE_IMPL_LOG = "<<<<< TOTPFacadeImpl";

    private static Logger logger= LogManager.getLogger(TOTPFacadeImpl.class);

    private TOTPServiceIntf totpServiceIntf = ServiceFactory.getTOTPService();

    private TOTPFacadeImpl(){ }

    private static final class InstanceHolder {

        private static final TOTPFacadeImpl INSTANCE = new TOTPFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static TOTPFacadeImpl getInstance() {

        return TOTPFacadeImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public TotpAuditTrailTO createTotpAuditTrail(String applicationName, String status, String attributeName, String attributeValue, String accountId, String userSeed, String comment) {
        logger.log(Level.DEBUG, TOTP_FACADE_IMPL_LOG + " createTotpAuditTrail : start");
        TotpAuditTrailTO totpAuditTrailTO = totpServiceIntf.createTotpAuditTrail(applicationName, status, attributeName, attributeValue, accountId, userSeed, comment);
        logger.log(Level.DEBUG, TOTP_FACADE_IMPL_LOG + " createTotpAuditTrail : end");
        return totpAuditTrailTO;
    }

    @Override
    public PaginatedTO<TotpAuditTrailTO> getAllTotpAuditLog(int page, int pageSize, String attributeName, String searchQuery, String totpStatus) {
        logger.log(Level.DEBUG, TOTP_FACADE_IMPL_LOG + " getAllTotpAuditLog : start");
        PaginatedTO<TotpAuditTrailTO> paginatedTO = new PaginatedTO<>();
        try {
            List<TotpAuditTrailTO> otpAuditLogList = totpServiceIntf.getAllTotpAuditLogPaginatedList(page, pageSize, attributeName,searchQuery, totpStatus);
            Long count = totpServiceIntf.getAllTOtpAuditLogPaginatedCount(attributeName, searchQuery, totpStatus);
            paginatedTO.setList(otpAuditLogList);
            paginatedTO.setTotalCount(count);
        } finally {
            logger.log(Level.DEBUG, TOTP_FACADE_IMPL_LOG + " getAllTotpAuditLog : end");
        }
        return paginatedTO;
    }
}
