package com.pro.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.pro.tile.tilemap.TileMap;
import de.lessvoid.nifty.Nifty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class HelloNiftySelectApp extends SimpleApplication {

    private Nifty nifty;

    public void simpleInitApp() {
//        try {
//            TileSet tileSet = TileSet.unmarshall(new FileInputStream(Paths.get("D:\\work\\testPro\\github\\client\\src\\main\\resources\\tiles\\examples\\desert.tsx").toFile()));
//
//            System.out.println(tileSet);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

//        try {
//            TileMap tileMap = TileMap.unmarshall(new FileInputStream(Paths.get("E:\\IdeaProjects\\BasicGame\\client\\src\\main\\resources\\tiles\\examples\\desertn.tmx").toFile()));
//
//            System.out.println(tileMap);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        /** Create a wall with a simple texture from test_data */
        Box box = new Box(Vector3f.ZERO, 2.5f, 2.5f, 1.0f);
        Spatial wallSpatial = new Geometry("Box", box);
        Material materialBrick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialBrick.setTexture("ColorMap", assetManager.loadTexture("tiles/examples/tmw_desert_spacing.png"));
        wallSpatial.setMaterial(materialBrick);
        wallSpatial.setLocalTranslation(2.0f, -2.5f, 0.0f);
        rootNode.attachChild(wallSpatial);
//
//        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(this.assetManager, this.inputManager, this.audioRenderer, this.guiViewPort);
//        nifty = niftyDisplay.getNifty();
//        HelloNiftySelectController controller = new HelloNiftySelectController();
//        nifty.fromXml("assets/Interface/menu.xml", "menu", controller);
//        guiViewPort.addProcessor(niftyDisplay);
//        flyCam.setEnabled(false);
//        inputManager.setCursorVisible(true);
    }

}