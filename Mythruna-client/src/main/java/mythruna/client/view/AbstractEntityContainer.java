package mythruna.client.view;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import mythruna.Coordinates;
import mythruna.es.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEntityContainer<T> extends AbstractControl {
    private EntityData ed;
    private int x;
    private int y;
    protected int xBase;
    protected int yBase;
    protected int zBase;
    private long columnId;
    protected EntitySet entities;
    private Map<EntityId, T> children = new HashMap<>();
    private Class[] types;

    protected AbstractEntityContainer(EntityData ed, int x, int y, Class[] types) {
        super.setEnabled(false);
        if ((types == null) || (types.length == 0))
            throw new IllegalArgumentException("Must specify types.");
        this.ed = ed;
        this.x = x;
        this.y = y;
        this.columnId = Coordinates.leafToColumnId(x, y);
        this.types = types;

        this.xBase = Coordinates.leafToWorld(x);
        this.yBase = Coordinates.leafToWorld(y);
        this.zBase = 0;
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("This control cannot be cloned.");
    }

    protected void initialize() {
        FieldFilter<Position> filter = new FieldFilter<>(Position.class, "columnId", this.columnId);
        this.entities = getEntityData().getEntities(filter, this.types);
        addChildren(this.entities);
    }

    protected void terminate() {
        removeChildren(this.entities);
        this.entities.release();
        this.entities = null;
    }

    protected EntityData getEntityData() {
        return this.ed;
    }

    protected abstract T createSpatial(Entity paramEntity);

    protected abstract Node adjustToLocal(Entity paramEntity);

    protected abstract void removeChild(Entity paramEntity, T paramT);

    protected T getChild(Entity e, boolean create) {
        Object result = this.children.get(e.getId());
        if ((result == null) && (create)) {
            result = createSpatial(e);
            this.children.put(e.getId(), (T) result);
        }
        return (T) result;
    }

    protected void addChildren(Set<Entity> set) {
        for (Entity e : set) {
            Node n = adjustToLocal(e);

            ((Node) this.spatial).attachChild(n);
        }
    }

    protected void updateChildren(Set<Entity> set) {
        for (Entity e : set) {
            Node n = adjustToLocal(e);
        }
    }

    protected void removeChildren(Set<Entity> set) {
        for (Entity e : set) {
            Object child = this.children.remove(e.getId());
            removeChild(e, (T) child);
        }
    }

    public int getChildCount() {
        if (this.entities == null)
            return 0;
        return this.entities.size();
    }

    protected void updateAll() {
        if (this.entities != null) {
            updateChildren(this.entities);
        }
    }

    public void setEnabled(boolean b) {
        if (isEnabled() == b)
            return;
        super.setEnabled(b);

        if (b)
            initialize();
        else
            terminate();
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    protected void controlUpdate(float tpf) {
        long start = System.nanoTime();
        boolean hasChanges = this.entities.applyChanges();
        long end = System.nanoTime();
        if (hasChanges) {
            if (end - start > 2000000L)
                System.out.println(getClass().getSimpleName() + " apply changes time:" + (end - start / 1000000.0D) + " ms");
            removeChildren(this.entities.getRemovedEntities());
            addChildren(this.entities.getAddedEntities());
            updateChildren(this.entities.getChangedEntities());
        }
    }
}