package in.fortytwo42.adapter.util.handler;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import com.itzmeds.rac.core.HttpProxyConfig;
import com.itzmeds.rac.core.ServiceUrlConfig;
import com.itzmeds.rac.core.client.InsecureHostnameVerifier;
import com.itzmeds.rac.core.client.InsecureTrustManager;
import com.itzmeds.rac.core.client.ServiceClient;
import com.itzmeds.rac.core.client.ServiceClientUtils;

public class RestServiceClient implements ServiceClient<RestClientTemplate> {
    private static final Logger LOGGER = LogManager.getLogger(RestServiceClient.class);
    private static final String HTTP_URL_PROTOCOL_PREFIX = "http://";
    private static final String HTTPS_URL_PROTOCOL_PREFIX = "https://";
    private static final String BASIC_AUTH_UID = "basicauth.uid";
    private static final String BASIC_AUTH_PWD = "basicauth.pwd";
    private static final String REQUEST_ENTITY_PROCESSING = "BUFFERED";
    private static final String OAUTH_USERNAME = "url.oauth.enabled";
    private RestClientTemplate restClientTemplate;
    private Integer connectionTimeout;
    private Integer readTimeout;

    public RestServiceClient() {
    }

    public RestServiceClient(int connTimeout, int readTimeout) {
        this.connectionTimeout = connTimeout;
        this.readTimeout = readTimeout;
    }

    public RestServiceClient(RestClientTemplate mockRestClientTemplate) {
        this.restClientTemplate = mockRestClientTemplate;
    }

    public RestClientTemplate createClientTemplate(String urlConfigKey, ServiceUrlConfig serviceUrlConfiguration) {
        if (this.restClientTemplate == null) {
            this.restClientTemplate = this.constructRestClientTemplate(urlConfigKey, serviceUrlConfiguration);
        }

        return this.restClientTemplate;
    }

    private RestClientTemplate constructRestClientTemplate(String urlConfigKey, ServiceUrlConfig serviceUrlConfiguration) {
        ClientConfig clientConfig = new ClientConfig();
        HttpProxyConfig proxyConfig = serviceUrlConfiguration.getProxySetting();
        StringBuilder restCallUrl = new StringBuilder();
        if (proxyConfig != null) {
            clientConfig.property("jersey.config.client.proxy.uri", proxyConfig.getProxyURL());
            clientConfig.property("jersey.config.client.proxy.username", proxyConfig.getUsername());
            clientConfig.property("jersey.config.client.proxy.password", proxyConfig.getPassword());
            clientConfig.property("jersey.config.client.request.entity.processing", "BUFFERED");
            ConnectorProvider httpConnector = new ApacheConnectorProvider();
            clientConfig.connectorProvider(httpConnector);
        }

        Client client = new JerseyClientBuilder().withConfig(clientConfig).build();

        if (serviceUrlConfiguration.isSSLEnabled()) {
            SSLContext restServiceTarget;
            restServiceTarget = null;

            try {
                restServiceTarget = SSLContext.getInstance("TLSv1.2");
//                System.setProperty("https.protocols", "TLSv1.2");
                TrustManager[] trustAllCerts = new TrustManager[]{new InsecureTrustManager()};
                restServiceTarget.init((KeyManager[])null, trustAllCerts, new SecureRandom());
            } catch (NoSuchAlgorithmException var9) {
                var9.printStackTrace();
            } catch (KeyManagementException var10) {
                var10.printStackTrace();
            }

            HostnameVerifier allHostsValid = new InsecureHostnameVerifier();
//            client = new JerseyClientBuilder().withConfig(clientConfig).sslContext(restServiceTarget).hostnameVerifier(allHostsValid).build();
            client = new JerseyClientBuilder().withConfig(clientConfig).build();//.sslContext(restServiceTarget).hostnameVerifier(allHostsValid).build();
            restCallUrl.append("https://").append(serviceUrlConfiguration.getHostname());
        } else {
            restCallUrl.append("http://").append(serviceUrlConfiguration.getHostname());
        }

        if (serviceUrlConfiguration.getUrlAdditionalProperties() != null && serviceUrlConfiguration.getUrlAdditionalProperties().get("basicauth.uid") != null) {
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder().build();
            client.register(feature);
        }

        if (this.connectionTimeout != null) {
            client.property("jersey.config.client.connectTimeout", this.connectionTimeout);
        }

        if (this.readTimeout != null) {
            client.property("jersey.config.client.readTimeout", this.readTimeout);
        }

        if (serviceUrlConfiguration.getPort() != null) {
            restCallUrl.append(":").append(serviceUrlConfiguration.getPort());
        }

        restCallUrl.append(serviceUrlConfiguration.getResourcePath());
        WebTarget restServiceTarget = client.target(restCallUrl.toString());
        restServiceTarget = serviceUrlConfiguration.getUrlPathParams() != null ? ServiceClientUtils.addPathParameters(serviceUrlConfiguration.getUrlPathParams(), restServiceTarget) : restServiceTarget;
        restServiceTarget = serviceUrlConfiguration.getUrlQueryParamListType() != null ? ServiceClientUtils.addQueryParameters(serviceUrlConfiguration.getUrlQueryParamListType(), restServiceTarget) : restServiceTarget;
        restServiceTarget = serviceUrlConfiguration.getUrlQueryParams() != null ? ServiceClientUtils.addQueryParameters(serviceUrlConfiguration.getUrlQueryParams(), restServiceTarget) : restServiceTarget;
        if (serviceUrlConfiguration.isOauthEnabled()) {
            restServiceTarget.register(OAuth2ClientSupport.feature((String)null));
        }

        LOGGER.info("REST URL for config id : " + urlConfigKey + " with static query & path params : " + restServiceTarget.getUri().toString());
        return new RestClientTemplate(restServiceTarget);
    }
}
