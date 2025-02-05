
package in.fortytwo42.adapter.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;

public class DeviceServiceImpl implements DeviceServiceIntf {

    private static final String DEVICE_SERVICE_IMPL_LOG = "<<<<< DeviceServiceImpl";
    private static Logger logger= LogManager.getLogger(DeviceServiceImpl.class);

    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private DeviceServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final DeviceServiceImpl INSTANCE = new DeviceServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static DeviceServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    @Override
    public DeviceTO editDevice(DeviceTO deviceTO) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_SERVICE_IMPL_LOG + " editDevice : start");
        AuditLogUtil.sendAuditLog(deviceTO.getDeviceName()+ " edit device request approved " , "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        deviceTO = iamExtensionService.editDevice(deviceTO);
        AuditLogUtil.sendAuditLog(deviceTO.getDeviceName()+ "  device edited successfully", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, DEVICE_SERVICE_IMPL_LOG + " editDevice : end");
        return deviceTO;
    }

    @Override
    public DeviceTO editDeviceBindToken(DeviceTO deviceTO) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_SERVICE_IMPL_LOG + " editDeviceBindToken : start");
        AuditLogUtil.sendAuditLog(deviceTO.getDeviceName()+ "  device bind token edit request approved ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        deviceTO = iamExtensionService.editDeviceBindToken(deviceTO);
        AuditLogUtil.sendAuditLog(deviceTO.getDeviceName()+ "  device bind token edited successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, DEVICE_SERVICE_IMPL_LOG + " editDeviceBindToken : end");
        return deviceTO;
    }
    
    @Override
    public DeviceTO unbindUsersFromDevice(DeviceTO deviceTO) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_SERVICE_IMPL_LOG + " unbindUsersFromDevice : start");
        AuditLogUtil.sendAuditLog(deviceTO.getDeviceName()+ " unbind user from  device request approved ", "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", "", null);
        deviceTO = iamExtensionService.unbindUsersFromDevice(deviceTO);
        AuditLogUtil.sendAuditLog(deviceTO.getDeviceName()+ " unbinding  user from  device successfully ", "ENTERPRISE", ActionType.UNBIND, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, DEVICE_SERVICE_IMPL_LOG + " unbindUsersFromDevice : end");
        return deviceTO;
    }

}
