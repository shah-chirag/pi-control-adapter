package in.fortytwo42.adapter.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.PermissionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class PermissionUtil.
 */
public class PermissionUtil {

    /** The permission util log. */
    private static String PERMISSION_UTIL_LOG = "<<<<< PermissionUtil";
    ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

	private static Logger logger= LogManager.getLogger(PermissionUtil.class);
    
    /**
     * Instantiates a new permission util.
     */
    private PermissionUtil() {
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final PermissionUtil INSTANCE = new PermissionUtil();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of PermissionUtil.
     *
     * @return single instance of PermissionUtil
     */
    public static PermissionUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /** The Constant CREATE_ADMIN. */
    public static final String CREATE_ADMIN = "CREATE_ADMIN";

    /** The Constant EDIT_ADMIN. */
    public static final String EDIT_ADMIN = "EDIT_ADMIN";

    /** The Constant DELETE_ADMIN. */
    public static final String DELETE_ADMIN = "DELETE_ADMIN";

    /** The Constant APPROVE_ADMIN. */
    public static final String APPROVE_ADMIN = "APPROVE_ADMIN";

    /** The Constant CREATE_APPLICATION. */
    public static final String CREATE_APPLICATION = "CREATE_APPLICATION";
    
    /** The Constant EDIT_APPLICATION. */
    public static final String EDIT_APPLICATION = "EDIT_APPLICATION";
    
    /** The Constant APPROVE_APPLICATION. */
    public static final String APPROVE_APPLICATION = "APPROVE_APPLICATION";

    public static final String ONBOARD_APPLICATION = "ONBOARD_APPLICATION";

    public static final String EDIT_USER_STATUS = "EDIT_USER_STATUS";

    public static final String USER_APPLICATION_MAPPING = "USER_APPLICATION_MAPPING";

    /** The Constant DELETE_APPLICATION. */
    public static final String DELETE_APPLICATION = "DELETE_APPLICATION";
    
    /** The Constant EDIT_USER. */
    public static final String EDIT_USER = "EDIT_USER";

    /** The Constant APPROVE_USER. */
    public static final String APPROVE_USER = "APPROVE_USER";

    /** The Constant VIEW_USERS. */
    public static final String VIEW_USERS = "VIEW_USERS";

    /** The Constant VIEW_PENDING_USERS. */
    public static final String VIEW_PENDING_USERS = "VIEW_PENDING_USERS";

    /** The Constant VIEW_APPLICATIONS. */
    public static final String VIEW_APPLICATIONS = "VIEW_APPLICATIONS";

    /** The Constant VIEW_PENDING_APPLICATIONS. */
    public static final String VIEW_PENDING_APPLICATIONS = "VIEW_PENDING_APPLICATIONS";

    /** The Constant VIEW_ADMINS. */
    public static final String VIEW_ADMINS = "VIEW_ADMINS";

    /** The Constant VIEW_PENDING_ADMINS. */
    public static final String VIEW_PENDING_ADMINS = "VIEW_PENDING_ADMINS";

    /** The Constant APPROVE_USER_BINDING. */
    public static final String APPROVE_USER_BINDING = "APPROVE_USER_BINDING";
    
    /** The Constant AD_SYNC. */
    public static final String AD_SYNC = "AD-SYNC";

    /** The Constant EXPORT_USER_DATA. */
    public static final String EXPORT_USER_DATA = "EXPORT_USER_DATA";

    /** The Constant HEALTH_CHECK. */
    private static final String HEALTH_CHECK = "Heath Check";
    
    /** The Constant VIEW_USER_AUDIT_TRAIL. */
    public static final String VIEW_USER_AUDIT_TRAIL = "VIEW_USER_AUDIT_TRAIL";
    
    /** The Constant VIEW_APPLICATION_AUDIT_TRAIL. */
    public static final String VIEW_APPLICATION_AUDIT_TRAIL = "VIEW_APPLICATION_AUDIT_TRAIL";
	
	/** The Constant GET_USER_SETTINGS_FOR_APPLICATION. */
	public static final String GET_USER_SETTINGS_FOR_APPLICATION = "GET_USER_SETTINGS_FOR_APPLICATION";
	
	/** The Constant GET_APPLICATION_LABEL. */
	public static final String GET_APPLICATION_LABEL = "GET_APPLICATION_LABEL";

	/** The Constant GET_PENDING_APPLICATION_LABEL. */
	public static final String GET_PENDING_APPLICATION_LABEL = "GET_PENDING_APPLICATION_LABEL";

	/** The Constant EDIT_APPLICATION_LABEL. */
	public static final String EDIT_APPLICATION_LABEL = "EDIT_APPLICATION_LABEL";

	/** The Constant DELETE_APPLICATION_LABEL. */
	public static final String DELETE_APPLICATION_LABEL = "DELETE_APPLICATION_LABEL";

	/** The Constant CREATE_APPLICATION_LABEL. */
	public static final String CREATE_APPLICATION_LABEL = "CREATE_APPLICATION_LABEL";

	/** The Constant APPROVE_APPLICATION_LABEL. */
	public static final String APPROVE_APPLICATION_LABEL = "APPROVE_APPLICATION_LABEL";
	
	/** The Constant UPDATE_USER_APPLICATION_REL. */
	public static final String UPDATE_USER_APPLICATION_REL = "UPDATE_USER_APPLICATION_REL";

    /** The Constant APPROVE_USER_APPLICATION_REL_BINDING. */
    public static final String APPROVE_USER_APPLICATION_REL_BINDING = "APPROVE_USER_APPLICATION_REL_BINDING";


    /** The Constant GET_USER_APPLICATION_REL. */
    public static final String GET_USER_APPLICATION_REL = "GET_USER_APPLICATION_REL";

    /** The Constant UPLOAD_ADD_ATTRIBUTE. */
    public static final String UPLOAD_ADD_ATTRIBUTE = "UPLOAD_ADD_ATTRIBUTE";
    
    public static final String APPROVE_USER_STATUS_UPDATE = "APPROVE_USER_STATUS_UPDATE";

    public static final String GET_USER_ACCOUNT_DETAILS = "GET_USER_ACCOUNT_DETAILS";
    
    public static final String ONBORAD_ADD_ATTRIBUTE = "ONBOARD_ADD_ATTRIBUTE";
    
    public static final String ATTRIBUTE_ADDITION = "ATTRIBUTE_ADDITION";
    
    public static final String ATTRIBUTE_MASTER_ADDITION = "ATTRIBUTE_MASTER_ADDITION";
    
    public static final String ATTRIBUTE_MASTER_DELETION = "ATTRIBUTE_MASTER_DELETION"; 
    
    public static final String ATTRIBUTE_UPDATION = "ATTRIBUTE_UPDATION";
    
    public static final String ATTRIBUTE_MASTER_UPDATION = "ATTRIBUTE_MASTER_UPDATION";

    public static final String FALLOUT_PROCESS = "FALLOUT_PROCESS";
        
  /*  public static final String VERIFY_ATTRIBUTE = "VERIFY_ATTRIBUTE";
    
    public static final String APPROVE_ATTRIBUTE_VERIFICATION = "APPROVE_ATTRIBUTE_VERIFICATION";
    
    public static final String ADD_ATTRIBUTE = "ADD_ATTRIBUTE";
    
    public static final String APPROVE_ATTRIBUTE_ADDITION = "APPROVE_ATTRIBUTE_ADDITION";*/

    /**
   * Gets the permission list.
   *
   * @return the permission list
   */
  public List<String> getPermissionList() {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " getPermissionList : start");
        List<String> permissionList = new ArrayList<>();
        permissionList.add(CREATE_ADMIN);
        permissionList.add(EDIT_ADMIN);
        permissionList.add(DELETE_ADMIN);
        permissionList.add(CREATE_APPLICATION);
        permissionList.add(EDIT_APPLICATION);
        permissionList.add(APPROVE_APPLICATION);
        permissionList.add(EDIT_USER);
        permissionList.add(APPROVE_USER); 
        permissionList.add(APPROVE_ADMIN);
        
        permissionList.add(DELETE_APPLICATION);
        permissionList.add(VIEW_USERS);
        permissionList.add(VIEW_APPLICATIONS);
        permissionList.add(VIEW_ADMINS);
        permissionList.add(VIEW_PENDING_USERS);
        permissionList.add(VIEW_PENDING_APPLICATIONS);
        permissionList.add(VIEW_PENDING_ADMINS);
        permissionList.add(EXPORT_USER_DATA);
        permissionList.add(VIEW_USER_AUDIT_TRAIL);
        permissionList.add(VIEW_APPLICATION_AUDIT_TRAIL);
        permissionList.add(GET_USER_SETTINGS_FOR_APPLICATION);
        permissionList.add(GET_APPLICATION_LABEL);
        permissionList.add(GET_PENDING_APPLICATION_LABEL);
        permissionList.add(EDIT_APPLICATION_LABEL);
        permissionList.add(DELETE_APPLICATION_LABEL);
        permissionList.add(CREATE_APPLICATION_LABEL);
        permissionList.add(APPROVE_APPLICATION_LABEL);
        permissionList.add(UPDATE_USER_APPLICATION_REL);
        permissionList.add(APPROVE_USER_APPLICATION_REL_BINDING);
        permissionList.add(GET_USER_APPLICATION_REL);
        permissionList.add(ONBORAD_ADD_ATTRIBUTE);
        permissionList.add(ATTRIBUTE_MASTER_ADDITION);
        permissionList.add(ATTRIBUTE_MASTER_DELETION);
        permissionList.add(ATTRIBUTE_MASTER_UPDATION);
        permissionList.add(ATTRIBUTE_ADDITION);
        permissionList.add(ATTRIBUTE_UPDATION);
        permissionList.add(GET_USER_ACCOUNT_DETAILS);
        permissionList.add(FALLOUT_PROCESS);
       /* permissionList.add(VERIFY_ATTRIBUTE);
        permissionList.add(APPROVE_ATTRIBUTE_VERIFICATION);
        permissionList.add(ADD_ATTRIBUTE);
        permissionList.add(APPROVE_ATTRIBUTE_ADDITION);*/
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " getPermissionList : end");
        return permissionList;
    }
    
    /**
     * Validate edit user permission.
     *
     * @param userType the user type
     * @param role the role
     * @throws AuthException the auth exception
     */
    public void validateEditUserPermission( String role) throws AuthException {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateEditUserPermission : start");
		PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
		if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.EDIT_USER, role)) {
            logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateEditUserPermission : end");
           throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
       }
