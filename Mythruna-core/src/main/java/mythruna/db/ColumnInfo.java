package mythruna.db;

import mythruna.BlockType;
import mythruna.Coordinates;
import mythruna.MaterialType;

public class ColumnInfo {

    public static final byte WATER = 1;
    public static final byte OCEAN = 2;
    public static final byte GROUND = 3;
    public static final byte TREES = 4;
    public static final byte HILLS = 5;
    public static final byte MOUNTAINS = 6;
    private static int seaLevel = 57;
    private static byte WATER_CANDIDATE = -1;
    private static byte LEAF_CANDIDATE = -2;
    private int x;
    private int y;
    private byte[][] elevations = new byte[32][32];
    private byte[][] types = new byte[32][32];

    private short[] counts = new short[7];
    private short maxHeight;
    private short minHeight;

    public ColumnInfo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public short getCount(byte type) {
        return this.counts[type];
    }

    public byte[][] getTypes() {
        return this.types;
    }

    public byte[][] getElevations() {
        return this.elevations;
    }

    public byte getElevation(int xWorld, int yWorld) {
        int i = xWorld - this.x;
        int j = yWorld - this.y;
        if ((i < 0) || (j < 0))
            return 0;
        if ((i >= 32) || (j >= 32))
            return 0;
        return this.elevations[i][j];
    }

    public byte getType(int xWorld, int yWorld) {
        int i = xWorld - this.x;
        int j = yWorld - this.y;
        if ((i < 0) || (j < 0))
            return 0;
        if ((i >= 32) || (j >= 32))
            return 0;
        return this.types[i][j];
    }

    public static byte valueToType(int v, int h, MaterialType type) {
        if ((type == null) || (type == MaterialType.EMPTY) || (v == 0)) {
            return 0;
        }

        if (type == MaterialType.WATER) {
            return 1;
        }

        if ((type == MaterialType.COBBLE) || (type == MaterialType.ROCK) || (type == MaterialType.WOOD) || (type == MaterialType.WADDLE) || (type == MaterialType.GLASS) || (type == MaterialType.FLORA)) {
            return 0;
        }
        return 3;
    }

    public void recalculate(LeafData[] leafs) {
        int count = 0;
        int total = 1024;

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                this.types[i][j] = 0;
            }

        }

        for (int i = 0; i < this.counts.length; i++) {
            this.counts[i] = 0;
        }
        this.maxHeight = 0;
        this.minHeight = 160;

        byte leafType = (byte) MaterialType.LEAVES.getId();

        int oceanDepthMinimum = 6;

        for (int l = leafs.length - 1; l >= 0; l--) {
            LeafData leaf = leafs[l];
            if ((leaf != null) && (!leaf.isEmpty())) {
                int z = Coordinates.leafToWorld(l);
                for (int i = 0; i < 32; i++) {
                    for (int j = 0; j < 32; j++) {
                        if ((this.types[i][j] == 0) || (this.types[i][j] == WATER_CANDIDATE) || (this.types[i][j] == LEAF_CANDIDATE)) {
                            for (int k = 31; k >= 0; k--) {
                                int v = leaf.getTypeUnchecked(i, j, k);
                                BlockType type = mythruna.BlockTypeIndex.types[v];
                                MaterialType matType = type != null ? type.getMaterial() : null;
                                byte material = type != null ? (byte) type.getMaterial().getId() : 0;

                                int h = z + k;
                                byte t = valueToType(v, h, matType);
                                if (t != 0) {
                                    if ((t == 1) && (l > 0) && (k > 0)) {
                                        if (this.types[i][j] != WATER_CANDIDATE) {
                                            this.types[i][j] = WATER_CANDIDATE;
                                            this.elevations[i][j] = (byte) h;
                                        }
                                    } else {
                                        if (this.types[i][j] == WATER_CANDIDATE) {
                                            int top = this.elevations[i][j];
                                            if (top - h > oceanDepthMinimum) {
                                                int tmp405_404 = 2;
                                                short[] tmp405_401 = this.counts;
                                                tmp405_401[tmp405_404] = (short) (tmp405_401[tmp405_404] + 1);
                                            } else {
                                                int tmp419_418 = 1;
                                                short[] tmp419_415 = this.counts;
                                                tmp419_415[tmp419_418] = (short) (tmp419_415[tmp419_418] + 1);
                                            }

                                            this.types[i][j] = (byte) (material | 0x80);
                                        } else {
                                            if (matType == MaterialType.LEAVES) {
                                                if (this.types[i][j] != 0)
                                                    continue;
                                                this.types[i][j] = LEAF_CANDIDATE;
                                                int tmp484_483 = 4;
                                                short[] tmp484_480 = this.counts;
                                                tmp484_480[tmp484_483] = (short) (tmp484_480[tmp484_483] + 1);
                                                continue;
                                            }

                                            if (this.types[i][j] == 0) {
                                                this.types[i][j] = material;
                                                byte tmp524_522 = t;
                                                short[] tmp524_519 = this.counts;
                                                tmp524_519[tmp524_522] = (short) (tmp524_519[tmp524_522] + 1);
                                            } else if (this.types[i][j] == LEAF_CANDIDATE) {
                                                this.types[i][j] = leafType;
                                                int tmp566_565 = 4;
                                                short[] tmp566_562 = this.counts;
                                                tmp566_562[tmp566_565] = (short) (tmp566_562[tmp566_565] + 1);
                                            }
                                        }

                                        count++;
                                        this.elevations[i][j] = (byte) h;
                                        if (h < this.minHeight)
                                            this.minHeight = (short) h;
                                        if (h > this.maxHeight) {
                                            this.maxHeight = (short) h;
                                        }

                                        if (count != 1024) break;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        return "ColumnInfo[" + this.x + "," + this.y + "]";
    }
}