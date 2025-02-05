package in.fortytwo42.adapter.service;

import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.tos.transferobj.FalloutTO;
import in.fortytwo42.tos.transferobj.OtpAuditLogRespTO;
import in.fortytwo42.tos.transferobj.OtpAuditLogSendTO;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.integration.exception.ActiveMQConectionException;
import in.fortytwo42.tos.transferobj.HotpTO;

import java.util.List;

public interface HOTPServiceIntf {

    HotpTO generateOtp(HotpTO hotpTO, Session session, Application application, User user, String attributeValue) throws AuthException, ActiveMQConectionException;

    HotpTO validateOtp (HotpTO hotpTO, Session session, String applicationId,String applicationName, String userAccountId, Integer tokenTtl, String enterpriseAccountId) throws AuthException;

    public List<OtpAuditLogSendTO> getAllOtpAuditLogSearchPaginatedList(int page, int pageSize, String applicationName, String userId) throws AttributeNotFoundException ;

    public Long getAllOtpAuditLogSearchPaginatedCount(String applicationName, String userId, Session session) throws AttributeNotFoundException ;

    public List<OtpAuditLogRespTO> getAllOtpAuditLogSearch1PaginatedList(int page, int pageSize, String applicationName, String userId, Session session) throws AttributeNotFoundException;
}