//		switch (userType) {
//		case ADUSER:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.EDIT_AD_USER, role)) {
//		        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateEditUserPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//			break;
//		case USER:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.EDIT_USER, role)) {
//	             logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateEditUserPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//			break;
//		default:
//            logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateEditUserPermission : end");
//			throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//		}
    }
    public void validateUsersPermissions( String role) throws AuthException {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateUsersPermissions : start");
        PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
        if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.EDIT_ADMIN, role)) {
            logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateUsersPermissions : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
    }
    
    /**
     * Validate approve user permission.
     *
     * @param userType the user type
     * @param role the role
     * @throws AuthException the auth exception
     */
    public void validateApproveUserPermission(String role) throws AuthException {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateApproveUserPermission : start");
    	PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
//		switch (userType) {
//		case ADUSER:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.APPROVE_AD_USER, role)) {
//		        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateApproveUserPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//			break;
//		case USER:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.APPROVE_USER, role)) {
//		        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateApproveUserPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//			break;
//		default:
//	        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateApproveUserPermission : end");
//			throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//		}
    	if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.APPROVE_USER, role)) {
            logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateApproveUserPermission : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
    }
    
    /**
     * Validate get users permission.
     *
     * @param userType the user type
     * @param role the role
     * @throws AuthException the auth exception
     */
    public void validateGetUsersPermission(String role) throws AuthException {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetUsersPermission : start");
    	PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
//		switch (userType) {
//		case ADUSER:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.VIEW_AD_USERS, role)) {
//		        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetUsersPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//			break;
//		case USER:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.VIEW_USERS, role)) {
//		        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetUsersPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//			break;
//		default:
//	        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetUsersPermission : end");
//			throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//		}
    	if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.VIEW_USERS, role)) {
            logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetUsersPermission : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
    }
    
    /**
     * Validate get pending users permission.
     *
     * @param userType the user type
     * @param role the role
     * @throws AuthException the auth exception
     */
    public void validateGetPendingUsersPermission( String role) throws AuthException {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetPendingUsersPermission : start");
    	PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
    	if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.VIEW_PENDING_USERS, role)) {
            logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetPendingUsersPermission : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
//		switch (userType) {
//		case ADUSER:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.VIEW_PENDING_AD_USERS, role)) {
//		        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetPendingUsersPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//			break;
//		case USER:
//			
//			break;
//		default:
//	        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetPendingUsersPermission : end");
//			throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//		}
    }
    
	/**
	 * Validate get user audit trail permission.
	 *
	 * @param role the role
	 * @throws AuthException the auth exception
	 */
	public void validateGetUserAuditTrailPermission(String role) throws AuthException {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetUserAuditTrailPermission : start");
		PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
		if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.VIEW_USER_AUDIT_TRAIL, role)) {
	        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetUserAuditTrailPermission : end");
			throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
		}
	}
	
	/**
	 * Validate get application audit trail permission.
	 *
	 * @param role the role
	 * @throws AuthException the auth exception
	 */
	public void validateGetApplicationAuditTrailPermission(String role) throws AuthException {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetApplicationAuditTrailPermission : strat");
		PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
		if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.VIEW_APPLICATION_AUDIT_TRAIL, role)) {
	        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetApplicationAuditTrailPermission : end");
			throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
		}
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateGetApplicationAuditTrailPermission : end");
	}
    
    /**
     * Validate start AD sync permission.
     *
     * @param role the role
     * @throws AuthException the auth exception
     */
