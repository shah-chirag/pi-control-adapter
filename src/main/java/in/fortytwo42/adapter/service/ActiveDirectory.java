
package in.fortytwo42.adapter.service;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ActiveDirectory {

    private static Logger logger= LogManager.getLogger(ActiveDirectory.class);
    private static final String ACTIVE_DIRECTORY = "<<<<< ActiveDirectory";

    public static LdapContext getConnection(String username, String password, String domainName, String ldapURL, String authType) throws NamingException {
        logger.log(Level.DEBUG, ACTIVE_DIRECTORY + " getConnection : start");
        Hashtable<String, String> props = new Hashtable<String, String>();
        String principalName = username + "@" + domainName;
        props.put(Context.SECURITY_PRINCIPAL, principalName);
        props.put(Context.SECURITY_AUTHENTICATION, authType);
        if (password != null) {
            props.put(Context.SECURITY_CREDENTIALS, password);
        }
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, ldapURL);
        props.put(Context.REFERRAL, "follow");
        try {
            return new InitialLdapContext(props, null);
        }
        catch (javax.naming.CommunicationException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new NamingException("Failed to connect to " + domainName + ((ldapURL == null) ? "" : " through " + ldapURL));
        }
        catch (NamingException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new NamingException("Failed to authenticate " + username + "@" + domainName + ((ldapURL == null) ? "" : " through " + ldapURL));
        }
        finally {
            logger.log(Level.DEBUG, ACTIVE_DIRECTORY + " getConnection : end");
        }

    }

    public static String toDC(String domainName) {
        logger.log(Level.DEBUG, ACTIVE_DIRECTORY + " toDC : start");
        StringBuilder buf = new StringBuilder();
        for (String token : domainName.split("\\.")) {
            if (token.length() == 0)
                continue; // defensive check
            if (buf.length() > 0)
                buf.append(",");
            buf.append("DC=").append(token);
        }
        String dc = buf.toString();
        logger.log(Level.DEBUG, ACTIVE_DIRECTORY + " dc : "+dc);
        logger.log(Level.DEBUG, ACTIVE_DIRECTORY + " toDC : start");
        return dc;
    }
    
    public static void main(String[] args) throws NamingException {
        ActiveDirectory.getConnection("vivek", "ft42@12345678$", "azdomain.local", "ldap://20.244.12.76:389", "simple");
    }

}
