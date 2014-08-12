package mythruna.db.cave;

import com.jme3.math.Vector3f;
import mythruna.Coordinates;
import mythruna.db.*;
import mythruna.event.EventDispatcher;
import mythruna.util.LruCache;
import mythruna.util.ReportSystem;
import mythruna.util.Reporter;
import mythruna.util.SerializationUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

public class CaveFilter implements GenerationFilter {

    private static Random random = new Random(0L);
    private LeafFileLocator locator;
    private LruCache<Long, CaveGenerator> generators = new LruCache(2);
    private long baseSeed;
    private GeneratorColumnFactory generator;
    private MemReporter memReporter = new MemReporter();

    public CaveFilter(LeafFileLocator locator, long baseSeed) {
        this.locator = locator;
        this.baseSeed = baseSeed;

        ReportSystem.registerCacheReporter(this.memReporter);
    }

    public void setSeed(long seed) {
        this.baseSeed = seed;
    }

    public void setGenerator(GeneratorColumnFactory generator) {
        this.generator = generator;
    }

    protected File getCaveFile(int x, int y, boolean createPaths) {
        File dir = this.locator.getNodeDirectory(x, y, 0, createPaths);
        if (dir == null)
            return null;
        File caveFile = new File(dir, "caves.bin");
        if ((!caveFile.exists()) && (!createPaths))
            return null;
        return caveFile;
    }

    protected CaveGenerator getGenerator(int x, int y) {
        long id = Coordinates.worldToNodeId(x, y);
        CaveGenerator result = this.generators.get(id);
        if (result != null) {
            return result;
        }
        File existing = getCaveFile(x, y, false);
        if (existing != null) {
            long start = System.nanoTime();
            result = (CaveGenerator) SerializationUtils.readObject(existing, true);
            long end = System.nanoTime();
            System.out.println("))))))) Read " + existing + "  in:" + (end - start / 1000000.0D) + " ms");
            this.generators.put(id, result);
            result.setGenerator(this.generator);
            System.out.println("******** Loading existing generator from:" + existing);
            return result;
        }

        System.out.println("((((((((((((((((((((((((((((((((((((((((((((");
        System.out.println("((((((((((((((((((((((((((((((((((((((((((((");
        System.out.println("Creating new cave generator for:" + id);
        System.out.println("((((((((((((((((((((((((((((((((((((((((((((");
        System.out.println("((((((((((((((((((((((((((((((((((((((((((((");
        long localSeed = this.baseSeed | id;
        int nx = Coordinates.nodeToWorld(Coordinates.worldToNode(x));
        int ny = Coordinates.nodeToWorld(Coordinates.worldToNode(y));

        long start = System.nanoTime();
        result = new CaveGenerator(localSeed, nx, ny, this.generator);

        GeneratorEvent event = new GeneratorEvent(result);
        System.out.println("((((((((((((((((((((((((((((((((((((((((((((");
        System.out.println("((((((((((((((((((((((((((((((((((((((((((((");
        System.out.println("Publishing event:" + event);
        EventDispatcher.getInstance().publishEvent(WorldDatabaseEvents.caveSystemCreated, event);
        System.out.println("((((((((((((((((((((((((((((((((((((((((((((");
        System.out.println("((((((((((((((((((((((((((((((((((((((((((((");

        result.compile();

        long end = System.nanoTime();
        System.out.println("))))))) Built generator in:" + (end - start / 1000000.0D) + " ms");
        this.generators.put(Long.valueOf(id), result);

        start = System.nanoTime();
        SerializationUtils.writeObject(result, getCaveFile(x, y, true), true);
        end = System.nanoTime();
        System.out.println("))))))) Wrote generator in:" + (end - start / 1000000.0D) + " ms");

        return result;
    }

    private boolean isWaterOrAir(int type) {
        return (type == 0) || (type == 8) || (type == 7);
    }

    private boolean isWater(int type) {
        return (type == 8) || (type == 7);
    }

    private int waterOrAir(int height) {
        int seaLevel = 57;
        if (height >= seaLevel)
            return 0;
        if (height == seaLevel - 1)
            return 8;
        return 7;
    }

