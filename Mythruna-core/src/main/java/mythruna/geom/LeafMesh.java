package mythruna.geom;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;

public class LeafMesh extends Mesh {

    private static final BoundingBox LEAF_BOUNDS = new BoundingBox(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(32.0F, 32.0F, 32.0F));
    private BoundingBox bounds;

    public LeafMesh() {
        this(null);
    }

    public LeafMesh(BoundingBox bounds) {
        this.bounds = (bounds != null ? bounds : LEAF_BOUNDS);
    }

    public void deleteBuffers() {
        for (VertexBuffer vb : getBufferList()) {
            BufferUtils.destroyDirectBuffer(vb.getData());
        }
    }

    public LeafMesh deepClone() {
        LeafMesh clone = (LeafMesh) super.clone();

        VertexBuffer bufClone = getBuffer(VertexBuffer.Type.Color).clone();
        clone.clearBuffer(VertexBuffer.Type.Color);
        clone.setBuffer(bufClone);

        clone.updateCounts();

        return clone;
    }

    public void relight(float sun, float local) {
        if (getBuffer(VertexBuffer.Type.Size) != null) {
            return;
        }
        VertexBuffer buff = getBuffer(VertexBuffer.Type.Color);
        FloatBuffer cb = (FloatBuffer) buff.getData();

        cb.clear();

        int size = getVertexCount();

        float[] temp = new float[size * 4];
        cb.get(temp);
        cb.clear();

        int pos = 0;
        for (int i = 0; i < size; ) {
            cb.put(local);
            cb.put(temp[(pos + 1)]);
            cb.put(temp[(pos + 2)]);
            cb.put(sun);

            i++;
            pos += 4;
        }

        buff.updateData(cb);
    }

    public BoundingVolume getBound() {
        return this.bounds;
    }

    public void updateBound() {
    }
}