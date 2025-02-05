
package in.fortytwo42.adapter.service;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.transferobj.ADUserTO;
import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.adapter.util.ADSyncUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.entities.bean.IdentityProvider;
import in.fortytwo42.entities.bean.ThirdParties;
import in.fortytwo42.entities.enums.IdentityProviderType;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;

public class ADSyncServiceImpl implements ADSyncServiceIntf {

    private static final String AD_SYNC_SERVICE_LOG = "<<<<< ADSyncServiceImpl";
    private static Logger logger= LogManager.getLogger(ADSyncServiceImpl.class);

    private ADConnectServiceIntf adConnectService = ServiceFactory.getADConnectService();

    private ADSyncServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final ADSyncServiceImpl INSTANCE = new ADSyncServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static ADSyncServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void syncADUsers(Session session, IdentityProviderTO identityProviderTO) throws IOException {
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " syncADUsers : start");
        //IdentityProviderTO identityProviderTO = identityProider.convertToTO();
        long processInitTime = System.currentTimeMillis();
        String adDomain = ADSyncUtil.getADDomain(identityProviderTO.getUserDomain());
        String adAdmin = ADSyncUtil.getADAdmin(identityProviderTO.getAdminDomain());
        String connectionUrl = identityProviderTO.getConnectionUrl();
        String adPassword = ADSyncUtil.getADPassword(identityProviderTO.getAdminCredential(), identityProviderTO.getAuthenticationType());
        String[] userAttributes = identityProviderTO.getUserAttributes().split(",");
        LdapContext context = ServiceFactory.getADConnectService().getLDAPContextForUser(adAdmin, adPassword, adDomain, connectionUrl, Constant.SIMPLE);
        if (context != null) {
            try {
                String authenticatedADUser = (String) context.getEnvironment().get(Context.SECURITY_PRINCIPAL);
                logger.log(Level.DEBUG, "******** authenticatedADUser : "+authenticatedADUser);
                if (authenticatedADUser.contains("@")) {
                    String domainName = authenticatedADUser.substring(authenticatedADUser.indexOf('@') + 1);
                    SearchControls controls = new SearchControls();
                    controls.setSearchScope(SUBTREE_SCOPE);
                    int pageSize = 500;
                    PagedResultsControl pagedResultsControl = new PagedResultsControl(pageSize, Control.CRITICAL);
                    context.setRequestControls(new Control[] { pagedResultsControl });
                    controls.setReturningAttributes(userAttributes);
                    byte[] cookie = null;
                    String lastSyncTime = identityProviderTO.getLastSyncTime();
                    String filter;
                    filter = ADSyncUtil.getFilters(identityProviderTO.getFilters());
                    //identityProider = DaoFactory.getIdentityProvideDao().update(session, identityProider);
                    saveAllADUsers(session, context, domainName, controls, pageSize, cookie, filter, lastSyncTime, processInitTime, identityProviderTO);
                    context.close();
                }
            }
            catch (Exception e) {
                logger.log(Level.FATAL, e);
                throw new IOException();
            }
            logger.log(Level.DEBUG, "Sync success");
        }
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " syncADUsers : end");
    }

    private void saveAllADUsers(Session session, LdapContext context, String domainName, SearchControls controls, int pageSize, byte[] cookie, String filter, String lastSyncTime, long processInitTime, IdentityProviderTO identityProider)
            throws NamingException, IOException {
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " saveAllADUsers : start");
        int page = 1;
        do {
            List<ADUserTO> users = new ArrayList<>();
            NamingEnumeration<?> answer = context.search(ActiveDirectory.toDC(domainName), filter, controls);
            validateADUserData(users, answer);
            if (!users.isEmpty()) {
                // TOD : Insert into third parties table
                insertIntoThirdPartiesTable(session, users, identityProider, processInitTime);
                for (ADUserTO adUser : users) {
                    logger.log(Level.DEBUG, " Ad User : " + new Gson().toJson(adUser));
                }
            }
            Control[] control = context.getResponseControls();
            if (control != null) {
                for (int i = 0; i < control.length; i++) {
                    if (control[i] instanceof PagedResultsResponseControl) {
                        PagedResultsResponseControl prrc = (PagedResultsResponseControl) control[i];
                        cookie = prrc.getCookie();
                    }
                }
            }

            context.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });
            page++;
        } while (cookie != null);
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " saveAllADUsers : end");
    }

    private void insertIntoThirdPartiesTable(Session session, List<ADUserTO> users, IdentityProviderTO identityProviderTO, long processInitTime) {
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " insertIntoThirdPartiesTable : start");
        List<ThirdParties> thirdPartiesUsers = new ArrayList<>();
        IdentityProvider identityProvider = DaoFactory.getIdentityProvideDao().getByType(IdentityProviderType.valueOf(identityProviderTO.getType()));
        for (int i = 0; i < users.size(); i++) {
            ADUserTO adUserTO = users.get(i);
            ThirdParties thirdPartiesUser = new ThirdParties();
            String userPrincipal = adUserTO.getUserPrincipal().split("@")[0];
            thirdPartiesUser.setUsername(userPrincipal.toUpperCase());
            thirdPartiesUser.setLocation(adUserTO.getLocation());
            thirdPartiesUser.setFullName(adUserTO.getCommonName());
            if (adUserTO.getMobile() != null) {
                thirdPartiesUser.setMobile(adUserTO.getMobile().replaceAll("[\\s-]", "").trim());
            }
            thirdPartiesUser.setFirstName(adUserTO.getFirstName());
            thirdPartiesUser.setLastName(adUserTO.getLastName());
            thirdPartiesUser.setEmail(adUserTO.getEmail());
            thirdPartiesUser.setWhenChanged(adUserTO.getWhenChanged());
            if (adUserTO.getCo() != null) {
                thirdPartiesUser.setCountry(adUserTO.getCo());
            }
            thirdPartiesUser.setProcessDateTime(new Timestamp(processInitTime));
            thirdPartiesUser.setIdentityProvider(identityProvider);
            thirdPartiesUsers.add(thirdPartiesUser);
        }
        DaoFactory.getThirdPartiesDao().bulkInsert(session, thirdPartiesUsers);
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " insertIntoThirdPartiesTable : end");
    }

    private void validateADUserData(List<ADUserTO> users, NamingEnumeration<?> answer) throws NamingException {
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " validateADUserData : start");
        try {
            while (answer.hasMore()) {
                Attributes attr = ((SearchResult) answer.next()).getAttributes();
                Attribute user = attr.get(Constant.USER_PRINCIPLE);
                if (user != null) {
                    String userPrincipal = (String) attr.get(Constant.USER_PRINCIPLE).get();
                    if (validateUsername(userPrincipal)) {
                        users.add(new ADUserTO(attr));
                    }
                }
            }
        }
        catch (PartialResultException e) {
            // Ignored
        }
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " validateADUserData : end");
    }

    private static boolean validateUsername(String username) {
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " validateUsername : start");
        String adUsernamePattern = Config.getInstance().getProperty(Constant.AD_USERNAME_PATTERN);
        if (adUsernamePattern == null || adUsernamePattern.isEmpty()) {
            return true;
        }
        Pattern pattern = Pattern.compile(adUsernamePattern);
        boolean isUsernameValid = pattern.matcher(username.split("@")[0]).matches();
        logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " validateUsername : end");
        return isUsernameValid;

    }

    @Override
    public AdfsDetailsTO getAttributes(String userId, String password) throws IOException {
        try {
            logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " getAttributes : start");
            LdapContext context = adConnectService.getLDAPContextForUser(userId, password, Config.getInstance().getProperty(Constant.AD_DOMAIN), Config.getInstance().getProperty(Constant.AD_CONNECTION_URL), Constant.SIMPLE);
            if (context != null) {
                String authenticatedADUser = (String) context.getEnvironment().get(Context.SECURITY_PRINCIPAL);

                if (authenticatedADUser.contains("@")) {
                    String domainName = Config.getInstance().getProperty(Constant.AD_DOMAIN);
                    String[] attrIDs = { "msDS-PhoneticDisplayName",
                                         "lastLogonTimestamp",
                                         "distinguishedName",
                                         "givenname",
                                         "mail",
                                         "extensionAttribute13",
                                         "mobile",
                                         "accountExpires",
                                         "pwdLastSet",
                                         "logonWorkstation",
                                         "otherMobile",
                                         "whenChanged", "userPrincipalName", "cn"};
                    String filter = "sAMAccountName=" + userId;

                    SearchControls controls = new SearchControls();
                    controls.setSearchScope(SUBTREE_SCOPE);
                    controls.setReturningAttributes(attrIDs);

                    List<ADUserTO> users = new ArrayList<>();
                    NamingEnumeration<SearchResult> answer = null;
                    try {
                         answer = context.search(ActiveDirectory.toDC(domainName), filter, controls);
                    } catch (NamingException e){
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                    if(answer!=null) {
                        while (answer.hasMore()) {
                            Attributes attr = answer.next().getAttributes();
                            Attribute user = attr.get(Constant.USER_PRINCIPLE);
                            if (user != null) {
                                users.add(new ADUserTO(attr));
                            }
                        }

                        context.close();
                        if (!users.isEmpty()) {
                            AdfsDetailsTO adfsDetailsTO = new AdfsDetailsTO();
                            adfsDetailsTO.setEmail(users.get(0).getEmail());
                            adfsDetailsTO.setMobile(users.get(0).getMobile());
                            logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " getAttributes : end");
                            return adfsDetailsTO;

                        }
                    }
                }
            }
            logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " getAttributes : end");
            throw new IOException();
        }
        catch (Exception e) {
            logger.log(Level.FATAL, e);
            throw new IOException();
        }
    }

