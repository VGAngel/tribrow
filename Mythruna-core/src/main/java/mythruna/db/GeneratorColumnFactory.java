package mythruna.db;

import mythruna.BlockType;
import mythruna.Coordinates;
import mythruna.db.cave.CaveFilter;
import mythruna.db.tree.TreeFilter;
import org.progeeks.map.AbstractNoiseElevationGenerator;
import org.progeeks.map.ElevationData;
import org.progeeks.map.ElevationGenerator;

import java.util.ArrayList;
import java.util.List;

public class GeneratorColumnFactory implements ColumnFactory {

    public static boolean wideExpanses = false;

    private int seed = 0;

    private int maxElevation = 128;

    private int seaLevel = 57;
    private int min = -15000;
    private int max = 18556;
    private ElevationGenerator[] generators;
    private ElevationData baseData = new ElevationData(34);
    private ElevationData top;
    private List<Strata> strata = new ArrayList();
    private GenerationFilter pre;
    private GenerationFilter trees = new TreeFilter();

    public GeneratorColumnFactory(LeafFileLocator locator, ElevationGenerator[] generators) {
        if (wideExpanses)
            this.maxElevation = 150;
        else {
            this.maxElevation = 128;
        }
        this.generators = generators;
        this.pre = new CaveFilter(locator, 0L);
        this.pre.setGenerator(this);
        this.trees.setGenerator(this);
    }

    public void setSeed(int seed) {
        for (ElevationGenerator g : this.generators)
            g.setSeed(seed);
        for (Strata s : this.strata)
            s.setSeed(seed);
        this.pre.setSeed(seed);
    }

    public void setStrata(int type, ElevationGenerator[] generators) {
        this.strata.add(new Strata(type, null, 0, generators));
    }

    public void setStrata(int type, ElevationGenerator base, int replace, ElevationGenerator[] generators) {
        this.strata.add(new Strata(type, base, replace, generators));
    }

    protected void invalidateGenerators() {
        this.top = this.baseData;
        for (ElevationGenerator g : this.generators) {
            g.setSourceData(this.top);
            g.invalidate();
            this.top = g.getGeneratedData();
        }
    }

    public int calculateElevation(int x, int y) {
        int z = 0;
        for (ElevationGenerator g : this.generators) {
            if ((g instanceof AbstractNoiseElevationGenerator)) {
                z = ((AbstractNoiseElevationGenerator) g).calculateElevation(y, x, z);
            }
        }

        int denom = (this.max - this.min) / this.maxElevation;

        z -= this.min;
        z /= denom;

        return z;
    }

