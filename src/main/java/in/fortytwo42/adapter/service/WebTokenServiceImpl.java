
package  in.fortytwo42.adapter.service;

import org.hibernate.Session;

import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserSessionTokenDaoIntf;

public class WebTokenServiceImpl implements WebTokenServiceIntf {

    private UserSessionTokenDaoIntf tokenDao = DaoFactory.getUserSessionTokenDao();

	
	private WebTokenServiceImpl() {
		super();
	}

	private static final class InstanceHolder {
		private static final WebTokenServiceImpl INSTANCE = new WebTokenServiceImpl();

		private InstanceHolder() {
			super();
		}
	}

	public static WebTokenServiceImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}

	@Override
	public void deleteExpiredTokens(Session session) {
		tokenDao.deleteTokens(session, System.currentTimeMillis());
	}
	
}
