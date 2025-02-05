package in.fortytwo42.adapter.util;

import java.util.Random;
import java.util.UUID;

public class UUIDGenerator {

    public static String generate() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    
    public static String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }
 
}
