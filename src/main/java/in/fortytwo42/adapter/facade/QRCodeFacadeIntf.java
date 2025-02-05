package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AuthenticationAttemptTO;
import in.fortytwo42.adapter.transferobj.QRCodeDataTO;
import in.fortytwo42.entities.bean.Application;

public interface QRCodeFacadeIntf {

    QRCodeDataTO getQRCode(QRCodeDataTO qrCodeDataTO, String applicationId) throws AuthException;

    QRCodeDataTO getQRCodeV4(QRCodeDataTO qrCodeDataTO) throws AuthException;

    QRCodeDataTO generateGenericQRCode(String applicationId, QRCodeDataTO qrCodeDataHeaderTO) throws Exception;

    String generateQRCode(String encryptedData) throws AuthException;

    String createGenericApprovalAttempt(String applicationId, QRCodeDataTO qrCodeDataHeaderTO) throws AuthException;

    void generateGenericQRApprovalAttempt(AuthenticationAttemptTO authenticationAttemptTO, Application application) throws AuthException;

    //AuthenticationAttemptTO getUpdatedApprovalAttempt(String applicationId, String transactionId) throws AuthException;
    QRCodeDataTO fetchQRStatus(QRCodeDataTO qrCodeDataTO) throws AuthException;

}
