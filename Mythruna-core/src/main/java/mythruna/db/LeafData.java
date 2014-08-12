package mythruna.db;

import mythruna.BlockTypeIndex;

import java.util.concurrent.atomic.AtomicBoolean;

public class LeafData implements CellAccess {

    private LeafInfo info;
    private int[][][] cells;
    private AtomicBoolean changed = new AtomicBoolean(false);

    public LeafData(LeafInfo info) {
        this.info = info;
    }

    public long getVersion() {
        return this.info.version;
    }

    public LeafInfo getInfo() {
        return this.info;
    }

    public void markChanged() {
        this.changed.set(true);
    }

    public void clearChanged() {
        this.changed.set(false);
    }

    public boolean isChanged() {
        return this.changed.get();
    }

    public int getX() {
        return this.info.x;
    }

    public int getY() {
        return this.info.y;
    }

    public int getZ() {
        return this.info.z;
    }

    public boolean isLit() {
        return this.info.lit;
    }

    public void setCells(int[][][] cells) {
        this.cells = cells;
    }

    public int[][][] getCells() {
        return this.cells;
    }

    public void clear() {
        this.info.solidCells = 0;
        this.info.emptyCells = 32768;
        this.cells = new int[32][32][32];
        this.info.lit = false;
        markChanged();
    }

    public final boolean isSolid() {
        return this.info.solidCells == 32768;
    }

    public final boolean isEmpty() {
        return (this.cells == null) || (this.info.emptyCells == 32768);
    }

    public final boolean hasCells() {
        return this.cells != null;
    }

    public LeafInfo.Fill getFill() {
        if (isEmpty())
            return LeafInfo.Fill.EMPTY;
        if (isSolid())
            return LeafInfo.Fill.SOLID;
        return LeafInfo.Fill.PARTIAL;
    }

    public final int elevation(int x, int y) {
        if (isSolid())
            return 32;
        if ((isEmpty()) || (this.cells == null)) {
            return -1;
        }
        if ((x < 0) || (y < 0))
            return -1;
        if ((x >= 32) || (y >= 32)) {
            return -1;
        }

        for (int i = 31; i >= 0; i--) {
            if (toType(this.cells[x][y][i]) > 0) {
                this.cells[x][y][i] = 6;
                return i + 1;
            }
        }
        return -1;
    }

    public final boolean contains(int x, int y, int z) {
        if ((x < this.info.x) || (y < this.info.y) || (z < this.info.z))
            return false;
        if ((x >= this.info.x + 32) || (y >= this.info.y + 32) || (z >= this.info.z + 32))
            return false;
        return true;
    }

    public final boolean containsLocal(int i, int j, int k) {
        if ((i < 0) || (j < 0) || (k < 0))
            return false;
        if ((i >= 32) || (j >= 32) || (k >= 32))
            return false;
        return true;
    }

    public final boolean containsLocal(int i, int j, int k, int dir) {
        i += mythruna.Direction.DIRS[dir][0];
        j += mythruna.Direction.DIRS[dir][1];
        k += mythruna.Direction.DIRS[dir][2];
        return containsLocal(i, j, k);
    }

    public final int getWorld(int x, int y, int z) {
        return getType(x - this.info.x, y - this.info.y, z - this.info.z);
    }

    public final int getWorldLight(int type, int x, int y, int z) {
        if (type == 0)
            return getWorldSunlight(x, y, z);
        if (type == 1)
            return getWorldLocalLight(x, y, z);
        return -1;
    }

    public int getWorldSunlight(int x, int y, int z) {
        int val = getRaw(x - this.info.x, y - this.info.y, z - this.info.z);
        return toSunlight(val);
    }

    public int getWorldLocalLight(int x, int y, int z) {
        int val = getRaw(x - this.info.x, y - this.info.y, z - this.info.z);
        return toLocalLight(val);
    }

    public final int setWorldType(int x, int y, int z, int type) {
        return setType(x - this.info.x, y - this.info.y, z - this.info.z, type);
    }

    public final int getType(int x, int y, int z) {
        if (this.cells == null) {
            return 0;
        }
        int val = getRaw(x, y, z);
        return toType(val);
    }

