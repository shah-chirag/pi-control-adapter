
package in.fortytwo42.adapter.service;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.MapperNotFoundException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.MapperDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.Mapper;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.transferobj.MapperTO;

public class MapperServiceImpl implements MapperServiceIntf {

    private static final String MAPPER_SERVICE_LOG = "<<<<< MapperServiceImpl";
    private static Logger logger= LogManager.getLogger(MapperServiceImpl.class);
    private MapperDaoIntf mapperDao = DaoFactory.getMapperDao();
    
    private MapperServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final MapperServiceImpl INSTANCE = new MapperServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static MapperServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public Mapper create(Session session, MapperTO mapperTO) {
        logger.log(Level.DEBUG, MAPPER_SERVICE_LOG + " create : start");
        Mapper mapper = new Mapper();
        mapper.setKey(mapperTO.getKey());
        mapper.setValue(mapperTO.getValue());
        mapper = DaoFactory.getMapperDao().create(session, mapper);
        logger.log(Level.DEBUG, MAPPER_SERVICE_LOG + " create : end");
        return mapper;
    }
    
    @Override
    public PaginatedTO<MapperTO> getMappers(int page, int limit, String searchText){
        logger.log(Level.DEBUG, MAPPER_SERVICE_LOG + " getMappers : start");
        List<Mapper> mappers = mapperDao.getPaginatedList(page, limit, searchText);
        Long count = mapperDao.getTotalCount(searchText);
        PaginatedTO<MapperTO> mapperTOs = new PaginatedTO<>();
        mapperTOs.setList(new EntityToTOConverter<Mapper, MapperTO>().convertEntityListToTOList(mappers));
        mapperTOs.setTotalCount(count);
        logger.log(Level.DEBUG, MAPPER_SERVICE_LOG + " getMappers : end");
        return mapperTOs;
    }

    @Override
    public Mapper getById(Long id) throws MapperNotFoundException {
        logger.log(Level.DEBUG, MAPPER_SERVICE_LOG + " getById : start");
        Mapper mapper;
        try {
            mapper = mapperDao.getById(id);
        }
        catch (NotFoundException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            throw new MapperNotFoundException();
        }
        logger.log(Level.DEBUG, MAPPER_SERVICE_LOG + " getById : end");
        return mapper;
    }

}
