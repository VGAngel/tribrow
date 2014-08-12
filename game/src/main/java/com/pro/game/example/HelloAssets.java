package com.pro.game.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

/**
 * Created with IntelliJ IDEA.
 * User: vgangel
 * Date: 8/14/13
 * Time: 4:09 PM
 */
public class HelloAssets extends SimpleApplication {

    public static void main(String[] args) {
        HelloAssets app = new HelloAssets();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial teapotSpatial = assetManager.loadModel("Models/Teapot/Teapot.obj");
        Material materialDefault = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        teapotSpatial.setMaterial(materialDefault);
        rootNode.attachChild(teapotSpatial);

        /** Create a wall with a simple texture from test_data */
        Box box = new Box(Vector3f.ZERO, 2.5f, 2.5f, 1.0f);
        Spatial wallSpatial = new Geometry("Box", box);
        Material materialBrick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialBrick.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
        wallSpatial.setMaterial(materialBrick);
        wallSpatial.setLocalTranslation(2.0f, -2.5f, 0.0f);
        rootNode.attachChild(wallSpatial);

        /** Display a line of text with a default font */
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText helloText = new BitmapText(guiFont, false);
        helloText.setSize(guiFont.getCharSet().getRenderedSize());
        helloText.setText("Hello World");
        helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);
        guiNode.attachChild(helloText);

        /** Load a model from test_data (OrgeXML + material + texture)*/
        Spatial ninjaSpatial = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        ninjaSpatial.scale(0.05f, 0.05f, 0.05f);
        ninjaSpatial.rotate(0.0f, -3.0f, 0.0f);
        ninjaSpatial.setLocalTranslation(0.0f, -5.0f, -2.0f);
        rootNode.attachChild(ninjaSpatial);
        //You must add a light to make the model visible
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);

//        assetManager.registerLocator("town.zip", ZipLocator.class);
//        Spatial scene = assetManager.loadModel("main.scene");
//        rootNode.attachChild(scene);

        assetManager.registerLocator("http://jmonkeyengine.googlecode.com/files/wildhouse.zip", HttpZipLocator.class);
        Spatial scene = assetManager.loadModel("main.scene");
        rootNode.attachChild(scene);
    }

}
