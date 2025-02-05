package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;

public interface HealthCheckFacadIntf {


    String getHealthCheckofConnections() throws AuthException;
}
