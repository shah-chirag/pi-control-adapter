package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.util.Set;

import in.fortytwo42.entities.enums.AdminSessionState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.AdminLoginLogServiceIntf;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.AttributeStoreServiceImpl;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.HOTPServiceIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.UserResponseTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.AttributeValidationUtil;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.adapter.util.RSAUtil;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.Enterprise;
import in.fortytwo42.entities.bean.Role;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.enums.AuthenticationStatus;
import in.fortytwo42.entities.enums.LoginStatus;
import in.fortytwo42.entities.enums.SessionState;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.integration.exception.ActiveMQConectionException;
import in.fortytwo42.tos.enums.NotificationStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.HotpTO;
import in.fortytwo42.tos.transferobj.OtpAuditLogRespTO;
import in.fortytwo42.tos.transferobj.OtpAuditLogSendTO;

public class HOTPFacadeImpl implements HOTPFacadeIntf{

    /** The HOTPFacadeImpl facade impl log. */
    private String HOTP_FACADE_IMPL_LOG = "<<<<< HOTPFacadeImpl";

    private static Logger logger= LogManager.getLogger(HOTPFacadeImpl.class);
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    /** The evidence store processor intf. */
    private HOTPServiceIntf hotpService = ServiceFactory.getHOTPService();

    private static IamExtensionServiceIntf iamExtension = ServiceFactory.getIamExtensionService();

    private final IAMUtil iamUtil = IAMUtil.getInstance();

    private final IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();

    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    //use it in facade layer
    private UserApplicationRelDaoIntf userApplicationDao = DaoFactory.getUserApplicationRel();

    private UserServiceIntf userService = ServiceFactory.getUserService();

    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();


    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final HOTPFacadeImpl INSTANCE = new HOTPFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    public static HOTPFacadeImpl getInstance() {
        return HOTPFacadeImpl.InstanceHolder.INSTANCE;
    }
    @Override
    public HotpTO generateOtp(HotpTO hotpTO, String applicationId) throws AuthException {
        logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " generateOtp : start");

        for (AttributeDataTO attributeDataTO: hotpTO.getSearchAttributes()) {
            AttributeValidationUtil.validateSearchAttributeValueAndUniquenessWithoutCrypto(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());
        }

        IamThreadContext.getSessionWithoutTransaction().beginTransaction();
        try {
            String searchAttributeValue = hotpTO.getSearchAttributes().get(0).getAttributeValue().toUpperCase();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> getApplicationByApplicationId |Epoch:"+System.currentTimeMillis());
            Application application = applicationService.getApplicationByApplicationId(applicationId);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> getApplicationByApplicationId |Epoch:"+System.currentTimeMillis());

            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> getIAMExtensionV2 |Epoch:"+System.currentTimeMillis());
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2WithoutCrypto(application.getEnterprise().getEnterpriseAccountId());
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> getIAMExtensionV2 |Epoch:"+System.currentTimeMillis());

            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> authenticateV2 |Epoch:"+System.currentTimeMillis());
            Token token = iamUtil.authenticateV2WithoutCrypto(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> authenticateV2 |Epoch:"+System.currentTimeMillis());

            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> searchAccount |Epoch:"+System.currentTimeMillis());
            AccountWE accountWE = iamExtensionService.searchAccount(hotpTO.getSearchAttributes(), iamExtension, token);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> searchAccount |Epoch:"+System.currentTimeMillis());

            String accountId = accountWE.getId();
            User user = null;

            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> getActiveUser |Epoch:"+System.currentTimeMillis());
            user = userService.getActiveUser(IamThreadContext.getSessionWithoutTransaction(), accountId);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> getActiveUser |Epoch:"+System.currentTimeMillis());

            if(user.getTwoFactorStatus().equals(TwoFactorStatus.DISABLED)) {
                throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_USER(), errorConstant.getERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_USER());

            }
            if(application.getTwoFactorStatus().equals(TwoFactorStatus.DISABLED)) {
                throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION(), errorConstant.getERROR_MESSAGE_TWO_FACTOR_AUTH_DISABLED_FOR_APPLICATION());

            }

            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> isApplicationUserBindingPresent |Epoch:"+System.currentTimeMillis());
            boolean isBindingPresent = userApplicationDao.isApplicationUserBindingPresent(application.getId(),user.getId(), IamThreadContext.getSessionWithoutTransaction());
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> isApplicationUserBindingPresent |Epoch:"+System.currentTimeMillis());

