/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.merciless.dmonkey;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

/**
 * @author kwando
 */
public class DebugState extends AbstractAppState {

    private Node guiNode;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        guiNode = new Node("debugInformation");

        guiNode = ((Node) app.getViewPort().getScenes().get(0));
        DMonkey dmonkey = stateManager.getState(DMonkey.class);
        Texture2D[] textures = {
                dmonkey.diffuse, dmonkey.normals, dmonkey.Zbuffer, dmonkey.lights};
        int vpHeight = app.getGuiViewPort().getCamera().getHeight();
        int vpWidth = app.getGuiViewPort().getCamera().getWidth();
        float scale = 4f;
        for (int i = 0; i < textures.length; i++) {
            Material material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            material.setTexture("ColorMap", textures[i]);
            Picture pic = new Picture("Normals");
            pic.setWidth(vpWidth / scale);
            pic.setHeight(vpHeight / scale);
            pic.move(vpWidth - (vpWidth / scale), vpHeight - (vpHeight / scale) * (i + 1), 0);
            pic.setMaterial(material);
            guiNode.attachChild(pic);
        }
    }
}
