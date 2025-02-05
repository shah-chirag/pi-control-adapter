package in.fortytwo42.adapter.service;

import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.LDAPDetailsDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.LDAPDetails;
import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class LDAPDetailsServiceImpl implements LDAPDetailsServiceIntf{

    SessionFactoryUtil sessionFactoryUtil= SessionFactoryUtil.getInstance();

    LDAPDetailsDaoIntf ldapDetailsDaoIntf = DaoFactory.getLDAPDetailsDao();

    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(LDAPDetailsServiceImpl.class);

    private String LDAP_DETAILS_SERVICE_IMPL = "<<<<<<< LDAPDetailsServiceImpl";


    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final LDAPDetailsServiceImpl INSTANCE = new LDAPDetailsServiceImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of LDAPDetailsServiceImpl.
     *
     * @return single instance of LDAPDetailsServiceImpl
     */
    public static LDAPDetailsServiceImpl getInstance() {
        return LDAPDetailsServiceImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public LDAPDetails addLdapDetails(LdapDetailsTO ldapDetailsTO) {
        logger.log(Level.DEBUG,LDAP_DETAILS_SERVICE_IMPL+"addLdapDetails : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            LDAPDetails ldapDetails = new LDAPDetails();
            if(ldapDetailsTO.getConnectionUrl()!=null){
                ldapDetails.setConnectionUrl(ldapDetailsTO.getConnectionUrl());
            }
            if(ldapDetailsTO.getDomainName()!=null){
                ldapDetails.setDomainName(ldapDetailsTO.getDomainName().toLowerCase());
            }
            if(ldapDetailsTO.getUserDomainName()!=null){
                ldapDetails.setUserDomainName(ldapDetailsTO.getUserDomainName());
            }
            if(ldapDetailsTO.getIsSslEnabled()!=null){
                ldapDetails.setSslEnabled(ldapDetailsTO.getIsSslEnabled());
            }
            if(ldapDetailsTO.getClientAddress()!=null){
                ldapDetails.setClientAddress(ldapDetailsTO.getClientAddress());
            }
            LDAPDetails ldapDetails1=  ldapDetailsDaoIntf.create(ldapDetails,session);
            session.getTransaction().commit();
            return ldapDetails1;

        }catch (Exception e){
            session.getTransaction().rollback();
            logger.log(Level.DEBUG,LDAP_DETAILS_SERVICE_IMPL+"addLdapDetails : "+e);
            throw e;

        }finally {
            if(session.isOpen()){
                session.close();
            }
            logger.log(Level.DEBUG,LDAP_DETAILS_SERVICE_IMPL+"addLdapDetails : end");
        }
    }

    @Override
    public LDAPDetails editLdapDetails(LdapDetailsTO ldapDetailsTO) throws NotFoundException {
        Session session = sessionFactoryUtil.getSession();
        try {
            LDAPDetails ldapDetails = ldapDetailsDaoIntf.getDetailsById(ldapDetailsTO.getId());
            if (ldapDetailsTO.getConnectionUrl() != null) {
                ldapDetails.setConnectionUrl(ldapDetailsTO.getConnectionUrl());
            }
            if (ldapDetailsTO.getDomainName() != null) {
                ldapDetails.setDomainName(ldapDetailsTO.getDomainName().toLowerCase());
            }
            if(ldapDetailsTO.getUserDomainName() != null){
                ldapDetails.setUserDomainName(ldapDetailsTO.getUserDomainName());
            }
            ldapDetails.setSslEnabled(ldapDetailsTO.getIsSslEnabled());

            ldapDetails.setClientAddress(ldapDetailsTO.getClientAddress());
            LDAPDetails ldapDetails1 = ldapDetailsDaoIntf.updateLdapDetails(ldapDetails, session);
            session.getTransaction().commit();
            return ldapDetails1;

        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_IMPL + "addLdapDetails : " + e);
            throw e;

        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_IMPL + "addLdapDetails : end");
        }
    }

    @Override
    public List<LdapDetailsTO> getPaginatedList(int pageNo, int limit, String searchText) {
        logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_IMPL + " getPaginatedList : start");
            List<LDAPDetails> ldapDetailsTOList = ldapDetailsDaoIntf.getPaginatedList(pageNo, limit, searchText);
            List<LdapDetailsTO> ldapDetailsTOS= new ArrayList<>();
            ldapDetailsTOList.forEach(e->ldapDetailsTOS.add(e.convertToTO()));
        logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_IMPL + " getPaginatedList : end");
            return ldapDetailsTOS;
    }

    @Override
    public long getPaginatedListCount( String searchText){
       return ldapDetailsDaoIntf.getTotalActiveCount(searchText);
    }

    @Override
    public LDAPDetails getLdapDetailsByDomainName(String domainName) throws NotFoundException {
       return ldapDetailsDaoIntf.getLdapDetailsByDomainName(domainName);
    }

    @Override
    public LDAPDetails getLdapDetailsByUserDomainName(String userDomainName) throws NotFoundException {
        return ldapDetailsDaoIntf.getLdapDetailsByUserDomainName(userDomainName);
    }
}
