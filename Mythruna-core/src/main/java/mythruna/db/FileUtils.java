package mythruna.db;

import java.io.*;

public class FileUtils {

    public FileUtils() {
    }

    public static long saveStream(File f, InputStream in) throws IOException {
        FileOutputStream fOut = new FileOutputStream(f);
        BufferedOutputStream out = new BufferedOutputStream(fOut, 65536);
        byte[] transferBuff = new byte[65536];
        try {
            int count = 0;
            int total = 0;
            while ((count = in.read(transferBuff)) >= 0) {
                out.write(transferBuff, 0, count);
                total += count;
            }

            return total;
        } finally {
            out.close();
        }
    }
}