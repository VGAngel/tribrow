package mythruna.es.action;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityAction;
import mythruna.es.EntityActionEnvironment;
import mythruna.es.EntityData;
import mythruna.es.EntityId;
import mythruna.phys.proto.SourceLink;
import mythruna.phys.proto.TargetLink;

@Serializable
public class RemoveLinkAction implements EntityAction {

    public RemoveLinkAction() {
    }

    public void runAction(EntityActionEnvironment env, EntityId target) {
        EntityData ed = env.getEntityData();

        System.out.println("Removing components from:" + target);
        ed.removeComponent(target, TargetLink.class);
        ed.removeComponent(target, SourceLink.class);
    }
}