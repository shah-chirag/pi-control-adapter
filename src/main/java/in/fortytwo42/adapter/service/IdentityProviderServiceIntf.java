
package in.fortytwo42.adapter.service;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.entities.enums.IdentityProviderType;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;

public interface IdentityProviderServiceIntf {

    IdentityProviderTO getIdentityProviderByType(IdentityProviderType identityProviderType);

    IdentityProviderTO createIdentityProvider(Session session, IdentityProviderTO identityProviderTO) throws AuthException;

    PaginatedTO<IdentityProviderTO> getProviders(int pageNo, int limit, String searchText);

    void syncUsers(Session session, IdentityProviderType identityProviderType) throws AuthException;

}
