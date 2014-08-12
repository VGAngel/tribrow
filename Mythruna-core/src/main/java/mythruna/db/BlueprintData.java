package mythruna.db;

public class BlueprintData {

    public long id;
    public String name;
    public int xSize;
    public int ySize;
    public int zSize;
    public int[][][] cells;
    public float scale;

    public BlueprintData() {
    }

    public String toString() {
        return "BlueprintData[" + this.id + ", " + this.name + ", x " + this.scale + " [" + this.xSize + "][" + this.ySize + "][" + this.zSize + "]]";
    }
}