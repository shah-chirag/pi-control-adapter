package in.fortytwo42.adapter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RSAUtil {
    private static final String ALGO = "RSA";

    private static final String RSA_UTIL_LOG = "<<<<< RSAUtil";

    private static Logger logger= LogManager.getLogger(RSAUtil.class);

    private static String iamPrivateKey = null;

//    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, CertificateException {
//        generating public and private key

//        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGO);
//        generator.initialize(2048);
//        KeyPair pair = generator.generateKeyPair();
//
//        PrivateKey privateKey = pair.getPrivate();
//        PublicKey publicKey = pair.getPublic();
//
//        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
//        try (FileOutputStream fos1 = new FileOutputStream("D:\\PROJECT 42 LABS\\Projects\\pi-control-adapter\\public.key")) {
//            fos1.write(Base64.getEncoder().encode(x509EncodedKeySpec.getEncoded()));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
//        try (FileOutputStream fos2 = new FileOutputStream("D:\\PROJECT 42 LABS\\Projects\\pi-control-adapter\\private.key")) {
//            fos2.write(Base64.getEncoder().encode(pkcs8EncodedKeySpec.getEncoded()));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

//        String enc = encryptData("ft42@123$");
//        System.out.println("E -> "+enc);
//        System.out.println("D -> "+decryptData("XeotnKJWxGVKP9s9ELVsFMbwzVTo2q66+Fp6t7gbPfERg5VLIItxAv37Vu6zJEPsG3ZS89YKE5VFoqskt3yyfpB7jFapxu2JZvM8jOPmXullzWSo3bcKq0pEdKN4vXuRkfYb5ou+xiIgC8IVP7L29iHm1UhhLwcZ/mRSHJb4+V8vE1Y6fYezJ4a8jHC6qB7md18fqSzKsC1rp5/NEKhdBTDbwCVS5Bg4UhazLObYNWo9gPu86FSt/9//0gtTqT6oE8LwxtjiwX7sg+gGiS3obKamKTmF0TA3EyRXgxmpnQKWzvzD9l2Sftian9xhxnkrZCpuXr06VsTSYlRRQRIJiw=="));

//    }

    public static String encryptData(String data, String certificateFilePath) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, CertificateException {
        logger.log(Level.DEBUG, RSA_UTIL_LOG + " encryptData : end");

//        File publicKeyFile = new File(Config.getInstance().getProperty(Constant.PUBLIC_KEY_FILE_PATH));
//        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
//
//        KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
//        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
//        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        FileInputStream fis = new FileInputStream(certificateFilePath);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(fis);
        PublicKey publicKey = cert.getPublicKey();

        Cipher encryptCipher = Cipher.getInstance(ALGO);
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] secretMessageBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

        logger.log(Level.DEBUG, RSA_UTIL_LOG + " encryptData : end");
        return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    }

    public static String decryptData(String data) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        logger.log(Level.DEBUG, RSA_UTIL_LOG + " decryptData : start");

        File privateKeyFile = new File(Config.getInstance().getProperty(Constant.PRIVATE_KEY_FILE_PATH));
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());

        iamPrivateKey = new String(privateKeyBytes, StandardCharsets.UTF_8);
        iamPrivateKey = iamPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "");
        iamPrivateKey = iamPrivateKey.replace("-----END PRIVATE KEY-----", "");

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(iamPrivateKey));
        KeyFactory kf = KeyFactory.getInstance(ALGO);
        PrivateKey privateKey = kf.generatePrivate(keySpec);

        Cipher decryptCipher = Cipher.getInstance(ALGO);
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decodedString = Base64.getDecoder().decode(data);

        byte[] decryptedMessageBytes = decryptCipher.doFinal(decodedString);
        logger.log(Level.DEBUG, RSA_UTIL_LOG + " decryptData : end");
        return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, CertificateException {
        String enc = encryptData("ft42@123$","D:\\PROJECT 42 LABS\\Projects\\pi-control-adapter\\iamadpuat.cer");
        System.out.println("----------");
        System.out.println(" Encrypted password -> "+enc);
        System.out.println("----------");
    }
}
