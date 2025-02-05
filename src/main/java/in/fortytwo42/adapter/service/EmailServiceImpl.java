
package  in.fortytwo42.adapter.service;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.handler.EmailHandler;

public class EmailServiceImpl implements EmailServiceIntf {

	private static Logger logger= LogManager.getLogger(EmailServiceImpl.class);

    private Config config = Config.getInstance();

	private EmailServiceImpl() {
		super();
	}

	private static final class InstanceHolder {
		private static final EmailServiceImpl INSTANCE = new EmailServiceImpl();

		private InstanceHolder() {
			super();
		}
	}

	public static EmailServiceImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}

	@Override
	public String sendEmail(String messageBody, String emailIds, String subject) throws MessagingException {
		boolean ismailSent = false;
		String host = config.getProperty(Constant.EMAIL_HOST);
		String sender = config.getProperty(Constant.EMAIL_SENDER);
		String password = config.getProperty(Constant.EMAIL_PASSWORD);
		String port = config.getProperty(Constant.EMAIL_PORT);
		String enableTTLS = config.getProperty(Constant.EMAIL_ENABLE_TTLS);
		return EmailHandler.sendEmail(messageBody,emailIds,subject,host,sender,password,port,enableTTLS);

	}
}
