package mythruna.item;

import mythruna.es.EntityComponent;
import mythruna.es.PersistentComponent;

public class ModelTemplate implements EntityComponent, PersistentComponent {

    private long blueprintId;
    private int version;

    public ModelTemplate() {
    }

    public ModelTemplate(long blueprintId, int version) {
        this.blueprintId = blueprintId;
        this.version = version;
    }

    public Class<ModelTemplate> getType() {
        return ModelTemplate.class;
    }

    public long getBlueprintId() {
        return this.blueprintId;
    }

    public int getVersion() {
        return this.version;
    }

    public String toString() {
        return "ModelTemplate[" + this.blueprintId + ":" + this.version + "]";
    }
}