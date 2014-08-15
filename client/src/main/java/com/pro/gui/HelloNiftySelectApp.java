package com.pro.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.pro.tile.TileSet;
import de.lessvoid.nifty.Nifty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HelloNiftySelectApp extends SimpleApplication {

    private Nifty nifty;

    public void simpleInitApp() {
        try {
            TileSet tileSet = TileSet.unmarshall(new FileInputStream(Paths.get("D:\\work\\testPro\\github\\client\\src\\main\\resources\\tiles\\examples\\desert.tsx").toFile()));

            System.out.println(tileSet);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(this.assetManager, this.inputManager, this.audioRenderer, this.guiViewPort);
        nifty = niftyDisplay.getNifty();
        HelloNiftySelectController controller = new HelloNiftySelectController();
        nifty.fromXml("assets/Interface/menu.xml", "menu", controller);
        guiViewPort.addProcessor(niftyDisplay);
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
    }

}