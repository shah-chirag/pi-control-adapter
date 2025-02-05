package  in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.exception.ServiceNotFoundException;
import in.fortytwo42.entities.bean.Service;

public interface ServiceProcessorIntf {

	Service getService(String serviceName) throws AuthException;

	Service getService(String serviceName, Session session) throws AuthException;
	
	Service createService(in.fortytwo42.enterprise.extension.tos.ServiceTO serviceTO);
	
	void bulkInsert(Session session, List<Service> services);
	
	List<Service> getAllServices();

    Service getServiceByServiceName(String serviceId) throws ServiceNotFoundException;
}
