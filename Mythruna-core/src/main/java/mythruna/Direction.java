package mythruna;

public class Direction {
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;
    public static final int UP = 4;
    public static final int DOWN = 5;
    public static final int DIR_COUNT = 6;
    public static final int[][] DIRS = {{0, -1, 0}, {0, 1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};

    public static final Vector3i[] VECS = {new Vector3i(DIRS[0][0], DIRS[0][1], DIRS[0][2]), new Vector3i(DIRS[1][0], DIRS[1][1], DIRS[1][2]), new Vector3i(DIRS[2][0], DIRS[2][1], DIRS[2][2]), new Vector3i(DIRS[3][0], DIRS[3][1], DIRS[3][2]), new Vector3i(DIRS[4][0], DIRS[4][1], DIRS[4][2]), new Vector3i(DIRS[5][0], DIRS[5][1], DIRS[5][2])};

    public static final Vector3i[] AXIS_VECS = {new Vector3i(1, 0, 0), new Vector3i(0, 1, 0), new Vector3i(0, 0, 1)};
    public static final int NORTH_MASK = 1;
    public static final int SOUTH_MASK = 2;
    public static final int EAST_MASK = 4;
    public static final int WEST_MASK = 8;
    public static final int UP_MASK = 16;
    public static final int DOWN_MASK = 32;
    public static final int[] MASKS = {1, 2, 4, 8, 16, 32};

    public static final int[] INVERSE = {1, 0, 3, 2, 5, 4};
    public static final int[] LEFT = {3, 2, 0, 1, 4, 5};
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;
    public static final int AXIS_COUNT = 3;
    public static final int[] DIR_AXIS = {1, 1, 0, 0, 2, 2};
    public static final int[] DIR_SIGN = {-1, 1, 1, -1, 1, -1};

    public static final float[][] NORMALS = {{0.0F, 0.0F, -1.0F}, {0.0F, 0.0F, 1.0F}, {1.0F, 0.0F, 0.0F}, {-1.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, -1.0F, 0.0F}};

    public static final float[][] BINORMALS = {{0.0F, 1.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, 0.0F, 1.0F}, {0.0F, 0.0F, -1.0F}};

    public static final float[][] TANGENTS = {{-1.0F, 0.0F, 0.0F}, {1.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -1.0F}, {0.0F, 0.0F, 1.0F}, {1.0F, 0.0F, 0.0F}, {1.0F, 0.0F, 0.0F}};

    public static final int[] CARDINAL_TO_DIR = {0, 2, 1, 3, 4, 5};

    public static final int[] DIR_TO_CARDINAL = {0, 2, 1, 3, 4, 5};

    public Direction() {
    }

    public static final int rotate(int dir, int dirDelta) {
        if ((dir == 4) || (dir == 5))
            return dir;
        if (dir < 0) {
            return dir;
        }
        int compass = DIR_TO_CARDINAL[dir];

        int count = dirDelta;
        if (count == 0)
            return dir;
        compass += count;
        while (compass < 0)
            compass += 4;
        while (compass >= 4)
            compass -= 4;
        return CARDINAL_TO_DIR[compass];
    }

    public static final boolean hasNorth(int mask) {
        return (mask & 0x1) != 0;
    }

    public static final boolean hasSouth(int mask) {
        return (mask & 0x2) != 0;
    }

    public static final boolean hasEast(int mask) {
        return (mask & 0x4) != 0;
    }

    public static final boolean hasWest(int mask) {
        return (mask & 0x8) != 0;
    }

    public static final boolean hasUp(int mask) {
        return (mask & 0x10) != 0;
    }

    public static final boolean hasDown(int mask) {
        return (mask & 0x20) != 0;
    }
}