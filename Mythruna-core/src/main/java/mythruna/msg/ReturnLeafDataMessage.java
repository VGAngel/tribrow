package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.db.LeafData;
import mythruna.db.LeafInfo;
import mythruna.db.io.CellDataProtocol;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Serializable
public class ReturnLeafDataMessage extends AbstractMessage {

    public static final int MAX_SIZE = 32000;
    private byte partsFlags = 16;
    private int x;
    private int y;
    private int z;
    private long version;
    private byte[] zippedData;
    private int typesSize;
    private int lightsSize;

    public ReturnLeafDataMessage() {
    }

    public static ReturnLeafDataMessage[] createMessages(LeafData data) {
        try {
            byte[] buffer = toBytes(data);

            if ((buffer == null) || (buffer.length <= 32000)) {
                ReturnLeafDataMessage msg = new ReturnLeafDataMessage();
                msg.x = data.getX();
                msg.y = data.getY();
                msg.z = data.getZ();
                msg.version = data.getInfo().version;
                msg.zippedData = buffer;
                msg.typesSize = data.getInfo().typesSize;
                msg.lightsSize = data.getInfo().lightsSize;

                return new ReturnLeafDataMessage[]{msg};
            }

            int count = buffer.length / 32000 + 1;

            ReturnLeafDataMessage[] array = new ReturnLeafDataMessage[count];
            int pos = 0;
            for (int i = 0; i < count; i++) {
                byte[] sub = new byte[Math.min(32000, buffer.length - pos)];
                System.arraycopy(buffer, pos, sub, 0, sub.length);
                pos += sub.length;

                array[i] = new ReturnLeafDataMessage();
                array[i].x = data.getX();
                array[i].y = data.getY();
                array[i].z = data.getZ();
                array[i].version = data.getInfo().version;
                array[i].zippedData = sub;
                array[i].partsFlags = (byte) (count << 4 | i);
                array[i].typesSize = data.getInfo().typesSize;
                array[i].lightsSize = data.getInfo().lightsSize;
            }
            return array;
        } catch (IOException e) {
            throw new RuntimeException("Error converting leaf data to bytes", e);
        }
    }

    public LeafData getLeafData() {
        if (getTotal() != 1)
            throw new RuntimeException("Cannot directly create leaf from partial message.");
        return getLeafData(new byte[][]{this.zippedData});
    }

    public LeafData getLeafData(byte[][] buffers) {
        LeafInfo info = new LeafInfo();
        info.x = this.x;
        info.y = this.y;
        info.z = this.z;
        info.lit = true;
        info.version = this.version;
        info.typesSize = this.typesSize;
        info.lightsSize = this.lightsSize;

        if (buffers[0] == null) {
            LeafData result = new LeafData(info);

            info.lit = false;

            return result;
        }

        int count = 0;
        for (int i = 0; i < buffers.length; i++)
            count += buffers[i].length;
        byte[] data = new byte[count];

        int pos = 0;
        for (int i = 0; i < buffers.length; i++) {
            System.arraycopy(buffers[i], 0, data, pos, buffers[i].length);
            pos += buffers[i].length;
        }

        try {
            return toLeaf(info, data);
        } catch (IOException e) {
            throw new RuntimeException("Error converting data to leaf", e);
        }
    }

    protected static LeafData toLeaf(LeafInfo info, byte[] data) throws IOException {
        ByteArrayInputStream bIn = new ByteArrayInputStream(data);
        GZIPInputStream gIn = new GZIPInputStream(bIn);
        BufferedInputStream in = new BufferedInputStream(gIn);
        CellDataProtocol prot = new CellDataProtocol();
        try {
            int[][][] cells = prot.read(info, in);

            LeafData result = new LeafData(info);
            result.setCells(cells);

            return result;
        } finally {
            in.close();
        }
    }

    protected static byte[] toBytes(LeafData data)
            throws IOException {
        if (data == null) {
            return null;
        }

        CellDataProtocol prot = new CellDataProtocol();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        GZIPOutputStream gOut = new GZIPOutputStream(bOut);
        BufferedOutputStream out = new BufferedOutputStream(gOut);
        try {
            prot.write(data.getInfo(), data.getCells(), out);
        } finally {
            out.close();
        }

        return bOut.toByteArray();
    }

