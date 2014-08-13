package com.pro.game.example.test.jme3test.app;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class TestReleaseDirectMemory extends SimpleApplication {

    public static void main(String[] args) {
        TestReleaseDirectMemory app = new TestReleaseDirectMemory();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
    }

    @Override
    public void simpleUpdate(float tpf) {
        ByteBuffer buf = BufferUtils.createByteBuffer(500000);
        BufferUtils.destroyDirectBuffer(buf);

        FloatBuffer buf2 = BufferUtils.createFloatBuffer(500000);
        BufferUtils.destroyDirectBuffer(buf2);
    }

}
