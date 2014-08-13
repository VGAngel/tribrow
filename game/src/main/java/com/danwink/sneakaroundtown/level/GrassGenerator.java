package com.danwink.sneakaroundtown.level;

import com.danwink.sneakaroundtown.SneakAroundTown;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

public class GrassGenerator implements ChunkGenerator {

    public void generateNodes(Level l, int x, int y, SneakAroundTown app) {
        Quad b = new Quad(l.chunkSize, l.chunkSize);
        Geometry a = new Geometry("q", b);
        Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Green);
        a.setMaterial(mat1);
        a.setShadowMode(ShadowMode.CastAndReceive);
        app.getRootNode().attachChild(a);

        RigidBodyControl phy = new RigidBodyControl(0.0f);

        a.addControl(phy);
        app.bulletAppState.getPhysicsSpace().add(phy);
        phy.setPhysicsLocation(new Vector3f(x * l.chunkSize, 0, y * l.chunkSize + l.chunkSize));

        Quaternion q = new Quaternion();
        q.fromAngleAxis(-FastMath.PI * .5f, new Vector3f(1, 0, 0));
        phy.setPhysicsRotation(q);

        phy.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        phy.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
    }
}