    public final int getCellType(int x, int y, int z) {
        return getType(x, y, z);
    }

    public final void setCellType(int x, int y, int z, int type) {
        setType(x, y, z, type);
    }

    public final int getRaw(int x, int y, int z) {
        if (this.cells == null) {
            return 0;
        }
        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= 32) || (y >= 32) || (z >= 32))
            return 0;
        return this.cells[x][y][z];
    }

    public final int getTypeUnchecked(int i, int j, int k) {
        int val = this.cells[i][j][k];
        return toType(val);
    }

    private int getSunUnchecked(int i, int j, int k) {
        int val = this.cells[i][j][k];
        return toSunlight(val);
    }

    public final int getType(int x, int y, int z, int dir) {
        x += mythruna.Direction.DIRS[dir][0];
        y += mythruna.Direction.DIRS[dir][1];
        z += mythruna.Direction.DIRS[dir][2];

        return getType(x, y, z);
    }

    public final int setType(int x, int y, int z, int val) {
        if (this.cells == null)
            return 0;
        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= 32) || (y >= 32) || (z >= 32)) {
            return 0;
        }
        int old = setTypeUnchecked(x, y, z, val);
        return old;
    }

    public final int setTypeUnchecked(int x, int y, int z, int val) {
        int raw = this.cells[x][y][z];
        int oldType = toType(raw);

        if ((oldType != 0) && (BlockTypeIndex.types[oldType].isSolid())) {
            this.info.solidCells -= 1;
        }
        if (val == 0)
            this.info.emptyCells += 1;
        else if ((BlockTypeIndex.types != null) && (BlockTypeIndex.types[val] != null) && (BlockTypeIndex.types[val].isSolid())) {
            this.info.solidCells += 1;
        }

        this.cells[x][y][z] = (toType(val) | raw & 0xFF000000);
        return oldType;
    }

    private int setSun(int x, int y, int z, int val) {
        if (this.cells == null)
            return 0;
        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= 32) || (y >= 32) || (z >= 32))
            return 0;
        int old = this.cells[x][y][z];

        this.cells[x][y][z] = (old & 0xFFFFFFF | (val & 0xF) << 28);
        return toSunlight(old);
    }

    private int setLocalLight(int x, int y, int z, int val) {
        if (this.cells == null)
            return 0;
        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= 32) || (y >= 32) || (z >= 32))
            return 0;
        int old = this.cells[x][y][z];

        this.cells[x][y][z] = (old & 0xF0FFFFFF | (val & 0xF) << 24);
        return toLocalLight(old);
    }

    public final int setLight(int type, int x, int y, int z, int val) {
        if (type == 0)
            return setSun(x, y, z, val);
        if (type == 1)
            return setLocalLight(x, y, z, val);
        return -1;
    }

    public final int setLightUnchecked(int type, int x, int y, int z, int val) {
        int old = this.cells[x][y][z];
        if (type == 0) {
            this.cells[x][y][z] = (old & 0xFFFFFFF | (val & 0xF) << 28);
            return toSunlight(old);
        }
        if (type == 1) {
            this.cells[x][y][z] = (old & 0xF0FFFFFF | (val & 0xF) << 24);
            return toLocalLight(old);
        }
        return -1;
    }

    public final int getLight(int type, int x, int y, int z) {
        if (this.cells == null)
            return 0;
        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= 32) || (y >= 32) || (z >= 32))
            return 0;
        return getLightUnchecked(type, x, y, z);
    }

    public final int getLightUnchecked(int type, int x, int y, int z) {
        int val = this.cells[x][y][z];
        if (type == 0)
            return toSunlight(val);
        if (type == 1)
            return toLocalLight(val);
        return -1;
    }

    public static final int toType(int v) {
        return v & 0xFFFFFF;
    }

    public static int toSunlight(int v) {
        return v >> 28 & 0xF;
    }

    public static int toLocalLight(int v) {
        return v >> 24 & 0xF;
    }

    public String toString() {
        return new StringBuilder().append("LeafData[changed:").append(this.changed.get()).append(", hasCells:").append(this.cells != null).append(", ").append(this.info).append("]").toString();
    }
}