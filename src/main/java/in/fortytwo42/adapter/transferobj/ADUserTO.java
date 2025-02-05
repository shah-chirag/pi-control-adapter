/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * @author ChiragShah
 *
 */
@JsonInclude(value = Include.NON_NULL)
public class ADUserTO {
    private String distinguishedName;
    private String userPrincipal;
    private String commonName;
    private String mobile;
    private String location;
    private String whenChanged;
    private String firstName;
    private String lastName;
    private String email;
    private String co;

    private static Logger logger= LogManager.getLogger(ADUserTO.class);
    

    public ADUserTO() {
        super();
    }

    public ADUserTO(Attributes attr) throws javax.naming.NamingException {
        userPrincipal = (String) attr.get("userPrincipalName").get();
        commonName = (String) attr.get("cn").get();
        distinguishedName = (String) attr.get("distinguishedName").get();
        if (attr.get("mobile") != null) {
            mobile = (String) attr.get("mobile").get();
        }
        if(attr.get("l") != null) {
            location = (String)attr.get("l").get();
        }
        if(attr.get("whenChanged") != null) {
            whenChanged = (String)attr.get("whenChanged").get();
        }
        if(attr.get("givenName") != null) {
            firstName = (String)attr.get("givenName").get();
        }
        if(attr.get("sn") != null) {
            lastName = (String)attr.get("sn").get();
        }
        if(attr.get("mail") != null) {
            email = (String)attr.get("mail").get();
        }
        if (attr.get("co") != null) {
            co = (String) attr.get("co").get();
        }
    }
    
    public void setUserPrincipal(String user) {
        this.userPrincipal = user + "@hjkbnm";
    }
    
    public String getUserPrincipal() {
        return userPrincipal;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobileNumber(String mobile) {
        this.mobile = mobile;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWhenChanged() {
        return whenChanged;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static HostnameVerifier getDoNotVerify() {
        return DO_NOT_VERIFY;
    }

    public static TrustManager[] getTrustAllCerts() {
        return getTrustManager;
    }

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    /**
     * Used to change the user password. Throws an IOException if the Domain
     * Controller is not LDAPS enabled.
     * 
     * @param trustAllCerts
     *            If true, bypasses all certificate and host name validation. If
     *            false, ensure that the LDAPS certificate has been imported into a
     *            trust store and sourced before calling this method. Example:
     *            String keystore =
     *            "/usr/java/jdk1.5.0_01/jre/lib/security/cacerts";
     *            System.setProperty("javax.net.ssl.trustStore",keystore);
     */
    public void changePassword(String oldPass, String newPass, boolean trustAllCerts, LdapContext context)
            throws java.io.IOException, NamingException {
        String dn = getDistinguishedName();

        // Switch to SSL/TLS
        StartTlsResponse tls = null;
        try {
            tls = (StartTlsResponse) context.extendedOperation(new StartTlsRequest());
        }
        catch (Exception e) {
            // "Problem creating object: javax.naming.ServiceUnavailableException: [LDAP:
            // error code 52 - 00000000: LdapErr: DSID-0C090E09, comment: Error initializing
            // SSL/TLS, data 0, v1db0"
            throw new java.io.IOException(
                    "Failed to establish SSL connection to the Domain Controller. Is LDAPS enabled?");
        }

        // Exchange certificates
        if (trustAllCerts) {
            tls.setHostnameVerifier(DO_NOT_VERIFY);
            SSLSocketFactory sf = null;
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, getTrustManager, null);
                sf = sc.getSocketFactory();
            }
            catch (java.security.NoSuchAlgorithmException | java.security.KeyManagementException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            tls.negotiate(sf);
        }
        else {
            tls.negotiate();
        }

        // Change password
        try {
            // ModificationItem[] modificationItems = new ModificationItem[1];
            // modificationItems[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new
            // BasicAttribute("unicodePwd", getPassword(newPass)));

            ModificationItem[] modificationItems = new ModificationItem[2];
            modificationItems[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                    new BasicAttribute("unicodePwd", getPassword(oldPass)));
            modificationItems[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                    new BasicAttribute("unicodePwd", getPassword(newPass)));
            context.modifyAttributes(dn, modificationItems);
        }
        catch (javax.naming.directory.InvalidAttributeValueException e) {
            String error = e.getMessage().trim();
            if (error.startsWith("[") && error.endsWith("]")) {
                error = error.substring(1, error.length() - 1);
            }
            logger.log(Level.ERROR, error);
            tls.close();
            throw new NamingException("New password does not meet Active Directory requirements. "
                                      + "Please ensure that the new password meets password complexity, "
                                      + "length, minimum password age, and password history requirements.");
        }
        catch (NamingException e) {
            tls.close();
            throw e;
        }

        // Close the TLS/SSL session
        tls.close();
    }

    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static TrustManager[] getTrustManager = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
    } };

    private byte[] getPassword(String newPass) {
        String quotedPassword = "\"" + newPass + "\"";
        char[] unicodePwd = quotedPassword.toCharArray();
        byte[] pwdArray = new byte[unicodePwd.length * 2];
        for (int i = 0; i < unicodePwd.length; i++) {
            pwdArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
            pwdArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
        }
        return pwdArray;
    }

    @Override
    public String toString() {
        return "ADUserTO [distinguishedName=" + distinguishedName
               + ", userPrincipal="
               + userPrincipal
               + ", commonName="
               + commonName
               + ", mobile="
               + mobile
               + ", location="
               + location
               + ", whenChanged="
               + whenChanged
               + ", firstName="
               + firstName
               + ", lastName="
               + lastName
               + ", email="
               + email
               + "]";
    }

}
