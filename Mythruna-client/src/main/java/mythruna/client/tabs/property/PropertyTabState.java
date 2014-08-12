package mythruna.client.tabs.property;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import mythruna.client.GameAppState;
import mythruna.client.GameClient;
import mythruna.client.KeyMethodAction;
import mythruna.client.PostProcessingState;
import mythruna.client.anim.Animation;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import mythruna.client.ui.Label;
import mythruna.client.ui.MouseEventControl;
import mythruna.client.ui.MouseListener;
import mythruna.client.ui.ObservableState;
import mythruna.es.*;
import org.progeeks.util.log.Log;

import java.util.*;

public class PropertyTabState extends ObservableState
        implements AnalogListener {
    static Log log = Log.getLog();
    private static final float REFERENCE_HEIGHT = 720.0F;
    private static final ColorRGBA titleColor = new ColorRGBA(0.2431373F, 0.2431373F, 0.4156863F, 1.0F);
    private static final ColorRGBA selectColor = new ColorRGBA(0.1215686F, 0.3921569F, 0.2078431F, 1.0F);

    private static final ColorRGBA nameColor = new ColorRGBA(0.3568628F, 0.07450981F, 0.07450981F, 1.0F);
    private static final ColorRGBA nameSelectColor = new ColorRGBA(0.1215686F, 0.3921569F, 0.3921569F, 1.0F);

    private static final ColorRGBA shadowColor = new ColorRGBA(0.0F, 0.0F, 0.0F, 0.0F);
    private GameClient gameClient;
    private GameAppState gameState;
    private AssetManager assets;
    private float heightOffset;
    private BitmapFont writingFont;
    private Node propertyNode;
    private Node smallBook;
    private PropertyViewPanel viewPanel;
    private EntitySet claims;
    private Map<String, PropertyGroup> groups = new HashMap<>();
    private PropertyGroup currentGroup = null;

    public PropertyTabState(GameClient gameClient, GameAppState gameState) {
        super("Property", false);
        this.gameClient = gameClient;
        this.gameState = gameState;
    }

    public void toggleProperty() {
        setEnabled(!isEnabled());
    }

    public void update(float tpf) {
        if ((this.claims != null) && (this.claims.applyChanges())) {
            applyAdds(this.claims.getAddedEntities());
            applyChanges(this.claims.getChangedEntities());
            applyRemoves(this.claims.getRemovedEntities());
        }

        boolean changed = false;
        for (PropertyGroup g : this.groups.values()) {
            if (g.update())
                changed = true;
        }
        if (changed) {
            float x = 240.0F;
            float y = 410.0F;

            PropertyGroup group = getGroup("Strongholds", true);
            group.getLabel().setLocalTranslation(x, y, 0.0F);
            y -= group.getHeight();

            group = getGroup("Towns", true);
            group.getLabel().setLocalTranslation(x, y, 0.0F);
            y -= group.getHeight();

            group = getGroup("Cities", true);
            group.getLabel().setLocalTranslation(x, y, 0.0F);
            y -= group.getHeight();

            group = getGroup("Town/City Plots", true);
            group.getLabel().setLocalTranslation(x, y, 0.0F);
            y -= group.getHeight();
        }
    }

    protected void applyAdds(Set<Entity> entities) {
        for (Entity e : entities) {
            ClaimType type = (ClaimType) e.get(ClaimType.class);
            getGroup(type.getClaimType(), true).addEntity(e);
        }
    }

    protected void applyRemoves(Set<Entity> entities) {
        for (Entity e : entities) {
            ClaimType type = (ClaimType) e.get(ClaimType.class);
            getGroup(type.getClaimType(), true).removeEntity(e);
        }
    }

    protected void applyChanges(Set<Entity> entities) {
        for (Entity e : entities) {
            ClaimType type = (ClaimType) e.get(ClaimType.class);

            System.out.println("Claim changed:" + e);
        }
    }

    protected String getGroupName(byte type) {
        switch (type) {
            case 0:
            default:
                return "World";
            case 1:
                return "Strongholds";
            case 2:
                return "Towns";
            case 3:
                return "Cities";
            case 4:
            case 5:
        }
        return "Town/City Plots";
    }

    protected PropertyGroup getGroup(byte type, boolean create) {
        return getGroup(getGroupName(type), create);
    }

    protected PropertyGroup getGroup(String name, boolean create) {
        PropertyGroup result = (PropertyGroup) this.groups.get(name);
        if ((result == null) && (create)) {
            result = new PropertyGroup(name);
            this.groups.put(name, result);
        }
        return result;
    }

    protected void setCurrentGroup(PropertyGroup group) {
        if (this.currentGroup == group)
            return;
        this.currentGroup = group;
        for (PropertyGroup g : this.groups.values()) {
            g.setActivated(g == this.currentGroup);
        }
    }

    protected void setCurrentProperty(Entity property) {
        System.out.println("setCurrentProperty( " + property + " )");
        this.viewPanel.setProperty(property);
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.assets = app.getAssetManager();

        Camera cam = getApplication().getCamera();
        float scale = 1.0F;
        if (cam.getHeight() < 720.0F) {
            scale = cam.getHeight() / 720.0F;
            this.heightOffset = 0.0F;
        } else {
            this.heightOffset = (cam.getHeight() - 720.0F);
        }

        this.propertyNode = new Node("Property Tab Root");
        this.propertyNode.setLocalScale(scale);
        this.propertyNode.setLocalTranslation(0.0F, this.heightOffset, 0.0F);
        this.smallBook = new Node("Property Ledger");

        this.viewPanel = new PropertyViewPanel(this.gameClient, app);
        this.viewPanel.setLocalTranslation(510.0F, this.heightOffset, 0.0F);
        this.propertyNode.attachChild(this.viewPanel);

        KeyMethodAction tab = new KeyMethodAction(this, "toggleProperty", 25);
        tab.attach(app.getInputManager());

        this.writingFont = app.getAssetManager().loadFont("Interface/templar32.fnt");

        Quad quad = new Quad(512.0F, 512.0F);
        Geometry smallBookGeom = new Geometry("Property Ledger", quad);
        Material m = new Material(this.assets, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setTexture("ColorMap", this.assets.loadTexture("Interface/small-book.jpg"));
        smallBookGeom.setMaterial(m);
        smallBookGeom.setLocalTranslation(0.0F, 0.0F, -5.0F);

        this.smallBook.attachChild(smallBookGeom);

        PropertyGroup group = getGroup("Strongholds", true);

        this.smallBook.attachChild(group.getLabel());

        group = getGroup("Towns", true);

        this.smallBook.attachChild(group.getLabel());

        group = getGroup("Cities", true);

        this.smallBook.attachChild(group.getLabel());

        group = getGroup("Town/City Plots", true);

        this.smallBook.attachChild(group.getLabel());

        this.propertyNode.attachChild(this.smallBook);
    }

    protected void enable() {
        super.enable();
        ((PostProcessingState) getStateManager().getState(PostProcessingState.class)).setRadialFadeOn(1.5F, 1.5F);

        Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();
        gui.attachChild(this.propertyNode);

        Camera cam = getApplication().getCamera();
        Vector3f start = new Vector3f(0.0F, 720.0F, -1.0F);
        Vector3f end = new Vector3f(-50.0F, 163.0F, -1.0F);

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.move(this.smallBook, start, end, 0.25F)});
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(this.smallBook, 0.01F, 1.0F, 0.25F)});

        end = new Vector3f(510.0F, this.heightOffset, 0.0F);
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.move(this.viewPanel, start, end, 0.25F)});
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(this.viewPanel, 0.01F, 1.0F, 0.25F)});

        EntityData ed = this.gameClient.getEntityData();
        this.claims = ed.getEntities(new FieldFilter(OwnedBy.class, "ownerId", this.gameClient.getPlayer()), new Class[]{OwnedBy.class, ClaimType.class, ClaimArea.class, Name.class});

        applyAdds(this.claims);

        this.viewPanel.enable();
    }

    protected void disable() {
        super.disable();

        this.viewPanel.setProperty(null);
        this.viewPanel.disable();
        applyRemoves(this.claims);
        this.claims.release();
        this.claims = null;

        ((PostProcessingState) getStateManager().getState(PostProcessingState.class)).setRadialFade(false);

        Camera cam = getApplication().getCamera();
        Vector3f start = new Vector3f(0.0F, 720.0F, -1.0F);
        Vector3f end = new Vector3f(-50.0F, 163.0F, -1.0F);

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.detach(this.propertyNode, 0.25F), Animation.scale(this.smallBook, 1.0F, 0.01F, 0.25F), Animation.move(this.smallBook, end, start, 0.25F)});

        end = new Vector3f(510.0F, this.heightOffset, 0.0F);
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(this.viewPanel, 1.0F, 0.01F, 0.25F), Animation.move(this.viewPanel, end, start, 0.25F)});
    }

    public void onAnalog(String name, float value, float tpf) {
        System.out.println("onAnalog(" + name + ", " + value + ")");
    }

    private class GroupExpander
            implements MouseListener {
        private PropertyTabState.PropertyGroup group;
        private int xDown;
        private int yDown;

        public GroupExpander(PropertyTabState.PropertyGroup group) {
            this.group = group;
        }

        public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
            event.setConsumed();

            if (event.isPressed()) {
                this.xDown = event.getX();
                this.yDown = event.getY();
            } else {
                int x = event.getX();
                int y = event.getY();
                if ((Math.abs(x - this.xDown) < 3) && (Math.abs(y - this.yDown) < 3)) {
                    System.out.println("Click:" + capture);

                    if (this.group.isActivated())
                        PropertyTabState.this.setCurrentGroup(null);
                    else
                        PropertyTabState.this.setCurrentGroup(this.group);
                }
            }
        }

        public void mouseEntered(MouseMotionEvent event, Spatial capture) {
            this.group.getLabel().setColor(PropertyTabState.selectColor);
        }

        public void mouseExited(MouseMotionEvent event, Spatial capture) {
            this.group.getLabel().setColor(PropertyTabState.titleColor);
            this.group.getLabel().setShadowColor(PropertyTabState.shadowColor);
        }

        public void mouseMoved(MouseMotionEvent event, Spatial capture) {
        }
    }

    private class PropertySelector
            implements MouseListener {
        private Label label;
        private Entity property;
        private int xDown;
        private int yDown;

        public PropertySelector(Label label, Entity property) {
            this.label = label;
            this.property = property;
        }

        public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
            event.setConsumed();

            if (event.isPressed()) {
                this.xDown = event.getX();
                this.yDown = event.getY();
            } else {
                int x = event.getX();
                int y = event.getY();
                if ((Math.abs(x - this.xDown) < 3) && (Math.abs(y - this.yDown) < 3)) {
                    System.out.println("Click:" + capture);

                    PropertyTabState.this.setCurrentProperty(this.property);
                }
            }
        }

        public void mouseEntered(MouseMotionEvent event, Spatial capture) {
            this.label.setColor(PropertyTabState.nameSelectColor);
        }

        public void mouseExited(MouseMotionEvent event, Spatial capture) {
            this.label.setColor(PropertyTabState.nameColor);
            this.label.setShadowColor(PropertyTabState.shadowColor);
        }

        public void mouseMoved(MouseMotionEvent event, Spatial capture) {
        }
    }

    private class PropertyGroup {
        private String name;
        private Label label;
        private boolean activated = false;
        private boolean invalid = true;
        private List<Entity> properties = new ArrayList<>();
        private List<Label> labels = new ArrayList<>();

        private PropertyGroup(String name) {
            this.name = name;

            this.label = new Label(PropertyTabState.this.writingFont);
            this.label.setText("+ " + name);
            this.label.setColor(PropertyTabState.titleColor);
            this.label.setShadowColor(PropertyTabState.shadowColor);
            this.label.addControl(new MouseEventControl());
            ((MouseEventControl) this.label.getControl(MouseEventControl.class)).addMouseListener(new PropertyTabState.GroupExpander(this));
        }

        public float getHeight() {
            float h = 30.0F;
            float spacing = 25.0F;
            if ((!this.properties.isEmpty()) && (this.activated)) {
                h += this.properties.size() * spacing;
                h += spacing * 0.5F;
            }
            return h;
        }

        protected void resetText() {
            StringBuilder sb = new StringBuilder();

            if (this.activated)
                sb.append("- ");
            else {
                sb.append("+ ");
            }
            sb.append(this.name);
            sb.append(" (" + this.properties.size() + ")");

            this.label.setText(sb.toString());
        }

        protected void updateChildren() {
            for (Label l : this.labels) {
                this.label.detachChild(l);
            }
            this.labels.clear();

            if (!this.activated) {
                return;
            }
            float y = 0.0F;
            float spacing = 25.0F;
            for (Entity e : this.properties) {
                Name n = (Name) e.get(Name.class);

                Label l = new Label(PropertyTabState.this.writingFont);
                this.labels.add(l);

                float scale = 0.75F;
                l.setLocalScale(scale);

                l.setText("- " + n.getName());
                l.setColor(PropertyTabState.nameColor);
                l.setShadowColor(PropertyTabState.shadowColor);

                y -= spacing;
                l.setLocalTranslation(15.0F, y, 0.0F);

                l.addControl(new MouseEventControl());
                ((MouseEventControl) l.getControl(MouseEventControl.class)).addMouseListener(new PropertyTabState.PropertySelector(l, e));

                this.label.attachChild(l);
            }
        }

        public void invalidate() {
            this.invalid = true;
        }

        public boolean update() {
            if (this.invalid) {
                resetText();
                updateChildren();
                this.invalid = false;
                return true;
            }
            return false;
        }

        public boolean addEntity(Entity e) {
            if (this.properties.add(e)) {
                invalidate();
                return true;
            }
            return false;
        }

        public boolean removeEntity(Entity e) {
            if (this.properties.remove(e)) {
                invalidate();
                return true;
            }
            return false;
        }

        public void setActivated(boolean f) {
            if (this.activated == f)
                return;
            this.activated = f;
            invalidate();
        }

        public boolean isActivated() {
            return this.activated;
        }

        public Label getLabel() {
            return this.label;
        }
    }
}