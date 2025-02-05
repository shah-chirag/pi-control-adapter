
package in.fortytwo42.adapter.util;

import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class CryptoJS.
 */
public class CryptoJS {

    /** The cryptojs log. */
    private static String CRYPTOJS_LOG = "<<<<< CryptoJS";
    private static final Logger logger=LogManager.getLogger(CryptoJS.class);

    
    /**
     * Instantiates a new crypto JS.
     */
    private CryptoJS() {
        super();
    }

    /**
     * Decrypt data.
     *
     * @param aesKey the aes key
     * @param encryptedData the encrypted data
     * @return the string
     */
    public static String decryptData(String aesKey, String encryptedData) {
        logger.log(Level.DEBUG, CRYPTOJS_LOG + " decryptData : start");
        try {
            byte[] cipherData = Base64.getDecoder().decode(encryptedData);
            byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            final byte[][] keyAndIV = generateKeyAndIV(32, 16, 1, saltData, aesKey.getBytes(StandardCharsets.UTF_8), md5);
            SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
            IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

            byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
            Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedData = aesCBC.doFinal(encrypted);
            return new String(decryptedData, StandardCharsets.UTF_8);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new UndeclaredThrowableException(e);
        }finally {
            logger.log(Level.DEBUG, CRYPTOJS_LOG + " decryptData : end");
        }
    }


    /**
     * Generates a key and an initialization vector (IV) with the given salt and password.
     * <p>
     * This method is equivalent to OpenSSL's EVP_BytesToKey function
     * (see https://github.com/openssl/openssl/blob/master/crypto/evp/evp_key.c).
     * By default, OpenSSL uses a single iteration, MD5 as the algorithm and UTF-8 encoded password data.
     * </p>
     * @param keyLength the length of the generated key (in bytes)
     * @param ivLength the length of the generated IV (in bytes)
     * @param iterations the number of digestion rounds 
     * @param salt the salt data (8 bytes of data or <code>null</code>)
     * @param password the password data (optional)
     * @param md the message digest algorithm to use
     * @return an two-element array with the generated key and IV
     */
    public static byte[][] generateKeyAndIV(int keyLength, int ivLength, int iterations, byte[] salt, byte[] password, MessageDigest md) {
        logger.log(Level.DEBUG, CRYPTOJS_LOG + " generateKeyAndIV : start");
        int digestLength = md.getDigestLength();
        int requiredLength = (keyLength + ivLength + digestLength - 1) / digestLength * digestLength;
        byte[] generatedData = new byte[requiredLength];
        int generatedLength = 0;
        try {
            md.reset();
            // Repeat process until sufficient data has been generated
            while (generatedLength < keyLength + ivLength) {
                // Digest data (last digest if available, password data, salt if available)
                if (generatedLength > 0) {
                    md.update(generatedData, generatedLength - digestLength, digestLength);
                }
                md.update(password);
                if (salt != null) {
                    md.update(salt, 0, 8);
                }
                md.digest(generatedData, generatedLength, digestLength);
                // additional rounds
                for (int i = 1; i < iterations; i++) {
                    md.update(generatedData, generatedLength, digestLength);
                    md.digest(generatedData, generatedLength, digestLength);
                }
                generatedLength += digestLength;
            }

            // Copy key and IV into separate byte arrays
            byte[][] result = new byte[2][];
            result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
            if (ivLength > 0) {
                result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);
            }
            return result;
        }
        catch (DigestException e) {
            throw new UndeclaredThrowableException(e);
        }
        finally {
            // Clean out temporary data
            Arrays.fill(generatedData, (byte) 0);
            logger.log(Level.DEBUG, CRYPTOJS_LOG + " generateKeyAndIV : end");
        }
    }
    
    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        //System.out.println("start..>>");
        //System.out.println(CryptoJS.decryptData("D98A47936BBD8135", "U2FsdGVkX1/KE1ArVqeYcim+RTJOc3tgvYIM0ZBSp0w="));
        //System.out.println("End...>>");
    }
}
