package com.pro.game.example;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * Created with IntelliJ IDEA.
 * User: vgangel
 * Date: 8/14/13
 * Time: 5:21 PM
 */
public class HelloInput extends SimpleApplication {

    public static void main(String[] args) {
        HelloInput app = new HelloInput();
        app.start();
    }

    protected Geometry player;
    Boolean isRunning = true;

    @Override
    public void simpleInitApp() {
        Box box = new Box(Vector3f.ZERO, 1, 1, 1);
        player = new Geometry("Player", box);
        Material material01 = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        player.setMaterial(material01);
        rootNode.attachChild(player);
        initKeys(); // load my custom keybinding
    }

    /**
     * Custom Keybinding: Map named actions to inputs.
     */
    private void initKeys() {
        // You can map one or several inputs to one named action
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Rotate", new KeyTrigger(KeyInput.KEY_SPACE),
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // Add the names to the action listener.
        inputManager.addListener(actionListener, "Pause");
        inputManager.addListener(analogListener, "Left", "Right", "Rotate");
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Pause") && !isPressed) {
                isRunning = !isRunning;
            }
        }
    };

    private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (isRunning) {
                if (name.equals("Rotate")) {
                    player.rotate(0, value * speed, 0);
                }
                if (name.equals("Right")) {
                    Vector3f v = player.getLocalTranslation();
                    player.setLocalTranslation(v.x + value * speed, v.y, v.z);
                }
                if (name.equals("Left")) {
                    Vector3f v = player.getLocalTranslation();
                    player.setLocalTranslation(v.x - value * speed, v.y, v.z);
                }
            } else {
                System.out.println("Press P to unpause.");
            }
        }
    };

}
