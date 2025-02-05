package in.fortytwo42.adapter.util.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.fortytwo42.adapter.facade.ApplicationFacadeIntf;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CsvConstant;
import in.fortytwo42.adapter.util.DateUtil;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.ServiceTO;

public class OnboardApplicationCsv extends BaseCsv{

    private static final String ONBOARD_APPLICATION_CSV_LOG = "<<<<< OnboardApplicationCsv";

    private static Logger logger= LogManager.getLogger(OnboardApplicationCsv.class);

    private final ApplicationFacadeIntf applicationFacade = FacadeFactory.getApplicationFacade();

    private static final ObjectMapper objectMapper = new ObjectMapper();
    ErrorConstantsFromConfigIntf errorConstant = ServiceFactory.getErrorConstant();


    /**
     * Instantiates a new base csv.
     */


    protected OnboardApplicationCsv() {
        super(PermissionUtil.ONBOARD_APPLICATION);
    }

    private static final class InstanceHolder {
        private static final OnboardApplicationCsv INSTANCE = new OnboardApplicationCsv();

        private InstanceHolder() {
            super();
        }
    }

    public static OnboardApplicationCsv getInstance() {
        return OnboardApplicationCsv.InstanceHolder.INSTANCE;
    }

    @Override
    protected void parseCSVandUpdateData(String[] record, String accountId, Session session, String fileName) {
        Long makerId = Long.parseLong(record[record.length-1]);
        logger.log(Level.DEBUG, ONBOARD_APPLICATION_CSV_LOG + " parseCSVandUpdateData : start");
        String enterpriseId = record[0].trim();
        String applicationName = record[1].trim();
        String applicationSecret = record[2].trim();
        //boolean
        String twoFactorStatus = record[3].trim();
        String applicationType = record[4].trim();
        String transactionTimeout = record[5].trim();
        String resetPinSetting = record[6].trim();
        String applicationDescription = record[7].trim();
        //boolean
        String isAuthenticationRequired = record[8].trim();
        //boolean
        String applicationPassword = record[9].trim();
        //epoch
        String activationDate = record[10].trim();
        //epoch
        String expirationDate = record[11].trim();
        //boolean
        String isNotificationEnabled = record[12].trim();
        String isPlainTextAllowed = record[13].trim();
        String isCamEnabled = record[14].trim();
        String services = record[15].trim();

        String tokenTtl = record[16].trim();
        String maxOtpAttempts = record[17].trim();
        //json
        //boolean
        String callbackUrl = record[18].trim();
        String queueName = record[19].trim();
        String comments = record[20].trim();

        //validate records

        ApplicationTO applicationTO = new ApplicationTO();
//        List<ServiceTO> appServices = new ArrayList<>();

        //convert required fields to respective data type

//        try {
//            appServices = objectMapper.readValue(services, new TypeReference<List<ServiceTO>>(){});
//
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
        long currentSystemDate= System.currentTimeMillis();
        long startDate= Long.valueOf(activationDate);
        long endDate= Long.valueOf(expirationDate);
        String csvComments = null;


        String actualServices = services.replaceAll("\\[|\\]", "");
        String[] ser = actualServices.split(",");
        List<ServiceTO> serviceTOList = new ArrayList<>();
        for(String serviceName : ser){
            ServiceTO service = new ServiceTO();
            service.setServiceName(serviceName);
            serviceTOList.add(service);
        }
        applicationTO.setServices(serviceTOList);
        applicationTO.setEnterpriseId(enterpriseId);
//        applicationTO.setServices(appServices);
        applicationTO.setApplicationName(applicationName);
        applicationTO.setActor(accountId);
        applicationTO.setActivationDate(DateUtil.getEpochTimeFromStringDateLong(activationDate));
        applicationTO.setApplicationType(applicationType);
        applicationTO.setApplicationSecret(applicationSecret);
        applicationTO.setIsNotificationEnabled(Boolean.parseBoolean(isNotificationEnabled));
        applicationTO.setCamEnabled(Boolean.parseBoolean(isCamEnabled));
        applicationTO.setExpirationDate(DateUtil.getEpochTimeFromStringDateLong(expirationDate));
        applicationTO.setPlaintextPasswordAllowed(Boolean.parseBoolean(isPlainTextAllowed));
        applicationTO.setAuthenticationRequired(Boolean.parseBoolean(isAuthenticationRequired));
        applicationTO.setAttemptCount(Integer.parseInt(maxOtpAttempts));
        applicationTO.setCallbackUrl(callbackUrl);
        applicationTO.setResetPinUserUnblockSetting(resetPinSetting);
        applicationTO.setComments(comments);
        applicationTO.setDescription(applicationDescription);
        applicationTO.setPassword(applicationPassword);
        applicationTO.setQueueName(queueName);
        applicationTO.setTokenTtl(Integer.parseInt(tokenTtl));
        applicationTO.setTransactionTimeout(Integer.parseInt(transactionTimeout));
        applicationTO.setTwoFactorStatus(twoFactorStatus);
        applicationTO.setIsCredentialsEncrypted(false);
        try {
            if (startDate >= currentSystemDate) {
                applicationTO.setActivationDate(Long.parseLong(activationDate));
            } else {
                throw new Exception("invalid start Date ");
            }
            if (endDate > startDate && endDate > currentSystemDate) {
                applicationTO.setExpirationDate(Long.parseLong(expirationDate));
            } else {
                throw new Exception("invalid end Date ");
            }
        } catch (Exception e1) {
            logger.log(Level.ERROR, e1.getMessage(), e1);
            csvComments = e1.getMessage();
        }

         try {
             if(csvComments==null) {
                 applicationFacade.onboardApplication("MAKER", "maker1",makerId, applicationTO, false);
             }
        } catch (IllegalArgumentException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            csvComments = errorConstant.getERROR_MESSAGE_INVALID_VALUE();
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            csvComments = e.getMessage();
        }
        String status = null;
        if (csvComments == null) {
            status = Constant.SUCCESS_STATUS;
            csvComments = Constant.SUCCESS_COMMENT;
        }
        else {
            status = Constant.FAILURE_STATUS;
        }

        //String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        String[] updatedRecord = {enterpriseId,applicationName,applicationSecret,twoFactorStatus,applicationType,transactionTimeout,resetPinSetting,applicationDescription,isAuthenticationRequired, applicationPassword, activationDate,expirationDate,isNotificationEnabled,isPlainTextAllowed, isCamEnabled, services,tokenTtl,maxOtpAttempts, callbackUrl,queueName, csvComments, status, csvComments};
        writer.writeNext(updatedRecord);
        logger.log(Level.DEBUG, ONBOARD_APPLICATION_CSV_LOG + " parseCSVandUpdateData : end");
    }

