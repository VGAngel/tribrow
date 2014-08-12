package mythruna.client.gm;

import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import mythruna.client.*;
import mythruna.client.anim.Animation;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import mythruna.client.ui.*;
import mythruna.client.view.BlockObject;
import mythruna.client.view.DragControl;
import mythruna.client.view.LocalArea;
import mythruna.db.BlueprintData;
import mythruna.es.*;
import mythruna.item.HeldEntities;
import mythruna.script.EntityParameter;
import org.progeeks.util.ObjectUtils;

public class ItemToolState extends ObservableState {
    private GameClient client;
    private EntitySet us;
    private LocalArea localArea;
    private Tool defaultTool;
    private Node hud;
    private ToolOrb leftOrb;
    private ToolOrb rightOrb;
    private float imageSize;
    private float margin;
    private float additionalObjectScale = 1.0F;

    private KeyMethodAction inventoryKey = new KeyMethodAction(this, "toggleInventory", 23);

    private boolean inventoryEnabled = false;

    private ControlSlot slot = ControlSlot.RightHand;
    private KeyMethodAction slotKey = new KeyMethodAction(this, "setLeftHandOn", 29, true);

    private ToolSlot[] slots = new ToolSlot[2];

    private Node perspectiveHud = null;
    private Quaternion lastCameraRotation;
    private EntitySet entities;

    public ItemToolState(GameClient client, LocalArea localArea, Node hud) {
        super("ItemTool", false);
        this.client = client;
        this.localArea = localArea;
        this.hud = hud;

        this.defaultTool = new DefaultTool(client.getPlayer());

        this.slots[ControlSlot.RightHand.ordinal()] = new ToolSlot();
        this.slots[ControlSlot.LeftHand.ordinal()] = new ToolSlot();
    }

    public GameClient getClient() {
        return this.client;
    }

    public LocalArea getLocalArea() {
        return this.localArea;
    }

    public void setInventoryEnabled(boolean b) {
        this.inventoryEnabled = b;
        if (b) {
            ((PopupMenuState) getState(PopupMenuState.class)).setEnabled(false);
        }
        ((InventoryState) getState(InventoryState.class)).setEnabled(b);
        ((ToolInputState) getState(ToolInputState.class)).setEnabled(!b);
    }

    public boolean isInventoryEnabled() {
        return ((InventoryState) getState(InventoryState.class)).isEnabled();
    }

    public void toggleInventory() {
        setInventoryEnabled(!isInventoryEnabled());
    }

    public void setLeftHandOn(boolean f) {
        System.out.println("==================setLeftHandOn(" + f + ")");
        if (f)
            this.slot = ControlSlot.LeftHand;
        else
            this.slot = ControlSlot.RightHand;
    }

    public void setToolEntity(EntityId entity) {
        setToolEntity(this.slot, entity);
    }

    public void setToolEntity(ControlSlot s, EntityId entity) {
        if (s == ControlSlot.LeftHand) {
            this.client.execute("setLeftTool", null, new EntityParameter(entity));
        } else {
            this.client.execute("setRightTool", null, new EntityParameter(entity));
        }
    }

    protected ToolSlot getToolSlot(ControlSlot s) {
        return this.slots[s.ordinal()];
    }

    protected ToolSlot getToolSlot() {
        return this.slots[this.slot.ordinal()];
    }

    protected ControlSlot getSlot(ToolOrb orb) {
        if (getToolSlot(ControlSlot.LeftHand).orb == orb)
            return ControlSlot.LeftHand;
        if (getToolSlot(ControlSlot.RightHand).orb == orb)
            return ControlSlot.RightHand;
        return null;
    }

    protected ToolSlot getToolSlot(ToolOrb orb) {
        for (ToolSlot s : this.slots) {
            if (s.orb == orb)
                return s;
        }
        return null;
    }

    protected void setTools(EntityId left, EntityId right) {
        getToolSlot(ControlSlot.LeftHand).setToolEntity(left);
        getToolSlot(ControlSlot.RightHand).setToolEntity(right);
    }

    protected Tool getTool() {
        return getToolSlot().getTool();
    }

