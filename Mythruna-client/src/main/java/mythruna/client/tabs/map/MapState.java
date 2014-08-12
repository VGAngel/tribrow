package mythruna.client.tabs.map;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.event.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import mythruna.Coordinates;
import mythruna.client.GameAppState;
import mythruna.client.GameClient;
import mythruna.client.KeyMethodAction;
import mythruna.client.ModeManager;
import mythruna.client.anim.Animation;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import mythruna.client.ui.HAlignment;
import mythruna.client.ui.Label;
import mythruna.client.ui.ObservableState;
import mythruna.sim.Mob;
import mythruna.sim.MobClass;
import org.progeeks.util.log.Log;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapState extends ObservableState implements AnalogListener {

    static Log log = Log.getLog();
    private static final String MAP_ZOOM = "Map Zoom";
    private static final String ZOOM_IN = "Zoom In";
    private static final String ZOOM_OUT = "Zoom Out";
    private Application app;
    private AssetManager assets;
    private GameClient gameClient;
    private GameAppState gameState;
    private Node mapNode;
    private Picture mapPicture;
    private Material mapMaterial;
    private MapBuilder mapBuilder;
    private Node shadowLayer;
    private Node markerLayer;
    private Node labelLayer;
    private BitmapFont labelFont;
    private Marker player;
    private Marker dirMarker;
    private Marker spawn;
    private float mapSize;
    private float imageSize;
    private int xMap;
    private int yMap;
    private Vector3f mapView = new Vector3f(0.0F, 0.0F, 1.0F);

    private float viewSize = 500.0F;

    private Map<Mob, Marker> playerMarkers = new HashMap();
    private Matrix4f mapTransform;
    private MouseObserver mouseObserver = new MouseObserver();

    private KeyMethodAction zoomIn = new KeyMethodAction(this, "zoomIn", 78);
    private KeyMethodAction zoomOut = new KeyMethodAction(this, "zoomOut", 74);

    private Set<Marker> hits = new HashSet();
    private Map<Marker, Label> hitLabels = new HashMap();
    private ImageConverter imageConverter;
    private PropertyMarkers worldMarkers;

    public MapState(GameClient gameClient, GameAppState gameState) {
        super("Map", false);
        this.gameClient = gameClient;
        this.gameState = gameState;
        this.mapBuilder = new MapBuilder(gameClient.getWorld().getWorldDatabase());
    }

    protected Vector3f getMapPosition() {
        Camera cam = this.app.getCamera();
        float width = cam.getHeight() * 0.9F;
        return new Vector3f(cam.getWidth() * 0.5F - width * 0.5F, cam.getHeight() * 0.5F - width * 0.5F, -1.0F);
    }

    protected Node getMapNode() {
        if (this.mapNode != null) {
            return this.mapNode;
        }
        this.mapNode = new Node("World Map");
        this.mapNode.addControl(this.worldMarkers);

        this.shadowLayer = new Node("Shadows");
        this.markerLayer = new Node("Markers");
        this.labelLayer = new Node("Labels");

        this.mapPicture = new Picture("Map");
        Camera cam = this.app.getCamera();

        float width = cam.getHeight() * 0.9F;
        this.mapSize = width;

        this.imageSize = this.mapBuilder.getImageSize();

        this.mapPicture.setWidth(width);
        this.mapPicture.setHeight(width);

        this.assets = this.app.getAssetManager();
        this.mapMaterial = new Material(this.assets, "MatDefs/Map.j3md");
        this.mapMaterial.setTexture("Border", this.assets.loadTexture("Interface/map-border.png"));
        this.mapMaterial.setTexture("Mask", this.assets.loadTexture("Interface/map-mask.png"));
        this.mapMaterial.setVector3("ViewParms", this.mapView);
        this.mapMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.mapPicture.setMaterial(this.mapMaterial);

        this.mapNode.attachChild(this.mapPicture);
        this.mapNode.setLocalTranslation(getMapPosition());

        this.mapNode.attachChild(this.shadowLayer);
        this.mapNode.attachChild(this.markerLayer);
        this.mapNode.attachChild(this.labelLayer);

        this.dirMarker = new Marker("Rose", new ColorRGBA(0.0F, 0.0F, 0.3F, 1.0F), "Interface/dir-marker.png", null, 36.75F, 24.75F, 12.375F, 12.375F, new ColorRGBA(0.0F, 0.5F, 0.7F, 1.0F));

        this.dirMarker.setVisible(true);

        this.player = new Marker("You", ColorRGBA.Green, "Interface/person-marker.png", "Interface/person-marker-shadow.png", 25.0F, 25.0F, 11.0F, 3.0F);

        this.player.setVisible(true);

        this.spawn = new Marker("Spawn", ColorRGBA.Cyan, "Interface/flag-marker.png", "Interface/flag-marker-shadow.png", 25.0F, 25.0F, 4.0F, 3.0F);

        Quad quad = new Quad(2000.0F, 2000.0F);
        Geometry overlay = new Geometry("maskOverlay", quad);
        Material m = new Material(this.assets, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", new ColorRGBA(1.0F, 0.0F, 0.0F, 0.0F));
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        overlay.setMaterial(m);
        float overlayZ = 0.5F;
        overlay.setLocalTranslation(-1995.0F, -100.0F, overlayZ);
        overlay.setUserData("guiLayer", Integer.valueOf(-1));
        this.mapNode.attachChild(overlay);

        overlay = new Geometry("maskOverlay", quad);
        overlay.setMaterial(m);
        overlay.setLocalTranslation(width - 10.0F, -100.0F, overlayZ);
        overlay.setUserData("guiLayer", Integer.valueOf(-1));
        this.mapNode.attachChild(overlay);

        overlay = new Geometry("maskOverlay", quad);
        overlay.setMaterial(m);
        overlay.setLocalTranslation(0.0F, -1990.0F, overlayZ);
        overlay.setUserData("guiLayer", Integer.valueOf(-1));
        this.mapNode.attachChild(overlay);

        overlay = new Geometry("maskOverlay", quad);
        overlay.setMaterial(m);
        overlay.setLocalTranslation(0.0F, width - 6.0F, overlayZ);
        overlay.setUserData("guiLayer", Integer.valueOf(-1));
        this.mapNode.attachChild(overlay);

        this.mapPicture.setUserData("guiLayer", Integer.valueOf(-1));

        return this.mapNode;
    }

    protected void refreshPicture() {
        Vector3f loc = this.gameClient.getLocation();
        int x = Coordinates.worldToLeaf(loc.x);
        int y = Coordinates.worldToLeaf(loc.y);

        BufferedImage mapImage = this.mapBuilder.getMapImage(x - 16, y - 16);
        if (mapImage == null) {
            return;
        }

        this.xMap = x;
        this.yMap = y;

        if (this.imageConverter == null) {
            this.imageConverter = new ImageConverter(mapImage);
        }
        Image image = this.imageConverter.toImage();
        Texture2D texture = new Texture2D();
        texture.setImage(image);
        this.mapMaterial.setTexture("Map", texture);
    }

    public void toggleMap() {
        setEnabled(!isEnabled());
    }

    public Matrix4f getMapTransform() {
        int x = this.xMap;
        int y = this.yMap;

        float scaledOffset = (this.imageSize - this.viewSize) * 0.5F;

        Vector3f loc = this.gameClient.getLocation();
        float xOffset = loc.x - Coordinates.leafToWorld(x);
        float yOffset = loc.y - Coordinates.leafToWorld(y);

        Matrix4f transform = getMapTransform(x, y, scaledOffset + xOffset, scaledOffset + yOffset);

        Matrix4f flip = new Matrix4f();
        flip.setScale(1.0F, -1.0F, 1.0F);
        flip.setTranslation(0.0F, this.mapSize, 0.0F);

        return flip.multLocal(transform);
    }

    public float getMapScale() {
        return this.mapSize / this.viewSize;
    }

    protected Matrix4f getMapTransform(int xLeaf, int yLeaf, float xOffset, float yOffset) {
        float xTranslate = Coordinates.leafToWorld(xLeaf - 16) + xOffset;
        float yTranslate = Coordinates.leafToWorld(yLeaf - 16) + yOffset;
        Matrix4f transform1 = new Matrix4f();
        transform1.setTranslation(-xTranslate, -yTranslate, 0.0F);

        Matrix4f transform2 = new Matrix4f();
        float scale = this.mapSize / this.viewSize;
        transform2.setScale(scale, scale, 1.0F);

        transform2.multLocal(transform1);

        this.mapTransform = transform2;

        return this.mapTransform;
    }

    public void update(float tpf) {
        refreshPicture();

        int x = this.xMap;
        int y = this.yMap;

        float scaledOffset = (this.imageSize - this.viewSize) * 0.5F;

        Vector3f loc = this.gameClient.getLocation();
        Quaternion facing = this.gameClient.getFacing();
        float xOffset = loc.x - Coordinates.leafToWorld(x);
        float yOffset = loc.y - Coordinates.leafToWorld(y);

        this.mapView.x = (scaledOffset + xOffset);
        this.mapView.y = (scaledOffset - yOffset);
        this.mapView.z = this.viewSize;
        this.mapView.multLocal(1.0F / this.imageSize);

        Matrix4f transform = getMapTransform(x, y, scaledOffset + xOffset, scaledOffset + yOffset);

        Vector3f pos = transform.mult(loc);
        this.player.setLocation(pos.x, pos.y, pos.y);

        float[] angles = facing.toAngles(null);
        this.dirMarker.setRotation(angles[1]);
        this.dirMarker.setLocation(pos.x, pos.y, 1.0F);

        pos = transform.mult(new Vector3f(512.5F, 512.5F, 0.0F));
        if ((pos.x < 12.0F) || (pos.x > this.mapSize - 15.0F) || (pos.y < 12.0F) || (pos.y > this.mapSize - 12.0F)) {
            this.spawn.setVisible(false);
        } else {
            this.spawn.setLocation(pos.x, pos.y, pos.y);
            this.spawn.setVisible(true);
        }

        Map newMarkers = new HashMap();

        long time = this.gameClient.getTime(GameClient.TimeType.RENDER);
        for (Mob e : this.gameClient.getMobs().mobs(MobClass.PLAYER)) {
            if (e.getId() != this.gameClient.getPlayer().getId()) {
                Marker m = (Marker) this.playerMarkers.remove(e);
                if (m == null) {
                    m = new Marker(e.getName(), ColorRGBA.Blue, "Interface/person-marker.png", "Interface/person-marker-shadow.png", 25.0F, 25.0F, 11.0F, 3.0F);

                    m.setVisible(true);
                }

                newMarkers.put(e, m);

                Vector3f entityPos = e.getPosition(time);
                if (entityPos == null) {
                    log.warn("************************");
                    log.warn("Mob:" + e + "  has null position at time:" + time);
                    log.warn("************************");
                } else {
                    pos = transform.mult(entityPos);

                    m.setLocation(pos.x, pos.y, pos.y);

                    if ((pos.x < 12.0F) || (pos.x > this.mapSize - 15.0F) || (pos.y < 12.0F) || (pos.y > this.mapSize - 12.0F))
                        m.setVisible(false);
                    else
                        m.setVisible(true);
                }
            }
        }
        for (Marker m : this.playerMarkers.values()) {
            m.setVisible(false);
        }

        this.playerMarkers = newMarkers;

        Map newLabels = new HashMap();
        for (Marker m : this.hits) {
            Label label = (Label) this.hitLabels.remove(m);
            if (label == null) {
                label = new Label(this.labelFont);
                label.setHAlignment(HAlignment.CENTER);
                label.setText(m.getLabel());
                label.setCullHint(Spatial.CullHint.Never);
                ColorRGBA textColor = new ColorRGBA();
                textColor.interpolate(m.getColor(), ColorRGBA.White, 0.75F);
                label.setColor(textColor);
                this.labelLayer.attachChild(label);
            }
            newLabels.put(m, label);
            Vector3f p = m.getLocation();
            float yText = 18.0F;
            label.setLocalTranslation(Math.round(p.x), Math.round(this.mapSize - p.y + yText), 10.0F);
        }

        for (Label l : this.hitLabels.values()) {
            this.labelLayer.detachChild(l);
        }
        this.hitLabels = newLabels;
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.app = app;
        this.assets = app.getAssetManager();

        this.worldMarkers = new PropertyMarkers(app, this.gameClient, this);

        KeyMethodAction tab = new KeyMethodAction(this, "toggleMap", 50);
        tab.attach(app.getInputManager());

        ModeManager.instance.addMode("Map Zoom", "Zoom In", "Zoom Out", this);

        this.labelFont = app.getAssetManager().loadFont("Interface/knights24.fnt");
    }

    protected void zoomIn(float value) {
        this.viewSize -= value * 10.0F;
        if (this.viewSize < 200.0F)
            this.viewSize = 200.0F;
    }

    protected void zoomOut(float value) {
        this.viewSize += value * 10.0F;
        if (this.viewSize > 992.0F)
            this.viewSize = 992.0F;
    }

    public void zoomIn() {
        zoomIn(1.0F);
    }

    public void zoomOut() {
        zoomOut(1.0F);
    }

    protected void enable() {
        super.enable();

        Node map = getMapNode();
        refreshPicture();

        this.worldMarkers.setEnabled(true);

        this.gameState.getGuiNode().attachChild(map);

        ModeManager.instance.setMode("Map Zoom", true);
        this.app.getInputManager().addRawInputListener(this.mouseObserver);
        Camera cam = this.app.getCamera();

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(map, 0.1F, 1.0F, 0.25F)});
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.move(map, new Vector3f(10.0F, cam.getHeight() * 0.8F, -1.0F), getMapPosition(), 0.25F)});

        this.zoomIn.attach(this.app.getInputManager());
        this.zoomOut.attach(this.app.getInputManager());
    }

    protected void disable() {
        super.disable();

        this.zoomIn.detach(this.app.getInputManager());
        this.zoomOut.detach(this.app.getInputManager());

        this.worldMarkers.setEnabled(false);

        this.app.getInputManager().removeRawInputListener(this.mouseObserver);
        ModeManager.instance.setMode("Map Zoom", false);

        Node map = getMapNode();
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(map, 1.0F, 0.1F, 0.25F)});
        Camera cam = this.app.getCamera();
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.move(map, getMapPosition(), new Vector3f(10.0F, cam.getHeight() * 0.8F, -1.0F), 0.25F)});

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.detach(map, 0.25F)});
    }

    protected void resetHit() {
        this.hits.clear();
    }

    protected void hit(Marker m, Vector3f loc) {
        this.hits.add(m);
    }

    public void onAnalog(String name, float value, float tpf) {
        if ("Zoom In".equals(name)) {
            zoomIn(value);
        } else if ("Zoom Out".equals(name)) {
            zoomOut(value);
        }
    }

    private class Marker {
        private String label;
        private Node marker;
        private Node highlight;
        private Node shadow;
        private ColorRGBA color;
        private Material markerMaterial;
        private float xOffset;
        private float yOffset;
        private float width;
        private float height;
        private boolean visible;

        public Marker(String label, ColorRGBA color, String tMarker, String tShadow, float width, float height, float xCenter, float yCenter) {
            this(label, color, tMarker, tShadow, width, height, xCenter, yCenter, null);
        }

        public Marker(String label, ColorRGBA color, String tMarker, String tShadow, float width, float height, float xCenter, float yCenter, ColorRGBA highlightColor) {
            this.label = label;
            this.color = color;
            this.xOffset = (-xCenter);
            this.yOffset = (-yCenter);
            this.width = width;
            this.height = height;

            Picture pic = new Picture("marker");
            pic.setWidth(width);
            pic.setHeight(height);
            pic.setPosition(this.xOffset, this.yOffset);

            this.markerMaterial = new Material(MapState.this.assets, "MatDefs/Marker.j3md");
            this.markerMaterial.setTexture("Marker", MapState.this.assets.loadTexture(tMarker));
            this.markerMaterial.setColor("Color", color);
            this.markerMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            pic.setMaterial(this.markerMaterial);

            this.marker = new Node("marker");
            this.marker.attachChild(pic);

            if (highlightColor != null) {
                pic = new Picture("marker highlight");
                pic.setWidth(width);
                pic.setHeight(height);
                pic.setPosition(this.xOffset, this.yOffset);

                this.markerMaterial = new Material(MapState.this.assets, "MatDefs/Marker.j3md");
                this.markerMaterial.setTexture("Marker", MapState.this.assets.loadTexture(tMarker));
                this.markerMaterial.setColor("Color", highlightColor);
                this.markerMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                pic.setMaterial(this.markerMaterial);

                this.highlight = new Node("highlight");
                this.highlight.attachChild(pic);
            }

            if (tShadow != null) {
                pic = new Picture("shadow");
                pic.setWidth(width);
                pic.setHeight(height);
                pic.setImage(MapState.this.app.getAssetManager(), tShadow, true);
                pic.setPosition(this.xOffset, this.yOffset);
                this.shadow = new Node("shadow");
                this.shadow.attachChild(pic);
            }
        }

        public String getLabel() {
            return this.label;
        }

        public ColorRGBA getColor() {
            return this.color;
        }

        public boolean contains(Vector3f p) {
            Vector3f loc = this.marker.getLocalTranslation().clone();

            loc.x += this.xOffset;
            loc.y += this.yOffset;

            if ((p.x < loc.x) || (p.y < loc.y))
                return false;
            if ((p.x > loc.x + this.width) || (p.y > loc.y + this.height)) {
                return false;
            }
            return true;
        }

        public void setVisible(boolean visible) {
            if (this.visible == visible)
                return;
            this.visible = visible;

            if (visible) {
                if (this.shadow != null)
                    MapState.this.shadowLayer.attachChild(this.shadow);
                MapState.this.markerLayer.attachChild(this.marker);
                if (this.highlight != null)
                    MapState.this.markerLayer.attachChild(this.highlight);
            } else {
                if (this.shadow != null)
                    MapState.this.shadowLayer.detachChild(this.shadow);
                MapState.this.markerLayer.detachChild(this.marker);
                if (this.highlight != null)
                    MapState.this.markerLayer.detachChild(this.highlight);
            }
        }

        public void setRotation(float rads) {
            rads -= 1.570796F;
            Quaternion quat = new Quaternion().fromAngleAxis(rads, Vector3f.UNIT_Z);
            if (this.shadow != null)
                this.shadow.setLocalRotation(quat);
            if (this.highlight != null)
                this.highlight.setLocalRotation(quat);
            this.marker.setLocalRotation(quat);
        }

        public void setLocation(float x, float y, float z) {
            if (this.shadow != null) {
                this.shadow.setLocalTranslation(x, MapState.this.mapSize - z, 0.0F);
            }

            if (this.highlight != null)
                this.highlight.setLocalTranslation(x - 1.0F, MapState.this.mapSize - y - 1.0F, 0.1F);
            this.marker.setLocalTranslation(x, MapState.this.mapSize - y, 0.2F);
        }

        public Vector3f getLocation() {
            Vector3f loc = this.marker.getLocalTranslation();
            return new Vector3f(loc.x, MapState.this.mapSize - loc.y, loc.z);
        }

        public String toString() {
            return "Marker[" + this.label + "]";
        }
    }

    private class MouseObserver
            implements RawInputListener {
        private MouseObserver() {
        }

        public void beginInput() {
        }

        public void endInput() {
        }

        public void onJoyAxisEvent(JoyAxisEvent evt) {
        }

        public void onJoyButtonEvent(JoyButtonEvent evt) {
        }

        public void onMouseMotionEvent(MouseMotionEvent evt) {
            float x = evt.getX();
            float y = evt.getY();

            Vector3f mapLoc = MapState.this.mapNode.getLocalTranslation();
            x -= mapLoc.x;
            y -= mapLoc.y;

            Vector3f loc = new Vector3f(x, y, 0.0F);

            MapState.this.resetHit();

            for (MapState.Marker m : MapState.this.playerMarkers.values()) {
                if (m.contains(loc))
                    MapState.this.hit(m, loc);
            }
            if (MapState.this.player.contains(loc))
                MapState.this.hit(MapState.this.player, loc);
            if (MapState.this.spawn.contains(loc))
                MapState.this.hit(MapState.this.spawn, loc);
        }

        public void onMouseButtonEvent(MouseButtonEvent evt) {
        }

        public void onKeyEvent(KeyInputEvent evt) {
        }

        public void onTouchEvent(TouchEvent evt) {
        }
    }
}