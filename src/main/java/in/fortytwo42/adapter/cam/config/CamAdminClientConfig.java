package in.fortytwo42.adapter.cam.config;


import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;

public class CamAdminClientConfig {

    private static Keycloak CamAdminRestClient;
    private static Config config = Config.getInstance();

    private CamAdminClientConfig() {

    }
    /** all the configurable properties needs to store in properties files **/

    public static Keycloak getCamRestInstance() {
        if (CamAdminRestClient == null) {
            int readTimeout = config.getProperty(Constant.CAM_READ_TIMEOUT) != null ? Integer.parseInt(config.getProperty(Constant.CAM_READ_TIMEOUT)) : 30;
            int connectTimeout = config.getProperty(Constant.CAM_CONNECT_TIMEOUT) != null ? Integer.parseInt(config.getProperty(Constant.CAM_CONNECT_TIMEOUT)) : 30;
            int checkoutTimeout = config.getProperty(Constant.CAM_CHECKOUT_TIMEOUT) != null ? Integer.parseInt(config.getProperty(Constant.CAM_CHECKOUT_TIMEOUT)) : 30;
            //change the constants
            ResteasyClient resteasyClient = new ResteasyClientBuilderImpl()
                    .connectionPoolSize(100)
                    .connectionCheckoutTimeout(checkoutTimeout, TimeUnit.SECONDS)
                    .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .maxPooledPerRoute(100)
                    .build();

            CamAdminRestClient = KeycloakBuilder.builder()
                    .serverUrl(Config.getInstance().getProperty(Constant.CAM_ADMIN_URL))
                    .realm(Config.getInstance().getProperty(Constant.CAM_REALM))
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientId(Config.getInstance().getProperty(Constant.CAM_PI_CONTROL_CLIENT_ID))
                    .clientSecret(Config.getInstance().getProperty(Constant.CAM_PI_CONTROL_SECRET))
                    .resteasyClient(resteasyClient)
                    .build();
        }
        return CamAdminRestClient;
    }
}
