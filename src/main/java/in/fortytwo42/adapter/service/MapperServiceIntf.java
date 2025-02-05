package in.fortytwo42.adapter.service;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.MapperNotFoundException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.entities.bean.Mapper;
import in.fortytwo42.tos.transferobj.MapperTO;

public interface MapperServiceIntf {

    Mapper create(Session session, MapperTO mapperTO);

    PaginatedTO<MapperTO> getMappers(int page, int limit, String searchText);

    Mapper getById(Long id) throws MapperNotFoundException;

}
