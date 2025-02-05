package in.fortytwo42.adapter.facade;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;
import in.fortytwo42.tos.transferobj.MapperTO;

public interface IdentityProviderFacadeIntf {

    IdentityProviderTO createIdentityProvider(String actor,Long id, String role, boolean saveRequest,IdentityProviderTO identityProviderTO) throws AuthException;

    IdentityProviderTO approveIdentityProviderRequest(Session session, String actor, IdentityProviderTO identityProviderTO) throws AuthException;

    PaginatedTO<IdentityProviderTO> getProviders(String role, int pageNo, String searchText) throws AuthException;

    void syncUsers(String identityProviderType) throws AuthException;

    MapperTO createMapper(String actor,Long id, String role, boolean saveRequest, MapperTO mapperTO) throws AuthException;

    MapperTO approveMapperCreateRequest(Session session, String actor, MapperTO mapperTO);

    PaginatedTO<MapperTO> getMappers(String role, int pageNo, String searchText) throws AuthException;

}
