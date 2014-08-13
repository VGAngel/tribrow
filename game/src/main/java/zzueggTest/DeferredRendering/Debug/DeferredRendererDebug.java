/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zzueggTest.DeferredRendering.Debug;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;

/**
 * @author Michael
 */
public class DeferredRendererDebug extends Node {

    int height = 180;
    int width = 320;

    SimpleApplication app;
    Texture2D[] texturesToDebug;

    public DeferredRendererDebug(SimpleApplication app, Texture2D[] texturesToDebug) {
        super("DRDebug");
        this.texturesToDebug = texturesToDebug;
        this.app = app;
        this.generateGeometry();
    }

    private void generateGeometry() {
        this.detachAllChildren();
        Quad quad = new Quad(width, height);
        Material material = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        for (int i = 0; i < this.texturesToDebug.length; i++) {
            Geometry g = new Geometry("DebugView:" + i, quad.clone());
            Material mat = material.clone();
            mat.setTexture("ColorMap", this.texturesToDebug[i]);
            g.setMaterial(mat);
            g.setLocalTranslation(this.app.getCamera().getWidth() - width, this.app.getCamera().getHeight() - (height * (i + 1)), -10);
            this.attachChild(g);
        }
    }


}
