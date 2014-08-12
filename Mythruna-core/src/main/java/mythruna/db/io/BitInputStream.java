package mythruna.db.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream {

    private InputStream in;
    private int lastByte;
    private int bits = 0;

    public BitInputStream(InputStream in) {
        this.in = in;
    }

    public int readBits(int count) throws IOException {
        if (count > 32) {
            throw new IllegalArgumentException("Bit count overflow:" + count);
        }
        int result = 0;

        int remainingCount = count;
        while (remainingCount > 0) {
            if (this.bits == 0) {
                int b = this.in.read();
                if (b < 0)
                    throw new IOException("End of stream reached.");
                this.lastByte = b;
                this.bits = 8;
            }

            int bitsToCopy = this.bits < remainingCount ? this.bits : remainingCount;

            int sourceShift = this.bits - bitsToCopy;

            int targetShift = remainingCount - bitsToCopy;

            result |= this.lastByte >> sourceShift << targetShift;

            remainingCount -= bitsToCopy;
            this.bits -= bitsToCopy;

            this.lastByte &= 255 >> 8 - this.bits;
        }

        return result;
    }

    public void close() throws IOException {
        this.in.close();
    }

    public static void main(String[] args) throws Exception {
        for (int count = 1; count <= 32; count++) {
            System.out.println("Count:" + count);
            byte[] bytes = {18, 52, 86, 120, -102, -68, -34, -1};
            int total = 64;

            ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
            BitInputStream in = new BitInputStream(bIn);

            int bitsRead = 0;
            while (bitsRead <= total - count) {
                System.out.println(Integer.toHexString(in.readBits(count)));
                bitsRead += count;
            }
        }
    }
}