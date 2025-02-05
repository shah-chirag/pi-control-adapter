/**
 * 
 */

package in.fortytwo42.adapter.util;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import org.apache.commons.codec.binary.Base64;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

/**
 * @author ChiragShah
 *
 */
public class TOTPUtil {

    private TOTPUtil() {
        super();
    }

    public static String generateRandomSeed() {
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        return secretGenerator.generate();
    }
    
    public static String getOnboardingQR(String secret, String label, String issuer) throws QrGenerationException {
        QrData data = new QrData.Builder().label(label).secret(secret).issuer(issuer).build();
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        Base64 base64Codec = new Base64();
        return new String(base64Codec.encode(imageData));
    }
    
    public static String getJSEncodedOnboardingQR(String secret, String label, String issuer) throws QrGenerationException {
        QrData data = new QrData.Builder().label(label).secret(secret).issuer(issuer).build();
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        String mimeType = generator.getImageMimeType();
        return getDataUriForImage(imageData, mimeType);
    }
    
    public static String[] getRecoveryCodes(int numberOfCodes) {
        RecoveryCodeGenerator recoveryCodes = new RecoveryCodeGenerator();
        return recoveryCodes.generateCodes(numberOfCodes);
    }
    
    /**
     * OTP Digits(6), TimePeriod(30), SystemTimeProvider(Current time)
     * Current and Last OTP Both Valid
     * @param secret
     * @param code
     * @return
     */
    public static boolean verifyCode(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(secret, code);
    }
    
    /**
     * Additional Parameters for overriding default like Digits, Validity TimePeriod, Old OTP validation
     * @param secret User secret
     * @param code User submitted OTP
     * @param codeDigits OTP generation digits
     * @param timePeriod OTP Validity Time Period Generation e.g. 30
     * @param allowedNoOfOldOTP Validity check for Last N OTP Values 
     * e.g. 1 -> Current and Last OTP both are valid, 0 -> Only Current OTP Valid
     * @return
     */
    public static boolean verifyCode(String secret, String code, int codeDigits, int timePeriod, int allowedNoOfOldOTP) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, codeDigits);
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        verifier.setTimePeriod(timePeriod);
        verifier.setAllowedTimePeriodDiscrepancy(allowedNoOfOldOTP);
        return verifier.isValidCode(secret, code);
    }
    
    /**
     * Additional Parameters for overriding default like Digits, Validity TimePeriod, Old OTP validation
     * @param secret User secret
     * @param code User submitted OTP
     * @param codeDigits OTP generation digits
     * @param timePeriod OTP Validity Time Period Generation e.g. 30
     * @param allowedNoOfPrevOTP Validity check for Last N OTP Values 
     * e.g. 1 meaning Current and Last OTP both are valid 
     * @param hashAlgorithm Algorithm for Hashing Default:SHA1 Options:SHA256,SHA512
     * @return
     */
    public static boolean verifyCode(String secret, String code, int codeDigits, int timePeriod, int allowedNoOfPrevOTP, HashingAlgorithm hashAlgorithm) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(hashAlgorithm, codeDigits);
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        verifier.setTimePeriod(timePeriod);
        verifier.setAllowedTimePeriodDiscrepancy(allowedNoOfPrevOTP);
        return verifier.isValidCode(secret, code);
    }
}
