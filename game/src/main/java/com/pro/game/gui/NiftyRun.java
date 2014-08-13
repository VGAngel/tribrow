package com.pro.game.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;

public class NiftyRun extends SimpleApplication {

    public static void main(String[] args) {
        NiftyRun niftyRun = new NiftyRun();
        niftyRun.setPauseOnLostFocus(false);
        niftyRun.start();
    }

    @Override
    public void simpleInitApp() {
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();

        nifty.fromXml("nifty/first.xml", "start");

        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);

        // disable the fly cam
        flyCam.setEnabled(false);
    }
}
