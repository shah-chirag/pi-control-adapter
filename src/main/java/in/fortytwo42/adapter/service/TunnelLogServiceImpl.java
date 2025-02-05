
package in.fortytwo42.adapter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.EventLogWorker;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.TunnelLogDaoIntf;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.entities.bean.TunnelLog;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.enums.TunnelStatus;
import in.fortytwo42.tos.transferobj.TunnelLogTO;

public class TunnelLogServiceImpl implements TunnelLogServiceIntf {

    /** The evidence facade impl log. */
    private String TUNNELLOG_SERVICE_IMPL_LOG = "<<<<< TunnelLogServiceImpl";

    private static Logger logger= LogManager.getLogger(TunnelLogServiceImpl.class);

    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private TunnelLogDaoIntf tunnelLogDao = DaoFactory.getTunnelLogDao();

    private TunnelLogServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final TunnelLogServiceImpl INSTANCE = new TunnelLogServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static TunnelLogServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public TunnelLogTO createTunnelLog(Session session, TunnelLogTO tunnelLogTO) throws AuthException {

        TunnelLog tunnelLog = new TunnelLog();
        tunnelLog.setDestinationIpAddress(tunnelLogTO.getTargetHost());
        tunnelLog.setDestinationPort(tunnelLogTO.getTargetPort());
        tunnelLog.setUserId(tunnelLogTO.getAccountId());
        tunnelLog.setSignTransactionId(tunnelLogTO.getSignTransactionId());
        tunnelLog.setClientIpAddress(tunnelLogTO.getClientIpAddress());
        tunnelLog.setStatus(TunnelStatus.valueOf(tunnelLogTO.getStatus()));
        tunnelLogDao.create(session, tunnelLog);
        return tunnelLog.convertToTO();
    }

    /**
     * Gets the audit trails.
     *
     * @param pageNo the page no
     * @param searchText the search text
     * @param fromDate the from date
     * @param toDate the to date
     * @param role the role
     * @return the audit trails
     * @throws AuthException the auth exception
     */
    @Override
    public PaginatedTO<TunnelLogTO> getAuditTrails(int pageNo, String searchText, Long fromDate, Long toDate) throws AuthException {
        logger.log(Level.DEBUG, TUNNELLOG_SERVICE_IMPL_LOG + " getUserAuditTrails : start");
        List<String> accountIdsMatch = null;
        if(searchText!=null) {
            accountIdsMatch = iamExtensionService.getAccountIds(searchText);
        }
        TunnelLogDaoIntf tunnelLogDaoIntf = DaoFactory.getTunnelLogDao();
        Long count = tunnelLogDaoIntf.getTotalCountAuditTrail(accountIdsMatch, fromDate, toDate);
        List<TunnelLog> tunnelLogs = tunnelLogDaoIntf.getAuditTrail(pageNo, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)), accountIdsMatch, fromDate, toDate);
        List<String> accountIds = new ArrayList<>();
        getAccountIds(tunnelLogs, accountIds);
        CountDownLatch latch = new CountDownLatch(1);
        EventLogWorker accountFilter = new EventLogWorker(latch, accountIds, "ACCOUNT", iamExtensionService);
        accountFilter.start();
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        AuditLogTO defaultAttributes = accountFilter.getDefaultAttributes();
        List<TunnelLogTO> tunnelLogsList = new EntityToTOConverter<TunnelLog, TunnelLogTO>().convertEntityListToTOList(tunnelLogs);
        for(TunnelLogTO tunnelLog:tunnelLogsList ) {
            tunnelLog = new TunnelLogFilter(tunnelLog,defaultAttributes).filter();
        }
        PaginatedTO<TunnelLogTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(tunnelLogsList);
        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, TUNNELLOG_SERVICE_IMPL_LOG + " getUserAuditTrails : end");
        return paginatedTO;
    }

    private void getAccountIds(List<TunnelLog> tunnelLogs,List<String> accountIds) {
        for(TunnelLog tunnelLog:tunnelLogs ) {
            accountIds.add(tunnelLog.getUserId());
        }
    }
}
