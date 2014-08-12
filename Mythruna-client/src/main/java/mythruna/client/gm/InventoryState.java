package mythruna.client.gm;

import com.jme3.app.Application;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import mythruna.client.ConveyerCamera;
import mythruna.client.GameClient;
import mythruna.client.tabs.bp.BlueprintObject;
import mythruna.client.ui.ClickAction;
import mythruna.client.ui.Command;
import mythruna.client.ui.GuiAppState;
import mythruna.client.ui.ObservableState;
import mythruna.client.view.BlockObject;
import mythruna.db.BlueprintData;
import mythruna.es.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InventoryState extends ObservableState {
    private GameClient client;
    private InputObserver inputListener = new InputObserver();

    private GridListener gridListener = new GridListener();
    private EntityId container;
    private EntitySet entities;
    private float additionalObjectScale = 1.0F;
    private ObjectGrid grid;
    private Map<EntityId, BlockObject> objectMap = new HashMap<>();

    public InventoryState(GameClient client) {
        super("ItemTool", false);
        this.client = client;

        this.container = client.getPlayer();
    }

    public void update(float tpf) {
        if (this.entities.applyChanges()) {
            addItems(this.entities.getAddedEntities());
            removeItems(this.entities.getRemovedEntities());
            updateItems(this.entities.getChangedEntities());

            this.grid.refreshView();
        }
        this.inputListener.update();
    }

    public void initialize(Application app) {
        super.initialize(app);

        float baseScale = app.getCamera().getHeight() / 720.0F;
        float maxScale = 1.0F;

        System.out.println("==================Base scale:" + baseScale);
        if (baseScale > maxScale) {
            this.additionalObjectScale = (maxScale / baseScale);

            baseScale = maxScale;
        }

        this.grid = new ObjectGrid((GuiAppState) getState(GuiAppState.class), app.getAssetManager(), "Inventory", 5, 4, 64.0F);

        this.grid.setLocalScale(baseScale);
        this.grid.setLocalTranslation(5.0F, app.getCamera().getHeight() - this.grid.getHeight() - 25.0F, 0.0F);
    }

    public void cleanup() {
    }

    protected void enable() {
        Camera cam = ((GuiAppState) getState(GuiAppState.class)).getCamera();

        ((ConveyerCamera) getState(ConveyerCamera.class)).setEnabled(false);

        cam.setLocation(new Vector3f(0.0F, 0.0F, 0.0F));
        cam.lookAtDirection(new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(0.0F, 1.0F, 0.0F));

        ((GuiAppState) getState(GuiAppState.class)).addDependent(this);
        getApplication().getInputManager().addRawInputListener(this.inputListener);

        ((GuiAppState) getState(GuiAppState.class)).getOrthoRoot().attachChild(this.grid);

        FieldFilter filter = new FieldFilter(InContainer.class, "parentId", this.container);

        this.entities = this.client.getEntityData().getEntities(filter, new Class[]{InContainer.class, ModelInfo.class});
        addItems(this.entities);
        this.grid.addCommand(this.gridListener);
        this.grid.refreshView();
    }

    protected void disable() {
        ((ConveyerCamera) getState(ConveyerCamera.class)).setEnabled(true);

        this.grid.removeCommand(this.gridListener);
        this.entities.release();
        this.objectMap.clear();
        this.grid.clear();

        getApplication().getInputManager().removeRawInputListener(this.inputListener);
        ((GuiAppState) getState(GuiAppState.class)).removeDependent(this);
        ((GuiAppState) getState(GuiAppState.class)).getOrthoRoot().detachChild(this.grid);
    }

    protected void addItems(Set<Entity> set) {
        if (set.isEmpty()) {
            return;
        }
        for (Entity e : set) {
            BlockObject icon = (BlockObject) this.objectMap.get(e.getId());
            if (icon == null) {
                System.out.println("Creating icon for:" + e);
                icon = createBlueprintIcon((ModelInfo) e.get(ModelInfo.class));
                icon.setEntity(e.getId());
                this.objectMap.put(e.getId(), icon);
            }
            this.grid.addObject(icon);
        }
    }

    protected void updateItems(Set<Entity> set) {
        if (set.isEmpty()) {
            return;
        }

        boolean modelsChanged = false;
        for (Entity e : set) {
            BlockObject icon = (BlockObject) this.objectMap.get(e.getId());
            ModelInfo mi = (ModelInfo) e.get(ModelInfo.class);
            if (mi.getBlueprintId() != icon.getBlueprintId()) {
                modelsChanged = true;
                icon.setBlueprintId(mi.getBlueprintId());
                if (!icon.isBuilt()) {
                    icon.build();
                    icon.applyUpdates(null);
                }
            }
        }
    }

    protected void removeItems(Set<Entity> set) {
        if (set.isEmpty()) {
            return;
        }
        for (Entity e : set) {
            BlockObject bo = (BlockObject) this.objectMap.remove(e);
            if (bo != null) {
                this.grid.removeObject(bo);
            }
        }
    }

    protected BlockObject createBlueprintIcon(BlueprintReference ref) {
        BlueprintData bp = this.client.getWorld().getBlueprint(ref.getBlueprintId());
        if (bp == null) {
            throw new RuntimeException("No blueprint found for id:" + ref.getBlueprintId());
        }

        BlueprintObject bo = new BlueprintObject(bp);
        bo.pack();
        bp.cells = bo.getCells();
        bp.xSize = bo.getSizeX();
        bp.ySize = bo.getSizeY();
        bp.zSize = bo.getSizeZ();

        return createIcon(bp);
    }

    protected BlockObject createBlueprintIcon(ModelInfo mi) {
        BlueprintData bp = this.client.getWorld().getBlueprint(mi.getBlueprintId());
        if (bp == null) {
            throw new RuntimeException("No blueprint found for id:" + mi.getBlueprintId());
        }
        return createIcon(bp);
    }

    protected BlockObject createIcon(BlueprintData data) {
        BlockObject bo = new BlockObject(data, false);

        float max = Math.max(data.xSize, Math.max(data.ySize, data.zSize));
        System.out.println("Max dimension:" + max);
        max = (float) (max * Math.sqrt(2.0D)) + 1.0F;

        float scale = 1.0F / max;

        System.out.println("Setting scale to:" + scale);
        bo.setScale(scale * this.additionalObjectScale);

        bo.setLighting(1.0F, 0.8F);

        bo.build();
        bo.applyUpdates(null);
        return bo;
    }

    private class InputObserver implements RawInputListener {
        private InputObserver() {
        }

        public void beginInput() {
        }

        public void endInput() {
        }

        public void onJoyAxisEvent(JoyAxisEvent evt) {
        }

        public void onJoyButtonEvent(JoyButtonEvent evt) {
        }

        public void onKeyEvent(KeyInputEvent evt) {
        }

        public void onMouseButtonEvent(MouseButtonEvent evt) {
        }

        protected void resetHover(long time) {
        }

        public void update() {
        }

        public void onMouseMotionEvent(MouseMotionEvent evt) {
        }

        public void onTouchEvent(TouchEvent evt) {
        }
    }

    private class GridListener implements Command<ClickAction> {
        private GridListener() {
        }

        public void execute(Object source, ClickAction action) {
            System.out.println("*(***** :" + action);

            if (action == ClickAction.Enter) {
                BlockObject bo = (BlockObject) InventoryState.this.grid.getSelectionModel().getSelected();
                if (bo == null)
                    ((ItemToolState) InventoryState.this.getState(ItemToolState.class)).setToolEntity(null);
                else
                    ((ItemToolState) InventoryState.this.getState(ItemToolState.class)).setToolEntity(bo.getEntity());
            }
        }
    }
}