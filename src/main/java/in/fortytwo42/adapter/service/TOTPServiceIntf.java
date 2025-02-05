package in.fortytwo42.adapter.service;

import in.fortytwo42.tos.transferobj.TotpAuditTrailTO;

import java.util.List;

public interface TOTPServiceIntf {
    TotpAuditTrailTO createTotpAuditTrail(String applicationName, String status, String attributeName, String attributeValue, String accountId, String userSeed, String comment);

    List<TotpAuditTrailTO> getAllTotpAuditLogPaginatedList(int page, int pageSize,String attributeName, String searchQuery, String totpStatus);

    Long getAllTOtpAuditLogPaginatedCount(String attributeName, String searchQuery, String totoStatus);
}
