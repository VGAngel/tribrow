package mythruna.db.io;

import mythruna.db.ColumnInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ColumnInfoProtocol {

    public static final byte PROTOCOL_VERSION1 = 42;

    public ColumnInfoProtocol() {
    }

    public ColumnInfo read(InputStream in, int protocolVersion)
            throws IOException {
        if (protocolVersion != 42) {
            throw new RuntimeException("Protocol version mismatch, read:" + protocolVersion);
        }

        ColumnInfo info = new ColumnInfo(0, 0);
        DataInputStream dIn = new DataInputStream(in);

        int countSize = dIn.readUnsignedByte();
        short[] typeCounts = new short[countSize];
        for (int i = 0; i < countSize; i++) {
            typeCounts[i] = dIn.readShort();
        }

        return info;
    }
}