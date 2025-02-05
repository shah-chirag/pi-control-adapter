package in.fortytwo42.adapter.service;

import java.io.InputStream;
import java.util.List;

import in.fortytwo42.adapter.jar.entities.FcmNotificationDetails;
import org.hibernate.Session;

import in.fortytwo42.adapter.cam.dto.Client;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.SRAApplicationSettingTO;
import in.fortytwo42.adapter.transferobj.StagingSRAProviderSettingTO;
import in.fortytwo42.daos.exception.ApplicationNotFoundException;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.RemoteAccessSettingTO;
import in.fortytwo42.tos.transferobj.UserApplicationRelTO;

public interface ApplicationServiceIntf {

    PaginatedTO<ApplicationTO> getApplications(String applicationUpdateStatus, int page, String searchText, String _2faStatusFilter, String applicationType, String role) throws AuthException;

    PaginatedTO<ApplicationTO> getApplicationAuditTrails(int page, String searchText, Long fromDate, Long toDate, String role) throws AuthException;

    String generateApplicationSecret();

    void uploadFile(String fileName, InputStream cryptoFile) throws AuthException;

    ApplicationTO authenticateApplication(String applicationId, String password) throws AuthException;

    ApplicationTO deleteApplication(Session session, ApplicationTO applicationTo, String role, String actor) throws AuthException;

    PaginatedTO<UserApplicationRelTO> getUserApplicationRels(String applicationId, String type, String searchQuery,int page, String role, String actor) throws AuthException;  

	List<Application> getApplications();
    
	Application getApplicationByApplicationId(String applicationId) throws AuthException;
	
	Application getNonADApplicationByApplicationId(String applicationId) throws AuthException;
    Application getNonADApplicationByApplicationId(String applicationId, Session session) throws AuthException;


    Application getApplicationWithCallbackUrl(String applicationId) throws AuthException;

    PaginatedTO<UserApplicationRelTO> getUserApplicationRels(Application application, String searchQuery, int page);

    ApplicationTO deleteApplication(Session session, ApplicationTO applicationTo, Application application, String actor) throws AuthException;

    Application getActiveById(Long applicationId) throws NotFoundException;

    List<Application> getPaginatedList(int page, int limit, String searchText, String _2faStatusFilter, String applicationTypeFilter);

    Long getTotalActiveApplicationCount(String searchText, String _2faStatusFilter, String applicationTypeFilter);

    Application updateApplication(Session session, Application application);

	Application getApplicationByApplicationAccountId(String applicationAccountId) throws AuthException;

    Application onboardApplication(Session session, ApplicationTO applicationRequestTO) throws AuthException;

    Application onboardApplicationV2(Session session, ApplicationTO applicationRequestTO, Client camClient) throws AuthException;

    RemoteAccessSettingTO getRemoteAccessSettings(RemoteAccessSettingTO remoteAccessSettingTO) throws AuthException;

    List<ApplicationTO> getSRAApplications(String SRAApplicationType, String role) throws AuthException;
    
    void updateSRAProviderSettingsOnServer(StagingSRAProviderSettingTO sraProviderSetting, Application application) throws AuthException;
    
    void updateSRAConsumerSettingsOnServer(SRAApplicationSettingTO sraConsumerSetting, Application application) throws AuthException;

    void updateSRAProviderSettings(StagingSRAProviderSettingTO stagingSRAProviderSetting, String applicationId) throws AuthException;
    
    Application editApplicationv2(Session session, ApplicationTO applicationTO) throws ApplicationNotFoundException, AuthException;

    ApplicationTO editSRASettings(Session session, SRAApplicationSettingTO stagingSRAConsumerSetting,StagingSRAProviderSettingTO stagingSRAProviderSettin,Application application,ApplicationTO applicationTO) throws AuthException;
    
    void updateSRAConsumerSettings(Session session, SRAApplicationSettingTO stagingSRAConsumerSetting, Application application) throws AuthException;
    
    ApplicationTO createSRAApplicationGatewayRelDeleteRequest(Session session, ApplicationTO applicationTO, String actor) throws AuthException;
    
    ApplicationTO approveDeleteSRAApplicationSetting(Session session, ApplicationTO applicationTO) throws AuthException;

    List<Application> getPaginatedList(int page, int parseInt, String searchText, String _2faStatusFilter, String applicationType, String applicationIds);

    Long getTotalActiveApplicationCount(String searchText, String _2faStatusFilter, String applicationTypeFilter, String applicationIds);

    List<Application> getPaginatedList(int page, int parseInt, String searchText);

    List<Application> getAllApplications();

    void addFCMMultiDeviceDetails(ApplicationTO applicationTO);

    void editFcmNotificationDetails(ApplicationTO applicationTO) throws AuthException;

    FcmNotificationDetails getFcmDetailsByApplicationId(String applicationId) throws Exception;
}
