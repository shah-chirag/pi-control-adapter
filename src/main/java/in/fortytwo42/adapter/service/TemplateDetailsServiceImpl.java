package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.TemplateDetailsDaoIntf;
import in.fortytwo42.daos.dao.ValidationRuleDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.TemplateDetails;
import in.fortytwo42.entities.bean.ValidationRule;
import in.fortytwo42.tos.enums.NotificationType;
import in.fortytwo42.tos.transferobj.TemplateDetailsTO;
import in.fortytwo42.tos.transferobj.ValidationRuleTO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class TemplateDetailsServiceImpl implements TemplateDetailsServiceIntf {


    TemplateDetailsDaoIntf templateDetailsDaoIntf = DaoFactory.getTemplateDetailsDao();

    private String TEMPLATE_DETAILS_SERVICE_API_LOG = "<<<<< TemplateDetailsServiceImpl";

    private static Logger logger = LogManager.getLogger(TemplateDetailsServiceImpl.class);

    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private ValidationRuleDaoIntf validationRuleDaoIntf = DaoFactory.getValidationRuleDao();

    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();


    private TemplateDetailsServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final TemplateDetailsServiceImpl INSTANCE = new TemplateDetailsServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static TemplateDetailsServiceImpl getInstance() {
        return TemplateDetailsServiceImpl.InstanceHolder.INSTANCE;
    }


    @Override
    public TemplateDetails onboardTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) {
        logger.log(Level.DEBUG, TEMPLATE_DETAILS_SERVICE_API_LOG + " approveOnboardTemplateDetails : start");

        TemplateDetails templateDetails = new TemplateDetails();
        templateDetails.setTemplateId(templateDetailsTO.getTemplateId());
        templateDetails.setTemplate(templateDetailsTO.getTemplate());
        templateDetails.setTemplateType(templateDetailsTO.getTemplateType());
        TemplateDetails temp = templateDetailsDaoIntf.create(session, templateDetails);
        List<ValidationRule> validationRuleList = new ArrayList<>();
        for (ValidationRuleTO validationRuleTO : templateDetailsTO.getValidationRuleTOList()) {
            ValidationRule validationRule = new ValidationRule();
            validationRule.setTemplateDetails(temp);
            validationRule.setRule(validationRuleTO.getRule());
            validationRule.setValidationReturn(validationRuleTO.getIsValidationReturn());
            validationRuleList.add(validationRule);
        }
        validationRuleDaoIntf.bulkInsert(session, validationRuleList);
        temp.setValidationRules(validationRuleList);
        return temp;
    }

    public List<ValidationRule> convertTOToEntity(List<ValidationRuleTO> validationRuleTO) {
        List<ValidationRule> validationRules = new ArrayList<>();
        for (ValidationRuleTO validationRuleTO1 : validationRuleTO) {
            ValidationRule validationRule = new ValidationRule();
            validationRule.setRule(validationRuleTO1.getRule());
            validationRules.add(validationRule);
        }
        return validationRules;
    }


    @Override
    public TemplateDetails editTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws NotFoundException, AuthException {

        TemplateDetails templateDetails = templateDetailsDaoIntf.getTemplateDetailsById(session, templateDetailsTO.getId());
         getValidationRuleList(templateDetailsTO, templateDetails,session);
        boolean templateType = false;
        boolean template = false;
        if (templateDetailsTO.getTemplateType().equals(templateDetails.getTemplateType())) {
            templateType = true;
        }
        if (templateDetailsTO.getTemplate().equals(templateDetails.getTemplate())) {
            template = true;
        }
        if (!templateType || !template ) {
            templateDetails.setTemplateType(templateDetailsTO.getTemplateType());
            templateDetails.setTemplate(templateDetailsTO.getTemplate());
            templateDetails.setTemplateId(templateDetailsTO.getTemplateId());
            templateDetails.setValidationRules(convertTOToEntity(templateDetailsTO.getValidationRuleTOList()));
            try {
                templateDetails = templateDetailsDaoIntf.update(session, templateDetails);

            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                throw e;
            }
        }
        return templateDetails;
    }

    private  List<ValidationRule> getValidationRuleList(TemplateDetailsTO templateDetailsTO, TemplateDetails templateDetails, Session session) throws NotFoundException, AuthException {
        List<ValidationRule> validationRuleList =  templateDetails.getValidationRules();

        for(ValidationRuleTO validationRuleTO : templateDetailsTO.getValidationRuleTOList()){
            if(validationRuleTO.getAction()!=null && !validationRuleTO.getAction().isEmpty()) {
                ValidationRule validationRule = new ValidationRule();
                if (validationRuleTO.getAction().equalsIgnoreCase(Constant.ADD.toString())) {
                    validationRule.setTemplateDetails(templateDetails);
                    validationRule.setRule(validationRuleTO.getRule());
                    validationRule.setValidationReturn(validationRuleTO.getIsValidationReturn());
                    if (validationRuleList.contains(validationRule)) {
                        throw new AuthException(new Exception(), errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_VALIDATION_RULE_ALREADY_EXIST());
                    } else {
                        validationRuleDaoIntf.create(session, validationRule);
                    }
                }
                if (validationRuleTO.getAction().equalsIgnoreCase(Constant.UPDATE)) {
                    ValidationRule validation = validationRuleDaoIntf.getActiveById(validationRuleTO.getRuleId(),session);
                    validation.setRule(validationRuleTO.getRule());
                    validation.setValidationReturn(validationRuleTO.getIsValidationReturn());
                    validationRuleDaoIntf.update(session, validation);

                }
                if (validationRuleTO.getAction().equalsIgnoreCase(Constant.DELETE)) {
                    ValidationRule validation = validationRuleDaoIntf.getActiveById(validationRuleTO.getRuleId(),session);
                    List<ValidationRule> validationRules = new ArrayList<>();
                    validationRules.add(validation);
                    validationRuleDaoIntf.bulkDelete(session, validationRules);
                }
            }


        }
        return validationRuleList;
    }

    @Override
    public TemplateDetails deleteTemplateDetails(Session session, TemplateDetailsTO templateDetailsTO) throws NotFoundException {
        TemplateDetails templateDetails =  templateDetailsDaoIntf.getTemplateDetailsById(session,templateDetailsTO.getId());
        List<TemplateDetails> templateDetailsList = new ArrayList<>();
        templateDetailsList.add(templateDetails);
        try {
            List<ValidationRule> validationRules=validationRuleDaoIntf.getValidationRuleByTemplateID(session,templateDetails.getTemplateId());
            validationRuleDaoIntf.bulkDelete(session,validationRules);
            templateDetailsDaoIntf.bulkDelete(session,templateDetailsList);

        }catch (Exception e ){
           logger.log(Level.ERROR,e.getMessage(),e);
           throw  e;
        }
        return templateDetails;
    }

    @Override
    public List<TemplateDetailsTO> getPaginatedList(int pageNo, int limit, String searchText) {
        logger.log(Level.DEBUG, TEMPLATE_DETAILS_SERVICE_API_LOG + " getPaginatedList : start");
        Session session = sessionFactoryUtil.openSessionWithoutTransaction();
        try {
            List<TemplateDetails> templateDetailsList = templateDetailsDaoIntf.getPaginatedList(pageNo, limit, searchText,session);
            List<TemplateDetailsTO> templateDetailsTOS = new ArrayList<>();
            templateDetailsList.forEach(e -> templateDetailsTOS.add(e.convertToTO()));
            logger.log(Level.DEBUG, TEMPLATE_DETAILS_SERVICE_API_LOG + " getPaginatedList : end");
            return templateDetailsTOS;
        }finally {
            if(session.isOpen()){
                session.close();
            }
        }
    }

    @Override
    public Long getPaginatedListCount(String searchText) {
        Session session = sessionFactoryUtil.openSessionWithoutTransaction();
        try {
            return templateDetailsDaoIntf.getTotalActiveCount(searchText, session);
        }finally {
            if(session.isOpen()){
                session.close();
            }
        }
    }


    @Override
    public TemplateDetails getTemplateDetailsByTemplateIdAndType(String templateId , NotificationType type ,Session session) throws NotFoundException {
       return templateDetailsDaoIntf.getTemplateDetailsByTemplateIdAndType(templateId,type,session);
    }
    @Override
    public TemplateDetails getLastTemplate(Session session) throws NotFoundException {
        return templateDetailsDaoIntf.getLastTemplate(session);
    }


}
