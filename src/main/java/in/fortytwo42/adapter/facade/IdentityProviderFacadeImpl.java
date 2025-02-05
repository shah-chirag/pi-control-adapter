
package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.IdentityProviderServiceIntf;
import in.fortytwo42.adapter.service.MapperServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.enums.IdentityProviderType;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;
import in.fortytwo42.tos.transferobj.MapperTO;

public class IdentityProviderFacadeImpl implements IdentityProviderFacadeIntf {

    private static final String IDENTITY_PROVIDER_FACADE_LOG = "<<<<< IdentityProviderFacadeImpl";
    private static Logger logger= LogManager.getLogger(IdentityProviderFacadeImpl.class);
    private IdentityProviderServiceIntf identityProviderServiceIntf = ServiceFactory.IdentityProviderService();
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private MapperServiceIntf mapperService = ServiceFactory.getMapperService();
    
    private IdentityProviderFacadeImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final IdentityProviderFacadeImpl INSTANCE = new IdentityProviderFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static IdentityProviderFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public IdentityProviderTO createIdentityProvider(String actor,Long id, String role, boolean saveRequest,IdentityProviderTO identityProviderTO) throws AuthException {
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " createIdentityProvider : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            identityProviderTO = requestService.createIdentityProviderCreateRequest(session, identityProviderTO,actor,id, saveRequest);
            if (!saveRequest) {
                identityProviderTO = approveIdentityProviderRequest(session, actor, identityProviderTO);
            }
            identityProviderTO.setStatus(Constant.SUCCESS_STATUS);
            identityProviderTO.setId(identityProviderTO.getId());
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
            logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " createIdentityProvider : end");
        }
        return identityProviderTO;
    }

    @Override
    public IdentityProviderTO approveIdentityProviderRequest(Session session, String actor, IdentityProviderTO identityProviderTO) throws AuthException {
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " approveApplicationOnboard : start");
        identityProviderTO = identityProviderServiceIntf.createIdentityProvider(session, identityProviderTO);
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " approveApplicationOnboard : end");
        return identityProviderTO;
    }

    @Override
    public PaginatedTO<IdentityProviderTO> getProviders(String role, int pageNo, String searchText) throws AuthException {
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " getProviders : start");
        int limit = Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT));
        PaginatedTO<IdentityProviderTO> identityProvideTOs = identityProviderServiceIntf.getProviders(pageNo, limit, searchText);
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " getProviders : end");
        return identityProvideTOs;
    }
    
    @Override
    public void syncUsers(String identityProviderType) throws AuthException {
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " syncUsers : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            identityProviderServiceIntf.syncUsers(session, IdentityProviderType.valueOf(identityProviderType));
            sessionFactoryUtil.closeSession(session);
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " syncUsers : end");
        }
    }

    @Override
    public MapperTO createMapper(String actor,Long id, String role, boolean saveRequest, MapperTO mapperTO) throws AuthException {
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " createMapper : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            mapperTO = requestService.createMapperCreateRequest(session, mapperTO, actor,id, saveRequest);
            if (!saveRequest) {
                mapperTO = approveMapperCreateRequest(session, actor, mapperTO);
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
            logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " createMapper : start");
        }
        return mapperTO;
    }
    
    @Override
    public MapperTO approveMapperCreateRequest(Session session, String actor, MapperTO mapperTO) {
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " approveMapperCreateRequest : start");
        mapperTO = mapperService.create(session, mapperTO).convertToTO();
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " approveMapperCreateRequest : end");
        return mapperTO;
    }
    
    @Override
    public PaginatedTO<MapperTO> getMappers(String role, int pageNo, String searchText) throws AuthException {
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " getMappers : start");
        int limit = Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT));
        PaginatedTO<MapperTO> mapperTOTOs = mapperService.getMappers(pageNo, limit, searchText);
        logger.log(Level.DEBUG, IDENTITY_PROVIDER_FACADE_LOG + " getMappers : end");
        return mapperTOTOs;
    }
    
/*    public static void main(String[] args) {
        String time = "20220421083728.0Z";
        DateFormat inputDateFormat  = new SimpleDateFormat("yyyyMMddHHmmss'.0Z'");
        DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
        try {
            String output = outputDateFormat.format(inputDateFormat.parse(time));
            System.out.println("Output : "+output);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

    }*/
}
