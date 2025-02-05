package in.fortytwo42.adapter.service;

import org.hibernate.Session;

public interface WebTokenServiceIntf {

	void deleteExpiredTokens(Session session);
}
