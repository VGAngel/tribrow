package mythruna.client.tabs.bp;

import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.controls.*;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.texture.Texture;
import mythruna.BlockType;
import mythruna.client.*;
import mythruna.client.anim.Animation;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import mythruna.client.tabs.TabState;
import mythruna.client.ui.*;
import mythruna.db.BlueprintData;
import mythruna.es.EntityAction;
import mythruna.es.EntityId;
import mythruna.es.action.RemoveBlueprintAction;
import mythruna.es.action.SaveBlueprintAction;
import mythruna.geom.Trifold;
import org.progeeks.util.ObjectUtils;

public class BlueprintEditorState extends ObservableState {
    private static final String BP_LEFT = "BP_PanLeft";
    private static final String BP_RIGHT = "BP_PanRight";
    private static final String BP_UP = "BP_PanUp";
    private static final String BP_DOWN = "BP_PanDown";
    private static final String BP_FORWARD = "BP_Forward";
    private static final String BP_BACK = "BP_Back";
    private static final String BP_STRAFE_LEFT = "BP_StrafeLeft";
    private static final String BP_STRAFE_RIGHT = "BP_StrafeRight";
    private static final String BP_RAISE = "BP_Raise";
    private static final String BP_LOWER = "BP_Lower";
    private static final String BP_JUMP = "BP_Jump";
    private static final String BP_RUN = "BP_Run";
    private static final String BP_ADJUST_ITEM = "BP Adjust Sub-type";
    private static final String BP_ADJUST_TYPE = "BP Adjust Type";
    private static final String BP_ITEM_UP = "BP Item Up";
    private static final String BP_ITEM_DOWN = "BP Item Down";
    private static final String BP_TYPE_UP = "BP Type Up";
    private static final String BP_TYPE_DOWN = "BP Type Down";
    private static final String BP_CLONE_TYPE = "BP Clone Type";
    private static final String BP_SELECT = "BP_Select";
    private static final String BP_PLACE = "BP_Place";
    private static final String[] BP_MAPPINGS = {"BP_PanLeft", "BP_PanRight", "BP_PanUp", "BP_PanDown", "BP_Forward", "BP_Back", "BP_StrafeLeft", "BP_StrafeRight", "BP_Raise", "BP_Lower", "BP_Jump", "BP_Run", "BP Item Up", "BP Item Down", "BP_Select", "BP_Place", "BP Adjust Sub-type", "BP Type Up", "BP Type Down", "BP Clone Type"};
    private GameClient gameClient;
    private InputHandler inputHandler;
    private float yaw = 2.356195F;
    private float pitch = 0.3926991F;
    private int bpSize = 10;
    private float distance = 12.0F;
    private float cameraDistance = 20.0F;
    private float setback = 0.0F;
    private float cameraElevation = 3.0F;

    private float buttonScale = 1.0F;
    private Workbench wb;
    private Node workbenchNode;
    private Avatar avatar;
    private float lastHeadAngle = 0.0F;
    private BlockIconSelection selector;
    private BlueprintIconSelection blueprintSelector;
    private BlueprintObject workingBlueprint;
    private EntityId workingBlueprintEntityId;
    private Palette palette;
    private PalettePage page = null;
    private int itemType = -1;

    private AnimationTask paletteMove = null;
    private Node buttons;
    private Geometry selectedBlock;
    private float blockSelectionUpdateTime = 0.0F;
    private BlueprintIntersector.Intersection lastHit = null;
    private Button[] paletteButtons;
    private KeyMethodAction rotate = new KeyMethodAction(this, "rotate", 19);
    private KeyMethodAction mirror = new KeyMethodAction(this, "mirror", 20);

    private float hitUpdateTime = 0.0F;

    public BlueprintEditorState(GameClient gameClient) {
        super("Blueprints", false);
        this.gameClient = gameClient;
        this.inputHandler = new InputHandler();
    }

    public void toggleEnabled() {
        setEnabled(!isEnabled());
    }

    public void rotate() {
        System.out.println("Rotate blueprint...");
        System.out.println("Working blueprint:" + this.workingBlueprint);
        System.out.println("Working blueprint entityId:" + this.workingBlueprintEntityId);
        this.workingBlueprint.rotate();
        this.workingBlueprint.regenerateGeometry();
    }

