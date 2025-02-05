package in.fortytwo42.adapter.facade;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.ServerRegistryDaoIntf;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.ServerRegistry;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.enums.ServerRegistryStatus;
import in.fortytwo42.tos.transferobj.ServerRegistryTO;

/**
 * The Class ServerRegistryFacadeImpl. ======= import
 * in.fortytwo42.adapter.transferobj.ServerRegistryTO; import
 * in.fortytwo42.adapter.util.EntityToTOConverter; import
 * in.fortytwo42.adapter.util.IAMLogger;
 * 
 * /** The Class ServerRegistryProcessorImpl.
 */
public class ServerRegistryFacadeImpl implements ServerRegistryFacadeIntf {

	/**
	 * The server registry processor log.
	 */
	private String SERVER_REGISTRY_PROCESSOR_LOG = "<<<<< ServerRegistryProcessorImpl";

	/**
	 * The Session Factory Util
	 */
	private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

	private static Logger logger= LogManager.getLogger(ServerRegistryFacadeImpl.class);

	private ServerRegistryFacadeImpl() {
		super();
	}

	/**
	 * The Class InstanceHolder.
	 */
	private static final class InstanceHolder {

		/**
		 * The Constant INSTANCE.
		 */
		private static final ServerRegistryFacadeImpl INSTANCE = new ServerRegistryFacadeImpl();

		/**
		 * Instantiates a new instance holder.
		 */
		private InstanceHolder() {
			super();
		}
	}

	/**
	 * Gets the single instance of ServerRegistryFacadeImpl.
	 *
	 * @return single instance of ServerRegistryProcessorImpl
	 */
	public static ServerRegistryFacadeImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}

	public void initializeService() {
		String ip;
		ServerRegistry serverRegistry = new ServerRegistry();
		Session session = sessionFactoryUtil.getSession();

		InetAddress localhost;
		try {
			localhost = InetAddress.getLocalHost();
			ip = localhost.getHostAddress().trim();
		} catch (UnknownHostException e) {
			ip = Constant.DEFAULT_IP_ADDRESS;
		}
		ServerRegistryDaoIntf serverRegistryDaoIntf = DaoFactory.getServerRegistryDao();
		try {
			serverRegistry = serverRegistryDaoIntf.getServerByIp(ip);
			serverRegistry.setStatus(ServerRegistryStatus.REGISTERED);
			serverRegistryDaoIntf.update(session, serverRegistry);
		} catch (Exception e) {
			serverRegistry.setIp(ip);
			serverRegistry.setStatus(ServerRegistryStatus.REGISTERED);
			serverRegistryDaoIntf.create(session, serverRegistry);
		} finally {
			sessionFactoryUtil.closeSession(session);
			logger.log(Level.DEBUG, SERVER_REGISTRY_PROCESSOR_LOG + " destroy : end");
		}
	}



	@Override
	public List<ServerRegistryTO> getServersRegistry() {
		logger.log(Level.DEBUG, SERVER_REGISTRY_PROCESSOR_LOG + " getServersRegistry : start");
		ServerRegistryDaoIntf serverRegistryDaoIntf = DaoFactory.getServerRegistryDao();
		List<ServerRegistry> serverRegistries = serverRegistryDaoIntf.getAll();
		logger.log(Level.DEBUG, SERVER_REGISTRY_PROCESSOR_LOG + " getServersRegistry : end");
		return new EntityToTOConverter<ServerRegistry, ServerRegistryTO>().convertEntityListToTOList(serverRegistries);
	}

	/**
	 * Destroy thread context.
	 */
	/*public void destroy() {
		Session session = sessionFactoryUtil.getSession();
		//String serverId = Config.getInstance().getProperty(Constant.SERVER_ID);
		ServerRegistryDaoIntf serverRegistryDaoIntf = DaoFactory.getServerRegistryDao();
		try {
		ServerRegistry serverRegistry = serverRegistryDaoIntf.getServerByServerId(serverId);
			serverRegistry.setStatus(ServerRegistryStatus.NOT_REGISTERED);
			serverRegistryDaoIntf.update(session, serverRegistry);
		} catch (NotFoundException e) {
		//	IAMLogger.getInstance().log(Level.ERROR, e.getMessage(), e);
		//} finally {
		//	sessionFactoryUtil.closeSession(session);
		//	logger.log(Level.DEBUG, SERVER_REGISTRY_PROCESSOR_LOG + " destroy : end");
		//}
		//}*/
	}

