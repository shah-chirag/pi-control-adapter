
package in.fortytwo42.adapter.util;

import java.security.cert.CertificateException;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.ApprovalAttemptPollerTO;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.enterprise.extension.utils.GsonProvider;
import in.fortytwo42.entities.bean.AuthenticationAttempt;

// TODO: Auto-generated Javadoc
/**
 * The Class CallbackUtil.
 */
public class CallbackUtil {

    /** The callback util. */
    private static String CALLBACK_UTIL_LOG = "<<<<< CallbackUtil";

    private static Logger logger= LogManager.getLogger(CallbackUtil.class);

    private UserServiceIntf userProcessor = ServiceFactory.getUserService();
    /**
     * Instantiates a new callback util.
     */
    private CallbackUtil() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {

        /** The Constant INSTANCE. */
        private static final CallbackUtil INSTANCE = new CallbackUtil();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of CallbackUtil.
     *
     * @return single instance of CallbackUtil
     */
    public static CallbackUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Send auth data to callback url.
     *
     * @param callbackUrl the callback url
     * @param approvalAttemptPollerTO the approval attempt poller TO
     */
    public void sendAuthDataToCallbackUrl(String callbackUrl, ApprovalAttemptPollerTO approvalAttemptPollerTO) {
        logger.log(Level.DEBUG, CALLBACK_UTIL_LOG + " sendAuthDataToCallbackUrl : start");
        try {
            ClientConfig config = new ClientConfig();
            config.register(GsonProvider.class);
            config.register(new LoggingFeature());
            Client client = ClientBuilder.newClient(config);
            WebTarget webTarget = client.target(callbackUrl);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            invocationBuilder.post(Entity.entity(approvalAttemptPollerTO, MediaType.APPLICATION_JSON), ApprovalAttemptPollerTO.class);
        }
        catch (WebApplicationException e) {
           logger.log(Level.ERROR, e.getMessage(), e);
            //			ProcessorFactory.getAuditLoggingProcessor().log(AuditLog.class, Constant.SYSTEM, AuditLogConstant.HEALTH_CHECK, e.getMessage(), AuditLogType.ERROR);
        }
        finally {
            logger.log(Level.DEBUG, CALLBACK_UTIL_LOG + " sendAuthDataToCallbackUrl : start");
        }
    }

    /**
     * Send auth data to cn B.
     *
     * @param callbackUrl the callback url
     * @param authToken the auth token
     * @param approvalAttemptTOResponse the approval attempt TO response
     */
    public void sendAuthDataToCnB(String callbackUrl, String authToken, AuthenticationAttempt approvalAttemptTOResponse) {
        logger.log(Level.DEBUG, CALLBACK_UTIL_LOG + " sendAuthDataToCnB : start");
        String approvalStatus;
        switch (approvalAttemptTOResponse.getAttemptStatus()) {
            case "APPROVED":
                approvalStatus = "A";
                break;
            case "REJECTED":
                approvalStatus = "R";
                break;
            case "TIMEOUT":
                approvalStatus = "T";
                break;
            default:
                approvalStatus = "T";
                break;
        }
        String requestData = "<REQUEST><APP_ID>CO</APP_xID><OPERATION_ID>IAM</OPERATION_ID><USER_ID>" +userProcessor.getUsername(approvalAttemptTOResponse.getUser().getId()) 
                             + "</USER_ID><TOKEN_NO>"
                             + approvalAttemptTOResponse.getTransactionId()
                             + "</TOKEN_NO><DATE_INIT>"
                             + approvalAttemptTOResponse.getDateTimeCreated()
                             + "</DATE_INIT><APPROVAL_STATUS>"
                             + approvalStatus
                             + "</APPROVAL_STATUS><CODTXN_TYPE>EX</CODTXN_TYPE></REQUEST>";
        logger.log(Level.TRACE, "CNB Callback Request[" + requestData + "]");
        String base64RequestData = Base64.getEncoder().encodeToString(requestData.getBytes());
       logger.log(Level.TRACE, "CNB Callback Request Base64[" + base64RequestData + "]");
        String finalRequest =
                            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:api=\"http://api.webservices.xjava.fcat.iflex.com\"><soap:Header /><soap:Body><api:processRequest><api:p_account_xml>"
                              + base64RequestData
                              + "</api:p_account_xml></api:processRequest></soap:Body></soap:Envelope>";
        logger.log(Level.TRACE, "CNB Callback Soap Request[" + finalRequest + "]");
        sendData(callbackUrl, authToken, finalRequest);
        logger.log(Level.DEBUG, CALLBACK_UTIL_LOG + " sendAuthDataToCnB : end");
    }

    /**
     * Send auth data to cn B.
     *
     * @param callbackUrl the callback url
     * @param authToken the auth token
     * @param approvalAttemptTOResponse the approval attempt TO response
     */
    public void sendAuthDataToCnB(String callbackUrl, String authToken, AuthenticationAttemptTO approvalAttemptTOResponse) {
        logger.log(Level.DEBUG, CALLBACK_UTIL_LOG + " sendAuthDataToCnB : start");
        String approvalStatus;
        switch (approvalAttemptTOResponse.getApprovalStatus()) {
            case "APPROVED":
                approvalStatus = "A";
                break;
            case "REJECTED":
                approvalStatus = "R";
                break;
            case "TIMEOUT":
                approvalStatus = "T";
                break;
            default:
                approvalStatus = "T";
                break;
        }
        String requestData = "<REQUEST><APP_ID>CO</APP_ID><OPERATION_ID>IAM</OPERATION_ID><USER_ID>" + "approvalAttemptTOResponse.getUsername()"
                             + "</USER_ID><TOKEN_NO>"
                             + approvalAttemptTOResponse.getTransactionId()
                             + "</TOKEN_NO><DATE_INIT>"
                             + "approvalAttemptTOResponse.getDateTimeCreated()"
                             + "</DATE_INIT><APPROVAL_STATUS>"
                             + approvalStatus
                             + "</APPROVAL_STATUS><CODTXN_TYPE>EX</CODTXN_TYPE></REQUEST>";
        logger.log(Level.TRACE, "CNB Callback Request[" + requestData + "]");
        String base64RequestData = Base64.getEncoder().encodeToString(requestData.getBytes());
        logger.log(Level.TRACE, "CNB Callback Request Base64[" + base64RequestData + "]");
        String finalRequest =
                            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:api=\"http://api.webservices.xjava.fcat.iflex.com\"><soap:Header /><soap:Body><api:processRequest><api:p_account_xml>"
                              + base64RequestData
                              + "</api:p_account_xml></api:processRequest></soap:Body></soap:Envelope>";
        logger.log(Level.TRACE, "CNB Callback Soap Request[" + finalRequest + "]");
        sendData(callbackUrl, authToken, finalRequest);
        logger.log(Level.DEBUG, CALLBACK_UTIL_LOG + " sendAuthDataToCnB : end");
    }

    /**
     * Send data.
     *
     * @param callbackUrl the callback url
     * @param authToken the auth token
     * @param finalRequest the final request
     */
    public void sendData(String callbackUrl, String authToken, String finalRequest) {
        logger.log(Level.DEBUG, CALLBACK_UTIL_LOG + " sendData : start");
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
            } };
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    // TODO Auto-generated method stub
                    return true;
                }
            };

            OkHttpClient client = new OkHttpClient();
            com.squareup.okhttp.MediaType mediaType = com.squareup.okhttp.MediaType.parse("text/xml;charset=UTF-8");
            RequestBody body = RequestBody.create(mediaType, finalRequest);
            Request request = new Request.Builder().url(callbackUrl).method("POST", body).addHeader("Content-Type", "text/xml;charset=UTF-8")
                    .addHeader("Authorization", AES128Impl.decryptData(authToken, KeyManagementUtil.getAESKey())).addHeader("USER_AGENT", "USER_AGENT").build();
            Response response = client.setSslSocketFactory(sc.getSocketFactory()).setHostnameVerifier(allHostsValid).newCall(request).execute();
            logger.log(Level.TRACE, "CNB Callback Response[" + response.body().string() + "]");

        }
        catch (WebApplicationException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            //			ProcessorFactory.getAuditLoggingProcessor().log(AuditLog.class, Constant.SYSTEM, AuditLogConstant.HEALTH_CHECK, e.getMessage(), AuditLogType.ERROR);
        }
        catch (Exception e) {
           logger.log(Level.ERROR, e.getMessage(), e);
            //			ProcessorFactory.getAuditLoggingProcessor().log(AuditLog.class, Constant.SYSTEM, AuditLogConstant.HEALTH_CHECK, e.getMessage(), AuditLogType.ERROR);
        }
        finally {
            logger.log(Level.DEBUG, CALLBACK_UTIL_LOG + " sendData : end");
        }
    }
}
