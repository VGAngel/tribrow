package mythruna;

import mythruna.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WorldInfo {
    private static final String WORLD_INFO_FILE = "world-info.json";
    private File file;
    private String name = "Mythruna";
    private int seed = 0;
    private Map<String, Object> metaData = new HashMap();

    protected WorldInfo(Map<String, Object> values) {
        if (values != null) {
            Number n = (Number) values.get("seed");
            if (n != null)
                this.seed = n.intValue();
            this.name = ((String) values.get("name"));
            this.metaData = ((Map) values.get("metaData"));
            if (this.metaData == null)
                this.metaData = new HashMap();
        }
    }

    public static WorldInfo load(File dir) {
        try {
            File f = new File(dir, "world-info.json");

            Map values = (Map) JsonUtils.loadJson(f);
            if (values == null) {
                return null;
            }
            WorldInfo info = new WorldInfo(values);
            info.file = f;
            return info;
        } catch (IOException e) {
            throw new RuntimeException("Error saving world info", e);
        }
    }

    public static WorldInfo create(File dir, String name, int seed) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not make directory:" + dir);
            }
        }
        WorldInfo info = new WorldInfo(null);
        info.name = name;
        info.seed = seed;
        info.file = new File(dir, "world-info.json");

        info.save();

        return info;
    }

    public boolean changeDirectory(File dir) throws IOException {
        if (dir.getCanonicalFile().equals(this.file.getCanonicalFile().getParentFile()))
            return false;
        this.file = new File(dir, "world-info.json");
        return true;
    }

    public String getName() {
        return this.name;
    }

    public int getSeed() {
        return this.seed;
    }

    public Map<String, Object> getMetaData() {
        return this.metaData;
    }

    public void save() {
        try {
            Map values = new HashMap();
            values.put("name", this.name);
            values.put("seed", Integer.valueOf(this.seed));
            values.put("metaData", this.metaData);
            JsonUtils.writeJson(this.file, values);
        } catch (IOException e) {
            throw new RuntimeException("Error saving world info", e);
        }
    }
}