package mythruna.db;

import mythruna.Coordinates;
import mythruna.db.io.CellDataProtocol;
import mythruna.db.io.LeafInfoProtocol;

import java.io.*;

public class DefaultLeafDatabase implements LeafDatabase {

    public static final int NODE_SIZE = 1024;
    private File base;
    private int seed;
    private LeafInfoProtocol infoProto = new LeafInfoProtocol();
    private CellDataProtocol cellProto = new CellDataProtocol();

    public DefaultLeafDatabase(File base, int seed) {
        this(base, seed, true);
    }

    public DefaultLeafDatabase(File base, int seed, boolean copyDefaultResources) {
        this.base = base;
        this.seed = seed;

        if (copyDefaultResources) {
            try {
                File node0 = getNode(0, 0, 0, false);

                if (node0 == null) {
                    node0 = getNode(0, 0, 0, true);
                    copyDefaultResources();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error initializing node0", e);
            }
        }
    }

    public boolean exists(int x, int y, int z) throws IOException {
        File f = getLeafFile(x, y, z, false);
        return f.exists();
    }

    public static void copyResources(File target, String srcRoot, String[] resources) throws IOException {
        for (String s : resources) {
            String path = srcRoot + "/" + s;
            System.out.println("Copying:" + path + "  to:" + target);
            InputStream in = DefaultLeafDatabase.class.getResourceAsStream(path);
            if (in == null)
                throw new RuntimeException("ERROR: no resource found at:" + path);
            FileUtils.saveStream(new File(target, s), in);
        }
    }

    protected void copyDefaultResources() {
        if (this.seed != 0) {
            return;
        }
        String[] resources = {"16x14x1.leaf", "16x14x2.leaf", "16x15x1.leaf", "16x15x2.leaf", "13x14x1.leaf", "13x14x2.leaf", "13x14x3.leaf", "14x13x1.leaf", "14x13x2.leaf", "14x14x1.leaf", "14x14x2.leaf", "15x13x1.leaf", "15x13x2.leaf", "15x14x1.leaf", "15x14x2.leaf", "15x15x1.leaf", "15x15x2.leaf", "15x16x1.leaf", "15x16x2.leaf", "16x14x1.leaf", "16x14x2.leaf", "16x15x1.leaf", "16x15x2.leaf", "16x16x1.leaf", "16x16x2.leaf"};

        String srcRoot = "/db-resources/seed-" + this.seed + "/node-0x0x0";
        try {
            File target = getNode(0, 0, 0, true);

            copyResources(target, srcRoot, resources);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected File getNode(int x, int y, int z, boolean create) throws IOException {
        int i = Coordinates.worldToNode(x);
        int j = Coordinates.worldToNode(y);
        int k = Coordinates.worldToNode(z);

        File f = new File(this.base, "node-" + i + "x" + j + "x" + k);
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

    protected File getLeafFile(int x, int y, int z, boolean createPaths) throws IOException {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        int k = Coordinates.worldToLeaf(z);

        File node = getNode(x, y, z, createPaths);
        if (node == null) {
            return null;
        }
        return new File(node, i + "x" + j + "x" + k + ".leaf");
    }

    public LeafInfo readInfo(int x, int y, int z) throws IOException {
        File f = getLeafFile(x, y, z, false);
        if ((f == null) || (!f.exists())) {
            return null;
        }
        FileInputStream fIn = new FileInputStream(f);
        BufferedInputStream in = new BufferedInputStream(fIn);
        try {
            int protocolVersion = in.read();
            return this.infoProto.read(in, protocolVersion);
        } finally {
            in.close();
        }
    }

    public LeafData readData(int x, int y, int z) throws IOException {
        File f = getLeafFile(x, y, z, false);
        if ((f == null) || (!f.exists())) {
            return null;
        }

        FileInputStream fIn = new FileInputStream(f);
        BufferedInputStream in = new BufferedInputStream(fIn);
        try {
            int protocolVersion = in.read();
            LeafInfo info = this.infoProto.read(in, protocolVersion);
            LeafData result = new LeafData(info);

            int[][][] cells = this.cellProto.read(info, in);
            result.setCells(cells);

            return result;
        } finally {
            in.close();
        }
    }

    protected LeafData readData(File f) throws IOException {
        FileInputStream fIn = new FileInputStream(f);
        BufferedInputStream in = new BufferedInputStream(fIn);
        try {
            int protocolVersion = in.read();
            LeafInfo info = this.infoProto.read(in, protocolVersion);
            LeafData result = new LeafData(info);

            int[][][] cells = this.cellProto.read(info, in);
            result.setCells(cells);

            return result;
        } finally {
            in.close();
        }
    }

    public void writeData(LeafData data) throws IOException {
        LeafInfo info = data.getInfo();
        File f = getLeafFile(info.x, info.y, info.z, true);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        this.cellProto.write(info, data.getCells(), buffer);

        FileOutputStream fOut = new FileOutputStream(f);
        BufferedOutputStream out = new BufferedOutputStream(fOut);
        try {
            out.write((byte) this.infoProto.getProtocolVersion());

            this.infoProto.write(info, out);

            out.write(buffer.toByteArray());
        } finally {
            out.close();
        }
    }
}