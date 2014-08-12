package mythruna.db;

import mythruna.BlockType;
import mythruna.db.tree.MapleTreeFactory;
import mythruna.db.tree.PineTreeFactory;
import mythruna.db.tree.ScrubTreeFactory;
import mythruna.db.tree.SimpleTreeFactory;

import java.util.Random;

public class LeafUtils {

    public static final int EMPTY_CELLS = 0;
    public static final int SOLID_CELLS = 1;
    private static Random random = new Random(0L);
    public static final int COORDINATE_MASK = 268435455;
    public static final int COORDINATE_BITS = 28;
    public static final int ELEVATION_MASK = 255;
    public static final int ELEVATION_BITS = 8;
    private static final TreeFactory SIMPLE = new SimpleTreeFactory();
    private static final TreeFactory MAPLE = new MapleTreeFactory();
    private static final TreeFactory SCRUB = new ScrubTreeFactory();
    private static final TreeFactory PINE = new PineTreeFactory();
    private static final TreeFactory SMALL_PINE = new PineTreeFactory(0, 3, 4, 3);
    private static final TreeFactory SCRUB_PINE = new PineTreeFactory(4, 3, 3, 6);

    private static TreeFactory[] treeFactories = {SIMPLE, SIMPLE, SIMPLE, SIMPLE, SIMPLE, SIMPLE, SIMPLE, SIMPLE, MAPLE, MAPLE, MAPLE, MAPLE, SCRUB, SCRUB, SMALL_PINE, SMALL_PINE, SCRUB_PINE};

    private static TreeFactory[] rockyFactories = {SIMPLE, SIMPLE, SCRUB, SCRUB, SCRUB, SCRUB_PINE, SCRUB_PINE, SCRUB_PINE, PINE, PINE, PINE, PINE, PINE, PINE, PINE, PINE, PINE};

    private static TreeFactory[] highFactories = {SIMPLE, SIMPLE, SIMPLE, SIMPLE, SCRUB_PINE, PINE, PINE, PINE, PINE, PINE, MAPLE, MAPLE, SCRUB, SCRUB, PINE, PINE, PINE};

    public LeafUtils() {
    }

    public static int[][] extractTypes(int[][][] cells) {
        int[][] types = new int[32][32];

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 31; k >= 0; k--) {
                    int t = cells[i][j][k];

                    if ((t != 0) && (t != 10) && (t != 82)) {
                        types[i][j] = t;
                        break;
                    }
                }
            }
        }

        return types;
    }

    public static void generateColumnTypes(int[][][] cells, int[][] map, int[][] mapTypes, long seed, int xBase, int yBase, int seaLevel) {
        int[][] local = new int[32][32];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                local[i][j] = map[(xBase + i)][(yBase + j)];
            }
        }

        int[][] types = new int[32][32];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                types[i][j] = mapTypes[(xBase + i)][(yBase + j)];
            }
        }

        random.setSeed(seed);

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                int hOrig = local[i][j];
                if (hOrig < 0) {
                    hOrig = 0;
                }

                int deepestDirt = hOrig - 10 + (int) (2.0D - random.nextDouble() * 4.0D);
                int h = hOrig;

                int baseValue = types[i][j];
                int topValue = types[i][j];
                int plugValue = 0;
                if (topValue == 82) {
                    plugValue = 82;
                    topValue = 2;
                    baseValue = 1;
                }
                if (baseValue == 2) {
                    baseValue = 1;
                }
                if (baseValue == 3) {
                    deepestDirt += 4;
                }
                BlockType baseType = mythruna.BlockTypeIndex.types[baseValue];
                BlockType topType = mythruna.BlockTypeIndex.types[topValue];

                for (int k = 0; k < h; k++) {
                    int v = baseValue;
                    if (k < deepestDirt) {
                        v = 4;
                    } else if (k == h - 1) {
                        v = topValue;
                    }
                    cells[i][j][k] = v;
                }

                if (plugValue != 0) {
                    types[i][j] = 82;
                }

                for (int k = h; k < seaLevel; k++) {
                    if (k == seaLevel - 1)
                        cells[i][j][k] = 8;
                    else
                        cells[i][j][k] = 7;
                }
            }
        }
    }
}