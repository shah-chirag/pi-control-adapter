package  in.fortytwo42.adapter.facade;

import java.io.IOException;
import java.util.List;

import in.fortytwo42.adapter.transferobj.BuildDetailsTO;

public interface BuildDetailsFacadeIntf {

    List<BuildDetailsTO> getBuildDetails() throws IOException;

}
