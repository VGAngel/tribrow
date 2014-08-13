/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.merciless.dmonkey.lights;

import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * Keeps the uniform LightPosition in synch with the world translation of the spatial
 */
public class LightLocationSynchControll extends AbstractControl {
    private final Material material;

    public LightLocationSynchControll(final Material material) {
        this.material = material;
    }

    @Override
    protected void controlUpdate(final float tpf) {
        this.material.setVector3("LightPosition", this.spatial.getWorldTranslation());
    }

    @Override
    protected void controlRender(final RenderManager rm, final ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(final Spatial spatial) {
        final Control control = new LightLocationSynchControll(((Geometry) spatial).getMaterial());
        control.setSpatial(spatial);
        return control;
    }
}
