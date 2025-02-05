
package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.MapperServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.tos.transferobj.MapperTO;

public class MapperFacadeImpl implements MapperFacadeIntf {

    private static final String MAPPER_FACADE_LOG = "<<<<< MapperFacadeImpl";
    private static Logger logger= LogManager.getLogger(MapperFacadeImpl.class);
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private MapperServiceIntf mapperService = ServiceFactory.getMapperService();
    
    private MapperFacadeImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final MapperFacadeImpl INSTANCE = new MapperFacadeImpl();

        private InstanceHolder() {

        }
    }

    public static MapperFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    @Override
    public MapperTO createMapper(String role, String actor,Long id, boolean saveRequest, MapperTO mapperTO) throws AuthException {
        logger.log(Level.DEBUG, MAPPER_FACADE_LOG + " createMapper : start");
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
            logger.log(Level.DEBUG, MAPPER_FACADE_LOG + " createMapper : start");
        }
        return mapperTO;
    }

    @Override
    public MapperTO approveMapperCreateRequest(Session session, String actor, MapperTO mapperTO) {
        logger.log(Level.DEBUG, MAPPER_FACADE_LOG + " approveMapperCreateRequest : start");
        mapperTO = mapperService.create(session, mapperTO).convertToTO();
        logger.log(Level.DEBUG, MAPPER_FACADE_LOG + " approveMapperCreateRequest : end");
        return mapperTO;
    }

}
