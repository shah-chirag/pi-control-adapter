
package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.TokenTO;

public interface TokenServiceIntf {

    /**
     * 
     * @param tokenTO
     * @return
     * @throws AuthException 
     */
    TokenTO editToken(TokenTO tokenTO) throws AuthException;

    /**
     * 
     * @param tokenTO
     * @return
     * @throws AuthException 
     */
    TokenTO editTokenRemoteWipe(TokenTO tokenTO) throws AuthException;

}
