
package in.fortytwo42.adapter.facade;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;

import in.fortytwo42.adapter.enums.DeviceMetadata;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.EventLogFilter;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.enterprise.extension.enums.IdType;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.enterprise.extension.tos.PaginatedTO;
import in.fortytwo42.enterprise.extension.webentities.DeviceWE;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;

// TODO: Auto-generated Javadoc
/**
 * The Class BuildDetailsFacadeImpl.
 */
public class AuditLogsFacadeImpl implements AuditLogsFacadeIntf {

    /** The build details facade impl log. */
    private String AUDIT_LOGS_FACADE_IMPL_LOG = "<<<<< AuditLogsFacadeImpl";

    private static Logger logger= LogManager.getLogger(AuditLogsFacadeImpl.class);

    /**
     * The Class InstanceHolder.
     */
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final AuditLogsFacadeImpl INSTANCE = new AuditLogsFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of AuditLogsFacadeImpl.
     *
     * @return single instance of AuditLogsFacadeImpl
     */
    public static AuditLogsFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public PaginatedTO<AuditLogTO> getAuditLogs(String queryParams) throws AuthException {
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " getAuditLogs : start");
        PaginatedTO<AuditLogTO> auditLogs = iamExtensionService.getAuditLogs(queryParams);
        if (auditLogs.getList() == null) {
            auditLogs.setList(new ArrayList<>());
        }
        AuditLogTO auditLogData = new AuditLogTO();
        List<String> accountIds = new ArrayList<>();
        List<String> deviceIds = new ArrayList<>();
        List<String> tokenIds = new ArrayList<>();
        Gson gson = new Gson();
        getAccountIds(auditLogs.getList(), accountIds, deviceIds, tokenIds);
        System.out.println("accountIds : " + gson.toJson(accountIds));
        System.out.println("deviceIds : " + gson.toJson(deviceIds));
        System.out.println("tokenIds : " + gson.toJson(tokenIds));
        auditLogData.setAccountIds(accountIds);
        CountDownLatch latch = new CountDownLatch(3);
        EventLogWorker accountFilter = new EventLogWorker(latch, accountIds, "ACCOUNT", iamExtensionService);
        EventLogWorker deviceFilter = new EventLogWorker(latch, deviceIds, "DEVICE", iamExtensionService);
        EventLogWorker tokenFilter = new EventLogWorker(latch, tokenIds, "TOKEN", iamExtensionService);
        accountFilter.start();
        deviceFilter.start();
        tokenFilter.start();
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        /*AuditLogTO defaultAttributes = iamExtensionService.getDefaultAttributes(auditLogData);
        Map<String, DeviceWE> deviceMap = deviceIds.size() > 0 ? iamExtensionService.getDevices(deviceIds) : new HashMap<>();
        Map<String, TokenWE> tokenMap = tokenIds.size() > 0 ? iamExtensionService.getTokens(tokenIds) : new HashMap<>();*/
        AuditLogTO defaultAttributes = accountFilter.getDefaultAttributes();
        Map<String, DeviceWE> deviceMap = deviceFilter.getDeviceMap();
        Map<String, TokenWE> tokenMap = tokenFilter.getTokenMap();

