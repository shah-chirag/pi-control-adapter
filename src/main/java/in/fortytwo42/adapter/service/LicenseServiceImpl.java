
package in.fortytwo42.adapter.service;

import static in.fortytwo42.adapter.util.AES128Impl.decryptData;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import in.fortytwo42.adapter.transferobj.ClientLicenseTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.daos.dao.ApplicationDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.EnterpriseDaoIntf;
import in.fortytwo42.daos.dao.UserDaoIntf;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.entities.bean.Enterprise;


public class LicenseServiceImpl implements LicenseServiceIntf {

	private static final String LICENSE_SERVICE_LOG = "<<<<< LicenseServiceImpl";
	private static Logger logger= LogManager.getLogger(LicenseServiceImpl.class);
	private EnterpriseDaoIntf enterpriseDaoIntf = DaoFactory.getEnterpriseDao();
	private UserDaoIntf userDaoIntf = DaoFactory.getUserDao();
	 private ApplicationDaoIntf applicationDaoIntf= DaoFactory.getApplicationDao();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	LoadingCache<String, ClientLicenseTO> licenseCache =
			CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(1,TimeUnit.MINUTES).build(new CacheLoader<String,
					ClientLicenseTO>() {
				@Override
				public ClientLicenseTO load(String anyKey){
					try {
						String enterpriseAccountId = Config.getInstance().getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
						Enterprise enterprise = enterpriseDaoIntf.getEnterpriseByAccountId(enterpriseAccountId);
						String encryptedLicense = enterprise.getLicense();
						String decryptedLicense = decryptData(encryptedLicense, Config.getInstance().getProperty("licenseEncryptionKey"));
						return OBJECT_MAPPER.readValue(decryptedLicense, ClientLicenseTO.class);
					} catch (Exception e) {
						logger.log(Level.ERROR, "Error occurred while querying for license.", e);
						throw new RuntimeException(e);
					}
				}
			});
	LoadingCache<String, Integer> userCountCache =
			CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(1,TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
				@Override
				public Integer load(String anyKey){
					try {
						return userDaoIntf.getTotalUsers();
					} catch (Exception e) {
						logger.log(Level.ERROR, "Error occurred while querying for license.", e);
						throw new RuntimeException(e);
					}
				}
			});
	LoadingCache<String, Integer> applicationCountCache =
			CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(1,TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
				@Override
				public Integer load(String anyKey){
					try {
						return applicationDaoIntf.getTotalApplicationCount();
					} catch (Exception e) {
						logger.log(Level.ERROR, "Error occurred while querying for license.", e);
						throw new RuntimeException(e);
					}
				}
			});
	private LicenseServiceImpl() {
		super();
	}

	private static final class InstanceHolder {
		private static final LicenseServiceImpl INSTANCE = new LicenseServiceImpl();

		private InstanceHolder() {
			super();
		}
	}

	public static LicenseServiceImpl getInstance() {
		return LicenseServiceImpl.InstanceHolder.INSTANCE;
	}

	@Override
	public void uploadLicense(String encryptedLicenseFile) {
		logger.log(Level.INFO, "Attempting to upload decrypted license on IAM server.");
		IAMExtensionV2 iamExtensionV2 = null;
		try {
			String enterpriseAccountId = Config.getInstance().getProperty(Constant.ENTERPRISE_ACCOUNT_ID);
			iamExtensionV2 = IAMUtil.getInstance().getIAMExtensionV2(enterpriseAccountId);
			Token token = IamExtensionServiceImpl.getInstance().getToken(iamExtensionV2);
			iamExtensionV2.uploadLicense(decryptData(encryptedLicenseFile, Config.getInstance().getProperty("licenseEncryptionKey")), token);
			logger.log(Level.INFO, "Uploading of decrypted license on IAM server successful.");
			enterpriseDaoIntf.uploadLicense(encryptedLicenseFile, enterpriseAccountId);
			logger.log(Level.INFO, "Uploaded encrypted license on Adapter successfully.");
		} catch (IAMException e) {
			logger.log(Level.ERROR, "Exception occurred while uploading license.", e);
		}
	}

	@Override
	public boolean isLicenseValid() {
		return false;
	}

	@Override
	public boolean canOnboardUser() {
		return false;
	}

	@Override
	public boolean canOnboardApplication() {
		return false;
	}

	@Override
	public ClientLicenseTO getLicense() {
		return licenseCache.getUnchecked(Constant.ENTERPRISE_ACCOUNT_ID);
	}

	@Override
	public int getTotalNumberOfOnboardedUsers() {
		return userCountCache.getUnchecked(Constant.ENTERPRISE_ID);
	}

	@Override
	public int getTotalNumberOfApplications() {
		return applicationCountCache.getUnchecked(Constant.ENTERPRISE_ID);
	}

}
