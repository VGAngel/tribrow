package mythruna.client;

import java.math.BigInteger;
import java.security.MessageDigest;

public class DigestUtils {
    public DigestUtils() {
    }

    public static String getMd5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] result = digest.digest(value.getBytes("UTF-8"));

            BigInteger bi = new BigInteger(1, result);

            String val = bi.toString(16);

            while (val.length() < result.length * 2) {
                val = "0" + val;
            }
            return val;
        } catch (Exception e) {
            throw new RuntimeException("Error creating digest", e);
        }
    }

    public static String hash(String value, String type) {
        try {
            MessageDigest digest = MessageDigest.getInstance(type);
            byte[] result = digest.digest(value.getBytes("UTF-8"));

            BigInteger bi = new BigInteger(1, result);

            String val = bi.toString(16);

            while (val.length() < result.length * 2) {
                val = "0" + val;
            }
            return val;
        } catch (Exception e) {
            throw new RuntimeException("Error creating digest", e);
        }
    }
}