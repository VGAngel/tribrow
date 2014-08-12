package mythruna.db.cave;

import com.jme3.math.LineSegment;
import com.jme3.math.Vector3f;
import mythruna.Coordinates;
import mythruna.db.GeneratorColumnFactory;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

public class CaveGenerator
        implements Serializable {
    static final long serialVersionUID = 42L;
    private static final int MARGIN = 12;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;
    private static final int seaLevel = 57;
    private Random random = new Random();
    private transient List<Influencer> list;
    private Influencer[] caves;
    private float xBase;
    private float yBase;
    private float cavernous;
    private transient GeneratorColumnFactory generator;
    private Map<Long, List<Influencer>> influenceMap = new HashMap<Long, List<Influencer>>();
    private Map<Influencer, List<Long>> idMap = new HashMap<Influencer, List<Long>>();

    public CaveGenerator(long seed, float xBase, float yBase, GeneratorColumnFactory generator) {
        long start = System.nanoTime();

        this.generator = generator;
        System.out.println(new StringBuilder().append("New cave generator:").append(xBase).append(", ").append(yBase).append("  seed:").append(seed).toString());
        this.xBase = xBase;
        this.yBase = yBase;

        this.random.setSeed(seed);

        int count = this.random.nextInt(4) + 3;

        System.out.println(new StringBuilder().append("Total number of cave systems:").append(count).append("   out of:").append(6).toString());
        this.cavernous = ((float) this.random.nextDouble() * 1.0F + 2.0F);

        System.out.println(new StringBuilder().append("----------------------------Cavernous:").append(this.cavernous).toString());
        this.list = new ArrayList<Influencer>();

        for (int i = 0; i < count; i++) {
            float x = (float) this.random.nextDouble() * 1000.0F + 12.0F;
            float y = (float) this.random.nextDouble() * 1000.0F + 12.0F;

            int elev = generator.calculateElevation((int) (x + xBase), (int) (y + yBase));

            float z = (float) this.random.nextDouble() * elev + 12;
            System.out.println(new StringBuilder().append("Fractal Z:").append(elev).append("  random z:").append(z).toString());
            addCaves(new Vector3f(x + xBase, y + yBase, z), null, 12.0D, this.list, 0);
        }

        int caveCount = this.list.size();
        List<Influencer> tempCaves = new ArrayList<Influencer>(this.list);

        int retryCount = 50;

        count = this.random.nextInt(5) + 2;

        for (int i = 0; i < count; i++) {
            double chance = this.random.nextDouble();
            float x;
            float y;
            float z;
            if (chance < 0.4D) {
                int index = this.random.nextInt(caveCount);
                Vector3f pos = (tempCaves.get(index)).getCenter();
                System.out.println(new StringBuilder().append("Reusing cave position:").append(pos).append("  from index:").append(index).toString());
                x = pos.x;
                y = pos.y;
                int elev = generator.calculateElevation((int) x, (int) y);
                System.out.println(new StringBuilder().append("Elevation at:").append(x).append(", ").append(y).append(" = ").append(elev).toString());
                if (elev < 57) {
                    if (retryCount-- > 0) {
                        System.out.println("Under water... try again.");
                        i--;
                        continue;
                    }

                }

                tempCaves.remove(index);
                caveCount--;

                z = pos.z + (float) (this.random.nextDouble() * 12.0D * 2.0D - 12.0D);
            } else {
                x = xBase + (float) this.random.nextDouble() * 1000.0F + 12.0F;
                y = yBase + (float) this.random.nextDouble() * 1000.0F + 12.0F;
                int elev = generator.calculateElevation((int) x, (int) y);
                System.out.println(new StringBuilder().append("Elevation at:").append(x).append(", ").append(y).append(" = ").append(elev).toString());
                if (elev < 57) {
                    if (retryCount-- > 0) {
                        System.out.println("Under water... try again.");
                        i--;
                        continue;
                    }
                }

                z = (float) this.random.nextDouble() * elev - 16 + 16.0F;
            }

            double chance2 = this.random.nextDouble();
            boolean breachSurface = chance2 < 0.5D;
            System.out.println(new StringBuilder().append("Force to surface:").append(breachSurface).toString());

            addGorge(new Vector3f(x, y, z), breachSurface, null, 40.0D, this.list, 0);
        }

        retryCount += 20;

        if (this.list.size() < 500) {
            count = this.random.nextInt(3) + 3;

            System.out.println(new StringBuilder().append("Total number of secondary cave systems:").append(count).append("   out of:").append(5).toString());
            for (int i = 0; i < count; i++) {
                float x = (float) this.random.nextDouble() * 1000.0F + 12.0F + xBase;
                float y = (float) this.random.nextDouble() * 1000.0F + 12.0F + yBase;

                int elev = generator.calculateElevation((int) x, (int) y);
                System.out.println(new StringBuilder().append("Elevation at:").append(x).append(", ").append(y).append(" = ").append(elev).toString());
                if (elev < 57) {
                    if (retryCount-- > 0) {
                        System.out.println("Under water... try again.");
                        i--;
                        continue;
                    }

                }

                float z = (float) this.random.nextDouble() * elev + 12;
                System.out.println(new StringBuilder().append("Fractal Z:").append(elev).append("  random z:").append(z).toString());
                addCaves(new Vector3f(x, y, z), null, 12.0D, this.list, 0);
            }
        }
        long end = System.nanoTime();
        System.out.println(new StringBuilder().append("Created CaveGenerators in:").append(end - start / 1000000.0D).append(" ms").toString());
    }

    public boolean containsWorldCoordinate(float x, float y) {
        if ((x < this.xBase) || (y < this.yBase))
            return false;
        if ((x - this.xBase >= 1024.0F) || (y - this.yBase >= 1024.0F))
            return false;
        return true;
    }

    public PointInfluencer addPoint(float x, float y, float z, float radius, float strength) {
        return (PointInfluencer) addInfluencer(new PointInfluencer(x, y, z, radius, strength));
    }

    public PointInfluencer addPoint(float x, float y, float radius, float strength) {
        int elev = this.generator.calculateElevation((int) (x + this.xBase), (int) (y + this.yBase));
        return addPoint(x, y, elev, radius, strength);
    }

    public PointInfluencer addPoint(float x, float y) {
        return addPoint(x, y, 5.0F, 5.0F);
    }

    public PointInfluencer addPoint(float x, float y, float z) {
        return addPoint(x, y, z, 5.0F, 5.0F);
    }

    public LineInfluencer addLine(float x1, float y1, float z1, float x2, float y2, float z2) {
        return (LineInfluencer) addInfluencer(new LineInfluencer(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), 5.0F, 5.0F));
    }

    public WallInfluencer addWall(float x1, float y1, float z1, float x2, float y2, float z2) {
        return (WallInfluencer) addInfluencer(new WallInfluencer(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), 5.0F, 5.0F, 5.0F));
    }

    public <T extends Influencer> T addInfluencer(T in) {
        if (this.list == null)
            throw new RuntimeException("Cave system has already been compiled.");
        this.list.add(in);
        return in;
    }

    public void compile() {
        long start = System.nanoTime();
        spatiallyIndex(this.list);

        propagateWater(this.list);

        this.caves = ((Influencer[]) this.list.toArray(new Influencer[this.list.size()]));
        this.list = null;

        System.out.println(new StringBuilder().append("total number of influencers:").append(this.caves.length).toString());

        long end = System.nanoTime();
        System.out.println(new StringBuilder().append("Compiled CaveGenerators in:").append(end - start / 1000000.0D).append(" ms").toString());
    }

    public void setGenerator(GeneratorColumnFactory generator) {
        this.generator = generator;
    }

    protected void printReport(String type, PrintWriter out) {
        out.println(new StringBuilder().append("CaveGenerator[").append(this.xBase).append(", ").append(this.yBase).append("] Influence entries:").append(this.influenceMap.size()).append("  id entries:").append(this.idMap.size()).toString());

        int infListSize = 0;
        int walls = 0;
        int lines = 0;
        int points = 0;
        for (List<Influencer> l : this.influenceMap.values()) {
            infListSize += l.size();
            for (Influencer i : l) {
                if ((i instanceof WallInfluencer))
                    walls++;
                else if ((i instanceof LineInfluencer))
                    lines++;
                else if ((i instanceof PointInfluencer)) {
                    points++;
                }
            }
        }
        int idListSize = 0;
        for (List<Long> l : this.idMap.values()) {
            idListSize += l.size();
        }
        out.println(new StringBuilder().append("   total:").append(infListSize).append(" walls:").append(walls).append(" lines:").append(lines).append(" points:").append(points).append("  IDs:").append(idListSize).toString());
    }

    protected void propagateWater(List<Influencer> list) {
        List<Influencer> wet = new ArrayList<Influencer>();

        for (Influencer in : list) {
            if (in.isWet()) {
                wet.add(in);
            }
        }
        for (int i = 0; i < wet.size(); i++) {
            Influencer w = wet.get(i);

            propagateWater(w, list, wet);
        }
    }

    protected void propagateWater(Influencer w, List<Influencer> all, List<Influencer> wet) {
        Set<Influencer> affected = new HashSet<>();
        for (Long id : getIds(w)) {
            affected.addAll(getInfluences(id.longValue()));
        }

        for (Influencer a : affected) {
            if (!a.isWet()) {
                if (intersects(w, a)) {
                    a.setWet(true);
                    wet.add(a);
                }
            }
        }
    }

    protected boolean intersects(Influencer a, Influencer b) {
        if ((a instanceof PointInfluencer)) {
            if ((b instanceof PointInfluencer)) {
                return intersectsPointPoint((PointInfluencer) a, (PointInfluencer) b);
            }
            if ((b instanceof LineInfluencer)) {
                return intersectsPointLine((PointInfluencer) a, (LineInfluencer) b);
            }
            if ((b instanceof WallInfluencer)) {
                return intersectsPointWall((PointInfluencer) a, (WallInfluencer) b);
            }
        } else if ((a instanceof LineInfluencer)) {
            if ((b instanceof PointInfluencer)) {
                return intersectsPointLine((PointInfluencer) b, (LineInfluencer) a);
            }
            if ((b instanceof LineInfluencer)) {
                return intersectsLineLine((LineInfluencer) a, (LineInfluencer) b);
            }
            if ((b instanceof WallInfluencer)) {
                return intersectsLineWall((LineInfluencer) a, (WallInfluencer) b);
            }
        } else if ((a instanceof WallInfluencer)) {
            if ((b instanceof PointInfluencer)) {
                return intersectsPointWall((PointInfluencer) b, (WallInfluencer) a);
            }
            if ((b instanceof LineInfluencer)) {
                return intersectsLineWall((LineInfluencer) b, (WallInfluencer) a);
            }
            if ((b instanceof WallInfluencer)) {
                return intersectsWallWall((WallInfluencer) a, (WallInfluencer) b);
            }
        }
        return true;
    }

    protected boolean intersectsPointPoint(PointInfluencer a, PointInfluencer b) {
        float z = a.getCenter().z;
        if (z - a.getRadius() > 57.0F)
            return false;
        z = b.getCenter().z;
        if (z - b.getRadius() > 57.0F) {
            return false;
        }
        float range = a.getRadius() + b.getRadius();
        range *= range;
        Vector3f dir = b.getCenter().subtract(a.getCenter());
        return dir.lengthSquared() <= range;
    }

    protected boolean intersectsPointLine(PointInfluencer a, LineInfluencer b) {
        float z = a.getCenter().z;
        if (z - a.getRadius() > 57.0F) {
            return false;
        }
        float range = a.getRadius() + b.getRadius();
        range *= range;
        float distSq = b.getDistanceSq(a.getCenter());

        return distSq <= range;
    }

    protected boolean intersectsLineLine(LineInfluencer a, LineInfluencer b) {
        if (!a.intersects(b)) {
            return false;
        }
        float range = a.getRadius() + b.getRadius();
        range *= range;

        LineInfluencer wet = a.isWet() ? a : b;
        LineInfluencer dry = wet == a ? b : a;

        Vector3f start = dry.getStart();
        Vector3f end = dry.getEnd();

        int xStart = (int) start.x;
        int yStart = (int) start.y;
        int zStart = (int) start.z;
        float xDelta = (int) end.x - xStart;
        float yDelta = (int) end.y - yStart;
        float zDelta = (int) end.z - zStart;
        int count;
        if (Math.abs(xDelta) > Math.abs(yDelta)) {
            count = (int) Math.abs(xDelta);
            xDelta /= count;
            yDelta /= count;
            zDelta /= count;
        } else {
            count = (int) Math.abs(yDelta);
            xDelta /= count;
            yDelta /= count;
            zDelta /= count;
        }

        Vector3f test = new Vector3f();
        for (int i = 0; i < count; i++) {
            test.x = (xStart + xDelta * i);
            test.y = (yStart + yDelta * i);
            test.z = (zStart + zDelta * i);

            float ds = wet.getDistanceSq(test);
            float waterRelative = test.z - dry.getRadius() - 57.0F;

            if (ds <= range) {
                if (waterRelative < 0.0F) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean intersectsPointWall(PointInfluencer a, WallInfluencer b) {
        float z = a.getCenter().z;
        if (z - a.getRadius() > 57.0F) {
            return false;
        }
        float range = a.getRadius() + b.getRadius();
        range *= range;
        float distSq = b.getDistanceSq(a.getCenter());

        return distSq <= range;
    }

    protected boolean intersectsLineWall(LineInfluencer a, WallInfluencer b) {
        if (a.getMax().z < b.getMin().z)
            return false;
        if (a.getMin().z > b.getMax().z) {
            return false;
        }

        float range = a.getRadius() + b.getRadius();
        range *= range;
        LineSegment aLine = a.getLine2D();
        LineSegment bLine = b.getLine2D();
        float distSq = aLine.distanceSquared(bLine);
        if (distSq > range) {
            return false;
        }

        Vector3f start = a.getStart();
        Vector3f end = a.getEnd();

        int xStart = (int) start.x;
        int yStart = (int) start.y;
        int zStart = (int) start.z;
        float xDelta = (int) end.x - xStart;
        float yDelta = (int) end.y - yStart;
        float zDelta = (int) end.z - zStart;
        int count;
        if (Math.abs(xDelta) > Math.abs(yDelta)) {
            count = (int) Math.abs(xDelta);
            xDelta /= count;
            yDelta /= count;
            zDelta /= count;
        } else {
            count = (int) Math.abs(yDelta);
            xDelta /= count;
            yDelta /= count;
            zDelta /= count;
        }

        Vector3f test = new Vector3f();
        for (int i = 0; i < count; i++) {
            test.x = (xStart + xDelta * i);
            test.y = (yStart + yDelta * i);
            test.z = (zStart + zDelta * i);

            float ds = b.getDistanceSq(test);
            float waterRelative = test.z - a.getRadius() - 57.0F;

            if (ds <= range) {
                if (waterRelative < 0.0F) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean intersectsWallWall(WallInfluencer a, WallInfluencer b) {
        if (a.getMax().z < b.getMin().z)
            return false;
        if (a.getMin().z > b.getMax().z) {
            return false;
        }

        float range = a.getRadius() + b.getRadius();
        range *= range;
        LineSegment aLine = a.getLine2D();
        LineSegment bLine = b.getLine2D();
        float distSq = aLine.distanceSquared(bLine);
        if (distSq > range) {
            return false;
        }
        return true;
    }

    public List<Influencer> getInfluences(int xLeaf, int yLeaf) {
        long id = Coordinates.leafToColumnId(xLeaf, yLeaf);
        return getInfluences(id);
    }

    protected List<Influencer> getInfluences(long id) {
        List<Influencer> list = (List<Influencer>) this.influenceMap.get(Long.valueOf(id));
        if (list == null)
            return Collections.EMPTY_LIST;
        return list;
    }

    protected List<Influencer> getList(int xLeaf, int yLeaf) {
        long id = Coordinates.leafToColumnId(xLeaf, yLeaf);

        List<Influencer> list = (List<Influencer>) this.influenceMap.get(Long.valueOf(id));
        if (list == null) {
            list = new ArrayList<Influencer>();
            this.influenceMap.put(Long.valueOf(id), list);
        }
        return list;
    }

    protected List<Long> getIds(Influencer in) {
        List<Long> ids = (List<Long>) this.idMap.get(in);
        if (ids == null) {
            ids = new ArrayList<Long>();
            this.idMap.put(in, ids);
        }
        return ids;
    }

    protected void spatiallyIndex(Influencer in) {
        Vector3f min = in.getMin();
        int xMin = Coordinates.worldToLeaf(min.x);
        int yMin = Coordinates.worldToLeaf(min.y);

        Vector3f max = in.getMax();
        int xMax = Coordinates.worldToLeaf(max.x);
        int yMax = Coordinates.worldToLeaf(max.y);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                getList(x, y).add(in);
                long id = Coordinates.leafToColumnId(x, y);
                getIds(in).add(Long.valueOf(id));
            }
        }
    }

    protected void spatiallyIndex(List<Influencer> list) {
        for (Influencer i : list)
            spatiallyIndex(i);
    }

    protected boolean isWet(float x, float y, float z, float height, float radius) {
        int elev = this.generator.calculateElevation((int) x, (int) y);

        if (elev <= 57) {
            if ((z + height + radius >= elev) && (z - radius <= 57.0F))
                return true;
        }
        return false;
    }

    protected boolean isWet(Vector3f start, Vector3f end, float height, float radius) {
        int xStart = (int) start.x;
        int yStart = (int) start.y;
        int zStart = (int) start.z;
        float xDelta = (int) end.x - xStart;
        float yDelta = (int) end.y - yStart;
        float zDelta = (int) end.z - zStart;
        int count;
        if (Math.abs(xDelta) > Math.abs(yDelta)) {
            count = (int) Math.abs(xDelta);
            xDelta /= count;
            yDelta /= count;
            zDelta /= count;
        } else {
            count = (int) Math.abs(yDelta);
            xDelta /= count;
            yDelta /= count;
            zDelta /= count;
        }

        for (int i = 0; i < count; i++) {
            float x = xStart + xDelta * i;
            float y = yStart + yDelta * i;
            float z = zStart + zDelta * i;

            if (isWet(x, y, z, height, radius)) {
                return true;
            }
        }

        return false;
    }

    protected void addGorge(Vector3f center, boolean breachSurface, Vector3f last, double lastSize, List<Influencer> list, int depth) {
        if (depth > 9) {
            return;
        }
        System.out.println(new StringBuilder().append("addGorge(").append(center).append(", ").append(breachSurface ? "ForceSurface" : "RandomElevation").append(") depth:").append(depth).toString());

        float height = (float) (this.random.nextDouble() * 24.0D) + 16.0F;

        float xDir = (float) range(-1.0D, 1.0D);
        float yDir = (float) range(-1.0D, 1.0D);
        float zDir = (float) range(-1.0D, 1.0D);

        float max = (float) lastSize;
        if (lastSize < 4.0D) {
            double chance = this.random.nextDouble();
            if (chance < 0.5D) {
                max = (float) lastSize * this.cavernous;
            }
        }
        double r = this.random.nextDouble() * 2.0D + 2.0D;
        if (r < 2.0D) {
            r = 2.0D;
        }
        double s = r + (this.random.nextDouble() * 10.0D - 5.0D);
        if (s < r) {
            s = r;
        }
        if (breachSurface) {
            int elev = this.generator.calculateElevation((int) center.x, (int) center.y);

            if (center.z + height < elev)
                center.z = (float) (elev - height + r);
            else if (center.z > elev) {
                center.z = elev;
            }
        }

        float lengthFactor1 = max * 0.6F;
        float lengthFactor2 = max * 0.3F;

        float l1 = -(float) (this.random.nextDouble() * lengthFactor1 + lengthFactor2);
        float l2 = (float) (this.random.nextDouble() * lengthFactor1 + lengthFactor2);

        Vector3f start = new Vector3f(center.x + xDir * l1, center.y + yDir * l1, center.z + zDir * l1);
        if (start.z < r)
            start.z = (float) r;
        Vector3f end = new Vector3f(center.x + xDir * l2, center.y + yDir * l2, center.z + zDir * l2);
        if (end.z < r) {
            end.z = (float) r;
        }

        boolean isWet = isWet(start, end, height, (float) r);

        WallInfluencer wall = new WallInfluencer(start, end, height, (float) r, (float) s);
        wall.setWet(isWet);
        list.add(wall);

        float continue1 = (float) this.random.nextDouble() * Math.abs(l1);
        if (continue1 > 2.0F) {
            addGorge(start, breachSurface, center, continue1, list, depth + 1);
        }
        float continue2 = (float) this.random.nextDouble() * l2;
        if (continue2 > 2.0F)
            addGorge(end, breachSurface, center, continue1, list, depth + 1);
    }

    protected void addCaves(Vector3f start, Vector3f last, double lastSize, List<Influencer> list, int depth) {
        if (depth > 9)
            return;
        System.out.println(new StringBuilder().append("addCaves(").append(start).append(") depth:").append(depth).toString());

        double max = lastSize;
        if (lastSize < 5.0D) {
            double chance = this.random.nextDouble();
            if (chance < 0.5D) {
                max = lastSize * this.cavernous;
            }
        }
        double r = this.random.nextDouble() * (max - 2.0D) + 2.0D;
        if (r < 2.0D) {
            r = 2.0D;
        }
        double s = r + (this.random.nextDouble() * 10.0D - 5.0D);
        if (s < r) {
            s = r;
        }

        PointInfluencer point = new PointInfluencer(start, (float) r, (float) s);
        list.add(point);

        int elev = this.generator.calculateElevation((int) start.x, (int) start.y);

        boolean isWet = false;
        if (elev <= 57) {
            if ((start.z + r >= elev) && (start.z - r <= 57.0D)) {
                isWet = true;
                point.setWet(true);
            }

        }

        double maxRadius = r;
        int count = 0;
        if (r < 4.0D) {
            double chance = this.random.nextDouble();
            if (chance < 0.5D)
                count = 0;
            else if (chance < 0.75D)
                count = 1;
            else {
                count = 2;
            }
        } else if (r < 10.0D) {
            count = this.random.nextInt(4);
        } else {
            count = this.random.nextInt(8);
            maxRadius = 6.0D;
        }

        if ((depth < 2) && (count == 0)) {
            count = 4;
        }

        for (int i = 0; i < count; i++) {
            double rSub = this.random.nextDouble() * (maxRadius - 2.0D) + 2.0D;
            double sSub = rSub + (this.random.nextDouble() * 10.0D - 5.0D);

            if (sSub < rSub) {
                sSub = rSub;
            }
            double length = this.random.nextDouble() * 40.0D;
            Vector3f end = randomPosition(start, r + rSub, length, (int) rSub);

            if ((end.x - this.xBase >= 12.0F) && (end.x - this.xBase <= 1012.0F)) {
                if ((end.y - this.yBase >= 12.0F) && (end.y - this.yBase <= 1012.0F)) {
                    LineInfluencer line = new LineInfluencer(start, end, (float) rSub, (float) sSub);
                    line.setWet(isWet);
                    list.add(line);
                    addCaves(end, start, rSub, list, depth + 1);
                }
            }
        }
    }

    protected double range(double min, double max) {
        double factor = max - min;
        double x = this.random.nextDouble() * factor + min;
        return x;
    }

    protected double nextDelta(double minDistance, double maxDistance) {
        double factor = maxDistance - minDistance;
        double x = this.random.nextDouble() * factor * 2.0D - factor;
        if (x < 0.0D)
            x -= minDistance;
        else
            x += minDistance;
        return x;
    }

    protected Vector3f randomPosition(Vector3f start, double minDistance, double maxDistance, int size) {
        double x = start.x + nextDelta(minDistance, maxDistance);
        if (x < this.xBase + size)
            x = size;
        else if (x > this.xBase + 1000.0F + 12.0F + size) {
            x = this.xBase + 1000.0F + 12.0F + size;
        }
        double y = start.y + nextDelta(minDistance, maxDistance);
        if (y < this.yBase + size)
            y = size;
        else if (y > this.yBase + 1000.0F + 12.0F + size) {
            y = this.yBase + 1000.0F + 12.0F + size;
        }
        double z = start.z + nextDelta(0.0D, 20.0D);
        if (z < size) {
            z = size;
        }

        return new Vector3f((float) x, (float) y, (float) z);
    }

    public Influencer[] getCaves() {
        return this.caves;
    }
}