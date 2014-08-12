package mythruna.client.env;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import mythruna.GameTime;
import mythruna.MaterialIndex;
import mythruna.client.GameClient;
import mythruna.client.ui.ObservableState;

import java.util.ArrayList;
import java.util.List;

public class Environment extends ObservableState {
    private static Environment instance = new Environment();
    private GameClient gameClient;
    private int effectRate = 100;
    private int lastEffectTime = -1;

    private List<TimeEffect> timeEffects = new ArrayList();
    private Geometry skyGeom;
    private Geometry groundGeom;
    private DirectionalLight sun;
    private Geometry lightSphere;
    private Geometry moonGeom;
    private Node moonNode;
    private Geometry starGeometry;
    private Spatial clouds;
    private Spatial clouds2;
    private Node flareNode;
    private Geometry flare;
    private Material flareMaterial;
    private ColorRGBA flareColor;
    private Vector3f lightPos = new Vector3f();
    private ColorRGBA cloudColor;
    private ColorRGBA waterColor = new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F);

    private ColorRGBA ambient = ColorRGBA.DarkGray.clone();
    private ColorRGBA diffuse = ColorRGBA.White.clone();
    private ColorRGBA specular = new ColorRGBA(0.0F, 0.0F, 0.0F, 1.0F);
    private ColorRGBA fogColor = new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F);

    private float fogDistance = 128.0F;

    public Environment() {
        super("Environment", true);
    }

    public static Environment getInstance() {
        return instance;
    }

    public void setGameClient(GameClient client) {
        this.gameClient = client;
    }

    protected void initialize(Application app) {
        super.initialize(app);

        Node rootNode = ((SimpleApplication) app).getRootNode();
        AssetManager assetManager = app.getAssetManager();

        addTimeEffect(new SunArc());
        addTimeEffect(new MoonArc());

        Sky sky = new Sky(24, 24);
        this.skyGeom = sky.getGeometry();
        Material skyMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture skyTexture = assetManager.loadTexture("Textures/sky-colors.png");
        skyMaterial.setTexture("ColorMap", skyTexture);
        this.skyGeom.setMaterial(skyMaterial);
        rootNode.attachChild(this.skyGeom);

        sky.setEastWest(1.0F, 1.0F, 1.0F);

        StarMesh stars = new StarMesh(20.0F, 1000);

        this.starGeometry = new Geometry("Stars", stars);

        final Material starMaterial = new Material(assetManager, "MatDefs/MyParticle.j3md");
        starMaterial.setBoolean("PointSprite", true);
        starMaterial.setTexture("Texture", assetManager.loadTexture("Textures/Smoke.png"));

        this.starGeometry.setMaterial(starMaterial);
        this.starGeometry.setCullHint(Spatial.CullHint.Never);
        this.starGeometry.setQueueBucket(RenderQueue.Bucket.Sky);

        this.starGeometry.setShadowMode(RenderQueue.ShadowMode.Off);

        float quadratic = app.getCamera().getHeight() / 720.0F * 6.0F;

        starMaterial.setFloat("Quadratic", quadratic);

        rootNode.attachChild(this.starGeometry);

        addTimeEffect(new FloatRangeEffect(new Float[]{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.3F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)}) {
            protected void update(Float value) {
                starMaterial.setFloat("Alpha", value.floatValue());
            }
        });
        this.sun = new DirectionalLight();
        Vector3f lightDir = new Vector3f(-0.3735267F, -0.5044417F, -0.77847F);
        this.sun.setDirection(lightDir);
        this.sun.setColor(new ColorRGBA(1.5F, 1.5F, 1.5F, 1.0F));
        rootNode.addLight(this.sun);

        boolean useFlare = true;

        Material sunMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        if (useFlare)
            sunMaterial.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 0.5F));
        else
            sunMaterial.setColor("Color", new ColorRGBA(0.95F, 0.9F, 0.6F, 1.0F));
        Sphere lite;
        if (useFlare)
            lite = new Sphere(30, 30, 30.0F);
        else {
            lite = new Sphere(30, 30, 30.0F);
        }
        this.lightSphere = new Geometry("lightsphere", lite);
        this.lightSphere.setMaterial(sunMaterial);
        Vector3f lightPos = lightDir.clone().multLocal(-500.0F);
        this.lightSphere.setLocalTranslation(lightPos);
        if (useFlare)
            this.lightSphere.setLocalScale(0.7F);
        this.lightSphere.setQueueBucket(RenderQueue.Bucket.Sky);
        this.lightSphere.setCullHint(Spatial.CullHint.Never);

        float flareSize = 300.0F;
        Quad flareQuad = new Quad(flareSize, flareSize);
        this.flare = new Geometry("flare", flareQuad);
        this.flareMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        this.flareColor = new ColorRGBA(0.95F, 0.9F, 0.7F, 0.8F);
        this.flareMaterial.setColor("Color", this.flareColor);

        this.flareMaterial.setTexture("ColorMap", assetManager.loadTexture("Textures/sun-256.png"));
        this.flareMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.flare.setMaterial(this.flareMaterial);
        this.flare.setLocalTranslation(-(flareSize * 0.5F), -(flareSize * 0.5F), 0.0F);
        this.flareNode = new Node("Flare Node");
        this.flareNode.setLocalTranslation(lightPos);
        this.flareNode.attachChild(this.flare);
        this.flareNode.setQueueBucket(RenderQueue.Bucket.Sky);
        this.flareNode.setCullHint(Spatial.CullHint.Never);
        this.flareNode.addControl(new BillboardControl());

        if (useFlare)
            rootNode.attachChild(this.flareNode);
        rootNode.attachChild(this.lightSphere);

        Texture moonTexture = assetManager.loadTexture("Textures/moon2.png");

        Material moonMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        moonMaterial.setTexture("ColorMap", moonTexture);
        moonMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        final ColorRGBA moonColor = ColorRGBA.White.clone();
        moonMaterial.setColor("Color", moonColor);

        Sphere moonMesh = new Sphere(20, 20, 1.25F);
        this.moonGeom = new Geometry("moon", moonMesh);

        moonMesh.setTextureMode(Sphere.TextureMode.Polar);
        this.moonGeom.setMaterial(moonMaterial);

        this.moonGeom.setQueueBucket(RenderQueue.Bucket.Sky);
        this.moonGeom.setCullHint(Spatial.CullHint.Never);

        this.moonNode = new Node("MoonNode");

        this.moonNode.attachChild(this.moonGeom);
        this.moonNode.setLocalTranslation(new Vector3f(0.0F, 200.0F, 0.0F));

        int starIndex = rootNode.getChildIndex(this.starGeometry);
        rootNode.attachChildAt(this.moonNode, starIndex);

        addTimeEffect(new ColorRangeEffect(new ColorRGBA[]{new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.6F, 0.5F, 0.4F, 1.0F), new ColorRGBA(1.2F, 0.9F, 0.8F, 1.0F), new ColorRGBA(1.4F, 1.1F, 0.9F, 1.0F), new ColorRGBA(1.4F, 1.3F, 1.1F, 1.0F), new ColorRGBA(1.4F, 1.3F, 1.2F, 1.0F), new ColorRGBA(1.3F, 1.3F, 1.2F, 1.0F), new ColorRGBA(1.2F, 1.2F, 1.1F, 1.0F), new ColorRGBA(1.2F, 1.2F, 1.1F, 1.0F), new ColorRGBA(1.2F, 1.2F, 1.1F, 1.0F), new ColorRGBA(1.2F, 1.2F, 1.1F, 1.0F), new ColorRGBA(1.2F, 1.2F, 1.1F, 1.0F), new ColorRGBA(1.3F, 1.3F, 1.2F, 1.0F), new ColorRGBA(1.4F, 1.3F, 1.2F, 1.0F), new ColorRGBA(1.4F, 1.2F, 0.9F, 1.0F), new ColorRGBA(1.3F, 1.1F, 0.8F, 1.0F), new ColorRGBA(0.6F, 0.5F, 0.4F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F)}) {
            protected void update(ColorRGBA value) {
                Environment.this.sun.setColor(value);

                float min = 0.9F;
                float max = 1.1F;
                float delta = max - min;
                float scale = (1.4F - value.r) / 1.3F;
                float colorScale = min + delta * scale;
                colorScale = Math.max(1.0F, colorScale);
                moonColor.r = colorScale;
                moonColor.g = colorScale;
                moonColor.b = colorScale;
                moonColor.a = Math.min(1.0F, scale * scale);
            }
        });
        addTimeEffect(new ColorRangeEffect(new ColorRGBA[]{new ColorRGBA(0.95F, 0.9F, 0.7F, 0.2F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.2F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.2F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.2F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.2F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.5F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.5F), new ColorRGBA(0.95F, 0.9F, 0.8F, 0.7F), new ColorRGBA(0.95F, 0.9F, 0.6F, 0.7F), new ColorRGBA(0.95F, 0.9F, 0.6F, 0.7F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.8F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.8F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.8F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.8F), new ColorRGBA(0.95F, 0.9F, 0.7F, 0.8F), new ColorRGBA(0.95F, 0.9F, 0.6F, 0.7F), new ColorRGBA(0.95F, 0.9F, 0.5F, 0.65F), new ColorRGBA(0.95F, 0.9F, 0.5F, 0.65F), new ColorRGBA(0.95F, 0.85F, 0.55F, 0.65F), new ColorRGBA(0.95F, 0.7F, 0.55F, 0.9F), new ColorRGBA(0.95F, 0.7F, 0.55F, 0.9F), new ColorRGBA(0.95F, 0.6F, 0.55F, 0.2F), new ColorRGBA(0.95F, 0.6F, 0.55F, 0.2F), new ColorRGBA(0.95F, 0.6F, 0.55F, 0.2F)}) {
            protected void update(ColorRGBA value) {
                Environment.this.flareColor.set(value);
            }
        });
        addTimeEffect(new ColorRangeEffect(new ColorRGBA[]{new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.1F, 0.0F, 0.2F, 1.0F), new ColorRGBA(0.2F, 0.2F, 0.3F, 1.0F), new ColorRGBA(0.3F, 0.3F, 0.4F, 1.0F), new ColorRGBA(0.8F, 0.8F, 0.7F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F), new ColorRGBA(0.9F, 0.8F, 0.7F, 1.0F), new ColorRGBA(0.2F, 0.2F, 0.4F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F)}) {
            protected void update(ColorRGBA value) {
                value.a = Environment.this.fogDistance;
                MaterialIndex.setFogColor(value);
            }
        });
        addTimeEffect(new Vector4fRangeEffect(new Vector4f[]{new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.5F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(1.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F), new Vector4f(0.0F, 0.0F, 0.0F, 0.0F)}) {
            protected void update(Vector4f value) {
                MaterialIndex.setTimeParms(value);
            }
        });
        boolean cloudsOn = true;

        if (cloudsOn) {
            addTimeEffect(new CloudArc());

            long baseSeed = 100L;
            CloudLayer cloudLayer = new CloudLayer(baseSeed, 20, 40.0F, 80.0F, 100.0F);
            Mesh cloudMesh = cloudLayer.createMesh();
            CloudLayer cloudLayer2 = new CloudLayer(baseSeed + 1L, 20, 40.0F, 80.0F, 100.0F);
            Mesh cloudMesh2 = cloudLayer2.createMesh();

            Geometry cloudGeometry1 = new Geometry("Cloud1", cloudMesh);
            Geometry cloudGeometry2 = new Geometry("Cloud2", cloudMesh2);

            Material cloudMat = new Material(assetManager, "MatDefs/Cloud.j3md");
            this.cloudColor = new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F);
            cloudMat.setColor("Color", this.cloudColor);

            Texture tex = assetManager.loadTexture("Textures/cloud-atlas-smaller.png");
            cloudMat.setTexture("ColorMap", tex);

            cloudGeometry1.setMaterial(cloudMat);
            cloudGeometry1.setCullHint(Spatial.CullHint.Never);
            cloudGeometry1.setQueueBucket(RenderQueue.Bucket.Sky);
            cloudGeometry2.setMaterial(cloudMat);
            cloudGeometry2.setCullHint(Spatial.CullHint.Never);
            cloudGeometry2.setQueueBucket(RenderQueue.Bucket.Sky);

            rootNode.attachChild(cloudGeometry1);
            rootNode.attachChild(cloudGeometry2);
            this.clouds = cloudGeometry1;
            this.clouds2 = cloudGeometry2;

            addTimeEffect(new ColorRangeEffect(new ColorRGBA[]{new ColorRGBA(0.05F, 0.05F, 0.1F, 2.0F), new ColorRGBA(0.05F, 0.05F, 0.1F, 2.0F), new ColorRGBA(0.05F, 0.05F, 0.1F, 2.0F), new ColorRGBA(0.05F, 0.05F, 0.1F, 2.0F), new ColorRGBA(0.05F, 0.05F, 0.1F, 2.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.5F, 0.3F, 0.3F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.7F, 0.7F, 1.0F), new ColorRGBA(0.7F, 0.6F, 0.6F, 1.0F), new ColorRGBA(0.6F, 0.5F, 0.5F, 1.0F), new ColorRGBA(0.4F, 0.3F, 0.3F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.05F, 0.05F, 0.1F, 2.0F), new ColorRGBA(0.05F, 0.05F, 0.1F, 2.0F)}) {
                protected void update(ColorRGBA value) {
                    Environment.this.cloudColor.set(value);
                }

            });
        }

        Ground ground = new Ground(24);
        this.groundGeom = ground.getGeometry();
        Material groundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        final ColorRGBA groundColor = new ColorRGBA(0.4F, 0.6F, 0.99F, 1.0F);
        groundMat.setColor("Color", groundColor);
        this.groundGeom.setMaterial(groundMat);
        this.groundGeom.setLocalTranslation(0.0F, -1.0F, 0.0F);
        rootNode.attachChild(this.groundGeom);

        addTimeEffect(new ColorRangeEffect(new ColorRGBA[]{new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.1F, 0.0F, 0.2F, 1.0F), new ColorRGBA(0.2F, 0.2F, 0.3F, 1.0F), new ColorRGBA(0.3F, 0.3F, 0.4F, 1.0F), new ColorRGBA(0.6F, 0.6F, 0.7F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.8F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.6F, 0.7F, 0.9F, 1.0F), new ColorRGBA(0.4F, 0.4F, 0.6F, 1.0F), new ColorRGBA(0.2F, 0.2F, 0.4F, 1.0F), new ColorRGBA(0.1F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F), new ColorRGBA(0.0F, 0.0F, 0.1F, 1.0F)}) {
            protected void update(ColorRGBA value) {
                groundColor.set(value);
            }
        });
        addTimeEffect(new ColorRangeEffect(new ColorRGBA[]{new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F), new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F), new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F), new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F), new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F), new ColorRGBA(0.0F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F), new ColorRGBA(0.0F, 0.1F, 0.2F, 1.0F), new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F), new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F), new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F), new ColorRGBA(0.0F, 0.01F, 0.02F, 1.0F)}) {
            protected void update(ColorRGBA value) {
                Environment.this.waterColor.set(value);
            }
        });
        addTimeEffect(new SkyEffect(sky, new Float[][]{{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)}, {Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)}, {Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)}, {Float.valueOf(0.8F), Float.valueOf(1.0F), Float.valueOf(1.0F)}, {Float.valueOf(0.7F), Float.valueOf(0.9F), Float.valueOf(1.5F)}, {Float.valueOf(0.6F), Float.valueOf(0.8F), Float.valueOf(2.25F)}, {Float.valueOf(0.5F), Float.valueOf(0.7F), Float.valueOf(2.25F)}, {Float.valueOf(0.5F), Float.valueOf(0.2F), Float.valueOf(2.5F)}, {Float.valueOf(0.3F), Float.valueOf(0.1F), Float.valueOf(2.0F)}, {Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(2.0F)}, {Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(2.0F)}, {Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.5F)}, {Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.5F)}, {Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.5F)}, {Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.5F)}, {Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.5F)}, {Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.5F)}, {Float.valueOf(0.1F), Float.valueOf(0.0F), Float.valueOf(2.0F)}, {Float.valueOf(0.3F), Float.valueOf(0.1F), Float.valueOf(2.25F)}, {Float.valueOf(0.7F), Float.valueOf(0.5F), Float.valueOf(2.5F)}, {Float.valueOf(0.9F), Float.valueOf(0.5F), Float.valueOf(2.0F)}, {Float.valueOf(0.9F), Float.valueOf(0.9F), Float.valueOf(2.0F)}, {Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.5F)}, {Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)}}));
    }

    public void setFogDistance(float f) {
        this.fogDistance = f;
    }

    public float getFogDistance() {
        return this.fogDistance;
    }

    public ColorRGBA getWaterColor() {
        return this.waterColor;
    }

    public Vector3f getLightPosition() {
        return this.lightPos;
    }

    public void addTimeEffect(TimeEffect e) {
        this.timeEffects.add(e);
    }

    public float getTime() {
        return (float) (getTotalTime() % 24.0D);
    }

    public double getTotalTime() {
        return this.gameClient.getGameTime() / 3600.0D;
    }

    public void update(float tpf) {
        double total = getTotalTime();
        double dayPart = total % 24.0D;

        for (TimeEffect e : this.timeEffects)
            e.timeUpdate((float) dayPart, total);
    }

    public class SkyEffect extends RangeEffect<Float[]> {
        private Sky sky;

        public SkyEffect(Sky sky, Float[][] values) {
            super(values);
            this.sky = sky;
        }

        protected void update(Float[] value) {
            this.sky.setEastWest(value[0].floatValue(), value[1].floatValue(), value[2].floatValue());
        }

        protected float interpolate(float val1, float val2, float ratio) {
            return val1 + (val2 - val1) * ratio;
        }

        protected Float[] interpolate(Float[] val1, Float[] val2, float ratio) {
            Float[] last = (Float[]) getLastValue();
            Float[] next = {Float.valueOf(interpolate(val1[0].floatValue(), val2[0].floatValue(), ratio)), Float.valueOf(interpolate(val1[1].floatValue(), val2[1].floatValue(), ratio)), Float.valueOf(interpolate(val1[2].floatValue(), val2[2].floatValue(), ratio))};

            if ((last != null) && (last[0].floatValue() == next[0].floatValue()) && (last[1].floatValue() == next[1].floatValue()) && (last[2].floatValue() == next[2].floatValue())) {
                return last;
            }
            return next;
        }
    }

    private class CloudArc
            implements TimeEffect {
        private CloudArc() {
        }

        public void timeUpdate(float hours, double totalTime) {
            double cloudDay = 11.0D;
            float dayPart = (float) (totalTime % cloudDay / cloudDay);
            float angle = 6.283186F * dayPart + 1.570796F;

            Vector3f cloudAxis = new Vector3f(0.5F, 0.0F, 0.5F).normalize();

            Vector3f cloudAxis2 = new Vector3f(0.4F, 0.0F, 0.5F).normalize();

            Quaternion cloudRot = new Quaternion().fromAngleAxis(angle, cloudAxis);
            Environment.this.clouds.setLocalRotation(cloudRot);
            Quaternion cloudRot2 = new Quaternion().fromAngleAxis(angle, cloudAxis2);
            Environment.this.clouds2.setLocalRotation(cloudRot2);
        }
    }

    private class MoonArc
            implements TimeEffect {
        private MoonArc() {
        }

        public void timeUpdate(float hours, double totalTime) {
            float angle = 6.283186F * ((hours - 13.0F) / 24.0F) + 1.570796F;

            float cos = FastMath.cos(angle);
            float sin = FastMath.sin(angle);

            Vector3f moonDir = new Vector3f(cos, sin, sin * -0.35F).normalize();

            if ((hours <= 4.0D) || (hours > 21.0D)) {
                Environment.this.sun.setDirection(moonDir);
            }

            float moonOffset = 0.0F;

            moonDir = new Vector3f(cos, sin, 0.0F).normalize();
            Vector3f pos = moonDir.mult(-20.0F).add(0.0F, moonOffset, 0.0F);

            if ((hours <= 4.0D) || (hours > 21.0D)) {
                Environment.this.lightPos.set(pos);
            }
            Quaternion starRot = new Quaternion().fromAngleAxis(angle, new Vector3f(0.0F, 0.0F, 1.0F));
            Environment.this.starGeometry.setLocalRotation(starRot);

            Environment.this.moonNode.setLocalTranslation(0.0F, 0.0F, 0.0F);
            Environment.this.moonGeom.setLocalTranslation(-20.0F, 0.0F, 0.0F);

            Environment.this.moonNode.setLocalTranslation(0.0F, -Math.abs(cos * cos) * 5.0F, 0.0F);

            Environment.this.moonGeom.setLocalScale(Math.max(0.65F, Math.min(1.0F, Math.abs(sin) * 1.5F)));

            double gameTime = Environment.this.gameClient.getGameTime();
            double oldTime = gameTime - 43200.0D;
            int gameDay = GameTime.toGameDay(oldTime);
            int dayOfMonth = GameTime.dayOfMonth(gameDay);

            int part = dayOfMonth;
            if (part > 14) {
                part = 28 - part;
            }

            float phaseAngle = part / 28.0F * 6.283186F;

            Quaternion phaseRot = new Quaternion().fromAngles(0.0F, -0.5890486F, 0.0F);

            Quaternion moonRot = new Quaternion().fromAngles(0.0F, 0.0F, angle);
            Environment.this.moonNode.setLocalRotation(moonRot.mult(phaseRot));

            Quaternion faceRot = new Quaternion().fromAngles(0.0F, 0.0F, -1.570796F);
            Quaternion faceRot2 = new Quaternion().fromAngles(0.0F, phaseAngle, 0.0F);

            Environment.this.moonGeom.setLocalRotation(faceRot2.mult(faceRot));
        }
    }

    private class SunArc
            implements TimeEffect {
        private SunArc() {
        }

        public void timeUpdate(float hours, double totalTime) {
            float angle = 6.283186F * ((hours - 1.0F) / 24.0F) + 1.570796F;

            float cos = FastMath.cos(angle);
            float sin = FastMath.sin(angle);

            Vector3f lightDir = new Vector3f(cos, sin, sin * 0.25F).normalize();

            if ((hours > 4.0D) && (hours <= 21.0D)) {
                Environment.this.sun.setDirection(lightDir);
            }

            float sunOffset = 100.0F;
            Vector3f pos = lightDir.mult(-500.0F).add(0.0F, sunOffset, 0.0F);
            Environment.this.lightSphere.setLocalTranslation(pos);
            Environment.this.flareNode.setLocalTranslation(pos);

            if ((hours > 4.0D) && (hours <= 21.0D))
                Environment.this.lightPos.set(pos);
        }
    }
}
