package mythruna.db.io;

import mythruna.db.BlueprintData;

import java.io.*;

public class BlueprintDataProtocol {

    public static final byte PROTOCOL_VERSION1 = 42;
    public static final byte PROTOCOL_VERSION2 = 43;

    public BlueprintDataProtocol() {
    }

    public int getProtocolVersion() {
        return 43;
    }

    public BlueprintData read(long id, InputStream in, int protocolVersion) throws IOException {
        if ((protocolVersion != 42) && (protocolVersion != 43)) {
            throw new RuntimeException("Protocol version mismatch, read:" + protocolVersion);
        }
        BlueprintData result = new BlueprintData();

        DataInputStream dIn = new DataInputStream(in);

        result.id = id;
        result.name = dIn.readUTF();
        result.xSize = dIn.readUnsignedByte();
        result.ySize = dIn.readUnsignedByte();
        result.zSize = dIn.readUnsignedByte();

        if (protocolVersion >= 43) {
            int b = dIn.readUnsignedByte();
            result.scale = (b / 100.0F);
        } else {
            result.scale = 0.25F;
        }

        int dataSize = dIn.readUnsignedShort();
        byte[] cellBuffer = new byte[dataSize];

        dIn.readFully(cellBuffer);

        ByteArrayInputStream inCells = new ByteArrayInputStream(cellBuffer);
        RleShortInput rleCells = new RleShortInput(new DataInputStream(inCells));

        result.cells = new int[result.xSize][result.ySize][result.zSize];

        for (int i = 0; i < result.xSize; i++) {
            for (int j = 0; j < result.ySize; j++) {
                for (int k = 0; k < result.zSize; k++) {
                    int v = rleCells.readShort();

                    result.cells[i][j][k] = v;
                }
            }
        }

        return result;
    }

    public void write(BlueprintData data, OutputStream out)
            throws IOException {
        ByteArrayOutputStream outCells = new ByteArrayOutputStream();
        DataOutputStream dOutCells = new DataOutputStream(new BufferedOutputStream(outCells, 16384));
        RleShortOutput rleCells = new RleShortOutput(dOutCells);
        try {
            int[][][] cells = data.cells;
            for (int i = 0; i < data.xSize; i++) {
                for (int j = 0; j < data.ySize; j++) {
                    for (int k = 0; k < data.zSize; k++) {
                        rleCells.writeShort((short) (cells[i][j][k] & 0xFFFF));
                    }
                }
            }
        } finally {
            rleCells.close();
        }

        byte[] cellBuffer = outCells.toByteArray();
        int dataSize = cellBuffer.length;

        DataOutputStream dOut = new DataOutputStream(out);

        dOut.writeUTF(data.name);
        dOut.writeByte(data.xSize);
        dOut.writeByte(data.ySize);
        dOut.writeByte(data.zSize);

        int scale = Math.min(255, Math.max(0, Math.round(data.scale * 100.0F)));
        dOut.writeByte(scale);

        dOut.writeShort(dataSize);
        dOut.write(cellBuffer);
    }

    private static class RleShortOutput {
        private int BIT_MASK = 32768;
        private int maxCount = 32767;
        private DataOutputStream dOut;
        private short lastValue;
        private int lastCount;

        public RleShortOutput(DataOutputStream dOut) {
            this.dOut = dOut;
        }

        public void writeShort(short i)
                throws IOException {
            if ((i == this.lastValue) && (this.lastCount < this.maxCount)) {
                this.lastCount += 1;
            } else {
                flushCount();
                this.lastCount = 1;
                this.lastValue = i;
            }
        }

        protected void flushCount() throws IOException {
            if (this.lastCount > 2) {
                this.dOut.writeShort((short) (this.lastCount | this.BIT_MASK));
                this.dOut.writeShort(this.lastValue);
            } else {
                while (this.lastCount > 0) {
                    this.dOut.writeShort(this.lastValue);
                    this.lastCount -= 1;
                }
            }
            this.lastCount = 0;
        }

        public void close() throws IOException {
            flushCount();
            this.dOut.close();
        }
    }

    private static class RleShortInput {
        private int BIT_MASK = 32768;
        private int NOT_BIT_MASK = 32767;
        private DataInputStream dIn;
        private short lastValue;
        private int lastCount;

        public RleShortInput(DataInputStream dIn) {
            this.dIn = dIn;
        }

        public short readShort() throws IOException {
            if (this.lastCount == 0) {
                short v = this.dIn.readShort();
                if ((v & this.BIT_MASK) == this.BIT_MASK) {
                    this.lastCount = (v & this.NOT_BIT_MASK);
                    this.lastValue = this.dIn.readShort();
                } else {
                    return v;
                }
            }
            this.lastCount -= 1;
            return this.lastValue;
        }

        public void close() throws IOException {
            this.dIn.close();
        }
    }
}