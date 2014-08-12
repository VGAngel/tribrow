package mythruna.es.action;

import com.jme3.network.serializing.Serializable;
import mythruna.es.*;

@Serializable
public class CreateBlueprintItemAction implements EntityAction {

    private long blueprintId;

    public CreateBlueprintItemAction() {
    }

    public CreateBlueprintItemAction(long blueprintId) {
        this.blueprintId = blueprintId;
    }

    public void runAction(EntityActionEnvironment env, EntityId target) {
        EntityData ed = env.getEntityData();

        EntityId item = ed.createEntity();
        ed.setComponents(item, new EntityComponent[]{new BlueprintReference(this.blueprintId), new InContainer(env.getPlayer(), 0)});
    }
}