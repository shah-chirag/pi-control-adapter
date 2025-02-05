package in.fortytwo42.adapter.cam.facade;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keycloak.representations.idm.ClientRepresentation;

import in.fortytwo42.adapter.cam.dto.Client;
import in.fortytwo42.adapter.cam.dto.ClientTO;
import in.fortytwo42.adapter.cam.service.CamAdminServiceIntf;
import in.fortytwo42.adapter.cam.util.TimeUtil;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.CamUserExistsException;
import in.fortytwo42.adapter.service.ServiceFactory;

public class ClientFacadeImpl implements ClientFacadeIntf{

    private static Logger logger= LogManager.getLogger();

    private final String CLIENT_FACADE_IMPL_LOG = "<<<<< ClientFacadeImpl";


    TimeUtil timeUtil = new TimeUtil();


    private final CamAdminServiceIntf camAdminService = ServiceFactory.getCamAdminService();

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final ClientFacadeImpl INSTANCE = new ClientFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    public static ClientFacadeImpl getInstance() {
        return ClientFacadeImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public Client onboardClient(String realm, ClientTO clientTO) throws AuthException {

        logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " onboardClientAuditTrails : start");

        Client client;
        try {

            long startTime = timeUtil.start();
            logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Create client role  started on " + startTime);

            client = camAdminService.createClient(realm, clientTO);


            long totalTime = timeUtil.stop();
            logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
            if (client != null) {
                timeUtil.start();
                client.setClientSecret(camAdminService.getClientSecret(realm, client.getClientKcId()));
                timeUtil.stop();
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Client creation failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "Client creation failed");
        }
        try {
            if(client != null) {
                long startTime = timeUtil.start();
                logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Create client role  started on " + startTime);

                boolean camStatus = camAdminService.createClientRole(realm, client.getClientKcId(), client.getClientId());
                if (!camStatus) {
                    logger.log(Level.ERROR, CLIENT_FACADE_IMPL_LOG + " Client role creation failed.");
                    throw new AuthException(new Exception(), 500L, "Client role creation failed");
                }
                long totalTime = timeUtil.stop();
                logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Client role creation failed with exception " + ExceptionUtils.getStackTrace(e));
            boolean camStatus = camAdminService.removeClient(realm, client.getClientKcId());
            if (!camStatus) {
                logger.log(Level.ERROR, CLIENT_FACADE_IMPL_LOG + "Client removal failed.");
                throw new AuthException(e, 500L, "Client removal failed");
            }
            throw new AuthException(e, 500L, "Client role creation failed");
        }
        logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " onboardClientAuditTrails : end");

        return client;
    }

    @Override
    public Client onboardClientWithCLientRepresentation(String realm, ClientRepresentation clientRepresentation) throws AuthException {

        logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " onboardClientWithCLientRepresentation : start");

        Client client;
        try {

            long startTime = timeUtil.start();
            logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Create client role  started on " + startTime);

            client = camAdminService.createClientWithRepresentation(realm, clientRepresentation);


            long totalTime = timeUtil.stop();
            logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
            if (client != null) {
                timeUtil.start();
                client.setClientSecret(camAdminService.getClientSecret(realm, client.getClientKcId()));
                timeUtil.stop();
            }
        } catch (CamUserExistsException e){
            throw new AuthException(e,400L,"Client Already exists");
        }
        catch (Exception e) {
            logger.log(Level.ERROR, "Client creation failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "Client creation failed");
        }
        try {
            if(client != null) {
                long startTime = timeUtil.start();
                logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Create client role  started on " + startTime);

                boolean camStatus = camAdminService.createClientRole(realm, client.getClientKcId(), client.getClientId());
                if (!camStatus) {
                    logger.log(Level.ERROR, CLIENT_FACADE_IMPL_LOG + " Client role creation failed.");
                    throw new AuthException(new Exception(), 500L, "Client role creation failed");
                }
                long totalTime = timeUtil.stop();
                logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Client role creation failed with exception " + ExceptionUtils.getStackTrace(e));
            boolean camStatus = camAdminService.removeClient(realm, client.getClientKcId());
            if (!camStatus) {
                logger.log(Level.ERROR, CLIENT_FACADE_IMPL_LOG + "Client removal failed.");
                throw new AuthException(e, 500L, "Client removal failed");
            }
            throw new AuthException(e, 500L, "Client role creation failed");
        }
        logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " onboardClientWithCLientRepresentation : end");

        return client;
    }
    @Override
    public ClientRepresentation getClient(String realm, String clientId) throws AuthException {
        logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " getClient : start");
        try {
           return camAdminService.getClient(realm, clientId);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, "get client failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "get client failed");
        }
        finally {
            logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " getClient : end");
        }
    }


    @Override
    public void editClient(String realm, String clientId, ClientTO clientTO) throws AuthException {
        logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " editClient : start");
        try {
                long startTime = timeUtil.start();
                logger.log(Level.DEBUG, "clientId : " + clientId);
                logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Edit client started on " + startTime);
                boolean camStatus = camAdminService.updateClient(realm, clientId, clientTO);
                if (!camStatus) {
                    logger.log(Level.ERROR, CLIENT_FACADE_IMPL_LOG + " Edit Client on CAM failed.");
                    throw new AuthException(new Exception(), 500L, "edit client failed");
                }
                long totalTime = timeUtil.stop();
                logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " Total time taken in milliseconds" + totalTime);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, "edit client failed with exception " + ExceptionUtils.getStackTrace(e));
            throw new AuthException(e, 500L, "edit client failed");
        }
        finally {
            logger.log(Level.DEBUG, CLIENT_FACADE_IMPL_LOG + " editClient : end");
        }
    }


}
