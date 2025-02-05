package in.fortytwo42.adapter.facade;

import java.util.List;

import in.fortytwo42.tos.transferobj.ServerRegistryTO;

public interface ServerRegistryFacadeIntf {
    List<ServerRegistryTO> getServersRegistry();
}
