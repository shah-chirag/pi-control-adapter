
package in.fortytwo42.adapter.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserUserGroupRelTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserUserGroupRelDaoInf;
import in.fortytwo42.daos.exception.UserUserGroupRelNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.UserUserGroupCompositeKey;
import in.fortytwo42.entities.bean.UserUserGroupRel;

public class UserUserGroupRelServiceImpl implements UserUserGroupRelServiceIntf {

    private static final String USER_USER_GROUP_REL_SERVICE_LOG = "<<<<< UserUserGroupRelServiceImpl";

    private static Logger logger= LogManager.getLogger(UserUserGroupRelServiceImpl.class);
    
    private UserUserGroupRelDaoInf userUserGroupRelDaoInf = DaoFactory.getUserUserGroupRelDao();

    private SessionFactoryUtil sessionFactoryUtil;

    private static final String SEARCH_QUERY = "searchQuery";
    
    private UserUserGroupRelServiceImpl() {
        super();
    }

    private static final class InstanceHolder {

        private static UserUserGroupRelServiceImpl INSTACE = new UserUserGroupRelServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static UserUserGroupRelServiceImpl getInstance() {
        return InstanceHolder.INSTACE;
    }

    @Override
    public UserUserGroupRel create(Session session, UserUserGroupRel userGroupRel) {
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG +" create : start");
        userGroupRel = userUserGroupRelDaoInf.create(session, userGroupRel);
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG +" create : end");
        return userGroupRel;
    }

    @Override
    public UserUserGroupRel getUserUserGroupForId(UserUserGroupCompositeKey userGroupCompositeKey) throws UserUserGroupRelNotFoundException {
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG +" getUserUserGroupForId : start");
        UserUserGroupRel userGroupRel = userUserGroupRelDaoInf.getUserUserGroupForId(userGroupCompositeKey);
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG +" getUserUserGroupForId : end");
        return userGroupRel;
    }

    @Override
    public List<UserUserGroupRel> getUserUserGroupRel(Long id) {
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG +" getUserUserGroupRel : start");
        List<UserUserGroupRel> userGroupRels = userUserGroupRelDaoInf.getUserUserGroupRel(id);
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG +" getUserUserGroupRel : end");
        return userGroupRels;
    }

    @Override
    public UserUserGroupRel update(Session session, UserUserGroupRel userGroupRel) {
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG + " update : start");
        userGroupRel = userUserGroupRelDaoInf.update(session, userGroupRel);
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG + " update : end");
        return userGroupRel;
    }
    
    @Override
    public PaginatedTO<UserUserGroupRelTO> getUserUserGroupMapping(long userGoupId, int pageNo, String searchText) {
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG + " getUserUserGroupMapping : start");
        List<Object[]> userGroupRels;
        long count ;
        if(searchText != null && ! searchText.isEmpty()) {
            String isAttributesInUpperCase = Config.getInstance().getProperty(Constant.IS_ATTRIBUTE_IN_UPPER_CASE);
            Boolean isAttributeUpperCase = isAttributesInUpperCase != null && !isAttributesInUpperCase.isEmpty() && Boolean.parseBoolean(isAttributesInUpperCase);
            List<String> accountIds = DaoFactory.getAttributeStoreDao().getAccountIdsBySearch(searchText,isAttributeUpperCase);
            logger.log(Level.DEBUG, "<<<< AccountId : "+accountIds.toString());
            userGroupRels = userUserGroupRelDaoInf.getUserUserGroupRel(userGoupId, searchText, pageNo, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)), accountIds);
            count = userUserGroupRelDaoInf.getUserUserGroupRelCount(userGoupId, searchText, accountIds);
        }else {
            userGroupRels = userUserGroupRelDaoInf.getUserUserGroupRel(userGoupId, searchText, pageNo, Integer.parseInt(Config.getInstance().getProperty(Constant.LIMIT)));
            count = userUserGroupRelDaoInf.getUserUserGroupRelCount(userGoupId, searchText);
        }
        List<UserUserGroupRelTO> userUserGrouprelTos = new ArrayList<>();
        for (Iterator<Object[]> iterator = userGroupRels.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            long userId = Long.parseLong(String.valueOf(row[0]));
            UserUserGroupRelTO userUserGroupRelTO = new UserUserGroupRelTO();
            //userUserGroupRelTO.setFullname(String.valueOf(row[3]));
            //userUserGroupRelTO.setMobile(String.valueOf(row[2]));
            AttributeStore attributeStore = DaoFactory.getAttributeStoreDao().getRegisteredByAttribute(userId);
            if (attributeStore != null) {
                userUserGroupRelTO.setUsername(attributeStore.getAttributeValue());
            }
            userUserGroupRelTO.setUserId(userId);
            userUserGroupRelTO.setTwoFactorStatus(String.valueOf(row[1]));
            userUserGroupRelTO.setAccountId(String.valueOf(row[2]));
            userUserGrouprelTos.add(userUserGroupRelTO);
        }
        PaginatedTO<UserUserGroupRelTO> paginatedTO = new PaginatedTO<>();
        paginatedTO.setList(userUserGrouprelTos);
        paginatedTO.setTotalCount(count);
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG + " getUserUserGroupMapping : end");
        return paginatedTO;
    }

    @Override
    public String getAccountIds(Long userGroupId) {
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG + " getAccountIds : start");
        List<Object[]> userGroupRels = userUserGroupRelDaoInf.userGroupUsers(userGroupId);
        StringBuilder accountIds = new StringBuilder();
        String accountids = "";
        for (Iterator<Object[]> iterator = userGroupRels.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            accountIds.append(row[0]).append(Constant._COMMA);
        }
        if (accountIds.length() > 0) {
            accountids = accountIds.subSequence(0, accountIds.length() - 1).toString();
        }
        logger.log(Level.DEBUG, USER_USER_GROUP_REL_SERVICE_LOG + " getAccountIds : end");
        return accountids;
    }


}
