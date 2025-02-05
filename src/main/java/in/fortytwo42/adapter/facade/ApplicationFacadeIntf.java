
package in.fortytwo42.adapter.facade;

import java.io.InputStream;
import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TotpSettingsTO;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.RemoteAccessSettingTO;
import in.fortytwo42.tos.transferobj.RequestTO;
import in.fortytwo42.tos.transferobj.RunningHashTo;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

public interface ApplicationFacadeIntf {

    PaginatedTO<UserApplicationRelTO> getUserApplicationRels(String applicationId, String status, String searchQuery, int page, String role, String actor) throws AuthException;

    ApplicationTO deleteApplication(ApplicationTO applicationTo, String role, String actor) throws AuthException;

    String generateApplicationSecret();

    PaginatedTO<RequestTO> getApplicationAuditTrails(int page, String searchText, Long fromDate, Long toDate, String role) throws AuthException;

    List<Application> getApplications();

    PaginatedTO<ApplicationTO> getApplications(int page, String searchText, String _2faStatusFilter, String applicationType, String role, Long userGroupId) throws AuthException;

    ApplicationTO onboardApplication(String role, String actor,Long id, ApplicationTO applicationTO,
                                     boolean saveRequest) throws AuthException;

    ApplicationTO clearCache( ApplicationTO applicationTO) throws AuthException;

    ApplicationTO onboardApplicationBulk(String role, String actor, ApplicationTO applicationTO) throws AuthException;

    ApplicationTO onboardApplication(Session session, String role, String actor,Long id, ApplicationTO applicationTO,
                                     boolean saveRequest) throws AuthException;
    
    ApplicationTO editApplicationv2(String role, String actor,Long id, ApplicationTO applicationTO,
                                    boolean saveRequest) throws AuthException;

    ApplicationTO editApplicationv2(Session session, String role, String actor,Long id, ApplicationTO applicationTO,
                                    boolean saveRequest) throws AuthException;


    List<ApplicationTO> getSRAApplications(String SRAApplicationType, String role) throws AuthException;
    
    RemoteAccessSettingTO getRemoteAccessSettings(RemoteAccessSettingTO remoteAccessSettingTO) throws AuthException;
    
    ApplicationTO deleteSRAApplicationSetting(String actor, String role, ApplicationTO applicationTO, boolean saveRequest)throws AuthException;
    
    ApplicationTO approveApplicationEdit(Session session, String role, String actor, ApplicationTO applicationTO) throws AuthException;
    
    ApplicationTO approveApplicationOnboard(Session session, String role, String actor, ApplicationTO applicationTO) throws AuthException;

    boolean isSRADetailsMatch(String senderAccountId, String host, Integer port) throws AuthException;

    CSVUploadTO uploadOnboardApplication(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException;

    String readSampleCsvFile(String fileName);

    String downloadUpdateApplicationStatus(String fileName, String role) throws AuthException;
    String generateRunningHash(String applicationId) throws AuthException;
    boolean verifyRunningHash(String applicationId, RunningHashTo runningHashTo) throws AuthException;
    public TotpSettingsTO getTotpSettingsByApplicationId(String applicationId)throws ApplicationNotFoundException;
}
