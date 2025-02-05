
package in.fortytwo42.adapter.util;


import java.lang.reflect.UndeclaredThrowableException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * AES encryption and decryption logic.
 *
 * @author ChiragShah
 */
public final class AES128Impl {

    /** The aes128 impl. */
    private static String AES128_IMPL = "<<<<< AES128Impl";

    private static Logger logger= LogManager.getLogger(AES128Impl.class);

    /** The Constant ALGO. */
    private static final String ALGO = "AES";

    /**
     * Instantiates a new AES 128 impl.
     */
    private AES128Impl() {
        super();
    }

    /**
     * Encrypt data.
     *
     * @param data the data
     * @param pass the pass
     * @return the string
     */
    public static String encryptData(String data, String pass) {
        logger.log(Level.DEBUG, AES128_IMPL + " encryptData : start");
        Key key = generateKey(pass);
        byte[] encryptedValue;
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            encryptedValue = c.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedValue);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new UndeclaredThrowableException(e);
        }finally {
            logger.log(Level.DEBUG, AES128_IMPL + " encryptData : end");
        }
    }

    /**
     * Decrypt data.
     *
     * @param encryptedData the encrypted data
     * @param pass the pass
     * @return the string
     */
    public static String decryptData(String encryptedData, String pass) {
        logger.log(Level.DEBUG, AES128_IMPL + " decryptData : start");
        Key key = generateKey(pass);
        byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedValue;
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            decryptedValue = c.doFinal(decodedValue);
            return new String(decryptedValue);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new UndeclaredThrowableException(e);
        }finally {
            logger.log(Level.DEBUG, AES128_IMPL + " decryptData : end");
        }
    }

    /**
     * Generate key.
     *
     * @param pass the pass
     * @return the key
     */
    private static Key generateKey(String pass) {
        logger.log(Level.DEBUG, AES128_IMPL + " generateKey : start");
        return new SecretKeySpec(pass.getBytes(), ALGO);
    }
    
    /**
     * Encrypt data with MD 5.
     *
     * @param data the data
     * @param pass the pass
     * @return the string
     */
    public static String encryptDataWithMD5(String data, String pass) {
        logger.log(Level.DEBUG, AES128_IMPL + " encryptDataWithMD5 : start");
		Key key = generateKeyWithMD5(pass);
		byte[] encryptedValue;
		try {
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.ENCRYPT_MODE, key);
			encryptedValue = c.doFinal(data.getBytes());
			return Base64.getMimeEncoder().encodeToString(encryptedValue);
		} catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage(), e);
			throw new UndeclaredThrowableException(e);
		}finally {
	        logger.log(Level.DEBUG, AES128_IMPL + " encryptDataWithMD5 : end");
		}
    }
    
    public static String encryptDataWithSHA256(String data, String pass) {
        logger.log(Level.DEBUG, AES128_IMPL + " encryptDataWithMD5 : start");
        Key key = generateKeyWithSHA256(pass);
        byte[] encryptedValue;
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            encryptedValue = c.doFinal(data.getBytes());
            return Base64.getMimeEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new UndeclaredThrowableException(e);
        }finally {
            logger.log(Level.DEBUG, AES128_IMPL + " encryptDataWithMD5 : end");
        }
    }
    
    /**
     * Decrypt data with MD 5.
     *
     * @param encryptedData the encrypted data
     * @param pass the pass
     * @return the string
     */
    public static String decryptDataWithMD5(String encryptedData, String pass) {
        logger.log(Level.DEBUG, AES128_IMPL + " decryptDataWithMD5 : start");
        Key key = generateKeyWithMD5(pass);
        byte[] decodedValue = Base64.getMimeDecoder().decode(encryptedData);
        byte[] decryptedValue;
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            decryptedValue = c.doFinal(decodedValue);
            return new String(decryptedValue);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new UndeclaredThrowableException(e);
        }finally {
            logger.log(Level.DEBUG, AES128_IMPL + " decryptDataWithMD5 : end");
        }
    }
    
	/**
	 * Generate key with MD 5.
	 *
	 * @param pass the pass
	 * @return the key
	 */
	private static Key generateKeyWithMD5(String pass) {
        logger.log(Level.DEBUG, AES128_IMPL + " generateKeyWithMD5 : start");
		byte[] md5 = getMD5(pass);
		if (md5 != null) {
	        logger.log(Level.DEBUG, AES128_IMPL + " generateKeyWithMD5 : end");
			return new SecretKeySpec(md5, "AES");
		}
        logger.log(Level.DEBUG, AES128_IMPL + " generateKeyWithMD5 : end");
		return null;
	}
	
	private static Key generateKeyWithSHA256(String pass) {
        logger.log(Level.DEBUG, AES128_IMPL + " generateKeyWithMD5 : start");
        byte[] md5 = getSHA256(pass);
        if (md5 != null) {
            logger.log(Level.DEBUG, AES128_IMPL + " generateKeyWithMD5 : end");
            return new SecretKeySpec(md5, "AES");
        }
        logger.log(Level.DEBUG, AES128_IMPL + " generateKeyWithMD5 : end");
        return null;
    }

	/**
	 * Gets the md5.
	 *
	 * @param key the key
	 * @return the md5
	 */
	private static byte[] getMD5(String key) {
        logger.log(Level.DEBUG, AES128_IMPL + " getMD5 : start");
		if (key != null) {
			try {
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				return messageDigest.digest(key.getBytes());
			} catch (NoSuchAlgorithmException e) {
	            logger.log(Level.ERROR, e.getMessage(), e);
			}finally {
		        logger.log(Level.DEBUG, AES128_IMPL + " getMD5 : end");
			}
		}
		return null;
	}
	
	private static byte[] getSHA256(String key) {
        logger.log(Level.DEBUG, AES128_IMPL + " getMD5 : start");
        if (key != null) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                return messageDigest.digest(key.getBytes());
            } catch (NoSuchAlgorithmException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }finally {
                logger.log(Level.DEBUG, AES128_IMPL + " getMD5 : end");
            }
        }
        return null;
    }
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		System.out.println(AES128Impl.decryptData("aAVpdgarxl0jzeJGnVT11A==", "eThWmZq4t7w9z$C&F)J@NcRfUjXn2r5u"));
	}

}
