package in.fortytwo42.adapter.facade;

import java.util.List;

import in.fortytwo42.tos.transferobj.ServiceTO;

public interface ServiceFacadeIntf {

    void loadServices();
    
    List<ServiceTO> getServices();

}
