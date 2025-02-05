
package in.fortytwo42.adapter.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.EnterpriseDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.Enterprise;
import in.fortytwo42.entities.enums.Status;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.transferobj.EnterpriseTO;

public class EnterpriseServiceImpl implements EnterpriseServiceIntf {

    private static final String ENTERPRISE_SERVICE_IMPL_LOG = "<<<<< EnterpriseServiceImpl";

    private EnterpriseDaoIntf enterpriseDao = DaoFactory.getEnterpriseDao();
    private static Logger logger=LogManager.getLogger(EnterpriseServiceImpl.class);

    private static final class InstanceHolder {
        private static final EnterpriseServiceImpl INSTANCE = new EnterpriseServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static EnterpriseServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public EnterpriseTO onboardEnterprise(EnterpriseTO enterpriseTO, Session session) {
        logger.log(Level.DEBUG, ENTERPRISE_SERVICE_IMPL_LOG + " onboardEnterprise : start");
        Enterprise enterprise = new Enterprise();
        enterprise.setEnterpriseId(enterpriseTO.getEnterpriseId());
        enterprise.setEnterpriseName(enterpriseTO.getEnterpriseName());
        enterprise.setEnterpriseAccountId(enterpriseTO.getEnterpriseAccountId());
        enterprise.setEnterprisePassword(AES128Impl.encryptData(enterpriseTO.getEnterprisePassword(), KeyManagementUtil.getAESKey()));
        enterprise.setEnterpriseSecret(AES128Impl.encryptData(enterpriseTO.getEnterpriseSecret(), KeyManagementUtil.getAESKey()));
        enterprise.setStatus(Status.ACTIVE);
        enterprise = enterpriseDao.create(session, enterprise);
        logger.log(Level.DEBUG, ENTERPRISE_SERVICE_IMPL_LOG + " onboardEnterprise : end");
        return enterprise.convertToTO();
    }

    @Override
    public Integer getEnterpriseCount() {
        logger.log(Level.DEBUG, ENTERPRISE_SERVICE_IMPL_LOG + " getEnterpriseCount : start");
        Integer count;
        try {
            count = enterpriseDao.getAllData().size();
        }
        catch (NotFoundException e) {
            logger.log(Level.ERROR, e);
            count = 0;
        }
        logger.log(Level.DEBUG, ENTERPRISE_SERVICE_IMPL_LOG + " getEnterpriseCount : end");
        return count;
    }

    @Override
    public List<EnterpriseTO> getEnterprises() {
        logger.log(Level.DEBUG, ENTERPRISE_SERVICE_IMPL_LOG + " getEnterprise : start");
        List<EnterpriseTO> enterpriseTOs = new ArrayList<>();
        List<Enterprise> enterprises = enterpriseDao.getAll();
        enterpriseTOs = new EntityToTOConverter<Enterprise, EnterpriseTO>().convertEntityListToTOList(enterprises);
        logger.log(Level.DEBUG, ENTERPRISE_SERVICE_IMPL_LOG + " getEnterprise : end");
        return enterpriseTOs;
    }
    
    @Override
    public Enterprise getEnterprise() {
        logger.log(Level.DEBUG, ENTERPRISE_SERVICE_IMPL_LOG + " getEnterprise : start");
        List<Enterprise> enterprises = enterpriseDao.getAll();
        logger.log(Level.DEBUG, ENTERPRISE_SERVICE_IMPL_LOG + " getEnterprise : end");
        Enterprise enterprise = null;
        if(enterprises != null && !enterprises.isEmpty()) {
            enterprise = enterprises.get(0);
        }
        return enterprise;
    }

    @Override
    public Enterprise updateEnterprise(Session session,Enterprise enterprise) {
        return enterpriseDao.update(session, enterprise);
    }
}
