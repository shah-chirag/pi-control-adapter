
package in.fortytwo42.adapter.service;

import javax.naming.ldap.LdapContext;

public interface ADConnectServiceIntf {

    LdapContext getLDAPContextForUser(String username, String password, String domain, String connectionUrl, String authType);

}
