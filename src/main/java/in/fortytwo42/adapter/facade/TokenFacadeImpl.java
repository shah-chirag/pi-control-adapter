
package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.enums.State;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.TokenServiceIntf;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.tos.transferobj.UserTO;

public class TokenFacadeImpl implements TokenFacadeIntf {

    private static final String TOKEN_FACADE_IMPL_LOG = "<<<<< TokenFacadeImpl";

    private static Logger logger= LogManager.getLogger(TokenFacadeImpl.class);

    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private RequestServiceIntf requestService = ServiceFactory.getRequestService();

    private TokenServiceIntf tokenServiceIntf = ServiceFactory.getTokenService();
    
    private UserServiceIntf userServiceIntf = ServiceFactory.getUserService();

    private TokenFacadeImpl() {
        super();
    }

    private static final class InstanceHolder {

        private static final TokenFacadeImpl INSTANCE = new TokenFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static TokenFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public PaginatedTO<TokenTO> getTokens(Integer page, Integer pageSize) throws AuthException {
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " getTokens : start");
        PaginatedTO<TokenWE> tokens = iamExtensionService.getTokens(page, pageSize);
        PaginatedTO<TokenTO> adapterPaginatedTO = new PaginatedTO<>();
        adapterPaginatedTO.setList(getTokenTOList(tokens.getList()));
        adapterPaginatedTO.setTotalCount(tokens.getTotalCount());
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " getTokens : end");
        return adapterPaginatedTO;
    }

    private List<TokenTO> getTokenTOList(List<TokenWE> tokenWEs) {
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " getTokenTOList : start");
        List<TokenTO> adapterTokenTOs = new ArrayList<>();
        for (TokenWE tokenWE : tokenWEs) {
            TokenTO adapterTokenTO = convertTokenWEToTO(tokenWE);
            adapterTokenTOs.add(adapterTokenTO);
        }
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " getTokenTOList : end");
        return adapterTokenTOs;
    }
    
    private TokenTO convertTokenWEToTO(TokenWE tokenWE) {
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " convertTokenWEToTO : start");
        TokenTO adapterTokenTO = new TokenTO();
        adapterTokenTO.setTokenId(tokenWE.getId());
        adapterTokenTO.setTokenUDID(tokenWE.getTokenUDID());
        adapterTokenTO.setState(State.valueOf(tokenWE.getState()));
        adapterTokenTO.setType(tokenWE.getType());
        adapterTokenTO.setAccountIds(tokenWE.getAccountIds());
        adapterTokenTO.setDateTimeCreated(tokenWE.getDateTimeCreated());
        adapterTokenTO.setDateTimeModified(tokenWE.getDateTimeModified());
        if(tokenWE.getTokenName() != null) {
            adapterTokenTO.setTokenName(tokenWE.getTokenName());  
        }
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " convertTokenWEToTO : end");
        return adapterTokenTO;
    }

    @Override
    public TokenTO editToken(TokenTO tokenTO, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " editToken : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            tokenTO = requestService.createEditTokenRequest(session, tokenTO, actor,id, RequestType.EDIT_TOKEN,saveRequest);
            if (!saveRequest) {
                tokenTO = tokenServiceIntf.editToken(tokenTO);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " editToken : end");
        }
        return tokenTO;
    }

    @Override
    public TokenTO editTokenRemoteWipe(TokenTO tokenTO, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " editTokenRemoteWipe : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            logger.log(Level.DEBUG, "<<<<< tokenTO : "+new Gson().toJson(tokenTO));
            tokenTO = requestService.createEditTokenRequest(session, tokenTO, actor,id,
                    RequestType.EDIT_TOKEN_REMOTE_WIPE, saveRequest);
            if (!saveRequest) {
                tokenTO = tokenServiceIntf.editTokenRemoteWipe(tokenTO);
            }
            logger.log(Level.DEBUG, "<<<<< tokenTO after: "+new Gson().toJson(tokenTO));
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " editTokenRemoteWipe : end");
        }
        return tokenTO;
    }
    
    @Override
    public TokenTO getToken(String tokenId) throws AuthException {
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " getToken : start");
        TokenWE tokenWE = iamExtensionService.getToken(tokenId);
        TokenTO tokenTO = convertTokenWEToTO(tokenWE);
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " getToken : end");
        return tokenTO;
    }
    
    @Override
    public List<UserTO> getUserInfo(String tokenId) throws AuthException {
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " getUserInfo : start");
        TokenTO tokenTO = getToken(tokenId);
        List<UserTO> users = userServiceIntf.getUsersInfo(tokenTO.getAccountIds());
        logger.log(Level.DEBUG, TOKEN_FACADE_IMPL_LOG + " getUserInfo : end");
        return users;
    }

}
