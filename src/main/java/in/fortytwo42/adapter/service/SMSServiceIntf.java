package in.fortytwo42.adapter.service;

import java.io.IOException;

public interface SMSServiceIntf {

	String sendSMS24By7(String mobile, String message) throws IOException;
	
    boolean sendApplicationLinks(String mobile);
}