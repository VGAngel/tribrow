package mythruna.client.tabs.bp;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.texture.Texture;
import mythruna.client.GameAppState;
import mythruna.client.anim.AnimationState;
import mythruna.client.tabs.TabState;
import mythruna.client.ui.GridControl;
import mythruna.geom.Trifold;

import java.util.Collection;
import java.util.List;

public class Palette extends Node {
    private Application app;
    private Node icons;
    private Spatial selected;
    private Geometry selectedIcon;
    private ColorRGBA selectionColor = ColorRGBA.Cyan.clone();
    private float selectionColorTheta = 0.0F;
    private GridControl iconGrid;
    private Trifold iconBorderMesh;
    private Geometry iconBorder;
    private Trifold paletteBgMesh;
    private Geometry paletteBg;

    public Palette(Application app) {
        super("Palette");

        this.app = app;

        setLocalScale(0.3F);

        this.icons = new Node("Palette Icons");
        attachChild(this.icons);

        this.iconGrid = new GridControl((AnimationState) app.getStateManager().getState(AnimationState.class), 10, -1);
        this.icons.addControl(this.iconGrid);

        WireBox boxMesh = new WireBox(0.51F, 0.51F, 0.51F);
        this.selectedIcon = new Geometry("Selected Icon", boxMesh);
        Material boxMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        boxMaterial.setColor("Color", this.selectionColor);
        this.selectedIcon.setMaterial(boxMaterial);

        this.paletteBgMesh = new Trifold(4.0F, 4.0F);
        this.paletteBgMesh.setFoldCoordinates(new Vector2f(0.1F, 0.1F), new Vector2f(0.1F, 0.1F));
        this.paletteBg = new Geometry("Palette Background", this.paletteBgMesh);
        Material bgMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        bgMaterial.setColor("Color", new ColorRGBA(0.0F, 0.2F, 0.4F, 1.0F));
        bgMaterial.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Front);
        this.paletteBg.setMaterial(bgMaterial);
        this.paletteBg.setLocalTranslation(0.0F, 0.0F, 0.0F);
        attachChild(this.paletteBg);

        this.iconBorderMesh = new Trifold(128.0F, 128.0F);
        this.iconBorderMesh.setFoldTextureCoordinates(new Vector2f(0.09375F, 0.09375F), new Vector2f(0.90625F, 0.90625F));

        this.iconBorderMesh.setFoldCoordinates(new Vector2f(12.0F, 12.0F), new Vector2f(116.0F, 116.0F));
        this.iconBorderMesh.updateGeometry();

        Material borderMaterial = new Material(app.getAssetManager(), "MatDefs/Composed.j3md");
        borderMaterial.setTexture("Map", app.getAssetManager().loadTexture("Interface/border.png"));
        Texture leather = app.getAssetManager().loadTexture("Interface/leather.jpg");
        leather.setWrap(Texture.WrapMode.Repeat);
        borderMaterial.setTexture("Mix", leather);
        borderMaterial.setVector3("MixParms", new Vector3f(0.01F, 0.01F, 1.0F));
        borderMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.iconBorder = new Geometry("Palette Border", this.iconBorderMesh);
        this.iconBorder.setLocalTranslation(700.0F, 300.0F, 0.0F);
        this.iconBorder.setMaterial(borderMaterial);
    }

    public float getWidth() {
        return this.iconGrid.getWidth() * 0.3F;
    }

    public List<Spatial> getIcons() {
        return this.iconGrid.children();
    }

    public GridControl getIconGrid() {
        return this.iconGrid;
    }

    protected <T extends AppState> T getState(Class<T> type) {
        return this.app.getStateManager().getState(type);
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

    public Vector2f getPaletteScreenSize() {
        return this.iconBorderMesh.getSize();
    }

    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        Vector3f wCorner1 = this.paletteBg.localToWorld(Vector3f.ZERO, null);
        Vector3f wCorner2 = this.paletteBg.localToWorld(new Vector3f(this.iconGrid.getWidth() + 1.0F, this.iconGrid.getHeight() + 1.0F, 0.0F), null);
        Vector3f corner1 = worldToScreen(wCorner1);
        Vector3f corner2 = worldToScreen(wCorner2);
        this.iconBorder.setLocalTranslation(corner2.x - 10.0F, corner1.y - 10.0F, 0.0F);

        Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();
        gui.updateGeometricState();

        this.selectionColorTheta += tpf * 2.0F;
        if (this.selectionColorTheta > 6.283186F)
            this.selectionColorTheta %= 6.283186F;
        float cos = FastMath.cos(this.selectionColorTheta);
        this.selectionColor.b = ((cos + 1.0F) * 0.5F);
    }

    public void setIconRotation(Quaternion rot) {
        this.iconGrid.setChildRotation(rot);
        this.selectedIcon.setLocalRotation(rot);
    }

    public void setSelectedIcon(Spatial icon) {
        Spatial oldSelected = this.selected;
        this.selected = icon;
        if (this.selected != null) {
            if (oldSelected == null)
                attachChild(this.selectedIcon);
            this.selectedIcon.setLocalTranslation(this.selected.getLocalTranslation());
        } else {
            this.selectedIcon.removeFromParent();
        }
    }

    public void setIcons(Collection<Spatial> icons) {
        this.iconGrid.clear();
        this.iconGrid.addAll(icons);
        this.iconGrid.refreshChildren();

        recalculate();
    }

    public void setParent(Node parent) {
        super.setParent(parent);

        if (parent == null) {
            Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();
            gui.detachChild(this.iconBorder);
        } else {
            Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();
            gui.attachChild(this.iconBorder);
            recalculate();
        }
    }

    public void updateWorldTransforms() {
        super.updateWorldTransforms();
    }

    protected void recalculate() {
        this.paletteBg.setLocalTranslation(-(this.iconGrid.getWidth() + 0.25F), -(this.iconGrid.getHeight() - 0.35F), 1.0F);

        Vector3f wCorner1 = this.paletteBg.localToWorld(Vector3f.ZERO, null);
        Vector3f wCorner2 = this.paletteBg.localToWorld(new Vector3f(this.iconGrid.getWidth() + 1.0F, this.iconGrid.getHeight() + 1.0F, 0.0F), null);

        Vector3f corner1 = worldToScreen(wCorner1);
        Vector3f corner2 = worldToScreen(wCorner2);

        float w = (int) Math.abs(corner1.x - corner2.x) + 20;
        float h = (int) Math.abs(corner1.y - corner2.y) + 20;

        this.iconBorderMesh.setSize(w, h);
        this.iconBorderMesh.updateGeometry();

        this.iconBorder.setLocalTranslation(corner2.x - 10.0F, corner1.y - 10.0F, 0.0F);

        this.paletteBgMesh.setSize(this.iconGrid.getWidth() + 1.0F, this.iconGrid.getHeight() + 1.0F);
        this.paletteBgMesh.updateGeometry();
    }
}