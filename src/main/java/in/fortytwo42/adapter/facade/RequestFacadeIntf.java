
package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.tos.transferobj.RequestTO;

public interface RequestFacadeIntf {

    PaginatedTO<RequestTO> getAllRequests(RequestType requestType, String actionType, int page, String token, Long toDate, Long fromDate, String searchText) throws AuthException;

    PaginatedTO<RequestTO> getRequests(String actionType, int page, String token, Long toDate, Long fromDate, String requestType) throws AuthException;

    PaginatedTO<RequestTO> getPaginatedApproveAndRejectedRequests(int page, int limit, String role, String requestType);

    RequestTO approveRequest(RequestTO requestTO, String token) throws AuthException, IAMException;

    PaginatedTO<RequestTO> getPendingRequests(int page, String role, String requestType,String actionType) throws AuthException;
}
