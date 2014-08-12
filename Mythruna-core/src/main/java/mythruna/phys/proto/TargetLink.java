package mythruna.phys.proto;

import com.jme3.math.Vector3f;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;
import mythruna.es.PersistentComponent;

public class TargetLink implements EntityComponent, PersistentComponent {

    private EntityId target;
    private Vector3f offset;

    public TargetLink() {
    }

    public TargetLink(EntityId target, Vector3f offset) {
        this.target = target;
        this.offset = offset;
    }

    public Class<TargetLink> getType() {
        return TargetLink.class;
    }

    public EntityId getTarget() {
        return this.target;
    }

    public Vector3f getOffset() {
        return this.offset;
    }

    public String toString() {
        return "TargetLink[" + this.target + " @ " + this.offset + "]";
    }
}