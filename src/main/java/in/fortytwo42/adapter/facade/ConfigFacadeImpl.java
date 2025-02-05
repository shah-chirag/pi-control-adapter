
package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.FileUtil;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.Config;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.transferobj.ConfigTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;

public class ConfigFacadeImpl implements ConfigFacadeIntf {

    private static final String CONFIG_FACADE_LOG = "<<<<< ConfigFacadeImpl";
    private static Logger logger= LogManager.getLogger();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private static final class InstanceHolder {
        private static final ConfigFacadeImpl INSTANCE = new ConfigFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static ConfigFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getConfig(String configFileName) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " getConfig : start");
        String basePath = System.getenv().get(Constant.ENVIRONMENT_VARIABLE);
        String serverName = System.getProperty("ServerId");
        basePath = (serverName != null && !serverName.isEmpty()) ? basePath + Constant.FILE_SPERATOR + serverName : basePath;
        String filePath = "";
        switch (configFileName) {
            case Constant.CONFIG_FILE:
                filePath = basePath + Constant.FILE_SPERATOR + Constant.CONFIG_FILE;
                break;
            case Constant.HIBERNATE_FILE:
                filePath = basePath + Constant.FILE_SPERATOR + Constant.HIBERNATE_FILE;
                break;
            case Constant.QUARTZ_FILE:
                filePath = basePath + Constant.FILE_SPERATOR + Constant.QUARTZ_FILE;
                break;
            case Constant.ENCRYPTION_FILE:
                filePath = basePath + Constant.FILE_SPERATOR + Constant.ENCRYPTION_FILE;
                break;
            case Constant.IAM_LOG_FILE:
                basePath = (serverName != null && !serverName.isEmpty()) ? Constant.DEFAULT_LOG_DIRECTORY+ Constant.FILE_SPERATOR + serverName : Constant.DEFAULT_LOG_DIRECTORY;
                filePath =basePath + Constant.FILE_SPERATOR + Constant.IAM_LOG_FILE;
                break;
            default:
                logger.log(Level.ERROR, errorConstant.getERROR_MESSAGE_INVALID_FILE_NAME());
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_FILE_NAME(), errorConstant.getERROR_MESSAGE_INVALID_FILE_NAME());
        }
        String fileContent = FileUtil.readFile(filePath);
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " getConfig : end");
        return fileContent;
    }

    @Override
    public PaginatedTO<ConfigTO> getConfigs(Integer page, String searchText, String configType) {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " getConfigs : start");
        List<Config> configList = DaoFactory.getConfigDao().getPaginatedList(page, Integer.parseInt(in.fortytwo42.adapter.util.Config.getInstance().getProperty(Constant.LIMIT)), searchText, configType);
        List<ConfigTO> configTOs = new EntityToTOConverter<Config, ConfigTO>().convertEntityListToTOList(configList);
        Long count = DaoFactory.getConfigDao().getTotalActiveCount(searchText, configType);
        PaginatedTO<ConfigTO> configTO = new PaginatedTO<>();
        configTO.setList(configTOs);
        configTO.setTotalCount(count);
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " getConfigs : end");
        return configTO;
    }

    @Override
    public ConfigTO addConfig(String role, String actor,Long id, ConfigTO configTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " addConfig : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            configTO = addConfig(session, role, actor, id, configTO, saveRequest);
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " addConfig : end");
        return configTO;
    }

    @Override
    public void approveRequest(Session session, ConfigTO configTO, String role, String actor) {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " approveRequest : start");

        if(configTO.getId() != null){
            configTO = ServiceFactory.getConfigService().updateConfig(session, configTO);
        }else{
            configTO = ServiceFactory.getConfigService().addConfig(session, configTO);
        }
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " approveRequest : end");
    }
    public void approveDeleteRequest(Session session, ConfigTO configTO, String role, String actor) {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " approveDeleteRequest : start");
            ServiceFactory.getConfigService().deleteConfig(session, configTO);
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " approveDeleteRequest : end");
    }

    @Override
    public ConfigTO updateConfig(String role, String actor, Long id, ConfigTO configTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " addConfig : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            configTO = updateConfig(session, role, actor, id, configTO, saveRequest);
            sessionFactoryUtil.closeSession(session);
        }catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " addConfig : end");
        return configTO;
    }

    public ConfigTO updateConfig(Session session, String role, String actor,Long id,ConfigTO configTO,
                                 boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " addConfig : start");
        validateRequest(configTO);
        validateUniqueness(configTO);
        configTO = ServiceFactory.getRequestService().createUpdateConfigRequest(session, configTO, actor,id ,saveRequest);
        if (!saveRequest) {
            approveRequest(session, configTO, role, actor);
        }
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " addConfig : end");
        return configTO;
    }

    public ConfigTO deleteConfig(String role, String actor, Long id, ConfigTO configTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " deleteConfig : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            configTO = deleteConfig(session, role, actor, id, configTO, saveRequest);
            sessionFactoryUtil.closeSession(session);
        }catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " deleteConfig : end");
        return configTO;
    }

    public ConfigTO deleteConfig(Session session, String role, String actor,Long id,ConfigTO configTO,
                                 boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " deleteConfig : start");
//        validateRequest(configTO);validateUniqueness(configTO);
        configTO = ServiceFactory.getRequestService().createDeleteConfigRequest(session, configTO, actor,id , saveRequest);
        if (!saveRequest) {
            approveDeleteRequest(session, configTO, role, actor);
        }
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " deleteConfig : end");
        return configTO;
    }


    private void validateRequest(ConfigTO configTO) throws AuthException{
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " validateRequest : start");
        try {
            Config config = DaoFactory.getConfigDao().getActiveById(configTO.getId());
            boolean isTypeUpdated = config.getConfigType().equals(configTO.getConfigType());
            boolean isKeyUpdated = config.getKey().equals(configTO.getKey());
            boolean isValueUpdated = config.getValue().equals(configTO.getValue());
            if (isTypeUpdated && isKeyUpdated && isValueUpdated) {
                logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " validateRequest : end");
                throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
            }
        } catch (NotFoundException e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_CONFIG_NOT_FOUND(), errorConstant.getERROR_MESSAGE_CONFIG_NOT_FOUND());
        }

        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " validateRequest : end");
    }

    public ConfigTO addConfig(Session session, String role, String actor,Long id, ConfigTO configTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " addConfig : start");
        validateUniqueness(configTO);
        configTO = ServiceFactory.getRequestService().createAddConfigRequest(session, configTO, actor,id, saveRequest);
        if (!saveRequest) {
            approveRequest(session, configTO, role, actor);
        }
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " addConfig : end");
        return configTO;
    }

    private void validateUniqueness(ConfigTO configTO) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " validateCreateConfigRequest : start");
        try {
            if (configTO.getId() != null) {
                ServiceFactory.getConfigService().getConfigValue(configTO.getKey(), configTO.getConfigType(), configTO.getId());
            } else {
                ServiceFactory.getConfigService().getConfigValue(configTO.getKey(), configTO.getConfigType());
            }
            throw new AuthException(null, errorConstant.getERROR_CODE_CONFIG_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_CONFIG_ALREADY_PRESENT());
        } catch (NotFoundException e) {

        } finally {
            logger.log(Level.DEBUG, CONFIG_FACADE_LOG + " validateCreateConfigRequest : end");
        }
    }

}