    protected void updateSlots() {
        Entity e = this.us.getEntity(this.client.getPlayer());
        System.out.println("-------------------updateslots() e:" + e);
        if (e == null) {
            setTools(null, null);
            return;
        }

        HeldEntities held = (HeldEntities) e.get(HeldEntities.class);
        setTools(held.getLeft(), held.getRight());
    }

    public void modelsChanged() {
        if (!this.entities.containsId(this.slots[0].toolEntity))
            this.slots[0].setToolEntity(null);
        else {
            this.slots[0].getTool().updateModel();
        }
        if (!this.entities.containsId(this.slots[1].toolEntity))
            this.slots[1].setToolEntity(null);
        else
            this.slots[1].getTool().updateModel();
    }

    public void update(float tpf) {
        if (this.us.applyChanges()) {
            updateSlots();
        }

        if (this.entities.applyChanges()) {
            modelsChanged();
        }

        this.slots[0].update(tpf);
        this.slots[1].update(tpf);

        Camera cam = getApplication().getCamera();

        if ((this.lastCameraRotation == null) || (!this.lastCameraRotation.equals(cam.getRotation()))) {
            this.lastCameraRotation = cam.getRotation().clone();
            this.perspectiveHud.setLocalRotation(cam.getRotation());
        }
    }

    public void initialize(Application app) {
        super.initialize(app);

        float baseScale = app.getCamera().getHeight() / 720.0F;

        float maxScale = 1.2F;
        if (baseScale > maxScale) {
            this.additionalObjectScale = (maxScale / baseScale);
            baseScale = maxScale;
        }

        this.imageSize = (baseScale * 80.0F);
        this.margin = (baseScale * 5.0F);

        GuiAppState gui = (GuiAppState) getState(GuiAppState.class);

        this.perspectiveHud = new Node("PerspectiveHud");

        this.rightOrb = new ToolOrb(app.getAssetManager(), this.imageSize, gui, gui.getPerspectiveRoot());
        this.rightOrb.setLocalTranslation(app.getCamera().getWidth() - this.imageSize - this.margin, this.margin, 0.0F);
        getToolSlot(ControlSlot.RightHand).setOrb(this.rightOrb);

        GuiUtils.addCommand(this.rightOrb, new RemoveToolCommand());

        this.leftOrb = new ToolOrb(app.getAssetManager(), this.imageSize, gui, gui.getPerspectiveRoot());
        this.leftOrb.setLocalTranslation(this.margin, this.margin, 0.0F);
        getToolSlot(ControlSlot.LeftHand).setOrb(this.leftOrb);

        GuiUtils.addCommand(this.leftOrb, new RemoveToolCommand());

        app.getStateManager().attach(new InventoryState(this.client));
        app.getStateManager().attach(new ToolInputState());

        FieldFilter filter = new FieldFilter(HeldEntities.class, "id", this.client.getPlayer());
        this.us = this.client.getEntityData().getEntities(filter, new Class[]{HeldEntities.class});
        updateSlots();
    }

    public void setDefaultCrosshair() {
        ((CrosshairState) getState(CrosshairState.class)).setImage("Interface/glass-orb-dark-48.png", 32.0F, 32.0F);
        ((CrosshairState) getState(CrosshairState.class)).setStyle(null);
    }

    public void setCrosshairImage(String name, float width, float height) {
        ((CrosshairState) getState(CrosshairState.class)).setImage(name, width, height);
    }

    public void cleanup() {
    }

