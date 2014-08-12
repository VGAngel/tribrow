package mythruna.phys;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.PersistentComponent;

@Serializable
public class BodyTemplate implements EntityComponent, PersistentComponent {

    private long blueprintId;

    public BodyTemplate() {
    }

    public BodyTemplate(long blueprintId) {
        this.blueprintId = blueprintId;
    }

    public long getBlueprintId() {
        return this.blueprintId;
    }

    public Class<BodyTemplate> getType() {
        return BodyTemplate.class;
    }

    public String toString() {
        return "BodyTemplate[" + getBlueprintId() + "]";
    }
}