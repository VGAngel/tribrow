package mythruna.db.tree;

import mythruna.db.CellAccess;
import mythruna.db.WorldUtils;

import java.util.Random;

public class PineTreeFactory2 {

    private static final int TRUNK = 161;
    private static final int TRUNK2 = 168;
    private static final int LEAVES = 160;
    private static final int LEAVES2 = 173;
    private static final int TOP = 231;
    private static final int BRANCH0 = 223;
    private static final int BRANCH45 = 227;
    private static final int BRANCH90 = 224;
    private static final int BRANCH135 = 228;
    private static final int BRANCH180 = 225;
    private static final int BRANCH225 = 229;
    private static final int BRANCH270 = 226;
    private static final int BRANCH315 = 230;
    private static final int[] PARTS = {0, 223, 227, 224, 228, 225, 229, 226, 230, 161, 168, 231};
    private static final int RADIUS = 3;
    private static final int WIDTH = 7;
    private static final int[][][] layers = {{{0, 0, 0, 7, 0, 0, 0}, {0, 6, 6, 7, 8, 8, 0}, {0, 6, 6, 7, 8, 8, 0}, {5, 5, 5, 9, 1, 1, 1}, {0, 4, 4, 3, 2, 2, 0}, {0, 4, 4, 3, 2, 2, 0}, {0, 0, 0, 3, 0, 0, 0}}, {{0, 0, 0, 7, 0, 0, 0}, {0, 0, 6, 7, 8, 0, 0}, {0, 6, 6, 7, 8, 8, 0}, {5, 5, 5, 9, 1, 1, 1}, {0, 4, 4, 3, 2, 2, 0}, {0, 0, 4, 3, 2, 0, 0}, {0, 0, 0, 3, 0, 0, 0}}, {{0, 0, 0, 7, 0, 0, 0}, {0, 0, 6, 7, 8, 0, 0}, {0, 6, 6, 7, 8, 8, 0}, {5, 5, 5, 9, 1, 1, 1}, {0, 4, 4, 3, 2, 2, 0}, {0, 0, 4, 3, 2, 0, 0}, {0, 0, 0, 3, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 6, 7, 8, 0, 0}, {0, 6, 6, 7, 8, 8, 0}, {0, 5, 5, 9, 1, 1, 0}, {0, 4, 4, 3, 2, 2, 0}, {0, 0, 4, 3, 2, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 6, 7, 8, 0, 0}, {0, 6, 6, 7, 8, 8, 0}, {0, 5, 5, 9, 1, 1, 0}, {0, 4, 4, 3, 2, 2, 0}, {0, 0, 4, 3, 2, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 0, 0, 0}, {0, 0, 6, 7, 8, 0, 0}, {0, 5, 5, 10, 1, 1, 0}, {0, 0, 4, 3, 2, 0, 0}, {0, 0, 0, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 0, 0, 0}, {0, 0, 6, 7, 8, 0, 0}, {0, 5, 5, 10, 1, 1, 0}, {0, 0, 4, 3, 2, 0, 0}, {0, 0, 0, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 6, 7, 8, 0, 0}, {0, 0, 5, 10, 1, 0, 0}, {0, 0, 4, 3, 2, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 6, 7, 8, 0, 0}, {0, 0, 5, 10, 1, 0, 0}, {0, 0, 4, 3, 2, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 0, 0, 0}, {0, 0, 5, 10, 1, 0, 0}, {0, 0, 0, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 0, 0, 0}, {0, 0, 5, 10, 1, 0, 0}, {0, 0, 0, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}};

    private int baseTrunkHeight = 1;
    private int randomTrunkHeight = 4;
    private int baseHeight = 3;
    private int randomHeight = 12;

    public PineTreeFactory2() {
    }

    public PineTreeFactory2(int baseTrunkHeight, int randomTrunkHeight, int baseHeight, int randomHeight) {
        this.baseTrunkHeight = baseTrunkHeight;
        this.randomTrunkHeight = randomTrunkHeight;
        this.baseHeight = baseHeight;
        this.randomHeight = randomHeight;
    }

    private void safeSet(int i, int j, int k, CellAccess world, int value) {
        int v = world.getCellType(i, j, k);
        if (!WorldUtils.canGrow(value, v)) {
            if (value == 231)
                System.out.println("Didn't put the top because we found:" + v + " instead.");
            return;
        }
        world.setCellType(i, j, k, value);
    }

    private void addLayer(int[][] layer, int i, int j, int k, CellAccess world, Random random) {
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                int r = layer[x][y];
                if (r != 0) {
                    int v = PARTS[r];
                    safeSet(i + x - 3, j + y - 3, k, world, v);
                }
            }
        }
    }

    public boolean addTree(int i, int j, int k, CellAccess world, Random random) {
        int trunkHeight = random.nextInt(this.randomTrunkHeight) + this.baseTrunkHeight;
        int height = random.nextInt(this.randomHeight) + this.baseHeight;

        if (k + trunkHeight + height + 1 >= 160) {
            return false;
        }

        float delta = layers.length / height;
        float layer = 0.0F;

        if (delta > 1.0F) {
            delta = 1.0F;
            layer = layers.length - height;
        }

        int trunkType = layers[Math.round(layer)][3][3];
        trunkType = PARTS[trunkType];

        int start = Math.max(k, 0);
        int end = Math.min(k + trunkHeight, 160);

        for (int z = start; z < end; z++) {
            safeSet(i, j, z, world, trunkType);
        }

        start = k + trunkHeight;
        end = start + height;

        for (int z = start; z < end; z++) {
            int index = Math.min(Math.round(layer), layers.length - 1);
            layer += delta;
            addLayer(layers[index], i, j, z, world, random);
        }

        safeSet(i, j, end, world, 231);

        return false;
    }
}