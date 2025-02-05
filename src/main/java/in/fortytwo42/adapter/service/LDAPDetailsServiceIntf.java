package in.fortytwo42.adapter.service;

import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.LDAPDetails;
import in.fortytwo42.tos.transferobj.LdapDetailsTO;

import java.util.List;

public interface LDAPDetailsServiceIntf {

    LDAPDetails addLdapDetails(LdapDetailsTO ldapDetailsTO);

    LDAPDetails editLdapDetails(LdapDetailsTO ldapDetailsTO) throws NotFoundException;

    List<LdapDetailsTO> getPaginatedList(int pageNo, int limit, String searchText);

    long getPaginatedListCount( String searchText);

    LDAPDetails getLdapDetailsByDomainName(String domainName) throws NotFoundException;

    LDAPDetails getLdapDetailsByUserDomainName(String userDomainName) throws NotFoundException;
}
