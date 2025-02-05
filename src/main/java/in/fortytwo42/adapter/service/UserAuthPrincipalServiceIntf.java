
package in.fortytwo42.adapter.service;

import org.hibernate.Session;

import in.fortytwo42.entities.enums.AuthenticationStatus;
import in.fortytwo42.entities.enums.SessionState;

public interface UserAuthPrincipalServiceIntf {

    /**
     * Business function to create user auth principal
     * @param username
     * @param authenticationStatus 
     * @param sessionState
     * @param token 
     */
    void userAuditLog(Session session,String username, AuthenticationStatus authenticationStatus, SessionState sessionState, String token);

    void updateAuditLog(Session session, String username, String token);

    void updateAuditLog(String username, String token);

}
