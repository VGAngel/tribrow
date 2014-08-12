package mythruna.phys.proto;

import com.jme3.math.Vector3f;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;
import mythruna.es.PersistentComponent;

public class SourceLink implements EntityComponent, PersistentComponent {

    private EntityId source;
    private Vector3f offset;

    public SourceLink() {
    }

    public SourceLink(EntityId source, Vector3f offset) {
        this.source = source;
        this.offset = offset;
    }

    public Class<SourceLink> getType() {
        return SourceLink.class;
    }

    public EntityId getSource() {
        return this.source;
    }

    public Vector3f getOffset() {
        return this.offset;
    }

    public String toString() {
        return "SourceLink[" + this.source + " @ " + this.offset + "]";
    }
}