            if(!isBindingPresent) {
                throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_USER_APPLICATION_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_APPLICATION_BINDING_NOT_FOUND());
            }

            if(hotpTO.getNotification() != null) {
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> generateOtp |Epoch:"+System.currentTimeMillis());
                HotpTO optResponse = hotpService.generateOtp(hotpTO, IamThreadContext.getSessionWithoutTransaction(), application, user, searchAttributeValue);
                logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> generateOtp |Epoch:"+System.currentTimeMillis());

                AuditLogUtil.sendAuditLog( "otp generated successfully ", "USER", ActionType.AUTHENTICATE_ONLINE, "", IdType.ACCOUNT, "", null, "", null);
                IamThreadContext.getSessionWithoutTransaction().getTransaction().commit();
                return optResponse;
            }else{
                throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_NOTIFICATION_TYPE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_NOTIFICATION_TYPE_NOT_FOUND());
            }

        } catch (IAMException e) {
            IamThreadContext.getSessionWithoutTransaction().getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        } catch (AuthException e) {
            IamThreadContext.getSessionWithoutTransaction().getTransaction().rollback();
            throw e;
        } catch (ActiveMQConectionException e) {
            throw new AuthException(new Throwable(), errorConstant.getERROR_CODE_INVALID_CONNECTION_SETTINGS(), errorConstant.getERROR_MESSAGE_INVALID_CONNECTION_SETTINGS());
        } finally {
            logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " generateOtp : end");
        }
    }

    private String getAccountId(HotpTO hotpTO) throws AuthException {
        String searchAttributeValue = null;
        String attributeName = null;

        for (AttributeDataTO attributeDataTO : hotpTO.getSearchAttributes()) {
            attributeName = attributeDataTO.getAttributeName();
            searchAttributeValue = attributeDataTO.getAttributeValue();
        }
        IAMExtensionV2 iamExtensionV2 = iamExtension.getIAMExtension();
        return iamExtension.getAccountId(attributeName, searchAttributeValue, iamExtensionV2);
    }

    @Override
    public HotpTO validateOtp(HotpTO hotpTO, String applicationId) throws AuthException {
        logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " validateOtp : start");

        for (AttributeDataTO attributeDataTO : hotpTO.getSearchAttributes()) {
            AttributeValidationUtil.validateSearchAttributeValueAndUniquenessWithoutCrypto(attributeDataTO.getAttributeName(), attributeDataTO.getAttributeValue());
        }
        IamThreadContext.getSessionWithoutTransaction().beginTransaction();
        try {
            String searchAttributeValue = hotpTO.getSearchAttributes().get(0).getAttributeValue().toUpperCase();
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> getApplicationByApplicationId |Epoch:"+System.currentTimeMillis());
            Application application = applicationService.getApplicationByApplicationId(applicationId);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> getApplicationByApplicationId |Epoch:"+System.currentTimeMillis());

            /*logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> getIAMExtensionV2 |Epoch:"+System.currentTimeMillis());
            IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2WithoutCrypto(application.getEnterprise().getEnterpriseAccountId());
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> getIAMExtensionV2 |Epoch:"+System.currentTimeMillis());

            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> authenticateV2 |Epoch:"+System.currentTimeMillis());
            Token token = iamUtil.authenticateV2WithoutCrypto(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> authenticateV2 |Epoch:"+System.currentTimeMillis());

            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> searchAccount |Epoch:"+System.currentTimeMillis());
            AccountWE accountWE = iamExtensionService.searchAccount(hotpTO.getSearchAttributes(), iamExtension, token);
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> searchAccount |Epoch:"+System.currentTimeMillis());

            String accountId = accountWE.getId();*/

            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "Start -> HOTPFacadeImpl -> validateOtp |Epoch:"+System.currentTimeMillis());
            HotpTO otpValidationResponse = hotpService.validateOtp(hotpTO,IamThreadContext.getSessionWithoutTransaction(),applicationId,application.getApplicationName(),searchAttributeValue, application.getTokenTtl(),
                    application.getEnterprise().getEnterpriseAccountId());
            logger.log(Level.DEBUG, "<<<<< PERFORMANCE " + "End -> HOTPFacadeImpl -> validateOtp |Epoch:"+System.currentTimeMillis());

            AuditLogUtil.sendAuditLog("token validated successfully ", "USER", ActionType.AUTHENTICATE_ONLINE, "", IdType.ACCOUNT, "", null, "", null);
            IamThreadContext.getSessionWithoutTransaction().getTransaction().commit();
            return otpValidationResponse;
        } catch (AuthException e) {
            IamThreadContext.getSessionWithoutTransaction().getTransaction().rollback();
            logger.log(Level.ERROR, e);
            throw e;
        }
        finally {
            logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " validateOtp : end");
        }
    }

    @Override
    public UserResponseTO tokenValidation(String userName, String ipAddress, String userAgent, String authToken) throws AuthException {
        logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " validateOtp : start");
        AdminLoginLogServiceIntf adminLoginLogService = ServiceFactory.getAdminLoginLogService();
        IamThreadContext.getSessionWithoutTransaction().beginTransaction();
        try {
            User user;
            try {
                user = ServiceFactory.getAttributeStoreService().getUserByAttributeValueWithUpperCase(userName);

            }
            catch (AttributeNotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_USER_NAME_TOKEN_INVALID(), errorConstant.getERROR_MESSAGE_USER_NAME_TOKEN_INVALID());
            }
            HotpTO hotp = new HotpTO();
            hotp.setAuthenticationToken(authToken);
            Enterprise enterprise;
            try{
                enterprise = DaoFactory.getEnterpriseDao().getEnterpriseByAccountId(Config.getInstance().getProperty(Constant.ENTERPRISE_ACCOUNT_ID));
            } catch (NotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_FOUND());
            }
            HotpTO otpValidationResponse =
                    hotpService.validateOtp(hotp, IamThreadContext.getSessionWithoutTransaction(), enterprise.getEnterpriseId(),enterprise.getEnterpriseName(), userName.toUpperCase(), null,
                            Config.getInstance().getProperty(Constant.ENTERPRISE_ACCOUNT_ID));
            UserResponseTO userResponseTO = new UserResponseTO();
            if (NotificationStatus.SUCCESS.equals(otpValidationResponse.getStatus())) {
                Set<Role> roleList = user.getRoles();
                StringBuilder roles = new StringBuilder();
                StringBuilder permissions = new StringBuilder();
                for (Role role : roleList) {
                    roles.append(role.getName().name()).append(Constant._COMMA);
                }
                roles.deleteCharAt(roles.lastIndexOf(Constant._COMMA));
                String token = FacadeFactory.getUserFacade().getToken(user.getId(),userName, roles.toString(),
                        permissions.toString(), ipAddress, userAgent, false);
                userResponseTO.setToken(token);
                userResponseTO.setStatus(Constant.SUCCESS_STATUS);
                ServiceFactory.getUserAuthPrincipalService().userAuditLog(IamThreadContext.getSessionWithoutTransaction(), userName, AuthenticationStatus.SUCCESS, SessionState.ACTIVE, token);
                int updatedRows=adminLoginLogService.updateAdminLoginLog(IamThreadContext.getSessionWithoutTransaction(), userName, LoginStatus.S2FA, AdminSessionState.A, null, roles.toString(),new Timestamp(System.currentTimeMillis()));
                if(updatedRows==0){
                    adminLoginLogService.adminLoginLog(userName, LoginStatus.S2FA,new Timestamp(System.currentTimeMillis()), AdminSessionState.A,roles.toString());
                }
                AuditLogUtil.sendAuditLog("token validated successfully ", "USER", ActionType.AUTHENTICATE_ONLINE, "", IdType.ACCOUNT, ThreadContext.get(Constant.REQUEST_REFERENCE),
                        null, "", null);
                IamThreadContext.getSessionWithoutTransaction().getTransaction().commit();
                return userResponseTO;
            }
            else {
                userResponseTO.setStatus(Constant.FAILURE_STATUS);
                adminLoginLogService.updateAdminLoginLog(IamThreadContext.getSessionWithoutTransaction(), userName, LoginStatus.F2FA, AdminSessionState.C, null, null,null);
                IamThreadContext.getSessionWithoutTransaction().getTransaction().commit();
                return userResponseTO;
            }
        }
        catch (AuthException e) {
            logger.log(Level.ERROR, e);
            IamThreadContext.getSessionWithoutTransaction().getTransaction().rollback();
            adminLoginLogService.updateAdminLoginLog(IamThreadContext.getSessionWithoutTransaction(), userName, LoginStatus.F2FA, AdminSessionState.C, null, null,null);
            throw e;
        }
        finally {
            logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " validateOtp : end");
        }
    }
    @Override
    public HotpTO validateOtp(HotpTO hotpTO, String applicationId, boolean isEncrypted) throws AuthException {
        logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " validateOtp : start");
        if (isEncrypted) {
            try {
                hotpTO.setAuthenticationToken(RSAUtil.decryptData(hotpTO.getAuthenticationToken()));
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                throw new AuthException(e, errorConstant.getERROR_CODE_INVALID_DATA(), errorConstant.getERROR_MESSAGE_HOTP_VALIDATION_FAILED());
            }
        }
        hotpTO = validateOtp(hotpTO, applicationId);
        logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " validateOtp : end");
        return hotpTO;
    }

    @Override
    public PaginatedTO<OtpAuditLogRespTO> getAllOtpAuditSearch1Log(int page, int pageSize, String attributeName, String searchText) {
        logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " getAllOtpAuditSearch1Log : start");
        PaginatedTO<OtpAuditLogRespTO> paginatedTO = new PaginatedTO<>();
        Session session = IamThreadContext.getSessionWithoutTransaction();
        try {
            List<OtpAuditLogRespTO> otpAuditLogList = hotpService.getAllOtpAuditLogSearch1PaginatedList(page, pageSize, attributeName, searchText, session);
            Long count = hotpService.getAllOtpAuditLogSearchPaginatedCount(attributeName,searchText, session);
            paginatedTO.setList(otpAuditLogList);
            paginatedTO.setTotalCount(count);
        }
        catch (AttributeNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            paginatedTO.setList(new ArrayList<>());
            paginatedTO.setTotalCount(0l);
        }
        finally {
            if(session.isOpen()){
                session.close();
            }
            logger.log(Level.DEBUG, HOTP_FACADE_IMPL_LOG + " getAllOtpAuditSearch1Log : end");
        }
        return paginatedTO;
    }

}
