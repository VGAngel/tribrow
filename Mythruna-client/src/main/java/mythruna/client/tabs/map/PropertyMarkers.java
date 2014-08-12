package mythruna.client.tabs.map;

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import mythruna.World;
import mythruna.client.GameClient;
import mythruna.client.ui.HAlignment;
import mythruna.client.ui.Label;
import mythruna.es.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PropertyMarkers extends AbstractControl {
    private static final ColorRGBA labelColor = new ColorRGBA(0.3568628F, 0.07450981F, 0.07450981F, 1.0F);
    private static final ColorRGBA shadowColor = new ColorRGBA(0.0F, 0.0F, 0.0F, 0.0F);
    private Application app;
    private GameClient gameClient;
    private World world;
    private EntityData ed;
    private BitmapFont writingFont;
    private MapState mapState;
    private EntityId parent;
    private EntitySet entities;
    private PropertyMarkers localClaims = null;
    private Entity currentZone = null;

    private Map<Entity, Label> labels = new HashMap();

    public PropertyMarkers(Application app, GameClient gameClient, MapState mapState) {
        this.app = app;
        this.gameClient = gameClient;
        this.world = gameClient.getWorld();
        this.ed = this.world.getEntityData();
        this.mapState = mapState;
        super.setEnabled(false);

        this.writingFont = app.getAssetManager().loadFont("Interface/templar32.fnt");
    }

    protected void initialize() {
        this.entities = this.ed.getEntities(new FieldFilter(ClaimType.class, "parent", this.parent), new Class[]{ClaimType.class, ClaimArea.class, Position.class, Name.class, OwnedBy.class});

        addNodes(this.entities);
    }

    protected void terminate() {
        if (this.localClaims != null)
            this.localClaims.terminate();
        this.localClaims = null;
        this.currentZone = null;
        removeNodes(this.entities);
        this.entities.release();
        this.entities = null;
    }

    protected void adjustLocal() {
        Matrix4f transform = this.mapState.getMapTransform();
        float scale = this.mapState.getMapScale();

        for (Map.Entry e : this.labels.entrySet()) {
            Position pos = (Position) ((Entity) e.getKey()).get(Position.class);
            Name name = (Name) ((Entity) e.getKey()).get(Name.class);

            Vector3f v = pos.getLocation();
            v = transform.mult(v);

            Quaternion rot = pos.getRotation();
            float[] angles = rot.toAngles(null);
            rot = new Quaternion().fromAngles(0.0F, 0.0F, angles[1]);
            Label l = (Label) e.getValue();
            l.setLocalRotation(rot);
            l.setLocalTranslation(v.x, v.y, 0.0F);

            ClaimType type = (ClaimType) ((Entity) e.getKey()).get(ClaimType.class);
            if (type.canBeParent()) {
                l.setLocalScale(scale / 1.2F);
                l.setText(name.getName());
            } else if (type.getClaimType() == 1) {
                l.setLocalScale(scale / 3.0F);
                String s = name.getName();
                s = s.replaceAll("\\s+", "\n");
                l.setText(s);
            } else {
                l.setLocalScale(scale / 5.0F);
                l.setText(name.getName());
            }
        }
    }

    protected void addNodes(Set<Entity> set) {
        for (Entity e : set) {
            Label l = new Label(this.writingFont);
            l.setText("Test");
            l.setHAlignment(HAlignment.CENTER);
            l.setColor(labelColor);
            l.setShadowColor(shadowColor);
            this.labels.put(e, l);
            ((Node) this.spatial).attachChild(l);
        }
    }

    protected void updateNodes(Set<Entity> set) {
    }

    protected void removeNodes(Set<Entity> set) {
        for (Entity e : set) {
            Label l = (Label) this.labels.remove(e);
            if (l != null)
                ((Node) this.spatial).detachChild(l);
        }
    }

    public Control cloneForSpatial(Spatial spatial) {
        return this;
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
        if (this.localClaims != null)
            this.localClaims.controlRender(rm, vp);
    }

    protected void controlUpdate(float tpf) {
        if (this.entities == null) {
            return;
        }
        this.entities.applyChanges();
        removeNodes(this.entities.getRemovedEntities());
        addNodes(this.entities.getAddedEntities());
        updateNodes(this.entities.getChangedEntities());
        if (this.localClaims != null) {
            this.localClaims.controlUpdate(tpf);
        }
        adjustLocal();
    }
}