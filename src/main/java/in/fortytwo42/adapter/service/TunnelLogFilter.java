
package in.fortytwo42.adapter.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.tos.transferobj.TunnelLogTO;

public class TunnelLogFilter implements EventLogFilter<TunnelLogTO> {

    private static final String ACCOUNT_LOG_FILTER_LOG = "<<<<< AccountLogFilter";
    private static Logger logger= LogManager.getLogger(TunnelLogFilter.class);
    private TunnelLogTO tunnelLogTO;
    private AuditLogTO defaultAttributes;

    public TunnelLogFilter(TunnelLogTO tunnelLogTO, AuditLogTO defaultAttributes) {
        this.tunnelLogTO = tunnelLogTO;
        this.defaultAttributes = defaultAttributes;
    }

    public TunnelLogFilter() {

    }

    @Override
    public TunnelLogTO filter() {
        logger.log(Level.DEBUG, ACCOUNT_LOG_FILTER_LOG + " filter : start");
        String accountId = tunnelLogTO.getAccountId();
        accountId = defaultAttributes.getAttributes().get(accountId) != null ? defaultAttributes.getAttributes().get(accountId) : accountId;
        tunnelLogTO.setAccountId(accountId);
        logger.log(Level.DEBUG, ACCOUNT_LOG_FILTER_LOG + " filter : end");
        return tunnelLogTO;
    }

}
