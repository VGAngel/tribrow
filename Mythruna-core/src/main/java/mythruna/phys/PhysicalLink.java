package mythruna.phys;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

@Serializable
public class PhysicalLink implements EntityComponent {

    private EntityId source;
    private Vector3f sourceOffset;
    private EntityId target;
    private Vector3f targetOffset;

    public PhysicalLink() {
    }

    public PhysicalLink(EntityId source, Vector3f sourceOffset, EntityId target, Vector3f targetOffset) {
        this.source = source;
        this.sourceOffset = sourceOffset;
        this.target = target;
        this.targetOffset = targetOffset;
    }

    public Class<PhysicalLink> getType() {
        return PhysicalLink.class;
    }

    public EntityId getSource() {
        return this.source;
    }

    public Vector3f getSourceOffset() {
        return this.sourceOffset;
    }

    public EntityId getTarget() {
        return this.target;
    }

    public Vector3f getTargetOffset() {
        return this.targetOffset;
    }

    public String toString() {
        return "Link[" + this.source + ":" + this.sourceOffset + ", " + this.target + ":" + this.targetOffset + "]";
    }
}