
package in.fortytwo42.adapter.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.enterprise.extension.enums.IdType;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;

public class AccountLogFilter implements EventLogFilter<AuditLogTO> {

    private static final String ACCOUNT_LOG_FILTER_LOG = "<<<<< AccountLogFilter";
    private static Logger logger= LogManager.getLogger(AccountLogFilter.class);
    private AuditLogTO auditLogTO;

    public AccountLogFilter(AuditLogTO auditLogTO) {
        this.auditLogTO = auditLogTO;
    }

    public AccountLogFilter() {
        
    }
    
    @Override
    public AuditLogTO filter() {
        logger.log(Level.DEBUG, ACCOUNT_LOG_FILTER_LOG + " filter : start");
        if (IdType.ACCOUNT.equals(auditLogTO.getCreatorIdType())) {
            String creatorId = auditLogTO.getCreatorId();
            creatorId = auditLogTO.getAttributes().get(creatorId) != null ? auditLogTO.getAttributes().get(auditLogTO.getCreatorId()) : auditLogTO.getCreatorId();
            auditLogTO.setCreatorId(creatorId);
            String actedOnAccountId = auditLogTO.getActedOnAccountId();
            actedOnAccountId = auditLogTO.getAttributes().get(actedOnAccountId) != null ? auditLogTO.getAttributes().get(auditLogTO.getActedOnAccountId()) : auditLogTO.getActedOnAccountId();
            auditLogTO.setActedOnAccountId(actedOnAccountId);
            AttributeDataTO attributeData = new AttributeDataTO();
            attributeData.setAttributeName(Constant.USER_ID);
            attributeData.setAttributeValue(actedOnAccountId);
            String logDetails = new Gson().toJson(attributeData);
            auditLogTO.setLogDetails(logDetails);
        }
        logger.log(Level.DEBUG, ACCOUNT_LOG_FILTER_LOG + " filter : end");
        return auditLogTO;
    }

}
