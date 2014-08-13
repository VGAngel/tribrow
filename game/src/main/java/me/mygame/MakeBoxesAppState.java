/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * @author normenhansen
 */
public class MakeBoxesAppState extends AbstractAppState {

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        AssetManager assetManager = app.getAssetManager();
        SimpleApplication application = (SimpleApplication) app;
        Node rootNode = application.getRootNode();
        rootNode.addLight(new DirectionalLight());
        //Note this is *not* how Minecraft is made,
        //a box world is not made out of boxes, google "voxels"!
        for (int i = 0; i < 10; i++) {
            Geometry geom = new Geometry("box", new Box(0.25f, 0.25f, 0.25f));
            geom.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
            geom.setLocalTranslation(0, i, 0);
            rootNode.attachChild(geom);
        }
    }

    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
    }

    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
    }
}
