package mythruna.geom;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import mythruna.Vector3i;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ForceFieldMesh extends Mesh {

    private Vector3f min;
    private Vector3f max;
    private float height;
    private float depth;

    public ForceFieldMesh(Vector3f min, Vector3f max, float height, float depth) {
        this.min = min;
        this.max = max;
        this.height = height;
        this.depth = depth;

        createMesh();
    }

    public ForceFieldMesh(Vector3i min, Vector3i max, float height, float depth) {
        this(new Vector3f(min.x, min.z, min.y), new Vector3f(max.x, max.z, max.y), height, depth);
    }

    public void setCorners(Vector3f min, Vector3f max, float height, float depth) {
        this.min = min;
        this.max = max;
        this.height = height;
        this.depth = depth;

        updateGeometry();
    }

    public void setCorners(Vector3i min, Vector3i max, float height, float depth) {
        setCorners(new Vector3f(min.x, min.z, min.y), new Vector3f(max.x, max.z, max.y), height, depth);
    }

    protected void createMesh() {
        FloatBuffer pb = BufferUtils.createVector3Buffer(15);
        updatePositions(pb);
        setBuffer(VertexBuffer.Type.Position, 3, pb);

        FloatBuffer tb = BufferUtils.createVector2Buffer(15);
        updateTexCoords(tb);
        setBuffer(VertexBuffer.Type.TexCoord, 2, tb);

        IntBuffer indexes = BufferUtils.createIntBuffer(96);
        indexes.put(new int[]{0, 1, 6, 0, 6, 5, 5, 6, 11, 5, 11, 10, 1, 2, 7, 1, 7, 6, 6, 7, 12, 6, 12, 11, 2, 3, 8, 2, 8, 7, 7, 8, 13, 7, 13, 12, 3, 4, 9, 3, 9, 8, 8, 9, 14, 8, 14, 13, 0, 6, 1, 0, 5, 6, 5, 11, 6, 5, 10, 11, 1, 7, 2, 1, 6, 7, 6, 12, 7, 6, 11, 12, 2, 8, 3, 2, 7, 8, 7, 13, 8, 7, 12, 13, 3, 9, 4, 3, 8, 9, 8, 14, 9, 8, 13, 14});

        setBuffer(VertexBuffer.Type.Index, 3, indexes);

        updateBound();
    }

    public void updateGeometry() {
        FloatBuffer pb = getFloatBuffer(VertexBuffer.Type.Position);
        pb.clear();
        updatePositions(pb);

        VertexBuffer pBuff = getBuffer(VertexBuffer.Type.Position);
        pBuff.updateData(pb);

        updateBound();
    }

    protected void updatePositions(FloatBuffer pb) {
        float top = this.min.y + this.height;
        float middle = this.min.y;
        float bottom = this.min.y - this.depth;

        pb.put(this.min.x).put(top).put(this.min.z);
        pb.put(this.min.x).put(top).put(this.max.z);
        pb.put(this.max.x).put(top).put(this.max.z);
        pb.put(this.max.x).put(top).put(this.min.z);
        pb.put(this.min.x).put(top).put(this.min.z);

        pb.put(this.min.x).put(middle).put(this.min.z);
        pb.put(this.min.x).put(middle).put(this.max.z);
        pb.put(this.max.x).put(middle).put(this.max.z);
        pb.put(this.max.x).put(middle).put(this.min.z);
        pb.put(this.min.x).put(middle).put(this.min.z);

        pb.put(this.min.x).put(bottom).put(this.min.z);
        pb.put(this.min.x).put(bottom).put(this.max.z);
        pb.put(this.max.x).put(bottom).put(this.max.z);
        pb.put(this.max.x).put(bottom).put(this.min.z);
        pb.put(this.min.x).put(bottom).put(this.min.z);
    }

    protected void updateTexCoords(FloatBuffer tb) {
        tb.put(0.0F).put(1.0F);
        tb.put(0.25F).put(1.0F);
        tb.put(0.5F).put(1.0F);
        tb.put(0.75F).put(1.0F);
        tb.put(1.0F).put(1.0F);

        tb.put(0.0F).put(0.0F);
        tb.put(0.25F).put(0.0F);
        tb.put(0.5F).put(0.0F);
        tb.put(0.75F).put(0.0F);
        tb.put(1.0F).put(0.0F);

        tb.put(0.0F).put(-1.0F);
        tb.put(0.25F).put(-1.0F);
        tb.put(0.5F).put(-1.0F);
        tb.put(0.75F).put(-1.0F);
        tb.put(1.0F).put(-1.0F);
    }
}