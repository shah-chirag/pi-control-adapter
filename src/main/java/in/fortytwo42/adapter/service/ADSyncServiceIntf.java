package in.fortytwo42.adapter.service;

import java.io.IOException;

import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import org.hibernate.Session;

import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;

public interface ADSyncServiceIntf {

    void syncADUsers(Session session, IdentityProviderTO identityProviderTO) throws IOException;

    AdfsDetailsTO getAttributes(String userId, String password) throws IOException;

    AdfsDetailsTO getAttributesWithCustomerAD(String userId, String password, LdapDetailsTO ldapDetailsTO) throws IOException;
}
