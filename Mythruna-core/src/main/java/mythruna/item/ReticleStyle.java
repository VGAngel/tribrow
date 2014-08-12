package mythruna.item;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

@Serializable
public class ReticleStyle implements EntityComponent {

    private EntityId id;
    private ReticleType type;
    private String text;

    public ReticleStyle() {
    }

    public ReticleStyle(EntityId id, ReticleType type, String text) {
        this.id = id;
        this.type = type;
        this.text = text;
    }

    public Class<ReticleStyle> getType() {
        return ReticleStyle.class;
    }

    public EntityId getId() {
        return this.id;
    }

    public ReticleType getReticleType() {
        return this.type;
    }

    public String getText() {
        return this.text;
    }

    public String toString() {
        return "ReticleStyle[" + this.type + ", " + this.text + "]";
    }
}