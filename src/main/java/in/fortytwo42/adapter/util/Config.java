
package in.fortytwo42.adapter.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

import in.fortytwo42.daos.util.EncryptionUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class Config.
 *
 * @author ChiragShah
 * This class defines configuration parameter names and associated getter methods.
 */
public class Config {

    /** The config. */
    private static String CONFIG = "<<<<< Config";

    /** The logger. */
    private static Logger logger= LogManager.getLogger(Config.class);
    
    /** The encryptor. */
    private StandardPBEStringEncryptor encryptor;
    
    /** The properties. */
    private Properties properties;

    /**
     * Keeping constructor private to build singleton configuration object.
     */
    private Config() {
        String encryptionKey = EncryptionUtil.getInstance().getEncryptionKey();
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionKey);
        properties = new EncryptableProperties(encryptor);
        loadConfigurations();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final Config INSTANCE = new Config();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of Config.
     *
     * @return single instance of Config
     */
    // Get Instance of the singleton Config class object
    public static Config getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Loads configuration properties.
     */
    public void loadConfigurations() {
        logger.log(Level.DEBUG, CONFIG + " loadConfigurations : start");
        String basePath = System.getenv().get(Constant.ENVIRONMENT_VARIABLE);
        //String basePath = "/home/neebal/Documents/FT42/iamCi2";
        String serverName = System.getProperty("ServerId");
        String finalPath = "";
        if (serverName != null && !serverName.isEmpty()) {
            finalPath = basePath + "/" + serverName + "/" + Constant.CONFIG_FILE;
        }
        else {
            finalPath = basePath + "/" + Constant.CONFIG_FILE;
        }
        try (InputStream configPropInStream = new FileInputStream(finalPath)) {
            properties.load(configPropInStream);
            configPropInStream.close();
        }
        catch (Exception e) {
            logger.log(Level.FATAL, e);
        }finally {
            logger.log(Level.DEBUG, CONFIG + " loadConfigurations : end");
        }
    }

    /**
     * getProperty Method that returns configuration property value.
     *
     * @param propName            : Property name for which value to be retrieved.
     * @return the property
     */
    public String getProperty(String propName) {
        logger.log(Level.DEBUG, CONFIG + " getProperty : start");
        if (properties != null) {
            logger.log(Level.DEBUG, CONFIG + " getProperty : end");
            String value=(String) properties.get(propName);
            return value!=null&&!value.isEmpty()?value.trim():value;
        }
        else {
            logger.log(Level.DEBUG, CONFIG + " getProperty : end");
            return null;
        }
    }

    /**
     * Gets the all properties.
     *
     * @return the all properties
     */
    public Map<Object, Object> getAllProperties() {
        logger.log(Level.DEBUG, CONFIG + " getAllProperties : start");
        Map<Object, Object> propertyMap = new HashMap<>();
        Set<Entry<Object, Object>> entries = properties.entrySet();
        for (Entry<Object, Object> entry : entries) {
            propertyMap.put(entry.getKey(), entry.getValue());
        }
        logger.log(Level.DEBUG, CONFIG + " getAllProperties : end");
        return propertyMap;
    }

    /**
     * Update properties.
     *
     * @param updatedProperties the updated properties
     */
    public void updateProperties(Map<Object, Object> updatedProperties) {
        properties.putAll(updatedProperties);
    }

}
