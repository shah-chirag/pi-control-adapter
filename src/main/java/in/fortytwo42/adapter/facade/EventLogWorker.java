
package in.fortytwo42.adapter.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.enterprise.extension.webentities.DeviceWE;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;

public class EventLogWorker extends Thread {

    CountDownLatch latch;
    List<String> ids;
    String type;
    IamExtensionServiceIntf iamExtensionService;

    private AuditLogTO defaultAttributes;
    private Map<String, DeviceWE> deviceMap;
    private Map<String, TokenWE> tokenMap;
    private static Logger logger= LogManager.getLogger(EventLogWorker.class);

    public EventLogWorker(CountDownLatch latch, List<String> ids, String type, IamExtensionServiceIntf iamExtensionService) {
        this.latch = latch;
        this.ids = ids;
        this.type = type;
        this.iamExtensionService = iamExtensionService;
    }

    @Override
    public void run() {
        try {
            if ("ACCOUNT".equals(type)) {
                AuditLogTO auditLogData = new AuditLogTO();
                auditLogData.setAccountIds(ids);
                this.defaultAttributes = iamExtensionService.getDefaultAttributes(auditLogData);
            }
            else if ("DEVICE".equals(type)) {
                this.deviceMap = ids.size() > 0 ? iamExtensionService.getDevices(ids) : new HashMap<>();
            }
            else {
                this.tokenMap = ids.size() > 0 ? iamExtensionService.getTokens(ids) : new HashMap<>();
            }
            latch.countDown();
            System.out.println(Thread.currentThread().getName()
                               + " finished");
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public AuditLogTO getDefaultAttributes() {
        return defaultAttributes;
    }

    public Map<String, DeviceWE> getDeviceMap() {
        return deviceMap;
    }

    public Map<String, TokenWE> getTokenMap() {
        return tokenMap;
    }

}
