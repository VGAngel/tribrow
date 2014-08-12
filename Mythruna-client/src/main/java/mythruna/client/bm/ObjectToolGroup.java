package mythruna.client.bm;

import mythruna.es.*;
import org.progeeks.util.log.Log;

import java.util.Map;
import java.util.TreeMap;

public class ObjectToolGroup extends ToolGroup {
    static Log log = Log.getLog();
    private EntitySet blueprints;
    private Map<EntityId, ObjectTool> toolMap = new TreeMap();

    public ObjectToolGroup(String name) {
        super(name);
    }

    public void initialize(ObjectSelector selector) {
        EntityData ed = selector.getWorld().getEntityData();
        this.blueprints = ed.getEntities(new FieldFilter(InContainer.class, "parentId", selector.getGameClient().getPlayer()), new Class[]{InContainer.class, BlueprintReference.class});

        for (Entity e : this.blueprints) {
            this.toolMap.put(e.getId(), new ObjectTool(e));
        }

        getTools().addAll(this.toolMap.values());

        int index = 0;
        for (Tool t : getTools()) {
            ((ObjectTool) t).setName("Object(" + index++ + ")");
        }

        super.initialize(selector);
    }

    public boolean update() {
        if (this.blueprints.applyChanges()) {
            if (log.isTraceEnabled())
                log.trace("******* There are blueprint changes...");
            if (log.isTraceEnabled()) {
                log.trace("All:" + this.blueprints);
            }

            for (Entity e : this.blueprints.getAddedEntities()) {
                if (log.isTraceEnabled())
                    log.trace("  added:" + e);
                ObjectTool tool = new ObjectTool(e);
                this.toolMap.put(e.getId(), tool);
                tool.initialize(getSelector());
            }
            for (Entity e : this.blueprints.getChangedEntities()) {
                if (log.isTraceEnabled())
                    log.trace("  changed:" + e);
                ObjectTool tool = (ObjectTool) this.toolMap.get(e.getId());
                tool.update();
            }
            for (Entity e : this.blueprints.getRemovedEntities()) {
                if (log.isTraceEnabled()) {
                    log.trace("  removed:" + e);
                }

                ObjectTool tool = (ObjectTool) this.toolMap.remove(e.getId());
                if (tool == null) ;
            }

            getTools().clear();
            getTools().addAll(this.toolMap.values());

            int index = 0;
            for (Tool t : getTools()) {
                ((ObjectTool) t).setName("Object(" + index++ + ")");
            }

            return true;
        }
        return false;
    }

    public String toString() {
        return "ObjectToolGroup[" + getName() + "]";
    }
}