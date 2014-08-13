package com.pro.game.example.test.jme3test.model;

import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.LodControl;
import com.pro.game.example.test.jme3test.post.BloomUI;

/**
 * @author Nehon
 */
public class TestHoverTank extends SimpleApplication {

    public static void main(String[] args) {
        TestHoverTank app = new TestHoverTank();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Node tank = (Node) assetManager.loadModel("Models/HoverTank/Tank2.mesh.xml");

        flyCam.setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(cam, tank, inputManager);
        chaseCam.setSmoothMotion(true);
        chaseCam.setMaxDistance(100000);
        chaseCam.setMinVerticalRotation(-FastMath.PI / 2);
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        Geometry tankGeom = (Geometry) tank.getChild(0);
        LodControl control = new LodControl();
        tankGeom.addControl(control);
        rootNode.attachChild(tank);

        Vector3f lightDir = new Vector3f(-0.8719428f, -0.46824604f, 0.14304268f);
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new ColorRGBA(1.0f, 0.92f, 0.75f, 1f));
        dl.setDirection(lightDir);

        Vector3f lightDir2 = new Vector3f(0.70518064f, 0.5902297f, -0.39287305f);
        DirectionalLight dl2 = new DirectionalLight();
        dl2.setColor(new ColorRGBA(0.7f, 0.85f, 1.0f, 1f));
        dl2.setDirection(lightDir2);

        rootNode.addLight(dl);
        rootNode.addLight(dl2);
        rootNode.attachChild(tank);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.Objects);
        bf.setBloomIntensity(2.0f);
        bf.setExposurePower(1.3f);
        fpp.addFilter(bf);
        BloomUI bui = new BloomUI(inputManager, bf);
        viewPort.addProcessor(fpp);
    }
}
