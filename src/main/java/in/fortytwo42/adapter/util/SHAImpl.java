package in.fortytwo42.adapter.util;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author ChiragShah sha-256 implementation
 */
public final class SHAImpl {

	private static Logger logger=LogManager.getLogger(SHAImpl.class);
	private static final String ALGO_256 = "SHA-256";

	private static final String ALGO_512 = "SHA-512";

	private SHAImpl() {
		super();
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] hashData256(byte[] data) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(ALGO_256);
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new UndeclaredThrowableException(e);
		}
		return messageDigest.digest(data);
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	public static String hashData256(String data) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(ALGO_256);
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new UndeclaredThrowableException(e);
		}
		return Base64.getEncoder().encodeToString(messageDigest.digest(data.getBytes()));
	}

	public static String sha256Hex(byte[] data) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(ALGO_256);
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new UndeclaredThrowableException(e);
		}
		return toHex(messageDigest.digest(data));
	}
	/**
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] hashData512(byte[] data) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(ALGO_512);
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new UndeclaredThrowableException(e);
		}
		return messageDigest.digest(data);
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	public static String hashData512(String data) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(ALGO_512);
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.FATAL, e.getMessage(), e);
			throw new UndeclaredThrowableException(e);
		}
		return Base64.getEncoder().encodeToString(messageDigest.digest(data.getBytes()));
	}

	private static String toHex(byte[] bytes) {
		return String.format("%020x", new BigInteger(1, bytes));
	}

	public static void main(String arg[]) {
        /*String salt = RandomString.nextString(20);
        System.out.println("Salt Generated=" + salt);
        String password = "pEky5nsm";
        System.out.println("Password=" + password);
        System.out.println("Password + Salt=" + (password + salt));
        String hash = SHAImpl.hashData256(password + salt);
        System.out.println("Base64(SHA256(password + salt))=" + hash);
        String secret = salt + hash;
        System.out.println("Header Data Sent >> Application-Secret : " + secret);*/
	}
}
