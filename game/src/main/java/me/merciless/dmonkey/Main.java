package me.merciless.dmonkey;

import me.merciless.control.RotationControl;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

/**
 * @author kwando
 */
public class Main extends SimpleApplication {

    private Node dmonkeyRoot;

    public static void main(final String[] args) {
        final Main app = new Main();
        final AppSettings settings = new AppSettings(true);
        settings.setWidth(1280);
        settings.setHeight(720);
        // settings.setDepthBits(16);

        app.setSettings(settings);
        // app.setShowSettings(false);
        app.start();
    }

    private Spatial scene;

    @Override
    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(2);
        this.flyCam.setDragToRotate(true);

        this.cam.setFrustumPerspective(65, 16f / 9f, 0.1f, 200);

        final DMonkey dmonkey = new DMonkey();
        this.dmonkeyRoot = dmonkey.getRootNode();
        this.stateManager.attach(dmonkey);

        // create cube
        Spatial cube = this.assetManager.loadModel("assets/Models/brokenCube.j3o");
        TangentBinormalGenerator.generate(cube);
        final Material mat = this.assetManager.loadMaterial("assets/Materials/TestMaterial.j3m");
        cube.setMaterial(mat);
        cube.addControl(new RotationControl(new Vector3f(0, 0.1f, 0.04f)));
        this.dmonkeyRoot.attachChild(cube);

        // add more random cubes
        final Node cubes = new Node("RotatingCubes");
        final Vector3f mov = new Vector3f(1.2f, 1.2f, 1);
        for (int i = 0; i < 200; i++) {
            cube = cube.clone();
            cube.move(FastMath.rand.nextFloat() * mov.x, FastMath.rand.nextFloat() * mov.y, FastMath.rand.nextFloat() * mov.z);
            cube.rotate(FastMath.rand.nextFloat(), FastMath.rand.nextFloat(), FastMath.rand.nextFloat());
            cube.setLocalScale(FastMath.nextRandomFloat() * 0.5f + 0.5f);
            cubes.attachChild(cube);
        }

        this.scene = this.assetManager.loadModel("assets/Scenes/DMonkeyTest.j3o");
        ((Node) this.scene).getChild("terrain").setMaterial(this.assetManager.loadMaterial("assets/Materials/DTerrain.j3m"));
        final Texture tex = this.assetManager.loadTexture("assets/Materials/Road/road_COLOR1.png");
        tex.setWrap(Texture.WrapMode.Repeat);
        tex.setAnisotropicFilter(5);

        this.dmonkeyRoot.attachChild(cubes);
        this.dmonkeyRoot.attachChild(this.scene);

        // some post filtering
        final FilterPostProcessor fpp = new FilterPostProcessor(this.assetManager);
        final BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Scene);
        fpp.addFilter(bloom);
        bloom.setExposureCutOff(0.5f);

        this.viewPort.addProcessor(fpp);

        for (int i = 0; i < 2; i++) {
            final FXAAFilter fxaa = new FXAAFilter();
            fpp.addFilter(fxaa);
        }
    }

    @Override
    public void simpleUpdate(final float tpf) {
        final float time = this.timer.getTimeInSeconds() / 2;
        this.listener.setLocation(this.cam.getLocation());
        // this.cam.setLocation(new Vector3f(FastMath.sin(time) * 5,
        // FastMath.abs(FastMath.cos(time * 1.3f)) * 5, FastMath.cos(time) *
        // 10));
        // this.cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }
}
