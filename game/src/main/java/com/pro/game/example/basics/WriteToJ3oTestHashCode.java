package com.pro.game.example.basics;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;


public class WriteToJ3oTestHashCode extends SimpleApplication {

    public static void main(String[] args) {
        WriteToJ3oTestHashCode app = new WriteToJ3oTestHashCode();
        app.start();
    }


    BitmapText ch;


    @Override
    public void simpleInitApp() {


        //Search for geometries
        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                //System.out.println(spatial + " Visited Spatial");
                if (spatial instanceof Geometry) {

                    Geometry geo = (Geometry) spatial;

                    // Test for hash code of Material and Mesh
                    System.out.println(geo.getMesh().hashCode() + " Mesh");
                    System.out.println(geo.getMaterial().hashCode() + " Material");

                }
            }
        };

        System.out.println("===X1===");
        Node x1 = (Node) assetManager.loadModel("Models/writej3o.j3o");
        x1.depthFirstTraversal(sgv);
        System.out.println("======================");

        System.out.println("===X2===");
        Node x2 = (Node) assetManager.loadModel("Models/writej3o.j3o");
        x2.depthFirstTraversal(sgv);
        System.out.println("======================");

        System.out.println("===X3===");
        Node x3 = (Node) assetManager.loadModel("Models/writej3o.j3o");
        x3.depthFirstTraversal(sgv);
        System.out.println("======================");

        viewPort.setBackgroundColor(ColorRGBA.Gray);
        flyCam.setMoveSpeed(30);
    }


}
