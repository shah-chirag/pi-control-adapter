package  in.fortytwo42.adapter.service;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.PermissionDaoIntf;
import in.fortytwo42.daos.dao.RoleDaoImpl;
import in.fortytwo42.daos.dao.RoleDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.Permission;
import in.fortytwo42.entities.bean.Role;
import in.fortytwo42.entities.enums.UserRole;

public class PermissionServiceImpl implements PermissionServiceIntf {

    private static Logger logger=LogManager.getLogger(PermissionServiceImpl.class);

    private PermissionDaoIntf permissionDao = DaoFactory.getPermissionDao();
    private RoleDaoIntf roleDao = RoleDaoImpl.getInstance();

    private PermissionUtil permissionUtil = PermissionUtil.getInstance();

    private PermissionServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final PermissionServiceImpl INSTANCE = new PermissionServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static PermissionServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    @Override
    public void loadPermissions(Session session) {
        List<String> permissionList = permissionUtil.getPermissionList();
        for(String name : permissionList) {
            Permission permission = null;
            try {
                permission = permissionDao.getPermissionByName(name);
            }
            catch(NotFoundException e) {
            }
            if(permission == null) {
                permission = new Permission();
                permission.setName(name);
                permissionDao.create(session, permission);
            }
        }
    }

    @Override
    public boolean isPermissionValidForRole(String permission, String adminRole) {
        boolean permissionPresent = false;
        try {
            String[] adminRoles = adminRole.split(",");
            for(String roleName : adminRoles) {
                logger.log(Level.INFO, "role " + roleName + " " + permission);
                Role role = roleDao.getRoleByName(UserRole.valueOf(roleName));
                List<Permission> permissions = role.getPermissions();
                for(Permission permission1 : permissions) {
                    logger.log(Level.INFO, "Permission " + permission1.getName());
                    if(permission1.getName().equals(permission)) {
                        permissionPresent = true;
                        break;
                    }
                }
            }
        }
        catch (NotFoundException e) {
        }
        return permissionPresent;
    }   
}
