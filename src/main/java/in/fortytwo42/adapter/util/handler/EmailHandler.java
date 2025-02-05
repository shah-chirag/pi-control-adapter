/**
 * 
 */

package in.fortytwo42.adapter.util.handler;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.client.Entity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itzmeds.adfs.client.SignOnException;
import com.itzmeds.adfs.client.SignOnService;
import com.itzmeds.adfs.client.SignOnServiceImpl;
import com.itzmeds.rac.core.ServiceUrlConfig;


/**
 * @author ChiragShah
 *
 */
public class EmailHandler {
    
    private EmailHandler() { 
        /*
         * Do Nothing
         */
    }
    private static Logger logger= LogManager.getLogger(EmailHandler.class);

    public static String sendEmail(String messageBody, String emailIds, String subject, String host, String sender, String password, String port, String enableTTLS) throws MessagingException {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host); // Setting up mail server
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.starttls.enable", enableTTLS);
        properties.put("mail.smtp.ssl.enable", enableTTLS);
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });
        session.setDebug(true);
        MimeMessage message = new MimeMessage(session); // MimeMessage object.
        message.setFrom(new InternetAddress(sender)); // Set From Field: adding senders email to from field.
        InternetAddress[] internetAddresses = InternetAddress.parse(emailIds);
        message.setRecipients(Message.RecipientType.TO, internetAddresses);
        message.setSubject(subject); // Set Subject: subject of the email
        message.setText(messageBody); // set body of the email.


        Transport.send(message); // Send email.

        logger.log(Level.INFO, "Email sent to " + emailIds);
        return "success";


    }

    public static void main(String args[]) throws SignOnException, JsonProcessingException {

        SignOnService signOnService = new SignOnServiceImpl();
//
//        1. SAML assertion token
//
//        String authRequest = signOnService.createSignOnRequest(domain+"\\"+mUsername, mPassword,
//                SignOnService.TokenTypes.SAML_TOKEN_TYPE);
//        2. Binary security token
//
//        String authRequest = signOnService.createSignOnRequest(domain+"\\"+mUsername, mPassword,
//                SignOnService.TokenTypes.BST_TOKEN_TYPE);
//        3. Json Web Token
//
        String authRequest = signOnService.createSignOnRequest("ashay@adfs.fortytwo42.in", "ft42@123$",SignOnService.TokenTypes.JWT_TOKEN_TYPE,"https://adfs.fortytwo42.in/adfs/services/trust/13/usernamemixed","urn:microsoft:adfs:claimsxray");
        System.out.println(authRequest);

        String loginSvcUrlTemplate = "{\"url.hostname\":\"adfs2016.adfs.fortytwo42.in\",\"url.port\":\"443\",\"url.resource.path\":\"/adfs/services/trust/13/usernamemixed\",\"url.ssl.enabled\":\"true\"}";

        ServiceUrlConfig authSvcUrlConfig = new ObjectMapper().readValue(loginSvcUrlTemplate, ServiceUrlConfig.class);

        RestClientTemplate restClientTemplateTemp = new RestServiceClient(1000000,1000000).createClientTemplate("LOGIN_ACCESS_TOKEN", authSvcUrlConfig);

        Entity<String> postCallInput = Entity.entity(authRequest, "application/soap+xml; charset=utf-8");

        String loginresponse = restClientTemplateTemp.create(null, null, postCallInput).readEntity(String.class);
        System.out.println("**********");
        System.out.println(loginresponse);
        System.out.println("**********");
        String authToken = signOnService.getJsonWebToken(loginresponse);
        System.out.println(authToken);
//        RequestedSecurityToken.Assertion samlAssertion = signOnService.getSamlToken(loginresponse);
//        System.out.println(samlAssertion.getAttributeStatement());
    }
}