    @Override
    protected List<String> getHeaderList() {
        logger.log(Level.DEBUG, ONBOARD_APPLICATION_CSV_LOG + " getHeaderList : start");
        List<String> headerList = new ArrayList<>();
        headerList.add(CsvConstant.CSV_ENTERPRISE_ID);
        headerList.add(CsvConstant.CSV_APPLICATION_NAME);
        headerList.add(CsvConstant.CSV_APPLICATION_SECRET);
        headerList.add(CsvConstant.TWO_FACTOR_STATUS);
        headerList.add(CsvConstant.CSV_APPLICATION_TYPE);
        headerList.add(CsvConstant.CSV_TRANSACTION_TIMEOUT);
        headerList.add(CsvConstant.CSV_RESET_PIN_UNBLOCK_SETTING);
        headerList.add(CsvConstant.CSV_APPLICATION_DESCRIPTION);
        headerList.add(CsvConstant.CSV_IS_AUTHENTICATION_REQUIRED);
        headerList.add(CsvConstant.CSV_APP_PASSWORD);
        headerList.add(CsvConstant.CSV_ACTIVATION_DATE);
        headerList.add(CsvConstant.CSV_EXPIRATION_DATE);
        headerList.add(CsvConstant.CSV_IS_NOTIFICATION_ENABLED);
        headerList.add(CsvConstant.CSV_IS_PLAINTEXT_ALLOWED);
        headerList.add(CsvConstant.CSV_IS_CAM_ENABLED);
        headerList.add(CsvConstant.CSV_SERVICES);
        headerList.add(CsvConstant.CSV_TOKEN_TTL);
        headerList.add(CsvConstant.CSV_MAX_OTP_ATTEMPTS);
        headerList.add(CsvConstant.CSV_CALL_BACK_URL);
        headerList.add(CsvConstant.CSV_QUEUE_NAME);
        headerList.add(CsvConstant.CSV_COMMENTS);

        logger.log(Level.DEBUG, ONBOARD_APPLICATION_CSV_LOG + " getHeaderList : end");
        return headerList;
    }
}
