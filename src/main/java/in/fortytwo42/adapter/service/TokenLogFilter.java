
package in.fortytwo42.adapter.service;

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.enterprise.extension.enums.IdType;
import in.fortytwo42.enterprise.extension.tos.AuditLogTO;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;

public class TokenLogFilter implements EventLogFilter<AuditLogTO> {

    private static final String TOKEN_LOG_FILTER_LOG = "<<<<< TokenLogFilter";
    private static Logger logger= LogManager.getLogger(TokenLogFilter.class);
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private AuditLogTO auditLogTO;
    Map<String, TokenWE> tokenMap;

    public TokenLogFilter(AuditLogTO auditLogTO, Map<String, TokenWE> tokenMap) {
        this.auditLogTO = auditLogTO;
        this.tokenMap = tokenMap;
    }

    public TokenLogFilter() {

    }

    @Override
    public AuditLogTO filter() {
        logger.log(Level.DEBUG, TOKEN_LOG_FILTER_LOG + " filter : start");
        if (IdType.TOKEN.equals(auditLogTO.getCreatorIdType())) {
            String tokenId = auditLogTO.getCreatorId();
            TokenWE token = tokenMap.get(tokenId);
            if (token != null) {
                TokenTO tokenTO = new TokenTO();
                tokenTO.setTokenUDID(token.getTokenUDID());
                auditLogTO.setTokenUDID(token.getTokenUDID());
                String tokenName = token.getTokenName() != null ? token.getTokenName() : token.getTokenUDID();
                tokenTO.setTokenName(tokenName);
                String actedOnAccountId = auditLogTO.getActedOnAccountId();
                if (actedOnAccountId != null) {
                    actedOnAccountId = auditLogTO.getAttributes().get(actedOnAccountId) != null ? auditLogTO.getAttributes().get(auditLogTO.getActedOnAccountId()) : auditLogTO.getActedOnAccountId();
                    auditLogTO.setActedOnAccountId(actedOnAccountId);
                }
                auditLogTO.setCreatorId(tokenName);
                auditLogTO.setLogDetails(new Gson().toJson(tokenTO));
            }
        }
        logger.log(Level.DEBUG, TOKEN_LOG_FILTER_LOG + " filter : end");
        return auditLogTO;
    }

}
