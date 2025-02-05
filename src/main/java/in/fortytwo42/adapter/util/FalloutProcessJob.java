
package in.fortytwo42.adapter.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
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
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.FalloutSyncDataDaoImpl;
import in.fortytwo42.daos.dao.FalloutSyncDataDaoIntf;
import in.fortytwo42.daos.exception.FalloutSyncDataNotFound;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.Fallout;
import in.fortytwo42.entities.bean.FalloutSyncData;
import in.fortytwo42.tos.enums.Status;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;
import in.fortytwo42.tos.transferobj.FalloutTO;
import in.fortytwo42.tos.transferobj.VTTokenResponseTO;
import in.fortytwo42.tos.transferobj.VTTokenTO;

// TODO: Auto-generated Javadoc

/**
 * The Class UserAuthPrincipalJob.
 */
public class FalloutProcessJob implements Job {

    /**
     * The user auth principal job.
     */
    private static final String FALLOUT_PROCESS_JOB = "<<<<< FalloutProcessJob";

    //SSLContext sslContext = getSSLContext();

    private static Logger logger= LogManager.getLogger(FalloutProcessJob.class);
    private final Config config = Config.getInstance();
    private final ExternalConfigUtil externalConfig=ExternalConfigUtil.getInstance();
    private final SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        requestReferenceNumber = requestReferenceNumber != null ? requestReferenceNumber : UUID.randomUUID().toString();
        ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
        logger.log(Level.INFO, FALLOUT_PROCESS_JOB + " execute : start");
        FalloutConfigTO falloutConfigTO = getFalloutConfig();
        boolean isDataSyncEnabled;
        boolean isDataProcessingEnabled;
        Integer numberOfRecordsToBeProcessed;
        Long dataFetchFrequency = null;
        String schedulerFrequency = null;
        if (falloutConfigTO != null) {
            isDataSyncEnabled = falloutConfigTO.getDehFalloutDataSync();
            isDataProcessingEnabled = falloutConfigTO.getDehFalloutDataProcess();
            numberOfRecordsToBeProcessed = falloutConfigTO.getNumberOfRecordsToBeProcessed();
            if (falloutConfigTO.getDataFetchFrequency() != null && falloutConfigTO.getDataFetchFrequency() > 0) {
                dataFetchFrequency = falloutConfigTO.getDataFetchFrequency();
            }
            if (falloutConfigTO.getSchedulerFrequency() != null && !falloutConfigTO.getSchedulerFrequency().isEmpty()) {
                schedulerFrequency = falloutConfigTO.getSchedulerFrequency();
            }
        } else {
            isDataSyncEnabled = Boolean.parseBoolean(Config.getInstance().getProperty(Constant.FALLOUT_PROCESS_DATA_SYNC));
            isDataProcessingEnabled = Boolean.parseBoolean(Config.getInstance().getProperty(Constant.FALLOUT_PROCESS_DATA_PROCESSING));
            numberOfRecordsToBeProcessed = 500;
        }
        boolean isFalloutEnabled = schedulerFrequency != null ? isFalloutEnabled(schedulerFrequency) : true;
        if (isDataSyncEnabled && isFalloutEnabled) {
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " Data Sync : start");
            Session session = null;
            try {
                String ft42Status = externalConfig.getProperty(Constant.FALLOUT_PROCESS_FT42_STATUS,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
                String[] ft42StatusArr = ft42Status.split(",");
                for (String status : ft42StatusArr) {
                    String startDateTime = startTime();
                    String endDateTime = dataFetchFrequency != null ? endTime(startDateTime, dataFetchFrequency) : endTime();
                    boolean ifFipsEnabled = externalConfig.getProperty(Constant.FALLOUT_FIPS_ENABLED, Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT) != null && Boolean.parseBoolean(externalConfig.getProperty(Constant.FALLOUT_FIPS_ENABLED, Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT));
                    List<FalloutTO> falloutTOs = ifFipsEnabled ? executeFalloutApiURLConnection(requestReferenceNumber, startDateTime, endDateTime, status) : executeFalloutApi(requestReferenceNumber, startDateTime, endDateTime, status);
                    session = sessionFactoryUtil.openSessionWithoutTransaction();
                    Transaction transaction = session.beginTransaction();
                    if (falloutTOs != null && !falloutTOs.isEmpty()) {
                        for (FalloutTO falloutTO : falloutTOs) {
                            falloutTO.setRequestReferenceNumber(requestReferenceNumber);
                            addFalloutRecord(falloutTO, session);
                        }
                    } else {
                        logger.log(Level.INFO, FALLOUT_PROCESS_JOB + " execute : fallout data not found");
                    }
                    updateFalloutLastSyncTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(endDateTime).getTime(), session);
                    transaction.commit();
                }
            } catch (Exception e) {
                if (session != null && session.isOpen() && session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
                logger.log(Level.ERROR, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
                logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " Data Sync : end");
            }
        }

