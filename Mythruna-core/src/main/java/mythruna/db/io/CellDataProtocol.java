package mythruna.db.io;

import mythruna.db.LeafInfo;

import java.io.*;

public class CellDataProtocol {

    public CellDataProtocol() {
    }

    public int[][][] read(LeafInfo info, InputStream in)
            throws IOException {
        if (info.typesSize == 0) {
            return (int[][][]) null;
        }
        int[][][] cells = new int[32][32][32];

        DataInputStream dIn = new DataInputStream(in);

        byte[] typeBuffer = new byte[info.typesSize];
        byte[] lightBuffer = new byte[info.lightsSize];

        dIn.readFully(typeBuffer);
        dIn.readFully(lightBuffer);

        ByteArrayInputStream inTypes = new ByteArrayInputStream(typeBuffer);
        ByteArrayInputStream inLights = new ByteArrayInputStream(lightBuffer);

        RleShortInput rleTypes = new RleShortInput(new DataInputStream(inTypes));
        RleShortInput rleLights = new RleShortInput(new DataInputStream(inLights));

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 32; k++) {
                    int v = rleTypes.readShort();
                    int light = rleLights.readShort();

                    cells[i][j][k] = (v | (light & 0xFF) << 24);
                }
            }
        }

        return cells;
    }

    public void write(LeafInfo info, int[][][] cells, OutputStream out)
            throws IOException {
        ByteArrayOutputStream types = new ByteArrayOutputStream();
        ByteArrayOutputStream lights = new ByteArrayOutputStream();

        DataOutputStream dOutTypes = new DataOutputStream(new BufferedOutputStream(types, 16384));
        RleShortOutput rleTypes = new RleShortOutput(dOutTypes);
        DataOutputStream dOutLights = new DataOutputStream(new BufferedOutputStream(lights, 16384));
        RleShortOutput rleLights = new RleShortOutput(dOutLights);
        try {
            if (cells != null) {
                for (int i = 0; i < 32; i++) {
                    for (int j = 0; j < 32; j++) {
                        for (int k = 0; k < 32; k++) {
                            rleTypes.writeShort((short) (cells[i][j][k] & 0xFFFF));
                            rleLights.writeShort((short) (cells[i][j][k] >> 24 & 0xFF));
                        }
                    }
                }
            }
        } finally {
            rleTypes.close();
            rleLights.close();
        }

        byte[] typeBuffer = types.toByteArray();
        byte[] lightBuffer = lights.toByteArray();

        info.typesSize = typeBuffer.length;
        info.lightsSize = lightBuffer.length;

        out.write(typeBuffer);
        out.write(lightBuffer);
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