    public void filter(int xLeaf, int yLeaf, int[][][] cells, int[][] elevations, int[][] types, long seed, int xOffset, int yOffset, int seaLevel) {
        int xBase = xLeaf * 32;
        int yBase = yLeaf * 32;

        long start = System.nanoTime();
        CaveGenerator def = getGenerator(xBase, yBase);

        List<Influencer> local = def.getInfluences(xLeaf, yLeaf);

        Influencer[] current = new Influencer[local.size()];

        int count = 0;
        for (Influencer in : local) {
            current[(count++)] = in;
        }

        System.out.println("Local influencer count:" + count);

        if (count >= 200) {
            System.out.println("Lots of local influencers at leaf:" + xBase + ", " + yBase);
            System.out.println("   Example:" + current[0]);
        }

        long end = System.nanoTime();
        System.out.println("Build local influencers in:" + (end - start / 1000000.0D) + " ms.");
        if (count == 0) {
            return;
        }
        random.setSeed(seed);
        Vector3f p = new Vector3f();

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                p.x = xBase + i;
                p.y = yBase + j;

                int lastType = cells[i][j][0];

                boolean debug = false;
                if ((xBase + i == 491) && (yBase + j == 841))
                    debug = true;
                if (debug) {
                    System.out.println("+++++++++++ debug +++++++++++++++");
                }
                boolean isWet = false;
                for (int k = 1; k < 160; k++) {
                    int type = cells[i][j][k];
                    if (debug)
                        System.out.println("k:" + k + "  type:" + type);
                    if (isWaterOrAir(type)) {
                        lastType = type;
                    } else {
                        p.z = k;

                        float s = 0.0F;
                        for (int t = 0; t < count; t++) {
                            Influencer in = current[t];
                            float st = in.getStrength(p);
                            s += st;
                            if ((st > 0.0F) && (in.isWet())) {
                                isWet = true;
                            }
                        }
                        boolean filled = true;
                        if (s > 0.0F) {
                            if (s > 1.0F) {
                                filled = false;
                            } else {
                                double chance = s;
                                if (random.nextDouble() <= chance) {
                                    filled = false;
                                }
                            }
                        }
                        if (type == 3) {
                            if (debug) {
                                System.out.println("SAND  filled:" + filled + "  wet:" + isWet);
                            }

                            if ((!filled) && (!isWet)) {
                                if (!isWaterOrAir(lastType))
                                    continue;
                                cells[i][j][(k - 1)] = 4;
                                lastType = cells[i][j][k] = 4;
                                continue;
                            }

                            if (isWet) {
                                if (isWaterOrAir(lastType)) {
                                    filled = false;
                                }
                            }
                        }

                        if (!filled) {
                            if (debug)
                                System.out.println("Clear block.");
                            if (isWet) {
                                lastType = cells[i][j][k] = waterOrAir(k);
                            } else {
                                lastType = cells[i][j][k] = 0;
                            }
                        } else {
                            isWet = false;
                        }
                    }

                }

                lastType = cells[i][j][0];
                int lastSolid = 0;
                int lastLastSolid = 0;
                int lastEmpty = -100;
                isWet = false;

                for (int k = 1; k < 160; k++) {
                    int type = cells[i][j][k];
                    if (debug) {
                        System.out.println("k:" + k + " type:" + type);
                    }

                    if ((type == 0) && (k < seaLevel)) {
                        int x;
                        int y;
                        for (int d = 0; d < 4; d++) {
                            x = i + mythruna.Direction.DIRS[d][0];
                            y = j + mythruna.Direction.DIRS[d][1];
                            if ((x >= 0) && (x < 32)) {
                                if ((y >= 0) && (y < 32)) {
                                    int t = cells[x][y][k];
                                    if (debug)
                                        System.out.println("  dir[" + d + "] = " + t);
                                    if ((isWater(t)) || (t == 3)) {
                                        type = cells[i][j][k] = 4;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (isWaterOrAir(type)) {
                        if ((lastEmpty < lastSolid) && (lastSolid - lastEmpty == 1)) {
                            isWet = isWater(cells[i][j][lastEmpty]);

                            if (isWet)
                                cells[i][j][lastSolid] = waterOrAir(lastSolid);
                            else
                                cells[i][j][lastSolid] = 0;
                            lastSolid = lastLastSolid;
                        } else {
                            isWet = type != 0;
                        }
                        lastEmpty = k;
                    } else {
                        lastLastSolid = lastSolid;
                        lastSolid = k;
                    }
                    lastType = type;
                }

                int height = lastSolid + 1;
                if (height < elevations[(xOffset + i)][(yOffset + j)]) {
                    int type = cells[i][j][lastSolid];
                    if ((type == 1) && (!isWater(cells[i][j][(lastSolid + 1)]))) {
                        type = cells[i][j][lastSolid] = 2;
                    }

                    elevations[(xOffset + i)][(yOffset + j)] = height;
                    types[(xOffset + i)][(yOffset + j)] = type;
                }
            }
        }

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                int type = types[(xOffset + i)][(yOffset + j)];
                int height = elevations[(xOffset + i)][(yOffset + j)];

                if ((type == 2) || (type == 82)) {
                    boolean canPlant = true;

                    for (int d = 0; d < 4; d++) {
                        int x = i + mythruna.Direction.DIRS[d][0];
                        int y = j + mythruna.Direction.DIRS[d][1];
                        int h = elevations[(xOffset + x)][(yOffset + y)];
                        int t = types[(xOffset + x)][(yOffset + y)];

                        if (((t != 2) && (t != 82)) || (h < height)) {
                            canPlant = false;
                        }
                    }

                    if (canPlant)
                        types[(xOffset + i)][(yOffset + j)] = 82;
                    else
                        types[(xOffset + i)][(yOffset + j)] = 2;
                }
            }
        }
    }

    public void filterOld(int xLeaf, int yLeaf, int[][][] cells, int[][] elevations, int[][] types, long seed, int xOffset, int yOffset, int seaLevel) {
        int xBase = xLeaf * 32;
        int yBase = yLeaf * 32;

        long start = System.nanoTime();
        CaveGenerator def = getGenerator(xBase, yBase);
        Influencer[] array = def.getCaves();

        Influencer[] current = new Influencer[array.length];

        int count = 0;
        for (Influencer in : array) {
            if (in.canInfluence(xBase, yBase, xBase + 32, yBase + 32)) {
                current[(count++)] = in;
            }
        }
        System.out.println("Local influencer count:" + count);
        long end = System.nanoTime();
        System.out.println("Build local influencers in:" + (end - start / 1000000.0D) + " ms.");
        if (count == 0) {
            return;
        }
        random.setSeed(seed);
        Vector3f p = new Vector3f();

        double radius = 10.0D;
        double radiusSq = radius * radius;
        double roughness = 2.0D;

        boolean makeAirTight = true;

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                p.x = xBase + i;
                p.y = yBase + j;

                int lastLastSolid = 0;
                int lastSolid = 0;
                int lastEmpty = -5;
                int lastLastType = 39;
                int lastType = 39;

                boolean columnChanged = false;

                boolean empty = false;

                for (int k = 1; k < 160; k++) {
                    int type = cells[i][j][k];
                    empty = (type == 0) || (type == 7) || (type == 8);

                    if ((type == 3) && (makeAirTight)) {
                        int replace = 4;
                        if (k <= seaLevel) {
                            int above = cells[i][j][(k + 1)];

                            if ((above == 7) || (above == 8) || (above == 0)) {
                                replace = 3;
                            }

                        }

                        if (replace == 3) {
                            lastLastType = lastType;
                            lastType = replace;
                            lastLastSolid = lastSolid;
                            lastSolid = k;
                            continue;
                        }
                    } else if ((type == 3) && (!makeAirTight)) {
                        if (isWaterOrAir(cells[i][j][(k - 1)])) {
                            type = cells[i][j][k] = waterOrAir(k);
                            empty = true;
                        }
                    } else if ((makeAirTight) && ((type == 7) || (type == 8))) {
                        lastEmpty = k;
                        continue;
                    }

                    p.z = k;

                    boolean filled = true;
                    if (!empty) {
                        float s = 0.0F;

                        for (int t = 0; t < count; t++) {
                            Influencer in = current[t];
                            float st = in.getStrength(p);
                            s += st;
                        }

                        filled = true;
                        if (s > 0.0F) {
                            if (s > 1.0F) {
                                filled = false;
                            } else {
                                double chance = s;
                                if (random.nextDouble() <= chance) {
                                    filled = false;
                                }
                            }
                        }
                    }
                    if (!filled) {
                        if ((makeAirTight) || (k >= seaLevel)) {
                            cells[i][j][k] = 0;
                        } else if (k == seaLevel - 1)
                            cells[i][j][k] = 8;
                        else {
                            cells[i][j][k] = 7;
                        }
                        columnChanged = true;
                        empty = true;
                    } else if ((!makeAirTight) && (k < seaLevel) && (type == 0)) {
                        if (k == seaLevel - 1)
                            cells[i][j][k] = 'Ö';
                        else {
                            cells[i][j][k] = 7;
                        }
                    }
                    if (empty) {
                        if ((lastEmpty < lastSolid) && (lastSolid - lastEmpty < 2)) {
                            if ((makeAirTight) || (lastSolid >= seaLevel)) {
                                cells[i][j][lastSolid] = 0;
                            } else if (lastSolid == seaLevel - 1)
                                cells[i][j][lastSolid] = 8;
                            else {
                                cells[i][j][lastSolid] = 7;
                            }
                            lastSolid = lastLastSolid;
                            lastType = lastLastType;
                        }
                        lastEmpty = k;
                    } else {
                        lastLastSolid = lastSolid;
                        lastSolid = k;
                        lastLastType = lastType;
                        lastType = type;
                    }

                }

                if ((columnChanged) && (makeAirTight)) {
                    int lastSet = 0;
                    int last = -1;
                    for (int k = 1; k < 160; k++) {
                        int v = cells[i][j][k];
                        if ((last == 0) && ((v == 7) || (v == 8) || (v == 3)))
                            cells[i][j][(k - 1)] = 4;
                        last = cells[i][j][(k - 1)];
                        if (v != 0) {
                            lastSolid = k;
                            lastType = v;
                        } else if (k <= seaLevel) {
                            int x;
                            int y;
                            for (int d = 0; d < 4; d++) {
                                x = i + mythruna.Direction.DIRS[d][0];
                                y = j + mythruna.Direction.DIRS[d][1];
                                if ((x >= 0) && (x < 32)) {
                                    if ((y >= 0) && (y < 32)) {
                                        int t = cells[x][y][k];
                                        if ((t == 7) || (t == 8) || (t == 3)) {
                                            cells[i][j][k] = 4;

                                            lastSolid = k;
                                            lastType = 4;
                                            break;
                                        }

                                    }

                                }

                            }

                        }

                    }

                }

                int height = lastSolid + 1;
                if (height < elevations[(xOffset + i)][(yOffset + j)]) {
                    elevations[(xOffset + i)][(yOffset + j)] = height;
                    boolean canPlant = (makeAirTight) || (height >= seaLevel);

                    if ((lastType == 1) && (canPlant)) {
                        lastType = 2;
                        cells[i][j][lastSolid] = 2;
                    }
                    types[(xOffset + i)][(yOffset + j)] = lastType;

                    int x = xOffset + i - 1;
                    int y = yOffset + j;
                    int h = elevations[x][y];
                    int v = types[x][y];

                    if ((h > height) && (v == 82))
                        types[x][y] = 2;
                    else if (h < height)
                        canPlant = false;
                    else if ((h == height) && (v != 2) && (v != 82) && (v != 1)) {
                        canPlant = false;
                    }

                    x = xOffset + i + 1;
                    y = yOffset + j;
                    h = elevations[x][y];
                    v = types[x][y];
                    if ((h > height) && (v == 82))
                        types[x][y] = 2;
                    else if (h < height)
                        canPlant = false;
                    else if ((h == height) && (v != 2) && (v != 82) && (v != 1)) {
                        canPlant = false;
                    }

                    x = xOffset + i;
                    y = yOffset + j - 1;
                    h = elevations[x][y];
                    v = types[x][y];
                    if ((h > height) && (v == 82))
                        types[x][y] = 2;
                    else if (h < height)
                        canPlant = false;
                    else if ((h == height) && (v != 2) && (v != 82) && (v != 1)) {
                        canPlant = false;
                    }

                    x = xOffset + i;
                    y = yOffset + j + 1;
                    h = elevations[x][y];
                    v = types[x][y];
                    if ((h > height) && (v == 82))
                        types[x][y] = 2;
                    else if (h < height)
                        canPlant = false;
                    else if ((h == height) && (v != 2) && (v != 82) && (v != 1)) {
                        canPlant = false;
                    }
                    if ((lastType == 2) && (canPlant))
                        types[(xOffset + i)][(yOffset + j)] = 82;
                }
            }
        }
    }

    private class MemReporter implements Reporter {
        private MemReporter() {
        }

        public void printReport(String type, PrintWriter out) {
            for (CaveGenerator cg : CaveFilter.this.generators)
                cg.printReport(type, out);
        }
    }
}