package  in.fortytwo42.adapter.service;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.handler.SMSHandler;


public class SMSServiceImpl implements SMSServiceIntf {

    private static Logger logger= LogManager.getLogger(SMSServiceImpl.class);
	private Config config = Config.getInstance();

	
    private SMSServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final SMSServiceImpl INSTANCE = new SMSServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static SMSServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    
	@Override
	public String sendSMS24By7(String mobile , String message) throws IOException {
        try {
            String smsApiUrl = config.getProperty(Constant.SMS + Constant._DOT + Constant.SMS_URL);
            String apikey = config.getProperty(Constant.SMS + Constant._DOT + Constant.SMS_API_KEY);
            String senderId = config.getProperty(Constant.SMS + Constant._DOT + Constant.SMS_SENDER_ID);
            String serviceName = config.getProperty(Constant.SMS + Constant._DOT + Constant.SMS_SERVICE_NAME);
            String sms = SMSHandler.sendSMS(smsApiUrl, apikey, senderId, serviceName, mobile, message);
            logger.log(Level.ERROR, "SMS sent to " + mobile);
            return sms;
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw e;
        }
	}


    @Override
    public boolean sendApplicationLinks(String mobile) {
        try {
            if (config.getProperty(Constant.MODE) == Constant.MODE_PRODUCTION) {
                String smsApiUrl = config.getProperty(Constant.SMS + Constant._DOT + Constant.SMS_URL);
                String apikey = config.getProperty(Constant.SMS + Constant._DOT + Constant.SMS_API_KEY);
                String senderId = config.getProperty(Constant.SMS + Constant._DOT + Constant.SMS_SENDER_ID);
                String serviceName = config.getProperty(Constant.SMS + Constant._DOT + Constant.SMS_SERVICE_NAME);
                SMSHandler.sendSMS(smsApiUrl, apikey, senderId, serviceName, mobile, Constant.APPLICATION_DOWNLOAD_LINK);
            }
            return true;
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }
    }
}
