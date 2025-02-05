package in.fortytwo42.adapter.util;

import javax.ws.rs.client.Entity;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.LDAPDetailsServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.LDAPDetails;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.itzmeds.adfs.client.SignOnException;
import com.itzmeds.adfs.client.SignOnService;
import com.itzmeds.adfs.client.SignOnServiceImpl;
import com.itzmeds.rac.core.ServiceUrlConfig;

import in.fortytwo42.adapter.transferobj.AdfsDetailsTO;
import in.fortytwo42.adapter.util.handler.RestClientTemplate;
import in.fortytwo42.adapter.util.handler.RestServiceClient;

import javax.ws.rs.ProcessingException;
import java.net.MalformedURLException;
import java.net.URL;

public class AdfsUtil {

    private Config config = Config.getInstance();
    private static Logger logger= LogManager.getLogger(AdfsUtil.class);
    private String ADFS_UTIL_LOG = "<<<<< ADFSUtilog";

    private LDAPDetailsServiceIntf ldapDetailsServiceIntf = ServiceFactory.getLdapDetailsService();

    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private static final class InstanceHolder {

        private static final AdfsUtil INSTANCE = new AdfsUtil();

        private InstanceHolder() {
            super();
        }
    }

    public static AdfsUtil getInstance() {
        return AdfsUtil.InstanceHolder.INSTANCE;
    }

    public AdfsDetailsTO getAdfs(String adId, String password) throws SignOnException, JsonProcessingException, ProcessingException, AuthException {
        logger.log(Level.DEBUG, ADFS_UTIL_LOG + " getAdfs : start");

        logger.log(Level.DEBUG, ADFS_UTIL_LOG + " getAdfs : " + adId);
        String userDomainName = null;
        LDAPDetails ldapDetails=null;
        String connectionUrlWithPort = null;
        String connectionPort = null;
        String connectionHost = null;
        String connectionUrl = null;
        String isSslEnabled = null;
        String clientAddress = null;
        if (ValidationUtilV3.isValid(adId) && adId.contains("@")) {
            int index = adId.indexOf('@');
            userDomainName=adId.substring(index+1);
            if(userDomainName!=null){
                try {
                    ldapDetails=  ldapDetailsServiceIntf.getLdapDetailsByUserDomainName(userDomainName);
                }catch (NotFoundException e){
                }
            }
        }
        if(ldapDetails!=null){
            connectionUrlWithPort =ldapDetails.getConnectionUrl();
            try {
                URL  url = new URL(connectionUrlWithPort);
                connectionPort= String.valueOf(url.getPort());
                connectionUrl= url.getProtocol()+"://"+url.getHost();
                connectionHost = url.getHost();
            } catch (MalformedURLException e) {
                throw new AuthException(new Exception(),errorConstant.getERROR_CODE_INVALID_DATA(),e.getMessage());
            }
            isSslEnabled = ldapDetails.getSslEnabled().toString();
            clientAddress = ldapDetails.getClientAddress();
        }else {
            connectionUrl = config.getProperty(Constant.ADFS_CONNECTION_URL);
            connectionHost = config.getProperty(Constant.ADFS_HOSTNAME);
            connectionPort = config.getProperty(Constant.ADFS_PORT);
            isSslEnabled = config.getProperty(Constant.ADFS_SSL_ENABLED);
            clientAddress = config.getProperty(Constant.ADFS_APPLICATION_IDENTIFIER);

        }
        SignOnService signOnService = new SignOnServiceImpl();
        String authRequest = signOnService.createSignOnRequest(adId, password, SignOnService.TokenTypes.JWT_TOKEN_TYPE, connectionUrl +"/adfs/services/trust/13/usernamemixed", clientAddress);

        StringBuilder requestBuilder = new StringBuilder(authRequest);
        String authRequestForLogging = requestBuilder.toString().replaceAll("(<wsse:Password[^>]*>)(.*?)(</wsse:Password>)", "$1******$3");
        logger.log(Level.DEBUG, ADFS_UTIL_LOG + " getAdfs : AuthRequest " +authRequestForLogging);


        String loginSvcUrlTemplate = "{\"url.hostname\":\""+connectionHost+"\",\"url.port\":"+connectionPort+",\"url.resource.path\":\"/adfs/services/trust/13/usernamemixed\",\"url.ssl.enabled\":"+ isSslEnabled +"}";

        logger.log(Level.DEBUG, ADFS_UTIL_LOG + " getAdfs : loginSvcUrlTemplate " +loginSvcUrlTemplate);


        ServiceUrlConfig authSvcUrlConfig = new ObjectMapper().readValue(loginSvcUrlTemplate, ServiceUrlConfig.class);

        RestClientTemplate restClientTemplateTemp = new RestServiceClient(1000000,1000000).createClientTemplate("LOGIN_ACCESS_TOKEN", authSvcUrlConfig);

        Entity<String> postCallInput = Entity.entity(authRequest, "application/soap+xml; charset=utf-8");

        String loginResponse = restClientTemplateTemp.create(null, null, postCallInput).readEntity(String.class);
        logger.log(Level.DEBUG, ADFS_UTIL_LOG + " getAdfs : loginResponse " +loginResponse);

        String authToken = signOnService.getJsonWebToken(loginResponse);
        logger.log(Level.DEBUG, ADFS_UTIL_LOG + " getAdfs : authToken " +authToken);

        Gson gson = new Gson();
        return gson.fromJson(authToken, AdfsDetailsTO.class);
    }
}