        System.out.println("deviceMap : " + new Gson().toJson(deviceMap));
        System.out.println("tokenMap : " + new Gson().toJson(tokenMap));
        for (AuditLogTO auditLog : auditLogs.getList()) {
            auditLog.setAttributes(defaultAttributes.getAttributes());
            auditLog = AuditLogUtil.getEventLogFilter(auditLog, deviceMap, tokenMap).filter();
        }
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " getAuditLogs : end");
        return auditLogs;
    }

    @Override
    public String downloadAuditLogs(String queryParams) throws AuthException {
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " downloadAuditLogs : start");
        PaginatedTO<AuditLogTO> auditLogs = iamExtensionService.getAuditLogs(queryParams);
        if (auditLogs.getList() == null) {
            auditLogs.setList(new ArrayList<>());
        }
        StringWriter stringWriter = new StringWriter();
        CSVWriter writer = new CSVWriter(stringWriter, ',');
        String[] headers = { "origin",
                             "actionType",
                             "logData",
                             "creatorId",
                             "creatorIdType",
                             "createdTime",
                             "actedOnAccountId",
                             "attemptId",
                             "macId",
                             "osVersion",
                             "platform",
                             "deviceName",
                             "pushToken",
                             "simIdentifier",
                             "architecture",
                             "ipAddress",
                             "tokenUDID" };
        writer.writeNext(headers);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss:SS");
        List<String> accountIds = new ArrayList<>();
        List<String> deviceIds = new ArrayList<>();
        List<String> tokenIds = new ArrayList<>();
        getAccountIds(auditLogs.getList(), accountIds, deviceIds, tokenIds);
        CountDownLatch latch = new CountDownLatch(3);
        EventLogWorker accountFilter = new EventLogWorker(latch, accountIds, "ACCOUNT", iamExtensionService);
        EventLogWorker deviceFilter = new EventLogWorker(latch, deviceIds, "DEVICE", iamExtensionService);
        EventLogWorker tokenFilter = new EventLogWorker(latch, tokenIds, "TOKEN", iamExtensionService);
        accountFilter.start();
        deviceFilter.start();
        tokenFilter.start();
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        /*AuditLogTO defaultAttributes = iamExtensionService.getDefaultAttributes(auditLog);
        Map<String, DeviceWE> deviceMap = deviceIds.size() > 0 ? iamExtensionService.getDevices(deviceIds) : new HashMap<>();
        Map<String, TokenWE> tokenMap = tokenIds.size() >0 ? iamExtensionService.getTokens(tokenIds) : new HashMap<>();*/
        AuditLogTO defaultAttributes = accountFilter.getDefaultAttributes();
        Map<String, DeviceWE> deviceMap = deviceFilter.getDeviceMap();
        Map<String, TokenWE> tokenMap = tokenFilter.getTokenMap();
        for (AuditLogTO auditLogTO : auditLogs.getList()) {
            logger.log(Level.DEBUG, "auditLogTO : " + new Gson().toJson(auditLogTO));
            auditLogTO.setAttributes(defaultAttributes.getAttributes());
            String createdTime = simpleDateFormat.format(new Date(auditLogTO.getCreatedTime()));
            AuditLogTO auditLog = AuditLogUtil.getEventLogFilter(auditLogTO, deviceMap, tokenMap).filter();
            Map<String, String> deviceInfo = auditLogTO.getDeviceInfo() != null ? auditLogTO.getDeviceInfo() : new HashMap<>();
            String[] auditLogEntry = { auditLogTO.getOrigin(),
                                       auditLogTO.getActionType().name(),
                                       auditLogTO.getLogData(),
                                       auditLogTO.getCreatorId(),
                                       auditLogTO.getCreatorIdType() == null ? "" : auditLogTO.getCreatorIdType().name(),
                                       createdTime,
                                       auditLogTO.getActedOnAccountId(),
                                       auditLogTO.getAttemptId(),
                                       deviceInfo.get(DeviceMetadata.MAC_ID.name()),
                                       deviceInfo.get(DeviceMetadata.OS_VERSION.name()),
                                       deviceInfo.get(DeviceMetadata.PLATFORM.name()),
                                       deviceInfo.get(DeviceMetadata.DEVICE_NAME.name()),
                                       deviceInfo.get(DeviceMetadata.PUSH_TOKEN.name()),
                                       deviceInfo.get(DeviceMetadata.SIM_IDENTIFIER.name()),
                                       deviceInfo.get(DeviceMetadata.ARCHITECTURE.name()),
                                       deviceInfo.get(DeviceMetadata.IP_ADDRESS.name()),
                                       auditLogTO.getTokenUDID(),
            };
            writer.writeNext(auditLogEntry);
        }
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " downloadAuditLogs : end");
        return stringWriter.toString();
    }

    @Override
    public void verifyAuditLog(AuditLogTO auditLogTO) throws AuthException{
        iamExtensionService.verifyAuditLog(auditLogTO);
    }
    private void getAccountIds(List<AuditLogTO> eventLogs, List<String> accountIds, List<String> deviceIds, List<String> tokenIds) {
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " getAccountIds : start");
        Set<String> accounts = new HashSet<>();
        Set<String> devices = new HashSet<>();
        Set<String> tokens = new HashSet<>();
        for (AuditLogTO auditLogTO : eventLogs) {
            if (IdType.ACCOUNT.equals(auditLogTO.getCreatorIdType())) {
                if (auditLogTO.getCreatorId() != null) {
                    accounts.add(auditLogTO.getCreatorId());
                }
            }
            if (IdType.DEVICE.equals(auditLogTO.getCreatorIdType()) && auditLogTO.getCreatorId() != null) {
                devices.add(auditLogTO.getCreatorId());
             
            }
            if (IdType.TOKEN.equals(auditLogTO.getCreatorIdType()) && auditLogTO.getCreatorId() != null) {
                tokens.add(auditLogTO.getCreatorId());
            }
            if (auditLogTO.getActedOnAccountId() != null) {
                accounts.add(auditLogTO.getActedOnAccountId());
            }
        }
        Gson gson = new Gson();
        System.out.println("Set accounts : " + gson.toJson(accounts));
        System.out.println("Set devices : " + gson.toJson(devices));
        System.out.println("Set tokens : " + gson.toJson(tokens));
        accountIds.addAll(accounts);
        deviceIds.addAll(devices);
        tokenIds.addAll(tokens);
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " getAccountIds : end");
    }
    
    /*private List<AuditLogTO> addDeviceDetails(List<AuditLogTO> auditLogs) {
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " addDeviceDetails : start");
        logger.log(Level.DEBUG, "auditLogs Before : " + new Gson().toJson(auditLogs));
        for (AuditLogTO auditLog : auditLogs) {
            applyDeviceFilter(auditLog);
        }
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " addDeviceDetails : end");
        return auditLogs;
    }*/

    /* private void applyAccountFilter(AuditLogTO auditLogTO) {
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " applyAccountFilter : start");
        if (IdType.ACCOUNT.equals(auditLogTO.getCreatorIdType())) {
            String creatorId = auditLogTO.getCreatorId();
            creatorId = auditLogTO.getAttributes().get(creatorId) != null ? auditLogTO.getAttributes().get(auditLogTO.getCreatorId()) : auditLogTO.getCreatorId();
            auditLogTO.setCreatorId(creatorId);
            String actedOnAccountId = auditLogTO.getActedOnAccountId();
            actedOnAccountId = auditLogTO.getAttributes().get(actedOnAccountId) != null ? auditLogTO.getAttributes().get(auditLogTO.getActedOnAccountId()) : auditLogTO.getActedOnAccountId();
            auditLogTO.setActedOnAccountId(actedOnAccountId);
            logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " applyAccountFilter : end");
        }
    }*/

    /*private void applyDeviceFilter(AuditLogTO auditLogTO) {
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " applyDeviceFilter : start");
        if (IdType.DEVICE.equals(auditLogTO.getCreatorIdType())) {
            String deviceId = auditLogTO.getCreatorId();
            try {
                DeviceWE device = iamExtensionService.getDevice(deviceId);
                if (device.getId() == null) {
                    device = iamExtensionService.getDeviceByUdid(deviceId);
                }
                if (device != null) {
                    auditLogTO.setLogDetails(device.getDeviceDetails());
                }
            }
            catch (AuthException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " applyDeviceFilter : end");
    }*/

    /*private Map<String, String> getDeviceDetails(AuditLogTO auditLogTO) {
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " getDeviceDetails : start");
        List<DeviceDetailsTO> deviceDetails = new ArrayList<>();
        Map<String, String> deviceInfo = new HashMap<>();
        if (auditLogTO.getLogDetails() != null) {
            Type type = new TypeToken<List<DeviceDetailsTO>>() {
            }.getType();
            deviceDetails = new Gson().fromJson(auditLogTO.getLogDetails(), type);
            for (DeviceDetailsTO deviceDetail : deviceDetails) {
                deviceInfo.put(deviceDetail.getKey().name(), deviceDetail.getValue());
            }
        }
        logger.log(Level.DEBUG, AUDIT_LOGS_FACADE_IMPL_LOG + " getDeviceDetails : end");
        return deviceInfo;
    }*/

    public static void main(String[] args) {
        ServiceLoader<EventLogFilter> loader = ServiceLoader.load(EventLogFilter.class);
        System.out.println("loader : " + loader.toString());
        for (EventLogFilter implClass : loader) {
            System.out.println("Class : " + implClass.getClass().getSimpleName());
            System.out.println("Class : " + implClass.getClass().getCanonicalName());
        }
    }
}
