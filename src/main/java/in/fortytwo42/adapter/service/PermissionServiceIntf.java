package in.fortytwo42.adapter.service;

import org.hibernate.Session;

public interface PermissionServiceIntf {

    void loadPermissions(Session session);
    
    boolean isPermissionValidForRole(String permission, String adminRole);
}
