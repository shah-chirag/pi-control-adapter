
package in.fortytwo42.adapter.facade;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.ContactWeTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;

public interface ContactFacadeIntf {

    ContactWeTO onboardContact(ContactWeTO contactWE, String actor,Long id, boolean saveRequest) throws AuthException;

    ContactWeTO approveOnboardContact(Session session, ContactWeTO contactWE, String actor) throws AuthException;

    ContactWeTO editContact(ContactWeTO contactWE, String actor, Long id, boolean saveRequest) throws AuthException;

    ContactWeTO approveEditContact(Session session, ContactWeTO contactWE, String actor) throws AuthException;

    PaginatedTO<AccountWE> getAllContact(String accountId,String attributeValue, Integer page, Integer pageSize) throws AuthException;

    PaginatedTO<AccountWE> getAccountsWithAttributeValue(String accountId,String attributeValue, Integer page, Integer pageSize) throws AuthException;
}
