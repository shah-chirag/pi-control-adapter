package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.LDAPDetailsServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.LDAPDetails;
import in.fortytwo42.tos.transferobj.LdapDetailsTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;
import java.util.Objects;

public class LDAPDetailsFacadeImpl implements LDAPDetailsFacadeIntf{



    private LDAPDetailsServiceIntf ldapDetailsServiceIntf = ServiceFactory.getLdapDetailsService();

    private static Logger logger= LogManager.getLogger(LDAPDetailsFacadeImpl.class);

    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private String LDAP_DETAILS_SERVICE_INTF ="<<<< LDAPDetailsFacadeimpl";

    private RequestServiceIntf requestService = ServiceFactory.getRequestService();

    SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private Config config = Config.getInstance();


    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final LDAPDetailsFacadeImpl INSTANCE = new LDAPDetailsFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of LDAPDetailsFacadeImpl.
     *
     * @return single instance of LDAPDetailsFacadeImpl
     */
    public static LDAPDetailsFacadeImpl getInstance() {
        return LDAPDetailsFacadeImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public LdapDetailsTO addLdapDetails(LdapDetailsTO ldapDetailsTO) throws AuthException {
        logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " addLdapDetails : start");
        try{
          LDAPDetails ldapDetails= ldapDetailsServiceIntf.addLdapDetails(ldapDetailsTO);
          return ldapDetails.convertToTO();
        }
        catch (Exception e){
            throw new AuthException(new Exception(),errorConstant.getERROR_CODE_INVALID_DATA(),e.getMessage());
        }finally {
            logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " addLdapDetails : end");
        }
    }

    @Override
    public LdapDetailsTO createLdapDetailsRequest( Long id,String role, String actor, LdapDetailsTO ldapDetailsTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " createLdapDetailsRequest : start");
        Session session= IamThreadContext.getSessionWithoutTransaction();
        try {
            ldapDetailsServiceIntf.getLdapDetailsByUserDomainName(ldapDetailsTO.getUserDomainName());
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(),errorConstant.getERROR_MESSAGE_LDAP_DETAILS_ALREADY_PRESENT());
        }catch (NotFoundException e){

        }
        try {
            ldapDetailsTO = requestService.createAddLdapDetailsRequest(id,session, ldapDetailsTO, actor, saveRequest);
            if (!saveRequest) {

                ldapDetailsTO = addLdapDetails(ldapDetailsTO);
            }
            session.close();
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }finally {
            if(session.isOpen()){
                session.close();
            }
            logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " createLdapDetailsRequest : end");
        }
        return ldapDetailsTO;
    }

    @Override
    public LdapDetailsTO editLdapDetails(Long id ,String role, String actor, LdapDetailsTO ldapDetailsTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " editLdapDetails : start");
        Session session = IamThreadContext.getSessionWithoutTransaction();
        try {
            boolean domainName =true, userDomainName = true ,connectionUrl = true, clientAddress=true, sslenabled=true;
             LDAPDetails ldapDetails = ldapDetailsServiceIntf.getLdapDetailsByUserDomainName(ldapDetailsTO.getUserDomainName());
            if(!Objects.equals(ldapDetails.getId(), ldapDetailsTO.getId())) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_LDAP_DETAILS_ALREADY_PRESENT());
            }
            if(! ldapDetailsTO.getDomainName().equals(ldapDetails.getDomainName())){
                domainName=false;
            }if(!ldapDetailsTO.getUserDomainName().equals(ldapDetails.getUserDomainName())){
                userDomainName=false;
            }
            if(!ldapDetailsTO.getConnectionUrl().equals(ldapDetails.getConnectionUrl())){
                connectionUrl=false;
            }
            if(ldapDetails.getClientAddress()==null&&ldapDetailsTO.getClientAddress()!=null || ldapDetails.getClientAddress()!=null&&ldapDetailsTO.getClientAddress()==null||ldapDetails.getClientAddress()!=null && ldapDetailsTO.getIsSslEnabled()!=null && !ldapDetails.getClientAddress().equals(ldapDetailsTO.getClientAddress())){
                clientAddress=false;
            }if(ldapDetails.getSslEnabled()==null&&ldapDetailsTO.getIsSslEnabled()!=null || ldapDetails.getSslEnabled()!=null&&ldapDetailsTO.getIsSslEnabled()==null || ldapDetails.getSslEnabled()!=null&&ldapDetailsTO.getIsSslEnabled()!=null&&!ldapDetails.getSslEnabled().equals(ldapDetailsTO.getIsSslEnabled())){
                sslenabled=false;
            }
            if(domainName && userDomainName && connectionUrl && clientAddress && sslenabled){
               throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
            }

        }catch (NotFoundException e){

        }

        try {
            ldapDetailsTO = requestService.createEditLdapDetailsRequest(id,session, ldapDetailsTO, actor, saveRequest);
            if (!saveRequest) {
                ldapDetailsTO = editLdapDetail(ldapDetailsTO);
            }
            session.close();
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " editLdapDetails : end");
        }
        return ldapDetailsTO;
    }
    @Override
    public LdapDetailsTO editLdapDetail(LdapDetailsTO ldapDetailsTO) throws AuthException {
        logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " addLdapDetails : start");
        try {
            LDAPDetails ldapDetails = ldapDetailsServiceIntf.editLdapDetails(ldapDetailsTO);
            return ldapDetails.convertToTO();
        } catch (Exception e) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " addLdapDetails : end");
        }
    }

    @Override
    public PaginatedTO<LdapDetailsTO> getLdapDetails(int pageNo, String searchText) {
        logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " getLdapDetails : start");
        PaginatedTO<LdapDetailsTO> ldapDetailsTOPaginatedList=new PaginatedTO<>();
        List<LdapDetailsTO> ldapDetailsTOList=ldapDetailsServiceIntf.getPaginatedList(pageNo, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText);
        ldapDetailsTOPaginatedList.setList(ldapDetailsTOList);
        ldapDetailsTOPaginatedList.setTotalCount(ldapDetailsServiceIntf.getPaginatedListCount(searchText));
        logger.log(Level.DEBUG, LDAP_DETAILS_SERVICE_INTF + " getLdapDetails : end");
        return ldapDetailsTOPaginatedList;
    }
}
