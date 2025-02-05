
package in.fortytwo42.adapter.facade;

import java.util.List;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.tos.transferobj.UserTO;

public interface TokenFacadeIntf {

    /**
     * 
     * @param page
     * @param pageSize
     * @return
     * @throws AuthException 
     */
    PaginatedTO<TokenTO> getTokens(Integer page, Integer pageSize) throws AuthException;

    /**
     * 
     * @param tokenTO
     * @param actor
     * @param saveRequest
     * @return
     * @throws AuthException
     */
    TokenTO editToken(TokenTO tokenTO, String actor,Long id, boolean saveRequest) throws AuthException;

    /**
     * 
     * @param tokenUDID
     * @param actor
     * @param saveRequest
     * @return
     * @throws AuthException
     */
    TokenTO editTokenRemoteWipe(TokenTO tokenTO, String actor,Long id, boolean saveRequest) throws AuthException;

    /**
     * 
     * @param tokenId
     * @return
     * @throws AuthException
     */
    TokenTO getToken(String tokenId) throws AuthException;

    /**
     * 
     * @param tokenId
     * @return
     * @throws AuthException 
     */
    List<UserTO> getUserInfo(String tokenId) throws AuthException;

}
