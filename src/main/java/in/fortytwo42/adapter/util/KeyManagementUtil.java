/**
 * 
 */

package in.fortytwo42.adapter.util;


/**
 * @author ChiragShah
 *
 */
public class KeyManagementUtil {


    private KeyManagementUtil() {
        super();
    }

    public static String getAESKey() {
        return Config.getInstance().getProperty(Constant.AES_KEY);
    }

}
