package com.danwink.sneakaroundtown.states;

import com.danwink.sneakaroundtown.Player;
import com.danwink.sneakaroundtown.SneakAroundTown;
import com.danwink.sneakaroundtown.level.Level;
import com.danwink.sneakaroundtown.level.LevelGeneratorV1;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
//import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
//import com.jme3.shadow.DirectionalLightShadowFilter;
//import com.jme3.shadow.DirectionalLightShadowRenderer;
//import com.jme3.shadow.EdgeFilteringMode;

public class PlayingState extends AbstractAppState {
    private boolean left = false, right = false, up = false, down = false;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private float airTime = 0;

    private SneakAroundTown app;
    private AppStateManager sm;

    Level l;

    Player p;

    public void initialize(AppStateManager sm, Application app) {
        this.sm = sm;
        this.app = (SneakAroundTown) app;

        l = LevelGeneratorV1.generateLevel();

        l.generateNodes(this.app);

        app.getInputManager().addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addMapping("back", new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addMapping("left", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("right", new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        //app.getInputManager().addListener( analogListener, new String[] {  } );
        app.getInputManager().addListener(actionListener, "jump", "forward", "back", "left", "right");

        app.getCamera().setFrustumPerspective(45f, (float) app.getCamera().getWidth() / app.getCamera().getHeight(), 0.01f, 1000f);

        p = new Player();

        Box b = new Box(.1f, .1f, .1f);
        Geometry a = p.g = new Geometry("q", b);
        Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Gray);
        a.setMaterial(mat1);
        a.setShadowMode(ShadowMode.CastAndReceive);
        this.app.getRootNode().attachChild(a);

        a.move(3, 3, 3);

//        BetterCharacterControl bcc = p.bcc = new BetterCharacterControl(1, 3, 0.5f);
//        a.addControl(bcc);

        //this.app.bulletAppState.getPhysicsSpace().add(bcc);


        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(1, -1, -.4f).normalize());
        this.app.getRootNode().addLight(sun);
 
//        /* Drop shadows */
//        final int SHADOWMAP_SIZE = 1024;
//        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(app.getAssetManager(), SHADOWMAP_SIZE, 3);
//        dlsr.setLight(sun);
//        dlsr.setLambda(0.55f);
//        dlsr.setShadowIntensity(0.6f);
//        dlsr.setEdgeFilteringMode(EdgeFilteringMode.Bilinear);
//        app.getViewPort().addProcessor(dlsr);
//
//        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(app.getAssetManager(), SHADOWMAP_SIZE, 3);
//        dlsf.setLight(sun);
//        dlsf.setEnabled(true);
//        dlsf.setLambda(0.55f);
//        dlsf.setShadowIntensity(0.6f);
//        dlsf.setEdgeFilteringMode(EdgeFilteringMode.Bilinear);
//        FilterPostProcessor fpp = new FilterPostProcessor(app.getAssetManager());
//        fpp.addFilter(dlsf);

        //SSAOFilter ssaoFilter = new SSAOFilter( 5.1f, 1.2f, 0.2f, 0.1f );
        //fpp.addFilter(ssaoFilter);

       // app.getViewPort().addProcessor(fpp);
    }

    public void cleanup() {
        super.cleanup();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    public void update(float d) {
        Vector3f camDir = app.getCamera().getDirection().clone().multLocal(0.25f);
        Vector3f camLeft = app.getCamera().getLeft().clone().multLocal(0.25f);
        camDir.y = 0;
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);

        if (left) walkDirection.addLocal(camLeft);
        if (right) walkDirection.addLocal(camLeft.negate());
        if (up) walkDirection.addLocal(camDir);
        if (down) walkDirection.addLocal(camDir.negate());

//        if (!p.bcc.isOnGround()) {
//            airTime = airTime + d;
//        } else {
//            airTime = 0;
//        }

        walkDirection.multLocal(d * 1000);

        //p.bcc.setWalkDirection(walkDirection);
        app.getCamera().setLocation(p.g.getWorldTranslation().add(0, 2, 0));
    }

    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("forward")) {
                up = keyPressed;
            } else if (name.equals("back")) {
                down = keyPressed;
            } else if (name.equals("right")) {
                right = keyPressed;
            } else if (name.equals("left")) {
                left = keyPressed;
            } else if (name.equals("jump") && keyPressed) {
                //p.bcc.jump();
            }
        }
    };

    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float d) {

        }
    };
}
