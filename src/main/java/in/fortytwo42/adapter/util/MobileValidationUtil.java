package in.fortytwo42.adapter.util;

// TODO: Auto-generated Javadoc
/**
 * The Class MobileValidationUtil.
 */
public class MobileValidationUtil {

    /**
     * Instantiates a new mobile validation util.
     */
    private MobileValidationUtil() {
        super();
    }

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final MobileValidationUtil INSTANCE = new MobileValidationUtil();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of MobileValidationUtil.
     *
     * @return single instance of MobileValidationUtil
     */
    public static MobileValidationUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    /**
     * Checks if is mobile valid.
     *
     * @param mobile the mobile
     * @return true, if is mobile valid
     */
    public boolean isMobileValid(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return false;
        }
        boolean isValid = true;
        
        return isValid;
    }
}
