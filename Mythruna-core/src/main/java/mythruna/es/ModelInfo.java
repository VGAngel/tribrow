package mythruna.es;

import com.jme3.network.serializing.Serializable;

@Serializable
public class ModelInfo implements EntityComponent, PersistentComponent {

    private long blueprintId;

    public ModelInfo() {
        this(-1L);
    }

    public ModelInfo(long blueprintId) {
        this.blueprintId = blueprintId;
    }

    public Class<ModelInfo> getType() {
        return ModelInfo.class;
    }

    public long getBlueprintId() {
        return this.blueprintId;
    }

    public String toString() {
        return "ModelInfo[" + this.blueprintId + "]";
    }
}