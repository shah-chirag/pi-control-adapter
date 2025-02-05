package in.fortytwo42.adapter.service;

import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.UserLock;
import org.hibernate.Session;
public interface UserLockServiceIntf {

	UserLock getUserLockByAttribute(String attribute) throws NotFoundException;
	UserLock addUserLock(Session session, UserLock userLock);

	UserLock deleteUserLock(Session session, UserLock userLock);
}
