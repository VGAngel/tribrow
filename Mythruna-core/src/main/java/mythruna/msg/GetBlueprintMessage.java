package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class GetBlueprintMessage extends AbstractMessage {

    private long id;

    public GetBlueprintMessage() {
    }

    public GetBlueprintMessage(long id) {
        this.id = id;
    }

    public long getBlueprintId() {
        return this.id;
    }

    public String toString() {
        return "GetBlueprintMessage[" + this.id + "]";
    }
}