package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.enums.ResponseStatus;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.CacheTO;
import in.fortytwo42.adapter.util.InfinispanUtil;
public class CacheFacadeImpl implements CacheFacadeIntf{

    private final String CACHE_FACADE_IMPL_LOG = "<<<<< CacheFacadeImpl";

    private static Logger logger= LogManager.getLogger(CacheFacadeImpl.class);
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();


    private static final class InstanceHolder {
        /** The Constant INSTANCE. */
        private static final CacheFacadeImpl INSTANCE = new CacheFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }
    public static CacheFacadeImpl getInstance() {
        return CacheFacadeImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public CacheTO clearCache(CacheTO cacheTO) throws AuthException {
        logger.log(Level.DEBUG, CACHE_FACADE_IMPL_LOG + " clearCache : start");
        switch (cacheTO.getCacheComponent()){
            case ADAPTER:
                InfinispanUtil.clearCache();
                cacheTO.setStatus(ResponseStatus.SUCCESS);
                break;
            case IAM:
            case IDS:
                try {
                    iamExtensionService.clearCache(cacheTO.getCacheComponent());
                    cacheTO.setStatus(ResponseStatus.SUCCESS);
                }
                catch (AuthException e) {
                    logger.log(Level.DEBUG, CACHE_FACADE_IMPL_LOG ,e);
                    throw e;
                }
                break;
            default:
                cacheTO.setStatus(ResponseStatus.FAILURE);
        }
        logger.log(Level.DEBUG, CACHE_FACADE_IMPL_LOG + " clearCache : end");
        return cacheTO;
    }

}