//    public void validateStartADSyncPermission(String role) throws AuthException {
//        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateStartADSyncPermission : start");
//        PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
//        if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.START_AD_SYNC, role)) {
//            logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateStartADSyncPermission : end");
//            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//        }
//        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateStartADSyncPermission : end");
//    }
//    
//	/**
//	 * Validate job permission.
//	 *
//	 * @param role the role
//	 * @param jobName the job name
//	 * @throws AuthException the auth exception
//	 */
//	public void validateJobPermission(String role, String jobName) throws AuthException {
//        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateJobPermission : start");
//		PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
//		switch (jobName) {
//		case AD_SYNC:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.START_AD_SYNC, role)) {
//		        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateJobPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//		case HEALTH_CHECK:
//			if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.HEALTH_CHECK, role)) {
//		        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateJobPermission : end");
//				throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
//			}
//		}
//        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateJobPermission : end");
//	}
    
    /**
     * Validate export users permission.
     *
     * @param role the role
     * @throws AuthException the auth exception
     */
    public void validateExportUsersPermission(String role) throws AuthException {
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateExportUsersPermission : start");
        PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
        if (!permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.EXPORT_USER_DATA, role)) {
            logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateExportUsersPermission : end");
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED(), errorConstant.getERROR_MESSAGE_PERMISSION_DENIED());
        }
        logger.log(Level.DEBUG, PERMISSION_UTIL_LOG + " validateExportUsersPermission : end");
    }
    
    /**
     * Validate attribute verification permission.
     *
     * @param role the role
     * @throws AuthException the auth exception
     */
    public void validateAttributeVerificationPermission(String role) throws AuthException {
        PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
        /*if (permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.VERIFY_ATTRIBUTE, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }  */
    }
    
    /**
     * Validate attribute approval permission.
     *
     * @param role the role
     * @throws AuthException the auth exception
     */
    public void validateAttributeApprovalPermission(String role) throws AuthException {
        PermissionServiceIntf permissionProcessorIntf = ServiceFactory.getPermissionService();
        /*if (permissionProcessorIntf.isPermissionValidForRole(PermissionUtil.APPROVE_ATTRIBUTE_VERIFICATION, role)) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PERMISSION_DENIED, errorConstant.getERROR_MESSAGE_PERMISSION_DENIED);
        }  */
    }

}
