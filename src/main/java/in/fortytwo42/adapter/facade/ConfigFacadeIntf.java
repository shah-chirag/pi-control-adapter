
package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.tos.transferobj.ConfigTO;
import org.hibernate.Session;

import java.util.List;

public interface ConfigFacadeIntf {

    String getConfig(String configFileName) throws AuthException;

    PaginatedTO<ConfigTO> getConfigs(Integer page, String searchText, String configType);

    ConfigTO addConfig(String role, String actor,Long id,ConfigTO configTO, boolean saveRequest) throws AuthException;

    void approveRequest(Session session, ConfigTO configTO, String role, String actor);
    public void approveDeleteRequest(Session session, ConfigTO configTO, String role, String actor);
    ConfigTO updateConfig(String role, String actor,Long id, ConfigTO configTO, boolean saveRequest) throws AuthException;
    public ConfigTO deleteConfig(String role, String actor, Long id, ConfigTO configTO, boolean saveRequest) throws AuthException;
}
