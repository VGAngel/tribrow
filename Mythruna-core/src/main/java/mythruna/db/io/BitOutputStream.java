package mythruna.db.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream {

    private OutputStream out;
    private int currentByte = 0;
    private int bits = 8;

    public BitOutputStream(OutputStream out) {
        this.out = out;
    }

    public void writeBits(int value, int count)
            throws IOException {
        value &= -1 >>> 32 - count;

        int remaining = count;
        while (remaining > 0) {
            int bitsToCopy = this.bits < remaining ? this.bits : remaining;

            int sourceShift = remaining - bitsToCopy;
            int targetShift = this.bits - bitsToCopy;

            this.currentByte |= value >>> sourceShift << targetShift;

            remaining -= bitsToCopy;
            this.bits -= bitsToCopy;

            value &= -1 >>> 32 - remaining;

            if (this.bits == 0) {
                flush();
            }
        }
    }

    protected void flush()
            throws IOException {
        this.out.write(this.currentByte);
        this.bits = 8;
        this.currentByte = 0;
    }

    public void close() throws IOException {
        flush();
        this.out.close();
    }

    public static void main(String[] args) throws Exception {
        test2();
    }

    public static void test2() throws Exception {
        byte[] bytes = {18, 52, 86, 120, -102, -68, -34, -1};

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(bOut);
        for (int i = 0; i < bytes.length; i++) {
            out.writeBits(1, 1);
            out.writeBits(bytes[i], 8);
        }
        out.close();

        byte[] toRead = bOut.toByteArray();
        System.out.println("Written length:" + toRead.length);

        ByteArrayInputStream bIn = new ByteArrayInputStream(toRead);
        BitInputStream in = new BitInputStream(bIn);
        for (int i = 0; i < bytes.length; i++) {
            int test = in.readBits(1);
            int val = in.readBits(8);
            System.out.print("[" + Integer.toHexString(val) + "]");
        }
        System.out.println();
    }

    public static void test1() throws Exception {
        for (int count = 1; count <= 32; count++) {
            System.out.println("Count:" + count);
            byte[] bytes = {18, 52, 86, 120, -102, -68, -34, -1};
            int total = 64;

            ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
            BitInputStream in = new BitInputStream(bIn);

            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            BitOutputStream out = new BitOutputStream(bOut);

            int bitsRead = 0;
            while (bitsRead <= total - count) {
                int val = in.readBits(count);
                out.writeBits(val, count);

                bitsRead += count;
            }

            byte[] result = bOut.toByteArray();
            for (int i = 0; i < result.length; i++)
                System.out.print("[" + Integer.toHexString(result[i] & 0xFF) + "]");
            System.out.println();
        }
    }
}