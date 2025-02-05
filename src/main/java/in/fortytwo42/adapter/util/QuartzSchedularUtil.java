package in.fortytwo42.adapter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

import in.fortytwo42.daos.util.EncryptionUtil;


// TODO: Auto-generated Javadoc
/**
 * The Class QuartzSchedularUtil.
 */
public class QuartzSchedularUtil {

    /** The quartz schedular util. */
    private static String QUARTZ_SCHEDULAR_UTIL = "<<<<< QuartzSchedularUtil";

    private static Logger logger= LogManager.getLogger(QuartzSchedularUtil.class);
    
    /** The encryptor. */
    private StandardPBEStringEncryptor encryptor;
    
    /** The properties. */
    private Properties properties;
    
    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final QuartzSchedularUtil INSTANCE = new QuartzSchedularUtil();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of QuartzSchedularUtil.
     *
     * @return single instance of QuartzSchedularUtil
     */
    // Get Instance of the singleton Config class object
    public static QuartzSchedularUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    /**
     * Instantiates a new quartz schedular util.
     */
    private QuartzSchedularUtil() {
        String encryptionKey = EncryptionUtil.getInstance().getEncryptionKey();
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionKey);
        properties = new EncryptableProperties(encryptor);
        loadConfigurations();    
    }
    
    
    /**
     * Loads configuration properties.
     */
    public void loadConfigurations() {
        logger.log(Level.DEBUG, QUARTZ_SCHEDULAR_UTIL + " loadConfigurations : start");
        String basePath = System.getenv().get(Constant.ENVIRONMENT_VARIABLE);
        try (InputStream configPropInStream = new FileInputStream(basePath + File.separator + Constant.QUARTZ_FILE)) {
            properties.load(configPropInStream);
            configPropInStream.close();
        }
        catch (Exception e) {
            logger.log(Level.FATAL, e);
        }finally {
            logger.log(Level.DEBUG, QUARTZ_SCHEDULAR_UTIL + " loadConfigurations : end");
        }
    }
    
    /**
     * Gets the properties.
     *
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }
}
