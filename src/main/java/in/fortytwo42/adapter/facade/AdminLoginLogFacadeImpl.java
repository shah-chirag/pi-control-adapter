
package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.List;

import in.fortytwo42.adapter.service.AdminLoginLogServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.entities.bean.AdminAuditLog;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.transferobj.AdminAuditLogTO;

public class AdminLoginLogFacadeImpl implements AdminLoginLogFacadeIntf {

    private AdminLoginLogServiceIntf adminLoginLogService = ServiceFactory.getAdminLoginLogService();
    private Config config = Config.getInstance();

    public AdminLoginLogFacadeImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final AdminLoginLogFacadeImpl INSTANCE = new AdminLoginLogFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static AdminLoginLogFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public PaginatedTO<AdminAuditLogTO> getPaginatedAdminLoginLogList(int pageNo, String userRole, String status, String searchText) {
        List<AdminAuditLog> adminLoginLogList = new ArrayList<>();
        Long count;
        adminLoginLogList = adminLoginLogService.getPaginatedList(pageNo, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText, userRole, status);
        List<AdminAuditLogTO> adminLoginLogs = new EntityToTOConverter<AdminAuditLog, AdminAuditLogTO>().convertEntityListToTOList(adminLoginLogList);
        count = adminLoginLogService.getTotalCountOfAdminLoginLog(searchText, userRole, status);
        PaginatedTO<AdminAuditLogTO> paginatedTO = new PaginatedTO<AdminAuditLogTO>();
        paginatedTO.setList(adminLoginLogs);
        paginatedTO.setTotalCount(count);
        return paginatedTO;

    }

}
