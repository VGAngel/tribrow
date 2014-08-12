package mythruna.es.action;

import com.jme3.network.serializing.Serializable;
import mythruna.World;
import mythruna.es.*;

@Serializable
public class RemoveBlueprintAction implements EntityAction {

    public RemoveBlueprintAction() {
    }

    public void runAction(EntityActionEnvironment env, EntityId target) {
        World world = env.getWorld();
        EntityData ed = env.getEntityData();

        if (target != null) {
            InContainer in = (InContainer) ed.getComponent(target, InContainer.class);
            if (!in.getParentId().equals(env.getPlayer())) {
                throw new RuntimeException("Player ID mismatches for item.");
            }
            ed.removeEntity(target);
        }
    }
}