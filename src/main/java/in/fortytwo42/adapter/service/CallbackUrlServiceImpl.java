package  in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.daos.dao.CallbackUrlDaoImpl;
import in.fortytwo42.daos.dao.CallbackUrlDaoIntf;
import in.fortytwo42.entities.bean.CallbackUrl;

public class CallbackUrlServiceImpl implements CallbackUrlServiceIntf{
    
    private CallbackUrlDaoIntf callbackUrlDao = CallbackUrlDaoImpl.getInstance();

    private static final class InstanceHolder {
        private static final CallbackUrlServiceImpl INSTANCE = new CallbackUrlServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static CallbackUrlServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    @Override
    public void bulkInsert(Session session, List<CallbackUrl> callbackUrls) {
        callbackUrlDao.bulkInsert(session, callbackUrls);
    }


}
