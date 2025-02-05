package in.fortytwo42.adapter.controllers;

import java.security.Security;
import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

import in.fortytwo42.adapter.facade.EnterpriseFacadeImpl;
import in.fortytwo42.adapter.facade.ServerRegistryFacadeImpl;
import in.fortytwo42.adapter.util.ADsyncSchedular;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.FalloutProcessScheduler;
import in.fortytwo42.adapter.util.IAMLogger;
import in.fortytwo42.adapter.util.InfinispanUtil;
import in.fortytwo42.adapter.util.NotificationHandler;
import in.fortytwo42.adapter.util.TokenDeletionSchedular;
import in.fortytwo42.adapter.util.UserAuthPrincipalSchedular;
import in.fortytwo42.adapter.util.handler.CAMUtil;
import in.fortytwo42.adapter.util.handler.QRtransactionScheduler;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.daos.util.handler.InfinispanHealthCheckScheduler;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving iamContext events.
 * The class that is interested in processing a iamContext
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addIamContextListener<code> method. When
 * the iamContext event occurs, that object's appropriate
 * method is invoked.
 *
 * @author ChiragShah
 */
public class IamContextListener implements ServletContextListener {

    private static IAMLogger initLogger = IAMLogger.getInstance();

    private static Logger logger= LogManager.getLogger(IamContextListener.class);

    private static Timer timer = new Timer();
    
    /**
     * Context initialized.
     *
     * @param paramServletContextEvent the param servlet context event
     */
    @Override
    public void contextInitialized(ServletContextEvent paramServletContextEvent) {
        logger.log(Level.DEBUG, Constant.SERVLET_CONTEXT_INITIALIZED + " contextInitialized : start");
        InfinispanUtil.getInstance();
        Boolean isFipsEnabled= Boolean.valueOf((Config.getInstance().getProperty(Constant.IS_FIPS_ENABLED)));
        if(isFipsEnabled) {
            Security.addProvider(new BouncyCastleFipsProvider());
        }
        SessionFactoryUtil.getInstance();
        //        BootstrapServiceIntf bootstrapService = new BootstrapServiceImpl();
        //        bootstrapService.initializeMasterData();
        //PTScheduler.getInstance().initializeService();
        TokenDeletionSchedular.getInstance().startSchedular();
        UserAuthPrincipalSchedular.getInstance().startSchedular();
        //NotificationHandler.getInstance().parseEventQueue();
//        ADsyncSchedular.getInstance().startSchedular();
        ADsyncSchedular.getInstance().startSchedular();
        ServerRegistryFacadeImpl.getInstance().initializeService();
        EnterpriseFacadeImpl.getInstance().initializeService();
        QRtransactionScheduler.getInstance().startSchedular();
        FalloutProcessScheduler.getInstance().startSchedular();
        logger.log(Level.DEBUG, Constant.SERVLET_CONTEXT_INITIALIZED + " contextInitialized : end");

    }

    /**
     * Context destroyed.
     *
     * @param paramServletContextEvent the param servlet context event
     */
    @Override
    public void contextDestroyed(ServletContextEvent paramServletContextEvent) {
        logger.log(Level.DEBUG, Constant.SERVLET_CONTEXT_DESTROYER_CALLED + " contextDestroyed : start");
        SessionFactoryUtil.getInstance().shutdown();
        //PTScheduler.getInstance().destroyThreadContext();
        CAMUtil.getInstance().shutDownExecutorService();
        IamThreadPoolController.getInstance().destroyExecutorService();
        NotificationHandler.getInstance().shutDownStopQueueProcessing();
        TokenDeletionSchedular.getInstance().shutdownSchedular();
        UserAuthPrincipalSchedular.getInstance().shutdownSchedular();
       // ServerRegistryFacadeImpl.getInstance().destroy();
        QRtransactionScheduler.getInstance().shutdownSchedular();
        InfinispanHealthCheckScheduler.getInstance().shutdownScheduler();
        logger.log(Level.DEBUG, Constant.SERVLET_CONTEXT_DESTROYER_CALLED + " contextDestroyed : start");
    }
}
