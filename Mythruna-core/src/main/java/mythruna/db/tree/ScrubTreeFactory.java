package mythruna.db.tree;

import mythruna.db.TreeFactory;
import mythruna.db.WorldUtils;

import java.util.Random;

public class ScrubTreeFactory implements TreeFactory {

    private static final int TRUNK = 161;
    private static final int LEAVES = 160;
    private static final int LEAVES2 = 173;
    private static final int B_EAST = 169;
    private static final int B_NORTH = 170;
    private static final int B_WEST = 171;
    private static final int B_SOUTH = 172;
    private static final int[] BRANCHES = {170, 172, 169, 171};
    private static final int RADIUS = 3;
    private static final int WIDTH = 7;
    private static final int[][][] layers = {{{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 9, 9, 9, 0, 0}, {0, 0, 9, 9, 9, 0, 0}, {0, 0, 9, 9, 9, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 8, 8, 8, 0, 0}, {0, 8, 8, 8, 8, 8, 0}, {0, 8, 8, 8, 8, 8, 0}, {0, 8, 8, 8, 8, 8, 0}, {0, 0, 8, 8, 8, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}, {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 6, 6, 6, 0, 0}, {0, 0, 6, 0, 6, 0, 0}, {0, 0, 6, 6, 6, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}}};

    public ScrubTreeFactory() {
    }

    private void safeSet(int i, int j, int k, int[][][] cells, int value) {
        int v = cells[i][j][k];
        if (!WorldUtils.canGrow(value, v))
            return;
        cells[i][j][k] = value;
    }

    private void addBranch(int i, int j, int k, int[][][] cells, int dir) {
        int x = mythruna.Direction.DIRS[dir][0];
        int y = mythruna.Direction.DIRS[dir][1];
        safeSet(i + x, j + y, k, cells, BRANCHES[dir]);

        safeSet(i + x + x, j + y + y, k, cells, 160);
        safeSet(i + x + x, j + y + y, k + 1, cells, 160);

        safeSet(i + x, j + y, k + 1, cells, 160);
        safeSet(i + x - y, j + y - x, k + 1, cells, 160);
        safeSet(i + x + y, j + y + x, k + 1, cells, 160);
        safeSet(i + x + x - y, j + y + y - x, k + 1, cells, 160);
        safeSet(i + x + x + y, j + y + y + x, k + 1, cells, 160);
    }

    private void addLayer(int[][] layer, int i, int j, int k, int[][][] cells, Random random) {
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                int r = layer[x][y];
                if (r != 0) {
                    if (r == 9) {
                        safeSet(i + x - 3, j + y - 3, k, cells, 160);
                    } else {
                        int chance = random.nextInt(9);
                        if (chance < r)
                            safeSet(i + x - 3, j + y - 3, k, cells, 160);
                    }
                }
            }
        }
    }

    public boolean addTree(int i, int j, int k, int[][][] cells, Random random) {
        if ((i < 3) || (j < 3) || (i >= 29) || (j >= 29)) {
            return false;
        }
        int height = random.nextInt(4) + 8;
        if (k + height >= 160) {
            return false;
        }
        int start = Math.max(k, 0);
        int end = Math.min(k + height, 160);

        for (int z = start; z < end - 1; z++) {
            safeSet(i, j, z, cells, 161);
        }

        int[] heights = {random.nextInt(height - 6) + 3, random.nextInt(height - 6) + 3, random.nextInt(height - 6) + 3, random.nextInt(height - 6) + 3};

        addBranch(i, j, k + heights[0], cells, 0);
        addBranch(i, j, k + heights[1], cells, 1);
        addBranch(i, j, k + heights[2], cells, 2);
        addBranch(i, j, k + heights[3], cells, 3);

        int level = end - 1;
        addLayer(layers[0], i, j, level--, cells, random);
        addLayer(layers[1], i, j, level--, cells, random);
        addLayer(layers[2], i, j, level--, cells, random);

        return false;
    }
}