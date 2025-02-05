package in.fortytwo42.adapter.service;

import javax.mail.MessagingException;

public interface EmailServiceIntf {
	
	String sendEmail(String messageBody,String emailIds, String subject) throws MessagingException;
	
}
