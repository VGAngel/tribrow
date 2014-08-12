package mythruna.db.io;

import mythruna.db.LeafInfo;

import java.io.*;

public class LeafInfoProtocol {

    public static final byte PROTOCOL_VERSION1 = 42;
    public static final byte PROTOCOL_VERSION2 = 43;
    public static final byte PROTOCOL_VERSION3 = 44;

    public LeafInfoProtocol() {
    }

    public int getProtocolVersion() {
        return 44;
    }

    public LeafInfo read(InputStream in, int protocolVersion) throws IOException {
        if ((protocolVersion != 42) && (protocolVersion != 43) && (protocolVersion != 44)) {
            throw new RuntimeException("Protocol version mismatch, read:" + protocolVersion);
        }
        LeafInfo info = new LeafInfo();
        DataInputStream dIn = new DataInputStream(in);

        info.x = dIn.readInt();
        info.y = dIn.readInt();
        info.z = dIn.readInt();
        if (protocolVersion >= 43)
            info.generationLevel = dIn.readInt();
        info.version = dIn.readLong();
        if (protocolVersion >= 44)
            info.branch = dIn.readLong();
        info.lit = dIn.readBoolean();
        info.emptyCells = dIn.readInt();
        info.solidCells = dIn.readInt();
        info.typesSize = dIn.readInt();
        info.lightsSize = dIn.readInt();

        return info;
    }

    public void write(LeafInfo info, OutputStream out) throws IOException {
        DataOutputStream dOut = new DataOutputStream(out);

        dOut.writeInt(info.x);
        dOut.writeInt(info.y);
        dOut.writeInt(info.z);
        dOut.writeInt(info.generationLevel);
        dOut.writeLong(info.version);
        dOut.writeLong(info.branch);
        dOut.writeBoolean(info.lit);
        dOut.writeInt(info.emptyCells);
        dOut.writeInt(info.solidCells);
        dOut.writeInt(info.typesSize);
        dOut.writeInt(info.lightsSize);
    }
}