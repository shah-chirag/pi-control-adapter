
package in.fortytwo42.adapter.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.KeyValueTO;
import in.fortytwo42.enterprise.extension.enums.IdType;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.enterprise.extension.webentities.DeviceWE;

public class DeviceLogFilter implements EventLogFilter<AuditLogTO> {

    private static final String DEVICE_LOG_FILTER_LOG = "<<<<< DeviceLogFilter";
    private static Logger logger= LogManager.getLogger(DeviceLogFilter.class);
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private AuditLogTO auditLogTO;
    Map<String, DeviceWE> deviceMap;

    public DeviceLogFilter(AuditLogTO auditLogTO, Map<String, DeviceWE> deviceMap) {
        this.auditLogTO = auditLogTO;
        this.deviceMap = deviceMap;
    }

    public DeviceLogFilter() {

    }

    @Override
    public AuditLogTO filter() {
        logger.log(Level.DEBUG, DEVICE_LOG_FILTER_LOG + " applyDeviceFilter : start");
        if (IdType.DEVICE.equals(auditLogTO.getCreatorIdType())) {
            String deviceId = auditLogTO.getCreatorId();
            DeviceWE device = deviceMap.get(deviceId);
            if (device != null) {
                DeviceTO deviceTO = new DeviceTO();
                String deviceName = device.getDeviceName() != null ? device.getDeviceName() : device.getDeviceUDID();
                deviceTO.setDeviceName(deviceName);
                deviceTO.setDeviceUDID(device.getDeviceUDID());
                Gson gson = new Gson();
                List<KeyValueTO> deviceDetails = gson.fromJson(device.getDeviceDetails(), new TypeToken<List<KeyValueTO>>() {
                }.getType());
                deviceTO.setDeviceDetails(deviceDetails);
                auditLogTO.setLogDetails(gson.toJson(deviceTO));
                Map<String, String> deviceInfo = new HashMap<>();
                if (deviceDetails != null) {
                    for (KeyValueTO deviceDetail : deviceDetails) {
                        deviceInfo.put(deviceDetail.getKey(), deviceDetail.getValue());
                    }
                }
                String actedOnAccountId = auditLogTO.getActedOnAccountId();
                if (actedOnAccountId != null) {
                    actedOnAccountId = auditLogTO.getAttributes().get(actedOnAccountId) != null ? auditLogTO.getAttributes().get(auditLogTO.getActedOnAccountId()) : auditLogTO.getActedOnAccountId();
                    auditLogTO.setActedOnAccountId(actedOnAccountId);
                }
                auditLogTO.setDeviceInfo(deviceInfo);
                auditLogTO.setCreatorId(deviceName);
            }
        }
        logger.log(Level.DEBUG, DEVICE_LOG_FILTER_LOG + " applyDeviceFilter : end");
        return auditLogTO;
    }

}
