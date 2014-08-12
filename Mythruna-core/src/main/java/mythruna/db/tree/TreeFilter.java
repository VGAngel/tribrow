package mythruna.db.tree;

import mythruna.BlockType;
import mythruna.db.GenerationFilter;
import mythruna.db.GeneratorColumnFactory;
import mythruna.db.TreeFactory;

import java.util.Random;

public class TreeFilter implements GenerationFilter {

    private static Random random = new Random(0L);
    private static Random random2 = new Random(0L);

    private static final TreeFactory SIMPLE = new SimpleTreeFactory();
    private static final TreeFactory MAPLE = new MapleTreeFactory();
    private static final TreeFactory SCRUB = new ScrubTreeFactory();
    private static final TreeFactory PINE = new PineTreeFactory();
    private static final TreeFactory SMALL_PINE = new PineTreeFactory(0, 3, 4, 3);
    private static final TreeFactory SCRUB_PINE = new PineTreeFactory(4, 3, 3, 6);

    private static TreeFactory[] treeFactories = {SIMPLE, SIMPLE, SIMPLE, SIMPLE, SIMPLE, SIMPLE, SIMPLE, SIMPLE, MAPLE, MAPLE, MAPLE, MAPLE, SCRUB, SCRUB, SMALL_PINE, SMALL_PINE, SCRUB_PINE};

    private static TreeFactory[] rockyFactories = {SIMPLE, SIMPLE, SCRUB, SCRUB, SCRUB, SCRUB_PINE, SCRUB_PINE, SCRUB_PINE, PINE, PINE, PINE, PINE, PINE, PINE, PINE, PINE, PINE};

    private static TreeFactory[] highFactories = {SIMPLE, SIMPLE, SIMPLE, SIMPLE, SCRUB_PINE, PINE, PINE, PINE, PINE, PINE, MAPLE, MAPLE, SCRUB, SCRUB, PINE, PINE, PINE};

    public TreeFilter() {
    }

    public void setSeed(long seed) {
    }

    public void setGenerator(GeneratorColumnFactory generator) {
    }

    public void filter(int xLeaf, int yLeaf, int[][][] cells, int[][] elevations, int[][] types, long seed, int xOffset, int yOffset, int seaLevel) {
        BlockType trunk = mythruna.BlockTypeIndex.types[9];
        BlockType branches = mythruna.BlockTypeIndex.types[10];

        random.setSeed(seed);
        random2.setSeed(seed);

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                if ((types[(xOffset + i)][(yOffset + j)] == 2) || (types[(xOffset + i)][(yOffset + j)] == 82)) {
                    int h = elevations[(xOffset + i)][(yOffset + j)];

                    boolean canHaveTree = true;
                    if ((i < 2) || (j < 2) || (i >= 30) || (j >= 30)) {
                        canHaveTree = false;
                    }
                    boolean hasGrass = types[(xOffset + i)][(yOffset + j)] == 82;

                    if (canHaveTree) {
                        boolean rocky = false;
                        for (int d = 0; d < 4; d++) {
                            int ni = i + mythruna.Direction.DIRS[d][0];
                            int nj = j + mythruna.Direction.DIRS[d][1];
                            if (types[(xOffset + ni)][(yOffset + nj)] == 4) {
                                rocky = true;
                                break;
                            }
                        }

                        int chance = (int) (random.nextDouble() * 500.0D);
                        if (rocky) {
                            if (chance < rockyFactories.length) {
                                TreeFactory tree = rockyFactories[chance];

                                if (tree != null) {
                                    tree.addTree(i, j, h, cells, random);
                                    hasGrass = false;
                                }
                            }
                        } else if (h > 96) {
                            if (chance < highFactories.length) {
                                TreeFactory tree = highFactories[chance];
                                if (tree != null) {
                                    tree.addTree(i, j, h, cells, random);
                                    hasGrass = false;
                                }

                            }

                        } else if (chance < treeFactories.length) {
                            TreeFactory tree = treeFactories[chance];
                            if (tree != null) {
                                tree.addTree(i, j, h, cells, random);
                                hasGrass = false;
                            }
                        }

                    }

                    if (hasGrass) {
                        double rand = random2.nextDouble() * 100.0D;
                        if (rand < 2.0D)
                            cells[i][j][h] = 'í';
                        else if (rand < 30.0D)
                            cells[i][j][h] = 82;
                        else if (rand < 40.0D)
                            cells[i][j][h] = 83;
                        else if (rand < 50.0D)
                            cells[i][j][h] = 84;
                        else if (rand < 52.0D)
                            cells[i][j][h] = 'è';
                        else if (rand < 53.0D)
                            cells[i][j][h] = 'é';
                        else if (rand < 54.0D)
                            cells[i][j][h] = 'ê';
                        else if (rand < 55.0D)
                            cells[i][j][h] = 'ë';
                        else if (rand < 56.0D)
                            cells[i][j][h] = 'ì';
                    }
                }
            }
        }
    }
}