package mythruna.util;

import mythruna.Coordinates;
import mythruna.db.DefaultLeafDatabase;
import mythruna.db.LeafData;
import mythruna.db.LeafDatabase;
import mythruna.db.LeafInfo;
import mythruna.db.io.LeafInfoProtocol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WaterRepair {

    public static final int SEA_LEVEL = 56;
    private static final LeafData NULL_LEAF = new LeafData(new LeafInfo());
    private LeafDatabase leafDb;
    private LeafInfoProtocol infoProto = new LeafInfoProtocol();
    private LruCache<String, LeafData> leafCache = new LruCache("leaf", 20);

    private long blocksChanged = 0L;
    private int leafsChanged = 0;

    public WaterRepair(File base) {
        this.leafDb = new DefaultLeafDatabase(base, 0, false);
    }

    protected LeafInfo getLeafInfo(File f) throws IOException {
        FileInputStream fIn = new FileInputStream(f);
        BufferedInputStream in = new BufferedInputStream(fIn);
        try {
            int protocolVersion = in.read();
            return this.infoProto.read(in, protocolVersion);
        } finally {
            in.close();
        }
    }

    public void process(File dir) throws IOException {
        if ("revs".equals(dir.getName())) {
            System.out.println("Skipping revisions directory:" + dir);
            return;
        }

        File[] list = dir.listFiles();
        for (File f : list) {
            if (f.isDirectory())
                process(f);
            else if (f.getName().endsWith(".leaf"))
                processFile(f);
        }
    }

    protected LeafData getLeaf(int x, int y, int z) throws IOException {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        int k = Coordinates.worldToLeaf(z);

        String key = i + ", " + j + ", " + k;
        LeafData result = (LeafData) this.leafCache.get(key);
        if (result == null) {
            result = this.leafDb.readData(x, y, z);
            if (result == null)
                result = NULL_LEAF;
            this.leafCache.put(key, result);
        }
        return result;
    }

    protected int getType(int x, int y, int z) throws IOException {
        LeafData result = getLeaf(x, y, z);

        if (result == NULL_LEAF)
            return 0;
        return result.getWorld(x, y, z);
    }

    protected boolean processLeaf(LeafData data) throws IOException {
        int xBase = data.getInfo().x;
        int yBase = data.getInfo().y;
        int zBase = data.getInfo().z;
        int changes = 0;

        for (int i = 0; i < 32; i++) {
            int x = xBase + i;
            for (int j = 0; j < 32; j++) {
                int y = yBase + j;
                for (int k = 0; k < 32; k++) {
                    int z = zBase + k;
                    if (z <= 56) {
                        int type = data.getType(i, j, k);
                        if (type == 0) {
                            int count = 0;
                            for (int d = 0; d < 6; d++) {
                                int neighbor = getType(x + mythruna.Direction.DIRS[d][0], y + mythruna.Direction.DIRS[d][1], z + mythruna.Direction.DIRS[d][2]);

                                if (neighbor == 8)
                                    count += 2;
                                else if (neighbor == 7) {
                                    count++;
                                }
                            }
                            if (count >= 3) {
                                int set = z == 56 ? 8 : 7;
                                data.setType(i, j, k, set);
                                changes++;
                                this.blocksChanged += 1L;
                            }
                        }
                    }
                }
            }
        }
        return changes > 0;
    }

    protected void processFile(File f) throws IOException {
        LeafInfo info = getLeafInfo(f);

        if (info.z >= 56) {
            return;
        }

        LeafData data = getLeaf(info.x, info.y, info.z);
        System.out.println("Data:" + data);

        if (processLeaf(data)) {
            System.out.println("*** Saving leaf changes.");
            this.leafDb.writeData(data);
            this.leafsChanged += 1;
        }
    }

    public static void main(String[] args)
            throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: WaterRepair [-d directory]");
        }

        String db = "mythruna.db";

        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                db = args[(++i)];
            }
        }

        System.out.println("Directory:" + db);

        WaterRepair c = new WaterRepair(new File(db));
        c.process(new File(db));

        System.out.println("Changed:" + c.blocksChanged + " blocks in:" + c.leafsChanged + " leafs.");
    }
}