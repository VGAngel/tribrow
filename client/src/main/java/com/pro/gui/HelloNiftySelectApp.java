package com.pro.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import de.lessvoid.nifty.Nifty;

import java.util.HashMap;
import java.util.Map;

public class HelloNiftySelectApp extends SimpleApplication {

    private Nifty nifty;

    public void simpleInitApp() {

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(this.assetManager, this.inputManager, this.audioRenderer, this.guiViewPort);
        nifty = niftyDisplay.getNifty();
        HelloNiftySelectController controller = new HelloNiftySelectController();
        nifty.fromXml("assets/Interface/hello-nifty-select-gui.xml", "menu", controller);
        guiViewPort.addProcessor(niftyDisplay);
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
    }

}