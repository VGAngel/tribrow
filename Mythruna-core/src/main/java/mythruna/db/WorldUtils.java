package mythruna.db;

import org.progeeks.map.ElevationGenerator;
import org.progeeks.map.FractalSumElevations;
import org.progeeks.map.RidgeFractalElevations;
import org.progeeks.map.SphericalElevations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WorldUtils {

    public static final int ROCK = 4;
    public static final int SAND = 3;
    public static final int GRASS = 2;
    public static final int DIRT = 1;
    public static final int TALL_GRASS = 82;
    public static final int WILD_FLOWERS = 232;
    public static final int FLOWERS1 = 233;
    public static final int FLOWERS2 = 234;
    public static final int FLOWERS3 = 235;
    public static final int BRUSH1 = 236;
    public static final int BRUSH2 = 237;
    private static final int TRUNK = 161;
    private static final int TRUNK2 = 168;
    private static final int LEAVES = 160;
    private static final int LEAVES2 = 173;
    private static final int TOP = 231;
    private static final int BRANCH1 = 169;
    private static final int BRANCH2 = 170;
    private static final int BRANCH3 = 171;
    private static final int BRANCH4 = 172;
    private static final int PINE_BRANCH0 = 223;
    private static final int PINE_BRANCH45 = 227;
    private static final int PINE_BRANCH90 = 224;
    private static final int PINE_BRANCH135 = 228;
    private static final int PINE_BRANCH180 = 225;
    private static final int PINE_BRANCH225 = 229;
    private static final int PINE_BRANCH270 = 226;
    private static final int PINE_BRANCH315 = 230;
    private static final Integer[] BRANCHES = {Integer.valueOf(223), Integer.valueOf(227), Integer.valueOf(224), Integer.valueOf(228), Integer.valueOf(225), Integer.valueOf(229), Integer.valueOf(226), Integer.valueOf(230), Integer.valueOf(169), Integer.valueOf(170), Integer.valueOf(171), Integer.valueOf(172)};

    private static final Integer[] PINE = {Integer.valueOf(223), Integer.valueOf(227), Integer.valueOf(224), Integer.valueOf(228), Integer.valueOf(225), Integer.valueOf(229), Integer.valueOf(226), Integer.valueOf(230), Integer.valueOf(231)};

    private static final Integer[] FOLIAGE = {Integer.valueOf(223), Integer.valueOf(227), Integer.valueOf(224), Integer.valueOf(228), Integer.valueOf(225), Integer.valueOf(229), Integer.valueOf(226), Integer.valueOf(230), Integer.valueOf(160), Integer.valueOf(173), Integer.valueOf(231)};

    private static final Set<Integer> BRANCH_SET = new HashSet(Arrays.asList(BRANCHES));
    private static final Set<Integer> PINE_SET = new HashSet(Arrays.asList(PINE));
    private static final Set<Integer> FOLIAGE_SET = new HashSet(Arrays.asList(FOLIAGE));

    public WorldUtils() {
    }

    public static boolean isFoliage(int type) {
        return FOLIAGE_SET.contains(Integer.valueOf(type));
    }

    public static boolean isLeaves(int type) {
        return (type == 160) || (type == 173);
    }

    public static boolean isBranch(int type) {
        return BRANCH_SET.contains(Integer.valueOf(type));
    }

    public static boolean isTrunk(int type) {
        return (type == 161) || (type == 168);
    }

    public static boolean isGrass(int type) {
        return (type == 82) || (type == 83) || (type == 84);
    }

    public static boolean canGrow(int grow, int replace) {
        if ((replace == 0) || (isGrass(replace))) {
            return true;
        }
        if ((isTrunk(grow)) && ((isFoliage(replace)) || (isBranch(replace)))) {
            return true;
        }

        if ((PINE_SET.contains(Integer.valueOf(grow))) && (isLeaves(replace))) {
            return true;
        }

        if ((isBranch(grow)) && (isLeaves(replace))) {
            return true;
        }

        return false;
    }

    public static int[][] generateTypes(int[][] map, int size, int seaLevel) {
        int[][] result = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int height = map[i][j];

                int total = 0;
                int count = 0;
                int hMax = -2147483648;
                int hMin = 2147483647;

                for (int x = i - 1; x <= i + 1; x++) {
                    for (int y = j - 1; y <= j + 1; y++) {
                        if ((x >= 0) && (y >= 0) && (x < size) && (y < size)) {
                            if ((x != i) || (y != j)) {
                                int val = map[x][y];
                                total += val;
                                count++;

                                if (val > hMax)
                                    hMax = val;
                                if (val < hMin)
                                    hMin = val;
                            }
                        }
                    }
                }
                int average = total / count;

                boolean rocky = false;

                if ((Math.abs(height - average) >= 2) || (hMax - hMin > 4)) {
                    rocky = true;
                }
                if (rocky) {
                    result[i][j] = 4;
                } else if (average <= seaLevel) {
                    result[i][j] = 3;
                } else {
                    result[i][j] = 2;
                }

            }

        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (result[i][j] == 2) {
                    int sandCount = 0;
                    for (int x = i - 1; x <= i + 1; x++) {
                        for (int y = j - 1; y <= j + 1; y++) {
                            if ((x >= 0) && (y >= 0) && (x < size) && (y < size)) {
                                if ((x != i) || (y != j)) {
                                    int val = result[x][y];
                                    if (val == 3)
                                        sandCount++;
                                }
                            }
                        }
                    }
                    if (sandCount >= 5) {
                        result[i][j] = 3;
                    }
                }
            }
        }
        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                if (result[i][j] == 2) {
                    int base = map[i][j];
                    boolean canPlant = true;

                    int x = i - 1;
                    int y = j;
                    if ((map[x][y] < base) || ((map[x][y] == base) && (result[x][y] != 2) && (result[x][y] != 82))) {
                        canPlant = false;
                    }
                    x = i + 1;
                    y = j;
                    if ((map[x][y] < base) || ((map[x][y] == base) && (result[x][y] != 2) && (result[x][y] != 82))) {
                        canPlant = false;
                    }
                    x = i;
                    y = j - 1;
                    if ((map[x][y] < base) || ((map[x][y] == base) && (result[x][y] != 2) && (result[x][y] != 82))) {
                        canPlant = false;
                    }
                    x = i;
                    y = j + 1;
                    if ((map[x][y] < base) || ((map[x][y] == base) && (result[x][y] != 2) && (result[x][y] != 82))) {
                        canPlant = false;
                    }

                    if (canPlant) {
                        result[i][j] = 82;
                    }
                }
            }
        }

        return result;
    }

    public static ColumnFactory createDefaultColumnFactory(LeafFileLocator locator, int seed) {
        int baseFrequency = 256;
        int sphereFrequency = baseFrequency * 4;
        int ridgeScale = 2500;

        if (GeneratorColumnFactory.wideExpanses) {
            ridgeScale = 3500;
            baseFrequency = 512;
            sphereFrequency = 4192;
        }

        SphericalElevations elev1 = new SphericalElevations();
        elev1.setSeed(seed);

        elev1.setFrequency(sphereFrequency);
        elev1.setElevationOffset(-1500);
        elev1.setElevationScale(5000);

        FractalSumElevations elev2 = new FractalSumElevations();
        elev2.setSeed(seed);
        elev2.setAdditive(true);
        elev2.setAffected(false);

        elev2.setFrequency(baseFrequency);
        elev2.setElevationOffset(-1500);
        elev2.setElevationScale(15000);
        elev2.setFrequencyIterations(8);
        elev2.setStartingFrequency(1.0D);

        RidgeFractalElevations elev3 = new RidgeFractalElevations();
        elev3.setSeed(seed);
        elev3.setAdditive(true);
        elev3.setAffected(true);

        elev3.setFrequency(baseFrequency * 2);
        elev3.setElevationOffset(-ridgeScale);
        elev3.setElevationScale(ridgeScale * 2);
        elev3.setFractalIncrement(0.5D);
        elev3.setGain(2.0D);
        elev3.setLacunarity(3.02D);
        elev3.setOctaves(5);
        elev3.setOffset(0.86D);
        elev3.setThreshold(1.5D);

        GeneratorColumnFactory columnFactory = new GeneratorColumnFactory(locator, new ElevationGenerator[]{elev1, elev2, elev3});

        FractalSumElevations rock1 = new FractalSumElevations();
        rock1.setSeed(seed);
        rock1.setFrequency(baseFrequency);
        rock1.setAdditive(true);
        rock1.setAffected(false);
        rock1.setElevationOffset(-1500);
        rock1.setElevationScale(15000);
        rock1.setFrequencyIterations(8);
        rock1.setStartingFrequency(1.0D);

        RidgeFractalElevations rock2 = new RidgeFractalElevations();
        rock2.setSeed(seed);
        rock2.setFrequency(baseFrequency * 2);
        rock2.setAdditive(true);
        rock2.setAffected(true);
        rock2.setElevationOffset(-2500);
        rock2.setElevationScale(5000);
        rock2.setFractalIncrement(0.5D);
        rock2.setGain(2.0D);
        rock2.setLacunarity(3.02D);
        rock2.setOctaves(5);
        rock2.setOffset(0.86D);
        rock2.setThreshold(1.5D);

        columnFactory.setStrata(4, null, 1, new ElevationGenerator[]{rock1, rock2});

        FractalSumElevations minerals2 = new FractalSumElevations();
        minerals2.setSeed(seed);
        minerals2.setAdditive(true);
        minerals2.setAffected(false);
        minerals2.setFrequency(baseFrequency);
        minerals2.setElevationOffset(-2000);
        minerals2.setElevationScale(8000);
        minerals2.setFrequencyIterations(8);
        minerals2.setStartingFrequency(1.0D);

        RidgeFractalElevations minerals3 = new RidgeFractalElevations();
        minerals3.setSeed(seed);
        minerals3.setAdditive(true);
        minerals3.setAffected(false);
        minerals3.setFrequency(baseFrequency * 2);
        minerals3.setElevationOffset(-150);
        minerals3.setElevationScale(325);
        minerals3.setFractalIncrement(0.1D);
        minerals3.setGain(2.0D);
        minerals3.setLacunarity(3.02D);
        minerals3.setOctaves(5);
        minerals3.setOffset(0.7D);
        minerals3.setThreshold(3.0D);

        columnFactory.setStrata(41, minerals2, -4, new ElevationGenerator[]{minerals2, minerals3});

        return columnFactory;
    }
}