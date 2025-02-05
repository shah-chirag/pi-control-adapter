package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.tos.transferobj.TotpAuditTrailTO;

public interface TOTPFacadeIntf {
    TotpAuditTrailTO createTotpAuditTrail(String applicationName, String status, String attributeName, String attributeValue, String accountId, String userSeed, String comment);

    PaginatedTO<TotpAuditTrailTO> getAllTotpAuditLog(int page, int pageSize,String attributeName, String searchQuery, String totpStatus);
}
