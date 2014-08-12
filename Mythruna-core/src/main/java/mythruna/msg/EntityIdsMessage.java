package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class EntityIdsMessage extends AbstractMessage {

    private int requestId;
    private byte part;
    private byte total;
    private long[] ids;

    public EntityIdsMessage() {
    }

    public EntityIdsMessage(int requestId) {
        this.requestId = requestId;
    }

    public EntityIdsMessage(int requestId, long[] ids, int part, int total) {
        this.requestId = requestId;
        this.ids = ids;
        this.part = (byte) part;
        this.total = (byte) total;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public long[] getEntityIds() {
        return this.ids;
    }

    public int getPart() {
        return this.part & 0xFF;
    }

    public int getTotal() {
        return this.total & 0xFF;
    }

    public boolean isLast() {
        return this.part == this.total;
    }

    public String toString() {
        return new StringBuilder().append("EntityIdsMessage[").append(this.requestId).append(", ").append(getPart()).append("/").append(getTotal()).append(", ").append(this.ids != null ? new StringBuilder().append("count=").append(this.ids.length).toString() : "empty").append("]").toString();
    }
}