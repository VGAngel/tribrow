package mythruna;

import org.progeeks.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameConstants {
    public static final String NAME = "Mythruna";
    public static final int VERSION = 20120627;
    public static final int DEFAULT_PORT = 4234;
    public static final int DEFAULT_SEED = 0;
    public static final int CH_TERRAIN = 0;
    public static final int CH_CHAT = 1;
    public static final int CH_ENTITIES = 2;
    private static String version;

    public GameConstants() {
    }

    public static String buildVersion() {
        if (version != null) {
            return version;
        }
        InputStream in = GameConstants.class.getResourceAsStream("/build.date");
        if (in == null) {
            return "Unknown";
        }
        try {
            version = StringUtils.readString(new InputStreamReader(in));
        } catch (IOException e) {
            version = "Error[" + e + "]";
        }
        return version;
    }
}