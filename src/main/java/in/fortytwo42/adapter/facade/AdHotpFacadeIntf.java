package in.fortytwo42.adapter.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itzmeds.adfs.client.SignOnException;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.tos.transferobj.AdHotpTO;

public interface AdHotpFacadeIntf {

    AdHotpTO generateAdOtp(AdHotpTO adHotpTO, String applicationId, String service) throws SignOnException, JsonProcessingException, AuthException, AttributeNotFoundException;
}
