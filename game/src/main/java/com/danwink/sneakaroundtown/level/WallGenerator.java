package com.danwink.sneakaroundtown.level;

import com.danwink.sneakaroundtown.SneakAroundTown;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class WallGenerator implements ChunkGenerator {

    public void generateNodes(Level l, int x, int y, SneakAroundTown app) {
        Box b = new Box(l.chunkSize / 2, 10, l.chunkSize / 2);
        Geometry a = new Geometry("q", b);
        Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Gray);
        a.setMaterial(mat1);
        a.setShadowMode(ShadowMode.CastAndReceive);
        app.getRootNode().attachChild(a);

        RigidBodyControl phy = new RigidBodyControl(0.0f);

        a.addControl(phy);
        app.bulletAppState.getPhysicsSpace().add(phy);
        phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + l.chunkSize * .5f, 10, y * l.chunkSize + l.chunkSize * .5f));
    }
}
