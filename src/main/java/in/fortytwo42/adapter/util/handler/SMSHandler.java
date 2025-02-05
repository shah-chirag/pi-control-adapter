
package  in.fortytwo42.adapter.util.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * sms gateway integration
 * @author ChiragShah
 *
 */
public class SMSHandler {

    private static final String APIKEY = "APIKEY=";
    private static final String SENDER_ID = "SenderID=";
    private static final String SERVICE_NAME = "ServiceName=";
    private static final String MOBILE_NO = "MobileNo=";
    private static final String MESSAGE = "Message=";

    private static Logger logger= LogManager.getLogger(SMSHandler.class);
    
    /** The Constant USER_BINDING. */
    private static final String SMSHANDLER = "SMSHandler";
    
    private SMSHandler() {}

    /**
     * api call to send sms
     * @param mobileNo
     * @param verificationKey
     * @param activationCode
     * @throws IOException
     */
    public static String sendSMS(String smsApiUrl, String apikey, String senderId, String serviceName, String mobileNo, String message) throws IOException {
        String urlString = smsApiUrl + "?" + APIKEY + apikey + "&" + SENDER_ID + senderId + "&" + SERVICE_NAME + serviceName + "&" + MOBILE_NO + mobileNo + "&" + MESSAGE + message;
        urlString = urlString.replaceAll(" ", "%20");

        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(SMSHandler.sendSMS("http://smsapi.24x7sms.com/api_2.0/SendSMS.aspx", "UPlpclPnhZo", "IAMMsg", "TEMPLATE_BASED", "+919769394754", "Dear User, your I-AM activation code is 184324."));
    }
}
