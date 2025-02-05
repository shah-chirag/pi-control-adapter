
package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.ServiceProcessorIntf;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.tos.transferobj.ServiceTO;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceFacadeImpl.
 */
public class ServiceFacadeImpl implements ServiceFacadeIntf {

    /** The service facade impl log. */
    private String SERVICE_FACADE_IMPL_LOG = "<<<<< ServiceFacadeImpl";

    private static Logger logger= LogManager.getLogger(ServiceFacadeImpl.class);

    /** The service processor intf. */
    private ServiceProcessorIntf serviceProcessorIntf = ServiceFactory.getServiceProcessor();

    private UserApplicationRelDaoIntf userApplicationRelDao = DaoFactory.getUserApplicationRel();
    
    private IAMUtil iamUtil = IAMUtil.getInstance();
    /**
     * Instantiates a new service facade impl.
     */

    /** The Session Factory Util */
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private ServiceFacadeImpl() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final ServiceFacadeImpl INSTANCE = new ServiceFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of ServiceFacadeImpl.
     *
     * @return single instance of ServiceFacadeImpl
     */
    public static ServiceFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Load services.
     */
    @Override
    public void loadServices() {
        logger.log(Level.DEBUG, SERVICE_FACADE_IMPL_LOG + " loadServices : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(null);
            List<in.fortytwo42.enterprise.extension.tos.ServiceTO> services = iamExtension.getServiceList();
            if (services != null && !services.isEmpty()) {

                List<Service> servicesToAdd = new ArrayList<>();
                for (in.fortytwo42.enterprise.extension.tos.ServiceTO service : services) {
                    Service serviceToAdd = null;
                    try {
                        serviceToAdd = serviceProcessorIntf.getService(service.getServiceName());
                    }
                    catch (AuthException e) {
                    }
                    if (serviceToAdd == null) {
                        servicesToAdd.add(serviceProcessorIntf.createService(service));
                    }
                }
                serviceProcessorIntf.bulkInsert(session, servicesToAdd);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (NumberFormatException | IAMException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            logger.log(Level.DEBUG, SERVICE_FACADE_IMPL_LOG + " loadServices : end");
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Gets the services.
     *
     * @return the services
     */
    @Override
    public List<ServiceTO> getServices() {
        logger.log(Level.DEBUG, SERVICE_FACADE_IMPL_LOG + " getServices : start");
        loadServices();
        List<Service> services = serviceProcessorIntf.getAllServices();
        List<ServiceTO> serviceTOs = new ArrayList<>();
        for (Iterator<Service> iterator = services.iterator(); iterator.hasNext();) {
            Service service = iterator.next();
            ServiceTO serviceTO = service.convertToTO();
            Long adUserCount = userApplicationRelDao.getUserCountForService(service.getId());
            serviceTO.setSubscribedUserCount(adUserCount);
            serviceTO.setStatus(service.getStatus());
            serviceTO.setDescription(service.getDescription());
            serviceTOs.add(serviceTO);
        }
        logger.log(Level.DEBUG, SERVICE_FACADE_IMPL_LOG + " getServices : end");
        return serviceTOs;
    }
}
