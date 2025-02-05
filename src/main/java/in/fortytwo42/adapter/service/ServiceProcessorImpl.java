package  in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.ServiceDaoIntf;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.entities.bean.Service;
//TODO: Kept the name as Processor since Service is the entity name
public class ServiceProcessorImpl implements ServiceProcessorIntf {

	private ServiceDaoIntf serviceDao = DaoFactory.getServiceDao();
	private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

	private ServiceProcessorImpl() {
	        super();
	    }

	private static final class InstanceHolder {
		private static final ServiceProcessorImpl INSTANCE = new ServiceProcessorImpl();

		private InstanceHolder() {
			super();
		}
	}

	public static ServiceProcessorImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}

	@Override
	public Service getService(String serviceName) throws AuthException {
		try {
			return serviceDao.getServiceByServiceName(serviceName);
		} catch (ServiceNotFoundException e) {
			throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
		}
	}


	@Override
	public Service getService(String serviceName, Session session) throws AuthException {
		try {
			return serviceDao.getServiceByServiceName(serviceName, session);
		} catch (ServiceNotFoundException e) {
			throw new AuthException(null, errorConstant.getERROR_CODE_SERVICE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_SERVICE_NOT_FOUND());
		}
	}

	@Override
	public Service createService(in.fortytwo42.enterprise.extension.tos.ServiceTO serviceTO) {
		Service service = new Service();
		service.setServiceName(serviceTO.getServiceName());
		service.setStatus(serviceTO.getStatus());
		service.setDescription(serviceTO.getDescription());
		return service;
	}

	@Override
	public void bulkInsert(Session session, List<Service> services) {
		if(!services.isEmpty()) {
			serviceDao.bulkInsert(session, services);
		}
	}

	@Override
	public List<Service> getAllServices() {
		return serviceDao.getAll();
	}
	
	@Override
	public Service getServiceByServiceName(String serviceId) throws ServiceNotFoundException{
	    return serviceDao.getServiceByServiceName(serviceId);
	}

}
