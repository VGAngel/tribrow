/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zzueggTest.DeferredRendering.RenderPasses.Abstract;

import com.jme3.post.SceneProcessor;
import com.jme3.texture.FrameBuffer;

/**
 * @author Michael
 */
public abstract class ZPass implements SceneProcessor {

    FrameBuffer deptBuffer;
    String techniqueName;

    public ZPass(FrameBuffer deptBuffer, String techniqueName) {
        this.deptBuffer = deptBuffer;
        this.techniqueName = techniqueName;
    }

    public FrameBuffer getDeptBuffer() {
        return deptBuffer;
    }

    public void setDeptBuffer(FrameBuffer deptBuffer) {
        this.deptBuffer = deptBuffer;
    }

    public String getTechniqueName() {
        return techniqueName;
    }

    public void setTechniqueName(String techniqueName) {
        this.techniqueName = techniqueName;
    }

}
