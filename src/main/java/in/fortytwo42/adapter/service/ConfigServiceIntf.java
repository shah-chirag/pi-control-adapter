package in.fortytwo42.adapter.service;

import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.tos.transferobj.ConfigTO;
import org.hibernate.Session;

public interface ConfigServiceIntf {
    ConfigTO addConfig(Session session, ConfigTO configTO);

    ConfigTO updateConfig(Session session, ConfigTO configTO);
    public void deleteConfig(Session session, ConfigTO configTO);
    ConfigTO getConfigValue(String key, String type) throws NotFoundException;

    ConfigTO getConfigValue(String key, String type, long id) throws NotFoundException;
}
