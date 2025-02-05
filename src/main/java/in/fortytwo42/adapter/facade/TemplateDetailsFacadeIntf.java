package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.tos.transferobj.TemplateDetailsTO;
import org.hibernate.Session;

public interface TemplateDetailsFacadeIntf {


    TemplateDetailsTO onboardTemplateDetails(String role, String actor, Long id, TemplateDetailsTO templateDetailsTO, boolean saveRequest) throws AuthException;

    TemplateDetailsTO approveOnboardTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws AuthException;

    TemplateDetailsTO editTemplateDetails(String role, String actor, Long id, TemplateDetailsTO templateDetailsTO, boolean saveRequest) throws AuthException;

    TemplateDetailsTO createDeleteTemplateDetailsRequest(String role, String actor, Long id, TemplateDetailsTO templateDetailsTO, boolean saveRequest) throws AuthException;

    TemplateDetailsTO approveDeleteTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws AuthException;

    PaginatedTO<TemplateDetailsTO> getAllTemplateDetails(int pageNo, String searchText);

    TemplateDetailsTO approveEditTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws AuthException;

    String getTemplateDetailsId() throws AuthException;
}
