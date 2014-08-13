/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.merciless.control;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * @author kwando
 */
public class RandomMoveControl extends AbstractControl {

    private float time;
    private Vector3f target = new Vector3f();
    private Vector3f from;
    private Vector3f pos;
    private float maxDistance = .2f;
    private float period = 1;

    public RandomMoveControl() {
        this(0.2f);
    }

    public RandomMoveControl(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    @Override
    protected void controlUpdate(float tpf) {
        time += tpf;
        if (time > period) {
            time -= period;
            randomizeTarget();
        }

        spatial.setLocalTranslation(pos.interpolate(from, target, time / period));
    }

    private void randomizeTarget() {
        pos = spatial.getLocalTranslation().clone();
        from = spatial.getLocalTranslation().clone();
        target.x = FastMath.nextRandomFloat() * maxDistance - maxDistance / 2f;
        target.y = FastMath.nextRandomFloat() * maxDistance - maxDistance / 2f + 2.5f;
        target.z = FastMath.nextRandomFloat() * maxDistance - maxDistance / 2f;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        randomizeTarget();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
