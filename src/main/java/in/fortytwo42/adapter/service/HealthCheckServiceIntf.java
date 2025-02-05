package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.exception.AuthException;

public interface HealthCheckServiceIntf {
    String getHealthCheckofConnections() throws AuthException;


}
