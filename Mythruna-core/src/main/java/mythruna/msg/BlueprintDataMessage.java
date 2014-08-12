package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.db.BlueprintData;
import mythruna.db.io.BlueprintDataProtocol;

import java.io.*;

@Serializable
public class BlueprintDataMessage extends AbstractMessage {

    private static BlueprintDataProtocol protocol = new BlueprintDataProtocol();
    private long id;
    private byte[] data;

    public BlueprintDataMessage() {
    }

    public BlueprintDataMessage(BlueprintData bpData) {
        this.id = bpData.id;

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(bout);
        try {
            protocol.write(bpData, out);
            out.close();
            this.data = bout.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error creating blueprint message data", e);
        }
    }

    public long getId() {
        return this.id;
    }

    public BlueprintData getData() {
        ByteArrayInputStream bin = new ByteArrayInputStream(this.data);
        BufferedInputStream in = new BufferedInputStream(bin);
        try {
            return protocol.read(this.id, in, protocol.getProtocolVersion());
        } catch (IOException e) {
            throw new RuntimeException("Error decoding blueprint message data", e);
        }
    }

    public String toString() {
        return "BlueprintDataMessage[" + this.id + ", dataSize=" + this.data.length + "]";
    }
}