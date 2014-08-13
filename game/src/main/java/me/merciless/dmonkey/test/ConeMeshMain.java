package me.merciless.dmonkey.test;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.util.TangentBinormalGenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.merciless.dmonkey.ConeMesh;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author kwando
 */
public class ConeMeshMain extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(5);
        Node parent = new Node();
        rootNode.attachChild(parent);
        Geometry geom = new Geometry("Cone", ConeMesh.volumeFromRangeAndCutoff(5, 20));
        geom.setMaterial(assetManager.loadMaterial("assets/Materials/VertexColor.j3m"));
        parent.lookAt(Vector3f.UNIT_XYZ, Vector3f.UNIT_Y);

        parent.attachChild(geom);


        //geom.addControl(new RotationControl(new Vector3f(1, 2, 0.7f)));
        ///*
        Mesh lines = TangentBinormalGenerator.genTbnLines(geom.getMesh(), 0.1f);
        lines.updateBound();
        geom = new Geometry("Normallines", lines);
        geom.setMaterial(assetManager.loadMaterial("assets/Materials/VertexColor.j3m"));
        parent.attachChild(geom);//*/
    }

    public static void main(String[] arg) {
        Logger.getLogger("").setLevel(Level.SEVERE);
        SimpleApplication app = new ConeMeshMain();
        AppSettings settings = new AppSettings(true);
        settings.setSamples(8);
        settings.setFrameRate(120);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }
}
