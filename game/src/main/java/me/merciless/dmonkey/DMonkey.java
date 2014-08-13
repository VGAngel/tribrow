package me.merciless.dmonkey;

import me.merciless.dmonkey.lights.DAmbientLight;
import me.merciless.dmonkey.lights.DPointLight;
import me.merciless.dmonkey.lights.LightLocationSynchControll;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture2D;

/**
 * @author kwando
 */
public class DMonkey extends AbstractAppState {
    private static DMonkey instance;

    private ViewPort view;
    private RenderManager renderManager;
    private final Node rootNode;
    private final Node lightNode;
    Texture2D normals;
    Texture2D diffuse;
    Texture2D lights;
    Texture2D Zbuffer;
    private FrameBuffer gbuffer;
    private FrameBuffer lightBuffer;
    private AssetManager assets;
    private Application app;
    private Camera cam;

    public static DMonkey getInstance() {
        return DMonkey.instance;
    }

    public DMonkey() {
        DMonkey.instance = this;
        this.rootNode = new Node();
        this.lightNode = new Node("LightBounds");
    }

    public Node getRootNode() {
        return this.rootNode;
    }

    @Override
    public void initialize(final AppStateManager stateManager, final Application app) {
        this.app = app;
        this.cam = app.getCamera();
        this.renderManager = app.getRenderManager();
        this.view = this.renderManager.createPreView("GBufferPass", this.cam);
        this.view.attachScene(this.rootNode);

        this.rootNode.updateGeometricState();
        this.lightNode.updateGeometricState();

        this.assets = app.getAssetManager();
        this.setupGBuffer();

        stateManager.attach(new DebugState());

        final Geometry geom = new Geometry("ResolveQuad", new Quad(1, 1));
        final Material resolveMat = new Material(this.assets, "assets/DMonkey/Resolve.j3md");
        geom.setMaterial(resolveMat);
        geom.setCullHint(Spatial.CullHint.Never);
        resolveMat.getAdditionalRenderState().setDepthTest(false);
        resolveMat.getAdditionalRenderState().setDepthWrite(false);
        resolveMat.setTexture("NormalBuffer", this.normals);
        resolveMat.setTexture("DepthBuffer", this.Zbuffer);
        resolveMat.setTexture("DiffuseBuffer", this.diffuse);
        resolveMat.setTexture("LightBuffer", this.lights);
        ((Node) app.getViewPort().getScenes().get(0)).attachChild(geom);

        this.addSkybox(this.lightNode);
    }

    public Camera getCamera() {
        return this.cam;
    }

    @Override
    public void stateAttached(final AppStateManager stateManager) {
    }

    @Override
    public void stateDetached(final AppStateManager stateManager) {
    }

    private void setupGBuffer() {
        System.out.println("setup GBUFFER");
        final int w = this.cam.getWidth();
        final int h = this.cam.getHeight();

        this.gbuffer = new FrameBuffer(w, h, 1);

        final Format format = Format.RGBA8;

        this.normals = new Texture2D(w, h, format);
        this.diffuse = new Texture2D(w, h, format);

        this.normals.setMagFilter(MagFilter.Nearest);
        this.diffuse.setMagFilter(MagFilter.Bilinear);

        this.normals.setMinFilter(MinFilter.NearestNoMipMaps);
        this.diffuse.setMagFilter(MagFilter.Nearest);

        this.normals.setMinFilter(MinFilter.NearestNoMipMaps);

        this.Zbuffer = new Texture2D(w, h, Format.Depth);
        this.gbuffer.setDepthTexture(this.Zbuffer);
        this.gbuffer.addColorTexture(this.normals);
        this.gbuffer.addColorTexture(this.diffuse);
        this.gbuffer.setMultiTarget(true);

        this.view.setOutputFrameBuffer(this.gbuffer);
        this.view.setClearFlags(true, true, true);

        this.setupLightBuffer(w, h);
    }

    private void setupLightBuffer(final int w, final int h) {
        this.lightBuffer = new FrameBuffer(w, h, 1);
        this.lights = new Texture2D(w, h, Format.RGB111110F);
        this.lightBuffer.setColorTexture(this.lights);
        this.lightBuffer.setDepthTexture(this.Zbuffer);

        final ViewPort lightViewPort = this.renderManager.createPreView("LightBuffer", this.cam);
        lightViewPort.attachScene(this.lightNode);
        lightViewPort.setOutputFrameBuffer(this.lightBuffer);
        lightViewPort.setClearColor(true);

        lightViewPort.addProcessor(new DAmbientLight());

        new DPointLight();

        for (int i = 0; i < 10; i++) {
            this.addSpotLight(Vector3f.ZERO.add(2 * i - 5, 4, FastMath.nextRandomFloat() * 10 - 5), 12);
        }
    }

