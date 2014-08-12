package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class ConsoleMessage extends AbstractMessage {

    private long time;
    private int sourceId;
    private String from;
    private String message;

    public ConsoleMessage() {
    }

    public ConsoleMessage(long time, int sourceId, String from, String message) {
        this.sourceId = sourceId;
        this.from = from;
        this.message = message;
    }

    public long getTime() {
        return this.time;
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public String getFrom() {
        return this.from;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return "ConsoleMessage[ time:" + this.time + ", from:" + this.from + "]";
    }
}