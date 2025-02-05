
package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserResponseTO;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.tos.transferobj.HotpTO;
import in.fortytwo42.tos.transferobj.OtpAuditLogRespTO;
import in.fortytwo42.tos.transferobj.OtpAuditLogSendTO;

public interface HOTPFacadeIntf {

    HotpTO generateOtp(HotpTO hotpTO, String applicationId) throws AuthException;

    HotpTO validateOtp(HotpTO hotpTO, String applicationId) throws AuthException, ApplicationNotFoundException;
    UserResponseTO tokenValidation(String userName, String ipAddress, String userAgent, String authToken) throws AuthException;

    HotpTO validateOtp(HotpTO hotpTO, String applicationId, boolean isEncrypted) throws AuthException;

    PaginatedTO<OtpAuditLogRespTO> getAllOtpAuditSearch1Log(int page, int pageSize, String applicationName,
                                                            String userId);
}
