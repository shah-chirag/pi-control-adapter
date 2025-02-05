
package in.fortytwo42.adapter.util;

import in.fortytwo42.tos.enums.AuthenticationType;

public class ADSyncUtil {

    public static String getADDomain(String userDomain) {
        String[] dc = userDomain.split(",");
        StringBuilder domain = new StringBuilder();
        for (int i = 0; i < dc.length; i++) {
            if (dc[i].split("=")[0].equals("DC")) {
                domain.append(dc[i].split("=")[1]).append(Constant._DOT);
            }
        }
        return domain.subSequence(0, domain.length() - 1).toString();
    }

    public static String getADAdmin(String adminDomain) {
        String[] cn = adminDomain.split(",");
        StringBuilder admin = new StringBuilder();
        for (int i = 0; i < cn.length; i++) {
            if (cn[i].split("=")[0].equals("CN")) {
                admin.append(cn[i].split("=")[1]).append(Constant._DOT);
            }
        }
        return admin.subSequence(0, admin.length() - 1).toString();
    }

    public static String getADPassword(String password, AuthenticationType authenticationType) {
        if (AuthenticationType.encrypted.equals(authenticationType)) {
            return AES128Impl.decryptData(password, Config.getInstance().getProperty(Constant.AD_ENCRYPTION_KEY));
        }
        return password;
    }

    public static String getFilters(String filters) {
        // TODO - ceperate the filters
        return filters;
    }
}
