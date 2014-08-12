package mythruna.db.tree;

import mythruna.db.CellAccess;
import mythruna.db.TreeFactory;
import mythruna.db.WorldUtils;

import java.util.Random;

public class SimpleTreeFactory implements TreeFactory {

    private static final int TRUNK1 = 161;
    private static final int TRUNK2 = 168;
    private static final int LEAVES = 160;
    private static final int LEAVES2 = 173;

    public SimpleTreeFactory() {
    }

    public boolean addTree(int i, int j, int k, int[][][] cells, Random random) {
        int height = random.nextInt(4) + 4;
        if (k + height >= 160) {
            return false;
        }
        int start = Math.max(k, 0);
        int end = Math.min(k + height, 160);

        int trunkType = 161;
        int leafType = 160;
        if (height < 6)
            trunkType = 168;
        if (height < 5) {
            leafType = 173;
        }

        for (int z = start; z < end; z++) {
            int v = cells[i][j][z];
            if (WorldUtils.canGrow(trunkType, v)) {
                cells[i][j][z] = trunkType;
            }
        }

        if ((end < 160) && (cells[i][j][end] == 0)) {
            cells[i][j][end] = leafType;
            cells[i][j][(end - 1)] = leafType;
        }

        if (height < 5) {
            int low = k + Math.max(1, height / 3);
            int w = 1;
            for (int top = end - 1; top >= low; top--) {
                for (int x = i - w; x <= i + w; x++) {
                    for (int y = j - w; y <= j + w; y++) {
                        if ((x != i) || (y != j)) {
                            if (cells[x][y][top] == 0) {
                                cells[x][y][top] = leafType;
                            }
                        }
                    }
                }
            }
        } else {
            int low = k + height * 2 / 3;
            if (trunkType == 161) {
                low--;
            }
            int w = 1;
            for (int top = end; top >= low; top--) {
                if (top == low)
                    w = 1;
                else if (top < end) {
                    w = 2;
                }
                for (int x = i - w; x <= i + w; x++) {
                    for (int y = j - w; y <= j + w; y++) {
                        if ((x != i) || (y != j)) {
                            if ((w != 2) || (((x != i - w) || (y != j - w)) && ((x != i + w) || (y != j - w)) && ((x != i - w) || (y != j + w)) && ((x != i + w) || (y != j + w)))) {
                                if (cells[x][y][top] == 0) {
                                    cells[x][y][top] = leafType;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean addTree(int i, int j, int k, int height, CellAccess world, Random random) {
        if (k + height >= 160) {
            return false;
        }
        int start = Math.max(k, 0);
        int end = Math.min(k + height, 160);

        int trunkType = 161;
        int leafType = 160;
        if (height < 6)
            trunkType = 168;
        if (height < 5) {
            leafType = 173;
        }

        for (int z = start; z < end; z++) {
            int v = world.getCellType(i, j, z);

            if ((v == 0) || (v == 82) || (v == 83) || (v == 84) || (v == 160) || (v == 173)) {
                world.setCellType(i, j, z, trunkType);
            }
        }

        int v = world.getCellType(i, j, end);
        if ((end < 160) && (v == 0)) {
            world.setCellType(i, j, end, leafType);
            world.setCellType(i, j, end - 1, leafType);
        }

        if (height < 5) {
            int low = k + Math.max(1, height / 3);
            int w = 1;
            for (int top = end - 1; top >= low; top--) {
                for (int x = i - w; x <= i + w; x++) {
                    for (int y = j - w; y <= j + w; y++) {
                        if ((x != i) || (y != j)) {
                            if (world.getCellType(x, y, top) == 0) {
                                world.setCellType(x, y, top, leafType);
                            }
                        }
                    }
                }
            }
        } else {
            int low = k + height * 2 / 3;
            if (trunkType == 161) {
                low--;
            }
            int w = 1;
            for (int top = end; top >= low; top--) {
                if (top == low)
                    w = 1;
                else if (top < end) {
                    w = 2;
                }
                for (int x = i - w; x <= i + w; x++) {
                    for (int y = j - w; y <= j + w; y++) {
                        if ((x != i) || (y != j)) {
                            if ((w != 2) || (((x != i - w) || (y != j - w)) && ((x != i + w) || (y != j - w)) && ((x != i - w) || (y != j + w)) && ((x != i + w) || (y != j + w)))) {
                                if (world.getCellType(x, y, top) == 0) {
                                    world.setCellType(x, y, top, leafType);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}