        if (isDataProcessingEnabled && isFalloutEnabled) {
            FacadeFactory.getFalloutFacade().processFalloutRecord(numberOfRecordsToBeProcessed);
        }

        logger.log(Level.INFO, FALLOUT_PROCESS_JOB + " execute : end");
    }

    private FalloutConfigTO getFalloutConfig() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " startTime : start");
        List<FalloutConfigTO> falloutConfigTOs = ServiceFactory.getFalloutConfigService().getConfigs();
        FalloutConfigTO falloutConfigTO = null;
        if (falloutConfigTOs != null && !falloutConfigTOs.isEmpty()) {
            falloutConfigTO = falloutConfigTOs.get(0);
        }
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " startTime : end");
        return falloutConfigTO;
    }

    public String startTime() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " startTime : start");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String formattedStartDateTime = null;
        Session session = SessionFactoryUtil.getInstance().getSession();
        try {
            FalloutSyncData falloutSyncData = FalloutSyncDataDaoImpl.getInstance().getFalloutSyncRecord(1l, session);
            if (falloutSyncData == null || falloutSyncData.getLastSyncTime() == null) {
                logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " FalloutSyncData record not found ");
                throw new Exception();
            }
            long starDateTime = falloutSyncData.getLastSyncTime().getTime();
            Date startTime = new Date(starDateTime);
            formattedStartDateTime = sdf.format(startTime);
            logger.log(Level.DEBUG, "startDateTime: " + formattedStartDateTime);
        } catch (FalloutSyncDataNotFound e) {
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " FalloutSyncData record not found ");
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " startTime : end");
            if (session.isOpen()) {
                session.close();
            }
        }
        return formattedStartDateTime;
    }

    public String endTime() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " endTime : start");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String formattedEndDateTime = null;
        try {
            Date todayDate = new Date();
            formattedEndDateTime = sdf.format(todayDate);
            logger.log(Level.DEBUG, "endDateTime: " + formattedEndDateTime);

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " endTime : end");
        }
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " endTime : end");
        return formattedEndDateTime;
    }

    private String endTime(String startDateTime, Long dataFetchFrequency) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String formattedEndDateTime = null;
        try {
            Long startTime = sdf.parse(startDateTime).getTime();
            long endMillis = startTime + dataFetchFrequency;
            Date endDate = new Date(endMillis);
            formattedEndDateTime = sdf.format(endDate);
            logger.log(Level.DEBUG, "endDateTime: " + formattedEndDateTime);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " endTime : end");
        }
        return formattedEndDateTime;
    }

    private SSLContext getSSLContext() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getSSLContext : start");
        try {
            SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build();
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getSSLContext : end");
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getSSLContext : end");
            throw new RuntimeException(e);
        }
    }

    private String executeVTAuthTokenApi() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApi : start");
        CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(getSSLContext()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        String apiUrl = externalConfig.getProperty(Constant.FALLOUT_PROCESS_VT_TOKEN_URL,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        String vtTokenPayload = getVTTokenPayload();
        String apiKey = externalConfig.getProperty(Constant.FALLOUT_PROCESS_API_KEY,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setHeader("apikey", apiKey);
        httpPost.setHeader("Content-Type", "application/json");
        StringEntity entity = new StringEntity(vtTokenPayload, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            String accessToken = null;
            HttpEntity responseEntity = response.getEntity();
            String data = EntityUtils.toString(responseEntity);
            if (response.getStatusLine().getStatusCode() == 200) {
                VTTokenResponseTO vtTokenResponseTO = StringUtil.fromJson(data, VTTokenResponseTO.class);
                accessToken = vtTokenResponseTO.getAccess_token();
                logger.log(Level.DEBUG, "VT access token : " + accessToken);
                logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApi : end");
            } else {
                logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " VT token status code : " + response.getStatusLine().getStatusCode());
                logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " VT token error : " + data);
            }
            return accessToken;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApi : end");
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException ex) {
                    logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApi : Failed to close http connection");
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private String executeVTAuthTokenApiURLConnection() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApiURLConnection: start");
        try {
            String apiUrl = externalConfig.getProperty(Constant.FALLOUT_PROCESS_VT_TOKEN_URL,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApiURLConnection - apiUrl : " + apiUrl);
            String vtTokenPayload = getVTTokenPayload();
            String apiKey = externalConfig.getProperty(Constant.FALLOUT_PROCESS_API_KEY,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
            URL url = new URL(apiUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("apikey", apiKey);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoOutput(true);
            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = vtTokenPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = httpURLConnection.getResponseCode();
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApiURLConnection - status code : " + responseCode);
            String accessToken = null;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    String data = response.toString();
                    logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApiURLConnection - data : " + data);
                    VTTokenResponseTO vtTokenResponseTO = StringUtil.fromJson(data, VTTokenResponseTO.class);
                    accessToken = vtTokenResponseTO.getAccess_token();
                    logger.log(Level.DEBUG, "VT access token: " + accessToken);
                }
            } else {
                logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApiURLConnection - error status code : " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorResponseLine = null;
                    while ((errorResponseLine = br.readLine()) != null) {
                        errorResponse.append(errorResponseLine.trim());
                    }
                    logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApiURLConnection - error data : " + errorResponse.toString());
                }
            }
            return accessToken;
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new RuntimeException(e);
        }finally {
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeVTAuthTokenApiURLConnection: end");
        }
    }




    private String getVTTokenPayload() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getVTTokenPayload : start");
        String grantType = externalConfig.getProperty(Constant.FALLOUT_PROCESS_GRANT_TYPE,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        String clientId = externalConfig.getProperty(Constant.FALLOUT_PROCESS_CLIENT_ID,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        String clientSecret = externalConfig.getProperty(Constant.FALLOUT_PROCESS_CLIENT_SECRET,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        String bankId = externalConfig.getProperty(Constant.FALLOUT_PROCESS_BANK_ID,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        String username = externalConfig.getProperty(Constant.FALLOUT_PROCESS_USERNAME,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        String password = externalConfig.getProperty(Constant.FALLOUT_PROCESS_PASSWORD,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        String languageId = externalConfig.getProperty(Constant.FALLOUT_PROCESS_LANGUAGE_ID,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        String channelId = externalConfig.getProperty(Constant.FALLOUT_PROCESS_CHANNEL_ID,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        VTTokenTO vtTokenTO = new VTTokenTO();
        vtTokenTO.setGrant_type(grantType);
        vtTokenTO.setClient_id(clientId);
        vtTokenTO.setClient_secret(clientSecret);
        vtTokenTO.setBank_id(bankId);
        vtTokenTO.setUsername(username);
        vtTokenTO.setPassword(password);
        vtTokenTO.setLanguage_id(languageId);
        vtTokenTO.setChannel_id(channelId);
        String vtTokenPayload = StringUtil.toJson(vtTokenTO);
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getVTTokenPayload - payload : " + vtTokenPayload);
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getVTTokenPayload : end");
        return vtTokenPayload;
    }


    private List<FalloutTO> executeFalloutApi(String requestReferenceNumber, String startDateTime, String endDateTime, String ft42Status) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeFalloutApi : start");
//        String dehStatus = externalConfig.getProperty(Constant.FALLOUT_PROCESS_DEH_STATUS,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
//        String ft42Status = externalConfig.getProperty(Constant.FALLOUT_PROCESS_FT42_STATUS,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        FalloutTO falloutTO = new FalloutTO();
        falloutTO.setStartDateTime(startDateTime);
        falloutTO.setEndDateTime(endDateTime);
//        falloutTO.setDehStatus(dehStatus);
        falloutTO.setFt42Status(ft42Status);
        String jsonPayload = StringUtil.toJson(falloutTO);
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + "executeFalloutApi - fallout payload : " + jsonPayload);

        String accessToken = executeVTAuthTokenApi();
        CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(getSSLContext()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        String apiUrl = externalConfig.getProperty(Constant.FALLOUT_PROCESS_DEH_URL,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApi - apiUrl : " + apiUrl);
        StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
        String apiKey = externalConfig.getProperty(Constant.FALLOUT_PROCESS_API_KEY,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setHeader("apikey", apiKey);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", accessToken);
        httpPost.setEntity(entity);
        List<FalloutTO> falloutList = new ArrayList<>();
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - status code : " + httpResponse.getStatusLine().getStatusCode());
            String dehResponseHeaderForNoRecords = externalConfig.getProperty(Constant.FALLOUT_PROCESS_NO_RECORDS_HEADER_NAME,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT) != null ?
                    externalConfig.getProperty(Constant.FALLOUT_PROCESS_NO_RECORDS_HEADER_NAME,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT) : "channelcontext";
            Header[] headers = httpResponse.getHeaders(dehResponseHeaderForNoRecords);
            String channelcontextHeader = null;
            if (headers.length > 0) {
                channelcontextHeader = headers[0].getValue();
            }
            channelcontextHeader = channelcontextHeader != null ? channelcontextHeader : "";
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - channelcontextHeader : " + channelcontextHeader);
            if (httpResponse.getStatusLine().getStatusCode() == 201) {
                HttpEntity responseEntity = httpResponse.getEntity();
                String data = EntityUtils.toString(responseEntity);
                falloutList = new Gson().fromJson(data, new TypeToken<List<FalloutTO>>() {
                }.getType());
            } else {
                logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApi - error status code : " + httpResponse.getStatusLine().getStatusCode());
                logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApi - error channelcontextHeader : " + channelcontextHeader);
                HttpEntity responseEntity = httpResponse.getEntity();
                if(responseEntity != null){
                    String data = EntityUtils.toString(responseEntity);
                    logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApi - error data : " + data);
                }
            }
            return falloutList;
        } catch (Exception e) {
            logger.log(Level.DEBUG, e.getMessage(), e);
            throw new Exception("Error in Fallout API");
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeFalloutApi : end");
        }
    }

    private List<FalloutTO> executeFalloutApiURLConnection(String requestReferenceNumber, String startDateTime, String endDateTime, String ft42Status) throws Exception {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection : start");
        try {
//            String dehStatus = externalConfig.getProperty(Constant.FALLOUT_PROCESS_DEH_STATUS,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
//            String ft42Status = externalConfig.getProperty(Constant.FALLOUT_PROCESS_FT42_STATUS,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
            FalloutTO falloutTO = new FalloutTO();
            falloutTO.setStartDateTime(startDateTime);
            falloutTO.setEndDateTime(endDateTime);
//            falloutTO.setDehStatus(dehStatus);
            falloutTO.setFt42Status(ft42Status);
            String jsonPayload = StringUtil.toJson(falloutTO);
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + "executeFalloutApiURLConnection - fallout payload : " + jsonPayload);
            String accessToken = executeVTAuthTokenApiURLConnection();
            String apiUrl = externalConfig.getProperty(Constant.FALLOUT_PROCESS_DEH_URL,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - apiUrl : " + apiUrl);
            String apiKey = externalConfig.getProperty(Constant.FALLOUT_PROCESS_API_KEY,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
            URL url = new URL(apiUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("apikey", apiKey);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Authorization", accessToken);
            httpURLConnection.setDoOutput(true);
            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            List<FalloutTO> falloutList = new ArrayList<>();
            int responseCode = httpURLConnection.getResponseCode();
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - status code : " + responseCode);
            String dehResponseHeaderForNoRecords = externalConfig.getProperty(Constant.FALLOUT_PROCESS_NO_RECORDS_HEADER_NAME,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT) != null ?
                    externalConfig.getProperty(Constant.FALLOUT_PROCESS_NO_RECORDS_HEADER_NAME,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT) : "channelcontext";
            String channelcontextHeader = httpURLConnection.getHeaderField(dehResponseHeaderForNoRecords);
            channelcontextHeader = channelcontextHeader != null ? channelcontextHeader : "";
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - channelcontextHeader : " + channelcontextHeader);
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    String data = response.toString();
                    logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - data  : " + data);
                    falloutList = new Gson().fromJson(data, new TypeToken<List<FalloutTO>>() {
                    }.getType());
                }
            } else {
                logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - error status code : " + responseCode);
                logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - error channelcontextHeader : " + channelcontextHeader);
                InputStream inputStream = httpURLConnection.getErrorStream();
                if(inputStream != null){
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String errorResponseLine = null;
                        while ((errorResponseLine = br.readLine()) != null) {
                            errorResponse.append(errorResponseLine.trim());
                        }
                        logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection - error data : " + errorResponse.toString());
                    } catch (Exception e){
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
            }
            return falloutList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " executeFalloutApiURLConnection : end");
        }
    }

    public void processFallout(String startDateTime, String endDateTime, Session session) {
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        requestReferenceNumber = requestReferenceNumber != null ? requestReferenceNumber : UUID.randomUUID().toString();
        ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " processFallout : start");
        FalloutConfigTO falloutConfigTO = getFalloutConfig();
        boolean isDataSyncEnabled;
        boolean isDataProcessingEnabled;
        Integer numberOfRecordsToBeProcessed;
        Long dataFetchFrequency = null;
        String schedulerFrequency = null;
        if (falloutConfigTO != null) {
            isDataSyncEnabled = falloutConfigTO.getDehFalloutDataSync();
            isDataProcessingEnabled = falloutConfigTO.getDehFalloutDataProcess();
            numberOfRecordsToBeProcessed = falloutConfigTO.getNumberOfRecordsToBeProcessed();
            if (falloutConfigTO.getDataFetchFrequency() != null && falloutConfigTO.getDataFetchFrequency() > 0) {
                dataFetchFrequency = falloutConfigTO.getDataFetchFrequency();
            }
            if (falloutConfigTO.getSchedulerFrequency() != null && !falloutConfigTO.getSchedulerFrequency().isEmpty()) {
                schedulerFrequency = falloutConfigTO.getSchedulerFrequency();
            }
        } else {
            isDataSyncEnabled = Boolean.parseBoolean(Config.getInstance().getProperty(Constant.FALLOUT_PROCESS_DATA_SYNC));
            isDataProcessingEnabled = Boolean.parseBoolean(Config.getInstance().getProperty(Constant.FALLOUT_PROCESS_DATA_PROCESSING));
            numberOfRecordsToBeProcessed = 500;
        }
        boolean isFalloutEnabled = schedulerFrequency != null ? isFalloutEnabled(schedulerFrequency) : true;
        if (isDataSyncEnabled && isFalloutEnabled) {
            try {
                String ft42Status = externalConfig.getProperty(Constant.FALLOUT_PROCESS_FT42_STATUS, Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT);
                String[] ft42StatusArr = ft42Status.split(",");
                for (String status : ft42StatusArr) {
                    startDateTime = startDateTime != null ? startDateTime : startTime();
                    logger.log(Level.DEBUG, "startDateTime : " + startDateTime);
                    endDateTime = dataFetchFrequency != null ? endTime(startDateTime, dataFetchFrequency) : endTime();
                    boolean ifFipsEnabled = externalConfig.getProperty(Constant.FALLOUT_FIPS_ENABLED, Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT) != null && Boolean.parseBoolean(externalConfig.getProperty(Constant.FALLOUT_FIPS_ENABLED, Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT));
                    List<FalloutTO> falloutTOs = ifFipsEnabled ? executeFalloutApiURLConnection(requestReferenceNumber, startDateTime, endDateTime, status) : executeFalloutApi(requestReferenceNumber, startDateTime, endDateTime, status);
               /* if (ifFipsEnabled) {
                    falloutTOs = executeFalloutApiURLConnection(requestReferenceNumber, startDateTime, endDateTime);
                } else
                    falloutTOs = executeFalloutApi(requestReferenceNumber, startDateTime, endDateTime);
                }*/
                    session.beginTransaction();
                    if (falloutTOs != null && !falloutTOs.isEmpty()) {
                        for (FalloutTO falloutTO : falloutTOs) {
                            falloutTO.setRequestReferenceNumber(requestReferenceNumber);
                            addFalloutRecord(falloutTO, session);
                        }
                    } else {
                        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " execute : fallout data not found");
                    }
                    updateFalloutLastSyncTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(endDateTime).getTime(), session);
                    session.getTransaction().commit();
                }
            } catch (Exception e) {
                if (session.isOpen() && session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
                logger.log(Level.ERROR, e.getMessage(), e);
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
                logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " processFallout : end");
            }
        }

        if (isDataProcessingEnabled && isFalloutEnabled) {
            FacadeFactory.getFalloutFacade().processFalloutRecord(numberOfRecordsToBeProcessed);
        }
    }

    private void updateFalloutLastSyncTime(Long endDateTime, Session session) {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " updateFalloutLastSyncTime : start");
        FalloutSyncDataDaoIntf falloutSyncDataDao = FalloutSyncDataDaoImpl.getInstance();
        try {
            FalloutSyncData falloutSyncData = falloutSyncDataDao.getFalloutSyncRecord(1l, session);
            falloutSyncData.setLastSyncTime(new Timestamp(endDateTime));
            falloutSyncDataDao.update(session, falloutSyncData);
        } catch (FalloutSyncDataNotFound e) {
            logger.log(Level.ERROR, FALLOUT_PROCESS_JOB + " FalloutSyncData record not found ");
        }
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " updateFalloutLastSyncTime : end");
    }

    public String getFalloutProcessFileName() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getFalloutProcessFileName : start");
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat(Constant.DATE_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone(Constant.TIMEZONE));
        String dateFormatted = formatter.format(date);
        String requestId = UUID.randomUUID().toString();
        String filename = "FalloutProcess" + Constant._UNDERSCORE + dateFormatted + Constant._UNDERSCORE + requestId + Constant.CSV_FILE_EXTENSION;
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getFalloutProcessFileName : end");
        return filename;
    }

    public void addFalloutRecord(FalloutTO falloutTO, Session session) throws ParseException {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " addFalloutRecord : start");
        Fallout fallout = new Fallout();
        String actualMobNo = falloutTO.getActualMobileNo();
        String newMobNo = falloutTO.getNewMobileNo();
        if (falloutTO.getActualMobileNo() != null && !falloutTO.getActualMobileNo().isEmpty()) {
            if (actualMobNo.contains(externalConfig.getProperty(Constant.FALLOUT_PROCESS_DEH_NO_MOBILE_NO,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT))) {
                falloutTO.setActualMobileNo(actualMobNo);
            }
            if (!actualMobNo.contains("+") && !actualMobNo.contains(externalConfig.getProperty(Constant.FALLOUT_PROCESS_DEH_NO_MOBILE_NO,Constant.EXTERNAL_CONFIG_TYPE_DEH_FALLOUT))) {
                String actualMobNoProperFormat = "+" + actualMobNo;
                falloutTO.setActualMobileNo(actualMobNoProperFormat);
            }
        }
        if (falloutTO.getNewMobileNo() != null && !falloutTO.getNewMobileNo().isEmpty()) {
            if (!falloutTO.getNewMobileNo().contains("+")) {
                String newMobNoProperFormat = "+" + newMobNo;
                falloutTO.setNewMobileNo(newMobNoProperFormat);
            }
        }
        if (falloutTO.getOldMobileNo() != null && !falloutTO.getOldMobileNo().isEmpty()) {
            String oldMobNo = falloutTO.getOldMobileNo();
            if (!falloutTO.getOldMobileNo().contains("+")) {
                String oldMobNoProperFormat = "+" + oldMobNo;
                falloutTO.setOldMobileNo(oldMobNoProperFormat);
            }
        }
        fallout.setData(falloutTO);
        fallout.setStatus(Status.PENDING);
        DaoFactory.getFalloutDao().create(session, fallout);
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " addFalloutRecord : end");
    }

    private boolean isFalloutEnabled(String schedulerFrequency) {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " isFalloutEnabled : start");
        boolean isFalloutEnabled = false;
        //IST Hrs : 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,90,20,21,22,23
        //UTC Hrs : 18,19,20,21,22,23,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17
        List<Integer> processingHrs = Arrays.stream(schedulerFrequency.split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        if (processingHrs.contains(getCurrentHour())) {
            isFalloutEnabled = true;
            logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " isFalloutEnabled : true for Hr : "+getCurrentHourInIST());
        }
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " isFalloutEnabled : end");
        return isFalloutEnabled;
    }

    private int getCurrentHour() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getCurrentHour : start");
        Calendar calendar = Calendar.getInstance();
        int hr = calendar.get(Calendar.HOUR_OF_DAY);
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getCurrentHour : end");
        return hr;
    }

    private static int getCurrentHourInIST(){
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getCurrentHourInIST : start");
        ZonedDateTime dateTimeIst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        int hourInIst = dateTimeIst.getHour();
        logger.log(Level.DEBUG, FALLOUT_PROCESS_JOB + " getCurrentHourInIST : end");
        return hourInIst;
    }

    public static void main(String[] args) {
        //String accessToken = new FalloutProcessJob().executeVTAuthTokenApi();
        //System.out.println("accessToken : " + accessToken);
    }

}
