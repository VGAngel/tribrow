package mythruna.client.env;

import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Cylinder;

import java.nio.FloatBuffer;

public class Sky {
    private static final float DEFAULT_RANGE = 2.0F;
    private int radials;
    private int slices;
    private Cylinder mesh;
    private Geometry geom;
    private float[] ranges;
    private float[] texCoords;
    private float centerTexCoord;
    private boolean valid;

    public Sky(int radials, int slices) {
        this.radials = radials;
        this.slices = slices;
        this.mesh = new Cylinder(slices, radials, 50.0F, 50.0F, true, true);

        this.ranges = new float[radials];
        for (int i = 0; i < radials; i++)
            this.ranges[i] = 2.0F;
        this.texCoords = new float[radials];
        this.valid = false;
    }

    public Geometry getGeometry() {
        if (this.geom == null) {
            this.geom = new Geometry("sky cylinder", this.mesh);
            Quaternion rot = new Quaternion().fromAngles(1.570796F, 0.0F, 0.0F);
            this.geom.setLocalRotation(rot);

            this.geom.setQueueBucket(RenderQueue.Bucket.Sky);
            this.geom.setCullHint(Spatial.CullHint.Never);
        }
        if (!this.valid) {
            refreshHorizon();
            this.valid = true;
        }
        return this.geom;
    }

    public void setEastWest(float east, float west, float horizon) {
        int halfRadials = this.radials / 2;
        float range = west - east;

        for (int i = 0; i < halfRadials; i++) {
            float ratio = halfRadials - i / halfRadials;
            this.texCoords[i] = (west + (east - west) * ratio);
            this.ranges[i] = horizon;
        }
        for (int i = halfRadials; i < this.radials; i++) {
            float ratio = i - halfRadials / halfRadials;
            this.texCoords[i] = (west + (east - west) * ratio);
            this.ranges[i] = horizon;
        }

        this.centerTexCoord = ((west + east) / 2.0F);
        refreshHorizon();
    }

    protected void refreshHorizon() {
        FloatBuffer tb = this.mesh.getFloatBuffer(VertexBuffer.Type.TexCoord);
        tb.clear();

        for (int slice = 0; slice < this.slices; slice++) {
            float ratio = slice / this.slices - 1;

            for (int radial = 0; radial < this.radials; radial++) {
                float t = 1.0F - ratio * this.ranges[radial];
                if (t < 0.0F) {
                    t = 0.0F;
                }
                tb.put(this.texCoords[radial]);
                tb.put(t);
            }

            tb.put(this.texCoords[0]);

            float t = 1.0F - ratio * this.ranges[0];
            if (t < 0.0F)
                t = 0.0F;
            tb.put(t);
        }

        tb.put(this.centerTexCoord).put(1.0F);
        tb.put(this.centerTexCoord).put(0.0F);

        VertexBuffer vBuff = this.mesh.getBuffer(VertexBuffer.Type.TexCoord);
        vBuff.updateData(tb);
    }
}