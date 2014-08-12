package mythruna.es.action;

import com.jme3.network.serializing.Serializable;
import mythruna.es.*;

@Serializable
public class MoveObjectAction implements EntityAction {

    private Position position;

    public MoveObjectAction() {
    }

    public MoveObjectAction(Position position) {
        this.position = position;
    }

    public void runAction(EntityActionEnvironment env, EntityId target) {
        if (env.getPerms().canMoveObject(this.position.getLocation(), target)) {
            EntityData ed = env.getEntityData();
            ed.setComponent(target, this.position);
        }
    }
}