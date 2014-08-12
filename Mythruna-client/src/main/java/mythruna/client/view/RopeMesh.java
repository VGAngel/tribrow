package mythruna.client.view;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class RopeMesh extends Mesh {
    private List<RopeLink> ropes = new ArrayList();
    private boolean needsUpdate = true;

    public RopeMesh() {
    }

    public RopeMesh(List<RopeLink> l) {
        for (RopeLink r : l)
            addRope(r);
    }

    public void addRope(RopeLink rope) {
        this.ropes.add(rope);
        rope.parent = this;
        this.needsUpdate = true;
    }

    public void updateRope(RopeLink rope) {
        this.needsUpdate = true;
    }

    public void update() {
        if (!this.needsUpdate) {
            return;
        }
        boolean updateAll = false;

        if (getVertexCount() != this.ropes.size() * 4) {
            createBuffers();
            updateAll = true;
        }

        updateBuffers(updateAll);
        this.needsUpdate = false;
    }

    protected void resizeFloatBuffer(VertexBuffer.Type type, int elementSize, int size) {
        FloatBuffer pb = ByteBuffer.allocateDirect(size * elementSize * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        VertexBuffer buffer = getBuffer(type);
        if (buffer == null) {
            setBuffer(type, elementSize, pb);
        } else {
            buffer.updateData(pb);
        }
    }

    protected void resizeShortBuffer(VertexBuffer.Type type, int elementSize, int size) {
        ShortBuffer pb = ByteBuffer.allocateDirect(size * elementSize * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        VertexBuffer buffer = getBuffer(type);
        if (buffer == null) {
            setBuffer(type, elementSize, pb);
        } else {
            buffer.updateData(pb);
        }
    }

    protected void createBuffers() {
        int size = this.ropes.size() * 4;

        resizeFloatBuffer(VertexBuffer.Type.Position, 3, size);
        resizeFloatBuffer(VertexBuffer.Type.Normal, 3, size);
        resizeFloatBuffer(VertexBuffer.Type.Color, 4, size);
        resizeFloatBuffer(VertexBuffer.Type.TexCoord, 2, size);
        resizeShortBuffer(VertexBuffer.Type.Index, 3, this.ropes.size() * 2);
    }

    protected void updateBuffers(boolean updateAll) {
        VertexBuffer vb = getBuffer(VertexBuffer.Type.Position);
        FloatBuffer verts = (FloatBuffer) vb.getData();
        verts.rewind();

        VertexBuffer nb = getBuffer(VertexBuffer.Type.Normal);
        FloatBuffer normals = (FloatBuffer) nb.getData();
        normals.rewind();

        VertexBuffer cb = getBuffer(VertexBuffer.Type.Color);
        FloatBuffer colors = (FloatBuffer) cb.getData();
        colors.rewind();

        VertexBuffer tb = getBuffer(VertexBuffer.Type.TexCoord);
        FloatBuffer texCoords = (FloatBuffer) tb.getData();
        texCoords.rewind();

        VertexBuffer ib = getBuffer(VertexBuffer.Type.Index);
        ShortBuffer index = (ShortBuffer) ib.getData();
        index.rewind();

        float thickness = 0.1F;

        int vertIndex = 0;
        for (RopeLink r : this.ropes) {
            if ((!updateAll) && (!r.changed)) {
                verts.position(verts.position() + 12);
                normals.position(normals.position() + 12);
                colors.position(colors.position() + 16);
                texCoords.position(texCoords.position() + 8);
                index.position(index.position() + 6);
            } else {
                Vector3f v1 = r.start;
                Vector3f v2 = r.end;

                Vector3f dir = v2.subtract(v1);
                float len = dir.length();
                dir.divideLocal(len);

                verts.put(v1.x).put(v1.y).put(v1.z);
                normals.put(dir.x).put(dir.y).put(dir.z);
                colors.put(0.0F).put(0.0F).put(1.0F).put(1.0F);
                texCoords.put(-0.5F).put(0.0F);

                verts.put(v1.x).put(v1.y).put(v1.z);
                normals.put(dir.x).put(dir.y).put(dir.z);
                colors.put(0.0F).put(0.0F).put(1.0F).put(1.0F);
                texCoords.put(0.5F).put(0.0F);

                verts.put(v2.x).put(v2.y).put(v2.z);
                normals.put(dir.x).put(dir.y).put(dir.z);
                colors.put(0.0F).put(0.0F).put(1.0F).put(1.0F);
                texCoords.put(0.5F).put(len / (thickness * 2.0F));

                verts.put(v2.x).put(v2.y).put(v2.z);
                normals.put(dir.x).put(dir.y).put(dir.z);
                colors.put(0.0F).put(0.0F).put(1.0F).put(1.0F);
                texCoords.put(-0.5F).put(len / (thickness * 2.0F));

                index.put((short) vertIndex).put((short) (vertIndex + 1)).put((short) (vertIndex + 2));
                index.put((short) vertIndex).put((short) (vertIndex + 2)).put((short) (vertIndex + 3));
            }

            vertIndex += 4;
        }

        vb.updateData(verts);
        nb.updateData(normals);
        cb.updateData(colors);
        tb.updateData(texCoords);
        ib.updateData(index);

        updateCounts();
        updateBound();
    }
}