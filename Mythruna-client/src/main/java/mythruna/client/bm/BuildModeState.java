package mythruna.client.bm;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.client.*;
import mythruna.client.ui.ObservableState;
import mythruna.client.view.LocalArea;

public class BuildModeState extends ObservableState implements AnalogListener, ActionListener {

    private static final String CAM_ITEM_UP = "CAM_ItemUp";
    private static final String CAM_ITEM_DOWN = "CAM_ItemDown";
    private static final String CAM_SELECT = "CAM_Select";
    private static final String CAM_PLACE = "CAM_Place";
    private Node guiRoot;
    private Node myGuiNode;
    private Node iconHolder;
    private BitmapText itemLabel;
    private Geometry selectedBlock;
    private Quaternion lastCameraRotation = null;
    private Object lastTool = null;
    private Vector3i lastHit = null;
    private InputManager inputManager;
    private GameClient gameClient;
    private ObjectSelector objectSelector;
    private LocalArea localArea;
    private ConveyerCamera camera;
    private KeyMethodAction selectTypeHit;

    public BuildModeState(Node guiRoot, GameClient gameClient, LocalArea localArea, ConveyerCamera camera) {
        super("BuildModeGui", true);
        this.guiRoot = guiRoot;
        this.gameClient = gameClient;
        this.localArea = localArea;
        this.camera = camera;
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.inputManager = app.getInputManager();

        this.objectSelector = new ObjectSelector(this.gameClient, this.inputManager, app.getStateManager());
        this.objectSelector.setLocalArea(this.localArea);

        this.myGuiNode = new Node("Perspective GUI");
        this.myGuiNode.setCullHint(Spatial.CullHint.Never);

        Camera cam = app.getCamera();
        this.iconHolder = new Node("IconHolder");
        this.iconHolder.setLocalTranslation(0.0F, -(cam.getHeight() * 0.5F) + 20.0F, 0.0F);
        this.iconHolder.setLocalScale(40.0F);
        this.myGuiNode.attachChild(this.iconHolder);

        BitmapFont guiFont = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.itemLabel = new BitmapText(guiFont, false);
        this.itemLabel.setLocalTranslation(cam.getWidth() / 2 + 30, this.itemLabel.getLineHeight() * 2.0F, -1.0F);
        this.itemLabel.setText("Testing");

        float guiScale = 0.2F / cam.getHeight();
        this.myGuiNode.setLocalTranslation(0.0F, 0.0F, -0.99F);
        this.myGuiNode.setLocalScale(guiScale);

        WireBox boxMesh = new WireBox(0.501F, 0.501F, 0.501F);
        this.selectedBlock = new Geometry("Selected Block", boxMesh);
        Material boxMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        boxMaterial.setColor("Color", ColorRGBA.Yellow);
        this.selectedBlock.setMaterial(boxMaterial);
        this.selectedBlock.setLocalTranslation(0.0F, 0.0F, 0.0F);
    }

    protected void enable() {
        if (this.itemLabel.getParent() != null) {
            return;
        }
        ((DebugHudState) getState(DebugHudState.class)).setBottom(true);
        ((GameAppState) getState(GameAppState.class)).setStatsOnBottom(true);
        ((GameAppState) getState(GameAppState.class)).setMessageMargin(0.0F);

        ((CrosshairState) getState(CrosshairState.class)).setImage("Textures/cursor.png", 32.0F, 32.0F);

        this.objectSelector.setEnabled(true);

        Node rootNode = ((SimpleApplication) getApplication()).getRootNode();
        this.localArea.attachChild(this.selectedBlock);
        rootNode.attachChild(this.myGuiNode);
        this.guiRoot.attachChild(this.itemLabel);

        this.inputManager.addMapping("CAM_Select", new Trigger[]{new MouseButtonTrigger(0)});
        this.inputManager.addMapping("CAM_Place", new Trigger[]{new MouseButtonTrigger(1)});

        this.inputManager.addListener(this, new String[]{"CAM_Select", "CAM_Place"});

        if (this.inputManager.hasMapping("SIMPLEAPP_CameraPos"))
            this.inputManager.deleteMapping("SIMPLEAPP_CameraPos");
        this.selectTypeHit = new KeyMethodAction(this, "findHitType", 46);
        this.selectTypeHit.attach(this.inputManager);
    }

    protected void disable() {
        this.objectSelector.setEnabled(false);
        Node rootNode = ((SimpleApplication) getApplication()).getRootNode();
        this.localArea.detachChild(this.selectedBlock);
        rootNode.detachChild(this.myGuiNode);
        this.guiRoot.detachChild(this.itemLabel);

        if (this.inputManager.hasMapping("CAM_Select"))
            this.inputManager.deleteMapping("CAM_Select");
        if (this.inputManager.hasMapping("CAM_Place")) {
            this.inputManager.deleteMapping("CAM_Place");
        }
        this.inputManager.removeListener(this);
        this.selectTypeHit.detach(this.inputManager);
    }

