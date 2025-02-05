/**
 * 
 */

package in.fortytwo42.adapter.util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.QueryFormatException;
import in.fortytwo42.enterprise.extension.utils.RandomString;

/**
 * string addition function using string builder
 * @author ChiragShah
 */
public class StringUtil {

    private static Gson gson = new Gson();

    private StringUtil() {
        super();
    }

    /**
     * concatenate all the string parts into one string using string builder
     * @param stringArray continues string 
     * @return concatenated string
     */
    public static String build(String... stringArray) {
        StringBuilder builder = new StringBuilder();
        if (stringArray != null && stringArray.length != 0) {
            for (String value : stringArray) {
                builder.append(value);
            }
        }
        return builder.toString();
    }

    public static String getHex(byte[] data) {
        return String.format("%020x", new BigInteger(1, data));
    }

    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryMap = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] keyValueQueries = query.split(Constant._AND);
            for (String keyValue : keyValueQueries) {
                if (!keyValue.isEmpty()) {
                    String[] queryString = keyValue.split(Constant._EQUAL, 2);
                    if (queryString.length == 2) {
                        if (queryString[1] != null && !queryString[1].isEmpty()) {
                            queryMap.put(queryString[0], queryString[1]);
                        }
                    }
                }
            }
        }
        return queryMap;
    }

    public static Object parseQueryValue(String value, in.fortytwo42.adapter.enums.QueryParam queryParam) throws QueryFormatException {
        try {
            switch (queryParam.getDataType()) {
                case STRING:
                    return value;
                case INTEGER:
                    if (value != null) {
                        return Integer.parseInt(value);
                    }
                    return null;
                case LONG:
                    if (value != null) {
                        return Long.parseLong(value);
                    }
                    return null;
                default:
                    return null;
            }
        }
        catch (Exception e) {
            throw new QueryFormatException();
        }
    }

    public static String paddLeft(String inputString, int expectedStringLength, char characterToPad) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expectedStringLength; i++) {
            sb.append(characterToPad);
        }
        return sb.substring(inputString.length()) + inputString;
    }

    public static String getRandomString(int length) {
        return RandomString.nextString(length);
    }

    public static String generateKey(String authCode) {
        String salt1 = "Zq4t7w9z$C7w9";
        String salt2 = "J@NcRfUjXnr5u";
        return salt1 + authCode + salt2;
    }

    public static String getKey(String key) {
        String salt1 = "z$C7w9";
        String salt2 = "jXnr5u";
        return salt1 + key + salt2;
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
    public static boolean isNotNullOrEmpty(String str){
        return !StringUtils.isNullOrEmpty(str);
    }
}
