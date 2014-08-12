package mythruna;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.db.*;
import mythruna.es.EntityData;

import java.util.List;

public class DefaultWorld implements World {

    public static final int CACHE_DEPTH = 1210;
    private static final Vector3f SPAWN_LOCATION = new Vector3f(512.5F, 512.5F, 78.0F);
    private static final Quaternion SPAWN_DIRECTION = Quaternion.DIRECTION_Z;
    private WorldDatabase worldDb;
    private CellAccess cellAccess;
    private CachingBlueprintDatabase bpDb;
    private EntityData entityData;
    private Vector3f defaultLoc = SPAWN_LOCATION;
    private Quaternion defaultDir = SPAWN_DIRECTION;

    public DefaultWorld(WorldDatabase worldDb, BlueprintDatabase bpDb, EntityData entityData) {
        this.worldDb = worldDb;
        this.cellAccess = worldDb;
        this.entityData = entityData;
        this.bpDb = new CachingBlueprintDatabase(bpDb);
    }

    public void setCellAccess(CellAccess access) {
        this.cellAccess = access;
    }

    public void close() {
        this.entityData.close();
        this.worldDb.close();
        this.bpDb.close();
    }

    public EntityData getEntityData() {
        return this.entityData;
    }

    public WorldDatabase getWorldDatabase() {
        return this.worldDb;
    }

    public List<Long> getBlueprintIds() {
        return this.bpDb.getIds();
    }

    public BlueprintData getBlueprint(long id) {
        return this.bpDb.getBlueprint(id);
    }

    public BlueprintData getBlueprint(long id, boolean load) {
        return this.bpDb.getBlueprint(id, load);
    }

    public BlueprintData createBlueprint(String name, int xSize, int ySize, int zSize, float scale, int[][][] cells) {
        return this.bpDb.createBlueprint(name, xSize, ySize, zSize, scale, cells);
    }

    public void setDefaultSpawnLocation(Vector3f loc) {
        this.defaultLoc = loc;
    }

    public Vector3f getDefaultSpawnLocation() {
        return this.defaultLoc;
    }

    public void setDefaultSpawnDirection(Quaternion quat) {
        this.defaultDir = quat;
    }

    public Quaternion getDefaultSpawnDirection() {
        return this.defaultDir;
    }

    public int findEmptySpace(float x, float y, float z, int height, LeafData local) {
        return findEmptySpace(Coordinates.worldToCell(x), Coordinates.worldToCell(y), Coordinates.worldToCell(z), height, local);
    }

    public final int findEmptySpace(int x, int y, int z, int height, LeafData local) {
        int z1 = z;
        int z2 = z;
        while ((z1 > 0) && (z2 < 160)) {
            int count = 0;
            for (int i = z1; i < z1 + height; ) {
                int type = getType(x, y, i, local);
                if (type != 0)
                    break;
                i++;
                count++;
            }

            if (count == height) {
                return z1;
            }
            count = 0;
            for (int i = z2; i < z2 + height; ) {
                int type = getType(x, y, i, local);
                if (type != 0)
                    break;
                i++;
                count++;
            }

            if (count == height) {
                return z2;
            }
            z1--;
            z2++;
        }

        return 160;
    }

    public final LeafData getLeaf(int x, int y, int z, LeafData local) {
        if ((local != null) && (local.contains(x, y, z)))
            return local;
        return this.worldDb.getLeaf(x, y, z);
    }

    public final int getType(float x, float y, float z, LeafData local) {
        return getType(Coordinates.worldToCell(x), Coordinates.worldToCell(y), Coordinates.worldToCell(z), local);
    }

    public final int getType(int x, int y, int z, LeafData local) {
        if (z < 0) {
            return 1;
        }
        LeafData leaf = getLeaf(x, y, z, local);
        if (leaf == null)
            return 0;
        return leaf.getWorld(x, y, z);
    }

    public final int getType(int x, int y, int z, int dir, LeafData local) {
        x += Direction.DIRS[dir][0];
        y += Direction.DIRS[dir][1];
        z += Direction.DIRS[dir][2];
        return getType(x, y, z, local);
    }

    public final int getSunlight(int x, int y, int z, LeafData local) {
        LeafData leaf = getLeaf(x, y, z, local);
        if (leaf == null)
            return 15;
        return leaf.getWorldSunlight(x, y, z);
    }

    public final int getLocalLight(int x, int y, int z, LeafData local) {
        LeafData leaf = getLeaf(x, y, z, local);
        if (leaf == null)
            return 15;
        return leaf.getWorldLocalLight(x, y, z);
    }

    public final int getCellType(int x, int y, int z) {
        return this.worldDb.getCellType(x, y, z);
    }

    public final int getLight(int lightType, int x, int y, int z) {
        return this.worldDb.getLight(lightType, x, y, z);
    }

    public final void setCellType(int x, int y, int z, int type) {
        this.cellAccess.setCellType(x, y, z, type);
    }
}