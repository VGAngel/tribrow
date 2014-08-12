package mythruna.db;

import mythruna.db.io.BlueprintDataProtocol;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DefaultBlueprintDatabase implements BlueprintDatabase {

    private File base;
    private static BlueprintDataProtocol bpProto = new BlueprintDataProtocol();

    public DefaultBlueprintDatabase(File base) {
        this.base = base;
        if (!base.exists()) {
            if (!base.mkdirs()) {
                throw new RuntimeException("Failed to create blueprint directory:" + base);
            }
            copyDefaultResources();
        }
    }

    public void close() {
    }

    public static BlueprintData loadResource(String name) throws IOException {
        InputStream rawIn = DefaultBlueprintDatabase.class.getResourceAsStream("/db-resources/" + name);
        BufferedInputStream in = new BufferedInputStream(rawIn);
        return readBlueprint(-1L, in);
    }

    public static void exportBlueprint(BlueprintData bp, File f)
            throws IOException {
        writeBlueprint(bp, f);
    }

    public static BlueprintData importBlueprint(File f) throws IOException {
        return readBlueprint(-1L, f);
    }

    protected void copyDefaultResources() {
        String[] resources = {"1299456980967.bp", "1299457293967.bp", "1299457813326.bp", "1299458315576.bp", "1308905675317.bp", "1308034318670.bp", "1308034600717.bp", "1308127658326.bp", "1308574507379.bp", "1308128399810.bp", "1308291932560.bp", "1308292196982.bp", "1308905881848.bp"};

        String srcRoot = "/db-resources/blueprints";

        for (String s : resources) {
            String path = srcRoot + "/" + s;
            try {
                System.out.println("Copying:" + path + "  to:" + this.base);
                InputStream in = getClass().getResourceAsStream(path);
                if (in == null)
                    throw new RuntimeException("ERROR: no resource found at:" + path);
                FileUtils.saveStream(new File(this.base, s), in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Long> getIds() {
        List results = new ArrayList();
        for (String s : this.base.list()) {
            if (s.endsWith(".bp")) {
                s = s.substring(0, s.length() - ".bp".length());
                results.add(new Long(s));
            }
        }
        return results;
    }

    protected File idToFile(long id) {
        return new File(this.base, id + ".bp");
    }

    protected boolean exists(long id) {
        return idToFile(id).exists();
    }

    public synchronized BlueprintData createBlueprint(String name, int xSize, int ySize, int zSize, float scale, int[][][] cells)
            throws IOException {
        long id = System.currentTimeMillis();
        while (exists(id)) {
            id += 1L;
        }
        BlueprintData result = new BlueprintData();
        result.id = id;
        result.name = name;
        result.xSize = xSize;
        result.ySize = ySize;
        result.zSize = zSize;
        result.scale = scale;

        result.cells = new int[xSize][ySize][zSize];
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                for (int k = 0; k < zSize; k++) {
                    result.cells[i][j][k] = cells[i][j][k];
                }
            }
        }

        storeBlueprint(result);

        return result;
    }

    protected void storeBlueprint(BlueprintData bp) throws IOException {
        File f = idToFile(bp.id);

        writeBlueprint(bp, f);
    }

    protected static void writeBlueprint(BlueprintData bp, File f) throws IOException {
        FileOutputStream fOut = new FileOutputStream(f);
        BufferedOutputStream out = new BufferedOutputStream(fOut);
        try {
            out.write((byte) bpProto.getProtocolVersion());
            bpProto.write(bp, out);
        } finally {
            out.close();
        }
    }

    protected static BlueprintData readBlueprint(long id, File f) throws IOException {
        FileInputStream fIn = new FileInputStream(f);
        BufferedInputStream in = new BufferedInputStream(fIn);
        return readBlueprint(id, in);
    }

    protected static BlueprintData readBlueprint(long id, InputStream in) throws IOException {
        try {
            int protocolVersion = in.read();
            BlueprintData bp = bpProto.read(id, in, protocolVersion);

            return bp;
        } finally {
            in.close();
        }
    }

    public BlueprintData getBlueprint(long id, boolean load) throws IOException {
        return getBlueprint(id);
    }

    public BlueprintData getBlueprint(long id) throws IOException {
        File f = idToFile(id);
        if ((f == null) || (!f.exists())) {
            return null;
        }
        return readBlueprint(id, f);
    }
}