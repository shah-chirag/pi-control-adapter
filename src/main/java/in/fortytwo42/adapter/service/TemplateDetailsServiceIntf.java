package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.TemplateDetails;
import in.fortytwo42.tos.enums.NotificationType;
import in.fortytwo42.tos.transferobj.TemplateDetailsTO;
import org.hibernate.Session;

import java.util.List;

public interface TemplateDetailsServiceIntf {


    TemplateDetails onboardTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO);

    TemplateDetails editTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws NotFoundException, AuthException;

    TemplateDetails deleteTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws NotFoundException;

    List<TemplateDetailsTO> getPaginatedList(int pageNo, int i, String searchText);

    Long getPaginatedListCount(String searchText);

    TemplateDetails getTemplateDetailsByTemplateIdAndType(String templateId, NotificationType type,Session session) throws NotFoundException;

    TemplateDetails getLastTemplate(Session session) throws NotFoundException;

}