    protected static void test(LeafData data) {
        System.out.println(new StringBuilder().append("Testing zip stuff for leaf:").append(data.getInfo()).toString());
        try {
            if (data.isEmpty()) {
                return;
            }

            long start = System.nanoTime();
            int size = test1(data);
            long end = System.nanoTime();
            long base;
            long delta = base = end - start;
            System.out.println(new StringBuilder().append("test1 size:").append(size).append("  in:").append(delta / 1000000.0D).append("ms").toString());

            start = System.nanoTime();
            size = test2(data);
            end = System.nanoTime();
            delta = end - start;
            double ratio = delta / base;
            System.out.println(new StringBuilder().append("test2 size:").append(size).append("  in:").append(delta / 1000000.0D).append("ms   ratio:").append(ratio).toString());

            start = System.nanoTime();
            size = test3(data);
            end = System.nanoTime();
            delta = end - start;
            ratio = delta / base;
            System.out.println(new StringBuilder().append("test3 size:").append(size).append("  in:").append(delta / 1000000.0D).append("ms   ratio:").append(ratio).toString());

            start = System.nanoTime();
            size = test4(data);
            end = System.nanoTime();
            delta = end - start;
            ratio = delta / base;
            System.out.println(new StringBuilder().append("test4 size:").append(size).append("  in:").append(delta / 1000000.0D).append("ms   ratio:").append(ratio).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static int test1(LeafData data) throws IOException {
        if (data.isEmpty()) {
            return 0;
        }

        LeafInfo copy = new LeafInfo(data.getInfo());
        CellDataProtocol prot = new CellDataProtocol();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(bOut);
        try {
            prot.write(copy, data.getCells(), out);
        } finally {
            out.flush();
            out.close();
        }

        return bOut.size();
    }

    protected static int test2(LeafData data) throws IOException {
        if (data.isEmpty()) {
            return 0;
        }

        LeafInfo copy = new LeafInfo(data.getInfo());
        CellDataProtocol prot = new CellDataProtocol();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        GZIPOutputStream gOut = new GZIPOutputStream(bOut);
        BufferedOutputStream out = new BufferedOutputStream(gOut);
        try {
            prot.write(copy, data.getCells(), out);
        } finally {
            out.flush();
            gOut.finish();
            out.close();
        }

        return bOut.size();
    }

    protected static int test3(LeafData data) throws IOException {
        if (data.isEmpty()) {
            return 0;
        }

        LeafInfo copy = new LeafInfo(data.getInfo());
        CellDataProtocol prot = new CellDataProtocol();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ZipOutputStream gOut = new ZipOutputStream(bOut);
        gOut.putNextEntry(new ZipEntry("leaf"));
        BufferedOutputStream out = new BufferedOutputStream(gOut);
        try {
            prot.write(copy, data.getCells(), out);
        } finally {
            out.flush();
            gOut.finish();
            out.close();
        }

        return bOut.size();
    }

    protected static int test4(LeafData data) throws IOException {
        if (data.isEmpty()) {
            return 0;
        }

        LeafInfo copy = new LeafInfo(data.getInfo());

        int[][][] cells = data.getCells();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        GZIPOutputStream gOut = new GZIPOutputStream(bOut);
        BufferedOutputStream out = new BufferedOutputStream(gOut);
        DataOutputStream dOut = new DataOutputStream(out);
        try {
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 32; j++) {
                    for (int k = 0; k < 32; k++) {
                        dOut.writeInt(cells[i][j][k]);
                    }
                }
            }
        } finally {
            dOut.close();
        }

        return bOut.size();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public long getVersion() {
        return this.version;
    }

    public byte[] getData() {
        return this.zippedData;
    }

    public int getPart() {
        return this.partsFlags & 0xF;
    }

    public int getTotal() {
        return this.partsFlags >> 4 & 0xF;
    }

    public String toString() {
        return new StringBuilder().append("ReturnLeafDataMessage[ ").append(this.x).append(", ").append(this.y).append(", ").append(this.z).append(", version=").append(this.version).append(", typesSize:").append(this.typesSize).append(", lightsSize:").append(this.lightsSize).append(", data size:").append(this.zippedData == null ? "null" : String.valueOf(this.zippedData.length)).append(", part(").append(getPart()).append("/").append(getTotal()).append(")]").toString();
    }
}