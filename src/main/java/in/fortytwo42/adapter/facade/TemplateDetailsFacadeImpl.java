package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.TemplateDetailsServiceIntf;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.TemplateDetailsDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.TemplateDetails;
import in.fortytwo42.entities.bean.ValidationRule;
import in.fortytwo42.tos.transferobj.TemplateDetailsTO;
import in.fortytwo42.tos.transferobj.ValidationRuleTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;
import java.util.Objects;

public class TemplateDetailsFacadeImpl implements TemplateDetailsFacadeIntf {


    private String TEMPLATE_DETAILS_FACADE_API_LOG = "<<<<< TemplateDetailsFacadeImpl";

    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();

    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private Config config = Config.getInstance();

    /**
     * The logger.
     */
    private final Logger logger = LogManager.getLogger(TemplateDetailsFacadeImpl.class);

    private RequestServiceIntf requestService = ServiceFactory.getRequestService();

    private TemplateDetailsServiceIntf templateDetailsServiceIntf = ServiceFactory.getTemPlateDetailsService();

    private TemplateDetailsDaoIntf templateDetailsDaoIntf = DaoFactory.getTemplateDetailsDao();


    private TemplateDetailsFacadeImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final TemplateDetailsFacadeImpl INSTANCE = new TemplateDetailsFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static TemplateDetailsFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public TemplateDetailsTO onboardTemplateDetails(String role, String actor, Long id, TemplateDetailsTO templateDetailsTO, boolean saveRequest) throws AuthException {

        logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " onboardTemplateDetails : start");
        Session session = IamThreadContext.getSessionWithoutTransaction();
        session.beginTransaction();
        try {
            try {
                templateDetailsServiceIntf.getTemplateDetailsByTemplateIdAndType(templateDetailsTO.getTemplateId(), templateDetailsTO.getTemplateType(), session);
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_TEMP_NAME_ALREADY_PRESENT());
            } catch (NotFoundException e) {
                logger.log(Level.DEBUG, "existing request not found, new Request creation start");
            }
            templateDetailsTO = requestService.createOnboardTemplateDetailsRequest(id, session, templateDetailsTO, actor, saveRequest);
            if (!saveRequest) {
                templateDetailsTO = approveOnboardTemplateDetails(session, templateDetailsTO);
            }
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " onboardTemplateDetails : end");
        }
        return templateDetailsTO;

    }

    @Override
    public TemplateDetailsTO approveOnboardTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws AuthException {
        logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " approveOnboardTemplateDetails : start");
        TemplateDetails templateDetails = templateDetailsServiceIntf.onboardTemplateDetails(session, templateDetailsTO);
        logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " approveOnboardTemplateDetails : end");
        return templateDetails.convertToTO();

    }

    @Override
    public TemplateDetailsTO editTemplateDetails(String role, String actor, Long id, TemplateDetailsTO templateDetailsTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " editTemplateDetails : start");
        Session session = IamThreadContext.getSessionWithoutTransaction();
        session.beginTransaction();
        try {
            validateIsExistingAndUpdatedDataSame(session, templateDetailsTO);
            templateDetailsTO = requestService.createEditTemplateDetailsRequest(id, session, templateDetailsTO, actor, saveRequest);
            if (!saveRequest) {
                templateDetailsTO = approveEditTemplateDetails(session, templateDetailsTO);
            }
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " editTemplateDetails : end");
        }
        return templateDetailsTO;

    }

    private void validateIsExistingAndUpdatedDataSame(Session session, TemplateDetailsTO templateDetailsTO) throws AuthException {
        try {
            TemplateDetails templateDetails = templateDetailsDaoIntf.getTemplateDetailsById(session, templateDetailsTO.getId());
            boolean isTemplate = false;
            boolean isType = false;
            boolean isTemplateId = false;
            boolean validationRule = true;
            if (templateDetails.getTemplate().equals(templateDetailsTO.getTemplate())) {
                isTemplate = true;
            }
            if (templateDetails.getTemplateId().equals(templateDetailsTO.getTemplateId())) {
                isTemplateId = true;
            }
            if (templateDetails.getTemplateType().equals(templateDetailsTO.getTemplateType())) {
                isType = true;
            }
            List<in.fortytwo42.entities.bean.ValidationRule> validationRules = templateDetails.getValidationRules();
            List<ValidationRuleTO> validationRuleTOS = templateDetailsTO.getValidationRuleTOList();
            if (validationRules.size() != validationRuleTOS.size()) {
                validationRule = false;
            } else {
                for (ValidationRuleTO validationRuleTO : templateDetailsTO.getValidationRuleTOList()) {
                    ValidationRule validationRule1 = new ValidationRule();
                    validationRule1.setRule(validationRuleTO.getRule());
                    validationRule1.setTemplateDetails(templateDetails);
                    int index = validationRules.indexOf(validationRule1);
                    if (index >= 0) {
                        ValidationRule validationRule2 = validationRules.get(index);
                        if (!validationRule2.getIsValidationReturn() == validationRuleTO.getIsValidationReturn() || Objects.equals(validationRuleTO.getAction(), Constant.DELETE)) {
                            validationRule = false;
                            break;
                        }
                    } else {
                        validationRule = false;
                        break;
                    }
                }
            }
            if (isTemplate && isType && isTemplateId && validationRule) {
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
            }
        } catch (NotFoundException e) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_TEMPLATE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TEMPLATE_NOT_FOUND());
        }
    }

    @Override
    public TemplateDetailsTO approveEditTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws AuthException {
        try {
            TemplateDetails templateDetails = templateDetailsServiceIntf.editTemplateDetails(session, templateDetailsTO);
            return templateDetails.convertToTO();
        } catch (NotFoundException e) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_TEMPLATE_NOT_FOUND());
        } finally {
            logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " approveOnboardTemplateDetails : end");
        }
    }

    @Override
    public TemplateDetailsTO createDeleteTemplateDetailsRequest(String role, String actor, Long id, TemplateDetailsTO templateDetailsTO, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " createDeleteTemplateDetailsRequest : start");
        Session session = IamThreadContext.getSessionWithoutTransaction();
        session.beginTransaction();
        try {
            try {
                templateDetailsDaoIntf.getTemplateDetailsById(session, templateDetailsTO.getId());
            } catch (NotFoundException e) {
                session.getTransaction().rollback();
                throw new AuthException(new Exception(), errorConstant.getERROR_CODE_TEMPLATE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_TEMPLATE_NOT_FOUND());
            }
            templateDetailsTO = requestService.createDeleteTemplateDetailsRequest(id, session, templateDetailsTO, actor, saveRequest);
            if (!saveRequest) {
                templateDetailsTO = approveDeleteTemplateDetails(session, templateDetailsTO);
            }
            sessionFactoryUtil.closeSession(session);
        } catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " createDeleteTemplateDetailsRequest : end");
        }
        return templateDetailsTO;


    }

    public TemplateDetailsTO approveDeleteTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws AuthException {

        logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " approveDeleteTemplateDetails : start");
        try {
            TemplateDetails templateDetails = templateDetailsServiceIntf.deleteTemplateDetails(session, templateDetailsTO);
            return templateDetails.convertToTO();
        } catch (NotFoundException e) {
            throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), e.getMessage());
        } finally {
            logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " approveDeleteTemplateDetails : end");
        }
    }


    @Override
    public PaginatedTO<TemplateDetailsTO> getAllTemplateDetails(int pageNo, String searchText) {
        logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " getAllTemplateDetails : start");
        PaginatedTO<TemplateDetailsTO> templateTOs = new PaginatedTO<>();
        List<TemplateDetailsTO> templateDetailsTOS = templateDetailsServiceIntf.getPaginatedList(pageNo, Integer.parseInt(config.getProperty(Constant.LIMIT)), searchText);
        templateTOs.setList(templateDetailsTOS);
        templateTOs.setTotalCount(templateDetailsServiceIntf.getPaginatedListCount(searchText));
        logger.log(Level.DEBUG, TEMPLATE_DETAILS_FACADE_API_LOG + " getAllTemplateDetails : end");
        return templateTOs;
    }

    @Override
    public String getTemplateDetailsId() throws AuthException {
        Session session = sessionFactoryUtil.openSessionWithoutTransaction();
        try {
            TemplateDetails templateDetails = templateDetailsServiceIntf.getLastTemplate(session);
            String id = Constant.TEMP_ID;
            if (templateDetails != null ) {
                long newIdNumber = templateDetails.getId() + 1;
                return id + String.format("%04d", newIdNumber);
            } else {
                return id + String.format("%04d", 1);
            }
        }catch (NotFoundException e){
            logger.log(Level.ERROR,e.getMessage(),e);
            throw new AuthException(new Exception(),errorConstant.getERROR_CODE_TEMPLATE_NOT_FOUND(),errorConstant.getERROR_MESSAGE_TEMPLATE_NOT_FOUND());
        }
        finally {
           sessionFactoryUtil.closeSessionWithoutCommit(session);
        }

    }
}
