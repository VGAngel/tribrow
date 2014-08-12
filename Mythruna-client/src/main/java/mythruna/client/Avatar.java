package mythruna.client;

import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import mythruna.MaterialIndex;
import mythruna.es.EntityId;

import java.util.ArrayList;
import java.util.List;

public class Avatar extends Node {
    public static final String DEFAULT_NAME = "Loading...";
    public static final String TYPE = "Avatar";
    private Spatial model;
    private String name;
    private float headHeight = 1.7F;
    private BitmapFont font;
    private BitmapText label;
    private Node textNode;
    private Quaternion facing;
    private Node headNode;
    private Node hairNode;
    private SkeletonControl skeletonControl;
    private List<Material> materials = new ArrayList();

    public Avatar(AssetManager assets, String name) {
        super("FemaleAvatar");

        String tempName = name == null ? "Loading..." : name;

        float scale = 0.2700617F;

        this.model = assets.loadModel("Models/female-parts.j3o");

        setupMaterial(this.model, assets);

        if ((this.model instanceof Node))
            fixBucket((Node) this.model);
        this.model.setQueueBucket(RenderQueue.Bucket.Opaque);
        this.model.setUserData("type", "Avatar");

        attachChild(this.model);

        this.model.setLocalTranslation(0.0F, -(this.headHeight / scale), 0.0F);
        setLocalScale(scale, scale, scale);

        this.font = assets.loadFont("Interface/knights24-outline.fnt");

        this.textNode = new Node("LabelNode");
        this.textNode.setLocalScale(0.7F, 0.7F, 0.7F);

        updateText(tempName);

        this.textNode.setLocalTranslation(0.0F, this.label.getHeight(), 0.0F);
        attachChild(this.textNode);

        System.out.println("Found skeleton:" + getSkeleton());
    }

    public int collideWith(Collidable other, CollisionResults results) {
        return super.collideWith(other, results);
    }

    protected void showControls(Spatial s) {
        System.out.println("show controls:" + s);
        int count = s.getNumControls();
        for (int i = 0; i < count; i++) {
            Control c = s.getControl(i);
            System.out.println("  c[" + i + "] = " + c);
        }

        if ((s instanceof Node)) {
            for (Spatial child : ((Node) s).getChildren())
                showControls(child);
        }
    }

    protected void attachHead(Node n) {
        this.headNode = n;
    }

    protected void attachHair(Node n) {
        this.hairNode = n;
    }

    protected void setupMaterial(Spatial s, AssetManager assets) {
        if ((s instanceof Geometry))
            setupMaterial((Geometry) s, assets);
        else
            setupMaterial((Node) s, assets);
    }

    protected void setupMaterial(Node n, AssetManager assets) {
        System.out.println("Node:" + n);
        if ("head".equals(n.getName()))
            attachHead(n);
        else if ("hair".equals(n.getName())) {
            attachHair(n);
        }
        for (Spatial s : n.getChildren())
            setupMaterial(s, assets);
    }

    protected void setupMaterial(Geometry g, AssetManager assets) {
        Material m = g.getMaterial();
        System.out.println("geom:" + g + "  has material:" + m);
        System.out.println("  pos:" + g.getLocalTranslation());

        MatParamTexture mpt = m.getTextureParam("DiffuseMap");
        Texture t = mpt.getTextureValue();
        System.out.println("  -- texture:" + t);

        Material material = new Material(assets, "MatDefs/LightingWithFog2.j3md");

        material.setTexture("DiffuseMap", t);

        material.setColor("Diffuse", MaterialIndex.getDiffuse());
        material.setColor("Ambient", MaterialIndex.getAmbient().mult(1.5F));
        material.setColor("Specular", MaterialIndex.getSpecular());
        material.setColor("FogColor", MaterialIndex.getFogColor());
        material.setFloat("Shininess", 100.0F);
        material.setFloat("SunFactor", 1.0F);
        material.setFloat("LightFactor", 0.0F);
        material.setBoolean("UseMaterialColors", true);

        g.setMaterial(material);

        this.materials.add(material);
    }

