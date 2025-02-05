package in.fortytwo42.adapter.controller;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.SHAImpl;
import in.fortytwo42.adapter.util.StringUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.UserFoundOnIDSException;
import in.fortytwo42.enterprise.extension.exceptions.ValidationException;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

public class SetCredentialsOnboarder implements Onboarder {

    private static Logger logger = LogManager.getLogger(SetCredentialsOnboarder.class);
    private static final String SET_CREDENTIAL_ONBOARDER_LOG = "<<<<< SetCredentialsOnboarder";

    private static final class InstanceHolder {
        private static final SetCredentialsOnboarder INSTANCE = new SetCredentialsOnboarder();

        private InstanceHolder() {
            super();
        }
    }

    public static SetCredentialsOnboarder getInstance() {
        return SetCredentialsOnboarder.InstanceHolder.INSTANCE;
    }

    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        return userTO.getUserCredential() == null || userTO.getUserCredential().isEmpty();
    }

    @Override
    public void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session) throws AuthException, ValidationException, UserFoundOnIDSException {
        logger.log(Level.DEBUG, SET_CREDENTIAL_ONBOARDER_LOG + " process : start");
        setCredentials(userTO, account.getId());
        logger.log(Level.DEBUG, SET_CREDENTIAL_ONBOARDER_LOG + " process : end");
    }

    public void setCredentials(UserIciciTO userTO, String accountId) throws AuthException {
        logger.log(Level.DEBUG, SET_CREDENTIAL_ONBOARDER_LOG + " setCredentials : start");
        String hashedPassword = StringUtil.getHex(SHAImpl.hashData256(StringUtil.build(IAMConstants.SALT, accountId, userTO.getUserCredential()).getBytes()));
        AccountWE accountWEForEdit = new AccountWE();
        accountWEForEdit.setApplicationId(userTO.getSubscribedApplications().get(0).getApplicationId());
        accountWEForEdit.setUserCredential(hashedPassword);
        accountWEForEdit.setAttributes(null);
        ServiceFactory.getIamExtensionService().editUserCredentials(accountWEForEdit, accountId);
        logger.log(Level.DEBUG, SET_CREDENTIAL_ONBOARDER_LOG + " setCredentials : end");
    }
}
