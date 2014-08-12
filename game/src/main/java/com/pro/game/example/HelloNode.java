package com.pro.game.example;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * Created with IntelliJ IDEA.
 * User: vgangel
 * Date: 8/14/13
 * Time: 3:49 PM
 */
public class HelloNode extends SimpleApplication {

    public static void main(String[] args) {
        HelloNode app = new HelloNode();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        /** create a blue box at coordinates (1, -1, 1) */
        Box box01 = new Box(1, 1, 1);
        Geometry blueGeometry = new Geometry("Box", box01);
        blueGeometry.setLocalTranslation(new Vector3f(1, -1, 1));
        Material material01 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material01.setColor("Color", ColorRGBA.Blue);
        blueGeometry.setMaterial(material01);

        /** create a red box straight above the blue one at (1, 3, 1) */
        Box box02 = new Box(1, 1, 1);
        Geometry redGeometry = new Geometry("Box", box02);
        redGeometry.setLocalTranslation(new Vector3f(1, 3, 1));
        Material material02 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material02.setColor("Color", ColorRGBA.Red);
        redGeometry.setMaterial(material02);

        /** Create a pivot node at (0, 0, 0) and attach it to the root node */
        Node pivotNode = new Node("pivot");
        rootNode.attachChild(pivotNode);

        /** Attach the two boxes to the *pivot* node. (And transitively to the root node.) */
        pivotNode.attachChild(blueGeometry);
        pivotNode.attachChild(redGeometry);

        /** Rotate the pivot node: Note that both boxes have rotated! */
        pivotNode.rotate(.4f, .4f, 0f);

        /** Create a Mesh shape, wrap it into a Geometry, and give it a Material. */
        Box meshBox = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry thingGeometry = new Geometry("thing", meshBox);
        Material material03 = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        thingGeometry.setMaterial(material03);

        rootNode.attachChild(thingGeometry);
    }

}
