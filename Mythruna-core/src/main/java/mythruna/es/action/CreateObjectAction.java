package mythruna.es.action;

import com.jme3.network.serializing.Serializable;
import mythruna.es.*;

@Serializable
public class CreateObjectAction implements EntityAction {

    private ModelInfo modelInfo;
    private Position position;

    public CreateObjectAction() {
    }

    public CreateObjectAction(ModelInfo modelInfo, Position position) {
        this.modelInfo = modelInfo;
        this.position = position;
    }

    public void runAction(EntityActionEnvironment env, EntityId target) {
        EntityData ed = env.getEntityData();

        EntityId entityId = ed.createEntity();

        if (env.getPerms().canAddObject(this.position.getLocation())) {
            ed.setComponents(entityId, new EntityComponent[]{this.position, this.modelInfo, new CreatedBy(env.getPlayer())});

            env.echo("Object created at:" + this.position.getLocation());
        } else {
            env.echo("You do not have permission to place an object here.");
        }
    }
}