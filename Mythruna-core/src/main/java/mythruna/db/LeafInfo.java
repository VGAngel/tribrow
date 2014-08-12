package mythruna.db;

public class LeafInfo {

    public static final int SIZE = 32;
    public static final int CELL_COUNT = 32768;
    public static final int LIGHT_SUN = 0;
    public static final int LIGHT_LOCAL = 1;
    public int x;
    public int y;
    public int z;
    public int generationLevel;
    public boolean lit;
    public int emptyCells;
    public int solidCells;
    public int typesSize;
    public int lightsSize;
    public long version;
    public long branch;

    public LeafInfo() {
    }

    public LeafInfo(LeafInfo info) {
        this.x = info.x;
        this.y = info.y;
        this.z = info.z;
        this.generationLevel = info.generationLevel;
        this.version = info.version;
        this.lit = info.lit;
        this.emptyCells = info.emptyCells;
        this.solidCells = info.solidCells;
        this.typesSize = info.typesSize;
        this.lightsSize = info.lightsSize;
    }

    public void set(LeafInfo info) {
        this.x = info.x;
        this.y = info.y;
        this.z = info.z;
        this.generationLevel = info.generationLevel;
        this.lit = info.lit;
        this.emptyCells = info.emptyCells;
        this.solidCells = info.solidCells;
        this.typesSize = info.typesSize;
        this.lightsSize = info.lightsSize;
    }

    public String toString() {
        return "LeafInfo[" + this.x + ", " + this.y + ", " + this.z + ", version:" + this.version + ", lit:" + this.lit + "]";
    }

    public static enum Fill {
        EMPTY, SOLID, PARTIAL;
    }
}