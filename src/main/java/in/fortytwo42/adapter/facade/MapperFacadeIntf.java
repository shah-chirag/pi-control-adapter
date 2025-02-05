
package in.fortytwo42.adapter.facade;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.tos.transferobj.MapperTO;

public interface MapperFacadeIntf {

    MapperTO createMapper(String role, String actor,Long id, boolean saveRequest, MapperTO mapperTO) throws AuthException;

    MapperTO approveMapperCreateRequest(Session session, String actor, MapperTO mapperTO);

}
