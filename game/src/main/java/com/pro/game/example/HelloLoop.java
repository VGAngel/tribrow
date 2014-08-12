package com.pro.game.example;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * Created with IntelliJ IDEA.
 * User: vgangel
 * Date: 8/14/13
 * Time: 5:12 PM
 */
public class HelloLoop extends SimpleApplication {

    public static void main(String[] args) {
        HelloLoop app = new HelloLoop();
        app.start();
    }

    protected Geometry player;

    @Override
    public void simpleInitApp() {
        /** this blue box is our player character */
        Box box = new Box(1, 1, 1);
        player = new Geometry("blue cube", box);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        player.setMaterial(material);
        rootNode.attachChild(player);
    }

    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        player.rotate(0, 1.5f * tpf, 0);
    }
}
