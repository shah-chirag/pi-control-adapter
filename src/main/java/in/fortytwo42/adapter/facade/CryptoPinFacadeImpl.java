package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import in.fortytwo42.adapter.enums.ResponseStatus;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.EnterpriseServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.CryptoPinTO;
import in.fortytwo42.adapter.util.AES128Impl;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.KeyManagementUtil;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.Enterprise;

public class CryptoPinFacadeImpl implements CryptoPinFacadeIntf{
    private static final String CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG = "<<<<< CryptoPinResource";
    private static Logger logger= LogManager.getLogger(CryptoPinFacadeImpl.class);
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    private EnterpriseServiceIntf encryptionService = ServiceFactory.getEnterpriseService();
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();


    private final IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

    private static final class InstanceHolder {
        /** The Constant INSTANCE. */
        private static final CryptoPinFacadeImpl INSTANCE = new CryptoPinFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }
    public static CryptoPinFacadeImpl getInstance() {
        return CryptoPinFacadeImpl.InstanceHolder.INSTANCE;
    }

    @Override
    public CryptoPinTO resetApplicationPin(CryptoPinTO cryptoPinTO) throws AuthException {
        logger.log(Level.DEBUG, CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG + " resetApplicationPin : start");
        Session session = sessionFactoryUtil.getSession();
        Application application = null;
        try {
            application = applicationService.getApplicationByApplicationId(cryptoPinTO.getApplicationId());
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
            AccountWE accountWE = iamExtensionService.getAccount(Constant.USER_ID, cryptoPinTO.getApplicationId());
            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty() || !accountWE.getAccountType().equals(AccountType.APPLICATION)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
            }
            try {
                iamExtensionV2.resetPin(accountWE.getCryptoDID(), accountWE.getId(), cryptoPinTO.getPassword(),"Application", reqRefNum);
            }
            catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                throw iamExceptionConvertorUtil.convertToAuthException(e);
            }
            AccountWE changePassAccount=new AccountWE();
            changePassAccount.setUserCredential(cryptoPinTO.getPassword());
            iamExtensionService.editUserCredentials(changePassAccount, application.getApplicationAccountId());
            application.setPassword(AES128Impl.encryptData(cryptoPinTO.getPassword(), KeyManagementUtil.getAESKey()));
            applicationService.updateApplication(session, application);
            sessionFactoryUtil.closeSession(session);
        }catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        cryptoPinTO.setStatus(ResponseStatus.SUCCESS);

        logger.log(Level.DEBUG, CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG + " resetApplicationPin : end");

        return cryptoPinTO;
    }

    @Override
    public CryptoPinTO changeApplicationPin(CryptoPinTO cryptoPinTO) throws AuthException{
        logger.log(Level.DEBUG, CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG + " changeApplicationPin : start");
        Session session = sessionFactoryUtil.getSession();
        Application application;
        try {
            if(cryptoPinTO.getPassword().equals(cryptoPinTO.getOldPassword())){
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_PASSWORD(),errorConstant.getERROR_MESSAGE_SAME_PASSWORD());
            }
            application = applicationService.getApplicationByApplicationId(cryptoPinTO.getApplicationId());
            if(!cryptoPinTO.getOldPassword().equals(AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()))){
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_PASSWORD(), errorConstant.getHUMANIZED_ERROR_VALIDATE_PASSWORD());
            }
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
            AccountWE accountWE = iamExtensionService.getAccount(Constant.USER_ID, cryptoPinTO.getApplicationId());
            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty() || !accountWE.getAccountType().equals(AccountType.APPLICATION)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
            }
            try {
                if (accountWE.getId() == null) {
                    throw new IAMException(null, IAMConstants.ERROR_CODE_ACCOUNT_NOT_PRESENT,
                            IAMConstants.ERROR_MSG_ACCOUNT_NOT_PRESENT);
                }
                iamExtensionV2.changePin(accountWE.getCryptoDID(), accountWE.getId(), cryptoPinTO.getOldPassword(), cryptoPinTO.getPassword(),"Application", reqRefNum);
            }
            catch (IAMException e) {
                logger.log(Level.DEBUG, e.getMessage(), e);
                throw iamExceptionConvertorUtil.convertToAuthException(e);
            }
            AccountWE changePassAccount=new AccountWE();
            changePassAccount.setUserCredential(cryptoPinTO.getPassword());
            iamExtensionService.editUserCredentials(changePassAccount, application.getApplicationAccountId());
            application.setPassword(AES128Impl.encryptData(cryptoPinTO.getPassword(), KeyManagementUtil.getAESKey()));
            applicationService.updateApplication(session, application);
            sessionFactoryUtil.closeSession(session);
        }catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        cryptoPinTO.setStatus(ResponseStatus.SUCCESS);
        logger.log(Level.DEBUG, CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG + " changeApplicationPin : end");
        return cryptoPinTO;
    }

    @Override
    public CryptoPinTO resetEnterprisePin(CryptoPinTO cryptoPinTO) throws AuthException{
        logger.log(Level.DEBUG, CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG + " resetEnterprisePin : start");
        Session session = sessionFactoryUtil.getSession();
        Enterprise enterprise = null;
        try {
            enterprise = encryptionService.getEnterprise();
            if(enterprise == null){
                throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_FOUND());
            }
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
            AccountWE accountWE = iamExtensionService.getAccount(Constant.USER_ID, enterprise.getEnterpriseId());
            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty() || !accountWE.getAccountType().equals(AccountType.ENTERPRISE)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
            }
            try {
                iamExtensionV2.resetPin(accountWE.getCryptoDID(), accountWE.getId(), cryptoPinTO.getPassword(),"Enterprise", reqRefNum);
            }
            catch (IAMException e) {
                logger.log(Level.DEBUG, e.getMessage(), e);
                throw iamExceptionConvertorUtil.convertToAuthException(e);
            }
            AccountWE changePassAccount=new AccountWE();
            changePassAccount.setUserCredential(cryptoPinTO.getPassword());
            iamExtensionService.editUserCredentials(changePassAccount, enterprise.getEnterpriseAccountId());
            enterprise.setEnterprisePassword(AES128Impl.encryptData(cryptoPinTO.getPassword(), KeyManagementUtil.getAESKey()));
            encryptionService.updateEnterprise(session, enterprise);
            sessionFactoryUtil.closeSession(session);
        }catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        cryptoPinTO.setStatus(ResponseStatus.SUCCESS);
        logger.log(Level.DEBUG, CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG + " resetEnterprisePin : end");
        return cryptoPinTO;
    }

    @Override
    public CryptoPinTO changeEnterprisePin(CryptoPinTO cryptoPinTO) throws AuthException{
        logger.log(Level.DEBUG, CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG + " changeEnterprisePin : start");
        Session session = sessionFactoryUtil.getSession();
        Enterprise enterprise;
        try {
            if(cryptoPinTO.getPassword().equals(cryptoPinTO.getOldPassword())){
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_PASSWORD(),errorConstant.getERROR_MESSAGE_SAME_PASSWORD());
            }
            enterprise = encryptionService.getEnterprise();
            if(enterprise == null){
                throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_FOUND());
            }
            if(!cryptoPinTO.getOldPassword().equals(AES128Impl.decryptData(enterprise.getEnterprisePassword(), KeyManagementUtil.getAESKey()))){
                throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_PASSWORD(), errorConstant.getHUMANIZED_ERROR_VALIDATE_PASSWORD());
            }
            String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
            IAMExtensionV2 iamExtensionV2 = iamExtensionService.getIAMExtension();
            AccountWE accountWE = iamExtensionService.getAccount(Constant.USER_ID, enterprise.getEnterpriseId());
            if (accountWE == null || accountWE.getId() == null || accountWE.getId().isEmpty() || !accountWE.getAccountType().equals(AccountType.ENTERPRISE)) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ACCOUNT_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ACCOUNT_NOT_FOUND());
            }
            try {
                iamExtensionV2.changePin(accountWE.getCryptoDID(), accountWE.getId(), cryptoPinTO.getOldPassword(), cryptoPinTO.getPassword(),"Enterprise", reqRefNum);
            }
            catch (IAMException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                throw iamExceptionConvertorUtil.convertToAuthException(e);
            }
            AccountWE changePassAccount=new AccountWE();
            changePassAccount.setUserCredential(cryptoPinTO.getPassword());
            iamExtensionService.editUserCredentials(changePassAccount, enterprise.getEnterpriseAccountId());
            enterprise.setEnterprisePassword(AES128Impl.encryptData(cryptoPinTO.getPassword(), KeyManagementUtil.getAESKey()));
            encryptionService.updateEnterprise(session, enterprise);
            sessionFactoryUtil.closeSession(session);
        }catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        cryptoPinTO.setStatus(ResponseStatus.SUCCESS);
        logger.log(Level.DEBUG, CRYPTO_PIN_WORKFLOW_FACADE_IMPL_LOG + " changeEnterprisePin : end");
        return cryptoPinTO;
    }




}


