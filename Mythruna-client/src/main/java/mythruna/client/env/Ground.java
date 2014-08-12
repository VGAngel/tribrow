package mythruna.client.env;

import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;

public class Ground {
    private static final float DEFAULT_RANGE = 2.0F;
    private int radials;
    private Cylinder mesh;
    private Geometry geom;

    public Ground(int radials) {
        this.radials = radials;
        this.mesh = new Cylinder(2, radials, 45.0F, 0.0F, true);
    }

    public Geometry getGeometry() {
        if (this.geom == null) {
            this.geom = new Geometry("ground dish", this.mesh);
            Quaternion rot = new Quaternion().fromAngles(1.570796F, 0.0F, 0.0F);
            this.geom.setLocalRotation(rot);

            this.geom.setQueueBucket(RenderQueue.Bucket.Sky);
            this.geom.setCullHint(Spatial.CullHint.Never);
        }
        return this.geom;
    }
}