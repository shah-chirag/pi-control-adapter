
package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.TunnelLogServiceIntf;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.tos.transferobj.TunnelLogTO;

// TODO: Auto-generated Javadoc
/**
 * The Class EvidenceFacadeImpl.
 */
public class TunnelLogFacadeImpl implements TunnelLogFacadeIntf {

    /** The evidence facade impl log. */
    private String TUNNELLOG_FACADE_IMPL_LOG = "<<<<< TunnelLogFacadeImpl";

    private static Logger logger= LogManager.getLogger(TunnelLogFacadeImpl.class);

    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private TunnelLogServiceIntf tunnelLogService = ServiceFactory.getTunnelLogService();

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final TunnelLogFacadeImpl INSTANCE = new TunnelLogFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of EvidenceFacadeImpl.
     *
     * @return single instance of EvidenceFacadeImpl
     */
    public static TunnelLogFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public TunnelLogTO createTunnelLog(TunnelLogTO tunnelLogTO) throws AuthException {
        logger.log(Level.DEBUG, TUNNELLOG_FACADE_IMPL_LOG + " createTunnelLog : start");

        Session session = sessionFactoryUtil.getSession();
        try {
            tunnelLogTO = tunnelLogService.createTunnelLog(session, tunnelLogTO);
            sessionFactoryUtil.closeSession(session);
            return tunnelLogTO;
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, TUNNELLOG_FACADE_IMPL_LOG + " createTunnelLog : end");
        }
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
        logger.log(Level.DEBUG, TUNNELLOG_FACADE_IMPL_LOG + " getUserAuditTrails : start");
        PaginatedTO<TunnelLogTO> paginatedTO = tunnelLogService.getAuditTrails(pageNo, searchText, fromDate, toDate);
        logger.log(Level.DEBUG, TUNNELLOG_FACADE_IMPL_LOG + " getUserAuditTrails : end");
        return paginatedTO;
    }

}
