package zzueggTest.DeferredRendering;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * @author Michael
 */
public class Main extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        DeferredRendering dr = new DeferredRendering(this);
        dr.startDeferedRendering();
        dr.setShowDebug(true);

        /** An unshaded textured cube. 
         *  Uses texture from jme3-test-data library! */
        Box boxshape1 = new Box(Vector3f.ZERO, 1f, 1f, 1f);
        Geometry cube_tex = new Geometry("A Textured Box", boxshape1);
        Material mat_tex = new Material(assetManager, "assets/Deferred/Default/Unshaded.j3md");
        //Texture tex = assetManager.loadTexture("Interface/Logo/Monkey.jpg");
        //mat_tex.setTexture("ColorMap", tex);
        cube_tex.setMaterial(mat_tex);
        rootNode.attachChild(cube_tex);
    }

    public static void main(String args[]) {
        Main main = new Main();
        main.start();
    }
}
