package mythruna.es.action;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.es.*;
import mythruna.phys.proto.Mobile;
import mythruna.phys.proto.SourceLink;
import mythruna.phys.proto.TargetLink;

@Serializable
public class CreateLinkAction implements EntityAction {

    public static final int TYPE_HAND = 0;
    private EntityId source;
    private Vector3f sourceOffset;
    private Vector3f targetOffset;
    private int type;

    public CreateLinkAction() {
    }

    public CreateLinkAction(Vector3f sourceOffset, Vector3f targetOffset) {
        this(null, sourceOffset, targetOffset, 0);
    }

    public CreateLinkAction(EntityId source, Vector3f sourceOffset, Vector3f targetOffset, int type) {
        this.source = source;
        this.sourceOffset = sourceOffset;
        this.targetOffset = targetOffset;
        this.type = type;
    }

    public void runAction(EntityActionEnvironment env, EntityId target) {
        EntityData ed = env.getEntityData();
        EntityId src = null;
        EntityId link = null;

        if (this.source == null) {
            link = env.getPlayer();
            src = env.getPlayer();

            ed.setComponents(link, new EntityComponent[]{new SourceLink(src, this.sourceOffset), new TargetLink(target, this.targetOffset), new Mobile(false)});
        } else {
            link = ed.createEntity();
            src = this.source;

            ed.setComponents(link, new EntityComponent[]{new SourceLink(src, this.sourceOffset), new TargetLink(target, this.targetOffset), new Mobile(true), new CreatedBy(env.getPlayer())});
        }
    }
}