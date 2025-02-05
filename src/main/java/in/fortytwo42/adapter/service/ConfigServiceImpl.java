package in.fortytwo42.adapter.service;

import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.Config;
import in.fortytwo42.tos.transferobj.ConfigTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

public class ConfigServiceImpl implements ConfigServiceIntf {

    private static final String CONFIG_SERVICE_LOG = "<<<<< ConfigServiceImpl";
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private static final class InstanceHolder {
        private static final ConfigServiceImpl INSTANCE = new ConfigServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static ConfigServiceImpl getInstance() {
        return ConfigServiceImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public ConfigTO addConfig(Session session, ConfigTO configTO) {
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " addConfig : start");
        Config config = new Config();
        config.setConfigType(configTO.getConfigType());
        config.setKey(configTO.getKey());
        config.setValue(configTO.getValue());
        Config configResp = DaoFactory.getConfigDao().create(session, config);
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " addConfig : end");
        return configResp.convertToTO();
    }

    @Override
    public ConfigTO updateConfig(Session session, ConfigTO configTO) {
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " updateConfig : start");
        Config config = null;
        try {
            config = DaoFactory.getConfigDao().getActiveById(configTO.getId());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        config.setConfigType(configTO.getConfigType());
        config.setKey(configTO.getKey());
        config.setValue(configTO.getValue());
        Config configResp = DaoFactory.getConfigDao().update(session, config);
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " updateConfig : end");
        return configResp.convertToTO();
    }

    public void deleteConfig(Session session, ConfigTO configTO) {
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " deleteConfig : start");
        Config config = null;
        try {
            config = DaoFactory.getConfigDao().getActiveById(configTO.getId());
            if(config!=null) {
                DaoFactory.getConfigDao().remove(session, config);
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " deleteConfig : end");
    }
    @Override
    public ConfigTO getConfigValue(String key, String type) throws NotFoundException {
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " getConfigValue : start");
        Config config = DaoFactory.getConfigDao().getConfigValue(key, type);
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " getConfigValue : end");
        return config.convertToTO();
    }

    @Override
    public ConfigTO getConfigValue(String key, String type, long id) throws NotFoundException {
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " getConfigValue : start");
        Config config = DaoFactory.getConfigDao().getConfigValue(key, type, id);
        logger.log(Level.DEBUG, CONFIG_SERVICE_LOG + " getConfigValue : end");
        return config.convertToTO();
    }

}