//    public static void main(String args[]) {
//        try {
//            AdfsDetailsTO adfsDetailsTO = getInstance().getAttributes("giridhararr","ft42@123$");
//            System.out.println("---> "+adfsDetailsTO.getEmail()+" "+adfsDetailsTO.getMobile());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }


    @Override
    public AdfsDetailsTO getAttributesWithCustomerAD(String userId, String password, LdapDetailsTO ldapDetailsTO) throws IOException {
        try {
            logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " getAttributes : start");
            LdapContext context = adConnectService.getLDAPContextForUser(userId, password, ldapDetailsTO.getDomainName(), ldapDetailsTO.getConnectionUrl(), Constant.SIMPLE);
            if (context != null) {
                String authenticatedADUser = (String) context.getEnvironment().get(Context.SECURITY_PRINCIPAL);

                if (authenticatedADUser.contains("@")) {
                    String domainName = ldapDetailsTO.getDomainName();
                    String[] attrIDs = { "msDS-PhoneticDisplayName",
                            "lastLogonTimestamp",
                            "distinguishedName",
                            "givenname",
                            "mail",
                            "extensionAttribute13",
                            "mobile",
                            "accountExpires",
                            "pwdLastSet",
                            "logonWorkstation",
                            "otherMobile",
                            "whenChanged", "userPrincipalName", "cn"};
                    String filter = "sAMAccountName=" + userId;

                    SearchControls controls = new SearchControls();
                    controls.setSearchScope(SUBTREE_SCOPE);
                    controls.setReturningAttributes(attrIDs);

                    List<ADUserTO> users = new ArrayList<>();
                    NamingEnumeration<SearchResult> answer = null;
                    try {
                        answer = context.search(ActiveDirectory.toDC(domainName), filter, controls);
                    } catch (NamingException e){
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                    if(answer!=null) {
                        while (answer.hasMore()) {
                            Attributes attr = answer.next().getAttributes();
                            Attribute user = attr.get(Constant.USER_PRINCIPLE);
                            if (user != null) {
                                users.add(new ADUserTO(attr));
                            }
                        }

                        context.close();
                        if (!users.isEmpty()) {
                            AdfsDetailsTO adfsDetailsTO = new AdfsDetailsTO();
                            adfsDetailsTO.setEmail(users.get(0).getEmail());
                            adfsDetailsTO.setMobile(users.get(0).getMobile());
                            logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " getAttributes : end");
                            return adfsDetailsTO;

                        }
                    }
                }
            }
            logger.log(Level.DEBUG, AD_SYNC_SERVICE_LOG + " getAttributes : end");
            throw new IOException();
        }
        catch (Exception e) {
            logger.log(Level.FATAL, e);
            throw new IOException();
        }
    }
}