    public synchronized LeafData[] createLeafs(int x, int y) {
        long start = System.nanoTime();
        int x2 = Coordinates.worldToLeaf(x);
        int y2 = Coordinates.worldToLeaf(y);

        int xBase = x2 * 32;
        int yBase = y2 * 32;

        this.baseData.setElevationOffset(0);
        this.baseData.setElevationScale(1);
        this.baseData.setBaseX(yBase);
        this.baseData.setBaseY(xBase);

        invalidateGenerators();

        int width = 34;

        int[][] elevations = this.top.getElevations();

        int denom = (this.max - this.min) / this.maxElevation;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                int h = elevations[i][j];
                h -= this.min;
                h /= denom;
                elevations[i][j] = h;
            }

        }

        int[][] types = WorldUtils.generateTypes(elevations, width, this.seaLevel);

        long seed = Coordinates.worldToColumnId(xBase, yBase);

        int[][][] cells = new int[32][32][' '];
        LeafUtils.generateColumnTypes(cells, elevations, types, seed, 1, 1, this.seaLevel);

        this.pre.filter(x2, y2, cells, elevations, types, seed, 1, 1, this.seaLevel);

        this.trees.filter(x2, y2, cells, elevations, types, seed, 1, 1, this.seaLevel);

        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                int v = types[i][j];
                if (v == 2) {
                    int min = 2147483647;
                    for (int d = 0; d < 4; d++) {
                        int i2 = i + mythruna.Direction.DIRS[d][0];
                        int j2 = j + mythruna.Direction.DIRS[d][1];
                        int v2 = types[i2][j2];
                        if (v2 == 4) {
                            min = Math.min(min, elevations[i2][j2]);
                        }
                    }
                    if (min != 2147483647) {
                        min -= 3;

                        if (min >= 0) {
                            min = Math.min(128, min);

                            for (int k = 0; k < min; k++) {
                                int t = cells[(i - 1)][(j - 1)][k];
                                if ((t != 0) && (t != 7) && (t != 8))
                                    cells[(i - 1)][(j - 1)][k] = 4;
                            }
                        }
                    }
                }
            }
        }
        for (Strata s : this.strata) {
            applyStrata(cells, s, xBase, yBase);
        }
        LeafData[] leafs = new LeafData[5];
        for (int i = 0; i < leafs.length; i++) {
            LeafInfo info = new LeafInfo();
            info.x = xBase;
            info.y = yBase;
            info.z = (i * 32);
            leafs[i] = new LeafData(info);
            copyLeafData(leafs[i], info.z, cells);
        }

        long end = System.nanoTime();
        System.out.println("Created leafs in:" + (end - start / 1000000.0D) + " ms");

        return leafs;
    }

    protected void copyLeafData(LeafData leaf, int zBase, int[][][] cells) {
        int[][][] leafCells = leaf.getCells();
        if (leafCells == null) {
            leafCells = new int[32][32][32];
            leaf.setCells(leafCells);
        }

        LeafInfo info = leaf.getInfo();
        info.emptyCells = 32768;
        info.solidCells = 0;

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 32; k++) {
                    int v = cells[i][j][(k + zBase)];
                    leafCells[i][j][k] = v;

                    BlockType type = mythruna.BlockTypeIndex.types[v];
                    if (type != null) {
                        if (type.isSolid())
                            info.solidCells += 1;
                        info.emptyCells -= 1;
                    }
                }
            }
        }
    }

    protected void applyStrata(int[][][] cells, Strata s, int xBase, int yBase) {
        ElevationData[] data = s.getData(xBase, yBase);
        int[][] base = data[0] != null ? data[0].getElevations() : (int[][]) null;
        int[][] elevations = data[1].getElevations();

        int denom = (this.max - this.min) / this.maxElevation;
        int width = 34;

        int replace = s.replace;

        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                int h = elevations[i][j];
                h -= this.min;
                h /= denom;

                int bottom = base == null ? this.min : base[i][j];
                bottom = (bottom - this.min) / denom;

                h = Math.min(128, h);

                for (int k = 0; k < h; k++) {
                    if (k >= bottom) {
                        int v = cells[(i - 1)][(j - 1)][k];
                        if ((replace <= 0) || (v == replace)) {
                            if ((replace >= 0) || (v <= -replace)) {
                                if ((v != 0) && (v != 7) && (v != 8))
                                    cells[(i - 1)][(j - 1)][k] = s.type;
                            }
                        }
                    }
                }
            }
        }
    }

    private class Strata {
        private int type;
        private int replace;
        private ElevationGenerator base;
        private ElevationGenerator[] generators;

        public Strata(int type, ElevationGenerator base, int replace, ElevationGenerator[] generators) {
            this.type = type;
            this.base = base;
            this.replace = replace;
            this.generators = generators;
        }

        public void setSeed(int seed) {
            if (this.base != null) {
                this.base.setSeed(seed);
            }
            for (ElevationGenerator g : this.generators)
                g.setSeed(seed);
        }

        public ElevationData[] getData(int xBase, int yBase) {
            ElevationData[] result = new ElevationData[2];
            ElevationData top = new ElevationData(34);
            top.setElevationOffset(0);
            top.setElevationScale(1);
            top.setBaseX(yBase);
            top.setBaseY(xBase);

            for (ElevationGenerator g : this.generators) {
                g.setSourceData(top);
                g.invalidate();
                top = g.getGeneratedData();
            }

            result[0] = (this.base != null ? this.base.getGeneratedData() : null);
            result[1] = top;
            return result;
        }
    }
}