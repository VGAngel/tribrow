package mythruna.es.action;

import com.jme3.network.serializing.Serializable;
import mythruna.World;
import mythruna.db.BlueprintData;
import mythruna.es.*;

@Serializable
public class SaveBlueprintAction implements EntityAction {

    private EntityId itemId;
    private String name;
    private short xSize;
    private short ySize;
    private short zSize;
    private int[][][] cells;
    private float scale;

    public SaveBlueprintAction() {
    }

    public SaveBlueprintAction(EntityId itemId, String name, int xSize, int ySize, int zSize, float scale, int[][][] cells) {
        this.itemId = itemId;
        this.name = name;
        this.xSize = (short) xSize;
        this.ySize = (short) ySize;
        this.zSize = (short) zSize;
        this.cells = cells;
        this.scale = scale;
    }

    public void runAction(EntityActionEnvironment env, EntityId target) {
        World world = env.getWorld();
        EntityData ed = env.getEntityData();

        BlueprintData bp = world.createBlueprint(this.name, this.xSize, this.ySize, this.zSize, this.scale, this.cells);

        if (this.itemId != null) {
            InContainer in = (InContainer) ed.getComponent(this.itemId, InContainer.class);
            if (!in.getParentId().equals(env.getPlayer())) {
                throw new RuntimeException("Player ID mismatches for item.");
            }
            ed.setComponents(this.itemId, new EntityComponent[]{new BlueprintReference(bp.id)});
        } else {
            EntityId item = ed.createEntity();
            ed.setComponents(item, new EntityComponent[]{new BlueprintReference(bp.id), new InContainer(env.getPlayer(), 0)});
        }
    }
}