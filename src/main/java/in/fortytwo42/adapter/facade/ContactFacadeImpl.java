
package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.ContactWeTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;

// TODO: Auto-generated Javadoc
/**
 * The Class ContactFacadeImpl.
 */
public class ContactFacadeImpl implements ContactFacadeIntf {

    /** The build details facade impl log. */
    private String CONTACT_FACADE_IMPL_LOG = "<<<<< ContactFacadeImpl";

    private static Logger logger= LogManager.getLogger(ContactFacadeImpl.class);

    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final ContactFacadeImpl INSTANCE = new ContactFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of BuildDetailsFacadeImpl.
     *
     * @return single instance of BuildDetailsFacadeImpl
     */
    public static ContactFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public ContactWeTO onboardContact(ContactWeTO contactWE, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " onboardContact : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            contactWE = requestService.createContactOnboardRequest(session, contactWE, actor,id, saveRequest);
            if (!saveRequest) {
                contactWE = approveOnboardContact(session, contactWE, actor);
            }
            contactWE.setStatus(Constant.SUCCESS_STATUS);
            contactWE.setId(contactWE.getId());
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " onboardContact : end");
        return contactWE;
    }

    @Override
    public ContactWeTO approveOnboardContact(Session session, ContactWeTO contactWE, String actor) throws AuthException {
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " approveOnboardContact : start");
        contactWE.setContactTO(iamExtensionService.onboardContact(contactWE.getContactTO()));
        contactWE.setStatus(Constant.SUCCESS_STATUS);
        contactWE.setId(contactWE.getId());
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " approveOnboardContact : end");
        return contactWE;
    }

    @Override
    public ContactWeTO editContact(ContactWeTO contactWE, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " editContact : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            contactWE = requestService.createContactEditRequest(session, contactWE, actor,id, saveRequest);
            if (!saveRequest) {
                contactWE = approveEditContact(session, contactWE, actor);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " editContact : end");
        return contactWE;
    }

    @Override
    public ContactWeTO approveEditContact(Session session, ContactWeTO contactWE, String actor) throws AuthException {
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " approveEditContact : start");
        contactWE.setContactTO(iamExtensionService.editContact(contactWE.getContactTO()));
        contactWE.setStatus(Constant.SUCCESS_STATUS);
        contactWE.setId(contactWE.getId());
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " approveEditContact : start");
        return contactWE;
    }
    
    @Override
    public PaginatedTO<AccountWE> getAllContact(String accountId,String attributeValue, Integer page, Integer pageSize) throws AuthException {
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " getAllContact : start");
        PaginatedTO<AccountWE> accounts = iamExtensionService.getAllContacts(accountId, attributeValue, page, pageSize);
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " getAllContact : end");
        return accounts;
    }
    
    @Override
    public PaginatedTO<AccountWE> getAccountsWithAttributeValue(String accountId,String attributeValue,Integer page, Integer pageSize) throws AuthException {
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " getAllContact : start");
        PaginatedTO<AccountWE> accounts = iamExtensionService.getAccountsWithAttributeValue(accountId, attributeValue,page, pageSize);
        logger.log(Level.DEBUG, CONTACT_FACADE_IMPL_LOG + " getAllContact : end");
        return accounts;
    }

}
