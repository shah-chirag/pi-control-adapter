package in.fortytwo42.adapter.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.controllers.IamThreadContext;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.ValidationException;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;

public class CryptoAttributesAndAttributeEditingOnboarder implements Onboarder{

    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(CryptoAttributesAndAttributeEditingOnboarder.class.getName());

    private String CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG = "<<<<< CryptoAttributesAndAttributeEditingOnboarder";

    private final CryptoAttributesOnboarder cryptoAttributesOnboarder = CryptoAttributesOnboarder.getInstance();
    private final AttributeEditingOnboarder attributeEditingOnboarder = AttributeEditingOnboarder.getInstance();
    private final Config config = Config.getInstance();
    private final ExecutorService pool;
    private final int retry;

    private static final class InstanceHolder {
        private static final CryptoAttributesAndAttributeEditingOnboarder INSTANCE = new CryptoAttributesAndAttributeEditingOnboarder();

        private InstanceHolder() {
            super();
        }
    }

    public static CryptoAttributesAndAttributeEditingOnboarder getInstance() {
        return InstanceHolder.INSTANCE;
    }


    private CryptoAttributesAndAttributeEditingOnboarder() {
        super();

        int poolSize = 1;
        try {
            poolSize = Integer.parseInt(config.getProperty(Constant.CRYPTO_ATTRIBUTES_ONBOARDER_THREADPOOL_SIZE));
        } catch (NumberFormatException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        pool = Executors.newFixedThreadPool(poolSize);

        int n = 5;
        try {
            n = Integer.parseInt(config.getProperty(Constant.ATTRIBUTE_EDITING_ONBOARDER_RETRY));
        } catch (NumberFormatException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        retry = n;
    }

    @Override
    public boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException {
        return cryptoAttributesOnboarder.validate(token, iamExtension, userTO, account, user) && attributeEditingOnboarder.validate(token, iamExtension, userTO, account, user);
    }

    @Override
    public void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session) throws AuthException, ValidationException {
        String reqRefNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        pool.submit(()->{
            ThreadContext.put(Constant.REQUEST_REFERENCE, reqRefNumber);
            IamThreadContext.setSessionWithoutTransaction(SessionFactoryUtil.getInstance().openSessionWithoutTransaction());
            logger.log(Level.DEBUG, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + ": process : start");

            try {
                if(!cryptoAttributesOnboarder.validate(token, iamExtension, userTO, account, user)){
                    cryptoAttributesOnboarder.process(token, iamExtension, userTO, account, user, IamThreadContext.getSessionWithoutTransaction());
                    logger.log(Level.DEBUG, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " Attribute successfully registered on crypto server");
                }
            } catch(AuthException e) {
                logger.log(Level.ERROR, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " Failed cryptoAttributesOnboarder");
            }

            logger.log(Level.DEBUG, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " UserIciciTO: "+new Gson().toJson(userTO));
            logger.log(Level.DEBUG, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " AccountWE: "+new Gson().toJson(account));

            try {
                if(!attributeEditingOnboarder.validate(token, iamExtension, userTO, account, user)){
                    int n = retry;
                    while (n > 0){
                        try {
                            attributeEditingOnboarder.process(token, iamExtension, userTO, account, user, IamThreadContext.getSessionWithoutTransaction());
                            logger.log(Level.DEBUG, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " Attribute successfully updated");
                            break;
                        }catch(AuthException e) {
                            n--;
                        }
                    }
                    if(n == 0){
                        logger.log(Level.ERROR, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " Failed attributeEditingOnboarder");
                    }
                }
            } catch(AuthException e) {
                logger.log(Level.ERROR, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " Failed attributeEditingOnboarder");
            }
            SessionFactoryUtil.getInstance().closeSessionWithoutCommit(IamThreadContext.getSessionWithoutTransaction());
            logger.log(Level.DEBUG, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " UserIciciTO: "+new Gson().toJson(userTO));
            logger.log(Level.DEBUG, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + " AccountWE: "+new Gson().toJson(account));
            logger.log(Level.DEBUG, CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER_LOG + ": process : end");
        });
    }
}
