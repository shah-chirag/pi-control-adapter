
package in.fortytwo42.adapter.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;

public class TokenServiceImpl implements TokenServiceIntf {

    private static final String TOKEN_SERVICE_IMPL_LOG = "<<<<< TokenServiceImpl";
    private static Logger logger= LogManager.getLogger(TokenServiceImpl.class);

    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private TokenServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final TokenServiceImpl INSTANCE = new TokenServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static TokenServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public TokenTO editToken(TokenTO tokenTO) throws AuthException {
        logger.log(Level.DEBUG, TOKEN_SERVICE_IMPL_LOG + " editToken : start");
        AuditLogUtil.sendAuditLog(tokenTO.getTokenName()+ " edit token request approved ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        tokenTO = iamExtensionService.editToken(tokenTO);
        AuditLogUtil.sendAuditLog(tokenTO.getTokenName()+ " token edited successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, TOKEN_SERVICE_IMPL_LOG + " editToken : end");
        return tokenTO;
    }

    @Override
    public TokenTO editTokenRemoteWipe(TokenTO tokenTO) throws AuthException {
        logger.log(Level.DEBUG, TOKEN_SERVICE_IMPL_LOG + " editTokenRemoteWipe : start");
        AuditLogUtil.sendAuditLog(tokenTO.getTokenName()+ " edit token remote wipe request approved ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        tokenTO = iamExtensionService.editTokenRemoteWipe(tokenTO);
        AuditLogUtil.sendAuditLog(tokenTO.getTokenName()+ "token remote wipe edited successfully ", "ENTERPRISE", ActionType.EDIT_ACCOUNT, "", IdType.ACCOUNT, "", "", "", null);
        logger.log(Level.DEBUG, TOKEN_SERVICE_IMPL_LOG + " editTokenRemoteWipe : end");
        return tokenTO;
    }
}
