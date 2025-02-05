package in.fortytwo42.adapter.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

public class HealthCheckServiceImpl implements HealthCheckServiceIntf {

    private static Logger logger= LogManager.getLogger(HealthCheckServiceImpl.class);
    private HealthCheckServiceIntf HealthCheckStoreService = ServiceFactory.getHealthCheckService();
    private static final String HEALTH_CHECK_SERVICE_IMPL_LOG = "<<<<< HealthCheckServiceImpl";
    private Config config = Config.getInstance();

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        /**
         * Instantiates a new instance holder.
         */
        private static final HealthCheckServiceImpl INSTANCE = new HealthCheckServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of EvidenceFacadeImpl.
     *
     * @return single instance of EvidenceFacadeImpl
     */
    public static HealthCheckServiceImpl getInstance() {
        return HealthCheckServiceImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public String getHealthCheckofConnections() throws AuthException {
        logger.log(Level.DEBUG, HEALTH_CHECK_SERVICE_IMPL_LOG + " getHealthCheckConnections : start");
        String health="healthy";
        String idsIamHostname = config.getProperty(Constant.HOSTNAME_IDS_IAM_ACTIVEMQ);
        int idsIamIpPort = Integer.parseInt(config.getProperty(Constant.IP_PORT_IDS_IAM));

        String postgressHostname = config.getProperty(Constant.HOSTNAME_POSTGRES);
        int postgressIpPort = Integer.parseInt(config.getProperty(Constant.IP_PORT_POSTGRES));

        String activeMQIpHostname = config.getProperty(Constant.HOSTNAME_ACTIVEMQ);
        int activeMQIpPort = Integer.parseInt(config.getProperty(Constant.IP_PORT_ACTIVEMQ));

        Boolean cloud = true, activeMq = true, postgress = true;
        SocketFactory sf = SocketFactory.getDefault();
        try (Socket socket = sf.createSocket()) {
            // socket.connect(new InetSocketAddress("127.0.0.1", 9443));
            socket.connect(new InetSocketAddress(idsIamHostname, idsIamIpPort));
            // socket.connect(new InetSocketAddress("e2e-idc.fortytwo42.in",9443) );
            // socket.connect(new InetSocketAddress("attr-chain-v2-database-1.cq8crwn48j7c.ap-south-1.rds.amazonaws.com",1521) );//postgress
            System.out.println("database is up");
            cloud=true;
        }
        catch (IOException e) {
              cloud = false;
            System.out.println("database is down");
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, HEALTH_CHECK_SERVICE_IMPL_LOG + e.getMessage()+"cloud is down");
        }
        try(Socket socket1 = sf.createSocket()) {
            socket1.connect(new InetSocketAddress(postgressHostname, postgressIpPort));
            postgress=true;
        }
        catch (IOException e) {
            postgress=false;
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, HEALTH_CHECK_SERVICE_IMPL_LOG + e.getMessage()+"postgress is down");
        }
        try(Socket socket2 = sf.createSocket()) {
            socket2.connect(new InetSocketAddress(activeMQIpHostname, activeMQIpPort));
            activeMq=true;
        }
        catch (IOException e) {
            activeMq=false;
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, HEALTH_CHECK_SERVICE_IMPL_LOG + e.getMessage()+"activeMq is down");
        }
        if(postgress&&cloud&&activeMq){
            return health;
        }
        else {
            logger.log(Level.DEBUG, HEALTH_CHECK_SERVICE_IMPL_LOG + " getHealthCheckofConnections : end");
            return "unhealthy";
        }

    }


}
