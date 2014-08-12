package mythruna.client.gm;

import mythruna.client.GameClient;
import mythruna.db.BlueprintData;
import mythruna.es.*;
import mythruna.script.ActionReference;
import mythruna.script.ToolActions;
import org.progeeks.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class DefaultTool implements Tool {

    private static Map<String, ActionReference> actions = new HashMap();
    private ItemToolState parent;
    private GameClient client;
    private EntityId toolEntity;
    private EntityId hoverEntity;
    private String name;
    private BlueprintData icon;
    private long lastHover = -1L;
    private long minHoverTime = 1000L;

    public DefaultTool(EntityId toolEntity) {
        this.toolEntity = toolEntity;
    }

    public String getName() {
        return this.name;
    }

    public BlueprintData getIcon() {
        return this.icon;
    }

    public void updateModel() {
        ModelInfo mi = (ModelInfo) this.client.getEntityData().getComponent(this.toolEntity, ModelInfo.class);
        if (mi == null) {
            this.icon = null;
            return;
        }

        long id = mi.getBlueprintId();
        if ((this.icon == null) || (this.icon.id != id)) {
            this.icon = this.client.getWorld().getBlueprint(id);
        }
    }

    protected void loadEntity(EntityId entity) {
        System.out.println("loadEntity( " + entity + " )");

        if (ObjectUtils.areEqual(entity, this.client.getPlayer())) {
            this.name = "Hand";
            this.icon = null;
            return;
        }

        this.name = "Unknown";
        Name n = (Name) this.client.getEntityData().getComponent(entity, Name.class);
        if (n != null) {
            this.name = n.getName();
        }

        this.icon = null;
        ModelInfo mi = (ModelInfo) this.client.getEntityData().getComponent(entity, ModelInfo.class);
        if (mi != null) {
            long id = mi.getBlueprintId();
            this.icon = this.client.getWorld().getBlueprint(id);
            return;
        }

        BlueprintReference ref = (BlueprintReference) this.client.getEntityData().getComponent(entity, BlueprintReference.class);
        if (ref != null) {
            long id = ref.getBlueprintId();
            this.icon = this.client.getWorld().getBlueprint(id);
            return;
        }
    }

    public void toolAttached(ItemToolState state) {
        this.parent = state;
        this.client = state.getClient();

        loadEntity(this.toolEntity);

        state.setDefaultCrosshair();

        if (actions.isEmpty()) {
            EntityData ed = this.client.getEntityData();
            FieldFilter filter = new FieldFilter(Name.class, "name", "DefaultTool");
            EntityId toolDef = ed.findEntity(filter, new Class[0]);

            System.out.println("found:" + toolDef);

            ToolActions ta = (ToolActions) ed.getComponent(toolDef, ToolActions.class);

            System.out.println("ta:" + ta);

            for (ActionReference ar : ta.getActions()) {
                actions.put(ar.getName(), ar);
            }
        }
    }

    public void toolDetached() {
    }

    public ControlSlot[] getSlots() {
        return null;
    }

    public void mainClick(ControlSlot slot) {
        Hit hit = this.parent.getNearestHit();
        System.out.println("mainClick(" + slot + ") hit:" + hit);
        if (hit == null) {
            return;
        }
        ActionReference a = (ActionReference) actions.get("mainClick");
        System.out.println("action:" + a);

        this.client.executeRef(a, this.toolEntity, hit.toHitParameter());
    }

    public void alternateClick(ControlSlot slot) {
        Hit hit = this.parent.getNearestHit();
        System.out.println("alternateClick(" + slot + ") hit:" + hit);
        if (hit == null) {
            return;
        }
        ActionReference a = (ActionReference) actions.get("alternateClick");
        System.out.println("action:" + a);

        this.client.executeRef(a, this.toolEntity, hit.toHitParameter());
    }

    public void mainButton(boolean down, ControlSlot slot) {
        System.out.println("mainButton(" + slot + ", " + down + ") hit:" + this.parent.getNearestHit());
    }

    public void alternateButton(boolean down, ControlSlot slot) {
        System.out.println("alternateButton(" + slot + ", " + down + ") hit:" + this.parent.getNearestHit());
    }

    public boolean mainDrag(int xDelta, int yDelta, int xTotal, int yTotal, ControlSlot slot) {
        return false;
    }

    public boolean alternateDrag(int xDelta, int yDelta, int xTotal, int yTotal, ControlSlot slot) {
        return false;
    }

    public boolean hover(boolean on, ControlSlot slot) {
        Hit hit = this.parent.getNearestHit();

        ActionReference a = (ActionReference) actions.get("hover");

        EntityId hitEntity = hit == null ? null : hit.getEntity();

        if ((hitEntity != null) && (this.hoverEntity != null) && (ObjectUtils.areEqual(hitEntity, this.hoverEntity))) {
            return on;
        }
        if ((on) && (hit != null) && (hit.getEntity() != null)) {
            this.parent.setCrosshairImage("Interface/glass-orb-dark-48.png", 64.0F, 64.0F);
            this.hoverEntity = hit.getEntity();
        } else {
            this.hoverEntity = null;
            this.parent.setDefaultCrosshair();
        }

        boolean result = true;

        if ((on) && (hit != null)) {
            long time = System.currentTimeMillis();

            if (time - this.lastHover > this.minHoverTime) {
                this.lastHover = time;
                this.client.executeRef(a, this.toolEntity, hit.toHitParameter());
            } else {
                result = false;

                this.hoverEntity = null;
            }
        }

        return result;
    }

    public void roll(int delta, ControlSlot slot) {
        System.out.println("roll(" + slot + ") hit:" + this.parent.getNearestHit());
    }
}