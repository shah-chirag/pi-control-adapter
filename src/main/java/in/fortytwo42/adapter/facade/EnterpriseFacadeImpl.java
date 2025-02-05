
package in.fortytwo42.adapter.facade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.EnterpriseServiceIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CryptoJS;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.Enterprise;
import in.fortytwo42.tos.transferobj.EnterpriseTO;

public class EnterpriseFacadeImpl implements EnterpriseFacadeIntf {

    private static final String ENTERPRISE_FACADE_IMPL_LOG = "<<<<< EnterpriseFacadeImpl";

    private static Logger logger= LogManager.getLogger(EnterpriseFacadeImpl.class);
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private EnterpriseServiceIntf enterpriseService = ServiceFactory.getEnterpriseService();

    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    private static final class InstanceHolder {

        private static final EnterpriseFacadeImpl INSTANCE = new EnterpriseFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static EnterpriseFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public EnterpriseTO onboardEnterprise(EnterpriseTO enterpriseTO) throws AuthException {
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " onboardEnterprise : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            Integer count = enterpriseService.getEnterpriseCount();
            if (count == 0) {
                String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
                IAMExtensionV2 iamExtension = ServiceFactory.getIamExtensionService().getIAMExtension();
                AccountWE enterpriseAccount = iamExtension.getAccount(Constant.USER_ID, enterpriseTO.getEnterpriseId());
                if (enterpriseAccount == null || enterpriseAccount.getId() == null) {
                    throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ENTERPRISE_NOT_FOUND());
                }
                String did = iamExtension.getDID(enterpriseTO.getEnterpriseAccountId(), reqRefNum);
                logger.log(Level.DEBUG, "Enterprise DID : "+did);
                Config config = Config.getInstance();
                String enterprisePassword = CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), enterpriseTO.getEnterprisePassword());
                enterpriseTO.setEnterprisePassword(enterprisePassword);
                enterpriseTO.setEnterpriseSecret(CryptoJS.decryptData(config.getProperty(Constant.APPLICATION_ENCRYPTION_KEY), enterpriseTO.getEnterpriseSecret()));
                enterpriseTO = enterpriseService.onboardEnterprise(enterpriseTO, session);
                updateEnterpriseProperties(enterpriseTO.getEnterpriseId(), enterpriseTO.getEnterpriseAccountId(), enterpriseTO.getEnterpriseName(), enterpriseTO.getEnterprisePassword());
                enterpriseTO.setEnterprisePassword(null);
                sessionFactoryUtil.closeSession(session);
            }
            else {
                throw new AuthException(null, errorConstant.getERROR_CODE_ENTERPRISE_ALREADY_ONBOARDED(), errorConstant.getERROR_MESSAGE_ENTERPRISE_ALREADY_ONBOARDED());
            }
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        catch (IAMException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            IAMExceptionConvertorUtil.getInstance().convertToAuthException(e);
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " onboardEnterprise : end");
        }
        return enterpriseTO;
    }

    @Override
    public List<EnterpriseTO> getEnterprises() {
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " getEnterprises : start");
        List<EnterpriseTO> enterpriseTOs = enterpriseService.getEnterprises();
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " getEnterprises : end");
        return enterpriseTOs;
    }

    private void updateEnterpriseProperties(String enterpriseId, String enterpriseAccountId, String enterpriseName, String enterprisePassword) {
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " updateEnterpriseProperties : start");
        System.out.println("enterpriseId : "+enterpriseId);
        System.out.println("enterpriseAccountId : "+enterpriseAccountId);
        System.out.println("enterpriseName : "+enterpriseName);
        System.out.println("enterprisePassword : "+enterprisePassword);
        Map<Object, Object> configProperties = new HashMap<>();
        configProperties.put(Constant.ENTERPRISE_ID, enterpriseId);
        configProperties.put(Constant.ENTERPRISE_ACCOUNT_ID, enterpriseAccountId);
        configProperties.put(Constant.ENTERPRISE_NAME, enterpriseName);
        configProperties.put(Constant.ENTERPRISE_PASSWORD, enterprisePassword);
        Config.getInstance().updateProperties(configProperties);
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " updateEnterpriseProperties : end");
    }

    public void initializeService() {
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " initializeService : start");
        Enterprise enterprise = enterpriseService.getEnterprise();
        if (enterprise != null && enterprise.getEnterpriseId() != null) {
            //String enterprisePassword = AES128Impl.decryptData(enterprise.getEnterprisePassword(), KeyManagementUtil.getAESKey());
            String enterprisePassword = enterprise.getEnterprisePassword();
            updateEnterpriseProperties(enterprise.getEnterpriseId(), enterprise.getEnterpriseAccountId(), enterprise.getEnterpriseName(), enterprisePassword);
        }
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " initializeService : end");
    }

    @Override
    public void uploadFile(String fileName, InputStream cryptoFile) throws AuthException {
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " uploadFile : start");
        Config config = Config.getInstance();
        String escFolderPath = config.getProperty(Constant.ESC_FOLDER_PATH) + Constant.FILE_SPERATOR + "Fortytwo42";
        File file = new File(escFolderPath);
        if (!file.exists()) {
            file.mkdir();
        }
        String filePath = escFolderPath + File.separator + fileName;
        saveCryptoFile(filePath, cryptoFile);
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " uploadFile : end");
    }

    private void saveCryptoFile(String path, InputStream cryptoFile) throws AuthException {
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " saveCryptoFile : start");
        OutputStream out = null;
        try {
            int read = 0;
            byte[] bytes = new byte[1024];
            out = new FileOutputStream(new File(path));
            while ((read = cryptoFile.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_IO_EXCEPTION(), errorConstant.getERROR_MESSAGE_IO_EXCEPTION());
        }
        finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            }
            catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
        logger.log(Level.DEBUG, ENTERPRISE_FACADE_IMPL_LOG + " saveCryptoFile : end");
    }
}
