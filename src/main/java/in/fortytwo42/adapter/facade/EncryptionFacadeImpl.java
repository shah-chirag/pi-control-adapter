package  in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ApplicationServiceIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.IAMExceptionConvertorUtil;
import in.fortytwo42.adapter.util.IAMUtil;
import in.fortytwo42.enterprise.extension.core.DecryptionDataV2;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.dataconverter.DataConverterFactory;
import in.fortytwo42.enterprise.extension.enums.AccountType;
import in.fortytwo42.enterprise.extension.enums.CI2Type;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptTO;
import in.fortytwo42.enterprise.extension.tos.ApprovalAttemptWE;
import in.fortytwo42.enterprise.extension.utils.IAMConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class EncryptionFacadeImpl.
 */
public class EncryptionFacadeImpl implements EncryptionFacadeIntf {

    /** The encryption facade impl log. */
    private String ENCRYPTION_FACADE_IMPL_LOG = "<<<<< EncryptionFacadeImpl";

	private static Logger logger= LogManager.getLogger(EncryptionFacadeImpl.class);

    private ApplicationServiceIntf applicationService = ServiceFactory.getApplicationService();
    
    private IAMUtil iamUtil = IAMUtil.getInstance();
    private IAMExceptionConvertorUtil iamExceptionConvertorUtil = IAMExceptionConvertorUtil.getInstance();

	/**
	 * The Class InstanceHolder.
	 */
	private static final class InstanceHolder {
		
		/** The Constant INSTANCE. */
		private static final EncryptionFacadeImpl INSTANCE = new EncryptionFacadeImpl();

		/**
		 * Instantiates a new instance holder.
		 */
		private InstanceHolder() {
			super();
		}
	}

	/**
	 * Gets the single instance of EncryptionFacadeImpl.
	 *
	 * @return single instance of EncryptionFacadeImpl
	 */
	public static EncryptionFacadeImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Gets the decryption key.
	 *
	 * @param signTransactionId the sign transaction id
	 * @param clientId the client id
	 * @return the decryption key
	 * @throws AuthException the auth exception
	 */
    /*@Override
    public DecryptionDataV2 getDecryptionKey(String signTransactionId, String clientId, String type) throws AuthException {
        logger.log(Level.DEBUG, ENCRYPTION_FACADE_IMPL_LOG + " getDecryptionKey : start");
    	Application application = applicationService.getApplicationByApplicationId(clientId);
    	try {
    		IAMExtensionV2 iamExtension = iamUtil.getIAMExtensionV2(application.getEnterprise().getEnterpriseAccountId());
    		Token token = iamUtil.authenticateV2(iamExtension, application.getApplicationId(), AES128Impl.decryptData(application.getPassword(), KeyManagementUtil.getAESKey()));
    		CI2Type ci2Type = type != null ? CI2Type.valueOf(type) : CI2Type.VERIFY;
    		return iamExtension.getDecryptionKey(token, signTransactionId, ci2Type);
    		//return iamExtension.getDecryptionKeyCopy(token, signTransactionId, ci2Type);
    	} catch (IAMException e) {
    		logger.log(Level.ERROR, e);
    		throw iamExceptionConvertorUtil.convertToAuthException(e);
    	}finally {
            logger.log(Level.DEBUG, ENCRYPTION_FACADE_IMPL_LOG + " getDecryptionKey : end");
    	}
    }*/
	
	@Override
    public DecryptionDataV2 getDecryptionKey(String signTransactionId, String clientId, String type) throws AuthException {
        logger.log(Level.DEBUG, ENCRYPTION_FACADE_IMPL_LOG + " getDecryptionKey : start");
        try {
			String reqRefNum = ThreadContext.get(Constant.REQUEST_REFERENCE);
			IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
			IAMExtensionV2 iamExtension = iamExtensionService.getIAMExtension();
			Token token = iamExtensionService.getToken(iamExtension);
			if (Boolean.parseBoolean(Config.getInstance().getProperty(Constant.DECRYPTION_KEY_TEST_MODE))) {
				ApprovalAttemptWE approvalAttemptWE = iamExtension.getApprovalAttemptBySignTransactionIdV2(token, signTransactionId);
				if (approvalAttemptWE != null) {
					AccountType accountType = AccountType.valueOf(token.getRole());
					ApprovalAttemptTO approvalAttemptV2 = DataConverterFactory.getDataFilter(accountType).getApprovalAttemptTO(approvalAttemptWE);
					DecryptionDataV2 decryptionDataV2 = new DecryptionDataV2();
					decryptionDataV2.setDecryptionKey(Config.getInstance().getProperty(Constant.DECRYPTION_KEY_FOR_TEST));
					decryptionDataV2.setConsumerAccountId(approvalAttemptV2.getConsumerAccountId());
					return decryptionDataV2;
				} else {
					throw new IAMException(null, IAMConstants.ERROR_CODE_TRANSACTION_NOT_PRESENT, IAMConstants.ERROR_MSG_TRANSACTION_NOT_PRESENT);
				}
			} else {
				CI2Type ci2Type = type != null ? CI2Type.valueOf(type) : CI2Type.VERIFY;
				return iamExtension.getDecryptionKey(token, signTransactionId, ci2Type, reqRefNum);
			}
        } catch (IAMException e) {
            logger.log(Level.ERROR, e);
            throw iamExceptionConvertorUtil.convertToAuthException(e);
        }finally {
            logger.log(Level.DEBUG, ENCRYPTION_FACADE_IMPL_LOG + " getDecryptionKey : end");
        }
    }
}
