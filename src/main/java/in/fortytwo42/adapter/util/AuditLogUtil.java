package in.fortytwo42.adapter.util;

import java.util.Map;

import in.fortytwo42.adapter.processor.AuditLogProcessorImpl;
import in.fortytwo42.adapter.processor.AuditLogProcessorIntf;
import in.fortytwo42.adapter.service.AccountLogFilter;
import in.fortytwo42.adapter.service.DeviceLogFilter;
import in.fortytwo42.adapter.service.EventLogFilter;
import in.fortytwo42.adapter.service.TokenLogFilter;
import in.fortytwo42.adapter.transferobj.AdapterAuditLogTO;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.enterprise.extension.webentities.DeviceWE;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;

public class AuditLogUtil {
    
    public static EventLogFilter<AuditLogTO> getEventLogFilter(AuditLogTO auditLogTO, Map<String, DeviceWE> deviceMap, Map<String, TokenWE> tokenMap) {
        EventLogFilter<AuditLogTO> eventLogFilter = null;
        if(auditLogTO.getCreatorIdType() ==  null){
            return new EventLogFilter<AuditLogTO>() {
                @Override
                public AuditLogTO filter() {
                    return auditLogTO;
                }
            };
        }
        switch (auditLogTO.getCreatorIdType()) {
            case ACCOUNT:
                eventLogFilter = new AccountLogFilter(auditLogTO);
                break;
            case DEVICE:
                eventLogFilter = new DeviceLogFilter(auditLogTO, deviceMap);
                break;
            case TOKEN:
                eventLogFilter = new TokenLogFilter(auditLogTO, tokenMap);
                break;
            default:

        }
        return eventLogFilter;
    }

    public static void sendAuditLog(String log, String origin, ActionType actionType, String creatorId, IdType creatorIdType, String reqRefNumber, String enterpriseAccountId, String actedOnAccountId, String attemptId) {
        AuditLogProcessorIntf auditLogProcessor = AuditLogProcessorImpl.getInstance();
        AdapterAuditLogTO auditLogTO = new AdapterAuditLogTO();
        auditLogTO.setLogData(log);
        auditLogTO.setOrigin(origin);
        auditLogTO.setActionType(actionType);
        auditLogTO.setCreatorId(creatorId);
        auditLogTO.setCreatorIdType(creatorIdType);
        auditLogTO.setReqRefNumber(reqRefNumber);
        auditLogTO.setActedOnAccountId(actedOnAccountId);
        auditLogTO.setAttemptId(attemptId);
        auditLogTO.setEnterpriseAccountId(enterpriseAccountId);
        auditLogProcessor.addAuditLog(auditLogTO);
    }
}