    public void update(float tpf) {
        this.objectSelector.update();

        Camera cam = getApplication().getCamera();

        if ((this.lastCameraRotation == null) || (!this.lastCameraRotation.equals(cam.getRotation()))) {
            this.lastCameraRotation = cam.getRotation().clone();
            this.myGuiNode.setLocalTranslation(cam.getDirection().mult(0.15F));
            this.myGuiNode.setLocalRotation(cam.getRotation());

            this.iconHolder.setLocalRotation(cam.getRotation().inverse());
        }

        if (this.lastTool != this.objectSelector.getSelectedTool()) {
            this.lastTool = this.objectSelector.getSelectedTool();
            Spatial icon = this.objectSelector.getIcon();

            this.iconHolder.detachAllChildren();

            if (icon != null) {
                this.iconHolder.attachChild(icon);
            }
            this.itemLabel.setText(this.objectSelector.getLabel());
        }

        if (!this.objectSelector.showBlockSelection()) {
            if (this.lastHit != null) {
                this.selectedBlock.setCullHint(Spatial.CullHint.Always);
            }
            this.lastHit = null;
        } else {
            WorldIntersector.Intersection hit = this.camera.getHit();
            Vector3i blockLocation = hit != null ? hit.getBlock() : null;
            if (blockLocation != null) {
                if (!blockLocation.equals(this.lastHit)) {
                    Vector3f trans = this.localArea.getSceneLocation(blockLocation.x, blockLocation.y, blockLocation.z);

                    this.selectedBlock.setLocalTranslation(trans.x + 0.5F, trans.y + 0.5F, trans.z + 0.5F);

                    this.selectedBlock.setCullHint(Spatial.CullHint.Never);
                }
            } else if (this.lastHit != null) {
                this.selectedBlock.setCullHint(Spatial.CullHint.Always);
            }
            this.lastHit = blockLocation;
        }
    }

    public void findHitType() {
        Vector3f pos = this.gameClient.getLocation();
        this.localArea.setLocation(pos.x, pos.y, pos.z);

        Vector3f dir = new Vector3f();
        this.camera.getCamera().getDirection(dir);
        pos = pos.clone();
        dir.set(dir.x, dir.z, dir.y);

        this.objectSelector.selectBlockType(pos, dir, toWorld(this.camera.getCamera().getRotation()));
    }

    protected Quaternion toWorld(Quaternion q) {
        return Coordinates.flipAxes(q);
    }

    public void onAnalog(String name, float value, float tpf) {
        long start = System.nanoTime();
        try {
            if (!isEnabled()) {
                long end;
                long delta;
                return;
            }
            if ((this.objectSelector != null) && (this.objectSelector.isCapturingView())) {
                Vector3f pos = this.gameClient.getLocation();
                this.localArea.setLocation(pos.x, pos.y, pos.z);

                Vector3f dir = new Vector3f();
                this.camera.getCamera().getDirection(dir);
                pos = pos.clone();
                dir.set(dir.x, dir.z, dir.y);

                this.objectSelector.viewMoved(pos, dir, toWorld(this.camera.getCamera().getRotation()));
            }
        } finally {
            long end = System.nanoTime();
            long delta = end - start;
            if (delta > 1000000L)
                System.out.println("BuildModeState.onAnalog() took:" + delta / 1000000.0D + " ms.");
        }
    }

    public void onAction(String name, boolean value, float tpf) {
        long start = System.nanoTime();
        try {
            if (!isEnabled()) {
                long end;
                long delta;
                return;
            }
            if (("CAM_Place".equals(name)) || ("CAM_Select".equals(name))) {
                Vector3f pos = this.gameClient.getLocation();
                this.localArea.setLocation(pos.x, pos.y, pos.z);

                Vector3f dir = new Vector3f();
                this.camera.getCamera().getDirection(dir);
                pos = pos.clone();
                dir.set(dir.x, dir.z, dir.y);

                if (this.objectSelector != null) {
                    if ("CAM_Select".equals(name))
                        this.objectSelector.select(pos, dir, toWorld(this.camera.getCamera().getRotation()), value);
                    else
                        this.objectSelector.place(pos, dir, toWorld(this.camera.getCamera().getRotation()), value);
                }
            }
        } finally {
            long end = System.nanoTime();
            long delta = end - start;
            if (delta > 1000000L)
                System.out.println("onAction() took:" + delta / 1000000.0D + " ms.");
        }
    }
}