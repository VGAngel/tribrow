/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zzueggTest.DeferredRendering;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import zzueggTest.DeferredRendering.Debug.DeferredRendererDebug;
import zzueggTest.DeferredRendering.RenderPasses.Abstract.ZPass;
import zzueggTest.DeferredRendering.RenderPasses.DepthPass;

/**
 * @author Michael
 */
public class DeferredRendering {

    SimpleApplication app;
    int height, width;
    Format zBufferFormat = Format.Depth32;
    Texture2D zBuffer;

    ZPass zBufferPass;
    FrameBuffer zBufferFB;
    ViewPort zBufferVP;

    public DeferredRendering(SimpleApplication app) {
        this.app = app;
    }

    public void startDeferedRendering() {
        this.initDeferredBuffers();
        this.initDeferredRenderers();
    }

    private void initDeferredBuffers() {
        height = this.app.getCamera().getHeight();
        width = this.app.getCamera().getWidth();

        this.zBuffer = new Texture2D(width, height, this.zBufferFormat);
    }

    private void initDeferredRenderers() {
        this.zBufferVP = this.app.getRenderManager().createPreView("ZPass", this.app.getViewPort().getCamera());
        this.zBufferVP.attachScene(this.app.getRootNode());
        this.zBufferFB = new FrameBuffer(width, height, 1);
        this.zBufferFB.setMultiTarget(true);
        this.zBufferFB.setDepthBuffer(zBufferFormat);
        this.zBufferFB.setDepthTexture(zBuffer);
        this.zBufferVP.setClearFlags(true, true, true);
        zBufferPass = new DepthPass(this.zBufferFB, "DeferredDepthPass");
        this.zBufferVP.addProcessor(zBufferPass);
    }

    DeferredRendererDebug dbg;

    public void setShowDebug(boolean debug) {
        if (debug) {
            dbg = new DeferredRendererDebug(this.app, new Texture2D[]{this.zBuffer});
            this.app.getGuiNode().attachChild(dbg);
        } else {
            this.app.getGuiNode().getChild("DRDebug").removeFromParent();
        }
    }
}
