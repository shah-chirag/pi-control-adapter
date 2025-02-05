package  in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.tos.transferobj.TunnelLogTO;

public interface TunnelLogFacadeIntf {

    TunnelLogTO createTunnelLog(TunnelLogTO tunnelLogTO) throws AuthException;

    PaginatedTO<TunnelLogTO> getAuditTrails(int pageNo, String searchText, Long fromDate, Long toDate) throws AuthException;
}
