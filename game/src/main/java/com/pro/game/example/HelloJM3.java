package com.pro.game.example;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * Created with IntelliJ IDEA.
 * User: vgangel
 * Date: 8/14/13
 * Time: 3:40 PM
 */
public class HelloJM3 extends SimpleApplication {

    public static void main(String[] args) {
        HelloJM3 app = new HelloJM3();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box box = new Box(1, 1, 1);
        Geometry geometry = new Geometry("Box", box);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Blue);
        geometry.setMaterial(material);
        rootNode.attachChild(geometry);
    }

}
