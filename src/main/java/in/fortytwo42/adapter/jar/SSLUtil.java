package in.fortytwo42.adapter.jar;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;

public class SSLUtil {

    private SSLUtil() {
        super();
    }

    /**
     * Function to get a java keystore for any type of keys
     * @param keyStoreType can be PKCS12, X509, JKS
     * @param keyStoreFilePath path to the key store file
     * @param keyStorePassword password of the keystore
     * @return java keystore object
     * @throws KeyStoreError
     */
    public static KeyStore getKeyStore(String keyStoreType, String keyStoreFilePath, String keyStorePassword)
            throws KeyStoreError {
        KeyStore clientStore;
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(keyStoreFilePath);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new KeyStoreError();
        }
        try {
            clientStore = KeyStore.getInstance(keyStoreType);
            clientStore.load(fileInputStream, keyStorePassword.toCharArray());
            fileInputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new KeyStoreError();
        }
        finally {
            try {
                fileInputStream.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
                System.out.println(e1.getMessage());
            }
        }
        return clientStore;
    }
}
