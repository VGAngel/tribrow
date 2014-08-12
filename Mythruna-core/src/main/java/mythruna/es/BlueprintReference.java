package mythruna.es;

import com.jme3.network.serializing.Serializable;

@Serializable
public class BlueprintReference implements EntityComponent, PersistentComponent {

    private long blueprintId;

    public BlueprintReference() {
        this(-1L);
    }

    public BlueprintReference(long blueprintId) {
        this.blueprintId = blueprintId;
    }

    public Class<BlueprintReference> getType() {
        return BlueprintReference.class;
    }

    public long getBlueprintId() {
        return this.blueprintId;
    }

    public String toString() {
        return "BlueprintReference[" + this.blueprintId + "]";
    }
}