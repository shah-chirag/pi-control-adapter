package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.tos.transferobj.AdminAuditLogTO;

public interface AdminLoginLogFacadeIntf {

    PaginatedTO<AdminAuditLogTO> getPaginatedAdminLoginLogList(int pageNo, String userRole, String status, String searchText);

}
