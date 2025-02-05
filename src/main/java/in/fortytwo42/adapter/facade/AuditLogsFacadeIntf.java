package  in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.enterprise.extension.tos.PaginatedTO;

public interface AuditLogsFacadeIntf {

    /**
     * 
     * @param queryParams
     * @return
     * @throws AuthException
     */
    PaginatedTO<AuditLogTO> getAuditLogs(String queryParams) throws AuthException;

    /**
     * 
     * @param queryParams
     * @return
     * @throws AuthException 
     */
    String downloadAuditLogs(String queryParams) throws AuthException;
    
    void verifyAuditLog(AuditLogTO auditLogTO) throws AuthException;

}
