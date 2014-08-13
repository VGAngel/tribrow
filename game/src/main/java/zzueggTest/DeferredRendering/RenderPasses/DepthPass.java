/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zzueggTest.DeferredRendering.RenderPasses;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import zzueggTest.DeferredRendering.RenderPasses.Abstract.ZPass;

/**
 * @author Michael
 */
public class DepthPass extends ZPass {

    RenderManager rm;
    ViewPort vp;
    boolean isInitialized = false;

    public DepthPass(FrameBuffer deptBuffer, String techniqueName) {
        super(deptBuffer, techniqueName);
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        this.rm = rm;
        this.vp = vp;
        vp.setOutputFrameBuffer(this.getDeptBuffer());

        this.isInitialized = true;
    }

    public void reshape(ViewPort vp, int w, int h) {

    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public void preFrame(float tpf) {
        rm.setForcedTechnique(this.getTechniqueName());
    }

    public void postQueue(RenderQueue rq) {

    }

    public void postFrame(FrameBuffer out) {
        rm.setForcedTechnique(null);
    }

    public void cleanup() {

    }

}
