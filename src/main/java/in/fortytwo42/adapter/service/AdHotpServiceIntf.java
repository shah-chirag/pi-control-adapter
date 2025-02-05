package in.fortytwo42.adapter.service;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.integration.exception.ActiveMQConectionException;
import in.fortytwo42.tos.transferobj.AdHotpTO;

public interface AdHotpServiceIntf {

    AdHotpTO generateHotp(AdHotpTO adHotpTO, Session session, String applicationId, String applicationName, String accountId, AdfsDetailsTO adfsDetailsTO, Integer attempts) throws AuthException, ActiveMQConectionException;
}
