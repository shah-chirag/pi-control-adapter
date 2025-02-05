package in.fortytwo42.adapter.service;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.FalloutConfig;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;

public class FalloutConfigServiceImpl implements FalloutConfigServiceIntf {

    private static Logger logger= LogManager.getLogger(FalloutConfigServiceImpl.class);

    private static final String CONFIG_SERVICE_IMPL_LOG = "<<<<< ConfigServiceImpl";

    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    private static final class InstanceHolder {
        private static final FalloutConfigServiceImpl INSTANCE = new FalloutConfigServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static FalloutConfigServiceImpl getInstance() {
        return FalloutConfigServiceImpl.InstanceHolder.INSTANCE;
    }


    @Override
    public List<FalloutConfigTO> getConfigs() {
        logger.log(Level.DEBUG, CONFIG_SERVICE_IMPL_LOG + " getConfigs : start");
        List<FalloutConfig> falloutConfigData = DaoFactory.getFalloutConfigDao().getAll();
        List<FalloutConfigTO> falloutConfigDataTOs = new EntityToTOConverter<FalloutConfig, FalloutConfigTO>().convertEntityListToTOList(falloutConfigData);
        logger.log(Level.DEBUG, CONFIG_SERVICE_IMPL_LOG + " getConfigs : end");
        return falloutConfigDataTOs;
    }

    @Override
    public FalloutConfigTO getConfigById(Long id) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_SERVICE_IMPL_LOG + " getConfigById : start");
        FalloutConfig falloutConfig = null;
        try {
            falloutConfig = DaoFactory.getFalloutConfigDao().getById(id);
        } catch (NotFoundException e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
        }
        FalloutConfigTO falloutConfigTO = falloutConfig.convertToTO();
        logger.log(Level.DEBUG, CONFIG_SERVICE_IMPL_LOG + " getConfigById : end");
        return falloutConfigTO;
    }

    @Override
    public void editFalloutConfig(Session session, FalloutConfigTO falloutConfigTO, String role, String actor) throws AuthException {
        logger.log(Level.DEBUG, CONFIG_SERVICE_IMPL_LOG + " editFalloutConfig : start");
        try {
            FalloutConfig falloutConfig = DaoFactory.getFalloutConfigDao().getById(falloutConfigTO.getId());
            falloutConfig.setDehFalloutDataSync(falloutConfigTO.getDehFalloutDataSync());
            falloutConfig.setDehFalloutDataProcess(falloutConfigTO.getDehFalloutDataProcess());
            falloutConfig.setNumberOfRecordsToBeProcessed(falloutConfigTO.getNumberOfRecordsToBeProcessed());
            falloutConfig.setDataFetchFrequency(falloutConfigTO.getDataFetchFrequency());
            falloutConfig.setSchedulerFrequency(falloutConfigTO.getSchedulerFrequency());
            DaoFactory.getFalloutConfigDao().update(session, falloutConfig);

        } catch (NotFoundException e) {
            throw new AuthException(e, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
        } finally {
            logger.log(Level.DEBUG, CONFIG_SERVICE_IMPL_LOG + " editFalloutConfig : end");
        }
    }

}
