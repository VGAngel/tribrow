package mythruna.db;

import mythruna.util.LruCache;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CachingBlueprintDatabase implements BlueprintDatabase {

    private LruCache<Long, BlueprintData> blueprints = new LruCache("Blueprints", 100);
    private BlueprintDatabase bpDb;

    public CachingBlueprintDatabase(BlueprintDatabase bpDb) {
        this.bpDb = bpDb;
    }

    public void close() {
        this.bpDb.close();
    }

    public List<Long> getIds() {
        if (this.bpDb == null)
            return Collections.emptyList();
        try {
            return this.bpDb.getIds();
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving IDs", e);
        }
    }

    protected BlueprintData loadBlueprint(long id) throws IOException {
        BlueprintData result = this.bpDb.getBlueprint(id);
        if (result == null) {
            return result;
        }
        this.blueprints.put(Long.valueOf(id), result);
        return result;
    }

    protected BlueprintData newBlueprint(String name, int xSize, int ySize, int zSize, float scale, int[][][] cells)
            throws IOException {
        return this.bpDb.createBlueprint(name, xSize, ySize, zSize, scale, cells);
    }

    public synchronized BlueprintData createBlueprint(String name, int xSize, int ySize, int zSize, float scale, int[][][] cells) {
        try {
            BlueprintData bp = newBlueprint(name, xSize, ySize, zSize, scale, cells);
            this.blueprints.put(Long.valueOf(bp.id), bp);
            return bp;
        } catch (IOException e) {
            throw new RuntimeException("Error creating blueprint", e);
        }
    }

    public BlueprintData getBlueprint(long id, boolean load) {
        BlueprintData bp = (BlueprintData) this.blueprints.get(Long.valueOf(id));
        if ((bp != null) || (!load))
            return bp;
        try {
            return loadBlueprint(id);
        } catch (IOException e) {
            throw new RuntimeException("Error reading blueprint for:" + id, e);
        }
    }

    public BlueprintData getBlueprint(long id) {
        return getBlueprint(id, true);
    }
}