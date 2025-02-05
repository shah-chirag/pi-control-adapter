
package in.fortytwo42.adapter.service;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ADConnectServiceImpl implements ADConnectServiceIntf {

    private static final String AD_CONNECT_SERVICE_LOG = "<<<<< ADConnectServiceImpl";

    private static Logger logger= LogManager.getLogger(ADConnectServiceImpl.class);

    private ADConnectServiceImpl() {
        super();
    }

    private static final class InstanceHolder {

        private static final ADConnectServiceImpl INSTANCE = new ADConnectServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static ADConnectServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public LdapContext getLDAPContextForUser(String username, String password, String domain, String connectionUrl, String authType) {
        logger.log(Level.DEBUG, AD_CONNECT_SERVICE_LOG + " getLDAPContextForUser : start");
        try {
            LdapContext ldapContext = ActiveDirectory.getConnection(username, password, domain, connectionUrl, authType);
            if (ldapContext != null) {
                return ldapContext;
            }
        }
        catch (NamingException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        logger.log(Level.DEBUG, AD_CONNECT_SERVICE_LOG + " getLDAPContextForUser : end");
        return null;
    }

}
