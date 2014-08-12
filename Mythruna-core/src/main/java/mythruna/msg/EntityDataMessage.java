package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.Entity;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

import java.util.Arrays;
import java.util.List;

@Serializable
public class EntityDataMessage extends AbstractMessage {

    private int setId;
    private ComponentData[] data;

    public EntityDataMessage() {
    }

    public EntityDataMessage(int setId, List<ComponentData> list) {
        this.setId = setId;
        this.data = ((ComponentData[]) list.toArray(new ComponentData[list.size()]));
    }

    public int getSetId() {
        return this.setId;
    }

    public ComponentData[] getData() {
        return this.data;
    }

    public String toString() {
        return "EntityDataMessage[" + this.setId + ", " + Arrays.asList(this.data) + "]";
    }

    @Serializable
    public static final class ComponentData {
        private EntityId entity;
        private EntityComponent[] components;

        public ComponentData() {
        }

        public ComponentData(Entity entity) {
            this(entity.getId(), entity.getComponents());
        }

        public ComponentData(EntityId entity, EntityComponent[] components) {
            this.entity = entity;
            this.components = components;
        }

        public EntityId getEntityId() {
            return this.entity;
        }

        public EntityComponent[] getComponents() {
            return this.components;
        }

        public String toString() {
            if (this.components == null)
                return "ComponentData[" + this.entity + ", null ]";
            return "ComponentData[" + this.entity + ", " + Arrays.asList(this.components) + "]";
        }
    }
}