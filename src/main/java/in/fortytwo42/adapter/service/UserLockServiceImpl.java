package in.fortytwo42.adapter.service;

import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserLockDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.UserLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

public class UserLockServiceImpl implements UserLockServiceIntf {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private UserLockDaoIntf userLockDaoIntf = DaoFactory.getUserLockDao();

    private UserLockServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final UserLockServiceImpl INSTANCE = new UserLockServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static UserLockServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    
	@Override
	public UserLock getUserLockByAttribute(String attribute) throws NotFoundException {
        try {
            return userLockDaoIntf.getUserLockByAttribute(attribute);
        }
        catch (NotFoundException notFoundException){
            throw notFoundException;
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw e;
        }
	}

    @Override
    public UserLock addUserLock(Session session, UserLock userLock) {
        return userLockDaoIntf.create(session,userLock);
    }

    @Override
    public UserLock deleteUserLock(Session session, UserLock userLock) {
         userLockDaoIntf.remove(session,userLock);
         return userLock;
    }

}
