package in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.entities.bean.Enterprise;
import in.fortytwo42.tos.transferobj.EnterpriseTO;

public interface EnterpriseServiceIntf {

    EnterpriseTO onboardEnterprise(EnterpriseTO enterpriseTO, Session session);

    Integer getEnterpriseCount();

    List<EnterpriseTO> getEnterprises();

    Enterprise getEnterprise();

    Enterprise updateEnterprise(Session session,Enterprise enterprise);

}
