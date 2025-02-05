package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.tos.transferobj.LdapDetailsTO;

public interface LDAPDetailsFacadeIntf {

    LdapDetailsTO addLdapDetails(LdapDetailsTO ldapDetailsTO) throws AuthException;

     LdapDetailsTO createLdapDetailsRequest(Long id ,String role, String actor, LdapDetailsTO ldapDetailsTO, boolean saveRequest) throws AuthException;


    LdapDetailsTO editLdapDetails(Long id,String role, String actor, LdapDetailsTO ldapDetailsTO, boolean saveRequest) throws AuthException;

    LdapDetailsTO editLdapDetail(LdapDetailsTO ldapDetailsTO) throws AuthException;

    PaginatedTO<LdapDetailsTO> getLdapDetails(int pageNo, String searchText);
}
