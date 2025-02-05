
package in.fortytwo42.adapter.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.google.gson.Gson;

import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.enums.Environment;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;
import in.fortytwo42.enterprise.extension.utils.RandomString;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.ServiceTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class IAMUtil {

    private static String IAM_UTIL_LOG = "<<<<< IAMUtil";
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger = LogManager.getLogger(IAMUtil.class);

    private Config config = Config.getInstance();
    private final ExternalConfigUtil externalConfig=ExternalConfigUtil.getInstance();
    
    private IAMUtil() {
        super();
    }

    private static final class InstanceHolder {
        private static final IAMUtil INSTANCE = new IAMUtil();

        private InstanceHolder() {
            super();
        }
    }

    public static IAMUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public IAMExtensionV2 getIAMExtensionV2(String enterpriseId) throws IAMException {
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" getIAMExtensionV2 : start");
        IAMExtensionV2 iamExtension = TokenStore.getInstance().getIAMExtensionV2(enterpriseId+" "+true);
        if (iamExtension == null) {
            iamExtension = generateIAMExtensionV2(enterpriseId,true);
        }
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" getIAMExtensionV2 : end");
        return iamExtension;
    }

    public IAMExtensionV2 getIAMExtensionV2WithoutCrypto(String enterpriseId) throws IAMException {
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" getIAMExtensionV2 : start");
        IAMExtensionV2 iamExtension = TokenStore.getInstance().getIAMExtensionV2(enterpriseId+" "+false);
        if (iamExtension == null) {
            iamExtension = generateIAMExtensionV2(enterpriseId,false);
        }
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" getIAMExtensionV2 : end");
        return iamExtension;
    }
    
    private IAMExtensionV2 generateIAMExtensionV2(String enterpriseAccountId, boolean shouldInitializeJNIClass) throws IAMException {
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" generateIAMExtensionV2 : start");
        IAMExtensionV2 iamExtensionV2 = null;
        try {
            String hostUrl = config.getProperty(Constant.IAM_SERVICE_URL);
            String hostName = config.getProperty(Constant.IAM_PINNING_HOST_NAME);
            String pinningHash = config.getProperty(Constant.IAM_PINNING_HASH);
            String escFolderPath = config.getProperty(Constant.ESC_FOLDER_PATH);
            String cryptoServerIp = config.getProperty(Constant.CRYPTO_SERVER_URL);
            String cryptoServerPort = config.getProperty(Constant.CRYPTO_SERVER_PORT);
            String enterpriseName = config.getProperty(Constant.ENTERPRISE_NAME);
            boolean tlsEnabled = Boolean.parseBoolean(config.getProperty(Constant.TLS_ENABLED));

            int retries = Integer.parseInt(config.getProperty(Constant.CRYPTO_RETRIES));
            int qrTimeout = Integer.parseInt(config.getProperty(Constant.CRYPTO_QR_TIME_OUT));
            int  numTokenTimeOut = Integer.parseInt(config.getProperty(Constant.CRYPTO_NUM_TOKEN_TIMEOUT));
            int sessionTimeOut = Integer.parseInt(config.getProperty(Constant.CRYPTO_SESSION_TIMEOUT));
          
            String proxyIp = config.getProperty(Constant.CRYPTO_PROXY_IP);
            int proxyPort = Integer.parseInt(config.getProperty(Constant.CRYPTO_PROX_PORT));

            int socksVersion = Integer.parseInt(config.getProperty(Constant.CRYPTO_SOCKS_VERSION));
            String socksUserName = config.getProperty(Constant.CRYPTO_SOCKS_USER_NAME);
            String socksPassword = config.getProperty(Constant.CRYPTO_SOCKS_PASSWORD);
           
            int logLevel = Integer.parseInt(config.getProperty(Constant.CRYPTO_LOG_LEVEL));
            String ipVersion = config.getProperty(Constant.CRYPTO_IP_VERSION);
            String jniClassPoolMapConfig = config.getProperty(Constant.JNI_POOL_MAP_CLEAR);
            String dbHost = config.getProperty(Constant.CRYPTO_DB_HOST);
            int dbPort = Integer.parseInt(config.getProperty(Constant.CRYPTO_DB_PORT));
            String dbName = config.getProperty(Constant.CRYPTO_DB_NAME);
            String dbUser = config.getProperty(Constant.CRYPTO_DB_USER);
            String dbPass = config.getProperty(Constant.CRYPTO_DB_PASS);
            Environment environment = null;
            try {
                 environment = Environment.valueOf(config.getProperty(Constant.ENVIRONMENT));
            } catch (Exception e){
                environment = Environment.DEVELOPMENT;
            }
            boolean updateEn = Boolean.parseBoolean(config.getProperty(Constant.UPDATE_ENABLE));
            int maxPoolSize = Integer.parseInt(config.getProperty(Constant.ORM_MAX_POOL_SIZE));
            int minPoolSize = Integer.parseInt(config.getProperty(Constant.ORM_MIN_POOL_SIZE));
            int cleanupThreadInterval = Integer.parseInt(config.getProperty(Constant.ORM_CLEANUP_INTERVAL_SECONDS));
            int dbConnectionTTL = Integer.parseInt(config.getProperty(Constant.ORM_TTL_SECONDS));
            int keepAliveInterval = Integer.parseInt(config.getProperty(Constant.ORM_KEEPALIVE_INTERVAL_SECONDS));
            int ci0ESCAuditInterval = Integer.parseInt(config.getProperty(Constant.CI0_AUDIT_INTERVAL_SECONDS));
            int maxDBRetries = Integer.parseInt(config.getProperty(Constant.ORM_RETRIES));
            int escCacheSize =Integer.parseInt(config.getProperty(Constant.ESC_CACHE_SIZE));
            jniClassPoolMapConfig = jniClassPoolMapConfig != null ? jniClassPoolMapConfig : "ALL";
            IAMExtensionV2.Builder builder = new IAMExtensionV2.Builder();
            builder.hostURL(hostUrl);
            builder.hostName(hostName);
            builder.pinningHash(pinningHash);
            builder.enterpriseAccountId(enterpriseAccountId);
            builder.enterpriseName(enterpriseName);
            builder.escFolderPath(escFolderPath);
            builder.logFolderPath(escFolderPath);
            builder.cryptoServerIP(cryptoServerIp);
            builder.cryptoServerPort(Integer.parseInt(cryptoServerPort));
            builder.tlsEnabled(tlsEnabled);

            builder.retries(retries);
            builder.qrTimeout(qrTimeout);
            builder.numtokentimeout(numTokenTimeOut);
            builder.proxyIP(proxyIp);
            builder.proxyPort(proxyPort);
            builder.socksVersion(socksVersion);
            builder.socksUsername(socksUserName);
            builder.socksPassword(socksPassword);
            builder.logLevel(logLevel);
            builder.ipVersion(ipVersion);
            builder.timeout(sessionTimeOut);
            builder.setJniClassPoolMapConfig(jniClassPoolMapConfig);
            builder.dbHost(dbHost);
            builder.dbPort(dbPort);
            builder.dbName(dbName);
            builder.dbUser(dbUser);
            builder.dbPass(dbPass);
            builder.shouldInitializeJNIClass(shouldInitializeJNIClass);
            builder.updateEn(updateEn);
            builder.maxPoolSize(maxPoolSize);
            builder.minPoolSize(minPoolSize);
            builder.cleanupThreadInterval(cleanupThreadInterval);
            builder.dbConnectionTTL(dbConnectionTTL);
            builder.keepAliveInterval(keepAliveInterval);
            builder.ci0ESCAuditInterval(ci0ESCAuditInterval);
            builder.maxDBRetries(maxDBRetries);
            builder.escCacheSize(escCacheSize);
            builder.environment(environment);
            iamExtensionV2 = builder.build();
            if (enterpriseAccountId != null) {
                TokenStore.getInstance().addIAMExtensionV2(enterpriseAccountId+" "+shouldInitializeJNIClass, iamExtensionV2);
            }
            return iamExtensionV2;
        }
        catch (Exception e) {
            logger.log(Level.FATAL, e);
            throw new IAMException(e, IAMConstants.ERROR_CODE_INCOMPLETE_DATA, e.getMessage());
        }finally {
            logger.log(Level.DEBUG, IAM_UTIL_LOG+" generateIAMExtensionV2 : end");  
        }
        
    }
    
    public IAMExtensionV2 generateIAMExtensionV2(String enterpriseAccountId, String escPath) throws IAMException {
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" generateIAMExtensionV2 : start");
        String hostUrl = Config.getInstance().getProperty(Constant.IAM_SERVICE_URL);
        String hostName = Config.getInstance().getProperty(Constant.IAM_PINNING_HOST_NAME);
        String pinningHash = Config.getInstance().getProperty(Constant.IAM_PINNING_HASH);
        String escFolderPath = escPath;
        String cryptoServerIp = Config.getInstance().getProperty(Constant.CRYPTO_SERVER_URL);
        String cryptoServerPort = Config.getInstance().getProperty(Constant.CRYPTO_SERVER_PORT);
        String enterpriseName = Config.getInstance().getProperty(Constant.ENTERPRISE_NAME);
        String jniClassPoolMapConfig = Config.getInstance().getProperty(Constant.JNI_POOL_MAP_CLEAR);
        jniClassPoolMapConfig = jniClassPoolMapConfig != null ? jniClassPoolMapConfig : "ALL";
        IAMExtensionV2.Builder builder = new IAMExtensionV2.Builder();
        builder.hostURL(hostUrl);
        builder.hostName(hostName);
        builder.pinningHash(pinningHash);
        builder.enterpriseAccountId(enterpriseAccountId);
        builder.enterpriseName(enterpriseName);
        builder.escFolderPath(escFolderPath);
        builder.logFolderPath(escFolderPath);
        builder.retries(20);
        builder.timeout(30);
        builder.cryptoServerIP(cryptoServerIp);
        builder.cryptoServerPort(Integer.parseInt(cryptoServerPort));
        builder.setJniClassPoolMapConfig(jniClassPoolMapConfig);
        IAMExtensionV2 iamExtensionV2 = builder.build();
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" generateIAMExtensionV2 : end");
        return iamExtensionV2;
    }
    
    public Token authenticateV2(IAMExtensionV2 iamExtension, String applicationId, String applicationPassword) throws IAMException {
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" authenticateV2 : start");
        Token token = TokenStore.getInstance().getTokenV2(applicationId);
        int tokenExpiryBufferInMin = Integer.parseInt(Config.getInstance().getProperty(Constant.TOKEN_EXPIRY_BUFFER_IN_MIN));
        logger.log(Level.INFO, "tokenExpiryBufferInMin : "+tokenExpiryBufferInMin);
        if(token != null) {
            logger.log(Level.INFO, "Exp Time : "+token.getExpiryTime().getTime());
            logger.log(Level.INFO, " Expiry time : "+(token.getExpiryTime().getTime() - (tokenExpiryBufferInMin * 60 * 1000)));
            logger.log(Level.INFO, "Ids Exp Time : "+token.getIdsExpiryTime().getTime());
            logger.log(Level.INFO, "Ids Expiry time : "+(token.getIdsExpiryTime().getTime() - (tokenExpiryBufferInMin * 60 * 1000)));
        }
        logger.log(Level.INFO, " Current Time : "+System.currentTimeMillis());
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        if (token == null || (System.currentTimeMillis() >= (token.getExpiryTime().getTime() - (tokenExpiryBufferInMin * 60 * 1000)))
                 ||(System.currentTimeMillis() >= (token.getIdsExpiryTime().getTime() - (tokenExpiryBufferInMin * 60 * 1000)))) {
            logger.log(Level.INFO, "Token expired");
            System.out.println("AuthenticateV2 "+applicationId+" pass"+applicationPassword);
            token = iamExtension.authenticate(applicationId, applicationPassword, reqRefNum);
            if (token != null) {
                logger.log(Level.INFO, "Token created : "+token);
                TokenStore.getInstance().addTokenV2(applicationId, token);
            }
        }else if(!iamExtension.checkApplicationInJnimap(applicationId)){
            token = iamExtension.authenticate(applicationId, applicationPassword, reqRefNum);
            if (token != null) {
                logger.log(Level.INFO, "Token created : "+token);
                TokenStore.getInstance().addTokenV2(applicationId, token);
            } 
        }
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" authenticateV2 : end");
        return token;
    }

    public Token authenticateV2WithoutCrypto(IAMExtensionV2 iamExtension, String applicationId, String applicationPassword) throws IAMException {
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" authenticateV2 : start");
        Token token = TokenStore.getInstance().getTokenV2(applicationId+"wc");
        int tokenExpiryBufferInMin = Integer.parseInt(Config.getInstance().getProperty(Constant.TOKEN_EXPIRY_BUFFER_IN_MIN));
        logger.log(Level.INFO, "tokenExpiryBufferInMin : "+tokenExpiryBufferInMin);
        if(token != null) {
            logger.log(Level.INFO, "Exp Time : "+token.getExpiryTime().getTime());
            logger.log(Level.INFO, " Expiry time : "+(token.getExpiryTime().getTime() - (tokenExpiryBufferInMin * 60 * 1000)));
            logger.log(Level.INFO, "Ids Exp Time : "+token.getIdsExpiryTime().getTime());
            logger.log(Level.INFO, "Ids Expiry time : "+(token.getIdsExpiryTime().getTime() - (tokenExpiryBufferInMin * 60 * 1000)));
        }
        logger.log(Level.INFO, " Current Time : "+System.currentTimeMillis());
        String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
        if (token == null || (System.currentTimeMillis() >= (token.getExpiryTime().getTime() - (tokenExpiryBufferInMin * 60 * 1000)))
                ||(System.currentTimeMillis() >= (token.getIdsExpiryTime().getTime() - (tokenExpiryBufferInMin * 60 * 1000)))) {
            logger.log(Level.INFO, "Token expired");
            System.out.println("AuthenticateV2 "+applicationId+" pass"+applicationPassword);
            token = iamExtension.authenticate(applicationId, applicationPassword, reqRefNum);
            if (token != null) {
                logger.log(Level.INFO, "Token created : "+token);
                TokenStore.getInstance().addTokenV2(applicationId+"wc", token);
            }
        }
        logger.log(Level.DEBUG, IAM_UTIL_LOG+" authenticateV2 : end");
        return token;
    }

    public void onboardUserApi(Application application, String service, String finalSearchAttributeValue, AdfsDetailsTO finalAdfsDetailsTO) {
        try{
            UserIciciTO userTO = new UserIciciTO();
            List<AttributeDataTO> searchAttributeList = new ArrayList<>();
            if(finalSearchAttributeValue!=null){
                AttributeDataTO attributeDataTO = new AttributeDataTO();
                attributeDataTO.setAttributeName(Constant.USER_ID);
                attributeDataTO.setAttributeValue(finalSearchAttributeValue);
                searchAttributeList.add(attributeDataTO);
            }
            if(finalAdfsDetailsTO.getMobile()!=null){
                AttributeDataTO attributeDataTO = new AttributeDataTO();
                attributeDataTO.setAttributeName(Constant.MOBILE_NO);
                attributeDataTO.setAttributeValue(finalAdfsDetailsTO.getMobile());
                searchAttributeList.add(attributeDataTO);
            }
            userTO.setSearchAttributes(searchAttributeList);

            List<AttributeDataTO> attributeDataList = new ArrayList<>();
            if(finalAdfsDetailsTO.getEmail()!=null){
                AttributeDataTO attributeDataTO = new AttributeDataTO();
                attributeDataTO.setAttributeName(Constant.EMAIL_ID);
                attributeDataTO.setAttributeValue(finalAdfsDetailsTO.getEmail());
                attributeDataList.add(attributeDataTO);
            }
            userTO.setAttributeData(attributeDataList);

            List<ApplicationTO> applicationTOList = new ArrayList<>();
            ApplicationTO applicationTO = new ApplicationTO();
            applicationTO.setApplicationId(application.getApplicationId());
            List<ServiceTO> serviceTOS = new ArrayList<>();
            ServiceTO serviceTO = new ServiceTO();
            serviceTO.setServiceName(service);
            serviceTOS.add(serviceTO);
            applicationTO.setServices(serviceTOS);
            applicationTOList.add(applicationTO);
            userTO.setSubscribedApplications(applicationTOList);
            boolean fipsEnabled = externalConfig.getProperty(Constant.FALLOUT_FIPS_ENABLED,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT) != null && Boolean.parseBoolean(externalConfig.getProperty(Constant.FALLOUT_FIPS_ENABLED,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT));
            if(fipsEnabled){
                onboardUserUrlConnection(userTO,application);
            }
            else{
                onboardUserHttpClient(userTO,application);
            }

        }
        catch(Exception e){
            logger.log(Level.ERROR,e.getMessage(),e);
        }
    }

    private void onboardUserUrlConnection(UserIciciTO userTO, Application application) {
        String status="";
        String result="";
        try {
            Gson gson = new Gson();
            URL url = new URL(config.getProperty(Constant.ONBOARD_USER_URL));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");

            connection.setRequestProperty("Application-Id", application.getApplicationId());
            connection.setRequestProperty("Application-Secret", generateApplicationSecret(application));
            connection.setRequestProperty(config.getProperty(Constant.APIGEE_XAPI_KEY_HEADER_NAME), config.getProperty(Constant.APIGEE_XAPI_KEY_HEADER_VALUE));
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Cookie", "d082c79a852a448ed27d7f7a6862c998=c5a42edcd39717f7ea16174e88f27d04");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            String jsonInputString = gson.toJson(userTO);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            UserIciciTO user;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    logger.log(Level.DEBUG,"Response: " + response.toString());
                    user=new Gson().fromJson(response.toString(),UserIciciTO.class);
                    if(user.getStatus().equals("SUCCESS")){
                        logger.log(Level.DEBUG,IAM_UTIL_LOG+ "User onboarded sucessfully");
                    }
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    logger.log(Level.ERROR,IAM_UTIL_LOG + errorResponse.toString());
                    user=new Gson().fromJson(errorResponse.toString(),UserIciciTO.class);
                    if(user.getStatus().equals("FAILED")){
                        logger.log(Level.ERROR,errorResponse);
                    }
                }
            }

        } catch (Exception e) {
            logger.log(Level.ERROR,IAM_UTIL_LOG+e.getMessage(),e);
        }
    }

    private void onboardUserHttpClient(UserIciciTO userTO, Application application) throws Exception{

        String jsonPayload = StringUtil.toJson(userTO);

        CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(getSSLContext()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        String apiUrl = config.getProperty(Constant.ONBOARD_USER_URL);
        StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setHeader("Application-Id", application.getApplicationId());
        httpPost.setHeader("Application-Secret", generateApplicationSecret(application));
        httpPost.setHeader(config.getProperty(Constant.APIGEE_XAPI_KEY_HEADER_NAME), config.getProperty(Constant.APIGEE_XAPI_KEY_HEADER_VALUE));
        httpPost.setEntity(entity);
        UserIciciTO user=new UserIciciTO();
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity responseEntity = httpResponse.getEntity();
                String data = EntityUtils.toString(responseEntity);
                user=new Gson().fromJson(data,UserIciciTO.class);
                if(user.getStatus().equals("SUCCESS")){
                    logger.log(Level.DEBUG,IAM_UTIL_LOG+ "User onboarded sucessfully");
                }
                else{
                    logger.log(Level.DEBUG,IAM_UTIL_LOG+ user.getErrorMessage());
                }
            } else {
                HttpEntity responseEntity = httpResponse.getEntity();
                if(responseEntity != null){
                    String data = EntityUtils.toString(responseEntity);
                    logger.log(Level.ERROR,data);
                }
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

    private SSLContext getSSLContext() {
        try {
            SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build();
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateApplicationSecret(Application application){
        String applicationSecretePlainText = AES128Impl.decryptData(application.getApplicationSecret(), KeyManagementUtil.getAESKey());
        if(application.getIsPlaintextPasswordAllowed()){
            return applicationSecretePlainText;
        }
        String randomSalt = RandomString.nextString(20);
        applicationSecretePlainText = applicationSecretePlainText + randomSalt;
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new UndeclaredThrowableException(e);
        }
        String generatedHash = Base64.getEncoder().encodeToString(messageDigest.digest(applicationSecretePlainText.getBytes()));
        String runningHash = randomSalt + generatedHash;
        return runningHash;
    }

}