    /*
     * Adds a spotlight to the scene...
     */
    private void addSpotLight(final Vector3f spotLightPos, final float angle) {
        final float multiplier = 2f;
        final ColorRGBA color = ColorRGBA.Yellow.add(ColorRGBA.White.mult(multiplier)).multLocal(1 / (1 + multiplier));
        final Geometry lightVolume = new Geometry("Spotlight", ConeMesh.fromRangeAndCutoff(5, angle));
        final Material mSpot = new Material(this.assets, "assets/DMonkey/SpotLight.j3md");
        lightVolume.setMaterial(mSpot);
        lightVolume.setLocalTranslation(spotLightPos);
        mSpot.setTexture("DiffuseBuffer", this.diffuse);
        mSpot.setTexture("DepthBuffer", this.Zbuffer);
        mSpot.setTexture("NormalBuffer", this.normals);
        mSpot.setColor("LightColor", color);
        mSpot.setFloat("CutoffAngle", angle * FastMath.DEG_TO_RAD);
        mSpot.setFloat("LightRange", 5);
        mSpot.setVector3("LightDirection", Vector3f.UNIT_Y.mult(-1));
        mSpot.setVector3("LightPosition", spotLightPos);
        mSpot.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Additive);
        mSpot.getAdditionalRenderState().setDepthTest(false);
        mSpot.getAdditionalRenderState().setDepthWrite(false);
        mSpot.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        lightVolume.addControl(new LightLocationSynchControll(mSpot));
        this.lightNode.attachChild(lightVolume);

        final Geometry volume = new Geometry("VolumetricLight", ConeMesh.volumeFromRangeAndCutoff(5, angle));
        final Material vm = new Material(this.assets, "assets/DMonkey/LightCone.j3md");
        volume.setMaterial(vm);
        volume.setLocalTranslation(spotLightPos);
        vm.setTexture("DiffuseBuffer", this.diffuse);
        vm.setTexture("DepthBuffer", this.Zbuffer);
        vm.setTexture("NormalBuffer", this.normals);
        vm.setColor("LightColor", color);
        vm.setFloat("CutoffAngle", angle * FastMath.DEG_TO_RAD);
        vm.setFloat("LightRange", 5);
        vm.setVector3("LightDirection", Vector3f.UNIT_Y.mult(-1));
        vm.setVector3("LightPosition", spotLightPos);
        vm.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Additive);
        vm.getAdditionalRenderState().setDepthTest(true);
        vm.getAdditionalRenderState().setDepthWrite(false);
        vm.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        volume.addControl(new LightLocationSynchControll(vm));
        volume.setQueueBucket(RenderQueue.Bucket.Translucent);

        this.lightNode.attachChild(volume);
    }

    @Override
    public void update(final float tpf) {
        super.update(tpf);
        this.rootNode.updateLogicalState(tpf);
        this.rootNode.updateGeometricState();
        this.lightNode.updateLogicalState(tpf);
        this.lightNode.updateGeometricState();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.renderManager.removePreView(this.view);
    }

    /*
     * This code adds a skybox and a cloud layer....
     */
    private void addSkybox(final Node lightNode) {
        final Box box = new Box(Vector3f.ZERO, 5, 5, 5);
        final Geometry sky = new Geometry("MySkybox", box);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);

        sky.setMaterial(this.assets.loadMaterial("assets/Materials/NightSky.j3m"));
        sky.addControl(new AbstractControl() {

            @Override
            protected void controlUpdate(final float tpf) {
                this.spatial.setLocalTranslation(DMonkey.this.cam.getLocation());
            }

            @Override
            protected void controlRender(final RenderManager rm, final ViewPort vp) {
            }

            @Override
            public Control cloneForSpatial(final Spatial spatial) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        final int size = 100;
        final Quad quad = new Quad(size, size);
        final Geometry clouds = new Geometry("clouds", quad);
        clouds.setMaterial(this.assets.loadMaterial("assets/Materials/Cloud.j3m"));
        clouds.rotate(FastMath.HALF_PI, 0, 0);
        clouds.move(0, 10, 0);
        clouds.addControl(new AbstractControl() {

            @Override
            protected void controlUpdate(final float tpf) {
                this.spatial.setLocalTranslation(DMonkey.this.cam.getLocation().add(new Vector3f(-size / 2, 4, -size / 2)));
            }

            @Override
            protected void controlRender(final RenderManager rm, final ViewPort vp) {
            }

            @Override
            public Control cloneForSpatial(final Spatial spatial) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        lightNode.attachChild(clouds);
    }

    public AssetManager getAssetManager() {
        return this.assets;
    }

    public Texture getDiffuseBuffer() {
        return this.diffuse;
    }

    public Texture getZBuffer() {
        return this.Zbuffer;
    }

    public Texture getNormalBuffer() {
        return this.normals;
    }

    public Node getLightNode() {
        return this.lightNode;
    }
}