    public void mirror() {
        System.out.println("Mirror blueprint...");
        System.out.println("Working blueprint:" + this.workingBlueprint);
        System.out.println("Working blueprint entityId:" + this.workingBlueprintEntityId);
        this.workingBlueprint.mirror();
        this.workingBlueprint.regenerateGeometry();
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.blueprintSelector = new BlueprintIconSelection(this, app, this.gameClient);

        Camera cam = app.getCamera();
        this.buttonScale = Math.min(1.0F, cam.getWidth() / 960.0F);

        this.selector = new BlockIconSelection(app.getAssetManager());

        KeyMethodAction tab = new KeyMethodAction(this, "toggleEnabled", 48);
        tab.attach(app.getInputManager());

        this.workingBlueprint = new BlueprintObject(this.bpSize, this.bpSize, this.bpSize);

        this.wb = new Workbench(this.bpSize);
        this.workbenchNode = this.wb.generateNode(0.8F, 0.8F);

        this.workbenchNode.attachChild(this.workingBlueprint.getNode());

        WireBox boxMesh = new WireBox(0.51F, 0.51F, 0.51F);
        this.selectedBlock = new Geometry("Selected Block", boxMesh);
        Material boxMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        boxMaterial.setColor("Color", ColorRGBA.Yellow);
        this.selectedBlock.setMaterial(boxMaterial);

        this.avatar = new Avatar(app.getAssetManager(), "");
        this.avatar.setLocalTranslation(0.0F, this.avatar.getHeadHeight() * 4.0F, 0.0F);
        Vector3f avatarScale = this.avatar.getLocalScale();
        this.avatar.setLocalScale(avatarScale.mult(4.0F));
        this.avatar.setLighting(0.8F, 0.8F);

        this.workbenchNode.attachChild(this.avatar);

        this.palette = new Palette(app);

        setPalettePage(PalettePage.BLUEPRINTS);

        this.buttons = new Node("Palette Buttons");

        float buttonSpacing = 36.0F;
        this.paletteButtons = new Button[3];
        this.paletteButtons[0] = createButton("Save", 0.0F, 2.0F * buttonSpacing * this.buttonScale);
        this.paletteButtons[0].addCommand(new SaveCommand());
        this.paletteButtons[1] = createButton("Clear", 0.0F, 1.0F * buttonSpacing * this.buttonScale);
        this.paletteButtons[1].addCommand(new PopupCommand(getStateManager(), "Are you sure you want to clear all blocks?", new ActionCommand[]{new ClearCommand(), new ActionCommand("Cancel")}));

        this.paletteButtons[2] = createButton("Back", 0.0F, 0.0F * buttonSpacing * this.buttonScale);
        this.paletteButtons[2].addCommand(new CancelCommand());

        this.buttons.attachChild(this.paletteButtons[0]);
        this.buttons.attachChild(this.paletteButtons[1]);
        this.buttons.attachChild(this.paletteButtons[2]);
    }

