package in.fortytwo42.adapter.util.handler;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.AuthTokenExpiredException;

public class HotpUtil {


    public static String generateRandomSeed(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }

    private static final int[] doubleDigits = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};

    public static int calcChecksum(long num, int digits) {
        boolean doubleDigit = true;
        int total = 0;
        while (0 < digits--) {
            int digit = (int) (num % 10);
            num /= 10;
            if (doubleDigit) {
                digit = doubleDigits[digit];
            }
            total += digit;
            doubleDigit = !doubleDigit;
        }
        int result = total % 10;
        if (result > 0) {
            result = 10 - result;
        }
        System.out.println("Checksum "+result);
        return result;
    }

    public static byte[] hmac_sha1(byte[] keyBytes, byte[] text) throws AuthException {
        Mac hmacSha1;
        try {
            hmacSha1 = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException nsae) {
            try {
                hmacSha1 = Mac.getInstance("HMAC-SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new AuthException(new Throwable(),1L,"algorithm not found");
            }
        }
        SecretKeySpec macKey =
                new SecretKeySpec(keyBytes, "RAW");
        try {
            hmacSha1.init(macKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return hmacSha1.doFinal(text);
    }

    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1,10,100,1000,10000,100000,1000000,10000000,100000000};

    public static String generateOTP(String seed, long counter, int codeDigits, boolean addChecksum) throws AuthException {
        System.out.println("Generate otp started");
        String result = null;
        int truncationOffset = -1;

        int digits = addChecksum ? (codeDigits + 1) : codeDigits;
        byte[] text = new byte[8];
        for (int i = text.length - 1; i >= 0; i--) {
            text[i] = (byte) (counter & 0xff);
            counter >>= 8;
        }

        // compute hmac hash
        byte[] hash = hmac_sha1(seed.getBytes(StandardCharsets.UTF_8), text);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;
        if ((0 <= truncationOffset) &&
                (truncationOffset < (hash.length - 4))) {
            offset = truncationOffset;
        }
        int binary =
                ((hash[offset] & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        if (addChecksum) {
            otp = (otp * 10) + calcChecksum(otp, codeDigits);
        }

        result = Integer.toString(otp);
        while (result.length() < digits) {
            result = "0" + result;
        }

        System.out.println(result);
        return result;
    }

    public static Boolean validateOtp(String seed, long counter, int codeDigits, String generatedOtp, Timestamp otpGeneratedAt, int validTill, boolean addChecksum)
            throws AuthTokenExpiredException, AuthException {
        System.out.println("Validate otp started");

        Timestamp currentTimestamp = Timestamp.from(Instant.now());
        System.out.println("CurrentTimestamp " + currentTimestamp);

        int diff = (int) ((currentTimestamp.getTime() - otpGeneratedAt.getTime()) / 1000);
        System.out.println("Diff in time = " + diff);

        if (diff <= validTill) {
            String result = generateOTP(seed, counter, codeDigits,addChecksum);

            if (generatedOtp.equals(result)) {
                System.out.println("Otp is valid");
                System.out.println(result);
                return true;
            } else {
                System.out.println("Otp is invalid");
                System.out.println(result);
                return false;
            }
        } else {
            throw new AuthTokenExpiredException();
        }
    }

    public static String generateHotp(String randomSeed, Long counter) {
        return null;
    }
}
