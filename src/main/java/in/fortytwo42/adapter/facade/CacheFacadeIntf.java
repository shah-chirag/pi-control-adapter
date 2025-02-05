package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CacheTO;

public interface CacheFacadeIntf {
    CacheTO clearCache(CacheTO cacheTO) throws AuthException;
}