    protected void enable() {
        super.enable();

        ((PostProcessingState) getStateManager().getState(PostProcessingState.class)).setRadialFadeOn(1.5F, 1.5F);
        Node tabRoot = ((TabState) getState(TabState.class)).getTabRoot();
        Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();

        Camera cam = getApplication().getCamera();
        float aspect = cam.getWidth() / cam.getHeight();
        if (aspect < 1.4D) {
            this.setback = 3.0F;
        }
        tabRoot.attachChild(this.workbenchNode);
        registerWithInput(getApplication().getInputManager());

        tabRoot.attachChild(this.palette);

        update(0.01F);

        ModeManager.instance.setMode("BP Adjust Type", true);

        Ray upperLeft = getScreenRay(40, (int) (cam.getHeight() * 0.9F));
        cam = ((TabState) getState(TabState.class)).getTabCamera();
        Vector3f pos = upperLeft.getOrigin().add(upperLeft.getDirection().mult(10.0F));

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.move(this.workbenchNode, pos, Vector3f.ZERO, 0.25F)});
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(this.workbenchNode, 0.01F, 1.0F, 0.25F)});

        Vector3f palettePos = getPalettePosition();
        pos = screenToWorld(cam.getWidth(), cam.getHeight() - 20, 10.0F);
        this.paletteMove = Animation.move(this.palette, pos, palettePos, 0.25F);
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{this.paletteMove});

        layoutPaletteButtons();
        showPaletteButtons(isEditing());
    }

    protected void disable() {
        super.disable();

        ((PostProcessingState) getStateManager().getState(PostProcessingState.class)).setRadialFade(false);
        Node tabRoot = ((TabState) getState(TabState.class)).getTabRoot();
        Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();

        showPaletteButtons(false);

        ModeManager.instance.setMode("BP Adjust Type", false);
        unregisterWithInput(getApplication().getInputManager());

        Camera cam = ((TabState) getState(TabState.class)).getTabCamera();
        Ray upperLeft = getScreenRay(40, (int) (cam.getHeight() * 0.9F));
        Vector3f pos = upperLeft.getOrigin().add(upperLeft.getDirection().mult(10.0F));

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.detach(this.workbenchNode, 0.25F), Animation.detach(this.palette, 0.25F), Animation.scale(this.workbenchNode, 1.0F, 0.01F, 0.25F), Animation.move(this.workbenchNode, Vector3f.ZERO, pos, 0.25F)});

        pos = screenToWorld(cam.getWidth(), cam.getHeight() - 20, 10.0F);
        this.paletteMove = Animation.move(this.palette, this.palette.getLocalTranslation(), pos, 0.25F);
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{this.paletteMove});
    }

    protected Button createButton(String text, float x, float y) {
        float triRadius = 17.0F;
        float triWidth = 150.0F;
        Trifold trifold = new Trifold(triWidth, triRadius * 2.0F);
        trifold.setFoldTextureCoordinates(new Vector2f(0.5F, 0.5F), new Vector2f(0.5F, 0.5F));
        trifold.setFoldCoordinates(new Vector2f(triRadius, triRadius), new Vector2f(triWidth - triRadius, triRadius));
        trifold.updateGeometry();

        Texture texture = getApplication().getAssetManager().loadTexture("Interface/bubble.png");
        Button button = new Button(getApplication(), trifold, texture);
        button.setText(text);
        button.setLocalTranslation(x, y, 0.0F);
        button.addControl(new MouseEventControl());
        button.setLocalScale(this.buttonScale);
        button.setTextColor(new ColorRGBA(0.4F, 0.6F, 0.6F, 1.0F));
        button.setTextShadowColor(new ColorRGBA(0.1F, 0.2F, 0.2F, 1.0F));
        return button;
    }

    protected void showPaletteButtons(boolean show) {
        if (this.buttons == null) {
            return;
        }
        if ((!show) && (this.buttons.getParent() != null)) {
            Camera cam = getApplication().getCamera();

            Vector3f pos = this.buttons.getLocalTranslation();
            Vector3f off = new Vector3f(cam.getWidth(), pos.y, pos.z);

            ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.detach(this.buttons, 0.25F), Animation.move(this.buttons, pos, off, 0.25F)});
        } else if ((show) && (this.buttons.getParent() == null)) {
            Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();
            if (this.buttons.getParent() == null) {
                gui.attachChild(this.buttons);
            }
            Camera cam = getApplication().getCamera();
            Vector2f paletteSize = this.palette.getPaletteScreenSize();

            Vector3f pos = new Vector3f(cam.getWidth() - paletteSize.x - 20.0F, 20.0F, 0.0F);
            Vector3f off = new Vector3f(cam.getWidth(), pos.y, pos.z);
            this.buttons.setLocalTranslation(off);

            ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.move(this.buttons, off, pos, 0.25F)});
        }
    }

    protected void layoutPaletteButtons() {
        Camera cam = getApplication().getCamera();

        Vector3f wCorner1 = this.palette.localToWorld(Vector3f.ZERO, null);
        Vector3f wCorner2 = this.palette.localToWorld(new Vector3f(4.4F, 0.0F, 0.0F), null);

        Vector3f corner1 = worldToScreen(wCorner1);
        Vector3f corner2 = worldToScreen(wCorner2);

        float w = (int) Math.abs(corner1.x - corner2.x) + 20;

        this.buttonScale = (w / 150.0F);

        int y = this.paletteButtons.length - 1;
        float spacer = 2.0F;
        for (int i = 0; i < this.paletteButtons.length; ) {
            Button b = this.paletteButtons[i];
            float h = b.getHeight() + spacer;
            b.setLocalScale(this.buttonScale);
            b.setLocalTranslation(0.0F, y * h * this.buttonScale, 0.0F);

            i++;
            y--;
        }

        this.buttons.setLocalTranslation(cam.getWidth() - w - 20.0F, 20.0F, 0.0F);
    }

    protected void registerWithInput(InputManager inputManager) {
        inputManager.addMapping("BP_PanLeft", new Trigger[]{new KeyTrigger(203)});
        inputManager.addMapping("BP_PanRight", new Trigger[]{new KeyTrigger(205)});
        inputManager.addMapping("BP_PanUp", new Trigger[]{new KeyTrigger(200)});
        inputManager.addMapping("BP_PanDown", new Trigger[]{new KeyTrigger(208)});

        inputManager.addMapping("BP_StrafeLeft", new Trigger[]{new KeyTrigger(30)});
        inputManager.addMapping("BP_StrafeRight", new Trigger[]{new KeyTrigger(32)});
        inputManager.addMapping("BP_Forward", new Trigger[]{new KeyTrigger(17)});
        inputManager.addMapping("BP_Back", new Trigger[]{new KeyTrigger(31)});
        inputManager.addMapping("BP_Raise", new Trigger[]{new KeyTrigger(16)});
        inputManager.addMapping("BP_Lower", new Trigger[]{new KeyTrigger(44)});
        inputManager.addMapping("BP_Jump", new Trigger[]{new KeyTrigger(57)});
        inputManager.addMapping("BP_Run", new Trigger[]{new KeyTrigger(42)});

        inputManager.addMapping("BP_Select", new Trigger[]{new MouseButtonTrigger(0)});
        inputManager.addMapping("BP_Place", new Trigger[]{new MouseButtonTrigger(1)});

        inputManager.addMapping("BP Adjust Sub-type", new Trigger[]{new KeyTrigger(29)});
        inputManager.addMapping("BP Type Up", new Trigger[]{new KeyTrigger(52)});
        inputManager.addMapping("BP Type Down", new Trigger[]{new KeyTrigger(51)});
        inputManager.addMapping("BP Clone Type", new Trigger[]{new KeyTrigger(46)});

        inputManager.addListener(this.inputHandler, BP_MAPPINGS);

        Joystick[] sticks = inputManager.getJoysticks();
        if ((sticks != null) && (sticks.length > 0)) {
            sticks[0].assignAxis("BP_StrafeRight", "BP_StrafeLeft", 254);
            sticks[0].assignAxis("BP_Forward", "BP_Back", 255);
            sticks[0].assignAxis("BP_PanRight", "BP_PanLeft", sticks[0].getXAxisIndex());
            sticks[0].assignAxis("BP_PanDown", "BP_PanUp", sticks[0].getYAxisIndex());
        }

        ModeManager.instance.addMode("BP Adjust Sub-type", "BP Item Up", "BP Item Down", this.inputHandler);
        ModeManager.instance.addMode("BP Adjust Type", "BP Type Up", "BP Type Down", this.inputHandler);

        this.rotate.attach(inputManager);
        this.mirror.attach(inputManager);
    }

    protected void unregisterWithInput(InputManager inputManager) {
        for (String s : BP_MAPPINGS) {
            inputManager.deleteMapping(s);
        }
        this.rotate.detach(inputManager);
        this.mirror.detach(inputManager);

        inputManager.removeListener(this.inputHandler);
        ModeManager.instance.removeMode("BP Adjust Sub-type");
        ModeManager.instance.removeMode("BP Adjust Type");
    }

    protected Ray getScreenRay(int x, int y) {
        Vector2f cursor = new Vector2f(x, y);
        Camera cam = ((TabState) getState(TabState.class)).getTabCamera();

        Vector3f clickFar = cam.getWorldCoordinates(cursor, 1.0F);
        Vector3f clickNear = cam.getWorldCoordinates(cursor, 0.0F);
        Ray mouseRay = new Ray(clickNear, clickFar.subtractLocal(clickNear).normalizeLocal());

        return mouseRay;
    }

    protected Vector3f screenToWorld(float x, float y, float distance) {
        Ray upperLeft = getScreenRay((int) x, (int) y);
        Vector3f pos = upperLeft.getOrigin().add(upperLeft.getDirection().mult(distance));
        return pos;
    }

    protected Vector3f worldToScreen(Vector3f world) {
        Camera cam = ((TabState) getState(TabState.class)).getTabCamera();
        if (cam == null)
            return world;
        return cam.getScreenCoordinates(world);
    }

    protected float screenSize(float distance, float size) {
        Camera cam = ((TabState) getState(TabState.class)).getTabCamera();
        Vector3f world = cam.getLocation().add(cam.getDirection().mult(distance));
        Vector3f left = world.add(cam.getLeft().mult(size));

        Vector3f screen1 = cam.getScreenCoordinates(world);
        Vector3f screen2 = cam.getScreenCoordinates(left);

        return Math.abs(screen2.x - screen1.x);
    }

    protected CollisionResults getHits() {
        Vector2f cursor = getApplication().getInputManager().getCursorPosition();

        Camera cam = ((TabState) getState(TabState.class)).getTabCamera();

        Vector3f clickFar = cam.getWorldCoordinates(cursor, 1.0F);
        Vector3f clickNear = cam.getWorldCoordinates(cursor, 0.0F);

        Ray mouseRay = new Ray(clickNear, clickFar.subtractLocal(clickNear).normalizeLocal());

        CollisionResults results = new CollisionResults();
        Node tabRoot = ((TabState) getState(TabState.class)).getTabRoot();
        int count = tabRoot.collideWith(mouseRay, results);

        return results;
    }

    protected boolean clickNode(Spatial s) {
        if (s == null) {
            return false;
        }
        ButtonControl c = (ButtonControl) s.getControl(ButtonControl.class);
        if (c != null) {
            c.click();
            return true;
        }

        return clickNode(s.getParent());
    }

    protected boolean clickUI(String action, boolean pressed) {
        CollisionResults results = getHits();

        for (CollisionResult cr : results) {
            Geometry g = cr.getGeometry();
            System.out.println("hit:" + g + "  parent:" + g.getParent());
            if (clickNode(g))
                return true;
        }
        return false;
    }

    public void update(float tpf) {
        this.blueprintSelector.update();

        Camera cam = ((TabState) getState(TabState.class)).getTabCamera();

        Vector3f position = new Vector3f(0.0F, 0.0F, this.cameraDistance + this.setback);

        Quaternion rot1 = new Quaternion().fromAngles(-this.pitch, 0.0F, 0.0F);
        Quaternion rot2 = new Quaternion().fromAngles(0.0F, this.yaw, 0.0F);
        Quaternion rot = rot2.mult(rot1);

        position = rot.mult(position);
        position.y += this.cameraElevation;

        cam.setLocation(position);

        rot1 = new Quaternion().fromAngles(this.pitch, 0.0F, 0.0F);
        rot2 = new Quaternion().fromAngles(0.0F, this.yaw + 3.141593F, 0.0F);
        rot = rot2.mult(rot1);
        cam.setRotation(rot);

        rot = new Quaternion().fromAngles(0.0F, this.yaw + 1.570796F, 0.0F);
        position = new Vector3f(0.0F, 0.0F, -(this.bpSize - 1.5F));
        position = rot.mult(position);
        this.avatar.setLocalTranslation(position.x, this.avatar.getHeadHeight() * 4.0F, position.z);

        if (!((AnimationState) getState(AnimationState.class)).hasTask(this.paletteMove)) {
            Vector3f palettePos = getPalettePosition();
            this.palette.setLocalTranslation(palettePos);
        }

        rot = cam.getRotation();
        this.palette.setLocalRotation(cam.getRotation());
        this.palette.setIconRotation(cam.getRotation().inverse());

        setItemType(this.selector.getTypeId());

        refreshSelectionHighlight();

        this.palette.recalculate();

        this.blockSelectionUpdateTime += tpf;
        if (this.blockSelectionUpdateTime > 0.1F) {
            this.blockSelectionUpdateTime = 0.0F;
            setCurrentHit(getHit());
        }

        Vector2f cursor = getApplication().getInputManager().getCursorPosition();
        float headAngle = cursor.y / cam.getHeight() - 0.5F;
        headAngle *= 1.570796F;
        if (headAngle != this.lastHeadAngle) {
            this.lastHeadAngle = headAngle;
            Quaternion headLift = new Quaternion().fromAngles(-headAngle, -3.141593F, 0.0F);
            this.avatar.setFacing(headLift);
        }
    }

    protected Vector3f getPalettePosition() {
        Camera cam = ((TabState) getState(TabState.class)).getTabCamera();

        float w = screenSize(10.0F, this.palette.getWidth()) + 30.0F;
        float h = screenSize(10.0F, 0.3F);
        h += 30.0F;
        return screenToWorld(cam.getWidth() - w, cam.getHeight() - h, 10.0F);
    }

    protected void setItemType(int type) {
        if (this.itemType == type)
            return;
        this.itemType = type;
    }

    protected Spatial getSelected() {
        Spatial selected = null;
        switch (this.page.ordinal())
        {
            case 1:
                break;
            case 2:
                int group = this.selector.getGroup();
                if (group < this.palette.getIcons().size())
                    selected = (Spatial) this.palette.getIcons().get(group);
                break;
            case 3:
                int item = this.selector.getItemType();
                if (item < this.palette.getIcons().size()) {
                    selected = (Spatial) this.palette.getIcons().get(item);
                }
        }
        return selected;
    }

    protected void refreshSelectionHighlight() {
        Spatial selected = getSelected();
        this.palette.setSelectedIcon(selected);
    }

    protected void refreshBlueprints() {
        if (this.page == PalettePage.BLUEPRINTS)
            this.palette.setIcons(this.blueprintSelector.getIcons());
    }

    protected boolean isEditing() {
        return (this.page == PalettePage.GROUPS) || (this.page == PalettePage.TYPES);
    }

    protected void setPalettePage(PalettePage p) {
        if (this.page == p) {
            return;
        }
        this.page = p;

        switch (this.page.ordinal())
        {
            case 1:
                this.palette.setIcons(this.blueprintSelector.getIcons());
                break;
            case 2:
                this.palette.setIcons(this.selector.getGroupIcons());
                break;
            case 3:
                this.palette.setIcons(this.selector.getTypeIcons());
        }

        showPaletteButtons(isEditing());
    }

    protected void setWorkingBlueprint(BlueprintData blueprint, EntityId entityId) {
        System.out.println("Set blueprint:" + blueprint);
        setPalettePage(PalettePage.GROUPS);

        if (blueprint != null) {
            this.workingBlueprint.copyData(blueprint);
        } else {
            this.workingBlueprint.clear();
        }
        this.workingBlueprint.regenerateGeometry();
        this.workingBlueprintEntityId = entityId;
    }

    protected Ray getObjectMouseRay() {
        Vector2f cursor = getApplication().getInputManager().getCursorPosition();
        Camera cam = ((TabState) getState(TabState.class)).getTabCamera();
        Vector3f clickFar = cam.getWorldCoordinates(cursor, 1.0F);
        swap(clickFar);
        Vector3f clickNear = cam.getWorldCoordinates(cursor, 0.0F);
        swap(clickNear);
        Ray mouseRay = new Ray(clickNear, clickFar.subtractLocal(clickNear).normalizeLocal());
        mouseRay.setLimit(this.cameraDistance + this.setback + this.bpSize);

        Vector3f origin = mouseRay.getOrigin();
        origin.addLocal(this.bpSize * 0.5F, this.bpSize * 0.5F, 0.0F);

        return mouseRay;
    }

    protected void swap(Vector3f v) {
        float t = v.z;
        v.z = v.y;
        v.y = t;
    }

    protected BlueprintIntersector.Intersection getHit() {
        Ray ray = getObjectMouseRay();
        BlueprintIntersector intersector = new BlueprintIntersector(this.workingBlueprint.getCells(), this.bpSize, ray);

        BlueprintIntersector.Intersection hit = null;
        if (intersector.hasNext()) {
            BlueprintIntersector.Intersection i = intersector.next();

            hit = i;
        }

        return hit;
    }

    protected void setCurrentHit(BlueprintIntersector.Intersection hit) {
        if (ObjectUtils.areEqual(this.lastHit, hit)) {
            return;
        }
        if (hit != null) {
            Vector3f cell = hit.getBlock();
            if (!this.wb.contains(cell)) {
                if (!this.wb.contains(cell.add(0.0F, 0.0F, 1.0F))) {
                    hit = null;
                }
            }
        }
        this.lastHit = hit;
        if (hit == null) {
            this.selectedBlock.removeFromParent();
            return;
        }

        if (this.selectedBlock.getParent() == null) {
            this.workbenchNode.attachChild(this.selectedBlock);
        }
        Vector3f cell = hit.getBlock();
        Vector3f model = this.wb.cellToModel(cell);

        this.selectedBlock.setLocalTranslation(model.x + 0.5F, model.y + 0.5F, model.z + 0.5F);
    }

    protected class DiscardCommand extends ActionCommand {
        public DiscardCommand() {
            super("Discard");
        }

        public void execute(Object source, Object a) {
            BlueprintEditorState.this.workingBlueprint.clear();
            BlueprintEditorState.this.workingBlueprint.regenerateGeometry();

            BlueprintEditorState.this.setPalettePage(BlueprintEditorState.PalettePage.BLUEPRINTS);
        }
    }

    protected class RemoveCommand extends ActionCommand {
        public RemoveCommand() {
            super("Remove");
        }

        public void execute(Object source, Object a) {
            EntityAction action = new RemoveBlueprintAction();
            BlueprintEditorState.this.gameClient.executeAction(action, BlueprintEditorState.this.workingBlueprintEntityId);

            BlueprintEditorState.this.workingBlueprint.clear();
            BlueprintEditorState.this.workingBlueprint.regenerateGeometry();

            BlueprintEditorState.this.setPalettePage(BlueprintEditorState.PalettePage.BLUEPRINTS);
        }
    }

    protected class SaveCommand extends ActionCommand {
        public SaveCommand() {
            super("Save");
        }

        public void execute(Object source, Object a) {
            if ((BlueprintEditorState.this.workingBlueprintEntityId != null) && (BlueprintEditorState.this.workingBlueprint.isEmpty())) {
                BlueprintEditorState.this.getApplication().getStateManager().attach(new PopupMessageState("Really delete blueprint?", new ActionCommand[]{new BlueprintEditorState.RemoveCommand(), new ActionCommand("Cancel")}));

                return;
            }

            if (!BlueprintEditorState.this.workingBlueprint.isChanged()) {
                return;
            }

            System.out.println("Working entity id:" + BlueprintEditorState.this.workingBlueprintEntityId);

            BlueprintEditorState.this.workingBlueprint.pack();

            EntityAction action = new SaveBlueprintAction(BlueprintEditorState.this.workingBlueprintEntityId, "Unnamed", BlueprintEditorState.this.workingBlueprint.getSizeX(), BlueprintEditorState.this.workingBlueprint.getSizeY(), BlueprintEditorState.this.workingBlueprint.getSizeZ(), 0.25F, BlueprintEditorState.this.workingBlueprint.getCells());

            BlueprintEditorState.this.gameClient.executeAction(action, null);

            BlueprintEditorState.this.workingBlueprint.resetSize(BlueprintEditorState.this.bpSize, BlueprintEditorState.this.bpSize, BlueprintEditorState.this.bpSize);
            BlueprintEditorState.this.workingBlueprint.regenerateGeometry();

            BlueprintEditorState.this.setPalettePage(BlueprintEditorState.PalettePage.BLUEPRINTS);
        }
    }

    protected class CancelCommand extends ActionCommand {
        public CancelCommand() {
            super("Cancel");
        }

        public void execute(Object source, Object a) {
            if (BlueprintEditorState.this.getState(PopupMessageState.class) != null) {
                System.out.println("Not popping up message because a message is already popped up.");
                return;
            }

            if (!BlueprintEditorState.this.workingBlueprint.isChanged()) {
                new BlueprintEditorState.DiscardCommand().execute(null, a);
                return;
            }

            BlueprintEditorState.this.getApplication().getStateManager().attach(new PopupMessageState("Save changes before returning?", new ActionCommand[]{new BlueprintEditorState.SaveCommand(), new BlueprintEditorState.DiscardCommand(), new ActionCommand("Cancel")}));
        }
    }

    protected class ClearCommand extends ActionCommand {
        public ClearCommand() {
            super("Clear");
        }

        public void execute(Object source, Object a) {
            BlueprintEditorState.this.workingBlueprint.clear();
            BlueprintEditorState.this.workingBlueprint.regenerateGeometry();
        }
    }

    private class InputHandler
            implements AnalogListener, ActionListener {
        private float moveSpeed = 4.0F;
        private float angleSpeed = 2.0F;
        private float coangleSpeed = 2.0F;

        public InputHandler() {
        }

        public void onAnalog(String name, float value, float tpf) {
            if ("BP Adjust Sub-type".equals(name)) {
                return;
            }

            if ("BP_Forward".equals(name)) {
                BlueprintEditorState.this.distance-= this.moveSpeed * tpf;
                BlueprintEditorState.this.pitch+= this.coangleSpeed * tpf;
//                BlueprintEditorState.access$024(BlueprintEditorState.this, this.moveSpeed * tpf);
//                BlueprintEditorState.access$116(BlueprintEditorState.this, this.coangleSpeed * tpf);
                if (BlueprintEditorState.this.pitch > 1.570796F)
                    BlueprintEditorState.this.pitch = 1.570796F;
            } else if ("BP_Back".equals(name)) {
                BlueprintEditorState.this.distance+= this.moveSpeed * tpf;
                BlueprintEditorState.this.pitch-= this.coangleSpeed * tpf;
//                BlueprintEditorState.access$016(BlueprintEditorState.this, this.moveSpeed * tpf);
//                BlueprintEditorState.access$124(BlueprintEditorState.this, this.coangleSpeed * tpf);
                if (BlueprintEditorState.this.pitch < 0.0F)
                    BlueprintEditorState.this.pitch = 0.0F;
            } else if ("BP_StrafeLeft".equals(name)) {
                BlueprintEditorState.this.yaw-= this.angleSpeed * tpf;
//                BlueprintEditorState.access$224(BlueprintEditorState.this, this.angleSpeed * tpf);
            } else if ("BP_StrafeRight".equals(name)) {
                BlueprintEditorState.this.yaw+= this.angleSpeed * tpf;
                //BlueprintEditorState.access$216(BlueprintEditorState.this, this.angleSpeed * tpf);
            } else if ("BP Item Up".equals(name)) {
                BlueprintEditorState.this.selector.incrementItemType();
            } else if ("BP Item Down".equals(name)) {
                BlueprintEditorState.this.selector.decrementItemType();
            } else if ("BP Type Up".equals(name)) {
                BlueprintEditorState.this.selector.incrementGroup();
            } else if ("BP Type Down".equals(name)) {
                BlueprintEditorState.this.selector.decrementGroup();
            }
        }

        public void onAction(String name, boolean value, float tpf) {
            if (("BP Adjust Sub-type".equals(name)) && (BlueprintEditorState.this.isEditing())) {
                ModeManager.instance.setMode("BP Adjust Sub-type", value);
                if (!value)
                    ModeManager.instance.setMode("BP Adjust Type", true);
                if (value)
                    BlueprintEditorState.this.setPalettePage(BlueprintEditorState.PalettePage.TYPES);
                else {
                    BlueprintEditorState.this.setPalettePage(BlueprintEditorState.PalettePage.GROUPS);
                }
            }
            if (value) {
                return;
            }
            if ("BP_Place".equals(name)) {
                if (BlueprintEditorState.this.clickUI(name, value)) {
                    return;
                }
                if (!BlueprintEditorState.this.isEditing()) {
                    return;
                }
                BlueprintIntersector.Intersection hit = BlueprintEditorState.this.getHit();
                if (hit == null) {
                    return;
                }

                Vector3f block = hit.getBlock();

                int side = hit.getSide();
                if (side < 0) {
                    return;
                }

                block.x += mythruna.Direction.DIRS[side][0];
                block.y += mythruna.Direction.DIRS[side][1];
                block.z += mythruna.Direction.DIRS[side][2];

                BlockType placeType = BlueprintEditorState.this.selector.getSelectedType();
                int existing = BlueprintEditorState.this.workingBlueprint.getType(block.x, block.y, block.z);
                if (existing == 0) {
                    if (BlueprintEditorState.this.workingBlueprint.setType(block.x, block.y, block.z, placeType.getId()))
                        BlueprintEditorState.this.workingBlueprint.regenerateGeometry();
                }
            } else if ("BP_Select".equals(name)) {
                if (BlueprintEditorState.this.clickUI(name, value)) {
                    return;
                }
                if (!BlueprintEditorState.this.isEditing()) {
                    return;
                }
                BlueprintIntersector.Intersection hit = BlueprintEditorState.this.getHit();
                if (hit == null) {
                    return;
                }

                Vector3f block = hit.getBlock();
                if (BlueprintEditorState.this.workingBlueprint.setType(block.x, block.y, block.z, 0))
                    BlueprintEditorState.this.workingBlueprint.regenerateGeometry();
            } else if (("BP Clone Type".equals(name)) && (BlueprintEditorState.this.isEditing())) {
                BlueprintIntersector.Intersection hit = BlueprintEditorState.this.getHit();
                if (hit == null) {
                    return;
                }
                System.out.println("hit type:" + hit.getType());
                if (hit.getType() <= 0) {
                    return;
                }
                BlockType blockType = mythruna.BlockTypeIndex.types[hit.getType()];
                BlueprintEditorState.this.selector.setSelectedType(blockType);
            }
        }
    }

    public static enum PalettePage {
        BLUEPRINTS, GROUPS, TYPES;
    }
}
