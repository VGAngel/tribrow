package mythruna.db;

import mythruna.Coordinates;
import org.progeeks.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class RevisionedLeafDatabase implements LeafDatabase {

    private static final long MS_PER_DAY = 86400000L;
    private File base;
    private DefaultLeafDatabase leafDb;
    private long baseRevision;

    public RevisionedLeafDatabase(File base, DefaultLeafDatabase leafDb) {
        this.base = base;
        this.leafDb = leafDb;
    }

    protected String getNodeName(int x, int y, int z) {
        int i = Coordinates.worldToNode(x);
        int j = Coordinates.worldToNode(y);
        int k = Coordinates.worldToNode(z);
        return "node-" + i + "x" + j + "x" + k;
    }

    protected String getLeafName(int x, int y, int z) {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        int k = Coordinates.worldToLeaf(z);
        return i + "x" + j + "x" + k + ".leaf";
    }

    protected File getNode(int x, int y, int z, long revision, boolean create)
            throws IOException {
        File f = new File(this.base, revision + "/" + getNodeName(x, y, z));
        if (!f.exists()) {
            if (create) {
                if (!f.mkdirs()) {
                    throw new IOException("Could not create node directory:" + f);
                }
            } else {
                f = null;
            }
        }
        return f;
    }

    protected File getLeafFile(int x, int y, int z, long revision, boolean createPaths) throws IOException {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        int k = Coordinates.worldToLeaf(z);

        File node = getNode(x, y, z, revision, createPaths);
        if (node == null) {
            return null;
        }
        return new File(node, i + "x" + j + "x" + k + ".leaf");
    }

    public Set<Long> getRevisions(int x, int y, int z) {
        TreeSet results = new TreeSet();

        File[] list = this.base.listFiles();

        String leaf = getNodeName(x, y, z) + "/" + getLeafName(x, y, z);

        for (File f : list) {
            if (f.isDirectory()) {
                File rev = new File(f, leaf);

                if (rev.exists()) {
                    long v = Long.parseLong(f.getName());
                    results.add(Long.valueOf(v));
                }
            }
        }
        return results;
    }

    public LeafInfo readInfo(int x, int y, int z)
            throws IOException {
        return this.leafDb.readInfo(x, y, z);
    }

    public LeafData readData(int x, int y, int z)
            throws IOException {
        return this.leafDb.readData(x, y, z);
    }

    public LeafData readData(int x, int y, int z, long rev)
            throws IOException {
        File f = getLeafFile(x, y, z, rev, false);
        if ((f == null) || (!f.exists())) {
            return null;
        }
        return this.leafDb.readData(f);
    }

    public void setBaseRevision(long baseRevision) {
        this.baseRevision = baseRevision;
    }

    public long getBaseRevision() {
        return this.baseRevision;
    }

    public long mark() {
        long time = System.currentTimeMillis();

        setBaseRevision(time);

        return time;
    }

    public long getCurrentRevision() {
        long time = System.currentTimeMillis();

        time -= time % 86400000L;

        if (time < getBaseRevision()) {
            time = getBaseRevision();
        }
        return time;
    }

    public void writeData(LeafData data)
            throws IOException {
        long rev = getCurrentRevision();
        LeafInfo info = data.getInfo();

        if (info.branch < rev) {
            File f = this.leafDb.getLeafFile(info.x, info.y, info.z, false);
            if ((f == null) || (!f.exists())) {
                System.out.println("No original to backup in this case.");
            } else {
                System.out.println("data branch:" + info.branch + "  current branch:" + rev);
                System.out.println("  info:" + info);
                System.out.println("  v:" + info.version + "  lit:" + info.lit + "  solid:" + info.solidCells + "  empty:" + info.emptyCells);
                System.out.println("Need to backup the original:" + f);

                File dest = getLeafFile(info.x, info.y, info.z, rev, true);
                System.out.println("To:" + dest);

                long bytes = FileUtils.copyFile(f, dest);
                System.out.println("Copied " + bytes + " bytes.");
            }

            info.branch = rev;
        }

        this.leafDb.writeData(data);
    }

    public boolean exists(int x, int y, int z) throws IOException {
        return this.leafDb.exists(x, y, z);
    }
}