package com.pro.game.example.test.jme3test.app;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.system.AppSettings;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class TestChangeAppIcon extends SimpleApplication {

    private static final Logger log = Logger.getLogger(TestChangeAppIcon.class.getName());

    public static void main(String[] args) {
        TestChangeAppIcon app = new TestChangeAppIcon();
        AppSettings settings = new AppSettings(true);

        try {
            Class<TestChangeAppIcon> clazz = TestChangeAppIcon.class;

            settings.setIcons(new BufferedImage[]{
                    ImageIO.read(clazz.getResourceAsStream("/Interface/icons/SmartMonkey256.png")),
                    ImageIO.read(clazz.getResourceAsStream("/Interface/icons/SmartMonkey128.png")),
                    ImageIO.read(clazz.getResourceAsStream("/Interface/icons/SmartMonkey32.png")),
                    ImageIO.read(clazz.getResourceAsStream("/Interface/icons/SmartMonkey16.png")),
            });
        } catch (IOException e) {
            log.log(java.util.logging.Level.WARNING, "Unable to load program icons", e);
        }
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Write text on the screen (HUD)
        setDisplayStatView(false);
        BitmapText helloText = new BitmapText(guiFont);
        helloText.setText("The icon of the app should be a smart monkey!");
        helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);
        guiNode.attachChild(helloText);
    }
}
