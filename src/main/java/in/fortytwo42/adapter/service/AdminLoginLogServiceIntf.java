
package in.fortytwo42.adapter.service;

import java.sql.Timestamp;
import java.util.List;

import in.fortytwo42.entities.enums.AdminSessionState;
import org.hibernate.Session;

import in.fortytwo42.entities.bean.AdminAuditLog;
import in.fortytwo42.entities.enums.LoginStatus;

public interface AdminLoginLogServiceIntf {

    Long getTotalCountOfAdminLoginLog(String searchText, String userRole, String status);

    List<AdminAuditLog> getPaginatedList(int pageNo, int limit, String searchText, String userRole, String status);

    /**
     * Admin Login log.
     *
     * @param username the username
     * @param loginStatus the login status
     */
    void adminLoginLog(String username, LoginStatus loginStatus, Timestamp loginTime, AdminSessionState sessionStatus, String role);

    /**
     * Update admin login log.
     *
     * @param username the username
     * @param status the status
     */
    int updateAdminLoginLog(Session session,String username, LoginStatus status, AdminSessionState sessionStatus, Timestamp logoutTime, String role,Timestamp initialLoginTime);

    void updateAdminLogoutTime( String username, AdminSessionState sessionStatus, Timestamp logoutTime);


}