    protected void enable() {
        System.out.println("ItemToolState enabled");
        ((DebugHudState) getState(DebugHudState.class)).setBottom(false);
        ((GameAppState) getState(GameAppState.class)).setStatsOnBottom(false);
        ((GameAppState) getState(GameAppState.class)).setMessageMargin(this.imageSize + this.margin);

        ((GuiAppState) getState(GuiAppState.class)).addDependent(this);

        Node rootNode = ((MainStart) getApplication()).getRootNode();
        rootNode.attachChild(this.perspectiveHud);

        for (ToolSlot s : this.slots) {
            s.attach();
        }
        ((GuiAppState) getState(GuiAppState.class)).getOverlayRoot().attachChild(this.leftOrb);
        ((GuiAppState) getState(GuiAppState.class)).getOverlayRoot().attachChild(this.rightOrb);

        this.inventoryKey.attach(getApplication().getInputManager());
        this.slotKey.attach(getApplication().getInputManager());

        if (this.inventoryEnabled)
            ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.enable(getState(InventoryState.class), true, 0.25F)});
        else {
            ((ToolInputState) getState(ToolInputState.class)).setEnabled(true);
        }

        FieldFilter filter = new FieldFilter(InContainer.class, "parentId", this.client.getPlayer());
        this.entities = this.client.getEntityData().getEntities(filter, new Class[]{InContainer.class, ModelInfo.class});
    }

    protected void disable() {
        System.out.println("ItemToolState disabled");

        this.entities.release();

        ((GuiAppState) getState(GuiAppState.class)).removeDependent(this);

        for (ToolSlot s : this.slots) {
            s.detach();
        }
        Node rootNode = ((MainStart) getApplication()).getRootNode();
        rootNode.detachChild(this.perspectiveHud);

        ((GuiAppState) getState(GuiAppState.class)).getOverlayRoot().detachChild(this.leftOrb);
        ((GuiAppState) getState(GuiAppState.class)).getOverlayRoot().detachChild(this.rightOrb);

        this.inventoryKey.detach(getApplication().getInputManager());
        this.slotKey.detach(getApplication().getInputManager());

        ((InventoryState) getState(InventoryState.class)).setEnabled(false);
        ((ToolInputState) getState(ToolInputState.class)).setEnabled(false);
    }

    protected WorldIntersector.Intersection intersectWorld(Vector3f pos, Vector3f dir) {
        Ray ray = new Ray(pos, dir);
        ray.setLimit(10.0F);

        WorldIntersector wi = new WorldIntersector(this.localArea, ray, new Integer[0]);
        WorldIntersector.Intersection hit = null;
        if (wi.hasNext()) {
            WorldIntersector.Intersection isect = wi.next();

            hit = isect;
        }

        return hit;
    }

    protected CollisionResult intersectObjects(Vector3f pos, Vector3f dir) {
        WorldIntersector.Intersection hit = intersectWorld(pos, dir);

        return intersectObjects(pos, dir, hit);
    }

    protected CollisionResult intersectObjects(Vector3f pos, Vector3f dir, WorldIntersector.Intersection limit) {
        Ray ray = new Ray(pos.clone(), dir.clone());
        if (limit != null) {
            Vector3f delta = limit.getPoint().subtract(pos);
            ray.setLimit(delta.length());
        } else {
            ray.setLimit(20.0F);
        }

        long start = System.nanoTime();

        CollisionResults results = new CollisionResults();

        ray.origin = new Vector3f(0.0F, 0.0F, 0.0F);

        float y = ray.direction.z;
        ray.direction.z = ray.direction.y;
        ray.direction.y = y;

        Node target = this.localArea;
        target.updateGeometricState();

        target.collideWith(ray, results);
        long end = System.nanoTime();
        for (int i = 0; i < results.size(); i++) {
            float d = results.getCollision(i).getDistance();
            Node parent = results.getCollision(i).getGeometry().getParent();
            Long id = (Long) parent.getUserData("id");
            if ((id != null) || (parent.getControl(DragControl.class) != null)) {
                return results.getCollision(i);
            }
        }

        return null;
    }

    protected void swapYZ(Vector3f v) {
        float z = v.z;
        v.z = v.y;
        v.y = z;
    }

    public Hit getNearestHit() {
        Vector3f pos = this.client.getLocation();
        Vector3f dir = this.client.getFacing().mult(new Vector3f(0.0F, 0.0F, 1.0F));
        swapYZ(dir);

        WorldIntersector.Intersection hit = intersectWorld(pos, dir);
        CollisionResult collision = intersectObjects(pos, dir, hit);

        Vector3f contact = null;
        Vector3f normal = null;
        EntityId entity = null;
        int cellType = hit != null ? hit.getType() : -1;

        if (collision != null) {
            Node parent = collision.getGeometry().getParent();
            Long id = (Long) parent.getUserData("id");
            if (id != null) {
                entity = new EntityId(id.longValue());
            }
            contact = collision.getContactPoint();
            swapYZ(contact);

            normal = collision.getContactNormal();
            swapYZ(normal);

            return new Hit(contact, normal, entity, cellType);
        }
        if (hit != null) {
            if (hit.getSide() == 4)
                normal = new Vector3f(0.0F, 0.0F, 1.0F);
            contact = hit.getPoint();
            return new Hit(contact, normal, entity, cellType);
        }

        return null;
    }

    public void mainClick() {
        getTool().mainClick(this.slot);
    }

    public void alternateClick() {
        getTool().alternateClick(this.slot);
    }

    public void mainButton(boolean down) {
        getTool().mainButton(down, this.slot);
    }

    public void alternateButton(boolean down) {
        getTool().alternateButton(down, this.slot);
    }

    public boolean mainDrag(int xDelta, int yDelta, int xTotal, int yTotal) {
        return getTool().mainDrag(xDelta, yDelta, xTotal, yTotal, this.slot);
    }

    public boolean alternateDrag(int xDelta, int yDelta, int xTotal, int yTotal) {
        return getTool().alternateDrag(xDelta, yDelta, xTotal, yTotal, this.slot);
    }

    public void roll(int amount) {
        getTool().roll(amount, this.slot);
    }

    public boolean hover(boolean on) {
        return getTool().hover(on, this.slot);
    }

    protected BlockObject createIcon(BlueprintData data) {
        if (data == null) {
            return null;
        }
        BlockObject bo = new BlockObject(data, false);

        float max = Math.max(data.xSize, Math.max(data.ySize, data.zSize));
        max = (float) (max * Math.sqrt(2.0D)) + 1.0F;

        float scale = 1.0F / max;

        System.out.println("Setting scale to:" + scale);
        bo.setScale(scale * this.additionalObjectScale);

        bo.setLighting(1.0F, 0.8F);

        bo.build();
        bo.applyUpdates(null);
        return bo;
    }

    private class ToolSlot {
        private ToolOrb orb;
        private Tool tool;
        private EntityId toolEntity;
        private String name;
        private BlueprintData icon;

        public ToolSlot() {
            setToolEntity(null);
            setTool(ItemToolState.this.defaultTool);
        }

        public void update(float tpf) {
            if (this.orb != null) {
                if (this.name != this.tool.getName())
                    this.orb.setName(this.tool.getName());
                if (this.icon != this.tool.getIcon()) {
                    this.orb.setIcon(ItemToolState.this.createIcon(this.tool.getIcon()));
                }
                this.name = this.tool.getName();
                this.icon = this.tool.getIcon();
            }
        }

        public void setOrb(ToolOrb orb) {
            this.orb = orb;
        }

        public void attach() {
            this.tool.toolAttached(ItemToolState.this);
        }

        public void detach() {
            this.tool.toolDetached();
        }

        public void setTool(Tool tool) {
            if (ObjectUtils.areEqual(this.tool, tool)) {
                return;
            }
            if (this.tool != null) {
                this.tool.toolDetached();
            }
            this.tool = tool;
            if (this.tool == null) {
                this.tool = ItemToolState.this.defaultTool;
            }
            if (ItemToolState.this.isEnabled())
                this.tool.toolAttached(ItemToolState.this);
        }

        public Tool getTool() {
            return this.tool;
        }

        public void setToolEntity(EntityId entity) {
            if (ObjectUtils.areEqual(entity, this.toolEntity)) {
                return;
            }
            this.toolEntity = entity;

            if (this.toolEntity == null) {
                setTool(ItemToolState.this.defaultTool);
                return;
            }

            setTool(new DefaultTool(entity));
        }
    }

    private class RemoveToolCommand
            implements Command<ClickAction> {
        private RemoveToolCommand() {
        }

        public void execute(Object source, ClickAction a) {
            if (a == ClickAction.Enter) {
                ToolOrb orb = (ToolOrb) source;
                ItemToolState.this.setToolEntity(ItemToolState.this.getSlot(orb), null);
            }
        }
    }
}