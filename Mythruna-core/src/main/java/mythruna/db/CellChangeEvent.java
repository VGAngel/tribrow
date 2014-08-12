package mythruna.db;

public class CellChangeEvent {

    private LeafData leaf;
    private int x;
    private int y;
    private int z;
    private int oldType;
    private int newType;

    public CellChangeEvent(LeafData leaf, int x, int y, int z, int oldType, int newType) {
        this.leaf = leaf;
        this.x = x;
        this.y = y;
        this.z = z;
        this.oldType = oldType;
        this.newType = newType;
    }

    public LeafData getLeaf() {
        return this.leaf;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public int getOldType() {
        return this.oldType;
    }

    public int getCellType() {
        return this.newType;
    }
}