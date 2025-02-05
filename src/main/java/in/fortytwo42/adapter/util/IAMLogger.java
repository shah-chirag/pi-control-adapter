
package in.fortytwo42.adapter.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Singleton class having logger implementation. It refers properties defined in logger.properties file.
 * @author ChiragShah
 *
 */
public class IAMLogger {

    private static String finalPath = null;
    
    static {
        String basePath = System.getenv().get(Constant.ENVIRONMENT_VARIABLE);
        System.out.println("basePath : "+basePath);
        String serverName = System.getProperty("ServerId");
        if (serverName != null && !serverName.isEmpty()) {
            finalPath = basePath + "/" + serverName + "/" + Constant.LOG4J_CONFIG_FILE;
        }
        else {
            finalPath = basePath + "/" + Constant.LOG4J_CONFIG_FILE;
        }
        System.out.println("finalPath : "+finalPath);
        Configurator.initialize(null, finalPath);
    }

    private static String appLoggerName = null;
    private Logger logger = null;

    /**
     * Private default constructor - For singleton
     */
    private IAMLogger() {
        super();
        init();
    }

    /**
     * Initialize logger
     */
    private void init() {
        IAMLogger.setLoggerName("AdapterLog");
        logger = LogManager.getLogger(appLoggerName);
        logger.log(Level.INFO, "Logger property file path : {}", finalPath);
    }

    private static void setLoggerName(String loggerName) {
        if (appLoggerName == null) {
            appLoggerName = loggerName;
        }
    }

    private static final class InstanceHolder {
        private static final IAMLogger INSTANCE = new IAMLogger();

        private InstanceHolder() {
            super();
        }
    }

    /**
     * getInstance: Return singleton implementation of Tivalogger
     * @return
     */
    public static IAMLogger getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Log a message.
     * @param level of severity, Log message
     * @param message
     * @return 
     * @exception SecurityException, IOException
     * @see
     */
    public void log(Level level, String message) {
        logger.log(level, message);
    }

    /**
     * Log an exception.
     * @param Level of severity, Exception Object
     * @return 
     * @exception SecurityException, IOException
     * @see
     */
    public void log(Level level, Exception errObj) {
        logger.log(level, errObj);
    }

    /**
     * Log a message and exception
     * @param Level of severity, Log message, Error Object
     * @return
     * @exception SecurityException, IOException
     * @see
     */
    public void log(Level level, String message, Exception errObj) {
        logger.log(level, message, errObj);
    }

}