    public void setEntityId(EntityId id) {
        if (id != null)
            this.model.setUserData("id", Long.valueOf(id.getId()));
    }

    public long getEntityId() {
        Long l = (Long) this.model.getUserData("id");
        if (l == null)
            return -1L;
        return l.longValue();
    }

    protected void updateText(String s) {
        if (this.label != null) {
            this.label.removeFromParent();
        }
        this.label = new BitmapText(this.font, false);
        this.label.setSize(1.0F);
        this.label.setText(s);
        float textWidth = this.label.getLineWidth() + 20.0F;
        float textOffset = textWidth / 2.0F;
        this.label.setBox(new Rectangle(-textOffset, 0.0F, textWidth, this.label.getHeight()));
        this.label.setColor(new ColorRGBA(0.0F, 1.0F, 1.0F, 1.0F));
        this.label.setAlignment(BitmapFont.Align.Center);
        this.label.setQueueBucket(RenderQueue.Bucket.Transparent);
        BillboardControl bc = new BillboardControl();
        bc.setAlignment(BillboardControl.Alignment.Screen);
        this.label.addControl(bc);

        this.textNode.attachChild(this.label);
    }

    public void setName(String name) {
        if ((this.name != null) && (this.name.equals(name)))
            return;
        if (name == null) {
            return;
        }
        this.name = name;

        updateText(name);
    }

    public String getName() {
        return this.name;
    }

    public float getHeadHeight() {
        return this.headHeight;
    }

    protected static void fixBucket(Node n) {
        n.setQueueBucket(RenderQueue.Bucket.Opaque);
        for (Spatial c : n.getChildren()) {
            c.setQueueBucket(RenderQueue.Bucket.Opaque);
            if ((c instanceof Node))
                fixBucket((Node) c);
        }
    }

    protected SkeletonControl findSkeletonControl(Node n) {
        for (Spatial s : n.getChildren()) {
            SkeletonControl sc = (SkeletonControl) s.getControl(SkeletonControl.class);

            if (sc != null) {
                return sc;
            }
            if ((s instanceof Node))
                sc = findSkeletonControl((Node) s);
            if (sc != null)
                return sc;
        }
        return null;
    }

    protected Skeleton getSkeleton() {
        if (this.skeletonControl != null)
            return this.skeletonControl.getSkeleton();
        this.skeletonControl = ((SkeletonControl) this.model.getControl(SkeletonControl.class));
        if (this.skeletonControl != null) {
            return this.skeletonControl.getSkeleton();
        }

        if ((this.model instanceof Node)) {
            this.skeletonControl = findSkeletonControl((Node) this.model);
        }

        if (this.skeletonControl != null)
            return this.skeletonControl.getSkeleton();
        return null;
    }

    public void setLighting(float sun, float local) {
        for (Material material : this.materials) {
            material.setFloat("SunFactor", sun);
            material.setFloat("LightFactor", local);
        }
    }

    public void setFacing(Quaternion quat) {
        if ((this.facing != null) && (this.facing.equals(quat))) {
            return;
        }
        this.facing = quat.clone();
        float[] angles = this.facing.toAngles(null);

        Quaternion body = new Quaternion().fromAngles(0.0F, angles[1], 0.0F);

        float angle = FastMath.clamp(angles[0], -1.570796F, 1.570796F);
        Quaternion head = new Quaternion().fromAngles(angle * 0.5F, 0.0F, 0.0F);
        Quaternion realHead = new Quaternion().fromAngles(angle * 0.5F, 0.0F, 0.0F);

        setLocalRotation(body);

        Bone b = getSkeleton().getBone("Head");
        b.setUserControl(true);
        b.setUserTransforms(Vector3f.ZERO, head, Vector3f.UNIT_XYZ);

        if (this.headNode != null)
            this.headNode.setLocalRotation(realHead);
        if (this.hairNode != null)
            this.hairNode.setLocalRotation(realHead);
    }

    public String toString() {
        return "Avatar[" + getName() + ", " + getEntityId() + "]";
    }
}