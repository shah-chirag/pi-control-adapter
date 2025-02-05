
package in.fortytwo42.adapter.facade;

import java.io.InputStream;
import java.util.List;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.tos.transferobj.EnterpriseTO;

public interface EnterpriseFacadeIntf {

    EnterpriseTO onboardEnterprise(EnterpriseTO enterpriseTO) throws AuthException;

    List<EnterpriseTO> getEnterprises();

    void uploadFile(String fileName, InputStream cryptoFileInputStream) throws AuthException;

}
