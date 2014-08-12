package mythruna.db.user;

import mythruna.PlayerData;
import org.progeeks.json.JsonParseException;
import org.progeeks.json.JsonParser;
import org.progeeks.json.JsonPrinter;
import org.progeeks.util.TemplateExpressionProcessor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultUserDatabase implements UserDatabase {

    public static final int DB_VERSION = 1;
    public static final String SETTINGS_FILE = "userDb.settings.json";
    private File base;
    private Map settings;
    private Map<String, Integer> userIds = new ConcurrentHashMap();
    private TemplateExpressionProcessor exp = TemplateExpressionProcessor.getDefaultProcessor();
    private JsonParser jsonParser = new JsonParser();
    private JsonPrinter jsonPrinter = new JsonPrinter();

    private AtomicInteger nextUserId = new AtomicInteger();

    public DefaultUserDatabase(File base) {
        this.base = base;

        if (!base.exists()) {
            if (!base.mkdirs()) {
                throw new RuntimeException("Could not create directory:" + base);
            }
        }
        loadSettings();
    }

    public Set<String> getUserIds() {
        return Collections.unmodifiableSet(this.userIds.keySet());
    }

    protected File getFile(String name) {
        File f = new File(this.base, name);
        return f;
    }

    protected Object loadJson(String file) throws IOException {
        File f = getFile(file);
        if (!f.exists())
            return null;
        FileReader in = new FileReader(f);
        try {
            return this.jsonParser.parse(in);
        } finally {
            in.close();
        }
    }

    protected void loadSettings() {
        try {
            this.settings = ((Map) loadJson("userDb.settings.json"));
            if (this.settings == null) {
                this.settings = new HashMap();
                this.settings.put("nextUserId", Integer.valueOf(0));
                this.settings.put("userIds", new HashMap());
                this.settings.put("version", Integer.valueOf(1));
            }

            this.nextUserId.set(((Integer) this.settings.get("nextUserId")).intValue());

            this.userIds.putAll((Map) this.settings.get("userIds"));
            this.settings.put("userIds", this.userIds);
        } catch (IOException e) {
            throw new RuntimeException("Error reading settings", e);
        }
    }

    protected void writeJson(String file, Object o) throws IOException {
        File f = getFile(file);
        FileWriter out = new FileWriter(f);
        try {
            this.jsonPrinter.write(o, out);
        } finally {
            out.close();
        }
    }

    protected void saveSettings() {
        try {
            writeJson("userDb.settings.json", this.settings);
        } catch (IOException e) {
            throw new RuntimeException("Error saving settings", e);
        }
    }

    protected String toUserFile(int id) {
        return id + ".user.json";
    }

    protected void saveUser(Map user) {
        int id = ((Integer) user.get("id")).intValue();
        try {
            writeJson(toUserFile(id), user);
        } catch (IOException e) {
            throw new RuntimeException("Error writing user, id:" + id, e);
        }
    }

    public PlayerData createUser(String userId, String password) {
        if (this.userIds.containsKey(userId)) {
            return null;
        }
        int id = this.nextUserId.getAndIncrement();
        this.settings.put("nextUserId", Integer.valueOf(this.nextUserId.get()));
        this.userIds.put(userId, Integer.valueOf(id));

        PlayerData player = new DefaultPlayer(this, new HashMap());
        player.set("id", Integer.valueOf(id));
        player.set("dbVersion", Integer.valueOf(1));
        player.set("createTime", Long.valueOf(System.currentTimeMillis()));

        player.set("userInfo.userId", userId);
        player.set("userInfo.password", password);
        player.save();

        saveSettings();

        return player;
    }

    protected PlayerData loadPlayer(int id) {
        try {
            Map values = (Map) loadJson(toUserFile(id));
            return new DefaultPlayer(this, values);
        } catch (IOException e) {
            throw new RuntimeException("Error loading user, id:" + id, e);
        }
    }

    public PlayerData getUser(String userId) {
        Integer id;
        if (!this.userIds.containsKey(userId)) {
            return null;
        }
        id = this.userIds.get(userId);
        try {
            Map values = (Map) loadJson(toUserFile(id));
            return new DefaultPlayer(this, values);
        } catch (JsonParseException e) {
            throw new RuntimeException("Error loading user, userId:" + userId + ", id:" + id, e);
        } catch (IOException e) {
            throw new RuntimeException("Error loading user, userId:" + userId + ", id:" + id, e);
        }
    }

    public PlayerData findUser(String property, Object value) {
        String v = String.valueOf(value);
        for (Integer id : this.userIds.values()) {
            PlayerData p = loadPlayer(id);
            String t = String.valueOf(p.get(property));
            if (v.equals(t))
                return p;
        }
        return null;
    }

    public static void main(String[] args)
            throws IOException {
        long start = System.nanoTime();
        DefaultUserDatabase test = new DefaultUserDatabase(new File("user.db.test"));
        long end = System.nanoTime();

        System.out.println("DB initialized in " + (end - start) / 1000000L + " ms.");

        PlayerData player = test.createUser(args[0], args[1]);
        if (player == null) {
            System.out.println("looking up existing player...");
            player = test.getUser(args[0]);
        }

        System.out.println("user:" + player);
    }
}