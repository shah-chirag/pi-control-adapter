
package in.fortytwo42.adapter.service;

import in.fortytwo42.integration.exception.ActiveMQConectionException;

public interface ResourceLogServiceIntf {

    void sendResourceLogToQueue(String resourceLogJson) throws ActiveMQConectionException;

}
