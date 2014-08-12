package mythruna.util;

import mythruna.db.LeafInfo;
import mythruna.db.io.LeafInfoProtocol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Cleaner {

    private int threshold;
    private LeafInfoProtocol infoProto = new LeafInfoProtocol();

    public Cleaner(int threshold) {
        this.threshold = threshold;
    }

    public void clean(File dir) throws IOException {
        File[] list = dir.listFiles();
        for (File f : list) {
            if (f.isDirectory())
                clean(f);
            else if (f.getName().endsWith(".leaf"))
                checkFile(f);
        }
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

    protected void checkFile(File f) throws IOException {
        LeafInfo info = getLeafInfo(f);

        System.out.println(f + " vesion:" + info.version + "  threshold:" + this.threshold);
        if (info.version < this.threshold) {
            if (!f.delete())
                System.err.println("Error deleting file:" + f);
            else
                System.out.println("...deleted.");
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: Cleaner [-d directory] [-v tile version threshold]");
            return;
        }

        String db = "mythruna.db";
        int threshold = 2;

        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                db = args[(++i)];
            } else if ("-v".equals(args[i])) {
                threshold = Integer.parseInt(args[(++i)]);
            }
        }

        System.out.println("Directory:" + db);
        System.out.println("Threshold:" + threshold);

        Cleaner c = new Cleaner(threshold);
        c.clean(new File(db));
    }
}