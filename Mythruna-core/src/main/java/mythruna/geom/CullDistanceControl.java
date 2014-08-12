package mythruna.geom;

import com.jme3.bounding.BoundingVolume;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

public class CullDistanceControl extends AbstractControl implements Cloneable {

    private int distance;
    private int distSq;

    public CullDistanceControl(int distance) {
        this.distance = distance;
        this.distSq = (distance * distance);
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        try {
            CullDistanceControl clone = (CullDistanceControl) super.clone();
            clone.spatial = null;
            clone.setSpatial(spatial);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error cloning control", e);
        }
    }

    protected void controlUpdate(float tpf) {
        BoundingVolume bv = this.spatial.getWorldBound();
        float x = bv.getCenter().x;
        float y = bv.getCenter().z;
        float z = bv.getCenter().y;

        float d = x * x + y * y + z * z;

        if (d < this.distSq) {
            this.spatial.setCullHint(Spatial.CullHint.Dynamic);
        } else {
            this.spatial.setCullHint(Spatial.CullHint.Always);
        }
    }
}