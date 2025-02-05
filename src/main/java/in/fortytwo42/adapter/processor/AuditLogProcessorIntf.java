/**
 * 
 */
package in.fortytwo42.adapter.processor;

import java.util.List;

import in.fortytwo42.adapter.transferobj.AdapterAuditLogTO;
import in.fortytwo42.entities.enums.OtpAction;
import in.fortytwo42.entities.enums.OtpStatus;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public interface AuditLogProcessorIntf {

    AdapterAuditLogTO addAuditLog(AdapterAuditLogTO auditLogTO);
    
    void addAuditLogs(List<AdapterAuditLogTO> auditLogTO);

//    void sendAuditLog(String log, String origin, ActionType actionType, String creatorId, IdType creatorIdType, String reqRefNumber, String enterpriseAccountId, String actedOnAccountId,
//            String attemptId);
    void addOtpAuditSendLogs(String applicationId, List<AttributeDataTO> attributeList, OtpAction action, OtpStatus status, String seed);
    void addOtpAuditValidateLogs(String applicationId, List<AttributeDataTO> attributeList, OtpAction action, OtpStatus status, String seed);
}
