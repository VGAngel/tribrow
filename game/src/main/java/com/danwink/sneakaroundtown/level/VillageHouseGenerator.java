package com.danwink.sneakaroundtown.level;

import com.danwink.sneakaroundtown.SneakAroundTown;
import com.danwink.sneakaroundtown.level.Chunk.ChunkType;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class VillageHouseGenerator implements ChunkGenerator {

    public void generateNodes(Level l, int x, int y, SneakAroundTown app) {
        //Floor
        {
            Box b = new Box(l.chunkSize / 2, .5f, l.chunkSize / 2);
            Geometry a = new Geometry("q", b);
            Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat1.setColor("Color", new ColorRGBA(1.f, .6f, .3f, 1.f));
            a.setMaterial(mat1);
            a.setShadowMode(ShadowMode.CastAndReceive);
            app.getRootNode().attachChild(a);

            RigidBodyControl phy = new RigidBodyControl(0.0f);

            a.addControl(phy);
            app.bulletAppState.getPhysicsSpace().add(phy);
            phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + l.chunkSize * .5f, 0, y * l.chunkSize + l.chunkSize * .5f));
        }

        if (l.getChunk(x - 1, y).type != ChunkType.VILLAGEHOUSE) {
            Box b = new Box(.5f, 5, l.chunkSize / 2);
            Geometry a = new Geometry("q", b);
            Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat1.setColor("Color", new ColorRGBA(1.f, .6f, .3f, 1.f));
            a.setMaterial(mat1);
            a.setShadowMode(ShadowMode.CastAndReceive);
            app.getRootNode().attachChild(a);

            RigidBodyControl phy = new RigidBodyControl(0.0f);

            a.addControl(phy);
            app.bulletAppState.getPhysicsSpace().add(phy);
            phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + .5f, 5.5f, y * l.chunkSize + l.chunkSize * .5f));
        }

        if (l.getChunk(x + 1, y).type != ChunkType.VILLAGEHOUSE) {
            Box b = new Box(.5f, 5, l.chunkSize / 2);
            Geometry a = new Geometry("q", b);
            Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat1.setColor("Color", new ColorRGBA(1.f, .6f, .3f, 1.f));
            a.setMaterial(mat1);
            a.setShadowMode(ShadowMode.CastAndReceive);
            app.getRootNode().attachChild(a);

            RigidBodyControl phy = new RigidBodyControl(0.0f);

            a.addControl(phy);
            app.bulletAppState.getPhysicsSpace().add(phy);
            phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + l.chunkSize - .5f, 5.5f, y * l.chunkSize + l.chunkSize * .5f));
        }

        if (l.getChunk(x, y - 1).type != ChunkType.VILLAGEHOUSE) {
            Box b = new Box(l.chunkSize / 2, 5, .5f);
            Geometry a = new Geometry("q", b);
            Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat1.setColor("Color", new ColorRGBA(1.f, .6f, .3f, 1.f));
            a.setMaterial(mat1);
            a.setShadowMode(ShadowMode.CastAndReceive);
            app.getRootNode().attachChild(a);

            RigidBodyControl phy = new RigidBodyControl(0.0f);

            a.addControl(phy);
            app.bulletAppState.getPhysicsSpace().add(phy);
            phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + l.chunkSize * .5f, 5.5f, y * l.chunkSize + .5f));
        }

        if (l.getChunk(x, y + 1).type != ChunkType.VILLAGEHOUSE) {
            if (l.getChunk(x, y).attributes.contains("door")) {
                {
                    Box b = new Box((l.chunkSize / 5), 5, .5f);
                    Geometry a = new Geometry("q", b);
                    Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    mat1.setColor("Color", new ColorRGBA(1.f, .6f, .3f, 1.f));
                    a.setMaterial(mat1);
                    a.setShadowMode(ShadowMode.CastAndReceive);
                    app.getRootNode().attachChild(a);

                    RigidBodyControl phy = new RigidBodyControl(0.0f);

                    a.addControl(phy);
                    app.bulletAppState.getPhysicsSpace().add(phy);
                    phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + (l.chunkSize / 5), 5.5f, y * l.chunkSize + l.chunkSize - .5f));
                }
                {
                    Box b = new Box((l.chunkSize / 5), 5, .5f);
                    Geometry a = new Geometry("q", b);
                    Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    mat1.setColor("Color", new ColorRGBA(1.f, .6f, .3f, 1.f));
                    a.setMaterial(mat1);
                    a.setShadowMode(ShadowMode.CastAndReceive);
                    app.getRootNode().attachChild(a);

                    RigidBodyControl phy = new RigidBodyControl(0.0f);

                    a.addControl(phy);
                    app.bulletAppState.getPhysicsSpace().add(phy);
                    phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + l.chunkSize - (l.chunkSize / 5), 5.5f, y * l.chunkSize + l.chunkSize - .5f));
                }
                {
                    Box b = new Box((l.chunkSize / 10), 2.5f, .5f);
                    Geometry a = new Geometry("q", b);
                    Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    mat1.setColor("Color", new ColorRGBA(1.f, .6f, .3f, 1.f));
                    a.setMaterial(mat1);
                    a.setShadowMode(ShadowMode.CastAndReceive);
                    app.getRootNode().attachChild(a);

                    RigidBodyControl phy = new RigidBodyControl(0.0f);

                    a.addControl(phy);
                    app.bulletAppState.getPhysicsSpace().add(phy);
                    phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + (l.chunkSize / 2), 8f, y * l.chunkSize + l.chunkSize - .5f));
                }
            } else {
                Box b = new Box(l.chunkSize / 2, 5, .5f);
                Geometry a = new Geometry("q", b);
                Material mat1 = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                mat1.setColor("Color", new ColorRGBA(1.f, .6f, .3f, 1.f));
                a.setMaterial(mat1);
                a.setShadowMode(ShadowMode.CastAndReceive);
                app.getRootNode().attachChild(a);

                RigidBodyControl phy = new RigidBodyControl(0.0f);

                a.addControl(phy);
                app.bulletAppState.getPhysicsSpace().add(phy);
                phy.setPhysicsLocation(new Vector3f(x * l.chunkSize + l.chunkSize * .5f, 5.5f, y * l.chunkSize + l.chunkSize - .5f));
            }
        }
    }
}
