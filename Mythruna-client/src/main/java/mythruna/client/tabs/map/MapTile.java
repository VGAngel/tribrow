package mythruna.client.tabs.map;

import mythruna.MaterialType;
import mythruna.db.ColumnInfo;
import mythruna.db.WorldDatabase;

public class MapTile {
    public static final int TOP_LEAF = 4;
    public static final byte WATER = 1;
    public static final byte GROUND = 2;
    public static final byte TREES = 3;
    public static final byte HILLS = 4;
    public static final byte MOUNTAINS = 5;
    public static final int TILE_SIZE = 256;
    private static final int NUM_LEAFS = 8;
    private int x;
    private int y;
    private byte[][] lowestLeaf = new byte[8][8];
    private byte[][] types = new byte[256][256];

    public MapTile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public static int worldToTile(int i) {
        if (i < 0) {
            i = (i + 1) / 256;
            return i - 1;
        }

        return i / 256;
    }

    public byte[][] getTypes() {
        return this.types;
    }

    public byte getType(int xWorld, int yWorld) {
        int xTile = xWorld - this.x;
        int yTile = yWorld - this.y;

        return this.types[xTile][yTile];
    }

    public void initialize(WorldDatabase worldDb, boolean forceLoad) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                reloadColumn(i, j, this.x + i * 32, this.y + j * 32, worldDb, forceLoad);
            }
        }
    }

    protected byte convert(byte t) {
        switch (t) {
            case 1:
                return 1;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 5;
        }
        return 0;
    }

    public static byte typeToTerrain(int v, int h) {
        if ((v & 0x80) != 0) {
            return 1;
        }
        MaterialType type = MaterialType.type(v);

        if (type == null) {
            return 0;
        }
        if (type == MaterialType.WATER)
            return 1;
        if (type == MaterialType.LEAVES) {
            return 3;
        }
        if (h > 100) {
            return 5;
        }
        if (h > 85) {
            return 4;
        }
        if (type == MaterialType.STONE) {
            return 4;
        }
        return 2;
    }

    public void reloadColumn(int xCol, int yCol, int xLeaf, int yLeaf, WorldDatabase worldDb) {
        reloadColumn(xCol, yCol, xLeaf, yLeaf, worldDb, false);
    }

    public void reloadColumn(int xCol, int yCol, int xLeaf, int yLeaf, WorldDatabase worldDb, boolean force) {
        int count = 0;

        int xBase = xCol * 32;
        int yBase = yCol * 32;

        ColumnInfo colInfo = worldDb.getColumnInfo(xLeaf, yLeaf, force);
        if (colInfo == null) {
            return;
        }
        byte[][] colTypes = colInfo.getTypes();
        byte[][] elevations = colInfo.getElevations();
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                byte t = typeToTerrain(colTypes[i][j], elevations[i][j] & 0xFF);

                this.types[(xBase + i)][(yBase + j)] = t;
            }
        }
    }
}